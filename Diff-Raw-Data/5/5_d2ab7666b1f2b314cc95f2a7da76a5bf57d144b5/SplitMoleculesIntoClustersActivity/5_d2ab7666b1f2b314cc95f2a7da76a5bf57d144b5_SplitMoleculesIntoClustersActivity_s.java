 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.weka;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import net.sf.taverna.t2.invocation.InvocationContext;
 import net.sf.taverna.t2.reference.ReferenceService;
 import net.sf.taverna.t2.reference.T2Reference;
 import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
 
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.CMLChemFile;
 import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
 import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
 import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
 import org.openscience.cdk.applications.taverna.interfaces.IFileWriter;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.io.SDFWriter;
 import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
 
 /**
  * Class which represents the Split Molecules Into Clusters activity.
  * 
  * @author Andreas Truzskowski
  * 
  */
 public class SplitMoleculesIntoClustersActivity extends AbstractCDKActivity implements IFileWriter {
 
 	public static final String SPLIT_MOLECULES_INTO_CLUSTERS = "Split Molecules Into Clusters";
 	private HashMap<Integer, File> files = null;
 	private HashMap<UUID, Integer> uuidClusterMap = null;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public SplitMoleculesIntoClustersActivity() {
 		this.INPUT_PORTS = new String[] { "Structures", "UUID Cluster CSV" };
 	}
 
 	@Override
 	protected void addInputPorts() {
 		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
 		addInput(this.INPUT_PORTS[1], 1, true, null, String.class);
 	}
 
 	@Override
 	protected void addOutputPorts() {
 		// Nothing to add
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<String, T2Reference> work(final Map<String, T2Reference> inputs, AsynchronousActivityCallback callback)
 			throws CDKTavernaException {
 		InvocationContext context = callback.getContext();
 		ReferenceService referenceService = context.getReferenceService();
 		List<CMLChemFile> chemFiles = null;
 		List<byte[]> dataArray = (List<byte[]>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[0]), byte[].class,
 				context);
 		try {
 			chemFiles = CDKObjectHandler.getChemFileList(dataArray);
 		} catch (Exception e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.OUTPUT_PORT_CONFIGURATION_ERROR, this.getActivityName(), e);
 			throw new CDKTavernaException(this.getConfiguration().getActivityName(), e.getMessage());
 		}
 		if (chemFiles.isEmpty()) {
 			return null;
 		}
 		List<String> csv = null;
 		try {
 			csv = (List<String>) referenceService.renderIdentifier(inputs.get(this.INPUT_PORTS[1]), String.class, context);
 		} catch (Exception e) {
 			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRONG_INPUT_PORT_TYPE);
 		}
 		File directory = (File) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE);
 		if (directory == null || !directory.exists()) {
 			throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.NO_OUTPUT_DIRECTORY_CHOSEN);
 		}
 		String extension = (String) this.getConfiguration().getAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
 		if (this.files == null) {
 			this.files = new HashMap<Integer, File>();
 		}
 		// Create cluster uuid map
 		if (this.uuidClusterMap == null) {
 			this.uuidClusterMap = new HashMap<UUID, Integer>();
 			for (int i = 1; i < csv.size(); i++) {
 				String[] values = csv.get(i).split(";");
 				uuidClusterMap.put(UUID.fromString(values[0]), Integer.parseInt(values[1]));
 			}
 		}
 		// Write SDFiles
 		for (CMLChemFile chemFile : chemFiles) {
 			List<IAtomContainer> containers = ChemFileManipulator.getAllAtomContainers(chemFile);
 			for (IAtomContainer container : containers) {
 				File file = null;
 				try {
 					if (container.getProperty(CDKTavernaConstants.MOLECULEID) == null) {
 						ErrorLogger.getInstance().writeError(CDKTavernaException.MOLECULE_NOT_TAGGED_WITH_UUID,
 								this.getActivityName());
 						continue;
 					}
 					UUID uuid = UUID.fromString((String) container.getProperty(CDKTavernaConstants.MOLECULEID));
 					Integer cluster = this.uuidClusterMap.get(uuid);
 					if (cluster == null) {
 						ErrorLogger.getInstance().writeError(CDKTavernaException.NO_CLUSTER_INFORMATION_AVAILABLE,
 								this.getActivityName());
 					}
 					file = this.files.get(cluster);
 					if (file == null) {
 						file = FileNameGenerator.getNewFile(directory.getPath(), extension, "Cluster_" + cluster);
 						this.files.put(cluster, file);
 					}
 					SDFWriter writer = new SDFWriter(new FileWriter(file, true));
 					writer.write(container);
 					writer.close();
 				} catch (Exception e) {
 					ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!",
 							this.getActivityName(), e);
 					throw new CDKTavernaException(this.getActivityName(), CDKTavernaException.WRITE_FILE_ERROR + file.getPath()
 							+ "!");
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getActivityName() {
 		return SplitMoleculesIntoClustersActivity.SPLIT_MOLECULES_INTO_CLUSTERS;
 	}
 
 	@Override
 	public HashMap<String, Object> getAdditionalProperties() {
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".sdf");
 		return properties;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Description: " + SplitMoleculesIntoClustersActivity.SPLIT_MOLECULES_INTO_CLUSTERS;
 	}
 
 	@Override
 	public String getFolderName() {
		return CDKTavernaConstants.IO_FOLDER_NAME;
 	}
 }
