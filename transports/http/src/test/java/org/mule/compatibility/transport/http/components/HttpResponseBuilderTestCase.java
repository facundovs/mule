/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.transport.http.components;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.compatibility.transport.http.CacheControlHeader;
import org.mule.compatibility.transport.http.CookieHelper;
import org.mule.compatibility.transport.http.CookieWrapper;
import org.mule.compatibility.transport.http.HttpConnector;
import org.mule.compatibility.transport.http.HttpConstants;
import org.mule.compatibility.transport.http.HttpResponse;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.EventContext;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.el.ExpressionLanguage;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.context.notification.DefaultFlowCallStack;
import org.mule.runtime.core.metadata.DefaultTypedValue;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.junit.Before;
import org.junit.Test;

@SmallTest
public class HttpResponseBuilderTestCase extends AbstractMuleTestCase {

  private static final String HTTP_BODY = "<html><head></head><body><p>This is the response body</p></body></html>";
  private static final String HEADER_STATUS = "#[header:status]";
  private static final String HEADER_CONTENT_TYPE = "#[header:contentType]";
  private static final String HEADER_CACHE_CONTROL = "#[header:cacheControl]";
  private static final String HEADER_EXPIRES = "#[header:expires]";
  private static final String HEADER_LOCATION = "#[header:location]";
  private static final String HEADER_NAME = "#[header:name]";
  private static final String HEADER_VALUE = "#[header:value]";
  private static final String HEADER_DOMAIN = "#[header:domain]";
  private static final String HEADER_PATH = "#[header:path]";
  private static final String HEADER_EXPIRY_DATE = "#[header:expiryDate]";
  private static final String HEADER_SECURE = "#[header:secure]";
  private static final String HEADER_VERSION = "#[header:version]";
  private static final String HEADER_DIRECTIVE = "#[header:directive]";
  private static final String HEADER_MAX_AGE = "#[header:maxAge]";
  private static final String HEADER_MUST_REVALIDATE = "#[header:mustRevalidate]";
  private static final String HEADER_NO_CACHE = "#[header:noCache]";
  private static final String HEADER_NO_STORE = "#[header:noStore]";


  private MuleContext muleContext;
  private InternalMessage mockMuleMessage;
  private ExpressionLanguage mockExpressionLanguage = mock(ExpressionLanguage.class);
  private Event mockEvent;

  @Before
  public void setUp() {
    muleContext = mock(MuleContext.class, RETURNS_DEEP_STUBS);
    mockEvent = mock(Event.class);
    mockMuleMessage = mock(InternalMessage.class);
    when(mockEvent.getMessage()).thenAnswer(invocation -> mockMuleMessage);
    when(mockEvent.getFlowCallStack()).thenReturn(new DefaultFlowCallStack());
    when(mockEvent.getError()).thenReturn(empty());
    final Flow flow = mock(Flow.class);
    when(flow.getMuleContext()).thenReturn(muleContext);
    EventContext executionContext = DefaultEventContext.create(flow, TEST_CONNECTOR);
    when(mockEvent.getContext()).thenReturn(executionContext);
    mockExpressionLanguage = mock(ExpressionLanguage.class);
    when(muleContext.getExpressionLanguage()).thenReturn(mockExpressionLanguage);
  }

  @Test
  public void testEmptyHttpResponseBuilder() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    mockMuleMessage = InternalMessage.builder().payload(HTTP_BODY).build();

    mockParse();
    HttpResponse httpResponse = (HttpResponse) httpResponseBuilder.process(mockEvent).getMessage().getPayload().getValue();
    assertEquals(HTTP_BODY, httpResponse.getBodyAsString());
    assertEquals(HttpConstants.HTTP11, httpResponse.getHttpVersion().toString());
    assertEquals(HttpConstants.SC_OK, httpResponse.getStatusCode());
    validateHeader(httpResponse.getHeaders(), HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.DEFAULT_CONTENT_TYPE);
  }

  @Test
  public void testHttpResponseBuilderAttributes() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    mockMuleMessage = InternalMessage.builder().payload(HTTP_BODY).build();

    httpResponseBuilder.setContentType("text/html");
    httpResponseBuilder.setStatus(String.valueOf(HttpConstants.SC_INTERNAL_SERVER_ERROR));

    mockParse();
    HttpResponse httpResponse = (HttpResponse) httpResponseBuilder.process(mockEvent).getMessage().getPayload().getValue();
    assertEquals(HTTP_BODY, httpResponse.getBodyAsString());
    assertEquals(HttpConstants.HTTP11, httpResponse.getHttpVersion().toString());
    assertEquals(HttpConstants.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusCode());
    validateHeader(httpResponse.getHeaders(), HttpConstants.HEADER_CONTENT_TYPE, "text/html");
  }

  @Test
  public void testHttpResponseBuilderAttributesWithExpressions() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    mockMuleMessage = InternalMessage.builder().payload(HTTP_BODY).build();

    httpResponseBuilder.setStatus(HEADER_STATUS);
    httpResponseBuilder.setContentType(HEADER_CONTENT_TYPE);

    when(mockExpressionLanguage.parse(HEADER_STATUS, mockEvent, null))
        .thenReturn(String.valueOf(HttpConstants.SC_INTERNAL_SERVER_ERROR));
    when(mockExpressionLanguage.parse(HEADER_CONTENT_TYPE, mockEvent, null)).thenReturn("text/html");


    HttpResponse httpResponse = (HttpResponse) httpResponseBuilder.process(mockEvent).getMessage().getPayload().getValue();
    assertEquals(HTTP_BODY, httpResponse.getBodyAsString());
    assertEquals(HttpConstants.HTTP11, httpResponse.getHttpVersion().toString());
    assertEquals(HttpConstants.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusCode());
    validateHeader(httpResponse.getHeaders(), HttpConstants.HEADER_CONTENT_TYPE, "text/html");
  }

  @Test
  public void testHttpResponseBuilderHeadersWithExpressions() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();

    Map<String, String> headers = new HashMap<>();
    headers.put("Cache-Control", HEADER_CACHE_CONTROL);
    headers.put("Expires", HEADER_EXPIRES);
    headers.put("Location", HEADER_LOCATION);
    httpResponseBuilder.setHeaders(headers);

    when(mockExpressionLanguage.parse("Cache-Control", mockEvent, null)).thenReturn("Cache-Control");
    when(mockExpressionLanguage.parse("Expires", mockEvent, null)).thenReturn("Expires");
    when(mockExpressionLanguage.parse("Location", mockEvent, null)).thenReturn("Location");
    when(mockExpressionLanguage.parse(HEADER_CACHE_CONTROL, mockEvent, null)).thenReturn("max-age=3600");
    when(mockExpressionLanguage.isExpression(HEADER_EXPIRES)).thenReturn(true);
    when(mockExpressionLanguage.evaluate(HEADER_EXPIRES, mockEvent, null)).thenReturn("Thu, 01 Dec 1994 16:00:00 GMT");
    when(mockExpressionLanguage.parse(HEADER_LOCATION, mockEvent, null)).thenReturn("http://localhost:8080");

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setHeaders(response, mockEvent);

    validateHeaders(response.getHeaders());
  }

  @Test
  public void testHttpResponseBuilderHeaders() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    Map<String, String> headers = new HashMap<>();
    headers.put("Cache-Control", "max-age=3600");
    headers.put("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
    headers.put("Location", "http://localhost:8080");
    httpResponseBuilder.setHeaders(headers);

    mockParse();
    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setHeaders(response, mockEvent);

    validateHeaders(response.getHeaders());
  }

  @Test
  public void testHttpResponseBuilderHeadersWithExpressionInHeaderName() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();

    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_LOCATION, "http://localhost:9090");
    httpResponseBuilder.setHeaders(headers);

    when(mockExpressionLanguage.parse(HEADER_LOCATION, mockEvent, null)).thenReturn("Location");
    when(mockExpressionLanguage.parse("http://localhost:9090", mockEvent, null)).thenReturn("http://localhost:9090");

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setHeaders(response, mockEvent);

    validateHeader(response.getHeaders(), "Location", "http://localhost:9090");
  }

  @Test
  public void testHttpResponseBuilderCookies() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    List<CookieWrapper> cookies = new ArrayList<>();

    cookies.add(createCookie("userName", "John_Galt", "localhost", "/", "Thu, 15 Dec 2013 16:00:00 GMT", "true", "1"));
    cookies.add(createCookie("userId", "1", "localhost", "/", "Thu, 01 Dec 2013 16:00:00 GMT", "true", "1"));

    mockParse();

    httpResponseBuilder.setCookies(cookies);

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCookies(response, mockEvent);

    Map<String, String> responseCookies = getHeaderCookie(response.getHeaders());
    assertNotNull(responseCookies);
    assertEquals("userName=John_Galt; Version=1; Domain=localhost; Path=/; Secure; Expires=Sun, 15-Dec-2013 16:00:00 GMT",
                 responseCookies.get("userName"));
    assertEquals("userId=1; Version=1; Domain=localhost; Path=/; Secure; Expires=Sun, 1-Dec-2013 16:00:00 GMT",
                 responseCookies.get("userId"));
  }

  @Test
  public void testHttpResponseBuilderCookiesWithExpressions() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();

    List<CookieWrapper> cookies = new ArrayList<>();
    cookies.add(createCookie(HEADER_NAME, HEADER_VALUE, HEADER_DOMAIN, HEADER_PATH, HEADER_EXPIRY_DATE, HEADER_SECURE,
                             HEADER_VERSION));
    httpResponseBuilder.setCookies(cookies);

    when(mockExpressionLanguage.parse(HEADER_NAME, mockEvent, null)).thenReturn("userName");
    when(mockExpressionLanguage.parse(HEADER_VALUE, mockEvent, null)).thenReturn("John_Galt");
    when(mockExpressionLanguage.parse(HEADER_DOMAIN, mockEvent, null)).thenReturn("localhost");
    when(mockExpressionLanguage.parse(HEADER_PATH, mockEvent, null)).thenReturn("/");
    when(mockExpressionLanguage.isExpression(HEADER_EXPIRY_DATE)).thenReturn(true);
    when(mockExpressionLanguage.evaluate(HEADER_EXPIRY_DATE, mockEvent, null)).thenReturn("Sun, 15 Dec 2013 16:00:00 GMT");
    when(mockExpressionLanguage.parse(HEADER_SECURE, mockEvent, null)).thenReturn("true");
    when(mockExpressionLanguage.parse(HEADER_VERSION, mockEvent, null)).thenReturn("1");

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCookies(response, mockEvent);

    Map<String, String> responseCookies = getHeaderCookie(response.getHeaders());
    assertNotNull(responseCookies);
    assertEquals("userName=John_Galt; Version=1; Domain=localhost; Path=/; Secure; Expires=Sun, 15-Dec-2013 16:00:00 GMT",
                 responseCookies.get("userName"));
  }

  @Test
  public void testHttpResponseDefaultVersion() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    when(mockMuleMessage.getInboundProperty(HttpConnector.HTTP_VERSION_PROPERTY)).thenReturn(HttpConstants.HTTP10);

    httpResponseBuilder.checkVersion(mockMuleMessage);

    assertEquals(HttpConstants.HTTP10, httpResponseBuilder.getVersion());
  }

  @Test
  public void testHttpResponseDefaultContentType() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    when(mockMuleMessage.getPayload()).thenReturn(new DefaultTypedValue("", DataType.HTML_STRING));

    when(mockEvent.getMessage()).thenReturn(mockMuleMessage);

    HttpResponse response = new HttpResponse();
    mockParse();
    httpResponseBuilder.setContentType(response, mockEvent);

    validateHeader(response.getHeaders(), HttpConstants.HEADER_CONTENT_TYPE, "text/html");
  }

  @Test
  public void testHttpResponseEmptyCacheControl() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    httpResponseBuilder.setCacheControl(new CacheControlHeader());

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCacheControl(response, mockEvent);
    assertNull(response.getFirstHeader(HttpConstants.HEADER_CACHE_CONTROL));
  }

  @Test
  public void testHttpResponseCacheControl() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    CacheControlHeader cacheControl = new CacheControlHeader();
    cacheControl.setDirective("public");
    cacheControl.setMaxAge("3600");
    cacheControl.setMustRevalidate("true");
    cacheControl.setNoCache("true");
    cacheControl.setNoStore("true");
    httpResponseBuilder.setCacheControl(cacheControl);
    mockParse();

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCacheControl(response, mockEvent);
    assertEquals("public,no-cache,no-store,must-revalidate,max-age=3600",
                 response.getFirstHeader(HttpConstants.HEADER_CACHE_CONTROL).getValue());
  }

  @Test
  public void testHttpResponseCacheControlWithExpressions() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    CacheControlHeader cacheControl = new CacheControlHeader();
    cacheControl.setDirective(HEADER_DIRECTIVE);
    cacheControl.setMaxAge(HEADER_MAX_AGE);
    cacheControl.setMustRevalidate(HEADER_MUST_REVALIDATE);
    cacheControl.setNoCache(HEADER_NO_CACHE);
    cacheControl.setNoStore(HEADER_NO_STORE);
    httpResponseBuilder.setCacheControl(cacheControl);

    when(mockExpressionLanguage.parse(HEADER_DIRECTIVE, mockEvent, null)).thenReturn("public");
    when(mockExpressionLanguage.parse(HEADER_MAX_AGE, mockEvent, null)).thenReturn("3600");
    when(mockExpressionLanguage.parse(HEADER_MUST_REVALIDATE, mockEvent, null)).thenReturn("true");
    when(mockExpressionLanguage.parse(HEADER_NO_CACHE, mockEvent, null)).thenReturn("true");
    when(mockExpressionLanguage.parse(HEADER_NO_STORE, mockEvent, null)).thenReturn("true");

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCacheControl(response, mockEvent);
    assertEquals("public,no-cache,no-store,must-revalidate,max-age=3600",
                 response.getFirstHeader(HttpConstants.HEADER_CACHE_CONTROL).getValue());
  }

  @Test
  public void testHttpResponseCacheControlWithExtension() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    CacheControlHeader cacheControl = new CacheControlHeader();
    cacheControl.setMaxAge("3600");
    httpResponseBuilder.setCacheControl(cacheControl);
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpConstants.HEADER_CACHE_CONTROL, "smax-age=3600");
    httpResponseBuilder.setHeaders(headers);
    mockParse();

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setHeaders(response, mockEvent);
    httpResponseBuilder.setCacheControl(response, mockEvent);

    assertEquals("max-age=3600,smax-age=3600", response.getFirstHeader(HttpConstants.HEADER_CACHE_CONTROL).getValue());

  }

  @Test
  public void testHttpResponseCopyOutboundProperties() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    Map<String, Object> outboundProperties = new HashMap<>();
    outboundProperties.put(HttpConstants.HEADER_AGE, "12");
    outboundProperties.put(HttpConstants.HEADER_CACHE_CONTROL, "max-age=3600");
    Cookie[] cookies = new Cookie[2];
    cookies[0] = new Cookie(null, "clientId", "2");
    cookies[1] = new Cookie(null, "category", "premium");
    outboundProperties.put(HttpConstants.HEADER_COOKIE_SET, cookies);

    Set<String> propertyNames = outboundProperties.keySet();
    when(mockMuleMessage.getOutboundPropertyNames()).thenReturn(propertyNames);
    for (String propertyName : propertyNames) {
      when(mockMuleMessage.getOutboundProperty(propertyName)).thenReturn(outboundProperties.get(propertyName));
      when(mockMuleMessage.getPayload())
          .thenReturn(new DefaultTypedValue<>(null, DataType.builder(DataType.OBJECT).charset(StandardCharsets.UTF_8).build()));
    }

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.copyOutboundProperties(response, mockMuleMessage);

    Header[] headers = response.getHeaders();
    for (Header header : headers) {
      if (HttpConstants.HEADER_COOKIE_SET.equals(header.getName())) {
        if (header.getValue().startsWith(cookies[0].getName())) {
          assertEquals(cookies[0].toString(), header.getValue());
        } else {
          assertEquals(cookies[1].toString(), header.getValue());
        }

      } else if (header.getName().startsWith(HttpConstants.CUSTOM_HEADER_PREFIX)) {
        assertEquals(outboundProperties.get(header.getName().substring(HttpConstants.CUSTOM_HEADER_PREFIX.length())),
                     header.getValue());
      } else {
        assertEquals(outboundProperties.get(header.getName()), header.getValue());
      }
    }
  }

  @Test
  public void testHttpResponseWithOutboundProperties() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    CacheControlHeader cacheControl = new CacheControlHeader();
    cacheControl.setMaxAge("3600");
    httpResponseBuilder.setCacheControl(cacheControl);

    Map<String, String> headers = new HashMap<>();
    headers.put(HttpConstants.HEADER_CACHE_CONTROL, "public");
    headers.put(HttpConstants.HEADER_AGE, "12");
    httpResponseBuilder.setHeaders(headers);

    Map<String, Serializable> outboundProperties = new HashMap<>();
    outboundProperties.put(HttpConstants.HEADER_CACHE_CONTROL, "no-cache");
    outboundProperties.put(HttpConstants.HEADER_AGE, "20");
    outboundProperties.put(HttpConstants.HEADER_LOCATION, "http://localhost:9090");

    mockParse();
    mockMuleMessage = InternalMessage.builder().payload(HTTP_BODY).outboundProperties(outboundProperties).build();

    HttpResponse httpResponse = (HttpResponse) httpResponseBuilder.process(mockEvent).getMessage().getPayload().getValue();
    Header[] resultHeaders = httpResponse.getHeaders();
    validateHeader(resultHeaders, HttpConstants.HEADER_CACHE_CONTROL, "max-age=3600,public");
    validateHeader(resultHeaders, HttpConstants.HEADER_AGE, "12");
    validateHeader(resultHeaders, HttpConstants.HEADER_LOCATION, "http://localhost:9090");
  }

  @Test
  public void testHttpResponseWithDateExpression() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpConstants.HEADER_EXPIRES, "#[now]");
    httpResponseBuilder.setHeaders(headers);

    Date now = new Date();

    when(mockExpressionLanguage.parse("Expires", mockEvent, null)).thenReturn("Expires");
    when(mockExpressionLanguage.isExpression("#[now]")).thenReturn(true);
    when(mockExpressionLanguage.evaluate("#[now]", mockEvent, null)).thenReturn(now);

    HttpResponse httpResponse = new HttpResponse();
    httpResponseBuilder.setHeaders(httpResponse, mockEvent);

    SimpleDateFormat httpDateFormatter = new SimpleDateFormat(HttpConstants.DATE_FORMAT_RFC822, Locale.US);
    httpDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

    validateHeader(httpResponse.getHeaders(), HttpConstants.HEADER_EXPIRES, httpDateFormatter.format(now));
  }


  @Test
  public void testHttpResponseCookieWithDateBuilder() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    Date now = new Date();
    List<CookieWrapper> cookies = new ArrayList<>();
    cookies.add(createCookie("test", "test", null, null, "#[now]", null, null));
    httpResponseBuilder.setCookies(cookies);

    when(mockExpressionLanguage.isExpression("#[now]")).thenReturn(true);
    when(mockExpressionLanguage.evaluate("#[now]", mockEvent, null)).thenReturn(now);
    when(mockExpressionLanguage.parse("test", mockEvent, null)).thenReturn("test");
    when(mockExpressionLanguage.parse("test", mockEvent, null)).thenReturn("test");

    HttpResponse response = new HttpResponse();
    httpResponseBuilder.setCookies(response, mockEvent);

    SimpleDateFormat httpCookieFormatter = new SimpleDateFormat(CookieHelper.EXPIRE_PATTERN, Locale.US);
    httpCookieFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

    String expectedCookieValue = "test=test; Expires=" + httpCookieFormatter.format(now);
    validateHeader(response.getHeaders(), HttpConstants.HEADER_COOKIE_SET, expectedCookieValue);
  }

  @Test
  public void testHttpResponseSetBodyWithHttpResponsePayload() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    HttpResponse response = new HttpResponse();
    response.setBody(HTTP_BODY);

    when(mockMuleMessage.getPayload()).thenReturn(new DefaultTypedValue(HTTP_BODY, DataType.OBJECT));

    httpResponseBuilder.setBody(response, mockMuleMessage, mockEvent);
    assertEquals(HTTP_BODY, response.getBodyAsString());
  }

  @Test
  public void testHttpResponseSetBody() throws Exception {
    HttpResponseBuilder httpResponseBuilder = createHttpResponseBuilder();
    HttpResponse response = new HttpResponse();

    when(mockMuleMessage.getPayload()).thenReturn(new DefaultTypedValue(HTTP_BODY, DataType.HTML_STRING));

    httpResponseBuilder.setBody(response, mockMuleMessage, mockEvent);
    assertEquals(HTTP_BODY, response.getBodyAsString());
  }

  private CookieWrapper createCookie(String name, String value, String domain, String path, String expiryDate, String secure,
                                     String version) {
    CookieWrapper cookie = new CookieWrapper();
    cookie.setName(name);
    cookie.setValue(value);
    cookie.setDomain(domain);
    cookie.setPath(path);
    cookie.setExpiryDate(expiryDate);
    cookie.setSecure(secure);
    cookie.setVersion(version);
    return cookie;
  }

  private Map<String, String> getHeaderCookie(Header[] headers) {
    Map<String, String> cookies = new HashMap<>();
    for (Header header : headers) {
      if ("Set-Cookie".equals(header.getName())) {
        cookies.put(header.getValue().split("=")[0], header.getValue());
      }
    }
    return cookies;
  }

  private void validateHeaders(Header[] responseHeaders) {
    validateHeader(responseHeaders, "Cache-Control", "max-age=3600");
    validateHeader(responseHeaders, "Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
    validateHeader(responseHeaders, "Location", "http://localhost:8080");
  }

  private HttpResponseBuilder createHttpResponseBuilder() throws InitialisationException {
    HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
    httpResponseBuilder.setMuleContext(muleContext);
    httpResponseBuilder.initialise();
    return httpResponseBuilder;
  }

  private void validateHeader(Header[] headers, String headerName, String expectedValue) {
    for (Header header : headers) {
      if (headerName.equals(header.getName())) {
        assertEquals(expectedValue, header.getValue());
        return;
      }
    }
    fail(String.format("Didn't find header: %s=%s", headerName, expectedValue));
  }

  private void mockParse() {
    when(mockExpressionLanguage.parse(anyString(), any(Event.class), any(FlowConstruct.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);
  }
}
