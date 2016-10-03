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
    //Se resolvio de esta forma por que no logre que le metodo element.getUserData() funcione, 
    //es decir buscaba separar lo definido en el mapping y el XSD para saber si el atributo lo habia seteado el usuario o no.
    //De esta forma, lo que hace es traer el contenido del XML en un string y luego lo evaluo con XPath.
    private void populate(Element element, String attr) {
    	String expression = MessageFormat.format("/{0}[@{1}]", new Object[]{DEFINITION_NAME,attr});
    	if(!existAttrInElement(element,expression))
    	{//if attribute exist
    		String systemValue = System.getProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous");
    		if(systemValue != null)
    		{//if variable jvm exist
    			element.setAttribute(attr, systemValue);
    		}
    	}
    }
    
	private boolean existAttrInElement(Element element, String expression) {
		try {
			String xmlStr = new ObjectProxy(((java.util.Hashtable)((java.util.Hashtable) new ObjectProxy(element.getOwnerDocument()).get("userData").getObject()).get(element)).entrySet().toArray()[0]).get("value").get("fData").get("xmlContent").getObjectAsString();
			Document xmlDocument = convertStringToDocument(xmlStr);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			Object node = xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
			return (node != null);
		} catch (Exception e) {
			return false;
		}
	}
	
    private Document convertStringToDocument(String xmlStr) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        builder = factory.newDocumentBuilder();  
        return builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
    }	
   
    public class ObjectProxy {

    	private Object object;
    	
    	public ObjectProxy(Object object){
    		super();
    		this.object = object;
    	}

    	public ObjectProxy get(String name){
    		Object value = getField(name) != null ? getField(name) : getMethod(name);
    		return new ObjectProxy(value);
    	}
   
    	
    	//-------- field -------------------------------------------
    	
    	private Object getField(String name){
    		return getField(object.getClass(),name);
    	}

    	@SuppressWarnings("rawtypes")
    	private Object getField(Class clazz,String name){
    		Object value = null;
    		try {
    			Field field = clazz.getDeclaredField(name);
    			field.setAccessible(true);
    			value = field.get(object);
    		} catch (Exception e) {
    			if(!"java.lang.Object".equals(clazz.getSuperclass().getName())){
    				value = getField(clazz.getSuperclass(),name);
    			}
    		}
    		return value;
    	}
    	
    	//-------- method -------------------------------------------
    	
    	private Object getMethod(String name){
    		return getMethod(object.getClass(),name);
    	}

    	@SuppressWarnings({ "rawtypes", "unchecked" })
    	private Object getMethod(Class clazz,String name){
    		Object value = null;
    		try {
    			Method method = clazz.getDeclaredMethod(name);
    			method.setAccessible(true);
    			value = method.invoke(object);
    		} catch (Exception e) {
    			if(!"java.lang.Object".equals(clazz.getSuperclass().getName())){
    				value = getMethod(clazz.getSuperclass(),name);
    			}
    		}
    		return value;
    	}
    	
    	public String getObjectAsString() {
    		if(object != null){
    			return object.toString();
    		}else{
    			return null;
    		}
    	}	
    	
    	public Object getObject() {
    		return object;
    	}

    	public void setObject(Object instance) {
    		this.object = instance;
    	}
    	
    }
    //TODO: REF:SE-3093 (end)
    
}