 /*
  * Copyright (C) 2008 Herve Quiroz
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
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonBuilder;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.PipelineException;
 import org.trancecode.xproc.XProcXmlModel;
 import org.trancecode.xproc.binding.InlinePortBinding;
 import org.trancecode.xproc.port.EnvironmentPort;
 import org.trancecode.xproc.port.Port;
 import org.trancecode.xproc.port.XProcPorts;
 
 /**
  * @author Herve Quiroz
  */
 public final class TryStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
 {
     private static final Logger LOG = Logger.getLogger(TryStepProcessor.class);
 
     @Override
     public Step getStepDeclaration()
     {
         return Step.newStep(XProcSteps.TRY, this, true);
     }
 
     @Override
     public QName getStepType()
     {
         return XProcSteps.TRY;
     }
 
     @Override
     public Environment run(final Step step, final Environment environment)
     {
         LOG.trace("step = {}", step.getName());
         assert step.getSubpipeline().size() == 2;
 
         try
         {
             final Step groupStep = step.getSubpipeline().get(0);
             assert groupStep.getType().equals(XProcSteps.GROUP);
             final Environment resultEnvironment = groupStep.run(environment);
             return buildResultEnvironment(groupStep, resultEnvironment);
         }
         catch (final PipelineException e)
         {
             final Step catchStep = step.getSubpipeline().get(1);
             assert catchStep.getType().equals(XProcSteps.CATCH);
            final XdmNode errorDocument = newErrorDocument(environment, e.getMessage());
             final Port errorPortdeclaration = Port
                     .newInputPort(catchStep.getName(), XProcPorts.ERROR, step.getLocation()).setPrimary(false)
                     .setSequence(false).setPortBindings(new InlinePortBinding(errorDocument, catchStep.getLocation()));
             final EnvironmentPort errorPort = EnvironmentPort.newEnvironmentPort(errorPortdeclaration, environment);
             final Environment catchEnvironment = environment.addPorts(errorPort);
             final Environment resultEnvironment = catchStep.run(catchEnvironment);
             return buildResultEnvironment(catchStep, resultEnvironment);
         }
     }
 
     private static XdmNode newErrorDocument(final Environment environment, final Object... errors)
     {
         final Processor processor = environment.getPipelineContext().getProcessor();
         final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
         builder.startDocument();
         builder.startElement(XProcXmlModel.Elements.ERRORS);
 
         for (final Object error : errors)
         {
            builder.startElement(XProcXmlModel.Elements.ERRORS);
             builder.text(error.toString());
             builder.endElement();
         }
 
         builder.endElement();
         builder.endDocument();
 
         return builder.getNode();
     }
 
     private Environment buildResultEnvironment(final Step step, final Environment environment)
     {
         Environment resultEnvironment = environment;
         for (final Port port : step.getOutputPorts())
         {
             EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port.setStepName(step.getName()),
                     environment);
             environmentPort = environmentPort.pipe(environment.getEnvironmentPort(port));
             resultEnvironment = resultEnvironment.addPorts(environmentPort);
         }
 
         final Port primaryOutputPort = step.getPrimaryOutputPort();
         if (primaryOutputPort != null)
         {
             resultEnvironment = resultEnvironment.setDefaultReadablePort(step.getPortReference(primaryOutputPort
                     .getPortName()));
         }
 
         return resultEnvironment;
     }
 
     public static final class CatchStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
     {
         @Override
         public Step getStepDeclaration()
         {
             return Step.newStep(XProcSteps.CATCH, this, true);
         }
 
         @Override
         public QName getStepType()
         {
             return XProcSteps.CATCH;
         }
     }
 }
