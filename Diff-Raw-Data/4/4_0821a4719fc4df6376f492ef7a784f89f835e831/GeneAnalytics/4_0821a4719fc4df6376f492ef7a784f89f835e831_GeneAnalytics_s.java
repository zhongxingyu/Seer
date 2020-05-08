 package bioGUI;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 
 public class GeneAnalytics {
 
 	static ArrayList<String> mFasta, mGFF;
 	static int numFiles = 0;
 	static Scanner fastaScanner;
 	static Scanner gffScanner;
 	static StringBuilder fastaText;
 
 	static HashMap<String, String> codonTranslate;
 
 	public GeneAnalytics() {
 	}
 
 	//need to support multiple fasta and gff files later
 	//    fasta and gff files should have a 1:1 correspondence,
 	//	ie, fasta1 is paired with gff1, etc
 	static HashMap<String,HashMap<String, String>> work(ArrayList<String> fastaFiles, ArrayList<String> gffFiles) throws Exception {
 		HashMap<String, HashMap<String, String>> results = new HashMap<String, HashMap<String, String>>();
 		String output = "";
 		mFasta = fastaFiles;
 		mGFF = gffFiles;
 //		System.out.println("sanity");
 		if (mFasta.size() != mGFF.size()) {
 			throw new Exception();
 		} else {
 			numFiles = mFasta.size();
 		}
 
 		codonTranslateSetup();
 
 		for (int i = 0; i < numFiles; i++) {
 			//build output string for a particular gff/fasta file combination
 			fastaScanner = new Scanner(new File(mFasta.get(i)));
 			fastaText = new StringBuilder();
 
 			String nextLine = "";
 
 			try {
 				fastaScanner.nextLine(); // throw out first line
 				while (fastaScanner.hasNextLine()) {
 					nextLine = fastaScanner.nextLine();
 					fastaText.append(nextLine);
 				}
 			} finally {
 				fastaScanner.close();
 			}
 			
 			String currentFile = mFasta.get(i);
 			results.put(currentFile, new HashMap<String, String>());
 //			System.out.println("fastaText: "+fastaText);
 			
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgGeneSpan();
 			results.get(currentFile).put("avgGeneSpan", new Double(avgGeneSpan()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgCDSLength();
 			results.get(currentFile).put("avgCDSLength", new Double(avgCDSLength()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgExonSize();
 			results.get(currentFile).put("avgExonSize", new Double(avgExonSize()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgIntronSize();
 			results.get(currentFile).put("avgIntronSize", new Double(avgIntronSize()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgIntergenicSize();
 			results.get(currentFile).put("avgIntergenicSize", new Double(avgIntergenicSize()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgNucDensitySpan();
 			results.get(currentFile).put("avgNucDensitySpan", new Double(avgNucDensitySpan()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + avgNucDensityRegion();
 			results.get(currentFile).put("avgNucDensityRegion", new Double(avgNucDensityRegion()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + cdsNucProportion();
 			results.get(currentFile).put("cdsNucProportion", new Double(cdsNucProportion()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + genesPerKB(10);
 			results.get(currentFile).put("genesPerKB", new Double(genesPerKB(10)).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 //			output += " " + kbPerGene();
 			results.get(currentFile).put("kbPerGene", new Double(kbPerGene()).toString());
 
 			gffScanner = new Scanner(new File(mGFF.get(i)));
 			HashMap<String, String> predicted = predictProtein();
 			output = "";
 			for(String key : predicted.keySet()){
 				output += key + ": " + predicted.get(key) + "\n";
 			}
 			results.get(currentFile).put("predictProtein", output);
 //			results.put(mFasta.get(i), output);
 //			System.out.println("output " + output);
 //			output = "";
 		}
 //		System.out.println("output " + output);
 		return results;
 	}
 
 	static void codonTranslateSetup(){
 		codonTranslate = new HashMap<String, String>();
 		codonTranslate.put("TTT", "F");
 		codonTranslate.put("TTC", "F");
 		codonTranslate.put("TTA", "L");
 		codonTranslate.put("TTG", "L");
 		codonTranslate.put("CTT", "L");
 		codonTranslate.put("CTC", "L");
 		codonTranslate.put("CTA", "L");
 		codonTranslate.put("CTG", "L");
 		codonTranslate.put("ATT", "I");
 		codonTranslate.put("ATC", "I");
 		codonTranslate.put("ATA", "I");
 		codonTranslate.put("ATG", "M");
 		codonTranslate.put("GTT", "V");
 		codonTranslate.put("GTC", "V");
 		codonTranslate.put("GTA", "V");
 		codonTranslate.put("GTG", "V");
 		codonTranslate.put("TCT", "S");
 		codonTranslate.put("TCC", "S");
 		codonTranslate.put("TCA", "S");
 		codonTranslate.put("TCG", "S");
 		codonTranslate.put("CCT", "P");
 		codonTranslate.put("CCC", "P");
 		codonTranslate.put("CCA", "P");
 		codonTranslate.put("CCG", "P");
 		codonTranslate.put("ACT", "T");
 		codonTranslate.put("ACC", "T");
 		codonTranslate.put("ACA", "T");
 		codonTranslate.put("ACG", "T");
 		codonTranslate.put("GCT", "A");
 		codonTranslate.put("GCC", "A");
 		codonTranslate.put("GCA", "A");
 		codonTranslate.put("GCG", "A");
 		codonTranslate.put("TAT", "Y");
 		codonTranslate.put("TAC", "Y");
 		codonTranslate.put("TAA", "*");
 		codonTranslate.put("TAG", "*");
 		codonTranslate.put("CAT", "H");
 		codonTranslate.put("CAC", "H");
 		codonTranslate.put("CAA", "Q");
 		codonTranslate.put("CAG", "Q");
 		codonTranslate.put("AAT", "N");
 		codonTranslate.put("AAC", "N");
 		codonTranslate.put("AAA", "K");
 		codonTranslate.put("AAG", "K");
 		codonTranslate.put("GAT", "D");
 		codonTranslate.put("GAC", "D");
 		codonTranslate.put("GAA", "E");
 		codonTranslate.put("GAG", "E");
 		codonTranslate.put("TGT", "C");
 		codonTranslate.put("TGC", "C");
 		codonTranslate.put("TGA", "*");
 		codonTranslate.put("TGG", "W");
 		codonTranslate.put("CGT", "R");
 		codonTranslate.put("CGC", "R");
 		codonTranslate.put("CGA", "R");
 		codonTranslate.put("CGG", "R");
 		codonTranslate.put("AGT", "S");
 		codonTranslate.put("AGC", "S");
 		codonTranslate.put("AGA", "R");
 		codonTranslate.put("AGG", "R");
 		codonTranslate.put("GGT", "G");
 		codonTranslate.put("GGC", "G");
 		codonTranslate.put("GGA", "G");
 		codonTranslate.put("GGG", "G");
 	}
 
 	static HashMap<String, String> processGFFLine(String line) {
 		Scanner lineScanner = new Scanner(line);
 		HashMap<String, String> parsedGFF = new HashMap<String, String>();
 
 		//Getting string values for each column
 		parsedGFF.put("seqName", lineScanner.next());
 		parsedGFF.put("progName", lineScanner.next());
 		parsedGFF.put("featureType", lineScanner.next());
 		parsedGFF.put("startNuc", lineScanner.next());
 		parsedGFF.put("endNuc", lineScanner.next());
 		parsedGFF.put("score", lineScanner.next());
 		parsedGFF.put("strand", lineScanner.next());
 		parsedGFF.put("frameNum", lineScanner.next());
 
 		//getting rid of semicolon at end of gene id
 		lineScanner.next();
 		String geneId = lineScanner.next();
 		parsedGFF.put("geneId", geneId.substring(0, geneId.length() - 1));
 		//breaking up values in col 9
 		lineScanner.next();
 		String transcriptId = lineScanner.next();
 		parsedGFF.put("transcriptId", transcriptId.substring(0, transcriptId.length() - 1));
 
 		return parsedGFF;
 	}
 
 	/*1.	Average gene span per contig/fosmid.
 	 * Average number of nucleotides between the highest position
 	 * indicated and the lowest position indicated in all features
 	 *  with the same gene_id in column 9
 	 */
 	static double avgGeneSpan() {
 
 		//highest pos stored as geneId : highestPos
 		HashMap<String, Integer> highestPos = new HashMap<String, Integer>();
 
 		//lowest pos stored as geneId : lowestPos
 		HashMap<String, Integer> lowestPos = new HashMap<String, Integer>();
 
 		HashMap<String, String> processedGFFLine;
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			String geneId = processedGFFLine.get("geneId");
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"))-1;
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"))-1;
 
 			if(processedGFFLine.get("strand").equals("-") && processedGFFLine.get("featureType").equals("mRNA")){
 				startNuc -= 3;
 				System.out.println("startNuc-3");
 			}
 			else if(processedGFFLine.get("strand").equals("+") && processedGFFLine.get("featureType").equals("mRNA")){
 				endNuc -= 3;
 				System.out.println("endNuc-3");
 			}
 
 			
 			if (highestPos.containsKey(geneId) && highestPos.get(geneId) < endNuc) {
 				highestPos.put(geneId, endNuc);
 			} else if (!highestPos.containsKey(geneId)) {
 				highestPos.put(geneId, endNuc);
 			}
 
 			if (lowestPos.containsKey(geneId) && lowestPos.get(geneId) > startNuc) {
 				lowestPos.put(geneId, startNuc);
 			} else if (!lowestPos.containsKey(geneId)) {
 				lowestPos.put(geneId, startNuc);
 			}
 		}
 		/*
         System.out.println("lowestPos:" + lowestPos);
         System.out.println("highestPos:" + highestPos);
 		 */
 		ArrayList<Integer> geneIdLengths = new ArrayList<Integer>();
 
 		for (String geneId : highestPos.keySet()) {
 			geneIdLengths.add(highestPos.get(geneId) - lowestPos.get(geneId)+1);
 		}
 
 
 		double totalLength = 0;
 		for (Integer i : geneIdLengths) {
 			totalLength += i;
 		}
 
 		double averageGeneSpan = totalLength / geneIdLengths.size();
 
 		return averageGeneSpan;
 	}
 
 	/*2.	Average length of CDS per contig/fosmid.
 	 * Average coding DNA sequence size, sum of the
 	 *  CDS regions with the same gene_id (see Figure 1 below)
 	 */
 	static double avgCDSLength() {
 		String output = "";
 
 		HashMap<String, String> processedGFFLine;
 
 		//stored as geneId : cdsLength
 		HashMap<String, Integer> cdsLength = new HashMap<String, Integer>();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			String geneId = processedGFFLine.get("geneId");
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"))-1;
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"))-1;
 			String featureType = processedGFFLine.get("featureType");
 
 			//			System.out.println("featureType:" + featureType);
 
 			if (featureType.equals("CDS")) {
 				if (cdsLength.containsKey(geneId)) {
 					cdsLength.put(geneId, cdsLength.get(geneId) + endNuc - startNuc+1);
 				} else {
 					cdsLength.put(geneId, endNuc - startNuc+1);
 				}
 			}
 		}
 
 		ArrayList<Integer> geneIdLengths = new ArrayList<Integer>();
 
 		for (String geneId : cdsLength.keySet()) {
 			geneIdLengths.add(cdsLength.get(geneId));
 		}
 
 		double totalLength = 0;
 		for (Integer i : geneIdLengths) {
 			totalLength += i;
 		}
 
 		//		System.out.println("totalLength: "+totalLength);
 		//		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
 		double avgCDSLength = totalLength / geneIdLengths.size();
 
 		return avgCDSLength;
 	}
 
 	/*3.	Average size of exon per contig/fosmid.
 	 * Average number of nucleotides between the first
 	 * position and the last position indicated in features
 	 * marked CDS in the GFF files.
 	 */
 	static double avgExonSize() {
 		String output = "";
 
 		HashMap<String, String> processedGFFLine;
 
 		ArrayList<Integer> exonLength = new ArrayList<Integer>();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"))-1;
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"))-1;
 			String featureType = processedGFFLine.get("featureType");
 
 			//			System.out.println("featureType:" + featureType);
 
 			if (featureType.equals("CDS")) {
 				exonLength.add(endNuc - startNuc+1);
 			}
 		}
 
 		double totalLength = 0;
 		for (Integer i : exonLength) {
 			totalLength += i;
 		}
 
 		//		System.out.println("totalLength: "+totalLength);
 		//		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
 		double avgExonLength = totalLength / exonLength.size();
 
 		return avgExonLength;
 	}
 
 	/*
 	 * 4.	Average size of intron per contig.
 	 *  Average number of nucleotides between
 	 *  the last position of a CDS region and
 	 *  the first position of the subsequent
 	 *  CDS region with the same transcript_id
 	 *  (see Figure 1 below)
 	 */
 	static double avgIntronSize() {
 		String output = "";
 
 		HashMap<String, String> processedGFFLine;
 
 		HashMap<String, ArrayList<Integer>> exonIndex = new HashMap<String, ArrayList<Integer>>();
 		ArrayList<Integer> intronLength = new ArrayList<Integer>();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			String geneId = processedGFFLine.get("geneId");
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"))-1;
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"))-1;
 			String featureType = processedGFFLine.get("featureType");
 			String transcriptId = processedGFFLine.get("transcriptId");
 
 			
 			//			System.out.println("featureType:" + featureType);
 
 			if (featureType.equals("CDS")) {
 				if (exonIndex.get(transcriptId) == null) {
 					ArrayList<Integer> al = new ArrayList<Integer>();
 					exonIndex.put(transcriptId, al);
 				}
 
 				exonIndex.get(transcriptId).add(startNuc);
 				exonIndex.get(transcriptId).add(endNuc);
 			}
 		}
 
 		for (String tId : exonIndex.keySet()) {
 			ArrayList<Integer> indicies = exonIndex.get(tId);
 			Collections.sort(indicies);
 
 			for (int i = 1; i < indicies.size() - 2; i += 2) {
 				intronLength.add(indicies.get(i + 1) - indicies.get(i));
 			}
 		}
 
 		double totalLength = 0;
 		for (Integer i : intronLength) {
 			totalLength += i;
 		}
 
 		//		System.out.println("totalLength: "+totalLength);
 		//		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
 		double avgIntronLength = totalLength / intronLength.size();
 
 		return avgIntronLength;
 	}
 
 	/*5.	Average intergenic region size per contig.
 	 * Average number of nucleotides between the last
 	 * nucleotide of the stop codon and the first nucleotide
 	 * of the next gene (distance between subsequent gene
 	 * spans with different gene_id�s in column 9 of the
 	 * GFF file, see Figure 2 below).
 	 */
 	static double avgIntergenicSize() {
 		String output = "";
 
 		//highest pos stored as geneId : highestPos
 		HashMap<String, Integer> highestPos = new HashMap<String, Integer>();
 
 		//lowest pos stored as geneId : lowestPos
 		HashMap<String, Integer> lowestPos = new HashMap<String, Integer>();
 
 		HashMap<String, String> processedGFFLine;
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			String geneId = processedGFFLine.get("geneId");
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"))-1;
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"))-1;
 
 			if (highestPos.containsKey(geneId) && highestPos.get(geneId) < endNuc) {
 				highestPos.put(geneId, endNuc);
 			} else if (!highestPos.containsKey(geneId)) {
 				highestPos.put(geneId, endNuc);
 			}
 
 			if (lowestPos.containsKey(geneId) && lowestPos.get(geneId) > startNuc) {
 				lowestPos.put(geneId, startNuc);
 			} else if (!lowestPos.containsKey(geneId)) {
 				lowestPos.put(geneId, startNuc);
 			}
 		}
 
 		ArrayList<Integer> indicies = new ArrayList<Integer>();
 		ArrayList<Integer> intergenic = new ArrayList<Integer>();
 
 		for (String geneId : highestPos.keySet()) {
 			indicies.add(lowestPos.get(geneId));
 			indicies.add(highestPos.get(geneId));
 		}
 		Collections.sort(indicies);
 		/*
         System.out.println("lowestPos:" + lowestPos);
         System.out.println("highestPos:" + highestPos);
 		 */
 		for (int i = 1; i < indicies.size() - 2; i += 2) {
 			intergenic.add(indicies.get(i + 1) - indicies.get(i)-1);//+1
 		}
 
 		double totalLength = 0;
 		for (Integer i : intergenic) {
 			totalLength += i;
 		}
 
 		//		System.out.println("totalLength: "+totalLength);
 		//		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
 		double avgIntergenicLength = totalLength / intergenic.size();
 
 		return avgIntergenicLength;
 	}
 
 
 	/*	#6
 6. Average density of nucleotides in a CDS span per contig.
 Average number of nucleotides in each  Coding DNA Sequence
   multiplied by the number different gene_id, divided by the
    total number of nucleotides in the entire reference sequence.
 	 */
 
 	static double avgNucDensitySpan() {
 		HashMap<String, String> processedGFFLine;
 		int CDSLenTotal = 0;
 		int numCDS = 0;
 		int numGeneIds = 0;
 		double CDSLenAvg = 0;
 		double totalNuc = fastaText.length();
 		  HashSet<String> uniquegeneid = new HashSet<String>();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 
 			String type = processedGFFLine.get("featureType");
 
 			if (type.equals("CDS")) {
 				Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
 				Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
 				CDSLenTotal += (Math.max(startNuc, endNuc) - Math.min(startNuc, endNuc)+1);
 				numCDS++;
 			}
 			numGeneIds++;
 			String geneid = processedGFFLine.get("geneId");
             uniquegeneid.add(geneid);
 		}
 		System.out.println("CDSLenTotal "+CDSLenTotal);
 		return (double) CDSLenTotal / (double) uniquegeneid.size() / totalNuc;
 	}
 
 
 	/*	#7
 7. Average density of nucleotides in a CDS region per contig. Average number of nucleotides in each CDS region, multiplied by the number different gene_id’s, divided by the total number of nucleotides in the entire reference sequence
 	 */
 
     static double avgNucDensityRegion() {
         HashMap<String, String> processedGFFLine;
         int CDSLenTotal = 0;
         int numCDS = 0;
         int numGeneIds = 0;
         double CDSLenAvg = 0;
         double totalNuc = fastaText.length();
         HashSet<String> uniquegeneid = new HashSet<String>();
 
 
 
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
 
             String type = processedGFFLine.get("featureType");
 
             if (type.equals("CDS")) {
                 Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
                 Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
                 CDSLenTotal += (Math.max(startNuc, endNuc) - Math.min(startNuc, endNuc)+1);
  //               CDSLenTotal += 3;
 
                 numCDS++;
             }
 
 
             String geneid = processedGFFLine.get("geneId");
             uniquegeneid.add(geneid);
 
         }
 
         numGeneIds = uniquegeneid.size();
 
         return (double) CDSLenTotal / (double) numCDS /* (double) numGeneIds /*/ / totalNuc;
     }
 
 
 
 	/*	#8
 8. Proportion of CDS nucleotides per contig. Total number of nucleotides in all CDS regions divided by total number of nucleotides in the reference sequence. If one or more CDS regions overlap, choose the longest CDS region for calculations (see Figure 3 below)
 	 */
 
 	static double cdsNucProportion() {
 		HashMap<String, String> processedGFFLine;
 		int CDSLenTotal = 0;
 		int numCDS = 0;
 		int numGeneIds = 0;
 		double CDSLenAvg = 0;
 		double totalNuc = fastaText.length();
 
 		ArrayList<Integer> start = new ArrayList<Integer>();
 		ArrayList<Integer> end = new ArrayList<Integer>();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 
 			String type = processedGFFLine.get("featureType");
 
 			if (type.equals("CDS")) {
 				Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
 				Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
 				start.add(startNuc);
 				end.add(endNuc);
 			}
 		}
 
 		int usedCDS = 0;
 		for (int i = 0; i < totalNuc; i++) {
 			for (int j = 0; j < start.size(); j++) {
 				if (i >= start.get(j) && i <= end.get(j)) {
 					usedCDS++;
 				}
 			}
 		}
 
 		return (double) usedCDS / totalNuc;
 	}
 
 
 	/*	#9
 9. Average Number of Genes per 10KB total number of gene spans with the same gene_id in column 9 divided by total number of nucleotides in the reference sequence multiplied by 10^4 (1 KB = 10^3 nucleotides)
 	 */
 
 	static double genesPerKB(int numKB) {
 		HashMap<String, String> processedGFFLine;
 		HashSet<String> uniquegeneid = new HashSet<String>();
 		double totalNuc = fastaText.length();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 
 			String type = processedGFFLine.get("geneId");
 
 			uniquegeneid.add(type);
 		}
 
 
 		return (double)uniquegeneid.size() / totalNuc * 10000.0;
 	}
 
 
 	/*	#10
 10. KB’s per Gene total number of nucleotides in the reference sequence in KB’s (1 KB = 10^3 nucleotides) divided by the number of gene spans with different gene_id’s in column 9
 	 */
 
 	static double kbPerGene() {
 		HashMap<String, String> processedGFFLine;
 		HashSet<String> uniquegeneid = new HashSet<String>();
 		double totalNuc = fastaText.length();
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 
 			String type = processedGFFLine.get("geneId");
 
 			uniquegeneid.add(type);
 		}
 
 
 		return totalNuc / 1000.0 / (double)uniquegeneid.size();
 	}
 
 
 
 	/*	#11
 11. Predicted protein sequence use features with identical labels in column 9 and marked CDS
  to obtain nucleotide sequence to be used, join these sequences in order of 
  lowest position to highest position indicated by these features. 
  If column 7 is marked + then proceed to translation, 
  if column 7 is marked - then the reverse complement needs to be created before translation. 
  Translate the appropriate sequence using chart in Figure 4 below
 	 */
 	static HashMap<String,String> predictProtein() { // apparently this is reading the fasta file off by two
 		
 		// + strand read low num to high num
 		// - strand read high num to low num
 		HashMap<String, String> transToSeq = new HashMap<String, String>();
 		HashMap<String, ArrayList<HashMap<String, String>>> transcriptIds = new HashMap<String, ArrayList<HashMap<String, String>>>();
 		HashMap<String, String> processedGFFLine;
 
 		while (gffScanner.hasNext()) {
 			String line = gffScanner.nextLine();
 			processedGFFLine = processGFFLine(line);
 			String geneId = processedGFFLine.get("geneId");
 			Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
 			Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 			String featureType = processedGFFLine.get("featureType");
 			String transcriptId = processedGFFLine.get("transcriptId");
 			String strand = processedGFFLine.get("strand");
 			
 			//splits up transcript ids into different lists for processing
 			if(featureType.equals("CDS")){
 				if(transcriptIds.containsKey(transcriptId)){
 					transcriptIds.get(transcriptId).add(processedGFFLine);
 				}
 				else{
 					transcriptIds.put(transcriptId, new ArrayList<HashMap<String,String>>());
 					transcriptIds.get(transcriptId).add(processedGFFLine);
 				}
 			}
 		}
 		
 		for(String transcriptId : transcriptIds.keySet()){
 			ArrayList<HashMap<String, String>> gffLines = transcriptIds.get(transcriptId);
 			HashMap<Integer, String> fastaLines = new HashMap<Integer, String>();
 			boolean plusStrand = true;
 			for(HashMap<String, String> gffLine : gffLines){
 				Integer startNuc = new Integer(gffLine.get("startNuc")) - 1;
 				Integer endNuc = new Integer (gffLine.get("endNuc")) - 1;
 				
 				String fastaLine = fastaText.substring(startNuc, endNuc+1);
 				fastaLines.put(startNuc, fastaLine);
 				plusStrand = gffLine.get("strand").equals("+");
 //				System.out.println("plusStrand: "+plusStrand);
 
 			}
 			Integer[] fastaStartNuc = new Integer[fastaLines.keySet().size()];
 			ArrayList<Integer> fastaOrdering = new ArrayList<Integer>(Arrays.asList(fastaLines.keySet().toArray(fastaStartNuc)));
 			
 			Collections.sort(fastaOrdering);
 			if(plusStrand){
 				Collections.reverse(fastaOrdering);
 			}
 			
 			String workingLine = "";
 			for(int i = 0; i < fastaOrdering.size(); i++){
 				workingLine += fastaLines.get(fastaOrdering.get(i));
 			}
 			String output = "";
 	
 			for(int i = 0; i+3 < workingLine.length(); i += 3){
 				//System.out.println("fastaLine.substring("+i+","+(i+3)+"): "+fastaLine.substring(i, i+3));
 				if(plusStrand)
 					output += codonTranslate.get(complement(workingLine.substring(i, i+3)));
 				else
 					output += codonTranslate.get(reverseComplement(workingLine.substring(i, i+3)));
 
 			}
 	
 			transToSeq.put(transcriptId, new StringBuffer(output).reverse().toString());
 		}
 		
 
 		return transToSeq;	
 	}
 
 	static String reverseComplement(String s){
 		String reverse = "";
 		String complement = "";
 
 		reverse = new StringBuffer(s).reverse().toString();
 //		System.out.println("s: "+s);
 //		System.out.println("reverse: "+reverse);
 		
 		for(int i = 0; i < reverse.length(); i++){
 			char c = reverse.charAt(i);
 			if(c == 'A'){
 				complement += "T";
 			}
 			else if(c == 'T'){
 				complement += "A";
 			}
 			else if(c == 'G'){
 				complement += "C";
 			}
 			else if(c == 'C'){
 				complement += "G";
 			}
 		}
 
 		return complement;
 	}
 
 	static String complement(String s){
 		String complement = "";
 
 		for(int i = 0; i < s.length(); i++){
 			char c = s.charAt(i);
 			if(c == 'A'){
 				complement += "T";
 			}
 			else if(c == 'T'){
 				complement += "A";
 			}
 			else if(c == 'G'){
 				complement += "C";
 			}
 			else if(c == 'C'){
 				complement += "G";
 			}
 		}
 
 		return complement;
 	}
 
 
 }
