 package medallia.runner;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableListMultimap;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimaps;
 import com.google.common.io.ByteStreams;
 import medallia.sim.FieldLayoutSimulator;
 import medallia.sim.RecordLayoutSimulator;
 import medallia.sim.RecordProcessor;
 import medallia.sim.SimulatorFactory;
 import medallia.sim.data.DatasetLayout;
 import medallia.sim.data.Field;
 import medallia.util.SimulatorUtil;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.List;
 import java.util.Random;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static medallia.util.SimulatorUtil.toSi;
 import static medallia.util.SimulatorUtil.unused;
 
 /** Simulator analysis result */
 public class Analysis {
 	/** Rate of records that are used for training vs total records */
 	public static final double SURVIVAL_RATE = 0.1;
 	public static final Function<Field,String> FIELD_BY_NAME = new Function<Field, String>() {
 		@Override
 		public String apply(Field field) {
 			return field.name;
 		}
 	};
 	public final long totalRows;
 	public final int totalLayouts;
 	public final int fields;
 	public final int columns;
 	public final int segments;
 	public final double usedBitsPercent;
 	public final double usedColumnsPercent;
 	public final long usedBytes;
 	public final long usedBits;
 	public final long[] layoutCounts;
 
 	public Analysis(long totalRows, int totalLayouts, int fields, int columns, int segments, double usedBitsPercent, double usedColumnsPercent, long usedBytes, long usedBits, long[] layoutCounts) {
 		this.totalRows = totalRows;
 		this.totalLayouts = totalLayouts;
 		this.fields = fields;
 		this.columns = columns;
 		this.segments = segments;
 		this.usedBitsPercent = usedBitsPercent;
 		this.usedColumnsPercent = usedColumnsPercent;
 		this.usedBytes = usedBytes;
 		this.usedBits = usedBits;
 		this.layoutCounts = layoutCounts;
 	}
 
 	/**
 	 * Analyze the result of a simulator.
 	 * <p>
 	 * This calculates the raw data null-coverage, the amount of columns used,
 	 * the amount of columns that would be shareable as null-vectors, and
 	 * finally the total dataset size.
 	 */
 	private static Analysis analyze(final List<int[]> segments, final List<Field> fields, final DatasetLayout stats) {
 		checkFields(fields, stats);
 
 		// Non-null values in column across entire dataset
 		final long[] counts = new long[fields.size()];
 
 		// Number of times layout id has been used
 		final long[] layoutCounts = new long[stats.layouts.length];
 
 		final int columns = SimulatorUtil.columnCount(fields);
 
 		// Number of bits per column
 		final int[] bitsPerColumn = new int[columns];
 
 		// Number of segments where this column has at least one
 		// non-null value.
 		final int[] columnUsedInSegments = new int[columns];
 
 		// Map from field id to column number
 		final int[] columnMap = new int[fields.size()];
 
 		// Map from bit index in the layout to relocated field
 		final int[] bitFieldMap = new int[fields.size()];
 
 		for (int i = 0; i < fields.size(); ++i) {
 			final Field field = fields.get(i);
 			columnMap[i] = field.column;
 			bitsPerColumn[field.column] += field.size;
 			bitFieldMap[field.getIndex()] = i;
 		}
 
 		// Check that fields are not over-allocated
 		for (int i = 0; i < bitsPerColumn.length; i++) {
 			checkArgument(bitsPerColumn[i] <= 32, "Column %s is using %s bits", i, bitsPerColumn[i]);
 		}
 
 		// Total rows in entire dataset
 		long totalRows = 0;
 
 		// Number of allocated column positions
 		long allocatedValues = 0;
 
 		for (int[] rows : segments) {
 			boolean[] columnUsed = new boolean[columns];
 			for (int id : rows) {
 				++layoutCounts[id];
 				BitSet bitSet = stats.layouts[id];
 				for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
 					++counts[bitFieldMap[i]];
 					columnUsed[columnMap[bitFieldMap[i]]] = true;
 				}
 				++totalRows;
 			}
 			for (int i = 0; i < columns; ++i)
 				if (columnUsed[i]) {
 					++columnUsedInSegments[i];
 					allocatedValues += rows.length;
 				}
 		}
 
 		// Total bits that cover non-null values
 		long usedBits = 0;
 		// Total number of bits allocated
 		long totalBits = 0;
 		for (int i = 0; i < fields.size(); ++i) {
 			totalBits += fields.get(i).size * totalRows;
 			usedBits += fields.get(i).size * counts[i];
 		}
 
 		// Number of non-null column vectors in all segments
 		long usedColumns = 0;
 
 		// Total number of column vectors
 		long totalColumns = 0;
 		for (int i = 0; i < columns; ++i) {
 			totalColumns += segments.size();
 			usedColumns += columnUsedInSegments[i];
 		}
 
		final int perSegmentCost = 40 * columns + 4096;
 		return new Analysis(
 				totalRows,
 				stats.layouts.length - 1,
 				fields.size(),
 				columns,
 				segments.size(),
 				(usedBits * 100.0) / totalBits,
 				(usedColumns * 100.0) / totalColumns,
 				allocatedValues * 4 + segments.size() * (perSegmentCost),
 				usedBits,
 				layoutCounts
 		);
 	}
 
 	private static void checkFields(List<Field> fields, DatasetLayout stats) {
 		final ImmutableMap<String, Field> srcFieldsByName = Maps.uniqueIndex(Arrays.asList(stats.fields), FIELD_BY_NAME);
 
 		for (Field field : fields) {
 			Field srcField = srcFieldsByName.get(field.name);
 			checkArgument(srcField != null, "Field %s is not in source data", field.name);
 			checkArgument(srcField.size == field.size, "Field %s has different size", field.name);
 			checkArgument(srcField.getIndex() == field.getIndex(), "Field %s has different index", field.name);
 		}
 		final ImmutableMap<String, Field> dstFieldsByName = Maps.uniqueIndex(fields, FIELD_BY_NAME);
 		for (Field srcField : stats.fields) {
 			checkArgument(dstFieldsByName.containsKey(srcField.name), "Field %s is in source data but not in finished layout", srcField.name);
 		}
 	}
 
 	private static long computeUsedBits(DatasetLayout stats) {
 		long usedBits = 0;
 		for (int[] segment : stats.segments) {
 			for (int row : segment) {
 				BitSet layout = stats.layouts[row];
 				for (int i = layout.nextSetBit(0); i >= 0; i = layout.nextSetBit(i+1)) {
 					usedBits += stats.fields[i].size;
 				}
 			}
 		}
 		return usedBits;
 	}
 
 	private static long[] computeLayoutCounts(DatasetLayout stats) {
 		long[] layoutCounts = new long[stats.layouts.length];
 		for (int[] layouts : stats.segments)
 			for (int idx : layouts)
 					layoutCounts[idx]++;
 		return layoutCounts;
 	}
 
 	public static Analysis simulateCompany(final SimulatorFactory fac, final DatasetLayout stats) {
 		// Capture the state of the DatasetLayout before a simulator is ran
 		final long[] layoutCounts = computeLayoutCounts(stats);
 		final long usedBits = computeUsedBits(stats);
 		final byte[] hash = computeHash(stats);
 
 		List<Field> fields = ImmutableList.copyOf(Iterables.transform(Arrays.asList(stats.fields), new Function<Field, Field>() {
 			@Override
 			public Field apply(Field field) {
 				return new Field(field, field.column);
 			}
 		}));
 
 		final BitSet[] layoutCopy = stats.layouts.clone();
 		for (int i = 0; i < layoutCopy.length; i++) {
 			if (layoutCopy[i] != null) {
 				layoutCopy[i] = (BitSet) layoutCopy[i].clone();
 			}
 		}
 
 		FieldLayoutSimulator fieldLayoutSimulator = fac.createFieldLayoutSimulator(layoutCopy, fields);
 		if (fieldLayoutSimulator != null) {
 			processRecords(stats, fieldLayoutSimulator, SURVIVAL_RATE);
 			fields = fieldLayoutSimulator.getFields();
 		}
 
 		final RecordLayoutSimulator sim = fac.createRecordLayoutSimulator(layoutCopy, fields);
 
 		// Allow GC of potentially massive object
 		fieldLayoutSimulator = null;
 		unused(fieldLayoutSimulator);
 
 		processRecords(stats, sim, 1);
 		sim.flush();
 
 		final Analysis analysis = analyze(ImmutableList.copyOf(sim.getSegments()), ImmutableList.copyOf(fields), stats);
 
 		// Do a sanity check
 		checkArgument(usedBits == analysis.usedBits, "Bit counts do not match");
 		checkArgument(Arrays.equals(layoutCounts, analysis.layoutCounts), "Layout counts do not match");
 		checkArgument(Arrays.equals(hash, computeHash(stats)), "Layout information was modified during run");
 
 		return analysis;
 	}
 
 	private static byte[] computeHash(final DatasetLayout stats) {
 		try {
 			final MessageDigest md5 = MessageDigest.getInstance("MD5");
 			try (final ObjectOutputStream oos = new ObjectOutputStream(new DigestOutputStream(ByteStreams.nullOutputStream(), md5))) {
 				oos.writeObject(stats);
 			}
 			return md5.digest();
 		} catch (IOException|NoSuchAlgorithmException e) {
 			throw new AssertionError(e);
 		}
 	}
 
 
 	private static void processRecords(DatasetLayout stats, RecordProcessor processor, double survivalRate) {
 		// Always use the same PRNG so results are comparable
 		final Random prng = new Random(0);
 		for (int[] layouts : stats.segments)
 			for (int idx : layouts)
 				if (survivalRate < 0 || survivalRate >= 1 || prng.nextDouble() < survivalRate)
 					processor.processRecord(idx);
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s rows (%s layouts), %s fields (%s columns in %s segments): %.1f%% used-data, %.1f%% used-columns, %s",
 				totalRows, totalLayouts, fields, columns, segments, usedBitsPercent, usedColumnsPercent, toSi(usedBytes));
 	}
 }
