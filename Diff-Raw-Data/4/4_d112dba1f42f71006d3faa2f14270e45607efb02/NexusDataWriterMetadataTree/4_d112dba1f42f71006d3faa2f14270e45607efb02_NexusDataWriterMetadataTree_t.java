 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.data.scan.datawriter;
 
 import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
 import gda.device.Scannable;
 import gda.device.scannable.ScannableUtils;
 import gda.jython.JythonServerFacade;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.nexusformat.NexusException;
 import org.nexusformat.NexusFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class NexusDataWriterMetadataTree extends NexusDataWriter {
 
 	private static final Logger logger = LoggerFactory.getLogger(NexusDataWriterMetadataTree.class);
 
 	private static Set<String> metadatascannables = new HashSet<String>();
 
 	private static Map<String, ScannableWriter> locationmap = new HashMap<String, ScannableWriter>();
 
 	private boolean weKnowTheLocationFor(String scannableName) {
 		return locationmap.containsKey(scannableName);
 	}
 
 	/**
 	 * this is run when processing the first ScanDataPoint
 	 * the file is in the root node
 	 * we add all the one off metadata here
 	 */
 	@Override
 	protected void makeScannablesAndMonitors(Collection<Scannable> scannablesAndMonitors) {
 		Set<String> metadatascannablestowrite = new HashSet<String>(metadatascannables);
 		
 		try {
 			file.opengroup(this.entryName, "NXentry");
 
 			Set<Scannable> wehavewritten = new HashSet<Scannable>();
 			for (Iterator<Scannable> iterator = scannablesAndMonitors.iterator(); iterator.hasNext();) {
 				Scannable scannable = iterator.next();
 				String scannableName = scannable.getName();
 				if (weKnowTheLocationFor(scannableName)) {
 					wehavewritten.add(scannable);
 					Collection<String> prerequisites = locationmap.get(scannableName).getPrerequisiteScannableNames();
 					if (prerequisites != null)
 						metadatascannablestowrite.addAll(prerequisites);
 					scannableID.addAll(locationmap.get(scannableName).makeScannable(file, scannable, getSDPositionFor(scannableName), generateDataDim(false, scanDimensions, null)));
 				}
 			}
 			
 			int oldsize;
 			do { // add dependencies of metadata scannables
 				oldsize = metadatascannablestowrite.size();
 				Set<String> aux = new HashSet<String>();
 				for (String s: metadatascannablestowrite) {
 					if (weKnowTheLocationFor(s)) {
 						Collection<String> prerequisites = locationmap.get(s).getPrerequisiteScannableNames();
 						if (prerequisites != null)
 							aux.addAll(prerequisites);
 					}
 				}
 				metadatascannablestowrite.addAll(aux);
 			} while(metadatascannablestowrite.size() > oldsize);
 			
 			// remove the ones in the scan, as they are not metadata
 			for (Scannable scannable : scannablesAndMonitors) {
 				metadatascannablestowrite.remove(scannable.getName());
 			}
 			// only use default writing for the ones we haven't written yet 
 			scannablesAndMonitors.removeAll(wehavewritten);
 			
 			makeMetadataScannables(metadatascannablestowrite);
 			
 			// Close NXentry
 			file.closegroup();
 		} catch (NexusException e) {
 			// FIXME NexusDataWriter should allow exceptions to be thrown
 			logger.error("TODO put description of error here", e);
 		}
 		super.makeScannablesAndMonitors(scannablesAndMonitors);
 	}
 	
 	private Object getSDPositionFor(String scannableName) {
 		int index = thisPoint.getScannableNames().indexOf(scannableName);
 		return thisPoint.getPositions().get(index);
 	}
 	
 	private void makeMetadataScannableFallback(Scannable scannable, Object position) throws NexusException {
 		String[] inputNames = scannable.getInputNames();
 		String[] extraNames = scannable.getExtraNames();
 		// FIXME ideally this would work for non-doubles as well
 		// FIXME this needs to bring in the units
 		Double[] positions = ScannableUtils.objectToArray(position);
 
 		logger.debug("Writing data for scannable (" + scannable.getName() + ") to NeXus file.");
 
 		// Navigate to correct location in the file.
 		try {
 			try {
 				file.makegroup("start_metadata", "NXcollection");
 			} catch (Exception e) {
 				// ignored
 			}
 			file.opengroup("start_metadata", "NXcollection");
 			file.makegroup(scannable.getName(), "NXcollection");
 			file.opengroup(scannable.getName(), "NXcollection");
 
 			for (int i = 0; i < inputNames.length; i++) {
 				file.makedata(inputNames[i], NexusFile.NX_FLOAT64, 1, new int[] {1});
 				file.opendata(inputNames[i]);
 				file.putdata(new double[] {positions[i]});
 				file.closedata();
 			}
 			for (int i = 0; i < extraNames.length; i++) {
				file.makedata(extraNames[i], NexusFile.NX_FLOAT64, 1, new int[] {1});
				file.opendata(extraNames[i]);
 				file.putdata(new double[] {positions[i]});
 				file.closedata();
 			}
 		} finally {
 			// close NXpositioner
 			file.closegroup();
 			// close NXcollection
 			file.closegroup();
 		}
 	}
 	
 	private void makeMetadataScannables(Set<String> metadatascannablestowrite) throws NexusException {
 		for(String scannableName: metadatascannablestowrite) {
 			try {
 				Scannable scannable = (Scannable) JythonServerFacade.getInstance().getFromJythonNamespace(scannableName);
 				Object position = scannable.getPosition();
 				if (weKnowTheLocationFor(scannableName)) {
 					locationmap.get(scannableName).makeScannable(file, scannable, position, new int[] {1});
 				} else {
 					makeMetadataScannableFallback(scannable, position);
 					// put in default location (NXcollection with name metadata)
 				}
 			} catch (NexusException e) {
 				throw e;
 			} catch (Exception e) {
 				logger.error("error getting "+scannableName+" from namespace or reading position from it.", e);
 			} 
 		}
 	}
 	
 	@Override
 	protected void writeScannable(Scannable scannable) throws NexusException {
 		if (!weKnowTheLocationFor(scannable.getName())) {
 			super.writeScannable(scannable);
 		} else {
 			file.opengroup(this.entryName, "NXentry");
 			locationmap.get(scannable.getName()).writeScannable(file, scannable, getSDPositionFor(scannable.getName()), generateDataStartPos(dataStartPosPrefix, null));
 			file.closegroup();
 		}
 	}
 
 	public static Set<String> getMetadatascannables() {
 		return metadatascannables;
 	}
 
 	public static void setMetadatascannables(Set<String> metadatascannables) {
 		if (metadatascannables == null)
 			NexusDataWriterMetadataTree.metadatascannables = new HashSet<String>();
 		else
 			NexusDataWriterMetadataTree.metadatascannables = metadatascannables;
 	}
 
 	public static Map<String, ScannableWriter> getLocationmap() {
 		return locationmap;
 	}
 
 	public static void setLocationmap(Map<String, ScannableWriter> locationmap) {
 		if (locationmap == null) 
 			NexusDataWriterMetadataTree.locationmap = new HashMap<String, ScannableWriter>();
 		else 
 			NexusDataWriterMetadataTree.locationmap = locationmap;
 	}
 }
