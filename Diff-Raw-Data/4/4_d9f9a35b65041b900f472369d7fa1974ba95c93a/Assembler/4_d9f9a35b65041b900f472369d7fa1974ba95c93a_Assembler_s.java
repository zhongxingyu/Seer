 package com.wolfesoftware.mipsos.assembler;
 
 import java.io.*;
 import java.util.*;
 
 import com.wolfesoftware.mipsos.common.*;
 
 //http://www.d.umn.edu/~gshute/spimsal/talref.html
 //http://6004.csail.mit.edu/6.371/handouts/mips6371.pdf
 
 public class Assembler
 {
     private static final String blankBinAddr = "          ";
     private static final String blankBinWord = "                  ";
 
     /**
      * calls assemble() with options gathered from the command-line arguments.
      * See printUsage().
      */
     public static void main(String[] args)
     {
         // get options from args
         LinkedList<String> argList = Util.arrayToLinkedList(args);
         AssemblerOptions options = new AssemblerOptions();
         options.parse(argList);
         if (argList.size() != 1)
             throw new RuntimeException();
         String inputPath = argList.getFirst();
 
         // call the assemble function
         try {
             assemble(inputPath, options);
         } catch (AssemblingException e) {
             System.out.print(e.toString());
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static byte[] assembleToBytes(String inputPath, AssemblerOptions options) throws AssemblingException, IOException
     {
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         options.outStream = outStream;
         assemble(inputPath, options);
         return outStream.toByteArray();
     }
     public static void assemble(String inputPath, AssemblerOptions options) throws AssemblingException, IOException
     {
         InputStream inStream = new FileInputStream(inputPath);
         // read input stream
         Scanner inScanner = new Scanner(inStream);
         StringBuilder fullSourceBuilder = new StringBuilder();
         ArrayList<Integer> lineIndecies = new ArrayList<Integer>();
         // convert all newlines to '\n'
         if (inScanner.hasNextLine()) { // do the first one specially with no "\n" at the beginning
             lineIndecies.add(fullSourceBuilder.length());
             fullSourceBuilder.append(inScanner.nextLine());
         }
         while (inScanner.hasNextLine()) {
             lineIndecies.add(fullSourceBuilder.length());
            fullSourceBuilder.append('\n').append(inScanner.nextLine());
         }
         String fullSource = fullSourceBuilder.toString();
 
         // tokenize
         Token.TokenBase[] tokens;
         try {
             tokens = Tokenizer.tokenize(fullSource);
         } catch (TokenizingException e) {
             int srcLocation = e.srcLocation;
             int line = Util.findInList(lineIndecies, srcLocation);
             int col = srcLocation - lineIndecies.get(line);
             throw new CompilingException(srcLocation, line + 1, col + 1, 1, e.message);
         }
 
         // parse
         Parser.Binarization binarization;
         try {
             binarization = Parser.parse(tokens, options.dataAddress, options.textAddress);
         } catch (ParsingException e) {
             Token.TokenBase token = tokens[e.tokenIndex];
             int startLine = Util.findInList(lineIndecies, token.srcStart);
             throw new CompilingException(token.srcStart, startLine + 1, token.srcStart - lineIndecies.get(startLine), token.srcEnd - token.srcStart, e.message);
         }
 
         // collect all the required labels
         BinTreeSet<String> requiredLabels = new BinTreeSet<String>();
         requiredLabels.add("main");
         for (Bin.BinBase binElem : binarization.dataElems)
             for (String s : binElem.getLabelDependencies())
                 requiredLabels.add(s);
         for (Bin.BinBase binElem : binarization.textElems)
             for (String s : binElem.getLabelDependencies())
                 requiredLabels.add(s);
         // collect all defined labels
         BinTreeSet<String> definedLabels = new BinTreeSet<String>();
         definedLabels.addAll(binarization.labels.keySet());
         // determine missing ones
         ArrayList<String> missingLabels = requiredLabels.complement(definedLabels);
         // check that all labels are defined
         if (!(missingLabels.isEmpty()))
             throw new UndefinedLabelsException(missingLabels); // report missing labels
 
         // output
         if (options.readable) {
             // header
             PrintStream printStream = new PrintStream(options.outStream);
             printStream.println("; header");
             byte[] bytes = binarization.header.getBinary(binarization.labels, -1);
             int wordCounter = 0;
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .data Offset");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .data Address");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .data Length");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .text Offset");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .text Address");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; .text Length");
             printStream.println(blankBinAddr + bytesWordToString(bytes, 4 * wordCounter++) + " ; executable entry point");
 
             // .data section
             verboseOutput(binarization.dataElems, printStream, ".data", options.dataAddress, binarization.labels, true, tokens, fullSource);
 
             // .text section
             verboseOutput(binarization.textElems, printStream, ".text", options.textAddress, binarization.labels, true, tokens, fullSource);
         } else {
             ArrayList<Segment> segments = new ArrayList<Segment>();
             DebugInfo debugInfo = options.debugInfo ? new DebugInfo(inputPath, binarization.labels) : null;
 
             // .data section
             ByteArrayOutputStream dataSection = new ByteArrayOutputStream();
             nonverboseOutput(options.dataAddress, binarization.dataElems, binarization.labels, dataSection);
             segments.add(makeMemorySegment(binarization.header.dataAddr, dataSection.toByteArray()));
             if (debugInfo != null)
                 debugInfo.write(options.dataAddress, binarization.dataElems, binarization.labels, lineIndecies, tokens);
 
             // .text section
             ByteArrayOutputStream textSection = new ByteArrayOutputStream();
             nonverboseOutput(options.textAddress, binarization.textElems, binarization.labels, textSection);
             segments.add(makeMemorySegment(binarization.header.textAddr, textSection.toByteArray()));
             if (debugInfo != null)
                 debugInfo.write(options.textAddress, binarization.textElems, binarization.labels, lineIndecies, tokens);
 
             // debug info segment
             if (debugInfo != null)
                 segments.add(debugInfo.toSegment());
 
             // entrypoint segment
             int entryPoint = binarization.labels.get("main").intValue();
             segments.add(makeEntrypointSegment(entryPoint));
 
             Segment[] segmentsArray = segments.toArray(new Segment[segments.size()]);
             ExecutableBinary binary = new ExecutableBinary(segmentsArray);
             binary.encode(options.outStream);
         }
     }
 
     private static Segment makeEntrypointSegment(int entryPoint)
     {
         HashMap<String, byte[]> attributes = new HashMap<String, byte[]>();
         attributes.put(Segment.ATTRIBUTE_TYPE, Segment.TYPE_ENTRYPOINT);
         attributes.put(Segment.ATTRIBUTE_ADDRESS, ByteUtils.convertInt(entryPoint));
         return new Segment(attributes, new byte[0]);
     }
 
     private static Segment makeMemorySegment(int address, byte[] bytes)
     {
         HashMap<String, byte[]> attributes = new HashMap<String, byte[]>();
         attributes.put(Segment.ATTRIBUTE_TYPE, Segment.TYPE_MEMORY);
         attributes.put(Segment.ATTRIBUTE_ADDRESS, ByteUtils.convertInt(address));
         return new Segment(attributes, bytes);
     }
 
     private static void verboseOutput(Bin.BinBase[] elems, PrintStream printStream, String sectionTitle, long baseAddress, HashMap<String, Long> labels, boolean useAddress,
             Token.TokenBase[] tokens, String fullSrc)
     {
         if (elems.length > 0) {
             printStream.println();
             printStream.println(sectionTitle);
         }
         long addr = baseAddress;
         for (Bin.BinBase binElem : elems) {
             byte[] bytes = binElem.getBinary(labels, addr);
             String[] strBinWords = new String[bytes.length >> 2];
             for (int j = 0; j < bytes.length / 4; j++) {
                 strBinWords[j] = (useAddress ? addrToString(addr) + ": " : blankBinAddr) + bytesWordToString(bytes, j * 4);
                 addr += 4;
             }
             String comment = fullSrc.substring(tokens[binElem.tokenStart].srcStart, tokens[binElem.tokenEnd - 1].srcEnd);
             String[] commentLines = comment.split("\n");
             int maxLen = Math.max(strBinWords.length, commentLines.length);
             for (int j = 0; j < maxLen; j++) {
                 printStream.println((j < strBinWords.length ? strBinWords[j] : blankBinWord) + (j < commentLines.length ? " ; " + commentLines[j] : ""));
             }
         }
     }
 
     private static void nonverboseOutput(long baseAddress, Bin.BinBase[] binElems, HashMap<String, Long> labels, OutputStream outStream) throws IOException
     {
         long addr = baseAddress;
         for (Bin.BinBase binElem : binElems) {
             byte[] data = binElem.getBinary(labels, addr);
             outStream.write(data);
             addr += data.length;
         }
     }
 
     // validates the address into the 32-bit unsigned range and returns a String
     // of the hex value in the form "HHHHHHHH"
     private static String addrToString(long addr)
     {
         // validate range
         if (!(0 <= addr && addr <= 0xFFFFFFFFL))
             throw new RuntimeException(); // todo
         String rtnStr = "";
         for (int i = 0; i < 8; i++) // eight nibbles in a word
         {
             rtnStr = Long.toHexString(addr & 0xF).toUpperCase() + rtnStr;
             addr >>= 4;
         }
         return rtnStr;
     }
 
     // returns a String of the hex value of one word starting at i in the byte
     // array in the form "HHHHHHHH"
     private static String bytesWordToString(byte[] bytes, int i)
     {
         String rtnStr = "";
         for (int j = i; j < i + 4; j++) // four bytes in a word
         {
             // big endian
             rtnStr += Integer.toHexString((bytes[j] & 0xF0) >> 4).toUpperCase();
             rtnStr += Integer.toHexString((bytes[j] & 0x0F) >> 0).toUpperCase();
         }
         return rtnStr;
     }
 
     public static ExecutableBinary assembleToBinary(String inputPath, AssemblerOptions assemblerOptions) throws AssemblingException, IOException
     {
         byte[] binaryBytes = Assembler.assembleToBytes(inputPath, assemblerOptions);
         InputStream binaryInputStream = new ByteArrayInputStream(binaryBytes);
         return ExecutableBinary.decode(binaryInputStream);
     }
 }
