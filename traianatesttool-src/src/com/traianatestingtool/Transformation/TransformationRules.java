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
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="transformations")
@XmlType(propOrder={"xslRules"})
public class TransformationRules
{
@XmlElement(name="xsl", required=true)
private List<XslRule> xslRules;

public List<XslRule> getXslRules()
{
return this.xslRules;
}
}

