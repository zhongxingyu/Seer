 /**
  * 
  */
 package commons.util;
 
 /**
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public enum DataUnit {
 	
 	B(1), 
	KB(1024 * B.getBytes()),
 	MB(1024 * KB.getBytes()),
 	GB(1024 * MB.getBytes()),
 	TB(1024 * GB.getBytes());
 	
 	private final long dataInBytes;
 	
 	/**
 	 * Default private constructor.
 	 */
 	private DataUnit(long dataInBytes) {
 		this.dataInBytes = dataInBytes;
 	}
 
 	/**
 	 * @return
 	 */
 	public long getBytes() {
 		return dataInBytes;
 	}
 	
 	public DataUnit getUnit(String unit){
 		return DataUnit.valueOf((unit + "b").toUpperCase());
 	}
 	
 	public static double [][] convert(double values[][], DataUnit from, DataUnit to){
 		double rate = 1.0 * from.dataInBytes / to.dataInBytes;
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[i].length; j++) {
 				values[i][j] = values[i][j] * rate; 
 			}
 		}
 		return values;
 	}
 
 	public static double [] convert(double values[], DataUnit from, DataUnit to){
 		double rate = 1.0 * from.dataInBytes / to.dataInBytes;
 		for (int i = 0; i < values.length; i++) {
 			values[i] = values[i] * rate; 
 		}
 		return values;
 	}
 
 	public static long [][] convert(long values[][], DataUnit from, DataUnit to){
 		long rate = from.dataInBytes / to.dataInBytes;
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[i].length; j++) {
 				values[i][j] = values[i][j] * rate; 
 			}
 		}
 		return values;
 	}
 
 	public static long [] convert(long values[], DataUnit from, DataUnit to){
 		long rate = from.dataInBytes / to.dataInBytes;
 		for (int i = 0; i < values.length; i++) {
 			values[i] = values[i] * rate; 
 		}
 		return values;
 	}
 }
