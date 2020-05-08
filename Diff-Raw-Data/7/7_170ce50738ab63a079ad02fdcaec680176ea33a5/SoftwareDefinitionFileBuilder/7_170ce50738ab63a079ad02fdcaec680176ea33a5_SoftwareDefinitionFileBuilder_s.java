 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 12, 2012
  */
 package edu.cmu.sv.arinc838.builder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.arinc.arinc838.FileDefinition;
 import com.arinc.arinc838.SdfFile;
 import com.arinc.arinc838.ThwDefinition;
 
 import edu.cmu.sv.arinc838.binary.BdfFile;
 import edu.cmu.sv.arinc838.util.Converter;
 import edu.cmu.sv.arinc838.validation.DataValidator;
 
 public class SoftwareDefinitionFileBuilder implements Builder<SdfFile> {
 
 	/**
 	 * The default file format version as defined in the spec. Value is {@value}
 	 */
 	public static final byte[] DEFAULT_FILE_FORMAT_VERSION = Converter
 			.hexToBytes("00008100");
 
 	private List<FileDefinitionBuilder> fileDefinitions = new ArrayList<FileDefinitionBuilder>();
 	private List<TargetHardwareDefinitionBuilder> thwDefinitions = new ArrayList<TargetHardwareDefinitionBuilder>();
 	private SoftwareDescriptionBuilder softwareDescription;
 	private IntegrityDefinitionBuilder lspIntegrityDefinition;
 	private IntegrityDefinitionBuilder sdfIntegrityDefinition;
 
 	public SoftwareDefinitionFileBuilder() {
 		;
 	}
 
 	public SoftwareDefinitionFileBuilder(SdfFile swDefFile) {
 		this.initialize(swDefFile);
 	}
 
 	public SoftwareDefinitionFileBuilder(BdfFile bdfFile) throws IOException {
 		this.initialize(bdfFile);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void initialize(SdfFile swDefFile) {
 		DataValidator.validateFileFormatVersion(swDefFile
 				.getFileFormatVersion());
 		List<FileDefinition> fileDefs = (List<FileDefinition>) DataValidator
 				.validateList1(swDefFile.getFileDefinitions());
 
 		for (FileDefinition fileDef : fileDefs) {
 			fileDefinitions.add(new FileDefinitionBuilder(fileDef));
 		}
 
 		for (ThwDefinition thwDef : swDefFile.getThwDefinitions()) {
 			thwDefinitions.add(new TargetHardwareDefinitionBuilder(thwDef));
 		}
 
 		softwareDescription = new SoftwareDescriptionBuilder(
 				swDefFile.getSoftwareDescription());
 		lspIntegrityDefinition = new IntegrityDefinitionBuilder(
 				swDefFile.getLspIntegrityDefinition());
 		sdfIntegrityDefinition = new IntegrityDefinitionBuilder(
 				swDefFile.getSdfIntegrityDefinition());
 	}
 
 	public void initialize(BdfFile file) throws IOException {
 
 		file.seek(BdfFile.FILE_FORMAT_VERSION_LOCATION);
 		byte[] fileFormatVersion = file.readHexbin32();
 		if (!Arrays.equals(fileFormatVersion,
 				SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION)) {
 			throw new IllegalArgumentException(
 					"File format not recognized. Expected: "
 							+ SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION
 							+ " Got: " + fileFormatVersion);
 		}
 
 		file.seek(file.readSoftwareDescriptionPointer());
 		softwareDescription = new SoftwareDescriptionBuilder(file);
 
 		file.seek(file.readTargetDefinitionsPointer());
 		long targetHardwareCount = file.readUint32();
 		for (int i = 0; i < targetHardwareCount; i++) {
 			long nextHardware = file.readUint32();
 			TargetHardwareDefinitionBuilder hardware = new TargetHardwareDefinitionBuilder(
 					file);
 			this.getTargetHardwareDefinitions().add(hardware);
 			file.seek(nextHardware);
 		}
 
 		file.seek(file.readFileDefinitionsPointer());
 		long fileDefinitionCount = file.readUint32();
 		for (int i = 0; i < fileDefinitionCount; i++) {
 			long nextFile = file.readUint32();
 			FileDefinitionBuilder fileDefinition = new FileDefinitionBuilder(
 					file);
 			this.getFileDefinitions().add(fileDefinition);
 			file.seek(nextFile);
 		}
 
 		file.seek(file.readSdfIntegrityDefinitionPointer());
 		this.setSdfIntegrityDefinition(new IntegrityDefinitionBuilder(file));
 
 		file.seek(file.readLspIntegrityDefinitionPointer());
 		this.setLspIntegrityDefinition(new IntegrityDefinitionBuilder(file));
 	}
 
 	public byte[] getFileFormatVersion() {
 		return DEFAULT_FILE_FORMAT_VERSION;
 	}
 
 	public SoftwareDescriptionBuilder getSoftwareDescription() {
 		return softwareDescription;
 	}
 
 	public void setSoftwareDescription(SoftwareDescriptionBuilder sd) {
 		softwareDescription = sd;
 	}
 
 	public List<TargetHardwareDefinitionBuilder> getTargetHardwareDefinitions() {
 		return thwDefinitions;
 	}
 
 	public List<FileDefinitionBuilder> getFileDefinitions() {
 		return fileDefinitions;
 	}
 
 	public IntegrityDefinitionBuilder getSdfIntegrityDefinition() {
 		return this.sdfIntegrityDefinition;
 	}
 
 	public void setSdfIntegrityDefinition(IntegrityDefinitionBuilder value) {
 		this.sdfIntegrityDefinition = value;
 	}
 
 	public IntegrityDefinitionBuilder getLspIntegrityDefinition() {
 		return this.lspIntegrityDefinition;
 	}
 
 	public void setLspIntegrityDefinition(IntegrityDefinitionBuilder value) {
 		this.lspIntegrityDefinition = value;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public SdfFile buildXml() {
 		SdfFile file = new SdfFile();
 		file.setFileFormatVersion(this.getFileFormatVersion());
 
 		// we have to re-validate this as a LIST1 since it can be modified
 		// without a set method to verify its validity prior to building
 		List<FileDefinitionBuilder> fileDefsValidated = (List<FileDefinitionBuilder>) DataValidator
 				.validateList1(fileDefinitions);
 		for (FileDefinitionBuilder fileDef : fileDefsValidated) {
 			file.getFileDefinitions().add(fileDef.buildXml());
 		}
 
 		for (TargetHardwareDefinitionBuilder thwDef : thwDefinitions) {
 			file.getThwDefinitions().add(thwDef.buildXml());
 		}
 
 		file.setLspIntegrityDefinition(lspIntegrityDefinition.buildXml());
 		file.setSdfIntegrityDefinition(sdfIntegrityDefinition.buildXml());
 		file.setSoftwareDescription(softwareDescription.buildXml());
 
 		return file;
 	}
 
 	@Override
 	public int buildBinary(BdfFile file) throws IOException {
 		file.seek(0);
 		// write the header
 		file.writePlaceholder(); // file size
 		file.writeHexbin32(getFileFormatVersion());
 		file.writePlaceholder(); // software description pointer
 		file.writePlaceholder(); // target hardware definition pointer
 		file.writePlaceholder(); // file definition pointer
 		file.writePlaceholder(); // SDF integrity definition pointer
 		file.writePlaceholder(); // LSP integrity definition pointer
 		this.getSoftwareDescription().buildBinary(file);
 
 		// Write the target hardware definitions
 		int size = this.getTargetHardwareDefinitions().size();
 		file.writeTargetDefinitionsPointer();
 		file.writeUint32(size);
 		if (size > 0) {
 			this.getTargetHardwareDefinitions().get(size - 1).setIsLast(true);
 			for (int i = 0; i < size; i++) {
 				this.getTargetHardwareDefinitions().get(i).buildBinary(file);
 			}
 		}
 
 		// write the file definitions
 		size = this.getFileDefinitions().size();
 		file.writeFileDefinitionsPointer();
 		file.writeUint32(size);
 		this.getFileDefinitions().get(size - 1).setIsLast(true);
 		for (int i = 0; i < size; i++) {
 			this.getFileDefinitions().get(i).buildBinary(file);
 		}
 
 		// write the SDF integrity def
 		file.writeSdfIntegrityDefinitionPointer();
 		this.getSdfIntegrityDefinition().setIntegrityValue(
 				Converter.hexToBytes("0000000A"));
 		this.getSdfIntegrityDefinition().buildBinary(file);
 
 		// write the LSP integrity def
 		file.writeLspIntegrityDefinitionPointer();
 		this.getLspIntegrityDefinition().setIntegrityValue(
 				Converter.hexToBytes("0000000A"));
 		this.getLspIntegrityDefinition().buildBinary(file);
 
 		// write the file size
 		file.seek(0);
 		file.writeUint32(file.length());
 		file.seek(file.length());
 
 		return (int) file.length();
 	}
 
 	public String getBinaryFileName() {
 		return getSoftwareDescription().getSoftwarePartNumber()
 				.replace("-", "") + ".BDF";
 	}
 
 	public String getXmlFileName() {
 		return getSoftwareDescription().getSoftwarePartNumber()
 				.replace("-", "") + ".XDF";
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return obj !=  null &&
 				this == obj ||
 				(obj instanceof SoftwareDefinitionFileBuilder &&
 				equals((SoftwareDefinitionFileBuilder)obj));		
 	}
 	
 	public boolean equals(SoftwareDefinitionFileBuilder obj){
 		return obj != null &&
 				this == obj ||
 				(this.getSoftwareDescription().equals(obj.getSoftwareDescription()) &&
 				this.getFileFormatVersion().equals(obj.getFileFormatVersion()) &&
 				this.getTargetHardwareDefinitions().equals(obj.getTargetHardwareDefinitions()) &&
 				this.getFileDefinitions().equals(obj.getFileDefinitions()) &&
 				this.getSdfIntegrityDefinition().equals(obj.getSdfIntegrityDefinition()) &&
 				this.getLspIntegrityDefinition().equals(obj.getLspIntegrityDefinition()));
 	}
 
 	@Override
 	public int hashCode() {
 		if (this.getSoftwareDescription() != null
 				&& this.getSoftwareDescription().getSoftwarePartNumber() != null) {
 			return this.getXmlFileName().hashCode();
 		}
 		return 0;
 	}
 }
