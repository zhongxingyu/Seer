 package mapreduce;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import vector.VectorDoubleWritable;
 import clusterer.KmeansCluster;
 import clusterer.KmeansClusterer;
 import config.Constants;
 import distanceMeasure.DistanceMeasure;
 import distanceMeasure.EuclideanDistance;
 
 public class KmeansMapper extends
 		Mapper<LongWritable, Text, LongWritable, KmeansCluster> {
 	protected VectorDoubleWritable point = null;
 	protected KmeansClusterer clusterer = new KmeansClusterer();
 	protected LongWritable id = new LongWritable(0);
 
 	int i = 0;
 
 	@Override
 	public void map(LongWritable key, Text values, Context context)
 			throws IOException {
 		point = new VectorDoubleWritable(values);
 
 		KmeansCluster cluster = null;
 		try {
 			cluster = clusterer.findNearestCluster(point);
 
 			KmeansCluster value = new KmeansCluster(cluster.getId(), point,
 					point.times(point));
 
 			/*
 			 * System.out.println(value.getId() + "\t" +
 			 * value.getCentroid().get().toString() + "\t" + value.getS1().get()
 			 * + "\t" + value.getS2().get());
 			 */
 
 			id.set(cluster.getId());
 			context.write(id, value);
 		} catch (IllegalStateException e) {
 			System.err.println("Error:\t" + e.getMessage() + " at row(" + key
 					+ ")");
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setup(Context context) throws IOException, InterruptedException {
 		super.setup(context);
 		Configuration conf = context.getConfiguration();
 		DistanceMeasure dm = null;
 		try {
 			dm = (DistanceMeasure) Class.forName(
 					"distanceMeasure."
 							+ conf.get(Constants.DISTANCE_MEASURE,
 									"EuclideanDistance")).newInstance();
 		} catch (InstantiationException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			dm = new EuclideanDistance();
 			e.printStackTrace();
 		}
 
 		this.clusterer = new KmeansClusterer(dm);
 
 		String clusterPath = conf.get(Constants.CLUSTER_PATH);
 		if (clusterPath != null && !clusterPath.isEmpty())
 			try {
 				this.clusterer.loadClusters(clusterPath, conf);
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		context.getCounter(Constants.COUNTER_GROUP, Constants.COUNTER_TOTAL)
 				.setValue(0);
 		context.getCounter(Constants.COUNTER_GROUP, Constants.COUNTER_TOTAL)
 				.setValue(this.clusterer.getClusters().size());
 		context.getCounter(Constants.COUNTER_GROUP, Constants.COUNTER_FILE)
 				.increment(1);
 
 		if (Constants.DEBUG) {
 			System.out.println("Mapper setup: Total cluster="
 					+ context.getCounter(Constants.COUNTER_GROUP,
 							Constants.COUNTER_TOTAL).getValue());
 			for (KmeansCluster clu : clusterer.getClusters()) {
 				System.out.println("Cluster " + clu.getId() + "/"
 						+ this.clusterer.getClusters().size() + ":\t"
 						+ clu.getCentroid());
 			}
 		}
 
 	}
 }
