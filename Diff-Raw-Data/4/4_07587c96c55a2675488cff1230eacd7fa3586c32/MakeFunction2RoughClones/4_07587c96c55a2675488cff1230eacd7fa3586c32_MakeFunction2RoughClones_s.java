 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import junit.framework.Assert;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import ch.unibe.scg.cc.ByteUtils;
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.activerecord.IPutFactory;
import ch.unibe.scg.cc.mappers.Protos.SnippetLocation;
import ch.unibe.scg.cc.mappers.Protos.SnippetMatch;
 
 import com.google.common.base.Optional;
 import com.google.protobuf.ByteString;
 
 /**
  * INPUT:<br>
  * 
  * <pre>
  * FAC1 --> { [FUN1|2] , [FUN2|3] , [FUN3|8] }
  * FAC2 --> { [FUN1|3] , [FUN3|9] }
  * </pre>
  * 
  * OUTPUT:<br>
  * 
  * <pre>
  * FUN1 --> { [FUN2,2|FAC1,3], [FUN3,2|FAC1,8], [FUN3,3|FAC2,9] }
  * FUN2 --> { [FUN1,3|FAC1,2], [FUN3,3|FAC1,8] }
  * FUN3 --> { [FUN1,8|FAC1,2], [FUN1,9|FAC2,9], [FUN2,8|FAC1,3] }
  * </pre>
  */
 public class MakeFunction2RoughClones implements Runnable {
 	static Logger logger = Logger.getLogger(MakeFunction2RoughClones.class.getName());
 	final HTable function2roughclones;
 	final HTable popularSnippets;
 	final MRWrapper mrWrapper;
 
 	@Inject
 	MakeFunction2RoughClones(@Named("function2roughclones") HTable function2roughclones,
 			@Named("popularSnippets") HTable popularSnippets, MRWrapper mrWrapper) {
 		this.function2roughclones = function2roughclones;
 		this.popularSnippets = popularSnippets;
 		this.mrWrapper = mrWrapper;
 	}
 
 	public static class MakeFunction2RoughClonesMapper extends
 			GuiceTableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
 		private static final int POPULAR_SNIPPET_THRESHOLD = 1000;
 		final IPutFactory putFactory;
 
 		@Inject
 		public MakeFunction2RoughClonesMapper(@Named("popularSnippets") HTableWriteBuffer popularSnippets,
 				IPutFactory putFactory) {
 			super(popularSnippets);
 			this.putFactory = putFactory;
 		}
 
 		/** receives rows from htable snippet2function */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void map(ImmutableBytesWritable uselessKey, Result value,
 				@SuppressWarnings("rawtypes") org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException,
 				InterruptedException {
 			byte[] snippet = value.getRow();
 			assert snippet.length == 21;
 
 			logger.finer("map snippet " + ByteUtils.bytesToHex(snippet));
 
 			// snippetHash = new byte[] {}; // dummy to save space
 
 			NavigableMap<byte[], byte[]> familyMap = value.getFamilyMap(GuiceResource.FAMILY);
 			Set<Entry<byte[], byte[]>> columns = familyMap.entrySet();
 			Iterator<Entry<byte[], byte[]>> columnIterator = columns.iterator();
 
 			// special handling of popular snippets
 			if (familyMap.size() > POPULAR_SNIPPET_THRESHOLD) {
 				logger.warning("FAMILY MAP SIZE " + familyMap.size());
 				// fill popularSnippets table
 				while (columnIterator.hasNext()) {
 					Entry<byte[], byte[]> column = columnIterator.next();
 					byte[] function = column.getKey();
 					byte[] location = column.getValue();
 					Put put = putFactory.create(function);
 					put.add(GuiceResource.FAMILY, snippet, 0l, location);
 					write(put);
 				}
 				// we're done, don't go any further!
 				return;
 			}
 
 			// cross product of the columns of the snippetHash
 			while (columnIterator.hasNext()) {
 				Entry<byte[], byte[]> columnFixed = columnIterator.next();
 				byte[] thisFunction = columnFixed.getKey();
 				byte[] thisLocation = columnFixed.getValue();
 				for (Entry<byte[], byte[]> columnVar : columns) {
 					if (columnFixed.equals(columnVar)) {
 						continue;
 					}
 
 					byte[] thatFunction = columnVar.getKey();
 					byte[] thatLocation = columnVar.getValue();
 
 					// save only half of the functions as row-key
 					// full table gets reconstructed in MakeSnippet2FineClones
 					// This *must* be the same as in CloneExpander.
 					if (ByteBuffer.wrap(thisFunction).compareTo(ByteBuffer.wrap(thatFunction)) >= 0) {
 						continue;
 					}
 
 					/*
 					 * REMARK 1: we don't set thisFunction because it gets
 					 * already passed to the reducer as key. REMARK 2: we don't
 					 * set thatSnippet because it gets already stored in
 					 * thisSnippet
 					 */
 					SnippetMatch snippetMatch = SnippetMatch
 							.newBuilder()
 							.setThisSnippetLocation(
 									SnippetLocation.newBuilder().setPosition(Bytes.toInt(Bytes.head(thisLocation, 4)))
 											.setLength(Bytes.toInt(Bytes.tail(thisLocation, 4)))
 											.setSnippet(ByteString.copyFrom(snippet)))
 							.setThatSnippetLocation(
 									SnippetLocation.newBuilder().setFunction(ByteString.copyFrom(thatFunction))
 											.setPosition(Bytes.toInt(Bytes.head(thatLocation, 4)))
 											.setLength(Bytes.toInt(Bytes.tail(thatLocation, 4)))).build();
 
 					context.write(new ImmutableBytesWritable(thisFunction),
 							new ImmutableBytesWritable(snippetMatch.toByteArray()));
 				}
 			}
 		}
 	}
 
 	public static class MakeFunction2RoughClonesReducer extends
 			GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		final IPutFactory putFactory;
 		final HashSerializer hashSerializer;
 
 		@Inject
 		public MakeFunction2RoughClonesReducer(@Named("function2roughclones") HTableWriteBuffer function2roughclones,
 				IPutFactory putFactory, HashSerializer hashSerializer) {
 			super(function2roughclones);
 			this.putFactory = putFactory;
 			this.hashSerializer = hashSerializer;
 		}
 
 		@Override
 		public void reduce(ImmutableBytesWritable functionHashKey, Iterable<ImmutableBytesWritable> snippetMatchValues,
 				Context context) throws IOException, InterruptedException {
 			Iterator<ImmutableBytesWritable> snippetMatchIterator = snippetMatchValues.iterator();
 
 			byte[] functionHash = functionHashKey.get();
 			logger.info("reduce " + ByteUtils.bytesToHex(functionHash));
 
 			Put put = putFactory.create(functionHash);
 
 			while (snippetMatchIterator.hasNext()) {
 				SnippetMatch snippetMatch = SnippetMatch.parseFrom(snippetMatchIterator.next().get());
 				SnippetLocation thisSnippet = snippetMatch.getThisSnippetLocation();
 				SnippetLocation thatSnippet = snippetMatch.getThatSnippetLocation();
 
 				byte[] cellName = Bytes.add(thatSnippet.getFunction().toByteArray(),
 						Bytes.add(Bytes.toBytes(thisSnippet.getPosition()), Bytes.toBytes(thisSnippet.getLength())));
 
 				// create partial SnippetLocations and clear fields already set
 				// in the cellName to save space in HBase
 				SnippetLocation thisPartialSnippet = SnippetLocation.newBuilder(thisSnippet).clearPosition()
 						.clearLength().build();
 				SnippetLocation thatPartialSnippet = SnippetLocation.newBuilder(thatSnippet).clearFunction().build();
 				SnippetMatch partialSnippetMatch = SnippetMatch.newBuilder().setThisSnippetLocation(thisPartialSnippet)
 						.setThatSnippetLocation(thatPartialSnippet).build();
 
 				put.add(GuiceResource.FAMILY, cellName, 0l, partialSnippetMatch.toByteArray());
 			}
 			context.write(functionHashKey, put);
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			mrWrapper.truncate(function2roughclones);
 			mrWrapper.truncate(popularSnippets);
 
 			Scan scan = new Scan();
 			scan.setCaching(100); // TODO play with this. (100 is default value)
 			scan.setCacheBlocks(false);
 			scan.addFamily(GuiceResource.FAMILY); // Gets all columns from the
 													// specified family.
 
 			Configuration config = new Configuration();
 			config.set(MRJobConfig.MAP_LOG_LEVEL, "DEBUG");
 			config.set(MRJobConfig.NUM_REDUCES, "30");
 			// TODO test that
 			config.set(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, "0");
 			config.set(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, "true");
 			config.set(MRJobConfig.IO_SORT_MB, "512");
 			config.set(MRJobConfig.IO_SORT_FACTOR, "100");
 			config.set(MRJobConfig.JOB_UBERTASK_ENABLE, "true");
 			// set to 1 if unsure TODO: check max mem allocation if only 1 jvm
 			config.set(MRJobConfig.JVM_NUMTASKS_TORUN, "-1");
 			config.set(MRJobConfig.TASK_TIMEOUT, "86400000");
 			config.set(MRJobConfig.MAP_MEMORY_MB, "1536");
 			config.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx1024M");
 			config.set(MRJobConfig.REDUCE_MEMORY_MB, "3072");
 			config.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx2560M");
 
 			mrWrapper.launchMapReduceJob(MakeFunction2RoughClones.class.getName() + "Job", config,
 					Optional.of("snippet2function"), Optional.of("function2roughclones"), scan,
 					MakeFunction2RoughClonesMapper.class.getName(),
 					Optional.of(MakeFunction2RoughClonesReducer.class.getName()), ImmutableBytesWritable.class,
 					ImmutableBytesWritable.class);
 
 			function2roughclones.flushCommits();
 		} catch (IOException e) {
 			throw new WrappedRuntimeException(e.getCause());
 		} catch (InterruptedException e) {
 			throw new WrappedRuntimeException(e.getCause());
 		} catch (ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e.getCause());
 		}
 	}
 
 	public static class MakeFunction2RoughClonesTest {
 		@Test
 		@Ignore
 		public void testIndex() {
 			// TODO
 			Assert.assertTrue(false);
 		}
 	}
 
 }
