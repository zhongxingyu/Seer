 /**
  * Copyright (c) 2012  Jose Cruz-Toledo
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.semanticscience.converter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.zip.Checksum;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.log4j.Logger;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.util.FileManager;
 
 /**
  * @author Jose Cruz-Toledo
  * 
  */
 public class RDFSyntaxConverter {
 	static final Logger log = Logger.getLogger(RDFSyntaxConverter.class);
 	/**
 	 * The file that is to be converted
 	 */
 	private File inputFile;
 	/**
 	 * The converted file
 	 */
 	private File outputFile;
 	private Model m;
 	private Dataset d;
 
 	public RDFSyntaxConverter() {
 		inputFile = null;
 		outputFile = null;
 		m = null;
 		d = null;
 	}
 
 	/**
 	 * Default converter, reads inputFile into a model, guesses the syntax and
 	 * outputs an RDF/XML RDF file in outputFile
 	 * 
 	 * @param inputFile
 	 *            the rdf file to be converted
 	 * @param outputFile
 	 *            the default output RDF file RDF/XML syntax
 	 * @throws Exception
 	 */
 	public RDFSyntaxConverter(File anInputFile, File anOutputFile)
 			throws Exception {
 		inputFile = anInputFile;
 		outputFile = anOutputFile;
 		try {
 			// Try reading the input file/directory
 			if (inputFile.isDirectory() && outputFile.isDirectory()) {
 				File[] files = inputFile.listFiles();
 				for (int i = 0; i < files.length; i++) {
 					File f = files[i];
 					System.out.println("Converting "+f.getAbsolutePath()+"... ");
 					FileUtils.cleanDirectory(new File("/tmp/jena"));
 					d = TDBFactory.createDataset("/tmp/jena/tdb");
 					m = d.getDefaultModel();
 					FileManager.get().readModel(m, f.getAbsolutePath());
 					// write it to file using rdf/xml syntax
 					String outname = FilenameUtils.getBaseName(f.getName());
 					File outFile = new File(outputFile.getAbsolutePath()+"/"
 							+ outname + ".rdf");
 					m.write(new FileOutputStream(outFile));
 				}
 				System.out.println("...done!");
 			} else {
 				throw new Exception(
 						"Input and output directories must be specified!");
 			}
 		} catch (FileNotFoundException e) {
 			log.error("could not write to file", e);
 		} finally {
 			try {
 				m.close();
 				d.close();
 			} catch (Exception e) {
 				log.error(e);
 			}
 		}
 	}
 
 	public RDFSyntaxConverter(File anInputFile, File anOutputFile,
 			String outputSyntax) throws Exception {
 		inputFile = anInputFile;
 		outputFile = anOutputFile;
 
 		if (inputFile.isDirectory() && outputFile.isDirectory()) {
 			if (checkOutputSyntax(outputSyntax)) {
 				File[] files = inputFile.listFiles();
 				for (int i = 0; i < files.length; i++) {
 					File f = files[i];
 					System.out.println("Converting "+f.getAbsolutePath()+" ...");
					FileUtils.deleteDirectory(new File("/tmp/jena"));
					FileUtils.forceMkdir(new File("/tmp/jena"));
					FileUtils.forceMkdir(new File("/tmp/jena/tdb"));
 					d = TDBFactory.createDataset("/tmp/jena/tdb");
 					m = d.getDefaultModel();
 					FileManager.get().readModel(m, f.getAbsolutePath());
 					// write it to file using rdf/xml syntax
 					String outname = FilenameUtils.getBaseName(f.getName());
 					String suffix = null;
 					if (outputSyntax.equals("RDF/XML")
 							| outputSyntax.equals("RDF/XML-ABBREV")) {
 						suffix = ".rdf";
 					} else if (outputSyntax.equals("N-TRIPLE")) {
 						suffix = ".nt";
 					} else {
 						suffix = "." + outputSyntax.toLowerCase();
 					}
 					File outFile = new File(outputFile.getAbsolutePath()+"/"
 							+ outname + suffix);
 					try {
 						m.write(new FileOutputStream(outFile),
 								outputSyntax);
 					} catch (FileNotFoundException e) {
 						log.error("could not write to file!", e);
 					} finally {
						
 					}
 				}
				try {
					m.close();
					d.close();
				} catch (Exception e) {
					log.error(e);
				}
 			} else {
 				System.out.println("Invalid syntax given! => " + outputSyntax);
 			}
 		} else {
 			throw new Exception(
 					"Input and output directories must be specified!");
 		}
 	}
 
 	public Model getModel() {
 		return this.m;
 	}
 
 	public File getOutputFile() {
 		return this.outputFile;
 
 	}
 
 	private boolean checkOutputSyntax(String aSyntax) {
 		if (aSyntax.equals("RDF/XML")) {
 			return true;
 		} else if (aSyntax.equals("RDF/XML-ABBREV")) {
 			return true;
 		} else if (aSyntax.equals("N-TRIPLE")) {
 			return true;
 		} else if (aSyntax.equals("TURTLE")) {
 			return true;
 		} else if (aSyntax.equals("TTL")) {
 			return true;
 		} else if (aSyntax.equals("N3")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
