 package gov.usgs.cida.watersmart.parse;
 
 import com.google.common.collect.Maps;
 import gov.usgs.cida.netcdf.dsg.Observation;
 import gov.usgs.cida.netcdf.dsg.RecordType;
 import gov.usgs.cida.netcdf.dsg.Station;
 import gov.usgs.cida.netcdf.dsg.StationTimeSeriesNetCDFFile;
 import gov.usgs.cida.watersmart.common.JNDISingleton;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.parse.column.AFINCHParser;
 import gov.usgs.cida.watersmart.parse.column.PRMSParser;
 import gov.usgs.cida.watersmart.parse.file.STATSParser;
 import gov.usgs.cida.watersmart.parse.file.SYEParser;
 import gov.usgs.cida.watersmart.parse.file.WATERSParser;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import javax.xml.stream.XMLStreamException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.NotImplementedException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CreateDSGFromZip {
     
     private static final Logger LOG = LoggerFactory.getLogger(CreateDSGFromZip.class);
     
     public static class ReturnInfo {
         public Collection<Station> stations;
         public List<String> properties;
         public String filename;
     }
     
     public static ReturnInfo create(File srcZip, RunMetadata runMeta) throws IOException, XMLStreamException {
         // Need to put the resulting NetCDF file somewhere that ncSOS knows about
         String sosPath = JNDISingleton.getInstance().getProperty("watersmart.sos.location", System.getProperty("java.io.tmpdir"));
         
         FileUtils.forceMkdir(new File(sosPath));
         
         File verifiedSrcZip = verifyZip(srcZip);
         String filename = verifiedSrcZip.getName().replace(".zip", ".nc");
         
         File ncFile = new File(sosPath + File.separator + runMeta.getTypeString() +
                                File.separator + filename);
        
        // Make sure the directory gets created if it doesn't exist
        FileUtils.forceMkdir(new File(ncFile.getParent()));
         LOG.debug(ncFile.getName() + " will be saved to " + sosPath);
        
         ZipFile zip = new ZipFile(verifiedSrcZip);
         Enumeration<? extends ZipEntry> entries = zip.entries();
         StationTimeSeriesNetCDFFile nc = null;
         
         // Get station wfs used for model
         
         StationLookup lookerUpper = new WFSPointStationLookup(runMeta);
         Collection<Station> stations = lookerUpper.getStations();
         ReturnInfo info = new ReturnInfo();
         info.stations = stations;
         info.filename = filename;
         
         while (entries.hasMoreElements()) {
             ZipEntry entry = entries.nextElement();
             LOG.debug(entry.getName());
             if (!entry.isDirectory()) {
                 InputStream inputStream = zip.getInputStream(entry);
                 DSGParser dsgParse = null;
                 switch (runMeta.getType()) {
                     case SYE: 
                         dsgParse = new SYEParser(inputStream, entry.getName(), lookerUpper);
                         break;
                     case WATERS:
                         dsgParse = new WATERSParser(inputStream, lookerUpper);
                         break;
                     case AFINCH:
                         dsgParse = new AFINCHParser(inputStream, lookerUpper);
                         break;
                     case WATERFALL:
                         // TODO make sure Waterfall uses SYEParser
                         dsgParse = new AFINCHParser(inputStream, lookerUpper);
                         break;
                     case STATS:
                         dsgParse = new STATSParser(inputStream, lookerUpper);
                         break;
                     case PRMS:
                         Matcher prmsMatcher = PRMSParser.prmsFileNamePattern.matcher(entry.getName());
                         if (prmsMatcher.matches()) {
                             InputStream statvarInStream = inputStream;
                             String expectedParamFile = prmsMatcher.group(1) + ".param";
                             ZipEntry paramEntry = zip.getEntry(expectedParamFile);
                             if (paramEntry == null) {
                                 LOG.error("could not find matching file: " + expectedParamFile);
                                 continue;
                             }
                             InputStream paramInStream = zip.getInputStream(paramEntry);
                             dsgParse = new PRMSParser(statvarInStream, paramInStream, lookerUpper);
                         }
                         else {
                             continue;
                         }
                         break;
                     case PRMS2:
                         dsgParse = new STATSParser(inputStream, lookerUpper);
                         break;
                     default:
                         throw new NotImplementedException("Parser not written yet");
                 }
 
                 // must parse Metadata for each file
                 RecordType meta = dsgParse.parse();
                 
                 // first file sets the rhythm
                 if (nc == null) {
                     info.properties = meta.getDataVarNames();
                     Station[] stationArray = stations.toArray(new Station[stations.size()]);
                     Map<String,String> globalAttrs = applyBusinessRulesToMeta(runMeta);
                     
                     nc = new StationTimeSeriesNetCDFFile(ncFile, meta, globalAttrs, true, stationArray);
                 }
                 while (dsgParse.hasNext()) {
                     Observation ob = dsgParse.next();
                     if (null != ob) {
                         nc.putObservation(ob);
                     }
                     else {
                         IOUtils.closeQuietly(inputStream);
                         break;
                     }
                 }
                 IOUtils.closeQuietly(inputStream);
             }
         }
         IOUtils.closeQuietly(nc);
         return info;
     }
     
     // TODO - Currently doing nothing. Ideally we want to make sure zip will get unzipped
     static File verifyZip(File zipFile) throws ZipException, IOException {
 //        File workDirectory = new File(System.getProperty("java.io.tmpdir") + File.separatorChar + );
 //        File tempFile = File.createTempFile("verifyZip-DeleteMe", "zip");
 //        FileUtils.copyFile(zipFile, tempFile);
 //        ZipFile zip = new ZipFile(tempFile);
         
         return zipFile;
     }
     
     private static Map<String, String> applyBusinessRulesToMeta(RunMetadata meta) {
         Map<String,String> globalAttrs = Maps.newLinkedHashMap();
         String title = "WaterSMART Intercomparison Portal - " + meta.getType().toString() +
                        " - " + meta.getScenario() + " - " + meta.getModelVersion() +
                        "." + meta.getRunIdent();
         String id = "watersmart." + meta.getType().toString() + "." + meta.getScenario() + 
                     "." + meta.getModelVersion() + "." + meta.getRunIdent();
         
         globalAttrs.put("title", title);
         globalAttrs.put("summary", meta.getComments());
         globalAttrs.put("id", id);
         globalAttrs.put("naming_authority", "gov.usgs.cida");
         globalAttrs.put("cdm_data_type", "Station");
         globalAttrs.put("date_created", meta.getCreationDate());
         globalAttrs.put("creator_name", meta.getName());
         globalAttrs.put("creator_email", meta.getEmail());
         globalAttrs.put("project", "WaterSMART Water Census");
         globalAttrs.put("processing_level", "Model Results");
         globalAttrs.put("standard_name_vocabulary", RecordType.CF_VER);
         return globalAttrs;
     }
 }
