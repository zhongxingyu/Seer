 package org.hampelratte.net.mms.asf.objects;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.hampelratte.net.mms.asf.UnknownAsfObjectException;
 import org.hampelratte.net.mms.asf.io.ASFInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ASFExtendedContentDescriptionObject extends ASFHeaderObject {
 
     private static transient Logger logger = LoggerFactory.getLogger(ASFExtendedContentDescriptionObject.class);
     
     private int descriptorCount;
     private List<ContentDescriptor<?>> contentDescriptors = new ArrayList<ContentDescriptor<?>>();
     
     @Override
     public void setData(byte[] data) throws IOException, UnknownAsfObjectException, InstantiationException, IllegalAccessException {
         super.setData(data);
 
         ByteArrayInputStream bin = new ByteArrayInputStream(data);
         ASFInputStream asfin = new ASFInputStream(bin);
         
         descriptorCount = asfin.readLEShort();
         
         for (int i = 0; i < descriptorCount; i++) {
             try {
                 int nameLength = asfin.readLEShort();
                 String name = asfin.readUTF16LE(nameLength);
                 logger.trace("Content descriptor name: {}", name);
                 
                 int dataType = asfin.readLEShort();
                 int dataLength = asfin.readLEShort();
                 
                 switch (dataType) {
                 case 0:
                     String stringValue = asfin.readUTF16LE(dataLength);
                     contentDescriptors.add(new ContentDescriptor<String>(name, stringValue));
                     break;
                 case 1:
                     byte[] byteValue = new byte[dataLength];
                     asfin.read(byteValue);
                     contentDescriptors.add(new ContentDescriptor<byte[]>(name, byteValue));
                     break;
                 case 2:
                    boolean boolValue = asfin.readBoolean();
                     contentDescriptors.add(new ContentDescriptor<Boolean>(name, boolValue));
                     break;
                 case 3:
                     long intValue = asfin.readLEInt();
                     contentDescriptors.add(new ContentDescriptor<Long>(name, intValue));
                     break;
                 case 4:
                     long longValue = asfin.readLELong();
                     contentDescriptors.add(new ContentDescriptor<Long>(name, longValue));
                     break;
                 case 5:
                     int shortValue = asfin.readLEShort();
                     contentDescriptors.add(new ContentDescriptor<Integer>(name, shortValue));
                     break;
                 default:
                     logger.warn("Unknown content descriptor type " + dataType + ". Skipping data.");
                    asfin.skip(dataLength);
                 }
             } catch (Exception e) {
                 logger.warn("Couldn't read content descriptor", e);
             }
         }
     }
     
     @Override
     public String toString() {
         StringBuilder s = new StringBuilder(super.toString());
         s.append(" [Descriptor Count:").append(descriptorCount);
         s.append(",ContentDescriptors:[");
         for (Iterator<ContentDescriptor<?>> iterator = contentDescriptors.iterator(); iterator.hasNext();) {
             ContentDescriptor<?> cd = iterator.next();
             s.append(cd.getName()).append('=').append(cd.getValue());
             s.append(iterator.hasNext() ? "," : "");
         }
         s.append("]]");
         return s.toString();
     }
     
     public class ContentDescriptor<T> {
         private String name;
 
         private T value;
         
         public ContentDescriptor(String name) {
             this.name = name;
         }
 
         public ContentDescriptor(String name, T value) {
             super();
             this.name = name;
             this.value = value;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public T getValue() {
             return value;
         }
 
         public void setValue(T value) {
             this.value = value;
         }
     }
 }
