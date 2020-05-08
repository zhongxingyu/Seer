 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xproc;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.net.URI;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathSelector;
 import net.sf.saxon.s9api.XdmAtomicValue;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmValue;
 import org.trancecode.annotation.ReturnsNullable;
 import org.trancecode.collection.TcMaps;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.binding.PortBinding;
 import org.trancecode.xproc.port.EnvironmentPort;
 import org.trancecode.xproc.port.Port;
 import org.trancecode.xproc.port.PortFunctions;
 import org.trancecode.xproc.port.PortReference;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.step.Step;
 import org.trancecode.xproc.variable.Variable;
 import org.trancecode.xproc.variable.Variables;
 
 /**
  * @author Herve Quiroz
  */
 public class Environment implements HasPipelineContext
 {
     private static final Logger LOG = Logger.getLogger(Environment.class);
 
     private static final QName ATTRIBUTE_NAME = new QName("name");
     private static final QName ATTRIBUTE_VALUE = new QName("value");
     private static final QName ELEMENT_PARAM = XProcXmlModel.xprocStepNamespace().newSaxonQName("param");
     private static final QName ELEMENT_RESULT = XProcXmlModel.xprocStepNamespace().newSaxonQName("result");
 
     private final EnvironmentPort defaultReadablePort;
     private final Map<QName, String> inheritedVariables;
     private final Map<QName, String> localVariables;
     private final PipelineContext configuration;
     private final Map<PortReference, EnvironmentPort> ports;
     private final Step pipeline;
     private final EnvironmentPort defaultParametersPort;
     private final EnvironmentPort xpathContextPort;
 
     private static Map<PortReference, EnvironmentPort> getPortsMap(final Iterable<EnvironmentPort> ports)
     {
         return Maps.uniqueIndex(ports, PortFunctions.portReference());
     }
 
     public static Environment newEnvironment(final Step pipeline, final PipelineContext configuration)
     {
         final Map<QName, String> variables = ImmutableMap.of();
         final Iterable<EnvironmentPort> ports = ImmutableList.of();
         return new Environment(pipeline, configuration, ports, null, null, null, variables, variables);
     }
 
     private Environment(final Step pipeline, final PipelineContext configuration,
             final Iterable<EnvironmentPort> ports, final EnvironmentPort defaultReadablePort,
             final EnvironmentPort defaultParametersPort, final EnvironmentPort xpathContextPort,
             final Map<QName, String> inheritedVariables, final Map<QName, String> localVariables)
     {
         this(pipeline, configuration, getPortsMap(ports), defaultReadablePort, defaultParametersPort, xpathContextPort,
                 inheritedVariables, localVariables);
     }
 
     private Environment(final Step pipeline, final PipelineContext configuration,
             final Map<PortReference, EnvironmentPort> ports, final EnvironmentPort defaultReadablePort,
             final EnvironmentPort defaultParametersPort, final EnvironmentPort xpathContextPort,
             final Map<QName, String> inheritedVariables, final Map<QName, String> localVariables)
     {
         this.pipeline = pipeline;
         this.configuration = configuration;
         this.ports = ImmutableMap.copyOf(ports);
         this.defaultReadablePort = defaultReadablePort;
         this.defaultParametersPort = defaultParametersPort;
         this.xpathContextPort = xpathContextPort;
         this.inheritedVariables = ImmutableMap.copyOf(inheritedVariables);
         this.localVariables = ImmutableMap.copyOf(localVariables);
     }
 
     private Environment setupStepEnvironment(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         Environment environment = setupInputPorts(step);
         environment = environment.setPrimaryInputPortAsDefaultReadablePort(step);
         environment = environment.setXPathContextPort(step);
         environment = environment.setDefaultParametersPort(step);
         environment = environment.setupVariables(step);
 
         return environment;
     }
 
     private Environment setupInputPorts(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();
 
         for (final Port port : step.getInputPorts())
         {
             EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port, this);
             if (port.getPortName().equals(XProcPorts.XPATH_CONTEXT) && port.getPortBindings().isEmpty()
                     && getXPathContextPort() != null)
             {
                 environmentPort = environmentPort.pipe(getXPathContextPort());
             }
             newPorts.put(port.portReference(), environmentPort);
         }
 
         for (final Port port : step.getOutputPorts())
         {
             if (port.portBindings().isEmpty())
             {
                 newPorts.put(port.portReference(), EnvironmentPort.newEnvironmentPort(port, this));
             }
         }
 
         return addPorts(newPorts);
     }
 
     public Environment setupOutputPorts(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         return setupOutputPorts(step, this);
     }
 
     public Environment setupOutputPorts(final Step step, final Environment sourceEnvironment)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();
 
         for (final Port port : step.getOutputPorts())
         {
             if (!ports.containsKey(port.portReference()))
             {
                 newPorts.put(port.portReference(), EnvironmentPort.newEnvironmentPort(port, sourceEnvironment));
             }
         }
 
         return addPorts(newPorts).setPrimaryOutputPortAsDefaultReadablePort(step, sourceEnvironment);
     }
 
     private Environment setPrimaryInputPortAsDefaultReadablePort(final Step step)
     {
         LOG.trace("{@method} step = {} ; type = {}", step.getName(), step.getType());
 
         final Port primaryInputPort = step.getPrimaryInputPort();
         LOG.trace("primaryInputPort = {}", primaryInputPort);
 
         if (primaryInputPort == null)
         {
             return this;
         }
 
         LOG.trace("new default readable port = {}", primaryInputPort);
 
         // if port is empty then pipe to existing default readable port
         final EnvironmentPort environmentPort = getEnvironmentPort(primaryInputPort);
         final EnvironmentPort nonEmptyEnvironmentPort;
         if (Iterables.isEmpty(environmentPort.portBindings()) && getDefaultReadablePort() != null)
         {
             nonEmptyEnvironmentPort = environmentPort.pipe(getDefaultReadablePort());
         }
         else
         {
             nonEmptyEnvironmentPort = environmentPort;
         }
 
         return addPorts(nonEmptyEnvironmentPort).setDefaultReadablePort(nonEmptyEnvironmentPort);
     }
 
     private Environment setPrimaryOutputPortAsDefaultReadablePort(final Step step, final Environment sourceEnvironment)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         final Port primaryOutputPort = step.getPrimaryOutputPort();
 
         if (primaryOutputPort == null)
         {
             return this;
         }
 
         LOG.trace("new default readable port = {}", primaryOutputPort);
 
         final EnvironmentPort environmentPort = getEnvironmentPort(primaryOutputPort);
         final EnvironmentPort nonEmptyEnvironmentPort;
         if (Iterables.isEmpty(environmentPort.portBindings()))
         {
             nonEmptyEnvironmentPort = environmentPort.pipe(sourceEnvironment.getDefaultReadablePort());
         }
         else
         {
             nonEmptyEnvironmentPort = environmentPort;
         }
 
         return addPorts(nonEmptyEnvironmentPort).setDefaultReadablePort(nonEmptyEnvironmentPort);
     }
 
     private Environment setXPathContextPort(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         final Port xpathContextPort = step.getXPathContextPort();
         if (xpathContextPort == null)
         {
             return this;
         }
 
         return setXPathContextPort(getEnvironmentPort(xpathContextPort));
     }
 
     private Environment setupVariables(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
         LOG.trace("variables = {}", Variables.getVariableNames(step.getVariables()));
 
         final Map<QName, String> allVariables = Maps.newHashMap(inheritedVariables);
         allVariables.putAll(localVariables);
 
         final Map<QName, String> newLocalVariables = Maps.newHashMap(localVariables);
         final List<XdmNode> newParameterNodes = Lists.newArrayList();
 
         for (final Variable variable : step.getVariables())
         {
             LOG.trace("variable = {}", variable);
             final String value;
             if (variable.getValue() != null)
             {
                 value = variable.getValue();
             }
             else if (variable.getSelect() == null)
             {
                 if (variable.isRequired())
                 {
                     throw XProcExceptions.xs0018(variable);
                 }
 
                 value = null;
             }
             else
             {
                 final PortBinding xpathPortBinding = variable.getPortBinding();
                 final XdmNode xpathContextNode;
                 if (xpathPortBinding != null)
                 {
                     xpathContextNode = Iterables.getOnlyElement(xpathPortBinding.newEnvironmentPortBinding(this)
                             .readNodes());
                 }
                 else
                 {
                     xpathContextNode = getXPathContextNode();
                 }
 
                 final XdmValue result = evaluateXPath(variable.getSelect(), getPipelineContext().getProcessor(),
                         xpathContextNode, allVariables, variable.getLocation());
                 final XdmItem resultNode = Iterables.getOnlyElement(result);
 
                 value = resultNode.getStringValue();
             }
 
             LOG.trace("{} = {}", variable.getName(), value);
 
             if (value != null)
             {
                 if (variable.isParameter())
                 {
                     final XdmNode parameterNode = newParameterElement(variable.getName(), value);
                     newParameterNodes.add(parameterNode);
                 }
                 else
                 {
                     allVariables.put(variable.getName(), value);
                     newLocalVariables.put(variable.getName(), value);
                 }
             }
         }
 
         final EnvironmentPort parametersPort = getDefaultParametersPort();
         final Environment resultEnvironment;
         if (newParameterNodes.isEmpty())
         {
             resultEnvironment = this;
         }
         else
         {
             assert parametersPort != null : step.toString();
             resultEnvironment = writeNodes(parametersPort, newParameterNodes);
         }
 
         return resultEnvironment.setLocalVariables(newLocalVariables);
     }
 
     private static XdmValue evaluateXPath(final String select, final Processor processor,
             final XdmNode xpathContextNode, final Map<QName, String> variables, final Location location)
     {
         LOG.trace("{@method} select = {} ; variables = {}", select, variables);
 
         try
         {
             final XPathCompiler xpathCompiler = processor.newXPathCompiler();
             for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
             {
                 if (variableEntry.getValue() != null)
                 {
                     xpathCompiler.declareVariable(variableEntry.getKey());
                 }
             }
 
             xpathCompiler.declareNamespace(XProcXmlModel.xprocNamespace().prefix(), XProcXmlModel.xprocNamespace()
                     .uri());
 
             final XPathSelector selector = xpathCompiler.compile(select).load();
             if (xpathContextNode != null)
             {
                 LOG.trace("xpathContextNode = {}", xpathContextNode);
                 selector.setContextItem(processor.newDocumentBuilder().build(xpathContextNode.asSource()));
             }
 
             for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
             {
                 if (variableEntry.getValue() != null)
                 {
                     LOG.trace("  {} = {}", variableEntry.getKey(), variableEntry.getValue());
                     selector.setVariable(variableEntry.getKey(), new XdmAtomicValue(variableEntry.getValue()));
                 }
             }
 
             final XdmValue result = selector.evaluate();
             LOG.trace("result = {}", result);
             return result;
         }
         catch (final SaxonApiException e)
         {
            final XProcException xprocException = XProcExceptions.xd0023(location, select, e.getMessage());
            xprocException.initCause(e);
            throw xprocException;
         }
     }
 
     public Environment newFollowingStepEnvironment(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         return newFollowingStepEnvironment().setupStepEnvironment(step);
     }
 
     public Environment newFollowingStepEnvironment()
     {
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, localVariables);
     }
 
     public Environment newChildStepEnvironment(final Step step)
     {
         LOG.trace("{@method} step = {}", step.getName());
 
         return newChildStepEnvironment().setupStepEnvironment(step);
     }
 
     public Environment newChildStepEnvironment()
     {
         final Map<QName, String> variables = ImmutableMap.of();
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, TcMaps.merge(inheritedVariables, localVariables), variables);
     }
 
     public Environment setLocalVariables(final Map<QName, String> localVariables)
     {
         assert localVariables != null;
 
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, TcMaps.merge(this.localVariables, localVariables));
     }
 
     public void setLocalVariable(final QName name, final String value)
     {
         localVariables.put(name, value);
     }
 
     public EnvironmentPort getDefaultReadablePort()
     {
         return defaultReadablePort;
     }
 
     public EnvironmentPort getXPathContextPort()
     {
         return xpathContextPort;
     }
 
     public Environment setXPathContextPort(final EnvironmentPort xpathContextPort)
     {
         assert xpathContextPort != null;
         assert ports.containsValue(xpathContextPort);
 
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, localVariables);
     }
 
     public PipelineContext getPipelineContext()
     {
         return configuration;
     }
 
     public String getVariable(final QName name, final String defaultValue)
     {
         assert name != null;
         LOG.trace("{@method} name = {} ; defaultValue = {}", name, defaultValue);
 
         final String value = getVariable(name);
         if (value != null)
         {
             return value;
         }
 
         return defaultValue;
     }
 
     public String getVariable(final QName name)
     {
         assert name != null;
         LOG.trace("{@method} name = {}", name);
 
         final String localValue = localVariables.get(name);
         if (localValue != null)
         {
             return localValue;
         }
 
         return inheritedVariables.get(name);
     }
 
     public Environment setDefaultReadablePort(final EnvironmentPort defaultReadablePort)
     {
         assert defaultReadablePort != null;
         assert ports.containsValue(defaultReadablePort);
         LOG.trace("{@method} defaultReadablePort = {}", defaultReadablePort);
 
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, localVariables);
     }
 
     public Environment setDefaultReadablePort(final PortReference portReference)
     {
         return setDefaultReadablePort(getPort(portReference));
     }
 
     public Map<PortReference, EnvironmentPort> getPorts()
     {
         return ports;
     }
 
     public EnvironmentPort getEnvironmentPort(final Port port)
     {
         return getEnvironmentPort(port.portReference());
     }
 
     public EnvironmentPort getEnvironmentPort(final PortReference portReference)
     {
         assert ports.containsKey(portReference) : "port = " + portReference + " ; ports = " + ports;
         return ports.get(portReference);
     }
 
     public Environment addPorts(final EnvironmentPort... ports)
     {
         return addPorts(ImmutableList.copyOf(ports));
     }
 
     public Environment addPorts(final Iterable<EnvironmentPort> ports)
     {
         assert ports != null;
         LOG.trace("{@method} ports = {}", ports);
 
         final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap(this.ports);
         newPorts.putAll(getPortsMap(ports));
 
         return new Environment(pipeline, configuration, newPorts, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, localVariables);
     }
 
     public Environment addPorts(final Map<PortReference, EnvironmentPort> ports)
     {
         assert ports != null;
         LOG.trace("{@method} ports = {}", ports);
 
         return new Environment(pipeline, configuration, TcMaps.merge(this.ports, ports), defaultReadablePort,
                 defaultParametersPort, xpathContextPort, inheritedVariables, localVariables);
     }
 
     public EnvironmentPort addEnvironmentPort(final Port port)
     {
         LOG.trace("{@method} port = {}", port);
         assert port.portReference().equals(port.portReference());
         assert !ports.containsKey(port.portReference());
         final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port, this);
         ports.put(port.portReference(), environmentPort);
         return environmentPort;
     }
 
     public Step getPipeline()
     {
         return pipeline;
     }
 
     public URI getBaseUri()
     {
         return URI.create(pipeline.getLocation().getSystemId());
     }
 
     public EnvironmentPort getDefaultParametersPort()
     {
         return defaultParametersPort;
     }
 
     private Environment setDefaultParametersPort(final Step step)
     {
         final Port port = step.getPrimaryParameterPort();
         if (port != null)
         {
             final EnvironmentPort environmentPort = getEnvironmentPort(port);
             assert environmentPort != null;
             return setDefaultParametersPort(environmentPort);
         }
 
         return this;
     }
 
     public Environment setDefaultParametersPort(final EnvironmentPort defaultParametersPort)
     {
         assert defaultParametersPort != null;
         assert ports.containsValue(defaultParametersPort);
         LOG.trace("{@method} defaultParametersPort = {}", defaultParametersPort);
 
         return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                 xpathContextPort, inheritedVariables, localVariables);
     }
 
     @ReturnsNullable
     public XdmNode getXPathContextNode()
     {
         // TODO cache
 
         final EnvironmentPort xpathContextPort = getXPathContextPort();
         if (xpathContextPort != null)
         {
             final Iterator<XdmNode> contextNodes = xpathContextPort.readNodes().iterator();
             if (contextNodes.hasNext())
             {
                 final XdmNode contextNode = contextNodes.next();
                 if (xpathContextPort.getDeclaredPort().getPortName().equals(XProcPorts.XPATH_CONTEXT))
                 {
                     // TODO XProc error
                     assert !contextNodes.hasNext();
                 }
 
                 return Saxon.asDocumentNode(contextNode, configuration.getProcessor());
             }
         }
 
         return Saxon.getEmptyDocument(configuration.getProcessor());
     }
 
     public XdmValue evaluateXPath(final String select)
     {
         assert select != null;
         LOG.trace("{@method} select = {}", select);
 
         final XdmNode xpathContextNode = getXPathContextNode();
         assert xpathContextNode != null;
         LOG.trace("xpathContextNode = {}", xpathContextNode);
 
         return evaluateXPath(select, xpathContextNode);
     }
 
     public XdmValue evaluateXPath(final String select, final XdmNode xpathContextNode)
     {
         assert select != null;
         LOG.trace("{@method} select = {}", select);
 
         // TODO slow
         final Map<QName, String> variables = TcMaps.merge(inheritedVariables, localVariables);
 
         try
         {
             final XPathCompiler xpathCompiler = configuration.getProcessor().newXPathCompiler();
             final String pipelineSystemId = getPipeline().getLocation().getSystemId();
             if (pipelineSystemId != null)
             {
                 xpathCompiler.setBaseURI(URI.create(pipelineSystemId));
             }
             for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
             {
                 if (variableEntry.getValue() != null)
                 {
                     xpathCompiler.declareVariable(variableEntry.getKey());
                 }
             }
 
             final XPathSelector selector = xpathCompiler.compile(select).load();
             selector.setContextItem(xpathContextNode);
 
             for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
             {
                 if (variableEntry.getValue() != null)
                 {
                     selector.setVariable(variableEntry.getKey(),
                             Saxon.getUntypedXdmItem(variableEntry.getValue(), configuration.getProcessor()));
                 }
             }
 
             return selector.evaluate();
         }
         catch (final Exception e)
         {
             throw new IllegalStateException("error while evaluating XPath query: " + select, e);
         }
     }
 
     private EnvironmentPort getPort(final PortReference portReference)
     {
         assert ports.containsKey(portReference) : "port = " + portReference.toString() + " ; ports = " + ports.keySet();
         return ports.get(portReference);
     }
 
     public Environment writeNodes(final PortReference portReference, final XdmNode... nodes)
     {
         return writeNodes(portReference, ImmutableList.copyOf(nodes));
     }
 
     public Environment writeNodes(final PortReference portReference, final Iterable<XdmNode> nodes)
     {
         LOG.trace("{@method} port = {}", portReference);
 
         return addPorts(getPort(portReference).writeNodes(nodes));
     }
 
     private Environment writeNodes(final EnvironmentPort port, final Iterable<XdmNode> nodes)
     {
         LOG.trace("{@method} port = {}", port);
 
         return addPorts(port.writeNodes(nodes));
     }
 
     public Iterable<XdmNode> readNodes(final PortReference portReference)
     {
         LOG.trace("{@method} port = {}", portReference);
         final Iterable<XdmNode> nodes = getPort(portReference).readNodes();
         LOG.trace("nodes = {}", nodes);
         return nodes;
     }
 
     public XdmNode readNode(final PortReference portReference)
     {
         return Iterables.getOnlyElement(readNodes(portReference));
     }
 
     public Map<QName, String> readParameters(final PortReference portReference)
     {
         final Map<QName, String> parameters = TcMaps.newSmallWriteOnceMap();
         for (final XdmNode parameterNode : readNodes(portReference))
         {
             final XPathCompiler xpathCompiler = getPipelineContext().getProcessor().newXPathCompiler();
             try
             {
                 final XPathSelector nameSelector = xpathCompiler.compile("string(//@name)").load();
                 nameSelector.setContextItem(parameterNode);
                 final String name = nameSelector.evaluateSingle().toString();
                 final XPathSelector valueSelector = xpathCompiler.compile("string(//@value)").load();
                 valueSelector.setContextItem(parameterNode);
                 final String value = valueSelector.evaluateSingle().toString();
                 // TODO name should be real QName
                 parameters.put(new QName(name), value);
             }
             catch (final SaxonApiException e)
             {
                 throw new PipelineException(e);
             }
         }
 
         return ImmutableMap.copyOf(parameters);
     }
 
     public XdmNode newParameterElement(final QName name, final String value)
     {
         Preconditions.checkNotNull(name);
         Preconditions.checkNotNull(value);
 
         final SaxonBuilder builder = new SaxonBuilder(configuration.getProcessor().getUnderlyingConfiguration());
         builder.startDocument();
         builder.startElement(ELEMENT_PARAM);
         builder.namespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel.xprocStepNamespace().uri());
         builder.attribute(ATTRIBUTE_NAME, name.toString());
         builder.attribute(ATTRIBUTE_VALUE, value);
         builder.text(value);
         builder.endElement();
         builder.endDocument();
 
         return builder.getNode();
     }
 
     public XdmNode newResultElement(final String value)
     {
         Preconditions.checkNotNull(value);
 
         final SaxonBuilder builder = new SaxonBuilder(configuration.getProcessor().getUnderlyingConfiguration());
         builder.startDocument();
         builder.startElement(ELEMENT_RESULT);
         builder.namespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel.xprocStepNamespace().uri());
         builder.text(value);
         builder.endElement();
         builder.endDocument();
 
         return builder.getNode();
     }
 }
