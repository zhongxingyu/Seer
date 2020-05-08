 package assemblernator;
 
 import java.io.ByteArrayOutputStream;
 
 import ulutil.FrankenOutput;
 
 /**
  * Test case generator for the link phase.
  * 
  * @author Josh Ventura
  * @date May 29, 2012; 11:44:10 AM
  */
 public class LinkerTest {
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 29, 2012; 11:42:31 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param lma
 	 *            The linker modules to link.
 	 * @param baos
 	 *            Stream to which loader file will be written, or null.
 	 * @return The HTML test case.
 	 */
 	public static String getTestCase(LinkerModule[] lma,
 			ByteArrayOutputStream baos) {
 		String res = "\n\n<h1>Linker Load Phase</h1>\n\n";
 		FrankenOutput uos = new FrankenOutput();
 
 		if (baos == null)
 			baos = new ByteArrayOutputStream();
 
 		String cst = Linker.link(lma, baos, uos);

 		for (LinkerModule lm : lma)
 			res += lm.progName + "\n" + lm.toString() + "\n";
 
		res += "\n\n<h1>Linking Phase: Combined Symbol Table</h1>\n\n"
 				+ (cst.isEmpty() ? "<i>&lt;The combined symbol table is empty.&gt;</i>"
 						: cst);
 		if (!uos.appme.isEmpty())
 			res += "\n\n<h2>Isolated Linker Errors</h2>\n" + uos.appme;
 
 		res += "\n\n<h2>Complete loader file</h2>\n\n<pre>"
 				+ (new String(baos.toByteArray())).replaceAll(lma[0].progName
 						+ ":", lma[0].progName + ":\n") + "</pre>";
 
 		return res;
 	}
 
 }
