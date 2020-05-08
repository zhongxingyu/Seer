 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.view;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.servicedesc.ServiceEndpoint;
 
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.jbi.event.ComponentEvent;
 import org.apache.servicemix.jbi.event.ComponentListener;
 import org.apache.servicemix.jbi.event.EndpointEvent;
 import org.apache.servicemix.jbi.event.ExchangeEvent;
 import org.apache.servicemix.jbi.event.ExchangeListener;
 import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
 import org.apache.servicemix.jbi.framework.Registry;
 import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
 import org.apache.servicemix.jbi.servicedesc.EndpointSupport;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
 
 /**
  * Creates a <a href="http://www.graphviz.org/">DOT</a> file showing the JBI MessageExchange
  * flow within the JBI Container.
 
  * @org.apache.xbean.XBean 
  * description="Generates DOT visualisations of the exchanges flow inside ServiceMix"
  * 
  * @version $Revision: 391707 $
  */
 public class DotViewFlowListener extends DotViewEndpointListener 
     implements ExchangeListener, ComponentListener {
 
     private Map flow;
     private Set flowLinks;
     private Set usedComponents;
     private Set componentsAsConsumer;
     private boolean displayComponents = true;
     
     public DotViewFlowListener() {
         setFile("ServiceMixFlow.dot");
         flow = new ConcurrentHashMap();
         flowLinks = new CopyOnWriteArraySet();
         usedComponents = new CopyOnWriteArraySet();
         componentsAsConsumer = new CopyOnWriteArraySet();
     }
 
     // Implementation methods
     // -------------------------------------------------------------------------
 
     protected void generateFile(PrintWriter writer) throws Exception {
         writer.println("digraph \"Apache ServiceMix\" {");
         writer.println();
         writer.println("label = \"Apache ServiceMix flow\";");
         writer.println("node [ shape = box, style = \"rounded,filled\", fontname = \"Helvetica-Oblique\", fontsize = 8 ];");
         writer.println();
 
         List brokerLinks = new ArrayList();
         Registry registry = getContainer().getRegistry();
         Collection components = registry.getComponents();
         for (Iterator iter = components.iterator(); iter.hasNext();) {
             ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();
             ServiceEndpoint[] ses = registry.getEndpointRegistry().getAllEndpointsForComponent(component.getComponentNameSpace());
             String name = component.getName();
             if (!usedComponents.contains(name)) {
                 continue;
             }
             // If we want to display components, create
             // a sub graph, grouping all the components
             // endpoints
             if (isDisplayComponents()) {
                 String id = encode(name);
                 writer.println("subgraph cluster_" + id + " {");
                 writer.println("  label=\"" + name + "\";");
                 if (componentsAsConsumer.contains(name)) {
                     writer.println("  " + id + " [ fillcolor = gray, label = \"" + name + "\" ];");
                 }
             }
             for (int i = 0; i < ses.length; i++) {
                 String key = EndpointSupport.getUniqueKey(ses[i]);
                 String epname = formatEndpoint(key);
                 if (!isDisplayComponents()) {
                     epname += "\\n" + name;
                 }
                 String color = "lightgray";
                 if (epname.startsWith("internal")) {
                     epname = epname.substring(10);
                     color = "#6699ff";
                 } else if (epname.startsWith("external")) {
                     epname = epname.substring(10);
                     color = "#66ccff";
                 } else if (epname.startsWith("dynamic")) {
                     epname = epname.substring(9);
                     color = "#6666ff";
                 } else if (epname.startsWith("linked")) {
                     epname = epname.substring(8);
                     color = "#66ffff";
                 } else {
                     color = "#f3f3f3";
                 }
                 String epid = encode(key);
                 writer.println("  " + epid + " [fillcolor = \"" + color + "\", label=\"" + epname + "\"];");
             }
             if (isDisplayComponents()) {
                 writer.println("}");
             }
         }
         writer.println();
         generateLinks(writer, brokerLinks);
 
         writer.println();
 
         generateLinks(writer, flowLinks);
         
         writer.println("}");
     }
     
     public void exchangeSent(ExchangeEvent event) {
         MessageExchange me = event.getExchange();
        if (me.getEndpoint() != null &&
            me instanceof MessageExchangeImpl) {
             MessageExchangeImpl mei = (MessageExchangeImpl) me;
             String source = (String) me.getProperty(JbiConstants.SENDER_ENDPOINT);
             if (source == null) {
                 source = mei.getSourceId().getName();
                 componentsAsConsumer.add(source);
             } else {
                 ServiceEndpoint[] ses = getContainer().getRegistry().getEndpointRegistry().getAllEndpointsForComponent(mei.getSourceId());
                 for (int i = 0; i < ses.length; i++) {
                     if (EndpointSupport.getKey(ses[i]).equals(source)) {
                         source = EndpointSupport.getUniqueKey(ses[i]);
                         break;
                     }
                 }
             }
             usedComponents.add(mei.getSourceId().getName());
            usedComponents.add(mei.getDestinationId().getName());
             String dest = EndpointSupport.getUniqueKey(mei.getEndpoint());
             Map componentFlow = createSource(source);
             if (componentFlow.put(dest, Boolean.TRUE) == null) {
                 flowLinks.add(encode(source) + " -> " + encode(dest));
                 viewIsDirty(mei.getEndpoint());
             }
         }
     }
     
     protected Map createSource(String name) {
         synchronized (flow) {
             Map componentFlow = (Map) flow.get(name);
             if (componentFlow == null) {
                 componentFlow = new ConcurrentHashMap();
                 flow.put(name, componentFlow);
             }
             return componentFlow;
         }
     }
 
     public void internalEndpointRegistered(EndpointEvent event) {
         String key = EndpointSupport.getUniqueKey(event.getEndpoint());
         createSource(key);
         super.internalEndpointRegistered(event);
     }
     
     public void externalEndpointRegistered(EndpointEvent event) {
         String key = EndpointSupport.getUniqueKey(event.getEndpoint());
         createSource(key);
         super.externalEndpointRegistered(event);
     }
     
     public void linkedEndpointRegistered(EndpointEvent event) {
         String key = EndpointSupport.getUniqueKey(event.getEndpoint());
         createSource(key);
         super.linkedEndpointRegistered(event);
     }
     
     public void componentInstalled(ComponentEvent event) {
         createSource(event.getComponent().getName());
     }
 
     public void componentStarted(ComponentEvent event) {
         createSource(event.getComponent().getName());
     }
 
     public void componentStopped(ComponentEvent event) {
     }
 
     public void componentShutDown(ComponentEvent event) {
     }
 
     public void componentUninstalled(ComponentEvent event) {
     }
 
     public boolean isDisplayComponents() {
         return displayComponents;
     }
 
     public void setDisplayComponents(boolean displayComponents) {
         this.displayComponents = displayComponents;
     }
 
 }
