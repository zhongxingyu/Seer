 package com.pace.base.app;
 
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 public class Season implements Cloneable {
 	
 	private transient static final Logger logger = Logger.getLogger(Season.class);
 	
     private String id;    
     private boolean isOpen = true;
     private String planCycle;
     private String[] years;
     private String[] plannableYears; //1860 - Non-plannable Year Support
     private String timePeriod;
     private PafDimSpec[] otherDims;
     @Deprecated
     private String year; // Not used - needed for backwards compatibility
     
 	/**
 	 * Default Season constructor 
 	 */
 	public Season() {
 		super();
 	}
 	/**
      * @return Returns the isOpen.
      */
     public boolean isOpen() {
         return isOpen;
     }
     /**
      * @param isOpen The isOpen to set.
      */
     public void setOpen(boolean isOpen) {
         this.isOpen = isOpen;
     }
     /**
      * @return Returns the otherDims.
      */
     public PafDimSpec[] getOtherDims() {
         return otherDims;
     }
     /**
      * @param otherDims The otherDims to set.
      */
     public void setOtherDims(PafDimSpec[] otherDims) {
         this.otherDims = otherDims;
     }
     /**
      * @return Returns the years.
      */
     public String[] getYears() {
         return years;
     }
     /**
      * @param years The years to set.
      */
     public void setYears(String[] years) {
         this.years = years;
     }
 
     /**
	 * @return the year - NOT USED - Needed for backwards compatability
 	 * @deprecated
 	 */
 	public String getYear() {
 		return year;
 	}
 	/**
	 * @param year the year to set -  NOT USED - Needed for backwards compatability
 	 * @deprecated
 	 */
 	public void setYear(String year) {
 		this.year = year;
 	}
 	
 
 	/**
 	 * @return the plannableYears
 	 */
 	public String[] getPlannableYears() {
 		return plannableYears;
 	}
 	/**
 	 * @param plannableYears the plannableYears to set
 	 */
 	public void setPlannableYears(String[] plannableYears) {
 		this.plannableYears = plannableYears;
 	}
 	/**
      * @return Returns the label.
      */
     public String getId() {
         return id;
     }
     /**
      * @param Id The id to set.
      */
     public void setId(String Id) {
         this.id = Id;
     }
 
     /**
      * @return Returns the timePeriod.
      */
     public String getTimePeriod() {
         return timePeriod;
     }
     /**
      * @param timePeriod The timePeriod to set.
      */
     public void setTimePeriod(String timePeriod) {
         this.timePeriod = timePeriod;
     }
     /**
      * @return Returns the planCycle.
      */
     public String getPlanCycle() {
         return planCycle;
     }
     /**
      * @param planCycle The planCycle to set.
      */
     public void setPlanCycle(String planCycle) {
         this.planCycle = planCycle;
     }
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + (isOpen ? 1231 : 1237);
 		result = prime * result + Arrays.hashCode(otherDims);
 		result = prime * result + Arrays.hashCode(years);
 		result = prime * result
 				+ ((planCycle == null) ? 0 : planCycle.hashCode());
 		result = prime * result
 				+ ((timePeriod == null) ? 0 : timePeriod.hashCode());
 		result = prime * result + ((year == null) ? 0 : year.hashCode());
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Season other = (Season) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (isOpen != other.isOpen)
 			return false;
 		if (!Arrays.equals(otherDims, other.otherDims))
 			return false;
 		if (!Arrays.equals(years, other.years))
 			return false;
 		if (planCycle == null) {
 			if (other.planCycle != null)
 				return false;
 		} else if (!planCycle.equals(other.planCycle))
 			return false;
 		if (timePeriod == null) {
 			if (other.timePeriod != null)
 				return false;
 		} else if (!timePeriod.equals(other.timePeriod))
 			return false;
 		if (year == null) {
 			if (other.year != null)
 				return false;
 		} else if (!year.equals(other.year))
 			return false;
 		return true;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Season clone() {
 
 		Season clonedSeason = null;
 		
 		try {
 			
 			clonedSeason = (Season) super.clone();
 			
 			if ( this.otherDims != null ) {
 				
 				clonedSeason.otherDims = this.otherDims.clone();			
 				
 			}
 			
 			if ( this.years != null ) {
 				
 				clonedSeason.years = this.years.clone();			
 				
 			}
 			
 		} catch (CloneNotSupportedException e) {
 			//can't happen if implements cloneable
 			logger.warn(e.getMessage());
 		}
 		
 		return clonedSeason;
 	}
     
     
 }
