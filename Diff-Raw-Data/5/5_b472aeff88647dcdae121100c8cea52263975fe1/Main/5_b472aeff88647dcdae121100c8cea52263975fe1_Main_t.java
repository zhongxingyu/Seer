 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package edu.toronto.cs.cidb.hpoa.main;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 
 import edu.toronto.cs.cidb.hpoa.annotation.HPOAnnotation;
 import edu.toronto.cs.cidb.hpoa.annotation.OmimHPOAnnotations;
 import edu.toronto.cs.cidb.hpoa.ontology.HPO;
 import edu.toronto.cs.cidb.hpoa.ontology.Ontology;
 import edu.toronto.cs.cidb.hpoa.ontology.OntologyTerm;
 import edu.toronto.cs.cidb.hpoa.ontology.clustering.BottomUpAnnClustering;
 import edu.toronto.cs.cidb.hpoa.prediction.ICPredictor;
 import edu.toronto.cs.cidb.hpoa.prediction.Predictor;
 import edu.toronto.cs.cidb.hpoa.utils.maps.SetMap;
 
 public class Main {
 
 	public static void main(String[] args) {
 		Ontology hpo = HPO.getInstance();
 		OmimHPOAnnotations ann = new OmimHPOAnnotations(hpo);
 		ann
 				.load(getInputFileHandler(
 						"http://compbio.charite.de/svn/hpo/trunk/src/annotation/phenotype_annotation.tab",
 						false));
 
 		// clusterOntology(hpo, ann);
 		generateSimilarityScores(ann, args);
 
 	}
 
 	protected static void generateSimilarityScores(HPOAnnotation ann,
 			String[] args) {
 		Predictor p = new ICPredictor();
 		p.setAnnotation(ann);
 		if (args.length == 0) {
 			return;
 		}
 		String inputFileName = args[0], outputFileName = "";
 		if (args.length > 1) {
 			outputFileName = args[1];
 		} else {
 			outputFileName = inputFileName + ".out";
 		}
 
 		try {
 			BufferedReader in = new BufferedReader(
 					new FileReader(inputFileName));
 			PrintStream out = new PrintStream(outputFileName);
 			String line;
 			int EXPECTED_LINE_PIECES = 2;
			int ANNOTATION_SET_POSITION = 1;
 			int QUERY_SET_POSITION = 0;
 			String SET_SEPARATOR = "\t+", ITEM_SEPARATOR = "\\s*[, ]\\s*", COMMENT_MARKER = "##";
 			int counter = 0;
 			while ((line = in.readLine()) != null) {
 				++counter;
 				if (line.startsWith(COMMENT_MARKER)) {
 					continue;
 				}
 				String pieces[] = line.split(SET_SEPARATOR);
 				if (pieces.length < EXPECTED_LINE_PIECES) {
 					System.err.println("Unexpected format for line " + counter
 							+ ":\n" + line + "\n\n");
 					continue;
 				}
 				List<String> query = Arrays.asList(pieces[QUERY_SET_POSITION]
 						.split(ITEM_SEPARATOR));
 				List<String> ref = Arrays
 						.asList(pieces[ANNOTATION_SET_POSITION]
 								.split(ITEM_SEPARATOR));
				// System.err.println(query);
				// System.err.println(ref);
 				out.println(line + "\t" + p.getSimilarityScore(query, ref));
 			}
 			out.flush();
 			out.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	protected static void clusterOntology(Ontology hpo, HPOAnnotation ann) {
 		BottomUpAnnClustering mfp = new BottomUpAnnClustering(hpo, ann,
 				getTemporaryFile("omim_symptoms_rank_data"),
 				getTemporaryFile("log_"
 						+ new Date(System.currentTimeMillis()).toString()));
 		mfp.buttomUpCluster().display(
 				getTemporaryFile("out_"
 						+ new Date(System.currentTimeMillis()).toString()));
 		// generateMapping(hpo, "out_2012-07-17", "decipher_hpo_subset");
 	}
 
 	protected static void generateMapping(Ontology hpo, String coreFileName,
 			String inputFileName) {
 		PrintStream out;
 		try {
 			out = new PrintStream(getTemporaryFile(inputFileName
 					+ "_hpo-core-mapping"));
 		} catch (FileNotFoundException e1) {
 			out = System.out;
 			e1.printStackTrace();
 		}
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getTemporaryFile(coreFileName)));
 			String line;
 			Set<String> core = new HashSet<String>();
 			while ((line = in.readLine()) != null) {
 				if (!line.startsWith("HP:")) {
 					continue;
 				}
 				String id = line.substring(0, 10);
 				core.add(id);
 			}
 			in.close();
 
 			SetMap<String, String> obsoleteTermMapping = new SetMap<String, String>();
 			obsoleteTermMapping.addTo("HP:0000489", "HP:0100886");
 			obsoleteTermMapping.addTo("HP:0000489", "HP:0100887");
 			obsoleteTermMapping.addTo("HP:0009885", "HP:0004322");
 
 			Set<String> newDHS = new HashSet<String>();
 
 			in = new BufferedReader(new FileReader(
 					getTemporaryFile(inputFileName)));
 
 			int count = 0;
 			while ((line = in.readLine()) != null) {
 				if (!line.startsWith("HP:")) {
 					continue;
 				}
 				++count;
 				Set<String> replacements = new HashSet<String>();
 				Set<String> front = new HashSet<String>();
 				Set<String> next = new HashSet<String>();
 				front.addAll(obsoleteTermMapping.safeGet(line.trim()));
 				if (front.isEmpty()) {
 					front.add(line.trim());
 				}
 				while (!front.isEmpty()) {
 					for (String tId : front) {
 						OntologyTerm t = hpo.getTerm(tId);
 						if (t != null) {
 							if (core.contains(t.getId())) {
 								replacements.add(t.getId());
 							} else {
 								for (String p : t.getParents()) {
 									next.add(p);
 								}
 							}
 						}
 					}
 					front.clear();
 					front.addAll(next);
 					next.clear();
 				}
 				if (!replacements.contains(line.trim())) {
 					out.println(line.trim() + "\t" + replacements);
 				}
 				newDHS.addAll(replacements);
 			}
 			in.close();
 
 			out.println();
 			// System.out.println("New # of leaves in ontology: " +
 			// leaves.size());
 			out.println("Initial DECIPHER phenotypes: " + count);
 			out.println("New DECIPHER phenotypes:     " + newDHS.size());
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (out != System.out) {
 			out.close();
 		}
 	}
 
 	public static File getInputFileHandler(String inputLocation,
 			boolean forceUpdate) {
 		try {
 			File result = new File(inputLocation);
 			if (!result.exists()) {
 				String name = inputLocation.substring(inputLocation
 						.lastIndexOf('/') + 1);
 				result = getTemporaryFile(name);
 				if (!result.exists()) {
 					result.createNewFile();
 					BufferedInputStream in = new BufferedInputStream((new URL(
 							inputLocation)).openStream());
 					OutputStream out = new FileOutputStream(result);
 					IOUtils.copy(in, out);
 					out.flush();
 					out.close();
 				}
 			}
 			return result;
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 
 	protected static File getTemporaryFile(String name) {
 		return getInternalFile(name, "tmp");
 	}
 
 	protected static File getInternalFile(String name, String dir) {
 		File parent = new File("", dir);
 		if (!parent.exists()) {
 			parent.mkdirs();
 		}
 		return new File(parent, name);
 	}
 }
