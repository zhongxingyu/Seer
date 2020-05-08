 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: zury
  * Date: 11/18/12
  * Time: 12:36 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TA {
 	
 	TF tf;
 	graph g;
 	Vector<String> words;
 	String[] files;
 	int flag;
 	
 	public TA(TF tf, graph g, Vector<String> words, String[] files, int flag)
 	{
 		this.tf = tf;
 		this.g = g;
 		this.words = words;
 		this.files = files;
 		this.flag = flag;
 	}
 	
 	public void run()
 	{
 		Comparator<webTf> comparator = Collections.reverseOrder();
 		Vector<wordWebTf> wwfVec = new Vector<wordWebTf>();
 		// build the table
 		for(int i=0;i<words.size();i++)
 		{
 			wordWebTf wwf = new wordWebTf(words.get(i));
 			for(Map.Entry<String,HashMap<String,Double>> entry : tf.getDocstf().entrySet())
 			{
				HashMap<String,Double> tfMap = entry.getValue();
 				String website = entry.getKey();
				Double tfScoreOfWord = tfMap.get(words.elementAt(i));
				if (tfScoreOfWord == null) tfScoreOfWord = 0.0;
 				webTf wt = new webTf(website,tfScoreOfWord);
 				wwf.wtVec.add(wt);
 			}
 			
 			//sort
 			Collections.sort(wwf.wtVec,comparator);
 			//add your thing here
 			wwfVec.add(wwf);
 		}
 
 		//ta algo
 		double trash;
 		for(int i=0;i<3;i++)
 		{
 			
 		}
 	}
 }
