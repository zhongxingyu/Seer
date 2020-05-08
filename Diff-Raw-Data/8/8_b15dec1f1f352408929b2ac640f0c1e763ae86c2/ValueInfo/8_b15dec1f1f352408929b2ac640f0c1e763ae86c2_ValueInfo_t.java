 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import org.melati.poem.generated.ValueInfoBase;
 
 /**
  * Abstract persistent generated from Poem.dsd
  * and extended to cover {@link Setting} and {@link ColumnInfo}.
  *
  * Melati POEM generated, programmer modifiable stub 
  * for a <code>Persistent</code> <code>ValueInfo</code> object.
  * 
  * 
  * <table> 
  * <tr><th colspan='3'>
  * Field summary for SQL table <code>ValueInfo</code>
  * </th></tr>
  * <tr><th>Name</th><th>Type</th><th>Description</th></tr>
  * <tr><td> displayname </td><td> String </td><td> A user-friendly name for 
  * the field </td></tr> 
  * <tr><td> description </td><td> String </td><td> A brief description of the 
  * field's function </td></tr> 
  * <tr><td> usereditable </td><td> Boolean </td><td> Whether it makes sense 
  * for the user to update the field's value </td></tr> 
  * <tr><td> typefactory </td><td> PoemTypeFactory </td><td> The field's 
  * Melati type </td></tr> 
  * <tr><td> nullable </td><td> Boolean </td><td> Whether the field can be 
  * empty </td></tr> 
  * <tr><td> size </td><td> Integer </td><td> For character fields, the 
  * maximum number of characters that can be stored, (-1 for unlimited) 
  * </td></tr> 
  * <tr><td> width </td><td> Integer </td><td> A sensible width for text boxes 
  * used for entering the field, where appropriate </td></tr> 
  * <tr><td> height </td><td> Integer </td><td> A sensible height for text 
  * boxes used for entering the field, where appropriate </td></tr> 
  * <tr><td> precision </td><td> Integer </td><td> Precision (total number of 
  * digits) for fixed-point numbers </td></tr> 
  * <tr><td> scale </td><td> Integer </td><td> Scale (number of digits after 
  * the decimal) for fixed-point numbers </td></tr> 
  * <tr><td> renderinfo </td><td> String </td><td> The name of the Melati 
  * templet (if not the default) to use for input controls for the field 
  * </td></tr> 
  * <tr><td> rangelow_string </td><td> String </td><td> The low end of the 
  * range of permissible values for the field </td></tr> 
  * <tr><td> rangelimit_string </td><td> String </td><td> The (exclusive) 
  * limit of the range of permissible values for the field </td></tr> 
  * </table> 
  * 
  * @generator org.melati.poem.prepro.TableDef#generateMainJava 
  */
 public class ValueInfo extends ValueInfoBase {
 
  /**
   * Constructor 
   * for a <code>Persistent</code> <code>ValueInfo</code> object.
   * 
   * @generator org.melati.poem.prepro.TableDef#generateMainJava 
   */
   public ValueInfo() { }
 
   // programmer's domain-specific code here
 
   /**
    * @return a Type Parameter based upon our size and nullability 
    */
   public PoemTypeFactory.Parameter toTypeParameter() {
     final Boolean nullableL = getNullable_unsafe();
     final Integer sizeL = getSize_unsafe();
     return
         new PoemTypeFactory.Parameter() {
           public boolean getNullable() {
             return nullableL == null || nullableL.booleanValue();
           }
 
           public int getSize() {
             return sizeL == null ? -1 : sizeL.intValue();
           }
         };
   }
 
   private SQLPoemType poemType = null;
 
   /**
    * @return our SQLPoemType
    */
   public SQLPoemType getType() {
     if (poemType == null) {
       PoemTypeFactory f = getTypefactory();
 
       if (f == null) {
         // this can happen before we have been fully initialised
         // it's convenient to return the "most general" type available ...
         return StringPoemType.nullableInstance;
       }
 
       poemType = f.typeOf(getDatabase(), toTypeParameter());
     }
 
     return poemType;
   }
 
   /**
    * @param nullableP
    * @return
    */
   private SQLPoemType getRangeEndType(boolean nullableP) {
     // FIXME a little inefficient, but rarely used; also relies on BasePoemType
     // should have interface for ranged type ...
 
     SQLPoemType t = getType();
 
     if (t instanceof BasePoemType) {
       BasePoemType unrangedType = (BasePoemType)((BasePoemType)t).clone();
       unrangedType.setRawRange(null, null);
       return (SQLPoemType)unrangedType.withNullable(nullableP);
     }
     else
       return null;
   }
 
   private FieldAttributes fieldAttributesRenamedAs(FieldAttributes c,
                                                    PoemType type) {
     return new BaseFieldAttributes(
         c.getName(), c.getDisplayName(), c.getDescription(), type,
         width == null ? 12 : width.intValue(),
         height == null ? 1 : height.intValue(),
         renderinfo,
         false, // indexed
         usereditable == null ? true : usereditable.booleanValue(),
         true // usercreateable
     );
   }
 
   /**
    * @param c a FieldAttributes eg a Field
    * @return a new FieldAttributes
    */
   public FieldAttributes fieldAttributesRenamedAs(FieldAttributes c) {
     return fieldAttributesRenamedAs(c, getType());
   }
 
   /**
    * @param c a Column with a Range
    * @return a Field representing the end of the Range
    */
   private Field rangeEndField(Column c) {
     SQLPoemType unrangedType = getRangeEndType(c.getType().getNullable());
 
     if (unrangedType == null)
       return null;
     else {
       Object raw;
       try {
         raw = unrangedType.rawOfString((String)c.getRaw_unsafe(this));
       }
       catch (Exception e) {
         c.setRaw_unsafe(this, null);
         raw = null;
        throw new AppBugPoemException("Found a bad entry for " + c + " in " +
                getTable().getName() + "/" + troid() + ": " +
                "solution is to null it out ...", e);
       }
 
       return new Field(
           raw,
           fieldAttributesRenamedAs(c, unrangedType));
     }
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.generated.ValueInfoBase#getRangelow_stringField()
    */
   public Field getRangelow_stringField() {
     Field it = rangeEndField(getValueInfoTable().getRangelow_stringColumn());
     return it != null ? it : super.getRangelow_stringField();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.generated.ValueInfoBase#getRangelimit_stringField()
    */
   public Field getRangelimit_stringField() {
     Field it = rangeEndField(getValueInfoTable().getRangelimit_stringColumn());
     return it != null ? it : super.getRangelimit_stringField();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.generated.ValueInfoBase#setRangelow_string(java.lang.String)
    */
   public void setRangelow_string(String value) {
     boolean nullableL = getValueInfoTable().getRangelow_stringColumn().
                         getType().getNullable();
     PoemType unrangedType = getRangeEndType(nullableL);
     if (unrangedType != null)
       value = unrangedType.stringOfRaw(unrangedType.rawOfString(value));
     super.setRangelow_string(value);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.generated.ValueInfoBase#setRangelimit_string(java.lang.String)
    */
   public void setRangelimit_string(String value) {
     boolean nullableL = getValueInfoTable().getRangelimit_stringColumn().
                         getType().getNullable();
     PoemType unrangedType = getRangeEndType(nullableL);
     if (unrangedType != null)
       value = unrangedType.stringOfRaw(unrangedType.rawOfString(value));
     super.setRangelimit_string(value);
   }
 }
