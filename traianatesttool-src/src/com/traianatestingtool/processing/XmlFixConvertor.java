/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.processing;

/**
 *
 * @author mukunthant
 */

import com.traianatestingtool.Utilities.FixConstants;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import quickfix.DataDictionary;
import quickfix.DataDictionary.GroupInfo;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.Message;
import quickfix.field.BeginString;
import quickfix.field.MsgType;

import com.traianatestingtool.Utilities.NullArgumentException;
import com.traianatestingtool.Utilities.TimeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message.Header;

public class XmlFixConvertor {

	private DataDictionary o_TransportDictionary;

	private DataDictionary o_ApplicationDictionary;

	private Map<String, Integer> mp_mangledTransportFields;

	private Map<String, Integer> mp_mangledApplicationFields;
	
	private final DocumentBuilderFactory o_DocumentBuilderFactory;
        private static final Logger LOGGER = LoggerFactory.getLogger(XmlFixConvertor.class);
        
     //   private static String sTimeZoneOffset=null;

	public XmlFixConvertor(DataDictionary oTransportDictionary,DataDictionary oApplicationDictionary) {
            if (oTransportDictionary == null)
                    throw new NullArgumentException("transportDictionary");
            if (oApplicationDictionary == null)
                    throw new NullArgumentException("applicationDictionary");
            this.o_TransportDictionary = oTransportDictionary;
            this.o_ApplicationDictionary = oApplicationDictionary;
            this.mp_mangledTransportFields = createMangledFieldsDictionary(oTransportDictionary);
            this.mp_mangledApplicationFields = (oTransportDictionary == oApplicationDictionary) ? this.mp_mangledTransportFields: createMangledFieldsDictionary(oApplicationDictionary);
            this.o_DocumentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	private static Map<String, Integer> createMangledFieldsDictionary(DataDictionary oSourceDictionary) 
        {
            Map<String, Integer> mpmagledFieldNames = new Hashtable<String, Integer>();
            for (int iTag : oSourceDictionary.getOrderedFields()) {
                    String sFieldName = oSourceDictionary.getFieldName(iTag);
                    String sGroupName = getGroupName(sFieldName);
                    if (!sFieldName.equals(sGroupName)) {
                            mpmagledFieldNames.put(sGroupName, iTag);
                            LOGGER.trace("Group-tag :{}-{}",sGroupName,iTag);
                    }
            }
            return mpmagledFieldNames;
	}
        
        private static String getGroupName(final String sGroupFieldName) {
            String sGroupName = sGroupFieldName;
            if (sGroupName.startsWith("No") && (sGroupName.length() > 2))
                    sGroupName = sGroupName.substring(2);
            if (sGroupName.endsWith("ies") && (sGroupName.length() > 3))
                    sGroupName = sGroupName.substring(0, sGroupName.length() - 3) + 'y';
            else if (sGroupName.endsWith("s") && (sGroupName.length() > 1))
                    sGroupName = sGroupName.substring(0, sGroupName.length() - 1);
            return sGroupName;
	}

	public Document toXml(Message oFixMessage) throws Exception {
            Document oDocument;
           
            oDocument = this.o_DocumentBuilderFactory.newDocumentBuilder().newDocument();
            if (oFixMessage == null)
            {
                    LOGGER.warn("NULL Fix message");
                    throw new NullArgumentException("fixMessage");
            }

            else
            {
                String sMsgType = getMsgType(oFixMessage);
              /*  if("LA".equals(sMsgType))
                {
                    if(oFixMessage.isSetField(71167))
                    {
                        sTimeZoneOffset=oFixMessage.getString(71167);
                    }
                } */
                String sMessageName = getMessageName(this.o_TransportDictionary.isAdminMessage(sMsgType) ? this.o_TransportDictionary
                                                : this.o_ApplicationDictionary, sMsgType);

                if(sMessageName !=null)
                {
                    Element messageElement = oDocument.createElement(sMessageName);
                    oDocument.appendChild(messageElement);
                    Element headerElement = oDocument.createElement("Header");
                    messageElement.appendChild(headerElement);
                    append(headerElement, oFixMessage.getHeader(), o_TransportDictionary, DataDictionary.HEADER_ID);

                    append(messageElement, oFixMessage, o_ApplicationDictionary, sMsgType);

                    Element trailerElement = oDocument.createElement("Trailer");
                    messageElement.appendChild(trailerElement);
                    append(trailerElement, oFixMessage.getTrailer(), o_TransportDictionary, DataDictionary.TRAILER_ID);
                }
                else{
                    LOGGER.warn("NULL Message type");
                    throw new NullArgumentException("Msg Type");
                }
            }
            return oDocument; 

                
       }
        private static void append(Element parentElement, FieldMap fields,DataDictionary oDictionary, String sMsgType) {
            append(parentElement, fields, oDictionary, oDictionary, sMsgType);
	}

	private static void append(Element parentElement, FieldMap fields,DataDictionary oMessageDictionary, DataDictionary oFieldsDictionary, String sMsgType) 
        {
            final Document document = parentElement.getOwnerDocument();
            for (Iterator<Field<?>> it = fields.iterator(); it.hasNext();) {
                    final Field<?> field = it.next();
                    final String sFieldName = oFieldsDictionary.getFieldName(field.getTag());
                    if (sFieldName == null) {
                        LOGGER.warn("Null field name");
                    }
                    
                    else 
                    {
                        final Element element = document.createElement(sFieldName);
                        parentElement.appendChild(element);
                        if(IsTimeField(sFieldName))
                        {
                            element.setTextContent(TimeUtilities.ConvertUTCFormatToTraianaFormat(field.getObject().toString()));
                          /*  if(IsTimeCustomarizationRequired(sMsgType,field.getTag()))
                            {
                                System.out.println("Pass");
                                String sCustomTiemField= CustmarizeTimeField(element.getTextContent());
                                System.out.println("St"+sCustomTiemField);
                                element.setTextContent(sCustomTiemField);
                            } */
    
                        }
                        else
                        {
                            element.setTextContent(field.getObject().toString());
                        }
                        if(oFieldsDictionary.isGroup(sMsgType, field.getTag()))
                        {
                            final int iGroupTag = field.getTag();
                            final GroupInfo groupInfo = oMessageDictionary.getGroup(sMsgType, iGroupTag);
                            if  (groupInfo == null) {
                                LOGGER.warn("Null Groupinfo for Grouptag");
                            } 
                            else 
                            {
                                    final String sGroupName = getGroupName(sFieldName);
                                    final DataDictionary oGroupDictionary = groupInfo.getDataDictionary();
                                    final Element grpelement = document.createElement(sGroupName);
                                    parentElement.appendChild(grpelement);
                                    for (Group oGroup : fields.getGroups(iGroupTag)) 
                                    {
                                        final Element rptgrpelement = document.createElement("group");
                                        grpelement.appendChild(rptgrpelement);
                                        append(rptgrpelement, oGroup, oGroupDictionary, oFieldsDictionary, sMsgType);

                                    }
                            } 
                        }
                    }


            }
		 
	}
        
      /*  private static boolean IsTimeCustomarizationRequired(String sMsgType, int iFieldName)
        {
            if("LA".equals(sMsgType) && iFieldName ==71139)
            {
                return true;
            }
            return false;
        } */
        
      /*  private static String CustmarizeTimeField(String sFieldValue)
        {
           StringBuilder sbTimefield = new StringBuilder();
           sbTimefield.append(sFieldValue);
          // String sOffSet=TimeUtilities.GetTimeZoneOffset();
           //sbTimefield.append(sOffSet);
           sbTimefield.append(sTimeZoneOffset);
           return sbTimefield.toString();
        } */
        
        private static String getMsgType(Message oFixMessage) throws FieldNotFound {
		return oFixMessage.getHeader().getString(MsgType.FIELD);
	}

	private static String getMessageName(DataDictionary oDataDictionary, String sMsgType) {
		return oDataDictionary.getValueName(MsgType.FIELD, sMsgType);
	}
        
        private static boolean IsTimeField(String sFieldName)
        {
            if(sFieldName.length() >4)
            {
                if("Time".equals(sFieldName.substring(sFieldName.length()-4, sFieldName.length())))
                {
                    return true;
                }
                
            }
            return false;
        }

	public Message toFix(Document xmlDocument, Header oHeader) {
		Message oMessage = new Message();
                int iGrpDelimTag;
		Element documentElement = xmlDocument.getDocumentElement();
                if(documentElement !=null)
                {
                    String sMessageName = documentElement.getNodeName();
                    String sMsgType = this.o_ApplicationDictionary.getMsgType(sMessageName);
                    if (sMsgType == null) {
                        sMsgType = this.o_TransportDictionary.getMsgType(sMessageName);
                        if (sMsgType == null)
                        {
                            LOGGER.warn("Message type is null");
                            return null; 
                        }
                    }
                    DataDictionary oMessageDictionary;
                    Map<String, Integer> mpMangledFields;
                    if (this.o_TransportDictionary.isAdminMessage(sMsgType)) 
                    {
                            oMessageDictionary = this.o_TransportDictionary;
                            mpMangledFields = this.mp_mangledTransportFields;
                    } else {
                            oMessageDictionary = this.o_ApplicationDictionary;
                            mpMangledFields = this.mp_mangledApplicationFields;
                    }

                    oMessage.getHeader().setFields(oHeader);
                    FieldMap header = oMessage.getHeader();
                    header.setString(BeginString.FIELD, this.o_TransportDictionary.getVersion());
                    header.setString(MsgType.FIELD, sMsgType);
                    header.removeField(FixConstants.TRAIANA_XMLCONTENT_FIELD_TAG);
                    header.removeField(FixConstants.TRAIANA_XMLLENGTH_FIELD_TAG);



                    boolean bHeaderProcessed = false;
                    boolean bTrailerProcessed = false;

                    final NodeList childNodes = documentElement.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); ++i) {
                            final Node node = childNodes.item(i);
                            if (node.getNodeType() != Node.ELEMENT_NODE)
                            {
                                LOGGER.trace("Element Node is skipped");
                                continue;
                            }
                            if(node.getTextContent().trim() ==null ||node.getTextContent().trim()=="")
                            {
                                LOGGER.trace("Null/Empty tag");
                                continue;
                            }
                            final String sNodeName = node.getNodeName();

                            if ("Header".equals(sNodeName)) 
                            {
                                    if (bHeaderProcessed)
                                    {
                                        LOGGER.warn("Header is already Processed");
                                    }
                                    bHeaderProcessed = true;

                                    processNode(header, node, this.o_TransportDictionary, this.o_TransportDictionary,this.mp_mangledTransportFields, sMsgType);
                            } else if ("Trailer".equals(sNodeName)) {
                                    if (bTrailerProcessed)
                                    {
                                        LOGGER.warn("Trail is already Processed");
                                        ;
                                    }
                                           
                                    bTrailerProcessed = true;

                                    processNode(oMessage.getTrailer(), node, this.o_TransportDictionary, this.o_TransportDictionary,this.mp_mangledTransportFields, sMsgType);
                            } else {
                                    int iTag = oMessageDictionary.getFieldTag(sNodeName);
                                    if (iTag == -1) {
                                            if (mpMangledFields.containsKey(sNodeName)) 
                                            {
                                                    int iGroupTag = mpMangledFields.get(sNodeName);
                                                    GroupInfo groupInfo = oMessageDictionary.getGroup(sMsgType, iGroupTag);
                                                    if (groupInfo == null) {

                                                           LOGGER.warn("Groupinfo is null");

                                                    } else {
                                                            DataDictionary oGroupDictionary = groupInfo.getDataDictionary();

                                                            iGrpDelimTag=groupInfo.getDelimeterField();

                                                            extractRepeatingGroups(iGroupTag,oGroupDictionary, oMessage, node, oMessageDictionary, oGroupDictionary, mpMangledFields,
                                                                            sMsgType,iGrpDelimTag);  

                                                    }
                                            }
                                            else
                                            {
                                                LOGGER.warn("Group tag not in mangledMap");
                                            }
                                    } 
                                    else 
                                    {
                                       /* if(IsTimeField(sNodeName))
                                        {
                                            oMessage.setString(iTag,TimeUtilities.ConvertTraianaFormatToUTCFormat(node.getTextContent().trim()).trim());
                                        } */
                                        if(node.getTextContent().trim() ==null ||node.getTextContent().trim()=="")
                                        {
                                            LOGGER.trace("Null/Empty tag");
                                            continue;
                                        }
                                        else
                                        {
                                            oMessage.setString(iTag, node.getTextContent().trim());
                                            LOGGER.trace("Field {} is attached to {}",node.getTextContent().trim(),iTag);
                                        }
                                    }
                            }
                    }
                    
                    // CustomarizeFixMessage(oMessage ,sMsgType);
                        
                    
                    return oMessage;
                }
                else
                {
                    LOGGER.warn("Document is Null");
                    return null;
                }
           
		
	}
        
     /*   private void CustomarizeFixMessage(Message oMessage, String sMsgType)
        {
            if("RL".equals(sMsgType))
            {
                try
                {
                    String sOrderId=oMessage.getString(71124);
                    String[] OrderIDParts = sOrderId.split("_");
                    oMessage.setString(71166, OrderIDParts[0]);
                }
                catch (Exception e)
                {
                    LOGGER.error("Errorr in Extracting field");
                }
            }
           
        } */

        private static void extractRepeatingGroups(int iGrptag,DataDictionary oGrpDictionary, FieldMap message, Node domNode, DataDictionary oMessageDictionary,
			DataDictionary oFieldsDictionary, Map<String, Integer> mpMangledFields, String sMsgType, int iGrpDelimTag)
        {
                Node oNotRptNode = domNode.cloneNode(true);
                Node oRptNode = clearNode(domNode.cloneNode(true));
                final NodeList childNodes = domNode.getChildNodes();
               
                int iDelimcount=0;
                int iOriginChildlenght=childNodes.getLength();
                for (int i = 0; i < iOriginChildlenght; ++i) 
                {
                    final Node node = childNodes.item(0);
                    
                    final String sNodeName = node.getNodeName();
                    int iTag = oMessageDictionary.getFieldTag(sNodeName);
                    if(iTag==iGrpDelimTag)
                    {
                        iDelimcount++;
                        if(iDelimcount >1)
                        {
                            Group group = new Group(iGrptag, iGrpDelimTag,oGrpDictionary.getOrderedFields());
                            processNode(group, oRptNode, oMessageDictionary,oFieldsDictionary, mpMangledFields, sMsgType);
                            message.addGroupRef(group);
                            oRptNode = clearNode(domNode.cloneNode(true));
                        }
                        
                    }
                    oRptNode.appendChild(node);
                    if(i==iOriginChildlenght-1 && iDelimcount >1)
                    {
                        Group group = new Group(iGrptag, iGrpDelimTag,oGrpDictionary.getOrderedFields());
                        processNode(group, oRptNode, oMessageDictionary,oFieldsDictionary, mpMangledFields, sMsgType);
                        message.addGroupRef(group);
                    }
                }
                
                if(iDelimcount ==1)
                {
                    Group group = new Group(iGrptag, iGrpDelimTag,oGrpDictionary.getOrderedFields());
                    processNode(group, oNotRptNode, oMessageDictionary,oFieldsDictionary, mpMangledFields, sMsgType);
                    message.addGroupRef(group);
                }
           
        } 
        
        private static Node clearNode(Node node)
        {
            final NodeList childNodes = node.getChildNodes();
            int iOriginNodeLength=childNodes.getLength();
                for (int i = 0; i < iOriginNodeLength; ++i) {
                    final Node oChildnode = childNodes.item(0);
                    node.removeChild(oChildnode);
                    
                }
            return node;
        }
        
	private static void processNode(FieldMap fields, Node domNode, DataDictionary oMessageDictionary,
			DataDictionary oFieldsDictionary, Map<String, Integer> mpMangledFields, String sMsgType) {
            
		final NodeList childNodes = domNode.getChildNodes();
                
		for (int i = 0; i < childNodes.getLength(); ++i) {
			final Node node = childNodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
                        {
                            LOGGER.trace("Element Type node is skipped");
                            continue;
                        }
			final String sNodeName = node.getNodeName();
			int iTag = oMessageDictionary.getFieldTag(sNodeName);
                        
			if (iTag == -1) {
				if (mpMangledFields.containsKey(sNodeName)) {
					int iGroupTag = mpMangledFields.get(sNodeName);
					GroupInfo groupInfo = oMessageDictionary.getGroup(sMsgType, iGroupTag);
					if (groupInfo == null) {
                                            
						LOGGER.warn("Group info is Null");
					} else 
                                        {
						DataDictionary oGroupDictionary = groupInfo.getDataDictionary();
                                                int iGrpDelimTag=groupInfo.getDelimeterField();
						extractRepeatingGroups(iGroupTag,oGroupDictionary,fields, node, oMessageDictionary, oGroupDictionary, mpMangledFields,
								sMsgType,iGrpDelimTag);
					}
				} 
                                else
					LOGGER.warn("Group tag is not defined in MangledMap");
			} else 
                        {
                          /*  if(IsTimeField(sNodeName))
                            {
                                fields.setString(iTag,TimeUtilities.ConvertTraianaFormatToUTCFormat(node.getTextContent().trim()).trim());
                            } */
                            if(node.getTextContent().trim() ==null ||node.getTextContent().trim()=="")
                            {
                                LOGGER.trace("Null/Empty tag");
                                continue;
                            }
                            else
                            {
                                fields.setString(iTag, node.getTextContent().trim()); 
                            }
			}
		}
	}

	

        public Message getTraianaFixMessage(String sXmlContent, Header oHeader)
        {
            Message oFixMessage=new Message();
            oFixMessage.getHeader().setFields(oHeader);
            oFixMessage.getHeader().setString(FixConstants.MSG_FIX_TAG, FixConstants.TRAIANA_MSG_FIX_TAG);
            oFixMessage.getHeader().setString(FixConstants.TRAIANA_XMLCONTENT_FIELD_TAG, sXmlContent);
            int iXmlLength=sXmlContent.length();
            oFixMessage.getHeader().setInt(FixConstants.TRAIANA_XMLLENGTH_FIELD_TAG, iXmlLength);
             
            return oFixMessage;
        }
        
        public String getMessageTypeforXML(String sMessageName)
        {
            return  this.o_ApplicationDictionary.getMsgType(Character.toUpperCase(sMessageName.charAt(0)) + sMessageName.substring(1));
        }
	

	
}

