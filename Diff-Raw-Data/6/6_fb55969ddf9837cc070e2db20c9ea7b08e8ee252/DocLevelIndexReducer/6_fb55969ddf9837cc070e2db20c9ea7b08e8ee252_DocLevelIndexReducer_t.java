 package nl.tudelft.mapred.a1;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 /**
  * Builds document-level inverted index file based on (word, document id) pairs.
  *
  */
 public class DocLevelIndexReducer extends Reducer<Text, Text, Text, Text> {
 	public void reduce(Text key, Iterable<Text> values, Context context)
 			throws IOException, InterruptedException {
 
 		List<String> toReturn = new ArrayList<String>();
 		while (values.iterator().hasNext()) {
 			toReturn.add(values.iterator().next().toString());
 		}
 		
 		//The document ids arrive in random order and they have to be sorted.
 		Collections.sort(toReturn);
 		
		context.write(key, new Text(StringUtils.join(toReturn, "|")));
 	}
 
 }
