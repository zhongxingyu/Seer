 package crowdtrust;
 
 public class ContinuousSubTaskBuilder extends SubTaskBuilder{
 	
 	String start;
 	String finish;
 	double precision;
 
 	public ContinuousSubTaskBuilder(int id, double confidence_threshold,
 			int number_of_labels, int max_labels) {
 		super(id, confidence_threshold, number_of_labels, max_labels);
 	}
 	
 	public void setRanges(String start, String finish){
 		this.start = start;
 		this.finish = finish;
 	}
 	
 	public void setPrecision(double p){
 		precision = p;
 	}
 	
 	@Override
 	public SubTask build() {
 		String [] starts = start.split("/");
 		String [] finishs = finish.split("/");
 		
 		int dimensions = starts.length;
 		
 		int [][] ranges = new int [dimensions][2];
 		
 		for (int i = 0; i < dimensions; i++){
 			ranges[i][0] = Integer.parseInt(starts[i]);
 			ranges[i][1] = Integer.parseInt(finishs[i]);
 		}
 		
		double variance = 4*precision;
 		ContinuousSubTask c;
 		if (starts.length == 1){
 			c = new SingleContinuousSubTask(id, confidence_threshold, 
 					number_of_labels, max_labels);
 		} else {
 			c = new MultiContinuousSubTask(id, confidence_threshold, 
 					number_of_labels, max_labels);
 		}
 		
 		c.setDimensions(dimensions);
 		c.setRange(ranges);
 		c.setVariance(variance);
 		c.precision = precision;
 		return c;
 	}
 	
 }
