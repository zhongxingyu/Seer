 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.okapi.applications.rainbow.Input;
 import net.sf.okapi.applications.rainbow.Project;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditorMapper;
 import net.sf.okapi.common.ParametersEditorMapper;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.pipeline.Pipeline;
 import net.sf.okapi.common.pipelinedriver.BatchItemContext;
 import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
 import net.sf.okapi.common.pipelinedriver.PipelineContext;
 import net.sf.okapi.common.pipelinedriver.PipelineDriver;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.ui.UIUtil;
 
 public class PipelineWrapper {
 	
 	public final Map<String, StepInfo> availableSteps;
 	
 	private String path;
 	private ArrayList<StepInfo> steps;
 	private IPipelineDriver driver;
 	private IFilterConfigurationMapper fcMapper;
 	private IParametersEditorMapper peMapper;
 
 	// Temporary class to create a list of available steps
 	private Map<String, StepInfo> buildStepList () {
 		LinkedHashMap<String, StepInfo> map = new LinkedHashMap<String, StepInfo>();
 		peMapper = new ParametersEditorMapper();
 		try {
 //TODO: Replace by auto-discovery of plugins			
 			IPipelineStep ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.common.RawDocumentToFilterEventsStep").newInstance();
 			StepInfo step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null);
 			IParameters params = ps.getParameters();
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			map.put(step.id, step);
 				
 			ps = (IPipelineStep)Class.forName(
 					"net.sf.okapi.steps.common.FilterEventsToRawDocumentStep").newInstance();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null);
 			params = ps.getParameters();
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			map.put(step.id, step);
 							
 			ps = (IPipelineStep)Class.forName(
 					"net.sf.okapi.steps.common.FilterEventsWriterStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null);
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.common.RawDocumentWriterStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(), null);
 			if ( params != null ) {
 				step.paramsData = params.toString();
 			}
 			map.put(step.id, step);
 									
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.bomconversion.BOMConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.bomconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.charlisting.CharListingStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.charlisting.ParametersUI", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.xsltransform.XSLTransformStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.xsltransform.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 		
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.linebreakconversion.LineBreakConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.linebreakconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.segmentation.SegmentationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.segmentation.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.textmodification.TextModificationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.textmodification.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.translationcomparison.TranslationComparisonStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.translationcomparison.ParametersUI", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.fullwidthconversion.FullWidthConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.fullwidthconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.searchandreplace.SearchAndReplaceStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 			ps.getName(), ps.getDescription(), ps.getClass().getName(),
 			params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.searchandreplace.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.uriconversion.UriConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.uriconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 			
 			ps = (IPipelineStep)Class.forName(
 				"net.sf.okapi.steps.encodingconversion.EncodingConversionStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.encodingconversion.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 						
 			ps = (IPipelineStep)Class.forName(
 			 	"net.sf.okapi.steps.generatesimpletm.GenerateSimpleTmStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addDescriptionProvider("net.sf.okapi.steps.generatesimpletm.ParametersUI", step.paramsClass);
 			}
 			map.put(step.id, step);
 
/*			ps = (IPipelineStep)Class.forName(
 			"net.sf.okapi.steps.tokenization.TokenizationStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.tokenization.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
 					
 
 			ps = (IPipelineStep)Class.forName(
 			"net.sf.okapi.steps.wordcount.WordCountStep").newInstance();
 			params = ps.getParameters();
 			step = new StepInfo(ps.getClass().getSimpleName(),
 				ps.getName(), ps.getDescription(), ps.getClass().getName(),
 				params.getClass().getName());
 			if ( params != null ) {
 				step.paramsData = params.toString();
 				peMapper.addEditor("net.sf.okapi.steps.wordcount.ui.ParametersEditor", step.paramsClass);
 			}
 			map.put(step.id, step);
*/					
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
 		return map;
 	}
 	
 	public PipelineWrapper (IFilterConfigurationMapper fcMapper) {
 		this.fcMapper = fcMapper;
 		steps = new ArrayList<StepInfo>();
 		driver = new PipelineDriver();
 		//TODO: use register system for this
 		availableSteps = buildStepList();
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
 	
 	public void load (String path) {
 		PipelineStorage store = new PipelineStorage();
 		store.setPath(path);
 		driver.setPipeline(store.read());
 		// Set the info-steps
 		StepInfo infoStep;
 		IParameters params;
 		steps.clear();
 		for ( IPipelineStep step : driver.getPipeline().getSteps() ) {
 			infoStep = new StepInfo(step.getClass().getSimpleName(),
 				step.getName(), step.getDescription(),
 				step.getClass().getName(), null);
 			params = step.getParameters();
 			if ( params != null ) {
 				infoStep.paramsData = params.toString();
 				infoStep.paramsClass = params.getClass().getName();
 			}
 			steps.add(infoStep);
 		}
 		this.path = path;
 	}
 	
 	public void save (String path) {
 		PipelineStorage store = new PipelineStorage();
 		copyInfoStepsToPipeline();
 		store.setPath(path);
 		store.write(driver.getPipeline());
 		this.path = path;
 	}
 	
 	private void copyInfoStepsToPipeline () {
 		try {
 			// Build the pipeline
 			driver.setPipeline(new Pipeline());
 			// FIXME: The pipeline and steps should only depend on IContext
 			((PipelineContext)driver.getPipeline().getContext()).setFilterConfigurationMapper(fcMapper);
 			for ( StepInfo stepInfo : steps ) {
 				IPipelineStep step = (IPipelineStep)Class.forName(stepInfo.stepClass).newInstance();
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
 
 	public void execute (Project prj) {
 		copyInfoStepsToPipeline();
 		// Set the batch items
 		driver.clearItems();
 		driver.getPipeline().getContext().removeProperty("outputFile");
 		int f = -1;
 		URI outURI;
 		URI inpURI;
 		RawDocument rawDoc;
 		BatchItemContext bic;
 		int inputRequested = driver.inputCountRequested();
 		
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
 				if ( 3 > f ) {
 					// Data is available
 					Input item2 = prj.getList(j).get(f);
 					// Input
 					outURI = (new File(prj.buildTargetPath(j, item2.relativePath))).toURI();
 					inpURI = (new File(prj.getInputRoot(j) + File.separator + item2.relativePath)).toURI();
 					rawDoc = new RawDocument(inpURI, prj.buildSourceEncoding(item),
 						prj.getSourceLanguage(), prj.getTargetLanguage());
 					rawDoc.setFilterConfigId(item2.filterConfigId);
 					bic.add(rawDoc, outURI, prj.buildTargetEncoding(item2));
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
 		String path = driver.getPipeline().getContext().getString("outputFile");
 		if ( path != null ) {
 			UIUtil.start(path);
 		}
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
