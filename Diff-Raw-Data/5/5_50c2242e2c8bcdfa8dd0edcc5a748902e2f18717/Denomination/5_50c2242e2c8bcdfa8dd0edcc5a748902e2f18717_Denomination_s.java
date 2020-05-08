 package com.github.croesch.types;
 
 import com.github.croesch.i18n.Text;
 
 /**
  * Represents the types of denomiation.
  * 
  * @author croesch
  * @since Date: Jun 16, 2011 9:19:09 PM
  */
 public enum Denomination {
 
   /** evangelic / protestant */
   EVANGELIC (Text.EVANGELIC),
 
   /** catholic */
   CATHOLIC (Text.CATHOLIC),
 
   /** orthodox */
   ORTHODOX (Text.ORTHODOX),
 
   /** muslim */
   MUSLIM (Text.MUSLIM),
 
   /** free church */
   FREE_CHURCH (Text.FREE_CHURCH),
 
   /** jewish */
   JEWISH (Text.JEWISH),
 
   /** other denomination */
  OTHER (Text.OTHER);
 
   /** the i18n representation of this object */
   private final String s;
 
   /**
    * Constructs a {@link Denomination} with the given i18n representation of the specific object.
    * 
    * @author croesch
    * @since Date: Jun 21, 2011
    * @param t the {@link Text} that represents this object.
    */
   private Denomination(final Text t) {
     this.s = t.text();
   }
 
   @Override
   public String toString() {
     return this.s;
   }
 
 }
