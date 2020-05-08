 package de.aidger.model.models;
 
 import java.math.BigDecimal;
 
 import de.aidger.model.AbstractModel;
 import de.unistuttgart.iste.se.adohive.model.IHourlyWage;
 
 /**
  * Represents a single entry in the hourly wage column of the database. Contains
  * functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 public class HourlyWage extends AbstractModel<IHourlyWage> implements
         IHourlyWage {
 
     /**
      * The qualification needed for the wage
      */
     private String qualification;
 
     /**
      * The month the wage is valid in.
      */
     private byte month;
 
     /**
      * The year the wage is valid in.
      */
     private short year;
 
     /**
      * The wage per hour.
      */
     private BigDecimal wage;
 
     /**
      * Initializes the HourlyWage class.
      */
     public HourlyWage() {
         validatePresenceOf(new String[] { "qualification", "wage" });
         // TODO: Validate month and year
     }
 
     /**
      * Initializes the HourlyWage class with the given hourly wage model.
      * 
      * @param h
      *            the hourly wage model
      */
     public HourlyWage(IHourlyWage h) {
         setId(h.getId());
         setMonth(h.getMonth());
         setQualification(h.getQualification());
         setWage(h.getWage());
         setYear(h.getYear());
     }
 
     /**
      * Clone the current wage
      */
     @Override
     public HourlyWage clone() {
         HourlyWage h = new HourlyWage();
         h.setId(id);
         h.setMonth(month);
         h.setQualification(qualification);
         h.setWage(wage);
         h.setYear(year);
         return h;
     }
 
     /**
      * Check if two objects are equal.
      * 
      * @param o
      *            The other object
      * @return True if both are equal
      */
     @Override
     public boolean equals(Object o) {
         if (o instanceof HourlyWage) {
             HourlyWage h = (HourlyWage) o;
            return h.id == id
                    && h.month == month
                     && h.year == year
                     && (qualification == null ? h.qualification == null
                             : h.qualification.equals(qualification))
                    && (wage == null ? h.wage == null : h.wage.equals(wage));
         } else {
             return false;
         }
     }
 
     /**
      * Generate a unique hashcode for this instance.
      * 
      * @return The hashcode
      */
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 37 * hash
                 + (qualification != null ? qualification.hashCode() : 0);
         hash = 37 * hash + month;
         hash = 37 * hash + year;
         hash = 37 * hash + (wage != null ? wage.hashCode() : 0);
         return hash;
     }
 
     /**
      * Get the month the wage is valid in.
      * 
      * @return The month the wage is valid in
      */
     @Override
     public byte getMonth() {
         return month;
     }
 
     /**
      * Get the qualification needed for the wage.
      * 
      * @return The qualification needed for the wage
      */
     @Override
     public String getQualification() {
         return qualification;
     }
 
     /**
      * Get the wage per hour.
      * 
      * @return The wage per hour
      */
     @Override
     public BigDecimal getWage() {
         return wage;
     }
 
     /**
      * Get the year the wage is valid in.
      * 
      * @return The year the wage is valid in
      */
     @Override
     public short getYear() {
         return year;
     }
 
     /**
      * Set the month the wage is valid in.
      * 
      * @param month
      *            The month the wage is valid in
      */
     @Override
     public void setMonth(byte month) {
         this.month = month;
     }
 
     /**
      * Set the qualification needed for the wage.
      * 
      * @param qual
      *            The qualification needed for the wage
      */
     @Override
     public void setQualification(String qual) {
         qualification = qual;
     }
 
     /**
      * Set the wage per hour.
      * 
      * @param wage
      *            The wage per hour
      */
     @Override
     public void setWage(BigDecimal wage) {
         this.wage = wage;
     }
 
     /**
      * Set the year the wage is valid in.
      * 
      * @param year
      *            The year the wage is valid in.
      */
     @Override
     public void setYear(short year) {
         this.year = year;
     }
 
 }
