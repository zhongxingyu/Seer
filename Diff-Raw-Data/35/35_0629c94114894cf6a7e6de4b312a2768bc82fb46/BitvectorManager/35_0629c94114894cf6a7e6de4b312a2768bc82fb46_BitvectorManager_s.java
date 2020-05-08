 package lupos.cloud.testing;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.NavigableMap;
 
 import lupos.cloud.hbase.HBaseConnection;
 import lupos.cloud.hbase.bulkLoad.HBaseKVMapper;
 import lupos.cloud.pig.JoinInformation;
 import lupos.cloud.pig.PigQuery;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.util.bloom.HashFunction;
 import org.apache.hadoop.util.bloom.Key;
 import org.apache.hadoop.util.hash.Hash;
 import org.apache.pig.impl.util.MultiMap;
 
 public class BitvectorManager {
 
 	JoinInformation currentSet = null;
 	public static final int VECTORSIZE = 100000;
 	public static final byte[] bloomfilter1ColumnFamily = "1".getBytes();
 	public static final byte[] bloomfilter2ColumnFamily = "2".getBytes();
 	private static HashFunction hash = new HashFunction(VECTORSIZE, 1,
 			Hash.JENKINS_HASH);
 	public static final String WORKING_DIR = "/tmp/CloudBitvectors";
 	public static final String BLOOMFILTER_NAME = "cloudBloomfilter_";
 
 	public static int hash(byte[] toHash) {
 		return hash.hash(new Key(toHash))[0];
 	}
 
 	public static void generateBitvector(
 			HashMap<String, HashSet<CloudBitvector>> bitvectors,
 			PigQuery pigQuery) throws IOException {
 		System.out.println("# bitvectors: " + bitvectors.size());
 
 		init();
 		for (String var : bitvectors.keySet()) {
 
 			MultiMap<Integer, BitSet> bitSetList = new MultiMap<Integer, BitSet>();
 			if (bitvectors.get(var).size() > 1) {
 				for (CloudBitvector bv : bitvectors.get(var)) {
 					bitSetList.put(
 							bv.getSetId(),
 							getBitSetFromeHbaseTable(bv.getTablename(),
 									bv.getRow(), bv.getColumnFamily()));
 				}
 			}
 
 			// Wenn nur ein bitvector vorahnden ist ignroriere diesen
 			if (bitSetList.size() == 0) {
				System.out.println(var
						+ " vector ignored, because appears only once");
 				for (CloudBitvector bv : bitvectors.get(var)) {
 					pigQuery.replaceBloomfilterName(
 							DigestUtils.sha512Hex(var + bv.getPatternId())
 									.toString(), WORKING_DIR + "/"
 									+ BLOOMFILTER_NAME + var.replace("?", "")
 									+ "_IGNORE");
 				}
 			} else {
 				// AND verknüpfen
 				ArrayList<BitSet> groupBitSetList = new ArrayList<BitSet>();
 				for (Integer setId : bitSetList.keySet()) {
 					groupBitSetList
 							.add(mergeBitSet(var, bitSetList.get(setId)));
 				}
				Integer groupCounter = 0;
 				for (BitSet bitVector : groupBitSetList) {
 					Path local = new Path("cloudBloomfilter_"
 							+ var.replace("?", "") + "_" + groupCounter);
 					Path remote = new Path(WORKING_DIR + "/" + BLOOMFILTER_NAME
 							+ var.replace("?", "") + "_" + groupCounter);
 
 					if (((double) bitVector.cardinality()) >= ((double) BitvectorManager.VECTORSIZE * (double) 0.95)) {
 						System.out
 								.println(var
 										+ " vector ignored, because to many true bits (>95%)");
 						for (CloudBitvector bv : bitvectors.get(var)) {
 							pigQuery.replaceBloomfilterName(
 									DigestUtils.sha512Hex(var + bv.getPatternId())
 											.toString(), WORKING_DIR + "/"
 											+ BLOOMFILTER_NAME + var.replace("?", "")
 											+ "_IGNORE");
 						}
 					} else {
 						writeByteToDisk(toByteArray(bitVector), local);
 						HBaseConnection.getHdfs_fileSystem().copyFromLocalFile(
 								true, true, local, remote);
 						new File(local.getName()).delete();
 
 						// Replace in Pig Programm
 						for (CloudBitvector bv : bitvectors.get(var)) {
 							if (bv.getSetId() == groupCounter) {
 								pigQuery.replaceBloomfilterName(DigestUtils
 										.sha512Hex(var + bv.getPatternId())
 										.toString(),
 										WORKING_DIR + "/" + BLOOMFILTER_NAME
 												+ var.replace("?", "") + "_"
 												+ groupCounter);
 							}
 						}
 					}
 					groupCounter++;
 
 				}
 			}
 		}
 
 	}
 
 	private static void init() throws IOException {
 		HBaseConnection.init();
 		HBaseConnection.getHdfs_fileSystem()
 				.delete(new Path(WORKING_DIR), true);
 		HBaseConnection.getHdfs_fileSystem().mkdirs(new Path(WORKING_DIR));
 
 	}
 
 	private static BitSet getBitSetFromeHbaseTable(String tablename,
 			String row, byte[] cf) throws IOException {
 
 		BitSet bitvector = new BitSet(VECTORSIZE);
 
 		// Spezialfall bei ?s ?p ?o, es gibt kein Bitvector für ?s, da rowkey,
 		// daher wird ein volller bitvector zurück gegeben
 		if (cf == null) {
 			bitvector.set(0, VECTORSIZE);
 			return bitvector;
 		}
 		HTable hTable = new HTable(HBaseConnection.getConfiguration(),
 				tablename);
 
 		Scan s = new Scan();
 		s.setStartRow(Bytes.toBytes(row));
 		s.setStopRow(Bytes.toBytes(row + "z"));
 		s.setBatch(250000);
 		s.setCacheBlocks(false);
 		s.addFamily(cf);
 
 		ResultScanner scanner = hTable.getScanner(s);
 		for (Result res = scanner.next(); res != null; res = scanner.next()) {
 			addResultToBitSet(bitvector, res, cf);
 		}
 
 		// cleanup
 		hTable.close();
 
 		return bitvector;
 	}
 
 	public static Result getBitvectorHbaseResult(String tablename,
 			String columnFamily, String column) throws IOException {
 		HBaseConnection.init();
 		return HBaseConnection
 				.getRowWithColumn(tablename, column, columnFamily);
 	}
 
 	public static BitSet addResultToBitSet(BitSet bitvector, Result resultMap,
 			byte[] cf) throws IOException {
 		NavigableMap<byte[], byte[]> cfResults = resultMap.getFamilyMap(cf);
 		if (cfResults != null) {
 			for (byte[] entry : cfResults.keySet()) {
 				Integer pos = byteArrayToInteger(entry);
 				bitvector.set(pos);
 			}
 		}
 		return bitvector;
 	}
 
 	private static Integer byteArrayToInteger(byte[] arr) {
 		return ByteBuffer.wrap(arr).getInt();
 	}
 
 	public static void writeByteToDisk(byte[] toWrite, Path path)
 			throws IOException {
 		FileOutputStream fos = new FileOutputStream(path.getName());
 		fos.write(toWrite);
 		fos.flush();
 		fos.close();
 	}
 
 	public static BitSet mergeBitSet(String var, List<BitSet> bitSetList)
 			throws IOException {
 		if (bitSetList.size() == 1) {
 			return bitSetList.get(0);
 		}
 		System.out.print("\n---> " + var + " is merged (and) from ");
 		BitSet bitvector = new BitSet(VECTORSIZE);
 		int j = 0;
 		for (BitSet bs : bitSetList) {
 			if (j > 0) {
 				System.out.print(", ");
 			}
 			int card = bs.cardinality();
 			System.out.print(card);
 			j++;
 		}
 
 		for (int i = 0; i < bitvector.size(); i++) {
 			boolean setBit = true;
 			for (BitSet bits : bitSetList) {
 				if (!bits.get(i)) {
 					setBit = false;
 				}
 			}
 			if (setBit) {
 				bitvector.set(i);
 			}
 		}
 		System.out.println(" to " + bitvector.cardinality() + " <---");
 		return bitvector;
 	}
 
 	public static BitSet getFullSetBitvector() {
 		BitSet bitvector = new BitSet(VECTORSIZE);
 		for (int i = 0; i < VECTORSIZE; i++) {
 			bitvector.set(i);
 		}
 		return bitvector;
 	}
 
 	public static byte[] toByteArray(BitSet bits) {
 		byte[] bytes = new byte[bits.length() / 8 + 1];
 		for (int i = 0; i < bits.length(); i++) {
 			if (bits.get(i)) {
 				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
 			}
 		}
 		return bytes;
 	}
 }
