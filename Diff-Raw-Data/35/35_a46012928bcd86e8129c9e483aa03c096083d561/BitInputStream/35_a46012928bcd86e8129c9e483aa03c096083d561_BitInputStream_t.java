 import java.io.IOException;
 import java.io.InputStream;
 
 public class BitInputStream extends InputStream {
	private int currentInt, bitsLeft = 0;
 	private InputStream stream;
 	
 	public BitInputStream(InputStream stream) {
 		this.stream = stream;
 	}
 	
 	@Override
	public int available() throws IOException {
		return stream.available();
	}
	
	@Override
	public void close() throws IOException {
		stream.close();
	}
	
	@Override
 	public int read() throws IOException {
 		return stream.read();
 	}
 	
 	public boolean readBit() throws IOException {
 		if (bitsLeft == 0) {
 			currentInt = read();
 			bitsLeft = 8;
 		}
 		return (currentInt << --bitsLeft & 1) == 1;
 	}
 }
