 /*-
  * Copyright (c) 2014 Mikolaj Izdebski
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.lbzip2.impl;
 
 import static org.lbzip2.impl.Constants.MAX_CODE_LENGTH;
 import static org.lbzip2.impl.MtfDecoder.CMAP_BASE;
 import static org.lbzip2.impl.PrefixDecoder.EOB;
 import static org.lbzip2.impl.PrefixDecoder.HUFF_START_WIDTH;
 import static org.lbzip2.impl.PrefixDecoder.RUN_A;
 import static org.lbzip2.impl.Status.MORE;
 import static org.lbzip2.impl.Status.OK;
 import static org.lbzip2.impl.Unsigned.uge;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 
 import org.lbzip2.StreamFormatException;
 
 public class MBC
 {
     private final InputStream in;
 
     private final OutputStream out;
 
     public MBC( InputStream in, OutputStream out )
     {
         this.in = in;
         this.out = out;
     }
 
     private static void err( String msg )
         throws StreamFormatException
     {
         throw new StreamFormatException( msg );
     }
 
     /* Read a single byte from stdin. */
     private int read()
         throws IOException
     {
         return in.read();
     }
 
     /* Print an error message and terminate. */
     private static void bad()
         throws StreamFormatException
     {
         err( "Data error" );
     }
 
     private long bb; /* the bit-buffer (left-justified) */
 
     private int bk; /* number of bits remaining in the `bb' bit-buffer */
 
     private int mbs; /* maximal block size (100k-900k in 100k steps) */
 
     private final Decoder ds = new Decoder();
 
     private int as; /*
                      * alphabet size (number of distinct prefix codes, 3-258)
                      */
 
     private int nt; /*
                      * number of prefix trees used for current block (2-6)
                      */
 
     private int ns; /* number of selectors (1-32767) */
 
     private final byte[][] len = new byte[6][259]; /*
                                                     * code lengths for different trees (element 258 is a sentinel)
                                                     */
 
     private final byte[] sel = new byte[32767]; /* selector MTF values */
 
     private final MtfDecoder mtf = new MtfDecoder();
 
     private final PrefixDecoder pd = new PrefixDecoder();
 
     private void need( int n )
         throws StreamFormatException, IOException
     {
         while ( bk < n )
         {
             long c = in.read();
             if ( c < 0 )
                 bad();
             bk += 8;
             bb += c << ( 64 - bk );
         }
     }
 
     private int peek( int n )
     {
         return (int) ( bb >>> ( 64 - n ) );
     }
 
     private void dump( int n )
     {
         bb <<= n;
         bk -= n;
     }
 
     /* Read and return `n' bits from the input stream. `n' must be <= 32. */
     private int get( int n )
         throws StreamFormatException, IOException
     {
         need( n );
         int x = peek( n );
         dump( n );
         return x;
     }
 
     /* Decode a single prefix code. The algorithm used is naive and slow. */
     private short get_sym()
         throws StreamFormatException, IOException
     {
         need( MAX_CODE_LENGTH );
         int x = pd.start[peek( HUFF_START_WIDTH )];
         int k = x & 0x1F;
         short s;
 
         if ( k <= HUFF_START_WIDTH )
         {
             /* Use look-up table in average case. */
             s = (short) ( x >> 5 );
         }
         else
         {
             /*
              * Code length exceeds HUFF_START_WIDTH, use canonical prefix decoding algorithm instead of look-up table.
              */
             while ( uge( bb, pd.base[k + 1] ) )
                 k++;
             s = pd.perm[pd.count[k] + (int) ( ( bb - pd.base[k] ) >>> ( 64 - k ) )];
         }
 
         dump( k );
         return s;
     }
 
     /* Retrieve bitmap. */
     private void bmp()
         throws StreamFormatException, IOException
     {
         int i, j;
         short b = (short) get( 16 );
         as = 0;
         mtf.initialize();
         for ( i = 0; i < 16; i++ )
         {
             if ( b < 0 )
             {
                 short s = (short) get( 16 );
                 for ( j = 0; j < 16; j++ )
                 {
                     if ( s < 0 )
                         mtf.imtf_slide[CMAP_BASE + as++] = (byte) ( 16 * i + j );
                     s *= 2;
                 }
             }
             b *= 2;
         }
         as += 2;
     }
 
     /* Retrieve selector MTF values. */
     private void smtf()
         throws StreamFormatException, IOException
     {
         int g;
         for ( g = 0; g < ns; g++ )
         {
             sel[g] = 0;
             while ( sel[g] < nt && get( 1 ) != 0 )
                 sel[g]++;
             if ( sel[g] == nt )
                 bad();
         }
         if ( ns > 18001 )
             ns = 18001;
     }
 
     /* Retrieve code lengths. */
     private void trees()
         throws StreamFormatException, IOException
     {
         int t, s;
         for ( t = 0; t < nt; t++ )
         {
             len[t][0] = (byte) get( 5 );
             for ( s = 0; s < as; s++ )
             {
                 if ( len[t][s] < 1 || len[t][s] > 20 )
                     bad();
                 while ( get( 1 ) != 0 )
                 {
                     len[t][s] += 1 - 2 * get( 1 );
                     if ( len[t][s] < 1 || len[t][s] > 20 )
                         bad();
                 }
                 len[t][s + 1] = len[t][s];
             }
         }
     }
 
     /* Retrieve block MTF values and apply IMTF transformation.. */
     private void data()
         throws StreamFormatException, IOException
     {
         int g, i, t;
         int s, r, h, c;
         ds.block_size = r = h = 0;
         Arrays.fill( ds.ftab, 0 );
         c = mtf.imtf_slide[mtf.imtf_row[0]] & 0xFF;
         int[] m = new int[6];
         for ( i = 0; i < 6; i++ )
             m[i] = i;
         for ( g = 0; g < ns; g++ )
         {
             i = sel[g];
             t = m[i];
             while ( i-- > 0 )
                 m[i + 1] = m[i];
             m[0] = t;
             pd.make_tree( len[t], as );
             for ( i = 0; i < 50; i++ )
             {
                 s = get_sym();
                 if ( s >= RUN_A )
                 {
                     r += 1 << ( h + s - RUN_A );
                     h++;
                     if ( r < 0 )
                         bad();
                 }
                 else
                 {
                     if ( ds.block_size + r > mbs )
                         bad();
                     ds.ftab[c] += r;
                     while ( r-- != 0 )
                         ds.tt[ds.block_size++] = c;
                     if ( s == EOB )
                         return;
                     c = mtf.mtf_one( s );
                     h = 0;
                     r = 1;
                 }
             }
         }
         bad();
     }
 
     /* Retrieve block. */
     private void retr()
         throws StreamFormatException, IOException
     {
         ds.rand = get( 1 ) != 0;
         ds.bwt_idx = get( 24 );
         bmp();
         nt = get( 3 );
         if ( nt < 2 || nt > 6 )
             bad();
         ns = get( 15 );
         smtf();
         trees();
         data();
     }
 
     private void decode_and_emit()
         throws StreamFormatException, IOException
     {
         if ( ds.bwt_idx >= ds.block_size )
             bad();
 
         ds.decode();
 
         byte[] buf = new byte[4096];
         int[] len = new int[1];
 
         Status status;
         do
         {
             len[0] = buf.length;
             status = ds.emit( buf, 0, len );
             out.write( buf, 0, buf.length - len[0] );
         }
         while ( status == MORE );
         assert ( status == OK );
     }
 
     /* Parse stream and bock headers, decompress any blocks found. */
     public void expand()
         throws StreamFormatException, IOException
     {
        int t, c;
         if ( get( 24 ) != 0x425A68 )
             bad();
        t = ( get( 8 ) - 0x31 ) & 0xFF;
         if ( t >= 9 )
             bad();
         do
         {
             c = 0;
             mbs = 100000 * ( t + 1 );
             while ( ( t = get( 16 ) ) == 0x3141 )
             {
                 if ( get( 32 ) != 0x59265359 )
                     bad();
                 t = get( 32 );
                 retr();
                 decode_and_emit();
                 if ( ds.crc != t )
                     bad();
                 c = ( ( c << 1 ) & 0xFFFFFFFF ) ^ ( c >>> 31 ) ^ t;
             }
             if ( t != 0x1772 )
                 bad();
             if ( get( 32 ) != 0x45385090 )
                 bad();
             if ( get( 32 ) != c )
                 bad();
             bk = 0;
         }
         while ( read() == 0x42 && read() == 0x5A && read() == 0x68 && ( t = get( 8 ) - 0x31 ) < 9 );
     }
 
     public static void main( String[] args )
     {
         try
         {
             MBC mbc = new MBC( System.in, System.out );
             mbc.expand();
         }
         catch ( StreamFormatException e )
         {
             System.err.println( "Data error" );
             System.exit( 1 );
         }
         catch ( IOException e )
         {
             e.printStackTrace();
             System.exit( 2 );
         }
     }
 }
