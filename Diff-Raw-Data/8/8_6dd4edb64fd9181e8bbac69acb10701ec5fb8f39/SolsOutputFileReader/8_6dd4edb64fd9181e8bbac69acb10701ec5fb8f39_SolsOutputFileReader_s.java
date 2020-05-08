 package pls;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.log4j.Logger;
 
 public class SolsOutputFileReader {
 	
 	private static final Logger LOG = Logger.getLogger(SolsOutputFileReader.class);
 	
 	public static void main(String[] args) throws IOException, InstantiationException, 
 		IllegalAccessException, ClassNotFoundException {
 		
 		Configuration conf = new Configuration();
 		FileSystem fs = FileSystem.get(conf);
 		
 		String solutionClass = args[0];
 		Path path;
 		if (args.length > 1) {
 			if (args[1].startsWith("-")) {
 				path = getLatestRunFolderPath(new Path("/users/sryza/testdir/"), fs, -Integer.parseInt(args[1]));
 			} else {
 				path = new Path(args[1]);
 			}
 		} else {
 			path = getLatestRunFolderPath(new Path("/users/sryza/testdir/"), fs, 0);
 		}
 		
 //		Path path = new Path("/users/sryza/testdir/1331512645039/");
 		
 		FileStatus[] statuses = fs.listStatus(path);
 		System.out.println("number of subfiles: " + statuses.length);
 		Arrays.sort(statuses, new NumberFolderComparator());
 		for (FileStatus fileStatus : statuses) {
 			Path subpath = fileStatus.getPath();
 			subpath = new Path(subpath, "part-00000");
 			System.out.println("Sols for " + subpath.toString());
 			List<PlsSolution> solutions = getFileSolutions(subpath, fs, conf, solutionClass);
 			for (PlsSolution sol : solutions) {
 				LOG.info("sol cost=" + sol.getCost() + ", id=" + sol.getSolutionId() + ", parent id=" + sol.getParentSolutionId());
 			}
 			System.out.println();
 		}
 	}
 	
 	public static List<PlsSolution> getFileSolutions(Path path, FileSystem fs, Configuration conf, String solutionClass)
 		throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
 		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
 		BytesWritable key = new BytesWritable();
 		BytesWritable value = new BytesWritable();
 		List<PlsSolution> sols = new ArrayList<PlsSolution>();
 		while (reader.next(key, value)) {
 			if (key.equals(PlsUtil.METADATA_KEY)) {
 				//TODO: read best solution so that we can note that it's not among reported if it's not
 				continue;
 			}
 			
 			PlsSolution sol = (PlsSolution)Class.forName(solutionClass).newInstance();
 			sol.buildFromStream(new DataInputStream(new ByteArrayInputStream(value.getBytes())));
 			sols.add(sol);
 		}
 		return sols;
 	}
 	
 	private static Path getLatestRunFolderPath(Path testDir, FileSystem fs, int howManyBack) throws IOException {
 		FileStatus[] statuses = fs.listStatus(testDir);
 		Arrays.sort(statuses, new NumberFolderComparator());
 		return statuses[statuses.length-1-howManyBack].getPath();
 	}
 	
 	private static class NumberFolderComparator implements Comparator<FileStatus> {
 		@Override
 		public int compare(FileStatus fs1, FileStatus fs2) {
			return getRunNumber(fs1) - getRunNumber(fs2);
 		}
 		
		private int getRunNumber(FileStatus fs) {
 			String name = fs.getPath().getName();
			return Integer.parseInt(name);
 		}
 	}
 }
