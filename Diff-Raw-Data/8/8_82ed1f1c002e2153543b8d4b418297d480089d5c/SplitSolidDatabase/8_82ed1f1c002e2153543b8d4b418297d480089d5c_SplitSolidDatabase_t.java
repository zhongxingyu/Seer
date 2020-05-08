 package edu.berkeley.gamesman.database;
 
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.database.util.SplitDatabaseWritable;
 import edu.berkeley.gamesman.database.util.SplitDatabaseWritableList;
 import edu.berkeley.gamesman.util.LongIterator;
 import edu.berkeley.gamesman.util.Util;
 import edu.berkeley.gamesman.util.biginteger.BigInteger;
 
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 /**
  * HadoopSplitDatabase contains a NavigableMap of read databases, sorted by
  * a range of hash values, so that it is possible to read from a previous
  * tier without combining all the mini-databases.
  * 
  * For writing databases, it uses beginWrite() and endWrite() in order to
  * create a mini-database only used for one thread of the solve.
  * 
  * @author Steven Schlansker
  */
 public class SplitSolidDatabase extends Database {
 
 	/** Default constructor. Must by followed by calls to setFilesystem(),
 	 * setOutputDirectory(), setDelegate(), and initialize().
 	 */
 	public SplitSolidDatabase() {
 	}
 
 	/** Default initialize() function. Should be overridden if a subclass
 	 * requires a different method to access the filesystem.
 	 */
 	@Override
 	public void initialize(String splitfilename) {
 		if (splitfilename == null || splitfilename.length()==0) {
 			return;
 		}
 		databaseTree = new TreeMap<Long, Database>();
 		databaseEnd = new HashMap<Long, Long>();
 		int lastslash = splitfilename.lastIndexOf('/');
 		inputFilenameBase = splitfilename.substring(0, lastslash + 1);
 		if (outputFilenameBase == null) {
 			outputFilenameBase = inputFilenameBase;
 		}
 		List<SplitDatabaseWritable> list = readIndexFile(splitfilename);
 		long next = 0;
 		for (SplitDatabaseWritable sdw : list) {
 			long start = sdw.getStart();
 			assert next == start;
 			long len = sdw.getLength();
 			if (len <= 0) {
 				Util.fatalError("Invalid length " + start
 						+ " in split database " + splitfilename);
 			}
 			String filename = sdw.getFilename();
 
 			Database db = createReadDatabase();
 			db.initialize(inputFilenameBase + filename, conf);
 			databaseTree.put(start, db);
 			databaseEnd.put(start, start + len);
 		}
 	}
 
 	protected Database createReadDatabase() {
 		return new SolidFileDatabase();
 	}
 	
 	protected SolidDatabase createWriteDatabase() {
 		return new SolidFileDatabase();
 	}
 	
 	protected void startedWrite(int tier, SolidDatabase db, long startRecord, long endRecord) {
 		/*
 		if (delegate != null) {
 			delegate.started(tier, db.uri, startRecord, endRecord);
 		}
 		*/
 	}
 	
 	protected void finishedWrite(int tier, SolidDatabase db, long startRecord, long endRecord) {
 		/*
 		if (delegate != null) {
 			delegate.finished(tier, db.uri, startRecord, endRecord);
 		}
 		*/
 	}
 
 	protected List<SplitDatabaseWritable> readIndexFile(String name) {
 		DataInputStream fd = null;
 		SplitDatabaseWritableList list = null;
 		try {
 			fd = new DataInputStream(new FileInputStream(name));
 			list = new SplitDatabaseWritableList();
 			list.readFields(fd);
 			if (conf == null && list.getConf() != null) {
 				conf = list.getConf();
 			}
 		} catch (EOFException e) {
 			// Nothing left in our list of databases, stop the loop.
 		} catch (IOException e) {
 			Util.fatalError("IOException in loading split database", e);
 		} finally {
 			if (fd != null) {
 				try {
 					fd.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * @param dirName The directory to create output chunks in
 	 * (defaults to the input directory).
 	 */
 	public void setOutputDirectory(String dirName) {
 		outputFilenameBase = dirName;
 	}
 
 	public void close() {
 		if (databaseTree != null) {
 			for (Map.Entry<Long, Database> dbpair : databaseTree.entrySet()) {
 				dbpair.getValue().close();
 			}
 		}
 		databaseTree = null;
 		databaseEnd = null;
 	}
 
 	protected String inputFilenameBase;
 	private SortedMap<Long, Database> databaseTree;
 	private Map<Long, Long> databaseEnd;
 
 	protected String outputFilenameBase;
 
 	protected final Long getDatabaseKeyFor(long loc) {
 		if (databaseTree.containsKey(loc)) {
 			return loc;
 		}
 		return databaseTree.headMap(loc+1).lastKey();
 	}
 
 	protected final Database getDatabaseFor(long loc) {
 		return databaseTree.get(getDatabaseKeyFor(loc));
 	}
 
 	@Override
 	public Database beginWrite(int tier, long startRecord, long endRecord) {
 		SolidDatabase db = createWriteDatabase();
 		String filename = tier + ".hdb." + startRecord;
 		String name = outputFilenameBase + "/" + filename;
 		System.out.println(name);
 		db.initialize(name, conf);
 		startedWrite(tier, db, startRecord, endRecord);
 
 		return db;
 	}
 
 	public void endWrite(int tier, Database db, long startRecord, long endRecord) {
 		SolidDatabase hdb = (SolidDatabase) db;
 		finishedWrite(tier, hdb, startRecord, endRecord);
 		db.close();
 	}
 
 	@Override
 	public void putBytes(byte[] arr, int offset, int length) {
 		throw new UnsupportedOperationException(
 				"HadoopSplitDatabase requires a position argument to putBytes");
 	}
 
 	@Override
 	public void putBytes(long loc, byte[] arr, int offset,
 			int length) {
 		throw new UnsupportedOperationException(
 			"putBytes can only be called from beginWrite()'s return value");
 	}
 
 	@Override
 	public Record getRecord(long rec) {
 		Long firstRec = getDatabaseKeyFor(rec);
 		Database db = databaseTree.get(firstRec);
		long firstRecGroupRec = (firstRec / conf.recordsPerGroup) * conf.recordsPerGroup;
 		return db.getRecord(rec - firstRecGroupRec);
 	}
 
 	@Override
 	public void getRecord(long rec, Record outRec) {
 		Long firstRec = getDatabaseKeyFor(rec);
 		Database db = databaseTree.get(firstRec);
		long firstRecGroupRec = (firstRec / conf.recordsPerGroup) * conf.recordsPerGroup;
		db.getRecord(rec - firstRecGroupRec, outRec);
 	}
 
 	@Override
 	public long getLongRecordGroup(long loc) {
 		Long firstRec = getDatabaseKeyFor((loc/conf.recordGroupByteLength)*conf.recordsPerGroup);
 		Database db = databaseTree.get(firstRec);
 		long startRecordGroupByte = (firstRec/conf.recordsPerGroup)*conf.recordGroupByteLength;
 
 /////////// SOURCE OF THE "LOSE in 0" BUG:
 /* FIXME: Need to support case where this record group crosses a *TIER* boundary...
    This does not come up in solving, but it does happen when querying the database. The problem is that
    'loc' is not specific enough to determine which *record* we want. Ideally the caller would pass that
    information into getLongRecordGroup, but at the moment we have two options:
    - Leave getLongRecordGroup unimplemented or throw an error if at a boundary
      (only implement getLongRecord)
    - Do some ugly record group merging at the tier boundaries
 
                           To be continued...            
 
 */
 		return db.getLongRecordGroup(loc - startRecordGroupByte);
 	}
 
 	@Override
 	public BigInteger getBigIntRecordGroup(long loc) {
 		throw new UnsupportedOperationException("BigInteger HadoopDatabase not implemented");
 	}
 
 	@Override
 	public void flush() {
 	}
 
 	@Override
 	public void seek(long position) {
 		throw new UnsupportedOperationException(
 				"HadoopSplitDatabase does not implement seek");
 	}
 
 	@Override
 	public void getBytes(long recordGroupByteLocation, byte[] arr, int offset,
 			int length) {
 		while (length > 0) {
 			long recordNumber = conf.recordsPerGroup-1+
 			((recordGroupByteLocation+(conf.recordGroupByteLength-1))/
 					conf.recordGroupByteLength)*conf.recordsPerGroup;
 			long currentDbStart = getDatabaseKeyFor(recordNumber);
 			long currentDbEnd = databaseEnd.get(currentDbStart);
 			Database db = databaseTree.get(currentDbStart);
 			int amtRead;
 			long startRecordGroupByte = (currentDbStart/conf.recordsPerGroup)*conf.recordGroupByteLength;
 			long endRecordGroupByte = ((currentDbEnd+(conf.recordsPerGroup-1))/conf.recordsPerGroup)*conf.recordGroupByteLength;
 			if (length > endRecordGroupByte - recordGroupByteLocation) {
 				amtRead = (int) (endRecordGroupByte - recordGroupByteLocation);
 				if (amtRead == 0) {
 					return; // Page.extendUp may call this with one record group past the end in the case that the end is exactly aligned.
 				}
 			} else {
 				amtRead = length;
 			}
 			assert amtRead > 0;
 			db.getBytes(recordGroupByteLocation-startRecordGroupByte, arr, offset, amtRead);
 			length -= amtRead;
 			recordGroupByteLocation += amtRead;
 			offset += amtRead;
 		}
 	}
 
 	@Override
 	public void getBytes(byte[] arr, int offset, int length) {
 		throw new UnsupportedOperationException(
 				"HadoopSplitDatabase requires a position argument to getBytes");
 	}
 
 	@Override
 	public LongIterator getLongRecordGroups(long recordGroupByteLocation, int numGroups) {
 		throw new UnsupportedOperationException("getLongRecordGroups is not yet implemented.");
 	}
 }
