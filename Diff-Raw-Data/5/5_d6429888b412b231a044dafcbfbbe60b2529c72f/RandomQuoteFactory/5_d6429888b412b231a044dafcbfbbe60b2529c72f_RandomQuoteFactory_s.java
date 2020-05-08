 package com.isd.training.quotes;
 
 import java.util.List;
 import java.util.Random;
 
 /**
  * A quote factory which returns a quote at random.
  *
  * @author Steven Benitez
  */
 public abstract class RandomQuoteFactory implements QuoteFactory {
     private final List<String> quotes;
 
     /**
      * Allows us to randomly select a quote.
      */
     private final Random random = new Random(System.currentTimeMillis());
 
     /**
      * Constructs a new RandomQuoteFactory using the supplied list of quotes. If
      * the quotes list is null or empty, an exception will be thrown.
      *
      * @param quotes The list of quotes to randomly select from.
      */
     protected RandomQuoteFactory(List<String> quotes) {
         if (quotes == null) {
             throw new NullPointerException("You must provide a non-null list of quotes!");
         }
 
         if (quotes.isEmpty()) {
             throw new IllegalArgumentException("You must provide a non-empty list of quotes!");
         }
 
         this.quotes = quotes;
     }
 
     /**
      * Returns a random quote.
      *
      * @return A random quote.
      */
     @Override
     public String getQuote() {
        // select a random number between 0 and the size of the list (minus 1,
        // since the index starts at 0).
        int index = random.nextInt(quotes.size() - 1);
         return quotes.get(index);
     }
 }
