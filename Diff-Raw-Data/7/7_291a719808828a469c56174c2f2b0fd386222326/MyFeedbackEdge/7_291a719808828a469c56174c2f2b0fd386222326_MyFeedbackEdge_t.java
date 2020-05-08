 ////////////////////////////////FeedbackHistoryGraphEdge//////////////////////////////////
 package trustGrapher.graph;
 
 import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
 import cu.repsystestbed.data.Feedback;
 import cu.repsystestbed.entities.Agent;
 import utilities.ChatterBox;
 
 /**
  * A wrapper for the FeedbackHistoryGraphEdge class for use in the TrustGrapher trust simulator
  * This is an edge that represents a list of transactions going from one peer to the other.
  * @author Andrew O'Hara
  */
 public class MyFeedbackEdge extends FeedbackHistoryGraphEdge {
     private int key;
 
 //////////////////////////////////Constructor///////////////////////////////////
 
     public MyFeedbackEdge(int key, Agent assessor, Agent assessee) throws Exception {
         super(assessor, assessee);
         this.key = key;
     }
 
 //////////////////////////////////Accessors/////////////////////////////////////
     public int getKey() {
         return key;
     }
 
     public boolean hasMultipleFeedback() {
         return super.feedbacks.size() > 1;
     }
 
     @Override
     public String toString(){
         String s = "";
         if (!feedbacks.isEmpty()){
             s = s + "" + feedbacks.get(0).value;
             for (int i=1 ; i<feedbacks.size() ; i++){
                 s = s + ", " + feedbacks.get(i).value;
             }
         }
         return s;
     }
 
     public Agent getAssessor(){
         return (Agent) src;
     }
 
     public Agent getAssessee(){
         return (Agent) sink;
     }
 
 ///////////////////////////////////Methods//////////////////////////////////////
     public void addFeedback(Agent assessor, Agent assessee, double feedback) {
         try {
             super.addFeedback(new Feedback(assessor, assessee, feedback));
         } catch (Exception ex) {
             ChatterBox.error(this, "addFeedback()", ex.getMessage());
         }
     }
 
     public void removeFeedback(double feedback) {
         for (int i = 0; i < feedbacks.size(); i++) {
             if (feedbacks.get(i).value == feedback) {
                 feedbacks.remove(i);
                break;
             }
         }
     }
 
     @Override
     public boolean equals(Object o){
         if (o instanceof FeedbackHistoryGraphEdge == false){
             return false;
         }
         MyFeedbackEdge other = (MyFeedbackEdge) o;
         return this.key == other.key;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 29 * hash + this.key;
         return hash;
     }
 }
 ////////////////////////////////////////////////////////////////////////////////
