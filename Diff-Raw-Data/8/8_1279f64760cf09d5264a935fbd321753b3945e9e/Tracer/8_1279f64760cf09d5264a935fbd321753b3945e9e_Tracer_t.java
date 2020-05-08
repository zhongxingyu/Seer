package hu.szintaxis;
 
 
 /*
  * Tracer osztly a szkeletonhoz a metdusok kvetsre.
  */
 public class Tracer {
 
 	public enum Direction {
 		Enter,
 		Leave
 	}
 	private static Tracer instance;
 	private int depth = 0;
 	
 	public static Tracer Instance(){
 		if(instance == null)
 			instance = new Tracer();
 		return instance;
 	}
 	
 	public void Trace(Direction direction, Object... arguments){
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		StringBuilder sBuilder = new StringBuilder();
 		//Kilpsnl a mlysget azeltt kell cskkenteni, hogy szmolnnk vele.
 		if (direction == Direction.Leave)
 			depth--;
 		
 		//Kiszmolja a behzst.
 		for (int i = 0; i < depth; i++) {
 			sBuilder.append("\t");
 		}
 		
 		if (direction == Direction.Enter)
 			depth++;
 		
 		//Irnyt hozzfzi a stringhez.
 		switch (direction) {
 		case Enter:
 			sBuilder.append(">>> | ");
 			break;
 		case Leave:
 			sBuilder.append("<<< | ");
 			break;
 		}
 		
 		//osztly s metdus nv stringhez fzse 
 		//(a stackben a 0. elem a getStackTrace, az 1. a Print, s a 2. az aktulis metdus ami rdekel minket)
 		String className = stack[2].getFileName();
 		className = className.substring(0, className.length()-5);
 		sBuilder.append(className);
 		sBuilder.append(".");
 		sBuilder.append(stack[2].getMethodName());
 		
 		//Esetleges argumentumok kirsa
 		if (direction == Direction.Enter) {
 			sBuilder.append("(");
 			for (Object object : arguments) {
 				sBuilder.append(object.toString());
 			}
 			sBuilder.append(")");
 		}
 		
 		//Esetleges visszatrsi rtk kirsa
 		if (direction == Direction.Leave && arguments.length == 1) {
 			sBuilder.append(":");
 			sBuilder.append(arguments[0].toString());
 		}
 		
 		//Elkszlt string rsa.
 		System.out.println(sBuilder.toString());
 	}
 }
