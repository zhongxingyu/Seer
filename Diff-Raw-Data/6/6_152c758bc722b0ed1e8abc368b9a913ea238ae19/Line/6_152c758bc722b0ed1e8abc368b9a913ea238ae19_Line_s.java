 package com.nigorojr.typebest;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Line extends JPanel implements Iterator<Word> {
     private ArrayList<Word> words = new ArrayList<Word>();
     private Iterator<Word> wordsIterator = words.iterator();
     private final JLabel space = new JLabel(" ");
 
     /**
      * Returns the total width of the words in the line by adding the length of
      * the word and the width of the spaces between them.
      * 
      * @return The total width of the words in the line, including the spaces
      *         between the words.
      */
     public int getLineWidth() {
         int width = 0;
         for (int i = 0; i < words.size(); i++)
            width += words.get(i).getWidth();

         // Add total length of the spaces between words
        width += space.getWidth() * (words.size() - 1);
 
         return width;
     }
 
     /**
      * Adds a word to the line. This method also adds a space character to the
      * line in order to separate the words with spaces.
      * 
      * @param word
      *            The one word to be added.
      */
     public void addWord(String word) {
         Word wordPanel = new Word(word);
         words.add(wordPanel);
 
         // Don't add a space character if it's the first word in the line
         if (!words.isEmpty())
             this.add(space);
         this.add(wordPanel);
     }
 
     /**
      * Accepts a word and add that to this line only if the width after adding
      * it is less than or equal to then also given width. The width of the space
      * between the last word and the word to be added is also considered.
      * 
      * @param word
      *            The word to be added.
      * @param n
      *            The limit width in pixels.
      * @return True if that word was added, false if not.
      */
     public boolean addWordIfWithin(String word, int n) {
         Word wordPanel = new Word(word);
         if (getLineWidth() + space.getWidth() + wordPanel.getWidth() <= n) {
             addWord(word);
             return true;
         }
         else
             return false;
     }
 
     /**
      * Returns the next word. This method can be used to avoid confusion when
      * using the <code>next</code> method in the Word class.
      * 
      * @return The next word as a Word object.
      */
     public Word nextWord() {
         return next();
     }
 
     @Override
     public boolean hasNext() {
         return wordsIterator.hasNext();
     }
 
     @Override
     public Word next() {
         return wordsIterator.next();
     }
 
     @Override
     public void remove() {
         wordsIterator.remove();
     }
 }
