 package genus;
 import java.util.Set;
 
 /** A bounded implementation of FindGenus.
  */
 public class BoundedFindGenus extends DefaultFindGenus
 {
     /** A lower bound for the number of faces. */
     private int previousResult;
 
     /** Constructor.
      */
     public BoundedFindGenus()
     {
        previousResult = 0;
     }
 
     @Override
     public void onRecursionStart()
     {
        previousResult = 0;
     }
 
     /** Check some bounding criteria.
      */
     @Override
     public boolean onRecurse(DefaultGraph graph, int cycleStart,
             int cycleSecond, int lastVertex, int current, int currentFaces,
             int edgesLeft, int edgesInCurrentCycle)
     {
         int girth = graph.getGirth();
 
         /* Minimum number of edges needed to finnish current cycle. */
         int neededInCurrent;
         if(edgesInCurrentCycle >= girth) {
             if(graph.hasEdge(current, cycleStart))
                 neededInCurrent = 1;
             else
                 neededInCurrent = 2;
         } else {
             neededInCurrent = girth - edgesInCurrentCycle;
         }
 
         /* Simple bounding based on edges left/current number of faces. The
          * +1 is the cycle we're currently working on. */
         int estimate = currentFaces + 1 + (edgesLeft - neededInCurrent) / girth;
 
         /* If we are not going to get higher than our previous result, we can
          * bound. Note that we add 1 to our previous result, this is because
          * either all results will be even, or all results will be odd. */
         if(estimate <= previousResult + 1) {
             return false;
         }
 
         /*float depth = (float) edgesLeft / (float) graph.getNumberOfEdges();
         if(previousResult >= 0 && current < 0 &&
                 estimate * 0.8f <= previousResult && depth >= 0.3f) {
             if(graph.estimate() <= previousResult) {
                 return false;
             }
         }*/
 
         return true;
     }
 
     /** Keep the maximum found.
      */
     @Override
     public void onRecursionEnd(int faces)
     {
         if(faces > previousResult)
             previousResult = faces;
     }
 }
