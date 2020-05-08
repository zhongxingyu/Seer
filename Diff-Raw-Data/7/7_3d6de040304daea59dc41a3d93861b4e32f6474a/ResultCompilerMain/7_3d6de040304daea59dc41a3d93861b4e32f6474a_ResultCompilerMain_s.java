 package sort;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.URISyntaxException;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import members.Competitor;
 import result.CvsReader;
 import result.Parser;
 import result.ParserException;
 
 public class ResultCompilerMain {
 
 	public static final String STARTTIMES = "start";
 	public static final String NAMEFILE = "namn";
 	public static final String FINISHTIMES = "slut";
 	public static final String RESULTFILE = "resultat";
 	public static final String EXTENSION = ".txt";
 
 	/**
 	 * 
 	 * Read input files and create list with competitors, and call printResults
 	 * to print the results to the output file.
 	 * 
 	 * @throws URISyntaxException
 	 * 
 	 */
 	public static void main(String[] args) throws URISyntaxException {
 		CodeSource codeSource = ResultCompilerMain.class.getProtectionDomain()
 				.getCodeSource();
 		File jarFile = new File(codeSource.getLocation().toURI().getPath());
 		String jarDir = jarFile.getParentFile().getPath();
 
 		Parser p = new Parser();
 
 		String startPath = jarDir + File.separator + STARTTIMES + EXTENSION;
 		String namePath = jarDir + File.separator + NAMEFILE + EXTENSION;
 		String finishPathPart = jarDir + File.separator + FINISHTIMES;
 		String resultPath = jarDir + File.separator + RESULTFILE + EXTENSION;
 
 		CvsReader startReader = new CvsReader(startPath);
 		CvsReader nameReader = new CvsReader(namePath);
 
 		ArrayList<CvsReader> endReaderList = new ArrayList<CvsReader>();
 		for (int i = 0; new File(finishPathPart + i + EXTENSION).exists(); i++) {
 			endReaderList.add(new CvsReader(finishPathPart + i + EXTENSION));
 		}
		// CvsReader nameReader = new CvsReader(jarDir + File.separator +
		// NAMEFILE);
 
 		Map<Integer, Competitor> map = new HashMap<Integer, Competitor>();
 
 		try {
 			p.parse(startReader.readAll(), map);
 			for (CvsReader end : endReaderList) {
 				p.parse(end.readAll(), map);
				// System.out.println("DERP");
 			}
 			// if(new File(jarDir + File.separator + NAMEFILE).exists()){
 			p.parse(nameReader.readAll(), map);
 			// }
			StdCompetitorPrinter printer = new StdCompetitorPrinter();
 			printer.printResults(new ArrayList<Competitor>(map.values()),
 					resultPath);
 		} catch (FileNotFoundException e) {
 			System.exit(-1);
 		} catch (ParserException e) {
 			System.exit(-1);
 		}
 	}
 
 }
