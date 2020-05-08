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
 package org.trancecode.xproc.parser;
 
 import org.trancecode.core.BinaryFunction;
 import org.trancecode.core.CollectionUtil;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.SaxonLocation;
 import org.trancecode.xml.SaxonUtil;
 import org.trancecode.xproc.PipelineException;
 import org.trancecode.xproc.Port;
 import org.trancecode.xproc.PortBinding;
 import org.trancecode.xproc.PortReference;
 import org.trancecode.xproc.Step;
 import org.trancecode.xproc.StepProcessor;
 import org.trancecode.xproc.Variable;
 import org.trancecode.xproc.XProcPorts;
 import org.trancecode.xproc.XProcSteps;
 import org.trancecode.xproc.Port.Type;
 import org.trancecode.xproc.binding.DocumentPortBinding;
 import org.trancecode.xproc.binding.EmptyPortBinding;
 import org.trancecode.xproc.binding.InlinePortBinding;
 import org.trancecode.xproc.binding.PipePortBinding;
 import org.trancecode.xproc.step.Pipeline;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import net.sf.saxon.s9api.DocumentBuilder;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XdmNode;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author Herve Quiroz
  * @version $Revision$
  */
 public class PipelineParser implements XProcXmlModel
 {
 	private static final Logger LOG = LoggerFactory.getLogger(PipelineParser.class);
 
 	private final Processor processor;
 	private final Source source;
 	private final Map<QName, Step> importedLibrary;
 	private final Map<QName, Step> localLibrary = Maps.newHashMap();
 	private final Map<QName, StepProcessor> stepProcessors;
 
 	private Step mainPipeline;
 	private XdmNode rootNode;
 
 
 	public PipelineParser(
 		final Processor processor, final Source source, final Map<QName, Step> library,
 		final Map<QName, StepProcessor> stepProcessors)
 	{
 		assert processor != null;
 		this.processor = processor;
 
 		assert source != null;
 		this.source = source;
 
 		assert library != null;
 		this.importedLibrary = library;
 
 		assert stepProcessors != null;
 		this.stepProcessors = stepProcessors;
 	}
 
 
 	public void parse()
 	{
 		try
 		{
 			final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
 			documentBuilder.setLineNumbering(true);
 			final XdmNode pipelineDocument = documentBuilder.build(source);
 			rootNode = SaxonUtil.getElement(pipelineDocument, ELEMENTS_ROOT);
 			if (rootNode.getNodeName().equals(ELEMENT_PIPELINE) || rootNode.getNodeName().equals(ELEMENT_DECLARE_STEP))
 			{
 				mainPipeline = parsePipeline(rootNode);
 			}
 			else if (rootNode.getNodeName().equals(ELEMENT_LIBRARY))
 			{
 				parseDeclareSteps(rootNode);
 			}
 			else
 			{
 				unsupportedElement(rootNode);
 			}
 		}
 		catch (final SaxonApiException e)
 		{
 			throw new PipelineException(e);
 		}
 	}
 
 
 	private void declareStep(final Step step)
 	{
 		localLibrary.put(step.getType(), step);
 	}
 
 
 	private void parseDeclareSteps(final XdmNode node)
 	{
 		for (final XdmNode stepNode : SaxonUtil.getElements(node, ELEMENTS_DECLARE_STEP_OR_PIPELINE))
 		{
 			final QName type = SaxonUtil.getAttributeAsQName(stepNode, ATTRIBUTE_TYPE);
 
 			if (stepProcessors.containsKey(type))
 			{
 				parseDeclareStep(stepNode);
 			}
 			else
 			{
 				parsePipeline(stepNode);
 			}
 		}
 	}
 
 
 	private void parseDeclareStep(final XdmNode stepNode)
 	{
 		final QName type = SaxonUtil.getAttributeAsQName(stepNode, ATTRIBUTE_TYPE);
 		LOG.trace("new step type: {}", type);
 
 		Step step = Step.newStep(type, stepProcessors.get(type), false);
 		step = parseDeclarePorts(stepNode, step);
 		step = parseVariables(stepNode, step);
 
 		declareStep(step);
 	}
 
 
 	private Step parseWithPorts(final Iterable<XdmNode> withPortNodes, final Step step)
 	{
 		return CollectionUtil.apply(step, withPortNodes, new BinaryFunction<Step, Step, XdmNode>()
 		{
 			@Override
 			public Step evaluate(final Step step, final XdmNode withPortNode)
 			{
 				return parseWithPort(withPortNode, step);
 			}
 		});
 	}
 
 
 	private Step parseWithOptionValue(final XdmNode stepNode, final Step step)
 	{
 		return CollectionUtil.apply(
 			step, SaxonUtil.getAttributes(stepNode).entrySet(),
 			new BinaryFunction<Step, Step, Map.Entry<QName, String>>()
 			{
 				@Override
 				public Step evaluate(final Step step, final Map.Entry<QName, String> attribute)
 				{
 					final QName name = attribute.getKey();
 					final String value = attribute.getValue();
 					if (name.getNamespaceURI().isEmpty() && !name.equals(ATTRIBUTE_NAME)
 						&& !name.equals(ATTRIBUTE_TYPE))
 					{
 						LOG.trace("{} = {}", name, value);
 						return step.withOptionValue(name, value);
 					}
 
 					return step;
 				}
 			});
 	}
 
 
 	private Step parseInstanceStepBindings(final XdmNode node, final Step step)
 	{
 		final Step stepWithPorts = parseWithPorts(SaxonUtil.getElements(node, ELEMENTS_PORTS), step);
 		final Step stepWithVariables = parseVariables(node, stepWithPorts);
 		final Step stepWithOptionValues = parseWithOptionValue(node, stepWithVariables);
 
 		return stepWithOptionValues;
 	}
 
 
 	private Step parseWithPort(final XdmNode portNode, final Step step)
 	{
 		final String portName;
 		if (portNode.getNodeName().equals(XProcXmlModel.ELEMENT_XPATH_CONTEXT))
 		{
 			portName = XProcPorts.XPATH_CONTEXT;
 		}
 		else
 		{
 			portName = portNode.getAttributeValue(ATTRIBUTE_PORT);
 		}
 		final Port port = step.getPort(portName);
 		assert port.isParameter() || port.getType().equals(getPortType(portNode)) : "port = " + port.getType()
 			+ " ; with-port = " + getPortType(portNode);
 
 		final String select = portNode.getAttributeValue(XProcXmlModel.ATTRIBUTE_SELECT);
 		LOG.trace("select = {}", select);
 
 		final Port configuredPort = port.setSelect(select).setPortBindings(parsePortBindings(portNode));
 
 		LOG.trace("step {} with port {}", step, port);
 
 		return step.withPort(configuredPort);
 	}
 
 
 	private String getPortName(final XdmNode portNode)
 	{
 		if (ELEMENTS_STANDARD_PORTS.contains(portNode.getNodeName()))
 		{
 			return portNode.getAttributeValue(ATTRIBUTE_PORT);
 		}
 
 		if (portNode.getNodeName().equals(ELEMENT_ITERATION_SOURCE))
 		{
 			return XProcPorts.ITERATION_SOURCE;
 		}
 
 		if (portNode.getNodeName().equals(ELEMENT_XPATH_CONTEXT))
 		{
 			return XProcPorts.XPATH_CONTEXT;
 		}
 
 		throw new IllegalStateException(portNode.getNodeName().toString());
 	}
 
 
 	private Iterable<PortBinding> parsePortBindings(final XdmNode portNode)
 	{
 		return Iterables.transform(
 			SaxonUtil.getElements(portNode, ELEMENTS_PORT_BINDINGS), new Function<XdmNode, PortBinding>()
 			{
 				@Override
 				public PortBinding apply(final XdmNode node)
 				{
 					return parsePortBinding(node);
 				}
 			});
 	}
 
 
 	private void unsupportedElement(final XdmNode node)
 	{
 		throw new IllegalStateException(node.getNodeName().toString());
 	}
 
 
 	private static Location getLocation(final XdmNode node)
 	{
 		// TODO cache locations per node (slower at parsing but less memory used)
 		return new SaxonLocation(node);
 	}
 
 
 	private Step getDeclaredStep(final QName name)
 	{
 		final Step fromLocalLibrary = localLibrary.get(name);
 		if (fromLocalLibrary != null)
 		{
 			return fromLocalLibrary;
 		}
 
 		final Step fromImportedLibrary = importedLibrary.get(name);
 		if (fromImportedLibrary != null)
 		{
 			return fromImportedLibrary;
 		}
 
 		throw new UnsupportedOperationException(name.toString());
 	}
 
 
 	private Step parseDeclarePort(final XdmNode portNode, final Step step)
 	{
 		final String portName;
 		if (portNode.getNodeName().equals(XProcXmlModel.ELEMENT_XPATH_CONTEXT))
 		{
 			portName = XProcPorts.XPATH_CONTEXT;
 		}
 		else
 		{
 			portName = portNode.getAttributeValue(ATTRIBUTE_PORT);
 		}
 		final Port.Type type = getPortType(portNode);
 
 		final Port port =
 			Port.newPort(step.getName(), portName, getLocation(portNode), type).setPrimary(
 				portNode.getAttributeValue(ATTRIBUTE_PRIMARY)).setSequence(
 				portNode.getAttributeValue(ATTRIBUTE_SEQUENCE)).setSelect(portNode.getAttributeValue(ATTRIBUTE_SELECT))
 				.setPortBindings(parsePortBindings(portNode));
 		LOG.trace("new port: {}", port);
 
 		return step.declarePort(port);
 	}
 
 
 	private static Type getPortType(final XdmNode node)
 	{
 		if (ELEMENTS_INPUT_PORTS.contains(node.getNodeName()))
 		{
 			return Type.INPUT;
 		}
 
 		if (node.getNodeName().equals(ELEMENT_INPUT))
 		{
 			if ("parameters".equals(node.getAttributeValue(ATTRIBUTE_KIND)))
 			{
 				return Type.PARAMETER;
 			}
 			else
 			{
 				return Type.INPUT;
 			}
 		}
 
 		return Type.OUTPUT;
 	}
 
 
 	private PortBinding parsePortBinding(final XdmNode portBindingNode)
 	{
 		if (portBindingNode.getNodeName().equals(ELEMENT_PIPE))
 		{
 			final String stepName = portBindingNode.getAttributeValue(ATTRIBUTE_STEP);
 			final String portName = portBindingNode.getAttributeValue(ATTRIBUTE_PORT);
 			return new PipePortBinding(new PortReference(stepName, portName), getLocation(portBindingNode));
 		}
 		else if (portBindingNode.getNodeName().equals(ELEMENT_EMPTY))
 		{
 			return new EmptyPortBinding(getLocation(portBindingNode));
 		}
 		else if (portBindingNode.getNodeName().equals(ELEMENT_DOCUMENT))
 		{
 			final String href = portBindingNode.getAttributeValue(ATTRIBUTE_HREF);
 			return new DocumentPortBinding(href, getLocation(portBindingNode));
 		}
 		else if (portBindingNode.getNodeName().equals(ELEMENT_INLINE))
 		{
 			final XdmNode inlineNode = SaxonUtil.getElement(portBindingNode);
 			return new InlinePortBinding(inlineNode, getLocation(portBindingNode));
 		}
 		else
 		{
 			throw new PipelineException("not supported: {}", portBindingNode.getNodeName());
 		}
 	}
 
 
 	private Step parseOption(final XdmNode node, final Step step)
 	{
 		LOG.trace("step = {}", step.getType());
 		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
 		Variable option = Variable.newOption(name, getLocation(node));
 
 		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
 		LOG.trace("name = {} ; select = {}", name, select);
 		option = option.setSelect(select);
 
 		final String required = node.getAttributeValue(ATTRIBUTE_REQUIRED);
 		LOG.trace("name = {} ; required = {}", name, required);
 		if (required != null)
 		{
 			option = option.setRequired(Boolean.parseBoolean(required));
 		}
 
 		return step.declareVariable(option);
 	}
 
 
 	private Step parseWithParam(final XdmNode node, final Step step)
 	{
		// TODO
		throw new UnsupportedOperationException();
 	}
 
 
 	private Step parseWithOption(final XdmNode node, final Step step)
 	{
 		LOG.trace("step = {}", step.getType());
 		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
 		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
 		LOG.trace("name = {} ; select = {}", name, select);
 		return step.withOption(name, select);
 	}
 
 
 	private Step parseDeclareVariable(final XdmNode node, final Step step)
 	{
 		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
 		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
 		Variable variable = Variable.newVariable(name);
 		variable = variable.setLocation(getLocation(node));
 		variable = variable.setSelect(select);
 		variable = variable.setRequired(true);
 		final PortBinding portBinding = Iterables.getOnlyElement(parsePortBindings(node), null);
 		variable = variable.setPortBinding(portBinding);
 
 		return step.declareVariable(variable);
 	}
 
 
 	private Step parsePipeline(final XdmNode node)
 	{
 		final String name = getStepName(node);
 		final QName type = SaxonUtil.getAttributeAsQName(node, ATTRIBUTE_TYPE);
 
 		Step pipeline = Pipeline.newPipeline(type).setName(name).setLocation(getLocation(node));
 
 		parseImports(node);
 		parseDeclareSteps(node);
 
 		pipeline = parseDeclarePorts(node, pipeline);
 		pipeline = Pipeline.addImplicitPorts(pipeline);
 		pipeline = parseVariables(node, pipeline);
 		pipeline = parseSteps(node, pipeline);
 
 		if (pipeline.getType() != null)
 		{
 			localLibrary.put(type, pipeline);
 		}
 
 		return pipeline;
 	}
 
 
 	private Step parseVariables(final XdmNode stepNode, final Step step)
 	{
 		return parseVariables(SaxonUtil.getElements(stepNode, ELEMENTS_VARIABLES), step);
 	}
 
 
 	private Step parseVariables(final Iterable<XdmNode> variableNodes, final Step step)
 	{
 		return CollectionUtil.apply(step, variableNodes, new BinaryFunction<Step, Step, XdmNode>()
 		{
 			@Override
 			public Step evaluate(final Step step, final XdmNode variableNode)
 			{
 				return parseVariable(variableNode, step);
 			}
 		});
 	}
 
 
 	private Step parseVariable(final XdmNode variableNode, final Step step)
 	{
 		if (variableNode.getNodeName().equals(XProcXmlModel.ELEMENT_WITH_OPTION))
 		{
 			return parseWithOption(variableNode, step);
 		}
 
 		if (variableNode.getNodeName().equals(XProcXmlModel.ELEMENT_WITH_PARAM))
 		{
 			return parseWithParam(variableNode, step);
 		}
 
 		if (variableNode.getNodeName().equals(XProcXmlModel.ELEMENT_VARIABLE))
 		{
 			return parseDeclareVariable(variableNode, step);
 		}
 
 		if (variableNode.getNodeName().equals(XProcXmlModel.ELEMENT_OPTION))
 		{
 			return parseOption(variableNode, step);
 		}
 
 		throw new IllegalStateException(variableNode.getNodeName().toString());
 	}
 
 
 	private Step parseDeclarePorts(final XdmNode stepNode, final Step step)
 	{
 		return parseDeclarePorts(SaxonUtil.getElements(stepNode, ELEMENTS_PORTS), step);
 	}
 
 
 	private Step parseDeclarePorts(final Iterable<XdmNode> declarePortNodes, final Step step)
 	{
 		return CollectionUtil.apply(step, declarePortNodes, new BinaryFunction<Step, Step, XdmNode>()
 		{
 			@Override
 			public Step evaluate(final Step step, final XdmNode withPortNode)
 			{
 				return parseDeclarePort(withPortNode, step);
 			}
 		});
 	}
 
 
 	private void parseImports(final XdmNode node)
 	{
 		for (final XdmNode importNode : SaxonUtil.getElements(node, ELEMENT_IMPORT))
 		{
 			parseImport(importNode);
 		}
 	}
 
 
 	private void parseImport(final XdmNode node)
 	{
 		final String href = node.getAttributeValue(ATTRIBUTE_HREF);
 		assert href != null;
 		LOG.trace("href = {}", href);
 		final Source librarySource;
 		try
 		{
 			librarySource = processor.getUnderlyingConfiguration().getURIResolver().resolve(href, source.getSystemId());
 		}
 		catch (final TransformerException e)
 		{
 			throw new PipelineException(e, "href = %s", href);
 		}
 		final PipelineParser parser = new PipelineParser(processor, librarySource, importedLibrary, stepProcessors);
 		parser.parse();
 		final Map<QName, Step> newLibrary = parser.getLibrary();
 		LOG.trace("new steps = {}", newLibrary.keySet());
 		localLibrary.putAll(newLibrary);
 	}
 
 
 	private Collection<QName> getSupportedStepTypes()
 	{
 		// TODO improve performance
 		final Collection<QName> types = new ArrayList<QName>();
 		types.addAll(localLibrary.keySet());
 		types.addAll(importedLibrary.keySet());
 		LOG.trace("types = {}", types);
 		return types;
 	}
 
 
 	private Step parseSteps(final XdmNode node, final Step compoundStep)
 	{
 		return compoundStep.setSubpipeline(parseInnerSteps(node));
 	}
 
 
 	private List<Step> parseInnerSteps(final XdmNode node)
 	{
 		return ImmutableList.copyOf(Iterables.transform(
 			SaxonUtil.getElements(node, getSupportedStepTypes()), new Function<XdmNode, Step>()
 			{
 				@Override
 				public Step apply(final XdmNode stepElement)
 				{
 					return parseInstanceStep(stepElement);
 				}
 			}));
 	}
 
 
 	private Step parseInstanceStep(final XdmNode node)
 	{
 		final String name = getStepName(node);
 		final QName type = node.getNodeName();
 		LOG.trace("name = {} ; type = {}", name, type);
 
 		final Step declaredStep = getDeclaredStep(type);
 		Step step = declaredStep.setName(name).setLocation(getLocation(node));
 		LOG.trace("new instance step: {}", step);
 
 		if (XProcSteps.WHEN_STEPS.contains(step.getType()))
 		{
 			// declare output ports
 			step = parseDeclarePorts(node, step);
 		}
 
 		return parseInstanceStep(node, step);
 	}
 
 
 	private Step parseInstanceStep(final XdmNode node, final Step step)
 	{
 		final Step stepWithBindings = parseInstanceStepBindings(node, step);
 
 		if (stepWithBindings.isCompoundStep())
 		{
 			return parseSteps(node, stepWithBindings);
 		}
 
 		return stepWithBindings;
 	}
 
 
 	private String getStepName(final XdmNode node)
 	{
 		final String explicitName = node.getAttributeValue(ATTRIBUTE_NAME);
 		if (explicitName != null && explicitName.length() > 0)
 		{
 			return explicitName;
 		}
 
 		return getImplicitName(node);
 	}
 
 
 	private String getImplicitName(final XdmNode node)
 	{
 		return "!" + getImplicitName(rootNode, node);
 	}
 
 
 	private String getImplicitName(final XdmNode rootNode, final XdmNode node)
 	{
 		if (rootNode == node || node.getParent() == null)
 		{
 			return "1";
 		}
 
 		final int index = getNodeIndex(node);
 		final XdmNode parentNode = node.getParent();
 
 		return getImplicitName(rootNode, parentNode) + "." + Integer.toString(index);
 	}
 
 
 	private int getNodeIndex(final XdmNode node)
 	{
 		final XdmNode parentNode = node.getParent();
 		if (parentNode == null)
 		{
 			return 1;
 		}
 
 		final List<XdmNode> childNodes = ImmutableList.copyOf(SaxonUtil.getElements(parentNode, getStepElements()));
 		assert childNodes.contains(node) : node.getNodeName();
 		return childNodes.indexOf(node) + 1;
 	}
 
 
 	private Collection<QName> getStepElements()
 	{
 		final Collection<QName> elements = Sets.newHashSet();
 		elements.addAll(getSupportedStepTypes());
 		elements.add(XProcXmlModel.ELEMENT_DECLARE_STEP);
 		elements.add(XProcXmlModel.ELEMENT_LIBRARY);
 		elements.add(XProcXmlModel.ELEMENT_PIPELINE);
 		return elements;
 	}
 
 
 	public Map<QName, Step> getLibrary()
 	{
 		return ImmutableMap.copyOf(localLibrary);
 	}
 
 
 	public Step getPipeline()
 	{
 		return mainPipeline;
 	}
 }
