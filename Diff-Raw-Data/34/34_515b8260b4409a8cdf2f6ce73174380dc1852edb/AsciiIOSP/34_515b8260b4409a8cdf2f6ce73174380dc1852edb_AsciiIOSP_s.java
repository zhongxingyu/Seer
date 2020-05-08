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
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Section;
 import ucar.nc2.Variable;
 import ucar.unidata.io.RandomAccessFile;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import lasp.tss.TSSException;
 import lasp.tss.util.RegEx;
 
 /**
  * Read tabular ASCII data. Assume one row per time sample, 
  * one column per variable. Supports scalar variables only, for now.
  * This uses a variable name convention: 
  *   single character followed by the column number (zero-based).
  * The NcML file must use this convention for the variable names, 
  * or more likely, "orgName".
  * 
  * @author Doug Lindholm
  */
 public class AsciiIOSP extends AbstractIOSP {
 
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(AsciiIOSP.class);
     
     private String _comment; //comment character
     private ArrayList<String[]> _dataStrings;
     
     /**
      * Called during "open" after the ncml has been read.
      */
     protected void init() {
         _comment = getCommentCharacter();
         
         //skip header
         String headerLength = getProperty("headerLength");
         if (headerLength != null) {
             int linesToSkip = Integer.parseInt(headerLength);
             skipLines(linesToSkip);
         }
         
         readAllData(); 
     }
   
     protected String getCommentCharacter() {
         String c = getProperty("commentCharacter");
         return c;
     }
     
     /**
      * Determine if the given line is a comment.
      */
     private boolean isComment(String line) {
         boolean b = (_comment != null) && (line.startsWith(_comment));
         return b;
     }
     
     /**
      * Read all the rows into a List of Strings. Parse later.
      */
     private void readAllData() { 
         _dataStrings = new ArrayList<String[]>();
         
         //See if we need to use a regular expression to read a formatted time.
         //Needed for datetime spanning multiple columns (e.g. yyyy mm dd).
         String regex = getTimeRegEx();
         
         //Start reading one line at a time.
         String line = readLine();
         while (line != null) {
             line = line.trim();
             
             //skip empty or commented line
             if (line.isEmpty() || isComment(line)) {
                 line = readLine();
                 continue;
             }
             
             String[] ss = null;
             if (regex != null) {
                 //This path will be taken if the time variable is "formatted".
                 //Assume time is first
                 String[] tsplit = RegEx.match(line, regex, RegEx.DELIMITER, ".*");
                 //skip line that doesn't match
                 if (tsplit == null) {
                     String msg = "Can't parse row of data, skipping: " + line;
                     _logger.warn(msg);
                     line = readLine();
                     continue; 
                 }
                 
                 String time = tsplit[0]; //time portion
                 line = tsplit[2].trim(); //the rest of the line
                 String[] vsplit = line.split(RegEx.DELIMITER);
                 int n = vsplit.length + 1;
                 ss = new String[n];
                 ss[0] = time;
                 for (int i=1; i<n; i++) {
                     ss[i] = vsplit[i-1];
                 }
             } else {
                 ss = line.split(RegEx.DELIMITER); 
             }
             
             _dataStrings.add(ss);
             line = readLine(); //read next line
             //TODO: check if cancelled
         }
     }
     
     /**
     * If the time unit is "formatted", create a regular expression to parse that format.
      */
     private String getTimeRegEx() {
         String regex = null;
         
         String timeUnit = getTimeUnit();
        if ("formatted".equals(timeUnit)) {
            String timeFormat = getTimeFormat();
            if (timeFormat == null) {
                String msg = "The formatted time variable does not define its format.";
                _logger.error(msg);
                throw new TSSException(msg);
            }
             
             //Simply match the number of characters in the format
            int n = timeFormat.length();
             regex = ".{"+n+"}";
         }
         
         return regex;
     }
     
     
     /**
      * Return the number of time samples.
      * If not defined, get from the size of the data.
      */
     protected int getLength() {
         int length = super.getLength(); //from ncml def
         
         if (length < 0) {
             length = _dataStrings.size();
         }
         
         return length;
     }
 
     /**
      * Skip the given number of lines in the data file (e.g. header).
      */
     private void skipLines(int n) {
         for (int i=0; i<n; i++) readLine();
     }
 
     /**
      * Return the next line in the file. Will be null if there are none left.
      */
     protected String readLine() {
         RandomAccessFile file = getFile();
         String line;
         try {
             line = file.readLine();
         } catch (IOException e) {
             String msg = "Unable to read line from file: " + file.getLocation();
             _logger.error(msg, e);
             throw new TSSException(msg, e);
         }
         return line;
     }
     
     /**
      * Implement the NetCDF AbstractIOServiceProvider "read" interface.
      * Populate an ma2.Array with data from the given Variable (that we defined during "open")
      * for the subset defined in the given Section.
      */
     public Array readData(Variable variable, Section section) throws IOException, InvalidRangeException {
         Array array = null;
         
         String vname = variable.getName(); //This will be the original name we gave it here.
         DataType type = variable.getDataType();
         
         //get index from var name suffix: v#
         String num = vname.substring(1); //assumes one character followed by the column number
         int varIndex = -1;
         try {
             varIndex = Integer.parseInt(num);
         } catch (NumberFormatException nfe) {
             String msg = "Invalid variable name: '" + vname + "'. AsciiIOSP expects a character followed by the column number.";
             _logger.error(msg, nfe);
             throw new TSSException(msg, nfe);
         }
         
         //TODO: support flattened spectra (column for each sample), treat as 2nd dim
         
         
         //Get info on time selection (1st dimension)
         //Assume only 1D for now
         int[] shape = section.getShape();
         int origin = section.getOrigin(0);
         int stride = section.getStride(0);
         int length = shape[0];
         
         //make array for Strings or doubles
         double[] data = null; 
         String[] sdata = null;
         if (type.isString()) sdata = new String[length];
         else data = new double[length];
 
         int j=0;
         for (int i=0; i<length; i++) {
             int index = origin + stride * i;
             String s = (_dataStrings.get(index))[varIndex];
             if (type.isString()) sdata[j] = s;
             else {
                 try {
                     data[j] = Double.parseDouble(s);
                 } catch (NumberFormatException e) {
                     //If it's not a number then it's NaN
                     String msg = "Unable to parse data value. Replacing with NaN: " + s;
                     _logger.warn(msg, e);
                     data[j] = Double.NaN;
                 }
             }
             j++;
         }
         
         //Construct the Array.
         if (type.isString()) array = Array.factory(type, shape, sdata);
         else array = Array.factory(type, shape, data);
         
         return array;
     }
     
     
     public String getFileTypeDescription() {
         String s = "ASCII table";
         return s;
     }
 
     public String getFileTypeId() {
         return "TSS-Ascii";
     }
     
 }
 
