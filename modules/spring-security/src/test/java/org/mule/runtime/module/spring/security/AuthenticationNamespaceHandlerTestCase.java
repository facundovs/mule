/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.spring.security;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.internal.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.core.api.config.MuleProperties;
import org.mule.runtime.core.api.security.SecurityProvider;
import org.mule.runtime.core.security.MuleSecurityManager;

import java.util.Collection;

import org.junit.Test;

public abstract class AuthenticationNamespaceHandlerTestCase extends ExtensionFunctionalTestCase {

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class[] {SocketsExtension.class, HttpConnector.class};
  }

  @Test
  public void testSecurityManagerConfigured() {
    MuleSecurityManager securityManager = muleContext.getRegistry().lookupObject(MuleProperties.OBJECT_SECURITY_MANAGER);
    assertNotNull(securityManager);

    Collection<SecurityProvider> providers = securityManager.getProviders();
    assertEquals(2, providers.size());

    assertThat(containsSecurityProvider(providers, UserAndPasswordAuthenticationProvider.class), is(true));
    assertThat(containsSecurityProvider(providers, PreAuthenticatedAuthenticationProvider.class), is(true));
  }

  private boolean containsSecurityProvider(Collection<SecurityProvider> providers, Class authenticationProviderClass) {
    for (SecurityProvider provider : providers) {
      assertEquals(SpringProviderAdapter.class, provider.getClass());
      if (authenticationProviderClass.equals(((SpringProviderAdapter) provider).getAuthenticationProvider().getClass())) {
        return true;
      }
    }
    return false;
  }
}
