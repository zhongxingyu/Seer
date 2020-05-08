 /*******************************************************************************
  * Copyright 2010 Olaf Sebelin
  * 
  * This file is part of Verdandi.
  * 
  * Verdandi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Verdandi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Verdandi.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 /*
  * Created on 29.09.2005
  * Author: osebelin
  *
  */
 package verdandi;
 
 import java.io.Serializable;
 
 /**
  * A projects summary.
  * 
  */
 public class SummaryItem implements Serializable {
 
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
 
   /*
    * FIXME: Trennen der Anzeige von und halten der Daten (Inhalt & layout!)
    * DurationFormatter kann ja wohl alles, was wir bruachen!
    */
 
   public static final int DISPLAY_DURATION_HHMM = 1;
 
   public static final int DISPLAY_DURATION_HH_QUARTER = 2;
 
   private static int displayMode = DISPLAY_DURATION_HH_QUARTER;
 
   private String projectid;
 
   private String projectName;
 
   private int duration;
 
   private String description;
 
   public SummaryItem() {
     super();
   }
 
   public SummaryItem(String projectid, String projectName, int duration) {
     this(projectid, projectName, duration, projectName);
   }
 
   public SummaryItem(String projectid, String projectName, int duration,
       String description) {
     super();
     this.projectid = projectid;
     this.projectName = projectName;
     this.duration = duration;
     this.description = description;
   }
 
   public SummaryItem(String projectid, String projectName, Long duration) {
     this(projectid, projectName, duration.intValue(), projectName);
   }
 
   public SummaryItem(String projectid, String projectName, Long duration,
       String description) {
     this(projectid, projectName, duration.intValue(), description);
   }
 
   /**
    * @return Returns the duration in minutes.
    */
   public int getDuration() {
     return duration;
   }
 
   /**
    * @return Returns the projectid.
    */
   public String getProjectid() {
     return projectid;
   }
 
   /**
    * @return Returns the projectName.
    */
   public String getProjectName() {
     return projectName;
   }
 
   /**
    * Sets the duration <b>in minutes</b>
    * 
    * @param duration
    *          The duration to set.
    */
   public void setDuration(int duration) {
     this.duration = duration;
   }
 
   /**
    * @param projectid
    *          The projectid to set.
    */
   public void setProjectid(String projectid) {
     this.projectid = projectid;
   }
 
   /**
    * @param projectName
    *          The projectName to set.
    */
   public void setProjectName(String projectName) {
     this.projectName = projectName;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
     StringBuffer buf = new StringBuffer();
     buf.append(projectName).append("(").append(projectid).append("): ");
     buf.append(getDurationAsString());
     return buf.toString();
   }
 
   /**
    * @return The formatted duration
    * @see DurationFormatter#format(int)
    */
   public String getDurationAsString() {
     return DurationFormatter.getFormatter().format(duration);
   }
 
   /**
    * @return Returns the displayMode.
    */
   public static int getDisplayMode() {
     return displayMode;
   }
 
   /**
    * @param displayMode
    *          The displayMode to set.
    */
   public static void setDisplayMode(int displayMode) {
     if (displayMode == DISPLAY_DURATION_HH_QUARTER
         || displayMode == DISPLAY_DURATION_HHMM) {
       SummaryItem.displayMode = displayMode;
     } else {
       throw new IllegalArgumentException(
           "One of {DISPLAY_DURATION_HH_QUARTER | DISPLAY_DURATION_HHMM}");
     }
   }
 
   /**
    * @return the description
    */
   public String getDescription() {
     return description;
   }
 
   public String getDescriptionOrName() {
     if (description == null || description.equals("")) {
       return projectName;
     }
     return description;
   }
 
   public String formatDurationInManDays() {
     int days = duration / (8 * 60);
     int hrs = duration % (8 * 60) / 60;
     int mins = duration % (8 * 60) % 60;
     return String.format("%02d:%02d:%02d", days, hrs, mins);
   }
 
  public String formatDurationInManHours() {
    int hrs = duration / 60;
    int mins = duration % 60;
    return String.format("%02d:%02d", hrs, mins);
  }

 }
