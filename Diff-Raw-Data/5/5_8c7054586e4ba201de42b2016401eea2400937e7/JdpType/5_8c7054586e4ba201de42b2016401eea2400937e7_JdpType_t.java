 /*
  * Copyright 2007 Josh Kropf
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
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.DataType;
 
 /**
  * Collection of project settings.  Project settings can be set using the
  * data type attributes, or by specifying a properties file that contains the
  * settings. The properties file must contains keys that match the attributes
  * of this data type exactly.
  * @author josh
  */
 public class JdpType extends DataType {
    private TypeAttribute type;
    private String title;
    private String version;
    private String vendor;
    private String description;
    private String arguments;
    private String midletClass;
    private boolean systemModule;
    private boolean runOnStartup;
    private int startupTier;
    private int ribbonPosition;
    private String icon;
    private String nameResourceBundle;
    private int nameResourceId;
    
    private List<EntryPointType> entryPoints = new ArrayList<EntryPointType>();
    
    public JdpType() {
       type = new TypeAttribute();
       type.setValue(TypeAttribute.CLDC);
       title = "";
       vendor = "<unknown>";
       version = "0.0";
       icon = "";
       arguments = "";
       midletClass = "";
       startupTier = 7;
       ribbonPosition = 0;
       nameResourceId = -1;
    }
    
    public TypeAttribute getType() {
       return type;
    }
    
    public void setType(TypeAttribute type) {
       this.type = type;
    }
    
    public String getArguments() {
       return arguments;
    }
    
    public void setArguments(String arguments) {
       this.arguments = arguments;
    }
    
    public String getMidletClass() {
       return midletClass;
    }
    
    public void setMidletClass(String midletClass) {
       this.midletClass = midletClass;
    }
    
    public String getVersion() {
       return version;
    }
    
    public void setVersion(String version) {
       this.version = version;
    }
    
   public String getDescription() {
       return description;
    }
    
   public void setDescription(String desc) {
       this.description = desc;
    }
    
    public int getRibbonPosition() {
       return ribbonPosition;
    }
    
    public void setRibbonPosition(int ribbonPosition) {
       this.ribbonPosition = ribbonPosition;
    }
    
    public boolean isRunOnStartup() {
       return runOnStartup;
    }
    
    public void setRunOnStartup(boolean runOnStartup) {
       this.runOnStartup = runOnStartup;
    }
    
    public int getStartupTier() {
       return startupTier;
    }
    
    public void setStartupTier(int startupTier) {
       if (startupTier >= 0 && startupTier <= 7) {
          this.startupTier = startupTier;
       } else {
          throw new BuildException("startuptier must be between 0 and 7");
       }
    }
    
    public boolean isSystemModule() {
       return systemModule;
    }
    
    public void setSystemModule(boolean systemModule) {
       this.systemModule = systemModule;
    }
    
    public String getTitle() {
       return title;
    }
    
    public void setTitle(String title) {
       this.title = title;
    }
    
    public String getVendor() {
       return vendor;
    }
    
    public void setVendor(String vendor) {
       this.vendor = vendor;
    }
    
    public String getIcon() {
       return icon;
    }
    
    public void setIcon(String icon) {
       this.icon = icon;
    }
 
    public String getNameResourceBundle() {
       return nameResourceBundle;
    }
 
    public void setNameResourceBundle(String nameResourceBundle) {
       this.nameResourceBundle = nameResourceBundle;
    }
 
    public int getNameResourceId() {
       return nameResourceId;
    }
 
    public void setNameResourceId(int nameResourceId) {
       this.nameResourceId= nameResourceId;
    }
 
    public void addEntry(EntryPointType entry) {
       entryPoints.add(entry);
    }
    
    /**
     * Loads project attributes from a properties file.
     * @param file
     * @throws BuildException
     */
    public void setFile(File file) throws BuildException {
       FileInputStream in = null;
       
       if (!file.isFile()) {
          throw new BuildException("file attribute must be a properties file");
       }
       
       try {
          in =  new FileInputStream(file);
          
          Properties props = new Properties();
          props.load(in);
          
          type.setValue(props.getProperty("type", TypeAttribute.CLDC));
          title = props.getProperty("title", "");
          version = props.getProperty("version", "0.0");
          vendor = props.getProperty("vendor", "<unknown>");
          description = props.getProperty("description");
          arguments = props.getProperty("arguments", "");
          midletClass = props.getProperty("midletclass", "");
          systemModule = Boolean.parseBoolean(props.getProperty("systemmodule", "false"));
          runOnStartup = Boolean.parseBoolean(props.getProperty("runonstartup", "false"));
          setStartupTier(Integer.parseInt(props.getProperty("startupTier", "7")));
          setRibbonPosition(ribbonPosition = Integer.parseInt(props.getProperty("ribbonposition", "0")));
          icon = props.getProperty("icon", "");
          nameResourceBundle = props.getProperty("nameresourcebundle");
          nameResourceId = Integer.parseInt(props.getProperty("nameresourceid", "-1"));
       } catch (IOException e) {
          throw new BuildException("error loading properties", e);
       } finally {
          if (in != null) {
             try { in.close(); }
             catch (IOException e) { }
          }
       }
    }
    
    /**
     * Generates a manifest file compatible with the rapc compiler.
     * @param file
     * @param output
     * @throws BuildException
     */
    public void writeManifest(File file, String output) throws BuildException {
       PrintStream out = null;
       
       try {
          // create file if it does not exist
          if (!file.exists()) file.createNewFile();
          
          out = new PrintStream(file);
          
          out.printf("MIDlet-Name: %s\n", output);
          out.printf("MIDlet-Version: %s\n", version);
          out.printf("MIDlet-Vendor: %s\n", vendor);
          
          if (description != null) {
             out.printf("MIDlet-Description: %s\n", description);
          }
          
          out.printf("MIDlet-Jar-URL: %s.jar\n", output);
          out.println("MIDlet-Jar-Size: 0");
          out.println("MicroEdition-Profile: MIDP-2.0");
          out.println("MicroEdition-Configuration: CLDC-1.1");
          
          if (TypeAttribute.CLDC.equals(type.getValue())) {
             out.printf("MIDlet-1: %s,%s,%s\n", title, icon, arguments);
             
             if (ribbonPosition > 0) {
                out.printf("RIM-MIDlet-Position-1: %d\n", ribbonPosition);
             }
 
             if (nameResourceBundle != null && nameResourceId > -1) {
                out.printf("RIM-MIDlet-NameResourceBundle-1: %s\n", nameResourceBundle);
                out.printf("RIM-MIDlet-NameResourceId-1: %d\n", nameResourceId);
             }
             
             int flags = 0x00;
             if (runOnStartup) flags |= 0xE1-((2*startupTier)<<4);
             if (systemModule) flags |= 0x02;
             out.printf("RIM-MIDlet-Flags-1: %d\n", flags);
             
             if (entryPoints.size() != 0) {
                int i = 2;
                for (EntryPointType entryPoint : entryPoints) {
                   out.printf("MIDlet-%d: %s,%s,%s\n", i, entryPoint.getTitle(),
                         entryPoint.getIcon(), entryPoint.getArguments());
                   
                   if (entryPoint.getRibbonPosition() > 0) {
                      out.printf("RIM-MIDlet-Position-%d: %d\n", i, entryPoint.getRibbonPosition());
                   }
 
                   if (entryPoint.getNameResourceBundle() != null && entryPoint.getNameResourceId() > -1) {
                      out.printf("RIM-MIDlet-NameResourceBundle-%d: %s\n", i, entryPoint.getNameResourceBundle());
                      out.printf("RIM-MIDlet-NameResourceId-%d: %d\n", i, entryPoint.getNameResourceId());
                   }
 
                   flags = 0x00;
                   if (entryPoint.isRunOnStartup()) {
                      flags |= 0xE1-((2*entryPoint.getStartupTier())<<4);
                   }
                   
                   if (entryPoint.isSystemModule()) {
                      flags |= 0x02;
                   }
                   
                   out.printf("RIM-MIDlet-Flags-%d: %d\n", i, flags);
                   
                   i ++;
                }
             }
          } else if (TypeAttribute.MIDLET.equals(type.getValue())) {
             out.printf("MIDlet-1: %s,%s,%s\n", title, icon, midletClass);
             
             if (ribbonPosition > 0) {
                out.printf("RIM-MIDlet-Position-1: %d\n", ribbonPosition);
             }
             
             int flags = 0xE0;
             if (systemModule) flags |= 0x02;
             out.printf("RIM-MIDlet-Flags-1: %d\n", flags);
          } else {
             int flags = 0x02;
             if (runOnStartup) flags |= 0xE1-((2*startupTier)<<4);
             out.printf("RIM-Library-Flags: %d\n", flags);
          }
       } catch (IOException e) {
          throw new BuildException("error creating manifest", e);
       } finally {
          if (out != null) out.close();
       }
    }
 }
