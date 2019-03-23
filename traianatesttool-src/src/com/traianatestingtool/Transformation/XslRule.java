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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"template"})
public class XslRule
{
@XmlAttribute(required=true)
private String applyOn;
@XmlElement(required=true)
private String template;

public String getApplyOn()
{
return this.applyOn;
}

public String getTemplate()
{
return this.template;
}
}