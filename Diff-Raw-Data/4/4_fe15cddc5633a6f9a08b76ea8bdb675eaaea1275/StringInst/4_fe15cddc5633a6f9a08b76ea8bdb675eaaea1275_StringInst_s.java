 package synthseq.playables.readables.filters;
 
 import synthseq.playables.readables.ReadableSound;
 
 public class StringInst extends ReadableSound {
 	private double[] buffer;
 	private int first = 0;
 	private int last = 0;
 	private double damper = .999;
 	private double playDamper = 0.999;
	private double stopDamper = 0.99;
 	private double alpha = 0.5;
 
 	public StringInst(double freq) {
 		this(freq, 0.5, .999);
 	}
 
 	public StringInst(double freq, double alpha) {
 		this(freq, alpha, .999);
 	}
 
 	public StringInst(double freq, double alpha, double damper) {
 		int len = (int) (44100 / freq);
 		buffer = new double[len];
 		last = len;
 		this.alpha = alpha;
 		this.playDamper = damper;
 	}
 
 	public void start() {
 		damper = playDamper;
 		for (int i = 0; i < buffer.length; i++) {
 			buffer[i] = (Math.random()) * 2 - 1;
 		}
 	}
 
 	public void stop() {
 		damper = stopDamper;
 	}
 
 	public double read() {
		damper = playDamper;
 		double tmp = damper * (alpha * pop() + (1 - alpha) * peek());
 		push(tmp);
 		return tmp;
 	}
 
 	private void push(double d) {
 		if (last++ >= buffer.length - 1)
 			last = 0;
 		buffer[last] = d;
 	}
 
 	private double pop() {
 		double tmp = buffer[first];
 		if (first++ >= buffer.length - 1)
 			first = 0;
 		return tmp;
 	}
 
 	private double peek() {
 		return buffer[first];
 	}
 }
