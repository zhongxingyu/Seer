 package de.hszg.atocc.core.util.test;
 
import de.hszg.atocc.core.util.SetService;
 import de.hszg.atocc.core.util.automaton.Alphabet;
 import de.hszg.atocc.core.util.automaton.InvalidAlphabetCharacterException;
import de.hszg.atocc.core.util.internal.SetServiceImpl;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Assert;
import org.junit.Before;
 import org.junit.Test;
 
 public final class AlphabetTests {
 
     private static final String A = "A";
     private static final String B = "B";
     private static final String C = "C";
    
    private SetService setService;
    
    @Before
    public void setUp() {
        setService = new SetServiceImpl();
    }
 
     @Test
     public void testGetCharacters() throws InvalidAlphabetCharacterException {
         final Alphabet alphabet = new Alphabet();
         alphabet.add(A);
         alphabet.add(B);
 
        Assert.assertEquals(setService.createSetWith(A, B), alphabet.getCharacters());
     }
     
     @Test
     public void testSetCharacters() throws InvalidAlphabetCharacterException {
         final Alphabet alphabet = new Alphabet();
         
         Assert.assertEquals(Collections.emptySet(), alphabet.getCharacters());
 
         alphabet.setTo(Arrays.asList(A, B));
 
        Assert.assertEquals(setService.createSetWith(A, B), alphabet.getCharacters());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getAlphabetCharactersShouldReturnUnmodifiableSet()
             throws InvalidAlphabetCharacterException {
         final Alphabet alphabet = new Alphabet();
         alphabet.add(A);
         alphabet.add(B);
 
         final Set<String> characters = alphabet.getCharacters();
         characters.add(C);
     }
 
     @Test
     public void setAlphabetShouldCopyValues() throws InvalidAlphabetCharacterException {
         final Alphabet alphabet = new Alphabet();
 
         Assert.assertEquals(Collections.emptySet(), alphabet.getCharacters());
 
         final Set<String> expected = new HashSet<String>();
         expected.addAll(Arrays.asList(A, B));
 
         alphabet.setTo(expected);
 
        expected.add(C);
 
         Assert.assertFalse(alphabet.contains(C));
     }
 
 }
