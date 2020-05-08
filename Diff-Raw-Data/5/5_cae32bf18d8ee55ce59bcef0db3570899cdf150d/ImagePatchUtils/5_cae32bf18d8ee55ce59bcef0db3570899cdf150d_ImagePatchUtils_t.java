 package edu.umd.cs.linqs.vision;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.umd.cs.linqs.vision.PatchStructure.Patch;
 import edu.umd.cs.psl.database.Database;
 import edu.umd.cs.psl.database.DatabaseQuery;
 import edu.umd.cs.psl.database.ResultList;
 import edu.umd.cs.psl.model.argument.GroundTerm;
 import edu.umd.cs.psl.model.argument.UniqueID;
 import edu.umd.cs.psl.model.argument.Variable;
 import edu.umd.cs.psl.model.atom.Atom;
 import edu.umd.cs.psl.model.atom.GroundAtom;
 import edu.umd.cs.psl.model.atom.QueryAtom;
 import edu.umd.cs.psl.model.atom.RandomVariableAtom;
 import edu.umd.cs.psl.model.predicate.Predicate;
 import edu.umd.cs.psl.util.database.Queries;
 
 public class ImagePatchUtils {
 
 	static Logger log = LoggerFactory.getLogger(ImagePatchUtils.class);
 
 	public static void insertFromPatchMap(Predicate relation, Database data, Map<Patch, Patch> map) {
 		for (Map.Entry<Patch, Patch> e : map.entrySet()) {
 			UniqueID A = data.getUniqueID(e.getKey().uniqueID());
 			UniqueID B = data.getUniqueID(e.getValue().uniqueID());
 			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(relation, A, B);
 			atom.setValue(1.0);
 			data.commit(atom);
 		}
 	}
 
 	/** do not use **/
 	public static void insertChildren(Predicate relation, Database data, PatchStructure h) {
 		//		for (Map.Entry<Patch, Patch> e : h.getParent().entrySet()) {
 		//			UniqueID B = data.getUniqueID(e.getKey().uniqueID());
 		//			UniqueID A = data.getUniqueID(e.getValue().uniqueID());
 		//			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(relation, A, B);
 		//			atom.setValue(1.0);
 		//			data.commit(atom);
 		//		}
 	}
 
 	public static void insertPixelPatchChildren(Predicate children, Database data, PatchStructure h) {
 		for (Patch p : h.getPatches().values()) {
 			UniqueID patch = data.getUniqueID(p.uniqueID());
 			for (int i : p.pixelList()) {
 				UniqueID pixel = data.getUniqueID(i);
 				RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(children, patch, pixel);
 				atom.setValue(1.0);
 				data.commit(atom);
 			}
 		}
 	}
 
 	public static void insertNeighbors(Predicate neighbor, Database data, PatchStructure ps) {
 		RandomVariableAtom atom;
 		for (Map.Entry<Patch, Patch> e : ps.getNorth().entrySet()) {
 			UniqueID A = data.getUniqueID(e.getKey().uniqueID());
 			UniqueID B = data.getUniqueID(e.getKey().uniqueID());
 			atom = (RandomVariableAtom) data.getAtom(neighbor, A, B);
 			atom.setValue(1.0);
 			data.commit(atom);
 			atom = (RandomVariableAtom) data.getAtom(neighbor, B, A);
 			atom.setValue(1.0);
 			data.commit(atom);
 		}
 		for (Map.Entry<Patch, Patch> e : ps.getEast().entrySet()) {	
 			UniqueID A = data.getUniqueID(e.getKey().uniqueID());
 			UniqueID B = data.getUniqueID(e.getKey().uniqueID());
 			atom = (RandomVariableAtom) data.getAtom(neighbor, A, B);
 			atom.setValue(1.0);
 			data.commit(atom);
 			atom = (RandomVariableAtom) data.getAtom(neighbor, B, A);
 			atom.setValue(1.0);
 			data.commit(atom);
 		}
 	}
 
 	public static void insertPatchLevels(Database data, PatchStructure h, Predicate level) {
 		for (Patch p : h.getPatches().values()) {
 			UniqueID L = data.getUniqueID(p.getLevel());
 			UniqueID A = data.getUniqueID(p.uniqueID());
 			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(level, A, L);
 			atom.setValue(1.0);
 			data.commit(atom);
 		}
 	}
 
 	/**
 	 * Assumes image is columnwise vectorized matrix of grayscale values in [0,1]
 	 * @param brightness predicate of brightness
 	 * @param imageID UniqueID of current image
 	 * @param data database to insert pixel values
 	 * @param hierarchy 
 	 * @param width width of image
 	 * @param height height of image
 	 * @param image vectorized image
 	 * @param mask vectorized mask of which entries to set ground truth on. If null, all entries are entered
 	 */
 	public static void setPixels(Predicate brightness, UniqueID imageID, Database data, PatchStructure hierarchy, int width, int height, double [] image, boolean [] mask) {
 		int k = 0;
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (mask == null || mask[k]) {
 					UniqueID pixel = data.getUniqueID(k);
 					RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(brightness, pixel, imageID);
 					atom.setValue(image[k]);
 					data.commit(atom);		
 				}
 				k++;
 			}
 		}
 	}
 	
 	/**
 	 * Sets k-means representation of observed image.
 	 * 
 	 * Assumes image is columnwise vectorized matrix of grayscale values in [0,1].
 	 * 
 	 * @param hasMean predicate of k-means representation
 	 * @param mean predicate used to store means for image
 	 * @param imageID UniqueID of current image
 	 * @param data database to insert pixel values
 	 * @param hierarchy 
 	 * @param width width of image
 	 * @param height height of image
 	 * @param image vectorized image
 	 * @param mask vectorized mask of which entries to set ground truth on. If null, all entries are entered
 	 */
 	public static void setObservedHasMean(Predicate hasMean, Predicate mean, UniqueID imageID, Database data, int width, int height, int numMeans, double variance, double [] image, boolean [] mask) {
 		/* Counts the number of observed pixels in the image */
 		int numObservedPixels = 0;
 		for (boolean masked : mask)
 			if (masked)
 				numObservedPixels++;
 		
 		/* Puts the observed pixels into their own array */
 		double[] observedImage = new double[numObservedPixels];
 		int j = 0;
 		for (int i = 0; i < image.length; i++)
 			if (mask[i])
 				observedImage[j++] = image[i];
 		
 		/* Sorts the pixel intensities */
 		Arrays.sort(observedImage);
 		
 		/* For each mean, identifies the quantiles, then computes the quantile's mean */
 		double [] means = new double[numMeans];
 		int currentIndex = 0;
 		int numPerQuantile = numObservedPixels / numMeans;
 		int numWithPlusOne = numObservedPixels % numMeans; 
 		for (int m = 0; m < numMeans; m++) {
 			int numInThisQuantile = (m < numWithPlusOne) ? numPerQuantile + 1 : numPerQuantile;
 			int numProcessed = 0;
 			double total = 0.0;
 			while (numProcessed < numInThisQuantile && currentIndex < numObservedPixels) {
 				total += observedImage[currentIndex++];
 				numProcessed++;
 			}
 			means[m] = total / numInThisQuantile;
 		}
 		
 		/* Computes value of hasMean(imageID, pixel, mean) for each pixel and mean */
 		int k = 0;
 		for (int i = 0; i < width; i++) {
 			for (j = 0; j < height; j++) {
 				if (mask == null || mask[k]) {
 					/* Populates HasMean with the k-means representation */
					UniqueID pixelID = data.getUniqueID(k);
 					double[] hasMeanValues = computeHasMean(image[k], means, variance);
 					for (int m = 0; m < numMeans; m++) {
 						UniqueID meanID = data.getUniqueID(m);
						RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(hasMean, imageID, pixelID, meanID);
 						atom.setValue(hasMeanValues[m]);
 						data.commit(atom);
 					}
 				}
 				k++;
 			}
 		}
 		
 		/* Finally, stores the means */
 		for (int m = 0; m < numMeans; m++) {
 			UniqueID meanID = data.getUniqueID(m);
 			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(mean, imageID, meanID);
 			atom.setValue(means[m]);
 			data.commit(atom);
 		}
 	}
 	
 	static double[] computeHasMean(double brightness, double[] means, double variance) {
 		double[] densities = new double[means.length];
 		for (int m = 0; m < means.length; m++) {
 			densities[m] = Math.exp(-1 * (brightness - means[m]) * (brightness - means[m]) / 2 / variance);
 			densities[m] /= Math.sqrt(2 * Math.PI * variance);
 		}
 		
 		double total = 0.0;
 		for (double density : densities)
 			total += density;
 		
 		for (int m = 0; m < means.length; m++)
 			densities[m] /= total;
 		
 		return densities;
 	}
 	
 	public static void populateHasMean(int width, int height, int numMeans, Predicate hasMean, Database data, UniqueID imageID) {
 		for (int m = 0; m < numMeans; m++) {
 			UniqueID meanID = data.getUniqueID(m);
 			int rvCount = 0;
 			int observedCount = 0;
 			int k = 0;
 			for (int i = 0; i < width; i++) {
 				for (int j = 0; j < width; j++) {
 					UniqueID pixelID = data.getUniqueID(k);
 					GroundAtom atom = data.getAtom(hasMean, imageID, pixelID, meanID);
 					if (atom instanceof RandomVariableAtom) {
 						((RandomVariableAtom) atom).setValue(0.0);
 						data.commit((RandomVariableAtom) atom);		
 						rvCount++;
 					}
 						else
 							observedCount++;
 					k++;
 				}
 			}
 			
 			log.debug("Image " + imageID + ", {} observed hasMean atoms, {} random variable hasMean atoms", observedCount, rvCount);
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * @param hasMean
 	 * @param mean
 	 * @param brightness
 	 * @param pic
 	 * @param data
 	 * @param width
 	 * @param height
 	 * @param numMeans
 	 */
 	public static void decodeBrightness(Predicate hasMean, Predicate mean, Predicate brightness, Predicate pic, Database data, int width, int height, int numMeans) {
 		Set<GroundAtom> picAtoms = Queries.getAllAtoms(data, pic);
 		ArrayList<GroundTerm> pics = new ArrayList<GroundTerm>();
 		for (GroundAtom atom : picAtoms) 
 			pics.add(atom.getArguments()[0]);
 		
 		for (GroundTerm imageID : pics) {
 			int k = 0;
 			
 			double [] means = new double[numMeans];
 			
 			for (int m = 0; m < numMeans; m++) {
 				UniqueID meanID = data.getUniqueID(m);
 				GroundAtom meanAtom = data.getAtom(mean, imageID, meanID);
 				means[m] = meanAtom.getValue();
 			}
 			
 			for (int i = 0; i < width; i++) {
 				for (int j = 0; j < height; j++) {
 					UniqueID pixelID = data.getUniqueID(k);
 					RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(brightness, pixelID, imageID);
 					double numer = 0;
 					double denom = 0;
 					
 					for (int m = 0; m < numMeans; m++) {
 						UniqueID meanID = data.getUniqueID(m);
 						GroundAtom hasMeanAtom = data.getAtom(hasMean, imageID, pixelID, meanID);
 						numer += hasMeanAtom.getValue() * means[m];
 						denom += hasMeanAtom.getValue();
 					}
 					
 					atom.setValue(numer / denom);
 					
 					k++;
 				}
 			}
 		}
 	}		
 	
 
 	public static void populatePixels(int width, int height, Predicate pixelBrightness, Database data, UniqueID imageID) {
 		int rv = 0;
 		int ov = 0;
 		int k = 0;
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				UniqueID pixel = data.getUniqueID(k);
 				Atom atom = data.getAtom(pixelBrightness, pixel, imageID);
 				if (atom instanceof RandomVariableAtom) {
 					((RandomVariableAtom) atom).setValue(0.0);
 					data.commit((RandomVariableAtom) atom);		
 					rv++;
 				} else
 					ov++;
 				k++;
 			}
 		}
 
 		log.debug("Saw {} random variables, {} observed variables", rv, ov);
 	}
 
 	/**
 	 * Populate all patches in hierarchy for given image
 	 * @param brightness
 	 * @param imageID
 	 * @param data
 	 * @param hierarchy
 	 */
 	public static void populateAllPatches(Predicate brightness, UniqueID imageID, Database data, PatchStructure hierarchy) {
 		log.debug("Populating " + brightness + " on image " + imageID);
 		for (Patch p : hierarchy.getPatches().values()) {
 			UniqueID patch = data.getUniqueID(p.uniqueID());
 			Atom atom = data.getAtom(brightness, patch, imageID);
 			if (atom instanceof RandomVariableAtom) {
 				data.commit((RandomVariableAtom) atom);
 			}
 		}
 	}
 	//	/**
 	//	 * Populate all patches in hierarchy for given image
 	//	 * @param brightness
 	//	 * @param imageID
 	//	 * @param data
 	//	 * @param hierarchy
 	//	 */
 	//	public static void populateAllPatches(List<Predicate> brightnessList, UniqueID imageID, Database data, PatchStructure hierarchy) {
 	//		for (Patch p : hierarchy.getPatches().values()) {
 	//			int level = p.getLevel();
 	//			UniqueID patch = data.getUniqueID(p.uniqueID());
 	//			Atom atom = data.getAtom(brightnessList.get(level), patch, imageID);
 	//			if (atom instanceof RandomVariableAtom) {
 	//				data.commit((RandomVariableAtom) atom);
 	//			}
 	//		}
 	//	}
 
 	/**
 	 * Loads vectorized image file
 	 * @param filename
 	 * @param width
 	 * @param height
 	 * @return
 	 */
 	public static List<double []> loadImages(String filename, int width, int height) {
 		Scanner imageScanner;
 		List<double []> images = new ArrayList<double []>();
 		try {
 			imageScanner = new Scanner(new FileReader(filename));
 			while (imageScanner.hasNext() && images.size() < 9999) {
 				String line = imageScanner.nextLine();
 				String [] tokens = line.split("\t");
 
 				assert(tokens.length == width * height);
 
 				double [] image = new double[width * height];
 
 				for (int i = 0; i < tokens.length; i++) {
 					image[i] = Double.parseDouble(tokens[i]);
 				}
 				images.add(image);
 			}
 			imageScanner.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return images;
 	}
 
 
 	/**
 	 * precomputes patch brightness from fully-observed database
 	 */
 	public static void computePatchBrightness(Predicate brightness, Predicate pixelBrightness, Database data, UniqueID imageID, PatchStructure ps, double [] image) {
 		log.debug("Computing patch brightness for image {}", imageID);
 		for (Patch p : ps.getPatches().values()) {
 			UniqueID patch = data.getUniqueID(p.uniqueID());
 			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(brightness, patch, imageID);
 			double sum = 0.0;
 			for (Integer pixel : p.pixelList()) {
 				sum += image[pixel];
 			}
 			atom.setValue(sum / p.pixelList().size());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public static void computeNeighborBrightness(Predicate neighborBrightness, Predicate brightness, Predicate neighbors, Database data, UniqueID imageID, PatchStructure ps) {
 		log.debug("Computing neighbor brightness for image {}", imageID);
 		for (Patch p : ps.getPatches().values()) {
 			UniqueID patch = data.getUniqueID(p.uniqueID());
 			RandomVariableAtom atom = (RandomVariableAtom) data.getAtom(neighborBrightness, patch, imageID);
 			double sum = 0.0;
 			Variable neigh = new Variable("neighbor");
 			QueryAtom q = new QueryAtom(neighbors, patch, neigh);
 			ResultList list = data.executeQuery(new DatabaseQuery(q));
 			if (list.size() > 0) {
 				for (int i = 0; i < list.size(); i++) {
 					GroundAtom nb = data.getAtom(brightness, list.get(i)[0], imageID);
 					sum += nb.getValue();
 				}
 				atom.setValue(sum / list.size());
 			}
 		}
 	}
 }
