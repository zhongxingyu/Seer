 package ch.unibe.scg.cells;
 
 import java.math.BigInteger;
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.common.base.Supplier;
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.google.protobuf.ByteString;
 
 
 class InMemorySource<T> implements CellSource<T>, CellLookupTable<T>, Iterable<Cell<T>> {
 	private static final long serialVersionUID = 1L;
 	private static final Comparator<ByteString> cmp = new LexicographicalComparator();
 
 	/** Shards are immutable. Does not contain empty shards. */
 	final private List<List<Cell<T>>> store;
 	/** A sorted index of row keys at the end of shards, to enable fast shard retrieving by row key. */
 	final private List<ByteString> splitIndex;
 	/** Lazily initialized column index. */
 	final private ColIndexSupplier colIndex = new ColIndexSupplier();
 
 	/** To look up a row, get a RowPointer, then look up its row in the store */
 	private static class RowPointer implements Comparable<RowPointer> {
 		final ByteString colKey;
 		final ByteString rowKey;
 
 		RowPointer(ByteString colKey, ByteString rowKey) {
 			this.colKey = colKey;
 			this.rowKey = rowKey;
 		}
 
 		@Override
 		public int compareTo(RowPointer o) {
 			return ComparisonChain
 				.start()
 				.compare(colKey, o.colKey, cmp)
 				.compare(rowKey, o.rowKey, cmp)
 				.result();
 		}
 	}
 
 	/**
 	 * Supplier a column index, enabling a fast way to read column.
 	 * The first call to get() will create index.
 	 */
 	private class ColIndexSupplier implements Supplier<List<RowPointer>> {
 		private List<RowPointer> columnIndex;
 
 		@Override
 		public synchronized List<RowPointer> get() {
 			if (columnIndex != null) {
 				return columnIndex;
 			}
 
 			List<RowPointer> colIndexBuilder = new ArrayList<>();
 			for (List<Cell<T>> shard : store) {
 				for (Cell<T> c : shard) {
 					colIndexBuilder.add(new RowPointer(c.getColumnKey(), c.getRowKey()));
 				}
 			}
 
 			// TODO: parallel sorting?
 			columnIndex = Ordering.natural().immutableSortedCopy(colIndexBuilder);
 
 			return columnIndex;
 		}
 	}
 
 	private InMemorySource(List<List<Cell<T>>> store, List<ByteString> splitIndex) {
 		this.store = store;
 		this.splitIndex = splitIndex;
 	}
 
 	private Object writeReplace() {
 		return new ShallowSerializingCopy.SerializableLiveObject(this);
 	}
 
 	static <T> InMemorySource<T> make(List<List<Cell<T>>> store) {
 		assert isStoreOk(store);
 		List<List<Cell<T>>> nonEmptyStore = new ArrayList<>(store.size());
 		List<ByteString> splitIndex = new ArrayList<>(store.size());
 
 		for (int i = 0; i < store.size(); i++) {
 			List<Cell<T>> shard = store.get(i);
 			if (!shard.isEmpty()) {
 				nonEmptyStore.add(shard);
 				splitIndex.add(shard.get(shard.size() - 1).getRowKey());
 			}
 		}
 
 		return new InMemorySource<>(nonEmptyStore, splitIndex);
 	}
 
 	@Override
 	public Iterator<Cell<T>> iterator() {
 		return Iterables.concat(store).iterator();
 	}
 
 	@Override
 	public int nShards() {
 		return store.size();
 	}
 
 	@Override
 	public void close() {
 		// Nothing to do.
 	}
 
 	@Override
 	public OneShotIterable<Cell<T>> getShard(int shard) {
 		return new AdapterOneShotIterable<>(store.get(shard));
 	}
 
 	@Override
 	public Iterable<Cell<T>> readRow(ByteString rowKeyPrefix) {
 		int fromShard = findShard(rowKeyPrefix);
 		if (fromShard < 0) {
 			return Collections.emptyList();
 		}
 
 		int from = Collections.binarySearch(
 				store.get(fromShard),
 				new Cell<T>(rowKeyPrefix, ByteString.EMPTY, ByteString.EMPTY));
 		if (from < 0) {
 			from = ~from; // ~from is the insertion point.
 		}
 
 		if (isKeyAllFF(rowKeyPrefix)) {
 			assert fromShard == store.size() - 1;
 			return store.get(fromShard).subList(from, store.get(fromShard).size());
 		}
 
 		ByteString toKey = keyPlusOne(rowKeyPrefix);
 		int toShard = findShard(toKey);
 		if (toShard < 0) {
 			// Couldn't find a toShard. This can only happen if fromShard was in the last shard already.
 			assert fromShard == store.size() - 1;
 			toShard = fromShard;
 		}
 
 		int to = Collections.binarySearch(store.get(toShard), new Cell<T>(toKey, ByteString.EMPTY, ByteString.EMPTY));
 		if (to >= 0) {
 			to++; // To must be exclusive.
 		} else {
 			to = ~to; // To should be insertion point.
 		}
 
 		if (fromShard == toShard) {
 			return store.get(fromShard).subList(from, to);
 		}
 
 		Iterable<Cell<T>> ret = store.get(fromShard).subList(from, store.get(fromShard).size());
 		for (int i = fromShard + 1; i < toShard; i++) {
 			ret = Iterables.concat(ret, store.get(i));
 		}
 		ret = Iterables.concat(ret, store.get(toShard).subList(0, to));
 
 		return ret;
 	}
 
 	@Override
 	public Iterable<Cell<T>> readColumn(ByteString columnKeyPrefix) {
 		int startPos = colIndexStartPos(columnKeyPrefix);
 		int endPos = colIndex.get().size();
 		if (!columnKeyPrefix.isEmpty()) {
 			if (isKeyAllFF(columnKeyPrefix)) {
 				assert startPos == store.size() - 1;
 			} else {
 				endPos = colIndexStartPos(keyPlusOne(columnKeyPrefix));
 			}
 		}
 
 		List<RowPointer> rows = colIndex.get().subList(startPos, endPos);
 
 		List<Cell<T>> ret = new ArrayList<>();
 		for (RowPointer r : rows) {
 			int shard = findShard(r.rowKey);
 			assert shard >= 0 : "Index contained incorrect information for row " + r.rowKey.toStringUtf8() + " col: "
 					+ columnKeyPrefix.toStringUtf8() + store;
 
 			int p = Collections.binarySearch(store.get(shard), new Cell<T>(r.rowKey, r.colKey, ByteString.EMPTY));
 			assert p >= 0 : "Index contained incorrect information for row " + r.rowKey.toStringUtf8() + " col: "
 					+ columnKeyPrefix.toStringUtf8() + store;
 			ret.add(store.get(shard).get(p));
 		}
 
 		return ret;
 	}
 
 	/** @return true if key consists only of bytes 0xff. False otherwise. */
 	private boolean isKeyAllFF(ByteString key) {
 		for (int i = 0; i < key.size(); i++) {
 			if (key.byteAt(i) != -1) { // -1 signed == 0xff unsigned.
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/** @return the index of shard that could contain the key prefix. -1 if there's none. */
 	private int findShard(ByteString needle) {
 		if (splitIndex.isEmpty() || cmp.compare(needle, splitIndex.get(splitIndex.size() -1)) > 0) {
 			return -1;
 		}
 
 		int ret = Collections.binarySearch(splitIndex, needle, cmp);
 		if (ret < 0) {
 			return ~ret;
 		}
 
 		return ret;
 	}
 
 	private static <T> boolean isStoreOk(List<List<Cell<T>>> store) {
 		for (List<Cell<T>> shard : store) {
 			if (!Ordering.<Cell<T>> natural().isOrdered(shard)) {
 				return false;
 			}
 		}
 
 		List<Cell<T>> prevShard = null;
 		for (List<Cell<T>> cur : store) {
 			if (cur.isEmpty()) {
 				continue;
 			}
 
 			if (prevShard != null && prevShard.get(prevShard.size() - 1).compareTo(cur.get(0)) >= 0) {
 				return false;
 			}
 
 			prevShard = cur;
 		}
 
 		Iterable<Cell<T>> flatStore = Iterables.concat(store);
 
 		Cell<T> prevCell = null;
 		for (Cell<T> c : flatStore) {
 			if (c.equals(prevCell)) {
 				return false;
 			}
 			prevCell = c;
 		}
 
 		return true;
 	}
 
 	private ByteString keyPlusOne(ByteString key) {
 		assert !key.isEmpty() : "This case needs special treatment on caller level.";
 		return ByteString.copyFrom(new BigInteger(key.toByteArray()).add(BigInteger.ONE)
 				.toByteArray());
 	}
 
 	/** @return the index of row pointer that could contain the column key prefix. */
 	private int colIndexStartPos(ByteString colKeyPrefix) {
 		int pos = Collections.binarySearch(colIndex.get(), new RowPointer(colKeyPrefix, ByteString.EMPTY));
 		if (pos < 0) {
 			pos = ~pos;
 		}
 
 		return pos;
 	}
 }
