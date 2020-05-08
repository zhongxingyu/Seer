 package Engine;
 
 import java.io.*;
 /* This class defines all the general miscellaneous math functions
  *  Should never be instanciated
  */
 
 /**
  * @author orpheon
  *
  */
 public class Functions
 {
 	// Converts a signed double into a 16bit bytearray (for outputting as sound)
 	// Credit goes to StackOverflow
 	public static byte[] convert_to_16bit_bytearray(double x)
 	{
 		// LITTLE ENDIAN!!!
 		if (Math.abs(x) > 1)
 		{
 			System.out.println("ERROR: Too large x value given for byte converting!");
 			System.out.println(x);
 			byte[] error = {1};
 			// FIXME: Use some python "raise" equivalent for stopping java
 			// This will call out a runtime error, only way I can think of to stop Java
 			System.out.println(error[4]);
 			return error;
 		}
 		// First cast it to short and multiply it with the size of short to use all of those 16 bits
 		short a = (short) (x * 32767);
 //		System.out.println("\nUnscaled: "+x+"\nScaled: "+a);
 		// Then convert that short into a byte array
 		// Code origin: http://stackoverflow.com/questions/2188660/convert-short-to-byte-in-java
 		byte[] ret = new byte[2];
 		ret[0] = (byte)(a & 0xff);
 		ret[1] = (byte)((a >> 8) & 0xff);
 
 		return ret;
 	}
 	
 	// Copy-pasted from StackOverflow as well, this is just a debugging tool
 	// It outputs a byte array written to it as a file
     static void array_write(byte[] aInput, String aOutputFileName){
     	System.out.println("Writing binary file "+aOutputFileName);
         try {
           OutputStream output = null;
           try {
             output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
             output.write(aInput);
           }
           finally {
             output.close();
           }
         }
         catch(FileNotFoundException ex){
         	System.out.println("File not found.");
         }
         catch(IOException ex){
         	System.out.println(ex);
         }
       }
     
     // Main resource: http://www.phys.unsw.edu.au/jw/notes.html
     // Interprets a string in format <letter><number> as a note and turns it into a frequency
     public static double convert_note2frequency(String input)
     {
     	String[] notes_sharp = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
     	String[] notes_flat = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
     	
     	int index, octave;
     	
     	if (input.length() == 2)
     	{
     		// Find the index of a note, it doesn't matter in what list since there's no #/b
     		for (index=0; !notes_sharp[index].equals(input.substring(0, 1)); index++);
     		octave = ((int) input.charAt(1)) - ((int) '0');
     	}
     	else if (input.length() == 3)
     	{
     		if (input.charAt(1) == '#')
     		{
    			for (index=0; !notes_sharp[index].equals(input.substring(0, 2)); index++);
     		}
     		else if (input.charAt(1) == 'b')
     		{
    			for (index=0; !notes_flat[index].equals(input.substring(0, 2)); index++);
     		}
     		else
     		{
     			throw new IllegalArgumentException();
     		}
     		octave = ((int) input.charAt(2)) - ((int) '0');
     	}
     	else
     	{
     		throw new IllegalArgumentException();
     	}
     	
     	System.out.println(index+"; "+octave);
     	
     	double value;
     	// Total note number
     	value = index + 12*octave;
     	// Normalize around A4 (number 57)
     	value -= 57;
     	// Get the frequency (2^(n/12) * A4 value)
     	return Math.pow(2, value/12) * 440;
     }
     // Main resource: http://www.phys.unsw.edu.au/jw/notes.html
     // Interprets a frequency and turns it into a string in format <letter><number>
     public static String convert_frequency2note(double freq)
     {
     	String[] notes_sharp = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
     	String[] notes_flat = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
     	
     	double value = freq/440;
     	int note = (int) (12 * (Math.log(value) / Math.log(2)));
     	// Normalize it to C0
     	note += 57;
     	return (notes_sharp[note % 12] + ((int)Math.floor(note/12)));
     }
     
     
     // Created from the python line
     // return sum(1<<(numbits-1-i) for i in range(numbits) if x>>i&1)
     // Also from StackOverflow
     public static int bit_reverse(int x, int numbits)
     {
     	int output = 0;
     	for (int i=0; i<numbits; i++)
     	{
     		if (((x>>i)&1) == 1)
     		{
     			output += 1<<(numbits-1-i);
     		}
     	}
     	return output;
     }
 	
     // FFT Algorithm
     // Main resource: http://cnx.org/content/m12016/latest/
     public static double[] fft(double[] input)
     {
     	// Input arrays (algorithm is in-place)
     	double[] real = input;
     	double[] imag = new double[input.length];
     	
     	// First do order switching (bit reversal)
     	int j = 0, log2N;
     	double tmp;
     	// Assuming the input was a power of two, log2N should be a whole number
     	log2N = (int) (Math.log(real.length)/Math.log(2));
     	for (int i=0; i<real.length; i++)
     	{
     		j = bit_reverse(i, log2N);
     		if (i < j)
     		{
     			// We haven't reversed these two numbers yet
     			tmp = real[i];
     			real[i] = real[j];
     			real[j] = tmp;
     		}
     	}
     	
     	// Do the actual fft (see "Butterfly calculation")
     	double offset, angle;
     	double c, s, tmp_real, tmp_imag;
     	for (int block_size=1; block_size<real.length; block_size*=2)
     	{
     		// In theory it should be offset = 2pi/2*block_size
     		offset = Math.PI/block_size;
     		angle = 0;
     		
     		for (int block=0; block<block_size; block++)
     		{
     			c = Math.cos(angle);
     			s = Math.sin(angle);
     			// TODO: Find out why a minus. Apparently twiddle factor is = e^(-i*2pi*f), and not e^(i*2pi*f)
     			angle -= offset;
     			
     			for (int k=block; k<real.length; k+=block_size*2)
     			{
     				// Def: G(k) = x[k] + iy[k] (Block 1), H(k) = x[k+block_size] + iy[k+block_size] (Block 2)
     				// Complex multiplication of H(k) and W
     				tmp_real = c*real[k+block_size] - s*imag[k+block_size];// a1*a2 - b1*b2
     				tmp_imag = c*imag[k+block_size] + s*real[k+block_size];// i(a1*b2 + a2*b1)
     				
     				// H(k) = G(k) - (H(k) * W) 
     				real[k+block_size] = real[k] - tmp_real;
     				imag[k+block_size] = imag[k] - tmp_imag;
     				
     				// G(k) = G(k) + (H(k) * W)
     				real[k] += tmp_real;
     				imag[k] += tmp_imag;
     			}
     		}
     	}
     	
     	// real is in the input, return the phase through the imag array
     	return imag;
     }
     
     // IFFT Algorithm
     // Derived from above
     public static void ifft(double[] amplitude, double[] phase)
     {
     	// Input arrays (algorithm is in-place)
     	// Inverse real and imaginary --> FFT becomes IFFT
     	// Proof: http://www.embedded.com/design/embedded/4210789/DSP-Tricks--Computing-inverse-FFTs-using-the-forward-FFT (Method #2)
     	double[] real = phase;
     	double[] imag = amplitude;
     	
     	// First do order switching (bit reversal)
     	int j = 0, log2N;
     	double tmp;
     	// Assuming the input was a power of two, log2N should be a whole number
     	log2N = (int) (Math.log(real.length)/Math.log(2));
     	for (int i=0; i<real.length; i++)
     	{
     		j = bit_reverse(i, log2N);
     		if (i < j)
     		{
     			// We haven't reversed these two numbers yet
     			tmp = real[i];
     			real[i] = real[j];
     			real[j] = tmp;
     			
     			// Notice the minus signs - This conjugates the entire thing which makes the function behave like a IFFT
     			tmp = imag[i];
     			imag[i] = imag[j];
     			imag[j] = tmp;
     		}
     	}
     	
     	// Do the actual fft (see "Butterfly calculation")
     	double offset, angle;
     	double c, s, tmp_real, tmp_imag;
     	for (int block_size=1; block_size<real.length; block_size*=2)
     	{
     		// In theory it should be offset = 2pi/2*block_size
     		offset = Math.PI/block_size;
     		angle = 0;
     		
     		for (int block=0; block<block_size; block++)
     		{
     			c = Math.cos(angle);
     			s = Math.sin(angle);
     			angle -= offset;
     			
     			for (int k=block; k<real.length; k+=block_size*2)
     			{
     				// Def: G(k) = x[k] + iy[k] (Block 1), H(k) = x[k+block_size] + iy[k+block_size] (Block 2)
     				// Complex multiplication of H(k) and W
     				tmp_real = c*real[k+block_size] - s*imag[k+block_size];// a1*a2 - b1*b2
     				tmp_imag = c*imag[k+block_size] + s*real[k+block_size];// i(a1*b2 + a2*b1)
     				
     				// H(k) = G(k) - (H(k) * W) 
     				real[k+block_size] = real[k] - tmp_real;
     				imag[k+block_size] = imag[k] - tmp_imag;
     				
     				// G(k) = G(k) + (H(k) * W)
     				real[k] += tmp_real;
     				imag[k] += tmp_imag;
     			}
     		}
     	}
     	
     	// Inefficient, but easy way to scale back
     	for (int i=0; i<real.length; i++)
     	{
     		real[i] /= real.length;
     		imag[i] /= imag.length;
     		
     		// This is cheating, but I'm pretty sure it's caused by rounding error or something, and this is the only efficient put to place it
     		real[i] = Math.max(-1, Math.min(1, real[i]));
     		imag[i] = Math.max(-1, Math.min(1, imag[i]));
     	}
     }
 }
