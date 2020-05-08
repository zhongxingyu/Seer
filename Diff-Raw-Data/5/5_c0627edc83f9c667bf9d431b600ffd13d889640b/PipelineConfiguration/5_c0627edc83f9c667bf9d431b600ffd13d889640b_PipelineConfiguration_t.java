 /*
  * Copyright (C) 2007 Herve Quiroz
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
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import org.trancecode.concurrent.TaskExecutor;
 import org.trancecode.concurrent.TaskExecutors;
 import org.trancecode.io.DefaultInputResolver;
 import org.trancecode.io.DefaultOutputResolver;
 import org.trancecode.io.InputResolver;
 import org.trancecode.io.OutputResolver;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.step.CoreStepProcessor;
 import org.trancecode.xproc.step.Step;
 import org.trancecode.xproc.step.StepProcessor;
 import org.trancecode.xproc.step.XProcSteps;
 import org.trancecode.xproc.xpath.XPathExtensionFunction;
 
 /**
  * @author Herve Quiroz
  */
 public final class PipelineConfiguration extends AbstractPipelineContext
 {
     private static final String RESOURCE_PATH_XPROC_LIBRARY_1_0 = "/org/trancecode/xproc/xproc-1.0.xpl";
     private static final Map<QName, StepProcessor> DEFAULT_STEP_PROCESSORS = getDefaultStepProcessors();
     private static final Map<QName, Step> CORE_LIBRARY = getCoreLibrary();
     private static final Iterable<XPathExtensionFunction> EXTENSION_FUNCTIONS = ImmutableList.copyOf(ServiceLoader
             .load(XPathExtensionFunction.class));
     private static final URI DEFAULT_LIBRARY_URI = URI.create("trancecode:tubular:default-library.xpl");
     private static final Set<URI> EMPTY_SET_OF_URIS = ImmutableSet.of();
     private static final Logger LOG = Logger.getLogger(PipelineConfiguration.class);
 
    private static PipelineLibrary DEFAULT_PIPELINE_LIBRARY = getDefaultPipelineLibrary();
 
    private static PipelineLibrary getDefaultPipelineLibrary()
     {
         final Map<String, Object> properties = Maps.newHashMap();
         properties.put(PROPERTY_EXECUTOR, TaskExecutors.onDemandExecutor());
         final Processor processor = new Processor(false);
         properties.put(PROPERTY_PROCESSOR, processor);
         properties.put(PROPERTY_URI_RESOLVER, processor.getUnderlyingConfiguration().getURIResolver());
         properties.put(PROPERTY_PIPELINE_LIBRARY, new PipelineLibrary(DEFAULT_LIBRARY_URI, CORE_LIBRARY,
                 EMPTY_SET_OF_URIS));
         properties.put(PROPERTY_STEP_PROCESSORS, DEFAULT_STEP_PROCESSORS);
         final PipelineContext context = new ImmutablePipelineContext(properties);
         final URL xprocLibraryUrl = PipelineConfiguration.class.getResource(RESOURCE_PATH_XPROC_LIBRARY_1_0);
         final Source defaultLibrarySource;
         try
         {
             defaultLibrarySource = new StreamSource(xprocLibraryUrl.openStream(), xprocLibraryUrl.toString());
         }
         catch (final IOException e)
         {
             throw new IllegalStateException("cannot parse default library: " + xprocLibraryUrl, e);
         }
         final PipelineLibrary library = PipelineParser.parseLibrary(context, defaultLibrarySource);
         LOG.trace("supported steps: {}", library.getStepTypes());
         return library;
     }
 
     private static Map<QName, StepProcessor> getDefaultStepProcessors()
     {
         final Map<QName, StepProcessor> processors = Maps.newHashMap();
         final Map<QName, StepProcessor> availableProcessors = Maps.uniqueIndex(ServiceLoader.load(StepProcessor.class),
                 new Function<StepProcessor, QName>()
                 {
                     @Override
                     public QName apply(final StepProcessor stepProcessor)
                     {
                         return stepProcessor.getStepType();
                     }
                 });
         processors.putAll(availableProcessors);
         for (final QName stepType : Iterables.concat(XProcSteps.REQUIRED_STEPS, XProcSteps.OPTIONAL_STEPS))
         {
             if (!processors.containsKey(stepType))
             {
                 processors.put(stepType, new StepProcessor()
                 {
                     @Override
                     public QName getStepType()
                     {
                         return stepType;
                     }
 
                     @Override
                     public Environment run(final Step step, final Environment environment)
                     {
                         throw new UnsupportedOperationException("step not supported: " + stepType);
                     }
                 });
             }
         }
 
         return ImmutableMap.copyOf(processors);
     }
 
     private static Map<QName, Step> getCoreLibrary()
     {
         final Map<QName, Step> coreSteps = Maps.newHashMap();
         for (final CoreStepProcessor coreStepProcessor : ServiceLoader.load(CoreStepProcessor.class))
         {
             coreSteps.put(coreStepProcessor.getStepType(), coreStepProcessor.getStepDeclaration());
         }
 
         return ImmutableMap.copyOf(coreSteps);
     }
 
     public PipelineConfiguration()
     {
         this(new Processor(false));
     }
 
     private static Map<String, Object> newEmptyPropertiesMap()
     {
         return Maps.newHashMap();
     }
 
     public PipelineConfiguration(final Processor processor)
     {
         super(newEmptyPropertiesMap());
         getProperties().put(PROPERTY_EXECUTOR, TaskExecutors.onDemandExecutor());
         getProperties().put(PROPERTY_INPUT_RESOLVER, DefaultInputResolver.INSTANCE);
         getProperties().put(PROPERTY_OUTPUT_RESOLVER, DefaultOutputResolver.INSTANCE);
         getProperties().put(PROPERTY_PIPELINE_LIBRARY, DEFAULT_PIPELINE_LIBRARY);
         getProperties().put(PROPERTY_PROCESSOR, Preconditions.checkNotNull(processor));
         getProperties().put(PROPERTY_STEP_PROCESSORS, Maps.newHashMap(DEFAULT_STEP_PROCESSORS));
         getProperties().put(PROPERTY_URI_RESOLVER, processor.getUnderlyingConfiguration().getURIResolver());
         for (final XPathExtensionFunction function : EXTENSION_FUNCTIONS)
         {
             LOG.trace("register XPath extension function: {}", function);
             processor.registerExtensionFunction(function.getExtensionFunctionDefinition());
         }
     }
 
     public void setExecutor(final TaskExecutor executor)
     {
         getProperties().put(PROPERTY_EXECUTOR, Preconditions.checkNotNull(executor));
     }
 
     public void setExecutor(final ExecutorService executor)
     {
         setExecutor(TaskExecutors.forExecutorService(executor));
     }
 
     public void setInputResolver(final InputResolver inputResolver)
     {
         getProperties().put(PROPERTY_INPUT_RESOLVER, Preconditions.checkNotNull(inputResolver));
     }
 
     public void setUriResolver(final URIResolver uriResolver)
     {
         getProperties().put(PROPERTY_URI_RESOLVER, Preconditions.checkNotNull(uriResolver));
     }
 
     public void setOutputResolver(final OutputResolver outputResolver)
     {
         getProperties().put(PROPERTY_OUTPUT_RESOLVER, Preconditions.checkNotNull(outputResolver));
     }
 
     public void registerStepProcessor(final StepProcessor stepProcessor)
     {
         Preconditions.checkNotNull(stepProcessor);
         final Map<QName, StepProcessor> stepProcessors = getProperty(PROPERTY_STEP_PROCESSORS);
         stepProcessors.put(stepProcessor.getStepType(), stepProcessor);
     }
 
     public void registerPipelineLibrary(final PipelineLibrary library)
     {
         Preconditions.checkNotNull(library);
         getProperties().put(PROPERTY_PIPELINE_LIBRARY, library.importLibrary(getPipelineLibrary()));
     }
 
     public void setProperty(final String name, final Object value)
     {
         properties.put(name, value);
     }
 }
