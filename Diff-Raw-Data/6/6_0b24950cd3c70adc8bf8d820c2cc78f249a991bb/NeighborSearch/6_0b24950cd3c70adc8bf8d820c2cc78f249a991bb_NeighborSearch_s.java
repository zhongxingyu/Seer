 package zone;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapred.*;
 
 import zone.io.StarInputFormat;
 
 /* export data from a SQL server
  * bcp "SELECT TOP 100000 * FROM [BestDR7].[dbo].[Zone]" queryout zone100000 -n -Sgw20 -T */
 
 public class NeighborSearch {
 	static public final int numZones = 360;
 	static public final int numBlocks = 720;
 	static public double blockWidth = 360.0 / numBlocks;
 	static public double zoneHeight = 180.0 / numZones;
 	// TODO for different zones, the block width can also be different.
 	static private double blockRanges[][] = new double[numBlocks][2];
 	// TODO I might need different zone height for different zones
 	static private double zoneRanges[][] = new double[numZones][2];
 	static private double maxAlphas[] = new double[numZones];
	static private double theta = 0.1;
 	
 	static public double calAlpha(double theta, double dec) {
 		if (Math.abs(dec) + theta > 89.9)
 			return 180;
 
 		return (double) Math.toDegrees(Math.abs(Math.atan(Math.sin(Math
 				.toRadians(theta))
 				/ Math.sqrt(Math.cos(Math.toRadians(dec - theta))
 						* Math.cos(Math.toRadians(dec + theta))))));
 	}
 	
 	static void init() {
 		zoneRanges[0][0] = -90;
 		zoneRanges[0][1] = -90 + zoneHeight;
 		for (int i = 1; i < zoneRanges.length; i++) {
 			zoneRanges[i][0] = zoneRanges[i - 1][1];
 			zoneRanges[i][1] = zoneRanges[i][0] + zoneHeight;
 		}
 
 		blockRanges[0][0] = 0;
 		blockRanges[0][1] = blockWidth;
 		for (int i = 1; i < blockRanges.length; i++) {
 			blockRanges[i][0] = blockRanges[i - 1][1];
 			blockRanges[i][1] = blockRanges[i][0] + blockWidth;
 		}
 
 		for (int i = 0; i < maxAlphas.length; i++) {
 			double maxDec = zoneRanges[i][1];
			if (maxDec < 0)
 				maxDec = zoneRanges[i][0];
 			maxAlphas[i] = calAlpha(theta, maxDec);
 		}
 	}
 
 	public static class Map extends MapReduceBase implements
 			Mapper<LongWritable, Star, BlockIDWritable, PairWritable> {
 		
 		public Map() {
 			init();
 		}
 
 		public void map(LongWritable key, Star value,
 				OutputCollector<BlockIDWritable, PairWritable> output,
 				Reporter reporter) throws IOException {
 
 			BlockIDWritable loc = new BlockIDWritable(value.ra, value.dec);
 			int zoneNum = loc.zoneNum;
 			int raNum = loc.raNum;
 			value.zoneNum = zoneNum;
 			PairWritable p = new PairWritable(value, null);
 
 			/*
 			 * When the block size increases (> theta), only part of a block
 			 * needs to be copied to its neighbor.
 			 */
 			output.collect(loc, p);
 
 			/*
 			 * only replicate objects in the border of a block. I expect most of
 			 * objects don't need to be copied.
 			 */
 			if (value.dec > zoneRanges[zoneNum][0] + theta
 					&& value.dec < zoneRanges[zoneNum][1] - theta
 					&& value.ra > blockRanges[raNum][0] + maxAlphas[zoneNum]
 					&& value.ra < blockRanges[raNum][1] - maxAlphas[zoneNum])
 				return;
 
 			/*
 			 * the code below is to copy the star to some neighbors. We only
 			 * need to copy an object to the bottom, left, left bottom, left top
 			 * neighbors
 			 */
 			value.margin = true;
 
 			/*
 			 * we should treat the entire zone 0 as a block, so we only needs to
 			 * copy some objects at the corner to their neighbors
 			 */
 			if (loc.zoneNum == 0) {
 				/* copy the object to the right top neighbor */
 				if (value.ra >= blockRanges[raNum][1] - maxAlphas[zoneNum]
 				                          						&& value.ra <= blockRanges[raNum][1]
 				                          						&& value.dec >= zoneRanges[zoneNum][1] - theta
 				                          						&& value.dec <= zoneRanges[zoneNum][1]) {
 					BlockIDWritable loc1 = new BlockIDWritable();
 					loc1.raNum = loc.raNum + 1;
 					if (loc1.raNum == numBlocks) {
 						loc1.raNum = 0;
 						value.ra -= 360;
 					}
 					loc1.zoneNum = loc.zoneNum + 1;
 					output.collect(loc1, p);
 				}
 				return;
 			} else if (loc.zoneNum == numZones - 1) {
 				/* copy the object to the bottom neighbor */
 				if (value.dec >= zoneRanges[zoneNum][0]
 				             						&& value.dec <= zoneRanges[zoneNum][0] + theta) {
 					BlockIDWritable loc1 = new BlockIDWritable();
 					loc1.raNum = loc.raNum;
 					loc1.zoneNum = loc.zoneNum - 1;
 					output.collect(loc1, p);
 
 					/* copy the object to the right bottom neighbor */
 					if (value.ra >= blockRanges[raNum][1] - maxAlphas[zoneNum]
 					                      							&& value.ra <= blockRanges[raNum][1]) {
 						loc1.raNum = loc.raNum + 1;
 						if (loc1.raNum == numBlocks) {
 							loc1.raNum = 0;
 							value.ra -= 360;
 						}
 						loc1.zoneNum = loc.zoneNum - 1;
 						output.collect(loc1, p);
 					}
 				}
 				return;
 			}
 
 			/* copy the object to the right neighbor */
 			double blockRange1 = blockRanges[raNum][1];
 			double maxAlpha = maxAlphas[zoneNum];
 			if (value.ra >= blockRange1 - maxAlpha
 					&& value.ra <= blockRange1) {
 				BlockIDWritable loc1 = new BlockIDWritable();
 				loc1.raNum = loc.raNum + 1;
 				/*
 				 * when the object is copied to the right neighbor, we need to
 				 * be careful. we need to convert ra and raNum if ra is close to
 				 * 360.
 				 */
 				if (loc1.raNum == numBlocks) {
 					loc1.raNum = 0;
 					value.ra -= 360;
 				}
 				loc1.zoneNum = loc.zoneNum;
 				output.collect(loc1, p);
 				/* copy the object to the right bottom neighbor */
 				if (value.dec >= zoneRanges[zoneNum][0]
 						&& value.dec <= zoneRanges[zoneNum][0] + theta) {
 					loc1.zoneNum = loc.zoneNum - 1;
 					output.collect(loc1, p);
 				}
 				/* copy the object to the right top neighbor */
 				if (value.dec >= zoneRanges[zoneNum][1] - theta
 						&& value.dec <= zoneRanges[zoneNum][1]) {
 					loc1.zoneNum = loc.zoneNum + 1;
 					output.collect(loc1, p);
 				}
 				if (loc1.raNum == 0) {
 					value.ra += 360;
 				}
 			}
 
 			/* copy the object to the bottom neighbor */
 			if (value.dec >= zoneRanges[zoneNum][0]
 					&& value.dec <= zoneRanges[zoneNum][0] + theta) {
 				BlockIDWritable loc1 = new BlockIDWritable();
 				loc1.raNum = loc.raNum;
 				loc1.zoneNum = loc.zoneNum - 1;
 				if (loc1.zoneNum >= 0) {
 					output.collect(loc1, p);
 				}
 			}
 
 		}
 	}
 
 	public static class Reduce extends MapReduceBase
 			implements
 			Reducer<BlockIDWritable, PairWritable, BlockIDWritable, PairWritable> {
 		
 		public Reduce() {
 			init();
 		}
 		
 		public void reduce(BlockIDWritable key, Iterator<PairWritable> values,
 				OutputCollector<BlockIDWritable, PairWritable> output,
 				Reporter reporter) throws IOException {
 			Vector<Star> starV = new Vector<Star>();
 			while (values.hasNext()) {
 				starV.add(values.next().get(0));
 			}
 			System.out.println(key + ": " + starV.size() + " stars");
 			int num = 0;
 			for (int i = 0; i < starV.size(); i++) {
 				for (int j = i + 1; j < starV.size(); j++) {
 					Star star1 = starV.get(i);
 					Star star2 = starV.get(j);
 					if (star1.margin && star2.margin)
 						continue;
 
 					if (star1.ra >= star2.ra - maxAlphas[key.zoneNum]
 							&& star1.ra <= star2.ra + maxAlphas[key.zoneNum]
 							&& star1.dec >= star2.dec - theta
 							&& star1.dec <= star2.dec + theta
 							&& star1.x * star2.x + star1.y * star2.y + star1.z
 									* star2.z > Math.cos(Math.toRadians(theta))) {
 						output.collect(key, new PairWritable(star1, star2));
 						output.collect(key, new PairWritable(star2, star1));
 						num += 2;
 					}
 				}
 			}
 			System.out.println("num: " + num);
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		JobConf conf = new JobConf(NeighborSearch.class);
 		conf.setJobName("star searching");
 
 		conf.setOutputKeyClass(BlockIDWritable.class);
 		conf.setOutputValueClass(PairWritable.class);
 
 		conf.setMapperClass(Map.class);
 		// conf.setCombinerClass(Reduce.class);
 		conf.setReducerClass(Reduce.class);
 
 		conf.setInputFormat(StarInputFormat.class);
 		conf.setOutputFormat(TextOutputFormat.class);
 
 		FileInputFormat.setInputPaths(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 
 		JobClient.runJob(conf);
 	}
 
 }
