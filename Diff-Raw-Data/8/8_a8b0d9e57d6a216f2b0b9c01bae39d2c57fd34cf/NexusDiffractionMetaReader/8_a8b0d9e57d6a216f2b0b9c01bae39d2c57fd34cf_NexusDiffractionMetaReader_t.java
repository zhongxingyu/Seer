 package org.dawnsci.persistence.metadata.diffraction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.nexus.IFindInNexus;
 import org.dawb.hdf5.nexus.NexusFindDatasetByName;
 import org.dawb.hdf5.nexus.NexusFindGroupByAttributeText;
 import org.dawb.hdf5.nexus.NexusUtils;
 
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 
 public class NexusDiffractionMetaReader {
 	
 	public enum DiffractionMetaValue {
 		PIXEL_SIZE,PIXEL_NUMBER,BEAM_CENTRE,DISTANCE,DETECTOR_ORIENTATION,ENERGY,PHI,EXPOSURE,BEAM_VECTOR
 	};
 	
 	private String filePath;
 	
 	private Map<DiffractionMetaValue,Boolean> successMap = new HashMap<DiffractionMetaValue,Boolean>();
 	
 	public NexusDiffractionMetaReader(String filePath) {
 		this.filePath = filePath;
 		for (DiffractionMetaValue val : DiffractionMetaValue.values()) {
 			successMap.put(val, false);
 		}
 	}
 
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize. Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize) {
 		return getDiffractionMetadataFromNexus(imageSize, null, null, null);
 	}
 	
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize. Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 * @param detprop. Detector properties object to be populated from the nexus file
 	 * @param diffcrys. Detector properties object to be populated from the nexus file
 	 * @param xyPixelSize. Guess at pixel size. Will be used if pixel size not read.
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize,DetectorProperties detprop, DiffractionCrystalEnvironment diffcrys) {
 		return getDiffractionMetadataFromNexus(imageSize, detprop, diffcrys, null);
 	}
 	
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize. Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 * @param detprop. Detector properties object to be populated from the nexus file
 	 * @param diffcrys. Detector properties object to be populated from the nexus file
 	 * @param xyPixelSize. Guess at pixel size. Will be used if pixel size not read.
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize,DetectorProperties detprop, DiffractionCrystalEnvironment diffcrys, double[] xyPixelSize) {
 		if (!HierarchicalDataFactory.isHDF5(filePath)) return null;
 		
 		if (detprop == null) detprop = getInitialDetectorProperties();
 		if (diffcrys == null) diffcrys = getInitialCrystalEnvironment();
 		
 		IHierarchicalDataFile hiFile = null;
 		
 		try {
 			hiFile = HierarchicalDataFactory.getReader(filePath);
 
 			Group rootGroup = hiFile.getRoot();
 
 			//Check only one entry in root - might not act on it at the moment but may be useful to know
 			if (rootGroup.getMemberList().size() > 1)
 				;//logger.warn("More than one root node in file, metadata may be incorrect");
 
 			Group nxInstrument = getNXGroup(rootGroup, "NXinstrument");
 
 			Group nxDetector = null;
 
 			if (nxInstrument != null) {
 				Group nxMono = getNXGroup(nxInstrument, "NXmonochromator");
 				if (nxMono != null) successMap.put(DiffractionMetaValue.ENERGY,updateEnergy(nxMono,diffcrys));
 				nxDetector = findBestNXDetector(nxInstrument, imageSize);
 				//if there is an instrument but no detector is may be a result node so look in here
 				if (nxDetector == null) nxDetector = nxInstrument;
 			}
 
 			//if no detectors with pixel in search the entire nxInstrument group
 			if (nxDetector == null) {
 				nxDetector = getNXGroup(rootGroup, "NXdetector");
 			}
 			
 			if (nxDetector != null) {
 				//populate the crystal environment
 				populateFromNexus(nxDetector, diffcrys);
 				//populate detector properties
 				populateFromNexus(nxDetector, detprop, imageSize);
 			}
 
 			} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (hiFile!= null)
 				try {
 					hiFile.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		}
 		
 		if (!successMap.get(DiffractionMetaValue.PIXEL_SIZE) && xyPixelSize != null) {
 			detprop.setHPxSize(xyPixelSize[0]);
 			detprop.setHPxSize(xyPixelSize[1]);
 		}
 		
 		
 		return new DiffractionMetadata(filePath,detprop,diffcrys);
 	}
 	
 	/**
 	 * Have complete DetectorProperties and DiffractionCrystalEnvironment values been read
 	 */
 	public boolean isCompleteRead() {
 		return !successMap.containsValue(false);
 	}
 	
 	/**
	 * Have enough values to perform downstream calculations been read (ie exposure time not read)
 	 */
 	public boolean isPartialRead() {
 		return successMap.get(DiffractionMetaValue.BEAM_CENTRE) &&
 			   successMap.get(DiffractionMetaValue.DETECTOR_ORIENTATION) &&
 			   successMap.get(DiffractionMetaValue.DISTANCE) &&
 			   successMap.get(DiffractionMetaValue.ENERGY) &&
 			   successMap.get(DiffractionMetaValue.PIXEL_NUMBER) &&
 			   successMap.get(DiffractionMetaValue.PIXEL_SIZE) &&
 			   successMap.get(DiffractionMetaValue.BEAM_VECTOR);
 	}
 	
 	/**
 	 * Were any values read from the Nexus file
 	 */
 	public boolean anyValuesRead() {
 		return successMap.containsValue(true);
 	}
 	
 	private Group findBestNXDetector(Group nxInstrument, int[] imageSize) {
 		//Find nxDetectors in instrument
 		// TODO should probably change to find data then locate correct
 		// detector from image size
		List<Group> nxDetectors = findNXDetectors(nxInstrument, "pixel");
 
 		if (nxDetectors.size() == 1 || imageSize == null) {
 			//only one nxdetector or we dont know the image size
 			//so just use the first one
 			return nxDetectors.get(0);
 		} else if (nxDetectors.size() > 1 && imageSize != null) {
 			for (Group detector : nxDetectors) {
 				H5ScalarDS dataset = getDataset(detector, "data");
 				long[] dataShape = dataset.getDims();
 				boolean matchesX = false;
 				boolean matchesY = false;
 				for (long val : dataShape) {
 					if (val == imageSize[0]) matchesX = true;
 					else if (val == imageSize[1]) matchesY = true;
 				}
 				if (matchesX & matchesY) {
 					return detector;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private Group getNXGroup (Group rootGroup, String nxAttribute) {
 		//Find NXinstrument (hopefully there is only one!)
 		NexusFindGroupByAttributeText finder = new NexusFindGroupByAttributeText(nxAttribute,NexusUtils.NXCLASS);
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(finder, rootGroup, true);
 		if (hOb.isEmpty() || !(hOb.get(0) instanceof Group)) return null;
 		return (Group)hOb.get(0);
 	}
 	
 	
 	private void populateFromNexus(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		successMap.put(DiffractionMetaValue.EXPOSURE, updateExposureTime(nexusGroup,diffcrys));
 		
 		//Energy might not have been in NXmonochromator, if not look here
 		if (!successMap.get(DiffractionMetaValue.ENERGY)) {
 			successMap.put(DiffractionMetaValue.ENERGY,updateEnergy(nexusGroup, diffcrys));
 		}
 		
 		boolean phi = updatePhiRange(nexusGroup, diffcrys) && updatePhiStart(nexusGroup, diffcrys);
 		successMap.put(DiffractionMetaValue.PHI, phi);
 	}
 	
 	private boolean updateEnergy(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "energy");
 		if (dataset == null) getDataset(nexusGroup, "wavelength");
 		if (dataset == null) return false;
 		double[] energyValues = getDoubleData(dataset);
 		if (energyValues == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("keV")) {
 			diffcrys.setWavelengthFromEnergykeV(energyValues[0]);
 			return true;
 			}
 		if (units.contains("Angstrom")) {
 			diffcrys.setWavelength(energyValues[0]);
 			return true;
 			}
 		return false;
 	}
 		
 	private boolean updatePhiStart(Group nexusGroup, DiffractionCrystalEnvironment diffcry) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "phi_start");
 		if (dataset == null) return false;
 		double[] value = getDoubleData(dataset);
 		if (value == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("degrees")) {
 			diffcry.setPhiStart(value[0]);
 			return true;
 			}
 		return false;
 	}
 	
 	private boolean updatePhiRange(Group nexusGroup, DiffractionCrystalEnvironment diffcry) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "phi_range");
 		if (dataset == null) return false;
 		double[] value = getDoubleData(dataset);
 		if (value == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("degrees")) {
 			diffcry.setPhiRange(value[0]);
 			return true;
 			}
 		return false;
 	}
 	
 	private boolean updateExposureTime(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "count_time");
 		if (dataset == null) dataset = getDataset(nexusGroup, "exposure");
 		if (dataset == null) return false;
 		double[] values = getFloatOrDoubleArray(dataset);
 		if (values == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("s")) {
 			diffcrys.setExposureTime(values[0]);
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean updateBeamCentre(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "beam centre");
 		if (dataset == null) dataset = getDataset(nexusGroup, "beam_centre");
 		if (dataset == null) return false;
 		double[] values = getDoubleData(dataset);
 		if (values == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("pixels")) {
 			detprop.setBeamCentreCoords(values);
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean updateDetectorDistance(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "camera length");
 		if (dataset == null) dataset = getDataset(nexusGroup, "distance");
 		if (dataset == null) return false;
 		double[] values = getDoubleData(dataset);
 		if (values == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("mm")) {
 			detprop.setBeamCentreDistance(values[0]);
 			return true;
 		}
 		return false;
 	}
 	
 	
 	private void populateFromNexus(Group nxDetector, DetectorProperties detprop, int[] shape) {
 		
 		successMap.put(DiffractionMetaValue.PIXEL_SIZE,updatePixelSize(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.BEAM_CENTRE, updateBeamCentre(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.DISTANCE, updateDetectorDistance(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.DETECTOR_ORIENTATION, updateDetectorOrientation(nxDetector, detprop));
 		successMap.put(DiffractionMetaValue.BEAM_VECTOR, updateBeamVector(nxDetector, detprop));
 		successMap.put(DiffractionMetaValue.PIXEL_NUMBER, updatePixelNumber(nxDetector, detprop));
 
 	}
 	
 	private boolean updateDetectorOrientation(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "detector_orientation");
 		if (dataset == null) return false;
 		double[] orArray = getDoubleData(dataset);
 		if (orArray == null) return false;
 		if (orArray.length != 9) return false;
 		detprop.setOrientation(new Matrix3d(orArray));
 		return true;
 		// TODO if file only contains pitch/roll/yaw find these then
 		// detprop.setNormalAnglesInDegrees(yaw, pitch, roll);
 	}
 	
 	private boolean updateBeamVector(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "beam_vector");
 		if (dataset == null) return false;
 		double[] vecArray = getDoubleData(dataset);
 		if (vecArray == null) return false;
 		if (vecArray.length != 3) return false;
 		detprop.setBeamVector(new Vector3d(vecArray));
 		return true;
 	}
 	
 	private boolean updatePixelNumber(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_number");
 		if (dataset == null) dataset = getDataset(nexusGroup, "distance");
 		if (dataset == null) return false;
 		int[] valuex = getIntData(dataset);
 		if (valuex == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		if (units.equals("pixels")) {
 			detprop.setPx(valuex[0]);
 		}
 		
 		dataset = getDataset(nexusGroup, "y_pixel_number");
 		if (dataset == null) dataset = getDataset(nexusGroup, "distance");
 		if (dataset == null) return false;
 		int[] valuey = getIntData(dataset);
 		if (valuey == null) return false;
 		String unitsy = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (unitsy == null) return false;
 		if (unitsy.equals("pixels")) {
 			detprop.setPy(valuey[0]);
 			return true;
 		}
 		return false;
 	}
 	
 	
 	private boolean updatePixelSize(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_size");
 		if (dataset == null) return false;
 		double[] xPx = getDoubleData(dataset);
 		if (xPx == null) return false;
 		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		if (units == null) return false;
 		
 		if (units.equals("mm")) {detprop.setVPxSize(xPx[0]);}
 		else if (units.equals("m")) {detprop.setVPxSize(xPx[0]*1000);}
 		
 		dataset = getDataset(nexusGroup, "y_pixel_size");
 		if (dataset == null) return false;
 		String unitsy = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
 		double[] yPx = getDoubleData(dataset);
 		if (yPx == null) return false;
 		if (unitsy == null) return false;
 		if (unitsy.equals("mm")) {
 			detprop.setHPxSize(xPx[0]);
 			return true;
 		} else if (unitsy.equals("m")) {
 			detprop.setHPxSize(xPx[0]*1000);
 			return true;
 		}
 		return false;
 		
 	}
 	
 	private H5ScalarDS getDataset(Group group, String name) {
 		NexusFindDatasetByName dataFinder = new NexusFindDatasetByName(name);
 		List<HObject>  hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder, group, true);
 		hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder,group, false);
 		if (hOb.isEmpty() || !(hOb.get(0) instanceof H5ScalarDS)) { return null;}
 		H5ScalarDS h5data = (H5ScalarDS)hOb.get(0);
 		
 		return h5data;
 	}
 	
 	private double[] getFloatOrDoubleArray(H5ScalarDS dataset) {
 		//should be single value or vector
 		if (dataset.getRank() > 2) { return null;}
 		try {
 			return (double[]) dataset.getData();
 		} catch (ClassCastException e) {
 			try {
 				float[] fData = (float[]) dataset.getData();
 				double[] dData = new double[fData.length];
 				
 				for (int i = 0 ; i<fData.length; i++) {
 					dData[i] = fData[i];
 				}
 				return dData;
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		} catch (OutOfMemoryError e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private int[] getIntData(H5ScalarDS dataset) {
 		//should be single value or vector
 		if (dataset.getRank() > 2) { return null;}
 		try {
 			return (int[]) dataset.getData();
 		} catch (OutOfMemoryError e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private double[] getDoubleData(H5ScalarDS dataset) {
 		//should be single value or vector
 		if (dataset.getRank() > 2) { return null;}
 		try {
 			return (double[]) dataset.getData();
 		} catch (OutOfMemoryError e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
	private List<Group> findNXDetectors(Group nxInstrument, String childNameContains) {
 		final String groupText = "NXdetector";
 		final String childText = childNameContains;;
 		
 		IFindInNexus findWithChild = new IFindInNexus() {
 			
 			@Override
 			public boolean inNexus(HObject nexusObject) {
 				if(nexusObject instanceof Group) {
 					if (NexusUtils.getNexusGroupAttributeValue((Group)nexusObject,NexusUtils.NXCLASS).toLowerCase().equals(groupText.toLowerCase())) {
 						for (Object ob: ((Group)nexusObject).getMemberList()) {
 							if(ob instanceof H5ScalarDS) {
 								if (((H5ScalarDS)ob).getName().toLowerCase().contains((childText.toLowerCase()))) {
 									return true;
 								}
 							}
 						}
 					}
 				}
 				return false;
 			}
 		};
 		
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(findWithChild, nxInstrument, false);
 		
 		List<Group> detectorGroups = new ArrayList<Group>(hOb.size());
 		
 		for (HObject ob : hOb) {
 			if (ob instanceof Group) {
 				detectorGroups.add((Group)ob);
 			}
 		}
 		return detectorGroups;
 	}
 	
 	private DetectorProperties getInitialDetectorProperties() {
 		//Try to return harmless but not physical values
 		Matrix3d identityMatrix = new Matrix3d();
 		identityMatrix.setIdentity();
 		return new DetectorProperties(new Vector3d(1,1,1), 1,
 				1, 1, 1, identityMatrix);
 	}
 	
 	private DiffractionCrystalEnvironment getInitialCrystalEnvironment() {
 		//Try to return harmless but not physical values
 		return new DiffractionCrystalEnvironment(1, 0, 0, 1);
 	}
 
 }
