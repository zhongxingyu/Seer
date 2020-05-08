 package edu.cmu.lti.oaqa.cse.configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.yaml.snakeyaml.Yaml;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class YAMLParser extends Parser {
 
 	public YAMLParser(String resource) {
 		super(resource);
 	}
 
 	/**
 	 * Takes a resource file defined by a yaml and turns it to
 	 * Map<String,Object> corresponding to its parameters.
 	 */
 	@Override
 	public Map<String, Object> getResMap(String resource)
 			throws FileNotFoundException {
 		// Get configuration file from classpath.
 		Map<String, Object> resMap;
 
 		InputStream input = this.getClass().getClassLoader()
 				.getResourceAsStream(resource);
 
 		Yaml yaml = new Yaml();
 
 		if (input != null) { // is file available?
 			resMap = (Map<String, Object>) yaml.load(input);
 			System.out.println(resMap);
 			return resMap;
 		}
 
 		System.err.println("File: " + resource + " not found");
 		throw new FileNotFoundException();
 	}
 
 	/**
 	 * 
 	 */
 
 	@Override
 	public CollectionReaderDescriptor buildCollectionReader() {
 		Map<String, Object> resMap = (Map<String, Object>) confMap
 				.get("collection-reader");
 		String className = buildClass(resMap);
 		CollectionReaderDescriptor crd = new CollectionReaderDescriptor(
 				className, resMap);
 		return crd;
 	}
 
 	/**
 	 * 
 	 */
 
 	@Override
 	public PipelineDescriptor buildPipelineDescriptor() {
 		List<Map<String, Object>> phaseResMaps = (List<Map<String, Object>>) confMap
 				.get("pipeline");
 
 		List<PhaseDescriptor> phaseDescs = new LinkedList<PhaseDescriptor>();
 		for (Map<String, Object> phaseResMap : phaseResMaps)
 			phaseDescs.add(buildPhaseDescriptor(phaseResMap));
 
 		return new PipelineDescriptor(phaseDescs);
 	}
 
 	/**
 	 * 
 	 * @param phaseResMap
 	 * @return
 	 */
 	public PhaseDescriptor buildPhaseDescriptor(Map<String, Object> phaseResMap) {
 		// System.out.println("Unflattened phaseMap = " + phaseResMap);
 		phaseResMap = flatten(phaseResMap);
 		// System.out.println("Flattened phaseMap = " + phaseResMap);
 		List<OptionDescriptor> optionDescs = buildOptionDescriptors(phaseResMap);
 		if (phaseResMap.containsKey("name")) {
 			String name = (String) phaseResMap.get("name");
 			phaseResMap.remove("name");
 			return new PhaseDescriptor(name, optionDescs);
 		} else
 			return new PhaseDescriptor(optionDescs);
 	}
 
 	/**
 	 * 
 	 */
 
 	public List<OptionDescriptor> buildOptionDescriptors(
 			Map<String, Object> phaseResMap) {
 		System.out.println(phaseResMap.get("options"));
 		List<Map<String, Object>> optionResMaps = (List<Map<String, Object>>) phaseResMap
 				.get("options");
 		List<OptionDescriptor> optionDescs = new LinkedList<OptionDescriptor>();
 		for (Map<String, Object> optionResMap : optionResMaps)
 			optionDescs.add(buildOptionDescriptor(optionResMap));
 		return optionDescs;
 	}
 
 	/**
 	 * 
 	 * @param optionResMap
 	 * @return
 	 */
 	public OptionDescriptor buildOptionDescriptor(
 			Map<String, Object> optionResMap) {
 		String className = buildClass(optionResMap);
 		return new OptionDescriptor(className, optionResMap);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private Map<String, String> getConfiguration() {
 		return (Map<String, String>) confMap.get("configuration");
 	}
 
 	/**
 	 * 
 	 */
 
 	@Override
 	public String getName() {
 		return getConfiguration().get("name");
 	}
 
 	/**
 	 * 
 	 */
 
 	@Override
 	public String getAuthor() {
 		return getConfiguration().get("author");
 	}
 
 	/**
 	 * Takes the specified yaml component and flattens the "inherits" therein so
 	 * that it only contains the "class" and associated parameters.
 	 * 
 	 * @param resMap
 	 *            is the yaml descriptor mapping from names to parameters.
 	 * @return
 	 */
 	private Map<String, Object> flatten(Map<String, Object> resMap) {
 		if (resMap.containsKey("inherit")) {
 			// Get resource specified by "inherit"
 			String resource = (String) resMap.get("inherit");
 			// convert package notation to file path
 			resource = resource.replace(".", "/") + ".yaml";
 			// Get resource map specified by the resource
 			Map<String, Object> inheritedResMap;
 			try {
 				inheritedResMap = getResMap(resource);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				return null;
 			}
 			// Remove "inherit" artifact from effective yaml
 			resMap.remove("inherit");
 			// Combine the current resource with the flattened inherited
 			// resource recursively to create effective yaml.
 			return combineMaps(resMap, flatten(inheritedResMap));
 		}
 		return resMap;
 	}
 
 	/**
 	 * Combines map1 and map2 into a single a map by taking the union of their
 	 * keys.
 	 * 
 	 * @param map1
 	 * @param map2
 	 * @return the union of map1 and map2.
 	 */
 	private static <K, V> Map<K, V> combineMaps(Map<K, V> map1,
 			Map<? extends K, ? extends V> map2) {
 		map1.putAll(map2);
 		return map1;
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public List<ConsumerDescriptor> buildConsumers() {
 		List<Map<String, Object>> consumerResMaps = (List<Map<String, Object>>) confMap
 				.get("consumers");
 		List<ConsumerDescriptor> consumerDescs = new LinkedList<ConsumerDescriptor>();
 		for (Map<String, Object> consumerResMap : consumerResMaps)
 			consumerDescs.add(buildConsumerDescriptor(consumerResMap));
 		return consumerDescs;
 	}
 
 	private ConsumerDescriptor buildConsumerDescriptor(
 			Map<String, Object> resMap) {
 		String className = buildClass(resMap);
 		return new ConsumerDescriptor(className, resMap);
 	}
 
 	private String buildClass(Map<String, Object> resMap) {
 		resMap = flatten(resMap);
 		String className = (String) resMap.get("class");
 		resMap.remove("class");
 		return className;
 	}
 
 	@Override
 	protected Map<String, ScoreDescriptor> buildScores() {
 		List<Map<String,Object>> scores =  (List<Map<String,Object>>)confMap.get("metrics");
 		Map<String,ScoreDescriptor> scoreMap = Maps.newHashMap();
		for(Map<String,Object> score : scores){
 			String className = buildClass(score);
 			ScoreDescriptor sd= new ScoreDescriptor((Double)score.get("cost"),(Double)score.get("benefit"));
 			scoreMap.put(className,sd);
		}
 			return scoreMap;
 	}
 
 	public static void main(String[] args) {
 		String resource = args[0];
 		YAMLParser parser = new YAMLParser(resource);
 		parser.buildPipelineDescriptor();
 	}
 
 }
