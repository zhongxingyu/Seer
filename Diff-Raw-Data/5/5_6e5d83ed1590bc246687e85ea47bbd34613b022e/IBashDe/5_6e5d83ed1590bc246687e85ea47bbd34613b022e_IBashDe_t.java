 package at.yawk.ircquotes.de.ibash;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import at.yawk.ircquotes.Author;
 import at.yawk.ircquotes.Quote;
 import at.yawk.ircquotes.QuoteSite;
 import at.yawk.ircquotes.SearchQuery;
 import at.yawk.ircquotes.SearchRequest;
 import at.yawk.yxml.EntityNamespace;
 import at.yawk.yxml.Lexer;
 import at.yawk.yxml.TextNode;
 import at.yawk.yxml.dom.DOMNode;
 import at.yawk.yxml.dom.DOMParser;
 
 public class IBashDe extends QuoteSite {
     public static void main(String... args) throws IOException {
         System.out.println(Arrays.asList(new IBashDe().parseQuotes("http://ibash.de/top_1.html")));
     }
     
     @Override
     public SearchRequest createRequest(SearchQuery query) {
         switch (query.getOrder()) {
         case BEST:
             return new SearchRequest() {
                 @Override
                 public Quote[] getBlock(int blockIndex) throws IOException {
                     return parseQuotes("http://ibash.de/top_" + blockIndex + ".html");
                 }
             };
         case NEWEST:
             return new SearchRequest() {
                 @Override
                 public Quote[] getBlock(int blockIndex) throws IOException {
                     return parseQuotes("http://ibash.de/neueste_" + blockIndex + ".html");
                 }
             };
         case RANDOM:
             return new SearchRequest() {
                 @Override
                 public Quote[] getBlock(int blockIndex) throws IOException {
                     return parseQuotes("http://ibash.de/random.html");
                 }
             };
         default:
             throw new UnsupportedOperationException();
         }
     }
     
     @Override
     public Quote getQuoteForId(int id) throws IOException {
         Quote[] result = parseQuotes("http://ibash.de/zitat_" + id + ".html");
         return result.length == 0 ? null : result[0];
     }
     
     @Override
     protected Author createAuthor(int id, String name) {
         throw new UnsupportedOperationException();
     }
     
     @Override
     protected Quote createQuote(int id, String quote, Author author) {
         return new QuoteImpl(id, quote, author) {
             @Override
             public Author getAuthor() {
                 throw new UnsupportedOperationException();
             }
         };
     }
     
     private Quote[] parseQuotes(String url) throws IOException {
         final Lexer l = new Lexer(new URL(url));
         l.setCleanupWhitespace(true);
         final DOMNode n = new DOMParser(l).parse();
         final List<Quote> quotes = new ArrayList<Quote>();
         for (DOMNode q : n.getChildrenForMatch(DOMNode.getAttributeEqualsMatcher("class", "quotetable"), Integer.MAX_VALUE, true)) {
             try {
                 final StringBuilder quote = new StringBuilder();
                 DOMNode table = getFirstChild(q.getChildren().get(1), 2);
                final List<DOMNode> rows = table.getChildren();
                for (int i = 0; i < rows.size() - 1; i++) {
                    for (DOMNode td : rows.get(i).getChildren()) {
                         final DOMNode sec = getFirstChild(td, 2);
                         if (sec.getElement() instanceof TextNode) {
                             quote.append(((TextNode) sec.getElement()).getUnescapedText(EntityNamespace.HTML_NAMESPACE));
                         }
                     }
                     quote.append('\n');
                 }
                 quotes.add(createQuote(Integer.parseInt(getFirstChild(q, 7).getElement().getRawContent().substring(1)), quote.toString(), null));
             } catch (IndexOutOfBoundsException e) {
                 // advertisements
             }
         }
         return quotes.toArray(new Quote[quotes.size()]);
     }
     
     private static DOMNode getFirstChild(DOMNode n, int iterations) {
         return iterations == 0 ? n : getFirstChild(n.getChildren().get(0), iterations - 1);
     }
 }
