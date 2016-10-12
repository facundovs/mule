/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.parsers;

import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.mule.api.config.MuleProperties;
import org.mule.tck.junit4.FunctionalTestCase;


//Existe el valor configurado especificamente y se ha pasado como parametro el valor a la jvm, entonces este ultimo se ignora y se toma el configurado
public class ConfigurationCustumValueWithSystemPropertiesTestCase extends FunctionalTestCase
{

	private static final String MULE_SYSTEM_TIMEOUT_SYNCHRONOUS = MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.synchronous";
	private static final String MULE_SYSTEM_TIMEOUT_TRANSACTION = MuleProperties.SYSTEM_PROPERTY_PREFIX + "timeout.transaction";

	public ConfigurationCustumValueWithSystemPropertiesTestCase() 
	{
		System.setProperty(MULE_SYSTEM_TIMEOUT_SYNCHRONOUS, "140000");
		System.setProperty(MULE_SYSTEM_TIMEOUT_TRANSACTION, "141000");
	}
	
    @Override
    public String getConfigFile()
    {
        return "configuration-custum-value.xml";
    }
    
	@Test
    public void testTimeoutSynchronous() throws Exception
    {
		assertThat(40000, Is.is(muleContext.getConfiguration().getDefaultResponseTimeout()));
    }
	
	@Test
    public void testTransactionTimeout() throws Exception
    {
		assertThat(41000, Is.is(muleContext.getConfiguration().getDefaultTransactionTimeout()));
    }
	
}