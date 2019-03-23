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


public class RMSHandler extends IoHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMSHandler.class);
    private static final Logger INTERFACELOGGER = LoggerFactory.getLogger("Interface");
    IoHub o_IoHub;
    Config o_Config;
    boolean b_LogonSent=false;
    boolean b_LogonReceived=false;
    boolean b_Logoutsent=false;
    TraianaClient o_Traianaclient;
    boolean bIsAdminMessage=false;
    DataDictionary o_AppDictionary=null;
    DataDictionary o_SessionDictionary=null;
    
   public RMSHandler(IoHub oIohub, Config oConfig) throws ConfigError
    {
         this.o_IoHub=oIohub;
         this.o_Config=oConfig;
         o_Traianaclient=new TraianaClient(o_IoHub,o_Config);
         o_AppDictionary = new DataDictionary(o_Config.getFixApplicationSpecificationPath());
         o_SessionDictionary = new DataDictionary(o_Config.getFixTransportSpecificationPath());
         
    }
  public void exceptionCaught(IoSession osession, Throwable t) throws Exception {
        LOGGER.warn(t.toString());
        o_IoHub.dropSessions();
        b_LogonReceived=false;
        b_LogonSent=false;
        b_Logoutsent=false;
  }

 public void messageReceived(IoSession osession, Object oMsg) 
 {    
     String sMsg = oMsg.toString();
     INTERFACELOGGER.info("Messgae: {} Recevived from RMSSession: {}",sMsg,osession.toString());
     try 
     {
        String sMsgType= MessageUtils.getMessageType(sMsg);
        LOGGER.info("Message{} received from{}",sMsgType,osession.toString());
        bIsAdminMessage=MessageUtils.isAdminMessage(sMsgType);

        if(bIsAdminMessage)
        {
               //Messages will be stored untl Sessioncreated method returns
                if((FixConstants.LOGON_MSG_FIX_TAG).equals(sMsgType) && o_IoHub.getTraianaHandlerSessionID() !=null)
                {
                    LOGGER.debug("Logon Received");
                    o_IoHub.getTraianaHandlerSessionID().write(sMsg);
                    b_LogonSent=true;
                }

                else if(b_LogonSent && (FixConstants.LOGOUT_MSG_FIX_TAG).equals(sMsgType) && o_IoHub.getTraianaHandlerSessionID() !=null )
                {
                   LOGGER.debug("Logout received");
                   o_IoHub.getTraianaHandlerSessionID().write(sMsg);

                }

               else if (b_LogonSent &&b_LogonReceived && o_IoHub.getTraianaHandlerSessionID() !=null)
               {
                   LOGGER.debug("Admn Msgs are passed through,{}",sMsgType);
                   o_IoHub.getTraianaHandlerSessionID().write(sMsg);
               }

               else if( b_LogonSent)
               {
                   o_IoHub.AddToRMSPendingList(sMsg);
                   LOGGER.debug("Admin Msg are added to Buffer,{}",sMsgType);
               }

        }

        else
        {

                if(o_Config.IsTestingModeForRMs() && o_Config.IsValidConvertableFixMessageforRMS(sMsgType))
                {
                    Message oFIXMessage = new Message();
                    oFIXMessage.fromString(sMsg, o_SessionDictionary, o_AppDictionary, true);
                    if(o_Config.IsMessageValidation())
                    {
                       try
                       {
                           o_AppDictionary.validate(oFIXMessage,true);
                           if (b_LogonSent &&b_LogonReceived)  //TEST mode
                           {
                               LOGGER.debug("Valid Message for Traina Converion in Test Mode,{}",sMsgType);
                               o_IoHub.CreateTraianaMessage(oFIXMessage);
                           }
                           else if(b_LogonSent)
                           {
                               LOGGER.debug("Valid Message for Traina Converion in Test Mode added tp pending, {}",sMsgType);
                               o_IoHub.AddToRMSPendingList(sMsg);
                           }
                       }

                       catch ( Exception e)
                       {
                            LOGGER.error("Error"+e.toString());
                            o_IoHub.getTraianaHandlerSessionID().write(sMsg);
                       } 
                    }
                    else
                    {
                        if (b_LogonSent &&b_LogonReceived) 
                           {
                               LOGGER.debug("Valid Message for Traina Converion in Test Mode without config alidation,{}",sMsgType);
                               o_IoHub.CreateTraianaMessage(oFIXMessage);
                           }
                           else if(b_LogonSent)
                           {
                               LOGGER.debug("Valid Message for Traina Converion in Test Mode without config alidation added to pending list,{}",sMsgType);
                               o_IoHub.AddToRMSPendingList(sMsg);
                           }
                    }
               }


               else
               {

                  if(FixConstants.TRAIANA_MSG_FIX_TAG.equals(sMsgType) && b_LogonSent &&b_LogonReceived )
                  {
                      LOGGER.debug("Traiana message Receied,{}",sMsgType);
                      Message oFIXMessage = new Message();
                      oFIXMessage.fromString(sMsg, o_SessionDictionary, o_AppDictionary, true);
                      o_IoHub.sendToTraiana(oFIXMessage);
                  }
                  else if(b_LogonSent &&b_LogonReceived)
                  {
                     LOGGER.debug("Application msgs are passed through,{}",sMsgType);
                     o_IoHub.getTraianaHandlerSessionID().write(sMsg);   
                  }
                  else if(b_LogonSent)
                  {
                     LOGGER.debug("App messages are Added to Buffer,{}",sMsgType);
                     o_IoHub.AddToRMSPendingList(sMsg);
                  }

              }


        }
     }
     catch (Exception e)
     {
         o_IoHub.getTraianaHandlerSessionID().write(sMsg);
         LOGGER.debug("Exception occured{}; Message passed through, {}",e.toString());
     }
     
   
 } 

 public void messageSent(IoSession osession, Object oMessage)  {
     String sMsg = oMessage.toString();
     String sMsgType;
     try 
     {
        sMsgType = MessageUtils.getMessageType(sMsg);
        bIsAdminMessage=MessageUtils.isAdminMessage(sMsgType);
     
        if(bIsAdminMessage)
        {
               if((FixConstants.LOGON_MSG_FIX_TAG).equals(sMsgType))
               {
                   while(true)
                   {
                       String sPendinMsg=o_IoHub.GetFromRMSPendingList();
                       if(sPendinMsg ==null)
                       {
                           LOGGER.debug("No meeage in buffer");
                           break;
                       }
                       else
                       {
                           String sPendingMsgType=MessageUtils.getMessageType(sPendinMsg);
                           boolean bIsAdminPendingMsg=MessageUtils.isAdminMessage(sPendingMsgType);
                           if(bIsAdminPendingMsg)
                           {
                               o_IoHub.getTraianaHandlerSessionID().write(sPendinMsg);
                           }
                           else 
                           {
                               try
                               {
                                    if(FixConstants.TRAIANA_MSG_FIX_TAG.equals(sPendingMsgType))
                                    {
                                        Message oFIXMessage = new Message();
                                        oFIXMessage.fromString(sPendinMsg, o_SessionDictionary, o_AppDictionary, true);
                                        o_IoHub.sendToTraiana(oFIXMessage);
                                    }
                                    else if(o_Config.IsValidConvertableFixMessageforRMS(sPendingMsgType) &&o_Config.IsTestingModeForRMs()) //TEST
                                    {
                                        Message oFIXMessage = new Message();
                                        oFIXMessage.fromString(sPendinMsg, o_SessionDictionary, o_AppDictionary, true);
                                        o_IoHub.CreateTraianaMessage(oFIXMessage);
                                    }
                                    else
                                    {
                                        o_IoHub.getTraianaHandlerSessionID().write(sPendinMsg);
                                    }
                                }
                               catch (Exception e)
                               {
                                    o_IoHub.getTraianaHandlerSessionID().write(sPendinMsg);
                               }
                           }
                       }
                   }
                   b_LogonReceived=true;
               }

               if(b_LogonSent && (FixConstants.LOGOUT_MSG_FIX_TAG).equals(sMsgType) )
               {
                   LOGGER.debug("Logout Received");
                   b_Logoutsent=true;
                   osession.close();

               }

        }
     
        } catch (InvalidMessage ex) {
            LOGGER.error("Erro Message sent");
        }
     INTERFACELOGGER.info("Messgae: {} Sent to RMS Session: {}",sMsg,osession.toString());
     
 }
 

 
 public void sessionCreated(IoSession oSession) throws Exception {
        LOGGER.debug("RMG Session created... {}", oSession);
        if( oSession.getTransportType() == TransportType.SOCKET )
        {
            ((SocketSessionConfig) oSession.getConfig() ).setReceiveBufferSize( 2048 );
        }
        o_IoHub.setRMSHandlerSessionID(oSession);
        if(!o_Traianaclient.Start())
        {
            LOGGER.warn("Session Disconnected as Couldn't conencted to  Traiana");
            System.out.println("Session Disconnected as it Couldn't connected to  Traiana");
            oSession.close();
        }
 }
 
 public void sessionClosed(IoSession session) throws Exception {
        LOGGER.debug("RMG Session closed...{}", session);
        if(b_Logoutsent==true)
        {
            o_IoHub.unSetRMSHandlerSessionID();
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
      LOGGER.warn("Timeout in RMS");
  }
 
 
 
}
