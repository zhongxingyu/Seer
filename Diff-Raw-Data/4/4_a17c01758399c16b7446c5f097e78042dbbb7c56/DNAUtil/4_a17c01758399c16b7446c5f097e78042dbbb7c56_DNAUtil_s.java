 package utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import model.Gene;
 
 public class DNAUtil {
 
 	public static double avgCDSSpan = 0;
 	public static double avgCDSSize = 0;
 	public static double avgExonSize = 0;
 	public static double avgIntronSize = 0;
 	public static double avgIntergenicSize = 0;
 	public static int totalNucleotides = 0;
 	public static int numGenes = 0;
 	public static int totalCDSActual = 0;
 	public static double totalT = 0;
 
 	// Static methods again works for me
 
 	public static String calculateResults(String filepath) throws Exception {
 
 		avgCDSSize = 0;
 		avgCDSSpan = 0;
 		avgExonSize = 0;
 		avgIntronSize = 0;
 		avgIntergenicSize = 0;
 		totalNucleotides = 0;
 		numGenes = 0;
 		totalCDSActual = 0;
 		totalT = 0;
 
 		// Name of the gene to all of the attributes of it
 		Map<String, List<Gene>> genes = new HashMap<String, List<Gene>>();
 
 		readFile(filepath, genes);
 
 		calculateGeneContentAndDensity(genes);
 
 		return displayInfo(genes);
 	}
 
 	private static String displayInfo(Map<String, List<Gene>> genes) {
 		String rtn = "";
 
 		DecimalFormat df = new DecimalFormat("#.##");
 		// for(String name : genes.keySet()) {
 		// rtn += (name + " " + genes.get(name).size() + "\n");
 		// }
 		rtn += "Gene Content:\n";
 		rtn += "a) " + df.format(avgCDSSpan) + "\tAverage CDS Span \n";
 		rtn += "b) " + df.format(avgCDSSize) + "\tAverage CDS Size \n";
 		rtn += "c) " + df.format(avgExonSize) + "\tAverage Exon Size \n";
 		rtn += "d) " + df.format(avgIntronSize) + "\tAverage Intron Size \n";
 		rtn += "e) " + df.format(avgIntergenicSize)
 				+ "\tAverage Intergenic Region Size \n";
 
 		rtn += "\n2. Gene Density:\n";
 
 		rtn += "a) " + df.format(numGenes * avgCDSSpan)
 				+ "\tNumber of Genes * Average CDS Span\n";
 		rtn += "b) " + df.format(numGenes * avgCDSSize)
 				+ "\tNumber of Genes * Average CDS Size\n";
 		rtn += "c) "
 				+ df.format((double) totalCDSActual / totalT)
 				+ "\tTotal CDS Size (combining isoforms) / number of nucleotides\n";
 		rtn += "d) "
 				+ df.format((double) numGenes / (totalNucleotides / 1000.0))
 				+ "\tNumber of Genes per KBPairs\n";
 		rtn += "e) " + df.format((totalNucleotides / 1000.0) / numGenes)
 				+ "\tKBPairs / Number of Genes \n";
 
 		return rtn;
 	}
 
 	private static void readFile(String filepath, Map<String, List<Gene>> genes)
 			throws Exception {
 		// Set up the file reader
 		File f = new File(filepath);
 		FileReader in = new FileReader(f);
 		BufferedReader reader = new BufferedReader(in);
 
 		String line = null;
 		do {
 			line = reader.readLine();
 
 			if (line != null) {
 				// Break the line apart by the tabs
 				String[] tokens = line.split("\t");
 
 				// Create a new Gene to save the parts
 				Gene g = new Gene();
 
 				// Set all the normal stuff
 				g.setChromosomeName(tokens[0]);
 				g.setSource(tokens[1]);
 
 				if (!tokens[2].equals("mRNA") && !tokens[2].equals("CDS")) {
 					continue;
 				}
 
 				g.setFeature(tokens[2]);
 				g.setStart(Integer.parseInt(tokens[3]));
 				g.setStop(Integer.parseInt(tokens[4]));
 				g.setScore(tokens[5].equals(".") ? 0 : Double
 						.parseDouble(tokens[5]));
 				g.setForwardStrand(tokens[6].equals("+") ? true : false);
 				g.setFrame(tokens[7].equals(".") ? 0 : Integer
 						.parseInt(tokens[7]));
 
 				String[] attributes = tokens[8].split(";");
 
 				// Save all attributes
 				for (String s : attributes) {
 					s = s.trim();
 					String[] pair = s.split(" ");
 					String key = pair[0];
 					String val = pair[1];
 
 					if (key.equals("gene_id")) {
 						g.setName(val.replace("\"", ""));
 						g.addAttribute(key, val.replace("\"", ""));
 					} else {
 						g.addAttribute(key, val.replace("\"", ""));
 					}
 				}
 
 				// Update start or stop based on + and - values
 				if (g.getFeature().equals("mRNA")) {
 					if (g.isForwardStrand()) {
 						g.setStop(g.getStop() + 3);
 					} else {
 						g.setStart(g.getStart() - 3);
 					}
 				}
 
 				// The gene is finished reading save it to the gene map
 				if (genes.get(g.getName()) == null) {
 					List<Gene> temp = new ArrayList<Gene>();
 					temp.add(g);
 					genes.put(g.getName(), temp);
 				} else {
 					genes.get(g.getName()).add(g);
 				}
 
 			}
 		} while (line != null);
 
 		// All done
 		reader.close();
 	}
 
 	private static void calculateGeneContentAndDensity(
 			Map<String, List<Gene>> genes) {
 		List<Gene> gList;
 		int mRNATotalSize = 0;
 		int mRNACount = 0;
 		Map<String, Double> averageCDSSizes = new HashMap<String, Double>();
 		Map<String, Double> averageExonSizes = new HashMap<String, Double>();
 		Map<String, AttributeInfo> isoforms = new HashMap<String, AttributeInfo>();
 		Map<String, AttributeInfo> geneInfos = new HashMap<String, AttributeInfo>();
 		int totalCDS = 0;
 		int totalIntergenicRegionSize = 0;
 		List<Gene> mRNAs = new ArrayList<Gene>();
 		boolean foundRNA = false;
 		// Lazy flag ... haha
 		boolean first = true;
 		int maxEndPos = 0;
 		int smallestStartPos = 0;
 		List<IntegerPair> cdsRegions = new ArrayList<IntegerPair>();
 
 		for (String name : genes.keySet()) {
 			gList = genes.get(name);
 			foundRNA = false;
 			for (Gene g : gList) {
 				// Average CDS Span
 				if (g.getFeature().equals("mRNA")) {
 					if (!foundRNA) {
 						mRNAs.add(g);
 						foundRNA = true;
 					}
 					mRNACount++;
 					mRNATotalSize += g.getStop() - g.getStart();
 				} else if (g.getFeature().equals("CDS")) {
 					totalCDS++;
 					cdsRegions.add(new IntegerPair(g.getStart(), g.getStop()));
 
 					// Average CDS size for all isoforms
 					String transcriptID = g.getAttributes()
 							.get("transcript_id");
 					if (isoforms.containsKey(transcriptID)) {
 						isoforms.get(transcriptID).add(
 								g.getStop() - g.getStart());
 					} else {
 						isoforms.put(transcriptID, new AttributeInfo(
 								transcriptID, g.getStop() - g.getStart()));
 					}
 
 					// Average Exon Size for each gene that is a CDS
 					String geneID = g.getAttributes().get("gene_id");
 					if (geneInfos.containsKey(geneID)) {
 						geneInfos.get(geneID).add(g.getStop() - g.getStart());
 					} else {
 						geneInfos.put(
 								geneID,
 								new AttributeInfo(geneID, g.getStop()
 										- g.getStart()));
 					}
 				}
 
 				if (first) {
 					maxEndPos = g.getStop();
 					smallestStartPos = g.getStart();
 				} else {
 					maxEndPos = (g.getStop() > maxEndPos) ? g.getStop()
 							: maxEndPos;
 					smallestStartPos = (g.getStart() < smallestStartPos) ? g
 							.getStart() : smallestStartPos;
 				}
 			}
 		}
 
 		// actual total cds size
 		Collections.sort(cdsRegions, new Comparator<IntegerPair>() {
 			@Override
 			public int compare(IntegerPair o1, IntegerPair o2) {
 				if (o1.start < o2.start) {
 					return -1;
 				} else if (o1.start > o2.start) {
 					return 1;
 				} else {
 					return 0;
 				}
 			}
 		});
 
 		// find and combine overlapping regions
 		IntegerPair curVal = cdsRegions.size() > 0 ? cdsRegions.get(0) : null;
		int cdsMin = curVal.start;
		int cdsMax = curVal.stop;
 		
 		Iterator<IntegerPair> iter = cdsRegions.iterator();
 		while (iter.hasNext()) {
 			IntegerPair temp = iter.next();
 			while(temp.start <= curVal.stop && iter.hasNext()) {
 				if(temp.stop > curVal.stop) {
 					curVal.stop = temp.stop;
 				}
 				temp = iter.next();
 			}
 			
 			totalCDSActual += (curVal.stop - curVal.start) + 1;
 			if(curVal.start < cdsMin) {
 				cdsMin = curVal.start;
 			}
 			if(curVal.stop > cdsMax) {
 				cdsMax = curVal.stop;
 			}
 			curVal = temp;
 		}
 
 		totalT = cdsMax - cdsMin + 1;
 		
 		// Average intergenic region size
 		Collections.sort(mRNAs, new Comparator<Gene>() {
 			@Override
 			public int compare(Gene o1, Gene o2) {
 				if (o1.getStart() < o2.getStart()) {
 					return -1;
 				} else if (o1.getStart() > o2.getStart()) {
 					return 1;
 				} else {
 					return 0;
 				}
 			}
 		});
 
 		int lastStop = -1;
 
 		for (Gene rna : mRNAs) {
 			if (lastStop < 0) {
 				lastStop = rna.getStop();
 			} else {
 				totalIntergenicRegionSize += rna.getStart() - lastStop;
 				lastStop = rna.getStop();
 			}
 		}
 
 		if (mRNAs.size() == 1) {
 			avgIntergenicSize = 0;
 		} else {
 			avgIntergenicSize = totalIntergenicRegionSize / (mRNAs.size() - 1);
 		}
 
 		// Average CDS Span
 		avgCDSSpan = (double) mRNATotalSize / mRNACount;
 
 		// Find average CDS size per isoform
 		for (String name : isoforms.keySet()) {
 			AttributeInfo iso = isoforms.get(name);
 			averageCDSSizes.put(iso.id, (double) iso.totalSize / iso.count);
 		}
 
 		// Find average of all isoform CDS size averages
 		for (String name : averageCDSSizes.keySet()) {
 			avgCDSSize += averageCDSSizes.get(name);
 		}
 
 		// Average CDS size for all isoforms
 		avgCDSSize /= isoforms.keySet().size();
 
 		// Average Exon Size for each gene
 		for (String name : geneInfos.keySet()) {
 			AttributeInfo geneInfo = geneInfos.get(name);
 			averageExonSizes.put(geneInfo.id, (double) geneInfo.totalSize
 					/ geneInfo.count);
 		}
 
 		// Find average of all Gene Exon size averages
 		for (String name : averageExonSizes.keySet()) {
 			avgExonSize += averageExonSizes.get(name);
 		}
 
 		// Average Exon Size for all genes
 		avgExonSize /= geneInfos.keySet().size();
 
 		// Average Intron Size
 		avgIntronSize = (avgCDSSpan - avgCDSSize) / (totalCDS - 1);
 
 		// Number of genes
 		numGenes = geneInfos.keySet().size();
 
 		// Finding highest Nucleotide number
 		totalNucleotides = maxEndPos - smallestStartPos;
 	}
 
 }
