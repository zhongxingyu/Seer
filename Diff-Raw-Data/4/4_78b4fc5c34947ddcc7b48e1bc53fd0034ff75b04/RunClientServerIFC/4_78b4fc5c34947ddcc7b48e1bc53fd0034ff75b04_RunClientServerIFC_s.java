 import java.io.IOException;
 
 import joana.sdg.SDGNode;
 
 import com.ibm.wala.ipa.cha.ClassHierarchyException;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
 
 import edu.kit.ifc.IFC;
 import edu.kit.ifc.IFC.Annotation;
 import edu.kit.ifc.IFC.IFCConfig;
 import edu.kit.ifc.IFC.SecurityLabel;
 import edu.kit.ifc.IFC.SecurityPolicy;
 import edu.kit.pp.mojo.chasdg.BytecodeLocation;
 
 /**
  * <p>This class runs the non-interference check for the client server encryption example in the paper
 * "A Framework for the Cryptographic Verification of Java-like Programs" at CSF 2012.</p>
  * 
  * <p>It uses a simple interface (<tt>edu.kit.ifc.IFC</tt>) to run information flow checks with our Joana 
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
 public class RunClientServerIFC {
 
 	public static void main(final String[] args) throws ClassHierarchyException, IOException, UnsoundGraphException,
 			CancelException {
 		final IFCConfig configClientOnly = IFCConfig.create("./example/bin",
 				"Lde/uni/trier/infsec/protocols/simplevoting/ClientOnlySetup", SecurityPolicy.CONFIDENTIALITY);
 		
 		// annotate input (first parameter) of method untrusted output as leaked to low output
 		configClientOnly.addAnnotation(Annotation.create(
 				"de.uni.trier.infsec.environment.Environment.untrustedOuput(I)V",
 				BytecodeLocation.ROOT_PARAM_PREFIX + "0",
 				SDGNode.Kind.FORMAL_IN,
 				SecurityLabel.LOW
 		));
 		
 		// annotate references to static variable ClientOnlySetup.secret as high input
 		configClientOnly.addAnnotation(Annotation.create(
 				"de.uni.trier.infsec.protocols.simplevoting.ClientOnlySetup.main([Ljava/lang/String;)V",
 				"de.uni.trier.infsec.protocols.simplevoting.ClientOnlySetup.secret",
 				SDGNode.Kind.EXPRESSION,
 				SDGNode.Operation.REFERENCE,
 				SecurityLabel.HIGH
 		));
 		
 		configClientOnly.verboseAnnotations = "true".equals(System.getProperty("verbose.annotations"));
 		configClientOnly.verboseTimings = "true".equals(System.getProperty("verbose.timings"));
 		
 		IFC.run(configClientOnly);
 	}
 
 }
