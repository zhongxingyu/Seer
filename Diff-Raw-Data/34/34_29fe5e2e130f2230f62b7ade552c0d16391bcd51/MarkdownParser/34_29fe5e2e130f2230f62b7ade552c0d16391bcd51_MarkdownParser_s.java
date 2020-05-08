 package net.rubygrapefruit.docs.markdown;
 
 import net.rubygrapefruit.docs.model.*;
 import net.rubygrapefruit.docs.parser.Parser;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Builds a document for some Markdown input.
  */
 public class MarkdownParser extends Parser {
     @Override
     protected Document doParse(Reader input, String fileName) throws Exception {
         BuildableDocument document = new BuildableDocument();
         DocumentBuilder builder = new DocumentBuilder(document);
         Lexer lexer = new Lexer(input);
         LineParser parser = new LineParser(lexer);
         while (parser.peek().type != LineType.Finish) {
             if (empty(parser)) {
                 continue;
             }
             if (header1(parser, builder)) {
                 continue;
             }
             if (header2(parser, builder)) {
                 continue;
             }
             if (itemisedList(parser, builder.getCurrentComponent())) {
                 continue;
             }
             if (orderedList(parser, builder.getCurrentComponent())) {
                 continue;
             }
             if (para(parser, builder.getCurrentComponent())) {
                 continue;
             }
 
             throw new UnsupportedOperationException(String.format("Did not match any productions."));
         }
 
         return document;
     }
 
     private boolean para(LineParser parser, BuildableContainer container) throws IOException {
         BuildableParagraph paragraph = container.addParagraph();
         paragraph.append(parser.next().getContent());
         while (parser.peek().type != LineType.Finish && parser.peek().type != LineType.Empty) {
             paragraph.append(" ");
             paragraph.append(parser.next().getContent());
         }
         return true;
     }
 
     private boolean itemisedList(LineParser parser, BuildableContainer container) throws IOException {
         if (parser.peek().type != LineType.ItemisedListItem) {
             return false;
         }
 
         BuildableList list = container.addItemisedList();
         while (listItem(parser, list, LineType.ItemisedListItem)) {
         }
 
         return true;
     }
 
     private boolean orderedList(LineParser parser, BuildableContainer container) throws IOException {
         if (parser.peek().type != LineType.OrderedListItem) {
             return false;
         }
 
         BuildableList list = container.addOrderedList();
         while (listItem(parser, list, LineType.OrderedListItem)) {
         }
 
         return true;
     }
 
     private boolean listItem(LineParser parser, BuildableList list, LineType itemType) throws IOException {
         if (parser.peek().type != itemType) {
             return false;
         }
 
         Line line = parser.next();
         BuildableListItem item = list.addItem();
         BuildableParagraph paragraph = item.addParagraph();
         paragraph.append(line.getContent(2));
 
         while (parser.peek().type != LineType.Finish && parser.peek().type != itemType) {
             while (parser.peek().type != LineType.Finish && parser.peek().type != LineType.Empty
                     && parser.peek().type != itemType) {
                 line = parser.next();
                 paragraph.append(" ");
                 paragraph.append(line.getContent());
             }
 
             while (parser.peek().type == LineType.Empty) {
                 parser.next();
             }
 
             if (parser.peek().type != LineType.Continue) {
                 break;
             }
 
             paragraph = item.addParagraph();
             paragraph.append(parser.next().getContent(1));
         }
 
         return true;
     }
 
     private boolean empty(LineParser parser) throws IOException {
         Line line = parser.peek();
         if (line.type == LineType.Empty) {
             parser.next();
             return true;
         }
         return false;
     }
 
     private boolean header1(LineParser parser, DocumentBuilder builder) throws IOException {
         Line line1 = parser.peek();
         Line line2 = parser.peek(1);
         if (line2.type == LineType.H1) {
             parser.next();
             parser.next();
             builder.appendSection(1).setTitle(line1.getContent());
             return true;
         }
         return false;
     }
 
     private boolean header2(LineParser parser, DocumentBuilder builder) throws IOException {
         Line line1 = parser.peek();
         Line line2 = parser.peek(1);
         if (line2.type == LineType.H2) {
             parser.next();
             parser.next();
             builder.appendSection(2).setTitle(line1.getContent());
             return true;
         }
         return false;
     }
 
     enum LineType {
         Empty, Text, H1, H2, ItemisedListItem, OrderedListItem, Finish, Continue
     }
 
     private static class Line {
         final LineType type;
         final List<Token> tokens;
 
         private Line(LineType type, List<Token> tokens) {
             this.type = type;
             this.tokens = tokens;
         }
 
         @Override
         public String toString() {
             return String.format("{%s '%s'}", type, getContent());
         }
 
         CharSequence getContent() {
             return getContent(0);
         }
 
         CharSequence getContent(int startToken) {
             StringBuilder builder = new StringBuilder();
            for (int i = startToken; i < tokens.size(); i++) {
                 Token token = tokens.get(i);
                if (token.type == TokenType.WhiteSpace || token.type == TokenType.Continue) {
                     builder.append(' ');
                 } else {
                     builder.append(token.value);
                 }
             }
             return builder.toString();
         }
     }
 
     private static class LineParser {
         final List<Line> queue = new ArrayList<Line>();
         final Lexer lexer;
 
         private LineParser(Lexer lexer) {
             this.lexer = lexer;
         }
 
         public Line peek() throws IOException {
             return peek(0);
         }
 
         public Line peek(int depth) throws IOException {
             while (queue.size() <= depth) {
                 Line line = scanLine();
                 if (line == null) {
                     return null;
                 }
                 queue.add(line);
             }
             return queue.get(depth);
         }
 
         public Line next() throws IOException {
             if (!queue.isEmpty()) {
                 return queue.remove(0);
             }
             return scanLine();
         }
 
         private Line scanLine() throws IOException {
             List<Token> tokens = new ArrayList<Token>();
             boolean hasContent = false;
             while (lexer.next()) {
                 hasContent = true;
                 if (lexer.getType() == TokenType.EOL) {
                     break;
                 }
                 tokens.add(lexer.getToken());
             }
             if (!hasContent) {
                 return new Line(LineType.Finish, tokens);
             }
 
            if (tokens.size() > 0 && tokens.get(0).type == TokenType.WhiteSpace) {
                tokens.remove(0);
            }
             if (tokens.size() > 0 && tokens.get(tokens.size() - 1).type == TokenType.WhiteSpace) {
                 tokens.remove(tokens.size() - 1);
             }
            if (tokens.size() == 1 && tokens.get(0).type == TokenType.Continue) {
                 tokens.remove(0);
             }
 
             if (tokens.isEmpty()) {
                 return new Line(LineType.Empty, tokens);
             }
            if (tokens.get(0).type == TokenType.Continue) {
                return new Line(LineType.Continue, tokens);
            }
             if (tokens.size() > 2 && tokens.get(0).type == TokenType.ItemisedListItem && tokens.get(1).type
                     == TokenType.WhiteSpace) {
                 return new Line(LineType.ItemisedListItem, tokens);
             }
             if (tokens.size() > 2 && tokens.get(0).type == TokenType.OrderedListItem && tokens.get(1).type
                     == TokenType.WhiteSpace) {
                 return new Line(LineType.OrderedListItem, tokens);
             }
             if (tokens.size() == 1 && tokens.get(0).type == TokenType.H1) {
                 return new Line(LineType.H1, tokens);
             }
             if (tokens.size() == 1 && tokens.get(0).type == TokenType.H2) {
                 return new Line(LineType.H2, tokens);
             }
             return new Line(LineType.Text, tokens);
         }
     }
 
     enum TokenType {
        WhiteSpace, Word, H1, H2, EOL, ItemisedListItem, OrderedListItem, Continue
     }
 
     private static class Token {
         private final TokenType type;
         private final String value;
 
         private Token(TokenType type, String value) {
             this.type = type;
             this.value = value;
         }
     }
 
     private static class Lexer {
         private final EndOfLineSpec endOfLineSpec = new EndOfLineSpec();
         private final WordSpec wordSpec = new WordSpec();
         private final TokenSpec whiteSpaceSpec = new SequenceSpec(' ', '\t');
         private final TokenSpec equalsSpec = new SequenceSpec('=');
         private final TokenSpec dashesSpec = new SequenceSpec('-');
         private final TokenSpec itemisedListItemSpec = new ChoiceSpec('*', '+', '-');
         private final TokenSpec numberedListItemSpec = new NumberedItemSpec();
        private final TokenSpec continueSpec = new ContinueSpec();
 
         private final Buffer buffer;
         private boolean atStartOfLine;
         private TokenType type;
 
         private Lexer(Reader input) {
             this.buffer = new Buffer(input);
         }
 
         Token getToken() {
             return new Token(type, buffer.getValue());
         }
 
         TokenType getType() {
             return type;
         }
 
         boolean next() throws IOException {
             type = scanNext();
             atStartOfLine = (type == TokenType.EOL);
             return type != null;
         }
 
         TokenType scanNext() throws IOException {
             if (buffer.scanFor(endOfLineSpec)) {
                 return TokenType.EOL;
             }
            if (atStartOfLine && buffer.scanFor(continueSpec)) {
                return TokenType.Continue;
            }
             if (atStartOfLine && buffer.scanFor(itemisedListItemSpec)) {
                 return TokenType.ItemisedListItem;
             }
             if (atStartOfLine && buffer.scanFor(numberedListItemSpec)) {
                 return TokenType.OrderedListItem;
             }
             if (atStartOfLine && buffer.scanFor(equalsSpec)) {
                 return TokenType.H1;
             }
             if (atStartOfLine && buffer.scanFor(dashesSpec)) {
                 return TokenType.H2;
             }
             if (buffer.scanFor(whiteSpaceSpec)) {
                 return TokenType.WhiteSpace;
             }
             if (buffer.scanFor(wordSpec)) {
                 return TokenType.Word;
             }
             return null;
         }
     }
 
     private abstract static class TokenSpec {
         public abstract void match(Buffer buffer) throws IOException;
     }
 
     private static class EndOfLineSpec extends TokenSpec {
         @Override
         public void match(Buffer buffer) throws IOException {
             buffer.consume('\r');
             buffer.consume('\n');
         }
     }
 
     private static class SequenceSpec extends TokenSpec {
         private final char[] candidates;
 
         private SequenceSpec(char... candidates) {
             this.candidates = candidates;
         }
 
         @Override
         public void match(Buffer buffer) throws IOException {
             while (buffer.consume(candidates)) {
             }
         }
     }
 
     private static class ChoiceSpec extends TokenSpec {
         private final char[] candidates;
 
         private ChoiceSpec(char... candidates) {
             this.candidates = candidates;
         }
 
         @Override
         public void match(Buffer buffer) throws IOException {
             for (char candidate : candidates) {
                 if (buffer.consume(candidate)) {
                     if (!buffer.lookingAt(candidate)) {
                         return;
                     }
                     buffer.unwind();
                 }
             }
         }
     }
 
     private static class NumberedItemSpec extends TokenSpec {
         @Override
         public void match(Buffer buffer) throws IOException {
             if (!buffer.consumeRange('0', '9')) {
                 return;
             }
             while (buffer.consumeRange('0', '9')) {
                 ;
             }
             if (!buffer.consume('.')) {
                 buffer.unwind();
             }
         }
     }
 
     private static class ContinueSpec extends TokenSpec {
         @Override
         public void match(Buffer buffer) throws IOException {
             if (buffer.consume('\t')) {
             } else {
                 int count = 0;
                 while (buffer.consume(' ')) {
                     count++;
                 }
                 if (count < 4) {
                     buffer.unwind();
                     return;
                 }
             }
             while (buffer.consume('\t', ' ')) {
             }
         }
     }
 
     private static class WordSpec extends TokenSpec {
         @Override
         public void match(Buffer buffer) throws IOException {
             while (buffer.consumeAnyExcept(' ', '\t', '\r', '\n')) {
             }
         }
     }
 
     private static class Buffer {
         private final Reader reader;
         private final char[] buffer = new char[8192];
         int startToken = 0;
         int endToken = 0;
         int endBuffer = 0;
         boolean matched;
 
         private Buffer(Reader reader) {
             this.reader = reader;
         }
 
         public boolean scanFor(TokenSpec spec) throws IOException {
             startToken = endToken;
             matched = false;
             spec.match(this);
             return matched;
         }
 
         public String getValue() {
             return new String(buffer, startToken, endToken - startToken);
         }
 
         public void unwind() {
             endToken = startToken;
             matched = false;
         }
 
         public boolean lookingAt(char... candidates) throws IOException {
             int ch = peek();
             if (ch < 0) {
                 return false;
             }
             for (int i = 0; i < candidates.length; i++) {
                 char candidate = candidates[i];
                 if (ch == candidate) {
                     return true;
                 }
             }
             return false;
         }
 
         boolean consumeRange(char from, char to) throws IOException {
             int ch = peek();
             if (ch >= from && ch <= to) {
                 next();
                 return true;
             }
             return false;
         }
 
         boolean consume(char... candidates) throws IOException {
             if (lookingAt(candidates)) {
                 next();
                 return true;
             }
             return false;
         }
 
         public boolean consumeAnyExcept(char... candidates) throws IOException {
             int ch = peek();
             if (ch < 0) {
                 return false;
             }
             for (int i = 0; i < candidates.length; i++) {
                 char candidate = candidates[i];
                 if (ch == candidate) {
                     return false;
                 }
             }
             next();
             return true;
         }
 
         private int peek() throws IOException {
             if (endToken == endBuffer) {
                 if (startToken == 0 && endBuffer == buffer.length) {
                     throw new UnsupportedOperationException("Buffer overflow not implemented yet.");
                 }
                 System.arraycopy(buffer, startToken, buffer, 0, endBuffer - startToken);
                 endToken -= startToken;
                 endBuffer -= startToken;
                 startToken = 0;
                 int nread = reader.read(buffer, endBuffer, buffer.length - endBuffer);
                 if (nread < 0) {
                     return -1;
                 }
                 endBuffer += nread;
             }
 
             return buffer[endToken];
         }
 
         private void next() {
             endToken++;
             matched = true;
         }
     }
 }
