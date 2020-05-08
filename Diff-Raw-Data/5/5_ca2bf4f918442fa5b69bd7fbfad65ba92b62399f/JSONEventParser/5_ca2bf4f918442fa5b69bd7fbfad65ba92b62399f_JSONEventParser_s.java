 package net.jhorstmann.json;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 public class JSONEventParser extends AbstractParser implements Iterator<JSONEventParser.EventType> {
 
     public enum EventType {
 
         START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY, PROPERTY, VALUE_NUMBER, VALUE_STRING, VALUE_NULL, VALUE_TRUE, VALUE_FALSE;
     }
 
     enum ContainerType {
 
         OBJECT, ARRAY;
     }
 
     enum ParserState {
 
         FIRST_ITEM, FIRST_PROPERTY, EXPECT_VALUE, EXPECT_PROPERTY, EXPECT_COLON, EXPECT_COMMA_OR_END;
     }
     private LinkedList<ContainerType> stack = new LinkedList<ContainerType>();
     private ParserState state = ParserState.EXPECT_VALUE;
     private Double currentNumber;
     private String currentString;
 
     public JSONEventParser(Reader reader) {
         super(reader);
     }
 
     public JSONEventParser(String json) {
         this(new StringReader(json));
     }
 
     public EventType nextEvent() throws IOException {
         currentNumber = null;
         currentString = null;
         int ch = peekToken();
         switch (ch) {
             case 'n':
                 if (state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_VALUE) {
                     consume("null");
                     state = ParserState.EXPECT_COMMA_OR_END;
                     return EventType.VALUE_NULL;
                 } else {
                     throw createSyntaxException(ch);
                 }
             case 't':
                 if (state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_VALUE) {
                     consume("true");
                     state = ParserState.EXPECT_COMMA_OR_END;
                     return EventType.VALUE_TRUE;
                 } else {
                     throw createSyntaxException(ch);
                 }
             case 'f':
                 if (state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_VALUE) {
                     consume("false");
                     state = ParserState.EXPECT_COMMA_OR_END;
                     return EventType.VALUE_FALSE;
                 } else {
                     throw createSyntaxException(ch);
                 }
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
             case '-':
                 if (state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_VALUE) {
                     currentNumber = parseDouble(ch);
                     state = ParserState.EXPECT_COMMA_OR_END;
                     return EventType.VALUE_NUMBER;
                 } else {
                     throw createSyntaxException(ch);
                 }
             case '"':
                 if (state == ParserState.FIRST_PROPERTY || state == ParserState.EXPECT_PROPERTY) {
                     currentString = parseStringImpl(ch);
                     state = ParserState.EXPECT_COLON;
                     return EventType.PROPERTY;
                 } else if (state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_VALUE) {
                     currentString = parseStringImpl(ch);
                     state = ParserState.EXPECT_COMMA_OR_END;
                     return EventType.VALUE_STRING;
                 } else {
                     throw createSyntaxException(ch);
                 }
             case ':':
                 if (state != ParserState.EXPECT_COLON) {
                     throw createSyntaxException(ch);
                 }
                 consume();
                 state = ParserState.EXPECT_VALUE;
                 return nextEvent();
             case ',':
                 if (stack.isEmpty() || state != ParserState.EXPECT_COMMA_OR_END) {
                     throw createSyntaxException(ch);
                 }
                 consume();
                 state = stack.getFirst() == ContainerType.OBJECT ? ParserState.EXPECT_PROPERTY : ParserState.EXPECT_VALUE;
                 return nextEvent();
             case '{':
                 if (state != ParserState.EXPECT_VALUE) {
                     throw createSyntaxException(ch);
                 }
                 consume();
                 stack.addFirst(ContainerType.OBJECT);
                 state = ParserState.FIRST_PROPERTY;
                 return EventType.START_OBJECT;
             case '[':
                 if (state != ParserState.EXPECT_VALUE) {
                     throw createSyntaxException(ch);
                 }
                 consume();
                 state = ParserState.FIRST_ITEM;
                 stack.addFirst(ContainerType.ARRAY);
                 return EventType.START_ARRAY;
             case '}':
                 if ((state == ParserState.FIRST_PROPERTY || state == ParserState.EXPECT_COMMA_OR_END) && !stack.isEmpty() && stack.removeFirst() == ContainerType.OBJECT) {
                     consume();
                     return EventType.END_OBJECT;
                 } else {
                     throw new JSONSyntaxException("Unexpected end of object in state " + state);
                 }
             case ']':
                 if ((state == ParserState.FIRST_ITEM || state == ParserState.EXPECT_COMMA_OR_END) && !stack.isEmpty() && stack.removeFirst() == ContainerType.ARRAY) {
                     consume();
                     return EventType.END_ARRAY;
                 } else {
                     throw new JSONSyntaxException("Unexpected end of array in state " + state);
                 }
             default:
                 throw createSyntaxException(ch);
         }
     }
 
     public boolean hasNextEvent() throws IOException {
         int ch = peekToken();
         return ch != -1;
     }
 
     public String getString() {
         if (currentString == null) {
             throw new IllegalStateException("No current string value");
         }
         return currentString;
     }
 
     public Double getNumber() {
         if (currentNumber == null) {
             throw new IllegalStateException("No current number value");
         }
         return currentNumber;
     }
 
     public boolean hasNext() {
         try {
             return hasNextEvent();
         } catch (IOException ex) {
             NoSuchElementException ex2 = new NoSuchElementException(ex.getMessage());
            ex2.initCause(ex2);
             throw ex2;
         }
     }
 
     public EventType next() {
         try {
             return nextEvent();
         } catch (IOException ex) {
             NoSuchElementException ex2 = new NoSuchElementException(ex.getMessage());
            ex2.initCause(ex2);
             throw ex2;
         }
     }
 
     public void remove() {
         throw new UnsupportedOperationException("Remove is not supported.");
     }
 }
