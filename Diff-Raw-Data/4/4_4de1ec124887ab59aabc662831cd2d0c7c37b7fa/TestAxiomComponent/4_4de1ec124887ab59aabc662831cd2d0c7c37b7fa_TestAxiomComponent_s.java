 /*
  * Copyright (c) 2009, Tim Watson
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *     * Neither the name of the author nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  */
 
 package org.axiom.integration.camel;
 
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.assertThat;
 import org.junit.runner.RunWith;
 import org.apache.camel.Component;
 import org.apache.camel.CamelContext;
 import org.apache.camel.Processor;
 import org.apache.camel.Exchange;
 import org.apache.camel.impl.ProcessorEndpoint;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.spi.Registry;
 import org.apache.commons.configuration.Configuration;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.Mockery;
 import org.jmock.Expectations;
 import static org.hamcrest.core.IsSame.sameInstance;
 
 @RunWith(JMock.class)
 public class TestAxiomComponent {
 
     private Mockery mockery;
     private CamelContext mockContext;
     private Registry registry;
     private ContextProcessingNode mockProcessor;
     private Configuration mockConfig;
     private AxiomComponent component;
     private static final String PROCESSOR = "processor";
 
     @Before
     public void beforeEach() {
         mockery = new Mockery();
         mockContext = mockery.mock(CamelContext.class);
         mockProcessor = mockery.mock(ContextProcessingNode.class);
         registry = mockery.mock(Registry.class);
         mockConfig = mockery.mock(Configuration.class);
         
         component = new AxiomComponent();
         component.setCamelContext(mockContext);
         component.setConfiguration(mockConfig);
     }
 
     @Test
     public void itShouldLookInTheRegistryForTheNamedContext() throws Exception {
         final String contextBeanName = "camelContextBean1";
         mockery.checking(new Expectations() {{
             allowing(mockContext).getRegistry();will(returnValue(registry));
 
             // the return value is unimportant for this test, so we'll just use the
             // mockContext we created to return this registry! 
             one(registry).lookup(contextBeanName, CamelContext.class);
             will(returnValue(mockContext));
 
             one(mockConfig).getString(AxiomComponent.CONTEXT_PROCESSOR_BEANID);
             will(returnValue(PROCESSOR));
 
             allowing(registry).lookup(PROCESSOR, ContextProcessingNode.class);
             will(returnValue(mockProcessor));            
 
             allowing(mockProcessor);
             allowing(mockContext);
         }});
 
         component.createEndpoint(String.format("axiom:%s", contextBeanName));
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void itShouldPukeIfTheNamedBeanIsNotFoundInTheRegistry() throws Exception {
         mockery.checking(new Expectations() {{
             allowing(mockContext).getRegistry();will(returnValue(registry));
 
             one(registry).lookup(with(any(String.class)), with(any(Class.class)));
             will(returnValue(null));
         }});
 
         component.createEndpoint("axiom:no-such-registered-camel-context");
     }
 
     @Test
     public void itShouldResolveSpecialHostAddressToTheCurrentContext() throws Exception {
         mockery.checking(new Expectations() {{
             allowing(mockContext).getRegistry();will(returnValue(registry));
 
             allowing(mockConfig).getString(AxiomComponent.CONTEXT_PROCESSOR_BEANID);
             will(returnValue(PROCESSOR));
 
            //NEW>>>>>>
             never(registry).lookup(with(any(String.class)), with(equal(CamelContext.class)));
 
             allowing(registry).lookup(PROCESSOR, ContextProcessingNode.class);
             will(returnValue(mockProcessor));
 
             allowing(mockContext);
             allowing(mockProcessor);            
         }});
 
         component.createEndpoint("axiom:host");
     }
 
     @Test
     public void itShouldLookupTheProcessingNodeInTheRegistry() throws Exception {
         final CamelContext mockTargetContext = mockContext;
        final String beanId = "axiom.control." + PROCESSOR + ".default";
 
         mockery.checking(new Expectations() {{
             allowing(mockContext).getRegistry();will(returnValue(registry));
 
             one(mockConfig).getString(AxiomComponent.CONTEXT_PROCESSOR_BEANID);
             will(returnValue(beanId));
 
             one(registry).lookup(with(any(String.class)), with(any(Class.class)));
             will(returnValue(mockTargetContext));
 
             one(registry).lookup(beanId, ContextProcessingNode.class);
             will(returnValue(mockProcessor));
 
             allowing(mockContext);
             allowing(mockProcessor);
         }});
 
         component.createEndpoint("axiom:ignored");
     }
 
     @Test
     public void itShouldSetTheTargetContextOnTheProcessingNode() throws Exception {
         final CamelContext mockTargetContext = mockContext;
 
         mockery.checking(new Expectations() {{
             allowing(mockContext).getRegistry();will(returnValue(registry));
 
             allowing(mockConfig).getString(AxiomComponent.CONTEXT_PROCESSOR_BEANID);
             will(returnValue(PROCESSOR));
 
             allowing(registry).lookup(PROCESSOR, ContextProcessingNode.class);
             will(returnValue(mockProcessor));
 
             one(registry).lookup(with(any(String.class)), with(any(Class.class)));
             will(returnValue(mockTargetContext));
 
             allowing(mockContext);
             one(mockProcessor).setContext(mockContext);
         }});
 
         component.createEndpoint("axiom:ignored");
     }
 }
