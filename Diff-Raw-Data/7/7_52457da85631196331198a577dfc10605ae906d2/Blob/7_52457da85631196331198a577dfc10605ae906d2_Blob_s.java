 package org.apache.poi.hpsf;
 
import org.apache.poi.util.LittleEndian;

 import org.apache.poi.util.Internal;
 
 @Internal
 class Blob
 {
     private byte[] _value;
 
     Blob( byte[] data, int offset )
     {
         int size = LittleEndian.getInt( data, offset );
 
         if ( size == 0 )
         {
             _value = new byte[0];
             return;
         }
 
        _value = LittleEndian.getByteArray( _value, offset
                 + LittleEndian.INT_SIZE, size );
     }
 
     int getSize()
     {
         return LittleEndian.INT_SIZE + _value.length;
     }
 }
