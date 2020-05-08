 /*
  * Copyright (c) 2010, Regents of the University of Colorado
  * 
  * All rights reserved. Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided that the following 
  * conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution. 
  * Neither the name of the University of Colorado nor the names of its
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package lasp.tss.iosp;
 
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 import org.jdom.filter.ElementFilter;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Section;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.Group;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Structure;
 import ucar.nc2.Variable;
 import ucar.nc2.iosp.AbstractIOServiceProvider;
 import ucar.nc2.util.CancelTask;
 import ucar.unidata.io.RandomAccessFile;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Extend the NetCDF AbstractIOServiceProvider to do most of the Dataset
  * definition from the NcML. Note, this uses a modification to the NetCDF-Java
  * code to get the JDOM Element that defines the "netcdf" element that this
  * IOSP supports. This allows us to define the NetcdfFile from the NcML
  * instead of relying on another source of metadata that may or may not exist.
  * The @see #readData(Variable,Section) method must be implemented by a subclass.
  * 
  * @author Doug Lindholm
  */
 public abstract class AbstractIOSP extends AbstractIOServiceProvider {
 
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(AbstractIOSP.class);
     
     private Properties _properties;
     private String _iospParam;
     
     private RandomAccessFile _raFile;
     private NetcdfFile _ncFile;
     private CancelTask _cancelTask;
     
     /**
      * JDOM Element defining the "netcdf" element that uses this IOSP.
      */
     private Element _ncElement;
 
     /**
      * Number of time samples found in the data source. "-1" implies undefined.
      */
     private int _ntim = -1;
     
     
     /**
      * Implement the NetCDF AbstractIOServiceProvider "read" interface.
      * Populate an ma2.Array with data from the given Variable (that we defined during "open")
      * for the subset defined in the given Section.
      */
     public abstract Array readData(Variable variable, Section section) throws IOException, InvalidRangeException;
         
     /**
      * Implementation of NetCDF abstract IOSP. This method will be called with:
      * - an open file as defined in the ncml "location"
      * - the shell of a NetcdfFile that needs to be populated
      * - a hook to cancel lengthy operations (not used, yet)
      */
     public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
         _raFile = raf;
         _ncFile = ncfile;
         _cancelTask =cancelTask;
         
         try {
             _ncElement = ncfile.getNetcdfElement();
             
             init();
    
             makeDimensions(null, _ncElement);
             makeVariables(null, _ncElement);
             makeGroups(null, _ncElement);
         } catch (Throwable t) {
             String msg = "IOSP open failed.";
             _logger.error(msg, t);
             throw new IOException(msg, t);
         }
     }
     
     /**
      * Hook to do some IOSP specific initialization.
      * Called just after NetCDF calls our "open" method.
      * Default to no-op.
      */
     protected void init() {}
     
     /**
      * Release resources used by this IOSP.
      * IMPORTANT: Override this if you use resources in addition to the "location" file.
      *   Note that NetCDF has already opened the "location" file so it needs to be closed.
      *   Otherwise, you may see "too many open files" type errors in your Servlet container.
      */
     public void close() throws IOException {
         try {
             getFile().close();
         } catch (IOException e) {
             String msg = "Failed closing data source: " + getFile().getLocation();
             _logger.warn(msg, e);
         }
     }
     
     /**
      * Look for group definitions in the ncml. Create them (without data) and add them to ncfile.
      */
     protected void makeGroups(Group parent, Element element) {
         List<Element> groups = element.getChildren("group", element.getNamespace());
         for (Element e : groups) {
             Group group = makeGroup(parent, e);
             _ncFile.addGroup(parent, group);
         }
     }
     
     /**
      * Create a Group with the given parent and the XML element that defines it.
      */
     protected Group makeGroup(Group parent, Element element) {
         Group group = null;
         
         String name = element.getAttributeValue("name");
         group = new Group(_ncFile, parent, name);
 
         makeDimensions(group, element);
         makeVariables(group, element);
         makeGroups(group, element);
         
         //reader needs to see group tsdsType, add to ncGroup as ncAtt for now
         String type = element.getAttributeValue("tsdsType");
         if (type != null) {
             Attribute att = new Attribute("tsdsType", type);
             group.addAttribute(att);
         }
         
         return group;
     }
 
     /**
      * Look for variable definitions in the ncml. Create them (without data) and add them to ncfile.
      */
     protected void makeVariables(Group parent, Element element) {
         List<Element> vars = element.getChildren("variable", element.getNamespace());
         for (Element e : vars) {
             Variable var = makeVariable(parent, e);
             _ncFile.addVariable(parent, var);
         }   
     }
     
     /**
      * Create a Variable with the given parent and the XML element that defines it.
      */
     protected Variable makeVariable(Group parent, Element element) {
         Variable var = null;
         
         String name = getVariableName(element);
         String shape = element.getAttributeValue("shape"); //may be null for scalar
         String type = element.getAttributeValue("type");
         
         if ("Structure".equals(type)) var = makeStructure(element);
         else{
             var = new Variable(_ncFile, parent, null, name);
             var.setDataType(DataType.getType(type));
             var.setDimensions(shape);
         }
         
         return var;
     }
 
     /**
      * Create a Structure variable from the XML element that defines it.
      */
     private Variable makeStructure(Element element) {
         String name = getVariableName(element);
         String shape = element.getAttributeValue("shape");
         
         Structure struct = new Structure(_ncFile, null, null, name);
         struct.setDimensions(shape);
         
         //Make member variables
         List<Element> vars = element.getChildren("variable", element.getNamespace());
         for (Element e : vars) {
             Variable v = makeVariable(null, e);
             v.setParentStructure(struct);
             struct.addMemberVariable(v);
         }
         
         return struct;
     }
     
     /**
      * Look for dimension definitions in the ncml. Create them and add them to ncfile.
      */
     protected void makeDimensions(Group parent, Element element) {
         List<Element> dims = element.getChildren("dimension", element.getNamespace());
         for (Element e : dims) {
             Dimension dim = makeDimension(e);
             _ncFile.addDimension(parent, dim);
         }
     }
 
     /**
      * Create a Dimension from the XML element that defines it.
      */
     protected Dimension makeDimension(Element element) {
         Dimension dim = null;
         boolean isShared = true; 
        
         boolean isUnlimited = false;
         String unlimited = element.getAttributeValue("isUnlimited");
         if (unlimited != null) isUnlimited = Boolean.parseBoolean(unlimited);
         
         boolean isVariableLength = false;
         String varlen = element.getAttributeValue("isVariableLength");
         if (varlen != null) isVariableLength = Boolean.parseBoolean(varlen);
         
         int n = 0; //default to zero length, 
         String length = element.getAttributeValue("length");
         if (length != null) n = Integer.parseInt(length);
 
         //If length is not defined and this is the time dimension,
         //  delegate to getLength() to get the number of time samples.
         //This may involve reading all the time data.
         String name = element.getAttributeValue("name");
         if ("time".equals(name)) {
             if (isVariableLength) {
                 n = -1; //unlimited (or unknown?)
             } else if (n == 0) { //not variable length and not defined in ncml
                 n = getLength(); //get length from other method (e.g. read times) and save it
                 _ntim = n;
             }
         }
 
         dim = new Dimension(name, n, isShared, isUnlimited, isVariableLength);
 
         return dim;
     }
 
     /**
      * Return the file that NetCDF handed us in the call to "open" (already open).
      */
     public RandomAccessFile getFile() {
         return _raFile;
     }
     
 
     /**
      * Return false so NetCDF won't automatically determine that this IOSP should handle the given file.
      * Use this IOSP only if it is specified in the ncml with the "iosp" attribute.
      */
     public boolean isValidFile(RandomAccessFile raf) throws IOException {
         return false;
     }
 
     /**
      * Called by NetCDF passing in the contents of the ncml "iospParam" attribute.
      * Assumes that message is a String of key,value pairs: "k1=v1 k2=v2"
      * Ignores return value.
      * @deprecated Use "netcdf" element attributes, 
      * @see #getProperty(String)
      */
     public Object sendIospMessage(Object message) {
         _iospParam = (String) message;
         
         if (message == null) return null;
         
         _logger.warn("Just wanted to let you know that you are using a deprecated technique.");
         
         //parse and save in _properties
         _properties = new Properties();
         String[] props = ((String) message).split("\\s"); //split on white space
         for (String string : props) {
             //split on first "="
             int index = string.indexOf("=");
             if (index > 0) {
                 String name = string.substring(0,index);
                 String value = string.substring(index+1);
                 _properties.put(name, value);
             }
         }
         
         return null;
     }
     
     /**
      * Get the value of the property with the given name.
      * These properties are defined in the iospParam (deprecated)
      * or the XML attributes of the "netcdf" element.
      */
     protected String getProperty(String name) {
         String prop = null;
         if (_properties != null) prop = _properties.getProperty(name); //from iospParam
         if (prop == null) {
             Element ncElement = getNetcdfElement();
             prop = ncElement.getAttributeValue(name); //from "netcdf" ncml Element
         }
 
         return prop;
     }
     
     /**
      * Return the XML element representing the netcdf component.
      */
     protected Element getNetcdfElement() {
         return _ncElement;
     }
     
     /**
      * Return the NetcdfFile that was given to us and populated by in in the "open" method.
      */
     protected NetcdfFile getNetcdfFile() {
         return _ncFile;
     }
 
     /**
      * Get the number of time samples from the ncml.
      * Return -1 if not defined which may be the case if the data have not been read.
      * The length should be established when creating the Dimension-s.
      */
     protected int getLength() { 
         return _ntim;
     }
     
     /**
      * Return the original name for the time variable.
      * Look for the "time" variable in the NcML.
      * Return the "orgName" if it exists or "time" otherwise.
      */
     protected String getTimeVarName() {
         Element telement = getTimeElement();
         String vname = getVariableName(telement);
         return vname;
     }
     
     /**
      * Return the XML element that defines the time variable.
      */
     protected Element getTimeElement() {
         //Make a filter that finds a variable named "time"
         ElementFilter filter = new ElementFilter("variable") {
             boolean match(Object o) {
                 boolean b = super.matches(o);
                 if (b) {
                     String name = ((Element) o).getAttributeValue("name");
                     b = "time".equals(name);
                 }
                 return b;
             };
         };
         
         Element ncElement = getNetcdfElement();
         List<Element> elmnts = ncElement.getContent(filter);
         Element telmnt = elmnts.get(0);
         return telmnt;
     }
     
     /**
      * Return the time variable's "units" nc2.Attribute value.
      */
     protected String getTimeUnit() {
         return getTimeAttribute("units");
     }
     
     /**
     * Return the time variable's "format" nc2.Attribute value.
     */
    protected String getTimeFormat() {
        return getTimeAttribute("format");
    }
    
    /**
      * Return the value of the named nc2.Attribute from the time variable.
      */
     protected String getTimeAttribute(String attName) {
         String att = null;
         
         Element telement = getTimeElement();
         List<Element> atts = telement.getChildren("attribute", telement.getNamespace());
         for (Element element : atts) {
             String name = element.getAttributeValue("name");
             if (attName.equals(name)) {
                 att = element.getAttributeValue("value");
                 break;
             }
         }
         
         return att;
     }
     
     /**
      * Return the name of a variable from the NcML element that defines it.
      * Use the original name if we have it.
      */
     protected String getVariableName(Element varElement) {
         String name = varElement.getAttributeValue("orgName"); 
         if (name == null) name = varElement.getAttributeValue("name");
         return name;
     }
     
     /**
      * Return the value of a given named attribute from the variable with the given name.
      */
     protected String getVariableAttribute(String varName, String attName) {
         String s = null;
         
         Element element = getNetcdfElement();
         Iterator it = (Iterator) element.getDescendants();
         while (it.hasNext()) {
             Object o = it.next();
             if (! (o instanceof Element)) continue;
             Element e = (Element) o;
             String ename = e.getName();
             if (! ename.equals("variable")) continue;
             
             String vname = e.getAttributeValue("name");
             if (vname.equals(varName)) {
                 s = e.getAttributeValue(attName);
                 break;
             }
         }
         
         return s;
     }
 }
