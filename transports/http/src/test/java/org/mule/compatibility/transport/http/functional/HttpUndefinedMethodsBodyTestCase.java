/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.transport.http.functional;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.module.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.module.http.api.HttpConstants.Methods.DELETE;
import static org.mule.runtime.module.http.api.HttpConstants.Methods.GET;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.internal.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.construct.Flow;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HttpUndefinedMethodsBodyTestCase extends ExtensionFunctionalTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Parameterized.Parameter(0)
  public String method;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{GET.name()}, {DELETE.name()}});
  }

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class[] {SocketsExtension.class, HttpConnector.class};
  }

  @Override
  protected String getConfigFile() {
    return "http-undefined-methods-body-config.xml";
  }

  @Test
  public void sendBody() throws Exception {
    sendRequestAndAssertMethod(TEST_PAYLOAD, TEST_PAYLOAD);
  }

  @Test
  public void noBody() throws Exception {
    sendRequestAndAssertMethod(EMPTY, "/");
  }

  private void sendRequestAndAssertMethod(String payload, String expectedContent) throws Exception {
    Flow flow = (Flow) getFlowConstruct("requestFlow");
    Event event = Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(payload))
        .addVariable("method", method).build();
    event = flow.process(event);

    assertThat(event.getMessage().getAttributes(), instanceOf(HttpResponseAttributes.class));
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessageAsString(muleContext), is(expectedContent));
  }

}
