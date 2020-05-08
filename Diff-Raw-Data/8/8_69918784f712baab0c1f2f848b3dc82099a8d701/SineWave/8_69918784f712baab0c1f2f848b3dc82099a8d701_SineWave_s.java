 package synthseq.playables.readables.waveforms;
 
 import synthseq.playables.readables.ReadableSound;
 
 public class SineWave extends ReadableSound{
 	private double time = 0;
 	private double freq;
 	private boolean running = false;
 
 	public SineWave(double freq) {
 		this.freq = freq;
 	}
 
 	@Override
 	public double read() {
 		if (!running)
 			return 0;
 		time += 2*Math.PI/(44100/freq);
 		if(time >= Math.PI*2)
			time = 0;
 		return Math.sin(time);
 	}
 
 	@Override
 	public void start() {
 		running = true;
 	}
 
 	@Override
 	public void stop() {
 		running = false;
 	}
 
 }
