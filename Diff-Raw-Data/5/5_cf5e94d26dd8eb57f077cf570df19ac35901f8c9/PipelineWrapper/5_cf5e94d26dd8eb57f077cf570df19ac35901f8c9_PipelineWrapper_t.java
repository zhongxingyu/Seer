 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.pipeline;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.okapi.applications.rainbow.Input;
 import net.sf.okapi.applications.rainbow.Project;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditorMapper;
 import net.sf.okapi.common.ParametersEditorMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.pipeline.IPipeline;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.pipeline.Pipeline;
 import net.sf.okapi.common.pipelinedriver.BatchItemContext;
 import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
 import net.sf.okapi.common.pipelinedriver.PipelineDriver;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.lib.plugins.PluginItem;
 import net.sf.okapi.lib.plugins.PluginsManager;
 
 public class PipelineWrapper {
 	
 	private Map<String, StepInfo> availableSteps;
 	private String path;
 	private ArrayList<StepInfo> steps;
 	private IPipelineDriver driver;
 	private IFilterConfigurationMapper fcMapper;
 	private IParametersEditorMapper peMapper;
 
 	public void addPlugins (List<PluginItem> plugins,
 		URLClassLoader classLoader)
 	{
 		try {
 			for ( PluginItem item : plugins ) {
				if ( item.getType() != PluginItem.TYPE_IPIPELINESTEP ) continue;
 				
 				IPipelineStep ps = (IPipelineStep)Class.forName(item.getClassName(), true, classLoader).newInstance();
 				IParameters params = ps.getParameters();
 				StepInfo stepInfo = new StepInfo(ps.getClass().getSimpleName(),
 					ps.getName(), ps.getDescription(), ps.getClass().getName(), classLoader,
 					(params==null) ? null : params.getClass().getName());
 				if ( params != null ) {
 					stepInfo.paramsData = params.toString();
 					if ( item.getEditorDescriptionProvider() != null ) {
 						peMapper.addDescriptionProvider(item.getEditorDescriptionProvider(), stepInfo.paramsClass);
 					}
 					if ( item.getParamsEditor() != null ) {
 						peMapper.addEditor(item.getParamsEditor(), stepInfo.paramsClass);
 					}
 				}
 				availableSteps.put(stepInfo.id, stepInfo);
 			}
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error when creating the plug-ins lists.", e);
 		}
 	}
 
 	/**
 	 * Populate the hard-wired steps.
 	 */
 	private void buildStepList () {
 		availableSteps = new LinkedHashMap<String, StepInfo>();
 		peMapper = new ParametersEditorMapper();
 		try {
 			IPipelineStep ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.common.RawDocumentToFilterEventsStep").newInstance();
 			StepInfo step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null, null);
 			IParameters params = ps.getParameters();
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			availableSteps.put(step.id, step);
 				
 			ps = (IPipelineStep)Class.forName(
 					"net.sf.okapi.steps.common.FilterEventsToRawDocumentStep").newInstance();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null, null);
 			params = ps.getParameters();
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			availableSteps.put(step.id, step);
 
 // Usable only via script			
 //			ps = (IPipelineStep)Class.forName(
 //					"net.sf.okapi.steps.common.FilterEventsWriterStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null, null);
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //			}
 //			availableSteps.put(step.id, step);
 
 // Usable only via script			
 //			ps = (IPipelineStep)Class.forName(
 //				"net.sf.okapi.steps.common.RawDocumentWriterStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null, null);
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //			}
 //			availableSteps.put(step.id, step);
 
 // Not ready			
 //			ps = (IPipelineStep)Class.forName(
 //				"net.sf.okapi.steps.encodingconversion.EncodingConversionStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 //				params.getClass().getName());
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //				peMapper.addEditor("net.sf.okapi.steps.encodingconversion.ui.ParametersEditor", step.paramsClass);
 //			}
 //			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.batchtranslation.BatchTranslationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.batchtranslation.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 // Not ready
 //			ps = (IPipelineStep)Class.forName(
 //				"net.sf.okapi.steps.bomconversion.BOMConversionStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 //				params.getClass().getName());
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //				peMapper.addEditor("net.sf.okapi.steps.bomconversion.ui.ParametersEditor", step.paramsClass);
 //			}
 //			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.charlisting.CharListingStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.charlisting.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.codesremoval.CodesRemovalStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.codesremoval.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 			 	"net.sf.okapi.steps.formatconversion.FormatConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.formatconversion.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.fullwidthconversion.FullWidthConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.fullwidthconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 			 	"net.sf.okapi.steps.generatesimpletm.GenerateSimpleTmStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.generatesimpletm.ParametersUI", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 			 	"net.sf.okapi.steps.leveraging.LeveragingStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.leveraging.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 // Not ready
 //			ps = (IPipelineStep)Class.forName(
 //				"net.sf.okapi.steps.linebreakconversion.LineBreakConversionStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 //				params.getClass().getName());
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //				peMapper.addEditor("net.sf.okapi.steps.linebreakconversion.ui.ParametersEditor", step.paramsClass);
 //				peMapper.addDescriptionProvider("net.sf.okapi.steps.linebreakconversion.Parameters", step.paramsClass);
 //			}
 //			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.searchandreplace.SearchAndReplaceStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.searchandreplace.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.segmentation.SegmentationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.segmentation.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.desegmentation.DesegmentationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.desegmentation.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.gcaligner.SentenceAlignerStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.gcaligner.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 			 	"net.sf.okapi.steps.simpletm2tmx.SimpleTM2TMXStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				null);
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.textmodification.TextModificationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.textmodification.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 		 		"net.sf.okapi.steps.tmimport.TMImportStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.tmimport.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.tokenization.TokenizationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.tokenization.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 			
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.translationcomparison.TranslationComparisonStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.translationcomparison.Parameters", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 // Not ready
 //			ps = (IPipelineStep)Class.forName(
 //				"net.sf.okapi.steps.uriconversion.UriConversionStep").newInstance();
 //			params = ps.getParameters();
 //			step = new StepInfo(ps.getClass().getSimpleName(),
 //				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 //				params.getClass().getName());
 //			if ( params != null ) {
 //				step.paramsData = params.toString();
 //				peMapper.addEditor("net.sf.okapi.steps.uriconversion.ui.ParametersEditor", step.paramsClass);
 //			}
 //			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.wordcount.WordCountStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.wordcount.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.xsltransform.XSLTransformStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null,
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.xsltransform.ui.ParametersEditor", step.paramsClass);
 			}
 			availableSteps.put(step.id, step);
 		}
 		catch ( InstantiationException e ) {
 			e.printStackTrace();
 		}
 		catch ( IllegalAccessException e ) {
 			e.printStackTrace();
 		}
 		catch ( ClassNotFoundException e ) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public PipelineWrapper (IFilterConfigurationMapper fcMapper,
 		String rootFolder)
 	{
 		this.fcMapper = fcMapper;
 		steps = new ArrayList<StepInfo>();
 		driver = new PipelineDriver();
 		driver.setFilterConfigurationMapper(this.fcMapper);
 		
 		// Hard-wired steps
 		buildStepList();
 
 		// Discover and add plug-ins
 		PluginsManager mgt = new PluginsManager();
 		mgt.discover(new File(rootFolder+File.separator+"dropins"), true);
 		addPlugins(mgt.getList(), mgt.getClassLoader());
 	}
 	
 	public void clear () {
 		steps.clear();		
 	}
 	
 	public String getPath () {
 		return path;
 	}
 	
 	public void setPath (String path) {
 		this.path = path;
 	}
 	
 	public IParametersEditorMapper getEditorMapper () {
 		return peMapper;
 	}
 	
 	public Map<String, StepInfo> getAvailableSteps () {
 		return availableSteps;
 	}
 	
 	public String getStringStorage () {
 		copyInfoStepsToPipeline();
 		PipelineStorage store = new PipelineStorage(availableSteps);
 		store.write(driver.getPipeline());
 		return store.getStringOutput();
 	}
 	
 	public void reset () {
 		clear();
 		path = null;
 		driver.setPipeline(new Pipeline());
 	}
 	
 	public void loadFromStringStorageOrReset (String data) {
 		if ( Util.isEmpty(data) ) {
 			reset();
 			return;
 		}
 		PipelineStorage store = new PipelineStorage(availableSteps, (CharSequence)data);
 		loadPipeline(store.read(), null);
 	}
 	
 	public void loadPipeline (IPipeline newPipeline,
 		String path)
 	{
 		driver.setPipeline(newPipeline);
 		// Set the info-steps
 		StepInfo infoStep;
 		IParameters params;
 		steps.clear();
 		for ( IPipelineStep step : driver.getPipeline().getSteps() ) {
 			infoStep = new StepInfo(step.getClass().getSimpleName(),
 				step.getName(), step.getDescription(),
 				step.getClass().getName(), step.getClass().getClassLoader(), null);
 			params = step.getParameters();
 			if ( params != null ) {
 				infoStep.paramsData = params.toString();
 				infoStep.paramsClass = params.getClass().getName();
 			}
 			steps.add(infoStep);
 		}
 		this.path = path;
 	}
 	
 	public void load (String path) {
 		PipelineStorage store = new PipelineStorage(availableSteps, path);
 		loadPipeline(store.read(), path);
 	}
 	
 	public void save (String path) {
 		PipelineStorage store = new PipelineStorage(availableSteps, path);
 		copyInfoStepsToPipeline();
 		store.write(driver.getPipeline());
 		this.path = path;
 	}
 	
 	private void copyInfoStepsToPipeline () {
 		try {
 			// Build the pipeline
 			driver.setPipeline(new Pipeline());
 			for ( StepInfo stepInfo : steps ) {
 				IPipelineStep step;
 				if ( stepInfo.loader == null ) {
 					step = (IPipelineStep)Class.forName(stepInfo.stepClass).newInstance();
 				}
 				else {
 					step = (IPipelineStep)Class.forName(stepInfo.stepClass,
 						true, stepInfo.loader).newInstance();
 				}
 				// Update the parameters with the one in the pipeline storage
 				IParameters params = step.getParameters();
 				if (( params != null ) && ( stepInfo.paramsData != null )) {
 					params.fromString(stepInfo.paramsData);
 				}
 				driver.addStep(step);
 			}
 		}
 		catch ( InstantiationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IllegalAccessException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ClassNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void copyParametersToPipeline (IPipeline pipeline) {
 		List<IPipelineStep> destSteps = pipeline.getSteps();
 		if ( destSteps.size() != steps.size() ) {
 			throw new RuntimeException("Parameters and destination do not match.");
 		}
 		StepInfo stepInfo;
 		for ( int i=0; i<destSteps.size(); i++ ) {
 			stepInfo = steps.get(i);
 			IParameters params = destSteps.get(i).getParameters();
 			if ( params != null ) {
 				params.fromString(stepInfo.paramsData);
 			}
 		}
 	}
 
 	public void execute (Project prj) {
 		copyInfoStepsToPipeline();
 		// Set the batch items
 		driver.clearItems();
 		//TODO: Replace this: driver.getPipeline().getContext().removeProperty("outputFile");
 		int f = -1;
 		URI outURI;
 		URI inpURI;
 		RawDocument rawDoc;
 		BatchItemContext bic;
 		int inputRequested = driver.getRequestedInputCount();
 		
 		for ( Input item : prj.getList(0) ) {
 			f++;
 			// Set the data for the first input of the batch item
 			outURI = (new File(prj.buildTargetPath(0, item.relativePath))).toURI();
 			inpURI = (new File(prj.getInputRoot(0) + File.separator + item.relativePath)).toURI();
 			rawDoc = new RawDocument(inpURI, prj.buildSourceEncoding(item),
 				prj.getSourceLanguage(), prj.getTargetLanguage());
 			rawDoc.setFilterConfigId(item.filterConfigId);
 			bic = new BatchItemContext(rawDoc, outURI, prj.buildTargetEncoding(item));
 			
 			// Add input/output data from other input lists if requested
 			for ( int j=1; j<3; j++ ) {
 				// Does the utility requests this list?
 				if ( j >= inputRequested ) break; // No need to loop more
 				// Do we have a corresponding input?
				if ( 3 > j ) {
 					// Data is available
 					List<Input> list = prj.getList(j);
 					// Make sure we have an entry for that list
 					if ( list.size() > f ) {
 						Input item2 = list.get(f);
 						// Input
 						outURI = (new File(prj.buildTargetPath(j, item2.relativePath))).toURI();
 						inpURI = (new File(prj.getInputRoot(j) + File.separator + item2.relativePath)).toURI();
 						rawDoc = new RawDocument(inpURI, prj.buildSourceEncoding(item),
 							prj.getSourceLanguage(), prj.getTargetLanguage());
 						rawDoc.setFilterConfigId(item2.filterConfigId);
 						bic.add(rawDoc, outURI, prj.buildTargetEncoding(item2));
 					}
 					// If no entry for that list: it'll be null
 				}
 				// Else: don't add anything
 				// The lists will return null and that is up to the utility to check.
 			}
 			
 			// Add the constructed batch item to the driver's list
 			driver.addBatchItem(bic);
 		}
 
 		// Execute
 		driver.processBatch();
 		
 		// Look if there 
 //TODO: Replace this		String path = driver.getPipeline().getContext().getString("outputFile");
 //		if ( path != null ) {
 //			UIUtil.start(path);
 //		}
 	}
 
 	public void addStep (StepInfo step) {
 		steps.add(step);
 	}
 	
 	public void insertStep (int index,
 		StepInfo step)
 	{
 		if ( index == -1 ) {
 			steps.add(step);
 		}
 		else {
 			steps.add(index, step);
 		}
 	}
 	
 	public void removeStep (int index) {
 		steps.remove(index);
 	}
 	
 	public List<StepInfo> getSteps () {
 		return steps;
 	}
 
 }
