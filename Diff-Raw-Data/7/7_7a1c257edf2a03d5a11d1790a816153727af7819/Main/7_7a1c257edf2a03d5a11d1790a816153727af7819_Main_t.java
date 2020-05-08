 package mcmapconverter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Set;
 import mcmaplib.*;
 
 public class Main {
     private static void printHelp(PrintStream out) {
        out.println("Usage:\n"
                  + "  inputFile outputFile\n"
                  + "  -in <format> -out <format> inputFile outputFile\n"
                  + "      Convert inputFile to outputFile\n"
                  + "  -formats\n"
                  + "      List file formats\n"
                  + "  -help\n"
                  + "      Command help");
     }
 
     private static void listFormats(PrintStream out, MapFormatDetector detector) {
         Iterable<MapFormat> formats;
 
         formats = detector.getMapFormats();
         out.println("Map formats:");
         for(MapFormat format : formats) {
             out.println(format.getName() + " - " + format.getDescription());
         }
     }
     
     public static void main(String[] args) {
         String inFormatName = null, outFormatName = null,
                inFileName = null, outFileName = null;
         MapFormat inFormat, outFormat;
         File inFile, outFile;
         MinecraftMap inMap, outMap;
         MapFormatDetector detector;
         
         try {
             detector = new MapFormatDetector();
             detector.addMapFormat(new DatMapFormat());
             detector.addMapFormat(new MCSharpMapFormat());
             detector.addMapFormat(new RUMMapFormat());
            detector.addMapFormat(new FCraftMapFormat());
             try {
                 for(int i = 0;i < args.length;i++) {
                     String arg;
 
                     arg = args[i];
                     if(arg.equals("-in")) {
                         inFormatName = args[++i];
                     } else if(arg.equals("-out")) {
                         outFormatName = args[++i];
                     } else if(arg.equals("-help")) {
                         printHelp(System.out);
                         System.exit(0);
                     } else if(arg.equals("-formats")) {
                         listFormats(System.out, detector);
                         System.exit(0);
                     } else if(inFileName == null) {
                         inFileName = arg;
                     } else if(outFileName == null) {
                         outFileName = arg;
                     } else {
                         throw new BadArgumentsException("Unknown argument.");
                     }
                 }
                 if(inFileName == null)
                     throw new BadArgumentsException("No input file specified");
                 if(outFileName == null)
                     throw new BadArgumentsException("no output file specified");
             } catch(ArrayIndexOutOfBoundsException e) {
                 throw new BadArgumentsException("Missing argument", e);
             }
 
             inFile = new File(inFileName);
             outFile = new File(outFileName);
 
             if(inFormatName == null) {
                 Set<MapFormat> formats;
                 
                 formats = detector.getMapFormats(inFile);
                 if(formats == null || formats.size() != 1) {
                     throw new MapConverterException("Could not detect input map format"
                             + " from input file extension,"
                             + " please specify an input file format with -in <format>");
                 }
                 inFormat = formats.iterator().next();
             } else
                 inFormat = detector.getMapFormat(inFormatName);
 
             if(inFormatName == null) {
                 Set<MapFormat> formats;
 
                 formats = detector.getMapFormats(outFile);
                 if(formats == null || formats.size() != 1) {
                     throw new MapConverterException("Could not detect output map format"
                             + " from output file extension,"
                             + " please specify an output file format with -out <format>");
                 }
                 outFormat = formats.iterator().next();
             } else
                 outFormat = detector.getMapFormat(outFormatName);
 
             try {
                 inMap = inFormat.load(inFile);
             } catch(FileNotFoundException e) {
                 throw new MapConverterException("Input file not found.", e);
             } catch(NotImplementedException e) {
                throw new MapConverterException("Converting from that map format is not supported: " + e.getMessage(), e);
             } catch(MapFormatException e) {
                 throw new MapConverterException("Input map file corrupt: " + e.getMessage(), e);
             } catch(IOException e) {
                 throw new MapConverterException("Error loading input map file: " + e.getMessage(), e);
             }
 
             try {
                 outMap = outFormat.convert(inMap);
             } catch(InvalidMapException e) {
                 throw new MapConverterException("Error converting map: " + e.getMessage(), e);
             }
 
             try {
                 outMap.save(outFile);
             } catch(NotImplementedException e) {
                throw new MapConverterException("Converting to that map format is not supported: " + e.getMessage(), e);
             } catch(IOException e) {
                 throw new MapConverterException("Error saving map file: " + e.getMessage(), e);
             }
         } catch(BadArgumentsException e) {
             System.err.println(e.getMessage());
             printHelp(System.err);
         } catch(MapConverterException e) {
             System.err.println(e.getMessage());
         }
     }
 }
 
 class MapConverterException extends Exception {
     public MapConverterException(String s) {
         super(s);
     }
 
     public MapConverterException(Throwable e) {
         super(e);
     }
 
     public MapConverterException(String s, Throwable e) {
         super(s, e);
     }
 
     public MapConverterException() {
         super();
     }
 }
 
 class BadArgumentsException extends MapConverterException {
     public BadArgumentsException(String s) {
         super(s);
     }
 
     public BadArgumentsException(Throwable e) {
         super(e);
     }
 
     public BadArgumentsException(String s, Throwable e) {
         super(s, e);
     }
 
     public BadArgumentsException() {
         super();
     }
 }
