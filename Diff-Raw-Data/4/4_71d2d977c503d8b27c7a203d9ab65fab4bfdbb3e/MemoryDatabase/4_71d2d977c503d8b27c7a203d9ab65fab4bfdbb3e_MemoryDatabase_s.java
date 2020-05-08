 package edu.berkeley.gamesman.database;
 
 import java.math.BigInteger;
 
 import edu.berkeley.gamesman.core.Configuration;
 
 /**
  * <p>
  * Stores the entire database as an array of bytes. This is a DatabaseWrapper
  * which means it can be flushed back to any other database (most commonly a
  * FileDatabase)
  * </p>
  * <p>
  * This class may also be used by game-specific solvers (See the C4CachedSolver)
  * for caching sets of records.
  * </p>
  * 
  * @author dnspies
  */
 public class MemoryDatabase extends DatabaseWrapper {
 
 	/**
 	 * Default constructor
 	 * 
 	 * @param db
 	 *            The backing database
 	 * @param uri
 	 *            The file-name of the backing database
 	 * @param conf
 	 *            The configuration object
 	 * @param solve
 	 *            true if writing as well as reading
 	 * @param firstRecord
 	 *            The index of the first record contained in this cache (may be
 	 *            different from db.firstRecord())
 	 * @param numRecords
 	 *            The number of records contained in this cache (may be
 	 *            different from db.numRecords())
 	 */
 	public MemoryDatabase(Database db, String uri, Configuration conf,
 			boolean solve, long firstRecord, long numRecords) {
 		this(db, uri, conf, solve, firstRecord, numRecords, true);
 	}
 
 	/**
 	 * A constructor for a memory database which will not initially contain any
 	 * information and may change sizes (see setRange).
 	 * 
 	 * @param db
 	 *            The backing database
 	 * @param uri
 	 *            The file-name of the backing database
 	 * @param conf
 	 *            The configuration object
 	 * @param solve
 	 *            true if writing as well as reading
 	 * @param backChanges
 	 *            If true, calling flush, close, and setRange will automatically
 	 *            flush any changes made back to the underlying database. In
 	 *            general if this is false, db is probably null
 	 */
 	public MemoryDatabase(Database db, String uri, Configuration conf,
 			boolean solve, boolean backChanges) {
 		this(db, uri, conf, solve, 0, 0, backChanges, true);
 	}
 
 	/**
 	 * A constructor for a memory database to be used as a fixed cache
 	 * 
 	 * @param db
 	 *            The backing database
 	 * @param uri
 	 *            The file-name of the backing database
 	 * @param conf
 	 *            The configuration object
 	 * @param solve
 	 *            true if writing as well as reading
 	 * @param firstRecord
 	 *            The index of the first record contained in this cache (may be
 	 *            different from db.firstRecord())
 	 * @param numRecords
 	 *            The number of records contained in this cache (may be
 	 *            different from db.numRecords())
 	 * @param backChanges
 	 *            If true, calling flush and close will flush any changes made
 	 *            back to the underlying database. Also the constructor will
 	 *            read in the specified records from the underlying database. In
 	 *            general if this is false, db is probably null
 	 */
 	public MemoryDatabase(Database db, String uri, Configuration conf,
 			boolean solve, long firstRecord, long numRecords,
 			boolean backChanges) {
 		this(db, uri, conf, solve, firstRecord, numRecords, backChanges, false);
 	}
 
 	private MemoryDatabase(Database db, String uri, Configuration conf,
 			boolean solve, long firstRecord, long numRecords,
 			boolean backChanges, boolean mutable) {
 		super(db, uri, conf, solve, firstRecord, numRecords);
 		firstRecord = this.myFirstRecord = super.firstRecord();
 		numRecords = this.myNumRecords = super.numRecords();
 		this.backChanges = backChanges;
 		this.mutable = mutable;
 		if (backChanges)
 			myHandle = db.getHandle();
 		else
 			myHandle = null;
 		if (!mutable && backChanges) {
 			firstByte = toByte(firstRecord);
 			numBytes = (int) (lastByte(firstRecord + numRecords) - firstByte);
 			memoryStorage = new byte[numBytes];
 			firstNum = toNum(firstRecord);
 			lastNum = toNum(firstRecord + numRecords);
 			db.getRecordsAsBytes(myHandle, firstByte, firstNum, memoryStorage,
 					0, numBytes, lastNum, true);
 		}
 	}
 
 	protected byte[] memoryStorage;
 
 	private final DatabaseHandle myHandle;
 
 	private long firstByte;
 
 	private int firstNum;
 
 	private int numBytes;
 
 	private int lastNum;
 
 	private long myFirstRecord;
 
 	private long myNumRecords;
 
 	private final boolean backChanges, mutable;
 
 	@Override
 	public void close() {
 		flush();
 		db.closeHandle(myHandle);
 		db.close();
 	}
 
 	@Override
 	public void flush() {
 		if (solve && myHandle != null) {
 			db.putRecordsAsBytes(myHandle, firstByte, firstNum, memoryStorage,
 					0, numBytes, lastNum, false);
 		}
 	}
 
 	@Override
 	protected void getBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int len) {
 		System.arraycopy(memoryStorage, (int) (loc - firstByte), arr, off, len);
 	}
 
 	@Override
 	protected void putBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int len) {
 		System.arraycopy(arr, off, memoryStorage, (int) (loc - firstByte), len);
 	}
 
 	@Override
 	protected long getRecordsAsLongGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum) {
 		return longRecordGroup(memoryStorage, (int) (byteIndex - firstByte));
 	}
 
 	@Override
 	protected long getLongRecordGroup(DatabaseHandle dh, long loc) {
 		return longRecordGroup(memoryStorage, (int) (loc - firstByte));
 	}
 
 	@Override
 	protected BigInteger getRecordsAsBigIntGroup(DatabaseHandle dh,
 			long byteIndex, int firstNum, int lastNum) {
 		return bigIntRecordGroup(memoryStorage, (int) (byteIndex - firstByte));
 	}
 
 	@Override
 	protected BigInteger getBigIntRecordGroup(DatabaseHandle dh, long loc) {
 		return bigIntRecordGroup(memoryStorage, (int) (loc - firstByte));
 	}
 
 	@Override
 	protected void putRecordGroup(DatabaseHandle dh, long loc, long r) {
 		toUnsignedByteArray(r, memoryStorage, (int) (loc - firstByte));
 	}
 
 	@Override
 	protected void putRecordsAsGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum, long r) {
 		long group1 = getLongRecordGroup(dh, byteIndex);
 		long group3 = group1;
 		r = splice(group1, r, firstNum);
 		r = splice(r, group3, lastNum);
 		toUnsignedByteArray(r, memoryStorage, (int) (byteIndex - firstByte));
 	}
 
 	@Override
 	protected void putRecordGroup(DatabaseHandle dh, long loc, BigInteger r) {
 		toUnsignedByteArray(r, memoryStorage, (int) (loc - firstByte));
 	}
 
 	@Override
 	protected void putRecordsAsGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum, BigInteger r) {
 		BigInteger group1 = getBigIntRecordGroup(dh, byteIndex);
 		BigInteger group3 = group1;
 		r = splice(group1, r, firstNum);
 		r = splice(r, group3, lastNum);
 		toUnsignedByteArray(r, memoryStorage, (int) (byteIndex - firstByte));
 	}
 
 	/**
 	 * Most databases have a fixed start and range. MemoryDatabase can be used
 	 * as a cache however and can be recycled in a pool by using the setRange
 	 * method
 	 * 
 	 * @param firstRecord
 	 *            The index of the first record contained in this cache
 	 * @param numRecords
 	 *            The number of records in this cache
 	 * @return The amount by which the internal array had to grow to incorporate
 	 *         the new records (0 if it did not grow).
 	 */
 	public int setRange(long firstRecord, int numRecords) {
 		if (!mutable)
 			throw new UnsupportedOperationException();
 		this.myFirstRecord = firstRecord;
 		this.myNumRecords = numRecords;
 		firstByte = toByte(firstRecord);
 		firstNum = toNum(firstRecord);
 		numBytes = (int) (lastByte(firstRecord + numRecords) - firstByte);
 		lastNum = toNum(firstRecord + numRecords);
 		int retVal = ensureByteSize(numBytes);
 		if (backChanges) {
 			db.getRecordsAsBytes(myHandle, firstByte, firstNum, memoryStorage,
 					0, numBytes, lastNum, true);
 		}
 		return retVal;
 	}
 
 	/**
 	 * Ensure the internal array for this cache contains numBytes bytes
 	 * 
 	 * @param numBytes
 	 *            The number of bytes required
 	 * @return The amount by which the internal array had to grow to accomadate
 	 *         the bytes (0 if it did not grow)
 	 */
 	public int ensureByteSize(int numBytes) {
 		if (memoryStorage == null || memoryStorage.length < numBytes) {
 			int retVal = numBytes
 					- (memoryStorage == null ? 0 : memoryStorage.length);
 			memoryStorage = new byte[numBytes];
 			return retVal;
 		} else
 			return 0;
 	}
 
 	@Override
 	public long firstRecord() {
 		return myFirstRecord;
 	}
 
 	@Override
 	public long numRecords() {
 		return myNumRecords;
 	}
 
 	// TODO Do this without cheating
 	@Override
 	public long getRecord(DatabaseHandle dh, long recordIndex) {
 		if (!superCompress && recordGroupByteLength == 1)
 			return memoryStorage[(int) (recordIndex - firstByte)];
 		else
 			return super.getRecord(dh, recordIndex);
 	}
 
 	// TODO Do this without cheating
 	@Override
 	public void putRecord(DatabaseHandle dh, long recordIndex, long r) {
 		if (!superCompress && recordGroupByteLength == 1)
 			memoryStorage[(int) (recordIndex - firstByte)] = (byte) r;
 		else
 			super.putRecord(dh, recordIndex, r);
 	}
 
 	@Override
 	public long getSize() {
 		return memoryStorage.length;
 	}
 }
