 /*
  * Copyright 2008 Josh Kropf
  * 
  * This file is part of bb-ant-tools.
  * 
  * bb-ant-tools is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * bb-ant-tools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with bb-ant-tools; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package ca.slashdev.bb.types;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.DataType;
 import org.apache.tools.ant.types.Resource;
 import org.apache.tools.ant.types.resources.FileResource;
 import org.apache.tools.ant.util.ResourceUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import ca.slashdev.bb.util.Utils;
 import ca.slashdev.bb.util.VersionMatch;
 
 /**
  * @author josh
  */
 public class ApplicationType extends DataType {
    private String id;
    private String name;
    private String description;
    private String version;
    private String vendor;
    private String copyright;
    private VersionMatch greater;
    private VersionMatch less;
    private Vector<CodSetType> codSets = new Vector<CodSetType>();
    
    public void setId(String id) {
       this.id = id;
    }
    
    public String getId() {
       return id;
    }
    
    public void setName(String name) {
       this.name = name;
    }
    
    public String getName() {
       return name;
    }
    
    public void setTitle(String name) {
       this.name = name;
    }
    
    public void setDescription(String desc) {
       this.description = desc;
    }
    
    public String getDescription() {
       return description;
    }
    
    public void setVersion(String version) {
       this.version = version;
    }
    
    public String getVersion() {
       return version;
    }
    
    public void setVendor(String vendor) {
       this.vendor = vendor;
    }
    
    public String getVendor() {
       return vendor;
    }
    
    public void setCopyright(String copyright) {
       this.copyright = copyright;
    }
    
    public String getCopyright() {
       return copyright;
    }
 
    public void setGreaterThan(String version) throws BuildException {
       greater = new VersionMatch(version, false);
    }
    
    public void setGreaterThanEqual(String version) {
       greater = new VersionMatch(version, true);
    }
    
    public void setLessThan(String version) {
       less = new VersionMatch(version, false);
    }
    
    public void setLessThanEqual(String version) {
       less = new VersionMatch(version, true);
    }
    
    public boolean hasVersionMatch() {
       return greater != null || less != null;
    }
    
    public void addCodSet(CodSetType codSet) {
       codSets.add(codSet);
    }
 
    public String getVersionMatch() {
       StringBuffer val = new StringBuffer();
       
       if (greater != null) {
          val.append(greater.isInclusive()? "[" : "(");
          val.append(greater.getVersion()).append(',');
       } else {
          val.append("(,");
       }
       
       if (less != null) {
          val.append(less.getVersion()).append(less.isInclusive()? "]" : ")");
       } else {
          val.append(")");
       }
       
       return val.toString();
    }
    
    public void setFile(File file) throws BuildException {
       FileInputStream in = null;
       
       if (!file.isFile()) {
          throw new BuildException("file attribute must be a properties file");
       }
       
       try {
          in =  new FileInputStream(file);
          
          Properties props = new Properties();
          props.load(in);
          
          id = props.getProperty("id");
          name = props.getProperty("name");
          if (name == null)
             name = props.getProperty("title");
          description = props.getProperty("description");
          version = props.getProperty("version");
          vendor = props.getProperty("vendor");
          copyright = props.getProperty("copyright");
       } catch (IOException e) {
          throw new BuildException("error loading properties", e);
       } finally {
          if (in != null) {
             try { in.close(); }
             catch (IOException e) { }
          }
       }
    }
    
    @SuppressWarnings("unchecked")
    public void generate(Document xmldoc, Element parent) {
       if (id == null) {
          throw new BuildException("id attribute is reqired");
       }
       
       Element child, appNode = xmldoc.createElement("application");
       parent.appendChild(appNode);
       
       appNode.setAttribute("id", id);
       
       String appBBVer = null;
       if (hasVersionMatch()) {
          appBBVer = getVersionMatch();
          appNode.setAttribute("_blackberryVersion", appBBVer);
       }
       
       appNode.appendChild(child = xmldoc.createElement("name"));
       if (name != null) {
          child.setTextContent(name);
       }
       
       appNode.appendChild(child = xmldoc.createElement("description"));
       if (description != null) {
          child.setTextContent(description);
       }
 
       appNode.appendChild(child = xmldoc.createElement("version"));      
       if (version != null) {
          child.setTextContent(version);
       }
       
       appNode.appendChild(child = xmldoc.createElement("vendor"));
       if (vendor != null) {
          child.setTextContent(vendor);
       }
 
       appNode.appendChild(child = xmldoc.createElement("copyright"));      
       if (copyright != null) {
          child.setTextContent(copyright);
       }
       
       for (CodSetType codSet : codSets) {
          String codSetBBVer = null;
          if (codSet.hasVersionMatch()) {
             codSetBBVer = codSet.getVersionMatch();
          }
          
          Element filesetNode = xmldoc.createElement("fileset");
          appNode.appendChild(filesetNode);
          
          filesetNode.setAttribute("Java", "1.0");
          
          if (codSetBBVer != null) {
         	 filesetNode.setAttribute("_blackberryVersion", codSetBBVer);
          }
          
          if (codSet.getDir() != null) {
         	 filesetNode.appendChild(child = xmldoc.createElement("directory"));
              child.setTextContent(codSet.getDir());
          }
          
          StringBuffer files = new StringBuffer();
          Iterator<Resource> i = codSet.getResources().iterator();
          while (i.hasNext()) {
             files.append('\n').append(Utils.getFilePart(i.next()));
          }
          files.append('\n');
          
          Element filesNode = xmldoc.createElement("files");
          filesNode.setTextContent(files.toString());
          
          filesetNode.appendChild(filesNode);
       }
    }
    
    @SuppressWarnings("unchecked")
    public void copyCodFiles(File destDir) throws IOException {
       Resource r;
       
       for (CodSetType codSet : codSets) {
         File codSetDestDir = destDir;
         
          if (codSet.getDir() != null) {
         	codSetDestDir = new File(destDir, codSet.getDir());
             if (!codSetDestDir.exists())
                if (!codSetDestDir.mkdirs())
                   throw new IOException("unable to create cod files director");
          }
          
          FileResource destFile;
          Iterator<Resource> i = codSet.getResources().iterator();
          while (i.hasNext()) {
             r = i.next();
             destFile = new FileResource(codSetDestDir, Utils.getFilePart(r));
             
             if (!r.equals(destFile))
                ResourceUtils.copyResource(r, destFile);
          }
       }
    }
 }
 
