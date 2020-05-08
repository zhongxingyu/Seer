 package com.a2devel.words.dao;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 
 import mt.rcasha.dict.client.DefinitionResponse;
 import mt.rcasha.dict.client.DictClient;
 import mt.rcasha.dict.client.DictError;
 import mt.rcasha.dict.client.DictException;
 import mt.rcasha.dict.client.Status;
 import mt.rcasha.dict.client.StatusException;
 
 import com.a2devel.words.to.Word;
 
 /**
  * {@link DictClient} utility wrapper to manage attempts, resolve random requests and exceptions.
  * @author fernanda
  * @see DictClient
  */
 public class Dictionary {
 	
 	/**
 	 * Maximum attempts to get a random word
 	 */
 	private static final int ATTEMPTS_NOT_MATCH = 3;
 	private static Random random = new Random();
 	/**
 	 * Dictionary database
 	 */
 	private String database;
 	/**
 	 * Match strategy to resolve dictionary requests 
 	 */
 	private String strategy;
 	/**
 	 * Dictionary access 
 	 */
 	private DictClient dictClient;
 	/**
 	 * Project properties file access
 	 */
 	private Properties properties;
 
 	public Dictionary() throws IOException, DictException{
 		properties = new Properties();
 		properties.load(Dictionary.class.getResourceAsStream("/dictionary.properties"));
 		strategy = properties.getProperty("dictionary.match_strategy");
 		dictClient = new DictClient(properties.getProperty("dictionary.host"));
 	}
 	
 	/**
 	 * Get a random word/translation for a given database and wrapped in a {@link Word} entity.
 	 * @return {@link Word} entity or null if no word was found after {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts
 	 * or the database is incorrect
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	public Word getWord(String database) throws DictException, IOException{
 		try {
 			dictClient.check();
 		} catch (DictError e) {
 			dictClient.init();
 		}
 		setDatabase(database);
 		Word word = getWord(0);
 		
 		if(word != null && database.contains("-")){
 			String[] languages = database.split("-");
 			if(languages.length == 2){
 				word.setWordLanguage(languages[0]);
 				word.setTranslationLanguage(languages[1]);
 			}
 		}
 		
 		return word;
 	}
 	
 	/**
 	 * Get a random word/translation wrapped in a {@link Word} entity. 
 	 * @param currentAttempt Current attempt to control the {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts allowed.
 	 * @return {@link Word} entity or null if no word was found after {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	protected Word getWord(int currentAttempt) throws DictException, IOException{
 		Word wordWrapper = null;
 		String word = this.getRandomWord();
 		String translation = this.getTranslation(word);
 		if(translation == null){
 			if(currentAttempt == Dictionary.ATTEMPTS_NOT_MATCH){
 				return null;
 			}else{
 				getWord(++currentAttempt);
 			}
 		}else{
 			 wordWrapper = new Word();
 			 wordWrapper.setWord(word);
 			 wordWrapper.setTranslation(translation);
 		}
 		return wordWrapper;
 	}
 
 	/**
 	 * Get a random word from the internal {@link DictClient} and database.
 	 * @return The word or null if no word was found after {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	private String getRandomWord() throws DictException, IOException{
 		return getRandomWord(0);
 	}
 	
 	/**
 	 * Get a random word from the internal {@link DictClient} and database.
 	 * @param currentAttempt Current attempt to control the {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts allowed.
 	 * @return The word or null if no word was found after {@link Dictionary#ATTEMPTS_NOT_MATCH} attempts
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	protected String getRandomWord(int currentAttempt) throws DictException, IOException{
 		Map<String,List<String>> matches = null;
 		try {
 			String letter = String.valueOf((char)(random.nextInt(26) + 'a'));
 			matches = dictClient.getMatches(getDatabase(), getStrategy(), letter);
 		} catch (StatusException e) {
 			if(e.getStatus() == Status.ERR_NO_MATCH && 
 					currentAttempt < Dictionary.ATTEMPTS_NOT_MATCH){
 				return getRandomWord(++currentAttempt);
 			}else{
 				throw e;
 			}
		} catch (Exception e){
 			if(currentAttempt < Dictionary.ATTEMPTS_NOT_MATCH){
 				dictClient.finalize();
 				dictClient.init();
 				return getRandomWord(++currentAttempt);
 			}else{
				throw new IOException(e.getMessage());
 			}
 		}
 		if(matches.containsKey(getDatabase())){
 			List<String> results = matches.get(getDatabase());
 			int item = random.nextInt(results.size());
 			return results.get(item);
 		}
 		return null;
 	}
 	
 	/**
 	 * Resolve the translation of the given word from the internal {@link DictClient} and database.
 	 * @param word The word to translate
 	 * @return The translation or null if no translation is found
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	private String getTranslation(String word) throws IOException, DictException{
 		String translation = new String();
 		List<DefinitionResponse> definitions = null;
 		
 		try {
 			definitions = dictClient.getDefinitions(getDatabase(), word);
 		} catch (StatusException e) {
 			if(e.getStatus() == Status.ERR_NO_MATCH){
 				return null;
 			}
 		}
 		
 		for (DefinitionResponse response : definitions) {
 			if(getDatabase().equals(response.getDatabase())){
 				List<String> lines = response.getLines();
 				for (int i = 1; i < lines.size(); i++) {
 					if(i > 1){
 						translation += "\n";
 					}
 					translation += lines.get(i);
 				}
 			}
 		}
 		
 		if(translation.length() == 0){
 			return null;
 		}
 		
 		return translation;
 	}
 	
 	/**
 	 * Stop the {@link DictClient} connection
 	 * @throws DictException
 	 * @throws IOException
 	 */
 	public void stop() throws DictException, IOException{
 		this.dictClient.finalize();
 	}
 	
 	public String getDatabase() {
 		return database;
 	}
 
 	public void setDatabase(String database) {
 		this.database = database;
 	}
 
 	public String getStrategy() {
 		return strategy;
 	}
 
 	public void setStrategy(String strategy) {
 		this.strategy = strategy;
 	}
 
 }
