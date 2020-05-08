 package org.publicmain.common;
 import java.sql.Time;
 
 import org.publicmain.nodeengine.ConnectionHandler;
 
 
 /**GGF mal ber java.util.logging nachdenken
  * @author tkessels
  *
  */
 public class LogEngine {
 	private static int verbosity=3;
 	public static final int INFO=3;
 	public static final int WARNING=2;
 	public static final int ERROR=1;
 	public static final int NONE=0;
 	
 	/**Gibt eine Exception auf dem Programm Fehlerstrom aus
 	 * @param e Die zu dokumentierende Exception
 	 */
 	public static void log(Exception e) {
 		if(verbosity>0){
 			log(e.getMessage(),ERROR);
 			//e.printStackTrace();
 		}
 	}
 	
 	/** Setzt die Meldeschwelle ab welchem schweregrad eine Ausgabe erfolgen soll.
 	 * @param x Das Verbosittslevel ?!?!
 	 */
 	public void serVerbosity(int x){
 		verbosity=x;
 	}
 	
 	/**Gibt eine Fehlermeldung auf dem Fehlerstrom aus 
 	 * @param meldung Der Text der Fehlermeldung
 	 * @param source Die Quelle des Fehlers
 	 * @param errorLevel Das Niveau des Fehlers (INFO, WARNING oder ERROR)
 	 */
 	public static void log(String meldung, Object source,int errorLevel){
 		if(errorLevel<=verbosity){
 			log(source.getClass().getSimpleName()+" : "+meldung,errorLevel);
 		}
 	}
 	
 	private static String msg2String(MSG x){
		return "MSG{"+x.getTyp()+"("+((x.getCode()!=null)?x.getCode():"")+((x.getGroup()!=null)?x.getGroup():"")+")"+ "\t:"+Math.abs(x.getSender()%10000)+"("+x.getId()+")"+">"+Math.abs(x.getEmpfnger()%10000)+"["+x.getData()+"]}";
 	}
 	
 	
 	
 	public static void log(Object source,String action,MSG x){
 		String sourceString=(source instanceof String)?(String)source:source.getClass().getSimpleName() ;
 		log(sourceString+ " : " + action+ " : "+msg2String(x),INFO);
 	}
 	
 	public static void log(ConnectionHandler newConnection){
 		
 	}
 	
 	public static void log(String meldung,int errorLevel){
 		if(errorLevel<=verbosity){
 			System.err.println(new Time(System.currentTimeMillis()).toString()+" : "+meldung);
 		}
 	}
 	
 	
 	
 
 }
