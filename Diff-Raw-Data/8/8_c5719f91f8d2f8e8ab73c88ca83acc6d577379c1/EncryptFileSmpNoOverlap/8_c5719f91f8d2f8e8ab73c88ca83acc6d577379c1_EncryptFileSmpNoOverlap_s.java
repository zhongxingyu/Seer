 //******************************************************************************
 //
 // Author: Brian Gianforcaro (bjg1955@cs.rit.edu)
 // File:   EncryptFilSmpNoOverlap.java
 //
 //******************************************************************************
 
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 
 import edu.rit.crypto.blockcipher.AES256CipherSmp;
 import edu.rit.util.Hex;
 
 import edu.rit.pj.Comm;
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import edu.rit.pj.BarrierAction;
 
 /**
  * Class EncryptFileSmpNoOverlap encryptes inputfile using the AES cipher. 
  * <P>
  * Usage: java EncryptFileSmpNoOverlap  <I>key</I> <I>inFileName</I> <I>outFileName</I> <I>chunkSize</I>
  * <BR><I>key</I> = Encryption key (256-bit hexadecimal number)
  * <BR><I>inFileName</I> = input file name
  * <BR><I>outFileName</I> = output file name 
  * <BR><I>chunkSize</I> = number of 16-byte blocks in a chunk
  * @author  Brian Gianforcaro (bjg1955@cs.rit.edu)
  * @version 13-Sep-2010
  */
 
 public class EncryptFileSmpNoOverlap {
 
     /** Argument Constants */
     static int KEY_ARG    = 0;
     static int INPUT_ARG  = 1;
     static int OUTPUT_ARG = 2;
     static int CHUNK_ARG  = 3;
     static int EOF = -1;
 
     /** Working data for enciphering */
     static byte[] key;
     static byte[] plainTextChunk;
     static byte[] cipherTextChunk;
     static int chunkSize;
     static BufferedInputStream iFile;
     static BufferedOutputStream oFile;
     static int blockSize = 16;
     static int READSIZE;
 
     /** Prevent construction */
     private EncryptFileSmpNoOverlap() { }
 
     /**
      * Print a usage message and exit.
      */
     private static void usage() {
         System.err.println("Usage: java EncryptFileSmpNoOverlap <key> <inFileName> <outFileName> <chunkSize>");
         System.err.println("   <key> = Encryption key (256-bit hexadecimal number)");
         System.err.println("   <inFileName> = input file name");
         System.err.println("   <outFileName> = output file name");
         System.err.println("   <chunkSize> = number of 16-byte blocks in a chunk");
         System.exit(0);
     }
 
     /**
      * Properly pad the end of a data block with zero's
      * @param block - the data block to pad
      * @param offset - the offset in the block to start the pad.
      */
     private static void pad( byte[] block, final int offset ) {
         for ( int i = offset; i < block.length; i++ ) {
             block[i] = (byte)0;
         }
     }
 
     /**
      * Parse the arguments to the Encryption
      * @param args - The arguments to the encryption
      */
     private static void parseArgs( String[] args ) throws Exception {
         // Unpack arguments
         key = Hex.toByteArray( args[KEY_ARG] );
         chunkSize = Integer.parseInt( args[CHUNK_ARG] );
         iFile = new BufferedInputStream( new FileInputStream( args[INPUT_ARG] ) ); 
         oFile = new BufferedOutputStream( new FileOutputStream( args[OUTPUT_ARG] ) );
 
         READSIZE = chunkSize*blockSize;
 
         // Initialize the working blocks
         plainTextChunk = new byte[READSIZE];
         cipherTextChunk = new byte[READSIZE];
     }
 
     /**
      * EncryptFileSmpNoOverlap main program.
      */
     public static void main( final String[] args ) throws Exception {
 
         // Start timer
         long t1 = System.currentTimeMillis();
 
         // Initialize my PJ's
         Comm.init( args );
 
         // Parse command line arguments.
         if ( args.length != 4 ) {
           usage();
         }
 
         parseArgs( args );
 
         ParallelTeam team = new ParallelTeam();
 
         int read;
         while ( true ) {
 
             read = iFile.read( plainTextChunk );
             if ( read == EOF ) {
                 break;
             } else {
                 // If we didn't read an entire block 
                 // we have to pad the rest of the data.
                 if ( read < READSIZE ) {
                     pad( plainTextChunk, read );
                 }
 
                 team.execute( new ParallelRegion() {
                     public void run() throws Exception {
                         execute( 0, chunkSize-1, new IntegerForLoop() {
                             public void run( int first, int last ) {
                                 byte[] plainText   = new byte[blockSize];
                                 byte[] cipherText  = new byte[blockSize];
                                 AES256CipherSmp cipher = new AES256CipherSmp( key );
                                 for ( int chunk = first; chunk <= last; chunk++ ) {
 
                                     System.arraycopy( plainTextChunk, chunk*blockSize, plainText, 0, blockSize );
                                     cipher.encrypt( plainText, cipherText );
                                     System.arraycopy( cipherText, 0, cipherTextChunk, chunk*blockSize, blockSize );
                                 }
                             }
                         });
                     }
                 });
 
                oFile.write( cipherTextChunk );
                // This marks EOF
                if ( read != READSIZE ) {
                     break;
                 }
             }
         }
 
         oFile.close();
         iFile.close();
 
         // Stop timing.
         System.out.println((System.currentTimeMillis()-t1) + " msec");
     }
 }
