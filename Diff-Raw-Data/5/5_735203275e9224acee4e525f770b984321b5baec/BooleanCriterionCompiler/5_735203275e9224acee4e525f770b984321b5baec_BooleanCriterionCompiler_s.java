 /**
  * Copyright 19.3.11 (c) DataLite, spol. s r.o. All rights reserved.
  * Web: http://www.datalite.cz    Mail: info@datalite.cz
  */
 package cz.datalite.zk.components.list.filter.compilers;
 
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 
 /**
  * Default implementation for boolean compiler.
  *
  * For boolean values it behavies as expected.
  * For String values (from QuickFilter):
  *      for a, ano, y, yes, true it compiles true condition
  *      for n, ne, no, false it compiles false condition
  * @author Jiri Bubnik <bubnik at datalite cz>
  */
 public class BooleanCriterionCompiler extends FilterCriterionCompiler  {
 
     @Override
     protected Criterion compileOperatorEqual( final String key, final Object... values ) {
         final Object value = values[0];
         if ( value != null && value instanceof String ) { // input from Quick Filter
             final String val = ( String ) value;
             if ( val.equalsIgnoreCase("a") || val.equalsIgnoreCase("ano") ||
                  val.equalsIgnoreCase("y") || val.equalsIgnoreCase("yes") ||
                  val.equalsIgnoreCase("true")) {
                 return compile( key, true );
             } else if ( val.equalsIgnoreCase("n") || val.equalsIgnoreCase("ne") ||
                  val.equalsIgnoreCase("no") ||
                  val.equalsIgnoreCase("false")) {
                return Restrictions.sqlRestriction( "1=1" );
             }
             else {
                return Restrictions.sqlRestriction( "1=0" );
             }
         } else if ( (value instanceof Boolean) || (Boolean.TYPE.equals( value.getClass() )) ) { // input from checkbox
             return compile( key, ( Boolean ) value );
         } else {
             return Restrictions.sqlRestriction( "1=0" );
         }
     }
 
     @Override
     protected Criterion compileOperatorNotEqual( final String key, final Object... values ) {
         return compile( key, !( Boolean ) values[0] );
     }
 
     protected Criterion compile( final String key, final boolean value ) {
         return Restrictions.eq( key, value );
 
 //        Special class needs to be created if mapped by character
 //        if ( value ) {
 //            return Restrictions.or( Restrictions.eq( key, "Y"), Restrictions.eq( key, "A"));
 //        } else {
 //            return Restrictions.not ( Restrictions.or( Restrictions.eq( key, "Y"), Restrictions.eq( key, "A")) );
 //        }
     }
 
 
     /**
      * Boolena compiler can work with boolean value or a String (for quick filter).
      *
      * @param value the value for filter
      * @return true if null, String or boolean
      */
     public boolean validateValue(Object value) {
         if (value == null)
             return true;
         else if (value instanceof String)
             return true;
         else if ( (value instanceof Boolean) || (Boolean.TYPE.equals( value.getClass() )) )
             return true;
         else
             return false;
     }
 }
