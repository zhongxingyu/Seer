 package edu.kit.ifc;
 import edu.kit.pp.mojo.chasdg.Main;
 import edu.kit.pp.mojo.chasdg.SDGBuilder.ExceptionAnalysis;
 import edu.kit.pp.mojo.chasdg.SDGBuilder.FieldPropagation;
 import edu.kit.pp.mojo.chasdg.SDGBuilder.PointsToPrecision;
 import edu.kit.pp.mojo.chasdg.graphs.WriteGraphToDot;
 import gnu.trove.set.TIntSet;
 import gnu.trove.set.hash.TIntHashSet;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import joana.ifc.core.SecurityNode;
 import joana.ifc.core.SecurityNode.SecurityNodeFactory;
 import joana.ifc.core.conc.PossibilisticNIChecker;
 import joana.ifc.core.violations.Violation;
 import joana.ifc.lattice.IEditableLattice;
 import joana.ifc.lattice.LatticeUtil;
 import joana.ifc.lattice.WrongLatticeDefinitionException;
 import joana.sdg.SDGNode;
 import joana.sdg.SDGSerializer;
 import jsdg.util.Log;
 import jsdg.util.Log.LogLevel;
 
 import com.ibm.wala.ipa.cha.ClassHierarchyException;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
 
 /**
  * <p>A simple interface to run information flow checks with our Joana 
  * (<a href="http://pp.info.uni-karlsruhe.de/project.php?id=30">pp.info.uni-karlsruhe.de/project.php?id=30</a>)
  * and IFC4MC 
  * (<a href="http://pp.info.uni-karlsruhe.de/projects/rs3/rs3.php">pp.info.uni-karlsruhe.de/projects/rs3/rs3.php</a>)
  * tool on Java 1.4 Bytecode programs.
  * It is possible to check programs for confidentiality or integrity.</p>
  * 
  * <p>Please keep in mind that these tools are research prototypes and may contain errors. Feel free to inform us of
  * any bugs you encountered at 
  * <a href="https://pp.info.uni-karlsruhe.de/joana-bugs">https://pp.info.uni-karlsruhe.de/joana-bugs</a>.</p>
  * 
  * <p>You may also want to check out our other information flow related software at
  * <a href="http://pp.info.uni-karlsruhe.de/~grafj/ifc/">pp.info.uni-karlsruhe.de/~grafj/ifc/</a>.
  * There is a viewer for our intermediate program representation, namely system dependence graphs.
  * The viewer is called GraphViewer and it can be used to take a look at the <tt>.pdg</tt> files that are created
  * during the analysis.</p>
  * 
  * <p>This code and most of the additional libraries have been developed as part of the research projects
  * "VALSOFT/Joana" (founded by the German DFG) as well as "Information Flow Control for Mobile Components Based
  * on Precise Analysis for Parallel Programs - IFC4MC" (founded by the German DFG as part of the Priority Program 1496
  * "Reliably Secure Software Systems – RS3").
  * You may find more information about this software at the homepage of the
  * <b>Programming Paradigms Group - IPD Snelting</b> at the <b>Karlsruhe Institute of Technology</b>
  * (<a href="http://pp.info.uni-karlsruhe.de/">pp.info.uni-karlsruhe.de</a>).
  * Feel free to contact us for further information about licensing, usage and research related issues.</p>
  * 
  * @author Juergen Graf <juergen.graf@gmail.com>
  *
  */
 public final class IFC {
 
 	private IFC() {}
 	
 	public enum SecurityLabel {
 		HIGH, LOW
 	}
 	
 	public enum SecurityPolicy {
 			// Attacker provided (low) input cannot change/influence important (high) computations.
 			// => Low inputs may not influence high outputs.
 		INTEGRITY,
 			// Secret (high) information is not leaked to public/attacker accessible (low) output.
 			// => High inputs may not influence low outputs.
 		CONFIDENTIALITY,
 	}
 	
 	public static class Annotation {
 		public final String methodSignature;
 		public final String statementSignature;
 		public final SDGNode.Kind kind;
 		public final SDGNode.Operation op;	// maybe null
 		public final SecurityLabel label;
 		private final Set<SecurityNode> matched = new HashSet<SecurityNode>();
 		private final TIntSet matchedMethodIds = new TIntHashSet();
 
 		public static Annotation create(final String methodSignature, final String statementSignature,
 				final SDGNode.Kind kind, final SecurityLabel label) {
 			return create(methodSignature, statementSignature, kind, null, label);
 		}
 		
 		public static Annotation create(final String methodSignature, final String statementSignature,
 				final SDGNode.Kind kind, final SDGNode.Operation op, final SecurityLabel label) {
 			return new Annotation(methodSignature, statementSignature, kind, op, label);
 		}
 		
 		private Annotation(final String methodSignature, final String statementSignature, final SDGNode.Kind kind,
 				final SDGNode.Operation op, final SecurityLabel label) {
 			if (methodSignature == null) {
 				throw new IllegalArgumentException("methodSignature is null.");
 			} else if (statementSignature == null) {
 				throw new IllegalArgumentException("statementSignature is null.");
 			} else if (kind == null) {
 				throw new IllegalArgumentException("SDG node kind is null.");
 			} else if (label == null) {
 				throw new IllegalArgumentException("security label is null.");
 			}
 			
 			this.methodSignature = methodSignature;
 			this.statementSignature = statementSignature;
 			this.kind = kind;
 			this.label = label;
 			this.op = op;		// maybe null
 		}
 
 		public String toString() {
 			return "Annotation(" + label + "): method(" + methodSignature + "), statement(" + statementSignature
 					+ "), kind(" + kind + ")" + (op == null ? "" : (", op(" + op + ")")) + " - " + matched.size()
 					+ " matches";
 		}
 		
 		public String toVerboseString() {
 			final StringBuilder sb = new StringBuilder(toString());
 			
 			for (final SecurityNode n : matched) {
 				sb.append("\n\t" + n.getId() + "|" + n.getKind() + "|" + n.getLabel() + "|" + n.getBytecodeName());
 			}
 			
 			return sb.toString();
 		}
 		
 		public void addMatchedMethod(final int methodId) {
 			matchedMethodIds.add(methodId);
 		}
 		
 		public boolean isMatchedMethod(final int methodId) {
 			return matchedMethodIds.contains(methodId);
 		}
 		
 		public void addMatch(final SecurityNode n) {
 			if (n == null) {
 				throw new IllegalArgumentException("do not add null.");
 			}
 			
 			matched.add(n);
 		}
 		
 		public boolean hasMatched() {
 			return !matched.isEmpty();
 		}
 		
 		public Collection<SecurityNode> getMatched() {
 			return Collections.unmodifiableSet(matched);
 		}
 	}
 	
 	public static class IFCConfig {
 
 		public boolean verboseAnnotations = false;
 		public boolean verboseTimings = false;
 		public final String classpath;
 		public final String mainClassSignature;
 		public final SecurityPolicy policy;
 		private final List<Annotation> annotations = new LinkedList<IFC.Annotation>();
 		
 		private IFCConfig(final String classpath, final String mainClassSignature, final SecurityPolicy policy) {
 			if (classpath == null) {
 				throw new IllegalArgumentException("classpath is null.");
 			} else if (mainClassSignature == null) {
 				throw new IllegalArgumentException("main class signature is null.");
 			} else if (policy == null) {
 				throw new IllegalArgumentException("security policy is null.");
 			}
 			
 			this.classpath = classpath;
 			this.mainClassSignature = mainClassSignature;
 			this.policy = policy;
 		}
 
 		public static IFCConfig create(final String classpath, final String mainClassSignature,
 				final SecurityPolicy policy) {
 			return create(classpath, mainClassSignature, policy, null);
 		}
 		
 		public static IFCConfig create(final String classpath, final String mainClassSignature,
 				final SecurityPolicy policy, final Annotation[] annotations) {
 			final IFCConfig cfg = new IFCConfig(classpath, mainClassSignature, policy);
 			
 			if (annotations != null) {
 				for (final Annotation annot : annotations) {
 					cfg.addAnnotation(annot);
 				}
 			}
 			
 			return cfg;
 		}
 		
 		public void addAnnotation(final Annotation annot) {
 			if (annot == null) {
 				throw new IllegalArgumentException("trying to add null to the list of annotations.");
 			}
 			
 			annotations.add(annot);
 		}
 		
 		public Collection<Annotation> getAnnotations() {
 			return Collections.unmodifiableList(annotations);
 		}
 		
 		public boolean hasAnnotations() {
 			return !annotations.isEmpty();
 		}
 	}
 	
 	/**
 	 * Runs an information flow check using the given configuration. Returns <tt>true</tt> if the program is considered
 	 * save, returns <tt>false</tt> if a leak has been found.
 	 * @param config The information flow configuration.
 	 * @return <tt>true</tt> if the program is considered save, or <tt>false</tt> if a leak has been found.
 	 */
 	public static boolean run(final IFCConfig config) throws ClassHierarchyException, IOException, UnsoundGraphException,
 			CancelException {
 		final Collection<Violation> vios = computeIFC(config);
 		
 		if (vios.size() == 0) {
			System.out.println("OK: Information flow considered save. The program is non-interferent.");
 		} else {
 			switch (config.policy) {
 			case CONFIDENTIALITY: {
				System.out.println("WARNING: Information flow considered unsave. It MAY leak high information.");
 			} break;
 			case INTEGRITY: {
				System.out.println("WARNING: Information flow considered unsave. Public (low) input"
 						+ "  MAY influence confidential (high) information.");
 			} break;
 			default:
 				throw new IllegalStateException("unknown security policy: " + config.policy);
 			}
 		}
 		
 		return vios.isEmpty();
 	}
 	
 	private static long buildSDG(final IFCConfig config, final String mainClassSimpleName, final String outputSDGfile)
 			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
 		Log.setMinLogLevel(LogLevel.WARN);
 
 		System.out.println("Analyzing class files from '" + new File(config.classpath).getAbsolutePath() + "'");
 		
 		final Main.Config cfg = new Main.Config(mainClassSimpleName,
 				config.mainClassSignature.substring(1) + ".main([Ljava/lang/String;)V", 
 				config.classpath,
 				PointsToPrecision.OBJECT_SENSITIVE,
 				ExceptionAnalysis.INTRAPROC, false, Main.STD_EXCLUSION_REG_EXP, null,
 				"./lib/jSDG-stubs-jre1.4.jar", null, ".",
 				FieldPropagation.OBJ_GRAPH);
 		
 		final joana.sdg.SDG sdg = Main.compute(System.out, cfg);
 		
 		final long t2 = System.currentTimeMillis();
 		
 		System.out.print("Saving SDG to " + outputSDGfile + "... ");
 		final BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(outputSDGfile));
 		SDGSerializer.toPDGFormat(sdg, bOut);
 		System.out.println("done.");
 		
 		return t2;
 	}
 	
 	public static Collection<Violation> computeIFC(final IFCConfig config) throws ClassHierarchyException, IOException,
 			UnsoundGraphException, CancelException {
 		final long t1 = System.currentTimeMillis();
 		
 		final String mainClassSimpleName = WriteGraphToDot.sanitizeFileName(config.mainClassSignature.substring(1));
 		final String outputSDGfile = mainClassSimpleName + "-main.ifc.pdg";
 
 		final long t2 = buildSDG(config, mainClassSimpleName, outputSDGfile);
 
 		System.out.println("Checking " + outputSDGfile);
 		final joana.sdg.SDG sdgSec = joana.sdg.SDG.readFrom(outputSDGfile, new SecurityNodeFactory());
 		
 		final long t3 = System.currentTimeMillis();
 
 		System.out.print("Annotating SDG nodes... ");
 		final IEditableLattice<String> lat = createLattice("low<=high");
 		final int matches = matchAnnotationsWithNodes(sdgSec, config.getAnnotations());
 		System.out.print("(" + matches + " nodes) ");
 		
 		// set required and provided
 		annotateSecurityNodes(config.getAnnotations(), config.policy);
 		
 		System.out.println("done.");
 
 		if (config.verboseAnnotations) {
 			System.out.println();
 			System.out.println(">>>>>>>> Annotations");
 			for (final Annotation annot : config.getAnnotations()) {
 				System.out.println(annot.toVerboseString());
 			}
 			System.out.println("<<<<<<<< Annotations");
 			System.out.println();
 		}
 		
 		final long t4 = System.currentTimeMillis();
 		
 		System.out.print("Checking information flow... ");
 		
 		final PossibilisticNIChecker ifc = new PossibilisticNIChecker(sdgSec, lat);
 		final Collection<Violation> vios = ifc.checkIFlow();
 
 		System.out.println("(" + vios.size() + " violations) done.");
 		
 		final long t5 = System.currentTimeMillis();
 
 		if (config.verboseTimings) {
 			System.out.println();
 			System.out.println(">>>>>>>> Timings");
 			System.out.println("time start to finish: " + (t5 - t1) + " ms");
 			System.out.println("time build sdg:       " + (t2 - t1) + " ms");
 			System.out.println("time run ifc:         " + (t5 - t4) + " ms");
 			System.out.println("time annotations:     " + (t4 - t3) + " ms");
 			System.out.println("time io:              " + (t3 - t2) + " ms");
 			System.out.println("<<<<<<<< Timings");
 			System.out.println();
 		}
 
 		return Collections.unmodifiableCollection(vios);
 	}
 	
 	private static void annotateSecurityNodes(final Collection<Annotation> annotations, final SecurityPolicy policy) {
 		for (final Annotation annot : annotations) {
 			for (final SecurityNode n : annot.getMatched()) {
 				switch (policy) {
 				case CONFIDENTIALITY: {
 					switch (annot.label) {
 					case HIGH: {
 						n.setProvided("high");
 					} break;
 					case LOW: {
 						n.setRequired("low");
 					} break;
 					default:
 						throw new IllegalStateException("unknown security label: " + annot.label);
 					}
 				} break;
 				case INTEGRITY: {
 					switch (annot.label) {
 					case HIGH: {
 						n.setRequired("high");
 					} break;
 					case LOW: {
 						n.setProvided("low");
 					} break;
 					default:
 						throw new IllegalStateException("unknown security label: " + annot.label);
 					}
 				} break;
 				default:
 					throw new IllegalStateException("unknown security policy: " + policy);
 				}
 			}
 		}
 	}
 	
 	private static int matchAnnotationsWithNodes(final joana.sdg.SDG sdgSec, final Collection<Annotation> annotations) {
 		int numberOfMatchedNodes = 0;
 		
 		// match method signatures
 		for (final SDGNode n : sdgSec.vertexSet()) {
 			if (n.getKind() == SDGNode.Kind.ENTRY) {
 				for (final Annotation annot : annotations) {
 					if (annot.methodSignature.equals(n.getBytecodeMethod())) {
 						annot.addMatchedMethod(n.getProc());
 					}
 				}
 			}
 		}
 
 		for (final SDGNode n : sdgSec.vertexSet()) {
 			for (final Annotation annot : annotations) {
 				if (annot.kind == n.kind && annot.isMatchedMethod(n.getProc()) 
 						&& ((n.getBytecodeName() != null && n.getBytecodeName().contains(annot.statementSignature))
 								|| (n.getLabel() != null && n.getLabel().contains(annot.statementSignature)))
 						&& (annot.op == null || n.getOperation() == annot.op)) {
 					if (!(n instanceof SecurityNode)) {
 						throw new IllegalStateException("no security nodes in sdg - used wrong node factory? "
 								+ "try 'joana.sdg.SDG.readFrom(sdgfile, new SecurityNodeFactory())'");
 					}
 					
 					annot.addMatch((SecurityNode) n);
 					numberOfMatchedNodes++;
 				}
 			}
 		}
 		
 		return numberOfMatchedNodes;
 	}
 	
 	private static IEditableLattice<String> createLattice(final String latStr) {
 		final IEditableLattice<String> lattice;
 		
 		try {
 			lattice = LatticeUtil.loadLattice(latStr.replaceAll("\\s*,\\s*", "\n"));
 		} catch (WrongLatticeDefinitionException e) {
 			throw new IllegalArgumentException("Invalid lattice specification: " + e.getMessage());
 		}
 
 		return lattice;
 	}
 
 }
