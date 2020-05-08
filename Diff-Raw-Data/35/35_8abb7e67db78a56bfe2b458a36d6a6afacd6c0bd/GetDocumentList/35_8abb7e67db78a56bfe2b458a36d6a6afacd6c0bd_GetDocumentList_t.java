 package com.technosophos.rhizome.command;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import com.technosophos.rhizome.controller.CommandResult;
 import com.technosophos.rhizome.document.DocumentCollection;
 import com.technosophos.rhizome.repository.RepositoryAccessException;
 import com.technosophos.rhizome.repository.RhizomeInitializationException;
 
 public class GetDocumentList extends AbstractCommand {
 	
 	/**
 	 * The string "fields".
 	 * The name of the configuration parameter (in {@link CommandConfiguration} that 
 	 * contains the list of fields that should be retrieved from the {@link Map} that 
 	 * is passed into doCommand.
 	 */
 	public static String CONF_PARAM_FIELDS = "fields";
 	
 	/**
 	 * The string "additional_fields".
 	 * Additional fields which should be retrieved, but not used to construct the 
 	 * narrower. (Nothing for these fields is fetched from the params list passed
 	 * into {@link doCommand(Map, List)}.
 	 */
 	public static String CONF_ADD_FIELDS = "additional_fields";
 	
 	/**
 	 * Get a list of documents.
 	 * <p>This command constructs a list of criterion for searching (a narrower), and then
 	 * performs the search, wrapping a {@link DocumentCollection} inside of a 
 	 * {@link CommandResult}, and inserting that into the {@link List} of CommandResult
 	 * objects.</p>
 	 * <p><b>How the Filter is Constructed: </b> The filter is constructed by first 
 	 * determining, based on the values stored in the 'fields' parameter of the 
 	 * {@link CommandConfiguration} object, which fields to retrieve from the {@link Map}
 	 * of parameters passed into this method. The narrower is build based on these conditions.</p>
 	 * <p>If the "additional_fields" parameter exists (and has values) in the CommandConfiguration
 	 * object, then those fields will also be stored in the returned DocumentCollection, if they exist.</p>
 	 * <p>Then, a search is executed, and the results stored in the CommandResult List.</p>
 	 */
 	@Override
 	public void doCommand(Map<String, Object> params,
 			List<CommandResult> results) {
 		CommandResult res;
 		DocumentCollection doc;
 		/*
 		 * What this command needs to do:
 		 * 1. Get from conf the list of fields that should be in params
 		 * 2. Get from params the name/value pairs for those fields
 		 * 3. See if there is a list of retrievable values in conf (values to get, but w/o comparisons)
 		 * 4. Do a narrowing search
 		 */
 		
 		if(!this.comConf.hasParameter(CONF_PARAM_FIELDS)) {
 			res = new CommandResult();
 			String errMsg = "\"fields\" param is not set in the configuration. Nothing to retrieve.";
 			String friendlyErrMsg = "A list of documents was requested, but the server could not process the request. We don't know what to look for.";
 			res.setError(errMsg, friendlyErrMsg);
 			results.add(res);
 			return;
 		}
 		
 		String [] fields = this.comConf.getParameter(CONF_PARAM_FIELDS);
 		HashMap<String, String> narrower = this.buildNarrower(fields, params);
 		if(narrower.size() == 0 ) {
 			res = new CommandResult();
 			String errMsg = "None of the expected keys (fields) appeared in the params passed to this command. Nothing to retrieve.";
 			String friendlyErrMsg = "A list of documents was requested, but not enough search information was given. We don't know what to look for.";
 			res.setError(errMsg, friendlyErrMsg);
 			results.add(res);
 			return;
 		}
 		
 		String [] additional_md = this.comConf.getParameter(CONF_ADD_FIELDS);
 		if(additional_md == null) additional_md = new String [0];
 		
 		try {
 			doc = this.repoman.getRepositorySearcher().narrowingSearch(narrower, additional_md);
 		} catch (RhizomeInitializationException rie) {
 			res = new CommandResult();
 			String errMsg = "Error initializing the repository.";
 			String friendlyErrMsg = "A list of documents was requested, but the data is unavailable. Nothing was returned.";
 			res.setError(errMsg, friendlyErrMsg, rie);
 			results.add(res);
 			return;
 		} catch (RepositoryAccessException rae) {
 			res = new CommandResult();
 			String errMsg = "Error doing narrowing search of the repository.";
 			String friendlyErrMsg = "A list of documents was requested, but the search failed. Nothing was returned.";
 			res.setError(errMsg, friendlyErrMsg, rae);
 			results.add(res);
 			return;
 		}
 		
 		// Package the results and return
 		res = new CommandResult(doc);
 	}
 	
 	/**
 	 * Create a Map that can be used as a narrower in a narrowing search.
 	 * <p>This version is <i>not strict</i>. If a field from the filds[] array is not present
 	 * in the params {@link Map}, it is simply skipped. Note, however, that the result of
 	 * this is that the Map returned could be empty.</p>
 	 * <p>This is prefix aware, and if a prefix is present, it will be prepended to each 
 	 * value in the fields array.</p>
 	 * <p>You might want to override this if you want any value checking done.</p>
 	 * @param fields array of field names (keys to look for in the params Map).
 	 * @param params a Map of parameters.
 	 * @return
 	 */
 	protected HashMap<String, String> buildNarrower(String[] fields, Map<String, Object> params) {
 		HashMap<String, String> m = new HashMap<String, String>();
 		for(String field: fields) {
 			if(params.containsKey(this.getPrefixedParamName(field)))
 				m.put(field, this.getParam(params, field).toString());
 		}
 		return m;
 	}
 
 }
