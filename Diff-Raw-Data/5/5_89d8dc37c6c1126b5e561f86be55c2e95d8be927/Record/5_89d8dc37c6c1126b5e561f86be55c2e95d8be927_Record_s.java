 package edu.berkeley.gamesman.core;
 
 import java.util.EnumMap;
 import java.util.Map.Entry;
 
 /**
  * Stores information about a game state
  * 
  * @author dnspies
  */
 public final class Record {
 	private final Configuration conf;
 
 	private final int[] values;
 
 	Record(Configuration conf, long state) {
 		this.conf = conf;
 		values = new int[conf.numFields()];
 		long remainingState = state;
 		int i = 0;
 		for (RecordFields rf : conf.usedFields) {
 			int val = conf.getFieldStates(rf);
 			this.values[i++] = (int) (remainingState % val);
 			remainingState /= val;
 		}
 	}
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 * @param values
 	 *            The values of each of the respective fields in this record
 	 *            ordered VALUE, REMOTENESS, SCORE
 	 */
 	public Record(Configuration conf, int... values) {
 		this.conf = conf;
 		this.values = new int[conf.numFields()];
 		for (int i = 0; i < values.length; i++) {
 			this.values[i] = values[i];
 		}
 	}
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 * @param pVal
 	 *            Just the primitive value. All other fields are initialized to
 	 *            zero.
 	 */
 	public Record(Configuration conf, PrimitiveValue pVal) {
 		this.conf = conf;
 		this.values = new int[conf.numFields()];
 		int i = 0;
 		for (RecordFields rf : conf.usedFields) {
 			if (rf.equals(RecordFields.VALUE))
 				this.values[i++] = pVal.value();
 			else
 				this.values[i++] = 0;
 		}
 	}
 
 	/**
 	 * Creates an empty record that can be written to.
 	 * 
 	 * @param conf
 	 *            The configuration object
 	 */
 	public Record(Configuration conf) {
 		this.conf = conf;
 		values = new int[conf.numFields()];
 	}
 
 	/**
 	 * @param field
 	 *            The field to change
 	 * @param value
 	 *            The new value of the field
 	 */
 	public void set(RecordFields field, int value) {
 		values[conf.getFieldIndex(field)] = value;
 	}
 
 	public void set(Record record) {
 		for (int i = 0; i < values.length; i++)
 			values[i] = record.values[i];
 	}
 
 	/**
 	 * @param rf
 	 *            The type of one block of information
 	 * @return The information encoded as a long
 	 */
 	public int get(RecordFields rf) {
 		return values[conf.getFieldIndex(rf)];
 	}
 
 	/**
 	 * @return The primitive value of this position
 	 */
 	public PrimitiveValue get() {
 		return PrimitiveValue.values[(get(RecordFields.VALUE))];
 	}
 
 	/**
 	 * @return The integer value of this record
 	 */
 	public long getState() {
 		long currentState = 0;
 		long multiplier = 1;
 		for (int i = 0; i < values.length; i++) {
 			currentState += values[i] * multiplier;
 			multiplier *= conf.storedFields[i];
 		}
 		return currentState;
 	}
 
 	/**
 	 * Changes this record to the previous position. WARNING! Does not change
 	 * the score. You must do that yourself.
 	 */
 	public void previousPosition() {
 		set(RecordFields.VALUE, get().previousMovesValue().value());
 		if (conf.containsField(RecordFields.REMOTENESS))
 			set(RecordFields.REMOTENESS, get(RecordFields.REMOTENESS) + 1);
 	}
 
 	@Override
 	public boolean equals(Object r) {
 		if (r instanceof Record) {
 			Record rec = (Record) r;
			return values.equals(rec.values);
 		} else
 			return false;
 	}
 
 	@Override
 	public String toString() {
 		String s = PrimitiveValue.values[values[conf
 				.getFieldIndex(RecordFields.VALUE)]].name();
 		if (conf.containsField(RecordFields.REMOTENESS))
 			return s + " in "
 					+ values[conf.getFieldIndex(RecordFields.REMOTENESS)];
 		else
 			return s;
 	}
 
 	private Record(Record record) {
 		this.conf = record.conf;
 		this.values = new int[conf.numFields()];
 		for (int i = 0; i < values.length; i++)
 			values[i] = record.values[i];
 	}
 
 	@Override
 	public Record clone() {
 		return new Record(this);
 	}
 
 	public void set(long state) {
 		long remainingState = state;
 		for (int i = 0; i < values.length; i++) {
 			int val = conf.storedFields[i];
 			values[i] = (int) (remainingState % val);
 			remainingState /= val;
 		}
 	}
 }
