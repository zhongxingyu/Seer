 package edu.elon.honors.price.data.field;
 
 import java.util.List;
 
import javax.xml.crypto.Data;

 public class HashData extends ValueData {
 
 	private int result = 1;
 
 	protected boolean inHash;
 
 	protected void addFields(DataObject dataObject) 
 			throws NumberFormatException, ParseDataException {
 		dataObject.addFields(this);
 	}
 	
 	public HashData(DataObject dataObject) {
 		super(dataObject);
 	}
 	
 	protected void reset() {
 		inHash = false;
 	}
 
 	@Override
 	public int hashCode() {
 		inHash = true;
 		result = 1;
 		try {
 			addFields(dataObject);
 		} catch (Exception e) {
 			e.printStackTrace();
 			result = 0;
 		}
 		inHash = false;
 		return result;
 	}
 	
 	// methods for adding a field for hashcode generation
 
 	protected void addHash(Object o) {
 		result = prime * result;
 		if (o == null) return;
 		
 		int hash;
 		if (o instanceof int[]) {
 			hash = hashArray(o, ArrayIO.intIO);
 		} else if (o instanceof boolean[]) {
 			hash = hashArray(o, ArrayIO.booleanIO);
 		} else if (o instanceof String[]) {
 			hash = hashArray(o, ArrayIO.stringIO);
 		} else if (o instanceof int[][]) {
 			hash = hashArray(o, int2dIO);
 		} else {
 			hash = o.hashCode();
 		}
 		result += hash;
 	}
 	
 	protected <T extends DataObject> void addHash(T[] o) {
 		result = prime * result;
 		if (o == null) return;
 		result += hashArray(o);
 	}
 	
 	private static <T extends DataObject> int hashArray(T[] a) {
 		int length = a.length;
 		int result = 1;
 		for (int i = 0; i < length; i++) {
 			result = prime * result + a[i].hashCode();
 		}
 		return result;
 	}
 
 	protected void addHash(int i) {
 		result = prime * result + i;
 	}
 
 	protected void addHash(long l) {
 		result = prime * result + (int) (l ^ (l >>> 32));
 	}
 
 	protected void addHash(short s) {
 		result = prime * result + s;
 	}
 
 	protected void addHash(float f) {
 		result = prime * result + Float.floatToIntBits(f);
 	}
 
 	protected void addHash(double d) {
 		long temp;
 		temp = Double.doubleToLongBits(d);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 	}
 
 	protected void addHash(byte b) {
 		result = prime * result + b;
 	}
 
 	protected void addHash(char c) {
 		result = prime * result + c;
 	}
 
 	protected void addHash(boolean b) {
 		result = prime * result + (b ? 1231 : 1237);
 	}
 	
 	public int add(int x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public long add(long x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public short add(short x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public float add(float x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public double add(double x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public byte add(byte x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public char add(char x) throws ParseDataException { 
 		addHash(x);
 		return x;
 	}
 	
 	public boolean add(boolean x) throws ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public String add(String x) throws ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x, Class<? extends T> clazz) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T add(T x, String clazz) throws ParseDataException, NumberFormatException {
 		addHash(x);
 		return x;
 	}
 	
 	public boolean[] addArray(boolean[] x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public int[] addArray(int[] x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public String[] addArray(String[] x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public int[][] add2DArray(int[][] x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public <T extends DataObject> T[] addArray(T[] x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public <T extends DataObject, L extends List<T>> L addList(L x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	/** Persists the list, using the provided class for reconstruction. See {@link Data#persist(Persistable, Class)} */
 	public <T extends DataObject, L extends List<T>> L addList(L x, Class<? extends T> clazz) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public List<Integer> addIntList(List<Integer> x) throws NumberFormatException, ParseDataException  {
 		addHash(x);
 		return x;
 	}
 
 	public List<String> addStringList(List<String> x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 	
 	public List<Boolean> addBooleanList(List<Boolean> x) throws NumberFormatException, ParseDataException {
 		addHash(x);
 		return x;
 	}
 }
