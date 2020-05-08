 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 /**
  * <p>
  * This 'reads' by actually 'translating' bytes off of a communication channel. It follows
  * the dead-simple "babble" protocol: the bytes should look like a series of 4-byte signed
  * integer lengths, followed by an arbitrary blob of the size specified by the int. The
  * size bytes are discarded after being used to determine the blob, and the blob is
  * wrapped in a ByteBuffer and returned as the result of the translation.
  * </p>
  * 
  * <p>
  * The concrete implementation of this is provided as a nested class in order to match the
  * organization of the closely related {@link TranslatorByteBufferToChannel} class; at
  * present there is only one such implementation ({@link Nonblocking}). It is presumed
  * that the base channel has already been set to a nonblocking mode.
  * </p>
  * 
  * @author hash
  * 
  */
 public abstract class TranslatorChannelToByteBuffer implements Translator<ReadableByteChannel,ByteBuffer> {
 	/**
 	 * <p>
 	 * This implementation of translating reads off of a channel is nonblocking.
 	 * </p>
 	 * 
 	 * <p>
 	 * The translate method of this Translator may return null for any given
 	 * invocation if there is not immediately (i.e. in a nonblocking sense) sufficient
 	 * readable data to yield a full blob.
 	 * </p>
 	 * 
 	 * <p>
 	 * It is NOT possible to use the same instance of a
 	 * TranslatorChannelToByteBuffer.Nonblocking in several TranslationStack for any
 	 * purpose if threads are involved. Since a it has no way to enforce the atomicity
 	 * of some of its internal operations without becoming a blocking system, it must
 	 * keep thread-local state.
 	 * </p>
 	 * 
 	 * @author hash
 	 */
 	public static class Nonblocking extends TranslatorChannelToByteBuffer {
 		private final ByteBuffer	$preint	= ByteBuffer.allocate(4);
 		private int			$messlen = -1;
 		private ByteBuffer		$mess;
 		
 		public ByteBuffer translate(ReadableByteChannel $base) throws TranslationException {
			if ($messlen <= 0) {
 				// try to read enough info to figure out what length of message we expect
 				try {
 					$base.read($preint);
 				} catch (IOException $e) {
 					if ($preint.remaining() != 4) throw new TranslationException("malformed babble -- partial message length header read before unexpected EOF", $e);
 					return null;	// this is the one place in the Babble protocol for a smooth shutdown to be legal... the pump should just notice the channel being closed before next time around.}
 				}
 				
 				if ($preint.remaining() > 0) return null; // don't have a size header yet.  keep waiting for more data.
 				$messlen = Primitives.intFromByteArray($preint.array());
 				$preint.rewind();
 				if ($messlen < 1) throw new TranslationException("malformed babble -- message length header not positive");
 				$mess = ByteBuffer.allocate($messlen);
 			}
 			// if procedure gets here, we either had messlen state from the last round or we have it now.
 			
 			// get the message (or at least part of it, if possible)
 			try {
 				$base.read($mess);
 			} catch (IOException $e) {
 				throw new TranslationException("babble of unexpected length", $e);
 			}
 			
 			if ($mess.remaining() > 0) return null; // we just don't have as much information as this chunk should contain yet.  keep waiting for more data.
 			
 			$messlen = -1;
			$preint.rewind();
 			$mess.rewind();
 			return $mess;
 		}
 	}
 }
