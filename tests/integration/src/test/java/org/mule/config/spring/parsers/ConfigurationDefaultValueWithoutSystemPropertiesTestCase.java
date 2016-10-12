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
import org.mule.tck.junit4.FunctionalTestCase;

//No se ha pasado como parametro el valor a la jvm y no existe el valor configurado especificamente, entonces se toma el valor default definido en el XSD 
public class ConfigurationDefaultValueWithoutSystemPropertiesTestCase extends FunctionalTestCase
{
	
    @Override
    public String getConfigFile()
    {
        return "configuration-default-value.xml";
    }
    
	@Test
    public void testTimeoutSynchronous() throws Exception
    {
		assertThat(10000, Is.is(muleContext.getConfiguration().getDefaultResponseTimeout()));
    }
	
	@Test
    public void testTransactionTimeout() throws Exception
    {
		assertThat(30000, Is.is(muleContext.getConfiguration().getDefaultTransactionTimeout()));
    }

}