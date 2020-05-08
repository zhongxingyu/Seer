 /**
  * Extract and display Scale Invariant Features after the method of David Lowe
  * \cite{Lowe04} in an image.
  * 
  * BibTeX:
  * <pre>
  * &#64;article{Lowe04,
  *   author    = {David G. Lowe},
  *   title     = {Distinctive Image Features from Scale-Invariant Keypoints},
  *   journal   = {International Journal of Computer Vision},
  *   year      = {2004},
  *   volume    = {60},
  *   number    = {2},
  *   pages     = {91--110},
  * }
  * </pre>
  */
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import mpi.cbg.fly.Feature;
 import mpi.cbg.fly.Filter;
 import mpi.cbg.fly.FloatArray2D;
 import mpi.cbg.fly.FloatArray2DSIFT;
 
 import org.neuroph.core.learning.SupervisedTrainingElement;
 import org.neuroph.core.learning.TrainingSet;
 import org.neuroph.nnet.MultiLayerPerceptron;
 import org.neuroph.util.TransferFunctionType;
 
 public class CbirWithSift extends JFrame {
 	// helper variables for the repaint
 	IgsImage cur_image;
 
 	// the extracted visual words - model for the VisualWordHistogram
 	List<VisualWord> bagofwords = new Vector<VisualWord>();
 
 	// a model to classify a VisualWordHistogram into a ImageClass
 	Object decisionModel;
 
 	// how many visual words should by classified
 	private static int K = 300;
 
 	// the minimum count of members in a "visual-word" class
 	private static int MIN_CLASS_SIZE = 5;
 
 	// how many images should be read from the input folders
 	private static int readImages = 10;
 	// private static int readImages = 10000;
 
 	// number of SIFT iterations: more steps will produce more features
 	// default = 4
 	private static int steps = 5;
 
 	// for testing: delay time for showing images in the GUI
 	private static int wait = 0;
 
 	/**
 	 * 
 	 * REPLACE THIS METHOD
 	 * 
 	 * 
 	 * Classifies a VisualWordHistogram into an Image Class based on the learned
 	 * model
 	 * 
 	 * @param histogram
 	 *            the given VisualWordHistogram
 	 * @return the name of the Image-Class
 	 */
 	public String doClassifyImageContent(int[] histogram) {
 
 		MultiLayerPerceptron myMlPerceptron = (MultiLayerPerceptron) decisionModel;
 
 		// convert to double[]
 		double[] dhistogram = new double[histogram.length];
 		for (int i = 0; i < histogram.length; i++) {
 			dhistogram[i] = histogram[i];
 		}
 
 		myMlPerceptron.setInput(dhistogram);
 		myMlPerceptron.calculate();
 		double[] output = myMlPerceptron.getOutput();
 
 		if (output[0] <= 0.5d) {
 			return "motorbike";
 		} else {
 			return "airplane";
 		}
 
 	}
 
 	/**
 	 * 
 	 * REPLACE THIS METHOD
 	 * 
 	 * 
 	 * Learns a model based on the training data set
 	 * 
 	 * @param dataSet
 	 *            a list of VisualWordHistograms for each Image-Class
 	 * @return a model for the Classifier
 	 */
 	public static Object doLearnDecisionModel(Map<String, Vector<int[]>> dataSet) {
 
 		// FrequentItemSet
 		Vector<int[]> histoCollection = new Vector<int[]>();
 
 		double minSupport = 0.1;
 		List<Integer> minimalSupportedItems = new LinkedList<Integer>();
 
 		// Calculate Support of Visual words over all class
 		double[] support = new double[K];
 		int imageCounter = 0;
 		for (String className : dataSet.keySet()) {
 			histoCollection.addAll(dataSet.get(className));
 		}
 		for (int[] histo : histoCollection) {
 			for (int i = 0; i < K; i++) {
 				if (histo[i] > 0) {
 					support[i]++;
 				}
 			}
 			imageCounter++;
 		}
 
 		List<List<Integer>> frequentItemSets = new LinkedList<List<Integer>>();
 
 		// Normalize Support and find supported items
 		for (int i = 0; i < K; i++) {
 			support[i] /= imageCounter;
 			if (support[i] >= minSupport) {
 				minimalSupportedItems.add(i);
 				List<Integer> frequentItem = new LinkedList<Integer>();
 				frequentItem.add(i);
 				frequentItemSets.add(frequentItem);
 			}
 		}
 
 		int numWords = 10;
 
 		for (int numberOfWords = 2; numberOfWords < numWords; numberOfWords++) {
 			System.out.println("Finding frequent item sets with "
 					+ numberOfWords + " visual words");
 
 			// Enhance Sets
 			List<List<Integer>> newFrequentItemSets = new LinkedList<List<Integer>>();
 			for (List<Integer> frequentItemSet : frequentItemSets) {
 				for (Integer supportedWord : minimalSupportedItems) {
 					if (!frequentItemSet.contains(supportedWord)) {
 						List<Integer> newFrequentItemSet = copyList(frequentItemSet);
 						if (!contains(newFrequentItemSets, newFrequentItemSet)) {
 							// Calculate Support
 							double s = calculateSupport(newFrequentItemSet,
 									histoCollection);
 							if (s >= minSupport) {
 								newFrequentItemSets.add(newFrequentItemSet);
 							}
 						}
 					}
 				}
 			}
 
 			frequentItemSets.clear();
 			frequentItemSets.addAll(newFrequentItemSets);
 		}
 
 		// Ein Eintrag in frequentItemSets entspricht einem Frequent Item Set.
 		// Dieses besteht aus den
 		// Indizes des histogramm arrays welche zusammen ein item set ergeben.
 
 		// NN (ohne frequent itemset)
 		Set<String> className = dataSet.keySet();
 		Collection<Vector<int[]>> dataValues = dataSet.values();
 		int size = dataSet.get(className.toArray()[0]).get(0).length;
 
 		// create training set (logical XOR function)
 		TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(
 				size, 1);
 
 		int classNum = 0;
 		for (Vector<int[]> v : dataValues) {
 			for (int[] data : v) {
 				// convert to double[]
 				double[] dData = new double[data.length];
 				for (int i = 0; i < data.length; i++) {
 					dData[i] = data[i];
 				}
 				trainingSet.addElement(new SupervisedTrainingElement(dData,
 						new double[] { classNum }));
 			}
 
 			classNum++;
 		}
 
 		// create multi layer perceptron
 		MultiLayerPerceptron nnet = new MultiLayerPerceptron(
 				TransferFunctionType.TANH, size, 1);
 		nnet.learn(trainingSet);
 
 		return nnet;
 	}
 
 	private static double calculateSupport(List<Integer> fis,
 			Vector<int[]> dataSet) {
 		double support = 0;
 
 		double f = 1 / dataSet.size();
 
 		for (int word : fis) {
 			for (int[] histo : dataSet) {
 				if (histo[word] > 0) {
 					support += f;
 				}
 			}
 		}
 
 		return support;
 	}
 
 	private static List<Integer> copyList(List<Integer> l) {
 		List<Integer> newList = new LinkedList<Integer>();
 		newList.addAll(l);
 		return newList;
 	}
 
 	private static boolean contains(List<List<Integer>> collection,
 			List<Integer> query) {
 		for (List<Integer> l : collection) {
 			if (l.containsAll(query) && query.containsAll(l)) {
 				// The same list
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * IMPLEMENT THIS METHOD
 	 * 
 	 * 
 	 * Classifies a SIFT-Feature Vector into a VisualWord Class by finding the
 	 * nearest visual word in the bagofwords "space"
 	 * 
 	 * @param f
 	 *            a SIFT feature
 	 * @return the class ID (0..k) or null if quality is not good enough
 	 */
 	public Integer doClassifyVisualWord(Feature f) {
 		if (bagofwords == null || f == null)
 			return null;
 
 		/*
 		 * Find best cluster
 		 */
 		// Index of best cluster
 		Integer bestmatch = null;
 		// Best cluster
 		VisualWord bestWord = null;
 		// Distance to best cluster so far
 		float shortestDistance = Float.MAX_VALUE;
 		// Helper variable, to know how long it takes to find the best cluster
 		int clusterUpdate = 0;
 
 		for (int i = 0; i < bagofwords.size(); i++) {
 			VisualWord word = bagofwords.get(i);
 			float distance = word.centroied.descriptorDistance(f);
 
 			if (bestmatch == null || distance < shortestDistance) {
 				bestmatch = i;
 				shortestDistance = distance;
 				bestWord = word;
 
 				clusterUpdate++;
 			}
 		}
 
 		/*
 		 * Check distance quality (this has to be done for the best cluster
 		 * only, thus it is not within the loop)
 		 */
 		// If a cluster has been found
 		if (bestWord != null) {
 			System.out.println("Found best Cluster in " + clusterUpdate
 					+ " moves.");
 
 			// Variante 1: Q-75
 
 			// the @{link VisualWord.verficationValue} is the Q-75 distance
 			// within the
 			// cluster. If the distance of this feature is shorter than 75 % of
 			// all
 			// cluster members, it really is assigned to this cluster
 
 			// erase the best match, if it does not fit the quality criteria
 			if (shortestDistance >= (Float) bestWord.verificationValue) {
 				bestmatch = null;
 			}
 
 			// Variante 2: Inner cohesion
 
 			// the @{link VisualWord.verficationValue} is the current average
 			// distance
 			// from cluster members to its centroid. The quality criteria is
 			// that the distance
 			// can at most be alpha * VisualWord.verficationValue, thus not only
 			// slightly increasing
 			// the clusters cohesion
 
 			float alpha = 1.1f;
 			if (alpha * shortestDistance > (Float) bestWord.verificationValue) {
 				bestmatch = null;
 			}
 		}
 
 		return bestmatch;
 	}
 
 	/**
 	 * 
 	 * 
 	 * IMPLEMENT THIS METHOD
 	 * 
 	 * 
 	 * a k-mean clustering implementation for SIFT-Features: (float[] :
 	 * Feature.descriptor)
 	 * 
 	 * @param _points
 	 *            a list of all found features in the training set
 	 * @param K
 	 *            how many classes (visual words)
 	 * @param minCount
 	 *            the minimum number of members in each class
 	 * @return the centroides of the k-mean = visual words list
 	 */
 	public static List<VisualWord> doClusteringVisualWords(
 			final Feature[] _points, int K, int minCount) {
 		System.out.println("Start clustering with: " + _points.length
 				+ " pkt to " + K + " classes");
 
 		List<VisualWord> centeroids = new LinkedList<VisualWord>();
 
 		for (int i = 0; i < K; i++) {
 			// take a random feature as start point
 			Random rnd = new Random();
 			int pos = rnd.nextInt(_points.length);
 			VisualWord tmp = new VisualWord();
 			tmp.centroied = _points[pos];
 			centeroids.add(tmp);
 		}
 
 		//
 		int newRandPos = 1;
 		boolean testAfterCenter = false;
 		while (newRandPos > 0) {
 			for (int c = 0; c < K; c++)
 				centeroids.get(c).nearFeature = new LinkedList<Feature>();
 
 			// Allocate each point to the nearest cluster center
 			for (int i = 0; i < _points.length; i++) {
 				double distance = 0.0;
 				int centerNr = 0;
 
 				for (int j = 0; j < K; j++) {
 					double d = _points[i]
 							.descriptorDistance(centeroids.get(j).centroied);
 
 					if (j == 0 || d < distance) {
 						distance = d;
 						centerNr = j;
 					}
 				}
 				centeroids.get(centerNr).nearFeature.add(_points[i]);
 			}
 
 			//
 			int nrOfDescElement = centeroids.get(0).centroied.descriptor.length;
 			newRandPos = 0;
 			for (int c = 0; c < K; c++) {
 				int nrOfFeatures = centeroids.get(c).nearFeature.size();
 				if (nrOfFeatures < minCount) {
 					newRandPos++;
 					// set to a new random position
 					Random rnd = new Random();
 					int pos = rnd.nextInt(_points.length);
 					centeroids.get(c).centroied = _points[pos];
 				}
 			}
 
 			// System.out.println(newRandPos);
 
 			// move centeroid into the center
 			if (newRandPos == 0) {
 				float percent = 0.75f;
 
 				for (int c = 0; c < K; c++) {
 					int nrOfFeatures = centeroids.get(c).nearFeature.size();
 					float[] distanceLimit = new float[nrOfFeatures];
 
 					for (int i = 0; i < nrOfFeatures; i++) {
 						distanceLimit[i] = centeroids.get(c).nearFeature
 								.get(i)
 								.descriptorDistance(centeroids.get(c).centroied);
 					}
 					Arrays.sort(distanceLimit);
 					int pos = (int) Math.ceil(distanceLimit.length * percent);
 
 					for (int d = 0; d < nrOfDescElement; d++) {
 						double dx = 0.0;
 						for (int f = 0; f < pos; f++) {
 							dx += centeroids.get(c).nearFeature.get(f).descriptor[d];
 						}
 						centeroids.get(c).centroied.descriptor[d] = (float) (dx / pos);
						centeroids.get(c).verificationValue = distanceLimit[pos];					}
				}
 
 				if (!testAfterCenter) {
 					testAfterCenter = true;
 					newRandPos++;
 				}
 			}
 		}
 
 		return centeroids;
 	}
 
 	/* Do not change anything from here */
 
 	// initial sigma
 	private static float initial_sigma = 1.6f;
 	// feature descriptor size
 	private static int fdsize = 4;
 	// feature descriptor orientation bins
 	private static int fdbins = 8;
 	// size restrictions for scale octaves, use octaves < max_size and >
 	// min_size only
 	private static int min_size = 64;
 	private static int max_size = 1024;
 
 	public CbirWithSift() throws IOException {
 		super("Clustering");
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 600);
 
 		Thread t = new Thread(new Runnable() {
 			public void run() {
 				try {
 
 					setTitle("Learning: readData");
 					LinkedList<IgsImage> trainingImages = readImages(
 							"training", readImages);
 
 					setTitle("Learning: VisualWord by Clustering");
 
 					Vector<Feature> allLearnFeatchers = new Vector<Feature>();
 					for (IgsImage i : trainingImages)
 						allLearnFeatchers.addAll(i.features);
 
 					long startTimeVW = System.currentTimeMillis();
 					// calculate the visual words with k-means
 					bagofwords = doClusteringVisualWords(
 							(Feature[]) allLearnFeatchers
 									.toArray(new Feature[0]), K, MIN_CLASS_SIZE);
 					long endTimeVW = System.currentTimeMillis();
 
 					setTitle("Show: visualWords in TraningsData");
 					Map<String, Vector<int[]>> imageContentTrainingData = new HashMap<String, Vector<int[]>>();
 
 					// create the VisiualWordHistograms for each training image
 					for (IgsImage i : trainingImages) {
 						if (!imageContentTrainingData.containsKey(i.className))
 							imageContentTrainingData.put(i.className,
 									new Vector<int[]>());
 						int[] ImageVisualWordHistogram = new int[K];
 
 						for (Feature f : i.features) {
 							Integer wordClass = doClassifyVisualWord(f);
 							if (wordClass != null)
 								ImageVisualWordHistogram[wordClass.intValue()]++;
 						}
 
 						imageContentTrainingData.get(i.className).add(
 								ImageVisualWordHistogram);
 
 						cur_image = i;
 						repaint();
 						Thread.sleep(wait);
 					}
 
 					long startTimeDM = System.currentTimeMillis();
 					setTitle("Learning: decisionModel");
 					decisionModel = doLearnDecisionModel(imageContentTrainingData);
 					long endTimeDM = System.currentTimeMillis();
 
 					setTitle("Testing: readData");
 					LinkedList<IgsImage> testImages = readImages("test",
 							readImages);
 
 					long startTime = System.currentTimeMillis();
 
 					int success = 0;
 					setTitle("Verify: test data");
 
 					// create the VisiualWordHistograms for each test image and
 					// classify it
 					for (IgsImage i : testImages) {
 						int[] ImageVisualWordHistogram = new int[K];
 
 						for (Feature f : i.features) {
 							Integer wordClass = doClassifyVisualWord(f);
 							if (wordClass != null)
 								ImageVisualWordHistogram[wordClass.intValue()]++;
 						}
 
 						i.classifiedName = doClassifyImageContent(
 								ImageVisualWordHistogram).toString();
 
 						if (i.isClassificationCorect())
 							success++;
 
 						cur_image = i;
 						repaint();
 						Thread.sleep(wait);
 					}
 
 					long endTime = System.currentTimeMillis();
 
 					System.out.println("Verified "
 							+ (success / (double) testImages.size()) * 100
 							+ "% in " + (endTime - startTime) + "ms");
 					System.out.println("Learned " + K + " Visual Words in: "
 							+ (endTimeVW - startTimeVW) + "ms!");
 					System.out.println("Learned the image classification in: "
 							+ (endTimeDM - startTimeDM) + "ms");
 
 				} catch (Exception _e) {
 					_e.printStackTrace();
 				}
 			}
 		});
 		t.setDaemon(true);
 		t.start();
 	}
 
 	/**
 	 * Reads maxImages from a folder, calculates the SIFT features and wraps the
 	 * results into a IgsImage also paints each image on the GUI
 	 * 
 	 * @param folder
 	 * @param maxImages
 	 * @return the list of read IgsImages
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	LinkedList<IgsImage> readImages(String folder, int maxImages)
 			throws IOException, InterruptedException {
 		LinkedList<IgsImage> images = new LinkedList<IgsImage>();
 
 		File actual = new File("./images/" + folder);
 
 		int i = 0;
 
 		for (File f : actual.listFiles()) {
 			if (i++ > maxImages)
 				break;
 			IgsImage image = new IgsImage();
 			image.image = ImageIO.read(f);
 			image.className = f.getName()
 					.substring(0, f.getName().indexOf('_'));
 			image.features = calculateSift(image.image);
 
 			cur_image = image;
 			repaint();
 			Thread.sleep(wait);
 
 			images.add(image);
 		}
 
 		return images;
 	}
 
 	/**
 	 * draws a rotated square with center point center, having size and
 	 * orientation
 	 */
 	static void drawSquare(Graphics _g, double[] o, double scale,
 			double orient, Integer _class) {
 		scale /= 2;
 
 		double sin = Math.sin(orient);
 		double cos = Math.cos(orient);
 
 		int[] x = new int[6];
 		int[] y = new int[6];
 
 		x[0] = (int) (o[0] + (sin - cos) * scale);
 		y[0] = (int) (o[1] - (sin + cos) * scale);
 
 		x[1] = (int) o[0];
 		y[1] = (int) o[1];
 
 		x[2] = (int) (o[0] + (sin + cos) * scale);
 		y[2] = (int) (o[1] + (sin - cos) * scale);
 		x[3] = (int) (o[0] - (sin - cos) * scale);
 		y[3] = (int) (o[1] + (sin + cos) * scale);
 		x[4] = (int) (o[0] - (sin + cos) * scale);
 		y[4] = (int) (o[1] - (sin - cos) * scale);
 		x[5] = x[0];
 		y[5] = y[0];
 
 		// if(_class==null || _class.intValue()==92 || _class.intValue()==69 ||
 		// _class.intValue()==91) {
 
 		_g.setColor(Color.red);
 		_g.drawPolygon(new Polygon(x, y, x.length));
 		_g.setColor(Color.yellow);
 		if (_class != null)
 			_g.drawString(_class + "", x[0], y[0]);
 		// }
 
 	}
 
 	@Override
 	public synchronized void paint(Graphics _g) {
 
 		_g.clearRect(0, 0, 1000, 1000);
 
 		if (cur_image == null)
 			return;
 
 		_g.drawImage(cur_image.image, 0, 0, null);
 
 		_g.setColor(cur_image.isClassificationCorect() ? Color.green
 				: Color.red);
 
 		_g.drawString(cur_image.className + " > " + cur_image.classifiedName,
 				20, cur_image.image.getHeight() + 40);
 
 		if (cur_image.features != null)
 			for (Feature f : cur_image.features)
 				drawSquare(_g, new double[] { f.location[0], f.location[1] },
 						fdsize * 4.0 * (double) f.scale,
 						(double) f.orientation, doClassifyVisualWord(f));
 
 	}
 
 	public static FloatArray2D ImageToFloatArray2D(BufferedImage image)
 			throws IOException {
 		FloatArray2D image_float = null;
 
 		int count = 0;
 		image_float = new FloatArray2D(image.getWidth(), image.getHeight());
 
 		for (int y = 0; y < image.getHeight(); y++) {
 			for (int x = 0; x < image.getWidth(); x++) {
 				int rgbV = image.getRGB(x, y);
 				int b = rgbV & 0xff;
 				rgbV = rgbV >> 8;
 				int g = rgbV & 0xff;
 				rgbV = rgbV >> 8;
 				int r = rgbV & 0xff;
 				image_float.data[count++] = 0.3f * r + 0.6f * g + 0.1f * b;
 			}
 		}
 
 		return image_float;
 	}
 
 	public static void main(String[] _args) throws Exception {
 		new CbirWithSift();
 	}
 
 	private Vector<Feature> calculateSift(BufferedImage image)
 			throws IOException {
 
 		Vector<Feature> _features = new Vector<Feature>();
 
 		FloatArray2DSIFT sift = new FloatArray2DSIFT(fdsize, fdbins);
 
 		FloatArray2D fa = ImageToFloatArray2D(image);
 		Filter.enhance(fa, 1.0f);
 
 		fa = Filter.computeGaussianFastMirror(fa,
 				(float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
 
 		long start_time = System.currentTimeMillis();
 		System.out.print("processing SIFT ...");
 
 		sift.init(fa, steps, initial_sigma, min_size, max_size);
 		_features = sift.run(max_size);
 
 		System.out.println(" took " + (System.currentTimeMillis() - start_time)
 				+ "ms to find \t" + _features.size() + " features");
 
 		return _features;
 	}
 
 }
