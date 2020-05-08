 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.metadata.IIOMetadata;
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
 import com.sun.media.imageio.plugins.tiff.TIFFField;
 
 /**
  * This class will read the MAR 300 image file type
  * 
  * This class is a sub class of the JavaImageLoader class. This reads the image
  * data. The following method initially reads the default tiff header while the
  * following reads the MAR specific headers. All information is read into a
  * hashmap as string object pairs. The MAR header is read as a stream of bytes
  * the value of which is determined in a C header file. Most values are 32bit ints.
  */
 public class MARLoader extends TIFFImageLoader implements IMetaLoader, Serializable {
 	static final int MAX_IMAGES = 9;
 	static final int MAR_HEADER_SIZE = 3072;
 	private boolean littleEndian;
 	private Map<String, Serializable> metadataTable = new HashMap<String, Serializable>();
 	private DiffractionMetadata diffMetadata;
 	
 	/**
 	 * @param FileName
 	 */
 	public MARLoader(String FileName) {
 		super(FileName);
 	}
 
 	@Override
 	protected Map<String, Serializable> createMetadata(IIOMetadata imageMetadata) throws ScanFileHolderException {
 		long offset = -1;
 		try {
 			TIFFDirectory tiffDir = TIFFDirectory.createFromMetadata(imageMetadata);
 
 			TIFFField[] tiffField = tiffDir.getTIFFFields();
 
 			for (TIFFField tfield : tiffField) {
 				if (tfield.getTagNumber() == 34710) {
 					offset = tfield.getAsLong(0);
 					continue;
 				}
 				metadataTable.put(tfield.getTag().getName(), tfield.getValueAsString(0));
 			}
 			if (offset < 0) {
 				throw new ScanFileHolderException(
 						"There was a problem getting to the begining of the MAR of the header");
 			}
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem loading tiff header metadata in the MAR Loader class", e);
 		}
 
 		File f = new File(fileName);
 
 		try {
 			InputStream is = new FileInputStream(f);
 			byte[] hbd = new byte[MAR_HEADER_SIZE];
 			is.skip(offset); // skip first Kb
 			is.read(hbd, 0, MAR_HEADER_SIZE);
 
 			// test big or little endian
 			int poss = 28;
 			if (Utils.beInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) == 4321) {
 				littleEndian = false;
 			}
 			if (Utils.leInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) == 1234) {
 				littleEndian = true;
 			} else {
 				throw new ScanFileHolderException("Unknown endian");
 			}
 			metadataTable.put("headerByteOrderLE", littleEndian);
 
 			// read header information 
 			// header format parameters 256 bytes
 			poss = 0; // current position -> position in the byte array
 			metadataTable.put("headerType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // flag for header type (can be
 												// used as magic number)
 			poss += 4;
 			byte[] b = new byte[16];
 			System.arraycopy(hbd, poss, b, 0, 16);
 			metadataTable.put("headerName", new String(b, "US-ASCII"));
 			// header name (MMX)
 			poss += 16;
 			metadataTable.put("headerMajorVersion", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]));// header_major_version
 																		// (n.)
 			poss += 4;
 			metadataTable.put("headerMinorVersion", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]));// header_minor_version
 																		// (.n)
 			poss += 4;
 			// int headerByteOrder =
 			// getHeaderInt(hbd[currentOffset],hbd[currentOffset
 			// +1],hbd[currentOffset +2],hbd[currentOffset +3]);
 			poss += 4;
 			// headerByteOrder already entered thus skip this header
 			boolean dataByteOrder = false;
 			if (Utils.beInt(hbd[poss], hbd[poss + 1],
 					hbd[poss + 2], hbd[poss + 3]) == 4321) {
 				dataByteOrder = false;
 			}
 			if (Utils.leInt(hbd[poss], hbd[poss + 1],
 					hbd[poss + 2], hbd[poss + 3]) == 1234) {
 				dataByteOrder = true;
 			} else {
 				// Do nothing--data should be read in superclass
 				// System.out.println("Unknown dataByteOrder");
 			}
 
 			metadataTable.put("dataByteOrder", dataByteOrder);
 			// BIG_ENDIAN (Motorola,MIPS); LITTLE_ENDIAN (DEC, Intel)
 			poss += 4;
 			metadataTable.put("headerSize", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// in bytes
 			poss += 4;
 			metadataTable.put("frameType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// flag for frame type
 			poss += 4;
 			metadataTable.put("magicNumber", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); 
 			// to be used as a flag - usually to indicate new file
 			poss += 4;
 			metadataTable.put("compressionType", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// type of image compression
 			poss += 4;
 			metadataTable.put("compression1", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 1
 			poss += 4;
 			metadataTable.put("compression2", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 2
 			poss += 4;
 			metadataTable.put("compression3", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 3
 			poss += 4;
 			metadataTable.put("compression4", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 4
 			poss += 4;
 			metadataTable.put("compression5", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 5
 			poss += 4;
 			metadataTable.put("compression6", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// compression parameter 6
 			poss += 4;
 			metadataTable.put("nHeaders", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // total number of headers
 			poss += 4;
 			metadataTable.put("nFast", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // number of pixels in one line
 			poss += 4;
 			metadataTable.put("nSlow", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // number of lines in image
 			poss += 4;
 			metadataTable.put("depth", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // number of bytes per pixel
 			poss += 4;
 			metadataTable.put("recordLength", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); 
 			// number of pixels between successive rows
 			poss += 4;
 			metadataTable.put("signifBits", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // true depth of data, in bits
 			poss += 4;
 			metadataTable.put("dataType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (signed,unsigned,float...)
 			poss += 4;
 			metadataTable.put("saturatedValue", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// value marks pixel as saturated
 			poss += 4;
 			metadataTable.put("sequence", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // TRUE or FALSE
 			poss += 4;
 			metadataTable.put("nImages", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); 
 			// total number of images - size of each is nfast*(nslow/nimages)
 			poss += 4;
 			metadataTable.put("origin", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // corner of origin
 			poss += 4;
 			metadataTable.put("orientation", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // direction of fast axis
 			poss += 4;
 			metadataTable.put("viewDirection", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // direction to view frame
 			poss += 4;
 			metadataTable.put("overflowLocation", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); // FOLLOWING_HEADER,// FOLLOWING_DATA
 			poss += 4;
 			metadataTable.put("over8Bits", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // # of pixels with counts > 255
 			poss += 4;
 			metadataTable.put("over16Bits", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // # of pixels with count > 65535
 			poss += 4;
 			metadataTable.put("multiplexed", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // multiplex flag
 			poss += 4;
 			metadataTable.put("numFastImages", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// # of images in fast direction
 			poss += 4;
 			metadataTable.put("numSlowImages", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // # of images in slow direction
 			poss += 4;
 			metadataTable.put("backgroundApplied", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// flags correction has been applied - hold magic number?
 			poss += 4;
 			metadataTable.put("biasApplied", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // flags correction has been
 												// applied - hold magic number ?
 			poss += 4;
 			metadataTable.put("flatFieldApplied", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// flags correction has been applied - hold magic number ?
 			poss += 4;
 			metadataTable.put("distortionApplied", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// flags correction has been applied - hold magic number ?
 			poss += 4;
 			metadataTable.put("originalHeaderType", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// Header/frame type from file that frame is read from
 			poss += 4;
 			metadataTable.put("fileSaved", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// Header/frame type from file that frame is read from
 			poss += 4;
 
 			// b = new byte[80]; // (64-40)*sizeof(int32)-16
 			// System.arraycopy(hbd, currentOffset, b, 0, 84);
 			// metadataTable.put("reserve1" , new String(b,"US-ASCII");
 
 			poss += 80; // move forward to ignore reserve1
 									// (64-40)*sizeof(int32)-16
 
 			
 			// Data statistics (128)
 			int[] totalCounts = new int[2];
 			totalCounts[0] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			totalCounts[1] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			metadataTable.put("totalCounts", totalCounts);
 			int[] specialCounts1 = new int[2];
 			specialCounts1[0] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			specialCounts1[1] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			metadataTable.put("specialCounts1", specialCounts1);
 			int[] specialCounts2 = new int[2];
 			specialCounts2[0] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			specialCounts2[1] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 			poss += 4;
 			metadataTable.put("specialCounts2", specialCounts1);
 			metadataTable.put("min", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("max", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("mean", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("rms", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("p10", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("p90", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			metadataTable.put("statsUpToDate", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));
 			poss += 4;
 			int[] pixelNoise = new int[MAX_IMAGES];
 			for (int i = 0; i < MAX_IMAGES; i++) {
 				pixelNoise[i] = getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]);
 				poss += 4;
 			}
 			metadataTable.put("pixelNoise", pixelNoise);
 			
 			poss += (32 - 13 - MAX_IMAGES) * 4; 
 			// Reserve 2 32-13-MAX_IMAGES*sizeof(int32)
 
 			// more Statistics (256)
 			int[] percentile = new int[128];
 			for (int i = 0; i < 128; i++) {
 				if (littleEndian) {
 					percentile[i] = Utils.leInt(hbd[poss],hbd[poss + 1]);
 				} else {
 					percentile[i] = Utils.beInt(hbd[poss],hbd[poss + 1]);
 				}
 				poss += 2;
 			}
 
 			// goniostat parameters (128)
 			metadataTable.put("xtalToDetector", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*distance in millimetres
 			poss += 4;
 			metadataTable.put("beamX", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// 1000*x beam position (pixels)
 			poss += 4;
 			metadataTable.put("beamY", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// 1000*y beam position (pixels)
 			poss += 4;
 			metadataTable.put("intergrationTime", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// integration time in milliseconds
 			poss += 4;
 			metadataTable.put("exposureTime", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// exposure time in milliseconds
 			poss += 4;
 			metadataTable.put("readoutTime", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// readout time in milliseconds
 			poss += 4;
 			metadataTable.put("nReads", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); 
 			// number of readouts to get this image
 			poss += 4;
 			metadataTable.put("start2theta", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*two_theta angle
 			poss += 4;
 			metadataTable.put("startOmega", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*omega angle
 			poss += 4;
 			metadataTable.put("startChi", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*chi angle
 			poss += 4;
 			metadataTable.put("startKappa", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*kappa angle
 			poss += 4;
 			metadataTable.put("startPhi", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*phi angle
 			poss += 4;
 			metadataTable.put("startDelta", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*delta angle
 			poss += 4;
 			metadataTable.put("startGamma", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*gamma angle
 			poss += 4;
 			metadataTable.put("startXtalToDetector", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*distance in mm (dist in um)
 			poss += 4;
 			metadataTable.put("stop2theta", (getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0)); // 1000*two_theta angle
 			poss += 4;
 			metadataTable.put("stopOmega", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*omega angle
 			poss += 4;
 			metadataTable.put("stopChi", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*chi angle
 			poss += 4;
 			metadataTable.put("stopKappa", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*kappa angle
 			poss += 4;
 			metadataTable.put("stopPhi", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*phi angle
 			poss += 4;
 			metadataTable.put("stopDelta", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*delta angle
 
 			poss += 4;
 			metadataTable.put("stopGamma", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*gamma angle
 
 			poss += 4;
 			metadataTable.put("stopXtalToDetector", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*distance in mm (dist in  um)
 			poss += 4;
 			metadataTable.put("rotationAxis", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // active rotation axis
 			poss += 4;
 			metadataTable.put("rotationRange", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*rotation angle
 			poss += 4;
 			metadataTable.put("detectorRotateX", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*rotation of detector around X
 			poss += 4;
 			metadataTable.put("detectorRotateY", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*rotation of detector around Y
 			poss += 4;
 			metadataTable.put("detectorRotateZ", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]) / 1000.0); 
 			// 1000*rotation of detector around  Z
 			poss += 4;
 
 			poss += (32 - 28) * 4;
 			// ignore reserve 3 32-28*sizeof(uint32)
 
 			// Detector parameters (128)
 			metadataTable.put("detectorType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // detector type
 
 			poss += 4;
 			metadataTable.put("pixelSizeX", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // pixel size (nanometers)
 
 			poss += 4;
 			metadataTable.put("pixelSizeY", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // pixel size (nanometers)
 			poss += 4;
 			metadataTable.put("meanBias", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]) / 1000.0); // 1000*mean bias value
 
 			poss += 4;
 			metadataTable.put("photonPer100ADU", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// photons/100 ADUs
 			poss += 4;
 
 			int[] measuredBias = new int[MAX_IMAGES];
 			for (int i = 0; i < MAX_IMAGES; i++) {
 				measuredBias[i] = getHeaderInt(hbd[poss],
 						hbd[poss + 1], hbd[poss + 2],
 						hbd[poss + 3]); // 1000*mean bias value for
 													// each image
 				poss += 4;
 			}
 			metadataTable.put("measuredBias", measuredBias);
 
 			int[] measuredTemperature = new int[MAX_IMAGES];
 			for (int i = 0; i < MAX_IMAGES; i++) {
 				measuredTemperature[i] = getHeaderInt(hbd[poss],
 						hbd[poss + 1], hbd[poss + 2],
 						hbd[poss + 3]); // Temperature of each detector
 													// in milliKelvins
 				poss += 4;
 			}
 			metadataTable.put("measuredTemperature", measuredTemperature);
 
 			int[] measuredPressure = new int[MAX_IMAGES];
 			for (int i = 0; i < MAX_IMAGES; i++) {
 				measuredPressure[i] = getHeaderInt(hbd[poss],
 						hbd[poss + 1], hbd[poss + 2],
 						hbd[poss + 3]);// Pressure of each chamber in
 												// microTorr
 				poss += 4;
 			}
 
 			metadataTable.put("measuredPressure", measuredPressure);
 
 			// currentOffset += 32 - 5 + 3 * MAX_IMAGES * 4; // 32-5+3*MAX_IMAGES*sizeof(int23)
 			// reserve 4 retired to make room for measured pressure and measured temperature
 
 			// X-ray source and optics parameters (128)
 			// X-ray source parameters 
 			metadataTable.put("sourceType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (code) - target, synch. etc
 			poss += 4;
 			metadataTable.put("sourceDx", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3]));// Optics param. - (size microns)
 			poss += 4;
 			metadataTable.put("sourceDy", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // Optics param. - (size microns)
 			poss += 4;
 			metadataTable.put("sourceWavelength", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]));
 			// wavelength (femtoMeters)
 			poss += 4;
 			metadataTable.put("sourcePower", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (Watts)
 			poss += 4;
 			metadataTable.put("sourceVoltage", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (Volts)
 			poss += 4;
 			metadataTable.put("sourceCurrent", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (microAmps)
 			poss += 4;
 			metadataTable.put("sourceBias", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // (Volts)
 			poss += 4;
 			metadataTable.put("sourcePolarizationX", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); // ()
 			poss += 4;
 			metadataTable.put("sourcePolarizationY", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]));// ()
 			poss += 4;
 
 			poss += 16; // ignore reserve_source 4*sizeof(int32)
 			
 			//X-ray optics parameters
 			metadataTable.put("opticsType", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // Optics type (code)
 			poss += 4;
 			metadataTable.put("opticsDx", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // Optics param. - (size microns)
 			poss += 4;
 			metadataTable.put("opticsDy", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // Optics param. - (size microns)
 			poss += 4;
 			metadataTable.put("opticsWavelength", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3]));
 			// Optics param. - (size microns)
 			poss += 4;
 			metadataTable.put("opticsDispersion", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// Optics param. - (*10E6)
 			poss += 4;
 			metadataTable.put("opticsCrossfireX", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// Optics param. - (microRadians)
 			poss += 4;
 			metadataTable.put("opticsCrossfireY", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); 
 			// Optics param. - (microRadians)
 			poss += 4;
 			metadataTable.put("opticsAngle", getHeaderInt(hbd[poss],hbd[poss + 1], hbd[poss + 2],hbd[poss + 3])); // Optics param. - (microRadians)
 
 			poss += 4;
 			metadataTable.put("opticsPolarizationX", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); // ()
 			poss += 4;
 			metadataTable.put("opticsPolarizationY", getHeaderInt(hbd[poss], hbd[poss + 1],hbd[poss + 2], hbd[poss + 3])); // ()
 			poss += 4;
 
 			poss += 16; // 4*sizeof(int32) reserve_optics
 			poss += 16; // 32-28*sizeof(int32) reserve5
 
 			
 			// File parameters 1024 bytes
 
 			b = new byte[128]; // filetitle
 			System.arraycopy(hbd, poss, b, 0, 128);
 			metadataTable.put("filetitle", new String(b, "US-ASCII")); // Title
 			poss += 128;
 
 			b = new byte[128]; // filepath
 			System.arraycopy(hbd, poss, b, 0, 128);
 			metadataTable.put("filepath", new String(b, "US-ASCII")); 
 			// path  name  for  data  file
 			poss += 128;
 
 			b = new byte[64]; // filename
 			System.arraycopy(hbd, poss, b, 0, 64);
 			metadataTable.put("filename", new String(b, "US-ASCII")); 
 			// name of data file
 			poss += 64;
 			b = new byte[32]; // acquire timestamp
 			System.arraycopy(hbd, poss, b, 0, 32);
 			metadataTable.put("AcquireTimestamp", new String(b, "US-ASCII")); 
 			// date and time of acquisition
 			poss += 32;
 
 			b = new byte[32]; // header timestamp
 			System.arraycopy(hbd, poss, b, 0, 32);
 			metadataTable.put("headerTimestamp", new String(b, "US-ASCII")); 
 			// date and time of header update
 			poss += 32;
 
 			b = new byte[512]; // fileComment
 			System.arraycopy(hbd, poss, b, 0, 512);
 			metadataTable.put("fileComment", new String(b, "US-ASCII"));
 			// date and time file saved
 			poss += 512;
 
 			poss += 96; // 1024-(128+128+64+3*32+512) // reserve 6
 
 			b = new byte[512]; // datasetComments
 			System.arraycopy(hbd, poss, b, 0, 512);
 			metadataTable.put("datasetComments", new String(b, "US-ASCII")); 
 			// comments - can be used as desired
 			poss += 512;
 
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem loading MAR metadata", e);
 		}
 
 		return createGDAMetadata();
 	}
 
 	private int getHeaderInt(int pos1, int pos2, int pos3, int pos4) {
 		int headerInt;
 		if (littleEndian)
 			headerInt = Utils.leInt(pos1, pos2, pos3, pos4);
 		else
 			headerInt = Utils.beInt(pos1, pos2, pos3, pos4);
 		return headerInt;
 	}
 
 	private Map<String, Serializable> createGDAMetadata() throws ScanFileHolderException {
 		Map<String, Serializable> GDAMetadata = new HashMap<String, Serializable>();
 
 		try {
 
 			double pixelSizeX = ((Integer) getMetadataValue("pixelSizeX")).intValue() / 1.0e6;
 			int imageWidth = Integer.parseInt((String) getMetadataValue("ImageWidth"));
 			double beamX = ((Double) getMetadataValue("beamX")).doubleValue();
 
 			double pixelSizeY = (((Integer) getMetadataValue("pixelSizeY")).intValue() / 1.0e6);
 			int imageLength = Integer.parseInt((String) getMetadataValue("ImageLength"));
 			double beamY = ((Double) getMetadataValue("beamY")).doubleValue();
 
 			double distance = ((Double) getMetadataValue("xtalToDetector")).doubleValue();
 			// NXGeometery:NXtranslation
			double[] detectorOrigin = { (imageWidth - beamX) * pixelSizeX, (imageLength - beamY) * pixelSizeY, distance };
 
 			GDAMetadata.put("NXdetector:NXgeometery:NXtranslation", detectorOrigin);
 			GDAMetadata.put("NXdetector:NXgeometery:NXtranslation:NXunits", "metre");
 
 			// NXGeometery:NXOrientation
 			double detectorRotationX = ((Double) getMetadataValue("detectorRotateX")).doubleValue();
 			double detectorRotationY = ((Double) getMetadataValue("detectorRotateY")).doubleValue();
 			double detectorRotationZ = ((Double) getMetadataValue("detectorRotateZ")).doubleValue();
 
 			Matrix3d rotX = new Matrix3d();
 			rotX.rotX(detectorRotationX);
 			Matrix3d rotY = new Matrix3d();
 			rotY.rotY(detectorRotationY);
 			Matrix3d rotZ = new Matrix3d();
 			rotZ.rotZ(detectorRotationZ);
 
 			Matrix3d euler = new Matrix3d();
 			euler.mul(rotX, rotY);
 			euler.mul(rotZ);
 			double[] tmp = new double[3];
 			double[] tmp1 = new double[3];
 			double[] directionCosine = new double[6];
 			euler.getColumn(0, tmp);
 			euler.getColumn(1, tmp1);
 
 			for (int i = 0; i < directionCosine.length / 2; i++) {
 				directionCosine[i] = tmp[i];
 				directionCosine[3 + i] = tmp1[i];
 			}
 			GDAMetadata.put("NXdetector:NXgeometery:NXorientation", directionCosine);
 
 			// NXGeometery:XShape (shape from origin (+x, +y, +z,0, 0, 0) > x,y,0,0,0,0)
			double[] detectorShape = { (imageWidth - beamX) * pixelSizeX, (imageLength - beamY) * pixelSizeY, 0, 0, 0, 0 };
 			GDAMetadata.put("NXdetector:NXgeometery:NXshape", detectorShape);
 			GDAMetadata.put("NXdetector:NXgeometery:NXshape:NXshape", "milli*metre");
 
 			// NXGeometery:NXFloat
 			GDAMetadata.put("NXdetector:x_pixel_size", pixelSizeX);
 			GDAMetadata.put("NXdetector:x_pixel_size:NXunits", "milli*metre");
 			GDAMetadata.put("NXdetector:y_pixel_size", pixelSizeY);
 			GDAMetadata.put("NXdetector:y_pixel_size:NXunits", "milli*metre");
 			
 			// "NXmonochromator:wavelength"
 			double lambda = ((Integer) getMetadataValue("sourceWavelength")).intValue() / 100000.0;
 			GDAMetadata.put("NXmonochromator:wavelength", lambda);
 			GDAMetadata.put("NXmonochromator:wavelength:NXunits", "Angstrom");
 
 		
 			// oscillation range
 			double startOmega =  Double.parseDouble( getMetadataValue("startOmega").toString());
 			double rangeOmega = startOmega - Double.parseDouble(getMetadataValue("stopOmega").toString());
 	    	GDAMetadata.put("NXSample:rotation_start",startOmega);
 			GDAMetadata.put("NXSample:rotation_start:NXUnits","degree");
 			GDAMetadata.put("NXSample:rotation_range",rangeOmega);
 			GDAMetadata.put("NXSample:rotation_range:NXUnits", "degree");
 			
 			//Exposure time
 			GDAMetadata.put("NXSample:exposure_time", getMetadataValue("exposureTime"));
 			GDAMetadata.put("NXSample:exposure_time:NXUnits", "seconds");
 			createMetadata(detectorOrigin, imageLength, imageWidth, pixelSizeX, pixelSizeY, euler, lambda, startOmega, rangeOmega);
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing numerical value from string", e);
 		} catch (ScanFileHolderException e) {
 			throw new ScanFileHolderException("A problem occoured parsing the internal metatdata into the GDA metadata", e);
 		}
 		return GDAMetadata;
 	}
 
 	private void createMetadata(double[] detectorOrigin, int imageLength, int imageWidth, double pixelSizeX, double pixelSizeY, Matrix3d euler, double lambda, double startOmega, double rangeOmega) throws ScanFileHolderException {
 		DetectorProperties detProps = new DetectorProperties(new Vector3d(detectorOrigin), imageLength, imageWidth, pixelSizeX, pixelSizeY, euler);
 		DiffractionCrystalEnvironment diffEnv = new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, (Double) getMetadataValue("exposureTime"));
 
 		diffMetadata = new DiffractionMetadata(fileName, detProps, diffEnv);
 		diffMetadata.setMetadata(createStringMap());
 	}
 
 	private Serializable getMetadataValue(String key) throws ScanFileHolderException {
 		try {
 			Serializable value = metadataTable.get(key);
 			return value;
 		} catch (Exception e) {
 			throw new ScanFileHolderException("The keyword " + key + " was not found in the MAR header", e);
 		}
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		boolean tmp = loadMetadata;
 		try {
 			loadMetadata = true;
 			loadFile(); // TODO Implement a method for loading meta which does not load the image
 		} finally {
 			loadMetadata = tmp;
 		}
 	}
 
 	private Map<String, String> createStringMap() {
 		Map<String, String> ret = new HashMap<String,String>(7);
 		for (String key : metadataTable.keySet()) {
 			ret.put(key, metadataTable.get(key).toString().trim());
 		}
 		return ret;
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return diffMetadata;
 	}
 	
 	@Override
 	public IMetaData getMetaData(AbstractDataset data) {
 		if (metadata == null) {
 			if (data!=null) return data.getMetadata(); 
 			return null;
 		}
 		return getMetaData();
 	}
 }
