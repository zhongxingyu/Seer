 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.sca;
 
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.component.ComponentContext;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.xml.bind.JAXBContext;
 
 import org.apache.servicemix.common.Endpoint;
 import org.apache.servicemix.common.ExchangeProcessor;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.tuscany.core.invocation.spi.ProxyFactory;
 import org.apache.tuscany.model.assembly.ConfiguredReference;
 import org.apache.tuscany.model.assembly.ConfiguredService;
 import org.apache.tuscany.model.assembly.EntryPoint;
 import org.apache.tuscany.model.assembly.Interface;
 import org.apache.tuscany.model.types.InterfaceType;
 import org.apache.tuscany.model.types.OperationType;
 import org.apache.tuscany.model.types.java.JavaOperationType;
 
/**
 * 
 * @author gnodet
 * @version $Revision: 366467 $
 * @org.xbean.XBean element="endpoint"
 *                  description="A sca endpoint"
 * 
 */
 public class ScaEndpoint extends Endpoint implements ExchangeProcessor {
 
     protected ServiceEndpoint activated;
     protected EntryPoint entryPoint;
     protected Object proxy;
     protected Map<Class, Method> methodMap;
     protected JAXBContext jaxbContext;
     protected DeliveryChannel channel;
 	
 	public ScaEndpoint(EntryPoint entryPoint) {
 		this.entryPoint = entryPoint;
 	}
 
 	public Role getRole() {
 		return Role.PROVIDER;
 	}
 
 	public void activate() throws Exception {
         logger = this.serviceUnit.getComponent().getLogger();
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         activated = ctx.activateEndpoint(service, endpoint);
         channel = ctx.getDeliveryChannel();
         // Get the target service
         ConfiguredReference referenceValue = entryPoint.getConfiguredReference();
         ConfiguredService targetServiceEndpoint = referenceValue.getConfiguredServices().get(0);
         // Create a proxy
         ProxyFactory proxyFactory = (ProxyFactory) targetServiceEndpoint.getProxyFactory();
         proxy = proxyFactory.createProxy();
         // Get the business interface
         Interface targetInterface = targetServiceEndpoint.getService().getInterfaceContract();
         InterfaceType targetInterfaceType = targetInterface.getInterfaceType();
         List<Class> classes = new ArrayList<Class>();
         methodMap = new HashMap<Class, Method>();
         for (OperationType oper : targetInterfaceType.getOperationTypes()) {
         	if (oper instanceof JavaOperationType) {
         		Method mth = ((JavaOperationType) oper).getJavaMethod();
         		Class[] params = mth.getParameterTypes();
         		if (params.length != 1) {
         			throw new IllegalStateException("Supports only methods with one parameter");
         		}
         		methodMap.put(params[0], mth);
         		classes.add(mth.getReturnType());
         		classes.add(params[0]);
         	}
         }
         jaxbContext = JAXBContext.newInstance(classes.toArray(new Class[0]));
 	}
 
 	public void deactivate() throws Exception {
         ServiceEndpoint ep = activated;
         activated = null;
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         ctx.deactivateEndpoint(ep);
 	}
 
 	public ExchangeProcessor getProcessor() {
 		return this;
 	}
 
 	public void process(MessageExchange exchange) throws Exception {
 		if (exchange.getStatus() == ExchangeStatus.DONE) {
 			return;
 		} else if (exchange.getStatus() == ExchangeStatus.ERROR) {
 			exchange.setStatus(ExchangeStatus.DONE);
 			channel.send(exchange);
 			return;
 		}
 		Object input = jaxbContext.createUnmarshaller().unmarshal(exchange.getMessage("in").getContent());
 		Method method = methodMap.get(input.getClass());
 		if (method ==  null) {
 			throw new IllegalStateException("Could not determine invoked web method");
 		}
 		boolean oneWay = method.getReturnType() == null;
 		Object output;
 		try {
 			output = method.invoke(proxy, new Object[] { input });
 		} catch (InvocationTargetException e) {
 			if (e.getCause() instanceof Exception) {
 				throw (Exception) e.getCause();
 			} else if (e.getCause() instanceof Error) {
 				throw (Error) e.getCause();
 			} else {
 				throw new RuntimeException(e.getCause());
 			}
 		}
 		if (oneWay) {
 			exchange.setStatus(ExchangeStatus.DONE);
 			channel.send(exchange);
 		} else {
 			NormalizedMessage msg = exchange.createMessage();
 			exchange.setMessage(msg, "out");
 			StringWriter writer = new StringWriter();
 			jaxbContext.createMarshaller().marshal(output, writer);
 			msg.setContent(new StringSource(writer.toString()));
 			channel.send(exchange);
 		}
 	}
 
 	public void start() throws Exception {
 	}
 
 	public void stop() throws Exception {
 	}
 
 }
