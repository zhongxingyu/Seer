 package multiplexer.jmx.tools.rulesconsts;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import multiplexer.Multiplexer.MultiplexerMessageDescription;
 import multiplexer.Multiplexer.MultiplexerPeerDescription;
 import multiplexer.Multiplexer.MultiplexerRules;
 
 import com.google.protobuf.TextFormat;
 
 public abstract class AbstractRulesCompiler implements
 	ConstantsFromRulesCompiler {
 
 	protected Options options;
 
 	@Override
 	public void compile(Options options) throws FileNotFoundException,
 		IOException, InvalidRulesFileException {
 
 		this.options = options;
 		MultiplexerRules rules = parse();
 		ConstantsGroup group = buildGroup(rules);
 		validateNames(group);
 		validateUniqness(group);
 		validateValues(group);
 		output(group);
 	}
 
 	protected MultiplexerRules parse() throws FileNotFoundException,
 		IOException {
 
 		MultiplexerRules.Builder rulesBuilder = MultiplexerRules.newBuilder();
 		TextFormat.merge(new FileReader(options.rulesFile), rulesBuilder);
 		return rulesBuilder.build();
 	}
 
 	protected ConstantsGroup buildGroup(MultiplexerRules rules) {
 
 		List<Constant<Integer>> peerTypes = new ArrayList<Constant<Integer>>();
 		List<Constant<Integer>> messageTypes = new ArrayList<Constant<Integer>>();
 
 		for (MultiplexerPeerDescription mpd : rules.getPeerList())
 			peerTypes.add(new Constant<Integer>(mpd.getName(), mpd.getType()));
 		for (MultiplexerMessageDescription mmd : rules.getTypeList())
 			messageTypes
 				.add(new Constant<Integer>(mmd.getName(), mmd.getType()));
 
 		return new ConstantsGroup(peerTypes, messageTypes);
 	}
 
 	protected void validateNames(ConstantsGroup group)
 		throws InvalidRulesFileException {
 		validateNames(group.getPeerTypes());
 		validateNames(group.getMessageTypes());
 	}
 
 	protected void validateNames(List<Constant<Integer>> constants)
 		throws InvalidRulesFileException {
 
 		for (Constant<?> c : constants) {
 			if (!c.name.toUpperCase().equals(c.name)) {
 				throw new InvalidRulesFileException("Constant name '" + c.name
 					+ "' is not in upper case.");
 			}
 		}
 	}
 
 	protected void validateUniqness(ConstantsGroup group)
 		throws InvalidRulesFileException {
 
 		validateUniqness(group.getPeerTypes());
 		validateUniqness(group.getMessageTypes());
 	}
 
 	protected void validateUniqness(List<Constant<Integer>> constants)
 		throws InvalidRulesFileException {
 		Map<Integer, String> values = new HashMap<Integer, String>();
 		for (Constant<Integer> c : constants) {
 			String otherName = values.put(c.value, c.name);
 			if (otherName != null) {
 				throw new InvalidRulesFileException("Constants '" + otherName
 					+ "' and '" + c.name + "' have the same value of "
 					+ c.value);
 			}
 		}
 	}
 
 	protected abstract void validateValues(ConstantsGroup group)
 		throws InvalidRulesFileException;
 
 	protected void output(ConstantsGroup group) throws IOException {
 		// TODO
 		prepareOutputConfiguration(options);
 
 		if (!new File(options.outputRoot).isDirectory()) {
 			throw new RuntimeException("File '" + options.outputRoot
 				+ "' does not exist or is not a directory.");
 		}
 
 		new File(options.outputFile).getParentFile().mkdirs();
 
 		Writer writer = new FileWriter(options.outputFile);
 		if (options.packageName != null && !options.packageName.isEmpty()) {
 			writer.write("package " + options.packageName + ";\n");
 		}
 		writer.write("\n");
 		writer.write("import java.util.*;\n");
 		writer.write("\n");
 		writer.write("public class " + options.outputClassName + " {\n");
 		writer.write("\n");
 		outputConstantsList(writer, "PeerTypes", group.getPeerTypes());
 		writer.write("\n");
 		outputConstantsList(writer, "MessageTypes", group.getMessageTypes());
 		writer.write("}\n");
 		writer.close();
 	}
 
 	protected void outputConstantsList(Writer writer, String innerClassName,
 		List<Constant<Integer>> messageTypes) throws IOException {
 
 		outputConstantsList(writer, innerClassName, messageTypes, "\t");
 	}
 
 	protected void outputConstantsList(Writer writer, String innerClassName,
 		List<Constant<Integer>> constants, final String linePrefix)
 		throws IOException {
 		final String lp = linePrefix + "\t";
 
 		writer.write(linePrefix + "public static class " + innerClassName
 			+ " {\n");
 		// constants for regular use
 		writer.write("\n");
 		for (Constant<Integer> c : constants) {
 			writer.write(lp + "public final static int " + c.name + " = "
 				+ c.value + ";\n");
 		}
 		// constants map for programmatic access
 		writer.write("\n");
 		writer.write(lp + "private static class MapHolder {\n");
 		writer.write(lp + "\t"
 			+ "public final static Map<String, Integer> constants;\n");
 		writer.write(lp + "\t" + "static {\n");
 		writer.write(lp + "\t\t"
 			+ "Map<String, Integer> tmp = new HashMap<String, Integer>();\n");
 		for (Constant<Integer> c : constants) {
 			writer.write(lp + "\t\t" + "tmp.put(\"" + c.name + "\", "
 				+ c.name + ");\n");
 		}
 		writer.write(lp + "\t\t"
 			+ "constants = Collections.unmodifiableMap(tmp);\n");
 		writer.write(lp + "\t" + "}\n"); // MapHolder.static
 		writer.write(lp + "}\n"); // MapHolder
 		writer.write("\n");
 		writer.write(lp + "public static Map<String, Integer> getMap() {\n");
 		writer.write(lp + "\t" + "return MapHolder.constants;\n");
 		writer.write(lp + "};\n");
 		writer.write(linePrefix + "}\n"); // innerClassName
 	}
 
 	public static void prepareOutputConfiguration(Options options) {
 		if (options.outputRoot == null) {
 			throw new RuntimeException("No output dir specified.");
 		}
 
 		if (options.outputRoot.endsWith(File.separator)) {
 			options.outputRoot = options.outputRoot.substring(0,
 				options.outputRoot.length() - 1);
 		}
 
 		if (options.outputFile == null) {
 			if (options.outputClass != null) {
 				options.outputFile = options.outputRoot + File.separator
					+ options.outputClass.replace(".", File.separator)
 					+ ".java";
 			} else {
 				throw new RuntimeException("No output file chosen.");
 			}
 		}
 		if (!options.outputFile.startsWith(options.outputRoot)) {
 			throw new RuntimeException(
 				"Output file is not located within output dir.");
 		}
 
 		if (options.outputClass == null) {
 			if (options.outputFile != null) {
 				options.outputClass = options.outputFile.substring(0,
 					options.outputFile.lastIndexOf('.')).substring(
					options.outputRoot.length() + 1).replace("/", ".");
 			} else {
 				throw new RuntimeException("Output class not specified.");
 			}
 		}
 
 		if (options.packageName == null) {
 			if (options.outputClass != null) {
 				options.packageName = options.outputClass.substring(0,
 					options.outputClass.lastIndexOf('.'));
 
 			} else {
 				throw new RuntimeException("Output package name not specified.");
 			}
 		}
 
 		options.outputClassName = options.outputClass
 			.substring(options.outputClass.lastIndexOf('.') + 1);
 	}
 }
