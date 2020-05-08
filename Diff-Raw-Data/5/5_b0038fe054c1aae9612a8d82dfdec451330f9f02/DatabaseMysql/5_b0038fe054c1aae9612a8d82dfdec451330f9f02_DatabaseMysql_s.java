 package org.sortdc.sortdc.database;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.dao.Token;
 
 public class DatabaseMysql extends Database {
 
     private Connection connection;
 
     public DatabaseMysql() {
         this.setHost("localhost");
         this.setPort(3306);
     }
 
     /**
      * Establishes connection with database
      *
      * @throws Exception
      */
     public void connect() throws Exception {
         if (this.db_name == null) {
             throw new Exception("Dbname not set");
         }
 
         this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.db_name, this.username, this.password);
         this.init();
     }
 
     /**
      * Initializes the database : creates tables if they don't exist
      * 
      * @throws Exception
      */
     private void init() throws Exception {
         FileReader input = new FileReader("config/mysql_structure.sql");
         BufferedReader br = new BufferedReader(input);
         String line, sql = "";
         while ((line = br.readLine()) != null) {
             sql += line;
         }
         Statement statement = this.connection.createStatement();
         String[] queries = sql.split(";");
         for (String query : queries) {
             if (!query.trim().equals("")) {
                 try {
                     statement.executeUpdate(query);
                 } catch (Exception e) {
                 }
             }
         }
     }
 
     /**
      * Finds all registered categories
      *
      * @return Categories list
      * @throws Exception
      */
     public List<Category> findAllCategories() throws Exception {
         List<Category> categories = new ArrayList<Category>();
         PreparedStatement statement = this.connection.prepareStatement("SELECT id FROM categories");
         ResultSet data = statement.executeQuery();
         while (data.next()) {
             Category category = new Category();
             category.setId(data.getString("id"));
             categories.add(category);
         }
         return categories;
     }
 
     /**
      * Saves or updates a category
      *
      * @param category
      * @throws Exception
      */
     public synchronized void saveCategory(Category category) throws Exception {
         if (category.getId() == null) {
             throw new Exception("You must set a category's id");
         }
         PreparedStatement statement = this.connection.prepareStatement("REPLACE INTO categories (id) VALUES (?)");
         statement.setString(1, category.getId());
         statement.executeUpdate();
     }
 
     /**
      * Deletes a category given its id
      * 
      * @param category_id
      * @throws Exception
      */
     public void deleteCategoryById(String category_id) throws Exception {
         PreparedStatement statement = this.connection.prepareStatement("DELETE FROM categories WHERE id = ?");
         statement.setString(1, category_id);
         statement.executeUpdate();
     }
 
     /**
      * Finds a document given its id
      *
      * @param id document id
      * @return document matching id
      * @throws Exception
      */
     public Document findDocumentById(String document_id) throws Exception {
         Document document = new Document();
         PreparedStatement statement;
         ResultSet data;
 
         statement = this.connection.prepareStatement("SELECT id, category_id FROM documents WHERE id = ? LIMIT 1");
         statement.setString(1, document_id);
         data = statement.executeQuery();
         if (data.next()) {
             document.setId(data.getString("id"));
             document.setCategoryId(data.getString("category_id"));
 
             Map<String, Integer> tokens = new HashMap<String, Integer>();
             statement = this.connection.prepareStatement(
                     "SELECT w.name, dw.occurrences "
                     + "FROM documents_tokens dw "
                     + "INNER JOIN tokens w ON w.id = dw.token_id "
                     + "WHERE dw.document_id = ?");
             statement.setString(1, document_id);
             data = statement.executeQuery();
             while (data.next()) {
                 tokens.put(data.getString("name"), data.getInt("occurrences"));
             }
             document.setTokensOccurrences(tokens);
 
         } else {
             throw new ObjectNotFoundException(ObjectNotFoundException.Type.DOCUMENT);
         }
         return document;
     }
 
     /**
      * Saves or updates a document
      *
      * @param document
      * @throws Exception
      */
     public synchronized void saveDocument(Document document) throws Exception {
         if (document.getTokensOccurrences().isEmpty()) {
             throw new Exception("Empty tokens list");
         }
 
         this.connection.setAutoCommit(false);
         try {
             PreparedStatement statement;
             PreparedStatement statement2;
             ResultSet data;
 
             String category_id = document.getCategoryId();
             if (category_id == null) {
                 throw new Exception("You must set a category's id");
             }
 
             String document_id;
             if (document.getId() == null) {
                 document_id = UUID.randomUUID().toString();
             } else {
                 document_id = document.getId();
                 this.deleteDocumentById(document_id);
             }
 
             statement = this.connection.prepareStatement("REPLACE INTO documents (id, category_id) VALUES (?, ?)");
             statement.setString(1, document_id);
             statement.setString(2, category_id);
             statement.executeUpdate();
 
             Set<String> tokens = new HashSet<String>(document.getTokensOccurrences().keySet());
             Map<String, Integer> tokens_ids = new HashMap<String, Integer>();
             Map<Integer, String> tokens_names = new HashMap<Integer, String>();
 
             statement = this.connection.prepareStatement("SELECT id, name FROM tokens WHERE name IN (" + this.generateQsForIn(tokens.size()) + ")");
             int i = 1;
             for (String token_name : tokens) {
                 statement.setString(i++, token_name);
             }
             data = statement.executeQuery();
             while (data.next()) {
                 tokens.remove(data.getString("name"));
                 tokens_ids.put(data.getString("name"), data.getInt("id"));
                 tokens_names.put(data.getInt("id"), data.getString("name"));
             }
 
             statement = this.connection.prepareStatement("INSERT INTO tokens (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
             for (String token_name : tokens) {
                 statement.setString(1, token_name);
                 statement.executeUpdate();
 
                 data = statement.getGeneratedKeys();
                 if (data != null && data.next()) {
                     tokens_ids.put(token_name, data.getInt(1));
                     tokens_names.put(data.getInt(1), token_name);
                 } else {
                     throw new Exception("Unable to add token: " + token_name);
                 }
             }
 
             statement = this.connection.prepareStatement("INSERT INTO documents_tokens (document_id, token_id, occurrences) VALUES (?, ?, ?)");
             for (Map.Entry<String, Integer> token : tokens_ids.entrySet()) {
                 if (!document.getTokensOccurrences().containsKey((String) token.getKey())) {
                     System.out.println(document.getTokensOccurrences());
                     System.out.println(token.getKey());
                 }
                 statement.setString(1, document_id);
                 statement.setInt(2, (Integer) token.getValue());
                 statement.setInt(3, document.getTokensOccurrences().get((String) token.getKey()));
                 statement.executeUpdate();
             }
 
             statement2 = this.connection.prepareStatement(
                     "UPDATE categories_tokens "
                     + "SET occurrences = occurrences + ? "
                     + "WHERE token_id = ? AND category_id = ?");
             statement = this.connection.prepareStatement(
                     "SELECT token_id "
                     + "FROM categories_tokens "
                     + "WHERE token_id IN (" + this.generateQsForIn(tokens_ids.size()) + ") AND category_id = ?");
             i = 1;
             for (Map.Entry<String, Integer> token : tokens_ids.entrySet()) {
                 statement.setInt(i++, (Integer) token.getValue());
             }
             statement.setString(i, category_id);
             data = statement.executeQuery();
             while (data.next()) {
                 int token_id = data.getInt("token_id");
                 statement2.setInt(1, document.getTokensOccurrences().get((String) tokens_names.get(token_id)));
                 statement2.setInt(2, token_id);
                 statement2.setString(3, category_id);
                 statement2.executeUpdate();
                 tokens_names.remove(token_id);
             }
 
             for (Map.Entry<Integer, String> token : tokens_names.entrySet()) {
                 statement = this.connection.prepareStatement(
                         "INSERT INTO categories_tokens "
                         + "(token_id, category_id, occurrences) "
                         + "VALUES (?, ?, ?)");
                 statement.setInt(1, token.getKey());
                 statement.setString(2, category_id);
                 statement.setInt(3, document.getTokensOccurrences().get((String) token.getValue()));
                 statement.executeUpdate();
             }
 
             this.connection.commit();
             document.setId(document_id);
 
         } catch (Exception e) {
             this.connection.rollback();
             throw e;
 
         } finally {
             this.connection.setAutoCommit(true);
         }
     }
 
     /**
      * Deletes a document given its id
      *
      * @param document_id
      * @throws Exception
      */
     public synchronized void deleteDocumentById(String document_id) throws Exception {
         PreparedStatement statement2 = this.connection.prepareStatement(
                 "UPDATE categories_tokens "
                 + "SET occurrences = GREATEST(0, occurrences - ?) "
                 + "WHERE token_id = ? AND category_id = ? "
                 + "LIMIT 1");
         PreparedStatement statement = this.connection.prepareStatement(
                 "SELECT dw.token_id, dw.occurrences, d.category_id "
                 + "FROM documents_tokens dw "
                 + "INNER JOIN documents d ON d.id = dw.document_id "
                 + "WHERE d.id = ?");
         statement.setString(1, document_id);
         ResultSet data = statement.executeQuery();
         while (data.next()) {
             statement2.setInt(1, data.getInt("occurrences"));
             statement2.setInt(2, data.getInt("token_id"));
             statement2.setString(3, data.getString("category_id"));
             statement2.executeUpdate();
         }
 
         statement = this.connection.prepareStatement("DELETE FROM documents WHERE id = ?");
         statement.setString(1, document_id);
         statement.executeUpdate();
     }
 
     /**
      * Finds a token given its id
      *
      * @param id token id
      * @return token matching id
      * @throws Exception
      */
     public Token findTokenById(String id) throws Exception {
         return this.findTokenByParam("id", id);
     }
 
     /**
      * Finds a token given its name
      *
      * @param name token name
      * @return token matching name
      * @throws Exception
      */
     public Token findTokenByName(String name) throws Exception {
         return this.findTokenByParam("name", name);
     }
 
     /**
      * Finds a token by a parameter (id or name)
      *
      * @param param search parameter
      * @param value id or name
      * @return token matching name
      * @throws Exception
      */
     private Token findTokenByParam(String param, String value) throws Exception {
         Token token = new Token();
         PreparedStatement statement;
         ResultSet data;
 
         statement = this.connection.prepareStatement("SELECT id, name FROM tokens WHERE " + param + " = ? LIMIT 1");
         statement.setString(1, value);
         data = statement.executeQuery();
         if (data.next()) {
             int token_id = data.getInt("id");
             token.setId(Integer.toString(token_id));
             token.setName(data.getString("name"));
 
             Map<String, Integer> occurences = new HashMap<String, Integer>();
             PreparedStatement statement2 = this.connection.prepareStatement("SELECT category_id, occurrences FROM categories_tokens WHERE token_id = ?");
             statement2.setInt(1, token_id);
             ResultSet data2 = statement2.executeQuery();
             while (data2.next()) {
                 occurences.put(data2.getString("category_id"), data2.getInt("occurrences"));
             }
             token.setOccurrencesByCategory(occurences);
 
         } else {
             throw new ObjectNotFoundException(ObjectNotFoundException.Type.TOKEN);
         }
         return token;
     }
 
     /**
      * Finds a list of tokens given a set of names
      *
      * @param names set of names
      * @return list of tokens matching names
      * @throws Exception
      */
     public List<Token> findTokensByNames(Set<String> names) throws Exception {
         List<Token> tokens = new ArrayList<Token>();
 
         PreparedStatement statement;
         ResultSet data;
 
         statement = this.connection.prepareStatement("SELECT id, name FROM tokens WHERE name IN (" + this.generateQsForIn(names.size()) + ")");
         int i = 1;
         for (String name : names) {
             statement.setString(i++, name);
         }
         data = statement.executeQuery();
         while (data.next()) {
             Token token = new Token();
             int token_id = data.getInt("id");
             token.setId(Integer.toString(token_id));
             token.setName(data.getString("name"));
 
             Map<String, Integer> occurences = new HashMap<String, Integer>();
             PreparedStatement statement2 = this.connection.prepareStatement("SELECT category_id, occurrences FROM categories_tokens WHERE token_id = ?");
             statement2.setInt(1, token_id);
             ResultSet data2 = statement2.executeQuery();
             while (data2.next()) {
                 occurences.put(data2.getString("category_id"), data2.getInt("occurrences"));
             }
             token.setOccurrencesByCategory(occurences);
             tokens.add(token);
         }
         return tokens;
     }
 
     /**
      * Deletes all categories and documents
      * 
      * @throws Exception 
      */
     public void empty() throws Exception {
         this.connection.prepareStatement("DELETE FROM categories").executeUpdate();
         this.connection.prepareStatement("DELETE FROM tokens").executeUpdate();
     }
 
     /**
      * Prepares a list of "?" for PreparedStatement IN clauses
      * 
      * @param numQs
      * @return
      */
     private String generateQsForIn(int numQs) {
         String items = "";
         for (int i = 0; i < numQs; i++) {
             items += "?";
             if (i < numQs - 1) {
                 items += ", ";
             }
         }
         return items;
     }
 }
