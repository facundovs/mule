/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.parsers.specific;

import static org.junit.Assert.assertEquals;

import org.mule.api.MuleContext;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.config.MuleProperties;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.junit.Test;

public class ConfigurationTimeoutSynchronousTestCase extends AbstractMuleTestCase
{
    private MuleContext muleContext;
	
	//Si hay un valor configurado en el archivo, el dato de la propertie se ignora.
	@Test
    public void testConfigurationTimeoutWithSystemPropertiesAndXMLFile() throws Exception
    {
//		System.clearProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous");
//		System.setProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous", "40000");
        muleContext = new DefaultMuleContextFactory().createMuleContext("config-timeout.xml");
        muleContext.start();
        MuleConfiguration config = muleContext.getConfiguration();
        assertEquals(30000, config.getDefaultResponseTimeout());
    }
	

	//Si no hay un valor configurado en el archivo, prevalece el valor de la propertie.
	@Test
    public void testConfigurationTimeoutWithSystemPropertiesAndWithoutXMLFile() throws Exception
    {
//		System.clearProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous");
//		System.setProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous", "40000");
        muleContext = new DefaultMuleContextFactory().createMuleContext("config-timeout2.xml");
        muleContext.start();
        MuleConfiguration config = muleContext.getConfiguration();
        assertEquals(40000, config.getDefaultResponseTimeout());
    }
	
	// Test del valor por default
	@Test
    public void testDefaultConfiguration() throws Exception
    {
		//System.clearProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous");
		muleContext = new DefaultMuleContextFactory().createMuleContext("config-timeout2.xml");
        muleContext.start();
        MuleConfiguration config = muleContext.getConfiguration();
        assertEquals(10000, config.getDefaultResponseTimeout());
    }
	
    protected void verifyConfiguration()
    {
        MuleConfiguration config = muleContext.getConfiguration();
        assertEquals(30000, config.getDefaultResponseTimeout());
    }
 
}
