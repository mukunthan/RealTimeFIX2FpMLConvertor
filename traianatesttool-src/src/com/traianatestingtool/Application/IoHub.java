/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Application;

import com.traianatestingtool.Transformation.TransformationEngine;
import com.traianatestingtool.Utilities.Config;
import com.traianatestingtool.Utilities.FixConstants;
import com.traianatestingtool.Utilities.NullArgumentException;
import com.traianatestingtool.Utilities.XmlUtils;
import com.traianatestingtool.processing.XmlFixConvertor;
import java.io.Closeable;
import java.util.LinkedList;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import quickfix.FieldNotFound;
import quickfix.Message;
/**
 *
 * @author mukunthant
 */
public class IoHub implements Closeable  {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IoHub.class);
    private IoSession o_RMSSessionID;
    private IoSession o_TraianaSessionID;
    
    private final XmlFixConvertor o_XmlFixConvertor;

    private  TransformationEngine o_VerifixToTraianaTransformer=null; 
    private  TransformationEngine o_TraianaToVeriiixTransformer=null;
    
    private final Config o_Config;
    
    private LinkedList<String> llst_RMSPendingMessages = new LinkedList<String>();
    private LinkedList<String> llst_TraianaPendingMessages = new LinkedList<String>();
    private final Object lock = new Object();
    private final Object lock1 = new Object();
    
   
 
    public IoHub(XmlFixConvertor xmlFixConvertor, TransformationEngine oVerifixToTraianaTransformer,TransformationEngine oTraianaToVeriiixTransformer, Config oConfig) {
        if (xmlFixConvertor == null)
                throw new NullArgumentException("xmlFixConvertor");
        if (oVerifixToTraianaTransformer == null)
                throw new NullArgumentException("VerifixToTraianaTransformer");
        if (oTraianaToVeriiixTransformer == null)
                throw new NullArgumentException("TraianaToVeriiixTransformer");
        this.o_XmlFixConvertor = xmlFixConvertor;
        this.o_VerifixToTraianaTransformer= oVerifixToTraianaTransformer;
        this.o_TraianaToVeriiixTransformer = oTraianaToVeriiixTransformer;
        this.o_Config=oConfig;
                        
    }

 
    public void setRMSHandlerSessionID(IoSession osession)
    {
        o_RMSSessionID=osession;
    }
    public void unSetRMSHandlerSessionID()
    {
        o_RMSSessionID=null;
    }
    public IoSession getRMSHandlerSessionID()
    {
        return o_RMSSessionID;
    }

    public void setTraianaHandlerSessionID(IoSession osession)
    {
        o_TraianaSessionID=osession;
    }
    public void unSetTraianaHandlerSessionID()
    {
        o_TraianaSessionID=null;
    }
    public IoSession getTraianaHandlerSessionID()
    {
        return o_TraianaSessionID;
    }
    public void close() {
        this.dropSessions();
    }
    public void dropSessions() {
         if (this.o_TraianaSessionID != null) {
            o_TraianaSessionID.close();
            this.o_TraianaSessionID = null;
         }
        if (this.o_RMSSessionID != null) {
            o_RMSSessionID.close();
            this.o_RMSSessionID = null;
       }
    }



    public void SendToRMS(Message oFixMessage)throws FieldNotFound, TransformerException, Exception {

        Document xml=o_XmlFixConvertor.toXml(oFixMessage);
        LOGGER.debug("Fix XML"+XmlUtils.xmlToString(xml));

        final DOMResult TraianadomResult = new DOMResult();
        this.o_VerifixToTraianaTransformer.transform(xml, TraianadomResult);
        LOGGER.debug("Transformed Traiana xml: {}",  XmlUtils.xmlToString((Document) TraianadomResult.getNode()));


        Message xmlNonFixMsg=o_XmlFixConvertor.getTraianaFixMessage(XmlUtils.xmlToString((Document) TraianadomResult.getNode()),oFixMessage.getHeader());
       
        LOGGER.debug("Fix message sent to RMS: {}",xmlNonFixMsg.toString());
        if (o_RMSSessionID == null) 
        {
            LOGGER.error("Unable to send to RMS. TCP session isn't opened with RMS");
        }
        else if ( o_RMSSessionID !=null)
        {
           o_RMSSessionID.write(xmlNonFixMsg.toString());
        }
    }

    public void AddToRMSPendingList(String sTraianaMessage)
    {
        synchronized (this.lock) {
            LOGGER.debug("Added to RMS pendinglist");
            this.llst_RMSPendingMessages.add(sTraianaMessage);
        }
    }
    public void AddToTraianaPendingList(String sRMSMessage)
    {
        synchronized (this.lock1) {
            LOGGER.debug("Added to Traianapendinglist");
            this.llst_TraianaPendingMessages.add(sRMSMessage);
        }
    }

    public String GetFromRMSPendingList()
    {
        if(this.llst_RMSPendingMessages == null) 
        {
            return null;
        }
           synchronized (this.lock) {
           String sTraianaFIXMessage = IoHub.this.llst_RMSPendingMessages.pollFirst();
           return sTraianaFIXMessage;
        }
    }
    public String GetFromTraianaPendingList()
    {
        if(this.llst_TraianaPendingMessages == null) 
        {
            return null;
        }
           synchronized (this.lock1) {
           String sRMSFIXMessage = IoHub.this.llst_TraianaPendingMessages.pollFirst();
           return sRMSFIXMessage;
        }
    }
    public void CreateTraianaMessage(Message oFixMessage) throws Exception {
        Document xml=o_XmlFixConvertor.toXml(oFixMessage);
        LOGGER.debug("Fix XML"+XmlUtils.xmlToString(xml));

        final DOMResult oTraianadomResult = new DOMResult();
        this.o_VerifixToTraianaTransformer.transform(xml, oTraianadomResult);
        LOGGER.debug("Transformed Traiana xml: {}"+ XmlUtils.xmlToString((Document) oTraianadomResult.getNode()));

        Message oXmlNonFixMsg=o_XmlFixConvertor.getTraianaFixMessage(XmlUtils.xmlToString((Document)oTraianadomResult.getNode()),oFixMessage.getHeader());

        LOGGER.debug("XmlNonFix meesgae sent to Traiana {}",oXmlNonFixMsg.toString());
        sendToTraiana(oXmlNonFixMsg);
       

    }

    public void sendToTraiana(Message oFixMessage) throws Exception
    {
        
        if(oFixMessage.getHeader().isSetField(FixConstants.TRAIANA_XMLCONTENT_FIELD_TAG))
        {
            String sXmlContent=oFixMessage.getHeader().getString(FixConstants.TRAIANA_XMLCONTENT_FIELD_TAG);
            Document xmlcontent=XmlUtils.convertStringToDocument(sXmlContent);

            LOGGER.debug("Traiana Messge XMLcontent {}",XmlUtils.xmlToString(xmlcontent));
            Element documentElement = xmlcontent.getDocumentElement();
            String sMessageName = documentElement.getNodeName();
            String sMsgType=o_XmlFixConvertor.getMessageTypeforXML(sMessageName);
            LOGGER.debug("Relevant Messge name: {} Message Type: {}",sMessageName,sMsgType);
            if(o_Config.IsValidConvertableFixMessageforRMS(sMsgType))
            {
               //Remove Fist node attributes 
               // Document ExtractedXml=XmlUtils.removeParentNodeAttributes(xmlcontent);
               final DOMResult FixdomResult = new DOMResult();
               this.o_TraianaToVeriiixTransformer.transform(xmlcontent, FixdomResult);

               LOGGER.debug("Transformed Fix xml: {}",XmlUtils.xmlToString((Document) FixdomResult .getNode()));

               Message oFix=o_XmlFixConvertor.toFix((Document) FixdomResult .getNode(),oFixMessage.getHeader());
               
               if(oFix !=null)
               {
                 LOGGER.debug("Fim message sent to Traiana {}",oFix.toString());
                 o_TraianaSessionID.write(oFix.toString());
               }
               else
               {
                   o_TraianaSessionID.write(oFixMessage.toString());
                   LOGGER.error("Null Converted Fix. Original passed throuh,{}",sMsgType);
               }
            }
            else
            {
                LOGGER.debug("Messages PAssed through to Traiana,{}",sMsgType);
                o_TraianaSessionID.write(oFixMessage.toString());
            }
        }
        else
        {
             LOGGER.debug("MEssgae don't have XML tag;Passed through");
             o_TraianaSessionID.write(oFixMessage.toString());
        }
    }  
    
 
}
