 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class History implements Serializable {
 
 	private static final long serialVersionUID = 2802095781867809709L;
 
 	private String ptuId;
 	private String name;
 	private Number[][] data;
 	private int index;
 	private String unit;
 
 	private final static int INITIAL_CAPACITY = 200;
 	private final static int TIME = 0;
 	private final static int VALUE = 1;
 	private final static int LOW_LIMIT = 2;
 	private final static int HIGH_LIMIT = 3;
 	private final static int SAMPLING_RATE = 4;
 
 	private final static int SIZE = SAMPLING_RATE + 1;
 
 	public History() {
 	}
 
 	public History(String ptuId, String name, String unit) {
 		this.ptuId = ptuId;
 		this.name = name;
 		this.unit = unit;
 		this.data = new Number[INITIAL_CAPACITY][SIZE];
 		index = 0;
 	}
 
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Number[][] getData() {
 		Number[][] result = new Number[index][2];
 
 		// FIXME #4 maybe needs handcopy, seems to work
 		System.arraycopy(data, 0, result, 0, index);
 		return result;
 	}
 
 	public Number[][] getLimits() {
 		Number[][] result = new Number[index][3];
 
 		for (int i = 0; i < index; i++) {
 			result[i][TIME] = data[i][TIME];
 			result[i][1] = data[i][LOW_LIMIT];
 			result[i][2] = data[i][HIGH_LIMIT];
 		}
 		return result;
 	}
 
 	public String getUnit() {
 		return unit;
 	}
 
 	public boolean addEntry(long time, Number value, Number lowLimit,
 			Number highLimit, Integer samplingRate) {
		// Temporary FIX for #376 and #377 (PRISMA)
		if (name.equalsIgnoreCase("DoseAccum") && (value.doubleValue() == 0)) {
			return false;
		}
		
 		if (index >= data.length) {
 			Number[][] newData = new Number[data.length * 2][SIZE];
 			System.arraycopy(data, 0, newData, 0, data.length);
 			data = newData;
 		}
 
 		data[index][TIME] = time;
 		data[index][VALUE] = value;
 		data[index][LOW_LIMIT] = lowLimit;
 		data[index][HIGH_LIMIT] = highLimit;
 		data[index][SAMPLING_RATE] = samplingRate;
 		index++;
 
 		return true;
 	}
 
 	public Measurement getMeasurement() {
 		int last = index - 1;
 		return index == 0 ? null : new Measurement(ptuId, name,
 				data[last][VALUE], data[last][LOW_LIMIT],
 				data[last][HIGH_LIMIT], unit,
 				data[last][SAMPLING_RATE].intValue(), new Date(
 						data[last][TIME].longValue()));
 	}
 
 	public int getSize() {
 		return index;
 	}
 }
