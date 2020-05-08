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
 
 import org.junit.Test;
 import org.junit.Ignore;
 import org.antlr.runtime.RecognitionException;
 import static novelang.parser.antlr.TreeFixture.tree;
 import static novelang.parser.antlr.AntlrTestHelper.BREAK;
 import static novelang.parser.NodeKind.*;
 
 /**
  * Tests for embedded list parsing.
  *
  * @author Laurent Caillette
  */
 public class EmbeddedListParsingTest {
   
   @Test
   public void embeddedListItemMinimum() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree(
         "- w",
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 EMBEDDED_LIST_ITEM_WITH_HYPHEN_,
                 tree( WHITESPACE_, " " ),
                 tree( WORD_, "w" )
             )
         )
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
 
   /**
    * Was a bug.
    */
   @Test
   public void embeddedListItemApostropheAndDot() throws RecognitionException {
     PARSERMETHOD_PART.checkTree(
         "- y'z.",
         tree(
             PART,
             tree( PARAGRAPH_REGULAR,
                 tree(
                     EMBEDDED_LIST_ITEM_WITH_HYPHEN_,
                     tree( WHITESPACE_, " " ),
                     tree( WORD_, "y" ),
                     tree( APOSTROPHE_WORDMATE, "'" ),
                     tree( WORD_, "z" ),
                     tree( PUNCTUATION_SIGN, tree( SIGN_FULLSTOP, "." ) )
                 )
             )
         )
     ) ;
   }
 
   @Test @Ignore
   public void embeddedListItemInsideParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree(
         "(" + BREAK +
         "- w" + BREAK +
         "- x" + BREAK +
         ")"
         ,
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 BLOCK_INSIDE_PARENTHESIS,
                 tree( EMBEDDED_LIST_ITEM_WITH_HYPHEN_, tree( WORD_, "w" ) ),
                 tree( EMBEDDED_LIST_ITEM_WITH_HYPHEN_, tree( WORD_, "x" ) )
             )
         )
     ); ;
   }
 
   @Test
   public void severalEmbeddedListItems() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree(
         "- w1" + BREAK +
         "  - w2" + BREAK,
         tree(
             PARAGRAPH_REGULAR,
             tree(
                 EMBEDDED_LIST_ITEM_WITH_HYPHEN_,
                 tree( WHITESPACE_, " " ),
                 tree( WORD_, "w1" )
             ),
             tree( LINE_BREAK_ ),
             tree( WHITESPACE_, "  " ),
             tree(
                 EMBEDDED_LIST_ITEM_WITH_HYPHEN_,
                 tree( WHITESPACE_, " " ),
                 tree( WORD_, "w2" )
             )
         )
     ) ;
   }
 
 
   @Test
   public void smallDashedListItemIsSingleWord() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree( "- x" ) ;
   }
 
   @Test
   public void smallDashedListItemIsSeveralWords() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.checkTree(
         "- x y z",
         tree(
             EMBEDDED_LIST_ITEM_WITH_HYPHEN_,
             tree( WHITESPACE_, " " ),
             tree( WORD_, "x" ),
             tree( WHITESPACE_, " " ),
             tree( WORD_, "y" ),
             tree( WHITESPACE_, " " ),
             tree( WORD_, "z" )
         )
 
     ) ;
   }
 
   @Test
   public void smallDashedListItemHasParenthesisAndDoubleQuotes() throws RecognitionException {
     PARSERMETHOD_SMALL_DASHED_LIST_ITEM.createTree( "- x (\"y \") z" ) ;
   }
 
 // =======
 // Fixture
 // =======
 
   private static final ParserMethod PARSERMETHOD_PARAGRAPH =
       new ParserMethod( "paragraph" ) ;
   private static final ParserMethod PARSERMETHOD_PART =
       new ParserMethod( "part" ) ;
   private final ParserMethod PARSERMETHOD_SMALL_DASHED_LIST_ITEM =
       new ParserMethod( "smallDashedListItem" ) ;
 
 
 }
