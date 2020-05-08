 package edu.berkeley.gamesman.core;
 
 import java.math.BigInteger;
 import java.util.Iterator;
 
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.LongIterator;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A Database is the abstract superclass of all data storage methods used in
  * Gamesman. Each particular Database is responsible for the persistent storage
  * of Records derived from solving games.
  * 
  * @author Steven Schlansker
  */
 public abstract class Database {
 
 	protected Configuration conf;
 
 	private byte[] groups;
 
 	private boolean solve;
 
	protected int maxBytes;
 
 	private long numBytes = -1;
 
 	private long firstByte;
 
 	/**
 	 * Initialize a Database given a URI and a Configuration. This method may
 	 * either open an existing database or create a new one. If a new one is
 	 * created, the Configuration should be stored. If one is opened, the
 	 * Configuration should be checked to ensure it matches that already stored.
 	 * This method must be called exactly once before any other methods are
 	 * called. The URI must be in the URI syntax, ex:
 	 * file:///absolute/path/to/file.db or gdfp://server:port/dbname If config
 	 * is null, it will use whatever is in the database. It is recommended that
 	 * you pass in the configuration that you are expecting to ensure you don't
 	 * load a db for a different game.
 	 * 
 	 * Note (By Alex Trofimov) I've updated the file Database to accept Relative
 	 * URL, so instead of file:/// you can just put the filename, and it will
 	 * create a file in the working directory (tested under windows & ubuntu).
 	 * 
 	 * @param uri
 	 *            The URI that the Database is associated with
 	 * @param config
 	 *            The Configuration that is relevant
 	 * @param solve
 	 *            true for solving, false for playing
 	 */
 	public final void initialize(String uri, Configuration config, boolean solve) {
 		conf = config;
 		this.solve = solve;
 		if (numBytes == -1) {
 			firstByte = 0;
 			numBytes = (conf.getGame().numHashes() + conf.recordsPerGroup - 1)
 					/ conf.recordsPerGroup * conf.recordGroupByteLength;
 		}
 		initialize(uri, solve);
 		maxBytes = 1024 - 1024 % conf.recordGroupByteLength;
 		assert Util.debug(DebugFacility.DATABASE, conf.recordsPerGroup
 				+ " records per group\n" + conf.recordGroupByteLength
 				+ " bytes per group");
 	}
 
 	/**
 	 * Initializes as above, but when the confiration is already specified
 	 * 
 	 * @param uri
 	 *            The URI that the Database is associated with
 	 * @param solve
 	 *            true for solving, false for playing
 	 */
 	public abstract void initialize(String uri, boolean solve);
 
 	/**
 	 * Ensure all buffers are flushed to disk. The on-disk state should be
 	 * consistent after this call returns.
 	 */
 	public abstract void flush();
 
 	/**
 	 * Close this Database, flush to disk, and release all associated resources.
 	 * This object should not be used again after making this call.
 	 */
 	public abstract void close();
 
 	/**
 	 * Retrieve the Configuration associated with this Database.
 	 * 
 	 * @return the Configuration stored in the database
 	 */
 	public final Configuration getConfiguration() {
 		return conf;
 	}
 
 	/**
 	 * Notifies the database that a thread has begun writing. The solver will
 	 * write to this range of records, and will be writing sequentially witin
 	 * this range until recordEnd-1.
 	 * 
 	 * @param tier
 	 *            Which tier this task belongs to (if solving all tiers at once)
 	 * @param recordStart
 	 *            The first record that will be written. Must be group-aligned.
 	 * @param recordEnd
 	 *            1 plus lastRecord
 	 * @return A database that I can write this chunk to
 	 */
 	public Database beginWrite(int tier, long recordStart, long recordEnd) {
 		return this;
 	}
 
 	/**
 	 * Notifies the database that a thread has finished writing.
 	 * 
 	 * @param tier
 	 *            Which tier this task belongs to (if solving all tiers at once)
 	 * @param db
 	 *            The sub-database to which the chunk was written
 	 * @param recordStart
 	 *            The first record that will be written. Must be group-aligned.
 	 * @param recordEnd
 	 *            1 plus lastRecord
 	 */
 	public void endWrite(int tier, Database db, long recordStart, long recordEnd) {
 	}
 
 	/**
 	 * Return the Nth Record in the Database
 	 * 
 	 * @param recordIndex
 	 *            The record number
 	 * @return The stored Record
 	 */
 	public Record getRecord(long recordIndex) {
 		Record r = conf.getGame().newRecord();
 		getRecord(recordIndex, r);
 		return r;
 	}
 
 	/**
 	 * Store the Nth Record in the Database in provided record
 	 * 
 	 * @param recordIndex
 	 *            The record number
 	 * @param r
 	 *            The record to store in
 	 */
 	public void getRecord(long recordIndex, Record r) {
 		if (!solve)
 			conf.getGame().setInterperet(recordIndex);
 		if (conf.superCompress) {
 			long group = recordIndex / conf.recordsPerGroup;
 			int num = (int) (recordIndex % conf.recordsPerGroup);
 			long byteOffset = group * conf.recordGroupByteLength;
 			if (conf.recordGroupUsesLong)
 				RecordGroup.getRecord(conf, getLongRecordGroup(byteOffset),
 						num, r);
 			else
 				RecordGroup.getRecord(conf, getBigIntRecordGroup(byteOffset),
 						num, r);
 		} else
 			RecordGroup.getRecord(conf, getLongRecordGroup(recordIndex
 					* conf.recordGroupByteLength), 0, r);
 	}
 
 	/**
 	 * @param recordIndex
 	 *            The index to look in
 	 * @param numRecords
 	 *            The number of records before hasNext returns false
 	 * @return An iterator starting at recordIndex over numRecords records in
 	 *         this database
 	 */
 	public Iterator<Record> getRecords(long recordIndex, int numRecords) {
 		RecordIterator ri;
 		if (conf.superCompress) {
 			long byteOffset = recordIndex / conf.recordsPerGroup
 					* conf.recordGroupByteLength;
 			int preRecords = (int) (recordIndex % conf.recordsPerGroup);
 			int recordGroups = (numRecords + preRecords - 1)
 					/ conf.recordsPerGroup + 1;
 			if (conf.recordGroupUsesLong)
 				ri = new RecordIterator(getLongRecordGroups(byteOffset,
 						recordGroups), preRecords, numRecords);
 			else
 				ri = new RecordIterator(getBigIntRecordGroups(byteOffset,
 						recordGroups), preRecords, numRecords);
 		} else {
 			long byteOffset = recordIndex * conf.recordGroupByteLength;
 			ri = new RecordIterator(
 					getLongRecordGroups(byteOffset, numRecords), 0, numRecords);
 		}
 		return ri;
 	}
 
 	/**
 	 * Store a record in the Database... WARNING! NOT SYNCHRONIZED
 	 * 
 	 * @param recordIndex
 	 *            The record number
 	 * @param r
 	 *            The Record to store
 	 */
 	public void putRecord(long recordIndex, Record r) {
 		if (conf.superCompress) {
 			int num = (int) (recordIndex % conf.recordsPerGroup);
 			long byteOffset = recordIndex / conf.recordsPerGroup
 					* conf.recordGroupByteLength;
 			if (conf.recordGroupUsesLong) {
 				long rg = getLongRecordGroup(byteOffset);
 				rg = RecordGroup.setRecord(conf, rg, num, r);
 				putRecordGroup(byteOffset, rg);
 			} else {
 				BigInteger rg = getBigIntRecordGroup(byteOffset);
 				rg = RecordGroup.setRecord(conf, rg, num, r);
 				putRecordGroup(byteOffset, rg);
 			}
 		} else
 			putRecordGroup(recordIndex * conf.recordGroupByteLength, r
 					.getState());
 	}
 
 	/**
 	 * Stores numRecords records from the records iterator in the database
 	 * 
 	 * @param recordIndex
 	 *            The index to look in
 	 * @param records
 	 *            An iterator starting at recordIndex over numRecords records
 	 * @param numRecords
 	 *            The number of records to go through
 	 */
 	public void putRecords(long recordIndex, RecordIterator records,
 			int numRecords) {
 		if (conf.superCompress) {
 			int preRecords = conf.recordsPerGroup
 					- ((int) ((recordIndex - 1) % conf.recordsPerGroup) + 1);
 			int recordGroups = (numRecords - preRecords) / conf.recordsPerGroup;
 			int mainRecords = recordGroups * conf.recordsPerGroup;
 			int postRecords = (numRecords - preRecords) % conf.recordsPerGroup;
 			for (int i = 0; i < preRecords; i++) {
 				putRecord(recordIndex++, records.next());
 			}
 			if (conf.recordGroupUsesLong) {
 				LongRecordGroupIterator rgi = new LongRecordGroupIterator(
 						records);
 				long groupByteOffset = recordIndex / conf.recordsPerGroup
 						* conf.recordGroupByteLength;
 				putRecordGroups(groupByteOffset, rgi, recordGroups);
 			} else {
 				BigIntRecordGroupIterator rgi = new BigIntRecordGroupIterator(
 						records);
 				long groupByteOffset = recordIndex / conf.recordsPerGroup
 						* conf.recordGroupByteLength;
 				putRecordGroups(groupByteOffset, rgi, recordGroups);
 			}
 			recordIndex += mainRecords;
 			for (int i = 0; i < postRecords; i++) {
 				putRecord(recordIndex++, records.next());
 			}
 		} else {
 			putRecordGroups(recordIndex * conf.recordGroupByteLength,
 					new LongRecordGroupIterator(records), numRecords);
 		}
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @return The group beginning at loc
 	 */
 	public long getLongRecordGroup(long loc) {
 		int groupsLength = conf.recordGroupByteLength;
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		getBytes(loc, groups, 0, groupsLength);
 		long v = RecordGroup.longRecordGroup(conf, groups, 0);
 		return v;
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @return The group beginning at loc
 	 */
 	public BigInteger getBigIntRecordGroup(long loc) {
 		int groupsLength = conf.recordGroupByteLength;
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		getBytes(loc, groups, 0, groupsLength);
 		BigInteger v = RecordGroup.bigIntRecordGroup(conf, groups, 0);
 		return v;
 	}
 
 	/**
 	 * Warning! This method must be synchronized by the caller
 	 * 
 	 * @param loc
 	 *            The location to start at
 	 * @param numGroups
 	 *            The number of groups to return
 	 * @return An iterator over numGroups RecordGroups from this database
 	 */
 	public Iterator<BigInteger> getBigIntRecordGroups(long loc, int numGroups) {
 		int groupsLength = Math.min(maxBytes, numGroups
 				* conf.recordGroupByteLength);
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		getBytes(loc, groups, 0, groupsLength);
 		return new BigIntRecordGroupByteIterator(groups, 0, numGroups);
 	}
 
 	/**
 	 * @param loc
 	 *            The location to start at
 	 * @param numGroups
 	 *            The number of groups to return
 	 * @return An iterator over numGroups RecordGroups from this database
 	 */
 	public LongIterator getLongRecordGroups(long loc, int numGroups) {
 		int groupsLength = Math.min(maxBytes, numGroups
 				* conf.recordGroupByteLength);
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		getBytes(loc, groups, 0, groupsLength);
 		return new LongRecordGroupByteIterator(groups, 0, numGroups);
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @param rg
 	 *            The record group to store
 	 */
 	public void putRecordGroup(long loc, long rg) {
 		int groupsLength = conf.recordGroupByteLength;
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		RecordGroup.toUnsignedByteArray(conf, rg, groups, 0);
 		putBytes(loc, groups, 0, groupsLength);
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @param rg
 	 *            The record group to store
 	 */
 	public void putRecordGroup(long loc, BigInteger rg) {
 		int groupsLength = conf.recordGroupByteLength;
 		if (groups == null || groups.length < groupsLength)
 			ensureGroupsLength(groupsLength);
 		// The condition avoids an unecessary synchronized method call
 		RecordGroup.toUnsignedByteArray(conf, rg, groups, 0);
 		putBytes(loc, groups, 0, groupsLength);
 	}
 
 	private class LongRecordGroupIterator implements LongIterator {
 		private Iterator<Record> recordIterator;
 
 		private Record[] records;
 
 		private int stop;
 
 		private int index;
 
 		private LongRecordGroupIterator(Iterator<Record> recordIterator) {
 			this.recordIterator = recordIterator;
 		}
 
 		private LongRecordGroupIterator(Record[] records, int offset, int length) {
 			this.records = records;
 			this.stop = offset + length;
 			this.index = offset;
 		}
 
 		public boolean hasNext() {
 			if (recordIterator == null)
 				return index < stop;
 			else
 				return recordIterator.hasNext();
 		}
 
 		public long next() {
 			if (recordIterator == null) {
 				long rg = RecordGroup.longRecordGroup(conf, records, index);
 				index += conf.recordsPerGroup;
 				return rg;
 			} else
 				return RecordGroup.longRecordGroup(conf, recordIterator);
 		}
 	}
 
 	private class BigIntRecordGroupIterator implements Iterator<BigInteger> {
 		private Iterator<Record> recordIterator;
 
 		private Record[] records;
 
 		private int stop;
 
 		private int index;
 
 		private BigIntRecordGroupIterator(Iterator<Record> recordIterator) {
 			this.recordIterator = recordIterator;
 		}
 
 		public BigIntRecordGroupIterator(Record[] records, int offset,
 				int length) {
 			this.records = records;
 			this.stop = offset + length;
 			this.index = offset;
 		}
 
 		public boolean hasNext() {
 			if (recordIterator == null)
 				return index < stop;
 			else
 				return recordIterator.hasNext();
 		}
 
 		public BigInteger next() {
 			if (recordIterator == null) {
 				BigInteger rg = RecordGroup.bigIntRecordGroup(conf, records,
 						index);
 				index += conf.recordsPerGroup;
 				return rg;
 			} else
 				return RecordGroup.bigIntRecordGroup(conf, recordIterator);
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException("remove() not supported");
 		}
 	}
 
 	protected class BigIntRecordGroupByteIterator implements
 			Iterator<BigInteger> {
 		private int onByte;
 
 		private int groupCount = 0;
 
 		private int totalGroups;
 
 		private byte[] groups;
 
 		public BigIntRecordGroupByteIterator(byte[] groups, int offset,
 				int numGroups) {
 			totalGroups = numGroups;
 			this.groups = groups;
 			onByte = offset;
 		}
 
 		public boolean hasNext() {
 			return groupCount < totalGroups;
 		}
 
 		public BigInteger next() {
 			if (onByte == maxBytes) {
 				int nextBytes = Math.min((totalGroups - groupCount)
 						* conf.recordGroupByteLength, maxBytes);
 				getBytes(groups, 0, nextBytes);
 				onByte = 0;
 			}
 			BigInteger result = RecordGroup.bigIntRecordGroup(conf, groups,
 					onByte);
 			onByte += conf.recordGroupByteLength;
 			++groupCount;
 			return result;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	protected class LongRecordGroupByteIterator implements LongIterator {
 		private int onByte;
 
 		private int groupCount = 0;
 
 		private int totalGroups;
 
 		private byte[] groups;
 
 		public LongRecordGroupByteIterator(byte[] groups, int offset,
 				int numGroups) {
 			totalGroups = numGroups;
 			this.groups = groups;
 			onByte = offset;
 		}
 
 		public boolean hasNext() {
 			return groupCount < totalGroups;
 		}
 
 		public long next() {
 			if (onByte == maxBytes) {
 				int nextBytes = Math.min((totalGroups - groupCount)
 						* conf.recordGroupByteLength, maxBytes);
 				getBytes(groups, 0, nextBytes);
 				onByte = 0;
 			}
 			long result = RecordGroup.longRecordGroup(conf, groups, onByte);
 			onByte += conf.recordGroupByteLength;
 			++groupCount;
 			return result;
 		}
 	}
 
 	private class RecordIterator implements Iterator<Record> {
 		private final Record[] currentRecords;
 
 		private long nextRecord = 0;
 
 		private final long numRecords;
 
 		private int index;
 
 		private LongIterator longRecordGroups;
 
 		private Iterator<BigInteger> recordGroups;
 
 		private RecordIterator(Iterator<BigInteger> recordGroups,
 				int preRecords, long numRecords) {
 			this.recordGroups = recordGroups;
 			this.numRecords = numRecords + preRecords;
 			currentRecords = new Record[conf.recordsPerGroup];
 			for (int i = 0; i < conf.recordsPerGroup; i++)
 				currentRecords[i] = conf.getGame().newRecord();
 			index = conf.recordsPerGroup;
 			for (int i = 0; i < preRecords; i++)
 				next();
 		}
 
 		private RecordIterator(LongIterator recordGroups, int preRecords,
 				long numRecords) {
 			this.longRecordGroups = recordGroups;
 			this.numRecords = numRecords + preRecords;
 			currentRecords = new Record[conf.recordsPerGroup];
 			for (int i = 0; i < conf.recordsPerGroup; i++)
 				currentRecords[i] = conf.getGame().newRecord();
 			index = conf.recordsPerGroup;
 			for (int i = 0; i < preRecords; i++)
 				next();
 		}
 
 		public boolean hasNext() {
 			return nextRecord < numRecords;
 		}
 
 		public Record next() {
 			if (index >= conf.recordsPerGroup) {
 				if (conf.recordGroupUsesLong)
 					RecordGroup.getRecords(conf, longRecordGroups.next(),
 							currentRecords, 0);
 				else
 					RecordGroup.getRecords(conf, recordGroups.next(),
 							currentRecords, 0);
 				index = 0;
 			}
 			return currentRecords[index++].clone();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException("remove() not supported");
 		}
 
 	}
 
 	/**
 	 * Stores numRecords records from the records array in the database
 	 * 
 	 * @param recordIndex
 	 *            The index to look in
 	 * @param records
 	 *            An array containing records
 	 * @param offset
 	 *            The offset at which to start reading from the array
 	 * @param numRecords
 	 *            The number of records to go through
 	 */
 	public void putRecords(long recordIndex, Record[] records, int offset,
 			int numRecords) {
 		if (conf.superCompress) {
 			int preRecords = conf.recordsPerGroup
 					- ((int) ((recordIndex - 1) % conf.recordsPerGroup) + 1);
 			int recordGroups = (numRecords - preRecords) / conf.recordsPerGroup;
 			int mainRecords = recordGroups * conf.recordsPerGroup;
 			int postRecords = (numRecords - preRecords) % conf.recordsPerGroup;
 			for (int i = 0; i < preRecords; i++) {
 				putRecord(recordIndex++, records[offset++]);
 			}
 			long groupByteOffset = recordIndex / conf.recordsPerGroup
 					* conf.recordGroupByteLength;
 			if (conf.recordGroupUsesLong) {
 				LongRecordGroupIterator rgi = new LongRecordGroupIterator(
 						records, offset, numRecords);
 				putRecordGroups(groupByteOffset, rgi, recordGroups);
 			} else {
 				BigIntRecordGroupIterator rgi = new BigIntRecordGroupIterator(
 						records, offset, numRecords);
 				putRecordGroups(groupByteOffset, rgi, recordGroups);
 			}
 			recordIndex += mainRecords;
 			offset += mainRecords;
 			for (int i = 0; i < postRecords; i++) {
 				putRecord(recordIndex++, records[offset++]);
 			}
 		} else
 			putRecordGroups(recordIndex * conf.recordGroupByteLength,
 					new LongRecordGroupIterator(records, offset, numRecords),
 					numRecords);
 	}
 
 	/**
 	 * Puts numGroups RecordGroups into this database starting at location loc
 	 * (loc is measured in bytes).
 	 * 
 	 * @param loc
 	 *            The location to start at
 	 * @param recordGroups
 	 *            An iterator over at least numGroups RecordGroups
 	 * @param numGroups
 	 *            The number of groups to store
 	 */
 	public synchronized void putRecordGroups(long loc,
 			LongIterator recordGroups, int numGroups) {
 		int groupsLength = numGroups * conf.recordGroupByteLength;
 		ensureGroupsLength(groupsLength);
 		int onByte = 0;
 		for (int i = 0; i < numGroups; i++) {
 			RecordGroup.toUnsignedByteArray(conf, recordGroups.next(), groups,
 					onByte);
 			onByte += conf.recordGroupByteLength;
 		}
 		putBytes(loc, groups, 0, groupsLength);
 	}
 
 	/**
 	 * Puts numGroups RecordGroups into this database starting at location loc
 	 * (loc is measured in bytes).
 	 * 
 	 * @param loc
 	 *            The location to start at
 	 * @param recordGroups
 	 *            An iterator over at least numGroups RecordGroups
 	 * @param numGroups
 	 *            The number of groups to store
 	 */
 	public synchronized void putRecordGroups(long loc,
 			Iterator<BigInteger> recordGroups, int numGroups) {
 		int groupsLength = numGroups * conf.recordGroupByteLength;
 		ensureGroupsLength(groupsLength);
 		int onByte = 0;
 		for (int i = 0; i < numGroups; i++) {
 			RecordGroup.toUnsignedByteArray(conf, recordGroups.next(), groups,
 					onByte);
 			onByte += conf.recordGroupByteLength;
 		}
 		putBytes(loc, groups, 0, groupsLength);
 	}
 
 	/**
 	 * Seek to this location and write len bytes from an array into the database
 	 * 
 	 * @param loc
 	 *            The location to seek to
 	 * @param arr
 	 *            An array to read from
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to write
 	 */
 	public synchronized void putBytes(long loc, byte[] arr, int off, int len) {
 		seek(loc);
 		putBytes(arr, off, len);
 	}
 
 	/**
 	 * Seek to this location and read len bytes from the database into an array
 	 * 
 	 * @param loc
 	 *            The location to seek to
 	 * @param arr
 	 *            The array to write to
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to read
 	 */
 	public synchronized void getBytes(long loc, byte[] arr, int off, int len) {
 		seek(loc);
 		getBytes(arr, off, len);
 	}
 
 	/**
 	 * Seek to this location in the database
 	 * 
 	 * @param loc
 	 *            The location to seek to
 	 */
 	public abstract void seek(long loc);
 
 	/**
 	 * Writes len bytes from the array into the database
 	 * 
 	 * @param arr
 	 *            An array to read from
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to write
 	 */
 	public abstract void putBytes(byte[] arr, int off, int len);
 
 	/**
 	 * Reads len bytes from the database into an array
 	 * 
 	 * @param arr
 	 *            An array to write to
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to read
 	 */
 	public abstract void getBytes(byte[] arr, int off, int len);
 
 	private final synchronized void ensureGroupsLength(int length) {
 		if (groups == null || groups.length < length)
 			groups = new byte[length];
 	}
 
 	/**
 	 * Fills a portion of the database with the passed record.
 	 * 
 	 * @param r
 	 *            The record
 	 * @param offset
 	 *            The byte offset into the database
 	 * @param len
 	 *            The number of bytes to fill
 	 */
 	public void fill(Record r, long offset, long len) {
 		Record[] recs = new Record[conf.recordsPerGroup];
 		for (int i = 0; i < conf.recordsPerGroup; i++)
 			recs[i] = r;
 		seek(offset);
 		while (len > 0) {
 			int groupsLength = (int) Math.min(len, maxBytes);
 			int numGroups = groupsLength / conf.recordGroupByteLength;
 			groupsLength = numGroups * conf.recordGroupByteLength;
 			ensureGroupsLength(groupsLength);
 			int onByte = 0;
 			if (conf.recordGroupUsesLong) {
 				long recordGroup = RecordGroup.longRecordGroup(conf, recs, 0);
 				for (int i = 0; i < numGroups; i++) {
 					RecordGroup.toUnsignedByteArray(conf, recordGroup, groups,
 							onByte);
 					onByte += conf.recordGroupByteLength;
 				}
 			} else {
 				BigInteger recordGroup = RecordGroup.bigIntRecordGroup(conf,
 						recs, 0);
 				for (int i = 0; i < numGroups; i++) {
 					RecordGroup.toUnsignedByteArray(conf, recordGroup, groups,
 							onByte);
 					onByte += conf.recordGroupByteLength;
 				}
 
 			}
 			putBytes(groups, 0, groupsLength);
 			len -= groupsLength;
 		}
 	}
 
 	/**
 	 * @return The number of bytes used to store all the records (This does not
 	 *         include the header size)
 	 */
 	public long getByteSize() {
 		return numBytes;
 	}
 
 	/**
 	 * If this database only covers a particular range of hashes for a game,
 	 * call this method before initialize if creating the database
 	 * 
 	 * @param firstByte
 	 *            The first byte this database contains
 	 * @param numBytes
 	 *            The total number of bytes contained
 	 */
 	public void setRange(long firstByte, long numBytes) {
 		this.firstByte = firstByte;
 		this.numBytes = numBytes;
 	}
 
 	/**
 	 * @return The index of the first byte in this database (Will be zero if
 	 *         this database stores the entire game)
 	 */
 	public long firstByte() {
 		return firstByte;
 	}
 }
