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
import com.traianatestingtool.Transformation.TransformationEngine;
import com.traianatestingtool.Utilities.Config;
import com.traianatestingtool.Utilities.FileReader;
import java.net.InetSocketAddress;
import java.io.File;



import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilter;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import org.slf4j.Logger;

import quickfix.DataDictionary;

import com.traianatestingtool.codec.FIXProtocolCodecFactory;
import com.traianatestingtool.processing.XmlFixConvertor;
import java.io.IOException;
import org.slf4j.LoggerFactory;

public class Application {

    private static final File CONFIGURATION_FILE = new File("traianatestingtool.properties"); 
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

public static void main(String[] args)  { 
        try {       
            IoHub o_IoHub = null;
            
            Config config = FileReader.readConfig(CONFIGURATION_FILE);
            File TraianaToVerifixFile = new File(config.getTraianaToVerifixConfigPath()); 
            File VerifixToTraianaFile = new File(config.getVeriFixToTraianaConfigPath()); 
            DataDictionary fixtDictionary = FileReader.readDictionary(new File(config.getFixTransportSpecificationPath()));
            DataDictionary fixDictionary = FileReader.readDictionary(new File(config.getFixApplicationSpecificationPath()));
            
            XmlFixConvertor o_Convertor = new XmlFixConvertor(fixtDictionary, fixDictionary); 
         
            TransformationEngine oVerifixToTraianaTransformer=TransformationEngine.newInstance(VerifixToTraianaFile);
            TransformationEngine oTraianaToVerifixTransformer=TransformationEngine.newInstance(TraianaToVerifixFile);
            o_IoHub = new IoHub(o_Convertor, oVerifixToTraianaTransformer, oTraianaToVerifixTransformer,config);
           
            IoAcceptor acceptor = new SocketAcceptor();
            IoFilter dsgCodecFilter = new ProtocolCodecFilter(new FIXProtocolCodecFactory());
            SocketAcceptorConfig cfg = new SocketAcceptorConfig();
            cfg.getFilterChain().addLast( "logger",new LoggingFilter() );
            cfg.getFilterChain().addLast( "codec",dsgCodecFilter);
            
            try
            {
                acceptor.bind(new InetSocketAddress(config.getRMSPort()),new RMSHandler(o_IoHub,config),cfg);
                LOGGER.info("Traiana Testing Tool started.");
                System.out.println("Traiana Testing Tool started.");
            }
            catch(IOException e)
            {
                LOGGER.error("RMS Port is Already in Used {}");
                System.out.println("RMS Port is Already in Used {}");
                return;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured {}",ex.toString());
            System.out.println("Exception occured {}"+ex.toString());
        }
     } 
        


}



