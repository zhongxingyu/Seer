 package edu.cmu.sv.arinc838.writer;
 
 import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
 
 public interface SdfWriter {

 	public String write (String path, SoftwareDefinitionFileBuilder builder) throws Exception;	
 }
