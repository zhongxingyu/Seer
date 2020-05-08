 package de.hsrm.mi.mobcomp.y2k11grp04.service;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.net.Uri;
 import android.util.Log;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Answer;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Comment;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Meeting;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Model;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Question;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.QuestionOption;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.StateModel;
 import de.hsrm.mi.mobcomp.y2k11grp04.model.Topic;
 
 public class MoodServerApi {
 	private HttpClient client;
 	private Map<Uri, Class<? extends Model>> contextToModel = new HashMap<Uri, Class<? extends Model>>();
 	private Map<Class<? extends Model>, Uri> modelToContext = new HashMap<Class<? extends Model>, Uri>();
 
 	private class JSONReader<T extends Model> {
 		private T objectInstance;
 
 		public static final String KEY_CONTEXT = "@context";
 		public static final String KEY_ID = "@id";
 		public static final String KEY_RELATIONS = "@relations";
 		public static final String KEY_STATUS = "status";
 		public static final String KEY_RESULT = "result";
 		private JSONObject objectData;
 		private List<Class<? extends Model>> acceptedModels;
 
 		public JSONReader(JSONObject jsonData, Class<T> objectClass,
 				String dataKey) throws ApiException {
 			newInstance(objectClass);
 			try {
 				objectData = jsonData.getJSONObject(dataKey);
 			} catch (JSONException e) {
 				throw new ApiException("API error: Response has no " + dataKey
 						+ " object.");
 			}
 		}
 
 		private void newInstance(Class<T> objectClass) {
 			try {
 				this.objectInstance = objectClass.newInstance();
 			} catch (IllegalAccessException e) {
 				throw new InvalidParameterException("Could not access class "
 						+ objectClass.toString());
 			} catch (InstantiationException e) {
 				throw new InvalidParameterException(
 						"Could not instantiate class " + objectClass.toString());
 			}
 		}
 
 		public JSONReader(Class<T> objectClass, JSONObject objectData) {
 			this.objectData = objectData;
 			newInstance(objectClass);
 		}
 
 		public T get() throws ApiException {
 
 			String objectContext;
 			Uri contextUri = getObjectInstanceContext();
 
 			try {
 				// Check context
 				objectContext = objectData.getString(KEY_CONTEXT);
 			} catch (JSONException e) {
 				throw new ApiException("API error: " + contextUri.toString()
 						+ " object has no " + KEY_CONTEXT);
 			}
 
 			if (!contextUri.toString().equals(objectContext)) {
 				throw new ApiException("Unexpected context: " + objectContext
 						+ ". Expected: " + contextUri.toString());
 			}
 
 			if (objectInstance instanceof StateModel) {
 				// Check @id
 				try {
 					Uri objectUri = Uri.parse(objectData.getString(KEY_ID));
 					((StateModel) objectInstance).setUri(objectUri);
 				} catch (JSONException e) {
 					throw new ApiException("API error: "
 							+ contextUri.toString() + " object has no "
 							+ KEY_ID);
 				}
 			}
 
 			for (Method m : objectInstance.getClass().getMethods()) {
 				if (!Modifier.isPublic(m.getModifiers())) {
 					continue;
 				}
 
 				@SuppressWarnings("rawtypes")
 				Class[] params = m.getParameterTypes();
 				if (params.length != 1) {
 					continue;
 				}
 				@SuppressWarnings("rawtypes")
 				Class param = m.getParameterTypes()[0];
 				if (!m.getName().substring(0, 3).equals("set")) {
 					continue;
 
 				}
 				String key = m.getName().substring(3, 4).toLowerCase()
 						+ m.getName().substring(4);
 				if (key.equals("uri"))
 					continue;
 				try {
 					if (param == int.class || param.equals(Integer.class)) {
 						Integer value;
 						try {
 							value = objectData.getInt(key);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get int value for " + key);
 						}
 						try {
 							m.invoke(objectInstance, value);
 						} catch (IllegalArgumentException e) {
 							throw new ApiException(objectInstance.getClass()
 									.toString()
 									+ "#"
 									+ m.getName()
 									+ "(Integer) did not work.");
 						}
 					} else if (param == Uri.class) {
 						String uriVal;
 						try {
 							uriVal = objectData.isNull(key) ? null : objectData
 									.getString(key);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get Uri value for " + key);
 						}
 						if (uriVal != null) {
 							Uri value = Uri.parse(uriVal);
 							try {
 								m.invoke(objectInstance, value);
 							} catch (IllegalArgumentException e) {
 								throw new ApiException(objectInstance
 										.getClass().toString()
 										+ "#"
 										+ m.getName() + "(Uri) did not work.");
 							}
 						}
 
 					} else if (param == boolean.class) {
 						boolean value;
 						try {
 							value = objectData.getBoolean(key);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get boolean value for " + key);
 						}
 						try {
 							m.invoke(objectInstance, value);
 						} catch (IllegalArgumentException e) {
 							throw new ApiException(objectInstance.getClass()
 									.toString()
 									+ "#"
 									+ m.getName()
 									+ "(boolean) did not work.");
 						}
 					} else if (param == String.class) {
 						String value;
 						try {
 							value = objectData.getString(key);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get string value for " + key);
 						}
 						try {
 							m.invoke(objectInstance, value);
 						} catch (IllegalArgumentException e) {
 							throw new ApiException(objectInstance.getClass()
 									.toString()
 									+ "#"
 									+ m.getName()
 									+ "(String) did not work.");
 						}
 					} else if (param == Date.class) {
 						String value;
 						try {
 							value = objectData.getString(key);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get string value for " + key);
 						}
 						String y = value.substring(0, 4);
 						String mon = value.substring(5, 7);
 						String d = value.substring(8, 10);
 						String h = value.substring(11, 13);
 						String min = value.substring(14, 16);
 						String s = value.substring(17, 19);
 						GregorianCalendar commentDate = new GregorianCalendar(
 								Integer.parseInt(y), Integer.parseInt(mon) - 1,
 								Integer.parseInt(d), Integer.parseInt(h),
 								Integer.parseInt(min), Integer.parseInt(s));
 						try {
 							m.invoke(objectInstance, commentDate.getTime());
 						} catch (IllegalArgumentException e) {
 							throw new ApiException(objectInstance.getClass()
 									.toString()
 									+ "#"
 									+ m.getName()
 									+ "(Date) did not work.");
 						}
 					} else if (param.isInstance(StateModel.class)) {
 						if (!contextToModel.values().contains(param))
 							continue;
 						@SuppressWarnings("unchecked")
 						Class<StateModel> stateModelParam = param;
 						StateModel child;
 						try {
 							JSONObject childData = objectData
 									.getJSONObject(key);
 							child = new JSONReader<StateModel>(stateModelParam,
 									childData).get();
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to get object value for " + key);
 						}
 						try {
 							m.invoke(objectInstance, child);
 						} catch (IllegalArgumentException e) {
 							throw new ApiException(objectInstance.getClass()
 									.toString()
 									+ "#"
 									+ m.getName()
 									+ "(StateModel) did not work.");
 						}
 					} else {
 						// TODO: support all Types
 						Log.d(getClass().getCanonicalName(), "Skipped value "
 								+ key + " of type " + param.toString() + " on "
 								+ objectInstance.getClass().toString());
 					}
 				} catch (IllegalAccessException e) {
 					throw new ApiException("Oops. I thought "
 							+ objectInstance.getClass().toString() + "#"
 							+ m.getName() + " was public.");
 				} catch (InvocationTargetException e) {
 					throw new ApiException("Could not I invoke "
 							+ objectInstance.getClass().toString() + "#"
 							+ m.getName());
 				}
 			}
 
 			// Add relations
 			if (objectInstance instanceof StateModel) {
 				if (objectData.has(KEY_RELATIONS)) {
 					try {
 						List<Relation> instanceRelations = new ArrayList<Relation>();
 						JSONArray relations = objectData
 								.getJSONArray(KEY_RELATIONS);
 						for (int i = 0; i < relations.length(); i++) {
 							Relation rel = new JSONReader<Relation>(
 									Relation.class, relations.getJSONObject(i))
 									.get();
 							if (!contextToModel.containsKey(rel
 									.getRelatedcontext())) {
 								Log.d(getClass().getCanonicalName(),
 										"Skipped relation with unknown context "
 												+ rel.getRelatedcontext()
 														.toString());
 								continue;
 							}
 							rel.setModel(contextToModel.get(rel
 									.getRelatedcontext()));
 							instanceRelations.add(rel);
 						}
 						((StateModel) objectInstance)
 								.setRelations(instanceRelations);
 					} catch (JSONException e) {
 						throw new ApiException("Failed to read relations from "
 								+ contextUri.toString());
 					}
 				}
 			}
 
 			return objectInstance;
 		}
 
 		/**
 		 * Lädt das Objekt und alle Relationen
 		 * 
 		 * TODO: Sicherstellen dass nur abwärts zeigende Relationen verfolgt
 		 * werden, sonst lädt man sich so den ganzen Graphen
 		 * 
 		 * @return T
 		 * @throws ApiException
 		 */
 		@SuppressWarnings("unchecked")
 		public T getRecursive() throws ApiException {
 			T top = this.get();
 			if (top instanceof StateModel) {
 				for (Relation relation : ((StateModel) top).getRelations()) {
 					if (!contextToModel.containsKey(relation
 							.getRelatedcontext())) {
 						Log.d(getClass().getCanonicalName(),
 								"Skipped unknown context "
 										+ relation.getRelatedcontext()
 												.toString());
 						continue;
 					}
 					if (acceptedModels != null
 							&& !acceptedModels.contains(relation.getModel())) {
 						Log.d(getClass().getCanonicalName(),
 								"Context not accepted: "
 										+ relation.getRelatedcontext()
 												.toString() + " on "
 										+ top.getClass().getCanonicalName());
 						continue;
 					}
 					if (relation.isList()) {
 						HttpGet request = new HttpGet(relation.getHref()
 								.toString());
 						try {
 							JSONObject response = execute(request);
 							JSONArray items = response.getJSONArray(KEY_RESULT);
 							List<StateModel> instanceItems = new ArrayList<StateModel>();
 							for (int i = 0; i < items.length(); i++) {
 								JSONObject item = items.getJSONObject(i);
 								try {
 									Class<? extends Model> itemClass = contextToModel
 											.get(Uri.parse(item
 													.getString(KEY_CONTEXT)));
 									@SuppressWarnings("rawtypes")
 									JSONReader<StateModel> reader = new JSONReader(
 											itemClass, item);
 									reader.setRecursiveModels(acceptedModels);
 									StateModel itemInstance = (StateModel) reader
 											.getRecursive();
 									instanceItems.add(itemInstance);
 								} catch (JSONException e) {
 									throw new ApiException(
 											"No context in item for "
 													+ getObjectInstanceContext()
 															.toString());
 								}
 							}
 							((StateModel) top).setRelationItems(relation,
 									instanceItems);
 						} catch (JSONException e) {
 							throw new ApiException(
 									"Failed to read list relation "
 											+ relation.getHref().toString()
 											+ " from "
 											+ getObjectInstanceContext()
 													.toString());
 						}
 					} else {
 						// TODO: Implementieren
 					}
 				}
 			}
 			return top;
 		}
 
 		private Uri getObjectInstanceContext() {
 			return modelToContext.get(this.objectInstance.getClass());
 		}
 
 		public void setRecursiveModels(Class<? extends Model>... models) {
 			acceptedModels = new ArrayList<Class<? extends Model>>();
 			for (Class<? extends Model> m : models) {
 				acceptedModels.add(m);
 			}
 		}
 
 		public void setRecursiveModels(List<Class<? extends Model>> models) {
 			acceptedModels = models;
 		}
 	}
 
 	public MoodServerApi() {
 		client = new DefaultHttpClient();
 		registerModel(ApiStatus.class,
 				Uri.parse("http://groupmood.net/jsonld/apistatus"));
 		registerModel(Relation.class,
 				Uri.parse("http://groupmood.net/jsonld/relation"));
 	}
 
 	public Meeting getMeeting(Uri meetingUri) throws ApiException {
 		HttpGet request = new HttpGet(meetingUri.toString());
 		JSONObject response = execute(request);
 		Meeting meeting = new JSONReader<Meeting>(response, Meeting.class,
 				JSONReader.KEY_RESULT).get();
 		return meeting;
 	}
 
 	public Meeting getMeetingRecursive(Uri meetingUri) throws ApiException {
 		HttpGet request = new HttpGet(meetingUri.toString());
 		JSONObject response = execute(request);
 		Meeting meeting = new JSONReader<Meeting>(response, Meeting.class,
 				JSONReader.KEY_RESULT).getRecursive();
 		return meeting;
 	}
 
 	public Topic getTopic(Uri topicUri) throws ApiException {
 		HttpGet request = new HttpGet(topicUri.toString());
 		JSONObject response = execute(request);
 		Topic topic = new JSONReader<Topic>(response, Topic.class,
 				JSONReader.KEY_RESULT).get();
 		return topic;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Question getQuestion(Uri questionUri) throws ApiException {
 		HttpGet request = new HttpGet(questionUri.toString());
 		JSONObject response = execute(request);
 		JSONReader<Question> reader = new JSONReader<Question>(response,
 				Question.class, JSONReader.KEY_RESULT);
 		reader.setRecursiveModels(QuestionOption.class);
 		Question question = reader.getRecursive();
 		return question;
 	}
 
 	private JSONObject execute(HttpUriRequest request) throws ApiException {
 		HttpResponse response;
 		request.setHeader("Accept", "application/json");
 		String dataAsString;
 		try {
 			response = client.execute(request);
 
 			HttpEntity entity = response.getEntity();
 			InputStream inputStream = entity.getContent();
 			ByteArrayOutputStream content = new ByteArrayOutputStream();
 			int readBytes = 0;
 			byte[] sBuffer = new byte[512];
 			while ((readBytes = inputStream.read(sBuffer)) != -1) {
 				content.write(sBuffer, 0, readBytes);
 			}
 			dataAsString = new String(content.toByteArray());
 
 			StatusLine status = response.getStatusLine();
 			switch (status.getStatusCode()) {
 			case 200: // OK
 			case 201: // Created
 				break;
 			default:
 				Log.e(getClass().getCanonicalName(),
 						"Request failed: " + request.getMethod() + " "
 								+ request.getURI().toString() + ": "
 								+ dataAsString.replaceAll("<[^>]+>", ""));
 				throw new ApiException("Invalid response from server: "
 						+ status.toString());
 			}
 		} catch (ClientProtocolException e) {
 			throw new ApiException("Protocol error: " + e.toString());
 		} catch (IOException e) {
 			throw new ApiException("I/O error: " + e.toString());
 		}
 		Log.d(getClass().getCanonicalName(), dataAsString);
 		JSONObject jsonResponse;
 		try {
 			jsonResponse = (JSONObject) new JSONTokener(dataAsString)
 					.nextValue();
 		} catch (JSONException e) {
 			throw new ApiException("Failed to parse JSON response: "
 					+ dataAsString);
 		}
 		JSONReader<ApiStatus> jsonResponseReader = new JSONReader<ApiStatus>(
 				jsonResponse, ApiStatus.class, JSONReader.KEY_STATUS);
 		ApiStatus s = jsonResponseReader.get();
 		if (!s.getMessage().equals(ApiStatus.STATUS_OK)) {
 			throw new ApiException("API error: " + s.getMessage() + "("
 					+ s.getCode() + ")");
 		}
 		return jsonResponse;
 	}
 
 	public void registerModel(Class<? extends Model> modelClass, Uri context) {
 		contextToModel.put(context, modelClass);
 		modelToContext.put(modelClass, context);
 	}
 
 	/**
 	 * Lädt die Topics eines Meetings
 	 * 
 	 * @param meeting
 	 * @return
 	 * @throws ApiException
 	 */
 	public ArrayList<Topic> getTopics(Meeting meeting) throws ApiException {
 		Relation topicRelation = getRelated(meeting, Topic.class);
 		HttpGet request = new HttpGet(topicRelation.getHref().toString());
 		JSONObject response = execute(request);
 
 		ArrayList<Topic> topics = new ArrayList<Topic>();
 
 		try {
 			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
 			for (int i = 0; i < items.length(); i++) {
 				Topic topic = new JSONReader<Topic>(Topic.class,
 						items.getJSONObject(i)).get();
 				topics.add(topic);
 			}
 		} catch (JSONException e) {
 			throw new ApiException("Failed to read topics from "
 					+ topicRelation.getHref().toString());
 		}
 		return topics;
 	}
 
 	public ArrayList<Comment> getComments(Topic topic) throws ApiException {
 		Relation commentRelation = getRelated(topic, Comment.class);
 		HttpGet request = new HttpGet(commentRelation.getHref().toString());
 		JSONObject response = execute(request);
 
 		ArrayList<Comment> comments = new ArrayList<Comment>();
 
 		try {
 			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
 			for (int i = 0; i < items.length(); i++) {
 				Comment comment = new JSONReader<Comment>(Comment.class,
 						items.getJSONObject(i)).get();
 				comments.add(comment);
 			}
 		} catch (JSONException e) {
 			throw new ApiException("Failed to read comments from "
 					+ commentRelation.getHref().toString());
 		}
 		return comments;
 	}
 
 	public Comment addComment(Topic topic, String comment) throws ApiException {
 		Relation commentRelation = getRelated(topic, Comment.class);
 		HttpPost request = new HttpPost(commentRelation.getHref().toString());
 		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
 		params.add(new BasicNameValuePair("comment", comment));
 		try {
			request.setEntity(new UrlEncodedFormEntity(params));
 		} catch (UnsupportedEncodingException e) {
 			throw new ApiException(e.getMessage());
 		}
 		JSONObject response = execute(request);
 		Comment c = new JSONReader<Comment>(response, Comment.class,
 				JSONReader.KEY_RESULT).get();
 		return c;
 	}
 
 	public Answer addAnswer(Question question, String answer)
 			throws ApiException {
 
 		Relation answerRelation = getRelated(question, Answer.class);
 		HttpPost request = new HttpPost(answerRelation.getHref().toString());
 		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
 		params.add(new BasicNameValuePair("answer", answer));
 		try {
			request.setEntity(new UrlEncodedFormEntity(params));
 		} catch (UnsupportedEncodingException e) {
 			throw new ApiException(e.getMessage());
 		}
 		JSONObject response = execute(request);
 		Answer a = new JSONReader<Answer>(response, Answer.class,
 				JSONReader.KEY_RESULT).get();
 		return a;
 
 	}
 
 	public ArrayList<Answer> addAnswers(Question question, String[] answerValues)
 			throws ApiException {
 
 		// Antworten senden
 		Relation answerRelation = getRelated(question, Answer.class);
 		HttpPost request = new HttpPost(answerRelation.getHref().toString());
 		List<NameValuePair> params = new ArrayList<NameValuePair>(
 				answerValues.length);
 		for (String answerValue : answerValues) {
 			params.add(new BasicNameValuePair("answer[]", answerValue));
 		}
 		try {
			request.setEntity(new UrlEncodedFormEntity(params));
 		} catch (UnsupportedEncodingException e) {
 			throw new ApiException(e.getMessage());
 		}
 		JSONObject response = execute(request);
 
 		// Liste mit erstellen Antworten laden
 		ArrayList<Answer> answers = new ArrayList<Answer>();
 		try {
 			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
 			for (int i = 0; i < items.length(); i++) {
 				Answer answer = new JSONReader<Answer>(Answer.class,
 						items.getJSONObject(i)).getRecursive();
 				answers.add(answer);
 			}
 		} catch (JSONException e) {
 			throw new ApiException("Failed to read answers from "
 					+ answerRelation.getHref().toString());
 		}
 
 		return answers;
 	}
 
 	public ArrayList<Question> getQuestionsWithExtras(Topic topic)
 			throws ApiException {
 		Relation questionRelation = getRelated(topic, Question.class);
 		HttpGet request = new HttpGet(questionRelation.getHref().toString());
 		JSONObject response = execute(request);
 
 		ArrayList<Question> questions = new ArrayList<Question>();
 
 		try {
 			JSONArray items = response.getJSONArray(JSONReader.KEY_RESULT);
 			for (int i = 0; i < items.length(); i++) {
 				Question question = new JSONReader<Question>(Question.class,
 						items.getJSONObject(i)).getRecursive();
 				questions.add(question);
 			}
 		} catch (JSONException e) {
 			throw new ApiException("Failed to read topics from "
 					+ questionRelation.getHref().toString());
 		}
 		return questions;
 	}
 
 	/**
 	 * Sucht auf einem Model die Beziehung zur Klasse relatedClass
 	 * 
 	 * @param relatedClass
 	 * @return
 	 * @throws Exception
 	 */
 	private Relation getRelated(StateModel obj,
 			Class<? extends Model> relatedClass) throws ApiException {
 		Relation relation = null;
 		for (Relation rel : obj.getRelations()) {
 			if (rel.getModel().equals(relatedClass))
 				relation = rel;
 		}
 		if (relation == null)
 			throw new ApiException(obj.getClass().getCanonicalName()
 					+ " has no related " + relatedClass.getCanonicalName());
 		return relation;
 	}
 }
