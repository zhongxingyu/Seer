 package com.ciphertool.sentencebuilder.util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.ciphertool.sentencebuilder.dao.WordDao;
 import com.ciphertool.sentencebuilder.entities.Word;
 
 public class FrequencyListImporter {
 	private String fileName;
 	private WordDao wordDao;
 	
 	private static Logger log = Logger.getLogger(FrequencyListImporter.class);
 	private static BeanFactory factory;
 	
 	private static void setUp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("beans-sentence.xml");
 		factory = context;
 		log.info("Spring context created successfully!");
 	}
 
 	public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException {
 		setUp();
 		FrequencyListImporter frequencyListImporter = (FrequencyListImporter) factory.getBean("frequencyListImporter");
 		frequencyListImporter.importFrequencyList();
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 * @throws SQLException 
 	 */
 	private void importFrequencyList() throws IOException, SQLException, ClassNotFoundException {
 		BufferedReader input = null;
 		try {
 			input =  new BufferedReader(new FileReader(fileName));
 		} catch (FileNotFoundException e) {
 			log.error("File: " + fileName + " not found.");
 			e.printStackTrace();
 		}
 		String [] line = null;
 		String word = null;
 		long frequency = 1;
 		String nextWord = input.readLine();
 		
 		//skip to the second record because the first record contains columns names
 		nextWord = input.readLine();
 		
 		int rowCount = 0;
 		
         log.info("Starting import...");
 		long start = System.currentTimeMillis();
 		List<Word> words = new ArrayList<Word>();
 		while (nextWord!=null) {
 			line = nextWord.split("\t");
 			word = line[0];
 			frequency = Long.parseLong(line[1]);
 			words = wordDao.findByWordString(word);
 			
 			//If word is not found, log at info level
 			if (words == null || words.size() == 0)
 				log.info("No frequency matches found in part_of_speech table for word: " + word);
 			
 			//Loop over the list and update each word with the frequency
 			for (Word w : words) {
 				//Don't update if the frequency weight from file is the same as what's already in the database.  It's pointless.
 				if (w.getFrequencyWeight() != frequency) {
 					w.setFrequencyWeight(frequency);
 					rowCount += wordDao.update(w) ? 1 : 0;
 				}
 			}
 			
 			nextWord = input.readLine();
 		}
 		long end = System.currentTimeMillis();
 		
 		log.info("Rows updated: " + rowCount);
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
