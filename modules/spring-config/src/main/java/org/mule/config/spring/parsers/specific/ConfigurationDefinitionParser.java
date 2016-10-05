/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.parsers.specific;

import static org.mule.config.spring.util.ProcessingStrategyUtils.DEFAULT_PROCESSING_STRATEGY;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.mule.api.config.MuleConfiguration;
import org.mule.api.config.MuleProperties;
import org.mule.config.spring.parsers.generic.NamedDefinitionParser;
import org.mule.config.spring.util.ProcessingStrategyUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses the <mule:configuration> element. If this element appears in multiple Xml config files each will its configuration
 * to a single {@link MuleConfiguration} object.
 *
 * @see MuleConfiguration
 */
public class ConfigurationDefinitionParser extends NamedDefinitionParser
{

	//TODO: REF:SE-3093 (begin)
	private static final String DEFINITION_NAME = "configuration";
	//TODO: REF:SE-3093 (end)
	
    public static final String DEFAULT_EXCEPTION_STRATEGY_ATTRIBUTE = "defaultExceptionStrategy-ref";
    public static final String DEFAULT_RESPONSE_TIMEOUT = "defaultResponseTimeout";
    public static final int DEFAULT_RESPONSE_TIMEOUT_DEFAULT_VALUE = 10000;
    
    private static final String DEFAULT_OBJECT_SERIALIZER_ATTRIBUTE = "defaultObjectSerializer-ref";

    public ConfigurationDefinitionParser()
    {
        super(MuleProperties.OBJECT_MULE_CONFIGURATION);
        addIgnored(DEFAULT_EXCEPTION_STRATEGY_ATTRIBUTE);
        singleton=true;
    }

    protected Class getBeanClass(Element element)
    {
        return MuleConfiguration.class;
    }
    
    @Override
    protected void doParse(Element element, ParserContext context, BeanDefinitionBuilder builder)
    {
        parseExceptionStrategy(element, builder);
        parseObjectSerializer(element, builder);
        ProcessingStrategyUtils.configureProcessingStrategy(element, builder, DEFAULT_PROCESSING_STRATEGY);
        //TODO: REF:SE-3093 (begin)
        populate(element,"defaultResponseTimeout");
        //TODO: REF:SE-3093 (end)
        super.doParse(element,context,builder);
    }

	private void parseExceptionStrategy(Element element, BeanDefinitionBuilder builder)
    {
        if (element.hasAttribute(DEFAULT_EXCEPTION_STRATEGY_ATTRIBUTE))
        {
            builder.addPropertyValue("defaultExceptionStrategyName", element.getAttribute(DEFAULT_EXCEPTION_STRATEGY_ATTRIBUTE));
        }
    }

    private void parseObjectSerializer(Element element, BeanDefinitionBuilder builder)
    {
        if (element.hasAttribute(DEFAULT_OBJECT_SERIALIZER_ATTRIBUTE))
        {
            builder.addPropertyReference("defaultObjectSerializer", element.getAttribute(DEFAULT_OBJECT_SERIALIZER_ATTRIBUTE));
        }
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException
    {
        return MuleProperties.OBJECT_MULE_CONFIGURATION;
    }

    //TODO: REF:SE-3093 (begin)
    private void populate(Element element, String attr) {
    	if(!existAttrInElement(element))
    	{// Si no existe una configuración, tomo la propertie. 
    		String systemValue = System.getProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous");
    		if(systemValue != null)
    		{ 
    			element.setAttribute(attr, systemValue);
    		}
    	}
    }
    
      
	private boolean existAttrInElement(Element element) {
		//Si existe el atributo en la configuración, y no es el valor por defecto...
		if (element.hasAttribute(DEFAULT_RESPONSE_TIMEOUT) && 
        		Integer.parseInt(element.getAttribute(DEFAULT_RESPONSE_TIMEOUT))!= DEFAULT_RESPONSE_TIMEOUT_DEFAULT_VALUE)
            {
            	return true ;
            }
		return false;
	}
	
    //TODO: REF:SE-3093 (end)
    
}