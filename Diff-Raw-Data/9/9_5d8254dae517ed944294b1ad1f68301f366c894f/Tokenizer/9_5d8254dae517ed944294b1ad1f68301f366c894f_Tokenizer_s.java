 package LexicalAnalyzer;
 
 import java.util.HashSet;
 
 /**
  * Analizador léxico. Clase encargada de la tokenización del código fuente.
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class Tokenizer {
 
     private int lineNumber, currentState;
     private char currentChar;
     private InputReader reader;
     private HashSet<String> keywords;
 
     /**
      * Constructor de la clase Tokenizer
      *
      * Inicializa los marcadores y las estructuras de datos utilizadas.
      *
      * @param filename Path del archivo con el código fuente
      */
     public Tokenizer(String filename) {
         this.currentState = 0;
         this.lineNumber = 1;
 
         keywords = new HashSet<>();
         populateKeywords();
 
         reader = new InputReader(filename);
     }
 
     /**
      * Proceso de tokenización.
      *
      * Análisis léxico del archivo de código fuente.
      *
      * Devuelve un token por vez recorriendo el archivo de forma secuencial.
      *
      * @returns Token encontrado
      * @throws LexicalException
      */
     public Token getToken() throws LexicalException {
         StringBuilder lexeme = new StringBuilder();
         boolean flagZero = false;
 
         this.currentState = 0;
 
         while (true) {
             currentChar = (char) reader.readChar();
 
             switch (currentState) {
                 case 0: {
                     if (currentChar == '_' || Character.isLetter(currentChar)) {
                         currentState = 1;
                         lexeme.append(currentChar);
                     } else if (Character.isDigit(currentChar)) {
                         if (currentChar == '0') {
                             flagZero = true;
                         }
                         currentState = 2;
                         lexeme.append(currentChar);
                     } else {
                         switch (currentChar) {
                             case ' ':
                                 break;
                             case '\t':
                                 break;
                             case '\n':
                                 lineNumber++;
                                 break;
                             case '\'':
                                 currentState = 3;
                                 lexeme.append(currentChar);
                                 break;
                             case '"':
                                 currentState = 4;
                                 lexeme.append(currentChar);
                                 break;
                             case '>':
                                 currentState = 5;
                                 lexeme.append(currentChar);
                                 break;
                             case '<':
                                 currentState = 6;
                                 lexeme.append(currentChar);
                                 break;
                             case '=':
                                 currentState = 7;
                                 lexeme.append(currentChar);
                                 break;
                             case '!':
                                 currentState = 8;
                                 lexeme.append(currentChar);
                                 break;
                             case '&':
                                 currentState = 9;
                                 lexeme.append(currentChar);
                                 break;
                             case '|':
                                 currentState = 10;
                                 lexeme.append(currentChar);
                                 break;
                             case '*':
                                 return new Token("*", "*", lineNumber);
                             case '/':
                                 currentState = 11;
                                 lexeme.append(currentChar);
                                 break;
                             case '+':
                                 return new Token("+", "+", lineNumber);
                             case '-':
                                 return new Token("-", "-", lineNumber);
                             case '(':
                                 return new Token("(", "(", lineNumber);
                             case ')':
                                 return new Token(")", ")", lineNumber);
                             case '{':
                                 return new Token("{", "{", lineNumber);
                             case '}':
                                 return new Token("}", "}", lineNumber);
                             case ';':
                                 return new Token(";", ";", lineNumber);
                             case ',':
                                 return new Token(",", ",", lineNumber);
                             case '.':
                                 return new Token(".", ".", lineNumber);
                             case '%':
                                 return new Token("%", "%", lineNumber);
                             case '\0':
                                 return new Token("EOF", "\0", lineNumber);
                             default:
                                 throw new LexicalException("Line: " + lineNumber + " - Unsupported char.");
                         }
                     }
                     break;
                 }
                 case 1:
                     if (Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_') {
                         lexeme.append(currentChar);
                     } else {
                         reader.resetMark();
                         String lexemeString = lexeme.toString();
                         if (keywords.contains(lexemeString)) {
                             // Es una palabra clave.
                             return new Token(lexemeString, lexemeString, lineNumber);
                         } else {
                             // Es un identificador.
                             return new Token("id", lexemeString, lineNumber);
                         }
                     }
                     break;
                 case 2:
                    if (Character.isDigit(currentChar)) {
                         if (flagZero) {
                             throw new LexicalException("Line: " + lineNumber + " - Wrong number format. A number cannot start with 0.");
                        } else if (Character.isLetter(currentChar) || notExpectedCharNumber(currentChar)) {
                            throw new LexicalException("Line: " + lineNumber + " - Wrong number format.");
                        } else {
                             lexeme.append(currentChar);
                         }
                     } else {
                         reader.resetMark();
                         return new Token("intLiteral", lexeme.toString(), lineNumber);
                     }
                     break;
                 case 3:
                     if (currentChar != '\\' && currentChar != '\'' && currentChar != '\0' && currentChar != '\n') {
                         lexeme.append(currentChar);
                         currentState = 31;
                         break;
                     } else if (currentChar == '\\') {
                         lexeme.append(currentChar);
                         currentState = 32;
                         break;
                     } else if (currentChar == '\'') {
                         throw new LexicalException("Line: " + lineNumber + " - Empty char.");
                     } else {
                         throw new LexicalException("Line: " + lineNumber + " - Wrong formed char.");
                     }
                 case 31:
                     if (currentChar == '\'') {
                         lexeme.append(currentChar);
                         String lexemeString = lexeme.toString();
                         return new Token("char", lexemeString, lineNumber);
                     } else {
                         throw new LexicalException("Line: " + lineNumber + " - Wrong formed char.");
                     }
                 case 32:
                     if (currentChar != '\\' && currentChar != '\'' && currentChar != '\0' && currentChar != '\n') {
                         lexeme.append(currentChar);
                         currentState = 31;
                         break;
                     } else {
                         throw new LexicalException("Line: " + lineNumber + " - Wrong formed char.");
                     }
                 case 4:
                     if (currentChar != '\n' && currentChar != '"') {
                         lexeme.append(currentChar);
                         currentState = 41;
                         break;
                     } else if (currentChar == '"') {
                         lexeme.append(currentChar);
                         String lexemeString = lexeme.toString();
                         return new Token("String", lexemeString, lineNumber);
                     } else {
                         throw new LexicalException("Line: " + lineNumber + " - Wrong formed String.");
                     }
                 case 41:
                     if (currentChar != '\n' && currentChar != '"') {
                         lexeme.append(currentChar);
                         break;
                     } else if (currentChar == '"') {
                         lexeme.append(currentChar);
                         String lexemeString = lexeme.toString();
                         return new Token("String", lexemeString, lineNumber);
                     } else {
                         throw new LexicalException("Line: " + lineNumber + " - Wrong formed String.");
                     }
                 case 5:
                     if (currentChar == '=') {
                         return new Token(">=", ">=", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token(">", ">", lineNumber);
                     }
                 case 6:
                     if (currentChar == '=') {
                         return new Token("<=", "<=", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token("<", "<", lineNumber);
                     }
                 case 7:
                     if (currentChar == '=') {
                         return new Token("==", "==", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token("=", "=", lineNumber);
                     }
                 case 8:
                     if (currentChar == '=') {
                         return new Token("!=", "!=", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token("!", "!", lineNumber);
                     }
                 case 9:
                     if (currentChar == '&') {
                         return new Token("&&", "&&", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token("&", "&", lineNumber);
                     }
                 case 10:
                     if (currentChar == '|') {
                         return new Token("||", "||", lineNumber);
                     } else {
                         reader.resetMark();
                         return new Token("|", "|", lineNumber);
                     }
                 case 11:
                     if (currentChar == '/') {
                         proccessComment(); // S11.1
                         currentState = 0;
                         lexeme = new StringBuilder();
                         break;
                     } else if (currentChar == '*') {
                         processBlockComment(); // S11.2
                         currentState = 0;
                         lexeme = new StringBuilder();
                         break;
                     } else {
                         reader.resetMark();
                         return new Token("/", "/", lineNumber);
                     }
             }
         }
     }
 
     /**
      * Palabras reservadas de MiniJava.
      *
      * Inicialización de una estructura de datos con las palabras reservadas de
      * MiniJava.
      *
      * Si un lexema es reconocido como palabra reservada, se creará el Token
      * adecuado.
      *
      */
     private void populateKeywords() {
         keywords.add("class");
         keywords.add("extends");
         keywords.add("var");
         keywords.add("static");
         keywords.add("dynamic");
         keywords.add("void");
         keywords.add("boolean");
         keywords.add("char");
         keywords.add("int");
         keywords.add("String");
         keywords.add("if");
         keywords.add("else");
         keywords.add("while");
         keywords.add("for");
         keywords.add("return");
         keywords.add("this");
         keywords.add("new");
         keywords.add("null");
         keywords.add("true");
         keywords.add("false");
 
         /*
          * 
          StringBuilder keyword;
          keyword = new StringBuilder("class");
          keywords.add(keyword);
          keyword = new StringBuilder("extends");
          keywords.add(keyword);
          keyword = new StringBuilder("var");
          keywords.add(keyword);
          keyword = new StringBuilder("static");
          keywords.add(keyword);
          keyword = new StringBuilder("dynamic");
          keywords.add(keyword);
          keyword = new StringBuilder("void");
          keywords.add(keyword);
          keyword = new StringBuilder("boolean");
          keywords.add(keyword);
          keyword = new StringBuilder("char");
          keywords.add(keyword);
          keyword = new StringBuilder("int");
          keywords.add(keyword);
          keyword = new StringBuilder("String");
          keywords.add(keyword);
          keyword = new StringBuilder("if");
          keywords.add(keyword);
          keyword = new StringBuilder("else");
          keywords.add(keyword);
          keyword = new StringBuilder("while");
          keywords.add(keyword);
          keyword = new StringBuilder("for");
          keywords.add(keyword);
          keyword = new StringBuilder("return");
          keywords.add(keyword);
          keyword = new StringBuilder("this");
          keywords.add(keyword);
          keyword = new StringBuilder("new");
          keywords.add(keyword);
          keyword = new StringBuilder("null");
          keywords.add(keyword);
          keyword = new StringBuilder("true");
          keywords.add(keyword);
          keyword = new StringBuilder("false");
          keywords.add(keyword);
          */
     }
 
     /**
      * Procesamiento de las líneas de comentario.
      *
      * Consume la línea comentada para seguir con la tokenización.
      */
     private void proccessComment() {
         currentChar = (char) reader.readChar();
 
         while (currentChar != '\n') {
             currentChar = (char) reader.readChar();
         }
     }
 
     /**
      * Procesamiento de los bloques de comentarios.
      *
      * Consume el bloque de comentarios para seguir con la tokenización. Si se
      * encuentra el fin de archivo, se considera como un error y se lanzará una
      * excepción.
      *
      * @throws LexicalException
      */
     private void processBlockComment() throws LexicalException {
         boolean closeBlockComment = false;
         char nextChar;
         currentChar = (char) reader.readChar();
 
         nextChar = (char) reader.readChar();
 
         while (!closeBlockComment && nextChar != '\0') {
             if (currentChar == '*' && nextChar == '/') {
                 closeBlockComment = true;
             }
 
             currentChar = nextChar;
             nextChar = (char) reader.readChar();
         }
 
         if (nextChar == '\0') {
             throw new LexicalException("Block comment not closed and reached EOF.");
         } else {
             reader.resetMark(); // requerido para casos en el que el siguiente lexema se encuentra inmediatamente
         }
     }
 
     /**
      * Verificación de número bien formado.
      *
      * Para facilitar la descripción de errores en futuras etapas se previene la
      * aparición de combinaciones sintácticamente imposibles (e.g. 123hola) en
      * esta etapa.
      *
      * @returns true si el caracter encontrado determina un número mal formado,
      * false en caso contrario
      */
     private boolean notExpectedCharNumber(char currentChar) {
         if (Character.isDigit(currentChar) || currentChar == ' ' || currentChar == '+' || currentChar == '-' || currentChar == '/' || currentChar == '*' || currentChar == '%') {
             return false;
         } else {
             return true;
         }
     }
 }
