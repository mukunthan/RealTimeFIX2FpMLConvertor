/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DataDictionary;

/**
 *
 * @author mukunthant
 */
public class FileReader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);
    
    public static Config readConfig(File file) throws IOException {
        if (!file.exists())
                return new Config();
        InputStream inputStream = null;
        IOException exception = null;
        try {
                inputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(inputStream);
                return new Config(properties);
        } catch (IOException ex) {
                exception = new IOException("Unable to read TTT configuration file", ex);
                throw exception;
        } catch (ConfigException ex) {
                exception = new IOException("Invalid TTT configuation file format", ex);
                throw exception;
        } finally {
                if (inputStream != null) {
                        try {
                                inputStream.close();
                        } catch (IOException ex) {
                                if (exception == null) {
                                        throw ex;
                                } else {
                                        LOGGER.error("Unable to close {}", file, ex);
                                }
                        }
                }
        }
    }
    
    public static DataDictionary readDictionary(File file) throws IOException {
        InputStream inputStream = null;
        IOException exception = null;
        try {
                inputStream = new FileInputStream(file);
                return new DataDictionary(inputStream);
        } catch (IOException ex) {
                exception = new IOException("Unable to read FIX dictionary " + file, ex);
                throw exception;
        } catch (ConfigError ex) {
                exception = new IOException("Invalid FIX dictionary format " + file, ex);
                throw exception;
        } finally {
                if (inputStream != null) {
                        try {
                                inputStream.close();
                        } catch (IOException ex) {
                                if (exception == null) {
                                        throw ex;
                                } else {
                                        LOGGER.error("Unable to close {}", file, ex);
                                }
                        }
                }
        }
    }
    
}
