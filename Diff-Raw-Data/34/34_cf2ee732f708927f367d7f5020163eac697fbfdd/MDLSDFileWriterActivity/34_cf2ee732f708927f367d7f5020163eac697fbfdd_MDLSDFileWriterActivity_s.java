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
 package org.openscience.cdk.applications.taverna.io;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.sf.taverna.t2.reference.ExternalReferenceSPI;
 import net.sf.taverna.t2.reference.impl.external.file.FileReference;
 import net.sf.taverna.t2.reference.impl.external.object.InlineStringReference;
 
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.CMLChemFile;
 import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
 import org.openscience.cdk.applications.taverna.basicutilities.FileNameGenerator;
 import org.openscience.cdk.applications.taverna.basicutilities.Tools;
 import org.openscience.cdk.applications.taverna.interfaces.IIterativeFileWriter;
 import org.openscience.cdk.io.SDFWriter;
 
 /**
  * Class which represents the MDL SDFile writer activity.
  * 
  * @author Andreas Truzskowski
  * 
  */
 public class MDLSDFileWriterActivity extends AbstractCDKActivity implements IIterativeFileWriter {
 
 	public static final String SD_FILE_WRITER_ACTIVITY = "SDfile Writer";
 	private File file = null;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public MDLSDFileWriterActivity() {
 		this.INPUT_PORTS = new String[] { "Structures", "File" };
 		this.OUTPUT_PORTS = new String[] { "Files" };
 	}
 
 	@Override
 	protected void addInputPorts() {
 		addInput(this.INPUT_PORTS[0], 1, true, null, byte[].class);
 		List<Class<? extends ExternalReferenceSPI>> expectedReferences = new ArrayList<Class<? extends ExternalReferenceSPI>>();
 		expectedReferences.add(FileReference.class);
 		expectedReferences.add(InlineStringReference.class);
 		addInput(this.INPUT_PORTS[1], 0, false, expectedReferences, null);
 	}
 
 	@Override
 	protected void addOutputPorts() {
 		addOutput(this.OUTPUT_PORTS[0], 1);
 	}
 
 	@Override
 	public void work() throws Exception {
 		// Get input
 		List<CMLChemFile> chemFileList = this.getInputAsList(this.INPUT_PORTS[0], CMLChemFile.class);
 		File targetFile = this.getInputAsFile(this.INPUT_PORTS[1]);
 		String directory = Tools.getDirectory(targetFile);
 		String name = Tools.getFileName(targetFile);
 		String extension = (String) this.getConfiguration().getAdditionalProperty(
 				CDKTavernaConstants.PROPERTY_FILE_EXTENSION);
 		Boolean oneFilePerIteration = (Boolean) this.getConfiguration().getAdditionalProperty(
 				CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION);
 		if (oneFilePerIteration) {
 			this.file = FileNameGenerator.getNewFile(directory, extension, name, this.iteration);
 		} else {
 			if (this.file == null) {
 				this.file = FileNameGenerator.getNewFile(directory, extension, name);
 			}
 		}
 		// Do work
 		List<String> resultFiles = new ArrayList<String>();
 		try {
 			SDFWriter writer = new SDFWriter(new FileWriter(file, !oneFilePerIteration));
 			for (CMLChemFile cmlChemFile : chemFileList) {
 				writer.write(cmlChemFile);
 			}
 			writer.close();
 			resultFiles.add(this.file.getPath());
 		} catch (Exception e) {
 			ErrorLogger.getInstance().writeError(CDKTavernaException.WRITE_FILE_ERROR + file.getPath() + "!",
 					this.getActivityName(), e);
 		}
 		// Set output
 		this.setOutputAsStringList(resultFiles, this.OUTPUT_PORTS[0]);
 	}
 
 	@Override
 	public String getActivityName() {
 		return MDLSDFileWriterActivity.SD_FILE_WRITER_ACTIVITY;
 	}
 
 	@Override
 	public HashMap<String, Object> getAdditionalProperties() {
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		properties.put(CDKTavernaConstants.PROPERTY_ONE_FILE_PER_ITERATION, true);
 		properties.put(CDKTavernaConstants.PROPERTY_FILE_EXTENSION, ".sdf");
 		return properties;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Description: " + MDLSDFileWriterActivity.SD_FILE_WRITER_ACTIVITY;
 	}
 
 	@Override
 	public String getFolderName() {
 		return CDKTavernaConstants.IO_FOLDER_NAME;
 	}
 }
