 package org.zwobble.shed.parser.parsing;
 
 import org.junit.Test;
 import org.zwobble.shed.parser.tokeniser.TokenPosition;
 import org.zwobble.shed.parser.tokeniser.Tokeniser;
 
 import com.google.common.collect.Iterators;
 import com.google.common.collect.PeekingIterator;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.zwobble.shed.parser.parsing.Result.success;
 
 public class ParserTest {
     private final Tokeniser tokeniser = new Tokeniser();
     private final Parser parser = new Parser();
     
     @Test public void
     packageDeclarationIsListOfIdentifiersJoinedByDots() {
         assertThat(parser.parsePackageDeclaration().parse(tokens("package shed.util.collections;")),
                    is(success(new PackageDeclarationNode(asList("shed", "util", "collections")))));
     }
     
     @Test public void
     sourceNodeBeginsWithPackageDeclaration() {
         assertThat(parser.source(tokens("package shed.util.collections;")),
                    is(success(new SourceNode(new PackageDeclarationNode(asList("shed", "util", "collections"))))));
     }
     
     @Test public void
     errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
         assertThat(parser.parsePackageDeclaration().parse(tokens("packag shed.util.collections;")),
                    is(Result.<PackageDeclarationNode>failure(asList(new Error(1, 1, "Expected keyword \"package\" but got identifier \"packag\"")))));
     }
     
     @Test public void
     errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
         assertThat(parser.parsePackageDeclaration().parse(tokens("package shed .util.collections;")),
                   is(Result.<PackageDeclarationNode>failure(asList(new Error(1, 13, "Expected symbol \";\" but got whitespace \" \"")))));
     }
     
     @Test public void
     errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
         assertThat(parser.parsePackageDeclaration().parse(tokens("package shed. util.collections;")),
                    is(Result.<PackageDeclarationNode>failure(asList(new Error(1, 14, "Expected identifier but got whitespace \" \"")))));
     }
     
     private PeekingIterator<TokenPosition> tokens(String input) {
         return Iterators.peekingIterator(tokeniser.tokenise(input).iterator());
     }
 }
