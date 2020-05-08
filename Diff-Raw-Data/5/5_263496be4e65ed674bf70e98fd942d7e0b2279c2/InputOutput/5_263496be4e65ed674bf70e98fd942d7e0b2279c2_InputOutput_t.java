 package utils;
 
 import utils.ReadDicom;
 import ij.IJ;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.StreamTokenizer;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 public class InputOutput {
 	//
 	// Absolute VS relative path in Eclipse, my experiences:
 	// 1) relative path: is relative to the bin directory in Eclipse, i.e:
 	// C:\eclipse2\eclipse\workspace_fmri2\ControlliMensili\bin\test3\
 	// in relative path is \test3\
 	// 2) you can access to the files in the relative path with
 	// getClass().getResource(name) or can transform the relative path in
 	// absolute
 	//
 
 	public String findResource(String name) {
 		String path = getClass().getResource(name).getPath();
 		return path;
 
 	}
 
 	public static String absoluteToRelative(String absolutePath) {
 		int start = absolutePath.indexOf("bin");
 		String relativePath = absolutePath.substring(start + 3);
 		String outPath = UtilAyv.reverseSlash(relativePath);
 		return outPath;
 	}
 
 	/***
 	 * Lo scopo di questa utility  di estrarre dal file jar il prototipo del
 	 * file csv, che poi verr usato dal programma. Se il file esistesse gi,
 	 * l'estrazione non deve avvenire (cos  possibile modificare il file senza
 	 * dover intervenire sul file jar)
 	 * 
 	 * @param fileName
 	 * @param destinationPath
 	 * @return
 	 */
 	// public boolean findCSV(String fileName) {
 	//
 	// URL url3 = this.getClass().getResource("/contMensili/p10rmn_.class");
 	// File file3 = new File(url3.getPath());
 	// MyLog.waitHere("url3= " + url3.toString());
 	//
 	// String myString = url3.toString();
 	//
 	// int start = myString.indexOf("plugins");
 	// int end = myString.lastIndexOf("!");
 	// MyLog.waitHere("start= "+start+" end= "+end);
 	// String myPart1 = myString.substring(start, end);
 	// MyLog.waitHere("myPart1= " + myPart1);
 	//
 	// end = myPart1.lastIndexOf("/");
 	// MyLog.waitHere("start= "+0+" end= "+end);
 	// String myPart2 = myPart1.substring(0, end+1);
 	// MyLog.waitHere("myPart2= " + myPart2);
 	//
 	//
 	// String parentX = file3.getParent();
 	// MyLog.waitHere("--> parentX= " + parentX);
 	//
 	// String pathX = file3.getPath();
 	// MyLog.waitHere("--> pathX= " + pathX);
 	//
 	// String myPathName = parentX + "/" + fileName;
 	// MyLog.waitHere("--> myPathName= " + myPathName);
 	//
 	// // String home4 = url2.getFile();
 	// // MyLog.waitHere("home4= " + home4);
 	//
 	// // find destination
 	// boolean present = checkFile(parentX + "/" + fileName);
 	// if (present) {
 	// MyLog.waitHere("skip perch file gi esistente");
 	// return true;
 	// }
 	//
 	// // find resource
 	// URL url1 = this.getClass().getResource("/" + fileName);
 	// if (url1 == null) {
 	// MyLog.waitHere("file " + fileName + " not found in jar");
 	// return false;
 	// }
 	// String home1 = url1.getPath();
 	// File file1 = new File(home1);
 	// String home11 = file1.getParent();
 	// // File outFile = new File("plugins\\ContMensili\\" + fileName);
 	// File outFile = new File(myPart2 + fileName);
 	//
 	// try {
 	//
 	// InputStream is = this.getClass()
 	// .getResourceAsStream("/" + fileName);
 	// if (is == null)
 	// MyLog.waitHere("is==null");
 	// else
 	// MyLog.waitHere("is= " + is);
 	// try {
 	// MyLog.waitHere("is.available()= " + is.available());
 	// } catch (IOException e1) {
 	// // TODO Auto-generated catch block
 	// e1.printStackTrace();
 	// }
 	//
 	// MyLog.waitHere("outfile= " + outFile);
 	// FileOutputStream fos = new FileOutputStream(outFile);
 	// while (is.available() > 0) {
 	// fos.write(is.read());
 	// }
 	// fos.close();
 	// is.close();
 	// } catch (IOException e) {
 	// MyLog.waitHere("GULP TIRA ARIA DI GUAI");
 	// }
 	//
 	// present = checkFile(outFile.getPath());
 	// if (present)
 	// MyLog.waitHere("file estratto");
 	// else
 	// MyLog.waitHere("FALLIMENTO file non estratto");
 	//
 	// return present;
 	// }
 
 	public boolean findCSV(String fileName) {
 
 		// ricerca del path in cui andare a scrivere
 		URL url3 = this.getClass().getResource("/contMensili/p10rmn_.class");
 		// File file3 = new File(url3.getPath());
 		String myString = url3.toString();
 		int start = myString.indexOf("plugins");
 		int end = myString.lastIndexOf("!");
 		String myPart1 = myString.substring(start, end);
 		end = myPart1.lastIndexOf("/");
 		String myPart2 = myPart1.substring(0, end + 1);
 		// definizione del nome del file che andremo a scrivere
 		File outFile = new File(myPart2 + fileName);
 		// Viene testata l'esistenza del file, se esiste non lo si copia, cos
 		// vengono mantenute eventuali modifiche dell'utlizzatore
 		boolean present = checkFile(outFile.getPath());
 		if (present) {
 			// MyLog.waitHere("skip perch file gi esistente");
 			return true;
 		}
 		// ricerco la risorsa da copiare, perch qui arrivo solo se la risorsa
 		// non esiste al di fuori del file jar
 		URL url1 = this.getClass().getResource("/" + fileName);
 		if (url1 == null) {
 			MyLog.waitHere("file " + fileName + " not found in jar");
 			return false;
 		}
 		try {
 			// tento la copia
 			InputStream is = this.getClass()
 					.getResourceAsStream("/" + fileName);
 			FileOutputStream fos = new FileOutputStream(outFile);
 			while (is.available() > 0) {
 				fos.write(is.read());
 			}
 			fos.close();
 			is.close();
 		} catch (IOException e) {
 			MyLog.waitHere("ERRORE ACCESSO");
 		}
 		present = checkFile(outFile.getPath());
 		if (present) {
 			// MyLog.waitHere("file estratto");
 		} else {
 			MyLog.waitHere("FALLIMENTO, FILE NON COPIATO");
 		}
 		return present;
 	}
 
 	/**
 	 * Deletes all files and subdirectories under dir. Returns true if all
 	 * deletions were successful. If a deletion fails, the method stops
 	 * attempting to delete and returns false.
 	 * http://javaalmanac.com/egs/java.io/DeleteDir.html
 	 */
 	public static boolean deleteDir(File dir) {
 
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					IJ.log("errore delete dir");
 					return false;
 				}
 			}
 		}
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	/**
 	 * Create a directory; all ancestor directories must exist
 	 * http://javaalmanac.com/egs/java.io/DeleteDir.html
 	 * 
 	 * @param dir
 	 * @return booleano true se ok
 	 */
 	public static boolean createDir(File dir) {
 
 		boolean success = dir.mkdir();
 		if (!success) {
 			return false;
 		} else
 			return true;
 	}
 
 	/**
 	 * separa il nome directory dal path
 	 * 
 	 * @param path
 	 *            path
 	 * @return directory
 	 */
 	public String extractDirectory(String path) {
 		int pos = path.lastIndexOf('\\');
 		String dir = path.substring(0, pos);
 		return dir;
 	}
 
 	/**
 	 * separa il filename dal path
 	 * 
 	 * @param path
 	 *            path
 	 * @return filename
 	 */
 	public String extractFileName(String path) {
 
 		int pos = path.lastIndexOf('\\');
 		String fileName = path.substring(pos + 1);
 		return fileName;
 	}
 
 	// //###############################################################################
 
 	/**
 	 * legge e carica in memoria il file codici.txt e expand.tx1
 	 * 
 	 * @param fileName
 	 *            path del file
 	 * @param tokenXriga
 	 *            numero di token per ogni riga
 	 * @return tabella col contenuto del file
 	 */
 	public String[][] readFile1(String fileName, int tokenXriga) {
 
 		int count1 = 0;
 		int count3 = 0;
 		String table[][] = null;
 		try {
 			// URL myurl = this.getClass().getResource(fileName);
 			// IJ.log("il file '" + fileName + "'  stato trovato in " + myurl);
 			InputStream is = getClass().getResourceAsStream(fileName);
 
 			Reader reader = new BufferedReader(new InputStreamReader(is));
 			StreamTokenizer strTok = new StreamTokenizer(reader);
 
 			strTok.resetSyntax();
 			strTok.wordChars('A', 'Z');
 			strTok.wordChars('a', 'z');
 			strTok.wordChars('0', '9');
 			strTok.wordChars('_', '_');
 
 			strTok.slashSlashComments(true);
 			strTok.slashStarComments(true);
 			strTok.eolIsSignificant(true);
 
 			while (strTok.nextToken() != StreamTokenizer.TT_EOF) {
 				if (strTok.ttype == StreamTokenizer.TT_WORD)
 					count1++;
 			}
 			reader.close();
 		} catch (Exception e) {
 			IJ.log("InputOutput.readFile1.readError");
 			IJ.error(e.getMessage());
 		}
 		int tot = count1 / tokenXriga;
 		table = new String[tot][tokenXriga];
 
 		// ora leggo e decodifico la lista
 		try {
 			InputStream is2 = getClass().getResourceAsStream(fileName);
 			Reader r2 = new BufferedReader(new InputStreamReader(is2));
 			StreamTokenizer tok1 = new StreamTokenizer(r2);
 
 			tok1.resetSyntax();
 			tok1.wordChars('A', 'Z');
 			tok1.wordChars('a', 'z');
 			tok1.wordChars('0', '9');
 			tok1.wordChars('_', '_');
 			tok1.wordChars('.', '.');
 
 			tok1.slashSlashComments(true);
 			tok1.slashStarComments(true);
 			tok1.eolIsSignificant(true);
 			int elem = 0;
 			while (tok1.nextToken() != StreamTokenizer.TT_EOF) {
 				switch (tok1.ttype) {
 				case StreamTokenizer.TT_EOL:
 					elem = 0;
 					break;
 				case StreamTokenizer.TT_WORD:
 					if (count3 < tot)
 						table[count3][elem] = tok1.sval;
 					if (elem == tokenXriga - 1)
 						count3++;
 					elem++;
 					break;
 				}
 			}
 			r2.close();
 		} catch (IOException e) {
 			System.out.println("st.nextToken() unsuccessful");
 		}
 		return (table);
 	}
 
 	// //###############################################################################
 
 	/**
 	 * legge e carica in memoria un file numerico
 	 * 
 	 * @param fileName
 	 *            path del file
 	 * @return vettore col contenuto del file
 	 */
 	public double[] readFile2(String fileName) {
 		double vet1[];
 		int len = 1;
 
 		try {
 
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			int n1 = -1;
 			while (in.readLine() != null) {
 				n1++;
 			}
 			in.close();
 			len = n1;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		vet1 = new double[len + 1];
 
 		try {
 
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str;
 			int n2 = -1;
 			while ((str = in.readLine()) != null) {
 				n2++;
 				vet1[n2] = ReadDicom.readDouble(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return vet1;
 	}
 
 	// //###############################################################################
 
 	/**
 	 * legge e carica in memoria il file. Legge anche i file contenuti in un
 	 * file JAR
 	 * 
 	 * @param fileName
 	 *            path del file
 	 * @param tokenXriga
 	 *            numero di token per ogni riga
 	 * @return tabella col contenuto del file
 	 */
 
 	public ArrayList<ArrayList<String>> readFile3(String fileName) {
 		ArrayList<ArrayList<String>> matrixTable = new ArrayList<ArrayList<String>>();
 		// IJ.log("readFile3 in esecuzione");
 		try {
 			// IJ.log("TENTATIVO LETTURA " + fileName);
 			// BufferedReader br = new BufferedReader(new FileReader(fileName));
 			// IJ.log("readFile3.br =" + br);
 
 			URL url1 = this.getClass().getResource("/" + fileName);
 			if (url1 == null) {
 				IJ.log("readFile3: file " + fileName + " not visible or null");
 				return null;
 			}
 			// IJ.log("readFile3.url1 =" + url1);
 
 			// String home = url1.getPath();
 
 			InputStream is = getClass().getResourceAsStream("/" + fileName);
 			// IJ.log("readFile3.is =" + is);
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			// IJ.log("readFile3.br =" + br);
 
 			while (br.ready()) {
 				String line = br.readLine();
 				if (line == null)
 					continue;
 				// only > java 6.0
 				// if (line.isEmpty())
 				// continue;
 				if (line.trim().length() == 0)
 					continue;
 				if (!isComment(line)) {
 					ArrayList<String> row = new ArrayList<String>();
 					String result = InputOutput.stripAllComments(line);
 					String[] splitted = result.split("\\s+");
 					for (int i1 = 0; i1 < splitted.length; i1++) {
 						row.add(splitted[i1]);
 					}
 					matrixTable.add(row);
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			IJ.log("readFile3 error <" + fileName + "> " + e.getMessage());
 			return null;
 		}
 		return matrixTable;
 
 	}
 
 	// //###############################################################################
 
 	/***
 	 * 
 	 * legge e carica in memoria il file da disco.
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 
 	public String[][] readFile4(String fileName) {
 		ArrayList<ArrayList<Object>> matrixTable = new ArrayList<ArrayList<Object>>();
 		ArrayList<Object> row1 = new ArrayList<Object>();
 
 		// int kk1 = 0;
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(new File(
 					fileName)));
 			while (br.ready()) {
 				// kk1++;
 				String line = br.readLine();
 
 				if (!isComment(line)) {
 					ArrayList<Object> row = new ArrayList<Object>();
 
 					String result = InputOutput.stripAllComments(line);
 					String[] splitted = result.split("\\s+");
 					for (int i1 = 0; i1 < splitted.length; i1++) {
 						row.add(splitted[i1]);
 					}
 					matrixTable.add(row);
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			IJ.error(e.getMessage());
 			// return null;
 		}
 		// ora trasferiamo tutto nella table
 		String[][] table = new String[matrixTable.size()][matrixTable.get(0)
 				.size()];
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<Object> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
 				table[i1][j1] = (String) row1.get(j1);
 			}
 		}
 		return (table);
 	}
 
 	// //###############################################################################
 
 	/**
 	 * legge e carica in memoria il file da disco.
 	 * 
 	 * @param fileName
 	 *            path del file
 	 * @param tokenXriga
 	 *            numero di token per ogni riga
 	 * @return tabella col contenuto del file
 	 */
 
 	public ArrayList<ArrayList<String>> readFile5(String fileName) {
 		ArrayList<ArrayList<String>> matrixTable = new ArrayList<ArrayList<String>>();
 		try {
 
 			BufferedReader br = new BufferedReader(new FileReader(new File(
 					fileName)));
 			while (br.ready()) {
 				String line = br.readLine();
 				if (line == null)
 					continue;
 				// only > java 6.0
 				// if (line.isEmpty())
 				// continue;
 				if (line.trim().length() == 0)
 					continue;
 				if (!isComment(line)) {
 					ArrayList<String> row1 = new ArrayList<String>();
 					String result = InputOutput.stripAllComments(line);
 					String[] splitted = result.split("#");
 					for (int i1 = 0; i1 < splitted.length; i1++) {
 						row1.add(splitted[i1]);
 					}
 					ArrayList<String> row2 = new ArrayList<String>();
 					for (int i1 = 1; i1 < splitted.length; i1 = i1 + 2) {
 						row2.add(row1.get(i1));
 					}
 					matrixTable.add(row2);
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			IJ.log("readFile5 error>" + e.getMessage());
 			return null;
 		}
 		return matrixTable;
 	}
 
 	// //###############################################################################
 
 	/***
 	 * Serve al leggere i file csv (valori delimitati da ;) legge anche
 	 * eventuali token vuoti
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public String[][] readFile6(String fileName) {
 
 		// IJ.log("readFile6= " + fileName);
 
 		ArrayList<ArrayList<Object>> matrixTable = new ArrayList<ArrayList<Object>>();
 		ArrayList<Object> row1 = new ArrayList<Object>();
 		String delimiter = ";";
 
 		try {
 			URL url1 = this.getClass().getResource("/" + fileName);
 			if (url1 == null) {
 				IJ.log("readFile6: file " + fileName + " not visible or null");
 				return null;
 			}
 			InputStream is = getClass().getResourceAsStream("/" + fileName);
 			// IJ.log("readFile3.is =" + is);
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
 			while (br.ready()) {
 				String line = br.readLine();
 				// a questo punto vedo se i primi due caratteri sono quelli di
 				// un commento, in questo caso non lo carico nella matrice
 				if (line.length() < 2)
 					continue;
 				String substr = line.substring(0, 2);
 				if (line.equals("") || substr.equals("//")
 						|| substr.equals("/*")) {
 					continue;
 				}
 				ArrayList<Object> row = new ArrayList<Object>();
 				String result = InputOutput.stripAllComments(line);
 				String[] splitted = result.split(delimiter, -1);
 				for (int i1 = 0; i1 < splitted.length; i1++) {
 					row.add(splitted[i1]);
 				}
 				matrixTable.add(row);
 			}
 			br.close();
 		} catch (Exception e) {
 			IJ.error(e.getMessage());
 		}
 
 		// ho caricato la matrice in memoria, ora si tratta di metterla nella
 		// tabella di output
 		//
 		// IJ.log("matrixTable.size= [" + matrixTable.size() + "]x["
 		// + matrixTable.get(0).size() + "]");
 
 		// ora trasferiamo tutto nella table
 		String[][] table = new String[matrixTable.size()][matrixTable.get(0)
 				.size()];
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<Object> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
 				table[i1][j1] = (String) row1.get(j1);
 			}
 		}
 		return (table);
 	}
 
 	// //###############################################################################
 
 	/***
 	 * Serve al leggere i file csv (valori delimitati da ;) legge anche
 	 * eventuali token vuoti.
 	 * 
 	 * La seconda colonna contiene il commento e viene saltata
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public String[][] readFile7(String fileName) {
 
 		ArrayList<ArrayList<Object>> matrixTable = new ArrayList<ArrayList<Object>>();
 		ArrayList<Object> row1 = new ArrayList<Object>();
 		String delimiter = ";";
 		try {
 			URL url1 = this.getClass().getResource("/" + fileName);
 			if (url1 == null) {
				IJ.log("readFile7: file " + fileName + " not visible or null");
 				return null;
 			}
 			InputStream is = getClass().getResourceAsStream("/" + fileName);
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			while (br.ready()) {
 				String line = br.readLine();
 				// a questo punto vedo se i primi due caratteri sono quelli di
 				// un commento, in questo caso non lo carico nella matrice
 				if (line.length() < 2)
 					continue;
 				String substr = line.substring(0, 2);
 				if (line.equals("") || substr.equals("//")
 						|| substr.equals("/*")) {
 					continue;
 				}
 				ArrayList<Object> row = new ArrayList<Object>();
 				String result = InputOutput.stripAllComments(line);
 				String[] splitted = result.split(delimiter, -1);
 				for (int i1 = 0; i1 < splitted.length; i1++) {
 					if (i1 == 1)
 						continue;
					row.add(splitted[i1].trim());
 				}
 				matrixTable.add(row);
 			}
 			br.close();
 		} catch (Exception e) {
 			IJ.error(e.getMessage());
 		}
 		// ho caricato la matrice in memoria, ora si tratta di metterla nella
 		// tabella di output
 		// IJ.log("matrixTable.size= [" + matrixTable.size() + "]x["
 		// + matrixTable.get(0).size() + "]");
 		// ora trasferiamo tutto nella table
 		String[][] table = new String[matrixTable.size()][matrixTable.get(0)
 				.size()];
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<Object> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
 				table[i1][j1] = (String) row1.get(j1);
 			}
 		}
 		return (table);
 	}
 
 	// //###############################################################################
 
 	/***
 	 * Legge i dati da un file e li resitituisce in un array double
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 
 	public static double[] readDoubleArrayFromFile(String fileName) {
 		ArrayList<Double> vetList = new ArrayList<Double>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				vetList.add(ReadDicom.readDouble(str));
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		double[] vetResult = new double[vetList.size()];
 		for (int i1 = 0; i1 < vetList.size(); i1++) {
 			vetResult[i1] = vetList.get(i1);
 		}
 		return vetResult;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce in un array float
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static float[] readFloatArrayFromFile(String fileName) {
 		ArrayList<Float> vetList = new ArrayList<Float>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				vetList.add(ReadDicom.readFloat(str));
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		float[] vetResult = new float[vetList.size()];
 		for (int i1 = 0; i1 < vetList.size(); i1++) {
 			vetResult[i1] = vetList.get(i1);
 		}
 		return vetResult;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce in un array int
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static int[] readIntArrayFromFile(String fileName) {
 		ArrayList<Integer> vetList = new ArrayList<Integer>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				vetList.add(ReadDicom.readInt(str));
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		int[] vetResult = new int[vetList.size()];
 		for (int i1 = 0; i1 < vetList.size(); i1++) {
 			vetResult[i1] = vetList.get(i1);
 		}
 		return vetResult;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce in un array string
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static String[] readStringArrayFromFile(String fileName) {
 
 		File file = new File(fileName);
 		if (!file.exists()) {
 			IJ.log("readStringArrayFromFile.fileNotExists " + fileName);
 		}
 
 		ArrayList<String> vetList = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				vetList.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		String[] vetResult = new String[vetList.size()];
 		for (int i1 = 0; i1 < vetList.size(); i1++) {
 			vetResult[i1] = vetList.get(i1).trim();
 		}
 		return vetResult;
 	}
 
 	// public static ArrayList<Double> readDoubleArrayListFromString(String
 	// strIn) {
 	// ArrayList<Double> arrList = new ArrayList<Double>();
 	// StringTokenizer parser = new StringTokenizer(strIn," \t\\,\\;");
 	// int total = parser.countTokens();
 	// for (int i1 = 0; i1 < total; i1++) {
 	// String next = parser.nextToken();
 	// arrList.add(ReadDicom.readDouble(next));
 	// }
 	// return arrList;
 	// }
 
 	/***
 	 * Legge i dati da una stringa e li restituisce in un ArrayList
 	 * 
 	 * @param strIn
 	 * @return
 	 */
 	public static ArrayList<String> readStringArrayListFromString(String strIn) {
 		ArrayList<String> arrList = new ArrayList<String>();
 		StringTokenizer parser = new StringTokenizer(strIn, " \t\\,\\;");
 		int total = parser.countTokens();
 		for (int i1 = 0; i1 < total; i1++) {
 			String next = parser.nextToken();
 			arrList.add(next);
 		}
 		return arrList;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce come double matrix
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static double[][] readDoubleMatrixFromFile(String fileName) {
 		ArrayList<ArrayList<String>> vetList = new ArrayList<ArrayList<String>>();
 		int rows = 0;
 		int columns = 0;
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				rows++;
 				// qui ho la linea, ora devo separare i tokens
 				ArrayList<String> arrList1 = readStringArrayListFromString(str);
 				columns = arrList1.size();
 				vetList.add(arrList1);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		double[][] vetResult = new double[rows][columns];
 
 		for (int row = 0; row < rows; row++) {
 			ArrayList<String> stringRiga = vetList.get(row);
 			for (int col = 0; col < columns; col++) {
 				vetResult[row][col] = ReadDicom.readDouble(stringRiga.get(col));
 			}
 		}
 		return vetResult;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce come float matrix
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static float[][] readFloatMatrixFromFile(String fileName) {
 		ArrayList<ArrayList<String>> vetList = new ArrayList<ArrayList<String>>();
 		int rows = 0;
 		int columns = 0;
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				rows++;
 				// qui ho la linea, ora devo separare i tokens
 				ArrayList<String> arrList1 = readStringArrayListFromString(str);
 				columns = arrList1.size();
 				vetList.add(arrList1);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		float[][] vetResult = new float[rows][columns];
 
 		for (int row = 0; row < rows; row++) {
 			ArrayList<String> stringRiga = vetList.get(row);
 			for (int col = 0; col < columns; col++) {
 				vetResult[row][col] = ReadDicom.readFloat(stringRiga.get(col));
 			}
 		}
 		return vetResult;
 	}
 
 	/***
 	 * Legge i dati da un file e li restituisce come int matrix
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static int[][] readIntMatrixFromFile(String fileName) {
 		ArrayList<ArrayList<String>> vetList = new ArrayList<ArrayList<String>>();
 		int rows = 0;
 		int columns = 0;
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				rows++;
 				// qui ho la linea, ora devo separare i tokens
 				ArrayList<String> arrList1 = readStringArrayListFromString(str);
 				columns = arrList1.size();
 				vetList.add(arrList1);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// ora trasferiamo tutto nel vettore
 		int[][] vetResult = new int[rows][columns];
 
 		for (int row = 0; row < rows; row++) {
 			ArrayList<String> stringRiga = vetList.get(row);
 			for (int col = 0; col < columns; col++) {
 				vetResult[row][col] = ReadDicom.readInt(stringRiga.get(col));
 			}
 		}
 		return vetResult;
 	}
 
 	// public static ArrayList<Integer> readIntegerArrayListFromString(String
 	// strIn) {
 	// ArrayList<Integer> arrList = new ArrayList<Integer>();
 	// StringTokenizer parser = new StringTokenizer(strIn, " \t\\,\\;");
 	// // nota bene: per il tab c' un solo slash
 	// int total = parser.countTokens();
 	// for (int i1 = 0; i1 < total; i1++) {
 	// String next = parser.nextToken().trim();
 	// Integer aux1 = Integer.parseInt(next);
 	// int aux2 = aux1.intValue();
 	// arrList.add(aux2);
 	// }
 	// return arrList;
 	// }
 
 	/***
 	 * Legge i dati da un file e li restituisce come string matrix
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static String[][] readStringMatrixFromFile(String fileName) {
 		ArrayList<ArrayList<String>> vetList = new ArrayList<ArrayList<String>>();
 		int rows = 0;
 		int columns = 0;
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String str = "";
 			while ((str = in.readLine()) != null) {
 				rows++;
 				// qui ho la linea, ora devo separare i tokens
 				ArrayList<String> arrList1 = readStringArrayListFromString(str);
 				columns = arrList1.size();
 				vetList.add(arrList1);
 			}
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// IJ.log("righe= " + rows + " colonne= " + columns);
 		// ora trasferiamo tutto nel vettore
 		String[][] vetResult = new String[rows][columns];
 		for (int i1 = 0; i1 < vetList.size(); i1++) {
 			ArrayList<String> stringRiga = vetList.get(i1);
 			for (int i2 = 0; i2 < stringRiga.size(); i2++) {
 				vetResult[i1][i2] = stringRiga.get(i2);
 			}
 		}
 		return vetResult;
 	}
 
 	/**
 	 * Viene utilizzato da FFTJ
 	 *  
 	 * @param mat1
 	 * @param mat2
 	 * @param index
 	 * @return
 	 */
 	// // NB: rinominato temporaneamente per vedere se lo ho utilizzato da
 	// qualche parte od era un progetto orfano
 
 	public static double[][][] addMatrix(double[][][] mat1, double[][] mat2,
 			int index) {
 		int slices1 = mat1.length;
 		int rows1 = mat1[0].length;
 		int columns1 = mat1[0][0].length;
 		int rows2 = mat2.length;
 		int columns2 = mat2[0].length;
 
 		if (rows1 != rows2 || columns1 != columns2) {
 			IJ.log("addMatrix different dimensions matrices");
 			return null;
 		}
 		if (index > slices1) {
 			IJ.log("addMatrix index too big");
 			return null;
 		}
 		double[][][] mat3 = cloneMatrix(mat1);
 		for (int i1 = 0; i1 < rows2; i1++) {
 			for (int i2 = 0; i2 < columns2; i2++) {
 				mat3[index][i1][i2] = mat2[i1][i2];
 			}
 		}
 		return mat3;
 	}
 
 	/***
 	 * Data una matrice, la duplica
 	 * 
 	 * @param mat1
 	 * @return
 	 */
 	public static double[][][] cloneMatrix(double[][][] mat1) {
 		int slices1 = mat1.length;
 		int rows1 = mat1[0].length;
 		int columns1 = mat1[0][0].length;
 		double[][][] mat2 = new double[slices1][rows1][columns1];
 		for (int i1 = 0; i1 < slices1; i1++) {
 			for (int i2 = 0; i2 < rows1; i2++) {
 				for (int i3 = 0; i3 < columns1; i3++) {
 					mat2[i1][i2][i3] = mat1[i1][i2][i3];
 				}
 			}
 		}
 		return mat2;
 
 	}
 
 	/***
 	 * Trasforma un arrayList in una matrice stringa
 	 * 
 	 * @param matrixTable
 	 * @return
 	 */
 	public String[][] fromArrayListToStringTable(
 			ArrayList<ArrayList<String>> matrixTable) {
 		ArrayList<String> row1 = new ArrayList<String>();
 		if (matrixTable == null) {
 			MyLog.here("fromArrayListToStringTable.matrixTable == null");
 			return null;
 		}
 		if (matrixTable.size() == 0) {
 			MyLog.here("fromArrayListToStringTable.matrixTable == 0");
 			return null;
 		}
 
 		// ora trasferiamo tutto nella table
 		String[][] table = new String[matrixTable.size()][matrixTable.get(0)
 				.size()];
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<String> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
 				table[i1][j1] = (String) row1.get(j1);
 			}
 		}
 		return (table);
 	}
 
 	/***
 	 * Trasforma un arrayList in una matrice double
 	 * 
 	 * @param matrixTable
 	 * @return
 	 */
 	public double[][] fromArrayListToDoubleTable(
 			ArrayList<ArrayList<Double>> matrixTable) {
 		int rows = 0;
 		int columns = 0;
 
 		ArrayList<Double> row1 = new ArrayList<Double>();
 		if (matrixTable == null) {
 			MyLog.here("fromArrayListToDoubleTable.matrixTable == null");
 			return null;
 		}
 		if (matrixTable.size() == 0) {
 			MyLog.here("fromArrayListToDoubleTable.matrixTable == 0");
 			return null;
 		}
 		rows = matrixTable.size();
 		columns = 0;
 
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			if (matrixTable.get(i1).size() > columns)
 				columns = matrixTable.get(i1).size();
 		}
 		// IJ.log("rows=" + rows + " columns= " + columns);
 
 		// ora trasferiamo tutto nella table
 		double[][] table = new double[rows][columns];
 		for (int i1 = 0; i1 < rows; i1++) {
 			for (int j1 = 0; j1 < columns; j1++) {
 				table[i1][j1] = Double.NaN;
 			}
 		}
 
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<Double> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(i1).size(); j1++) {
 				table[i1][j1] = (Double) row1.get(j1).doubleValue();
 			}
 		}
 		return (table);
 	}
 
 	/***
 	 * Effettua il dump di un ArrayList<ArrayList<String>>
 	 * 
 	 * @param matrixTable
 	 * @param title
 	 */
 
 	public static void dumpArrayListTable(
 			ArrayList<ArrayList<String>> matrixTable, String title) {
 		ArrayList<String> row1 = new ArrayList<String>();
 		if (matrixTable == null) {
 			IJ.log("fromArrayListToStringTable.matrixTable == null");
 			return;
 		}
 		IJ.log("---- " + title + " ----");
 		// ArrayList<String> riga = matrixTable.get(0);
 		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
 			ArrayList<String> arrayList = matrixTable.get(i1);
 			row1 = arrayList;
 			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
 				IJ.log((String) row1.get(j1));
 			}
 		}
 		return;
 	}
 
 	/***
 	 * Verifica la disponibilit di una directory
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static boolean checkDir(String name) {
 		File dirCheck = new File(name);
 		if (!dirCheck.exists())
 			return false;
 		else
 			return true;
 	}
 
 	/***
 	 * Verifica la disponibilit di un file jar
 	 * 
 	 * @param source
 	 * @return
 	 */
 	public boolean checkJar(String source) {
 		URL url1 = this.getClass().getResource("/" + source);
 		if (url1 != null)
 			return true;
 		else
 			return false;
 	}
 
 	/***
 	 * Verifica la disponibilit di un file
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static boolean checkFile(String name) {
 		File fileCheck = new File(name);
 		if (!fileCheck.exists())
 			return false;
 		else
 			return true;
 	}
 
 	/**
 	 * verifica codice
 	 * 
 	 * @param codice
 	 *            codice da verificare
 	 * @param tab2
 	 *            tabella contenente codici.txt
 	 * @return true se il codice esiste
 	 */
 	public static boolean isCode(String codice, String[][] tab2) {
 		if (codice == null)
 			IJ.log("codice == null");
 		if (tab2 == null)
 			IJ.log("tab2 == null");
 		boolean trovato = false;
 		for (int i1 = 0; i1 < tab2.length; i1++) {
 			if (codice.compareTo(tab2[i1][0]) == 0)
 				trovato = true;
 		}
 		return trovato;
 	}
 
 	/**
 	 * Estrae al volo un file da un archivio .jar by Ral Gagnon
 	 * (www.rgagnon.com/javadetails/java-0429.html) for correct work in junit
 	 * the source file (ie: test2.jar) must be in the iw2ayv "data" sourceFolder
 	 * 
 	 * 
 	 * 
 	 * 
 	 * @param source
 	 *            il nome del file jar es: "test2.jar"
 	 * @param object
 	 *            il nome del file da estrarre es: "BT2A_testP6"
 	 * @param dest
 	 *            la directory di destinazione es: "./Test2/" in caso di
 	 *            eccezioni stampa lo stack trace
 	 */
 
 	public void extractFromJAR(String source, String object, String dest) {
 
 		long count = 0;
 
 		try {
 			URL url1 = this.getClass().getResource("/" + source);
 			if (url1 == null) {
 				IJ.log("extractFromJAR: file " + source
 						+ " not visible or null");
 				return;
 			}
 			String home = url1.getPath();
 			// allo scopo di separare il nome della directory da quello del
 			// file utilizzo le due righe seguenti
 			File file = new File(home);
 			String home2 = file.getParent();
 
 			JarFile jar = new JarFile(home);
 
 			ZipEntry entry = jar.getEntry(object);
 
 			if (!checkDir(home2 + dest)) {
 				boolean ok = createDir(new File(home2 + dest));
 				if (!ok) {
 					MyLog.caller("failed directory creation=" + home2 + dest);
 					return;
 				}
 			}
 
 			File efile = new File(home2 + dest, entry.getName());
 
 			InputStream in = new BufferedInputStream(jar.getInputStream(entry));
 			OutputStream out = new BufferedOutputStream(new FileOutputStream(
 					efile));
 			byte[] buffer = new byte[2048];
 			for (;;) {
 				int nBytes = in.read(buffer);
 				if (nBytes <= 0)
 					break;
 				out.write(buffer, 0, nBytes);
 				count = count + nBytes;
 			}
 			out.flush();
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Estrae al volo un file da un archivio .jar by Ral Gagnon
 	 * (www.rgagnon.com/javadetails/java-0429.html) for correct work in junit
 	 * the source file (ie: test2.jar) must be in the iw2ayv "data" sourceFolder
 	 * 
 	 * 
 	 * 
 	 * 
 	 * @param source
 	 *            il nome del file jar es: "test2.jar"
 	 * @param object
 	 *            il nome del file da estrarre es: "BT2A_testP6"
 	 * @param dest
 	 *            la directory di destinazione es: "./Test2/" in caso di
 	 *            eccezioni stampa lo stack trace
 	 */
 
 	public long extractFromJAR2(String source, String object, String dest) {
 
 		long count = 0;
 
 		try {
 			URL url1 = this.getClass().getResource("/" + source);
 			if (url1 == null) {
 				IJ.log("extractFromJAR: file " + source
 						+ " not visible or null");
 				return -1L;
 			}
 			String home = url1.getPath();
 			// allo scopo di separare il nome della directory da quello del
 			// file utilizzo le due righe seguenti
 			File file = new File(home);
 			String home2 = file.getParent();
 
 			JarFile jar = new JarFile(home);
 
 			ZipEntry entry = jar.getEntry(object);
 
 			if (!checkDir(home2 + dest)) {
 				boolean ok = createDir(new File(home2 + dest));
 				if (!ok) {
 					MyLog.caller("failed directory creation=" + home2 + dest);
 					return -2L;
 				}
 			}
 
 			File efile = new File(home2 + dest, entry.getName());
 
 			InputStream in = new BufferedInputStream(jar.getInputStream(entry));
 			OutputStream out = new BufferedOutputStream(new FileOutputStream(
 					efile));
 			byte[] buffer = new byte[2048];
 			for (;;) {
 				int nBytes = in.read(buffer);
 				if (nBytes <= 0)
 					break;
 				out.write(buffer, 0, nBytes);
 				count = count + nBytes;
 			}
 			out.flush();
 			out.close();
 			in.close();
 			return count;
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			return -3L;
 		}
 	}
 
 	public String findListTestImages(String source, String[] list,
 			String destination) {
 		if (list == null) {
 			IJ.log("findListTestImages.list==null");
 			return null;
 		}
 		for (int i1 = 0; i1 < list.length; i1++) {
 			extractFromJAR(source, list[i1], destination);
 		}
 		URL url1 = this.getClass().getResource(destination);
 		if (url1 == null) {
 			IJ.log("findListTestImages.url1==null");
 			return null;
 		}
 		String home1 = url1.getPath();
 		return (home1);
 	}
 
 	public String[] findListTestImages2(String source, String[] list,
 			String destination) {
 		if (list == null) {
 			MyLog.here("list==null");
 			return null;
 		}
 		for (int i1 = 0; i1 < list.length; i1++) {
 			extractFromJAR(source, list[i1], destination);
 		}
 		URL url1 = this.getClass().getResource(destination);
 		if (url1 == null) {
 			MyLog.caller("url1==null");
 			return null;
 		}
 
 		String home1 = url1.getPath();
 		// nb: home  un path assoluto (preceduto da un "/")
 		String[] path = new String[list.length];
 		for (int i1 = 0; i1 < list.length; i1++) {
 			path[i1] = home1 + "/" + list[i1];
 		}
 		return (path);
 	}
 
 	public static boolean isComment(String riga) {
 		if (riga.length() < 2) {
 			return false;
 		}
 		String dueCaratteri = riga.substring(0, 2);
 		if (dueCaratteri.equals("//")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static String stripComment(String riga) {
 		int beginComment = 0;
 		int endComment = 0;
 		int fromIndex = 0;
 		String noComment = "";
 
 		beginComment = riga.indexOf("/*", fromIndex);
 		if (beginComment > 0) {
 			fromIndex = beginComment;
 			endComment = riga.indexOf("*/", fromIndex);
 		} else {
 			return riga;
 		}
 		if (endComment < fromIndex)
 			return null;
 		if (endComment < 0)
 			endComment = riga.length();
 
 		noComment = riga.substring(0, beginComment)
 				+ riga.substring(endComment + 2, riga.length());
 		return noComment;
 	}
 
 	public static String stripSlashComment(String riga) {
 		int beginComment = 0;
 		beginComment = riga.indexOf("//");
 		String noComment = "";
 		if (beginComment > 0) {
 			noComment = riga.substring(0, beginComment);
 		} else
 			noComment = riga;
 		return noComment;
 	}
 
 	public static String stripAllComments(String riga) {
 		String pass1 = stripSlashComment(riga);
 		String pass2 = "";
 		int count = 0;
 		while (!pass2.equals(pass1)) {
 			if (count > 0)
 				pass1 = pass2;
 			count++;
 			pass2 = stripComment(pass1);
 		}
 		return pass2;
 	}
 
 	/**
 	 * from:
 	 * "http://www.roseindia.net/java/example/java/util/ZipRetrieveElements.shtml"
 	 * 
 	 * @param sourcefile
 	 * @param destination
 	 */
 	public static void ZipRetrieveElements(String sourcefile, String destination) {
 		OutputStream out = null;
 
 		try {
 			if (!sourcefile.endsWith(".zip")) {
 				// System.out.println("Invalid file name!");
 				// System.exit(0);
 			} else if (!new File(sourcefile).exists()) {
 				System.out.println("File not exist!");
 				System.exit(0);
 			}
 			ZipInputStream zis1 = new ZipInputStream(new FileInputStream(
 					sourcefile));
 			ZipFile zf1 = new ZipFile(sourcefile);
 			int count = 0;
 			for (Enumeration<? extends ZipEntry> em = zf1.entries(); em
 					.hasMoreElements();) {
 				String targetfile = em.nextElement().toString();
 				out = new FileOutputStream(destination + "/" + targetfile);
 				byte[] buf = new byte[1024];
 				int len = 0;
 				while ((len = zis1.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				count += 1;
 			}
 			if (count > 0)
 				// System.out.println("" + count + " files unzipped.");
 				out.close();
 			zis1.close();
 		} catch (IOException e) {
 			System.out.println("Error: Operation failed!");
 			System.exit(0);
 		}
 	}
 
 	public static void debugListFiles(String dir) {
 		File fdir = new File(dir);
 		String[] list = fdir.list();
 		for (int i1 = 0; i1 < list.length; i1++) {
 			IJ.log("" + i1 + " " + list[i1]);
 		}
 	}
 
 	public static void unZip(String zipFile, String destDir) {
 		final int BUFFER = 2048;
 
 		try {
 			BufferedOutputStream dest = null;
 			FileInputStream fis = new FileInputStream(zipFile);
 			ZipInputStream zis = new ZipInputStream(
 					new BufferedInputStream(fis));
 			ZipEntry entry;
 			while ((entry = zis.getNextEntry()) != null) {
 				// System.out.println("Extracting: " +entry);
 				int count;
 				byte data[] = new byte[BUFFER];
 				// write the files to the disk
 				FileOutputStream fos = new FileOutputStream(destDir + "\\"
 						+ entry.getName());
 				dest = new BufferedOutputStream(fos, BUFFER);
 				while ((count = zis.read(data, 0, BUFFER)) != -1) {
 					dest.write(data, 0, count);
 				}
 				dest.flush();
 				dest.close();
 			}
 			zis.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
