 package gov.usgs.cida.daymet;
 
 import com.google.common.base.Joiner;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import ucar.ma2.Array;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import ucar.ma2.DataType;
 import ucar.ma2.IndexIterator;
 import ucar.ma2.Range;
 import ucar.nc2.Attribute;
 import ucar.nc2.NetcdfFileWriter;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1D;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDataset;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.unidata.geoloc.ProjectionRect;
 
 
 public class Daymet {
     
     static final Map<String, VariableOut> VARIABLE_MAP;
     
     static {
         Map map = new LinkedHashMap<String, VariableOut>();
         map.put("tmax", new VariableOut("tmax", DataType.BYTE, 0.5d, Byte.MIN_VALUE));
         map.put("tmin", new VariableOut("tmin", DataType.BYTE, 0.5d, Byte.MIN_VALUE));
         map.put("srad", new VariableOut("srad", DataType.SHORT, 0.2d, Short.MIN_VALUE));
         map.put("vp", new VariableOut("vp", DataType.SHORT, 20d, Short.MIN_VALUE));
        map.put("swe", new VariableOut("swe", DataType.SHORT, 20d, Short.MIN_VALUE)); // ?? could fit in unsigned byte
        map.put("prcp", new VariableOut("prcp", DataType.SHORT, 20d, Short.MIN_VALUE)); // ?? could fit in unsigned byte
         map.put("dayl", new VariableOut("dayl", DataType.FLOAT, 1d, Float.valueOf(-9999f)));
         VARIABLE_MAP = Collections.unmodifiableMap(map);
     }
 
 	private static List<String> getTileIdList() throws IOException{
         List<String> list = new ArrayList<String>();
 		BufferedReader reader = null;
         try {
             reader = new BufferedReader(
                 new InputStreamReader(Daymet.class.getResourceAsStream("/tiles.txt")));
             String line;
             while( (line = reader.readLine()) != null) {
                 line = line.trim();
                 if ( !( line.isEmpty() || '#' == line.charAt(0) ) ) {
                     list.add(line);
                 }
             }
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         return list;
     }
     
     public static void traverse(File srcDirectory, File dstDirectory, List<Integer> years, List<String> variables, int tStep) throws IOException, InvalidRangeException {
         if (!dstDirectory.exists()) {
             if (!dstDirectory.mkdirs()) {
                 throw new IllegalArgumentException(dstDirectory.getPath() + " does not exist and could not be created");
             }
         }
         if (!srcDirectory.exists()) {
             throw new IllegalArgumentException(srcDirectory.getPath() + " does not exist.");
         }
         Collection<String> varsUnknown = new ArrayList<String>(variables);
         varsUnknown.removeAll(VARIABLE_MAP.keySet());
         if (!varsUnknown.isEmpty()) {
             throw new IllegalArgumentException("Uknown Variables: " + Joiner.on(", ").join(varsUnknown));
         }
         for (Integer year : years) {
             try {
                 for (String variable : variables) {
                     TileList tileList = getTileListFromDirectory(srcDirectory, year, variable);
                     if (tileList != null && tileList.size() > 0) {
                         VariableOut variableOut = VARIABLE_MAP.get(variable);
                         try {
                             int tCount = tileList.getTiles().get(0).getGridDatatype().getTimeDimension().getLength(); 
                             if (tCount > 365) {
                                 tCount = 365; // Revert MOWS 365->366 day hack.
                             }
                             for (int tIndex = 0; tIndex < tCount ; tIndex += tStep) {
                                 long t0 = System.currentTimeMillis();
                                 int tMin = tIndex ;
                                 int tMax = tIndex + tStep;
                                 if (tMax > tCount) {
                                     tMax = tCount;
                                 }
                                 String ncName = generateNetCDFOutputFileName(year, tIndex/tStep, variable);
                                 File ncFile = new File(dstDirectory, ncName);
                                 System.out.println("starting " + ncFile.getPath() + " [" + tMin + ":" + tMax + "]") ;
                                 writeFile(ncFile.getPath(), tileList, variableOut, new Range(tMin, tMax - 1));  // - 1 since upper bound is inclusive
                                 System.out.println("finished " + ncFile.getPath() + ": " + ((double)(System.currentTimeMillis() - t0) / 1000d) + "s");
                             }
                         } finally {
                             if (tileList != null) {
                                 tileList.dispose();
                             }
                         }
                     } else {
                         System.err.println("No tiles found in " + srcDirectory.getPath() + " for year: " + year + "| variable: "  + variable);
                     }
                 }
             } catch (NumberFormatException e) {
                 // TODO: log
             }
         }
     }
     
     public static void writeFile(String fileName, TileList tileList, VariableOut variable, Range timeInRange) throws IOException, InvalidRangeException {
         NetcdfFileWriter writer = Daymet.createFile(fileName, tileList, variable, timeInRange);
         Variable outputVariable = writer.findVariable(variable.name);
         int[] outputArrayShape = { 1, outputVariable.getShape(1), outputVariable.getShape(2) };
         Range.Iterator timeInRangeIterator = timeInRange.getIterator();
         int timeOutIndex = 0;
         while(timeInRangeIterator.hasNext()) {
             int timeInIndex = timeInRangeIterator.next();
             Array outputArray = Array.factory(outputVariable.getDataType(), outputArrayShape);
             fillMissingQuick(outputArray, variable.missingValue);
             for (Daymet.Tile tile : tileList.getTiles()) {
                 Array tileArray = tile.getGridDatatype().readVolumeData(timeInIndex);
                 if (tileArray.getSize() > 1) {
                     List<Range> tileSectionRanges = new ArrayList<Range>(3);
                     tileSectionRanges.add(new Range(0, 0));
                     tileSectionRanges.addAll(tileList.getSectionRangeYX(tile));
                     Array sectionedArray  = outputArray.section(tileSectionRanges);
                     if (sectionedArray.getSize() == tileArray.getSize()) {
                         IndexIterator sectionIterator = sectionedArray.getIndexIterator();
                         IndexIterator tileIterator = tileArray.getIndexIterator();
                         while (sectionIterator.hasNext() && tileIterator.hasNext()) {
                             float t = tileIterator.getFloatNext();
                             if (t == t) {
                                 sectionIterator.setObjectNext(Double.valueOf(t / variable.scale));
                             } else {
                                 sectionIterator.next();
                             }
                         }
                     } else {
                         System.out.println(" skipped tileid " + tile.getNetCDFFile().findAttValueIgnoreCase(null, "tileid", "unknown"));
                     }
                 }
             }
             writer.write(outputVariable, new int[] { timeOutIndex++, 0, 0 }, outputArray);
         }
         writer.close();
     }
     
     public static NetcdfFileWriter createFile(String fileName, TileList tileList, VariableOut variableOut, Range tRange) throws IOException, InvalidRangeException {
         NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);
         
         ProjectionRect projectionRect = tileList.getProjectionRect();
         Tile tile = tileList.getTiles().get(0);
         NetcdfFile ncFile = tile.getNetCDFFile();
         
         List<Dimension> oDimensions = ncFile.getDimensions();
         oDimensions.remove(ncFile.findDimension("x"));
         oDimensions.remove(ncFile.findDimension("y"));
         oDimensions.remove(ncFile.findDimension("time"));
         for (Dimension oDimension: oDimensions) {
             writer.addDimension(null, oDimension.getName(), oDimension.getLength());
         }
         writer.addDimension(null, "x", (int)projectionRect.getWidth());
         writer.addDimension(null, "y", (int)projectionRect.getHeight());
         writer.addDimension(null, "time", tRange.length());
         
         for (Variable oVariable : ncFile.getVariables()) {
             String oVariableName = oVariable.getShortName();
             if (variableOut.name.equals(oVariableName)) {
                 // detects grid variable, transform variable DataType along with
                 // the "missing_value" attribute, adds "scale_factor" attribute
                 Variable nVariable = writer.addVariable(null,
                         oVariableName,
                         variableOut.dataType,
                         oVariable.getDimensionsString());
                 for (Attribute oAttribute : oVariable.getAttributes()) {
                     String oAttributeName = oAttribute.getName();
                     if ( !( '_' == oAttributeName.charAt(0) ||
                             "missing_value".equals(oAttributeName))) {
                         writer.addVariableAttribute(nVariable, oAttribute);
                     }
                 }
                 nVariable.addAttribute(new Attribute("missing_value", variableOut.missingValue));
                 if (variableOut.scale != 1) {
                     nVariable.addAttribute(new Attribute("scale_factor", Double.valueOf(variableOut.scale)));
                 }
             } else if ( "yearday".equals(oVariableName)) {
                 // Revert MOWS 365->366 hack, rewrite "valid_range" attribute
                 Variable nVariable = writer.addVariable(null,
                         oVariableName,
                         oVariable.getDataType(),
                         oVariable.getDimensionsString());
                 for (Attribute oAttribute : oVariable.getAttributes()) {
                     String oAttributeName = oAttribute.getName();
                     if ( !( '_' == oAttributeName.charAt(0) || "valid_range".equals(oAttributeName) ) ) {
                         writer.addVariableAttribute(nVariable, oAttribute);
                     }
                 }
                 nVariable.addAttribute(new Attribute("valid_range", Arrays.asList(new Short[] { Short.valueOf((short)1), Short.valueOf((short)365) })));
             } else if ( !( "lat".equals(oVariableName) || "lon".equals(oVariableName) ) ) {
                 // pass through everything else except "lat" and "lon" variables as
                 // these aren't needed.
                 Variable nVariable = writer.addVariable(null,
                         oVariableName,
                         oVariable.getDataType(),
                         oVariable.getDimensionsString());
                 for (Attribute oAttribute : oVariable.getAttributes()) {
                     String oAttributeName = oAttribute.getName();
                     if ('_' != oAttributeName.charAt(0)) {
                         writer.addVariableAttribute(nVariable, oAttribute);
                     }
                 }
             }
         }
         for (Attribute oAttribute : ncFile.getGlobalAttributes()) {
             String oAttributeName = oAttribute.getName();
             // filter out year or tile specific attributes.
             if ( !( '_' == oAttributeName.charAt(0) ||
                     "tileid".equals(oAttributeName) || 
                     "start_year".equals(oAttributeName) ||
                     "start_yday".equals(oAttributeName)) ) {
                 writer.addGroupAttribute(null, oAttribute);
             }
         }
         writer.create();
         
         Variable xVariable = writer.findVariable("x");
         Variable yVariable = writer.findVariable("y");
         Variable tVariable = writer.findVariable("time");
         Variable tbVariable = writer.findVariable("time_bnds");
         Variable ydVariable = writer.findVariable("yearday");
         
         // projection rect is in 'km' we need to write 'm'.  Additionally, the 
         // fact that the data grid is 1 km^2 makes the converion simple. 
         writer.write(xVariable, Array.makeArray(xVariable.getDataType(), (int)projectionRect.getWidth(), projectionRect.getMinX() * 1000d, 1000d));
         writer.write(yVariable, Array.makeArray(yVariable.getDataType(), (int)projectionRect.getHeight(), projectionRect.getMaxY() * 1000d, -1000d));
         
         writer.write(tVariable, ncFile.findVariable("time").read(Arrays.asList(new Range[] { tRange })));
         writer.write(tbVariable, ncFile.findVariable("time_bnds").read(Arrays.asList(new Range[] { tRange, null /* 'null' means all */ })));
         writer.write(ydVariable, ncFile.findVariable("yearday").read(Arrays.asList(new Range[] { tRange })));
         writer.flush();
         
         return writer;
     }
     
     public static TileList getTileListFromDirectory(File file, int year, String variable) throws IOException {
         if (!file.isDirectory()) {
             throw new IllegalStateException(file.getPath() + " is not directory.");
         }
         TileList tileList = new TileList(variable);
         for (String tileId : getTileIdList()) {
             // assumes  [source directory]/year/tileid/[filename].nc
             File candidate = new File(new File( new File(file, Integer.toString(year)), tileId), generateNetCDFInputFileName(tileId, year, variable));
             if (candidate.exists()) {
                 tileList.add(candidate);
             }
         }
         return tileList;
     }
     
     public static String generateNetCDFInputFileName(String tileId, int year, String variable) {
         return String.format("%s_%d_%s.nc", tileId, year, variable);
     }
     
     public static String generateNetCDFInputFileName(int tileId, int year, String variable) {
         return generateNetCDFInputFileName(Integer.toString(tileId), year, variable);
     }
     
     public static String generateNetCDFOutputFileName(int year, int index, String variable) {
         return String.format("%d-%02d_%s.nc", year, index, variable);
     }
     
     
     public static class Tile {
         FeatureDataset featureDataset = null;
         GridDatatype gridDatatype = null;
         ProjectionRect projectionRect = null;
         Tile(File file, String variable) throws IOException {
             featureDataset = FeatureDatasetFactoryManager.open(FeatureType.GRID, file.getPath(), null, new Formatter(System.err));
             if (featureDataset instanceof GridDataset) {
                 featureDataset.calcBounds();
                 GridDataset gds = (GridDataset)featureDataset;
                 gridDatatype = gds.findGridDatatype(variable);
                 if (gridDatatype != null) {
                     /* call below fails with 1x1 grids */
                     //projectionRect = gridDatatype.getCoordinateSystem().getBoundingBox();
                     GridCoordSystem gcs = gridDatatype.getCoordinateSystem();
                     CoordinateAxis xa = gcs.getXHorizAxis();
                     CoordinateAxis ya = gcs.getYHorizAxis();
                     if (xa instanceof CoordinateAxis1D && ya instanceof CoordinateAxis1D) {
                         projectionRect = new ProjectionRect(
                                 xa.getMinValue(), ya.getMinValue(),
                                 xa.getMaxValue(), ya.getMaxValue());
                     } else {
                         throw new IllegalArgumentException("Unable to handle x or y coordinate axes with greater than 1 dimension(s)");
                     }
                 } else {
                     throw new IllegalArgumentException("Unable to extract Grid for " + variable);
                 }
             } else {
                 throw new IllegalArgumentException("Unexpected FeatureType encountered");
             }
         }
         
         public ProjectionRect getProjectionRect() {
             return projectionRect;
         }
         
         public NetcdfFile getNetCDFFile() {
             return featureDataset == null ? null : featureDataset.getNetcdfFile();
         }
         
         public GridDatatype getGridDatatype() {
             return gridDatatype;
         }
         
         public void dispose() {
             if (featureDataset != null) {
                 try {
                     featureDataset.close();
                 } catch (IOException ignore) { }
                 featureDataset = null;
             }
         }
     }
     
     public static class TileList {
         String variable;
         List<Tile> tileList;
         ProjectionRect projectionRect;
         
         public TileList(String variable) {
             this.variable = variable;
             tileList = new ArrayList<Tile>();
         }
         
         public void add(File file) throws IOException {
             Tile tile = new Tile(file, variable);
             if (projectionRect == null) {
                 projectionRect = new ProjectionRect(tile.getProjectionRect());
             } else {
                 projectionRect.add(tile.getProjectionRect());
             }
             tileList.add(tile);
         }
         
         public List<Tile> getTiles() {
             return Collections.unmodifiableList(tileList);
         }
         
         public ProjectionRect getProjectionRect() {
             return projectionRect;
         }
         
         public void dispose() {
             if (tileList != null) {
                 for (Tile tile : tileList) {
                     tile.dispose();
                 }
                 tileList.clear();
                 projectionRect = null;
             }
         }
         
         public List<Range> getSectionRangeYX(Tile tile) throws InvalidRangeException {
             List<Range> sectionYX = new ArrayList<Range>(2);
             
             // use offset from max since step is < 0 (as index increases, value decreases) 
             int ySetMax = (int)projectionRect.getMaxY();
             int yTileMax = (int)tile.projectionRect.getMaxY();
             int yTileOffset = ySetMax - yTileMax;
             sectionYX.add(new Range(yTileOffset, yTileOffset + (int)tile.projectionRect.getHeight()));
             
             // use offset from min since step is > 0 (as index increases, value increases)
             int xSetMin = (int)projectionRect.getMinX();
             int xTileMin = (int)tile.projectionRect.getMinX();
             int xTileOffset = xTileMin - xSetMin;
             sectionYX.add(new Range(xTileOffset, xTileOffset + (int)tile.projectionRect.getWidth()));
 
             return sectionYX;
         }
         
         public int size() {
             return tileList == null ? 0 : tileList.size();
         }
     }
     
     public static class VariableOut {
         public final String name;
         public final DataType dataType;
         public final double scale;
         public final Number missingValue;
         VariableOut(String name, DataType dataType, double scale, Number missingValue) {
             this.name = name;
             this.dataType = dataType;
             this.scale = scale;
             this.missingValue = missingValue;
         }
     }
     
     private static void fillMissingQuick(Array array, Number missingValue) {
         Object o = array.getStorage();
         if (o instanceof float[]) {
             Arrays.fill((float[])o, missingValue.floatValue());
         } else if (o instanceof short[]) {
             Arrays.fill((short[])o, missingValue.shortValue());
         } else if (o instanceof byte[]) {
             Arrays.fill((byte[])o, missingValue.byteValue());
         } else {
             throw new UnsupportedOperationException("fillMissingQuick(...) not implemented for this array type");
         }
     }
 
     public static void main(String[] args) {
         if (args.length == 0) {
             printUsageAndExit();
             System.exit(-1);
         }
         try {
             
             Integer yearMin = null;
             Integer yearMax = null;
             List<String> variables = null;
             Integer timeSteps = null;
             String srcPath = null;
             String dstPath = null;
             
             for (String arg : args) {
                 if (arg.startsWith("-y=") && arg.length() > 3) {
                     String[] split = arg.substring(3).split(":");
                     try {
                         if (split.length == 1) {
                             yearMin = yearMax = Integer.valueOf(split[0]);
                         } else if (split.length == 2) {
                             yearMin = Integer.valueOf(split[0]);
                             yearMax = Integer.valueOf(split[1]);
                         }
                     } catch (NumberFormatException e) {
                         printUsageAndExit();
                     }
                 } else if (arg.startsWith("-v=") && arg.length() > 3) {
                     String[] split = arg.substring(3).split(",");
                     variables = Arrays.asList(split);
                 } else if (arg.startsWith("-t=") && arg.length() > 3) {
                     try {
                         timeSteps = Integer.valueOf(arg.substring(3));
                     } catch (NumberFormatException e) {
                         printUsageAndExit();
                     }
                 } else if (arg.startsWith("-s=") && arg.length() > 3) {
                     srcPath = arg.substring(3);
                 } else if (arg.startsWith("-d=") && arg.length() > 3) {
                     dstPath = arg.substring(3);
                 } else {
                     printUsageAndExit("Argument unknown: " + arg);
                 }
             }
             
             if (yearMin == null || yearMax == null || yearMin > yearMax || yearMin < 1980 || yearMax > 2100) {
                 printUsageAndExit("Invalid year specification");
             }
             int yearCount = yearMax - yearMin + 1;  // +1 since upper-bound is inclusive
             List<Integer> years = new ArrayList(yearCount);
             for (int yearIndex = 0; yearIndex < yearCount; yearIndex++) {
                 years.add(yearMin + yearIndex);
             }
             
             if (variables == null) {
                 variables = new ArrayList<String>(VARIABLE_MAP.keySet());
             } else {
                 Collection<String> varsUnknown = new ArrayList<String>(variables);
                 varsUnknown.removeAll(VARIABLE_MAP.keySet());
                 if (!varsUnknown.isEmpty()) {
                     printUsageAndExit("Variable(s) unknown: " + Joiner.on(", ").join(varsUnknown));
                 }
             }
             
             if (timeSteps == null) {
                 timeSteps = 4;
             } else {
                 if (timeSteps < 1) {
                     printUsageAndExit("Invalid timestep: " + timeSteps + ", must be greater than 1");
                 }
             }
             
             if (srcPath == null) {
                 srcPath = ".";
             }
             if (dstPath == null) {
                 dstPath = ".";
             }
             File srcDir = new File(srcPath);
             if (!srcDir.canRead()) {
                 printUsageAndExit("Directory unreadable: " + srcPath);
             }
             File dstDir = new File(dstPath);
             if (!dstDir.exists()) {
                 if (dstDir.mkdirs()) {
                     printUsageAndExit("Unable to create destination directory");
                 }
             }
             
             System.out.println("Traversing...");
             System.out.println(" years:      " + Joiner.on(", ").join(years));
             System.out.println(" variables:  " + Joiner.on(", ").join(variables));
             System.out.println(" timesteps:  " + timeSteps);
             System.out.println(" source dir: " + srcPath);
             System.out.println(" dest dir:   " + dstPath);
 
             traverse(srcDir, dstDir, years, variables, timeSteps);
             
         } catch (Exception e) {
             e.printStackTrace(System.err);
             printUsageAndExit();
         }
     }
  
     public static void printUsageAndExit(String message) {
         System.err.println(message);
         System.err.println();
         printUsageAndExit();
     }
     
     public static void printUsageAndExit() {
         System.err.println("java -jar daymet.jar -y=start_year[:end_year] [-v=var1,var2,...,varN] [-t=time_steps] [-s=source_dir] [-d=destination_dir]");
         System.exit(-1);
     }
     
  }
