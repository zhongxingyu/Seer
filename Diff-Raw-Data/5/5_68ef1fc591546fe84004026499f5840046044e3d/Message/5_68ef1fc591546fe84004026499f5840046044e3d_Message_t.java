 package net.cheney.cocktail.message;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import javax.annotation.Nonnull;
 
 public abstract class Message {
 	
 	public abstract long contentLength() throws IOException;
 	
 	abstract static class StartLine {
 
 		private final Version version;
 
 		StartLine(@Nonnull Version version) {
 			this.version = version;
 		}
 		
 		public final Version version() {
 			return this.version;
 		}
 		
 		@Override
 		public abstract int hashCode();
 		
 		@Override
 		public abstract boolean equals(Object obj);
 
 	}
 
 	public boolean closeRequested() {
		for(String value : header(Header.CONNECTION)) {
			if(value.equalsIgnoreCase("close")) return true;
		}
		return false;
 	}
 	
 	public abstract Version version();
 	
 	public abstract Iterable<Header> headers();
 	
 	public abstract Header.Accessor header(Header header);
 	
 	public enum TransferEncoding {
 		NONE,
 		IDENTITY
 	}
 	
 	public TransferEncoding transferCoding() {
 		return TransferEncoding.NONE;
 	}
 	
 	public abstract ByteBuffer body();
 		
 }
