 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.fusepool.enhancer.engines.dictionaryannotator;
 
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.process.CoreLabelTokenFactory;
 import edu.stanford.nlp.process.PTBTokenizer;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.arabidopsis.ahocorasick.SearchResult;
 import org.arabidopsis.ahocorasick.AhoCorasick;
 import org.tartarus.snowball.SnowballStemmer;
 
 /**
  * 
  * @author Gabor
  */
 public class DictionaryAnnotator {
     
     private TokenizedText tokenizedText;
     private List<TokenizedText> tokenizedTerms;
     private DictionaryStore originalDictionary;
     private DictionaryStore processedDictionary;
     private List<Entity> entities;
     private AhoCorasick tree;
     private boolean caseSensitive;
     private int caseSensitiveLength;
     private boolean eliminateOverlapping;
     private String stemmingLanguage;
     private Boolean stemming;
     private Map<String,String> languages;
     
     /**
      * Initializes the dictionary annotator by reading the dictionary and building
      * the search tree which is the soul of the Aho-Corasic algorithm.
      * 
      * @param _dictionary
      * @param _stemmingLanguage
      * @param _caseSensitive
      * @param _caseSensitiveLength
      * @param _eliminateOverlapping 
      */
     public DictionaryAnnotator(DictionaryStore _dictionary, String _stemmingLanguage, boolean _caseSensitive, 
             int _caseSensitiveLength, boolean _eliminateOverlapping) {
         
         stemmingLanguage = _stemmingLanguage;
         caseSensitive = _caseSensitive;
         caseSensitiveLength = _caseSensitiveLength;
         eliminateOverlapping = _eliminateOverlapping;
         
         if(stemmingLanguage == null)
         {
             stemmingLanguage = "None";
         }
         else if(stemmingLanguage.isEmpty())
         {
             stemmingLanguage = "None";
         }
         
         languages = new HashMap<String,String>();
         languages.put("None", "");
         languages.put("Danish", "danish");
         languages.put("Dutch", "dutch");
         languages.put("English", "english");
         languages.put("Finnish", "finnish");
         languages.put("French", "french");
         languages.put("German", "german");
         languages.put("Hungarian", "hungarian");
         languages.put("Italian", "italian");
         languages.put("Norwegian", "norwegian");
         //languages.put("english2", "porter");
         languages.put("Portuguese", "portuguese");
         languages.put("Romanian", "romanian");
         languages.put("Russian", "russian");
         languages.put("Spanish", "spanish");
         languages.put("Swedish", "swedish");
         languages.put("Turkish", "turkish");
         
         originalDictionary = new DictionaryStore();
         processedDictionary = new DictionaryStore();
         
         stemming = false;
         if(stemmingLanguage != null){
             if(!languages.get(stemmingLanguage).isEmpty()){
                 stemmingLanguage = languages.get(stemmingLanguage);
                 stemming = true;
             }
         }
         
         String[] terms = ReadDictionary(_dictionary.keywords);
         tokenizedTerms = TokenizeTerms(terms);
         
         if(stemming) {
             StemTerms();
         }
         
         tree = new AhoCorasick();
         for (TokenizedText e : tokenizedTerms) {
             tree.add(e.text, e.text);
 	}
 	tree.prepare();
     }
     
     /**
      * Processes the input text and returns the found entities.
      * @param text
      * @return 
      */
     public List<Entity> Annotate(String text){
 //        long start = System.currentTimeMillis();
         tokenizedText = TokenizeText(text);
 //        long end = System.currentTimeMillis();
 //        System.out.println("TokenizeText: " + Long.toString(end - start) + "ms");
         if(stemming) {
 //            start = System.currentTimeMillis();
             StemText();
 //            end = System.currentTimeMillis();
 //            System.out.println("StemDocuments: " + Long.toString(end - start) + "ms");
         }
 //        start = System.currentTimeMillis();
         FindEntities(tokenizedText);
 //        end = System.currentTimeMillis();
 //        System.out.println("FindEntities: " + Long.toString(end - start) + "ms");
 
 //        start = System.currentTimeMillis();
         EliminateOverlapping();
 //        end = System.currentTimeMillis();
 //        System.out.println("EliminateOverlapping: " + Long.toString(end - start) + "ms");
         
         List<Entity> entitiesToReturn = new ArrayList<Entity>(); 
         for(Entity e : entities){
             if(!e.overlap){
                 entitiesToReturn.add(e);
             }
         }
         return entitiesToReturn;
     }
     
     /**
      * Reads the dictionary from the input stream.
      * @param input
      * @return 
      */
     private String[] ReadDictionary(Map<String,UriRef> dictionary) {
         String[] entities = new String[dictionary.size()];
         Set set = dictionary.entrySet();
         Iterator iterator = set.iterator();
         
         int index = 0;
         String label;
         UriRef uri;
         while(iterator.hasNext()){
             Map.Entry me = (Map.Entry)iterator.next();
             label = (String) me.getKey();
             uri = (UriRef) me.getValue();
             entities[index] = label;
             originalDictionary.AddElement(label, uri);       
             index++;
         }         
         return entities;
     }
 
     /**
      * Tokenizes the all the entities in the dictionary and returns the 
      * tokenized entities. 
      * If caseSensitive is true and caseSensitiveLength > 0 all tokens 
      * whose length is equal or bigger than the caseSensitiveLength are 
      * converted to lowercase.
      * If caseSensitive is true and caseSensitiveLength = 0 no conversion
      * is applied.
      * If caseSensitive is false all tokens are converted to lowercase.
      * 
      * @param originalTerms
      * @return 
      */
     public List<TokenizedText> TokenizeTerms(String[] originalTerms) {
         StringReader sr;
         PTBTokenizer ptbt;
         StringBuilder sb;
         String[] terms = originalTerms;
         
         List<TokenizedText> tlist = new ArrayList<TokenizedText>();
         TokenizedText tokText;
         for (int i = 0; i < originalTerms.length; i++) {
             tokText = new TokenizedText(originalTerms[i]);
             
             sr = new StringReader(terms[i]);
             ptbt = new PTBTokenizer(sr, new CoreLabelTokenFactory(), "ptb3Escaping=false");
             sb = new StringBuilder();
             
             Token t;
             String word;
             int position = 1;
             int begin, end;
             sb.append(" ");
             if (caseSensitive) {
                 if (caseSensitiveLength > 0) {
                     for (CoreLabel label; ptbt.hasNext();) {
                         label = (CoreLabel) ptbt.next();
                         word = label.word();
                         
                         if (word.length() > caseSensitiveLength) {
                             word = word.toLowerCase();
                         }
                         
                         t = new Token(word);
                         t.setOriginalBegin(label.beginPosition());
                         t.setOriginalEnd(label.endPosition());
 
                         begin = position + 1;
                         t.setBegin(begin);
 
                         end = begin + word.length();
                         t.setEnd(end);
 
                         position = end;
 
                         tokText.addToken(t);
 
                        sb.append(word);
                         sb.append(" ");
                     }
                 }
                 else{
                     for (CoreLabel label; ptbt.hasNext();) {
                         label = (CoreLabel) ptbt.next();
                         word = label.word();
 
                         t = new Token(word);
                         t.setOriginalBegin(label.beginPosition());
                         t.setOriginalEnd(label.endPosition());
 
                         begin = position + 1;
                         t.setBegin(begin);
 
                         end = begin + word.length();
                         t.setEnd(end);
 
                         position = end;
 
                         tokText.addToken(t);
 
                        sb.append(word);
                         sb.append(" ");
                     }
                 }
             } else {
                 for (CoreLabel label; ptbt.hasNext();) {
                     label = (CoreLabel) ptbt.next();
                     word = label.word();
 
                     word = word.toLowerCase();
 
                     t = new Token(word);
                     t.setOriginalBegin(label.beginPosition());
                     t.setOriginalEnd(label.endPosition());
 
                     begin = position + 1;
                     t.setBegin(begin);
 
                     end = begin + word.length();
                     t.setEnd(end);
 
                     position = end;
 
                     tokText.addToken(t);
 
                    sb.append(word);
                     sb.append(" ");
                 }
             }
               
             
             tokText.setText(sb.toString());
             tlist.add(tokText);
         }
         return tlist;
     }
     
     /**
      * Tokenizes the original text and returns the tokenized text. 
      * If caseSensitive is true and caseSensitiveLength > 0 all tokens 
      * whose length is equal or bigger than the caseSensitiveLength are 
      * converted to lowercase.
      * If caseSensitive is true and caseSensitiveLength = 0 no conversion
      * is applied.
      * If caseSensitive is false all tokens are converted to lowercase.
      * 
      * @param text
      * @return 
      */
     public TokenizedText TokenizeText(String text) {
         StringReader sr;
         PTBTokenizer ptbt;
         StringBuilder sb;
         
         TokenizedText tokText = new TokenizedText(text);
 
         sr = new StringReader(text);
         ptbt = new PTBTokenizer(sr, new CoreLabelTokenFactory(), "ptb3Escaping=false");
         sb = new StringBuilder();
 
         Token t;
         String word;
         int position = 0;
         int begin, end;
 
         sb.append(" ");
         if(caseSensitive){
             if(caseSensitiveLength > 0){
                 for (CoreLabel label; ptbt.hasNext();) {
                     label = (CoreLabel) ptbt.next();
                     word = label.word();
 
                     if(word.length() > caseSensitiveLength){
                         word = word.toLowerCase();
                     }
 
                     t = new Token(word);
                     t.setOriginalBegin(label.beginPosition());
                     t.setOriginalEnd(label.endPosition());
 
                     begin = position + 1;
                     t.setBegin(begin);
 
                     end = begin + word.length();
                     t.setEnd(end);
 
                     position = end;
 
                     tokText.addToken(t);
 
                     sb.append(word);
                     sb.append(" ");
                 }  
             }
             else{
                 for (CoreLabel label; ptbt.hasNext();) {
                     label = (CoreLabel) ptbt.next();
                     word = label.word();
 
                     t = new Token(word);
                     t.setOriginalBegin(label.beginPosition());
                     t.setOriginalEnd(label.endPosition());
 
                     begin = position + 1;
                     t.setBegin(begin);
 
                     end = begin + word.length();
                     t.setEnd(end);
 
                     position = end;
 
                     tokText.addToken(t);
 
                     sb.append(word);
                     sb.append(" ");
                 }          
             }
         }
         else{
             for (CoreLabel label; ptbt.hasNext();) {
                 label = (CoreLabel) ptbt.next();
                 word = label.word();
 
                 word = word.toLowerCase();
 
                 t = new Token(word);
                 t.setOriginalBegin(label.beginPosition());
                 t.setOriginalEnd(label.endPosition());
 
                 begin = position + 1;
                 t.setBegin(begin);
 
                 end = begin + word.length();
                 t.setEnd(end);
 
                 position = end;
 
                 tokText.addToken(t);
 
                 sb.append(word);
                 sb.append(" ");
             }
         }
         tokText.setText(sb.toString());
         return tokText;
     }
     
     /**
      * This function runs the Aho-Corasick string matching algorithm on the 
      * tokenized (and stemmed) text using the search tree built from the dictionary.
      * @param tokenizedText 
      */
     private void FindEntities(TokenizedText tokenizedText){
         entities = new ArrayList<Entity>();
         Entity entity = null;
         String str = "";
         int length = 0, lastIndex = 0, maxlength = 0;
         for (Iterator iter = tree.search(tokenizedText.text.toCharArray()); iter.hasNext(); ) {
 	    SearchResult result = (SearchResult) iter.next();
             maxlength = 0;
             for(Object e : result.getOutputs()){
                 length = e.toString().length();
                 if(maxlength < length){
                     str = e.toString();
                     maxlength = length;
                 }
 //                System.out.println("name\t-->\t\"" + str + "\"");
 //                System.out.println("begin\t-->\t" + (result.getLastIndex() - str.length()));
             }
             if(!str.equals("")){
             	str = str.substring(1, str.length() - 1);
                 length = str.length();
                 lastIndex = result.getLastIndex() - 1;
                 
                 entity = tokenizedText.FindMatch((lastIndex - length), lastIndex);
                 //entity.text = str;
                 entity.uri = stemming ? processedDictionary.GetURI(str) : originalDictionary.GetURI(entity.label);
                 entities.add(entity);
             }
 	}
     }
     
     /**
      * Eliminates the overlaps among all the entities found in the text. If 
      * we have two entities, Entity1 and Entity2, and Entity1 is within the 
      * boundaries of Entity2, then Entity1 is marked and is later discarded.
      * If the variable eliminateOverlapping is true, entities whose 
      * boundaries are overlapping are also marked and are later discarded.
      */
     public void EliminateOverlapping(){
         Entity e1, e2;
         for (int i = 0; i < entities.size(); i++) {
             e1 = entities.get(i);
             for (int j = 0; j < entities.size(); j++) {
                 e2 = entities.get(j);
                 if(i == j){
                     continue;
                 }
                 else{
                     if(e1.begin > e2.end){
                         continue;
                     }
                     else if(e1.end < e2.begin){
                         continue;
                     }
                     else{
                         if(e1.begin >= e2.begin && e1.end <= e2.end){
                             e1.overlap = true;
                             break;
                         }
                         else if(eliminateOverlapping){
                             if(e1.begin > e2.begin && e1.end > e2.end && e1.begin < e2.end){
                                 e1.overlap = true;
                                 break;
                             }
                             else if(e1.begin < e2.begin && e1.end < e2.end && e1.end < e2.begin){
                                 e1.overlap = true;
                                 break;
                             }
                             else{
                                 continue;
                             }
                         }
                         else{
                             continue;
                         }
                     }
                 }
             }
         }
     }
     
     /**
      * The function is responsible for the stemming of each entity in the dictionary
      * based on the stemming language defined in the constructor.
      */
     private void StemTerms() {
         try {
             int offset = 0;
             int overallOffset = 0;
             String word = "";
             String name;
             UriRef uri;
             StringBuilder sb = new StringBuilder();
             Class stemClass = Class.forName("org.tartarus.snowball.ext." + stemmingLanguage + "Stemmer");
             SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
             for (TokenizedText tokenizedText : tokenizedTerms) {
                 sb = new StringBuilder();
                 sb.append(" ");
                 for (Token token : tokenizedText.tokens) {
                     stemmer.setCurrent(token.text);
                     stemmer.stem();
                     word = stemmer.getCurrent();  
                     
                     offset = token.text.length() - word.length();
                     
                     token.begin -= overallOffset;
                     overallOffset += offset;
                     token.end -= overallOffset;
                     
                     sb.append(word);
                     sb.append(" ");
 
                     token.text = word;
                 }
                 name = sb.toString();
                 uri = originalDictionary.GetURI(tokenizedText.originalText);
                 tokenizedText.setText(name);
                 processedDictionary.AddElement(name.substring(1, name.length() - 1), uri);
             }
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * The function is responsible for the stemming of the main text based on
      * the stemming language defined in the constructor.
      */
     private void StemText() {
         try {
             int offset = 0;
             int overallOffset = 0;
             String word = "";
             StringBuilder sb = new StringBuilder();
             Class stemClass = Class.forName("org.tartarus.snowball.ext." + stemmingLanguage + "Stemmer");
             SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
 
             sb = new StringBuilder();
             sb.append(" ");
             for (Token token : tokenizedText.tokens) {
                 stemmer.setCurrent(token.text);
                 stemmer.stem();
                 word = stemmer.getCurrent();   //TODO save stemmed text and new positions
 
                 offset = token.text.length() - word.length();
 
                 token.begin -= overallOffset;
                 overallOffset += offset;
                 token.end -= overallOffset;
 
                 sb.append(word);
 
                 sb.append(" ");
 
                 token.text = word;
             }
             tokenizedText.setText(sb.toString());
             
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 }
