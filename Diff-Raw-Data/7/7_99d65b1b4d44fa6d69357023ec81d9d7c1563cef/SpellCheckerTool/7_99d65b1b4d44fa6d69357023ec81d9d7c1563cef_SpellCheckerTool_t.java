 /*
  * SpellCheckerTool.java
  *
  * Created on April 22, 2006, 8:30 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.erici.boggle.client.tools;
 
 import com.swabunga.spell.engine.SpellDictionary;
 import com.swabunga.spell.engine.SpellDictionaryHashMap;
 import com.swabunga.spell.event.SpellChecker;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Eric Internicola
  */
 public class SpellCheckerTool
 {
     //==========================================================================
     //  VARIABLE(S)
     //==========================================================================
     private static SpellCheckerTool     instance            = null;
     //private SpellDictionary             dictionary          = null;
     private SpellChecker                spellChecker        = null;
     private boolean                     spellingOverride    = false;
     private boolean                     loaded              = false;
     
     private static final String[]       SPELL_CHECKERS      = {
         "center.dic",
         "color.dic",
         "custom.dic",
         "eng_com.dic",
         "ize.dic",
         "labeled.dic",
         "yze.dic"
     };
     
     //==========================================================================
     //  CONSTRUCTOR(S)
     //==========================================================================
     
     //--------------------------------------------------------------------------
     // <>
     //--------------------------------------------------------------------------
     /**
      *
      * @param
      * @throws
      *
      * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
      */
     private SpellCheckerTool()
     {
         try
         {
             setSpellChecker(new SpellChecker());
             for(int i=0;i<SPELL_CHECKERS.length;i++)
             {
                ClassLoader loader = getClass().getClassLoader();
                InputStream in = loader.getResourceAsStream(SPELL_CHECKERS[i]);
                SpellDictionary dictionary = new SpellDictionaryHashMap(new InputStreamReader(in));
                // SpellDictionary dictionary = new SpellDictionaryHashMap(new InputStreamReader(getClass().getClassLoader().getSystemResourceAsStream(SPELL_CHECKERS[i])));
                 getSpellChecker().addDictionary(dictionary);
             }
         }
         catch (FileNotFoundException ex)
         {
             ex.printStackTrace();
             setSpellingOverride(true);
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
             setSpellingOverride(true);
         }
     }
     
     //==========================================================================
     //  METHOD(S)
     //==========================================================================
     
     //--------------------------------------------------------------------------
     // get
     //--------------------------------------------------------------------------
     /**
      * Singleton Accessor method.
      * @return The Singleton instance of this service.
      *
      * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
      */
     public static synchronized SpellCheckerTool get()
     {
         if(getInstance()==null)
         {
             setInstance(new SpellCheckerTool());
         }
         
         return getInstance();
     }
     
     public static void loadSpellCheckerWithDialog(JFrame parent)
     {
         
     }
     
     //--------------------------------------------------------------------------
     // checkWord
     //--------------------------------------------------------------------------
     /**
      * This method checks the provided word to see if it is correctly spelled
      * or not.
      * @param word The word to check.
      * @return True if the word is correctly spelled, False otherwise.
      *
      * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
      */
     public boolean checkWord(String word)
     {
         boolean correct = isSpellingOverride();
         
         if(!correct)
         {            
             correct = getSpellChecker().isCorrect(word);            
         }
         
         return correct;
     }
     
     //--------------------------------------------------------------------------
     // getSuggestions
     //--------------------------------------------------------------------------
     /**
      * This method gives you 5 suggestions for the word you provide.
      * @param word The word (mispelling) that you want suggestions for.
      * @return An array of Suggestions (literally a String array cast down to Object).
      *
      * @author <a href='mailto:intere@gmail.com'>Eric Internicola</a>
      */
     public Object[] getSuggestions(String word)
     {
         if(!isSpellingOverride())
         {
             return getSpellChecker().getSuggestions(word,5).toArray();
         }
         return new String[0];
     }
     
     public static void main(String[] args)
     {
         System.out.println(get().checkWord("ren"));
     }
     
     //==========================================================================
     //  GETTER(S) & SETTER(S)
     //==========================================================================
     
     private static SpellCheckerTool getInstance()
     {
         return instance;
     }
     
     private static void setInstance(SpellCheckerTool aInstance)
     {
         instance = aInstance;
     }
     
     protected boolean isSpellingOverride()
     {
         return spellingOverride;
     }
     
     protected void setSpellingOverride(boolean spellingOverride)
     {
         this.spellingOverride = spellingOverride;
     }
     
     public boolean isLoaded()
     {
         return loaded;
     }
     
     protected void setLoaded(boolean loaded)
     {
         this.loaded = loaded;
     }
     
     public SpellChecker getSpellChecker()
     {
         return spellChecker;
     }
     
     public void setSpellChecker(SpellChecker spellChecker)
     {
         this.spellChecker = spellChecker;
     }
 }
