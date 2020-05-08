 package com.ciphertool.sentencebuilder.util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.ciphertool.sentencebuilder.dao.WordDao;
 import com.ciphertool.sentencebuilder.entities.Word;
 import com.ciphertool.sentencebuilder.entities.WordId;
 
 public class WordListImporter {
 	private String fileName;
 	private WordDao wordDao;
 	
 	private static Logger log = Logger.getLogger(WordListImporter.class);
 	private static BeanFactory factory;
 	
 	private static void setUp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("beans-sentence.xml");
 		factory = context;
 		log.info("Spring context created successfully!");
 	}
 
 	public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException {
 		setUp();
 		WordListImporter wordListImporter = (WordListImporter) factory.getBean("wordListImporter");
 		wordListImporter.importWordList();
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 * @throws SQLException 
 	 */
 	private void importWordList() throws IOException, SQLException, ClassNotFoundException {
 		BufferedReader input = null;
 		try {
 			input =  new BufferedReader(new FileReader(fileName));
 		} catch (FileNotFoundException e) {
 			log.error("File: " + fileName + " not found.");
 			e.printStackTrace();
 		}
 		String [] line = null;
 		String word = null;
 		char [] partOfSpeech = null;
 		String nextWord = input.readLine();
 		int rowCount = 0;
 		
         log.info("Starting import...");
 		long start = System.currentTimeMillis();
 		while (nextWord!=null) {
 			line = nextWord.split("\t");
 			word = line[0];
 			//the pipe character is just informational in the word list
 			partOfSpeech = line[1].toCharArray();
 			for (int i = 0; i < partOfSpeech.length; i++) {
 				if (partOfSpeech[i]!='|')
 					rowCount += wordDao.insert(new Word(new WordId(word, partOfSpeech[i]), 1)) ? 1 : 0;
 			}
 			nextWord = input.readLine();
 		}
 		long end = System.currentTimeMillis();
 		
 		log.info("Rows inserted: " + rowCount);
 		log.info("Time elapsed: " + (end-start) + "ms");
 	}
 
 	@Required
 	public void setFileName(String file) {
 		fileName = file;
 	}
 
 	@Autowired
 	public void setWordDao(WordDao wordDao) {
 		this.wordDao = wordDao;
 	}
 }
