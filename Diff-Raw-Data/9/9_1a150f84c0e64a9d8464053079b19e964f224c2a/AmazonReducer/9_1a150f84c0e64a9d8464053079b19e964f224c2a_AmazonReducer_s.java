 package amazonEmotion;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Reducer;
 import amazonEmotion.DoubleArrayWritable;
 
 public class AmazonReducer extends Reducer<DoubleWritable, DoubleArrayWritable, Text, Text> {
 
 	public final static String[] emotions = new String[]{"anger", "anticipation", "disgust", "fear", "joy", 
 		"sadness", "surprise", "trust", "noEmotion", "positive", "negative", "noSentiment"};
 	
 		@Override
 		protected void reduce(DoubleWritable key, Iterable<DoubleArrayWritable> values, Context context) throws IOException, InterruptedException {
 			//context.getCounter(Count.SCORE).increment(1);
 
 			DecimalFormat myFormatter = new DecimalFormat("#.###");
 			
 			int count = 0;
 			double[] array = new double[12];
 			for (DoubleArrayWritable value : values) {
 				Writable[] writeArray = value.get();
 				
 				count++;
 				String lineToPrint = "";
 				for(int i = 0; i < writeArray.length; i++){
 					DoubleWritable d = (DoubleWritable) writeArray[i];
 					array[i] += d.get();
 					lineToPrint += myFormatter.format(d.get()) + "\t";
 				}
				//context.write(new Text(""+key), new Text(lineToPrint));
 			}
 				
 
 			String output = "";
 			
 			myFormatter = new DecimalFormat("#.###");
 			
 			for(int i = 0; i < array.length; i++){
 				//System.out.println(array1[i] + " " + count1);
 				array[i] = array[i]/(double)count;
 				output += emotions[i] + ":" + myFormatter.format(array[i]) + "\t";
 			}
 			
 			context.write(new Text(""+key), new Text(output));
 			
 			//highestScore += value.get();
 			//context.write(key, new Text("score: "+highestScore));
 			//context.write(SCORE);
 		}
 
 	
 }
