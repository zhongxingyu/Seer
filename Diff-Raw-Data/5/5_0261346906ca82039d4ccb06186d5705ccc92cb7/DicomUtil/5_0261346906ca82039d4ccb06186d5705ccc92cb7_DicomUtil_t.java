 package de.sofd.viskit.util;
 
 import java.nio.*;
 import java.util.*;
 
 import org.dcm4che2.data.*;
 
 import com.sun.opengl.util.*;
 
 import de.sofd.util.*;
 import de.sofd.viskit.image3D.jogl.util.LinAlg;
 import de.sofd.viskit.model.WindowingFunction;
 
 public class DicomUtil {
 
     public static ShortBuffer getFilledShortBuffer(ArrayList<DicomObject> dicomList) {
         if (dicomList.isEmpty())
             return null;
 
         // dicom object with reference values
         DicomObject refDicom = dicomList.get(0);
 
         int[] dim = new int[3];
         dim[0] = refDicom.getInt(Tag.Columns);
         dim[1] = refDicom.getInt(Tag.Rows);
         dim[2] = dicomList.size();
         
         ShortBuffer dataBuf = BufferUtil.newShortBuffer(dim[0] * dim[1] * dim[2]);
 
         for (DicomObject dicomObject : dicomList)
         {
             
             short[] pixData = dicomObject.getShorts(Tag.PixelData);
             float rescaleIntercept = dicomObject.getFloat(Tag.RescaleIntercept);
             float rescaleSlope = dicomObject.getFloat(Tag.RescaleSlope);
             
             if ( rescaleSlope != 0 || rescaleIntercept != 0)
             {
                 for ( int i = 0; i < pixData.length; ++i )
                     pixData[i] = (short)(pixData[i]*rescaleSlope + rescaleIntercept); 
             }
             
             dataBuf.put(pixData);
         }
 
         dataBuf.rewind();
 
         return dataBuf;
     }
 
     public static ShortBuffer getFilledShortBuffer(DicomObject dicomObject) {
         int[] dim = new int[2];
         dim[0] = dicomObject.getInt(Tag.Columns);
         dim[1] = dicomObject.getInt(Tag.Rows);
 
         ShortBuffer dataBuf = BufferUtil.newShortBuffer(dim[0] * dim[1]);
 
         dataBuf.put(dicomObject.getShorts(Tag.PixelData));
 
         dataBuf.rewind();
 
         return dataBuf;
     }
 
     public static ArrayList<ShortBuffer> getFilledShortBufferList(ArrayList<DicomObject> dicomList) {
         ArrayList<ShortBuffer> shortBufferList = new ArrayList<ShortBuffer>(dicomList.size());
 
         for (DicomObject dicomObject : dicomList)
             shortBufferList.add(getFilledShortBuffer(dicomObject));
 
         return shortBufferList;
     }
 
     public static ShortBuffer getWindowing(ArrayList<DicomObject> dicomList, ShortRange range) {
         ShortBuffer windowing = ShortBuffer.allocate(dicomList.size() * 2);
         
         for (DicomObject dicomObject : dicomList) {
             short winCenter = (short) dicomObject.getFloat(Tag.WindowCenter);
             short winWidth = (short) dicomObject.getFloat(Tag.WindowWidth);
 
             if (winCenter == 0 && winWidth == 0) {
                 winWidth = (short) Math.min(range.getDelta(), Short.MAX_VALUE);
                 winCenter = (short) (range.getMin() + range.getDelta() / 2);
             }
 
             windowing.put(winCenter);
             windowing.put(winWidth);
 
         }
 
         windowing.rewind();
 
         return windowing;
     }
 
     public static ArrayList<WindowingFunction> getWindowing(ShortBuffer windowing) {
         ArrayList<WindowingFunction> windowingList = new ArrayList<WindowingFunction>();
 
         for (int i = 0; i < windowing.capacity() / 2; ++i) {
             windowingList.add(new WindowingFunction(windowing.get(i * 2 + 0), windowing.get(i * 2 + 1)));
         }
 
         return windowingList;
     }
 
     
     public static PatientBasedMainAxisOrientation getSliceOrientation(DicomObject dobj) {
         return getSliceOrientation(dobj, 0);
     }
     
     public static PatientBasedMainAxisOrientation getSliceOrientation(DicomObject dobj, int frame) {
         try {
             float[] firstRowAndColumn = getImageOrientationPatient(dobj, frame);
             float[] surfaceNormal = LinAlg.cross(firstRowAndColumn, 0, firstRowAndColumn, 3, null);
             MainAxisDirection normalDir = getMainAxisDirectionOfUnitVector(surfaceNormal);
             MainAxisOrientation normalOrient = getMainAxisOrientationByDirection(normalDir);
             PatientBasedMainAxisOrientation result = getSliceOrientationBySurfaceNormalOrientation(normalOrient);
             return result;
         } catch (NullPointerException ignore) {
             return null;
         } catch (NumberFormatException ignore) {
             return null;
         } catch (IndexOutOfBoundsException ignore) {
             return null;
         }
     }
 
     
     public static enum MainAxisOrientation {X, Y, Z};
     
     public static enum MainAxisDirection {
         PLUS_X, MINUS_X, PLUS_Y, MINUS_Y, PLUS_Z, MINUS_Z;
         
         public static final MainAxisDirection[] ALL = {PLUS_X, MINUS_X, PLUS_Y, MINUS_Y, PLUS_Z, MINUS_Z};
     };
     
    public static enum PatientBasedMainAxisOrientation {SAGITTAL, CORONAL, TRANSVERSAL};
     
     private static final MainAxisOrientation[] mainAxisOrientationByDirection = {  // index = direction.ordinal()
         MainAxisOrientation.X, MainAxisOrientation.X,
         MainAxisOrientation.Y, MainAxisOrientation.Y,
         MainAxisOrientation.Z, MainAxisOrientation.Z
     };
 
     public static MainAxisOrientation getMainAxisOrientationByDirection(MainAxisDirection dir) {
         return mainAxisOrientationByDirection[dir.ordinal()];
     }
     
     private static final MainAxisDirection[] mainAxisReverseDirectionByDirection = {  // index = direction.ordinal()
         MainAxisDirection.MINUS_X, MainAxisDirection.PLUS_X,
         MainAxisDirection.MINUS_Y, MainAxisDirection.PLUS_Y,
         MainAxisDirection.MINUS_Z, MainAxisDirection.PLUS_Z
     };
     
     public static MainAxisDirection getReverseDirection(MainAxisDirection dir) {
         return mainAxisReverseDirectionByDirection[dir.ordinal()];
     }
     
     private static final PatientBasedMainAxisOrientation[] sliceOrientationBySurfaceNormalOrientation = {  // index = orientation.ordinal(), orient. in patient-based CS
        PatientBasedMainAxisOrientation.SAGITTAL,
         PatientBasedMainAxisOrientation.CORONAL,
         PatientBasedMainAxisOrientation.TRANSVERSAL
     };
     
     public static PatientBasedMainAxisOrientation getSliceOrientationBySurfaceNormalOrientation(MainAxisOrientation orient) {
         return sliceOrientationBySurfaceNormalOrientation[orient.ordinal()];
     }
     
     private static final float[][] mainAxisDirectionUnitVectorByDirection = {
         { 1, 0, 0}, {-1, 0, 0},
         { 0, 1, 0}, { 0,-1, 0},
         { 0, 0, 1}, { 0, 0,-1},
     };
     
     public static float[] getUnitVectorOfMainAxisDirection(MainAxisDirection dir) {
         return mainAxisDirectionUnitVectorByDirection[dir.ordinal()];
     }
     
     public static MainAxisDirection getMainAxisDirectionOfUnitVector(float[] v) {
         MainAxisDirection maxDir = null;
         float maxDot = Float.MIN_VALUE;
         for (MainAxisDirection dir : MainAxisDirection.ALL) {
             float[] dirVec = mainAxisDirectionUnitVectorByDirection[dir.ordinal()];
             float dot = LinAlg.dot(dirVec, v);
             if (dot > maxDot) {
                 maxDot = dot;
                 maxDir = dir;
             }
         }
         if (maxDot > 0.95 && maxDot < 1.000000001) {
             return maxDir;
         } else {
             // v doesn't point in any main axis direction very much, and/or isn't normalized
             // (there are pathological cases in which the latter isn't caught)
             return null;
         }
     }
     
     // patient RCS *directions* (anterior/posterior etc.) also depend on whether it's a head or torso image... (cf. http://en.wikipedia.org/wiki/Anatomical_terms_of_location#Medical_.28human.29_directional_terms)
     // ...so we don't determine that for now!
     
     /*
      * copied from org.dcm4che2.hp.plugins.AlongAxisComparator
      */
     public static float[] getImageOrientationPatient(DicomObject o, int frame)
     {
         float[] iop;
         if ((iop = o.getFloats(Tag.ImageOrientationPatient)) != null)
             return iop;
         
         // Check the shared first in the case of image orientation
         int[] tagPath = { 
                 Tag.SharedFunctionalGroupsSequence, 0,
                 Tag.PlaneOrientationSequence, 0,
                 Tag.ImageOrientationPatient };
         if ((iop = o.getFloats(tagPath)) != null)
             return iop;
         
         tagPath[0] = Tag.PerframeFunctionalGroupsSequence;
         tagPath[1] = frame;
         return o.getFloats(tagPath);
     }
 
 }
