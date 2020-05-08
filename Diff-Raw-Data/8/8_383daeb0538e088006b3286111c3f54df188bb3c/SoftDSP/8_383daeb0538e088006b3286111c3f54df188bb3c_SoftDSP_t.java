 package uk.co.peopleandroid.jarvar.internals;
 
 public class SoftDSP extends Thread {
 	
 	boolean running = true;
 
 	public void run() {
 		while(running) {
 			eval();
 			Thread.yield();
 		}
 	}
 	
 	public void destroy() {
 		running = false;
 	}
 	
 	int mem[] = new int[1024];
 	
 	void eval() {
 		int pc = mem[0];
 		int a = pc >>> 24;//pc
 		int b = (pc >> 16) & 255;//inf
 		int c = (pc >> 8) & 255;//zero
 		int d = pc & 255;//NaN
 		int i = mem[a];//instruction
 		
 		int ia = i >>> 24;
 		int ib = (i >> 16) & 255;
 		int ic = (i >> 8) & 255;
 		int id = i & 255;
 		
 		float x = asF(ia);
 		x *= x;
 		x *= asF(id);
 		x = asF(ic) - x;
 		x *= asF(ib);
		a++;
 		if(x == Float.NaN) a = d;
 		if(x == Float.NEGATIVE_INFINITY || x == Float.POSITIVE_INFINITY) a = b;
 		if(x == 0.0) a = c;
		pc = (a << 24) | (pc & 0xffffff);//restore pc
 		mem[0] = pc;
		mem[ia] = asI(x);//as jump vectoring allowed!!!
 	}
 	
 	float asF(int idx) {
 		return Float.intBitsToFloat(mem[idx]);
 	}
 	
 	int asI(float val) {
 		return Float.floatToIntBits(val);
 	}
 }
