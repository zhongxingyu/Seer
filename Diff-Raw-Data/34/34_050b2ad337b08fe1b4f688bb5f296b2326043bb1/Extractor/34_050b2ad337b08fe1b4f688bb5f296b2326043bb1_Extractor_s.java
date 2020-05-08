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
 	
 	/**
	 * The state of the preview function for the descriptor.
 	 * 
	 * @return true if descriptor has preview, false if not
 	 */
	public boolean hasPreview();
 	
 }
