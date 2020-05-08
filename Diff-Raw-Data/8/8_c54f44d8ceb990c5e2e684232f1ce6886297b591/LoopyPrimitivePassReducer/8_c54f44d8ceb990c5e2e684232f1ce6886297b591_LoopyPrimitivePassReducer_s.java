 package edu.berkeley.gamesman.loopyhadoop;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.database.FileDatabase;
 import edu.berkeley.gamesman.game.Game;
 import edu.berkeley.gamesman.parallel.RangeFile;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.LocalFileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class LoopyPrimitivePassReducer<S extends State> extends
 		Reducer<RangeFile, LongWritable, LongWritable, IntWritable> {
 	private FileSystem fs;
 	private Configuration conf;
 	private Game<S> game;
 	private final Random rand = new Random();
 	private IntWritable zero = new IntWritable(0);
 	private Path primitivePath;
 
 	@Override
 	public void setup(Context context) {
 		try {
 			org.apache.hadoop.conf.Configuration hadoopConf = context
 					.getConfiguration();
 			conf = Configuration.deserialize(hadoopConf
 					.get("gamesman.configuration"));
 			fs = FileSystem.get(hadoopConf);
 			game = conf.getCheckedGame();
 			primitivePath = new Path(hadoopConf.get("primitive.output"));
 		} catch (IOException e) {
 			throw new Error(e);
 		} catch (ClassNotFoundException e) {
 			throw new Error(e);
 		}
 	}
 
 	@Override
 	public void reduce(RangeFile rangeFile, Iterable<LongWritable> hashes,
 			Context context) {
 		Path path = rangeFile.myFile.getPath();
 		try {
 			LocalFileSystem lfs = new LocalFileSystem();
 			String stringPath = lfs.pathToFile(path).getPath();
 			String localStringPath = stringPath + "_local";
 			Path localPath = new Path(localStringPath);
 			fs.copyToLocalFile(path, localPath);
 			FileDatabase database = new FileDatabase(localStringPath);
 			DatabaseHandle readHandle = database.getHandle(true);
 			DatabaseHandle writeHandle = database.getHandle(false);
 			Record record = game.newRecord();
 			S gameState = game.newState();
 
 			Path primitiveFile = new Path(primitivePath, "range"
 					+ rangeFile.myRange.firstRecord
 					+ "to"
 					+ (rangeFile.myRange.firstRecord
 							+ rangeFile.myRange.numRecords - 1));
 			SequenceFile.Writer primitiveFileWriter = SequenceFile
 					.createWriter(fs, context.getConfiguration(),
 							primitiveFile, LongWritable.class,
 							LongWritable.class);
 
 			/*
 			 * all hashes are going to be in the same file so this goes outside
 			 * the loop
 			 */
 			boolean changesMade = false;
 			for (LongWritable hash : hashes) {
 				long longHash = hash.get();
 				game.hashToState(longHash, gameState);
 				long recordHash = database.readRecord(readHandle, longHash);
 				game.longToRecord(gameState, recordHash, record);
 				/* get the record associated with this hash */
 
 				boolean visited = record.value != Value.IMPOSSIBLE;
 
 				if (!visited) {
 					Value primitiveValue = game.primitiveValue(gameState);
 					if (primitiveValue == Value.UNDECIDED) {
 						// value is not primitive
 						record.value = Value.DRAW;
 					} else // primitive
 					{
 						primitiveFileWriter.append(hash, new LongWritable(
 								recordHash));
 						record.value = primitiveValue;
 						record.remoteness = 0;
 						// we have to deal with this during the solve for
 						// non-primitives?
 					}
 					recordHash = game.recordToLong(gameState, record);
 					database.writeRecord(writeHandle, longHash, recordHash);
 					// write this record to the database
 					changesMade = true;
 					context.write(hash, zero);
 				}
 			}
 
 			primitiveFileWriter.close();
 			database.close();
 
 			if (changesMade) {
				String localTempPathString = localStringPath + "_temp";
				new File(localStringPath)
						.renameTo(new File(localTempPathString));
 				Path tempPath = new Path(stringPath + "_" + rand.nextLong());
 				// use a random long to prevent collisions in the expensive copy
 				// step
				fs.moveFromLocalFile(new Path(localTempPathString), tempPath);
 				// copy the written database to hdfs
 				fs.rename(tempPath, path);
 				// rename to complete process
 			}
 		} catch (IOException e) {
 			throw new Error(e);
 		} catch (ClassNotFoundException e) {
 			throw new Error(e);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
