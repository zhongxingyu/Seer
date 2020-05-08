 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
 import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerImageReader;
 import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerInfo;
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import omero.ServerError;
 
 
 /**
  * Utilities for getting files from a directory or OMERO data source and matching them together into sets of the different channels of the same image.
  * 
  * @author Colin J. Fuller
  */
 public class DirUtils {
 
     private DirUtils() {}
 
     
     /**
      * Gets filenames of the images to be processed (either from a directory or OMERO data source), along with a display name (i.e. name without a full path) for each.
      * @param params    The ParameterDictionary containing the parameters to be used for the analysis; the data directory or OMERO data source will be pulled from here.
      * @return          A List containing ImageSets, one per file.
      */
     public static java.util.List<ImageSet> makeMultiwavelengthFileSets(ParameterDictionary params) {
 
 
         java.util.Vector<ImageSet> outputSets = new java.util.Vector<ImageSet>();
 
         if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {
 
             String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");
 
 
             for (String id : imageIds) {
 
                 ImageSet currSet = new ImageSet(params);
 
                 currSet.addImageWithOmeroId(Long.parseLong(id));
 
                 outputSets.add(currSet);
             }
 
 
         } else {
 
             java.io.File directory = new java.io.File(params.getValueForKey("local_directory"));
 
             String commonFilenameTag = "";
             String imageExtension = "";
 
             if (params.hasKey("image_extension")) {
                 imageExtension = params.getValueForKey("image_extension");
             }
 
             if (params.hasKey("common_filename_tag")) {
                 commonFilenameTag = params.getValueForKey("common_filename_tag");
             }
 
             for (java.io.File f : directory.listFiles()) {
 
 
                 if ((f.getName().matches(".*Thumb.*")) || (! f.getName().toLowerCase().matches(".*" + imageExtension.toLowerCase() + "$")) || (! f.getName().matches(".*" + commonFilenameTag + ".*"))) {
                     continue;
                 }
 
             	System.out.println(f.getAbsolutePath());
 
                 
                 ImageSet set = new ImageSet(params);
 
                 set.addImageWithFilename(f.getAbsolutePath());
 
             
                 outputSets.add(set);
 
             }
         }
 
 
         return outputSets;
 
     }
 
 
     
     /**
      * Gets a list of String arrays each containing a set of filenames that correspond to image files for the color channels of a split channel image.
      *
      * @param params    The ParameterDictionary containing the parameters for the analysis.  The directory or OMERO source and channel names will be taken from here.
      * @return          A List containing ImageSets.  Each set can load the Images for the channels of an image.
      */
     public static java.util.List<ImageSet> makeSetsOfMatchingFiles(ParameterDictionary params) {
 
         String[] setTags = params.getValueForKey("channel_name").split(" ");
 
         int numPerSet = 0;
         if (params.hasKey("number_of_channels")) {
             numPerSet = Integer.parseInt(params.getValueForKey("number_of_channels"));
         } else {
             numPerSet = setTags.length;
             params.addIfNotSet("number_of_channels", Integer.toString(numPerSet));
         }
 
         java.util.Vector<ImageSet>  outputSets = new java.util.Vector<ImageSet>();
 
 
 
 
         java.io.File directory = new java.io.File(params.getValueForKey("local_directory"));
 
         java.util.HashMap<String, Long> idLookupByName = new java.util.HashMap<String, Long>();
 
         if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {
 
             String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");
 
             OmeroServerImageReader ir = new OmeroServerImageReader();
 
             try {
 
                 for (String id : imageIds) {
 
                     Long idL = Long.parseLong(id);
 
                     OmeroServerInfo osi = new OmeroServerInfo(params.getValueForKey("omero_hostname"), params.getValueForKey("omero_username"), params.getValueForKey("omero_password").toCharArray());
                     
                     String name = ir.getImageNameForOmeroId(idL, osi);
  
                     idLookupByName.put(name, idL);
 
                 }
 
             } catch (ServerError e) {
 
                 LoggingUtilities.getLogger().severe("Exception encountered while accessing image on OMERO server: ");
                 e.printStackTrace();
 
             } finally {
                 ir.closeConnection();
             }
 
 
         } else {
 
 
             for (java.io.File f : directory.listFiles()) {
 
                 //added hack here for case insensitive extension
             	
             	String commonFilenameTag = "";
                 String imageExtension = "";
 
                 if (params.hasKey("image_extension")) {
                     imageExtension = params.getValueForKey("image_extension");
                 }
 
                 if (params.hasKey("common_filename_tag")) {
                     commonFilenameTag = params.getValueForKey("common_filename_tag");
                 }
             	
 
                 if ((f.getName().matches(".*Thumb.*")) || (! f.getName().toLowerCase().matches(".*" + imageExtension.toLowerCase() + "$")) || (! f.getName().matches(".*" + commonFilenameTag + ".*"))) {
                     continue;
                 }
 
                 idLookupByName.put(f.getAbsolutePath(), null);
             }
 
         }
 
         for (String name : idLookupByName.keySet()) {
 
             if (! name.matches(".*" + setTags[0] + ".*")){
                 continue;
             }
 
             ImageSet tempSet = new ImageSet(params);
 
             for (int i = 0; i < numPerSet; i++) {
 
                 //tempSet[i] = directory.getAbsolutePath() + java.io.File.separator + f.getName().replaceAll(setTags[0], setTags[i]);
 
                 String subName = name.replaceAll(setTags[0], setTags[i]);
 
                 if (! idLookupByName.containsKey(subName)) {
                     tempSet = null;
                     break;
                 }
 
                 Long id = idLookupByName.get(subName);
 
 
                 if (id != null) {
                     tempSet.addImageWithOmeroIdAndName(id, subName);
                 } else {
                 	tempSet.addImageWithFilename(subName);
                 }
             }
 
             outputSets.add(tempSet);
         }
 
         return outputSets;
 
     }
 
 }
