 package de.uni_koblenz.jgralab.gretl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.pcollections.Empty;
 import org.pcollections.PMap;
 
 import de.uni_koblenz.jgralab.AttributedElement;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphIOException;
 import de.uni_koblenz.jgralab.ImplementationType;
 import de.uni_koblenz.jgralab.JGraLab;
 import de.uni_koblenz.jgralab.M1ClassManager;
 import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
 import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
 import de.uni_koblenz.jgralab.schema.AttributedElementClass;
 import de.uni_koblenz.jgralab.schema.EnumDomain;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.Schema;
 import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
 import de.uni_koblenz.jgralab.schema.exception.SchemaException;
 import de.uni_koblenz.jgralab.schema.impl.NamedElementImpl;
 import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;
 
 /**
  * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
  */
 public class Context {
 
 	private static Logger logger = JGraLab.getLogger(Context.class.getPackage()
 			.getName());
 	public static final String DEFAULT_SOURCE_GRAPH_ALIAS = "default";
 	public static final String DEFAULT_TARGET_GRAPH_ALIAS = "target";
 
 	private static final Pattern QUERY_GRAPH_ALIAS_PATTERN = Pattern.compile(
 			"\\p{Space}*(#(\\p{Alnum}+)#\\p{Space}*).*", Pattern.DOTALL);
 
 	private final Map<String, Graph> sourceGraphs = new HashMap<String, Graph>(
 			1);
 	private GreqlEvaluator eval = null;
 
 	Schema targetSchema = null;
 
 	private String targetSchemaCodeDirectory = null;
 
 	/**
 	 * This lets you set the directory where to commit the target schema code
 	 * to. Normally, the code isn't committed at all but compiled in memory, but
 	 * you can use this for debugging purposes.
 	 * 
 	 * The value <code>null</code> (default) means don't commit.
 	 * 
 	 * @param targetSchemaCodeDirectory
 	 *            the targetSchemaCodeDirectory to set
 	 */
 	public void setTargetSchemaCodeDirectory(String targetSchemaCodeDirectory) {
 		this.targetSchemaCodeDirectory = targetSchemaCodeDirectory;
 	}
 
 	/**
 	 * @return the targetSchema
 	 */
 	public final Schema getTargetSchema() {
 		return targetSchema;
 	}
 
 	Graph targetGraph = null;
 
 	public enum TransformationPhase {
 		SCHEMA, GRAPH
 	}
 
 	TransformationPhase phase = TransformationPhase.SCHEMA;
 
 	/**
 	 * @return the transformation phase
 	 */
 	public final TransformationPhase getPhase() {
 		return phase;
 	}
 
 	private String targetSchemaName;
 	private String targetGraphClassName;
 
 	/**
 	 * saves for nested transformations if the actual is the outermost one
 	 */
 	boolean outermost = true;
 
 	/**
 	 * Maps from {@link AttributedElementClass} to a map, mapping old elements
 	 * to their images. (zeta-reverse)
 	 */
 	private Map<AttributedElementClass, PMap<Object, AttributedElement>> imgMap = new HashMap<AttributedElementClass, PMap<Object, AttributedElement>>();
 
 	/**
 	 * Maps from {@link AttributedElementClass} to a map, mapping new elements
 	 * to the elements they were created for (their archetypes). (zeta)
 	 */
 	private Map<AttributedElementClass, PMap<AttributedElement, Object>> archMap = new HashMap<AttributedElementClass, PMap<AttributedElement, Object>>();
 
 	private final Map<String, Object> greqlExtraVars = new HashMap<String, Object>();
 	private final Set<String> greqlImports = new HashSet<String>();
 
 	final void setGReQLVariable(String name, Object val) {
 		greqlExtraVars.put(name, val);
 	}
 
 	final void setGReQLVariable(String name, String greqlExpression) {
 		greqlExtraVars.put(name, evaluateGReQLQuery(greqlExpression));
 	}
 
 	final void setGReQLHelper(String name, String greqlExpression) {
 		ensureGreqlEvaluator();
 		eval.setSubQuery(name, greqlExpression);
 	}
 
 	final void addGReQLImport(String qualifiedName) {
 		greqlImports.add(qualifiedName);
 	}
 
 	private final String getGreqlImportString(Graph graph) {
 		StringBuilder sb = new StringBuilder();
 		for (String s : greqlImports) {
 			// don't import, if the element[s] don't exist in the graph to be
 			// queried.
 			if (s.endsWith(".*")) {
 				if (graph.getSchema().getPackage(s.replace(".*", "")) == null) {
 					continue;
 				}
 			} else {
 				if (graph.getSchema().getAttributedElementClass(s) == null) {
 					continue;
 				}
 			}
 
 			// ok, we can use it, so do the import!
 			sb.append("import ");
 			sb.append(s);
 			sb.append("; ");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Creates a new Context object
 	 * 
 	 * @param targetSchemaName
 	 *            The name of the target schema
 	 * @param targetGraphClassName
 	 *            The name of the target graph class
 	 */
 	public Context(String targetSchemaName, String targetGraphClassName) {
 		this.targetSchemaName = targetSchemaName;
 		this.targetGraphClassName = targetGraphClassName;
 
 		// Check if the target schema is already present and we can thus skip
 		// the SCHEMA phase.
 		try {
 			Class<?> schemaClass = M1ClassManager.instance(targetSchemaName)
 					.loadClass(targetSchemaName);
 			Method schemaInstanceMethod = schemaClass.getMethod("instance");
 			targetSchema = (Schema) schemaInstanceMethod.invoke(null);
 		} catch (Exception e) {
 			// Failing is ok here.
 		}
 		// Do it here, cause that takes some time. We don't want to have that
 		// counted to the transformation time...
 		ensureGreqlEvaluator();
 	}
 
 	/**
 	 * Creates a new Context object with the given target schema.
 	 */
 	public Context(Schema targetSchema) {
 		this.targetSchema = targetSchema;
 		this.targetSchemaName = targetSchema.getQualifiedName();
 		// Do it here, cause that takes some time. We don't want to have that
 		// counted to the transformation time...
 		ensureGreqlEvaluator();
 
 	}
 
 	/**
 	 * Creates a new Context object with the given graph set as source and
 	 * target. So this is useful only for in-place transforms.
 	 */
 	public Context(Graph g) {
 		setTargetGraph(g);
 		setSourceGraph(g);
 		// Do it here, cause that takes some time. We don't want to have that
 		// counted to the transformation time...
 		ensureGreqlEvaluator();
 	}
 
 	/**
 	 * @param aec
 	 *            the AttributedElementClass for which to get the archMap
 	 *            mappings
 	 * @return a map from target graph elements (images) to their archetypes
 	 */
 	public final PMap<AttributedElement, Object> getArch(
 			AttributedElementClass aec) {
 		PMap<AttributedElement, Object> result = archMap.get(aec);
 		if (result == null) {
 			result = Empty.orderedMap();
 			archMap.put(aec, result);
 		}
 		return result;
 	}
 
 	/**
 	 * @param aec
 	 *            the AttributedElementClass for which to get the archMap
 	 *            mappings
 	 * @return a map from archetypes to target graph elements, which are their
 	 *         images
 	 */
 	public final PMap<Object, AttributedElement> getImg(
 			AttributedElementClass aec) {
 		PMap<Object, AttributedElement> result = imgMap.get(aec);
 		if (result == null) {
 			result = Empty.orderedMap();
 			imgMap.put(aec, result);
 		}
 		return result;
 	}
 
 	/**
 	 * Ensures that theres a function for this attributed element class, even
 	 * though this function may be empty.
 	 * 
 	 * @param aec
 	 *            the AttributedElementClass for which to ensure the
 	 *            archMap/imgMap mappings
 	 */
 	final void ensureMappings(AttributedElementClass aec) {
 		getImg(aec);
 		getArch(aec);
 		// validateMappings();
 	}
 
 	/**
 	 * Ensures that theres a function for all attributed elements in the target
 	 * schema.
 	 */
 	public final void ensureAllMappings() {
 		ensureMappings(targetSchema.getGraphClass());
 		for (AttributedElementClass gec : targetSchema.getGraphClass()
 				.getGraphElementClasses()) {
 			ensureMappings(gec);
 		}
 	}
 
 	public final void printImgMappings() {
 		System.out.println("Image Mappings:");
 		for (Entry<AttributedElementClass, PMap<Object, AttributedElement>> e : imgMap
 				.entrySet()) {
 			AttributedElementClass aec = e.getKey();
 			PMap<Object, AttributedElement> img = e.getValue();
 			if (aec.isInternal()) {
 				continue;
 			}
 			System.out.println("Mappings for: " + aec.getQualifiedName());
 			for (Entry<Object, AttributedElement> entry : img.entrySet()) {
 				System.out.println("    " + entry.getKey() + " ==> "
 						+ entry.getValue());
 			}
 		}
 	}
 
 	final void addMapping(AttributedElementClass attrElemClass,
 			Object archetype, AttributedElement image) {
 		addMappingToClass(attrElemClass, archetype, image);
 		addMappingsToSuperClasses(attrElemClass, archetype, image);
 	}
 
 	private final void addMappingsToSuperClasses(
 			final AttributedElementClass subClass, final Object archetype,
 			final AttributedElement image) {
 		for (AttributedElementClass superClass : subClass.getAllSuperClasses()) {
 			if (superClass.isInternal()) {
 				continue;
 			}
 			addMappingToClass(superClass, archetype, image);
 		}
 	}
 
 	private final void addMappingToClass(AttributedElementClass attrElemClass,
 			Object archetype, AttributedElement image) {
 		addArchMapping(attrElemClass, image, archetype);
 		addImgMapping(attrElemClass, archetype, image);
 	}
 
 	private void addArchMapping(AttributedElementClass attrElemClass,
 			AttributedElement image, Object archetype) {
 		PMap<AttributedElement, Object> map = archMap.get(attrElemClass);
 		if (map.containsKey(image)) {
 			throw new GReTLBijectionViolationException(this, "'"
 					+ image
 					+ "' already maps to '"
 					+ map.get(image)
 					+ "' in "
 					+ toGReTLVarNotation(attrElemClass.getQualifiedName(),
 							GReTLVariableType.ARCH)
 					+ "! You wanted to create an archMap mapping from '"
 					+ image + "' to '" + archetype + "'.");
 		}
 
 		// everything is fine
 		archMap.remove(map);
 		map = map.plus(image, archetype);
 		archMap.put(attrElemClass, map);
 	}
 
 	private void addImgMapping(AttributedElementClass attrElemClass,
 			Object archetype, AttributedElement image) {
 		PMap<Object, AttributedElement> map = imgMap.get(attrElemClass);
 		if (map.containsKey(archetype)) {
 			throw new GReTLBijectionViolationException(this, "'"
 					+ archetype
 					+ "' already maps to '"
 					+ map.get(archetype)
 					+ "' in "
 					+ toGReTLVarNotation(attrElemClass.getQualifiedName(),
 							GReTLVariableType.IMG)
 					+ "! You wanted to create an imgMap mapping from '"
 					+ archetype + "' to '" + image + "'.");
 		}
 
 		// everything is fine
 		imgMap.remove(map);
 		map = map.plus(archetype, image);
 		imgMap.put(attrElemClass, map);
 	}
 
 	private Random uniqueSeed = new Random();
 
 	/**
 	 * @return a String that is guaranteed to be unique (used for implicit
 	 *         archetypes)
 	 */
 	public String getUniqueString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<--[");
 		sb.append(uniqueSeed.nextInt());
 		sb.append("]--[");
 		sb.append(System.currentTimeMillis());
 		sb.append("]-->");
 		return sb.toString();
 	}
 
 	public final void validateMappings() {
 		if (imgMap.size() != archMap.size()) {
 			@SuppressWarnings("rawtypes")
 			Map m = imgMap;
 			@SuppressWarnings("rawtypes")
 			Map o = archMap;
 			if (archMap.size() > imgMap.size()) {
 				m = archMap;
 				o = imgMap;
 			}
 			@SuppressWarnings("unchecked")
 			Set<AttributedElementClass> keySet = m.keySet();
 			for (AttributedElementClass aec : keySet) {
 				if (!o.containsKey(aec)) {
 					System.err.println(toGReTLVarNotation(aec
 							.getQualifiedName(),
 							(o == archMap) ? GReTLVariableType.ARCH
 									: GReTLVariableType.IMG)
 							+ " is missing");
 				}
 			}
 			throw new GReTLBijectionViolationException(this,
 					"The imgMap and archMap mappings aren't valid! "
 							+ "The sizes of imgMap (" + imgMap.size()
 							+ ") and archMap (" + archMap.size()
 							+ ") don't match!");
 		}
 		for (AttributedElementClass aec : archMap.keySet()) {
 			if (!imgMap.containsKey(aec)) {
 				throw new GReTLBijectionViolationException(this,
 						"The imgMap and archMap mappings aren't valid! "
 								+ "imgMap contains no mappings for '"
 								+ aec.getQualifiedName() + "'!");
 			}
 			@SuppressWarnings("rawtypes")
 			Map arch = archMap.get(aec);
 			@SuppressWarnings("rawtypes")
 			Map img = imgMap.get(aec);
 
 			if (arch.size() != img.size()) {
 				throw new GReTLBijectionViolationException(this,
 						"The imgMap and archMap mappings aren't valid! "
 								+ "The sizes of '"
 								+ toGReTLVarNotation(aec.getQualifiedName(),
 										GReTLVariableType.IMG)
 								+ "' and '"
 								+ toGReTLVarNotation(aec.getQualifiedName(),
 										GReTLVariableType.ARCH)
 								+ "' don't match!");
 			}
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			Set<Map.Entry> entries = arch.entrySet();
 			for (@SuppressWarnings("rawtypes")
 			Entry entry : entries) {
 				if (!img.containsKey(entry.getValue())) {
 					throw new GReTLBijectionViolationException(this,
 							"The imgMap and archMap mappings aren't valid! "
 									+ "imgMap contains no mapping for '"
 									+ entry.getValue() + "'!");
 				}
 				if (!img.get(entry.getValue()).equals(entry.getKey())) {
 					throw new GReTLBijectionViolationException(this,
 							"The imgMap and archMap mappings aren't valid! "
 									+ "imgMap is not inverse to archMap for '"
 									+ toGReTLVarNotation(
 											aec.getQualifiedName(),
 											GReTLVariableType.ARCH) + "'! "
 									+ "archMap: " + entry.getKey() + " --> "
 									+ entry.getValue() + ", but imgMap: "
 									+ entry.getValue() + " --> "
 									+ img.get(entry.getValue()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Swap this context object. E.g. make the current target graph the default
 	 * source graph and reinitialize all member vars such as archMap/imgMap.
 	 * This is mainly useful for chaining multiple transformations.
 	 * 
 	 * @return this context object itself
 	 */
 	public final Context swap() {
 		logger.info("Swapping context...");
 		logger.info("Old target schema name: "
 				+ targetSchema.getQualifiedName());
 
 		// forget the old source graphs
 		sourceGraphs.clear();
 
 		// make the target graph the default source graph and increase version
 		// number
 		Graph oldTargetGraph = targetGraph;
 		String newSchemaPackagePrefix = targetSchema.getPackagePrefix();
 		Pattern stepSuffix = Pattern.compile(".*(_v(\\d+))$");
 		Matcher matcher = stepSuffix.matcher(newSchemaPackagePrefix);
 		int step = 1;
 		if (matcher.matches()) {
 			String matchGroup = matcher.group(2);
 			step = Integer.parseInt(matchGroup);
 			newSchemaPackagePrefix = newSchemaPackagePrefix
 					.substring(0, newSchemaPackagePrefix.length()
 							- matcher.group(1).length());
 		}
 		step++;
 		newSchemaPackagePrefix += "_v" + step;
 		targetSchemaName = newSchemaPackagePrefix + "."
 				+ targetSchema.getName();
 		logger.info("New target schema name: " + targetSchemaName);
 		setSourceGraph(oldTargetGraph);
 
 		return reset(true);
 	}
 
 	/**
 	 * Reset this context, so that the same context can be passed to another
 	 * transformation. This means, everything except the source graph is
 	 * cleared.
 	 * 
 	 * @return the context
 	 */
 	public final Context reset(boolean forgetTargetSchema) {
 		// reinitialize outermost/phase
 		outermost = true;
 		phase = TransformationPhase.SCHEMA;
 
 		// forget target graph and schema
 		targetGraph = null;
 		if (forgetTargetSchema) {
 			targetSchema = null;
 		}
 
 		// clear archMap/imgMap
 		archMap.clear();
 		imgMap.clear();
 
 		// reset the GreqlEvaluator index cache, they prevent garbage
 		// collection!
 		GreqlEvaluator.resetGraphIndizes();
 		GreqlEvaluator.resetOptimizedSyntaxGraphs();
 
 		// clear imports/extra vars
 		greqlExtraVars.clear();
 		greqlImports.clear();
 
 		return this;
 	}
 
 	/**
 	 * Sets the (default) source graph for the transformation
 	 * 
 	 * @param sourceGraph
 	 *            the source graph
 	 */
 	public final void setSourceGraph(Graph sourceGraph) {
 		addSourceGraph(DEFAULT_SOURCE_GRAPH_ALIAS, sourceGraph);
 	}
 
 	/**
 	 * adds a source graph for the transformation
 	 * 
 	 * @param alias
 	 *            the alias to access this source graph (used as prefix #name#
 	 *            in semantic expressions)
 	 * @param sourceGraph
 	 *            the source graph
 	 */
 	public final void addSourceGraph(String alias, Graph sourceGraph) {
 		if (sourceGraphs.containsKey(alias)) {
 			throw new GReTLException(this,
 					"There's already a source graph with name '" + alias + "'.");
 		}
 
 		if (!alias.matches("\\w+")) {
 			throw new GReTLException(this, "Invalid source graph name '"
 					+ alias + "'. Must be only word characters.");
 		}
 
 		if (alias.equals(DEFAULT_TARGET_GRAPH_ALIAS)) {
 			throw new GReTLException(this, "Invalid source graph name '"
 					+ alias
 					+ "'. That's the default alias for the target graph.");
 		}
 
 		// If the target schema is already there, then it is of course ok to
 		// transform to that existing schema.
 		if (sourceGraph.getSchema().getQualifiedName().equals(targetSchemaName)
 				&& (targetSchema == null)) {
 			throw new SchemaException(
 					"The schema names of source and target have to be different. "
 							+ "Currently both are named '" + targetSchemaName
 							+ "'.");
 		}
 
 		sourceGraphs.put(alias, sourceGraph);
 	}
 
 	/**
 	 * @return a map, mapping aliases to source graphs
 	 */
 	public final Map<String, Graph> getSourceGraphs() {
 		return sourceGraphs;
 	}
 
 	public final Graph getSourceGraph() {
 		return getSourceGraph(DEFAULT_SOURCE_GRAPH_ALIAS);
 	}
 
 	public final Graph getSourceGraph(String alias) {
 		return sourceGraphs.get(alias);
 	}
 
 	/**
 	 * returns the target graph of the transformation if no target graph exists,
 	 * it will be created
 	 * 
 	 * @return the target graph
 	 */
 	public final Graph getTargetGraph() {
 		return targetGraph;
 	}
 
 	/**
 	 * @param targetGraph
 	 *            the targetGraph to set
 	 */
 	public final void setTargetGraph(Graph targetGraph) {
 		this.targetGraph = targetGraph;
 		targetSchema = targetGraph.getSchema();
 	}
 
 	/**
 	 * creates a blank target Schema
 	 */
 	final void createTargetSchema() {
 		String[] qn = SchemaImpl.splitQualifiedName(targetSchemaName);
 		targetSchema = new SchemaImpl(qn[1], qn[0]);
 		GraphClass gc = targetSchema.createGraphClass(targetGraphClassName);
 		ensureMappings(gc);
 	}
 
 	/**
 	 * creates the target graph from the target schema
 	 */
 	final void createTargetGraph() {
 		boolean targetSchemaIsCompiled = true;
 		try {
 			targetSchema.getGraphClass().getM1Class();
 		} catch (M1ClassAccessException e) {
 			targetSchemaIsCompiled = false;
 		}
 
 		if (!targetSchemaIsCompiled) {
 			if (targetSchemaCodeDirectory != null) {
 				try {
 					targetSchema.commit(targetSchemaCodeDirectory,
 							CodeGeneratorConfiguration.MINIMAL);
 				} catch (GraphIOException e1) {
 					e1.printStackTrace();
 				}
 			}
 			logger.info("Compiling schema '" + targetSchema.getQualifiedName()
 					+ "'...");
 			targetSchema.compile(CodeGeneratorConfiguration.MINIMAL);
 		} else {
 			logger.info("Schema '" + targetSchema.getQualifiedName()
 					+ "' is already compiled or in the CLASSPATH...");
 		}
 
 		Method graphCreateMethod = targetSchema
 				.getGraphCreateMethod(ImplementationType.STANDARD);
 
 		try {
 			targetGraph = (Graph) graphCreateMethod
 					.invoke(null, null, 500, 500);
 			targetSchema = targetGraph.getSchema();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new GReTLException(this,
 					"Something failed when creating the target graph!", e);
 		}
 
 		for (Entry<String, Graph> e : sourceGraphs.entrySet()) {
 			String sourceGraphName = e.getKey();
 			Graph sourceGraph = e.getValue();
 
 			if (sourceGraphName.equals(DEFAULT_SOURCE_GRAPH_ALIAS)) {
 				// The default source graph is the archetype of the new target
 				// graph.
 				addMapping(targetGraph.getGraphClass(), sourceGraph,
 						targetGraph);
 			}
 		}
 	}
 
 	void initializeEnumValue2LiteralMaps() {
 		// Make enum constants accessible via maps of the form enum_QName:
 		// String -> Object
 		for (EnumDomain d : targetGraph.getSchema().getEnumDomains()) {
 			try {
 				Class<?> myEnum = Class.forName(targetSchema.getPackagePrefix()
 						+ "." + d.getQualifiedName(), false, targetSchema
 						.getClass().getClassLoader());
 
 				Method valueOf = myEnum.getMethod("valueOf",
 						new Class<?>[] { String.class });
 				PMap<String, Object> map = Empty.orderedMap();
 				for (String c : d.getConsts()) {
 					Object constant = valueOf.invoke(null, new Object[] { c });
 					map = map.plus(c, constant);
 				}
 				setGReQLVariable(
 						toGReTLVarNotation(d.getQualifiedName(),
 								GReTLVariableType.ENUM), map);
 			} catch (ClassNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public enum GReTLVariableType {
 		ARCH, IMG, ENUM
 	}
 
 	public static String toGReTLVarNotation(String qualifiedName,
 			GReTLVariableType type) {
 		String qName = NamedElementImpl.toUniqueNameNotation(qualifiedName);
 		return type.toString().toLowerCase() + "_" + qName;
 	}
 
 	public final <T> T evaluateGReQLQuery(String greqlExpression) {
 		if (phase == TransformationPhase.SCHEMA) {
 			return null;
 		}
 
 		String name = DEFAULT_SOURCE_GRAPH_ALIAS;
 		// System.out.println("greqlExp = '" + greqlExpression + "'.");
 		Matcher m = QUERY_GRAPH_ALIAS_PATTERN.matcher(greqlExpression);
 		if (m.matches()) {
 			greqlExpression = greqlExpression.replace(m.group(1), "");
 			name = m.group(2);
 
 			if (name.equals(DEFAULT_TARGET_GRAPH_ALIAS)) {
 				return evalGReQLQuery(greqlExpression, targetGraph);
 			}
 
 			if (!sourceGraphs.containsKey(name)) {
 				throw new GReTLException(this, "No source graph with name '"
 						+ name + "'.");
 			}
 		}
 
 		return evalGReQLQuery(greqlExpression, sourceGraphs.get(name));
 	}
 
 	@SuppressWarnings("unchecked")
 	private final <T> T evalGReQLQuery(String semanticExpression, Graph graph) {
 		if (phase == TransformationPhase.SCHEMA) {
 			return null;
 		}
 
 		if (semanticExpression.isEmpty()) {
 			logger.severe("The given semantic expression is empty!  Fix that!");
 			return null;
 		}
 
 		PMap<String, Object> greqlMapping = getGreqlVariablesNeededByQuery(semanticExpression);
 		StringBuilder sb = new StringBuilder();
 
 		if (sourceGraphs.values().contains(graph)) {
 			sb.append(getGreqlImportString(graph));
 		}
 
 		sb.append(getGreqlUsingString(greqlMapping));
 		sb.append(semanticExpression);
 		String query = sb.toString();
 		logger.finest("GReQL: " + semanticExpression);
 
 		ensureGreqlEvaluator();
 		eval.setDatagraph(graph);
 		eval.setQuery(query);
 		eval.setVariables(greqlMapping);
 
 		// eval.setOptimize(false);
 
 		eval.startEvaluation();
 
 		// log.fine("GReQL result: " + result);
 		return (T) eval.getResult();
 	}
 
 	/**
 	 * Ensure that the {@link GreqlEvaluator} <code>eval</code> exists.
 	 */
 	private void ensureGreqlEvaluator() {
 		if (eval == null) {
 			eval = new GreqlEvaluator((String) null, null, null);
 		}
 	}
 
 	private final PMap<String, Object> getGreqlVariablesNeededByQuery(
 			String query) {
 		PMap<String, Object> result = Empty.orderedMap();
 
 		for (String extraVar : greqlExtraVars.keySet()) {
 			if (query.contains(extraVar)) {
 				result = result.plus(extraVar, greqlExtraVars.get(extraVar));
 			}
 		}
 
 		for (Entry<AttributedElementClass, PMap<AttributedElement, Object>> e : archMap
 				.entrySet()) {
 			String varName = toGReTLVarNotation(e.getKey().getQualifiedName(),
 					GReTLVariableType.ARCH);
 			if (query.contains(varName)) {
 				result = result.plus(varName, e.getValue());
 			}
 		}
 
 		for (Entry<AttributedElementClass, PMap<Object, AttributedElement>> e : imgMap
 				.entrySet()) {
 			String varName = toGReTLVarNotation(e.getKey().getQualifiedName(),
 					GReTLVariableType.IMG);
 			if (query.contains(varName)) {
 				result = result.plus(varName, e.getValue());
 			}
 		}
 
 		return result;
 	}
 
 	private final String getGreqlUsingString(Map<String, Object> greqlMapping) {
 		if (greqlMapping.size() == 0) {
 			return "";
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append("using ");
 		boolean first = true;
 		for (String name : new TreeSet<String>(greqlMapping.keySet())) {
 			if (first) {
 				first = false;
 			} else {
 				sb.append(", ");
 			}
 			sb.append(name);
 		}
 		sb.append(": ");
 		return sb.toString();
 	}
 
 	public final void storeTrace(String fileName) {
 		// TODO: Implement me!!!
 	}
 
 	public final void restoreTrace(String fileName) {
 		// TODO: Implement me!!!
 	}
 
 }
