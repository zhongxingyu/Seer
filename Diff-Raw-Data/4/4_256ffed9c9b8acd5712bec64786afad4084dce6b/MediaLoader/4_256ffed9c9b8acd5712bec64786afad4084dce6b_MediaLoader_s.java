 package org.witness.informa;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.util.JSONTokener;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.ektorp.*;
 import org.ektorp.http.HttpClient;
 import org.ektorp.http.StdHttpClient;
 import org.ektorp.impl.StdCouchDbConnector;
 import org.ektorp.impl.StdCouchDbInstance;
 import org.ektorp.util.Base64;
 
 import org.witness.informa.utils.Constants;
 import org.witness.informa.utils.CouchParser;
 import org.witness.informa.utils.CouchParser.Source;
 import org.witness.informa.utils.CouchParser.Submission;
 import org.witness.informa.utils.CouchParser.User;
 import org.witness.informa.utils.LocalConstants;
 import org.witness.informa.utils.CouchParser.Derivative;
 
 public class MediaLoader implements Constants {
 	public InformaSearch search;
 
 	StdCouchDbConnector dbSources, dbDerivatives, dbUsers, dbSubmissions;
 
 	public MediaLoader() {
 		StdCouchDbInstance db = null;
 		try {
 			HttpClient couchClient = new StdHttpClient.Builder()
 				.url("http://localhost:5984")
 				.username(LocalConstants.USERNAME)
 				.password(LocalConstants.PASSWORD)
 				.build();
 			db = new StdCouchDbInstance(couchClient);
 		} catch (MalformedURLException e) {
 			CouchParser.Log(Couch.ERROR, e.toString());
 			e.printStackTrace();
 
 		}
 
 		dbSources = new StdCouchDbConnector("sources", db);
 		dbDerivatives = new StdCouchDbConnector("derivatives", db);
 		dbUsers = new StdCouchDbConnector("admin", db);
 		dbSubmissions = new StdCouchDbConnector("submissions", db);
 
 		search = new InformaSearch(dbDerivatives);
 
 		/* TODO: you can clear the databases by uncommenting the following
 		 CouchParser.ClearDatabase(dbDerivatives, Derivative.class);
 		 CouchParser.ClearDatabase(dbSubmissions, Submission.class);
 		 CouchParser.ClearDatabase(dbSources, Source.class);
 		 */
 		 
		 CouchParser.ClearDatabase(dbSubmissions, Submission.class);
 	}
 
 	public ArrayList<JSONObject> getDerivatives() {
 		ViewQuery getDerivatives = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		ArrayList<JSONObject> res = CouchParser.getRows(dbDerivatives, getDerivatives, Couch.Views.Derivatives.GET_ALL_SHORTENED, null);
 		return res;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> saveSearch(Map<String, Object> saveRequest) {
 
 		ViewQuery getUsers = new ViewQuery().designDocId(Couch.Design.ADMIN);
 		Map<String, Object> savedSearches = new HashMap<String, Object>();
 		savedSearches.put(Couch.Views.Admin.Keys.SAVED_SEARCHES, search.saveSearch((String) saveRequest.get(DC.Options.ALIAS), (Map<String, Object>) saveRequest.get(DC.Options.PARAMETERS), CouchParser.getRecord(dbUsers, getUsers, Couch.Views.Admin.GET_BY_ID, (String) saveRequest.get(DC.Options._ID), null)));
 
 		Map<String, Object> savedSearchResult = new HashMap<String, Object>();
 		savedSearchResult.put(Couch.Views.Admin.Keys.SAVED_SEARCHES, (List<JSONObject>) savedSearches.get(Couch.Views.Admin.Keys.SAVED_SEARCHES));
 
 		String newRev = CouchParser.updateRecord(User.class, dbUsers, (String) saveRequest.get(DC.Options._ID), (String) saveRequest.get(DC.Options._REV), savedSearches);
 		if(newRev != null) {
 			savedSearchResult.put(Couch.Views.Admin.Keys._REV, newRev);
 			return savedSearchResult;
 		} else
 			return null;
 
 	}
 
 	public ArrayList<JSONObject> getSources() {
 		ArrayList<JSONObject> sourcesList = new ArrayList<JSONObject>();
 
 		return sourcesList;
 	}
 
 	public ArrayList<JSONObject> getSearchResults(Map<String, Object> searchParams) {
 		return search.query(searchParams);
 	}
 
 	public ArrayList<JSONObject> getSearchResults(String viewHash) {
 		return search.query(viewHash);
 	}
 
 	public JSONObject loginUser(Map<String, Object> credentials, String sessionId) {
 		ViewQuery getUsers = new ViewQuery().designDocId(Couch.Design.ADMIN);
 		String unpw = (String) credentials.get("username") + (String) credentials.get("password");
 		// TODO: set current session id, cookies, etc.
 		JSONObject user = CouchParser.getRecord(dbUsers, getUsers, Couch.Views.Admin.ATTEMPT_LOGIN, unpw, new String[] {"unpw"});
 
 		Map<String, Object> new_session = new HashMap<String, Object>();
 		new_session.put(DC.Options.CURRENT_SESSION, sessionId);
 
 		if(user != null)
 			user.put(DC.Options._REV, CouchParser.updateRecord(User.class, dbUsers, user.getString(DC.Options._ID), user.getString(DC.Options._REV), new_session));
 
 		user.remove(DC.Options.CURRENT_SESSION);
 		return user;
 	}	
 
 	public JSONObject renameMedia(String id, String rev, String newAlias) {
 		Map<String, Object> setAlias = new HashMap<String, Object>();
 		setAlias.put("alias", newAlias);
 
 		JSONObject res = new JSONObject();
 		res.put(DC.Options.ALIAS, newAlias);
 		res.put(DC.Options.RESULT, CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, setAlias));
 		return res;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject loadMedia(String id) {
 		ViewQuery getDerivative = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, getDerivative, Couch.Views.Derivatives.GET_BY_ID, id, null);
 
 		// copy representations to img cache
 		File mediaCache = new File(MEDIA_CACHE);
 		if(!mediaCache.exists())
 			mediaCache.mkdir();
 
 		// TODO: this should be streamed directly to the client, not dropped into the session_cache...
 		Iterator<String> rIt = derivative.getJSONArray("representation").iterator();
 		String path = "";
 		while(rIt.hasNext()) {
 			String repName = rIt.next();
 			path = repName.substring(0, repName.length() - 4);
 			File original = new File(DERIVATIVE_ROOT + path, repName);
 			File representation = new File(MEDIA_CACHE, original.getName());
 			try {
 				FileChannel o = new FileInputStream(original).getChannel();
 				FileChannel r = new FileOutputStream(representation).getChannel();
 				r.transferFrom(o, 0, o.size());
 			} catch(IOException e) {
 				CouchParser.Log(Couch.ERROR, e.toString());
 				e.printStackTrace();
 			}
 		}
 
 		derivative.put(DC.Options.MESSAGES, getMessages(path).toString());
 		return derivative;
 	}
 
 	public void cleanup() {
 		CouchParser.Log(Couch.INFO, "cleaning up derivatives");
 		for(JSONObject d : getDerivatives()) {
 			ViewQuery getDerivative = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 			String id = (String) d.get(DC.Options._ID);
 
 			JSONObject derivative = CouchParser.getRecord(dbDerivatives, getDerivative, Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 			try {
 				String rev = (String) derivative.get(DC.Options._REV);
 				JSONArray discussions = derivative.getJSONArray("discussions");
 				for(Object discussion_ : discussions) {
 					JSONObject discussion = (JSONObject) discussion_;
 
 					if(discussion.containsKey(DC.Options.DELETE_FLAG) && discussion.getBoolean(DC.Options.DELETE_FLAG)) {
 						discussions.remove(discussion);
 					}
 				}
 
 				Map<String, Object> updateValues = new HashMap<String, Object>();
 				updateValues.put("discussions", discussions);
 
 				CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 			} catch(NullPointerException e) {}
 		}
 	}
 
 	private JSONArray getMessages(String path) {
 		JSONArray messages = new JSONArray();
 		File dir = new File(DERIVATIVE_ROOT + path + "/messages");
 		for(File msgFile : dir.listFiles()) {
 			if(!msgFile.getName().equals(".DS_Store")) {
 				JSONObject message = new JSONObject();
 				try {
 					String ts = msgFile.getName().split("_")[0];
 					if(ts.length() == 10)
 						ts += "000";
 					message.put(DC.Options.CONTENT, fileToString(msgFile));
 					message.put(DC.Options.TIMESTAMP, Long.parseLong(ts));
 					message.put(DC.Options.FROM_CLIENT, (msgFile.getName().split(".txt")[0] + ".").contains("_R.") ? true : false);
 					messages.add(message);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return messages;
 	}
 
 	public static String fileToString(File file) throws IOException {
 		StringBuffer sb = new StringBuffer();
 		FileInputStream fis = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 		String line;
 		while((line = br.readLine()) != null)
 			sb.append(line + "<br />");
 		return sb.toString();
 	}
 
 	public static ArrayList<String> fileToStrings(File file) throws IOException {
 		ArrayList<String> fStrings = new ArrayList<String>();
 		FileInputStream fis = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 		String line;
 		while((line = br.readLine()) != null)
 			fStrings.add(line);
 		return fStrings;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject addAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) annotationPack.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		Map<String, Object> entity = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) entity.remove(DC.Options._ID);
 
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 		String rev = (String) derivative.get(DC.Options._REV);
 
 		JSONObject annotationStub = new JSONObject();
 		annotationStub.put(DC.Options.TIMESTAMP, System.currentTimeMillis());
 		annotationStub.put(DC.Options.ORIGINATED_BY, user.get(DC.Options._ID));
 
 		Map<String, Object> annotation = (Map<String, Object>) entity.get(DC.Options.ANNOTATION);
 		annotationStub.put(DC.Options.DURATION, annotation.get(DC.Options.DURATION));
 		annotationStub.put(DC.Options.TIME_IN, annotation.get(DC.Options.TIME_IN));
 		annotationStub.put(DC.Options.TIME_OUT, annotation.get(DC.Options.TIME_OUT));
 		annotationStub.put(DC.Options.REGION_BOUNDS, annotation.get(DC.Options.REGION_BOUNDS));
 		annotationStub.put(DC.Options.ANNOTATIONS, new JSONArray());
 		d.add(annotationStub);
 
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 		int discussionId = d.size() - 1;
 
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject appendToAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		if(!CouchParser.validateUser(dbUsers, (Map<String, Object>) annotationPack.get(DC.Options.USER)))
 			return result;
 
 		Map<String, Object> discussion = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) discussion.remove(DC.Options._ID);
 		String rev = (String) discussion.remove(DC.Options._REV);
 		int discussionId = Integer.parseInt(String.valueOf(discussion.get(DC.Options.DISCUSSION_ID)));
 
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 
 		JSONObject annotation = new JSONObject();
 		annotation.put(Annotation.Keys.CONTENT, (String) discussion.remove(Annotation.Keys.CONTENT));
 		annotation.put(Annotation.Keys.SUBMITTED_BY, ((Map<String, Object>) annotationPack.get(DC.Options.USER)).get(DC.Options._ID));
 		annotation.put(Annotation.Keys.DATE, System.currentTimeMillis());
 		((JSONArray) ((JSONObject) d.get(discussionId)).get(DC.Options.ANNOTATIONS)).add(annotation);
 
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object sendMessage(Map<String, Object> messagePack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		if(!CouchParser.validateUser(dbUsers, (Map<String, Object>) messagePack.get(DC.Options.USER)))
 			return result;
 
 		Map<String, Object> entity = (Map<String, Object>) messagePack.get(DC.Options.ENTITY);
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, entity.get(DC.Options._ID), new String[] {"j3m"});
 
 		Iterator<String> rIt = derivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 
 		String messagePath = DERIVATIVE_ROOT + path + 
 				"/messages/" + 
 				System.currentTimeMillis() + "_" +
 				DigestUtils.md5Hex((String) entity.get(DC.Options.CONTENT)) +
 				".txt";
 		CouchParser.Log(Couch.INFO, messagePath);
 
 		try {
 			FileWriter fw = new FileWriter(messagePath);
 			fw.write((String) entity.get(DC.Options.CONTENT));
 			fw.flush();
 			fw.close();
 			derivative.put(DC.Options.MESSAGES, getMessages(path));
 			result.put(DC.Keys.RESULT, derivative);
 		} catch (IOException e) {
 			CouchParser.Log(Couch.ERROR, e.toString());
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object modifyAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		// user, annotation, modification to perform
 		Map<String, Object> user = (Map<String, Object>) annotationPack.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		Map<String, Object> discussion = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) discussion.remove(DC.Options._ID);
 		String rev = (String) discussion.remove(DC.Options._REV);
 		int discussionId = Integer.parseInt(String.valueOf(discussion.get(DC.Options.DISCUSSION_ID)));
 
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 
 		// does annotation really belong to user?
 		String originatedBy = d.getJSONObject(discussionId).getString(DC.Options.ORIGINATED_BY);
 		if(!originatedBy.equals(user.get(DC.Options._ID)))
 			return result;
 
 		// if so...
 		long editType = (Long) discussion.get(DC.Options.EDIT_TYPE);		
 		switch((int) editType) {
 		case DC.EditType.DELETE:
 			d.getJSONObject(discussionId).put(DC.Options.DELETE_FLAG, true);
 			// set this as deleted, but update the record after everyone is gone
 			break;
 		case DC.EditType.MOVE:
 			// perform modification on timeIn, timeOut, duration, regionBounds
 			Map<String, Object> annotation = (Map<String, Object>) discussion.get(DC.Options.ANNOTATION);
 			CouchParser.Log(Couch.INFO, annotation.toString());
 			d.getJSONObject(discussionId).put(DC.Options.TIME_IN, annotation.get(DC.Options.TIME_IN));
 			d.getJSONObject(discussionId).put(DC.Options.TIME_OUT, annotation.get(DC.Options.TIME_OUT));
 			d.getJSONObject(discussionId).put(DC.Options.DURATION, annotation.get(DC.Options.DURATION));
 			d.getJSONObject(discussionId).put(DC.Options.REGION_BOUNDS, annotation.get(DC.Options.REGION_BOUNDS));
 
 			break;
 		}
 
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject requestUploadTicket(Map<String, Object> uploadOpts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) uploadOpts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		Map<String, Object> sourceId = new HashMap<String, Object>();
 		sourceId.put("java.lang.String", "import_" + user.get(DC.Options._ID));
 
 		Map<String, Map<String, Object>> initialValues = new HashMap<String, Map<String, Object>>();
 		initialValues.put("sourceId", sourceId);
 
 		// create a blank submission descriptor with user with only a sourceId as "import_USERHASH"
 		// _rev is auth token here (later to be exchanged for j3m)
 		
 		// takes  key { className : val }
 		String[] newSubmission = CouchParser.createRecord(Submission.class, dbSubmissions, initialValues);
 		if(newSubmission == null)
 			return result;
 
 		result.put(DC.Keys.RESULT, DC.Results.OK);
 		result.put(DC.Options.NEW_SUBMISSION_ID, newSubmission[0]);
 		result.put(DC.Options.NEW_SUBMISSION_REV, newSubmission[1]);
 		result.put(DC.Options.NEW_SUBMISSION_URL, LocalConstants.SERVER_URL);
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject loadAdminModulesForUser(Map<String, Object> opts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) opts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		List<String> modules = new ArrayList<String>();
 		for(Entry<Integer, String> module : Admin.MODULES.entrySet()) {
 			// TODO: if user can use this module... (if blah blah blah == module.getKey())
 			modules.add(module.getValue());
 		}
 
 		if(modules.isEmpty())
 			return result;
 
 		result.put(DC.Keys.RESULT, DC.Results.OK);
 		result.put(DC.Options.AVAILABLE_MODULES, modules.toArray(new String[modules.size()]));
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject downloadClientCredentials(Map<String, Object> opts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) opts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		File ictd = new File(LocalConstants.ENGINE_ROOT + "sources/" + (String) opts.get(DC.Options.SOURCE_ID) + "/" + LocalConstants.ORGANIZATION_NAME + ".ictd");
 		if(!ictd.exists())
 			return result;
 
 
 		try {
 			FileInputStream fis = new FileInputStream(ictd);
 			byte[] ictd_ = new byte[fis.available()];
 			fis.read(ictd_);
 
 			result.put(DC.Keys.FILE_DATA, ictd.getAbsolutePath());
 			result.put(DC.Keys.SOURCE_ID, (String) opts.get(DC.Options.SOURCE_ID));
 			result.put(DC.Keys.CONTAINER_URL, LocalConstants.SERVER_URL + "ictd.php");
 			result.put(DC.Keys.RESULT, DC.Results.OK);
 
 			fis.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings({ "unchecked", "deprecation" })
 	public JSONObject initNewClient(Map<String, Object> opts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) opts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		Map<String, Object> newClient = (Map<String, Object>) opts.get(DC.Options.NEW_CLIENT);
 		try {
 			String name = (String) newClient.get(DC.Options.NEW_CLIENT_NAME);
 			String email = (String) newClient.get(DC.Options.NEW_CLIENT_EMAIL);
 
 			String key_package = new String(Base64.decode((String) newClient.get(DC.Options.NEW_CLIENT_KEY)));
 
 			String key = key_package.substring(0, key_package.lastIndexOf(0x0A));
 			String base_image = key_package.substring(key_package.lastIndexOf(0x0A) + 1);
 
 			// XXX: GET THE FINGERPRINT: this is tempName OK?
 
 			String tempKeyName = DigestUtils.md5Hex(name + email + System.currentTimeMillis()) + ".asc";
 
 			try {
 				FileWriter fw = new FileWriter(LocalConstants.CLIENT_TEMP + tempKeyName);
 				fw.write(key);
 				fw.flush();
 				fw.close();
 			} catch (IOException e) {
 				CouchParser.Log(Couch.ERROR, e.toString());
 				e.printStackTrace();
 			}
 
 			List<String> cmd = new ArrayList<String>();
 			if(LocalConstants.SUDOER != null) {
 				cmd.add("echo");
 				cmd.add(LocalConstants.SUDOER);
 				cmd.add("|");
 			}
 
 			cmd.add("sudo");
 			cmd.add("-S");
 			cmd.add(LocalConstants.ScriptsRoot.PY + "new_client.py");
 			cmd.add(name);
 			cmd.add(email);
 			cmd.add(LocalConstants.CLIENT_TEMP + tempKeyName);
 
 			StringBuffer cmdStr = new StringBuffer();
 			for(String c : cmd)
 				cmdStr.append(" " + c + " ");
 
 			String cmds[] = {
 					"sh",
 					"-c",
 					cmdStr.toString().substring(1)
 			};
 
 			CouchParser.Log(Couch.INFO, cmdStr.toString());
 
 			Process p = Runtime.getRuntime().exec(cmds);
 			DataInputStream dis_i = new DataInputStream(p.getInputStream());
 			DataInputStream dis_e = new DataInputStream(p.getErrorStream());
 			try {
 				String line;
 				while((line = dis_e.readLine()) != null)
 					CouchParser.Log(Couch.INFO, line);
 			} catch(IOException e) {
 				CouchParser.Log(Couch.ERROR, e.toString());
 				e.printStackTrace();
 			}
 
 			try {
 				String line, pyResult = null;
 				while((line = dis_i.readLine()) != null) {
 					CouchParser.Log(Couch.INFO, line);
 					pyResult = line;
 				}
 
 				// parse pyResult
 				String[] pResult = pyResult.split(", ");
 				for(int x=0; x<pResult.length; x++) {
 					pResult[x] = pResult[x].replace("[", "");
 					pResult[x] = pResult[x].replace("]", "");
 					pResult[x] = pResult[x].replace("u'", "");
 					pResult[x] = pResult[x].replace("'", "");
 				}
 
 				// add new source in db (sourceId has to be lowercased!)
 				JSONObject cred = (JSONObject) new JSONTokener(pResult[1]).nextValue();
 
 				File sourceRoot = new File(LocalConstants.ENGINE_ROOT + "sources/" + ((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase());
 
 				if(!sourceRoot.exists()) {
 					// copy over key
 					sourceRoot.mkdir();
 					File original = new File(LocalConstants.CLIENT_TEMP + tempKeyName);
 					File sourceKey = new File(sourceRoot, ((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase() + ".asc");
 					File baseImage = new File(sourceRoot, "base_image.jpg");
 
 					try {
 						FileChannel o = new FileInputStream(original).getChannel();
 						FileChannel r = new FileOutputStream(sourceKey).getChannel();
 						r.transferFrom(o, 0, o.size());
 
 						FileOutputStream fos = new FileOutputStream(baseImage);
 						fos.write(Base64.decode(base_image));
 						fos.flush();
 						fos.close();
 
 					} catch(IOException e) {
 						CouchParser.Log(Couch.ERROR, e.toString());
 						e.printStackTrace();
 						original.delete();
 						sourceKey.delete();
 						sourceRoot.delete();
 						return result;
 					} 
 
 					// delete tmp
 					original.delete();
 					original = new File(pResult[0]);
 					File sourceICTD = new File(LocalConstants.ENGINE_ROOT + "sources/" + ((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase(), LocalConstants.ORGANIZATION_NAME + ".ictd");
 
 					try {
 						FileChannel o = new FileInputStream(original).getChannel();
 						FileChannel r = new FileOutputStream(sourceICTD).getChannel();
 						r.transferFrom(o, 0, o.size());
 
 
 						if(checkForSource(((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase(), name, email)) {
 
 							result.put(DC.Options.NEW_CLIENT, ((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase());
 							result.put(DC.Keys.RESULT, DC.Results.OK);
 
 
 							original.delete();
 							sourceKey.delete();
 							sourceRoot.delete();
 							return result;
 						}
 					} catch(IOException e) {
 						CouchParser.Log(Couch.ERROR, e.toString());
 						e.printStackTrace();
 						original.delete();
 						sourceKey.delete();
 						sourceRoot.delete();
 						sourceICTD.delete();
 						return result;
 					} 
 				} else {
 					if(checkForSource(((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase(), name, email)) {
 						result.put(DC.Options.NEW_CLIENT, ((String) cred.get(Couch.Views.Sources.Keys.SOURCE_ID)).toLowerCase());
 						result.put(DC.Keys.RESULT, DC.Results.OK);
 					}
 				}
 
 
 			} catch(IOException e) {
 				CouchParser.Log(Couch.ERROR, e.toString());
 				e.printStackTrace();
 			}
 
 		} catch(IOException e) {
 			CouchParser.Log(Couch.ERROR, e.toString());
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	public boolean checkForSource(String sourceId_, String alias_, String email_) {
 		CouchParser.Log(Couch.DEBUG, "checking for this source: " + sourceId_);
 		
 		if(CouchParser.getRecord(dbSources, new ViewQuery().designDocId(Couch.Design.SOURCES), Couch.Views.Sources.GET_BY_ID, sourceId_, null) == null) {
 			CouchParser.Log(Couch.DEBUG, "source does not exist... placing it");
 			Map<String, Object> sourceId = new HashMap<String, Object>();
 			sourceId.put("java.lang.String", sourceId_);
 
 			Map<String, Object> alias = new HashMap<String, Object>();
 			alias.put("java.lang.String", alias_);
 
 			Map<String, Object> email = new HashMap<String, Object>();
 			email.put("java.lang.String", email_);
 
 			Map<String, Map<String, Object>> initialValues = new HashMap<String, Map<String, Object>>();
 			initialValues.put(Couch.Views.Sources.Keys.SOURCE_ID, sourceId);
 			initialValues.put(Couch.Views.Sources.Keys.ALIAS, alias);
 			initialValues.put(Couch.Views.Sources.Keys.EMAIL, email);
 
 			if(CouchParser.createRecord(Source.class, dbSources, initialValues) != null) {
 				return true;
 			}
 		} else {
 			return true;
 		}
 		
 		return false;
 	}
 
 	public JSONObject loadClients() {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		ViewQuery getClients = new ViewQuery().designDocId(Couch.Design.SOURCES);
 		ArrayList<JSONObject> res = CouchParser.getRows(dbSources, getClients, Couch.Views.Sources.GET_BY_ID, Couch.Views.Sources.Omits.SHORT_DESCRIPTION);
 		if(res.size() == 0)
 			return result;
 
 		ViewQuery getSubmissionsBySource = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		for(JSONObject j : res) {
 			// get the number of submissions, and date of latest submission
 			CouchParser.Log(Couch.INFO, j.toString());
 			String sourceId = j.getString(Couch.Views.Sources.Keys.SOURCE_ID);
 			ArrayList<JSONObject> submissions = CouchParser.getRows(dbDerivatives, getSubmissionsBySource, Couch.Views.Derivatives.GET_BY_SOURCE_ID, sourceId, Couch.Views.Derivatives.Omits.SHORT_DESCRIPTION);
 
 			j.remove(Couch.Documents._ID);
 			j.remove(Couch.Documents._REV);
 
 			// XXX: why is this wonky?
 			j.put(Couch.Views.Sources.Keys.NUMBER_OF_SUBMISSIONS, submissions == null ? 0 :submissions.size());
 
 			if(submissions != null) {
 				/*
 				CouchParser.Log(Couch.INFO, "before sort:");
 				for(JSONObject sub : submissions)
 					CouchParser.Log(Couch.INFO, sub.toString());
 				 */
 
 				Collections.sort(submissions, new Comparator<JSONObject>() {
 					public int compare(JSONObject j1, JSONObject j2) {
 						if(j1.getLong(Couch.Views.Derivatives.Keys.TIMESTAMP_INDEXED) == j2.getLong(Couch.Views.Derivatives.Keys.TIMESTAMP_INDEXED))
 							return 0;
 						return j1.getLong(Couch.Views.Derivatives.Keys.TIMESTAMP_INDEXED) < j2.getLong(Couch.Views.Derivatives.Keys.TIMESTAMP_INDEXED) ? -1 : 1;
 					}
 
 				});
 
 				/*
 				CouchParser.Log(Couch.INFO, "AFTER sort:");
 				for(JSONObject sub : submissions)
 					CouchParser.Log(Couch.INFO, sub.toString());
 				 */	
 				j.put(Couch.Views.Sources.Keys.LAST_SUBMISSION_DATE, submissions.get(0).getLong(Couch.Views.Derivatives.Keys.TIMESTAMP_INDEXED));
 			}
 		}
 
 		result.put(DC.Keys.CLIENT_LIST, res);
 		result.put(DC.Keys.RESULT, DC.Results.OK);
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject getClient(Map<String, Object> opts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 
 		Map<String, Object> user = (Map<String, Object>) opts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 
 		ViewQuery getClient = new ViewQuery().designDocId(Couch.Design.SOURCES);
 		JSONObject source = CouchParser.getRecord(dbSources, getClient, Couch.Views.Sources.GET_BY_ID, opts.get(DC.Options.SOURCE_ID), null);
 		if(source == null)
 			return result;
 
 		result.put(DC.Keys.METADATA, source);
 		result.put(DC.Keys.RESULT, DC.Results.OK);
 		return result;
 	}
 }
