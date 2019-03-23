/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Utilities;

/**
 *
 * @author mukunthant
 */
import java.util.Properties;

public class Config {

	private static final int DEFAULT_RMS_PORT = 11531;

	private static final int DEFAULT_TRAIANA_PORT = 12000;

	private static final String DEFAULT_TRAIANA_ADDRESS = "127.0.0.1";
	
	private static final String DEFAULT_FIX_TRANSPORT_SPECIFICATION_PATH = "FIXT11.xml";

	private static final String DEFAULT_FIX_APPLICATION_SPECIFICATION_PATH = "FIX50.xml";

	private static final String DEFAULT_VERIFIX_TO_TRAIANA_CONFIG_PATH = "verifix-to-traiana.xml";

	private static final String DEFAULT_TRAIANA_TO_VERIFIX_CONFIG_PATH = "traiana-to-verifix.xml";
        
        private static final String DEFAULT_TRAIANA_TRAIANAFIX_MESSAGE="";
        
        private static final String DEFAULT_RMS_TRAIANAFIX_MESSAGE="";
        
        private static final boolean DEFAULT_RMS_TESTINGMODE=false;
        
        private static final boolean DEFAULT_MESSAGE_VALIDATION=false;
        
        private static final String DEFAULT_NODE_ATTRIBUTE="fpmlVersion=\"5-6\" xmlns=\"http://www.fpml.org/FpML-5/pretrade\"";

	
        private static final String TRAIANA_PORT_PROPERTY = "traiana.port";
        private static final String RMS_PORT_PROPERTY = "rms.port";
        private static final String TRAIANA_ADDRESS_PROPERTY = "traiana.address";
        private static final String FIX_TRANSPORT_SPECIFICATION_PATH_PROPERTY="fix.transport";
        private static final String FIX_APPLICATION_SPECIFICATION_PATH_PROPERTY="fix.application";
        private static final String VERIFIX_TO_TRAIANA_CONFIG_PATH_PROPERTY = "verifix2traiana";
        private static final String TRAIANA_TO_VERIFIX_CONFIG_PATH_PROPERTY = "traiana2verifix";
        private static final String TRAIANA_FIX_FOR_TRAIANA_CONFIG_PATH_PROPERTY="traina.convertablefixmessages";
        private static final String RMS_FIX_FOR_TRAIANA_CONFIG_PATH_PROPERTY="rms.convertablefixmessages";
        private static final String RMS_TESTING_MODE_PROPERTY="rms.testingmode";
        private static final String FIX_MESSAGE_VALIDATION_PATH_PROPERTY="fix.messagevalidation";
        private static final String FPML_XML_NODE_ATTRIBUTE_PATH_PROPERTY="xml.firstnodeattribute";
        

	private final int iRMSPort;
        private final int iTraianaPort;
        private final String sTraianaAddress;
        private final String sFixTransportSpecificationPath;
        private final String sFixApplicationSpecificationPath;
	private final String sVerifixToTraianaConfigPath;
	private final String sTraianaToVerifixConfigPath;
        private final String sTraianaFixForTraiana;
        private final String sRMSFixForTraiana;
	private final boolean bIsRMSTestingmode;
        private final boolean bIsMessageValidationMode;
        private static String sXMLNodeAttribute;
        
        private String[] a_RMSValidConvertableMsgs;
        private String[] a_TraianaValidConvertableMsgs;

	
	public Config() {
		this.iRMSPort = DEFAULT_RMS_PORT;
		this.iTraianaPort = DEFAULT_TRAIANA_PORT;
                this.sTraianaAddress=DEFAULT_TRAIANA_ADDRESS;
		this.sFixTransportSpecificationPath = DEFAULT_FIX_TRANSPORT_SPECIFICATION_PATH;
		this.sFixApplicationSpecificationPath = DEFAULT_FIX_APPLICATION_SPECIFICATION_PATH;
		this.sVerifixToTraianaConfigPath=DEFAULT_VERIFIX_TO_TRAIANA_CONFIG_PATH;
                this.sTraianaToVerifixConfigPath=DEFAULT_TRAIANA_TO_VERIFIX_CONFIG_PATH;
                this.sTraianaFixForTraiana=DEFAULT_TRAIANA_TRAIANAFIX_MESSAGE;
                this.sRMSFixForTraiana=DEFAULT_RMS_TRAIANAFIX_MESSAGE;
                this.bIsRMSTestingmode=DEFAULT_RMS_TESTINGMODE;
                this.bIsMessageValidationMode=DEFAULT_MESSAGE_VALIDATION;
                this.sXMLNodeAttribute=DEFAULT_NODE_ATTRIBUTE;
                setTraianaValidMessageTypes();
                setRMSValidMessageTypes();
		
	}

	public Config(Properties properties) throws ConfigException {
		this.iRMSPort = getProperty(properties, RMS_PORT_PROPERTY, DEFAULT_RMS_PORT);
		this.iTraianaPort = getProperty(properties, TRAIANA_PORT_PROPERTY, DEFAULT_TRAIANA_PORT);
                this.sTraianaAddress = properties.getProperty(TRAIANA_ADDRESS_PROPERTY, DEFAULT_TRAIANA_ADDRESS);
		this.sFixTransportSpecificationPath = properties.getProperty(FIX_TRANSPORT_SPECIFICATION_PATH_PROPERTY,DEFAULT_FIX_TRANSPORT_SPECIFICATION_PATH);
		this.sFixApplicationSpecificationPath = properties.getProperty(FIX_APPLICATION_SPECIFICATION_PATH_PROPERTY,DEFAULT_FIX_APPLICATION_SPECIFICATION_PATH);
		this.sVerifixToTraianaConfigPath = properties.getProperty(VERIFIX_TO_TRAIANA_CONFIG_PATH_PROPERTY,DEFAULT_VERIFIX_TO_TRAIANA_CONFIG_PATH);
		this.sTraianaToVerifixConfigPath = properties.getProperty(TRAIANA_TO_VERIFIX_CONFIG_PATH_PROPERTY,DEFAULT_TRAIANA_TO_VERIFIX_CONFIG_PATH);
                this.sTraianaFixForTraiana = properties.getProperty(TRAIANA_FIX_FOR_TRAIANA_CONFIG_PATH_PROPERTY,DEFAULT_TRAIANA_TRAIANAFIX_MESSAGE);
                this.sRMSFixForTraiana = properties.getProperty(RMS_FIX_FOR_TRAIANA_CONFIG_PATH_PROPERTY,DEFAULT_RMS_TRAIANAFIX_MESSAGE);
                this.bIsRMSTestingmode=getProperty(properties,RMS_TESTING_MODE_PROPERTY , DEFAULT_RMS_TESTINGMODE);
                this.bIsMessageValidationMode=getProperty(properties,FIX_MESSAGE_VALIDATION_PATH_PROPERTY , DEFAULT_MESSAGE_VALIDATION);
                this.sXMLNodeAttribute=properties.getProperty(FPML_XML_NODE_ATTRIBUTE_PATH_PROPERTY,DEFAULT_NODE_ATTRIBUTE);
                setTraianaValidMessageTypes();
                setRMSValidMessageTypes();
        }
	
	public int getRMSPort() {
		return this.iRMSPort;
	}

	public int getTRAIANAPort() {
		return this.iTraianaPort;
	}

	public String getTRAIANAAddress() {
		return this.sTraianaAddress;
	}
        

	public String getFixTransportSpecificationPath() {
		return this.sFixTransportSpecificationPath;
	}

	public String getFixApplicationSpecificationPath() {
		return this.sFixApplicationSpecificationPath;
	}

	public String getVeriFixToTraianaConfigPath() {
		return this.sVerifixToTraianaConfigPath;
	}

	public String getTraianaToVerifixConfigPath() {
		return this.sTraianaToVerifixConfigPath;
	}
	
        private String getValidRMSFixMessages() {
              return this.sRMSFixForTraiana;       
	}
        
        private String getValidTraianaFixMessages() {
              return this.sTraianaFixForTraiana;       
	}
        
        private void setRMSValidMessageTypes(){
           String sConvertableMsgList=getValidRMSFixMessages();
           a_RMSValidConvertableMsgs=sConvertableMsgList.split(",");

        }
 
        public boolean IsValidConvertableFixMessageforRMS(String sMsgType){
            for(int i=0;i<a_RMSValidConvertableMsgs.length;i++)
            {
                 if (a_RMSValidConvertableMsgs[i].equals(sMsgType)) {
                     return true;
                 }
            }
            return false;
         }
         
        private void setTraianaValidMessageTypes(){
            String sConvertableMsgList=getValidTraianaFixMessages();
            a_TraianaValidConvertableMsgs=sConvertableMsgList.split(",");

        }
 
        public boolean IsValidConvertableFixMessageforTraiana(String sMsgType){
            for(int i=0;i<a_TraianaValidConvertableMsgs.length;i++)
            {
                 if (a_TraianaValidConvertableMsgs[i].equals(sMsgType)) {
                     return true;
                 }
            }

            return false;
        }
        
        public boolean IsTestingModeForRMs()
        {
            return this.bIsRMSTestingmode;
        }

        public boolean IsMessageValidation()
        {
            return this.bIsMessageValidationMode;
        }
        
        public static String getXMLFirstNodeAttribute()
        {
            return sXMLNodeAttribute;
        } 
        
	private static final int getProperty(Properties properties, String key, int defaultValue)
			throws ConfigException {
		String value = properties.getProperty(key);
		if (value == null)
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new ConfigException(String.format("Invalid value for '%s' configuration: %s",
					key, value), ex);
		}
	}
        
        private static final boolean getProperty(Properties properties, String key, boolean defaultValue)
			throws ConfigException {
		String value = properties.getProperty(key);
		if (value == null)
			return defaultValue;
		return Boolean.parseBoolean(value);
	}

	
}
