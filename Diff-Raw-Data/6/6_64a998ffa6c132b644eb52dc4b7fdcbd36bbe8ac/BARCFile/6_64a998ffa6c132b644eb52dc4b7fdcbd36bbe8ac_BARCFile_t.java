 /*
  * Copyright (c) 2008-2010 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package iudex.barc;
 
import static com.gravitext.util.Charsets.*;
 import iudex.http.Header;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import com.gravitext.util.ResizableCharBuffer;
 
 /**
  * Basic ARChive File Reader/Writer. Supports record types, up to three
  * HTTP-like key value header blocks ("meta", "request", and "response" ), and
  * optional GZIP compression.
  *
  * A BARCFile supports concurrent random access read by offset and concurrent
  * sequential read sessions via multiple {@link RecordReader} instances in
  * multiple threads. Writes are directly appended on to the end of a BARCFile by
  * stream and thus only one thread can write at a time. This must be externally
  * controlled. However, writes are atomically advertised on write record close,
  * so reading threads need not be blocked during a write operation. An external
  * process may see an empty or partial BARC record at the end of a
  * BARCFile while a record append operation is in progress.
  *
  * @see http://upload.wikimedia.org/wikipedia/commons/a/ae/BARC-LARC-XV-2.jpeg
  */
 public final class BARCFile implements Closeable
 {
     public BARCFile( File file ) throws IOException
     {
         file.createNewFile();
         _rafile = new RandomAccessFile( file, "rw" );
         _channel = _rafile.getChannel();
         _end = new AtomicLong( _channel.size() );
     }
 
     public void close() throws IOException
     {
         if( _currentRecord != null ) {
             _currentRecord.close();
             _currentRecord = null;
         }
 
         _rafile.close();
     }
 
     public Record append() throws IOException
     {
         if( _currentRecord != null ) _currentRecord.close();
 
         _currentRecord = new Record();
 
         return _currentRecord;
     }
 
     public void truncate() throws IOException
     {
         _end.set( 0 );
         _channel.truncate( 0 );
     }
 
     public void markReplaced( long offset ) throws IOException
     {
         Record rec = new Record( offset );
         rec.markReplaced();
         rec.close();
     }
 
     public Record read( long offset ) throws IOException
     {
         return new Record( offset );
     }
 
     /**
      * Returns a new and independent RecordReader over each Record in this
      * BARC file.
      */
     public RecordReader reader()
     {
         return new RecordReader();
     }
 
     public long size()
     {
         return _end.get();
     }
 
     /**
      * Provides sequential access to each record in a BARC File. This includes
      * R-records, which many applications will want to skip.
      */
     public final class RecordReader
     {
         /**
          * Returns next record or null if no record remains (EOF).
          * Record.close() should be called when done with each record.
          */
         Record next() throws IOException
         {
             Record record = null; //null as in end
 
             if( _offset >= _rEnd ) {
                 _rEnd = size();
             }
 
             //FIXME: Skip empty records here?
             //(And/or: Don't update offset of empty close?)
             if( _offset < _rEnd ) {
                 record = BARCFile.this.read( _offset );
                 _offset += ( HEADER_LENGTH + record.length() );
             }
             return record;
         }
 
         private long _rEnd = 0;
         private long _offset = 0;
     }
 
     public final class Record implements Closeable
     {
         public long offset()
         {
             return _offset;
         }
 
         public int length()
         {
             return _length;
         }
 
         public boolean isCompressed()
         {
             return _compressed;
         }
 
         public void setCompressed( boolean doCompress )
         {
             if( ( _out != null ) ||
                 ( _writeState == WriteState.CLOSED ) ) {
                 throw new IllegalStateException
                 ( "Invalid setCompressed() after first write or on read." );
             }
             _compressed = doCompress;
         }
 
         public char type()
         {
             return _type;
         }
 
         public void setType( char type )
         {
             if( _writeState == WriteState.CLOSED ) {
                 throw new IllegalStateException
                 ( "Invalid setType() on read or after close." );
             }
             _type = type;
         }
 
         public void writeMetaHeaders( Iterable<Header> headers )
             throws IOException
         {
             checkState( WriteState.META_HEADER );
             _metaHeaderLength = writeHeaderBlock( headers );
         }
 
         public void writeRequestHeaders( Iterable<Header> headers )
             throws IOException
         {
             checkState( WriteState.RQST_HEADER );
             _rqstHeaderLength = writeHeaderBlock( headers );
         }
 
         public void writeResponseHeaders( Iterable<Header> headers )
             throws IOException
         {
             checkState( WriteState.RESP_HEADER );
             _respHeaderLength = writeHeaderBlock( headers );
         }
 
         public List<Header> metaHeaders() throws IOException
         {
             if( _metaHeaders == null ) {
                 openInputStream();
                 _metaHeaders = ( _metaHeaderLength == 0 ) ?
                     EMPTY_HEADERS : parseHeaderBlock( _metaHeadBytes );
             }
             return _metaHeaders;
         }
 
         public List<Header> requestHeaders() throws IOException
         {
             if( _rqstHeaders == null ) {
                 openInputStream();
                 _rqstHeaders = ( _rqstHeaderLength == 0 ) ?
                     EMPTY_HEADERS : parseHeaderBlock( _rqstHeadBytes );
             }
             return _rqstHeaders;
         }
 
         public List<Header> responseHeaders() throws IOException
         {
             if( _respHeaders == null ) {
                 openInputStream();
                 _respHeaders = ( _respHeaderLength == 0 ) ?
                     EMPTY_HEADERS : parseHeaderBlock( _respHeadBytes );
             }
             return _respHeaders;
         }
 
         public InputStream bodyInputStream() throws IOException
         {
             openInputStream(); // if not already
             return _in;
         }
 
         public OutputStream bodyOutputStream() throws IOException
         {
             checkState( WriteState.BODY );
             openOutputStream(); // if not already
             return _out;
         }
 
         /**
          * Copy from other record (open for read) to this record (open for
          * write). This records isCompressed flag is used for the write
          * (instead of being copied).
          */
         public void copyFrom( Record other ) throws IOException
         {
             setType( other.type() );
             openOutputStream();
 
             checkState( WriteState.META_HEADER );
             other.openInputStream();
 
             _metaHeaderLength = writeHeaderBlock( other._metaHeadBytes );
             checkState( WriteState.RQST_HEADER );
             _rqstHeaderLength = writeHeaderBlock( other._rqstHeadBytes );
             checkState( WriteState.RESP_HEADER );
             _respHeaderLength = writeHeaderBlock( other._respHeadBytes );
 
             copy( other.bodyInputStream(), bodyOutputStream() );
             close();
 
             //FIXME: Optimize to single buffer copy in case where other and this
             //have same compressed setting.
         }
 
         public void close() throws IOException
         {
             if( _out != null ) _out.close();
             else               closeOutput();
 
             if( _in != null ) _in.close();
         }
 
         /**
          * Open for write.
          */
         Record() throws IOException
         {
             _offset = _end.get(); // Append to end
             writeBARCHeader(); // Initial copy
             _channel.position( _offset + HEADER_LENGTH );
         }
 
         /**
          * Mark this record as a replaced an rewrite its header.
          * @throws IOException
          */
         public void markReplaced() throws IOException
         {
             _type = 'R';
             writeBARCHeader();
             _writeState = WriteState.CLOSED;
         }
 
         /**
          * Open for read.
          */
         Record( long offset ) throws IOException
         {
             _offset = offset;
             _writeState = WriteState.CLOSED;
             readBARCHeader();
         }
 
         private void closeInput() throws IOException
         {
             _in = null;
             _metaHeaders   = null;
             _rqstHeaders   = null;
             _respHeaders   = null;
             _metaHeadBytes = null;
             _rqstHeadBytes = null;
             _respHeadBytes = null;
         }
 
         private void closeOutput() throws IOException
         {
             if( _writeState != WriteState.CLOSED  ) {
                 _writeState = WriteState.CLOSED;
                 long end = _channel.position();
                 _length = ( (int) ( end - _offset ) - HEADER_LENGTH );
                 if( _length > 0 ) {
                     _channel.write( CRLF_BYTES.duplicate() );
                     end += 2;
                     _length += 2;
                 }
                 writeBARCHeader(); // With adjusted lengths
                 _out = null;
                 _channel.force( false );
                 _end.set( end );
             }
         }
 
         private void checkState( WriteState nextState )
         {
             if( _writeState.ordinal() >= nextState.ordinal() ) {
                 throw new IllegalStateException
                     ( _writeState + " >= " + nextState );
             }
             _writeState = nextState;
         }
 
         private void readBARCHeader() throws IOException
         {
 
             ByteBuffer bbuff = ByteBuffer.allocate( HEADER_LENGTH );
             if( _channel.read( bbuff, _offset ) != HEADER_LENGTH ) {
                 throw new IOException( "Incomplete header read at offset: " +
                                        _offset );
             }
             bbuff.flip();
 
             CharBuffer cbuff = ASCII.decode( bbuff );
 
             if( ! "BARC1 ".contentEquals( cbuff.subSequence( 0, 6 ) ) ) {
                 throw new IOException( "Not a header at read offset:" +
                                        _offset );
                 //FIXME: Custom IOException derivative?
             }
             cbuff.position( 6 );
             _length = getHex( cbuff, 8 );
             cbuff.get(); // skip space
             _type = cbuff.get();
             _compressed = ( cbuff.get() == 'C' );
             cbuff.get(); // skip space
             _metaHeaderLength = getHex( cbuff, 4 );
             cbuff.get(); // skip space
             _rqstHeaderLength = getHex( cbuff, 4 );
             cbuff.get(); // skip space
             _respHeaderLength = getHex( cbuff, 4 );
         }
 
         private void writeBARCHeader() throws IOException
         {
             CharBuffer cbuff = CharBuffer.allocate( HEADER_LENGTH );
             cbuff.put( "BARC1 " );
             putHex( _length, 8, cbuff );
             cbuff.put( ' ' );
             cbuff.put( _type ).put( _compressed ? 'C' : 'P' );
             cbuff.put( ' ' );
             putHex( _metaHeaderLength, 4, cbuff );
             cbuff.put( ' ' );
             putHex( _rqstHeaderLength, 4, cbuff );
             cbuff.put( ' ' );
             putHex( _respHeaderLength, 4, cbuff );
             cbuff.put( CRLF ).put( CRLF );
             cbuff.flip();
 
             _channel.write( ASCII.encode( cbuff ),  _offset );
         }
 
         private ByteBuffer readHeaderBlock( int headerLength )
             throws IOException
         {
             ByteBuffer hbuff = ByteBuffer.allocate( headerLength );
             while( hbuff.remaining() > 0 ) {
                 int rlen = _in.read( hbuff.array(), hbuff.position(),
                                      hbuff.remaining() );
                if( rlen < 1 ) {
                     throw new IOException( "Incomplete header block." );
                 }
                 hbuff.position( hbuff.position() + rlen );
             }
             hbuff.flip();
             return hbuff;
         }
 
         private List<Header> parseHeaderBlock( ByteBuffer buffer )
             throws IOException
         {
             CharBuffer cbuff = UTF_8.decode( buffer );
             List<Header> headers = new ArrayList<Header>( 6 );
             int i = cbuff.position();
             final int end = cbuff.limit() - 2; //-2 for end CRLF
             int last = i;
             CharSequence name = null;
             while( i < end ) {
                 final char c = cbuff.get( i );
                 if( name == null ) {
                     if( c == ':' ) {
                         name = cbuff.subSequence( last, i );
                         last = ( i += 2 );
                     }
                     else ++i;
                 }
                 else if( c == '\r' ) {
                     headers.add(
                         new Header( name, cbuff.subSequence( last, i ) ) );
                     name = null;
 
                     if ( cbuff.get( ++i ) != '\n' ) {
                         throw new IOException(
                             "Invalid header content: \\r, !=\\n" );
                     }
                     last = ++i;
                 }
                 else ++i;
             }
 
             return headers;
         }
 
         private int writeHeaderBlock( Iterable<Header> headers )
             throws IOException
         {
             ResizableCharBuffer cbuff = new ResizableCharBuffer( 256 );
 
             for( Header header : headers ) {
                 putObj( header.name(), cbuff );
                 cbuff.put( ": " );
                 putObj( header.value(), cbuff );
                 cbuff.put( CRLF );
             }
             cbuff.put( CRLF );
 
             ByteBuffer bbuff = UTF_8.encode( cbuff.flipAsCharBuffer() );
 
             int length = bbuff.remaining();
 
             openOutputStream(); // if not already
 
             _out.write( bbuff.array(),
                         bbuff.arrayOffset() + bbuff.position(),
                         bbuff.remaining() );
 
             return length;
         }
 
         private int writeHeaderBlock( ByteBuffer block )
             throws IOException
         {
            int length = block.remaining();
            openOutputStream(); // if not already
 
            _out.write( block.array(),
                        block.arrayOffset() + block.position(),
                        block.remaining() );
 
            return length;
         }
 
         private void openInputStream() throws IOException
         {
             if( _in == null ) {
                 int len = _length;
                 if( len >= 2 ) len -= 2; //-2 for end CRLF
                 _in = new RecordInputStream( _offset + HEADER_LENGTH, len );
                 if( _compressed && len > 0 ) {
                     _in = new GZIPInputStream( _in, BUFFER_SIZE );
                 }
 
                 _metaHeadBytes = readHeaderBlock( _metaHeaderLength );
                 _rqstHeadBytes = readHeaderBlock( _rqstHeaderLength );
                 _respHeadBytes = readHeaderBlock( _respHeaderLength );
             }
         }
 
         private void openOutputStream() throws IOException
         {
             if( _out == null ) {
                 _out = new RecordOutputStream();
 
                 if( _compressed ) {
                     _out = new GZIPOutputStream( _out, BUFFER_SIZE );
                 }
             }
         }
 
         private final class RecordOutputStream extends OutputStream
         {
             @Override
             public void close() throws IOException
             {
                 Record.this.closeOutput();
             }
 
             @Override
             public void write( byte[] bytes, int offset, int length )
                 throws IOException
             {
                 _channel.write( ByteBuffer.wrap( bytes, offset, length ) );
             }
 
             @Override
             public void write( int b ) throws IOException
             {
                 _channel.write( ByteBuffer.wrap( new byte [] { (byte) b } ) );
             }
         }
 
         private final class RecordInputStream extends InputStream
         {
             RecordInputStream( long offset, int length )
             {
                 _offset = offset;
                 _end = _offset + length;
             }
 
             @Override
             public void close() throws IOException
             {
                 Record.this.closeInput();
             }
 
             @Override
             public int read( byte[] b, int offset, int length )
                 throws IOException
             {
                 if( ( _offset + length ) > _end ) {
                     length = (int) ( _end - _offset );
                     if( length == 0 ) return -1;
                 }
                 ByteBuffer buff = ByteBuffer.wrap( b, offset, length );
                 int rlen = _channel.read( buff, _offset );
                 if( rlen > 0 ) _offset += rlen;
                 return rlen;
             }
 
             @Override
             public int read() throws IOException
             {
                 if( _offset < _end ) {
                     ByteBuffer buff = ByteBuffer.allocate( 1 );
                     int len = _channel.read( buff, _offset );
                     if( len == 1 ) {
                         ++_offset;
                         buff.flip();
                         return ( buff.get() & 0xFF );
                     }
                 }
                 return -1; //EOF
             }
 
             private long _offset;
             private final long _end;
         }
 
         private final long _offset;
         private int _length = 0;
         private char _type = 'R';
         private boolean _compressed = false;
         private int _metaHeaderLength = 0;
         private int _rqstHeaderLength = 0;
         private int _respHeaderLength = 0;
 
         private OutputStream _out = null;
         private WriteState _writeState = WriteState.BEGIN;
 
         private InputStream _in = null;
         private ByteBuffer _metaHeadBytes = null;
         private ByteBuffer _rqstHeadBytes = null;
         private ByteBuffer _respHeadBytes = null;
         private List<Header> _metaHeaders = null;
         private List<Header> _rqstHeaders = null;
         private List<Header> _respHeaders = null;
     }
 
     static final int HEADER_LENGTH = 36;
 
     private static final List<Header> EMPTY_HEADERS = Collections.emptyList();
 
     private static final String CRLF = "\r\n";
 
     private static final ByteBuffer CRLF_BYTES =
         ASCII.encode( CharBuffer.wrap( CRLF ) );
 
     private static final int BUFFER_SIZE = 2048;
 
     private static enum WriteState {
         BEGIN,
         META_HEADER,
         RQST_HEADER,
         RESP_HEADER,
         BODY,
         CLOSED
     };
 
     private Record _currentRecord;
     private final RandomAccessFile _rafile;
     private final FileChannel _channel;
     private final AtomicLong _end;
 
     static int copy( InputStream in, OutputStream out )
         throws IOException
     {
         int total = 0;
         int len = 0;
         final byte[] buff = new byte[ BUFFER_SIZE ];
         while( true ) {
             len = in.read( buff );
             if( len < 0 ) break;
             if( len > 0 ) {
                 out.write( buff, 0, len );
                 total += len;
             }
         }
         return total;
     }
 
     static void putObj( Object it, ResizableCharBuffer b )
     {
         if( it instanceof CharSequence ) b.put( (CharSequence ) it );
         else b.put( it.toString() );
     }
 
     static void putHex( int number,
                         int digits,
                         CharBuffer out )
     {
         int i = 0;
         int shift = 4 * digits;
         while( i < digits ){
             shift -= 4;
             out.put( HEX_DIGITS[ ( number >> shift ) & 0x0f ] );
             ++i;
         }
     }
 
     static int getHex( CharBuffer cbuff, int digits )
         throws IOException
     {
         int value = 0;
         while( --digits >= 0 ) {
             value <<= 4;
             char d = cbuff.get();
             if( ( d >= '0' ) && ( d <= '9' ) ) {
                 value += ( d - '0' );
             }
             else if( ( d >= 'a' ) && ( d <= 'f' ) ) {
                 value += 10 + ( d - 'a' );
             }
             else throw new IOException( "Illegal hex digit: [" + d + "]." );
         }
         return value;
     }
 
     private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
 
 }
