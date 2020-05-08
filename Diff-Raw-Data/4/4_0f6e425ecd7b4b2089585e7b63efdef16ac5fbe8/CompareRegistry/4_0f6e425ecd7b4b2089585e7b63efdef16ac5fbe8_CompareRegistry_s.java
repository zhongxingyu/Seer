 /*******************************************************************************
  * Copyright (c) 2010 University of Illinois. All rights reserved.
  * 
  * Developed by: 
  * National Center for Supercomputing Applications (NCSA)
  * University of Illinois
  * http://www.ncsa.illinois.edu/
  * 
  * Permission is hereby granted, free of charge, to any person obtaining 
  * a copy of this software and associated documentation files (the 
  * "Software"), to deal with the Software without restriction, including 
  * without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to permit 
  * persons to whom the Software is furnished to do so, subject to the 
  * following conditions:
  * 
  * - Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimers.
  * - Redistributions in binary form must reproduce the above copyright notice, 
  *   this list of conditions and the following disclaimers in the documentation 
  *   and/or other materials provided with the distribution.
  * - Neither the names of National Center for Supercomputing Applications,
  *   University of Illinois, nor the names of its contributors may 
  *   be used to endorse or promote products derived from this Software 
  *   without specific prior written permission.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
  * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
  * SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  ******************************************************************************/
 /**
  * 
  */
 package edu.illinois.ncsa.versus.registry;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import sun.misc.Service;
 import edu.illinois.ncsa.versus.adapter.Adapter;
 import edu.illinois.ncsa.versus.descriptor.Descriptor;
 import edu.illinois.ncsa.versus.extract.Extractor;
 import edu.illinois.ncsa.versus.measure.Measure;
 
 /**
  * Registry of available extractors, features, measures.
  * 
  * @author Luigi Marini
  * 
  */
 public class CompareRegistry {
 
 	/**
 	 * A map between feature ids and extractors capable of producing that
 	 * feature type
 	 **/
 	private final Map<String, Collection<Extractor>> featureToExtractorsMap;
 
 	/**
 	 * Collection of known measures.
 	 */
 	private final Collection<Measure> measures;
 
 	/**
 	 * Collection of known features.
 	 */
 	private final Collection<Descriptor> features;
 
 	/**
 	 * Collection of known adapters.
 	 */
 	private final Collection<Adapter> adapters;
 
 	private final Collection<Extractor> extractors;
 
 	/**
 	 * Create an instance and load known extractors.
 	 */
 	public CompareRegistry() {
 		featureToExtractorsMap = new HashMap<String, Collection<Extractor>>();
 		measures = new HashSet<Measure>();
 		features = new HashSet<Descriptor>();
 		extractors = new HashSet<Extractor>();
 		adapters = new HashSet<Adapter>();
 		populateRegistry();
 	}
 
 	/**
 	 * Populate registry.
 	 * 
 	 * TODO switch to dynamic loading
 	 */
 	private void populateRegistry() {
 		Iterator<Adapter> iter = Service.providers(Adapter.class);
 		while (iter.hasNext()) {
 			Adapter adapter = iter.next();
 			adapters.add(adapter);
 		}
 
 		// measures
 		Iterator<Measure> iterMeasure = Service.providers(Measure.class);
 		while (iterMeasure.hasNext()) {
 			Measure measure = iterMeasure.next();
 			measures.add(measure);
 		}
 
 		// array feature extractors
 		Iterator<Extractor> iterExtractor = Service.providers(Extractor.class);
 		while (iterExtractor.hasNext()) {
 			Extractor extractor = iterExtractor.next();
 			extractors.add(extractor);
 		}
 	}
 
 	/**
 	 * Get list of keys for features available on the classpath.
 	 * 
 	 * @return keys of available features
 	 */
 	public Collection<String> getAvailableFeatures() {
 		return featureToExtractorsMap.keySet();
 	}
 
 	/**
 	 * Get known measures.
 	 * 
 	 * @return known measures
 	 */
 	public Collection<Measure> getAvailableMeasures() {
 		return measures;
 	}
 
 	/**
 	 * Given the key of a feature, return an instance of that feature.
 	 * 
 	 * @param key
 	 *            of feature
 	 * @return an instance of a feature
 	 */
 	public Descriptor getFeature(String key) {
 		return null;
 	}
 
 	/**
 	 * Given a feature type return all extractors know to extract this type.
 	 * 
 	 * @param string
 	 *            a unique string identifying the feature type
 	 * @return a collection of extractors
 	 * @deprecated
 	 */
 	@Deprecated
 	public Collection<Extractor> getAvailableExtractors(String featureType) {
 		return featureToExtractorsMap.get(featureType);
 	}
 
 	/**
 	 * Get all available extractors.
 	 * 
 	 * @return a collection of extractors
 	 */
 	public Collection<Extractor> getAvailableExtractors() {
 		return extractors;
 	}
 
 	/**
 	 * Get all adapters known to the system.
 	 * 
 	 * @return collection of adapters
 	 */
 	public Collection<Adapter> getAvailableAdapters() {
 		return adapters;
 	}
 
 	/**
 	 * Get ids of all known adapters.
 	 * 
 	 * @return collection of adapters ids
 	 */
 	public Collection<String> getAvailableAdaptersIds() {
 		Collection<String> adaptersIds = new HashSet<String>();
 		for (Adapter adapter : adapters) {
 			adaptersIds.add(adapter.getClass().getName());
 		}
 		return adaptersIds;
 	}
 
 	/**
 	 * Get ids of all known extractors.
 	 * 
 	 * @return collection of extractors ids
 	 */
 	public Collection<String> getAvailableExtractorsIds() {
 		Collection<String> extractorIds = new HashSet<String>();
		for (Extractor adapter : extractors) {
			extractorIds.add(adapter.getClass().getName());
 		}
 		return extractorIds;
 	}
 
 	/**
 	 * Get ids of all known measures.
 	 * 
 	 * @return collection of measures ids
 	 */
 	public Collection<String> getAvailableMeasuresIds() {
 		Collection<String> measuresIds = new HashSet<String>();
 		for (Measure measure : measures) {
 			measuresIds.add(measure.getClass().getName());
 		}
 		return measuresIds;
 	}
 }
