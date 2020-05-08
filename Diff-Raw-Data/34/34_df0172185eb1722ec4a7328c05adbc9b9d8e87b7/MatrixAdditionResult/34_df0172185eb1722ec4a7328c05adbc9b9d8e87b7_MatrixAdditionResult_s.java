 package jobs;
 
 public class MatrixAdditionResult implements JobResult{
 
    private Integer result;
 
     public MatrixAdditionResult() {
        result = 0;
     }
 
    public MatrixAdditionResult(Integer r) {
         result = r;
     }
 
     @Override
     public String getResult() {
         return result.toString();
     }
 
     @Override
     public String toString() {
         return getResult();
     }
 
     @Override
     public JobResult aggregate(JobResult result) {
        Integer r = Integer.valueOf(result.getResult());
         return new MatrixAdditionResult(r + this.result);
     }
 }
