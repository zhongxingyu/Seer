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
 
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import org.junit.Test;
 import antlr.RecognitionException;
 import static novelang.parser.NodeKind.*;
 import static novelang.parser.antlr.TreeFixture.tree;
 import static novelang.parser.antlr.AntlrTestHelper.BREAK;
 
 /**
  * @author Laurent Caillette
  */
 public class TagParsingTest {
 
   @Test
   public void parseSingleTag() throws RecognitionException {
     PARSERMETHOD_TAG.checkTreeAfterSeparatorRemoval(
         "@foo-1",
         tree( TAG, "foo-1" )
 
     ) ;
   }
 
   @Test
   public void levelIntroducer() throws RecognitionException {
     PARSERMETHOD_PART.checkTreeAfterSeparatorRemoval(
         "@stuff-1 @stuff-2 " + BREAK +
         "  @stuff-3 " + BREAK +
         "==",
         tree(
             PART,
             tree(
                 LEVEL_INTRODUCER_,
                 tree( LEVEL_INTRODUCER_INDENT_, "==" ),
                 tree( TAG, "stuff-1" ),
                 tree( TAG, "stuff-2" ),
                 tree( TAG, "stuff-3" )
             )
 
         )
     ) ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private static final Log LOG = LogFactory.getLog( TagParsingTest.class ) ;
   private static final ParserMethod PARSERMETHOD_TAG = new ParserMethod( "tag" ) ;
   private static final ParserMethod PARSERMETHOD_PART = new ParserMethod( "part" ) ;
 
 }
