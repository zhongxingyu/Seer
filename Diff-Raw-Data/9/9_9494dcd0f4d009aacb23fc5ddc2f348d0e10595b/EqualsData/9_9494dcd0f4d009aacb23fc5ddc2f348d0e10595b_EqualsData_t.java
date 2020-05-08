 package edu.elon.honors.price.data.field;
 
 import java.util.List;
 
 public class EqualsData extends ValueData {
 	
 	protected boolean inLeftEquals;
 	protected boolean inRightEquals;
 
 	// the last ___ added by the object
 	private Object lastObject;
 	private int lastInt;
 	private long lastLong;
 	private short lastShort;
 	private double lastDouble;
 	private float lastFloat;
 	private byte lastByte;
 	private char lastChar;
 	private boolean lastBoolean;
 
 	// used during equality comparison
 	private boolean equalSoFar;
 	// the index of the field we're comparing now
 	private int fieldIndex;
 	// the other HashCode we're comparing to
 	private EqualsData compareTo;	
 	// the field we are attempting to read from a hash code
 	private int desiredFieldIndex;
 	
 	protected void addFields(DataObject dataObject) 
 			throws NumberFormatException, ParseDataException {
 		dataObject.addFields(this);
 	}
 	
 	public EqualsData(DataObject dataObject) {
 		super(dataObject);
 	}
 	
 	protected void reset() {
 		lastObject = null;
 		compareTo = null;
 		inLeftEquals = false;
 		inRightEquals = false;
 	}
 
 	protected void addLeftEquals(Object o) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		boolean eq;
 		Object o2 = compareTo.lastObject;
 		if (o == null) {
 			eq = o2 == null;
 		} else {
 			eq = o.equals(o2);
 			if (!eq && o.getClass() == o2.getClass()) {
 				if (o instanceof int[]) {
 					eq = areArraysEqual(o, o2, ArrayIO.intIO);
 				} else if (o instanceof boolean[]) {
 					eq = areArraysEqual(o, o2, ArrayIO.booleanIO);
 				} else if (o instanceof String[]) {
 					eq = areArraysEqual(o, o2, ArrayIO.stringIO);
 				} else if (o instanceof int[][]) {
 					eq = areArraysEqual(o, o2, int2dIO);
 				}
 			}
 		}
 		if (!eq) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected <T extends DataObject> void addLeftEquals(T[] o) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		boolean eq;
 		T[] o2 = (T[]) compareTo.lastObject;
 		if (o == null) {
 			eq = o2 == null;
 		} else {
 			eq = areArraysEqual(o, o2);
 		}
 		if (!eq) equalSoFar = false;
 		fieldIndex++;
 	}
 	
 	private static <T extends DataObject>  boolean areArraysEqual(T[] a1, T[] a2) {
 		if (a1.length != a2.length) return false;
 		for (int i = 0; i < a1.length; i++) {
 			if (!a1[i].equals(a2[i])) return false;
 		}
 		return true;
 	}
 
 	protected void addLeftEquals(int i) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (i != compareTo.lastInt) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(long l) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (l != compareTo.lastLong) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(short s) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (s != compareTo.lastShort) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(float f) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (f != compareTo.lastFloat) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(double d) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (d != compareTo.lastDouble) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(byte b) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (b != compareTo.lastByte) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(char c) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (c != compareTo.lastChar) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	protected void addLeftEquals(boolean b) {
 		if (!equalSoFar) return;
 		compareTo.populateField(fieldIndex);
 		if (b != compareTo.lastBoolean) equalSoFar = false;
 		fieldIndex++;
 	}
 
 	// methods for reading a field from another HashCode
 
 	protected void addRightEquals(Object o) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastObject = o;
 	}
 
 	protected void addRightEquals(int i) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastInt = i;
 	}
 
 	protected void addRightEquals(long l) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastLong = l;
 	}
 
 	protected void addRightEquals(short s) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastShort = s;
 	}
 
 	protected void addRightEquals(float f) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastFloat = f;
 	}
 
 	protected void addRightEquals(double d) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastDouble = d;
 	}
 
 	protected void addRightEquals(byte b) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastByte = b;
 	}
 
 	protected void addRightEquals(char c) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastChar = c;
 	}
 
 	protected void addRightEquals(boolean b) {
 		if (desiredFieldIndex >= 0 && fieldIndex++ != desiredFieldIndex) return;
 		lastBoolean = b;
 	}
 	
 	public void addHashField(Object o) {
 		if (inLeftEquals) addLeftEquals(o);
 		if (inRightEquals) addRightEquals(o);
 	}
 	
 	public <T extends DataObject> void addHashField(T[] o) {
 		if (inLeftEquals) addLeftEquals(o);
 		if (inRightEquals) addRightEquals(o);
 	}
 
 	public void addHashField(int i) {
 		if (inLeftEquals) addLeftEquals(i);
 		if (inRightEquals) addRightEquals(i);
 	}
 
 	public void addHashField(long l) {
 		if (inLeftEquals) addLeftEquals(l);
 		if (inRightEquals) addRightEquals(l);
 	}
 
 	public void addHashField(short s) {
 		if (inLeftEquals) addLeftEquals(s);
 		if (inRightEquals) addRightEquals(s);
 	}
 
 	public void addHashField(float f) {
 		if (inLeftEquals) addLeftEquals(f);
 		if (inRightEquals) addRightEquals(f);
 	}
 
 	public void addHashField(double d) {
 		if (inLeftEquals) addLeftEquals(d);
 		if (inRightEquals) addRightEquals(d);
 	}
 
 	public void addHashField(byte b) {
 		if (inLeftEquals) addLeftEquals(b);
 		if (inRightEquals) addRightEquals(b);
 	}
 
 	public void addHashField(char c) {
 		if (inLeftEquals) addLeftEquals(c);
 		if (inRightEquals) addRightEquals(c);
 	}
 
 	public void addHashField(boolean b) {
 		if (inLeftEquals) addLeftEquals(b);
 		if (inRightEquals) addRightEquals(b);
 	}
 	
 	public int add(int x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public long add(long x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public short add(short x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public float add(float x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public double add(double x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public byte add(byte x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public char add(char x) throws ParseDataException { 
 		addHashField(x);
 		return x;
 	}
 	
 	public boolean add(boolean x) throws ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public String add(String x) throws ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x, Class<? extends T> clazz) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x, String clazz) throws ParseDataException, NumberFormatException {
 		addHashField(x);
 		return x;
 	}
 	
 	// Arrays are stored int the format "length \n 1|2|3|4"	
 	public boolean[] addArray(boolean[] x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public int[] addArray(int[] x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public String[] addArray(String[] x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public int[][] add2DArray(int[][] x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T[] addArray(T[] x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public <T extends DataObject, L extends List<T>> L addList(L x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	/** Persists the list, using the provided class for reconstruction. See {@link Data#persist(Persistable, Class)} */
 	public <T extends DataObject, L extends List<T>> L addList(L x, Class<? extends T> clazz) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public List<Integer> addIntList(List<Integer> x) throws NumberFormatException, ParseDataException  {
 		addHashField(x);
 		return x;
 	}
 
 	public List<String> addStringList(List<String> x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	public List<Boolean> addBooleanList(List<Boolean> x) throws NumberFormatException, ParseDataException {
 		addHashField(x);
 		return x;
 	}
 	
 	
 	// Iterates through the other hashable's fields
 	// until it reads the field with the desired index.
 	// The desired field, therefore, will be stored in one
 	// of the "lastXXX" fields of this class
 	protected void populateField(int index) {
 		inRightEquals = true;
 		desiredFieldIndex = index;
 		fieldIndex = 0;
 		try {
 			addFields(dataObject);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		inRightEquals = false;
 	}
 
 	/** 
 	 * Compares this HashCode to another, based on the fields added by
 	 * its Hashable
 	 */
 	public boolean equals(EqualsData otherData) {
 		//TODO: support inheritance.. maybe?
 
 		// check for obvious incompatibility
 		if (otherData == null) return false;
 		DataObject otherObject = otherData.dataObject;
 		if (this.dataObject == otherObject) return true;
 		if (this.dataObject == null || otherObject == null) return false;
 		if (this.dataObject.getClass() != otherObject.getClass()) return false;
 
 		// by definition, if the hashcodes aren't equal, nor are the fields 
 		if (otherObject.hashCode() != dataObject.hashCode()) return false;
 
 		// The process works by having our DataObject
 		// add each of its fields. After every one,
 		// we have the other DataObject add its fields
 		// but we only store the one that matches the field
 		// out DataObject just added. Then we compare them and repeat
 		// until we find an inequality or all the fields have been added.
 
 		equalSoFar = true;
 		inLeftEquals = true;
 		compareTo = otherData;
 		desiredFieldIndex = -1;
 		fieldIndex = 0;
 		try {
 			addFields(dataObject);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		inLeftEquals = false;
 		return equalSoFar;
 	}
 }
