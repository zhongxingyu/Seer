 /*******************************************************************************
  * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
  * This program and the accompanying materials are made available under the terms of the new BSD license which
  * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
  * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
  * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring Algorithm Christopher Barnes - regex
  * scoring algorithim
  ******************************************************************************/
 package org.vivoweb.harvester.score;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vivoweb.harvester.util.InitLog;
 import org.vivoweb.harvester.util.IterableAdaptor;
 import org.vivoweb.harvester.util.args.ArgDef;
 import org.vivoweb.harvester.util.args.ArgList;
 import org.vivoweb.harvester.util.args.ArgParser;
 import org.vivoweb.harvester.util.repo.JenaConnect;
 import org.vivoweb.harvester.util.repo.MemJenaConnect;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.util.ResourceUtils;
 //import com.hp.hpl.jena.rdf.model.Statement;
 //import com.hp.hpl.jena.rdf.model.StmtIterator;
 //import org.vivoweb.harvester.util.repo.MemJenaConnect;
 //import java.util.Stack;
 
 /**
  * VIVO Match
  * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
  * @author Stephen Williams svwilliams@ctrip.ufl.edu
  * @author Christopher Haines hainesc@ctrip.ufl.edu
  */
 public class Match {
 	/**
 	 * SLF4J Logger
 	 */
 	private static Logger log = LoggerFactory.getLogger(Match.class);
 	/**
 	 * Model for VIVO instance
 	 */
 	private final JenaConnect scoreJena;
 	/**
 	 * Model where input is stored
 	 */
 	private final JenaConnect inputJena;
 	/**
 	 * Model where output goes
 	 */
 	private final JenaConnect outputJena;
 	/**
 	 * Link the resources found by match Algorithm
 	 */
 	private final Map<String, String> linkProps;
 	/**
 	 * Rename resources found by match Algorithm
 	 */
 	private final boolean renameRes;
 	/**
 	 * Pubmed Match threshold
 	 */
 	private final float matchThreshold;
 	/**
 	 * Clear all literal values out of matched sets
 	 */
 	private final boolean clearLiterals;
 
 	
 	/**
 	 * Constructor
 	 * @param inputJena model containing statements to be scored
 	 * @param scoreJena the model that contains the score values
 	 * @param outputJena the model to which matched structures are written
 	 * @param threshold match things with a total score greater than or equal to this threshold
 	 * @param renameRes should I just rename the args?
 	 * @param linkProps bidirectional link
 	 * @param clearLiterals clear all the literal values out of matches
 	 */
 	public Match(JenaConnect inputJena, JenaConnect scoreJena, JenaConnect outputJena, boolean renameRes, float threshold, Map<String, String> linkProps, boolean clearLiterals) {
 		if(scoreJena == null) {
 			throw new IllegalArgumentException("Score Model cannot be null");
 		}
 		this.scoreJena = scoreJena;
 		
 		if(inputJena == null) {
 			throw new IllegalArgumentException("Match Input cannot be null");
 		}
 		this.inputJena = inputJena;
 		
 		this.outputJena = outputJena;
 		
 		this.matchThreshold = threshold;
 		this.renameRes = renameRes;
 		this.linkProps = linkProps;
 		this.clearLiterals = clearLiterals;
 	}
 	
 	/**
 	 * Constructor
 	 * @param args argument list
 	 * @throws IOException error parsing options
 	 */
 	public Match(String... args) throws IOException {
 		this(new ArgList(getParser(), args));
 	}
 	
 	/**
 	 * Constructor
 	 * @param opts parsed argument list
 	 * @throws IOException error parsing options
 	 */
 	public Match(ArgList opts) throws IOException {
 		// Connect to score data model
 		this.scoreJena = JenaConnect.parseConfig(opts.get("s"), opts.getValueMap("S"));
 		
 		// Connect to input model
 		this.inputJena = JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I"));
 		
 		// Connect to output model
 		if (opts.has("o")){
 			this.outputJena = JenaConnect.parseConfig(opts.get("o"), opts.getValueMap("O"));
 		} else {
 			this.outputJena = null;
 		}
 			
 		
 		this.renameRes = opts.has("r");
 		this.linkProps = opts.getValueMap("l");
 		this.matchThreshold = Float.parseFloat(opts.get("t"));
 		this.clearLiterals = opts.has("c");
 	}
 	
 	/**
 	 * Find all nodes in the given namepsace matching on the given predicates
 	 * @param threshold the value to look for in the sparql query
 	 * @return mapping of the found matches
 	 */
 	private Map<String,String> match(float threshold){
 		//Build query to find all nodes matching on the given predicates
 		String sQuery =	"" +
 				"PREFIX scoreValue: <http://vivoweb.org/harvester/scoreValue/>\n" +
 				"SELECT DISTINCT ?sVivo ?sInput (sum(?weightValue) AS ?sum) \n" +
 				"WHERE { \n" +
 				"  ?s scoreValue:InputRes ?sInput . \n" +
 				"  ?s scoreValue:VivoRes ?sVivo . \n" +
 				"  ?s scoreValue:hasScoreValue ?value . \n" +
 				"  ?value scoreValue:WeightedScore ?weightValue . \n" +
 				"}" +
 				"GROUP BY ?sVivo ?sInput \n" +
 				"HAVING (?sum >= "+threshold+")" +
 				"";
 		Map<String,String> uriMatchMap = new HashMap<String,String>();
 		for(QuerySolution solution : IterableAdaptor.adapt(this.scoreJena.executeSelectQuery(sQuery))) {
 			String sInputURI = solution.getResource("sInput").getURI();
 			String sVivoURI = solution.getResource("sVivo").getURI();
 			Float weight = Float.valueOf(solution.getLiteral("sum").getFloat());
			log.trace("input: "+sInputURI);
			log.trace("vivo: "+sVivoURI);
			log.trace("weight: "+weight);
 			uriMatchMap.put(sInputURI, sVivoURI);
 			log.debug("Match found: <"+sInputURI+"> in Input matched with <"+sVivoURI+"> in Vivo");
 		}
 		
 		log.info("Match found " + uriMatchMap.keySet().size() + " links between Vivo and the Input model");
 		
 		return uriMatchMap;
 	}
 	
 	/**
 	 * Rename the resource set as the key to the value matched
 	 * @param matchSet a result set of scoreResources, vivoResources
 	 */
 	private void rename(Map<String,String> matchSet){
 		for(String oldUri : matchSet.keySet()) {
 			//get resource in input model and perform rename
 			Resource res = this.inputJena.getJenaModel().getResource(oldUri);
 			String newUri = matchSet.get(oldUri);
 			log.trace("Renaming match <" + oldUri + "> to <" + newUri + ">");
 			ResourceUtils.renameResource(res, newUri);
 		}
 	}
 	
 	/**
 	 * Link matched scoreResources to vivoResources using given linking predicates
 	 * @param matchSet a mapping of matched scoreResources to vivoResources
 	 * @param vivoToInput vivo to input property
 	 * @param inputToVivo input to vivo property
 	 */
 	private void link(Map<String,String> matchSet, String vivoToInput, String inputToVivo) {
 		Property vivoToInputProperty = ResourceFactory.createProperty(vivoToInput);
 		Property inputToVivoProperty = ResourceFactory.createProperty(inputToVivo);
 		
 		for(String inputUri : matchSet.keySet()) {
 			// get resources and add linking triples
 			String vivoUri = matchSet.get(inputUri);
 			Resource inputRes = this.inputJena.getJenaModel().getResource(inputUri);	
 			Resource vivoRes = this.scoreJena.getJenaModel().getResource(vivoUri);
 			log.trace("Adding input to vivo match link [ <" + inputUri + "> <" + inputToVivo + "> <" + vivoUri + "> ]");
 			this.inputJena.getJenaModel().add(inputRes, inputToVivoProperty, vivoRes);
 			log.trace("Adding vivo to input match link [ <" + vivoUri + "> <" + vivoToInput + "> <" + inputUri + "> ]");
 			this.inputJena.getJenaModel().add(vivoRes, vivoToInputProperty, inputRes);			
 		}
 	}
 	
 	/**
 	 * Clear out rdf:type and literal values of matched scoreResources
 	 * TODO stephen: TEST
 	 * @param resultMap a mapping of matched scoreResources to vivoResources
 	 * @throws IOException error building construct
 	 */
 	private void clearTypesAndLiterals(Map<String, String> resultMap) throws IOException {
 		if(!resultMap.values().isEmpty()) {
 			Set<String> uriFilters = new HashSet<String>();
 			for(String uri : resultMap.values()) {
 				uriFilters.add("(str(?s) = \"" + uri + "\")");
 			}
 			String query = "" +
 			"DELETE {\n" +
 			"  ?s ?p ?o\n" +
 			"} WHERE {\n" +
 			"  ?s ?p ?o .\n" +
			"  FILTER ( isLiteral(?o || (str(?p)='http://www.w3.org/1999/02/22-rdf-syntax-ns#type')) && ("+StringUtils.join(uriFilters, " || ")+")) .\n" +
 			"}";
 			String conQuery = query.replaceFirst("DELETE", "CONSTRUCT");
 			log.debug("Construct Query:\n"+conQuery);
 			log.debug("Constructed Literal Set:\n"+this.inputJena.executeConstructQuery(conQuery).toString());
 			log.debug("Clear Literal Query:\n" + query);
 			this.inputJena.executeUpdateQuery(query);
 		}
 	}
 	
 	/**
 	 * @param matchSet the set of matches to run against
 	 * @return the completed model of matches
 	 * @throws IOException no idea why it throws this 
 	 */
 	private JenaConnect outputMatches(Map<String,String> matchSet) throws IOException{
 		Stack<String> linkRes = new Stack<String>();
 		JenaConnect returnModel = new MemJenaConnect();
 		for(String oldUri : matchSet.keySet()) {
 			Resource res = this.scoreJena.getJenaModel().getResource(matchSet.get(oldUri));
 			if (!linkRes.contains(res)){
 				returnModel = recursiveBuild(res, linkRes);
 				linkRes.push(res.getURI());
 			}			
 		}
 		return returnModel;
 	}
 	
 	/**
 	 * @param mainRes item to push into returnModel
 	 * @param linkRes list of items to not move to
 	 * @return model thats being returned with matches
 	 * @throws IOException I have no idea why mem throws this
 	 */
 	/* Traverses paperNode and adds to toReplace model
 	 * @param mainRes the main resource
 	 * @param linkRes the resource to link it to
 	 * @return the model containing the sanitized info so far
 	 * @throws IOException error connecting
 	 */
 	private static JenaConnect recursiveBuild(Resource mainRes, Stack<String> linkRes) throws IOException {
 		JenaConnect returnModel = new MemJenaConnect();
 		StmtIterator mainStmts = mainRes.listProperties();
 		
 		while (mainStmts.hasNext()) {
 			Statement stmt = mainStmts.nextStatement();
 			
 				// log.debug(stmt.toString());
 				returnModel.getJenaModel().add(stmt);
 				
 				//todo change the equals t o
 				if (stmt.getObject().isResource() && !linkRes.contains(stmt.getObject().asResource().getURI()) && !stmt.getObject().asResource().equals(mainRes)) {
 					linkRes.push(mainRes.getURI());
 					returnModel.getJenaModel().add(recursiveBuild(stmt.getObject().asResource(), linkRes).getJenaModel());
 					linkRes.pop();
 				}
 				if (!linkRes.contains(stmt.getSubject().getURI()) && !stmt.getSubject().equals(mainRes)) {
 					linkRes.push(mainRes.getURI());
 					returnModel.getJenaModel().add(recursiveBuild(stmt.getSubject(), linkRes).getJenaModel());
 					linkRes.pop();
 				}
 		}
 		
 		return returnModel;
 	}
 	
 	/**
 	 * Get the OptionParser
 	 * @return the OptionParser
 	 */
 	private static ArgParser getParser() {
 		ArgParser parser = new ArgParser("Match");
 		// Inputs
 		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input-config").setDescription("inputConfig JENA configuration filename, by default the same as the vivo JENA configuration file").withParameter(true, "CONFIG_FILE").setRequired(true));
 		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("score-config").setDescription("scoreConfig JENA configuration filename").withParameter(true, "CONFIG_FILE").setRequired(true));
 		
 		// Outputs
 		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output-config").setDescription("outputConfig JENA configuration filename, when set nodes that meet the threshold are pushed to the output model").withParameter(true, "CONFIG_FILE").setRequired(false));
 		
 		// Model name overrides
 		parser.addArgument(new ArgDef().setShortOption('S').setLongOpt("scoreOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of score jena model config using VALUE").setRequired(false));
 		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
 		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
 		
 		// Matching Algorithms
 		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("threshold").withParameter(true, "THRESHOLD").setDescription("match records with a score over THRESHOLD").setRequired(true));
 		
 		// Linking Methods
 		parser.addArgument(new ArgDef().setShortOption('l').setLongOpt("link").withParameterValueMap("VIVO_TO_INPUT_PREDICATE", "INPUT_TO_VIVO_PREDICATE").setDescription("link the two matched entities together using INPUT_TO_VIVO_PREDICATE and INPUT_TO_VIVO_PREDICATE").setRequired(false));
 		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rename").setDescription("rename or remove the matched entity from scoring").setRequired(false));
 		
 		// options
 		parser.addArgument(new ArgDef().setShortOption('c').setLongOpt("clear-type-and-literals").setDescription("clear all rdf:type and literal values out of the nodes matched").setRequired(false));
 		return parser;
 	}	
 	
 	/**
 	 * Execute scoreJena object algorithms
 	 * @throws IOException error connecting
 	 */
 	public void execute() throws IOException {
 		log.info("Running specified algorithims");
 		
 		Map<String,String> pubmedResultMap = match(this.matchThreshold);
 		
 		if(this.renameRes) {
 			rename(pubmedResultMap);
 		}
 		
 		if(this.linkProps != null) {
 			for(String vivoToInput : this.linkProps.keySet()) {
 				link(pubmedResultMap, vivoToInput, this.linkProps.get(vivoToInput));
 			}
 		}
 		
 		if(this.clearLiterals) {
 			clearTypesAndLiterals(pubmedResultMap);
 		}
 		
 		if(this.outputJena != null) {
 			this.outputJena.getJenaModel().add(outputMatches(pubmedResultMap).getJenaModel());
 		}
 	}
 
 	/**
 	 * Main method
 	 * @param args command line arguments
 	 */
 	public static void main(String... args) {
 		InitLog.initLogger(Match.class);
 		log.info(getParser().getAppName()+": Start");
 		try {
 			new Match(args).execute();
 		} catch(IllegalArgumentException e) {
 			log.error(e.getMessage(), e);
 			System.out.println(getParser().getUsage());
 		} catch(Exception e) {
 			log.error(e.getMessage(), e);
 		}
 		log.info(getParser().getAppName()+": End");
 	}
 	
 }
