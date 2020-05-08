 package su.boptim.al.subjson;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class SubJson
 {
     // JUMP POINTS -- see big comment in parse()
     static final int LBL_PARSE_VALUE = 0;
     static final int LBL_PARSE_ARRAY = 1;
     static final int LBL_PA_STARTVALUE = 2;
     static final int LBL_PA_PARSEDVALUE = 3;
     static final int LBL_PARSE_OBJECT = 4;
     static final int LBL_PO_STARTKV = 5;
     static final int LBL_PO_PARSEDKV = 6;
     static final int LBL_ROUTE_VALUE = 7;
     
     public static boolean isDigit(int rune) 
     {
         if (rune >= '0' && rune <= '9') return true;
         else return false;
     }
 
     public static boolean isHexDigit(int rune)
     {
         if ((rune >= '0' && rune <= '9') ||
             (rune >= 'a' && rune <= 'f') ||
             (rune >= 'A' && rune <= 'F')) return true;
         else return false;
     }
     
     // Returns true if the unicode codepoint argument is a control character
     // (as defined by rfc4627). That is, U+0000 through U+001F.
     public static boolean isControlCharacter(int rune) 
     {
         if (rune >= 0 && rune <= 0x1f) return true;
         else return false;
     }
 
     public static boolean isWhitespace(int rune)
     {
         switch (rune) {
         case 0x20:
         case 0x09:
         case 0x0A:
         case 0x0D:
             return true;
         default:
             return false;
         }
     }
 
     public static boolean isArray(Object o)
     {
         return o instanceof ArrayList<?>;
     }
 
     public static boolean isObject(Object o)
     {
         return o instanceof HashMap<?,?>;
     }
 
     public static Object startArray()
     {
         return new ArrayList();
     }
 
     public static void arrayAppend(Object a, Object value)
     {
         ArrayList<Object> arr = (ArrayList<Object>)a;
         arr.add(value);
     }
 
     public static Object finishArray(Object arr)
     {
         return arr;
     }
 
     public static Object startObject()
     {
         return new HashMap<String,Object>();
     }
 
     public static void objectInsert(Object o, Object key, Object value)
     {
        HashMap<String, Object> obj = (HashMap<String,Object>)o;
         obj.put((String)key, value);
     }
 
     public static Object finishObject(Object obj)
     {
         return obj;
     }
 
     // jsonSrc must be pointing at the first character of a valid JSON object,
     // and the LightReader must buffer at least one character for backwards
     // movement.
     public static Object parse(LightReader jsonSrc) throws Exception
     {
         ArrayDeque<Object> valueStack = new ArrayDeque<Object>();
         ArrayDeque<Object> keyStack = new ArrayDeque<Object>(); // For parsing KV pairs in objects.
         int currState = LBL_PARSE_VALUE; 
 
         int currRune = 0;
         
         // Although null is a value we care about, we have written this so
         // that if there is no valid value read, it will have errored, so
         // (hopefully) latestValue is always the correct latest value.
         Object latestValue = null;
 
         // JSON can basically nest arbitrarily deeply, so only break out the
         // functions that read terminals (null/true/false, numbers, and strings)!
         // Everything else, we'll do with an explicit stack structure in this
         // loop without growing the execution stack.
 
         /*
           Basically, we wish we could write the following code (more or less):
 
           parseValue()                            // LBL_PARSE_VALUE:
               case '[': parseArray();
               case '{': parseObject();
               default: return parsePrimitive();
           
           parseArray()                            // LBL_PARSE_ARRAY:
               parse('[');
               startvalue:                         // LBL_PA_STARTVALUE:
               parseValue();
               if (lookahead == ',')               // LBL_PA_PARSEDVALUE:
                   parse(',');
                   goto startvalue;
               else
                   parse(']');
                   return newArray;
 
            parseObject()                          // LBL_PARSE_OBJECT:
                parse('{');
                startkv:                           // LBL_PO_STARTKV:
                parseString();
                parse(':');
                parseValue();
                if (lookahead == ',')
                    parse(',');
                    goto startkv;
                else
                    parse('}');
                    return newObject;
 
            However, Java, of course, does not allow us to use goto, nor does it
            let us have arbitrarily looping recursion (like the calls to 
            parseValue() in parseArray() and parseObject()). Thus, we must 
            manually manage the stack and simulate the gotos, and we do this in 
            the big while loop with nested switch statements. This interpretation
            should help reason about the loop; the outer switch is any point you
            might jump to in the pseudocode above. Pushing something onto the stack
            and 'break dispatch'ing is a function call. Popping off the stack and 
            setting a new state is like a function return. 
 
            I refer to these "set currState + break dispatch" as "calling" the 
            "function" they are jumping to, to help annotate the code below.
          */
         while (currRune != -1) {
             dispatch:
             switch (currState) {
             case LBL_PARSE_VALUE:
                 currRune = jsonSrc.read();
 
                 switch (currRune) {
                     // whitespace
                 case 0x20: // space
                 case 0x09: // tab
                 case 0x0A: // linefeed
                 case 0x0D: // carriage return
                     skipWhitespace(jsonSrc);
                     break dispatch; // Skip checking for value to insert.
                     
                     // null
                 case 'n': 
                     jsonSrc.move(-1); // Undo the lookahead that let us know it was true.
                     parseNull(jsonSrc);
 
                     latestValue = null;
                     break; // Jump to cleanup code after inner switch.
                     
                     // true & false
                 case 't':
                 case 'f':
                     jsonSrc.move(-1); // Undo the lookahead that let us know it was true.
                     latestValue = parseBoolean(jsonSrc);
                     break; // Jump to cleanup code after inner switch.
                     
                     // Number
                 case '-':
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
                     jsonSrc.move(-1); // Undo the lookahead that told us this was a number.
                     latestValue = parseNumber(jsonSrc);
                     break; // Jump to cleanup code after inner switch
                     
                     // String
                 case '"':
                     jsonSrc.move(-1);
                     latestValue = parseString(jsonSrc);
                     break; // Jump to cleanup code after inner switch
                     
                     // Array
                 case '[':
                     jsonSrc.move(-1); // Pushback for "parseArray()"
                     currState = LBL_PARSE_ARRAY;
                     break dispatch; // "Call" "parseArray()"
                 case ']':
                     // In correct JSON we should only see this after a '[', which
                     // will have "called" "parseArray()", so we shouldn't see this here.
                     throw new IllegalArgumentException("Encountered unexpected ']'.");
                     
                     // Object
                 case '{':
                     jsonSrc.move(-1); // Pushback for "parseObject()"
                     currState = LBL_PARSE_OBJECT;
                     break dispatch; // "Call" "parseObject()"
                 case '}':
                     // In correct JSON we should only see this after a '{', which
                     // will have "called" "parseObject()", so we shouldn't see this here.
                     throw new IllegalArgumentException("Encountered unexpected '}'.");
                     
                 case ',':
                     // We should only see a comma while reading an array or object.
                     throw new IllegalArgumentException("Encountered a comma, but weren't reading an object or array.");
                     
                 default:
                     throw new IllegalArgumentException("Encountered invalid character in input.");
                 }
 
                 // Fall through to route_value() to finish the value...
             case LBL_ROUTE_VALUE:
                 // Having read a value, we need to figure out where to store it and
                 // where to "return" to. If the value stack is empty, we are done and
                 // the value should be returned. Otherwise, we need to insert it on
                 // the value stack, depending on what is on top of that, and "return" to
                 // the "function" that was building what was on top of the stack.
                 if (valueStack.isEmpty()) {
                     return latestValue;
                 } else {
                     if (isArray(valueStack.peek())) {
                         // We had to parse a value while parsing an array
                         arrayAppend(valueStack.peek(), latestValue);
                         currState = LBL_PA_PARSEDVALUE;
                     } else if (isObject(valueStack.peek())) {
                         objectInsert(valueStack.peek(), keyStack.pop(), latestValue);
                         currState = LBL_PO_PARSEDKV;
                     }
                     break dispatch;
                 }
                 // Can't fall through to here.
 
                 // "parseArray()" (see comment above)
             case LBL_PARSE_ARRAY:
                 parseChar(jsonSrc, '[');
                 valueStack.push(startArray());
             case LBL_PA_STARTVALUE: // Note: Falls through from LBL_PARSE_ARRAY!
                 skipWhitespace(jsonSrc);
                 currRune = jsonSrc.read();
 
                 // Need to check for empty array, where an attempt to read a 
                 // value would fail.
                 jsonSrc.move(-1); // Pushback for "parseValue()" or finish array.
                 if (currRune == -1) {
                     throw new IllegalArgumentException("Reached EOF while parsing an array.");
                 } else if (currRune != ']') {
                     currState = LBL_PARSE_VALUE; // "Call" "parseValue()"
                     break dispatch;
                 }                     
                 // currRune == ']', so fall through to finish array
             case LBL_PA_PARSEDVALUE:         // ... which will know to return here from stack top.
                 skipWhitespace(jsonSrc);
                 currRune = jsonSrc.read();
                 jsonSrc.move(-1); // Pushback for parseChar().
                 if (currRune == ',') {
                     parseChar(jsonSrc, ',');
                     currState = LBL_PA_STARTVALUE;
                     break dispatch;
                 } else {
                     parseChar(jsonSrc, ']');
                     latestValue = finishArray(valueStack.pop());
                     // Now we need to check stack to figure out where to return to.
                     currState = LBL_ROUTE_VALUE; // "call" "route_value()"
                     break dispatch;
                 }
 
                 // "parseObject()" (see comment above)
             case LBL_PARSE_OBJECT:
                 parseChar(jsonSrc, '{');
                 valueStack.push(startObject());
             case LBL_PO_STARTKV: // Note: Falls through from LBL_PARSE_OBJECT!
                 skipWhitespace(jsonSrc);
                 currRune = jsonSrc.read(); 
 
                 // Need to check for '}' in case of empty object. If so,
                 // fall through to PARSEDKV to finish object.
                 jsonSrc.move(-1); // Pushback for "parseValue()" or finish object.
                 if (currRune == -1) {
                     throw new IllegalArgumentException("Reached EOF while parsing an object.");
                 } else if (currRune != '}') {
                     keyStack.push(parseString(jsonSrc));
                     skipWhitespace(jsonSrc);
                     parseChar(jsonSrc, ':');
                     skipWhitespace(jsonSrc);
                     currState = LBL_PARSE_VALUE; // "Call" "parseValue()"
                     break dispatch;
                 }
                 // currRune == '}' so fall through to finish object
             case LBL_PO_PARSEDKV:            // ... which will know to return here from stack top.
                 skipWhitespace(jsonSrc);
                 currRune = jsonSrc.read();
                 jsonSrc.move(-1); // Pushback for readChar().
                 if (currRune == ',') {
                     parseChar(jsonSrc, ',');
                     currState = LBL_PO_STARTKV;
                     break dispatch;
                 } else {
                     parseChar(jsonSrc, '}');
                     latestValue = finishObject(valueStack.pop());
                     // Now we need to check stack to figure out where to return to.
                     currState = LBL_ROUTE_VALUE; // "call" "route_value()"
                     break dispatch;
                 }
             }
         }   
         return valueStack.pop(); // No idea how we'd get here.
     }
 
     /*
       Given a LightReader at any point, skips past any whitespace (space, tab, CR, LF)
       so that the next character read will be something that is not whitespace (or EOF).
     */
     private static void skipWhitespace(LightReader jsonSrc)
     {
         int currRune = jsonSrc.read();
         
         while (true) {
             switch (currRune) {
             case -1: 
                 return;
             case 0x20: // space
             case 0x09: // tab
             case 0x0A: // linefeed
             case 0x0D: // carriage return
                 currRune = jsonSrc.read();
                 break;
             default:
                 jsonSrc.move(-1);  // Undo the lookahead that was not whitespace.
                 return;
             }
         }
     }
 
     /*
       Given a LightReader, attempts to read the next character from it and check that
       it is the character given as the second argument. If it is, it simply returns
       and the LightReader will be on the next character after the one just read. 
       Otherwise, throws a descriptive error.
      */
     private static void parseChar(LightReader jsonSrc, char theChar)
     {
         int currRune = jsonSrc.read();
         if (currRune == theChar) {
             return;
         } else if (currRune == -1) {
             throw new IllegalArgumentException("Read EOF when " + theChar 
                                                + " was expected.");
         } else {
             throw new IllegalArgumentException("Read " + (char)currRune + " when "
                                                + theChar + " was expected.");
         }
     }
 
     /*
       parseNull takes a LightReader that is pointing at a JSON null literal and
       advances the LightReader to the character after the end of the literal, 
       while checking that the null literal is correctly written and providing errors
       if not.
     */
     private static void parseNull(LightReader jsonSrc)
     {   
         // This loop only executes once, use it to simulate goto with a break.
         while (true) {
             int currRune = jsonSrc.read();
             if (currRune != 'n') break;
             
             currRune = jsonSrc.read();
             if (currRune != 'u') break;
 
             currRune = jsonSrc.read();
             if (currRune != 'l') break;
 
             currRune = jsonSrc.read();
             if (currRune != 'l') break;
 
             return;
         }
 
         // If we got here, it is because we had to break due to a parse error.
         throw new IllegalArgumentException("Encountered invalid input while attempting to read the null literal.");
     }
 
     /*
       parseBoolean takes a LightReader that is pointing at a JSON boolean literal
       and does two things:
       1) Returns the boolean value that literal represents (true or false)
       2) Advances the LightReader to the character after the end of the literal.
     */
     private static Boolean parseBoolean(LightReader jsonSrc)
     {
         int currRune = jsonSrc.read();
         switch (currRune) {
         case 't':
             // This loop only executes once, use it to simulate goto with a break.
             while (true) {
                 currRune = jsonSrc.read();            
                 if (currRune != 'r') break;
                 
                 currRune = jsonSrc.read();
                 if (currRune != 'u') break;
                 
                 currRune = jsonSrc.read();
                 if (currRune != 'e') break;
                 
                 return Boolean.TRUE;
             }
             
             // If we got here, it is because we had to break due to a parse error.
             throw new IllegalArgumentException("Encountered invalid input while attempting to read the boolean literal 'true'.");
         case 'f':
             // This loop only executes once, use it to simulate goto with a break.
             while (true) {
                 currRune = jsonSrc.read();
                 if (currRune != 'a') break;
 
                 currRune = jsonSrc.read();
                 if (currRune != 'l') break;
                 
                 currRune = jsonSrc.read();
                 if (currRune != 's') break;
                 
                 currRune = jsonSrc.read();
                 if (currRune != 'e') break;
 
                 return Boolean.FALSE;
             }
             
             // If we got here, it is because we had to break due to a parse error.
             throw new IllegalArgumentException("Encountered invalid input while attempting to read the boolean literal 'false'.");
         default:
             // This code should never execute unless there is a bug; this function
             // should only be called if the next 4 or 5 characters in the LightReader
             // will be one of the two boolean literals.
             throw new IllegalArgumentException("Attempted to parse a boolean literal out of input that was not pointing at one.");
         }
     }
 
     /* 
        parseNumber takes a LightReader that is pointing at a JSON number literal
        and does two things: 
        1) Returns the Number that literal represents
        2) Advances the LightReader to the first non-number character in the JSON
           source (that is, a read() after this function will return the next character
           after the number literal). Basically clips the number off the front of
           the stream.
     */
     private static Number parseNumber(LightReader jsonSrc)
     {
         StringBuilder sb = new StringBuilder();
         int currRune = jsonSrc.read();
 
         // This while loop will only execute once, we use it
         // to get access to the break statement to jump to the
         // finishing up code from various points. Note that every
         // call to read() must handle EOF with either a thrown
         // exception or a break from the main loop.
         boolean sawDecimal = false; // Will use these to parse at the end.
         boolean sawExponent = false;
         while (true) {
             boolean sawNegation = currRune == '-' ? true : false;
             if (sawNegation) {
                 // We'll append the negation to the string and move on to
                 // look for the first digit.
                 sb.appendCodePoint(currRune); 
                 currRune = jsonSrc.read();
             }
             
             // If we saw a '-' and the next character is not a digit or is EOF, 
             // it is invalid JSON. JSON requires at least one digit before decimal,
             // exponent parts, and end-of-number.
             if (sawNegation && !isDigit(currRune)) { // Also handles EOF.
                 throw new NumberFormatException("While attempting to parse a negative number, the negative sign was not followed by a digit.");
             }
             
             // A JSON number can only have a single leading 0 digit when it
             // is just before a decimal point or exponentiation.
             boolean sawLeadingZero = currRune == '0' ? true : false;
             sb.appendCodePoint(currRune);
             currRune = jsonSrc.read();
             
             if (sawLeadingZero && isDigit(currRune)) {
                 throw new NumberFormatException("While attempting to parse a number, there was a leading zero not immediately followed by a decimal point or exponentiation.");
             } else if (currRune == -1) {
                 break; // EOF, but enough input to parse a number.
             }
             
             // Copy as many digits as are present into the current string. Note that if
             // we already saw a '.' or 'e' (for example, this loop doesn't execute and
             // we move right on to the next test.
             while (isDigit(currRune)) {
                 sb.appendCodePoint(currRune);
                 currRune = jsonSrc.read();
             }
             
             if (currRune == -1) break; // EOF, but enough input to parse a number.
             
             // At this point, currRune has read a codepoint that is not the
             // unicode value for a digit. It may be a valid character for a number
             // or may indicate the number has finished.
             sawDecimal = currRune == '.' ? true : false;
             if (sawDecimal) {
                 // We saw a decimal point, so add it on and continue reading digits.
                 sb.appendCodePoint(currRune);
                 currRune = jsonSrc.read();
                 
                 // We must read at least one digit before moving on.
                 // Also handles EOF.
                 if (!isDigit(currRune)) {
                     throw new NumberFormatException("While attempting to parse a number, there was a decimal point not immediately followed by a digit.");
                 }
                 
                 while (isDigit(currRune)) {
                     sb.appendCodePoint(currRune);
                     currRune = jsonSrc.read();
                 }
             }
             
             // When we've gotten here, we've either seen a decimal point followed by
             // at least one digit or we haven't, so there may be exponentiation or
             // the end of the number ahead. We know whatever is next is not a digit,
             // but the only thing that can keep a number going now is e/E.
             if (currRune == 'e' || currRune == 'E') {
                 // Having seen an e/E, we must see at least one digit, possibly preceded
                 // by + or -.
                 sawExponent = true;
                 sb.appendCodePoint(currRune);
                 currRune = jsonSrc.read();
             } else break; // Handles EOF and non-digit, but enough to make number.
             
             // If we reach this point, then we saw e or E and did another read().
             // There might be a + or - which we will simply add to the number's
             // string and continue on to read digits and check for non-number chars.
             if (currRune == '+' || currRune == '-') {
                 // Just tack it on and continue on to the next character.
                 sb.appendCodePoint(currRune);
                 currRune = jsonSrc.read();
             }
             
             // Now currRune is past any e/E or +/- that would be valid. currRune must
             // be either a digit, EOF, or some non-number character. If it's not the
             // first one of those, then we've reached the end of the number.
             while (isDigit(currRune)) {
                 sb.appendCodePoint(currRune);
                 currRune = jsonSrc.read();
             }
             
             break; // We have to break out of the infinite loop every time.
         }
         
         // Finish up the parsing of the number.
         Number retVal = null;
         if (sawDecimal || sawExponent) { 
             // If there was a decimal point or exponent, it must be floating point.
             retVal = new Double(sb.toString()); 
         } else {
             retVal = new Integer(sb.toString());
         }
         
         // We had to see a non-number character to know we were past the end of
         // the number, so if we didn't see an EOF, move back so that whoever 
         // needs it next can have it available.
         if (currRune != -1) {
             jsonSrc.move(-1); 
         }
         return retVal;
     }
 
     /* 
        parseString takes a LightReader that is pointing at a JSON string literal
        and does two things: 
        1) Returns the String that literal represents
        2) Advances the LightReader to the first character after the end of the 
           string in the JSON source. Basically clips the string off the front
           of the stream.
     */
     private static String parseString(LightReader jsonSrc)
     {
         StringBuilder sb = new StringBuilder();
         int currRune = jsonSrc.read();
         
         if (currRune != '"') {
             throw new IllegalArgumentException("Attempted to parse a string literal from input that was not pointing at one.");
         }
 
         while (true) {
             currRune = jsonSrc.read();
             
             if (isControlCharacter(currRune)) {
                 throw new IllegalArgumentException("Encountered a control character while parsing a string.");
             } else {
                 switch (currRune) {
                 // End of input
                 case -1:
                     throw new IllegalArgumentException("Encountered end of input while reading a string.");
                 // End of string
                 case '"':
                     return sb.toString();
                 // Escape sequence. We'll handle it right here entirely.
                 case '\\':
                     currRune = jsonSrc.read();
                     switch (currRune) {
                     case '"': // Escaped quotation mark
                         sb.append('"');
                         break;
                     case '\\': // Escaped reverse solidus
                         sb.append('\\');
                         break;
                     case '/': // Escaped solidus
                         sb.append('/');
                         break;
                     case 'b': // Escaped backspace
                         sb.append('\b');
                         break;
                     case 'f': // Escaped formfeed
                         sb.append('\f');
                         break;
                     case 'n': // Escaped newline
                         sb.append('\n');
                         break;
                     case 'r': // Escaped carriage return
                         sb.append('\r');
                         break;
                     case 't': // Escaped tab
                         sb.append('\t');
                         break;
                     case 'u': // Escaped Unicode character
                         StringBuilder codepoint = new StringBuilder();
                         
                         for (int i = 0; i < 4; i++) {
                             currRune = jsonSrc.read();
                             if (isHexDigit(currRune)) {
                                 codepoint.appendCodePoint(currRune);
                             } else {
                                 throw new IllegalArgumentException("Encountered invalid input while reading a Unicode escape sequence.");
                             }
                         }
                         
                         sb.appendCodePoint(Integer.parseInt(codepoint.toString(), 16));
                         break;
                     default:
                         throw new IllegalArgumentException("Encountered invalid input while reading an escape sequence.");
                     }
                     break;
                 default:
                     // Just some regular old character.
                     sb.appendCodePoint(currRune);
                     break;
                 }
             }
         }
     }
 }
