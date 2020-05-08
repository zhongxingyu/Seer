 // %3324148068:hoplugins.teamAnalyzer.report%
 package hoplugins.teamAnalyzer.report;
 
 import hoplugins.teamAnalyzer.vo.PlayerPerformance;
 
 
 /**
  * A Report class used for collect data on a specific spot on the field
  *
  * @author <a href=mailto:draghetto@users.sourceforge.net>Massimiliano Amato</a>
  */
 public class Report {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** The rating achieved */
     private double rating;
 
     /** Number of appearance */
     private int appearance;
 
     /** The player id */
     private int playerId;
 
     /** The position who has the player that played in the spot, defender or extra midfielder */
     private int position;
 
     /** The spot place on the lineup */
     private int spot;
    private int status;

     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new Report object.
      *
      * @param pp PlayerPerformance for which the report has to be built
      */
     public Report(PlayerPerformance pp) {
         this.spot = pp.getId();
         this.position = pp.getPositionCode();
         this.playerId = pp.getSpielerId();

        this.status = pp.getStatus();
     }
 
     /**
      * Creates a new Report object.
      */
     public Report() {
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Document Me!
      *
      * @param i
      */
     public void setAppearance(int i) {
         appearance = i;
     }
 
     /**
      * Document Me!
      *
      * @return
      */
     public int getAppearance() {
         return appearance;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param i
      */
     public void setPlayerId(int i) {
         playerId = i;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return
      */
     public int getPlayerId() {
         return playerId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param i
      */
     public void setPosition(int i) {
         position = i;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return
      */
     public int getPosition() {
         return position;
     }
 
     /**
      * Document Me!
      *
      * @param d
      */
     public void setRating(double d) {
         rating = d;
     }
 
     /**
      * Document Me!
      *
      * @return
      */
     public double getRating() {
         return rating;
     }
 
     /**
      * Document Me!
      *
      * @param i
      */
     public void setSpot(int i) {
         spot = i;
     }
 
     /**
      * Document Me!
      *
      * @return
      */
     public int getSpot() {
         return spot;
     }
 
     /**
      * Add another performance to the Report, updating appearance and average rating the rest has
      * to be updated in child classes
      *
      * @param pp
      */
     public void addPerformance(PlayerPerformance pp) {
         appearance++;
         rating = ((rating * (appearance - 1)) + pp.getRating()) / appearance;
     }
 
     /**
      * toString methode: creates a String representation of the object
      *
      * @return the String representation
      */
     @Override
 	public String toString() {
         StringBuffer buffer = new StringBuffer();
 
         buffer.append("numberAppearance = " + appearance);
         buffer.append(", averageRating = " + rating);
         buffer.append(", spot = " + spot);
         buffer.append(", position = " + position);
 
         return buffer.toString();
     }
 }
