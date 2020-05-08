 /*
  * Copyright (C) 2009 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.parser.antlr;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Ignore;
 import org.junit.Test;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import novelang.common.SyntacticTree;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.*;
 import novelang.parser.SourceUnescape;
 import static novelang.parser.antlr.AntlrTestHelper.BREAK;
 import static novelang.parser.antlr.TreeFixture.tree;
 
 /**
  * GUnit sucks as it has completely obscure failures and stupid reports,
  * but it has some nice ideas to borrow.
  *
  * @author Laurent Caillette
  */
 public class ParagraphParsingTest {
 
   @Test
   public void wordContainsOELigatured() throws RecognitionException {
     PARSERMETHOD_WORD.createTree( "\u0153\u0152" ) ;
   }
 
 
   @Test
   public void failOnUnknownEscapedCharacter() throws RecognitionException {
     PARSERMETHOD_WORD.checkFails(
         SourceUnescape.ESCAPE_START + "does-not-exist" + SourceUnescape.ESCAPE_END ) ;
   }
 
   @Test
   public void paragraphIsSimplestList() throws RecognitionException {
     PARSERMETHOD_BIG_DASHED_LIST_ITEM.checkTreeAfterSeparatorRemoval( "--- w0", tree(
         PARAGRAPH_AS_LIST_ITEM_WITH_TRIPLE_HYPHEN_,
         tree( WORD_, "w0" )
     ) ) ;
   }
 
 
   
 
   @Test
   public void paragraphIsWordThenComma() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( "w0,", tree(
         NodeKind.PARAGRAPH_REGULAR,
         tree( WORD_, "w0" ),
         tree( PUNCTUATION_SIGN, tree( SIGN_COMMA, "," ) )
     ) ) ;
   }
 
 
 
   @Test
   public void paragraphIsWordsWithCommaInTheMiddle1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0,w1", tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             tree( PUNCTUATION_SIGN, tree( SIGN_COMMA, "," ) ),
             tree( WORD_, "w1" )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphIsWordThenApostrophe() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0'", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_APOSTROPHE_WORDMATE
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0'w1", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_APOSTROPHE_WORDMATE,
             tree( WORD_, "w1" )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphIsWordThenSemicolon() throws RecognitionException {
     SyntacticTree tree = PARSERMETHOD_WORD.createTree( "w0" ) ;
     LOG.debug( tree.toStringTree() ) ;
 
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0;", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_SEMICOLON
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenFullStop() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0.", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_FULLSTOP
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenQuestionMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0?", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_QUESTIONMARK
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenExclamationMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0!", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_EXCLAMATION_MARK
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenColon() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0:", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_COLON
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenEllipsis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0...", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             TREE_SIGN_ELLIPSIS
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphBodyIsEmphasizedWordThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0//w1" );
   }
 
   @Test
   public void paragraphIsWordsWithApostropheThenEmphasis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "w0 w1'w2//w3//.", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( WORD_, "w0" ),
             tree( WORD_, "w1" ),
             TREE_APOSTROPHE_WORDMATE,
             tree( WORD_, "w2" ),
             tree( BLOCK_INSIDE_SOLIDUS_PAIRS, tree( WORD_, "w3" ) ),
             TREE_SIGN_FULLSTOP
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsMultilineQuote() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "\"w1" + BREAK +
         "w2\"" 
     );
   }
 
   @Test
   public void paragraphIsMultilineQuoteWithPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "\"w1 w2. w3 w4." + BREAK +
         "w5 !\"" + BREAK +
         "w6 w7." );
   }
 
   @Test
   public void paragraphIsMultilineQuoteWithPunctuationSigns2() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w1.//" + BREAK +
         "w2. w3." 
     ) ;
   }
 
   @Test
   public void paragraphIsEmphasisAndQuoteWithPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w0.// " + BREAK +
         "  w1. w2. w3. " + BREAK +
         "  w4 : w5 w6. " + BREAK +
         "  \"w7 w8 ?\"." 
     );
   }
 
 
   @Test
   public void paragraphIsJustEllipsis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "..." ) ;
   }
 
   @Test
   public void paragraphIsEllipsisThenWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "...w0" ) ;
   }
 
   @Test
   public void paragraphIsEllipsisInsideBrackets() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "[...]" ) ;
   }
 
   @Test
   public void paragraphIsWordsAndPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "w1 w2, w3 w4." ) ;
   }
 
   @Test
   public void paragraphIsParenthesizedWordsWithApostropheInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0'w1)" ) ;
   }
 
   @Test
   public void paragraphIsParenthesizedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0,w1)" ) ;
   }
 
   @Test
   public void paragraphIsEmphasizedWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0'w1\"" ) ;
   }
 
   @Test
   public void paragraphIsQuotedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0,w1\"" ) ;
   }
 
   @Test
   public void paragraphIsInterpolatedWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "--w0'w1--" );
   }
 
   @Test
   public void paragraphIsInterpolatedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "--w0,w1--" );
   }
   
   @Test @Ignore
   public void paragraphHasLineBreaksInsideParenthensis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(  
         "(" + BREAK +
         "x" + BREAK +
         ")",
         tree( 
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_PARENTHESIS,
                 tree( WORD_, "x")
             )
         )
     ) ;
   }
 
   @Test
   public void paragraphIsQuoteOfOneWordThenParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "\"w0(w1)\"", tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_DOUBLE_QUOTES,
                 tree( WORD_, "w0" ),
                 tree( BLOCK_INSIDE_PARENTHESIS, tree( WORD_, "w1" ) )
             )
         ) ) ;
   }
 
   @Test
   public void paragraphIsQuoteOfOneWordThenSpaceParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "\"w0 (w1)\"", tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_DOUBLE_QUOTES,
                 tree( WORD_, "w0" ),
                 tree( BLOCK_INSIDE_PARENTHESIS, tree( WORD_, "w1" ) )
             )
         ) ) ;
   }
   
 
   
   @Test @Ignore
   public void paragraphIsTextThenImage() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
       "blah" + BREAK +
       "./foo.jpg",
         tree(
           PARAGRAPH_REGULAR,
           tree( WORD_, "blah"),
           tree( RASTER_IMAGE, tree( "./foo.jpg" ) )
         )        
     ) ;
   }
   
   @Test @Ignore
   public void paragraphIsTextThenImageThenText() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
       "w" + BREAK +
       "./foo.jpg" + BREAK +
       "x" + BREAK,      
         tree(
           PARAGRAPH_REGULAR,
           tree( WORD_, "w"),
           tree( RASTER_IMAGE, tree( "./foo.jpg" ) ),
           tree( WORD_, "x")
         )        
     ) ;
   }
   
   @Test @Ignore
   public void paragraphIsImageThenText() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
       "./foo.jpg" + BREAK +
       "x" + BREAK,      
         tree(
           PARAGRAPH_REGULAR,
           tree( RASTER_IMAGE, tree( "./foo.jpg" ) ),
           tree( WORD_, "x")
         )        
     ) ;
   }
   
   
   @Test
   public void bigListItemContainsUrl() throws RecognitionException {
     PARSERMETHOD_BIG_DASHED_LIST_ITEM.checkTreeAfterSeparatorRemoval(  
         "--- w" + BREAK +
         "http://novelang.sf.net"
         ,
         tree(
             PARAGRAPH_AS_LIST_ITEM_WITH_TRIPLE_HYPHEN_,
             tree( WORD_, "w" ),
             tree( URL_LITERAL, "http://novelang.sf.net" )
         )
     );
   }
 
   @Test
   public void bigListItemHasTag() throws RecognitionException {
     PARSERMETHOD_BIG_DASHED_LIST_ITEM.checkTreeAfterSeparatorRemoval(
         "@foo" + BREAK +
         "--- w",
         tree(
             PARAGRAPH_AS_LIST_ITEM_WITH_TRIPLE_HYPHEN_,
             tree( TAG, "foo" ),
             tree( WORD_, "w" )
             )
     );
   }
 
 
 
 
   @Test
   public void paragraphBodyHasThreeWordsOnThreeLinesAndFullStopAtEndOfFirstLine()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "w0." + BREAK +
         "w1" + BREAK +
         "w2" 
     );
   }
 
   @Test
   public void paragraphBodyIsJustEmphasizedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "//w0//", tree(
             NodeKind.PARAGRAPH_REGULAR,
         tree( BLOCK_INSIDE_SOLIDUS_PAIRS, tree( WORD_, "w0" ) ) )
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustParenthesizedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "(w0)", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( BLOCK_INSIDE_PARENTHESIS, tree( WORD_, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustQuotedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "\"w0\"", tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( BLOCK_INSIDE_DOUBLE_QUOTES, tree( WORD_, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustInterpolatedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "-- w0 --", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( BLOCK_INSIDE_HYPHEN_PAIRS, tree( WORD_, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustInterpolatedWordWithSilentEnd() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "-- w0 -_", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( BLOCK_INSIDE_TWO_HYPHENS_THEN_HYPHEN_LOW_LINE, tree( WORD_, "w0" ) )
         ) 
     );
   }
 
   @Test
   public void paragraphBodyIsJustBracketedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval( 
         "[w0]", 
         tree(
             NodeKind.PARAGRAPH_REGULAR,
             tree( BLOCK_INSIDE_SQUARE_BRACKETS, tree( WORD_, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyHasQuotesAndWordAndSpaceAndQuotes()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0\"w2 \"w3\"" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle1()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w00\" w01 w02 \" w03 w04 ! \"." );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle2()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "w10 \"w11\" \"w12\", \"w13\"" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle3()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w20 w21... w22\" !" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndParenthesisAndPunctuationSignsAndWordsInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"p00 (w01) w02.\" w04 (w05 \"w06 (w07)\".)." );
   }
 
   @Test
   public void
   paragraphBodyHasQuotesAndParenthesisAndBracketsAndPunctuationSignsAndWordsInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"p00 (w01) w02.\"w04(w05 \"[w06] (w07)\".)." );
   }
 
   @Test
   public void paragraphBodyHasWordThenInterpolatedClauseThenFullStop()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "p10 -- w11 w12 --." );
   }
 
   @Test
   public void paragraphBodyHasWordThenInterpolatedClauseSilentEndThenFullStop()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "p20 -- w21 w22 -_." );
   }
 
   @Test
   public void paragraphBodyIsQuoteWithWordThenParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingQuoteAndParenthesisAndEmphasis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1 //w2//)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingQuoteAndParenthesisAndEmphasisAndParenthesisAgain()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1 //w2 (w3)//)\"" );
   }
 
   @Test
   public void
   paragraphBodyIsNestingQuoteAndParenthesisAndInterpolatedClauseAndParenthesisAgainAndBrackets()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"(w0 -- w1 (w2 [w3]) --)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingEmphasisAndParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0 (w1)//." );
   }
 
   @Test
   public void paragraphBodyIsNestingEmphasisAndParenthesisAndQuotesAndHasQuestionMarkAtTheEnd()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0 (w1, \"w2\")// ?" );
   }
 
   @Test
   public void paragraphBodyIsParenthesisWithWordThenExclamationMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 !)" );
   }
 
   @Test
   public void paragraphBodyIsParenthesisWithWordAndQuotesAndEllipsisInside()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 \"w1\"...)" );
   }
 
   @Test
   public void
   paragraphBodyHasNestingParenthesisAndQuoteEmphasisThenSemiColonAndWordAndExclamationMark()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 \"w1 //w2//\") : w3 !" );
   }
 
   @Test
   public void
   paragraphBodyHasQuoteThenParenthesisThenEmphasisThenInterpolatedClauseThenBracketsNoSpace()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w00\"(w01)//w02//--w03--[w04]" );
   }
 
   @Test
   public void
   paragraphBodyIsNestingEmphasisAndParenthesisAndInterpolatedClauseAndQuotesOnSeveralLines()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w1" + BREAK +
         "(w2 " + BREAK +
         "-- w3  " + BREAK +
         "\"w4 " + BREAK +
         "w5\"--)//." 
     );
   }
  
  @Test
  public void paragraphIsBlockAfterTildeInsideBlockInsideDoubleQuotes() {
    PARSERMETHOD_PARAGRAPH.createTree( "\"~x\"" ) ;
  }
 
   @Test
   public void
   paragraphIsEmphasisWithWordThenUrlThenWordOnSeveralLines()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
         "//y" + BREAK +
         "http://foo.net " + BREAK +
         "z//",
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_SOLIDUS_PAIRS,
                 tree( WORD_, "y" ),
                 tree( URL_LITERAL, "http://foo.net" ),
                 tree( WORD_, "z" )
             )
         )
     ) ;
   }
 
   @Test
   public void
   paragraphIsEmphasisWithWordThenUrlThenWordOnSeveralLinesAndLineBreakAtEnd()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
         "//y" + BREAK +
         "http://foo.net " + BREAK +
         "z " + BREAK +
         "//",
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_SOLIDUS_PAIRS,
                 tree( WORD_, "y" ),
                 tree( URL_LITERAL, "http://foo.net" ),
                 tree( WORD_, "z" )
             )
         )
     ) ;
   }
 
   @Test
   public void
   parenthesizedUrlWithNameThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.checkTreeAfterSeparatorRemoval(
         "(\"y\"" + BREAK +
         "http://novelang.sf.net " + BREAK +
         "z)",
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_PARENTHESIS,
                 tree( BLOCK_INSIDE_DOUBLE_QUOTES, tree( WORD_, "y" ) ),
                 tree( URL_LITERAL, "http://novelang.sf.net" ),
                 tree( WORD_, "z" )
             )
         )
     ) ;
   }
 
 
   /**
    * Do we need to support this one?
    */
   @Test @Ignore 
   public void paragraphIsParenthesisWithBreakThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree(
         "(" + BREAK +
         "w)"
     ) ;
   }
 
   @Test
   public void paragraphIsWordThenParenthesisThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w1(w2)w3)" ) ;
   }
 
   @Test
   public void paragraphIsWordThenParenthesisThenWordWithTrailingSpace()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w1(w2)w3 )" ) ;
   }
 
   @Test
   public void paragraphIsWordThenParenthesis()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w1(w2))" ) ;
   }
 
   @Test
   public void paragraphHasParenthesisAndDoubleQuotedTextOnTwoLines()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree(
         "(x y) z" + BREAK +
         "1 2 \"3 " + BREAK +
         "4\""
     ) ;
   }
 
   @Test
   public void paragraphIsJustAUrl()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "http://foo.com" ) ;
   }
 
   @Test
   public void paragraphIsTwoUrls()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree(
         "http://foo.com " + BREAK +
         "http://bar.com"
     ) ;
   }
 
 
   @Test
   public void paragraphIsUrlsInsideBlockOfSolidusPairs()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree(
         "//w" + BREAK +
         "http://bar.com" + BREAK +
         "//"
     ) ;
   }
 
 
   @Test
   public void paragraphHasSoftInlineLiteral() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "x  `y + 1`" ) ;
   }
 
   @Test
   public void paragraphIsDoubleQuotesWithEndingPeriodInside() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w.\"" ) ;
   }
 
   @Test
   public void paragraphIsDoubleHyphenWithCommaInside() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "-- w, x --" ) ;
   }
 
   @Test
   public void paragraphIsDoubleSolidusWithPeriodInside() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "//w.//" ) ;
   }
 
   @Test
   public void paragraphIsDoubleSolidusWithPeriodThenWhitespaceInside() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "//w. //" ) ;
   }
 
   @Test
   public void paragraphHasBlockAfterTildeThatShouldNotBeGreedy() {
     PARSERMETHOD_PARAGRAPH.checkTree(
         "~w`/`x y",
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_AFTER_TILDE,
                 tree(
                     SUBBLOCK,
                     tree( WORD_, "w" ),
                     tree( BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS, "/" ),
                     tree( WORD_, "x" )
                 )
             ),
             tree( WHITESPACE_, " " ),
             tree( WORD_, "y" )
         )
     ) ;
   }
 
 
 
 
 // =======
 // Fixture
 // =======
 
   private static final Log LOG = LogFactory.getLog( ParagraphParsingTest.class ) ;
   private static final ParserMethod PARSERMETHOD_WORD =
       new ParserMethod( "word" ) ;
   private static final ParserMethod PARSERMETHOD_PARAGRAPH =
       new ParserMethod( "paragraph" ) ;
   private static final ParserMethod PARSERMETHOD_BIG_DASHED_LIST_ITEM =
       new ParserMethod( "bigDashedListItem" ) ;
 
   private static final SyntacticTree TREE_APOSTROPHE_WORDMATE = tree( APOSTROPHE_WORDMATE, "'" ) ;
 
   private static final SyntacticTree TREE_SIGN_EXCLAMATION_MARK =
       tree( PUNCTUATION_SIGN, tree( SIGN_EXCLAMATIONMARK, "!" ) ) ;
 
   private static final SyntacticTree TREE_SIGN_SEMICOLON =
       tree( PUNCTUATION_SIGN, tree( SIGN_SEMICOLON, ";" ) );
 
   private static final SyntacticTree TREE_SIGN_FULLSTOP =
       tree( PUNCTUATION_SIGN, tree( SIGN_FULLSTOP, "." ) ) ;
 
   private static final SyntacticTree TREE_SIGN_QUESTIONMARK =
       tree( PUNCTUATION_SIGN, tree( SIGN_QUESTIONMARK, "?" ) );
   private static final SyntacticTree TREE_SIGN_COLON =
       tree( PUNCTUATION_SIGN, tree( SIGN_COLON, ":" ) ) ;
 
   private static final SyntacticTree TREE_SIGN_ELLIPSIS =
       tree( PUNCTUATION_SIGN, tree( SIGN_ELLIPSIS, "..." ) );
 
   private static final ParserMethod PARSERMETHOD_BLOCK_AFTER_TILDE =
       new ParserMethod( "blockAfterTilde" ) ;
 
 
 }
