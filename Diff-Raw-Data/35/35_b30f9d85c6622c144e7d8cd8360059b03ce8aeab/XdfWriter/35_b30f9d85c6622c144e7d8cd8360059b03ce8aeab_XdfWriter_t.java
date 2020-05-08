 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 6, 2012
  */
 package edu.cmu.sv.arinc838.writer;
 
 import java.io.File;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 
 import com.arinc.arinc838.SdfFile;
 import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
 
 import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
 
 public class XdfWriter implements SdfWriter {
 	@Override
 	public String write(String path, SoftwareDefinitionFileBuilder builder)
 			throws Exception {
 		File file = new File(path + builder.getXmlFileName());
 		SdfFile sdfFile = builder.buildXml();
 		return write(file, sdfFile);
 	}
 

 	public String write(File file, SdfFile sdfFile) throws Exception {
 		JAXBContext jaxbContext = JAXBContext.newInstance(SdfFile.class);
 		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 
 		NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
 			public String getPreferredPrefix(String namespaceUri,
 					String suggestion, boolean requirePrefix) {
 				if (namespaceUri.contains("arinc.com")) {
 					return "sdf";
 				} else {
 					return "";
 				}
 			}
 		};
 		jaxbMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
 				mapper);
 		// output pretty printed
 		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 		jaxbMarshaller.marshal(sdfFile, file);
 		return file.getCanonicalPath();
 	}
 }
