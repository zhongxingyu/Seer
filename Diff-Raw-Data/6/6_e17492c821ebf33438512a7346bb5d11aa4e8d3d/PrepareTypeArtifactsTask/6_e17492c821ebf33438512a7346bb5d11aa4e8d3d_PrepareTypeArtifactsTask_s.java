 package edu.umn.msi.cagrid.introduce.interfaces.client.ant;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 
 public class PrepareTypeArtifactsTask extends Task {
   LinkedList<Schema> schemas = new LinkedList<Schema>();
   private String wsdlFilePath;
   private String namespaceMappingFilePath;
   
  
   private static final String WSDL_PREFIX="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\" name=\"Types\" targetNamespace=\"http://helloworld.cagrid.nci.nih.gov/Types\">\n<types><schema xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://helloworld.cagrid.nci.nih.gov/Types\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">";
   private static final String WSDL_SUFFIX="</schema></types></definitions>";
   
   public void execute() {
     File[] files = new File[]{ new File(wsdlFilePath), new File(namespaceMappingFilePath)};
     String[] contents = new String[] { getWsdlFileContents(), getNamespaceToPackageContents() };
     try {
       for(int i = 0 ; i < files.length; i++) {
         FileWriter writer = new FileWriter(files[i]);
         writer.write(contents[i]);
         writer.close();
       }
     } catch(IOException e) {
       throw new BuildException("Failed to create type artifacts", e);
     }
   }
   
   public String escapeForPropertiesFile(String str) {
     return str.replaceAll("\\:", "\\\\:");
   }
   
   public String getNamespaceToPackageContents() {
     StringBuffer contents = new StringBuffer();
     for(Schema schema : schemas) {
       String line = schema.getNamespace() + "=" + schema.getPackage();
       contents.append(escapeForPropertiesFile(line) + "\n");
     }
     return contents.toString();
   }
   
   public String getWsdlFileContents() {
     StringBuffer imports = new StringBuffer();
     for(Schema schema : schemas) {
       imports.append("<import namespace=\"");
       imports.append(schema.getNamespace());
       imports.append("\" schemaLocation=\"");
      imports.append(schema.getPath());
       imports.append("\" />");
     }
     return WSDL_PREFIX + imports.toString() + WSDL_SUFFIX;
   }
   
   public Schema createSchema() {
     Schema schema = new Schema();
     schemas.add(schema);
     return schema;
   }
   
   public static class Schema {
     private String path;
     private String package_;
     private String namespace;
     public String getPath() {
       return path;
     }
     public void setPath(String path) {
       this.path = path;
     }
     public String getPackage() {
       return package_;
     }
     public void setPackage(String package_) {
       this.package_ = package_;
     }
     public String getNamespace() {
       return namespace;
     }
     public void setNamespace(String namespace) {
       this.namespace = namespace;
     }
   }
 
   public String getWsdlFile() {
     return wsdlFilePath;
   }
 
   public void setWsdlFile(String wsdlFile) {
     this.wsdlFilePath = wsdlFile;
   }
 
   public String getNamespaceMappingFile() {
     return namespaceMappingFilePath;
   }
 
   public void setNamespaceMappingFile(String namespaceMappingFile) {
     this.namespaceMappingFilePath = namespaceMappingFile;
   }
 }
