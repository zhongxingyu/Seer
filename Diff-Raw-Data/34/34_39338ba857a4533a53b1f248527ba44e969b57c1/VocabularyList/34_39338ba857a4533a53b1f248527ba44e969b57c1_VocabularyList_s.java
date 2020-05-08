 package com.tuvocabulario.vocabuloid;
 
 import java.util.Hashtable;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import android.content.Context;
 
 /**
  * Provides access to tuvocabulario.com VocabularyList model. 
  *
  * @author Ulf Mhring
  * @version 0.2
  */
 public class VocabularyList extends RestClient {
 	
 	/** Maps to id */
 	private int id;
 	/** Maps to name */
 	private String name;
 	/** Maps to type */
 	private String type;
 	/** Maps to source language (id) */
 	private int languageFromId;
 	/** Maps to source language (name) */
 	private String languageFromName;
 	/** Maps to target language (id) */
 	private int languageToId;
 	/** Maps to target language (name) */
 	private String languageToName;
 	/** Number of vocabularies contained in list */
 	private int size;
 	/** Collection of contained vocabularies */	
 	private Vocabulary[] vocabularies;
 	/** Collection of supported tenses */	
 	private Hashtable<Integer,String> tenses;
 	/** Currently selected tense */	
 	private int selectedTense;
 	
 	/** 
      * Initializes RESTful connection
      *
      * @param ctx Context needed for assessing shared resources
      */
 	public VocabularyList(Context context) {
 		super(context);
 	}
 	
 	/** 
      * Returns a specific {@link Vocabulary Vocabulary} from {@link vocabularies vocabularies} Array
      *
      * @param position Position of VocabularyList in Array
      */
 	public Vocabulary getVocabulary(int position) {
 		if (vocabularies == null) { getRemoteData(); }
 		return vocabularies[position];
 	}
 	
 	/** 
      * Returns contained vocabularies as {@link Vocabulary Vocabulary} Array
      */
 	public Vocabulary[] getVocabularies() {
 		if (vocabularies == null) { getRemoteData(); }
 		return vocabularies;
 	}
 	
 	/** 
      * Returns a String Array with the words of all contained vocabularies
      */
 	public String[] getVocabulariesAsStrings() {
 		if (vocabularies == null) { getRemoteData(); }
 		String[] values = new String[vocabularies.length];
 		for (int i=0; i<vocabularies.length; i++) {
 			values[i] = vocabularies[i].getWord();
 		}
 		return values;
 	}
 	
 	/** 
      * Fetches new vocabularies from server
      */
 	protected void getRemoteData() {
 		try {
 			JSONObject response = getObject("/lists/" + id + ".json");
 			JSONObject root = response.getJSONObject(response.names().getString(0));
 			name = root.getString("name");
 			size = root.getInt("size");
 			languageFromId = root.getJSONObject("language_from").getInt("id");
 			languageFromName = root.getJSONObject("language_from").getString("word");
 			if (root.has("language_to")) { languageToId = root.getJSONObject("language_to").getInt("id"); languageToName = root.getJSONObject("language_to").getString("word"); };
 			setType(response.names().getString(0));
 			JSONArray result = root.getJSONArray("vocabularies");
 			vocabularies = new Vocabulary[result.length()];
 			for (int i=0;i<result.length();i++) {
 				JSONObject remote_vocabulary = result.getJSONObject(i);
 				Vocabulary vocabulary = new Vocabulary(mContext);
				vocabulary.initialize(remote_vocabulary.getInt("id"), "Vocabulary", remote_vocabulary.getString("word"), remote_vocabulary.getString("gender"), root.getJSONObject("language_from").getInt("id"), root.getJSONObject("language_from").getString("word"));
 				vocabularies[i] = vocabulary;
             }
 		}
 		catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/** 
      * Determines whether list is empty
      */
 	public Boolean empty() {
 		return vocabularies == null || vocabularies.length == 0;
 	}
 	
 	/** 
      * Determines whether list is a VerbList
      */
 	public Boolean isVerbList() {
 		return type == "VerbList";
 	}
 	
 	/** Returns vocabulary list id */
 	public int getId() { return id; }
 	
 	/** Set new vocabulary list id */
 	public void setId(int id) { this.id = id; }
 	
 	/** Sets type of vocabulary list (verb or vocabulary list) */
 	public void setType(String type) {
 		if (type.endsWith("_verb_list")) {
 			this.type = "VerbList";
 			if (tenses == null) { setTenses(); }
 		}
 		else {
 			this.type = "VocabularyList";
 			tenses = null;
 		}
 	}
 	
 	/** Returns headline for 1st flash card */
 	public String getFrom() {
 		if (isVerbList()) { return mContext.getString(R.string.message_tenses_infinitive); }
 		return languageFromName;
 	}
 	
 	/** Returns headline for 2nd flash card */
 	public String getTo() {
 		if (isVerbList()) { return getSelectedTenseName(); }
 		return languageToName;
 	}
 	
 	/** Returns selector for translations or conjugation (wrapper method) */
 	public int getSelector() {
 		if (isVerbList()) { return getSelectedTenseId(); }
 		return languageToId;
 	}
 	
 	/** Returns vocabulary list name */
 	public String getName() { return name; }
 
 	/** Set new vocabulary list id */
 	public void setName(String name) { this.name = name; }
 	
 	/** Returns vocabulary list language (id) */
 	public int getLanguageFromId() { return languageFromId; }
 	
 	/** Sets new vocabulary list language (id) */
 	public void setLanguageFromId(int languageFromId) { this.languageFromId = languageFromId; }
 	
 	/** Sets new vocabulary list language (name) -> only applies to vocabulary lists */
 	public void setLanguageFromName(String languageFromName) { this.languageFromName = languageFromName; }
 	
 	/** Returns size of vocabulary list */
 	public int getSize() { return size; }
 	
 	/** Sets size of vocabulary list */
 	public void setSize(int size) { this.size = size; }
 	
 	/** Returns tenses supported -> applies to verb lists only */
 	public Hashtable<Integer,String> getTenses() { return tenses; } 
 	
 	/** Sets supported tenses -> applies to verb lists only */
 	public void setTenses() {
 		JSONArray result = getCollection("/tenses.json?language_id=" + languageFromId);
 		tenses = new Hashtable<Integer,String>();
 		try {
 			for (int i=0;i<result.length();i++) {
 				JSONObject remotetense = result.getJSONObject(i).getJSONObject("conjugation_time");
 				tenses.put(remotetense.getInt("id"), remotetense.getString("name"));
 	        }
 		}
 		catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/** Returns selected tense name -> applies to verb lists only */
 	public String getSelectedTenseName() { return tenses.get(selectedTense); }
 	
 	/** Returns selected tense id -> applies to verb lists only */
 	public int getSelectedTenseId() { return selectedTense; }
 	
 	/** Sets selected tense -> applies to verb lists only */
 	public void setSelectedTense(int tenseId) { this.selectedTense = tenseId; }
 }
