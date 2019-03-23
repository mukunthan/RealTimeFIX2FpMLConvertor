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
 
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
 
 public class XmlUtils
 {
     
   public static String xmlToString(Document document) throws TransformerConfigurationException, TransformerException
   {
     if (document == null) {
       throw new IllegalArgumentException("document can not be null");
     }
      document.setXmlStandalone(true);
      Transformer transformer = null;
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      //transformer.setOutputProperty(OutputKeys.METHOD, "html");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(document);
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      String sReplacedXML = xmlString.replaceFirst("fpmlVersion=\"5-6\"",Config.getXMLFirstNodeAttribute());
      return sReplacedXML;
   }
   
   public static Document convertStringToDocument(String xmlStr) throws Exception 
   {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder builder; 
       
            builder = factory.newDocumentBuilder(); 
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
    }
   
 }



