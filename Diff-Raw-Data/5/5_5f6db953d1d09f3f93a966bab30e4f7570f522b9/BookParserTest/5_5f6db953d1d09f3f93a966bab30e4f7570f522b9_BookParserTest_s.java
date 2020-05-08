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
 
 import static novelang.parser.NodeKind.*;
 import static novelang.parser.antlr.TreeFixture.tree;
 import static novelang.parser.antlr.AntlrTestHelper.BREAK;
 
 import org.junit.Test;
 
 /**
  * Tests for Book parsing (with functions).
  * 
  * @author Laurent Caillette
  */
 public class BookParserTest {
   
   @Test
   public void book() {
     PARSERMETHOD_BOOK.checkTreeAfterSeparatorRemoval(
         BREAK + BREAK +
         " insert file:x/y/z createlevel " + BREAK +
         "   style=whatever" + BREAK +
         BREAK +
         " mapstylesheet a=b c=d " + BREAK +
         "   e=f  " + BREAK +
         BREAK +
         " insert file:uvw.nlp " + BREAK         
         ,
         tree(
             BOOK,
             tree(
                 COMMAND_INSERT_,
                 tree( URL_LITERAL, "file:x/y/z" ),
                 tree( COMMAND_INSERT_CREATELEVEL_ ),
                 tree( COMMAND_INSERT_STYLE_, "whatever" )
             ),
             tree(
                 COMMAND_MAPSTYLESHEET_,
                 tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "a" ), tree( "b" ) ),
                 tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "c" ), tree( "d" ) ),
                 tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "e" ), tree( "f" ) )
             ),
             tree(
                 COMMAND_INSERT_,
                 tree( URL_LITERAL, "file:uvw.nlp" )
             )
             
         )
     ) ;
     
   }
   
 // ======  
 // insert  
 // ======  
 
   @Test
   public void insertFunctionCall() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:.",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithRecurse() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. recurse",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMMAND_INSERT_RECURSE_ )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithSort() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. sort=path-",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMMAND_INSERT_SORT_, "path-" )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithCreateLevel() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. createlevel",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMMAND_INSERT_CREATELEVEL_ )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithStyle() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. style=whatever",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMMAND_INSERT_STYLE_, "whatever" )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithOneFragmentIdentifier() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. \\\\y\\z",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMPOSITE_IDENTIFIER, tree( "y" ), tree( "z" ) )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithTwoFragmentIdentifiers() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:. \\\\w\\x \\\\y\\z",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:." ),
             tree( COMPOSITE_IDENTIFIER, tree( "w" ), tree( "x" ) ),
             tree( COMPOSITE_IDENTIFIER, tree( "y" ), tree( "z" ) )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallWithEverything() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkTreeAfterSeparatorRemoval(  
         "insert file:x/y/z.nlp recurse sort=version- createlevel style=whatever " +
        "\\\\w\\x \\\\y\\z",
         tree(
             COMMAND_INSERT_,
             tree( URL_LITERAL, "file:x/y/z.nlp" ),
             tree( COMMAND_INSERT_RECURSE_ ),
             tree( COMMAND_INSERT_SORT_, "version-" ),
             tree( COMMAND_INSERT_CREATELEVEL_ ),
             tree( COMMAND_INSERT_STYLE_, "whatever" ),
            tree( COMPOSITE_IDENTIFIER, tree( "w" ), tree( "x" ) ),
             tree( COMPOSITE_IDENTIFIER, tree( "y" ), tree( "z" ) )
         )    
     ) ;
   }
   
   @Test
   public void insertFunctionCallRequiresUrl() {
     PARSERMETHOD_FUNCTIONCALL_INSERT.checkFails(  
         "insert $recurse"
     ) ;
   }
   
 
 // =============  
 // mapstylesheet  
 // =============
   
   @Test
   public void mapstylesheetFunctionCallOneStylesheet() {
     PARSERMETHOD_FUNCTIONCALL_MAPSTYLESHEET.checkTreeAfterSeparatorRemoval(  
         "mapstylesheet abc=w.xyz",
         tree(
             COMMAND_MAPSTYLESHEET_,
             tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "abc" ), tree( "w.xyz" ) )
         )    
     ) ;
   }
   
   @Test
   public void mapstylesheetFunctionCallTwoStylesheets() {
     PARSERMETHOD_FUNCTIONCALL_MAPSTYLESHEET.checkTreeAfterSeparatorRemoval(  
         "mapstylesheet abc=w.xyz  123=456",
         tree(
             COMMAND_MAPSTYLESHEET_,
             tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "abc" ), tree( "w.xyz" ) ),
             tree( COMMAND_MAPSTYLESHEET_ASSIGNMENT_, tree( "123" ), tree( "456" ) )
         )    
     ) ;
   }
   
   
   
   
 // =======  
 // Fixture
 // =======
   
 
   private static final ParserMethod PARSERMETHOD_FUNCTIONCALL_INSERT = 
       new ParserMethod( "functionCallInsert" ) ;
   
   private static final ParserMethod PARSERMETHOD_FUNCTIONCALL_MAPSTYLESHEET =
       new ParserMethod( "functionCallMapstylesheet" ) ;
   
   private static final ParserMethod PARSERMETHOD_BOOK = new ParserMethod( "book" ) ;
   
   
   
 }
