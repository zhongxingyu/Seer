 package org.sterling.scanner;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.sterling.scanner.InputReaderTest.reader;
 import static org.sterling.scanner.ScannerUtils.invalidEscapeSequence;
 import static org.sterling.scanner.ScannerUtils.unexpectedInput;
 import static org.sterling.scanner.ScannerUtils.unterminatedCharacter;
 import static org.sterling.scanner.ScannerUtils.unterminatedString;
 import static org.sterling.scanner.TokenKind.CHARACTER;
 import static org.sterling.scanner.TokenKind.INTEGER;
 import static org.sterling.scanner.TokenKind.STRING;
 import static org.sterling.scanner.TokenKind.UNDEFINED;
 
 import org.junit.Test;
 import org.sterling.SyntaxException;
 
 public class ScannerUtilsTest {
 
     @Test
     public void unexpectedInputShouldNotThrowWithoutThrow() throws SyntaxException {
         unexpectedInput(reader("Should not throw without throw operator"), UNDEFINED);
     }
 
     @Test(expected = SyntaxException.class)
     public void unexpectedInputShouldThrowWithThrow() throws SyntaxException {
         throw unexpectedInput(reader("Should throw with throw operator"), UNDEFINED);
     }
 
     @Test
     public void shouldReplaceCharacterWithName() throws SyntaxException {
         assertThat(unexpectedInput(reader(")"), UNDEFINED).getMessage(), containsString("RIGHT PARENTHESIS ')'"));
     }
 
     @Test
     public void shouldReplaceNullCharacterWithEndOfInputAndBlank() throws SyntaxException {
         assertThat(unexpectedInput(reader("\0"), UNDEFINED).getMessage(), containsString("END OF INPUT ''"));
     }
 
     @Test
     public void shouldReplaceControlCharacterWithBlank() throws SyntaxException {
         assertThat(unexpectedInput(reader("\b"), UNDEFINED).getMessage(), containsString("BACKSPACE ''"));
     }
 
     @Test
     public void shouldReplaceNewLineWithBlank() throws SyntaxException {
         assertThat(unexpectedInput(reader("\n++"), UNDEFINED).getMessage(), containsString("LINE FEED (LF) ''"));
     }
 
     @Test
     public void shouldNotReplaceSpace() throws SyntaxException {
         assertThat(unexpectedInput(reader(" "), UNDEFINED).getMessage(), containsString("SPACE ' '"));
     }
 
     @Test
     public void shouldShowLocation() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         when(location.toString()).thenReturn("-->LOCATION HERE<--");
         assertThat(unexpectedInput(reader, UNDEFINED).getMessage(), containsString("[-->LOCATION HERE<--]"));
     }
 
     @Test
     public void shouldShowNameOfExpectedKind() throws SyntaxException {
         assertThat(unexpectedInput(reader("oops"), INTEGER).getMessage(), containsString("expecting INTEGER"));
         assertThat(unexpectedInput(reader("oops"), STRING).getMessage(), containsString("expecting STRING"));
     }
 
     @Test
     public void unterminatedStringShouldShowLocation() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         when(location.toString()).thenReturn("-->LOCATION HERE<--");
         assertThat(unterminatedString(reader, STRING).getMessage(), containsString("[-->LOCATION HERE<--]"));
     }
 
     @Test
     public void unterminatedStringShouldShowTokenKind() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         assertThat(unterminatedString(reader, STRING).getMessage(), containsString("Unterminated STRING"));
     }
 
     @Test
     public void unterminatedCharacterShouldShowLocation() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         when(location.toString()).thenReturn("-->LOCATION HERE<--");
         assertThat(unterminatedCharacter(reader, CHARACTER).getMessage(), containsString("[-->LOCATION HERE<--]"));
     }
 
     @Test
     public void unterminatedCharacterShouldShowTokenKind() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         assertThat(unterminatedCharacter(reader, CHARACTER).getMessage(), containsString("Unterminated CHARACTER"));
     }
 
     @Test
     public void invalidEscapeSequenceShouldShowLocation() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         when(location.toString()).thenReturn("-->LOCATION HERE<--");
         assertThat(invalidEscapeSequence(reader, STRING).getMessage(), containsString("[-->LOCATION HERE<--]"));
     }
 
     @Test
     public void invalidEscapeSequenceShouldShowTokenKind() throws SyntaxException {
         InputReader reader = mock(InputReader.class);
         Location location = mock(Location.class);
         when(reader.getLocation()).thenReturn(location);
         assertThat(invalidEscapeSequence(reader, STRING).getMessage(), containsString("Invalid escape sequence in STRING"));
     }
 }
