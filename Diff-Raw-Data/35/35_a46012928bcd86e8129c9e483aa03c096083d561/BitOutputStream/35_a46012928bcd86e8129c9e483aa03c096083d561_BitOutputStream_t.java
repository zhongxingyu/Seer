 import java.io.IOException;
 import java.io.OutputStream;
 
 public class BitOutputStream extends OutputStream {
 	private int next = 0, bitsSoFar = 0;
 	private OutputStream stream;
 	
 	public BitOutputStream(OutputStream stream) {
 		this.stream = stream;
 	}
 	
 	@Override
 	public void flush() throws IOException {
 		write(next);
 		super.flush();
 	}
 	
 	public void write(boolean b) throws IOException {
 		int value = (b ? 1 : 0) << 8 - ++bitsSoFar;
 		next |= value;
 		if (bitsSoFar == 8) {
 			write(next);
 			next = 0;
 		}
 	}
 	
 	@Override
 	public void write(int i) throws IOException {
 		stream.write(i);
 	}
 }
