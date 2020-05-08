 /*
  * $Id$
  * (c) Copyright 2009 freiheit.com technologies GmbH
  *
  * Created on 20.10.2009 by Marcus Thiesen (marcus@freiheit.com)
  *
  * This file contains unpublished, proprietary trade secret information of
  * freiheit.com technologies GmbH. Use, transcription, duplication and
  * modification are strictly prohibited without prior written consent of
  * freiheit.com technologies GmbH.
  */
 package org.thiesen.exiftool.tiff;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 
 import javax.annotation.Nonnull;
 
 import org.apache.commons.lang.StringUtils;
 import org.thiesen.exiftool.tiff.TiffReader.IntReader;
 
 
 class IFDEntry {
     private final static Charset ASCII = Charset.forName("ASCII");
     
     private final IFDEntryTag _tag;
     private final IFDEntryType _type;
     private final long _numberOfValues;
     private final long _valueOrOffsetInBytes;
     private final int _tagFieldId;
     
     private final String _stringValue;
     private final byte[] _byteValue;
     
     public IFDEntry(IntReader intReader, byte[] tiffData, int tagFieldIdentifier, int fieldType,
             long numberOfValues, long valueOrOffsetInBytes) throws IOException {
         super();
         _tagFieldId = tagFieldIdentifier;
         _tag = IFDEntryTag.valueOf( tagFieldIdentifier );
         _type = IFDEntryType.valueOf( fieldType );
         _numberOfValues = numberOfValues;
         _valueOrOffsetInBytes = valueOrOffsetInBytes;
 
         String stringValue = null;
         byte[] byteValue = null;
         switch (_type) {
         case ASCII: byteValue = Arrays.copyOfRange(tiffData, (int)_valueOrOffsetInBytes, (int)_valueOrOffsetInBytes + (int)_numberOfValues); stringValue = new String( byteValue, ASCII ); break;
        case SHORT: stringValue = readUnsignedShort( intReader, valueOrOffsetInBytes, tiffData ); break;
         case LONG: if ( _valueOrOffsetInBytes != 0 ) stringValue = readUnsingedInt( intReader, valueOrOffsetInBytes, tiffData ); break;
         case BYTE: byteValue = Arrays.copyOfRange(tiffData, (int)_valueOrOffsetInBytes, (int)_valueOrOffsetInBytes + (int)_numberOfValues );
         }
 
         _byteValue = byteValue != null ? byteValue : new byte[0];
         _stringValue = StringUtils.defaultString(stringValue);
 
     }
 
     private String readUnsingedInt(IntReader intReader, long valueOrOffsetInBytes, byte[] tiffData) throws IOException {
         final ByteArrayInputStream in = new ByteArrayInputStream(tiffData);
         if ( in.skip(valueOrOffsetInBytes) != valueOrOffsetInBytes ) {
             throw new IllegalArgumentException("Premature end of file");
         }
 
         return "" + intReader.readUnsignedInt32(in);
     }
 
     private String readUnsignedShort(IntReader intReader, long valueOrOffsetInBytes, byte[] tiffData) throws IOException {
         final ByteArrayInputStream in = new ByteArrayInputStream(tiffData);
         if ( in.skip(valueOrOffsetInBytes) != valueOrOffsetInBytes ) {
             throw new IllegalArgumentException("Premature end of file");
         }
 
         return "" + intReader.readUnsignedInt16(in);
     }
 
     @Override
     public String toString() {
         return "IFDEntry of Type " + _tag + " with type " + _type + " ["+ _tagFieldId +"] (numberOfValues: " + _numberOfValues + ", valueOrOffsetInBytes: " + _valueOrOffsetInBytes + ") " + getStringValue();
     }
 
     public boolean isDirectory() {
         return _tag != null && _tag.isDirectory();
     }
 
 
     public long getOffset() {
         return _valueOrOffsetInBytes;
     }
 
     public @Nonnull byte[] getByteValue() {
         return _byteValue;
     }
 
     public String getStringValue() {
         return _stringValue;
     }
 
 }
