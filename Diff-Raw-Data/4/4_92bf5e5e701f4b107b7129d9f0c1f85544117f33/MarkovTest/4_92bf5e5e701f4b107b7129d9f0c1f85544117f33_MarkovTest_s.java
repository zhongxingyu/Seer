 import org.junit.Test;
 import src.MarkovGenerator;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class MarkovTest {
     @Test
     public void shouldReadEmptyFrequencyFromEmptyText(){
         String text = "";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.frequencyOf("a"), is(0));
     }
 
     @Test
     public void shouldReadFrequenciesFromText(){
         String text = "ab";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.frequencyOf("a"), is(1));
     }
 
     @Test
     public void shouldReadMultipleFrequenciesFromText(){
         String text = "aaaa";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.frequencyOf("a"), is(4));
     }
 
     @Test
     public void shouldReadCommonlyFollowedByFromText(){
         String text = "ab";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.mostCommonlyFollowedOf("a").character(), is('b'));
     }
 
     @Test
     public void shouldReadCommonlyFollowedByFromTextAsNullWhenNone(){
         String text = "ab";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.mostCommonlyFollowedOf("a").character(), is('b'));
     }
 
     @Test
     public void shouldReadCommonlyFollowedByFromTextAsLastWhenTied(){
         String text = "acad";
         MarkovGenerator chain = new MarkovGenerator(text);
         assertThat(chain.mostCommonlyFollowedOf("a").character(), is('d'));
     }
 
     @Test
     public void shouldGenerateShortText(){
         String text = "ac ac ";
         MarkovGenerator generator = new MarkovGenerator(text);
         assertThat(generator.generate(5), is("ac ab"));
     }
 
     @Test
     public void shouldGenerateLongText(){
         String text = "We can extend this idea to longer sequences of letters. The order-2 text was made by generating each letter as a function of the two letters preceding it (a letter pair is often called a digram). The digram TH, for instance, is often followed in English by the vowels A, E, I, O, U and Y, less frequently by R and W, and rarely by other letters. The order-3 text is built by choosing the next letter as a function of the three previous letters (a trigram). By the time we get to the order-4 text, most words are English, and you might not be surprised to learn that it was generated from a Sherlock Holmes story (``The Adventure of Abbey Grange''). A classically educated reader of a draft of this column commented that this sequence of fragments reminded him of the evolution from Old English to Victorian English.\n" +
                 "Readers with a mathematical background might recognize this process as a Markov chain. One state represents each k-gram, and the odds of going from one to another don't change, so this is a ``finite-state Markov chain with stationary transition probabilities''.\n" +
                 "\n" +
                 "We can also generate random text at the word level. The dumbest approach is to spew forth the words in a dictionary at random. A slightly better approach reads a document, counts each word, and then selects the next word to be printed with the appropriate probability. We can get more interesting text, though, by using Markov chains that take into account a few preceding words as they generate the next word. Here is some random text produced after reading a draft of the first 14 columns of this book:";
         MarkovGenerator generator = new MarkovGenerator(text);
        assertThat(generator.generate(500), is("ac ab"));
     }
 }
