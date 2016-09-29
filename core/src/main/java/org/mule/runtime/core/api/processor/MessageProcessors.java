/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.processor;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.processor.chain.DefaultMessageProcessorChainBuilder;

import java.util.List;

/**
 * Some convenience methods for message processors.
 */
public class MessageProcessors {

  private MessageProcessors() {
    // do not instantiate
  }

  /**
   * Creates a new {@link MessageProcessorChain} from one or more {@link Processor}'s. Note that this performs chains construction
   * but wil not inject {@link MuleContext} or {@link FlowConstruct} or perform any lifecycle.
   *
   * @param processors processors to construct chains from.
   * @return new {@link MessageProcessorChain} instance.
   */
  public static MessageProcessorChain newChain(Processor... processors) {
    return new DefaultMessageProcessorChainBuilder().chain(processors).build();
  }

  /**
   * Creates a new {@link MessageProcessorChain} from a {@link List} of {@link Processor}'s. Note that this performs chains
   * construction but wil not inject {@link MuleContext} or {@link FlowConstruct} or perform any lifecycle.
   *
   * @param processors list of processors to construct chains from.
   * @return new {@link MessageProcessorChain} instance.
   */
  public static MessageProcessorChain newChain(List<Processor> processors) {
    return new DefaultMessageProcessorChainBuilder().chain(processors).build();
  }

}
