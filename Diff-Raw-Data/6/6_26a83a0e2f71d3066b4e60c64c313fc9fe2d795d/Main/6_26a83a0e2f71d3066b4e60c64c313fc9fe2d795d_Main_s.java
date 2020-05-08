 package net.mirky.redis;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public final class Main extends AbstractMain {
 	public static final String VERSION_DATA = "redis 0.1";
 	
 	@Valued("format")
 	@Letter('f')
 	public String formatOverride;
 
 	public Main(String... args) throws CommandLineParseError {
 	    super(args);
     }
 
     public static final void main(String... args) {
 		try {
 	        new Main(args).run();
 		} catch (Throwable e) {
 			System.err.println("redis: "  + e.getMessage());
 			e.printStackTrace(System.err);
 			System.exit(1);
         }
 	}
 
     @Mode("version") @SupremeMode
     public final void version() {
         System.out.println(VERSION_DATA);
         System.exit(0);
     }
 
     @Mode("help") @SupremeMode
     public final void usage() {
         for (String line : new TextResource("usage.txt")) {
         	System.out.println(line);
         }
         System.exit(0);
     }
 
     @Mode("list")
     @Letter('l')
     @ArgCountLimits(min = 1, max = 1)
     public final void list() throws RuntimeException {
         listOrExtract(false);
     }
 
     @Mode("extract")
     @Letter('x')
     @ArgCountLimits(min = 1, max = 1)
     public final void extract() throws RuntimeException {
         listOrExtract(true);
     }
 
     private final void listOrExtract(boolean extract) throws RuntimeException {
         String filename = arguments[0];
         TaggedData object = null;
         try {
             object = loadRootObject(filename);
         } catch (ImageError e) {
             System.err.println("redis: " + e.getMessage());
             System.exit(1);
         } catch (Format.OptionError e) {
             System.err.println("redis: " + e.getMessage());
             System.exit(1);
         }
         assert object != null;
         ReconstructionDataCollector rcn;
         try {
             ByteArrayOutputStream stream = new ByteArrayOutputStream();
             PrintStream port = new PrintStream(stream);
             rcn = object.format.analyser.dis(object.format, object.data, port);
         } catch (ImageError e) {
             System.err.println("redis: " + filename + ": " + e.getMessage());
             System.exit(1);
             throw new RuntimeException("bug detected");
         }
         if (rcn == null || rcn.externalFileOrder.isEmpty()) {
             System.err.println("redis: " + filename + ": no children");
             System.exit(1);
             throw new RuntimeException("bug detected");
         }
         rcn.verifyReconstruction(object.data);
         if (!extract) {
         	rcn.listExternalFiles();
         } else {
         	try {
         		rcn.writeExternalFilesOut(filename + ".rcn");
         	} catch (IOException e) {
         		System.err.println("redis: I/O error");
         		e.printStackTrace(System.err);
         		System.exit(1);
         	}
         }
     }
 
     @Mode("reconstruct")
     @Letter('r')
     @ArgCountLimits(min = 1, max = 1)
     public final void reconstruct() {
         reconstructOrTest(false);
     }
 
     @Mode("test")
     @Letter('t')
     @ArgCountLimits(min = 1, max = 1)
     public final void testReconstruction() {
         reconstructOrTest(true);
     }
 
     private final void reconstructOrTest(boolean testOnly) {
         if (formatOverride != null && formatOverride.length() != 0) {
         	System.err.println("redis: the --format command line option is not applicable in this mode");
         	System.exit(1);
         }
         String filename = arguments[0];
         if (!filename.toLowerCase().endsWith(".rcn")) {
         	System.err.println("redis: " + filename + ": no .rcn suffix detected");
         	System.exit(1);
         }
         String originalFilename = filename.substring(0, filename.length() - 4);
         byte[] image = null;
         try {
         	image = ReconstructionItem.Collector.parse(filename).reconstruct(new ExternalFileLoader.Real());
         } catch (ReconstructionFailure e) {
         	System.err.println("redis: " + e.getMessage());
         	System.exit(1);
         }
         if (testOnly) {
         	System.out.println(originalFilename + " can be successfully reconstructed");
         } else {
         	try {
         		FileOutputStream stream = new FileOutputStream(originalFilename);
         		stream.write(image);
         		stream.close();
         	} catch (IOException e) {
         		System.err.println("redis: I/O error");
         		e.printStackTrace(System.err);
         		System.exit(1);
         	}
         	assert image != null;
         	System.out.println(originalFilename + " " + image.length);
         }
     }
 
     @Mode("hexdump")
     @Letter('h')
     @ArgCountLimits(min = 1, max = 1)
     public final void hexdump() throws ImageError, Format.OptionError {
         String filename = arguments[0];
         hexdump(filename, loadRootObject(filename));
     }
 
     @Mode("dis")
     @Letter('d')
     @ArgCountLimits(min = 1, max = 1)
    public final void dis() throws CommandLineParseError, ImageError, Format.OptionError {
         String filename = arguments[0];
         TaggedData root = loadRootObject(filename);
         dis(filename, root.format, root.data);
     }
 
     @Mode("identify")
     @Letter('i')
     @DefaultMode
     @ArgCountLimits(min = 1, max = 1)
     public final void identify() throws ImageError, Format.OptionError {
         loadFilesizes();
         loadSuffixen();
         String filename = arguments[0];
         assert FILESIZES != null;
         assert SUFFIXEN != null;
         TaggedData root = loadRootObject(filename);
         identify(filename, root, "");
     }
 
     @Mode("dump-lang")
     @ArgCountLimits(min = 1, max = 1)
     public final void dumpLang() {
         if (formatOverride != null && formatOverride.length() != 0) {
             System.err.println("redis: the --format command line option is not applicable in this mode");
             System.exit(1);
         }
         try {
             Disassembler.Lang.get(arguments[0]).dumpLang(System.out);
         } catch (Disassembler.Lang.UnknownLanguage e) {
             System.err.println("redis: " + arguments[0] + ": unknown disassembly language");
             System.exit(1);
         }
     }
     
     @Mode("dump-decoding")
     @ArgCountLimits(min = 1, max = 1)
     public final void dumpDecoding() {
         try {
             Decoding.get(arguments[0]).dumpDecoding(System.out);
         } catch (Decoding.ResolutionError e) {
             System.err.println("redis: " + e.getMessage());
             System.exit(1);
         }
     }
 
     @Mode("list-simple-types")
     @ArgCountLimits(max = 0)
     public final void listSimpleTypes() {
         Format.OptionType.listSimpleTypes();
     }
 
     private final TaggedData loadRootObject(String filename) throws ImageError, Format.OptionError {
         byte[] data = BinaryUtil.loadFile(filename);
         Format format = Format.guess(filename, data);
         System.out.println("Format guessed: " + format.toString());
         if (formatOverride != null) {
             format = format.parseChange(formatOverride);
             System.out.println("Format used: " + format.toString());
         }
         return new TaggedData(data, format);
     }
 
     private final void hexdump(String filename, TaggedData object) {
         System.out.println("Filename: " + filename);
 		System.out.println("Format: " + object.format.toString());
         Analyser analyser = object.format.analyser;
         ReconstructionDataCollector rcn = null;
         try {
             ByteArrayOutputStream stream = new ByteArrayOutputStream();
             PrintStream port = new PrintStream(stream);
             rcn = analyser.dis(object.format, object.data, port);
         } catch (ImageError e) {
             System.out.println("(container format but parsing failed, \"" + e.getMessage() + "\")");
         }
         analyser.hexdump(object.format, object.data, System.out);
         if (rcn == null || rcn.externalFileOrder.isEmpty()) {
         	System.out.println("(no children)");
         } else {
         	System.out.println("(" + rcn.externalFileOrder.size() + " extractables found)");
         	for (String childName : rcn.externalFileOrder) {
         		TaggedData child = rcn.getFile(childName);
         		hexdump(filename + "//" + childName, child);
         	}
         }
         System.out.println();
     }
 
     private final void dis(String filename, Format format, byte[] data) {
         System.out.println("Filename: " + filename);
         System.out.println("Format: " + format.toString());
 		System.out.println();
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
         PrintStream port = new PrintStream(stream);
         ReconstructionDataCollector rcn;
         String error;
 		try {
             rcn = format.analyser.dis(format, data, port);
             error = null;
         } catch (ImageError e) {
             error = e.getMessage();
             rcn = null;
         }
         if (error != null) {
             System.out.println(DisplayUtil.brightRed("(parsing failed: " + error + ")"));
         }
         String disOutput = stream.toString();
         if (disOutput.length() != 0) {
             System.out.println(disOutput);
             if (error != null) {
                 System.out.println(DisplayUtil.brightRed("(parsing incomplete: " + error + ")"));
             }
             System.out.println();
         }
         if (rcn == null || rcn.externalFileOrder.isEmpty()) {
             System.out.println("(" + filename + " has no children)");
         } else {
             System.out.println("(" + rcn.externalFileOrder.size() + " extractables found)");
             for (String childName : rcn.externalFileOrder) {
                 TaggedData object = rcn.getFile(childName);
                 dis((filename + "//" + childName), object.format, object.data);
             }
             System.out.println();
         }
     }
 
     private static Map<Integer, String> FILESIZES = null;
 	
 	private static final void loadFilesizes() {
 		assert FILESIZES == null;
 		FILESIZES = new HashMap<Integer, String>();
 		for (String line : new TextResource("filesizes.txt")) {
 			int sep = line.indexOf(':');
 			if (sep == -1) {
 				throw new RuntimeException("error parsing known filesize list");
 			}
 			Integer size;
 			try {
 				size = new Integer(Integer.parseInt(line.substring(0, sep)));
 			} catch (NumberFormatException e) {
 				throw new RuntimeException("error parsing known filesize list", e);
 			}
 			do {
 				sep++;
 				if (sep == line.length()) {
 					throw new RuntimeException("error parsing known filesize list");
 				}
 			} while (sep < line.length() && line.charAt(sep) == ' ');
 			String name = line.substring(sep);
 			if (size.intValue() < 0 || FILESIZES.containsKey(size)) {
 				throw new RuntimeException("error parsing known filesize list");
 			}
 			FILESIZES.put(size, name);
 		}
 	}
 
 	private static Map<String, ArrayList<String>> SUFFIXEN = null;
 
 	private static final void loadSuffixen() {
 		assert SUFFIXEN == null;
 		SUFFIXEN = loadMultiAssocResource("suffixen.txt");
 	}
 
 	private static final Map<String, ArrayList<String>> loadMultiAssocResource(
 			String resourceName) throws RuntimeException {
 		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
 		for (String line : new TextResource(resourceName)) {
 			int sep = line.indexOf(':');
 			String key = line.substring(0, sep);
 			do {
 				sep++;
 				if (sep == line.length()) {
 					throw new RuntimeException("error parsing resource " + resourceName);
 				}
 			} while (sep < line.length() && line.charAt(sep) == ' ');
 			String value = line.substring(sep);
 			ArrayList<String> list = map.get(key);
 			if (list == null) {
 				list = new ArrayList<String>();
 				map.put(key, list);
 			}
 			list.add(value);
 		}
 		return map;
 	}
 	
 	final void identify(String filename, TaggedData object, String linePrefix) {
 		System.out.println(linePrefix + "Filename: " + filename);
 		int lastDot = filename.lastIndexOf('.');
 		if (lastDot != -1) {
 			String suffix = filename.substring(lastDot).toLowerCase();
 			ArrayList<String> indications = SUFFIXEN.get(suffix);
 			if (indications != null) {
 				for (String i : indications) {
 					System.out.println(linePrefix + "    consistent with " + i);
 				}
 			}
 		}
 		System.out.println(linePrefix + "File size: " + object.data.length + " (0x" + Hex.t(object.data.length) + ") bytes");
 		String indication = FILESIZES.get(new Integer(object.data.length));
 		if (indication != null) {
 			System.out.println(linePrefix + "    consistent with " + indication);
 		}
 		int crc32 = BinaryUtil.crc32(object.data);
 		System.out.println(linePrefix + "CRC32: " + Hex.t(crc32));
 		System.out.println(linePrefix + "MD5: " + BinaryUtil.md5(object.data));
 		System.out.println(linePrefix + "SHA1: " + BinaryUtil.sha1(object.data));
 		System.out.println(linePrefix + "Format: " + object.format.toString());
 		new ByteVectorStatistics(object.data).printOut(linePrefix);
 		System.out.println();
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		PrintStream port = new PrintStream(stream);
 		ReconstructionDataCollector rcn;
 		try {
             rcn = object.format.analyser.dis(object.format, object.data, port);
         } catch (ImageError e) {
         	System.out.println(linePrefix + "    parsing failed, \"" + e.getMessage() + "\")");
         	return;
         }
         if (rcn == null || rcn.externalFileOrder.isEmpty()) {
         	System.out.println("(no children)");
         } else {
         	System.out.println("(" + rcn.externalFileOrder.size() + " child(ren) found)");
         	for (String childName : rcn.externalFileOrder) {
         		TaggedData child = rcn.getFile(childName);
         		identify(filename + "//" + childName, child, linePrefix + "    ");
         	}
         }
 	}
 }
