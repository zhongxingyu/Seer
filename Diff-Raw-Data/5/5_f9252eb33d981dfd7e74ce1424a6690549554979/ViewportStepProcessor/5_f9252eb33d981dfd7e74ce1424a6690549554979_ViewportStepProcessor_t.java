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
 package org.trancecode.xproc.step;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 import java.util.EnumSet;
 import java.util.List;
 
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xml.saxon.SaxonProcessor;
 import org.trancecode.xml.saxon.SaxonProcessorDelegate;
 import org.trancecode.xml.saxon.SaxonProcessorDelegates;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcException;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.binding.InlinePortBinding;
 import org.trancecode.xproc.port.EnvironmentPort;
 import org.trancecode.xproc.port.Port;
 import org.trancecode.xproc.port.XProcPorts;
 import org.trancecode.xproc.variable.Variable;
 import org.trancecode.xproc.variable.XProcOptions;
 import org.trancecode.xproc.xpath.IterationPositionXPathExtensionFunction;
 
 /**
  * {@code p:viewport}.
  * 
  * @author Herve Quiroz
  * @see <a href="http://www.w3.org/TR/xproc/#p.viewport">p:viewport</a>
  */
 public final class ViewportStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(ViewportStepProcessor.class);
 
     @Override
     public Step getStepDeclaration()
     {
         return Step.newStep(XProcSteps.VIEWPORT, this, true)
                 .declareVariable(Variable.newOption(XProcOptions.MATCH).setRequired(true))
                 .declarePort(Port.newInputPort(XProcPorts.VIEWPORT_SOURCE).setSequence(false))
                 .declarePort(Port.newOutputPort(XProcPorts.RESULT).setSequence(false));
     }
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.VIEWPORT;
     }
 
     @Override
     public Environment run(final Step step, final Environment environment)
     {
         LOG.trace("step = {}", step.getName());
 
         final Environment viewportEnvironment = environment.newFollowingStepEnvironment(step);
 
         final List<XdmNode> sourceNodes = ImmutableList.copyOf(viewportEnvironment.readNodes(step
                 .getPortReference(XProcPorts.VIEWPORT_SOURCE)));
         if (sourceNodes.size() != 1)
         {
             throw XProcExceptions.xd0003(step.getLocation(), sourceNodes.size());
         }
         final XdmNode sourceDocument = sourceNodes.get(0);
 
         final String match = viewportEnvironment.getVariable(XProcOptions.MATCH);
         LOG.trace("match = {}", match);
 
         final SaxonProcessorDelegate runSubpipeline = new AbstractSaxonProcessorDelegate()
         {
             private int iterationPosition = 1;
 
             private void runSubpipeline(final XdmNode node, final SaxonBuilder builder)
             {
                 LOG.trace("run subpipeline on: {}", node.getNodeName());
                Environment subpipelineEnvironment = viewportEnvironment.newChildStepEnvironment();
                 final Port currentPort = Port.newInputPort(step.getName(), "current", step.getLocation())
                         .setPortBindings(new InlinePortBinding(node, step.getLocation()));
                 final EnvironmentPort currentEnvironmentPort = EnvironmentPort.newEnvironmentPort(currentPort,
                         viewportEnvironment);
                subpipelineEnvironment = subpipelineEnvironment.addPorts(currentEnvironmentPort);
                 final int previousIterationPosition = IterationPositionXPathExtensionFunction
                         .setIterationPosition(iterationPosition++);
                 Environment resultEnvironment;
                 try
                 {
                     resultEnvironment = runSteps(step.getSubpipeline(), subpipelineEnvironment);
                 }
                 finally
                 {
                     IterationPositionXPathExtensionFunction.setIterationPosition(previousIterationPosition);
                 }
                 resultEnvironment = resultEnvironment.setupOutputPorts(step, resultEnvironment);
                 final XdmNode resultNode = Iterables.getOnlyElement(resultEnvironment.getDefaultReadablePort()
                         .readNodes(), null);
                 if (resultNode != null)
                 {
                     builder.nodes(resultNode);
                 }
             }
 
             @Override
             public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
             {
                 runSubpipeline(node, builder);
                 return false;
             }
 
             @Override
             public void endDocument(final XdmNode node, final SaxonBuilder builder)
             {
                 // document has been started and ended
             }
 
             @Override
             public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
             {
                 runSubpipeline(node, builder);
                 return EnumSet.noneOf(NextSteps.class);
             }
 
             @Override
             public void endElement(final XdmNode node, final SaxonBuilder builder)
             {
                 // element has been started and ended
             }
         };
         final SaxonProcessorDelegate runSubpipelineForElements = SaxonProcessorDelegates.forNodeKinds(
                 ImmutableSet.of(XdmNodeKind.DOCUMENT, XdmNodeKind.ELEMENT), runSubpipeline,
                 SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                 {
                     @Override
                     public XProcException apply(final XdmNode node)
                     {
                         return XProcExceptions.xd0010(node);
                     }
                 }));
 
         final SaxonProcessor matchProcessor = new SaxonProcessor(environment.getPipelineContext().getProcessor(),
                 SaxonProcessorDelegates.forXsltMatchPattern(environment.getPipelineContext().getProcessor(), match,
                         step.getNode(), runSubpipelineForElements, new CopyingSaxonProcessorDelegate()));
 
         final XdmNode resultDocument = matchProcessor.apply(sourceDocument);
 
         Environment resultEnvironment = viewportEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT),
                 resultDocument);
         resultEnvironment = resultEnvironment.setupOutputPorts(step, environment);
         return resultEnvironment;
     }
 }
