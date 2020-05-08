 package org.sortdc.sortdc;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.dao.Word;
 import org.sortdc.sortdc.database.Database;
 
 public class Classifier {
 
     private Tokenization tokenization;
     private Database database;
     private Map<String, Category> categories;
 
     /**
      * Instance of Classifier
      *
      * @param tokenization Instance of Tokenization
      */
     public Classifier(Tokenization tokenization, Database database) throws Exception {
         this.tokenization = tokenization;
         this.database = database;
         this.loadCategories();
     }
 
     /**
      * Assigns a new text to an existing category
      *
      * @param text Text
      * @param category_id Category's id
      * @throws Exception
      */
     public void train(String name, String text, String category_name) throws Exception {
         if (!this.categories.containsKey(category_name)) {
             this.addCategory(category_name);
         }
         List<String> words = tokenization.extract(text);
         Map<String, Integer> occurrences = this.tokenization.getOccurrences(words);
 
         Document doc;
         try {
             doc = this.database.findDocumentByName(name);
         } catch (Exception e) {
             doc = new Document();
         }
         doc.setName(name);
         doc.setCategoryId(this.categories.get(category_name).getId());
         doc.setWordsOccurrences(occurrences);
 
         this.database.saveDocument(doc);
     }
 
     /**
      * For each category, determines the probability that the given text belongs to it
      *
      * @param text Text
      * @return Probabilities by category
      * @throws Exception
      */
     public Map<String, Float> categorize(String text) throws Exception {
         List<String> tokens = tokenization.extract(text);
         Set<String> words_set = new HashSet<String>(tokens);
         Map<String, Integer> doc_words_occurrences = this.tokenization.getOccurrences(tokens);
         List<Word> words = this.database.findWordsByNames(words_set);
         Map<String, Float> categories_prob = new HashMap<String, Float>();
 
         int nb_words = 0;
         for (Word word : words) {
             if (doc_words_occurrences.containsKey(word.getName())) {
                 nb_words += doc_words_occurrences.get(word.getName());
             }
         }
 
         for (Category category : this.categories.values()) {
             Float category_prob = 0f;
             for (Word word : words) {
                 if (!doc_words_occurrences.containsKey(word.getName()) || !word.getOccurrencesByCategory().containsKey(category.getId())) {
                     continue;
                 }
                 category_prob +=
                         new Float(doc_words_occurrences.get(word.getName())
                         * word.getOccurrencesByCategory().get(category.getId()))
                         / new Float(word.getOccurrences());
             }
            if (nb_words != 0) {
                category_prob /= nb_words;
            }
             categories_prob.put(category.getName(), category_prob);
         }
         return categories_prob;
     }
 
     /**
      * Creates a category
      *
      * @param name Category's unique name
      * @throws Exception
      */
     public void addCategory(String name) throws Exception {
         Category category = new Category();
         category.setName(name);
         this.database.saveCategory(category);
         this.categories.put(name, category);
     }
 
     /**
      * Loads all categories and stores it in this.categories
      * 
      * @throws Exception
      */
     private synchronized void loadCategories() throws Exception {
         this.categories = new HashMap<String, Category>();
         List<Category> categories_list = this.database.findAllCategories();
         for (Category category : categories_list) {
             this.categories.put(category.getName(), category);
         }
     }
 }
