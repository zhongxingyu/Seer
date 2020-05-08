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
 package org.novelang.parser.antlr.delimited;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.ImmutableList;
 
 /**
  * This enum represents blocks (subset of a paragraph-like piece of source document) which
  * have a start and end delimiter and which must be paired.
  * This is more or less a duplicate of what's defined in ANTLR grammar but there is no (simple?)
  * mean to extract literal corresponding to start and end tokens, so we hope that all unit tests
  * will detect any desynchronization.
  *
  * <p>
  * Definition : <em>twin delimiters</em> are start and end delimiters for a kind of block, and
 * those two delimiters cannot be confused.
  * <p>
 * Definition : <em>only delimiters</em> are delimiters which are the same for the beginning
 * and the end of the block.
  *
  * @see DefaultBlockDelimitersBoundary
  *
  * @author Laurent Caillette
  */
 public enum BlockDelimiter {
 
  PARENTHESIS( true, "(", ")" ),
  SQUARE_BRACKETS( true, "[", "]" ),
  DOUBLE_QUOTES( false, "\"", "\"" ),
  SOLIDUS_PAIRS( false, "//", "//" ),
  ASTERISK_PAIRS( false, "**", "**" ),
  TWO_HYPHENS( false, "--", "--", "-_" ),
   ;
 
   private final boolean twin ;
   private final String start ;
   private final String[] end ;
 
   private BlockDelimiter( final boolean twin, final String start, final String... end ) {
     this.twin = twin ;
     this.start = start ;
     this.end = end.clone() ;
   }
 
   public String getStart() {
     return start ;
   }
 
   public String[] getEnd() {
     return end.clone() ;
   }
 
   public boolean isTwin() {
     return twin ;
   }
 
   private static final Predicate< BlockDelimiter > IS_TWIN = new Predicate<BlockDelimiter>() {
     public boolean apply( final BlockDelimiter blockDelimiter ) {
       return blockDelimiter.isTwin() ;
     }
   } ;
   private static final Predicate< BlockDelimiter > IS_ONLY = Predicates.not( IS_TWIN ) ;
 
   public static Iterable< BlockDelimiter > getTwinDelimiters() {
     return Iterables.filter( ImmutableList.of( values() ), IS_TWIN ) ;
   }
 
  public static Iterable< BlockDelimiter > getOnlyDelimiters() {
     return Iterables.filter( ImmutableList.of( values() ), IS_ONLY ) ;
   }
 }
