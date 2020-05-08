 package com.morgajel.spoe.service;
 
 import junit.framework.TestCase;
 import java.util.ArrayList;
 import java.util.List;
 
 import  com.morgajel.spoe.domain.Snippet;
 
 public class SimpleSnippetManagerTest extends TestCase {
 
     private SimpleSnippetManager snippetManager;
     
     private List<Snippet> snippets;
     
     private static int SNIPPET_COUNT = 2;
     
     private static String CHPTR_1_TEXT = "So I kicked him right in the icicles.";
     
     private static String CHPTR_2_TEXT = "Tun like fluffy food.";
     
     protected void setUp() throws Exception {
         snippetManager = new SimpleSnippetManager();
         
         snippets = new ArrayList<Snippet>();
         
         // stub up a list of snippets
         Snippet snippet = new Snippet();
         snippet.setText("So I kicked him right in the icicles.");
         snippets.add(snippet);
 
         snippet = new Snippet();
         snippet.setText("Tun like fluffy food.");
         snippets.add(snippet);
         
         
         snippetManager.setSnippets(snippets);
 
         
         
     }
 
    public void testGetProductsWithNoProducts() {
         snippetManager = new SimpleSnippetManager();
         assertNull(snippetManager.getSnippets());
     }
 
     
    public void testGetProducts() {
         List<Snippet> snippets = snippetManager.getSnippets();
         assertNotNull(snippets);        
         assertEquals(SNIPPET_COUNT, snippetManager.getSnippets().size());
     
         Snippet snippet = snippets.get(0);
         assertEquals(CHPTR_1_TEXT, snippet.getText());
         
         snippet = snippets.get(1);
         assertEquals(CHPTR_2_TEXT, snippet.getText());
               
     }     
     
     
 }
