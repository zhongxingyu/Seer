 package mono;
 
 import genetic.GeneticAlgorithmFunction;
 import genetic.Genome;
 import genetic.Nature;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 public class Main implements GeneticAlgorithmFunction<Character> {
 	Nature<Character> nature;
 	Map<String, Double> tetagrams;
 	String cipherText;
 
 	public Main() throws IOException {
 		tetagrams = new HashMap<String, Double>();
 		readTetagrams("tetagrams.txt");
 		readCipher("cipher.txt");
 
 		/* There are 26 characters in the alphabet, so that's the genome size. */
		nature = new Nature<Character>(this, 1000, 100, 26, 0.8, 0.00, true);
 		Genome<Character> best = nature.evolve();
 		System.out.println(transscribeCipherText(cipherText, best));
 
 	}
 
 	public void readCipher(String filename) throws FileNotFoundException {
 		String file = new Scanner(new File(filename)).useDelimiter("\\Z")
 				.next();
 
 		file = file.replaceAll("[^A-Za-z]", "");
 		cipherText = file;
 	}
 
 	public String transscribeCipherText(String cipher, Genome<Character> genome) {
 		/* Transcribe ciphertext */
 		char[] newTextArray = cipherText.toCharArray();
 		List<Character> alphabetMap = genome.getGenes();
 		for (int i = 0; i < newTextArray.length; i++) {
 			newTextArray[i] = alphabetMap.get(newTextArray[i] - 'A');
 		}
 
 		String newText = new String(newTextArray);
 
 		return newText;
 	}
 
 	public void readTetagrams(String filename) throws IOException {
 		BufferedReader d = new BufferedReader(new InputStreamReader(
 				new FileInputStream(filename)));
 
 		String line;
 		while ((line = d.readLine()) != null) {
 			Scanner scanner = new Scanner(line);
 			scanner.useDelimiter(" ");
 
 			String tet = scanner.next();
 			Double freq = Double.valueOf(scanner.next());
 			tetagrams.put(tet, freq);
 
 			scanner.close();
 		}
 	}
 
 	/**
 	 * This fitness test rates match between the genome and the order of the
 	 * alphabet.
 	 * 
 	 * @param genome
 	 *            Genome
 	 * @return Rate between 0 and 26
 	 */
 	public double fitnessTest(Genome<Character> genome) {
 		double fitness = 0;
 		for (int i = 0; i < genome.getGenes().size(); i++) {
 			fitness += genome.getGenes().get(i).charValue() == i + 65 ? 1 : 0;
 		}
 
 		return fitness;
 	}
 
 	@Override
 	public double fitness(Genome<Character> genome) {
 		String newText = transscribeCipherText(cipherText, genome);
 		Map<String, Integer> cipherTetagrams = new HashMap<String, Integer>();
 
 		for (int i = 0; i < newText.length() - 4; i++) {
 			String tet = newText.substring(i, i + 4);
 
 			if (cipherTetagrams.containsKey(tet)) {
 				Integer count = cipherTetagrams.get(tet);
 				cipherTetagrams.put(tet, count + 1);
 			} else {
 				cipherTetagrams.put(tet, 1);
 			}
 		}
 
 		double fitness = 0;
 		for (Map.Entry<String, Double> entry : tetagrams.entrySet()) {
 			if (cipherTetagrams.containsKey(entry.getKey())) {
 				Double freq = cipherTetagrams.get(entry.getKey())
 						/ (double) (newText.length() - 3);
 				
 				Double logFreq = Math.log(freq);
 				Double sourceLogFreq = Math.log(entry.getValue());
 				double sigma = 2;
 				//System.out.println(logFreq + " " + sourceLogFreq);
				fitness += Math.exp((logFreq - sourceLogFreq) / sigma);
 			}
 		}
 
 		return fitness;
 	}
 
 	public static double expApprox(double val) {
 		final long tmp = (long) (1512775 * val + 1072632447);
 		return Double.longBitsToDouble(tmp << 32);
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		Main main = new Main();
 		System.exit(0);
 	}
 
 	@Override
 	public Genome<Character> randomGenome(int size) {
 		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		List<Character> genome = new ArrayList<Character>();
 
 		for (int i = 0; i < alphabet.length(); i++) {
 			genome.add(alphabet.charAt(i));
 		}
 
 		Collections.shuffle(genome);
 		return new Genome<Character>(genome);
 	}
 }
