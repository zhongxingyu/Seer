 package org.zephir.photorenamer.dao;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import mediautil.image.jpeg.LLJTran;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.sanselan.Sanselan;
 import org.apache.sanselan.common.IImageMetadata;
 import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
 import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
 import org.apache.sanselan.formats.tiff.TiffField;
 import org.apache.sanselan.formats.tiff.TiffImageMetadata;
 import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
 import org.apache.sanselan.formats.tiff.constants.TiffConstants;
 import org.apache.sanselan.formats.tiff.constants.TiffTagConstants;
 import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
 import org.apache.sanselan.formats.tiff.write.TiffOutputField;
 import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zephir.photorenamer.core.PhotoRenamerCore;
 import org.zephir.util.exception.CustomException;
 
 public final class JpegDAO {
 	private static Logger log = LoggerFactory.getLogger(PhotoRenamerCore.class);
 
 	private JpegDAO() {
 	}
 
 	private static SimpleDateFormat dateTimeOriginalItemFormat = new SimpleDateFormat("''yyyy:MM:dd HH:mm:ss''");
 
 	public static Date getDateTimeOriginal(File file) throws CustomException {
 		try {
 			IImageMetadata metadata = Sanselan.getMetadata(file);
 			if (metadata instanceof JpegImageMetadata) {
 				JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
 				TiffField dateField = jpegMetadata.findEXIFValue(TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
 				if (dateField != null) {
 					String dateTimeOriginalItem = dateField.getValueDescription();
 					return dateTimeOriginalItemFormat.parse(dateTimeOriginalItem);
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			throw new CustomException("getDateTimeOriginal(file='" + file.getAbsolutePath() + "') KO: " + e, e);
 		}
 	}
 
 	// http://www.impulseadventure.com/photo/exif-orientation.html
 	// EXIF orientation values
 	// 1 = top, left
 	// 3 = bottom, right
 	// 6 = right, top
 	// 8 = left, bottom
 	// 2* = top, right
 	// 4* = bottom, left
 	// 5* = left, top
 	// 7* = right, bottom
 	public static boolean rotateImage(File file) throws CustomException {
 		try {
 			IImageMetadata metadata = Sanselan.getMetadata(file);
 			if (metadata instanceof JpegImageMetadata) {
 				JpegImageMetadata metaJpg = (JpegImageMetadata) metadata;
 				TiffField orientationField = metaJpg.findEXIFValue(TiffTagConstants.TIFF_TAG_ORIENTATION);
 				if (orientationField != null) {
 					int orientation = orientationField.getIntValue();
 					int rotationNeeded = -1;
 					if (orientation == 6) { // orientation right, top
 						// rotate 90 clockwise
 						rotationNeeded = LLJTran.ROT_90;
 						log.debug("Rotating '" + file.getAbsolutePath() + "' 90 clockwise.");
 					} else if (orientation == 3) { // orientation bottom, right
 						// rotate 180 clockwise
 						rotationNeeded = LLJTran.ROT_180;
 						log.debug("Rotating '" + file.getAbsolutePath() + "' 180 clockwise.");
 					} else if (orientation == 8) { // orientation left, bottom
 						// rotate 270 clockwise
 						rotationNeeded = LLJTran.ROT_270;
 						log.debug("Rotating '" + file.getAbsolutePath() + "' 270 clockwise.");
 					}
 
 					if (rotationNeeded != -1) {
 						// 1. Initialize LLJTran and Read the entire Image including Appx markers
 						LLJTran llj = new LLJTran(file);
 						// If you pass the 2nd parameter as false, Exif information is not
 						// loaded and hence will not be written.
 						llj.read(LLJTran.READ_ALL, true);
 
 						// 2. Transform the image using default options along with
 						// transformation of the Orientation tags. Try other combinations of
 						// LLJTran_XFORM.. flags. Use a jpeg with partial MCU (partialMCU.jpg)
 						// for testing LLJTran.XFORM_TRIM and LLJTran.XFORM_ADJUST_EDGES
 						int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;
 						llj.transform(rotationNeeded, options);
 
 						// 3. Save the Image which is already transformed as specified by the
 						// input transformation in Step 2, along with the Exif header.
 						OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
 						llj.save(out, LLJTran.OPT_WRITE_ALL);
 						out.close();
 
 						// Cleanup
 						llj.freeMemory();
 
 						// update the metadata back
 						TiffImageMetadata exif = null;
 						if (metaJpg != null) {
 							exif = metaJpg.getExif();
 						}
 						TiffOutputSet outputSet = new TiffOutputSet();
 						if (exif != null) {
 							outputSet = exif.getOutputSet();
 						}
 
 						// change orientation tag info
 						if (outputSet != null) {
 							outputSet.removeField(TiffConstants.TIFF_TAG_ORIENTATION);
 							TiffOutputField newOrientationField = TiffOutputField.create(ExifTagConstants.EXIF_TAG_ORIENTATION, outputSet.byteOrder, 1);
 							TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
 							exifDirectory.add(newOrientationField);
 						}
 
 						// create stream using temp file for dst
 						File tempFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
 						OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));
 
 						// write/update EXIF metadata to output stream
 						try {
							new ExifRewriter().updateExifMetadataLossless(file, os, outputSet);
 						} finally {
 							if (os != null) {
 								os.close();
 							}
 						}
 
 						// copy temp file over original file
 						FileUtils.copyFile(tempFile, file);
 
 						return true;
 					}
 				}
 			}
 			return false;
 		} catch (Exception e) {
 			throw new CustomException("rotateImage(file='" + file.getAbsolutePath() + "') KO: " + e, e);
 		}
 	}
 }
