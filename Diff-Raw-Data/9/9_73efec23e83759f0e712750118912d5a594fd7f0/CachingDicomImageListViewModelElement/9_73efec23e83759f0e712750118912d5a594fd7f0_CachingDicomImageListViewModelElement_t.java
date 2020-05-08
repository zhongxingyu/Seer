 package de.sofd.viskit.model;
 
 import de.sofd.draw2d.Drawing;
 import de.sofd.util.FloatRange;
 import de.sofd.viskit.test.windowing.RawDicomImageReader;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.ShortBuffer;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import org.dcm4che2.data.BasicDicomObject;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.data.UID;
 import org.dcm4che2.io.DicomOutputStream;
 import org.dcm4che2.media.FileMetaInformation;
 
 import com.sun.opengl.util.BufferUtil;
 
 /**
  * Implements getDicomObject(), getImage() as caching delegators to the (subclass-provided)
  * methods getImageKey(), getBackendDicomObject(), and optionally getBackendImage() and getBackendDicomObjectMetaData().
  *
  * TODO: Optional caching of #getRawImage()?
  *
  * @author olaf
  */
 public abstract class CachingDicomImageListViewModelElement extends AbstractImageListViewModelElement implements DicomImageListViewModelElement {
 
     static {
         RawDicomImageReader.registerWithImageIO();
     }
 
     /**
      * Extract from the backend and return the DicomObject. This method should not cache the
      * results or anything like that (this base class will do that), so it may be time-consuming.
      *
      * @return
      */
     protected abstract DicomObject getBackendDicomObject();
 
     /**
      * Same as {@link #getBackendDicomObject() }, but for the image. Default implementation
      * extracts the image from the getBackendDicomObject().
      *
      * @return
      */
     protected BufferedImage getBackendImage() {
         DicomObject dcmObj = getBackendDicomObject();
 
         // extract the BufferedImage from the received imageDicomObject
         ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
         DicomOutputStream dos = new DicomOutputStream(bos);
         try {
             String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
             if (null == tsuid) {
                 tsuid = UID.ImplicitVRLittleEndian;
             }
             FileMetaInformation fmi = new FileMetaInformation(dcmObj);
             fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
             dos.writeFileMetaInformation(fmi.getDicomObject());
             dos.writeDataset(dcmObj, tsuid);
             dos.close();
 
             Iterator it = ImageIO.getImageReadersByFormatName("RAWDICOM");
             if (!it.hasNext()) {
                 throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
             }
             ImageReader reader = (ImageReader) it.next();
             ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
             if (null == in) {
                 throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
             }
             try {
                 reader.setInput(in);
                 return reader.read(0);
             } finally {
                 in.close();
             }
         } catch (IOException e) {
             throw new IllegalStateException("error trying to extract image from DICOM object", e);
         }
     }
 
     protected FloatRange pixelValuesRange, usedPixelValuesRange;
 
 
     // TODO: use a utility library for the cache
 
     private static class LRUMemoryCache<K,V> extends LinkedHashMap<K,V> {
         private final int maxSize;
         public LRUMemoryCache(int maxSize) {
             this.maxSize = maxSize;
         }
         @Override
         protected boolean removeEldestEntry(Entry<K,V> eldest) {
             return this.size() > maxSize;
         }
     }
 
     // TODO: unify the two caches into one
 
     private static LRUMemoryCache<Object, DicomObject> dcmObjectCache
         = new LRUMemoryCache<Object, DicomObject>(5);
 
     private static LRUMemoryCache<Object, BufferedImage> imageCache
         = new LRUMemoryCache<Object, BufferedImage>(5);
 
     private static LRUMemoryCache<Object, DicomObject> rawDicomImageMetadataCache
         = new LRUMemoryCache<Object, DicomObject>(2000);
 
 
     @Override
     public DicomObject getDicomObject() {
         DicomObject result = dcmObjectCache.get(getImageKey());
         if (result == null) {
             result = getBackendDicomObject();
             dcmObjectCache.put(getImageKey(), result);
         }
         return result;
     }
 
     public boolean isDicomObjectCached() {
         return dcmObjectCache.containsKey(getImageKey());
     }
 
     public boolean isImageCached() {
         return imageCache.containsKey(getImageKey());
     }
 
     @Override
     public DicomObject getDicomImageMetaData() {
         DicomObject result = rawDicomImageMetadataCache.get(getImageKey());
         if (result == null) {
             result = getBackendDicomImageMetaData();
             rawDicomImageMetadataCache.put(getImageKey(), result);
         }
         return result;
     }
 
     /**
      * Same as {@link #getBackendDicomObject() }, but for the DICOM metadata ({@link #getDicomImageMetaData() }).
      * Default implementation extracts the metadata from the getBackendDicomObject().
      *
      * @return
      */
    protected DicomObject getBackendDicomImageMetaData() {
         // even though the getDicomObject() could serve as the metadata object
         // (it's a superset of it -- essentially, it's the metadata plus the pixel data),
         // we extract the metadata subset and return that, because it will be much smaller
         // in terms of memory footprint and thus many more of these objects fit in the
         // dicomMetadataCache. Alternatively, we might also not have a dicomMetadataCache
         // at all and always return the getDicomObject() directly, relying on its cache --
         // but it contains fewer elements, and the returned complete getDicomObject()s
         // would be large and may consume large amounts of heap space depending on how
         // long the caller keeps those DicomObjects referenced
         DicomObject result = new BasicDicomObject();
         getBackendDicomObject().subSet(0, Tag.PixelData - 1).copyTo(result);  // make a deep copy so no reference to the PixelData is kept
         return result;
     }
 
     @Override
     public BufferedImage getImage() {
         BufferedImage result = imageCache.get(getImageKey());
         if (result == null) {
             result = getBackendImage();
             imageCache.put(getImageKey(), result);
         }
         return result;
     }
 
     @Override
     public boolean hasRawImage() {
         return null != maybeGetProxyRawImage();
     }
 
     @Override
     public boolean isRawImagePreferable() {
         return hasRawImage();
     }
 
     @Override
     public RawImage getRawImage() {
         RawImageImpl result = (RawImageImpl) getProxyRawImage();
         result.setPixelData(BufferUtil.newShortBuffer(getDicomObject().getShorts(Tag.PixelData))); // type of buffer may later depend on image metadata
         return result;
     }
 
     @Override
     public RawImage getProxyRawImage() {
         RawImageImpl result = maybeGetProxyRawImage();
         if (null == result) {
             throw new IllegalStateException("this model element can't provide a raw image");
         }
         return result;
     }
 
     protected RawImageImpl maybeGetProxyRawImage() {
         DicomObject imgMetadata = getDicomImageMetaData();
         int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
         if (bitsAllocated <= 0) {
             return null;
         }
         int bitsStored = imgMetadata.getInt(Tag.BitsStored);
         if (bitsStored <= 0) {
             return null;
         }
         boolean isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
         // TODO: return null if compressed
         // TODO: support for RGB (at least don't misinterpret it as luminance)
         // TODO: account for endianness (Tag.HighBit)
         int pixelFormat, pixelType;
         // TODO: maybe use static multidimensional tables instead of nested switch statements
         switch (bitsAllocated) {
             case 8:
                 return null;
             case 16:
                 pixelFormat = RawImage.PIXEL_FORMAT_LUMINANCE;
                 switch (bitsStored) {
                     case 12:
                         pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_12BIT : RawImage.PIXEL_TYPE_UNSIGNED_12BIT);
                         break;
                     case 16:
                         pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_16BIT : RawImage.PIXEL_TYPE_UNSIGNED_16BIT);
                         break;
                     default:
                         return null;
                 }
                 break;
             default:
                 return null;
         }
         int width = imgMetadata.getInt(Tag.Columns);
         int height = imgMetadata.getInt(Tag.Rows);
         return new RawImageImpl(width, height, pixelFormat, pixelType, null);
     }
 
     @Override
     public FloatRange getPixelValuesRange() {
         if (null == pixelValuesRange) {
             setPixelValuesRange();
         }
         return pixelValuesRange;
     }
 
     protected void setPixelValuesRange() {
         if (hasRawImage() && isRawImagePreferable()) {
             RawImage imgMetaData = getProxyRawImage();
             // TODO: maybe use static multidimensional tables instead of nested switch statements
             switch (imgMetaData.getPixelType()) {
                 case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
                     pixelValuesRange = new FloatRange(0, 255);
                     break;
                 case RawImage.PIXEL_TYPE_UNSIGNED_12BIT:
                     pixelValuesRange = new FloatRange(0, 4095);
                     break;
                 case RawImage.PIXEL_TYPE_UNSIGNED_16BIT:
                     pixelValuesRange = new FloatRange(0, 65535);
                     break;
                 case RawImage.PIXEL_TYPE_SIGNED_12BIT:
                     pixelValuesRange = new FloatRange(-2048, 2047);
                     break;
                 case RawImage.PIXEL_TYPE_SIGNED_16BIT:
                     pixelValuesRange = new FloatRange(-32768, 32767);
                     break;
             }
         } else {
             // TODO: obtain from getImage();
             pixelValuesRange = new FloatRange(0, 4095);
         }
     }
 
     @Override
     public FloatRange getUsedPixelValuesRange() {
         if (null == usedPixelValuesRange) {
             setUsedPixelValuesRange();
         }
         return usedPixelValuesRange;
     }
 
     protected void setUsedPixelValuesRange() {
         if (hasRawImage() && isRawImagePreferable()) {
             RawImage img = getRawImage();  // we rely on this being fast after the first call b/c of BasicDicomObject's internal caching of getShorts(Tag.PixelData)
             short min = Short.MAX_VALUE;
             short max = Short.MIN_VALUE;
             int pxCount = (img.getPixelFormat() == RawImage.PIXEL_FORMAT_RGB ? 3 : 1) * img.getWidth() * img.getHeight();
             ShortBuffer buf = (ShortBuffer) img.getPixelData();
             for (int i = 0; i < pxCount; i++) {
                 short val = buf.get(i);
                 if (val < min) { min = val; }
                 if (val > max) { max = val; }
             }
             usedPixelValuesRange = new FloatRange(min, max);
         } else {
             // TODO: obtain from getImage();
             usedPixelValuesRange = new FloatRange(200, 800);
         }
     }
 
 }
