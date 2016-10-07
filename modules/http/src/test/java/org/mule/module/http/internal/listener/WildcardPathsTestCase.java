package org.mule.module.http.internal.listener;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.tck.AbstractMuleTestCase;


public class WildcardPathsTestCase extends AbstractMuleTestCase
{
    private MuleContext muleContext;	
	

	@Test
    public void testWildcardURL1() throws Exception
    {
		muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
		muleContext.start();
        assertEquals("V1 Flow invoked",doGet("http://0.0.0.0:8081/"));
        muleContext.dispose();
    }
	
	@Test
    public void testWildcardURL2() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("V1 Flow invoked",doGet("http://0.0.0.0:8081/taxes"));
        muleContext.dispose();
    }
	
	@Test
    public void testWildcardURL3() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("V1 Flow invoked",doGet("http://0.0.0.0:8081/taxes/1"));
        muleContext.dispose();
    }
	
	@Test
    public void testWildcardURL4() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals(doGet("http://0.0.0.0:8081/v2"),"v2 flow invoked");
        muleContext.dispose();
    }
	
	@Test
    public void testWildcardURL5() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals(doGet("http://0.0.0.0:8081/v2/console"),"v2 flow invoked");
        muleContext.dispose();

    }
	
	@Test
    public void testWildcardURL6() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("V2 - Healthcheck",doGet("http://0.0.0.0:8081/v2/taxes/healthcheck"));
        muleContext.dispose();
    }
	
	@Test
    public void testWildcardURL7() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("V2 - Healthcheck",doGet("http://0.0.0.0:8081/v2/taxes/healthcheck"));
        muleContext.dispose();

    }
	
    @Test
    public void testWildcardURL8() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("v2 flow invoked",doGet("http://0.0.0.0:8081/v2/taxes/1"));
        muleContext.dispose();

    }
	
	@Test
    public void testWildcardURL9() throws Exception
    {
        muleContext = new DefaultMuleContextFactory().createMuleContext("HttpTestPathsWildcard.xml");
        muleContext.start();
        assertEquals("v2 flow invoked",doGet("http://0.0.0.0:8081/v2/taxes"));
        muleContext.dispose();
    }
	private String doGet(String urlString){
		 URL url;
		 String inputLine=null;
		try {
			url = new URL(urlString);
		    URLConnection yc = url.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        inputLine = in.readLine();

	        in.close();
		} catch (IOException e) {
		
		}
		return inputLine;
	}
}
