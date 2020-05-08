 //******************************************************************************
 //
 // File:    MandelbrotSetClu2.java
 // Author:  Brian Gianforcaro ( bjg1955@cs.rit.edu )
 //
 //******************************************************************************
 
 import edu.rit.color.HSB;
 
 import edu.rit.image.PJGColorImage;
 import edu.rit.image.PJGImage;
 
 import edu.rit.mp.IntegerBuf;
 import edu.rit.mp.ObjectBuf;
 
 import edu.rit.mp.buf.ObjectItemBuf;
 
 import edu.rit.pj.Comm;
 import edu.rit.pj.CommStatus;
 import edu.rit.pj.CommRequest;
 import edu.rit.pj.IntegerSchedule;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelSection;
 import edu.rit.pj.ParallelTeam;
 
 import edu.rit.util.Range;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Class MandelbrotSetClu2Overlap is a cluster parallel program that calculates the
  * Mandelbrot Set. The program uses the master-worker pattern for load
  * balancing. Each worker process in the program calculates a series of row
  * slices of the Mandelbrot Set image, as assigned by the master process. The
  * worker processes send the slices to the master process. After receiving all
  * the slices, the master process writes the image to a file.
  * <P>
  * The row slices are determined by the <TT>pj.schedule</TT> property specified
  * on the command line; the default is to divide the rows evenly among the
  * processes (i.e. no load balancing). For further information about the
  * <TT>pj.schedule</TT> property, see class {@linkplain edu.rit.pj.PJProperties
  * PJProperties}.
  * <P>
  * Usage: java -Dpj.np=<I>K</I> [ -Dpj.schedule=<I>schedule</I> ]
  * MandelbrotSetClu2Overlap <I>width</I> <I>height</I>
  * <I>xcenter</I> <I>ycenter</I> <I>resolution</I> <I>maxiter</I> <I>gamma</I>
  * <I>filename</I>
  * <BR><I>K</I> = Number of parallel processes
  * <BR><I>schedule</I> = Load balancing schedule
  * <BR><I>width</I> = Image width (pixels)
  * <BR><I>height</I> = Image height (pixels)
  * <BR><I>xcenter</I> = X coordinate of center point
  * <BR><I>ycenter</I> = Y coordinate of center point
  * <BR><I>resolution</I> = Pixels per unit
  * <BR><I>maxiter</I> = Maximum number of iterations
  * <BR><I>gamma</I> = Used to calculate pixel hues
  * <BR><I>filename</I> = PJG image file name
  * <P>
  * The program considers a rectangular region of the complex plane centered at
  * (<I>xcenter,ycenter</I>) of <I>width</I> pixels by <I>height</I> pixels,
  * where the distance between adjacent pixels is 1/<I>resolution</I>. The
  * program takes each pixel's location as a complex number <I>c</I> and performs
  * the following iteration:
  * <P>
  * <I>z</I><SUB>0</SUB> = 0
  * <BR><I>z</I><SUB><I>i</I>+1</SUB> = <I>z</I><SUB><I>i</I></SUB><SUP>2</SUP> + <I>c</I>
  * <P>
  * until <I>z</I><SUB><I>i</I></SUB>'s magnitude becomes greater than or equal
  * to 2, or <I>i</I> reaches a limit of <I>maxiter</I>. The complex numbers
  * <I>c</I> where <I>i</I> reaches a limit of <I>maxiter</I> are considered to
  * be in the Mandelbrot Set. (Actually, a number is in the Mandelbrot Set only
  * if the iteration would continue forever without <I>z</I><SUB><I>i</I></SUB>
  * becoming infinite; the foregoing is just an approximation.) The program
  * creates an image with the pixels corresponding to the complex numbers
  * <I>c</I> and the pixels' colors corresponding to the value of <I>i</I>
  * achieved by the iteration. Following the traditional practice, points in the
  * Mandelbrot set are black, and the other points are brightly colored in a
  * range of colors depending on <I>i</I>. The exact hue of each pixel is
  * (<I>i</I>/<I>maxiter</I>)<SUP><I>gamma</I></SUP>. The image is stored in a
  * Parallel Java Graphics (PJG) file specified on the command line.
  * <P>
  * The computation is performed in parallel in multiple processors. The program
  * measures the computation's running time, including the time to write the
  * image file.
  *
  * @author  Alan Kaminsky
  * @version 16-Feb-2011
  */
 
 public class MandelbrotSetClu2Overlap {
 
   //-----------------------
   // Prevent construction.
   //-----------------------
 
   private MandelbrotSetClu2Overlap() { }
 
   //---------------------------
   // Program shared variables.
   //---------------------------
 
   // Communicator.
   static Comm world;
   static int size;
   static int rank;
 
   // Command line arguments.
   static int width;
   static int height;
   static double xcenter;
   static double ycenter;
   static double resolution;
   static int maxiter;
   static double gamma;
   static File filename;
 
   // Initial pixel offsets from center.
   static int xoffset;
   static int yoffset;
 
   // Image matrix.
   static int[][] matrix;
   static PJGColorImage image;
 
   // Table of hues.
   static int[] huetable;
 
   // Slice chunks for workers
   static int[][] slice; 
   static int[][] sliceTmp;
 
   // Message tags.
   static final int WORKER_MSG = 0;
   static final int MASTER_MSG = 1;
   static final int PIXEL_DATA_MSG = 2;
 
   // Number of chunks the worker computed.
   static int chunkCount;
 
   //-------------------
   // Main program.
   //-------------------
  
   /**
    * Print a usage message and exit.
    */
   private static void usage() {
     System.err.println( "Usage: java -Dpj.np=<K> [-Dpj.schedule=<schedule>] MandelbrotSetClu2Overlap <width> <height> <xcenter> <ycenter> <resolution> <maxiter> <gamma> <filename>" );
     System.err.println( "<K> = Number of parallel processes" );
     System.err.println( "<schedule> = Load balancing schedule" );
     System.err.println( "<width> = Image width (pixels)" );
     System.err.println( "<height> = Image height (pixels)" );
     System.err.println( "<xcenter> = X coordinate of center point" );
     System.err.println( "<ycenter> = Y coordinate of center point" );
     System.err.println( "<resolution> = Pixels per unit" );
     System.err.println( "<maxiter> = Maximum number of iterations" );
     System.err.println( "<gamma> = Used to calculate pixel hues" );
     System.err.println( "<filename> = PJG image file name" );
     System.exit(1);
   }
 
   /**
    *
    * @param args - The arguments to parse
    */
   private static void parseArgs( String[] args ) {
 
     width = Integer.parseInt( args[0] );
     height = Integer.parseInt( args[1] );
     xcenter = Double.parseDouble( args[2] );
     ycenter = Double.parseDouble( args[3] );
     resolution = Double.parseDouble( args[4] );
     maxiter = Integer.parseInt( args[5] );
     gamma = Double.parseDouble( args[6] );
 
     if ( rank == 0 ) {
       filename = new File( args[7] );
     }
  
     // Initial pixel offsets from center.
     xoffset = -(width - 1) / 2;
     yoffset = (height - 1) / 2;
   }
 
   /**
    * Initialize the static hue table for later use.
    */
   private static void initHueTable() {
     // Create table of hues for different iteration counts.
     huetable = new int [maxiter+1];
     for ( int i = 0; i < maxiter; ++i ) {
       float hue = (float) Math.pow( ((double)i)/((double)maxiter), gamma );
       huetable[i] = HSB.pack( /*hue*/ hue,
                               /*sat*/ 1.0f,
                               /*bri*/ 1.0f );
     }
     huetable[maxiter] = HSB.pack( 1.0f, 1.0f, 0.0f );
   }
 
 
   private static void swap() {
 
     if ( sliceTmp == null || sliceTmp.length != slice.length ) {
       sliceTmp = new int[slice.length][width];
     }
 
     for ( int i = 0; i < slice.length; ++i ) {
       for ( int j = 0; j < width; ++j ) {
         sliceTmp[i][j] = slice[i][j];
       }
     }
   }
 
  private static void computeSlice( int lb, int ub, int len ) {
 
     // Allocate storage for matrix row slice if necessary.
     if ( slice == null || slice.length < len ) {
       slice = new int [len][width];
     }
 
     // Compute all rows and columns in slice.
     for ( int r = lb; r <= ub; ++r ) {
 
       int[] slice_r = slice[r-lb];
       double y = ycenter + (yoffset - r) / resolution;
 
       for ( int c = 0; c < width; ++c ) {
         double x = xcenter + (xoffset + c) / resolution;
         // Iterate until convergence.
         int i = 0;
         double aold = 0.0;
         double bold = 0.0;
         double a = 0.0;
         double b = 0.0;
         double zmagsqr = 0.0;
         while (i < maxiter && zmagsqr <= 4.0) {
           ++ i;
           a = aold*aold - bold*bold + x;
           b = 2.0*aold*bold + y;
           zmagsqr = a*a + b*b;
           aold = a;
           bold = b;
         }
 
         // Record number of iterations for pixel.
         slice_r[c] = huetable[i];
       }
     }
   }
 
   /**
    * Perform the worker section.
    * @exception  Exception Thrown if an I/O error occurred.
    */
   private static void workerSection() throws IOException {
 
     // Request to support async sends/recives
     CommRequest request = new CommRequest();
 
     ObjectItemBuf<Range> rangeBuf;
     Range range;
 
     // Seed loop algorithm with a range
     rangeBuf = ObjectBuf.buffer();
     world.receive( 0, WORKER_MSG, rangeBuf );
     range = rangeBuf.item;
 
     // Process chunks from master.
     while ( true ) {
 
       if ( range == null ) {
         break;
       }
 
       int len = range.length();
       // Compute the worker's current chunk
       computeSlice( range.lb(), range.ub(), len );
 
       // Make sure our last async send is done before we
       // start sending this section's data.
       if ( chunkCount > 0 ) {
         request.waitForFinish();
       }
 
       // Swap slice & sliceTmp
       swap();
 
       // Send chunk range back to master.
       world.send( 0, MASTER_MSG, rangeBuf );
 
       // Receive chunk range from master. If null, no more work.
       rangeBuf = ObjectBuf.buffer();
       world.receive( 0, WORKER_MSG, rangeBuf );
       range = rangeBuf.item;
 
       // Send pixel data to master.
      world.send( 0,
                  PIXEL_DATA_MSG,
                  IntegerBuf.rowSliceBuffer( sliceTmp, new Range( 0, len-1 ) ),
                  request );
 
       ++chunkCount;
     }
   }
 
   /**
    * Perform the master section.
    * @exception IOException Thrown if an I/O error occurred.
    */
   private static void masterSection() throws IOException {
 
     int worker;
     Range range;
 
     // Allocate all rows of image matrix.
     matrix = new int[height][width];
 
     // Set up a schedule object to divide the row range into chunks.
     IntegerSchedule schedule = IntegerSchedule.runtime();
     schedule.start( size, new Range( 0, height-1 ) );
 
     // Send initial chunk range to each worker. If range is null, no more
     // work for that worker. Keep count of active workers.
     int activeWorkers = size;
     for ( worker = 0; worker < size; ++worker ) {
       range = schedule.next( worker );
       world.send( worker, WORKER_MSG, ObjectBuf.buffer( range ) );
       if (range == null) { 
         --activeWorkers;
       }
     }
 
     Range nextRange;
 
     // Repeat until all workers have finished.
     while ( activeWorkers > 0 ) {
 
       // Receive a chunk range from any worker.
       ObjectItemBuf<Range> rangeBuf = ObjectBuf.buffer();
       CommStatus status = world.receive( null, MASTER_MSG, rangeBuf );
       worker = status.fromRank;
       range = rangeBuf.item;
 
       // Schedule next range
       nextRange  = schedule.next( worker );
 
       // Send next chunk range to that specific worker.
       world.send( worker, WORKER_MSG, ObjectBuf.buffer( nextRange ) );
 
       // Receive pixel data from that specific worker.
       world.receive( worker, PIXEL_DATA_MSG,
                      IntegerBuf.rowSliceBuffer( matrix, range ) );
 
       range = nextRange;
       // If null, no more work.
       if (range == null) {
         --activeWorkers;
       }
     }
   }
 
   
   /**
    * Mandelbrot Set main program.
    */
   public static void main( String[] args ) throws Exception {
 
     // Start timing.
     long t1 = System.currentTimeMillis();
 
     // Initialize middle ware.
     Comm.init( args );
     world = Comm.world();
     size = world.size();
     rank = world.rank();
 
     // Validate command line arguments.
     if (args.length != 8) {
       usage();
     }
 
     parseArgs( args );
     initHueTable();
     
     long t2 = System.currentTimeMillis();
 
     if (rank == 0) {
       // In master process, run master section and worker section in parallel.
       new ParallelTeam(2).execute( new ParallelRegion() {
         public void run() throws Exception {
           execute( new ParallelSection() {
             public void run() throws Exception {
               masterSection();
             }
           },
           new ParallelSection() {
             public void run() throws Exception {
               workerSection();
             }
           });
         }
       });
     } else { 
       // In worker process, run only worker section.
       workerSection();
     }
 
     long t3 = System.currentTimeMillis();
 
     // Write image to PJG file in master process.
     if ( rank == 0 ) {
       FileOutputStream fOutStream = new FileOutputStream( filename );
       BufferedOutputStream ostream = new BufferedOutputStream( fOutStream );
       image = new PJGColorImage( height, width, matrix );
       PJGImage.Writer writer = image.prepareToWrite( ostream );
       writer.write();
       writer.close();
     }
 
     // Stop timing.
     long t4 = System.currentTimeMillis();
     System.out.println( chunkCount + " chunks " + rank );
     System.out.println( (t2-t1) + " msec pre " + rank );
     System.out.println( (t3-t2) + " msec calc " + rank );
     System.out.println( (t4-t3) + " msec post " + rank );
     System.out.println( (t4-t1) + " msec total " + rank );
   }
 
   //-------------------
   // Hidden operations.
   //-------------------
 
 
 }
