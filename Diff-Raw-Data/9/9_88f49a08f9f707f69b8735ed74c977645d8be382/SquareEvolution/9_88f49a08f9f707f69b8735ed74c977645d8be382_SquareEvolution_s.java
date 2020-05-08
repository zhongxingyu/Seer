 package SE;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.Writer;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import Logging.BadProteinLogEntry;
 import Logging.FitnessLogEntry;
 import Logging.FoldedProteinLogEntry;
 import Logging.LogWriter;
 import Logging.OutputLogEntry;
 
 
 public class SquareEvolution {
 
	private static final String VERSION = "2.5.2";
 
 	private static String[] bases = {"A","G","C","T"};
 
 	/*
 	 * fitness of genes that make no protein at all
 	 * 	see "Square Evolution Log 01.doc" page 58
 	 *  see also "Square Evolution Log 06.docx" page 45
 	 */
 	public static final double NO_PROTEIN_FITNESS = 1E-18f;
 
 	/*
 	 * fitness of genes that make protein that has no
 	 * structure (Structure = "none")
 	 * see "Square Evolution Log 01.doc" page 58
 	 * see also "Square Evolution Log 06.docx" page 45
 	 */
 	public static final double NO_STRUCTURE_FITNESS = 1E-18f;
 
 	private NaturalSelection natSel;
 
 	private Configuration config;
 
 	private ProteinDatabase proteinDatabase;
 
 	private Random r;
 
 	private LogWriter logWriter;
 
 	private FitnessProcessor fitnessProcessor;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SquareEvolution squareEvolution = null;
 		String configFile = "test.xml";
 		if (args.length == 1) {
 			if (args[0].equals("-v")) {
 				printInfoAndExit();
 			} else {
 				configFile = args[0];
 			}
 		}
 		squareEvolution = new SquareEvolution(configFile);
 		squareEvolution.run();
 	}
 
 	public SquareEvolution(String configFileName) {
 		config = new Configuration(configFileName);
 		r = new Random();
 		proteinDatabase = ProteinDatabase.getInstance();
 		logWriter = new LogWriter(config);
 		fitnessProcessor = new FitnessProcessor(config, proteinDatabase);
 	}
 
 
 	private void run() {
 
 		Socket s = null;
 
 		try {
 			// start connection to folding server
 			s = new Socket("localhost", config.getPortNum());
 
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(s.getInputStream()));
 			PrintStream out = new PrintStream(
 					s.getOutputStream(), true /* autoFlush */);
 
 			//OutputLog header
 			Date start = new Date();
 			logWriter.addLogEntry(new OutputLogEntry("Square Aipotu Version = " + VERSION));
 			logWriter.addLogEntry(new OutputLogEntry(config.toString()));
 			logWriter.addLogEntry(new OutputLogEntry("Start =" + start.toString()));
 
 			// fitness log header
 			logWriter.addLogEntry(new FitnessLogEntry(config.isNearestNeighborMode()));
 
 			// if desired, protein folding log header
 			if (!config.getProteinFoldingFileName().equals("")) {
 				logWriter.addLogEntry(new FoldedProteinLogEntry());
 			}
 
 			// bad protein log header
 			logWriter.addLogEntry(new BadProteinLogEntry());
 
 			// see discussion of neutrality in "Square Evolution Log 02.docx" pages 14, 17, 25, 26
 			double adjustedNoProteinFitness = 
 					config.getNeutrality() + (1 - config.getNeutrality()) * NO_PROTEIN_FITNESS;
 
 			if (!config.isNearestNeighborMode()) {
 				/*
 				 * normal mode => evolution
 				 */
 				Organism[] world = new Organism[config.getPopulationSize()];
 				String[] proteins = new String[config.getPopulationSize()];
 
 				for (int i = 0; i < config.getNumRuns(); i++) {
 
 					// clear folded protein db at each run
 					// (since random, probably different proteins each time anyway)
 					// put in an entry in the database for no protein
 					proteinDatabase.clearAndAddBlankEntry(config);
 
 					Date runStartTime = new Date();
 
 					// set up starting world (generation 0)
 					if (config.getStartingOrganismDNA().equals("Random")) {
 						RandomDNASequenceGenerator rgen = RandomDNASequenceGenerator.getInstance();
 						for (int x = 0; x < config.getPopulationSize(); x++) {
 							world[x] = new TypeAOrganism(
 									rgen.generateRandomNonCodingNmer(
 											config.getGeneticCode(),
 											config.getStartingRandomDNASequenceLength()), 
 											config.getGeneticCode());
 						}
 					} else {
 						for (int x = 0; x < config.getPopulationSize(); x++) {
 							world[x] = new TypeAOrganism(
 									config.getStartingOrganismDNA(), 
 									config.getGeneticCode());
 						}
 					}
 
 					// run the generations
 					for (int g = 0; g < config.getStopAtGeneration(); g++) {
 
 						HashSet<String>	proteinsToFold = new HashSet<String>();
 
 						// express the proteins & see if they need to be folded
 						for (int x = 0; x < config.getPopulationSize(); x++) {
 							proteins[x] = 
 									(world[x].getGeneticCode()).translateWithCache(world[x].getDNA());
 
 							/*
 							 * if there is a protein and it is too short, it is absurd
 							 * see Square Evolution Log 03 p13
 							 * so put in dummy entry for it
 							 * 
 							 * but, if there's no protein, don't do this - you should get the
 							 *  NO_PROTEIN_FITNESS, not the NO_STRUCTURE_FITNESS
 							 *  
 							 */
 							if ((proteins[x].length() < config.getMinAcceptableProteinLength())
 									&& (!proteins[x].equals(""))) {
 								/*
 								 * note that fitness is adjusted in constructor to account for neutrality
 								 * see Square Evolution Log 06 page 25
 								 * 
 								 */
 								proteinDatabase.addEntry(new ProteinDatabaseEntry(
 										config,
 										proteins[x], 
 										"None", 
 										config.getLigandSequence(),
 										config.getLigandStructure(),
 										config.getLigandRotamer(),	
 										0,					// best lig x
 										0,					// best lig y
 										Double.MIN_VALUE,	// dGf
 										Double.MIN_VALUE,	// dGb
 										NO_STRUCTURE_FITNESS));	// fitness
 							}
 
 							/*
 							 *  make sure the submission won't be longer than 100 chars
 							 *  that's the max (including terminating \0) the buffer
 							 *  at the CUDAFolder can hold
 							 *  so the protein + ligand seq + ligand str + 2 (rotamer)
 							 *    + 3 commas + \0 <= 100
 							 *  90 to be safe
 							 */
 							if ((proteins[x].length() 
 									+ config.getLigandSequence().length() 
 									+ config.getLigandStructure().length()) > 90) { 
 
 								/*
 								 * in that case, add a dummy entry
 								 * note that fitness is adjusted in constructor to account for neutrality
 								 * see Square Evolution Log 06 page 25
 								 * 
 								 */
 								proteinDatabase.addEntry(new ProteinDatabaseEntry(
 										config,
 										proteins[x], 
 										"None", 
 										config.getLigandSequence(),
 										config.getLigandStructure(),
 										config.getLigandRotamer(),	
 										0,					// best lig x
 										0,					// best lig y
 										Double.MIN_VALUE,	// dGf
 										Double.MIN_VALUE,	// dGb
 										NO_STRUCTURE_FITNESS));	// fitness
 
 							}
 							if (!proteinDatabase.containsEntry(
 									proteins[x], 
 									config.getLigandSequence(), 
 									config.getLigandStructure())) {
 								proteinsToFold.add(proteins[x]);
 							}
 						}
 
 						if (config.getProteinFoldingFileName().equals("")) {
 							foldProteins(proteinsToFold, in, out, false);
 						} else {
 							foldProteins(proteinsToFold, in, out, true);
 						}
 
 						// get fitness result for all generations
 						//   only calculate diversity when a fitness report generation
 						FitnessResult fr = fitnessProcessor.calculateFitnesses(
 								world, 
 								proteins, 
 								g%config.getFitnessReportInterval() == 0);
 
 						// debugging see SquareEvolutionLog 06 page 10+
 						if (fr.getTotalFitness()/config.getPopulationSize() == Double.NaN) {
 							System.out.println("NaN avg fit: run= " + i 
 									+ "; Gen= " + g 
 									+ "; totFit= " + fr.getTotalFitness()
 									+ "; pop size= " + config.getPopulationSize()
 									+ "; avgFit = " + fr.getTotalFitness()/config.getPopulationSize());
 						}
 
 						if (g%config.getMiniReportInterval() == 0) {
 							logWriter.addLogEntry(
 									new OutputLogEntry("Run: " + i
 											+ " Gen: " + g
 											+ " Av_fit: " + fr.getTotalFitness()/config.getPopulationSize() 
 											+ " Max_fit: " + fr.getMaxFitness()
 											+ " " + fr.getBestEntry().proteinSequence));
 						}
 
 						if (g%config.getBigReportInterval() == 0) {
 							logWriter.addLogEntry(
 									new OutputLogEntry(
 											config.getGeneticCode().prettyTranslate(fr.getBestDNA())));
 							logWriter.addLogEntry(
 									new OutputLogEntry(DisplayStructure.getStructure(
 											fr.getBestEntry().proteinSequence, 
 											fr.getBestEntry().proteinStructure, 
 											fr.getBestEntry().ligandSequence, 
 											fr.getBestEntry().ligandStructure, 
 											fr.getBestEntry().bestRotamer, 
 											fr.getBestEntry().bestLigX, 
 											fr.getBestEntry().bestLigY)));
 						}
 
 						if (g%config.getFitnessReportInterval() == 0) {
 							logWriter.addLogEntry(
 									new FitnessLogEntry(i + ","
 											+ g + ","
 											+ fr.getMaxFitness() + "," 
 											+ fr.getTotalFitness()/config.getPopulationSize() + ","
 											+ fr.getBestEntry().proteinSequence.length() + ","
 											+ fr.getBestEntry().bestLigX + ","
 											+ fr.getBestEntry().bestLigY + ","
 											+ fr.getBestEntry().bestRotamer + ","
 											+ fr.getBestEntry().proteinSequence + ","
 											+ fr.getNumberOfDNASpecies() + ","
 											+ fr.getShannonDNADiversity() + ","
 											+ fr.getNumberOfProtSpecies() + ","
 											+ fr.getShannonProtDiversity()));
 						}
 
 						/* output info on all organisms in world to file GenX.csv in Generations/
 						 * one file per generation
 						 * DNA, protein, fitness
 						 * do this immediately (not in log writer)
 						 */
 						if (config.outputWorldEachGeneration()) {
 							String worldFileName = String.format("Generations/Gen%04d.csv", g);
 							File worldFile = new File(worldFileName);
 							Writer worldWriter = new BufferedWriter(new FileWriter(worldFile));
 							try {
 								worldWriter.write("Index,DNA,Protein,Fitness\n");
 								for (int n = 0; n < config.getPopulationSize(); n++) {
 									String protein = proteins[n];
 									ProteinDatabaseEntry pde = proteinDatabase.getEntry(
 											protein,
 											config.getLigandSequence(),
 											config.getLigandStructure());
 									worldWriter.write(n + "," 
 											+ world[n].getDNA() + "," 
 											+ protein + "," 
 											+ pde.fitness + "\n");
 								}
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 							finally {
 								worldWriter.close();
 							}
 						}
 
 						//make next generation
 						int popSize = config.getPopulationSize();
 						Organism[] selectedOrganisms = new Organism[popSize];
 
 						if(config.isPickSingleBestOrganism()) {
 							/*
 							 * for testing purposes, this makes new pop from popSize 
 							 *   copies of single fittest organism from prev generation 
 							 */
 							for (int x = 0; x < popSize; x++) {
 								selectedOrganisms[x] = new TypeAOrganism(fr.getBestDNA(), config.getGeneticCode());
 							}
 
 						} else {
 							/*
 							 * normal mode = pick in proportion to fitness
 							 */
 							natSel = new NaturalSelection(fr.getCumulativeFitnesses());
 
 							// pick by fitness
 							for (int x = 0; x < popSize; x++) {
 								selectedOrganisms[x] = world[natSel.getRandomOrganismIndexByFitness()];
 							}
 						}
 						// mutate them
 						world = config.getMutator().massMutate(selectedOrganisms);
 						selectedOrganisms = null;
 					}
 
 					config.getGeneticCode().clearTranslatedSequenceCache();
 
 					Date runEndTime = new Date();
 					long runTime = runEndTime.getTime() - runStartTime.getTime();
 					logWriter.addLogEntry(
 							new OutputLogEntry("Run # " + i + " took " + runTime/1000 + " seconds."));
 					logWriter.addLogEntry(new OutputLogEntry("***********"));
 					logWriter.addLogEntry(new OutputLogEntry(""));
 				}
 				
 				// final log entries
 				Date end = new Date();
 				logWriter.addLogEntry(new OutputLogEntry("End: " + end.toString())); 
 				long timeElapsed = (end.getTime() - start.getTime());
 				logWriter.addLogEntry(
 						new OutputLogEntry("It took:"));
 				logWriter.addLogEntry(
 						new OutputLogEntry("\t" + TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + " seconds."));
 				logWriter.addLogEntry(
 						new OutputLogEntry("\t" + TimeUnit.MILLISECONDS.toHours(timeElapsed) + " hours."));
 				logWriter.addLogEntry(
 						new OutputLogEntry("\t" + TimeUnit.MILLISECONDS.toDays(timeElapsed) + " days."));	
 				logWriter.addLogEntry(
 						new OutputLogEntry("Final Stats:"));
 				logWriter.addLogEntry(
 						new OutputLogEntry("Minimum nonzero fitness = " + proteinDatabase.getMinimumFitness()));
 				logWriter.addLogEntry(
 						new OutputLogEntry("Number of proteins folded = " + proteinDatabase.getSize()));
 				logWriter.addLogEntry(
 						new OutputLogEntry(FoldingErrorLog.getInstance().toString()));
 
 			} else {
 				/*
 				 * nearest neighbor mode - just take the DNAs in the DNAforNNAFileName file 
 				 * and find the nearest single bp mutants from them
 				 */
 
 				// initialize
 				proteinDatabase.clearAndAddBlankEntry(config);
 
 				// read in set of starting DNAs
 				ArrayList<String> startingDNAs = new ArrayList<String>();
 				BufferedReader br = new BufferedReader(new FileReader(config.getDNAforNNAFileName()));
 				String line = null;
 				while ((line = br.readLine()) != null) {
 					startingDNAs.add(line.trim());
 				}
 				br.close();
 
 				// determine task from config
 				String task = "";
 				if (config.getLigandRotamer() == 2) task = "D"; // dimers
 				if (config.getLigandRotamer() == -1) task = "L"; // ligand binders
 				if (config.getLigandRotamer() == 0) task = "P"; // polymers
 
 				/*
 				 * for each one, find all the single mutants
 				 *  and add them to the "to do" set
 				 */
 				ArrayList<NeighborResult> allResults = new ArrayList<NeighborResult>();
 				for (int i = 0; i < startingDNAs.size(); i++) {
 					ArrayList<NeighborResult> results = new ArrayList<NeighborResult>();
 					// first one is the starting one
 					String currentDNA = startingDNAs.get(i);
 					NeighborResult first = new NeighborResult(i, currentDNA);
 					String startingProtein = config.getGeneticCode().translateWithCache(currentDNA);
 					first.protein = startingProtein;
 					first.task = task;
 					first.silent = true;
 					results.add(first);
 
 					// then, all the mutants of that sequence
 					for (int x = 0; x < currentDNA.length(); x++) {
 						String ob = currentDNA.substring(x, x + 1);
 						for (String nb: bases) {
 							if (!nb.equals(ob)) {
 								StringBuffer sb = new StringBuffer(currentDNA);
 								sb.replace(x, x + 1, nb);
 								String DNA = sb.toString();
 								String protein = config.getGeneticCode().translateWithCache(DNA);
 								NeighborResult nr = new NeighborResult(i, DNA);
 								nr.codeName = config.getGeneticCodeFileName();
 								nr.protein = protein;
 								nr.silent = protein.equals(startingProtein);
 								nr.task = task;
 								results.add(nr);
 							}
 						}
 					}
 					/*
 					 * collect all proteins that need folding
 					 * and do sanity checks like above
 					 */
 					HashSet<String> proteinsToFold = new HashSet<String>();
 					for (NeighborResult nr : results) {		
 						String p = nr.protein;
 						if ((p.length() < config.getMinAcceptableProteinLength())
 								&& (!p.equals(""))) {
 							proteinDatabase.addEntry(new ProteinDatabaseEntry(
 									config,
 									p, 
 									"None", 
 									config.getLigandSequence(),
 									config.getLigandStructure(),
 									config.getLigandRotamer(),	
 									0,					// best lig x
 									0,					// best lig y
 									Double.MIN_VALUE,	// dGf
 									Double.MIN_VALUE,	// dGb
 									NO_STRUCTURE_FITNESS));	// fitness
 						}
 
 						if ((p.length() 
 								+ config.getLigandSequence().length() 
 								+ config.getLigandStructure().length()) > 90) { 
 							proteinDatabase.addEntry(new ProteinDatabaseEntry(
 									config,
 									p, 
 									"None", 
 									config.getLigandSequence(),
 									config.getLigandStructure(),
 									config.getLigandRotamer(),	
 									0,					// best lig x
 									0,					// best lig y
 									Double.MIN_VALUE,	// dGf
 									Double.MIN_VALUE,	// dGb
 									NO_STRUCTURE_FITNESS));	// fitness
 
 						}
 						if (!proteinDatabase.containsEntry(
 								p, 
 								config.getLigandSequence(), 
 								config.getLigandStructure())) {
 							proteinsToFold.add(p);
 						}
 					}
 					foldProteins(proteinsToFold, in , out, false);
 					
 					// put the proper values in the results
 					double fitnessOfStartingProtein = proteinDatabase.getEntry(
 							startingProtein, config.getLigandSequence(), 
 								config.getLigandStructure()).fitness;
 					for (NeighborResult nr : results) {
 						double fitness = proteinDatabase.getEntry(
 								nr.protein, config.getLigandSequence(), 
 								config.getLigandStructure()).fitness;
 						nr.rawFit = fitness;
 						nr.normFit = fitness/fitnessOfStartingProtein;
 					}
 					
 					// pool results from each sequence into monster
 					allResults.addAll(results);
 				}
 				
 				// output the results
 				for (NeighborResult nr : allResults) {
 					logWriter.addLogEntry(new FitnessLogEntry(nr.toString()));
 				}
 			}
 
 			out.print("?");  // tell the server to close the socket
 
 
 			// write out the logs
 			Date startSave = new Date();
 			logWriter.saveLogs();
 			Date endSave = new Date();
 			long saveTime = endSave.getTime() - startSave.getTime();
 			System.out.println("It took " + saveTime + " mSec to save the logs.");
 
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 	private void foldProteins(HashSet<String> toFold, 
 			BufferedReader in, 
 			PrintStream out,
 			boolean logFoldedProteins) throws IOException {
 
 		//		System.out.println("Folding " + toFold.size() + " sequences.");
 		// convert set to array for easier processing
 		String[] proteinsToFold = new String[toFold.size()];
 		Iterator<String> it = toFold.iterator();
 		int i = 0;
 		while (it.hasNext()) {
 			String next = it.next();
 
 			proteinsToFold[i] = next;
 			i++;
 		}
 
 		// break set into groups of 100 per server request (run)
 		int numFoldingRuns = (toFold.size() + 99)/100;
 
 		for (int run = 0; run < numFoldingRuns; run++) {
 
 			// build the request string for this run
 			StringBuffer b = new StringBuffer();
 			int x = 0;
 			for (x = 0; x < 100; x++) {
 				if (((100 * run) + x) >= toFold.size()) break;
 				b.append(proteinsToFold[((100 * run) + x)] + ","
 						+ config.getLigandSequence() + ","
 						+ config.getLigandStructure() + ","
 						+ config.getLigandRotamer() + ";");
 			}
 			String request = b.toString();
 			int numProtsInThisSet = x;
 			// send to server & get response
 			out.print(request);
 			out.flush();
 			String response = in.readLine();
 
 			/*
 			 *  process response string
 			 *  pieces[0] = protein seq
 			 *  pieces[1] = dGfolding
 			 *  pieces[2] = protein structure
 			 *  pieces[3] = best binding energy
 			 *  pieces[4] = binding partition sum
 			 *  pieces[5] = best bindging rotamer
 			 *  pieces[6] = best binding ligand X
 			 *  pieces[7] = best binding ligand Y
 			 */
 			String[] responses = response.split(";");
 			if (responses.length != (numProtsInThisSet + 1)) {  // first is padding
 				System.out.println(
 						"Monster error! Sent " 
 								+ numProtsInThisSet 
 								+ " seqs to fold; got back " + responses.length);
 			}
 			for (int y = 0; y < responses.length; y++) {
 				String[] pieces = responses[y].split(",");
 				if (pieces.length == 8) {
 					String proteinStructure = pieces[2];
 					if (proteinStructure.contains("Folding or Binding")) {
 						// log the error
 						FoldingErrorLog.getInstance().addError(proteinStructure);
 						// give it 'none' structure
 						proteinStructure = "None";
 					}
 
 					/*
 					 * with some proteins, you get an infinite energy
 					 *    probably an overflow
 					 *    if so, log error and give no conformation
 					 */
 					boolean badEnergyError = false;
 					if (pieces[1].equals("inf") || pieces[1].equals("nan")) {
 						FoldingErrorLog.getInstance().addError("Infinite or Nan dGfolding");
 						badEnergyError = true;
 					}
 					if (pieces[3].equals("inf") || pieces[3].equals("nan")) {
 						FoldingErrorLog.getInstance().addError("Infinite or Nan best binding energy");
 						badEnergyError = true;
 					}
 					if (pieces[4].equals("inf") || pieces[4].equals("nan")) {
 						FoldingErrorLog.getInstance().addError("Infinite or Nan binding partition sum");
 						badEnergyError = true;
 					}
 
 					if(badEnergyError) {
 						logWriter.addLogEntry(
 								new BadProteinLogEntry(pieces[0] + "," 
 										+ pieces[1] + "," 
 										+ pieces[3] + "," 
 										+ pieces[4]));
 
 						/*
 						 * in that case, add a dummy entry
 						 * note that fitness is adjusted in constructor to account for neutrality
 						 * see Square Evolution Log 06 page 25
 						 * 
 						 */
 						ProteinDatabase.getInstance().addEntry(
 								new ProteinDatabaseEntry(
 										config,
 										pieces[0], 
 										"None", 
 										config.getLigandSequence(),
 										config.getLigandStructure(),
 										Integer.parseInt(pieces[5]),	
 										0,					// best lig x
 										0,					// best lig y
 										Double.MIN_VALUE,	// dGf
 										Double.MIN_VALUE,	// dGb
 										NO_STRUCTURE_FITNESS));	// fitness
 
 					} else {
 						double bestBindingEnergy = Double.parseDouble(pieces[3]);
 						double bindingPartitionSum = Double.parseDouble(pieces[4]);
 						double z = bindingPartitionSum - Math.exp(-bestBindingEnergy);
 						/*
 						 * theoretically, this should NEVER be < 0
 						 * 	since it's the sum - the max!
 						 *  but floating point errors can do this
 						 *  so set to 0 
 						 *  see SquareProteinEvolution Log 6 pages 10, 18-20 
 						 *  
 						 *  but only do this if the protein has a structure
 						 *    (it turns out that z < 0 if protein has no structure
 						 *      or is too long, etc and didn't get bound)
 						 *      
 						 */
 						if ((z < 0) && (!proteinStructure.equals("None"))) {
 							logWriter.addLogEntry(new BadProteinLogEntry(
 									"z < 0; " + pieces[0]
 											+ "; bindPartSum = " + bindingPartitionSum 
 											+ "; numUnbStates= " + config.getNumUnboundStates() 
 											+ "; expBestBindNrg= " + Math.exp(-bestBindingEnergy)));
 							z = 0;
 						}
 						double dGb = bestBindingEnergy + Math.log(z + config.getNumUnboundStates());
 						ProteinDatabase.getInstance().addEntry(
 								new ProteinDatabaseEntry(
 										config,
 										pieces[0], 
 										proteinStructure,
 										config.getLigandSequence(),
 										config.getLigandStructure(),
 										Integer.parseInt(pieces[5]), 
 										Integer.parseInt(pieces[6]), 
 										Integer.parseInt(pieces[7]), 
 										Double.parseDouble(pieces[1]), 
 										dGb));
 						if (logFoldedProteins) {
 							logWriter.addLogEntry(new FoldedProteinLogEntry(
 									pieces[0] + ","
 											+ pieces[2] + ","
 											+ config.getLigandSequence() + ","
 											+ config.getLigandStructure() + ","
 											+ pieces[1] + ","
 											+ pieces[3] + ","
 											+ pieces[4] + ","
 											+ pieces[5] + ","
 											+ pieces[6] + ","
 											+ pieces[7]));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private static void printInfoAndExit() {
 		System.out.println("Square evolution version " + VERSION);
 		System.out.println("These are the commands you can run:");
 		System.out.println("\tSE.TestCode <GeneticCodeFileName> - tests a genetic code file.");
 		System.out.println("\tSE.PrettyTrans <GeneticCodeFileName> <DNASequenceToTranslate> - translates DNA with a specific genetic code.");
 		System.out.println("\tSE.RevTrans <GeneticCodeFileName> <ProteinToReversetranslate> - finds a DNA that encodes that protein with that genetic code.");
 		System.out.println("\tSE.ProteinPicture - makes pretty pictures of proteins in an output.txt file.");
 		System.exit(0);
 	}
 }
