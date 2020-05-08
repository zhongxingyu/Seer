 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.equalizer;
 
 /**
  * Simple bean class to hold category names and their values. The name is immutable.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0
  */
 public final class EqualizerCategory implements Cloneable {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient String name;
 
     private int value;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EqualizerCategory object using the given name. Value defaults to <code>0</code>
      *
      * @param  name  the name of the category
      *
      * @see    #EqualizerCategory(java.lang.String, int)
      */
     public EqualizerCategory(final String name) {
         this(name, 0);
     }
 
     /**
      * Creates a new EqualizerCategory object using the given name and value.
      *
      * @param   name   the name of the category
      * @param   value  the value of the category
      *
      * @throws  IllegalArgumentException  if the give name is <code>null</code> or empty
      */
     public EqualizerCategory(final String name, final int value) {
         if ((name == null) || name.isEmpty()) {
             throw new IllegalArgumentException("name must not be null or empty"); // NOI18N
         }
 
         this.name = name;
         this.value = value;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Gets the category name.
      *
      * @return  the category name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Gets the value.
      *
      * @return  the current value
      */
     public int getValue() {
         return value;
     }
 
     /**
      * Sets the value.
      *
      * @param  value  the new value
      */
     public void setValue(final int value) {
         this.value = value;
     }
 
     @Override
     public Object clone() {
         try {
             // nothing else to do, only primitives, so it is already a "deep-clone"
             return super.clone();
         } catch (final CloneNotSupportedException ex) {
             // shouldn't happen at all
             throw new InternalError();
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
 
        hash += (this.name == null) ? 0 : 5 * hash +  this.name.hashCode();
        hash += 5 * hash + this.value;
 
         return hash;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final EqualizerCategory other = (EqualizerCategory)obj;
         if ((this.name == null) ? (other.name != null) : (!this.name.equals(other.name))) {
             return false;
         }
         if (this.value != other.value) {
             return false;
         }
 
         return true;
     }
 }
