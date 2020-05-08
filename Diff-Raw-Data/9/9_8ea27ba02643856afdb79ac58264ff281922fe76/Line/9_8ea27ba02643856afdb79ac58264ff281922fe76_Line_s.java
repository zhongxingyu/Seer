 package com.nigorojr.typebest;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Line extends JPanel implements Iterator<Word> {
     private ArrayList<Word> words = new ArrayList<Word>();
     private Iterator<Word> wordsIterator;
     private final JLabel space = new JLabel(" ");
 
     public Line() {
         space.setPreferredSize(new Dimension(15, 0));
         this.setLayout(new FlowLayout(FlowLayout.LEADING, space
                 .getPreferredSize().width, space.getPreferredSize().height));
         this.setAlignmentX(LEFT_ALIGNMENT);
     }
 
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
             width += words.get(i).getPreferredSize().width;
        // Add total length of the spaces between words
        width += space.getPreferredSize().width * (words.size() - 1);
 
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
 
         wordsIterator = words.iterator();
     }
 
     /**
      * Accepts a word and a width and checks if adding that word will exceed the
      * given width.
      * 
      * @param word
      *            The word to be checked.
      * @param n
      *            The limit width in pixels.
      * @return True if that word is within the given width, false if not.
      */
     public boolean isWordWithin(String word, int n) {
         Word wordPanel = new Word(word);
         return getLineWidth() + space.getPreferredSize().width
                 + wordPanel.getPreferredSize().width <= n;
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
 
     @Override
     public Dimension getMaximumSize() {
         Dimension dim = new Dimension(this.getPreferredSize());
         return dim;
     }
 }
