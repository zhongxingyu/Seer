 /**
  * 
  */
 package edu.illinois.ncsa.versus.extract;
 
 import java.util.Set;
 
 import edu.illinois.ncsa.versus.adapter.Adapter;
 import edu.illinois.ncsa.versus.descriptor.Descriptor;
 
 /**
  * An extractor extracts a particular feature from an adapter.
  * 
  * @author Luigi Marini
  * 
  */
 public interface Extractor {
 
 	/**
 	 * Extract a feature F from and adapter A.
 	 * 
 	 * @param adapter
 	 * @return feature
 	 */
 	public Descriptor extract(Adapter adapter) throws Exception;
 
 	/**
 	 * Create an instance of the adapter required for this extractor.
 	 * 
 	 * @return an adapter instance used by this extractor
 	 * 
 	 * @deprecated Now extractors can support multiple adapter types
 	 */
 	@Deprecated
 	public Adapter newAdapter();
 
 	/**
 	 * The pretty name for the extractor to use in applications.
 	 * 
 	 * @return the pretty name of the extractor
 	 */
 	String getName();
 
 	/**
 	 * The set of adapters supported by the extractor.
 	 * 
 	 * @return adapters supported by the extractor
 	 */
 	Set<Class<? extends Adapter>> supportedAdapters();
 
 	/**
 	 * The class of the feature that will be created by this extractor.
 	 * 
 	 * @return the class type of the output feature
 	 */
 	Class<? extends Descriptor> getFeatureType();
 	
	
	public boolean hasPreview();
	
 	/**
 	 * 
	 * NULL if no preview for the extractor, if there is a visualizer class for the specified preview, then return the name of visualuzer class
	 * 
	 * e.g. the MD5Extractor visualizer class is named MD5Visualizer, which is what this function returns.
	 * 
	 * @return the name of the visualizer class (with the preview) for the specified extractor
 	 */
	public String previewName();
 	
 }
