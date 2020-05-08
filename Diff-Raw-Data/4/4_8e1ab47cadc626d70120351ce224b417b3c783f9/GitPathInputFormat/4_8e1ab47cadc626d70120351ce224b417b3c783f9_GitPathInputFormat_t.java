 package ch.unibe.scg.cc.mappers.inputformats;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 
 public class GitPathInputFormat extends FileInputFormat<Text, BytesWritable> {
 	static Logger logger = Logger.getLogger(GitPathInputFormat.class.getName());
 
 	@Override
 	protected boolean isSplitable(JobContext context, Path filename) {
 		return false;
 	}
 
 	@Override
 	public RecordReader<Text, BytesWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
 			throws IOException, InterruptedException {
 		logger.finer("yyy recordreader creation");
 		return new GitPathRecordReader();
 	}
 
 	private static class GitPathRecordReader extends RecordReader<Text, BytesWritable> {
 		private static final int BYTE_BUFFER_SIZE = 8192;
 		private FSDataInputStream fsin;
 		private Text currentKey;
 		private BytesWritable currentValue;
 		private boolean isFinished = false;
 		private Path packFilePath;
 		private FileSystem fs;
 
 		@Override
 		public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException,
 				InterruptedException {
 			FileSplit split = (FileSplit) inputSplit;
 			Configuration conf = taskAttemptContext.getConfiguration();
 			Path path = split.getPath();
 			fs = path.getFileSystem(conf);
 			this.packFilePath = split.getPath();
 		}
 
 		@Override
 		public boolean nextKeyValue() throws IOException, InterruptedException {
 			if (isFinished) {
 				return false;
 			}
 
 			logger.finer("yyy opening " + packFilePath);
 
 			fsin = fs.open(packFilePath);
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			byte[] temp = new byte[BYTE_BUFFER_SIZE];
 
 			int bytesRead;
 			do {
 				bytesRead = fsin.read(temp, 0, BYTE_BUFFER_SIZE);
 				if (bytesRead > 0) {
 					bos.write(temp, 0, bytesRead);
 				}
 			} while (bytesRead > 0);
 
 			currentValue = new BytesWritable(bos.toByteArray());
 			currentKey = new Text(packFilePath.toString());
 
 			isFinished = true;
 			return true;
 		}
 
 		@Override
 		public float getProgress() throws IOException, InterruptedException {
 			return isFinished ? 1 : 0;
 		}
 
 		@Override
 		public Text getCurrentKey() throws IOException, InterruptedException {
 			return currentKey;
 		}
 
 		@Override
 		public BytesWritable getCurrentValue() throws IOException, InterruptedException {
 			return currentValue;
 		}
 
 		@Override
 		public void close() throws IOException {
			if(fsin != null) {
				fsin.close();
			}
 		}
 	}
 }
