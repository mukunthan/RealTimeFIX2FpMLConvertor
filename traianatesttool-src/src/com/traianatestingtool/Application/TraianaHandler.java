/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Application;


/**
 *
 * @author mukunthant
 */

import com.traianatestingtool.Utilities.Config;
import com.traianatestingtool.Utilities.FixConstants;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.InvalidMessage;

import quickfix.Message;
import quickfix.MessageUtils;

public class TraianaHandler extends IoHandlerAdapter {
    IoHub  o_IoHub;
    boolean b_LogonSent=false;
    boolean b_LogonReceived=false;
    boolean b_Logoutsent=false;
    Config o_Config;
    boolean bIsAdminMessage=false;
    DataDictionary o_AppDictionary=null;
    DataDictionary o_SessionDictionary=null;
    Boolean b_IsConnectionCreated=false;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraianaHandler.class);
    private static final Logger INTERFACELOGGER = LoggerFactory.getLogger("Interface");
    
    public TraianaHandler(IoHub oIohub, Config oConfig) throws ConfigError
    {
        this.o_IoHub=oIohub;
        this.o_Config=oConfig;
        o_AppDictionary = new DataDictionary(o_Config.getFixApplicationSpecificationPath());
        o_SessionDictionary = new DataDictionary(o_Config.getFixTransportSpecificationPath());
        
    }

 public void exceptionCaught(IoSession session, Throwable t) throws Exception {
     LOGGER.warn(t.toString());
     System.out.println(t.toString());
     o_IoHub.dropSessions();
     b_LogonReceived=false;
     b_LogonSent=false;
     b_Logoutsent=false;
 }

 public void messageReceived(IoSession osession, Object msg)  
 {
     String sMsg = msg.toString();
     INTERFACELOGGER.info("Messgae: {} Recevived from TraianaSession: {}",sMsg,osession.toString());
     try{
        String sMsgType=MessageUtils.getMessageType(sMsg);
         LOGGER.info("Message{} received from{}",sMsgType,osession.toString());
        bIsAdminMessage=MessageUtils.isAdminMessage(sMsgType);


        if(bIsAdminMessage)
        {

               if((FixConstants.LOGON_MSG_FIX_TAG).equals(sMsgType)  && o_IoHub.getRMSHandlerSessionID() !=null)
               {
                    o_IoHub.getRMSHandlerSessionID().write(sMsg);
                    while(true)
                   {
                       String sPendinMsg=o_IoHub.GetFromTraianaPendingList();
                       if(sPendinMsg ==null)
                       {
                           LOGGER.debug("No Messages in Buffer");
                           break;
                       }
                       else
                       {
                           String sPendingMsgType=MessageUtils.getMessageType(sPendinMsg);
                           boolean bIsAdminPendingMsg=MessageUtils.isAdminMessage(sPendingMsgType);
                           if(bIsAdminPendingMsg)
                           {
                               o_IoHub.getRMSHandlerSessionID().write(sPendinMsg);
                           }
                           else
                           {
                               if(o_Config.IsValidConvertableFixMessageforTraiana(sPendingMsgType)) 
                               {
                                   Message oFIXMessage = new Message();
                                   oFIXMessage.fromString(sPendinMsg, o_SessionDictionary, o_AppDictionary, true);
                                   o_IoHub.SendToRMS(oFIXMessage);
                               }
                               else
                               {
                                   o_IoHub.getRMSHandlerSessionID().write(sPendinMsg);
                               }

                           }
                       }
                   }
                    b_LogonSent=true;
               }
               else if(b_LogonReceived &&(FixConstants.LOGOUT_MSG_FIX_TAG).equals(sMsgType) && o_IoHub.getRMSHandlerSessionID()!=null )
               {
                   LOGGER.debug("Logout Received");
                   o_IoHub.getRMSHandlerSessionID().write(sMsg);
                   b_Logoutsent=true;
                   osession.close();

               }
               else if (b_LogonSent &&b_LogonReceived &&o_IoHub.getRMSHandlerSessionID() !=null)
               {
                   o_IoHub.getRMSHandlerSessionID().write(sMsg);
               }
               else
               {

                   o_IoHub.AddToTraianaPendingList(sMsg);
               }

        }
        else
        {
             if(!o_Config.IsValidConvertableFixMessageforTraiana(sMsgType) && b_LogonSent)
             {
                 LOGGER.debug("Message are paased through for RMS,{}",sMsgType);
                o_IoHub.getRMSHandlerSessionID().write(sMsg);
             }

            else
            {
               Message oFIXMessage = new Message();
               oFIXMessage.fromString(sMsg, o_SessionDictionary,o_AppDictionary, true); 
               if(o_Config.IsMessageValidation())
               {
                   try
                   {
                       o_AppDictionary.validate(oFIXMessage,true);
                       if (b_LogonSent &&b_LogonReceived &&o_IoHub.getRMSHandlerSessionID() !=null)
                       {
                           LOGGER.debug("Valid Message for RMS,{}",sMsgType);
                           o_IoHub.SendToRMS(oFIXMessage);
                       }
                       else
                       {
                           LOGGER.debug("Valid Message for RMS added to pending list,{}",sMsgType);
                          o_IoHub.AddToTraianaPendingList(sMsg);
                       }
                   }
                   catch (Exception e)
                   {
                       o_IoHub.getRMSHandlerSessionID().write(sMsg);
                       LOGGER.error("Validation Error"+e.toString());
                   }
               }
               else
               {
                   if (b_LogonSent &&b_LogonReceived &&o_IoHub.getRMSHandlerSessionID() !=null)
                   {
                       o_IoHub.SendToRMS(oFIXMessage);
                        LOGGER.debug("App msgs are sent to RMS,{}",sMsgType);
                   }
                   else
                   {
                      LOGGER.debug("App messages are Added to Buffer,{}",sMsgType);
                      o_IoHub.AddToTraianaPendingList(sMsg);
                   }
               }
            }
        }
     }
     catch (Exception e)
     {
         o_IoHub.getRMSHandlerSessionID().write(sMsg);
         LOGGER.debug("Exception occured{}; Message passed through, {}",e.toString());
     }

 } 
 
 public void messageSent(IoSession osession, Object oMessage) {
        try 
        {
            String sMsg = oMessage.toString();
            String sMsgType=MessageUtils.getMessageType(sMsg);
            bIsAdminMessage=MessageUtils.isAdminMessage(sMsgType);
            INTERFACELOGGER.info("Messgae: {} Sent to TraianaSession:{}",sMsg,osession.toString());
            if(bIsAdminMessage)
            {
                if((FixConstants.LOGON_MSG_FIX_TAG).equals(sMsgType))
                {
                    b_LogonReceived=true;
                }
                else if(b_LogonSent && (FixConstants.LOGOUT_MSG_FIX_TAG).equals(sMsgType) )
                {
                    LOGGER.debug("Logout sent");
                    
                }
            }  
        } 
        catch (InvalidMessage ex) {
            LOGGER.error("Error msg type sent");
        }

 }
 
 

 public void sessionCreated(IoSession session) throws Exception {
    LOGGER.debug("Traiana Session created..."+ session);
    if( session.getTransportType() == TransportType.SOCKET )
    {
       ((SocketSessionConfig) session.getConfig() ).setReceiveBufferSize( 2048 );
    }
     o_IoHub.setTraianaHandlerSessionID(session);
     b_IsConnectionCreated=true;
     b_LogonReceived=false;
     b_LogonSent=false;
     b_Logoutsent=false;
 }
 
 public void sessionClosed(IoSession session) throws Exception {
    LOGGER.debug("Traiana Session closed..."+ session);
    if(b_Logoutsent==true)
    {
        o_IoHub.unSetTraianaHandlerSessionID();
    }
    else
    {
        LOGGER.debug("Session close without logout");
        o_IoHub.dropSessions();
    }
    b_LogonReceived=false;
    b_LogonSent=false;
    b_Logoutsent=false;
        
 }
 
 public void sessionIdle(IoSession oSession, IdleStatus status) throws Exception {
      LOGGER.warn("Timeout in traiana");
  }
 
 
 
 
 
}
