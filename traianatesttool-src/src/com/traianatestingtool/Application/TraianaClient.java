/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Application;

import com.traianatestingtool.Utilities.Config;
import com.traianatestingtool.codec.FIXProtocolCodecFactory;
import java.net.InetSocketAddress;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFilter;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;

/**
 *
 * @author mukunthant
 */
public class TraianaClient {
    Config o_Config;
    IoHub o_IoHub;
    TraianaHandler o_TraianaHandler;
    private ConnectFuture connFuture;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraianaClient.class);
    
    public TraianaClient(IoHub oIoHub, Config oConfig) throws ConfigError
    {
        this.o_IoHub=oIoHub;
        this.o_Config=oConfig;
        o_TraianaHandler=new TraianaHandler(o_IoHub,o_Config);
    }
    public boolean Start() throws InterruptedException
    {
        IoFilter dsgCodecFilter = new ProtocolCodecFilter(new FIXProtocolCodecFactory());
        IoConnector initiator =new SocketConnector();
        SocketConnectorConfig cfgini = new SocketConnectorConfig(); 
        cfgini.getFilterChain().addLast( "logger",new LoggingFilter() );
        cfgini.getFilterChain().addLast( "codec",dsgCodecFilter);
        connFuture= initiator.connect(new InetSocketAddress(o_Config.getTRAIANAAddress(),o_Config.getTRAIANAPort()),o_TraianaHandler,cfgini);
        connFuture.join();
        if(connFuture.isConnected()) 
        {
             LOGGER.info("Traiana server(Verifix) side Client started.");
            return true;
        }
        else
        {
            return false;
        }     
        
    }
        
}
