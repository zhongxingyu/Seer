 /**
  * 
  */
 package com.saplo.api.client.manager;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.saplo.api.client.ResponseCodes;
 import com.saplo.api.client.SaploClient;
 import com.saplo.api.client.SaploClientException;
 import com.saplo.api.client.entity.JSONRPCRequestObject;
 import com.saplo.api.client.entity.JSONRPCResponseObject;
 import com.saplo.api.client.entity.SaploCollection;
 import com.saplo.api.client.entity.SaploFuture;
 import com.saplo.api.client.entity.SaploGroup;
 import com.saplo.api.client.entity.SaploTag;
 import com.saplo.api.client.entity.SaploText;
 import com.saplo.api.client.entity.SaploText.RelatedBy;
 import com.saplo.api.client.util.ClientUtil;
 
 /**
  * A manager class for operations on {@link SaploText} objects
  * 
  * @author progre55
  *
  */
 public class SaploTextManager {
 
 	private SaploClient client;
 	private ExecutorService es;
 	private static final int thread_count = 5;
 	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");;
 
 	/**
 	 * A simple constructor 
 	 * 
 	 * @param clientToUse - the client to use to communicate to the Saplo-API server
 	 */
 	public SaploTextManager(SaploClient clientToUse) {
 		this.client = clientToUse;
 		es = Executors.newFixedThreadPool(thread_count);
 	}
 
 	/**
 	 * Add a text to a collection.
 	 * 
 	 * @param saploText - the text to be added, should contain a 
 	 * {@link SaploText#getCollection()} object of type {@link SaploCollection}
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void create(SaploText saploText) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(ClientUtil.NULL_STRING.equals(saploText.getBody()))
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.body");
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("body", saploText.getBody());
 		if(!ClientUtil.NULL_STRING.equals(saploText.getHeadline()))
 			params.put("headline", saploText.getHeadline());
 		if(!ClientUtil.NULL_STRING.equals(saploText.getLead()))
 			params.put("lead", saploText.getLead());
 		if(saploText.getPublishDate() != null)
 			params.put("publish_date", sf.format(saploText.getPublishDate()));
 		else
 			params.put("publish_date", sf.format(new Date()));
 		if(saploText.getUrl() != null)
 			params.put("url", saploText.getUrl().toString());
 		if(!ClientUtil.NULL_STRING.equals(saploText.getAuthors()))
 			params.put("authors", saploText.getAuthors());
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.create", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonText = (JSONObject)client.parseResponse(response);
 
 		SaploText.convertFromJSONToText(jsonText, saploText);
 	}
 
 	/**
 	 * Asynchronously create text.
 	 * This method returns a {@link SaploFuture}<{@link Boolean}> object, 
 	 * which is <code>true</code> in case of success and <code>false</code> in case of failure.
 	 * 
 	 * Here is an example usage:
 	 * <pre>
 	 *	SaploText myText = new SaploText(myCollection, myTextBody);
 	 *	SaploFuture<Boolean> future = textMgr.createAsync(myText);
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
 	 *		int myTextId = myText.getId();
 	 *		// do some other operations as you prefer
 	 *	} else {
 	 *		// something went wrong
 	 *	}
 	 * </pre>
 	 * 
 	 * 
 	 * @param saploText - the {@link SaploText} to create
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> createAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					create(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 	
 	/**
 	 * A convenient method for adding texts with only required parameters.
 	 * 
 	 * @param collectionId
 	 * @param bodyText
 	 * @return saploText
 	 * 
 	 * @throws SaploClientException
 	 * @throws JSONException
 	 */
 	public SaploText create(int collectionId, String bodyText) throws SaploClientException, JSONException {
 		SaploCollection col = new SaploCollection();
 		col.setId(collectionId);
 		
 		SaploText saploText = new SaploText(col, bodyText);
 		
 		create(saploText);
 		
 		return saploText;
 	}
 
 	/**
 	 * Get a text given its id and collection_id from the API
 	 * 
 	 * @param saploText - a {@link SaploText} object with a mandatory id and collection parameters
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void get(SaploText saploText) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.get", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonText = (JSONObject)client.parseResponse(response);
 
 		SaploText.convertFromJSONToText(jsonText, saploText);
 	}
 
 	/**
 	 * Asynchronously get a text.
 	 * For an example usage, see {@link #createAsync(SaploText)}
 	 * 
 	 * @param saploText - a {@link SaploText} object with a mandatory id and collection parameters
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> getAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					get(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * A convenient way of getting text with only the required parameters
 	 * 
 	 * @param collectionId
 	 * @param textId
 	 * @return saploText
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException
 	 */
 	public SaploText get(int collectionId, int textId) throws JSONException, SaploClientException {
 		SaploCollection col = new SaploCollection();
 		col.setId(collectionId);
 		
 		SaploText text = new SaploText();
 		text.setCollection(col);
 		text.setId(textId);
 		
 		get(text);
 		
 		return text;
 	}
 	
 	/**
 	 * Update an existing text.
 	 * 
 	 * @param saploText - the {@link SaploText} to be updated
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void update(SaploText saploText) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 
 		if(!ClientUtil.NULL_STRING.equals(saploText.getHeadline()))
 			params.put("headline", saploText.getHeadline());
 
 		if(!ClientUtil.NULL_STRING.equals(saploText.getBody()))
 			params.put("body", saploText.getBody());
 
 		if(null != saploText.getPublishDate())
 			params.put("publish_date", sf.format(saploText.getPublishDate()));
 
 		if(null != saploText.getUrl())
 			params.put("url", saploText.getUrl().toString());
 
 		if(!ClientUtil.NULL_STRING.equals(saploText.getAuthors()))
 			params.put("authors", saploText.getAuthors());
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.update", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject jsonText = (JSONObject)client.parseResponse(response);
 
 		SaploText.convertFromJSONToText(jsonText, saploText);
 	}
 
 	/**
 	 * Asynchronously update a given text.
 	 * For an example usage, see {@link #createAsync(SaploText)}
 	 * 
 	 * @param saploText - the {@link SaploText} to be updated
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> updateAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					update(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Delete a text from the {@link SaploCollection} it belongs to.
 	 * 
 	 * @param saploText - the {@link SaploText} to be deleted, 
 	 * should have id and collection attributes
 	 * @return true if success
 	 * 
 	 * @throws JSONException if failure
 	 * @throws SaploClientException 
 	 */
 	public boolean delete(SaploText saploText) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.delete", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);

		return (Boolean)client.parseResponse(response);
 	}
 
 	/**
 	 * Asynchronously delete a given text.
 	 * For an example usage, see {@link #createAsync(SaploText)}
 	 * 
 	 * @param saploText - the {@link SaploText} to be deleted
 	 * @return a {@link SaploFuture}<{@link Boolean}> with success or fail
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> deleteAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>(es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					return delete(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 			}
 		}));
 	}
 	
 	/** 
 	 * A convenient way of deleting SaploText by textId
 	 *  
 	 * @param textId
 	 * @return success?
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException
 	 */
 	public boolean delete(int collectionId, int textId) throws JSONException, SaploClientException {
 		
 		SaploCollection collection = new SaploCollection();
 		collection.setId(collectionId);
 		
 		SaploText text = new SaploText();
 		text.setId(textId);
 		text.setCollection(collection);
 		
 		
 		return delete(text);
 	}
 
 	/**
 	 * Get all entity tags that exist in the text. 
 	 * Categorized as person, organization, location, url, unknown.
 	 * 
 	 * @param saploText - the text to extract the {@link SaploTag}s from
 	 * @param wait - how long to wait for the API to return (seconds)
 	 * @param skipCategorization
 	 * @return tagList - a {@link List} containing all the tags extracted
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public List<SaploTag> tags(SaploText saploText, int wait, boolean skipCategorization) throws JSONException, SaploClientException {
 
 		List<SaploTag> tagList = new ArrayList<SaploTag>();
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 		if(wait >= 0)
 			params.put("wait", wait);
 		params.put("skip_categorization", skipCategorization);
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.tags", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawResult = (JSONObject)client.parseResponse(response);
 
 		JSONArray tags = rawResult.getJSONArray("tags");
 		for(int i = 0; i < tags.length(); i++) {
 			JSONObject tagJson = tags.getJSONObject(i);
 			SaploTag saploTag = SaploTag.convertFromJSONToTag(tagJson);
 			tagList.add(saploTag);
 		}
 		return tagList;
 	}
 
 	/**
 	 * Asynchronously get all entity tags that exist in the text.
 	 * For an example usage, see {@link #createAsync(SaploText)}
 	 * 
 	 * @param saploText - the text to extract the {@link SaploTag}s from
 	 * @param wait - how long to wait for the API to return (seconds)
 	 * @param skipCategorization
 	 * @return {@link SaploFuture}<{@link List}<{@link SaploTag}>> containing all the tags extracted
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<List<SaploTag>> tagsAsync(final SaploText saploText, final int wait, final boolean skipCategorization) {
 		return new SaploFuture<List<SaploTag>>( es.submit(new Callable<List<SaploTag>>() {
 			public List<SaploTag> call() throws SaploClientException {
 				List<SaploTag> tagList = null;
 				try {
 					tagList = tags(saploText, wait, skipCategorization);
 				} catch (JSONException e) {
 					return null;
 				}
 				return tagList;
 			}
 		}));
 	}
 	
 	/**
 	 * Get all entity tags that exist in the text. 
 	 * Categorized as person, organization, location, url, unknown.
 	 * 
 	 * @param saploText - the text to extract the {@link SaploTag}s from
 	 * @return tagList - a {@link List} containing all the tags extracted
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public List<SaploTag> tags(SaploText saploText) throws JSONException, SaploClientException {
 		return tags(saploText, ClientUtil.NULL_INT, false);
 	}
 
 	/**
 	 * Asynchronously get all entity tags that exist in the text.
 	 *
 	 * Here is an example usage:
 	 * <pre>
 	 *	SaploText myText = new SaploText(myCollection, myTextBody);
 	 *	SaploFuture<List<SaploTag>> future = textMgr.tagsAsync(myText;
 	 *
 	 *	// do some other stuff here
 	 *
 	 *	List<SaploTag> tags;
 	 *	try {
 	 *		tags = future.get(3, TimeUnit.SECONDS);
 	 *
 	 *		// do whatever you want with the <code>tags</code>
 	 *
 	 *	} catch (InterruptedException e) {
 	 *		e.printStackTrace();
 	 *	} catch (ExecutionException e) {
 	 *		e.printStackTrace();
 	 *	} catch (TimeoutException e) {
 	 *		future.cancel(false);
 	 *		e.printStackTrace();
 	 *	}
 	 *
 	 * </pre>
 	 * 
 	 * @param text - the text to extract the {@link SaploTag}s from
 	 * @return {@link SaploFuture}<{@link List}<{@link SaploTag}>> containing all the tags extracted
 	 * @throws
 	 */
 	public SaploFuture<List<SaploTag>> tagsAsync(final SaploText saploText) {
 		return new SaploFuture<List<SaploTag>>( es.submit(new Callable<List<SaploTag>>() {
 			public List<SaploTag> call() throws SaploClientException {
 				List<SaploTag> tagList = null;
 				try {
 					tagList = tags(saploText);
 				} catch (JSONException e) {
 					return null;
 				}
 				return tagList;
 			}
 		}));
 	}
 
 	/**
 	 * Retrieve tags by just providing collectionId and textId.
 	 * 
 	 * @param collectionId
 	 * @param textId
 	 * @return list
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException
 	 */
 	public List<SaploTag> tags(int collectionId, int textId) throws JSONException, SaploClientException {
 		SaploCollection col = new SaploCollection();
 		col.setId(collectionId);
 		
 		SaploText text = new SaploText();
 		text.setCollection(col);
 		text.setId(textId);
 		
 		return tags(text); 
 	}
 
 	/**
 	 * Search a collection/collections for related texts to a given text.
 	 *  
 	 * @param saploText - the {@link SaploText} to compare to
 	 * @param relatedBy - How the texts should be related.
 	 * {@link RelatedBy#context} - search for texts based on the same semantic meaning.
 	 * {@link RelatedBy#statistic} - search for texts based on statistical relations. 
 	 * E.g. number of words, similar words, common words etc. 
 	 * @param collectionScope - Search the given collections to find related texts.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param limit - the maximum number of related texts in the result. 
 	 * There is no guarantee that the number of results will be equal the limit. Max 50.
 	 * @param minThreshold - the minimum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @param maxThreshold - the maximum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @return relatedTextsList - a {@link List} containing related texts to the given text
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void relatedTexts(SaploText saploText, RelatedBy relatedBy, 
 			SaploCollection[] collectionScope, int wait, int limit, 
 			double minThreshold, double maxThreshold) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		List<SaploText> relatedTextsList = new ArrayList<SaploText>();
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 		if(relatedBy != null)
 			params.put("related_by", relatedBy);
 		if(collectionScope != null && collectionScope.length > 0) {
 			int collectionIds[] = new int[collectionScope.length];
 			for(int i = 0; i < collectionScope.length; i++) {
 				collectionIds[i] = collectionScope[i].getId();
 			}
 			params.put("collection_scope", collectionIds);
 		} else {
 			int collectionIds[] = {saploText.getCollection().getId()};
 			params.put("collection_scope", collectionIds);
 		}
 
 		if(wait >= 0)
 			params.put("wait", wait);
 		if(limit > 0)
 			params.put("limit", limit);
 		if(minThreshold >= 0 && minThreshold <= 1)
 			params.put("min_threshold", minThreshold);
 		if(maxThreshold >= 0 && minThreshold <= 1)
 			params.put("max_threshold", maxThreshold);
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.relatedTexts", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawResult = (JSONObject)client.parseResponse(response);
 
 		JSONArray texts = rawResult.getJSONArray("related_texts");
 		for(int i = 0; i < texts.length(); i++) {
 			JSONObject textJson = texts.getJSONObject(i);
 			SaploText relText = SaploText.convertFromJSONToText(textJson);
 			relText.setRelatedToText(saploText);
 			relatedTextsList.add(relText);
 		}
 		saploText.setRelatedTexts(relatedTextsList);
 		//		return relatedTextsList;
 	}
 
 	/**
 	 * Asynchronously search a collection/collections for related texts to a given text.
 	 * 
 	 * Here is an example usage:
 	 * <pre>
 	 *	SaploText myText = new SaploText(myCollection, myTextBody);
 	 *	SaploFuture<Boolean> future = textMgr.relatedTextsAsync(myText, relatedBy, colScope, 10, 10, 0, 1);
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
 	 *		List<SaploText> relatedTexts = myText.getRelatedTexts();
 	 *
 	 *		// do some other operations as you prefer
 	 *	} else {
 	 *		// something went wrong
 	 *	}
 	 * </pre>
 	 * 
 	 * @param saploText - the {@link SaploText} to compare to
 	 * @param relatedBy - How the texts should be related.
 	 * {@link RelatedBy#context} - search for texts based on the same semantic meaning.
 	 * {@link RelatedBy#statistic} - search for texts based on statistical relations. 
 	 * E.g. number of words, similar words, common words etc. 
 	 * @param collectionScope - Search the given collections to find related texts.
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param limit - the maximum number of related texts in the result. 
 	 * There is no guarantee that the number of results will be equal the limit. Max 50.
 	 * @param minThreshold - the minimum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @param maxThreshold - the maximum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @return SaploFuture<relatedTextsList> - a {@link List} containing related texts to the given text
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedTextsAsync(final SaploText saploText, final RelatedBy relatedBy, 
 			final SaploCollection[] collectionScope, final int wait, final int limit, 
 			final double minThreshold, final double maxThreshold) {
 		return new SaploFuture<Boolean>( es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					relatedTexts(saploText, relatedBy, collectionScope, wait, limit, minThreshold, maxThreshold);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Search the current collection for related texts to a given text
 	 * with default values
 	 * 
 	 * @param saploText - the {@link SaploText} object to compare to
 	 * @return relatedTextsList - a {@link List} containing related texts to the given text
 	 * 
 	 * @throws JSONException 
 	 * @throws SaploClientException 
 	 */
 	public void relatedTexts(SaploText saploText) throws JSONException, SaploClientException {
 		relatedTexts(saploText, null, null, -1, -1, -1, -1);
 	}
 
 	/**
 	 * Asynchronously search a collection/collections for related texts to a given 
 	 * text with default values. <br>
 	 * For an example usage, see {@link #relatedTextsAsync(SaploText, 
 	 * RelatedBy, SaploCollection[], int, int, double, double)}
 	 * 
 	 * @param saploText - the {@link SaploText} to compare to
 	 * @return SaploFuture<relatedTextsList> - a {@link List} containing related texts to the given text
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedTextsAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>( es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					relatedTexts(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 	
 	/**
 	 * Search for groups that are related to a given text.
 	 *  
 	 * @param saploText - the {@link SaploText} object to compare to
 	 * @param relatedBy - How the texts should be related.
 	 * {@link RelatedBy#automatic} - let the engine to choose the best approach.
 	 * {@link RelatedBy#semantic} - search for texts based on the same semantic meaning.
 	 * {@link RelatedBy#statistic} - search for texts based on statistical relations.
 	 * @param groupScope - the {@link SaploGroup}s the text should be compared to
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param minThreshold - the minimum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @param maxThreshold - the maximum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @return relatedGroupsList - a {@link List} containing related texts to the given group(s)
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void relatedGroups(SaploText saploText, RelatedBy relatedBy, 
 			SaploGroup[] groupScope, int wait, double minThreshold, 
 			double maxThreshold) throws JSONException, SaploClientException {
 
 		if(saploText.getCollection() == null || saploText.getCollection().getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.collection", "text.collection.id");
 		if(saploText.getId() <= 0)
 			throw new SaploClientException(ResponseCodes.MSG_CLIENT_FIELD, 
 					ResponseCodes.CODE_CLIENT_FIELD, "text.id");
 
 		List<SaploGroup> relatedGroupsList = new ArrayList<SaploGroup>();
 
 		JSONObject params = new JSONObject();
 		params.put("collection_id", saploText.getCollection().getId());
 		params.put("text_id", saploText.getId());
 		if(relatedBy != null)
 			params.put("related_by", relatedBy);
 		if(groupScope != null && groupScope.length > 0) {
 			int groupIds[] = new int[groupScope.length];
 			for(int i = 0; i < groupScope.length; i++) {
 				groupIds[i] = groupScope[i].getId();
 			}
 			params.put("group_scope", groupIds);
 		}
 		if(wait >= 0)
 			params.put("wait", wait);
 		if(minThreshold >= 0 && minThreshold <= 1)
 			params.put("min_threshold", minThreshold);
 		if(maxThreshold >= 0 && minThreshold <= 1)
 			params.put("max_threshold", maxThreshold);
 
 		JSONRPCRequestObject request = new JSONRPCRequestObject(client.getNextId(), "text.relatedGroups", params);
 
 		JSONRPCResponseObject response = client.sendAndReceive(request);
 
 		JSONObject rawResult = (JSONObject)client.parseResponse(response);
 
 		JSONArray groups = rawResult.getJSONArray("related_groups");
 		for(int i = 0; i < groups.length(); i++) {
 			JSONObject groupJson = groups.getJSONObject(i);
 			SaploGroup relGroup = SaploGroup.convertFromJSONToGroup(groupJson);
 			relGroup.setRelatedToText(saploText);
 			relatedGroupsList.add(relGroup);
 		}
 		saploText.setRelatedGroups(relatedGroupsList);
 		//		return relatedGroupsList;
 	}
 
 	/**
 	 * Asynchronously search for groups that are related to a given text.
 	 * <br>
 	 * For an example usage, see {@link #relatedTextsAsync(SaploText, 
 	 * RelatedBy, SaploCollection[], int, int, double, double)}
 	 *  
 	 * @param saploText - the {@link SaploText} object to compare to
 	 * @param relatedBy - How the texts should be related.
 	 * {@link RelatedBy#automatic} - let the engine to choose the best approach.
 	 * {@link RelatedBy#semantic} - search for texts based on the same semantic meaning.
 	 * {@link RelatedBy#statistic} - search for texts based on statistical relations.
 	 * @param groupScope - the {@link SaploGroup}s the text should be compared to
 	 * @param wait - maximum time to wait for the result to be calculated.
 	 * @param minThreshold - the minimum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @param maxThreshold - the maximum similarity threshold, between 0 and 1 (1 = 100% similar)
 	 * @return SaploFuture<relatedGroupsList> - a {@link List} containing related texts to the given group(s)
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedGroupsAsync(final SaploText saploText, final RelatedBy relatedBy, 
 			final SaploGroup[] groupScope, final int wait, final double minThreshold, 
 			final double maxThreshold) {
 		return new SaploFuture<Boolean>( es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					relatedGroups(saploText, relatedBy, groupScope, wait, minThreshold, maxThreshold);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 
 	/**
 	 * Search for groups that are related to a given text, with default values.
 	 * 
 	 * @param saploText - the {@link SaploText} object to compare to
 	 * @return relatedGroupsList - a {@link List} containing related texts to the given group(s)
 	 * 
 	 * @throws JSONException
 	 * @throws SaploClientException 
 	 */
 	public void relatedGroups(SaploText saploText) throws JSONException, SaploClientException {
 		relatedGroups(saploText, null, null, -1, -1, -1);
 	}
 
 	/**
 	 * Asynchronously search for groups that are related to a given text.
 	 * <br>
 	 * For an example usage, see {@link #relatedTextsAsync(SaploText, 
 	 * RelatedBy, SaploCollection[], int, int, double, double)}
 	 *  
 	 * @param saploText - the {@link SaploText} object to compare to
 	 * @return SaploFuture<relatedGroupsList> - a {@link List} containing related texts to the given group(s)
 	 * @throws SaploClientException
 	 */
 	public SaploFuture<Boolean> relatedGroupsAsync(final SaploText saploText) {
 		return new SaploFuture<Boolean>( es.submit(new Callable<Boolean>() {
 			public Boolean call() throws SaploClientException {
 				try {
 					relatedGroups(saploText);
 				} catch (JSONException e) {
 					return false;
 				}
 				return true;
 			}
 		}));
 	}
 	
 	public static SaploText getTextObject(int collectionId, int textId) {
 		SaploCollection col = new SaploCollection();
 		col.setId(collectionId);
 		
 		SaploText text = new SaploText();
 		text.setCollection(col);
 		text.setId(textId);
 		
 		return text;
 	}
 }
