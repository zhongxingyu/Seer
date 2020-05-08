 package metagenomics;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.zip.Deflater;
 
 public class CompressionSort {
 
 	public class Read {
 		String readString;
 		int cluster;
 		String fileName;
 
 		public Read(String s, int c, String f) {
 			this.readString = s;
 			this.cluster = c;
 			this.fileName = f;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + cluster;
 			result = prime * result
 					+ ((readString == null) ? 0 : readString.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Read other = (Read) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			// if (cluster != other.cluster)
 			// return false;
 			if (readString == null) {
 				if (other.readString != null)
 					return false;
 			} else if (!readString.equals(other.readString))
 				return false;
 			return true;
 		}
 
 		private CompressionSort getOuterType() {
 			return CompressionSort.this;
 		}
 	}
 
 	private File inputDir;
 	private int totalFiles;
 	private final double CLUSTERDIFFTHRESHOLD = 0.1;
 	private static boolean PREPEND_RANDOM_DNA;
 	private ArrayList<ArrayList<Read>> readClusters;
 
 	/**
 	 * @param random true if random dna is to be prepended before compression.
 	 * @param inputDirName
 	 * @param nClusters
 	 */
 	public CompressionSort(boolean random, String inputDirName, int nClusters) {
 		readClusters = new ArrayList<ArrayList<Read>>(nClusters);
 		PREPEND_RANDOM_DNA = random;
 		this.inputDir = new File(inputDirName);
 		if (!inputDir.exists()) {
 			System.err.println("Cannot find input directory " + inputDirName);
 		}
 		init();
 	}
 
 	public static void recursiveDelete(File dir) {
 		File[] files = dir.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			files[i].delete();
 		}
 	}
 
 	private void init() {
 		// two clusters
 		readClusters.add(new ArrayList<Read>());
 		readClusters.add(new ArrayList<Read>());
 		File[] inputReads = inputDir.listFiles();
 		totalFiles = inputReads.length;
 		// shuffle array first
 		Collections.shuffle(Arrays.asList(inputReads));
 		int roundRobin = 0;
 		for (int i = 0; i < inputReads.length; i++) {
 			readClusters.get(roundRobin).add(
 					new Read(getString(inputReads[i]), roundRobin,
 							inputReads[i].getName()));
 			roundRobin = (roundRobin + 1 == readClusters.size()) ? 0
 					: roundRobin + 1;
 			boolean correctCluster = Integer.parseInt(inputReads[i].getName()
					.charAt(5) + "") == roundRobin;
 			System.out.printf("\t%d,%s,%d\n", roundRobin,
 					inputReads[i].getName(), (correctCluster) ? 1 : 0);
 		}
 	}
 
 	public void sort() {
 		int i = 0;
 		do {
 			System.out.printf("----BEGINNING ITERATION %d----\n", i);
 			i++;
 			// keep count of read allocations as you iterate, then use that to
 			// compare against cluster diff threshold
 			int[] lengths = new int[] { readClusters.get(0).size(),
 					readClusters.get(1).size() };
 			double fileDiff = Math.min((double) lengths[0] / lengths[1],
 					(double) lengths[1] / lengths[0]);
 			if (fileDiff < CLUSTERDIFFTHRESHOLD) {
 				System.out
 						.printf("Relative cluster size max exceeded. fileDiff %f totalFiles %d\n",
 								fileDiff, totalFiles);
 				return;
 			}
 			if (i > 250)
 				return;
 		} while (compressSort());
 	}
 
 	private boolean compressSort() {
 		boolean wasMoved = false;
 		Map<Read, Integer> clusterMap = new HashMap<Read, Integer>();
 		for (ArrayList<Read> cluster : readClusters) {
 			for (Read read : cluster) {
 				// compare compression lengths across clusters
 				int minDist = 0;
 				int belongingToCluster = 0;
 				for (int i = 0; i < readClusters.size(); i++) {
 					int compressDist = getCompressDist(read.readString, i);
 					if (i == 0) {
 						minDist = compressDist;
 					}
 					if (compressDist < minDist) {
 						minDist = compressDist;
 						belongingToCluster = i;
 					}
 					// System.out
 					// .printf("compressDist: %d \t minDist: %d \t cluster: %d \t belongingtoCluster: %d\n",
 					// compressDist, minDist, i,
 					// belongingToCluster);
 				}
 				clusterMap.put(read, belongingToCluster);
 
 			}
 		}
 		// move files to proper clusters
 		// initialize empty new clusters
 		ArrayList<ArrayList<Read>> newClusters = new ArrayList<ArrayList<Read>>(
 				readClusters.size());
 		for (int i = 0; i < readClusters.size(); i++) {
 			newClusters.add(new ArrayList<Read>());
 		}
 
 		int moveCounter = 0;
 		for (Read s : clusterMap.keySet()) {
 			int newCluster = clusterMap.get(s);
 			if (s.cluster != newCluster) {
 				wasMoved = true;
 				s.cluster = newCluster;
 				moveCounter++;
 			}
 			newClusters.get(newCluster).add(s);
 			boolean correctCluster = Integer
					.parseInt(s.fileName.charAt(5) + "") == newCluster;
 			System.out.printf("\t%d,%s,%d\n", newCluster, s.fileName,
 					(correctCluster) ? 1 : 0);
 		}
 		// replace old read clusters
 		readClusters = newClusters;
 		System.err
 				.printf("Moved %d reads to different cluster.\n", moveCounter);
 		return wasMoved;
 
 	}
 
 	private int getCompressDist(String read, int c) {
 		StringBuilder sb = new StringBuilder();
 		// prepend with random data
 		if (PREPEND_RANDOM_DNA)
 			sb.append(ReadGenerator.randomDNA(new Random(), 640)); // 10 * 4^3
 		for (Read s : readClusters.get(c)) {
 			if (!s.readString.equals(read)) {
 				sb.append(s.readString);
 			}
 		}
 		// calculate compression size without file
 		byte[] b = sb.toString().getBytes();
 		Deflater compresser = new Deflater();
 		compresser.setInput(b);
 		compresser.finish();
 		int bytesWithoutFile = compresser.deflate(new byte[b.length]);
 		// now with the file
 		sb.append(read);
 		b = sb.toString().getBytes();
 		compresser = new Deflater();
 		compresser.setInput(b);
 		compresser.finish();
 		int compressionDistance = compresser.deflate(new byte[b.length])
 				- bytesWithoutFile;
 		// System.err.printf("CompressionDistance:\t%d\n", compressionDistance);
 		return compressionDistance;
 	}
 
 	private String getString(File file) {
 		Scanner sc = null;
 		try {
 			sc = new Scanner(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		StringBuilder sb = new StringBuilder();
 		while (sc.hasNextLine()) {
 			sb.append(sc.nextLine());
 		}
 		sc.close();
 		return sb.toString();
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		boolean isRandom = false;
 		String inputDir = null;
 		int nClusters = 0;
 		if (args.length < 2) {
 			System.err.println("Usage: CompressionSort <flags> inputDir nClusters");
 			System.exit(0);
 		}
 		
 		for (int i = 0; i < args.length; i++){
 			if(args[i].charAt(0)=='-'){
 				switch(args[i].charAt(1)){
 				case 'r':
 					isRandom = true;
 					i++;
 					break;
 				}
 			}
 			inputDir = args[i++];
 			nClusters = Integer.parseInt(args[i++]);
 		}
 		
 		for (int i = 0; i < 5; i++) {
 			(new ReadGenerator("temp", "read", new File[] {
 					new File("Genomes/Acidilobus-saccharovorans.fasta"),
 					new File("Genomes/Caldisphaera-lagunensis.fasta") }))
 					.readGenerator(40, 1024);
 			long timeStart = System.currentTimeMillis();
 			CompressionSort cs = new CompressionSort(isRandom, inputDir, nClusters);
 			cs.sort();
 			System.out.println("Done compression sort, took "
 					+ (System.currentTimeMillis() - timeStart) + " ms.");
 		}
 
 	}
 
 }
