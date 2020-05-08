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
 
 import org.trancecode.annotation.ReturnsNullable;
 import org.trancecode.core.CollectionUtil;
 import org.trancecode.xml.SaxonUtil;
 import org.trancecode.xproc.step.Pipeline;
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathSelector;
 import net.sf.saxon.s9api.XdmAtomicValue;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmValue;
 
 import org.slf4j.ext.XLogger;
 import org.slf4j.ext.XLoggerFactory;
 
 
 /**
  * @author Herve Quiroz
  * @version $Revision$
  */
 public class Environment
 {
 	private static final XLogger LOG = XLoggerFactory.getXLogger(Environment.class);
 	private static final Map<QName, String> EMPTY_VARIABLES_MAP = Collections.emptyMap();
 	private static final Map<PortReference, EnvironmentPort> EMPTY_PORTS_MAP = Collections.emptyMap();
 	private static final Iterable<EnvironmentPort> EMPTY_PORTS_LIST = Collections.emptyList();
 
 	private static final Function<EnvironmentPort, PortReference> FUNCTION_GET_PORT_REFERENCE =
 		new Function<EnvironmentPort, PortReference>()
 		{
 			@Override
 			public PortReference apply(final EnvironmentPort environmentPort)
 			{
 				return environmentPort.getDeclaredPort().getPortReference();
 			}
 		};
 
 	private static final Predicate<Port> PREDICATE_HAS_PORT_BINDINGS = new Predicate<Port>()
 	{
 		@Override
 		public boolean apply(final Port port)
 		{
 			return !port.getPortBindings().isEmpty();
 		}
 	};
 
 	private final EnvironmentPort defaultReadablePort;
 	private final Map<QName, String> inheritedVariables;
 	private final Map<QName, String> localVariables;
 	private final Configuration configuration;
 	private final Map<PortReference, EnvironmentPort> ports;
 	private final Pipeline pipeline;
 	private final EnvironmentPort defaultParametersPort;
 	private final EnvironmentPort xpathContextPort;
 
 
 	private static Map<PortReference, EnvironmentPort> getPortsMap(final Iterable<EnvironmentPort> ports)
 	{
 		return Maps.uniqueIndex(ports, FUNCTION_GET_PORT_REFERENCE);
 	}
 
 
 	public static Environment newEnvironment(final Pipeline pipeline, final Configuration configuration)
 	{
 		return new Environment(pipeline, configuration, EMPTY_PORTS_LIST, null, null, null, EMPTY_VARIABLES_MAP,
 			EMPTY_VARIABLES_MAP);
 	}
 
 
 	private Environment(
 		final Pipeline pipeline, final Configuration configuration, final Iterable<EnvironmentPort> ports,
 		final EnvironmentPort defaultReadablePort, final EnvironmentPort defaultParametersPort,
 		final EnvironmentPort xpathContextPort, final Map<QName, String> inheritedVariables,
 		final Map<QName, String> localVariables)
 	{
 		this(pipeline, configuration, getPortsMap(ports), defaultReadablePort, defaultParametersPort, xpathContextPort,
 			inheritedVariables, localVariables);
 	}
 
 
 	private Environment(
 		final Pipeline pipeline, final Configuration configuration, final Map<PortReference, EnvironmentPort> ports,
 		final EnvironmentPort defaultReadablePort, final EnvironmentPort defaultParametersPort,
 		final EnvironmentPort xpathContextPort, final Map<QName, String> inheritedVariables,
 		final Map<QName, String> localVariables)
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
 		LOG.trace("step = {}", step.getName());
 
 		return setupInputPorts(step).setPrimaryInputPortAsDefaultReadablePort(step).setXPathContextPort(step)
 			.setupVariables(step);
 	}
 
 
 	private Environment setupInputPorts(final Step step)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();
 
 		for (final Port port : step.getInputPorts())
 		{
 			newPorts.put(port.getPortReference(), EnvironmentPort.newEnvironmentPort(port, this));
 		}
 
 		for (final Port port : step.getOutputPorts())
 		{
 			if (port.portBindings.isEmpty())
 			{
 				newPorts.put(port.getPortReference(), EnvironmentPort.newEnvironmentPort(port, this));
 			}
 		}
 
 		return addPorts(newPorts);
 	}
 
 
 	public Environment setupOutputPorts(final Step step)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		return setupOutputPorts(step, this);
 	}
 
 
 	public Environment setupOutputPorts(final Step step, final Environment sourceEnvironment)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();
 
 		for (final Port port : step.getOutputPorts())
 		{
 			if (!ports.containsKey(port.getPortReference()))
 			{
 				newPorts.put(port.getPortReference(), EnvironmentPort.newEnvironmentPort(port, sourceEnvironment));
 			}
 		}
 
 		return addPorts(newPorts).setPrimaryOutputPortAsDefaultReadablePort(step, sourceEnvironment);
 	}
 
 
 	private Environment setPrimaryInputPortAsDefaultReadablePort(final Step step)
 	{
 		LOG.trace("step = {} ; type = {}", step.getName(), step.getType());
 
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
 		if (Iterables.isEmpty(environmentPort.portBindings))
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
 		LOG.trace("step = {}", step.getName());
 
 		final Port primaryOutputPort = step.getPrimaryOutputPort();
 
 		if (primaryOutputPort == null)
 		{
 			return this;
 		}
 
 		LOG.trace("new default readable port = {}", primaryOutputPort);
 
 		final EnvironmentPort environmentPort = getEnvironmentPort(primaryOutputPort);
 		final EnvironmentPort nonEmptyEnvironmentPort;
 		if (Iterables.isEmpty(environmentPort.portBindings))
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
 		LOG.trace("step = {}", step.getName());
 
 		final Port xpathContextPort = step.getXPathContextPort();
 		if (xpathContextPort == null)
 		{
 			return this;
 		}
 
 		return setXPathContextPort(getEnvironmentPort(xpathContextPort));
 	}
 
 
 	private Environment setupVariables(final Step step)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		final Map<QName, String> allVariables = Maps.newHashMap(inheritedVariables);
 		allVariables.putAll(localVariables);
 
 		final Map<QName, String> newLocalVariables = Maps.newHashMap(localVariables);
 
 		for (final Variable variable : step.getVariables())
 		{
 			final String value;
 			if (variable.getValue() != null)
 			{
 				value = variable.getValue();
 			}
 			else
 			{
 				value =
 					SaxonUtil.evaluateXPath(
 						variable.getSelect(), getConfiguration().getProcessor(), getXPathContextNode(), allVariables,
 						variable.getLocation()).toString();
 			}
 
 			LOG.trace("{} = {}", variable.getName(), value);
 
 			if (variable.isParameter())
 			{
 				final EnvironmentPort parametersPort = getDefaultParametersPort();
 				assert parametersPort != null;
 				final XdmNode parameterNode =
 					XProcUtil.newParameterElement(variable.getName(), value, getConfiguration().getProcessor());
 				parametersPort.writeNodes(parameterNode);
 			}
 			else
 			{
 				allVariables.put(variable.getName(), value);
 				newLocalVariables.put(variable.getName(), value);
 			}
 		}
 
 		return setLocalVariables(newLocalVariables);
 	}
 
 
 	public Environment newFollowingStepEnvironment(final Step step)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		return newFollowingStepEnvironment().setupStepEnvironment(step);
 	}
 
 
 	public Environment newFollowingStepEnvironment()
 	{
 		return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
 			xpathContextPort, inheritedVariables, localVariables);
 	}
 
 
 	public Environment newChildStepEnvironment(final Step step)
 	{
 		LOG.trace("step = {}", step.getName());
 
 		return newChildStepEnvironment().setupStepEnvironment(step);
 	}
 
 
 	public Environment newChildStepEnvironment()
 	{
 		return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
 			xpathContextPort, CollectionUtil.merge(inheritedVariables, localVariables), EMPTY_VARIABLES_MAP);
 	}
 
 
 	public Environment setLocalVariables(final Map<QName, String> localVariables)
 	{
 		assert localVariables != null;
 
 		return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
 			xpathContextPort, inheritedVariables, CollectionUtil.merge(this.localVariables, localVariables));
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
 
 
 	public Configuration getConfiguration()
 	{
 		return configuration;
 	}
 
 
 	public String getVariable(final QName name, final String defaultValue)
 	{
 		assert name != null;
 		LOG.trace("name = {} ; defaultValue = {}", name, defaultValue);
 
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
 		LOG.trace("name = {}", name);
 
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
 		LOG.trace("defaultReadablePort = {}", defaultReadablePort);
 
 		return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
 			xpathContextPort, inheritedVariables, localVariables);
 	}
 
 
 	public Map<PortReference, EnvironmentPort> getPorts()
 	{
 		return ports;
 	}
 
 
 	public EnvironmentPort getEnvironmentPort(final Port port)
 	{
 		return getEnvironmentPort(port.getPortReference());
 	}
 
 
 	public EnvironmentPort getEnvironmentPort(final String stepName, final String portName)
 	{
 		return ports.get(new PortReference(stepName, portName));
 	}
 
 
 	private EnvironmentPort getEnvironmentPort(final PortReference portReference)
 	{
 		assert ports.containsKey(portReference) : "port = " + portReference + " ; ports = " + ports;
 		return ports.get(portReference);
 	}
 
 
 	public Environment addPorts(final EnvironmentPort... ports)
 	{
 		return addPorts(Arrays.asList(ports));
 	}
 
 
 	public Environment addPorts(final Iterable<EnvironmentPort> ports)
 	{
 		assert ports != null;
 		LOG.trace("ports = {}", ports);
 
 		final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap(this.ports);
 		newPorts.putAll(getPortsMap(ports));
 
 		return new Environment(pipeline, configuration, newPorts, defaultReadablePort, defaultParametersPort,
 			xpathContextPort, inheritedVariables, localVariables);
 	}
 
 
 	public Environment addPorts(final Map<PortReference, EnvironmentPort> ports)
 	{
 		assert ports != null;
 		LOG.trace("ports = {}", ports);
 
 		return new Environment(pipeline, configuration, CollectionUtil.merge(this.ports, ports), defaultReadablePort,
 			defaultParametersPort, xpathContextPort, inheritedVariables, localVariables);
 	}
 
 
 	public EnvironmentPort addEnvironmentPort(final Port port)
 	{
 		LOG.entry(port);
 		assert port.getPortReference().equals(port.getPortReference());
 		assert !ports.containsKey(port.getPortReference());
 		final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port, this);
 		ports.put(port.getPortReference(), environmentPort);
 		return environmentPort;
 	}
 
 
 	public Pipeline getPipeline()
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
 
 
 	public Environment setDefaultParametersPort(final EnvironmentPort defaultParametersPort)
 	{
 		assert defaultParametersPort != null;
 		assert ports.containsValue(defaultParametersPort);
 		LOG.trace("defaultParametersPort = {}", defaultParametersPort);
 
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
 			if (!contextNodes.hasNext())
 			{
 				return null;
 			}
 
 			final XdmNode contextNode = contextNodes.next();
 			if (xpathContextPort.getDeclaredPort().getPortName().equals(XProcPorts.XPATH_CONTEXT))
 			{
 				// TODO XProc error
 				assert !contextNodes.hasNext();
 			}
 
 			return contextNode;
 		}
 
 		return null;
 	}
 
 
 	public XdmValue evaluateXPath(final String select)
 	{
 		assert select != null;
 		LOG.entry(select);
 
 		// TODO slow
 		final Map<QName, String> variables = CollectionUtil.merge(inheritedVariables, localVariables);
 
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
 			final XdmNode xpathContextNode = getXPathContextNode();
 			if (xpathContextNode != null)
 			{
 				LOG.trace("xpathContextNode = {}", xpathContextNode);
 				selector.setContextItem(xpathContextNode);
 			}
 
 			for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
 			{
 				if (variableEntry.getValue() != null)
 				{
 					selector.setVariable(variableEntry.getKey(), new XdmAtomicValue(variableEntry.getValue()));
 				}
 			}
 
 			return selector.evaluate();
 		}
 		catch (final Exception e)
 		{
 			throw new PipelineException(e, "error while evaluating XPath query: %s", select);
 		}
 	}
 
 
 	private EnvironmentPort getPort(final PortReference portReference)
 	{
 		assert ports.containsKey(portReference) : portReference.toString();
 		return ports.get(portReference);
 	}
 
 
 	private EnvironmentPort getPort(final String stepName, final String portName)
 	{
 		return getPort(new PortReference(stepName, portName));
 	}
 
 
 	public Environment writeNodes(final String stepName, final String portName, final XdmNode... nodes)
 	{
 		return writeNodes(stepName, portName, ImmutableList.of(nodes));
 	}
 
 
 	public Environment writeNodes(final String stepName, final String portName, final Iterable<XdmNode> nodes)
 	{
 		LOG.trace("stepName = {} ; portName = {}", stepName, portName);
 
 		return addPorts(getPort(stepName, portName).writeNodes(nodes));
 	}
 
 
 	public Iterable<XdmNode> readNodes(final String stepName, final String portName)
 	{
 		return getPort(stepName, portName).readNodes();
 	}
 
 
 	public XdmNode readNode(final String stepName, final String portName)
 	{
 		return Iterables.getOnlyElement(getPort(stepName, portName).readNodes());
 	}
 
 
	public Map<QName, String> readParameters(final String stepName, final String portName, final Environment environment)
 	{
 		final Map<QName, String> parameters = CollectionUtil.newSmallWriteOnceMap();
 		for (final XdmNode parameterNode : readNodes(stepName, portName))
 		{
			final XPathCompiler xpathCompiler = environment.getConfiguration().getProcessor().newXPathCompiler();
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
 
 		return parameters;
 	}
 }
