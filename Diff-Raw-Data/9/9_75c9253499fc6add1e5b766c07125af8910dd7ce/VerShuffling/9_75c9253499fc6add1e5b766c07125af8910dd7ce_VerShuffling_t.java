 package algorithms.verifiers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import main.Logger;
 
 import algorithms.provers.ProveCCPoS;
 import algorithms.provers.ProveShuffling;
 import algorithms.provers.ProveSoC;
 import arithmetic.objects.ElementsExtractor;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.basicelements.BooleanArrayElement;
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.groups.IGroup;
 import arithmetic.objects.groups.IGroupElement;
 import arithmetic.objects.groups.ProductGroupElement;
 import arithmetic.objects.ring.IRing;
 import arithmetic.objects.ring.IntegerRingElement;
 import arithmetic.objects.ring.ProductRingElement;
 import cryptographic.primitives.PseudoRandomGenerator;
 import cryptographic.primitives.RandomOracle;
 
 /**
  * This class provides the functionality of verifying the shuffling.
  * 
  * @author Sofi
  */
 
 public class VerShuffling {
 
 	private static final String POSC_COMMITMENT = "PoSCCommitment";
 	private static final String EMPTY_STRING = "";
 	private static final String PERMUTATION_COMMITMENT = "PermutationCommitment";
 	private static final String POS_REPLY = "PoSReply";
 	private static final String POS_COMMITMENT = "PoSCommitment";
 	private static final String MAXCIPH = "maxciph";
 	private static final String CCPOS_REPLY = "CCPoSReply";
 	private static final String CCPOS_COMMITMENT = "CCPoSCommitment";
 	private static final String BT_FILE_EXT = ".bt";
 	private static final String KEEP_LIST = "KeepList";
 	private static final String PROOFS = "proofs";
 
 	private static ArrayOfElements<IGroupElement> PermutationCommitment;
 	private static Node PoSCommitment;
 	private static Node PoSReply;
 	private static Node PoSCCommitment;
 	private static Node PoSCReply;
 	private static Node CCPoSCommitment;
 	private static Node CCPoSReply;
 	private static BooleanArrayElement keepList;
 
 	private static Logger logger;
 
 	/**
 	 * 
 	 * @param ROSeed
 	 * @param ROChallenge
 	 * @param directory
 	 * @param prefixToRO
 	 * @param lambda
 	 * @param N
 	 * @param ne
 	 * @param nr
 	 * @param nv
 	 * @param prg
 	 * @param Gq
 	 * @param pk
 	 * @param L0
 	 * @param Llambda
 	 * @param posc
 	 * @param ccpos
 	 * @param Zq
 	 * @param width
 	 * @param h
 	 * @param logger
 	 * @return true if verification of shuffling was successful, false
 	 *         otherwise.
 	 */
 	static public boolean verify(RandomOracle ROSeed, RandomOracle ROChallenge,
 			String directory, byte[] prefixToRO, int lambda, int N, int ne,
 			int nr, int nv, PseudoRandomGenerator prg, IGroup Gq,
 			ProductGroupElement pk, 
 			//ArrayOfElements<ProductGroupElement> L0,
 			//ArrayOfElements<ProductGroupElement> Llambda, 
 			ArrayOfElements<ArrayOfElements<ProductGroupElement>> L,
 			boolean posc,
 			boolean ccpos, IRing<IntegerRingElement> Zq, int width,
 			ArrayOfElements<IGroupElement> h, Logger logger1) {
 
 		logger = logger1;
 
 		// set maxciph (indicates if there was pre-computation)
 		int maxciph = readMaxciph(directory);
 		
 		// If maxciph == -1, we use the whole permutation commitment array
 		// (otherwise, we use only the relevant ones).
 		if (maxciph == -1) {
 
 			// read lists of ciphertexts from 1 to lambda.
 			boolean retValue;
 			for (int i = 1; i <= lambda; i++) {
 				retValue = true; // initialize
 
 				if (!readFilesPoS(i, directory, Gq, Zq, N, width)) {
 					retValue = false;
 				}
 
 				retValue = retValue
 						&& ProveShuffling.prove(ROSeed, ROChallenge,
 								prefixToRO, N, ne, nr, nv, prg, Gq, pk,
 								L.getAt(i-1), L.getAt(i), width, PermutationCommitment,
 								PoSCommitment, PoSReply, h, logger);
 
 				// If !retValue, it means that reading the elements
 				// failed or the prover rejected
 				if (!retValue) {
 					if (!compareCiphertextsArrays(L.getAt(i-1), L.getAt(i)))
 						return false;
 				}
 				logger.sendLog("Proof of shuffling of party " + i +" succeeded", Logger.Severity.NORMAL);
 				//TODO remove printout
 				System.out.println("Proof of shuffling of party " + i +" succeeded");
 			}
 			return true;
 
 		} else {
 
 			// maxciph file exists (we have the size of pre-computed arrays)
 			boolean retValue;
 			if (N > maxciph) {
 				return false;
 			}
 
 			for (int i = 1; i <= lambda; i++) {
 				// Step 1 in the algorithm
 				retValue = true; // initialize
 				if (!readFilesPoSC(i, directory, Gq, Zq, N, width)
 						|| (!ProveSoC.prove(ROSeed, ROChallenge, prefixToRO, N,
 								ne, nr, nv, prg, Gq, PermutationCommitment,
 								PoSCCommitment, PoSCCommitment, h, logger))) {
 					// if the algorithm rejects or reading failed set
 					// permutation commitment to be h
 					PermutationCommitment = h;
 				}
 
 				// Step 2: potential early abort
 				if (!ccpos) {
 					return true;
 				}
 
 				// Step 3: Shrink permutation commitment
 				// trying to read the KeepList Array
 				byte[] keepListFile = ElementsExtractor.btFromFile(directory,
 						PROOFS, KEEP_LIST + getNumStringForFileName(i)
 								+ BT_FILE_EXT);
 				// if the file doesn't exist - fill the array with N truths and
 				// the rest are false.
 				if (keepListFile == null) {
 					keepList = new BooleanArrayElement(
 							createBooleanArrayWithNTrue(N, maxciph));
 				} else {
 					keepList = new BooleanArrayElement(keepListFile);
 				}
 				// shrink the permutation commitment according to keep list
 				PermutationCommitment = shrinkPrmutatuonCommitment(maxciph);
 
 				// Step 4 and 5 of the algorithm
 				if (!readFilesCCPos(i, directory, Gq, Zq, N, width)) {
 					retValue = false;
 				}
 
 				retValue = retValue
 						&& ProveCCPoS.prove(ROSeed, ROChallenge,
 								prefixToRO, N, ne, nr, nv, prg, Gq, pk,
 								L.getAt(i-1), L.getAt(i), width, PermutationCommitment,
 								PoSCommitment, PoSReply, h, logger);
 
 				// If !retValue, it means that reading the elements
 				// failed or the prover rejected
 				if (!retValue) {
 					if (!compareCiphertextsArrays(L.getAt(i-1), L.getAt(i)))
 						return false;
 				}
 				logger.sendLog("Proof of shuffling of party " + i +" succeeded", Logger.Severity.NORMAL);
 				//TODO remove printout
 				System.out.println("Proof of shuffling of party " + i +" succeeded");
 			}
 		}
 		logger.sendLog(
 				"Finished verifying shuffling",
 				Logger.Severity.NORMAL);
 		return true;
 	}
 
 	
 	private static ArrayOfElements<IGroupElement> shrinkPrmutatuonCommitment(
 			int maxciph) {
 		ArrayOfElements<IGroupElement> ret = new ArrayOfElements<IGroupElement>();
 
 		for (int j = 0; j < maxciph; j++) {
 			if (keepList.getAt(j)) {
 				ret.add(PermutationCommitment.getAt(j));
 			}
 		}
 		return ret;
 	}
 
 	private static boolean[] createBooleanArrayWithNTrue(int N, int maxciph) {
 		boolean[] tempArr = new boolean[maxciph];
 		for (int j = 0; j < maxciph; j++) {
 			if (j < N) {
 				tempArr[j] = true;
 			} else {
 				tempArr[j] = false;
 			}
 		}
 		return tempArr;
 	}
 
 	/**
 	 * Read the files as byte[]. These byte[] objects will be sent to the
 	 * relevant constructors to make the objects (Node, cipher-texts, array of
 	 * commitment...) that will be sent to the provers.
 	 * 
 	 * @param i
 	 *            the mix server
 	 * @param directory
 	 *            the directory
 	 * @param Gq
 	 *            the IGroup
 	 * @param Zq
 	 *            the IRing
 	 * @param width
 	 * @return true if the reading and extraction of variables succeeded
 	 */
 	private static boolean readFilesPoSC(int i, String directory, IGroup Gq,
 			IRing<IntegerRingElement> Zq, int N, int width) {
 
 		byte[] bPoSCCommitment = ElementsExtractor.btFromFile(directory,
 				PROOFS, POSC_COMMITMENT + getNumStringForFileName(i)
 						+ BT_FILE_EXT);
 		if (bPoSCCommitment == null) {
 			logger.sendLog("POSC commitment file not found.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		byte[] bPoSCReply = ElementsExtractor.btFromFile(directory, PROOFS,
 				"PoSCReply" + getNumStringForFileName(i) + BT_FILE_EXT);
 		if (bPoSCReply == null) {
 			logger.sendLog("POSC reply file not found.", Logger.Severity.ERROR);
 			return false;
 		}
 
 		byte[] bPermutationCommitment = ElementsExtractor.btFromFile(directory,
 				PROOFS, PERMUTATION_COMMITMENT + getNumStringForFileName(i)
 						+ BT_FILE_EXT);
 		if (bPermutationCommitment == null) {
 			logger.sendLog("permutation commitment file not found.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		// create the objects from the byte[] and set the global fields, that
 		// will be sent to the provers.
 		PermutationCommitment = ArrayGenerators.createGroupElementArray(
 				bPermutationCommitment, Gq);
 
 		// first create the nodes as a generic node, then create the appropriate
 		// types from the byte[] data, according to prover Algorithm
 		PoSCCommitment = new Node(bPoSCCommitment);
 
 		// Read the A', C', D' GroupElements
 		PoSCCommitment.setAt(1, ElementsExtractor.createGroupElement(
 				PoSCCommitment.getAt(1).toByteArray(), Gq)); // A'
 
 		PoSCCommitment.setAt(3, ElementsExtractor.createGroupElement(
 				PoSCCommitment.getAt(3).toByteArray(), Gq)); // C'
 
 		PoSCCommitment.setAt(4, ElementsExtractor.createGroupElement(
 				PoSCCommitment.getAt(4).toByteArray(), Gq)); // D'
 
 		// Read B and B' arrays of N elements in Gq, and verify their size
 		ArrayOfElements<IGroupElement> tempB = ArrayGenerators
 				.createGroupElementArray(PoSCCommitment.getAt(0).toByteArray(),
 						Gq);
 		if (tempB.getSize() != N) {
 			return false;
 		}
 		PoSCCommitment.setAt(0, tempB); // B
 
 		tempB = ArrayGenerators.createGroupElementArray(PoSCCommitment.getAt(2)
 				.toByteArray(), Gq);
 		if (tempB.getSize() != N) {
 			return false;
 		}
 		PoSCCommitment.setAt(2, tempB); // B'
 
 		// Create the PoSCReply in the same way
 		PoSCReply = new Node(bPoSCReply);
 
 		// Read Ka, Kc, Kd as RingElements
 		IntegerRingElement tempR = new IntegerRingElement(
 				ElementsExtractor.leafToInt(PoSCReply.getAt(0).toByteArray()),
 				Zq);
 		PoSCReply.setAt(0, tempR); // Ka
 
 		tempR = new IntegerRingElement(ElementsExtractor.leafToInt(PoSCReply
 				.getAt(2).toByteArray()), Zq);
 		PoSCReply.setAt(2, tempR); // Kc
 
 		tempR = new IntegerRingElement(ElementsExtractor.leafToInt(PoSCReply
 				.getAt(3).toByteArray()), Zq);
 		PoSCReply.setAt(3, tempR); // Kd
 
 		// Read Kb and Ke as arrays of Ring Elements, and verify they size == N
 		ArrayOfElements<IntegerRingElement> tempK = ArrayGenerators
 				.createRingElementArray(PoSCReply.getAt(1).toByteArray(), Zq);
 		if (tempK.getSize() != N) {
 			return false;
 		}
 		PoSCReply.setAt(1, tempK); // Kb
 
 		tempK = ArrayGenerators.createRingElementArray(PoSCReply.getAt(4)
 				.toByteArray(), Zq);
 		if (tempK.getSize() != N) {
 			return false;
 		}
 		PoSCReply.setAt(4, tempK); // Ke
 
 		return true;
 	}
 
 	
 	/**
 	 * This method reads the relevant files for the i'th mix server. It sets the
 	 * global fields, and the main function will send them to the Shuffling of
 	 * commitments prover.
 	 * 
 	 * @param i
 	 *            mix server index
 	 * @param directory
 	 *            the directory where the files are
 	 * @param Gq
 	 *            the IGroup
 	 * @param Zq
 	 *            the IRing
 	 * @param width
 	 * @return true if the reading and extraction of variables succeeded
 	 */
 	private static boolean readFilesCCPos(int i, String directory, IGroup Gq,
 			IRing<IntegerRingElement> Zq, int N, int width) {
 
 		// read the files as byte[]
 		byte[] bCCPoSCommitment = ElementsExtractor.btFromFile(directory,
 				PROOFS, CCPOS_COMMITMENT + getNumStringForFileName(i)
 						+ BT_FILE_EXT);
 		if (bCCPoSCommitment == null) {
 			logger.sendLog("ccPOS commitment file not found.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		byte[] bCCPoSReply = ElementsExtractor.btFromFile(directory, PROOFS,
 				CCPOS_REPLY + getNumStringForFileName(i) + BT_FILE_EXT);
 		if (bCCPoSReply == null) {
 			logger.sendLog("ccPOS reply file not found.", Logger.Severity.ERROR);
 			return false;
 		}
 
 		// create the objects from the byte[] and set the global fields
 
 		// first create generic nodes, then create the appropriate types from
 		// the byte[] data according to the prover Algorithm
 		CCPoSCommitment = new Node(bCCPoSCommitment);
 
 		// A' As GroupElement
 		CCPoSCommitment.setAt(0, ElementsExtractor.createGroupElement(
 				CCPoSCommitment.getAt(0).toByteArray(), Gq));
 
 		// read B' as Ciphertext
 		CCPoSCommitment.setAt(1, ElementsExtractor.createCiphertext(
 				CCPoSCommitment.getAt(1).toByteArray(), Gq, width));
 
 		// create the CCPoSReply in the same way
 		CCPoSReply = new Node(bCCPoSReply);
 
 		// read Ka, Kb, Ke as RingElements
 		CCPoSReply.setAt(
 				0,
 				new IntegerRingElement(ElementsExtractor.leafToInt(CCPoSReply
 						.getAt(0).toByteArray()), Zq)); // Ka
 
 		// read Kb as productRingElement
 		CCPoSReply.setAt(1, new ProductRingElement(CCPoSReply.getAt(1)
 				.toByteArray(), Zq, width));
 
 		// read Ke as arrays of Ring Elements, and verify if they are of size N
 		ArrayOfElements<IntegerRingElement> tempK = ArrayGenerators
 				.createRingElementArray(CCPoSReply.getAt(2).toByteArray(), Zq);
 		if (tempK.getSize() != N) {
 			return false;
 		}
 		CCPoSReply.setAt(2, tempK);
 
 		return true;
 	}
 
 	/**
 	 * This method reads the relevant files for the i'th mix server. It sets the
 	 * global fields, and the main function will send them to the Suffling
 	 * prover.
 	 * 
 	 * @param i
 	 *            the relevant mix-server
 	 * @param Gq
 	 *            - The IGroup
 	 * @param width
 	 * @return true if the reading and extraction of variables succeded
 	 */
 	private static boolean readFilesPoS(int i, String directory, IGroup Gq,
 			IRing<IntegerRingElement> Zq, int N, int width) {
 
 		// read the files as byte[].
 
 		byte[] bPoSCommitment = ElementsExtractor.btFromFile(directory, PROOFS,
 				POS_COMMITMENT + getNumStringForFileName(i) + BT_FILE_EXT);
 		if (bPoSCommitment == null) {
 			logger.sendLog("POS commitment file not found.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		byte[] bPoSReply = ElementsExtractor.btFromFile(directory, PROOFS,
 				POS_REPLY + getNumStringForFileName(i) + BT_FILE_EXT);
 		if (bPoSReply == null) {
 			logger.sendLog("POS reply file not found.", Logger.Severity.ERROR);
 			return false;
 		}
 
 		byte[] bPermutationCommitment = ElementsExtractor.btFromFile(directory,
 				PROOFS, PERMUTATION_COMMITMENT + getNumStringForFileName(i)
 						+ BT_FILE_EXT);
 		if (bPermutationCommitment == null) {
 			logger.sendLog("permutation commitment file not found.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		/*
 		 * The following steps create the objects from the byte[] and set the
 		 * global fields, that will be sent to the provers.
 		 */
 		PermutationCommitment = ArrayGenerators.createGroupElementArray(
 				bPermutationCommitment, Gq);
 
 		// first create the nodes like a generic one, and then create the
 		// appropriate types from the byte[] data, according to prover Algorithm
 		PoSCommitment = new Node(bPoSCommitment);
 
 		// Read the A', C', D' GroupElements
 		PoSCommitment.setAt(1, ElementsExtractor.createGroupElement(
 				PoSCommitment.getAt(1).toByteArray(), Gq)); // A'
 
 		PoSCommitment.setAt(3, ElementsExtractor.createGroupElement(
 				PoSCommitment.getAt(3).toByteArray(), Gq)); // C'
 
 		PoSCommitment.setAt(4, ElementsExtractor.createGroupElement(
 				PoSCommitment.getAt(4).toByteArray(), Gq)); // D'
 
 		// read F' Ciphertext
 		PoSCommitment.setAt(5, ElementsExtractor.createCiphertext(PoSCommitment
 				.getAt(5).toByteArray(), Gq, width));
 
 		// read B and B' arrays of N elements in Gq
 		ArrayOfElements<IGroupElement> tempB = ArrayGenerators
 				.createGroupElementArray(PoSCommitment.getAt(0).toByteArray(),
 						Gq);
 		if (tempB.getSize() != N) {
 			return false;
 		}
 		PoSCommitment.setAt(0, tempB); // B
 
 		tempB = ArrayGenerators.createGroupElementArray(PoSCommitment.getAt(2)
 				.toByteArray(), Gq);
 		if (tempB.getSize() != N) {
 			return false;
 		}
 		PoSCommitment.setAt(2, tempB); // B'
 
 		// create the PoSReply in the same way
 		PoSReply = new Node(bPoSReply);
 
 		// Read Ka, Kc, Kd as RingElements
 		PoSReply.setAt(
 				0,
 				new IntegerRingElement(ElementsExtractor.leafToInt(PoSReply
 						.getAt(0).toByteArray()), Zq)); // Ka
 
 		PoSReply.setAt(
 				2,
 				new IntegerRingElement(ElementsExtractor.leafToInt(PoSReply
 						.getAt(2).toByteArray()), Zq)); // Kc
 
 		PoSReply.setAt(
 				3,
 				new IntegerRingElement(ElementsExtractor.leafToInt(PoSReply
 						.getAt(3).toByteArray()), Zq)); // Kd
 
 		// Read Kf as productRingElement
 		PoSReply.setAt(5, new ProductRingElement(PoSReply.getAt(5)
 				.toByteArray(), Zq, width));
 
 		// Read Kb and Ke as arrays of Ring Elements
 		ArrayOfElements<IntegerRingElement> tempK = ArrayGenerators
 				.createRingElementArray(PoSReply.getAt(1).toByteArray(), Zq);
 		if (tempK.getSize() != N) {
 			return false;
 		}
 		PoSReply.setAt(1, tempK); // Kb
 
 		tempK = ArrayGenerators.createRingElementArray(PoSReply.getAt(4)
 				.toByteArray(), Zq);
 		if (tempK.getSize() != N) {
 			return false;
 		}
 		PoSReply.setAt(4, tempK); // Ke
 
 		return true;
 	}
 
 	/**
 	 * Returns the maxciph read from file or -1 if the file doesn't exists.
 	 * 
 	 * @param directory
 	 *            - the directory where the file should be.
 	 */
 	private static int readMaxciph(String directory) {
 		Scanner text = null;
 		try {
			text = new Scanner(new File (new File(directory, PROOFS), MAXCIPH));
 		} catch (FileNotFoundException e) {
 			return -1;
 		}
 
 		return text.nextInt();
 	}
 
 	private static boolean compareCiphertextsArrays(
 			ArrayOfElements<ProductGroupElement> one,
 			ArrayOfElements<ProductGroupElement> two) {
 
 		for (int i = 0; i < one.getSize(); i++) {
 			if (!one.getAt(i).equals(two.getAt(i))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	// TODO printout method - delete?
 		static String bytArrayToHex(byte[] a) {
 			StringBuilder sb = new StringBuilder();
 			for (byte b : a)
 				sb.append(String.format("%02x", b & 0xff));
 			return sb.toString();
 		}
 		
 		/**
 		 * @param i
 		 *            the number to change
 		 * @return the number as string, if i < 10, add "0" to the begining of the
 		 *         string
 		 */
 		private static String getNumStringForFileName(int i) {
 			return (i < 10 ? "0" : EMPTY_STRING) + i;
 		}
 
 }
