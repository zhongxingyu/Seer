 package com.tuvocabulario.vocabuloid;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import android.content.Context;
 
 /**
  * Provides access to tuvocabulario.com Vocabulary model. 
  *
  * @author Ulf Mhring
 * @version 0.3
  */
 public class Vocabulary extends RestClient {
 	
 	/** Maps to id */
 	private int id;
 	/** Maps to word */
 	private String word;
 	/** Maps to type */
 	private String type;
 	/** Maps to language (field id) */
 	private int languageId;
 	/** Maps to language (field word) */
 	private String languageName;
 	/** Collection of contained vocabularies */
 	private Vocabulary[] translations;
 	/** Collection of conjugations */
 	private String[] conjugation;
 	
 	/** 
      * Initializes RESTful connection
      *
      * @param ctx Context needed for assessing shared resources
      */
 	public Vocabulary(Context context) {
 		super(context);
 	}
 	
 	/** 
      *  Returns a formatted String with either translations or conjugation set (wrapper method)
      *
      * @param selectorId ID of language the translations should be in or tense for conjugations
      * @param verblist Determines if verb list (true) or vocabulary list (false)
      */
 	public String getResultAsFormattedString(int selectorId, Boolean verblist) {
 		if (verblist) {
 			return getConjugationsAsFormattedString(selectorId);
 		}
 		return getTranslationsAsFormattedString(selectorId);
 	}
 	
 	/** 
      *  Returns a formatted String with all translations (as words)
      *
      * @param languageId ID of language the translations should be in
      */
 	public String getConjugationsAsFormattedString(int tenseId) {
 		if (conjugation == null) { getRemoteConjugation(tenseId); }
 		if (conjugation != null && conjugation.length > 0) {
 			String result = "";
 			for (String word : conjugation) {
 				result += word + "\n";
 			}
 			return result.substring(0,result.length()-1);
 		}
 		else {
 			return mContext.getString(R.string.message_no_conjugations);
 		}		
 	}
 	
 	/** 
      *  Returns a formatted String with all translations (as words)
      *
      * @param languageId ID of language the translations should be in
      */
 	public String getTranslationsAsFormattedString(int languageId) {
 		if (translations == null) { getRemoteTranslations(languageId); }
 		if (translations != null && translations.length > 0) {
 			String result = "";
 			for (Vocabulary vocabulary : translations) {
 				result += vocabulary.getWord() + "\n";
 			}
 			return result.substring(0,result.length()-1);
 		}
 		else {
 			return mContext.getString(R.string.message_no_translations);
 		}		
 	}
 	
 	/** 
      *  Fetches conjugation from the server
      *
      * @param tenseId ID of tense the conjugation should be in
      */
 	protected void getRemoteConjugation(int tenseId) {
 		try {
 			JSONArray result = getCollection("/vocabularies/" + id + "/conjugate.json?tense_id=" + tenseId);
 			conjugation = new String[result.length()];
 			for (int i=0;i<result.length();i++) {
 				JSONObject remote_conjugation = result.getJSONObject(i);
 				conjugation[i] = remote_conjugation.getString("person").split("/")[0] + " - " + remote_conjugation.getString("verb");
 			}
 		}
 		catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/** 
      *  Fetches new translations from the server
      *
      * @param languageId ID of language the translations should be in
      */
 	protected void getRemoteTranslations(int languageId) {
 		try {
 			JSONArray result = getCollection("/vocabularies/" + id + "/translate.json?language_id=" + languageId);
 			translations = new Vocabulary[result.length()];
 			for (int i=0;i<result.length();i++) {
 				JSONObject remote_vocabulary = result.getJSONObject(i);
 				String root = remote_vocabulary.names().getString(0);
 				Vocabulary vocabulary = new Vocabulary(mContext);
				vocabulary.initialize(remote_vocabulary.getJSONObject(root).getInt("id"), type, remote_vocabulary.getJSONObject(root).getString("word"), remote_vocabulary.getJSONObject(root).getJSONObject("language").getInt("id"), remote_vocabulary.getJSONObject(root).getJSONObject("language").getString("word"));
 				translations[i] = vocabulary;
 			}
 		}
 		catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/** 
      *  Initializes a new vocabulary based on remote data
      *
      * @param id id Field
      * @param type type Field
      * @param word word Field
      * @param languageId language_id Field
      * @param languageName language_name Field
      */
	public void initialize(int id, String type, String word, int languageId, String languageName) {
 		this.id = id;
 		this.type = type;
 		this.word = word;
 		this.languageName = languageName;
 		this.languageId = languageId;
 	}
 
 	public int getId() { return id; }
 	
	public String getWord() { return word; }
 	
 	public String getType() { return type; }
 	
 	public int getLanguageId() { return languageId; }
 	
 	public String getLanguageName() { return languageName; }
 }
