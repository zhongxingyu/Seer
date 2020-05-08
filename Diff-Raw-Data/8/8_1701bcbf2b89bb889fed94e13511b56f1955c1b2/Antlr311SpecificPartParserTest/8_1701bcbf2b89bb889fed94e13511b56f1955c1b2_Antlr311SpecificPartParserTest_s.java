 /*
  * Copyright (C) 2008 Laurent Caillette
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
 import org.junit.Test;
 import static novelang.parser.antlr.AntlrTestHelper.BREAK;
 
 /**
  * Test new parser features.
  * 
  * @author Laurent Caillette
  */
 public class Antlr311SpecificPartParserTest {
   
   public static final String BREAK = "\n" ;
 
   private final ParserMethod PARSERMETHOD_PART =
       new ParserMethod( "part" ) ;
   private final ParserMethod PARSERMETHOD_SMALL_DASHED_LIST_ITEM =
       new ParserMethod( "smallDashedListItem" ) ;
   private final ParserMethod PARSERMETHOD_PARAGRAPH =
       new ParserMethod( "paragraph" ) ;
   private final ParserMethod PARSERMETHOD_TITLE =
       new ParserMethod( "title" ) ;
 
   @Test
   public void paragraphIsParenthesisWithBreakThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree(
         "(" + BREAK +
         "w)" 
     ) ;
   }
 
   @Test
   public void paragraphIsTwoSmallListItems()
       throws RecognitionException
   {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree(
         "- x" + BREAK +
         "- y"
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
   public void titleHasDoubleQuotesThenUrl()
       throws RecognitionException
   {
     PARSERMETHOD_TITLE.createTree( 
         "a" + BREAK +
         "http://bar.com"
     ) ;
   }
   
   @Test
   public void smallDashedListItemIsSingleWord() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree( "- x" ) ;
   }
   
   @Test
   public void smallDashedListItemIsSeveralWords() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree( "- x y z" ) ;
   }
   
   @Test
   public void smallDashedListItemHasParenthesisAndDoubleQuotes() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree( "- x (\"y \") z" ) ;
   }
   
   @Test
   public void
   paragraphBodyIsNestingEmphasisAndParenthesisAndInterpolatedClauseAndQuotesOnSeveralLines()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "(x " + BREAK +
         "y)" 
     );
   }
 
   @Test
   public void partIsBigDashedListItem() throws RecognitionException {
     PARSERMETHOD_PART.createTree( "--- w." ) ;
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
   public void paragraphIsBigListItemWithColumnAndSoftInlineLiteral() throws RecognitionException {
    PARSERMETHOD_PARAGRAPH.createTree( "--- w : `y`" ) ;
   }
 
   @Test
   public void paragraphIsBigListItemWithSoftInlineLiteral() throws RecognitionException {
    PARSERMETHOD_PARAGRAPH.createTree( "--- `y`" ) ;
   }
 
   @Test
   public void paragraphIsDoubleQuotedWordsWithApostropheAndPeriod() throws RecognitionException {
    PARSERMETHOD_PARAGRAPH.createTree( "\"x'y.\"" ) ;
   }
 
 
 
 
 
 }
