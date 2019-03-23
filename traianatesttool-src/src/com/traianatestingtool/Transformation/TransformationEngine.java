package com.traianatestingtool.Transformation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mukunthant
 */
 import java.io.File;
 import java.util.ArrayList;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathException;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 public class TransformationEngine
 {
   private static final JAXBContext JC_CONTEXT;
   private final ArrayList<XslTransformation> xslTransformations;
   private final DocumentBuilder documentBuilder;
   private final Transformer identityTransformer;
   
   static
   {
     try
     {
       JC_CONTEXT = JAXBContext.newInstance(new Class[] { TransformationRules.class });
     }
     catch (JAXBException ex)
     {
       InternalError error = new InternalError("Unable to create JAXB context for " + 
         TransformationRules.class);
       error.initCause(ex);
       throw error;
     }
   }
   
   private TransformationEngine(TransformationRules rules)
     throws Exception
   {
     DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
     this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
     
     this.xslTransformations = new ArrayList(rules.getXslRules().size());
     XPathFactory xpathFactory = XPathFactory.newInstance();
     XPath xpath = xpathFactory.newXPath();
     
     TransformerFactory transformerFactory = TransformerFactory.newInstance();
     this.identityTransformer = transformerFactory.newTransformer();
     for (XslRule xslRule : rules.getXslRules())
     {
        XPathExpression selector;
       try
       {
         selector = xpath.compile(xslRule.getApplyOn());
       }
       catch (XPathException ex)
       {
        
         throw new Exception(
           String.format("Invalid XPath expression '%s'", new Object[] { xslRule.getApplyOn() }), ex);
       }
      
       File templateFile = new File(xslRule.getTemplate());
Transformer transformer; 
       try
       {
         transformer = transformerFactory.newTransformer(new StreamSource(templateFile));
       }
       catch (TransformerException ex)
       {
        
         throw new Exception(
           String.format("Invalid XSL template '%s'", new Object[] { xslRule.getTemplate() }), ex);
       }
       
       XslTransformation transformation = new XslTransformation(selector, transformer);
       this.xslTransformations.add(transformation);
     }
   }
   
   public static TransformationEngine newInstance(File config)
     throws Exception
   {
     Unmarshaller unmarshaller = JC_CONTEXT.createUnmarshaller();
     TransformationRules rules = (TransformationRules)unmarshaller.unmarshal(config);
     return new TransformationEngine(rules);
   }
   
   public void transform(InputSource source, Result outputTarget)
     throws Exception
   {
     transform(this.documentBuilder.parse(source), outputTarget);
   }
   
   public void transform(Document source, Result outputTarget)
     throws Exception
   {
     for (XslTransformation transformation : this.xslTransformations)
     {
       Boolean result = (Boolean)transformation.getSelector().evaluate(
         source, XPathConstants.BOOLEAN);
       if ((result != null) && (result.booleanValue()))
       {
         transformation.getTransformer().transform(new DOMSource(source), outputTarget);
         return;
       }
     }
     this.identityTransformer.transform(new DOMSource(source), outputTarget);
   }
 }