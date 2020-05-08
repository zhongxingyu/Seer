 package uk.org.smithfamily.utils.normaliser;
 
 import java.io.*;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class Normaliser
 {
     enum Section
     {
         None, Header, Expressions, Gauges, Logs, FrontPage, Constants, PcVariables, ConstantsExtensions
     }
 
     private static final String        TAB          = "    ";
 
     private static List<String>        runtime      = new ArrayList<String>();
     private static List<String>        logHeader    = new ArrayList<String>();
     private static List<String>        logRecord    = new ArrayList<String>();
     private static List<String>        gaugeDef     = new ArrayList<String>();
     private static Map<String, String> classList    = new TreeMap<String, String>();
     private static Map<String, String> runtimeVars;
     private static Map<String, String> evalVars;
     private static Map<String, String> constantVars;
     private static List<String>        defaults;
     private static Set<String>         flags;
     private static String              fingerprintSource;
     private static ArrayList<String>   gaugeDoc;
     private static String              signatureDeclaration;
     private static String              queryCommandStr;
     private static String              ochGetCommandStr;
     private static String              ochBlockSizeStr;
     private static ArrayList<String>   defaultGauges;
     private static boolean 			   isCRC32Protocol;
     private static int                 currentPage  = 0;
     private static ArrayList<Constant> constants;
     private static ArrayList<String>   pageSizes;
     private static ArrayList<String>   pageIdentifiers;
     private static ArrayList<String>   pageActivateCommands;
     private static ArrayList<String>   pageReadCommands;
     private static Section             currentSection;
     private static String              className;
     private static int                 interWriteDelay;
     private static int                 pageActivationDelayVal;
     private static Set<String>         alwaysInt    = new HashSet<String>(Arrays.asList(new String[] {}));
     private static Set<String>         alwaysDouble = new HashSet<String>(Arrays.asList(new String[] { "pulseWidth", "throttle", "accDecEnrich",
             "accDecEnrichPcnt", "accEnrichPcnt", "accEnrichMS", "decEnrichPcnt", "decEnrichMS", "time", "egoVoltage", "egoVoltage2", "egoCorrection", "veCurr",
             "lambda", "TargetLambda"               }));
     private static File                outputDirectory;
 
     private static String              classSignature;
 
     /**
      * @param args
      * @throws FileNotFoundException
      */
     public static void main(String[] args) throws IOException
     {
         File f = new File(args[0]);
         outputDirectory = new File(args[1]);
         outputDirectory.mkdirs();
 
         BufferedReader br = new BufferedReader(new FileReader(f));
 
         String line;
 
         while ((line = br.readLine()) != null)
         {
             if (!line.startsWith("#"))
             {
                 preProcess(f.getParent(), line);
             }
         }
         outputRegistry(f.getParentFile());
     }
 
     private static void outputRegistry(File baseLocation) throws IOException
     {
         File srcDirectory= new File(outputDirectory,"gen_src/uk/org/smithfamily/mslogger/ecuDef/gen");
         File or = new File(srcDirectory, "ECURegistry.java");
         PrintWriter w = new PrintWriter(new FileWriter(or));
 
         File template = new File(baseLocation, "ECURegistry.java");
         BufferedReader br = new BufferedReader(new FileReader(template));
         String line;
 
         while ((line = br.readLine()) != null)
         {
             if (line.trim().equals("<LIST>"))
             {
                 for(String name : classList.keySet())
                 {
                     String sig = classList.get(name);
                     w.println(String.format("        registerEcu(%s.class,\"%s\");",name,sig));
                 }
             }
             else
             {
                 w.println(line);
             }
         }
         br.close();
         w.close();
     }
 
     /**
      * Initialise our stores to start a new class definition
      * 
      * @param filename
      * @param line
      * @throws IOException
      */
     private static void preProcess(String directory, String filename) throws IOException
     {
         signatureDeclaration = "";
         queryCommandStr = "";
         runtime = new ArrayList<String>();
         logHeader = new ArrayList<String>();
         logRecord = new ArrayList<String>();
         runtimeVars = new HashMap<String, String>();
         evalVars = new HashMap<String, String>();
         constantVars = new HashMap<String, String>();
         defaults = new ArrayList<String>();
         constants = new ArrayList<Constant>();
         flags = new HashSet<String>();
         gaugeDef = new ArrayList<String>();
         gaugeDoc = new ArrayList<String>();
         defaultGauges = new ArrayList<String>();
         pageActivateCommands = new ArrayList<String>();
         pageIdentifiers = new ArrayList<String>();
         fingerprintSource = "";
         currentPage = 0;
         isCRC32Protocol = false;
         constants = new ArrayList<Constant>();
         // Default for those who don't define it. I'm looking at you megasquirt-I.ini!
         ochGetCommandStr = "byte [] ochGetCommand = new byte[]{'A'};";
         currentSection = Section.None;
 
         File f = new File(directory, filename);
         if (f.isDirectory())
             return;
 
         className = f.getName();
         process(f, false);
     }
 
     /**
      * Iterate over the file to read it
      * 
      * @param f
      * @param subRead
      * @throws IOException
      */
     private static void process(File f, boolean subRead) throws IOException
     {
 
         BufferedReader br = new BufferedReader(new FileReader(f));
 
         String line;
 
         while ((line = br.readLine()) != null)
         {
             if (line.startsWith("#include "))
             {
                 handleImport(line, f);
                 continue;
             }
 
             if (line.trim().equals("[MegaTune]"))
             {
                 currentSection = Section.Header;
                 continue;
             }
             else if (line.trim().equals("[OutputChannels]"))
             {
                 currentSection = Section.Expressions;
                 continue;
             }
             else if (line.trim().equals("[Datalog]"))
             {
                 currentSection = Section.Logs;
                 continue;
 
             }
             else if (line.trim().equals("[GaugeConfigurations]"))
             {
                 currentSection = Section.Gauges;
                 continue;
 
             }
             else if (line.trim().equals("[FrontPage]"))
             {
                 currentSection = Section.FrontPage;
             }
             else if (line.trim().equals("[Constants]"))
             {
                 currentSection = Section.Constants;
             }
             else if (line.trim().equals("[PcVariables]"))
             {
                 currentSection = Section.PcVariables;
             }
             else if (line.trim().equals("[ConstantsExtensions]"))
             {
                 currentSection = Section.ConstantsExtensions;
             }
             else if (line.trim().startsWith("["))
             {
                 currentSection = Section.None;
                 continue;
 
             }
             switch (currentSection)
             {
             case Expressions:
                 processExpr(line);
                 break;
             case Logs:
                 processLogEntry(line);
                 break;
             case Gauges:
                 processGaugeEntry(line);
                 break;
             case Header:
                 processHeader(line);
                 break;
             case FrontPage:
                 processFrontPage(line);
                 break;
             case Constants:
                 processConstants(line);
                 break;
             case PcVariables:
                 processPcVariables(line);
                 break;
             case ConstantsExtensions:
                 processConstantsExtensions(line);
                 break;
             }
 
         }
         if (!subRead)
         {
             writeFile(className);
         }
     }
 
     private static String removeComments(String line)
     {
         line += "; junk";
         line = StringUtils.trim(line).split(";")[0];
         line = line.trim();
         return line;
     }
 
     private static void processConstantsExtensions(String line)
     {
         line = removeComments(line);
 
         if (line.contains("defaultValue"))
         {
             String statement = "";
             String[] definition = line.split("=")[1].split(",");
             if (definition[1].contains("\""))
             {
                 statement = "String ";
             }
             else
             {
                 statement = "int ";
             }
             statement += definition[0] + " = " + definition[1] + ";";
             defaults.add(statement);
         }
 
     }
 
     private static void processPcVariables(String line)
     {
 
     }
 
     /**
      * Process an included/imported file
      * 
      * @param line
      * @param parentFile
      * @throws IOException
      */
     private static void handleImport(String line, File parentFile) throws IOException
     {
         Pattern importFile = Pattern.compile(".*\\\"(.*)\\\"");
 
         Matcher importFileM = importFile.matcher(line);
         if (importFileM.matches())
         {
             String fileName = importFileM.group(1);
             File imported = new File(parentFile.getAbsoluteFile().getParentFile(), fileName);
             process(imported, true);
         }
 
     }
 
     /**
      * Process the [Constants] section of the ini file
      * 
      * @param line
      */
     private static void processConstants(String line)
     {
         line = removeComments(line);
         if (StringUtils.isEmpty(line))
         {
             return;
         }
         if(line.contains("messageEnvelopeFormat"))
         {
         	isCRC32Protocol = line.contains("msEnvelope_1.0");
         }
         Matcher pageM = Patterns.page.matcher(line);
         if (pageM.matches())
         {
             currentPage = Integer.parseInt(pageM.group(1).trim());
             return;
         }
         Matcher pageSizesM = Patterns.pageSize.matcher(line);
         if (pageSizesM.matches())
         {
             String values = StringUtils.remove(pageSizesM.group(1), ' ');
             String[] list = values.split(",");
             pageSizes = new ArrayList<String>(Arrays.asList(list));
         }
         Matcher pageIdentifersM = Patterns.pageIdentifier.matcher(line);
         if (pageIdentifersM.matches())
         {
             String values = StringUtils.remove(pageIdentifersM.group(1), ' ');
             values = StringUtils.remove(values, '"');
             String[] list = values.split(",");
             pageIdentifiers = new ArrayList<String>(Arrays.asList(list));
         }
 
         Matcher pageActivateM = Patterns.pageActivate.matcher(line);
         if (pageActivateM.matches())
         {
             String values = StringUtils.remove(pageActivateM.group(1), ' ');
             values = StringUtils.remove(values, '"');
             String[] list = values.split(",");
             pageActivateCommands = new ArrayList<String>(Arrays.asList(list));
         }
 
         Matcher pageReadCommandM = Patterns.pageReadCommand.matcher(line);
         if (pageReadCommandM.matches())
         {
             String values = StringUtils.remove(pageReadCommandM.group(1), ' ');
             values = StringUtils.remove(values, '"');
             String[] list = values.split(",");
             pageReadCommands = new ArrayList<String>(Arrays.asList(list));
         }
 
         Matcher interWriteDelayM = Patterns.interWriteDelay.matcher(line);
         if (interWriteDelayM.matches())
         {
             interWriteDelay = Integer.parseInt(interWriteDelayM.group(1).trim());
             return;
         }
         Matcher pageActivationDelayM = Patterns.pageActivationDelay.matcher(line);
         if (pageActivationDelayM.matches())
         {
             pageActivationDelayVal = Integer.parseInt(pageActivationDelayM.group(1).trim());
             return;
         }
         Matcher bitsM = Patterns.bits.matcher(line);
         Matcher constantM = Patterns.constantScalar.matcher(line);
         Matcher constantSimpleM = Patterns.constantSimple.matcher(line);
         if (constantM.matches())
         {
             String name = constantM.group(1);
             String classtype = constantM.group(2);
             String type = constantM.group(3);
             int offset = Integer.parseInt(constantM.group(4).trim());
             String units = constantM.group(5);
             String scaleText = constantM.group(6);
             String translateText = constantM.group(7);
             String lowText = constantM.group(8);
             String highText = constantM.group(9);
             String digitsText = constantM.group(10);
             double scale = !StringUtils.isEmpty(scaleText) ? Double.parseDouble(scaleText) : 0;
             double translate = !StringUtils.isEmpty(translateText) ? Double.parseDouble(translateText) : 0;
             double low = !StringUtils.isEmpty(lowText) ? Double.parseDouble(lowText) : 0;
             double high = !StringUtils.isEmpty(highText) ? Double.parseDouble(highText) : 0;
             int digits = !StringUtils.isEmpty(digitsText) ? (int) Double.parseDouble(digitsText) : 0;
 
             Constant c = new Constant(currentPage, name, classtype, type, offset, "", units, scale, translate, low, high, digits);
 
             if (scale == 1.0)
             {
                 constantVars.put(name, "int");
             }
             else
             {
                 constantVars.put(name, "double");
             }
             constants.add(c);
         }
         else if (constantSimpleM.matches())
         {
             String name = constantSimpleM.group(1);
             String classtype = constantSimpleM.group(2);
             String type = constantSimpleM.group(3);
             int offset = Integer.parseInt(constantSimpleM.group(4).trim());
             String units = constantSimpleM.group(5);
             double scale = Double.parseDouble(constantSimpleM.group(6));
             double translate = Double.parseDouble(constantSimpleM.group(7));
 
             Constant c = new Constant(currentPage, name, classtype, type, offset, "", units, scale, translate, 0, 0, 0);
 
             if (scale == 1.0)
             {
                 constantVars.put(name, "int");
             }
             else
             {
                 constantVars.put(name, "double");
             }
             constants.add(c);
 
         }
         else if (bitsM.matches())
         {
             String name = bitsM.group(1);
             String offset = bitsM.group(3);
             String start = bitsM.group(4);
             String end = bitsM.group(5);
 
             Constant c = new Constant(currentPage, name, "bits", "", Integer.parseInt(offset.trim()), "[" + start + ":" + end + "]", "", 1, 0, 0, 0, 0);
             constantVars.put(name, "int");
             constants.add(c);
 
         }
         else if (line.startsWith("#"))
         {
             String preproc = (processPreprocessor(line));
             Constant c = new Constant(currentPage, preproc, "", "PREPROC", 0, "", "", 0, 0, 0, 0, 0);
             constants.add(c);
         }
     }
 
     private static void processFrontPage(String line)
     {
         line = removeComments(line);
 
         Matcher dgM = Patterns.defaultGauge.matcher(line);
         if (dgM.matches())
         {
             defaultGauges.add(dgM.group(1));
         }
     }
 
     private static void processHeader(String line)
     {
         Matcher queryM = Patterns.queryCommand.matcher(line);
         if (queryM.matches())
         {
             queryCommandStr = "byte[] queryCommand = new byte[]{'" + queryM.group(1) + "'};";
             return;
         }
 
         Matcher sigM = Patterns.signature.matcher(line);
         Matcher sigByteM = Patterns.byteSignature.matcher(line);
         if (sigM.matches())
         {
             String tmpsig = sigM.group(1);
             if (line.contains("null"))
             {
                 tmpsig += "\\0";
             }
             classSignature = tmpsig;
             signatureDeclaration = "String signature = \"" + tmpsig + "\";";
         }
         else if (sigByteM.matches())
         {
             String b = sigByteM.group(1).trim();
             classSignature = "" + (byte) (Integer.parseInt(b));
             signatureDeclaration = "String signature = \"\"+(byte)" + b + ";";
         }
 
     }
 
     private static void processGaugeEntry(String line)
     {
         line = removeComments(line);
 
         Matcher m = Patterns.gauge.matcher(line);
         if (m.matches())
         {
             String name = m.group(1);
             String channel = m.group(2);
             String title = m.group(3);
             String units = m.group(4);
             String lo = m.group(5);
             String hi = m.group(6);
             String loD = m.group(7);
             String loW = m.group(8);
             String hiW = m.group(9);
             String hiD = m.group(10);
             String vd = m.group(11);
             String ld = m.group(12);
 
             String g = String.format("GaugeRegister.INSTANCE.addGauge(new GaugeDetails(\"%s\",\"%s\",%s,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,45));", name,
                     channel, channel, title, units, lo, hi, loD, loW, hiW, hiD, vd, ld);
 
             g = g.replace("{", "").replace("}", "");
             String gd = String
                     .format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                             name, channel, title, units, lo, hi, loD, loW, hiW, hiD, vd, ld);
             gaugeDoc.add(gd);
             gaugeDef.add(g);
 
         }
         else if (line.startsWith("#"))
         {
             gaugeDef.add(processPreprocessor(line));
             gaugeDoc.add(String.format("<tr><td colspan=\"12\" id=\"preprocessor\">%s</td></tr>", line));
         }
 
     }
 
     private static void writeFile(String className) throws IOException
     {
         className = StringUtils.capitalize(className);
         className = StringUtils.remove(className, ".");
         className = StringUtils.remove(className, "ini");
         className = StringUtils.replace(className, " ", "_");
         className = StringUtils.replace(className, "-", "_");
         className = StringUtils.replace(className, "&", "_");
         String classFile = "";
 
         classList.put(className,classSignature);
         String directory = outputDirectory.getAbsolutePath() + File.separator + "gen_src/uk/org/smithfamily/mslogger/ecuDef/gen/";
         new File(directory).mkdirs();
         classFile = directory + className + ".java";
         System.out.println("Writing to " + classFile);
         PrintWriter writer = new PrintWriter(new FileWriter(classFile));
         String fingerprint = getFingerprint();
         System.out.println(fingerprint + " : " + className);
 
         outputPackageAndIncludes(writer);
 
         writer.println("/*");
         writer.println("Fingerprint : " + fingerprint);
         writer.println("*/");
 
         writer.println("@SuppressWarnings(\"unused\")");
         writer.println("public class " + className + " extends Megasquirt\n{");
         outputConstructor(writer, className);
         writer.println(TAB+"private Map<String,Double> fields = new HashMap<String,Double>();");
         writer.println(TAB + queryCommandStr);
         writer.println(TAB + signatureDeclaration);
         writer.println(TAB + ochGetCommandStr);
         writer.println(TAB + ochBlockSizeStr);
         outputGlobalVars(writer);
         outputRTCalcs(writer);
         outputLogInfo(writer);
         outputGauges(writer);
 
         // outputGaugeDoc(writer);
 
         outputOverrides(writer);
         outputLoadConstants(writer);
         writer.println("\n}\n");
 
         writer.close();
     }
 
  
 
 	private static void outputGlobalVars(PrintWriter writer)
     {
         writer.println("//Flags");
         for (String name : flags)
         {
             writer.println(TAB + "public boolean " + name + ";");
         }
         writer.println("//Defaults");
         for (String d : defaults)
         {
             writer.println(TAB + "public " + d);
         }
         Map<String, String> vars = new TreeMap<String, String>();
         vars.putAll(runtimeVars);
         vars.putAll(evalVars);
         for (String v : vars.keySet())
         {
             constantVars.remove(v);
         }
         writer.println("//Variables");
         for (String name : vars.keySet())
         {
             String type = getType(name, vars);
             writer.println(TAB + "public " + type + " " + name + ";");
         }
         writer.println("\n//Constants");
         for (String name : constantVars.keySet())
         {
             String type = getType(name, constantVars);
             writer.println(TAB + "public " + type + " " + name + ";");
         }
         writer.println("\n");
         writer.println(TAB + "private String[] defaultGauges = {");
         boolean first = true;
         for (String dg : defaultGauges)
         {
             if (!first)
                 writer.println(",");
             first = false;
             writer.print(TAB + TAB + "\"" + dg + "\"");
         }
         writer.println("\n" + TAB + "};");
 
     }
 
     private static String getType(String name, Map<String, String> vars)
     {
         String type = vars.get(name);
         if (alwaysInt.contains(name))
         {
             type = "int";
         }
         else if (alwaysDouble.contains(name))
         {
             type = "double";
         }
         return type;
     }
 
     private static void outputRTCalcs(PrintWriter writer)
     {
         writer.println("	@Override");
         writer.println("	public void calculate(byte[] ochBuffer)");
         writer.println("    {");
         for (String defn : runtime)
         {
             writer.println(TAB + TAB + defn);
             // System.out.println(defn);
         }
         writer.println(TAB + "}");
     }
 
     private static void outputLogInfo(PrintWriter writer)
     {
         writer.println(TAB + "@Override");
         writer.println(TAB + "public String getLogHeader()");
         writer.println(TAB + "{");
         writer.println(TAB + TAB + "StringBuffer b = new StringBuffer();");
         for (String header : logHeader)
         {
             writer.println(TAB + TAB + header);
         }
         writer.println(TAB + TAB + "b.append(MSUtils.getLocationLogHeader());");
         writer.println(TAB + TAB + "return b.toString();\n" + TAB + "}\n");
         writer.println(TAB + "@Override");
         writer.println(TAB + "public String getLogRow()");
         writer.println(TAB + "{");
         writer.println(TAB + TAB + "StringBuffer b = new StringBuffer();");
 
         for (String record : logRecord)
         {
             writer.println(TAB + TAB + record);
         }
         writer.println(TAB + TAB + "b.append(MSUtils.getLocationLogRow());");
         writer.println(TAB + TAB + "return b.toString();\n" + TAB + "}\n");
     }
 
     private static void outputGauges(PrintWriter writer)
     {
         writer.println(TAB + "@Override");
         writer.println(TAB + "public void initGauges()");
         writer.println(TAB + "{");
         for (String gauge : gaugeDef)
         {
             boolean okToWrite = true;
             if (gauge.contains("GaugeRegister"))
             {
                 String[] parts = gauge.split(",");
                 String channel = parts[2];
                 okToWrite = runtimeVars.containsKey(channel) || evalVars.containsKey(channel);
             }
             if (okToWrite)
             {
                 writer.println(TAB + TAB + gauge);
             }
         }
         writer.println(TAB + "}\n");
     }
 
     @SuppressWarnings("unused")
     private static void outputGaugeDoc(PrintWriter writer)
     {
         writer.println("/*");
         for (String gauge : gaugeDoc)
         {
             writer.println(gauge);
         }
 
         writer.println("*/");
     }
 
     private static void outputConstructor(PrintWriter writer, String className)
     {
         writer.println(TAB + "public " + className + "(Context c)");
         writer.println(TAB + "{");
         writer.println(TAB + TAB + "super(c);");
         writer.println(TAB + TAB + "refreshFlags();");
         writer.println(TAB + "}");
         writer.println(TAB + "@Override");
         writer.println(TAB + "public void refreshFlags()");
         writer.println(TAB + "{");
         for (String flag : flags)
         {
             writer.println(TAB + TAB + flag + " = isSet(\"" + flag + "\");");
         }
         writer.println(TAB + "}");
 
     }
 
     private static void outputPackageAndIncludes(PrintWriter writer)
     {
         writer.println("package uk.org.smithfamily.mslogger.ecuDef.gen;");
         writer.println("");
         writer.println("import java.io.IOException;");
         writer.println("import java.util.*;");
         writer.println("");
         writer.println("import android.content.Context;");
         writer.println("");
         writer.println("import uk.org.smithfamily.mslogger.ecuDef.*;");
         writer.println("import uk.org.smithfamily.mslogger.widgets.GaugeDetails;");
         writer.println("import uk.org.smithfamily.mslogger.widgets.GaugeRegister;");
 
     }
 
     private static void outputLoadConstants(PrintWriter writer)
     {
         writer.println(TAB + "@Override");
         writer.println(TAB + "public void loadConstants(boolean simulated)");
         writer.println(TAB + "{");
         writer.println(TAB + TAB + "byte[] pageBuffer = null;");
         int pageNo = 0;
         for (Constant c : constants)
         {
             if (c.getPage() != pageNo)
             {
                 pageNo = c.getPage();
                 int pageSize = Integer.parseInt(pageSizes.get(pageNo - 1).trim());
                 String activateCommand = null;
                 if (pageNo - 1 < pageActivateCommands.size())
                 {
                     activateCommand = pageActivateCommands.get(pageNo - 1);
                 }
                 String readCommand = null;
                 if (pageNo - 1 < pageReadCommands.size())
                 {
                     readCommand = pageReadCommands.get(pageNo - 1);
                 }
 
                 outputLoadPage(pageNo, 0, pageSize, activateCommand, readCommand, writer);
             }
             // getScalar(String bufferName,String name, String dataType, String
             // offset, String scale, String numOffset)
             String name = c.getName();
             if (!"PREPROC".equals(c.getType()))
             {
                 String def;
                 if ("bits".equals(c.getClassType()))
                 {
                     String bitspec = StringUtils.remove(StringUtils.remove(c.getShape(), '['), ']');
                     String[] bits = bitspec.split(":");
                     int offset = c.getOffset();
                     String start = bits[0];
                     String end = bits[1];
                     String ofs = "0";
                     if (end.contains("+"))
                     {
                         String[] parts = end.split("\\+");
                         end = parts[0];
                         ofs = parts[1];
                     }
                     def = (name + " = MSUtils.getBits(pageBuffer," + offset + "," + start + "," + end + "," + ofs + ");");
                 }
                 else
                 {
                     def = getScalar("pageBuffer", constantVars.get(name), name, c.getType(), "" + c.getOffset(), "" + c.getScale(), "" + c.getTranslate());
                 }
                 writer.println(TAB + TAB + def);
             }
             else
             {
                 writer.println(TAB + TAB + name);
             }
 
         }
 
         writer.println(TAB + "}");
     }
 
     private static void outputLoadPage(int pageNo, int pageOffset, int pageSize, String activate, String read, PrintWriter writer)
     {
         if (activate != null)
         {
 
             activate = processStringToBytes(activate, pageOffset, pageSize, pageNo);
         }
         if (read != null)
         {
             read = processStringToBytes(read, pageOffset, pageSize, pageNo);
         }
         writer.println(TAB + TAB + String.format("pageBuffer = loadPage(%d,%d,%d,%s,%s);", pageNo, pageOffset, pageSize, activate, read));
 
     }
 
     private static String processStringToBytes(String s, int offset, int count, int pageNo)
     {
         String ret = "new byte[]{";
 
         ret += HexStringToBytes(s, offset, count, pageNo);
 
         ret += "}";
         return ret;
     }
 
     private static String HexStringToBytes(String s, int offset, int count, int pageNo)
     {
         String ret = "";
         boolean first = true;

         for (int p = 0; p < s.length(); p++)
         {
             if (!first)
                 ret += ",";
 
             char c = s.charAt(p);
             switch (c)
             {
             case '\\':
                 ret += HexByteToDec(s.substring(p));
                 p = p + 3;
                 ;
                 break;
 
             case '%':
                 p++;
                 c = s.charAt(p);
 
                 assert c == '2';
                 p++;
                 c = s.charAt(p);
                 if (c == 'o')
                 {
                     ret += bytes(offset);
                 }
                 else if (c == 'c')
                 {
                     ret += bytes(count);
                 }
                 else if (c == 'i')
                 {
                     String identifier = pageIdentifiers.get(pageNo - 1);
                    identifier = identifier.replace("$tsCanId", "x00");
 
                     ret += HexStringToBytes(identifier, offset, count, pageNo);
                 }
                 break;
 
             default:
                 ret += Byte.toString((byte) c);
                 break;
             }
             first = false;
         }
         return ret;
     }
 
     private static int HexByteToDec(String s)
     {
         String digits = "0123456789abcdef";
         int i = 0;
         char c = s.charAt(i++);
         assert c == '\\';
         c = s.charAt(i++);
         assert c == 'x';
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         int val = 0;
         int digit = digits.indexOf(c);
         val = digit * 16;
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         digit = digits.indexOf(c);
         val = val + digit;
         return val;
 
     }
 
     private static String bytes(int val)
     {
         int hi = val / 256;
         int low = val % 256;
         if (hi > 127)
             hi -= 256;
         if (low > 127)
             low -= 256;
         return "" + hi + "," + low;
     }
 
     private static void outputOverrides(PrintWriter writer)
     {
         String overrides = TAB + "@Override\n" + TAB + "public String getSignature()\n" + TAB + "{\n" + TAB + TAB + "return signature;\n" + "}\n" + TAB
                 + "@Override\n" + TAB + "public byte[] getOchCommand()\n" + TAB + "{\n" + TAB + TAB + "return this.ochGetCommand;\n" + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public byte[] getSigCommand()\n" + TAB + "{\n" + TAB + TAB + "return this.queryCommand;\n" + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public int getBlockSize()\n" + TAB + "{\n" + TAB + TAB + "return this.ochBlockSize;\n" + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public int getSigSize()\n" + TAB + "{\n" + TAB + TAB + "return signature.length();\n" + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public int getPageActivationDelay()\n" + TAB + "{\n" + TAB + TAB + "return " + pageActivationDelayVal + ";\n"
                 + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public int getInterWriteDelay()\n" + TAB + "{\n" + TAB + TAB + "return " + interWriteDelay + ";\n" + TAB + "}\n" +
                 TAB + "@Override\n" + TAB + "public boolean isCRC32Protocol()\n" + TAB + "{\n" + TAB + TAB + "return " +isCRC32Protocol + ";\n" + TAB + "}\n" +
 
                 TAB + "@Override\n" + TAB + "public int getCurrentTPS()\n" + TAB + "{\n";
         if (runtimeVars.containsKey("tpsADC"))
         {
             overrides += TAB + TAB + "return (int)tpsADC;\n";
         }
         else
         {
             overrides += TAB + TAB + "return 0;\n";
         }
 
         overrides += TAB + "}\n" +
 
         TAB + "@Override\n" + TAB + "public String[] defaultGauges()\n" + TAB + "{\n" + TAB + TAB + "return defaultGauges;\n" + TAB + "}\n";
 
         writer.println(overrides);
     }
 
     private static String getFingerprint()
     {
         StringBuffer b = new StringBuffer();
         try
         {
             MessageDigest md = MessageDigest.getInstance("MD5");
             byte[] array = md.digest(fingerprintSource.getBytes());
             for (int i = 0; i < array.length; i++)
             {
                 b.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
             }
         }
         catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace();
         }
 
         return b.toString();
     }
 
     private static void processLogEntry(String line)
     {
         line = removeComments(line);
 
         Matcher logM = Patterns.log.matcher(line);
         if (logM.matches())
         {
             String header = logM.group(2);
             String variable = logM.group(1);
             if ("double".equals(runtimeVars.get(variable)))
             {
                 variable = "round(" + variable + ")";
             }
             logHeader.add("b.append(\"" + header + "\").append(\"\\t\");");
             logRecord.add("b.append(" + variable + ").append(\"\\t\");");
         }
         else if (line.startsWith("#"))
         {
             String directive = processPreprocessor(line);
             logHeader.add(directive);
             logRecord.add(directive);
         }
     }
 
     private static void processExpr(String line)
     {
         String definition = null;
         line = removeComments(line);
 
         line = StringUtils.replace(line, "timeNow", "timeNow()");
         Matcher bitsM = Patterns.bits.matcher(line);
         Matcher scalarM = Patterns.scalar.matcher(line);
         Matcher exprM = Patterns.expr.matcher(line);
         Matcher ochGetCommandM = Patterns.ochGetCommand.matcher(line);
         Matcher ochBlockSizeM = Patterns.ochBlockSize.matcher(line);
         if (bitsM.matches())
         {
             String name = bitsM.group(1);
             String offset = bitsM.group(3);
             String start = bitsM.group(4);
             String end = bitsM.group(5);
             String ofs = "0";
             if (end.contains("+"))
             {
                 String[] parts = end.split("\\+");
                 end = parts[0];
                 ofs = parts[1];
             }
             definition = (name + " = MSUtils.getBits(ochBuffer," + offset + "," + start + "," + end + "," + ofs + ");");
             runtime.add(definition);
             runtimeVars.put(name, "int");
         }
         else if (scalarM.matches())
         {
             String name = scalarM.group(1);
             String dataType = scalarM.group(2);
             String offset = scalarM.group(3);
             String scalingRaw = scalarM.group(4);
             String[] scaling = scalingRaw.split(",");
             String scale = scaling[1].trim();
             String numOffset = scaling[2].trim();
             if (Double.parseDouble(scale) != 1)
             {
                 runtimeVars.put(name, "double");
             }
             else
             {
                 runtimeVars.put(name, "int");
             }
             definition = getScalar("ochBuffer", runtimeVars.get(name), name, dataType, offset, scale, numOffset);
             fingerprintSource += definition;
             runtime.add(definition);
         }
         else if (exprM.matches())
         {
             String name = exprM.group(1);
             if ("pwma_load".equals(name))
             {
                 // Hook to hang a break point on
                 @SuppressWarnings("unused")
                 int x = 1;
             }
             String expression = deBinary(exprM.group(2).trim());
             Matcher ternaryM = Patterns.ternary.matcher(expression);
             if (ternaryM.matches())
             {
                 // System.out.println("BEFORE : " + expression);
                 String test = ternaryM.group(1);
                 String values = ternaryM.group(2);
                 if (StringUtils.containsAny(test, "<>!="))
                 {
                     expression = "(" + test + ") ? " + values;
                 }
                 else
                 {
                     expression = "((" + test + ") != 0 ) ? " + values;
                 }
                 // System.out.println("AFTER  : " + expression + "\n");
             }
             if (expression.contains("*") && expression.contains("=="))
             {
                 expression = convertC2JavaBoolean(expression);
             }
             definition = name + " = (" + expression + ");";
             runtime.add(definition);
             if (isFloatingExpression(expression))
             {
                 evalVars.put(name, "double");
             }
             else
             {
                 evalVars.put(name, "int");
             }
         }
         else if (ochGetCommandM.matches())
         {
             String och = ochGetCommandM.group(1);
             if (och.length() > 1)
             {
                 och = HexStringToBytes(och, 0, 0, 0);
             }
             else
             {
                 och = "'" + och + "'";
             }
             ochGetCommandStr = "byte [] ochGetCommand = new byte[]{" + och + "};";
         }
         else if (ochBlockSizeM.matches())
         {
             ochBlockSizeStr = "int ochBlockSize = " + ochBlockSizeM.group(1) + ";";
         }
         else if (line.startsWith("#"))
         {
             runtime.add(processPreprocessor(line));
         }
         else if (!StringUtils.isEmpty(line))
         {
             System.out.println(line);
         }
     }
 
     private static String convertC2JavaBoolean(String expression)
     {
         Matcher matcher = Patterns.booleanConvert.matcher(expression);
         StringBuffer result = new StringBuffer(expression.length());
         while (matcher.find())
         {
             matcher.appendReplacement(result, "");
             result.append(matcher.group(1) + " ? 1 : 0)");
         }
         matcher.appendTail(result);
         expression = result.toString();
         return expression;
 
     }
 
     private static String deBinary(String group)
     {
         Matcher binNumber = Patterns.binary.matcher(group);
         if (!binNumber.matches())
         {
             return group;
         }
         else
         {
             String binNum = binNumber.group(2);
             int num = Integer.parseInt(binNum, 2);
             String expr = binNumber.group(1) + num + binNumber.group(3);
             return deBinary(expr);
         }
     }
 
     private static boolean isFloatingExpression(String expression)
     {
         boolean result = expression.contains(".");
         if (result)
         {
             return result;
         }
         for (String var : runtimeVars.keySet())
         {
             if (expression.contains(var) && runtimeVars.get(var).equals("double"))
             {
                 result = true;
             }
         }
         for (String var : evalVars.keySet())
         {
             if (expression.contains(var) && evalVars.get(var).equals("double"))
             {
                 result = true;
             }
         }
 
         return result;
     }
 
     private static String processPreprocessor(String line)
     {
         String filtered;
         boolean stripped = false;
 
         filtered = line.replace("  ", " ");
         stripped = filtered.equals(line);
         while (!stripped)
         {
             line = filtered;
             filtered = line.replace("  ", " ");
             stripped = filtered.equals(line);
         }
         String[] components = line.split(" ");
         String flagName = components.length > 1 ? sanitize(components[1]) : "";
         if (components[0].equals("#if"))
         {
             flags.add(flagName);
             return ("if (" + flagName + ")\n        {");
         }
         if (components[0].equals("#elif"))
         {
             flags.add(flagName);
             return ("}\n        else if (" + flagName + ")\n        {");
         }
         if (components[0].equals("#else"))
         {
             return ("}\n        else\n        {");
         }
         if (components[0].equals("#endif"))
         {
             return ("}");
         }
 
         return "";
     }
 
     private static String sanitize(String flagName)
     {
         return StringUtils.replace(flagName, "!", "n");
     }
 
     private static String getScalar(String bufferName, String javaType, String name, String dataType, String offset, String scale, String numOffset)
     {
         if (javaType == null)
         {
             javaType = "int";
         }
         String definition = name + " = (" + javaType + ")((MSUtils.get";
         if (dataType.startsWith("S"))
         {
             definition += "Signed";
         }
         int size = Integer.parseInt(dataType.substring(1).trim());
         switch (size)
         {
         case 8:
             definition += "Byte";
             break;
         case 16:
             definition += "Word";
             break;
         case 32:
             definition += "Long";
             break;
         default:
             definition += dataType;
             break;
         }
         definition += "(" + bufferName + "," + offset + ") + " + numOffset + ") * " + scale + ");";
         return definition;
     }
 
 }
