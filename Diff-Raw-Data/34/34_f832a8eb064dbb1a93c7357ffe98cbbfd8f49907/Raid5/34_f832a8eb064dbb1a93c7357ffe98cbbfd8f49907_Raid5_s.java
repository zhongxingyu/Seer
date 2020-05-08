 package de.dhbw.mannheim.cloudraid;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class Raid5 {
 
 	public static final int[] EXPONENTS = new int[16];
 
 	static {
 		for (int i = 0; i < 16; i++) {
 			EXPONENTS[i] = 1 << i;
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length != 5) {
 			System.out
 					.println("You have to specify <split|merge> <infile|outfile> <dev0> <dev1> <dev2>");
 			System.exit(1);
 		}
 		long startTime = System.currentTimeMillis();
 		if (args[0].toLowerCase().equals("split"))
 			try {
 				Raid5.split(args[1], args[2], args[3], args[4]);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		else if (args[0].toLowerCase().equals("merge"))
 			try {
 				Raid5.merge(args[1], args[2], args[3], args[4]);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		else {
 			System.out
 					.println("Unknown mode! Use either \"split\" or \"merge\"");
 			System.exit(2);
 		}
 		long endTime = System.currentTimeMillis();
 		System.out.println((endTime - startTime) / 1000 + " s.");
 	}
 
 	/**
 	 * This function splits the content of the given `infile` to the files that
 	 * are specified by `dev0`, `dev1` and `dev2`. The parity moves after each
 	 * byte. Its position for the first byte is `dev2`, for the second byte is
 	 * `dev0`, for the third byte is `dev1` and for the forth byte `dev2` again.
 	 * 
 	 * @param infile
 	 *            The filepath to the input file that should be split for the
 	 *            RAID
 	 * @param dev0
 	 *            The filepath to the first device file
 	 * @param dev1
 	 *            The filepath to the second device file
 	 * @param dev2
 	 *            The filepath to the third device file
 	 * @throws IOException
 	 */
 	public static void split(String infile, String dev0, String dev1,
 			String dev2) throws IOException {
 		InputStream in = new FileInputStream(new File(infile));
 		OutputStream[] out = { new FileOutputStream(dev0),
 				new FileOutputStream(dev1), new FileOutputStream(dev2) };
 
 		// we read 2 characters for easier working
 		byte[] chars = new byte[2];
 		int charNum = in.read(chars);
 		// the initial device for the parity is 2. We will increment it, so the
 		// next device will be 0
 		int parityPos = 2;
 
 		while (charNum > 0) {
 			int a = 0, b = 0;
 
			// we check whether we have read 1 or 2 bytes. And since we allways
 			// read two bytes, we can decide here, if the input file has an odd
 			// file size
 			if (charNum == 2) {
 				int index = 7;
 				// we take the ordinal value of both read bytes an put them into
 				// a single integer, where the first byte uses the bits 8 to 15
 				// and the second byte uses 0 to 7
 				int doubleChar = ((chars[0]) << 8) | chars[1];
 
 				// iterating over the EXPONENTS to split the two characters into
 				// two separat bytes
 				for (int i = 15; i >= 0; i -= 2) {
 					// if the according bit is set, we set the bit in our output
 					// byte. This is the same as `a = a | Math.pow(2,index)`
 					if ((doubleChar & EXPONENTS[i]) > 0)
 						a |= (1 << index);
 					if ((doubleChar & EXPONENTS[i - 1]) > 0)
 						b |= (1 << index);
 					index--;
 				}
 				// we now write the byte for each device. Because we use an
 				// array for storing the devices, we can simply increment the
 				// parity position and detect the new device for the parity
 				out[(parityPos + 1) % 3].write(a);
 				out[(parityPos + 2) % 3].write(b);
 				// This writes the parity as `a xor b`
 				out[parityPos].write(a ^ b);
 			} else {
 				// this is called if the file size is odd
 				// this calculates the 1st-complement
 				int p = (~chars[0]) + 256;
 				// and here we write the byte and the parity
 				out[(parityPos + 1) % 3].write(chars[0]);
 				out[parityPos].write(p);
 			}
 			parityPos = (++parityPos) % 3;
 			// try to read the next 2 bytes
 			charNum = in.read(chars);
 		}
 		in.close();
 		out[0].close();
 		out[1].close();
 		out[2].close();
 
 	}
 
 	/**
 	 * This function merges the content of the given device files `dev0`, `dev1`
 	 * and `dev2` to the `outfile`. The device for the first byte partiy must be
 	 * `dev2`, for the second byte must be `dev0` and for the third byte must be
 	 * `dev1`.
 	 * 
 	 * @param outfile
	 *            The filepath to the output file that should be splittet for
	 *            the RAID
 	 * @param dev0
 	 *            The filepath to the first device file
 	 * @param dev1
 	 *            The filepath to the second device file
 	 * @param dev2
 	 *            The filepath to the third device file
 	 * @throws IOException
 	 */
 	public static void merge(String outfile, String dev0, String dev1,
 			String dev2) throws IOException {
 
 		OutputStream out = new FileOutputStream(outfile);
 		InputStream[] in = { new FileInputStream(dev0),
 				new FileInputStream(dev1), new FileInputStream(dev2) };
 
 		// this describes the position of the parity device. See
 		// `Raid5.split()` for further information
 		int parityPos = 2;
 
		// we read the 3 separat bytes, that we combine to 2
 		int l = in[(parityPos + 1) % 3].read();
 		int r = in[(parityPos + 2) % 3].read();
 		int p = in[parityPos].read();
 
 		// as long as we have a value for all 3 variables, we are neither at the
 		// end of the files nor the file has an odd file size
 		while (l >= 0 && r >= 0 && p >= 0) {
 			// check that the parity matches. If not - print a message
 			if ((l ^ r) != p)
 				System.err
 						.println("[WARNING] parity does not match the values of device 1 and 2");
 
 			int outByte = 0;
 
 			// iterate over the list of bits
 			for (int i = 7; i >= 0; i--) {
 				// if the current bit is number 3, we write the first byte and
 				// continue with the second one
 				if (EXPONENTS[i] == 0x08) {
 					out.write(outByte);
 					outByte = 0;
 				}
 
 				// if the regarding bit is set in the first device, we set it in
 				// the output byte as well. We then shift the output byte one
 				// left and set the next bit in the output byte if the according
 				// bit is set in the second device as well.
 				outByte <<= 1;
 				if ((l & EXPONENTS[i]) > 0)
 					outByte |= 1;
 				outByte <<= 1;
 				if ((l & EXPONENTS[i]) > 0)
 					outByte |= 1;
 			}
 			// change the parity device
 			parityPos = (++parityPos) % 3;
 
 			// write the output byte and read the next input bytes
 			out.write(outByte);
 			l = in[(parityPos + 1) % 3].read();
 			r = in[(parityPos + 2) % 3].read();
 			p = in[parityPos].read();
 		}
 
 		// This if statement only evaluates to true if the original file has an
 		// odd file size
 		if (l >= 0 && p >= 0) {
 			// So what we do here is to check if the *overhead* byte matches the
 			// parity (and write a message if that isn't the case), and write it
 			// to the output file
 			if ((l ^ p) != 0xFF)
 				System.err
 						.println("[WARNING] parity does not match the values of device 1 and 2'");
 			out.write(l);
 		}
 
 	}
 
 }
