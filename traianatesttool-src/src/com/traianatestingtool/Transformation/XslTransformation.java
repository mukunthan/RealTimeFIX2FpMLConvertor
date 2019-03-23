/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Transformation;

/**
 *
 * @author mukunthant
 */
 
 import javax.xml.transform.Transformer;
 import javax.xml.xpath.XPathExpression;
 
 class XslTransformation
 {
   private final XPathExpression selector;
   private final Transformer transformer;
   
   XslTransformation(XPathExpression selector, Transformer transformer)
   {
     assert (selector != null);
     assert (transformer != null);
     this.selector = selector;
     this.transformer = transformer;
   }
   
   public XPathExpression getSelector()
   {
     return this.selector;
   }
   
   public Transformer getTransformer()
   {
     return this.transformer;
   }
 }




