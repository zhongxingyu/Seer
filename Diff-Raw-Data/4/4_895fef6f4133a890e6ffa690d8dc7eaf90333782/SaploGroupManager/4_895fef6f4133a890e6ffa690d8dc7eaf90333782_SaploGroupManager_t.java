 /**
  * 
  */
 package com.saplo.api.client.manager;
 
 import static com.saplo.api.client.ResponseCodes.CODE_CLIENT_FIELD;
 import static com.saplo.api.client.ResponseCodes.CODE_JSON_EXCEPTION;
 import static com.saplo.api.client.ResponseCodes.CODE_MALFORMED_RESPONSE;
 import static com.saplo.api.client.ResponseCodes.MSG_CLIENT_FIELD;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.saplo.api.client.SaploClient;
 import com.saplo.api.client.SaploClientException;
 import com.saplo.api.client.entity.JSONRPCRequestObject;
 import com.saplo.api.client.entity.JSONRPCResponseObject;
 import com.saplo.api.client.entity.SaploCollection;
 import com.saplo.api.client.entity.SaploFuture;
 import com.saplo.api.client.entity.SaploGroup;
 import com.saplo.api.client.entity.SaploText;
 import com.saplo.api.client.util.ClientUtil;
 
 /**
  * A manager class for operations on {@link SaploGroup} objects
  * 
  * @author progre55
  */
 public class SaploGroupManager {
 
 	//	private static Logger logger = Logger.getLogger(SaploGroupManager.class);
 
 	private SaploClient client;
 	private ExecutorService es;
 
 	/**
 	 * The default and only constructor.
 	 * 
 	 * @param clientToUse - a {@link SaploClient} object to be used with this manager.
 	 */
 	public SaploGroupManager(SaploClient clientToUse) {
 		this.client = clientToUse;
 		es = client.getAsyncExecutor();
 	}
 
 	/**
 	 * Create a new text group on the API server
 	 * 
 	 * @param saploGroup - the new {@link SaploGroup} object to be created
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public void create(SaploGroup saploGroup) throws SaploClientException {
 		
 		if(saploGroup.getLanguage() == null)
 			throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "group.language");
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("name", saploGroup.getName());
 			if(!ClientUtil.NULL_STRING.equals(saploGroup.getDescription()))
 				params.put("description", saploGroup.getDescription());
 			params.put("language", saploGroup.getLanguage().toString());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.create", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonGroup = (JSONObject)client.parseResponse(response);
 
 		SaploGroup.convertFromJSONToGroup(jsonGroup, saploGroup);
 	}
 
 	/**
 	 * Asynchronously create a new text group on the API server.
 	 * This method returns a {@link SaploFuture}<{@link Boolean}> object, 
 	 * which is <code>true</code> in case of success and <code>false</code> in case of failure.
 	 * 
 	 * Here is an example usage:
 	 * <pre>
 	 *	SaploGroup myGroup = new SaploGroup("my example group", Language.en);
 	 *	Future<Boolean> future = groupMgr.createAsync(myGroup);
 	 *
 	 *	// do some other stuff here
 	 *
 	 *	boolean createOk = false;
 	 *	try {
 	 *		createOk = future.get(3, TimeUnit.SECONDS);
 	 *	} catch (InterruptedException e) {
 	 *		e.printStackTrace();
 	 *	} catch (ExecutionException e) {
 	 *		e.printStackTrace();
 	 *	} catch (TimeoutException e) {
 	 *		future.cancel(false);
 	 *		e.printStackTrace();
 	 *	}
 	 *
 	 *	if(createOk) {
 	 *		int myGroupId = myGroup.getId();
 	 *
 	 *		// do some other operations as you prefer
 	 *
 	 *	} else {
 	 *		// something went wrong, so do smth about it
 	 *	}
 	 * </pre>
 	 * 
 	 * 
 	 * @param saploGroup - the new {@link SaploGroup} object to be created
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> createAsync(final SaploGroup saploGroup) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				create(saploGroup);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Update an existing group's name or description.
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} to update
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public void update(SaploGroup saploGroup) throws SaploClientException {
 		verifyId(saploGroup);
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 			if(!ClientUtil.NULL_STRING.equals(saploGroup.getName()))
 				params.put("name", saploGroup.getName());
 			if(!ClientUtil.NULL_STRING.equals(saploGroup.getDescription()))
 				params.put("description", saploGroup.getDescription());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.update", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonGroup = (JSONObject)client.parseResponse(response);
 
 		SaploGroup.convertFromJSONToGroup(jsonGroup, saploGroup);
 	}
 
 	/**
 	 * Asynchronously update an existing group's name or description.
 	 * For an example usage, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} to update
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> updateAsync(final SaploGroup saploGroup) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				update(saploGroup);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Reset the given group.
 	 * WARNING: This will remove all texts linked to that group and remove all results for the group
 	 * 
 	 * @param saploGroup - the group to reset
 	 * @throws SaploClientException
 	 */
 	public void reset(SaploGroup saploGroup) throws SaploClientException {
 		verifyId(saploGroup);
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.reset", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonGroup = (JSONObject)client.parseResponse(response);
 
 		SaploGroup.convertFromJSONToGroup(jsonGroup, saploGroup);
 
 	}
 
 	/**
 	 * Asynchronously reset a group.
 	 * For an example usage on async, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} to reset
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> resetAsync(final SaploGroup saploGroup) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				reset(saploGroup);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Delete a given group
 	 * WARNING: This will remove the group and all its associated results.
 	 * 
 	 * @param saploGroup - the group to delete
 	 * @return success/fail
 	 * 
 	 * @throws SaploClientException
 	 */
 	public boolean delete(SaploGroup saploGroup) throws SaploClientException {
 		verifyId(saploGroup);
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.delete", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject result = (JSONObject)client.parseResponse(response);
 
 		return result.optBoolean("success", false);
 	}
 
 	/**
 	 * Asynchronously delete a group.
 	 * For an example usage on async, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} to delete
 	 * @return success/fail
 	 * @throws SaploClientException
 	 */	
 	public SaploFuture<Boolean> deleteAsync(final SaploGroup saploGroup) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				return delete(saploGroup);
 			}
 		}));
 	}
 
 	/**
 	 * List all the groups that belong to the user.
 	 * 
 	 * @return a {@link List} containing all the users {@link SaploGroup}s.
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public List<SaploGroup> list() throws SaploClientException {
 		List<SaploGroup> groupList = new ArrayList<SaploGroup>();
 
 		JSONObject params = new JSONObject();
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.list", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawJson = (JSONObject)client.parseResponse(response);
 
 		try {
 			JSONArray groups = rawJson.getJSONArray("groups");
 			for(int i = 0; i < groups.length(); i++) {
 				JSONObject jsonGroup = groups.getJSONObject(i);
 				SaploGroup saploGroup = SaploGroup.convertFromJSONToGroup(jsonGroup);
 				groupList.add(saploGroup);
 			}
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_MALFORMED_RESPONSE, je);
 		}
 		return groupList;
 	}
 
 	/**
 	 * Asynchronously list all the groups that belong to the user.
 	 * For an example usage, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @return {@link SaploFuture}<{@link List}<{@link SaploGroup}>> containing all the user {@link SaploGroup}s
 	 * @throws
 	 */
 	public SaploFuture<List<SaploGroup>> listAsync() {
 		return new SaploFuture<List<SaploGroup>>(es.submit(new Callable<List<SaploGroup>>() {
 			public List<SaploGroup> call() throws SaploClientException {
 				return list();
 			}
 		}));
 	}
 
 	/**
 	 * Get a list of all texts ({@link SaploText}) that exist in a group.
 	 * 
 	 * @param saploGroup - The group whose text list we want. {@link SaploGroup#getId()} is mandatory.
 	 * @return textList - a {@link List} populated with {@link SaploText} objects 
 	 * (only {@link SaploCollection#getId()} and {@link SaploText#getId()} params)
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public List<SaploText> listTexts(SaploGroup saploGroup) throws SaploClientException {
 		List<SaploText> textList = new ArrayList<SaploText>();
 
 		verifyId(saploGroup);
 		
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.listTexts", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawJson = (JSONObject)client.parseResponse(response);
 
 		try {
 			JSONArray texts = rawJson.getJSONArray("texts");
 			for(int i = 0; i < texts.length(); i++) {
 				JSONObject jsonText = texts.getJSONObject(i);
 				SaploText saploText = SaploText.convertFromJSONToText(jsonText);
 				textList.add(saploText);
 			}
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_MALFORMED_RESPONSE, je);
 		}
 		
 		return textList;
 	}
 
 	/**
 	 * Asynchronously get a list of all texts ({@link SaploText}) that exist in a group.
 	 * For an example usage, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param group - The group whose text list we want. {@link SaploGroup#getId()} is mandatory.
 	 * @return {@link SaploFuture}<{@link List}<{@link SaploText}>> populated with {@link SaploText} objects 
 	 * (only {@link SaploCollection#getId()} and {@link SaploText#getId()} params)
 	 * @throws
 	 */
 	public SaploFuture<List<SaploText>> listTextsAsync(final SaploGroup saploGroup) {
 		return new SaploFuture<List<SaploText>>(es.submit(new Callable<List<SaploText>>() {
 			public List<SaploText> call() throws SaploClientException {
 				return listTexts(saploGroup);
 			}
 		}));
 	}
 
 	/**
 	 * Add a text to a given group.
 	 * 
 	 * @param saploGroup - which {@link SaploGroup} to add the text to
 	 * @param saploText - the {@link SaploText} to add
 	 * @return true - on success
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public boolean addText(SaploGroup saploGroup, SaploText saploText) throws SaploClientException {
 		verifyId(saploGroup);
 		verifyCollection(saploText);
 		verifyId(saploText);
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 			params.put("collection_id", saploText.getCollection().getId());
 			params.put("text_id", saploText.getId());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.addText", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject result = (JSONObject)client.parseResponse(response);
 
 		return result.optBoolean("success", false);
 	}
 
 	/**
 	 * Asynchronously add a text to a given group.
 	 * For an example usage, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param saploGroup - which {@link SaploGroup} to add the text to
 	 * @param saploText - the {@link SaploText} to add
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> addTextAsync(final SaploGroup saploGroup, final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				addText(saploGroup, saploText);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Delete a text from a given group.
 	 * 
 	 * @param saploGroup - which {@link SaploGroup} to delete the text from
 	 * @param saploText - the {@link SaploText} to delete
 	 * @return true - on success
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public boolean deleteText(SaploGroup saploGroup, SaploText saploText) throws SaploClientException {
 		verifyId(saploGroup);
 		verifyCollection(saploText);
 		verifyId(saploText);
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 			params.put("collection_id", saploText.getCollection().getId());
 			params.put("text_id", saploText.getId());
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.deleteText", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
		JSONObject result = (JSONObject)client.parseResponse(response);
		
		return result.optBoolean("success", false);
 	}
 
 	/**
 	 * Asynchronously delete text from a given group.
 	 * For an example usage, see {@link #createAsync(SaploGroup)}
 	 * 
 	 * @param saploGroup - which {@link SaploGroup} to delete text from
 	 * @param saploText - the {@link SaploText} to delete
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> deleteTextAsync(final SaploGroup saploGroup, final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				deleteText(saploGroup, saploText);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Get related groups for a given {@link SaploGroup} object.
 	 * Then the related group list can be retrieved by {@link SaploGroup#getRelatedGroups()}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} object to search related groups against
 	 * @param groupScope - the {@link SaploGroup}s the given group should be compared to.
 	 * By default, all the user groups are searched.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * 
 	 * @throws SaploClientException 
 	 */
 	public void relatedGroups(SaploGroup saploGroup, SaploGroup[] groupScope, int wait) throws SaploClientException {
 		verifyId(saploGroup);
 		
 		List<SaploGroup> relatedGroupsList = new ArrayList<SaploGroup>();
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 			if(groupScope != null && groupScope.length > 0) {
 				int groupIds[] = new int[groupScope.length];
 				for(int i = 0; i < groupScope.length; i++) {
 					groupIds[i] = groupScope[i].getId();
 				}
 				params.put("group_scope", groupIds);
 			}
 			if(wait >= 0)
 				params.put("wait", wait);
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.relatedGroups", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawResult = (JSONObject)client.parseResponse(response);
 
 		try {
 			JSONArray groups = rawResult.getJSONArray("related_groups");
 			for(int i = 0; i < groups.length(); i++) {
 				JSONObject groupJson = groups.getJSONObject(i);
 				SaploGroup relGroup = SaploGroup.convertFromJSONToGroup(groupJson);
 				relGroup.setRelatedToGroup(saploGroup);
 				relatedGroupsList.add(relGroup);
 			}
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		saploGroup.setRelatedGroups(relatedGroupsList);
 	}
 
 	/**
 	 * Asynchronously get related groups for a given {@link SaploGroup} object.
 	 * Then the related group list can be retrieved by {@link SaploGroup#getRelatedGroups()}
 	 * 
 	 * Here is an example usage:
 	 * <pre>
 	 *	SaploGroup myGroup = new SaploGroup("my example group", Language.en);
 	 *	SaploFuture<Boolean> future = groupMgr.relatedGroupsAsync(myGroup, relatedBy, groupScope, 10);
 	 *
 	 *	// do some other stuff here
 	 *
 	 *	boolean processOk = false;
 	 *	try {
 	 *		processOk = future.get(3, TimeUnit.SECONDS);
 	 *	} catch (InterruptedException e) {
 	 *		e.printStackTrace();
 	 *	} catch (ExecutionException e) {
 	 *		e.printStackTrace();
 	 *	} catch (TimeoutException e) {
 	 *		future.cancel(false);
 	 *		e.printStackTrace();
 	 *	}
 	 *
 	 *	if(processOk) {
 	 *		List<SaploGroup> relatedGroups = myGroup.getRelatedGroups();
 	 *
 	 *		// do some other operations as you prefer
 	 *
 	 *	} else {
 	 *		// something went wrong
 	 *	}
 	 * </pre>
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} object to search related groups against
 	 * @param groupScope - the {@link SaploGroup}s the given group should be compared to.
 	 * By default, all the user groups are searched.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @return SaploFuture<relatedGroupsList> - a {@link List} containing related groups to the given group
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedGroupsAsync(final SaploGroup saploGroup, final SaploGroup[] groupScope, final int wait) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				relatedGroups(saploGroup, groupScope, wait);
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Search for texts that are related to the given group.
 	 * Then, the related text list can be retrieved by {@link SaploGroup#getRelatedTexts()}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} object to search related texts against
 	 * @param collection - Search the given collections to find related texts.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param limit - the maximum number of related texts in the result. 
 	 * @throws SaploClientException 
 	 */
 	public void relatedTexts(SaploGroup saploGroup, SaploCollection collection, int wait, int limit) throws SaploClientException {
 		verifyId(saploGroup);
 		
 		List<SaploText> relatedTextsList = new ArrayList<SaploText>();
 
 		JSONObject params = new JSONObject();
 		try {
 			params.put("group_id", saploGroup.getId());
 			if(collection != null) {
 				params.put("collection_scope", collection.getId());
 			} else {
 				throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "collection_scope");
 			}
 			if(wait >= 0)
 				params.put("wait", wait);
 			if(limit > 0)
 				params.put("limit", limit);
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "group.relatedTexts", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawResult = (JSONObject)client.parseResponse(response);
 
 		try {
 			JSONArray texts = rawResult.getJSONArray("related_texts");
 
 			for(int i = 0; i < texts.length(); i++) {
 				JSONObject textJson = texts.getJSONObject(i);
 				SaploText relText = SaploText.convertFromJSONToText(textJson);
 				relText.setRelatedToGroup(saploGroup);
 				relatedTextsList.add(relText);
 			}
 		} catch(JSONException je) {
 			throw new SaploClientException(CODE_JSON_EXCEPTION, je);
 		}
 
 		saploGroup.setRelatedTexts(relatedTextsList);
 	}
 
 	/**
 	 * Search for texts that are related to the given groups.
 	 * Then, the related text list can be retrieved by {@link SaploGroup#getRelatedTexts()}
 	 * NOTE: For now, only the first group in the array is used. 
 	 * Use {@link #relatedTexts(SaploGroup, SaploCollection, int, int)} instead.
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} object to search related texts against
 	 * @param collectionScope - Search the given collections to find related texts.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param limit - the maximum number of related texts in the result. 
 	 * @throws SaploClientException 
 	 */
 	public void relatedTexts(SaploGroup saploGroup, SaploCollection[] collectionScope, int wait, int limit) 
 	throws SaploClientException {
 		if(collectionScope.length > 0) {
 			relatedTexts(saploGroup, collectionScope[0], wait, limit);
 		} else {
 			throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "collection_scope");
 		}
 	}
 	
 	/**
 	 * Asynchronously search for texts that are related to the given group.
 	 * Then, the related text list can be retrieved by {@link SaploGroup#getRelatedTexts()}
 	 * 
 	 * @param saploGroup - the {@link SaploGroup} object to search related texts against
 	 * @param collection - Search the given collections to find related texts.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param limit - the maximum number of related texts in the result. 
 	 * @return SaploFuture<relatedTextsList> - a {@link List} containing related texts to the given group
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedTextsAsync(final SaploGroup saploGroup, final SaploCollection collection, final int wait, final int limit) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				relatedTexts(saploGroup, collection, wait, limit);
 				return true;
 			}
 		}));
 	}
 
 	/*
 	 * ensure the given group has id
 	 */
 	private static void verifyId(SaploGroup saploGroup) throws SaploClientException {
 		if(saploGroup.getId() < 1)
 			throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "group.id");
 	}
 	
 	/*
 	 * ensure the given text has id
 	 */
 	private static void verifyId(SaploText saploText) throws SaploClientException {
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "text.id");
 	}
 
 	/*
 	 * ensure the given text has collection_id
 	 */
 	private static void verifyCollection(SaploText saploText) throws SaploClientException {
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(MSG_CLIENT_FIELD, CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 	}
 
 }
