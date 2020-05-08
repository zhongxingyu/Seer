 package net.thomasnardone.utils.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class CountingInputStream extends InputStream {
 	private long				count;
 	private final InputStream	input;
 
 	public CountingInputStream(final InputStream input) {
 		this.input = input;
 		count = 0;
 	}
 
 	public long getCount() {
 		return count;
 	}
 
 	public String getFormattedCount() {
 		return ByteFormatter.format(count);
 	}
 
 	@Override
 	public int read() throws IOException {
		final int read = input.read();
		if (read > -1) {
			count++;
		}
		return read;
 	}
 }
