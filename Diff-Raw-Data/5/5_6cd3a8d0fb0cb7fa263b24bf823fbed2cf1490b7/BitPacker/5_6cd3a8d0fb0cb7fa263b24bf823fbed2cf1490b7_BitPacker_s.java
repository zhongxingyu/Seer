 package compress;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Jake Bellamy 1130587 jrb46
  * @author Michael Coleman 1144239 mjc62
  */
 public class BitPacker {
 	
 	IOHandler io;
 	
 	/**
 	 * Construct a new BitPacker reading from standard in.
 	 */
 	public BitPacker() {
 		io = new IOConsoleHandler();
 	}
 	
 	/**
 	 * Construct a new BitPacker reading from a file.
 	 * @param filename The file name to read from.
 	 */
 	public BitPacker(String filename) {
 		io = new IOFileHandler(filename);
 	}
 	
 	/**
 	 * Construct a new BitPacker with a given IO Handler.
 	 * @param io The IO Handler to use with this bit packer.
 	 */
 	public BitPacker(IOHandler io) {
 		this.io = io;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		BitPacker bp;
 		if (args.length > 0) {
 			bp = new BitPacker(args[0]);
 			System.out.println("Encoding from file " + args[0] + "...");
 		} else {
 			bp = new BitPacker();
 			System.out.println("Packing from standard input.\n Please " +
 					"enter the tuples you wish to pack followed by an " +
 					"empty line.");
 		}
 		bp.pack();
 	}
 	
 	/**
 	 * Reads tuples from the IO Handler and packs the tuples so they use
 	 * the minimum amount of bits needed.
 	 */
 	public void pack() {
		int indexLength = 6;
 		
 		String message = io.readString();
 		Pattern p = Pattern.compile("[^0-9]");
 		Matcher m = p.matcher(message);
 		int startindex = 0, mindex = 0, byteIndex = 0, bitIndex = 8;
 		int bufLength = 50;
 		byte[] buf = new byte[bufLength + 1];
 		
 		/**
 		 * Loop through each tuple that we find, calculate lengths and
 		 * indices.
 		 */
 		while (m.find()) {
 			mindex = m.end();
 			String phnum = message.substring(startindex, mindex - 1);
 			startindex = mindex;
 			
 			if (phnum.length() == 0) {
 				continue;
 			}
 			int phraseNum = Integer.parseInt(phnum);
 			int character = message.charAt(mindex - 1);
			int phraseLength = getBitLength(phraseNum) + 1;
 			
 			
 			/**
 			 * With each tuple that we find we are outputting 3 things:
 			 * The number of bits needed to represent the phrase number,
 			 * The phrase number itself,
 			 * And the character.
 			 */
 			for (int i = 0; i < 3; i++) {
 				int[] vars = setVars(i, indexLength, phraseLength, 
 						phraseNum, character);
 				
 				/**
 				 * Set the next offset for the placement of the next bits 
 				 * that we are inserting into the byte. If the offset is
 				 * negative that indicates that there is not enough space
 				 * in this byte to store these bits completely so we must
 				 * split it up and store the overflow into the next byte.
 				 */
 				bitIndex -= vars[0];
 				if (bitIndex < 0) {
 					//Calculate indices and lengths for correct placement.
 					int topbits = bitIndex + vars[0];
 					int off = Math.abs(bitIndex);
 					/**
 					 * Store what we can fit into this byte, and the rest
 					 * in the next byte.
 					 */
 					buf[byteIndex]= setBits
 							(buf[byteIndex], vars[1], off, topbits);
 					
 					buf[++byteIndex] = setBits
 							(buf[byteIndex], vars[1], 8 - off);
 					
 					//Fix up the bit index so it references this new byte
 					bitIndex += 8;
 				} else {
 					/**
 					 * The bits we are storing fits into the space left
 					 * in this byte so put all of them in.
 					 */
 					buf[byteIndex] = setBits
 							(buf[byteIndex], vars[1], bitIndex);
 				}
 				
 				/**
 				 * Check if we have reached the length of our buffer.
 				 * Output the entire buffer except the last byte as there 
 				 * may be more space left in it to insert bits into.
 				 * Swap the last byte to the first position of our new
 				 * buffer and fix up the current byte index.
 				 */
 				if (byteIndex == bufLength) {
 					io.writeBytes(buf, bufLength);
 					byte swap = buf[byteIndex];
 					buf = new byte[bufLength + 1];
 					buf[0] = swap;
 					byteIndex = 0;
 				}
 			}
 
 		}
 		/**
 		 * There is no more input so write out the data we collected and
 		 * close the streams.
 		 */
 		io.writeBytes(buf, byteIndex + 1);
 		io.closeAllStreams();
 		
 	}
 	
 	/**
 	 * Gets some bits out of a byte. For example to extract the x bits
 	 * from a byte given as {@code 000x xx00} then call this method with 
 	 * {@code length = 3} and {@code offset = 2}.
 	 * @param b The byte we are extracting bits from.
 	 * @param length The number of bits to extract.
 	 * @param offset The starting position in the byte to extract from.
 	 * @return An integer with the extracted bits.
 	 */
 	private int getBits(byte b, int length, int offset) {
 		/**
 		 * Build up a mask full of 1-bits for ANDing with the byte that 
 		 * we want the bits out of.
 		 */
 		int mask = 0;
 		for (int i = 0; i < length; i++) {
 			mask = (mask << 1) + 1;
 		}
 		return (b >> offset) & mask;
 	}
 	
 	/**
 	 * Sets bits in a byte.
 	 * @param b The byte in which to store bytes into.
 	 * @param bits The bits to store.
 	 * @param offset The position in which to store the bits.
 	 * @return The same byte except with the given bits set in it.
 	 */
 	private byte setBits(byte b, int bits, int offset) {
 		return (byte) (b | (bits << offset));
 	}
 	
 	private byte setBits(byte b, int bits, int offset, int length) {
 		int mask = 0;
 		for (int i = 0; i < length; i++) {
 			mask = (mask << 1) + 1;
 		}
 		return (byte) (b | ((bits >> offset) & mask));
 	}
 	
 	/**
 	 * Gets the minimum number of bits needed to represent a given number.
 	 * @param num The number to check.
 	 * @return The number of significant bits in the number.
 	 */
 	private int getBitLength(int num) {
 		return (32 - Integer.numberOfLeadingZeros(num));
 	}
 	
 	/**
 	 * Sets up variables passed in by the packing method. This is a simple
 	 * convenience method so the variables may be used in a for loop.
 	 */
 	private int[] setVars(int i, int indexLength, int phraseLength, 
 			int phraseNum, int character) {
 		int[] vars = new int[2];
 		switch (i) {
 		case 0:
 			vars[0] = indexLength;
 			vars[1] = phraseLength;
 			return vars;
 		case 1:
 			vars[0] = phraseLength;
 			vars[1] = phraseNum;
 			return vars;
 		case 2:
 			vars[0] = 8;
 			vars[1] = character;
 			return vars;
 		default:
 			return vars;
 		}
 	}
 }
