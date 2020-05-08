 package org.lilian.data.real.fractal.random;
 
 import static org.lilian.util.Functions.log2;
 import static org.lilian.util.Functions.tic;
 import static org.lilian.util.Functions.toc;
 import static org.lilian.util.Series.series;
 import static org.lilian.data.real.fractal.random.DiscreteRIFS.Codon;
 
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.lilian.Global;
 import org.lilian.data.real.Datasets;
 import org.lilian.data.real.Density;
 import org.lilian.data.real.KernelDensity;
 import org.lilian.data.real.MVN;
 import org.lilian.data.real.Point;
 import org.lilian.data.real.Similitude;
 import org.lilian.data.real.fractal.IFS;
 import org.lilian.data.real.weighted.Weighted;
 import org.lilian.data.real.weighted.WeightedLists;
 import org.lilian.models.BasicFrequencyModel;
 import org.lilian.search.Builder;
 import org.lilian.search.Parameters;
 import org.lilian.util.Functions;
 import org.lilian.util.Series;
 import org.lilian.util.distance.Distance;
 import org.lilian.util.distance.EuclideanDistance;
 import org.lilian.util.distance.HausdorffDistance;
 import org.lilian.util.graphs.old.Stochastic;
 
 /**
  * 
  * TODO:
  * - Use data as basis.
  * @author Peter
  *
  */
 public class RIFSEM
 {
 	
 	private static double KERNEL_VAR = 0.5;
 	private static Distance<List<Point>> hausdorffDistance = 
 			new HausdorffDistance<Point>(new EuclideanDistance());
 	
 	private static boolean useSphericalMVN = false;
 	private static final int COVARIANCE_THRESHOLD = 5;
 	// * If a code has fewer than this number of points, it is not
 	//   used in reconstructing the choice tree
 	private static final int TREE_POINTS_THRESHOLD = 10; 
 	
 	private List<List<Point>> data;
 	private List<List<Point>> dataSample;
 	
 	// * The three main components of an EM iteration
 	private DiscreteRIFS<Similitude> model;
 	private List<SChoiceTree> trees;
 		
 	// * A code tree for each dataset. This list holds the root nodes.
 	private List<Node> codeTrees;
 	
 	private int compPerIFS, depth, sampleSize, numSources;
 	
 	private double spanningPointsVariance, perturbVar;
 	
 	private Builder<Similitude> builder;
 	private Builder<IFS<Similitude>> ifsBuilder;
 	
 	/**
 	 * 
 	 * @param initial Model consisting of IFSs with equal size.
 	 * @param data
 	 * @param depth
 	 * @param sampleSize
 	 * @param numSources How many points claim responsibility for a given code
 	 */
 	public RIFSEM(
 			DiscreteRIFS<Similitude> initial, 
 			List<List<Point>> data, int depth, int sampleSize,
 			double spanningPointsVariance, double perturbVar, int numSources)
 	{
 		
 		this.model = initial;
 		this.data = data;
 		this.depth = depth;
 		this.sampleSize = sampleSize;
 		this.spanningPointsVariance = spanningPointsVariance;
 		this.compPerIFS = model.models().get(0).size();
 		this.numSources = numSources;
 
 		// * Check that all component IFSs of model have the same size
 		for(IFS<Similitude> ifs : model.models())
 			if(ifs.size() != compPerIFS)
 				throw new IllegalArgumentException("All component IFSs of the initial model should have the same number of component transformations.");
 				
 		dataSample = new ArrayList<List<Point>>(data.size());
 		
 		codeTrees = new ArrayList<Node>();
 		for(int i : series(dataSample.size()))
 			codeTrees.add(new Node(null, null));
 		
 		resample();
 		
 		// * Add trees
 		trees = new ArrayList<SChoiceTree>(data.size());
 		for(int i : Series.series(dataSample.size()))
 			trees.add(new SChoiceTree(compPerIFS, model.size(), depth));
 		
 		findCodes();		
 		
 		builder = Similitude.similitudeBuilder(model.dimension());
 		ifsBuilder = IFS.builder(compPerIFS, builder);
 	}
 	
 //	public RIFSEM(DiscreteRIFS<Similitude> initial, List<List<Point>> data,
 //			List<ChoiceTree> dataTrees, int depth, int sampleSize, double spanningPointsVariance, double perturbVar)
 //	{
 //		this(initial, data, depth, sampleSize, spanningPointsVariance, perturbVar);
 //		this.trees = dataTrees;
 //	}
 
 	public void iteration()
 	{
 		resample();
 
 
 		tic();
 		findCodes();
 		Global.log().info("Finished finding codes. " + toc() + " seconds.");
 		
 		tic();
 		findTrees();
 		Global.log().info("Finished finding trees. " + toc() + " seconds.");
 		
 		for(SChoiceTree tree : trees)
 			System.out.println(tree);
 		
 		tic();
 		findModel();
 		Global.log().info("Finished finding model. " + toc() + " seconds.");
 
 	}
 	
 	public DiscreteRIFS<Similitude> model()
 	{
 		return model;
 	}
 	
 	/**
 	 * Finds a new Model given the current codes and sequences.
 	 */
 	public void findModel()
 	{	
 		// * Look for matching codes in the code tree
 		Maps maps = findMaps();
 
 		// * Frequency model for all component IFSs
 		BasicFrequencyModel<Integer> freqs = new BasicFrequencyModel<Integer>(); 
 		for(SChoiceTree tree : trees)
 			tree.count(freqs);
 		
 		// * Models and weights
 		List<IFS<Similitude>> modelComponents = new ArrayList<IFS<Similitude>>(model.size());
 		List<Double> modelWeights = new ArrayList<Double>(model.size());
 		
 		// * Initialize empty
 		for (int i : Series.series(model.size()))
 			modelComponents.add(null);
 		for (int i : Series.series(model.size()))
 			modelWeights.add(-1.0);
 
 		// * Keep check of components which were unassigned
 		List<Integer> modelAssigned = new ArrayList<Integer>(model.size());
 		List<Integer> modelUnassigned = new ArrayList<Integer>(model.size());
 
 
 		for(int h : series(model.size()))
 		{
 			IFS<Similitude> component = model.models().get(h);
 			int numComponents = component.size();
 			
 			// * Initialize empty trans and weights list
 			List<Similitude> trans = new ArrayList<Similitude>(numComponents);
 			for (int i : Series.series(numComponents))
 				trans.add(null);
 	
 			List<Double> weights = new ArrayList<Double>(numComponents);
 			for (int i : Series.series(numComponents))
 				weights.add(1.0 / numComponents);
 	
 			// * Keep check of components which were unassigned
 			List<Integer> assigned = new ArrayList<Integer>(numComponents);
 			List<Integer> unassigned = new ArrayList<Integer>(numComponents);
 	
 			for (int i : series(numComponents))
 			{
 				Codon c = new Codon(h, i);
 				int n = maps.size(c);
 	
 				if (n != 0) // codes found containing this comp
 				{
 					// * Find the map for the point pairs
 					Similitude map = findMap(maps.from(c), maps.to(c), component.get(i));
 					
 					
 					// * Find the weight for the frequency pairs
 					double weight = findScalar(maps.fromWeights(c),
 							maps.toWeights(c));
 	
 					trans.set(i, map);
 					weights.set(i, weight);
 	
 					assigned.add(i);
 				} else
 				{ // No codes found with this component
 					unassigned.add(i);
 				}
 			}
 	
 			if(! unassigned.isEmpty())
 				Global.log().info("unassigned: " + unassigned);
 			
 			if (assigned.isEmpty())
 				modelUnassigned.add(h);
			else
 				modelAssigned.add(h);
 	
 			// * For each unassigned IFS component, take a random assigned component and
 			//   perturb it slightly.
 			for (int i : unassigned)
 			{
 				int j = assigned.get(Global.random.nextInt(assigned.size()));
 				Similitude source = trans.get(j);
 				double sourceWeight = weights.get(j);
 	
 				Similitude perturbed0 = Parameters.perturb(source, builder, perturbVar);
 				Similitude perturbed1 = Parameters.perturb(source, builder, perturbVar);
 
 				trans.set(i, perturbed0);
 				trans.set(j, perturbed1);
 	
 				weights.set(i, sourceWeight / 2.0);
 				weights.set(j, sourceWeight / 2.0);
 			}
 										
 			component = new IFS<Similitude>(trans.get(0), weights.get(0));
 			for (int i : series(1, numComponents))
 				component.addMap(trans.get(i), weights.get(i));
 			
 			double ifsPrior = freqs.probability(h);
 			
 			modelComponents.set(h, component);
 			modelWeights.set(h, ifsPrior);
 		}
 		
 		// * For each unassigned model component, take a random assigned component and
 		//   perturb it slightly.
 		for (int h : modelUnassigned)
 		{
 			int j = modelAssigned.get(Global.random.nextInt(modelAssigned.size()));
 			IFS<Similitude> source = modelComponents.get(j);
 			double sourceWeight = modelWeights.get(j);
 
 			IFS<Similitude> perturbed0 = Parameters.perturb(source, ifsBuilder, perturbVar);
 			IFS<Similitude> perturbed1 = Parameters.perturb(source, ifsBuilder, perturbVar);
 
 			modelComponents.set(h, perturbed0);
 			modelComponents.set(j, perturbed1);
 
 			modelWeights.set(h, sourceWeight / 2.0);
 			modelWeights.set(j, sourceWeight / 2.0);
 		}		
 		
 		model = null;
 		for(int h : series(modelComponents.size()))
 			if(model == null)
 				model = new DiscreteRIFS<Similitude>(modelComponents.get(h), modelWeights.get(h));
 			else
 				model.addModel(modelComponents.get(h), modelWeights.get(h));
 	}
 	
 	/**
 	 * Resample the sampled datasets.
 	 * 
 	 * @param sampleSize The number of points to sample per dataset.
 	 */
 	public void resample()
 	{
 		dataSample.clear();
 		
 		for(List<Point> points : data)
 			dataSample.add(Datasets.sample(points, sampleSize));
 		
 	}
 	
 	/**
 	 * Finds a new coding given the current model and sequences.
 	 */
 	public void findCodes()
 	{
 		codeTrees.clear();
 		
 		for(int i : Series.series(dataSample.size()))
 		{
 			List<Point> points = dataSample.get(i);
 			SChoiceTree tree = trees.get(i);
 			
 			Node root = new Node(null, null);
 			codeTrees.add(root);
 
 			for(Point point : points)
 			{
 				SearchStochastic.SearchResult result =
 						SearchStochastic.search(model, tree, point, new MVN(data.get(0).get(0).dimensionality()), numSources);
 						
 				Weighted<List<Codon>> codes = result.codes();
 				for(int j : Series.series(codes.size()))
 					root.observe(codes.get(j), point, codes.weight(j));
 			}
 		}
 			
 		
 //		for(int j : series(20))
 //
 //			System.out.println(codeTree.random().code());
 		
 	}
 	
 	/**
 	 * Finds new ChoiceTrees given the current model and coding
 	 */
 	public void findTrees()
 	{
 		trees.clear();
 		
 		for(int i : series(data.size()))
 		{
 			// * We start with a random tree, and add the choices we can figure
 			//   out from the data.
 			SChoiceTree tree = new SChoiceTree(compPerIFS, model.size(), depth);
 			
 			codeTrees.get(i).build(tree);
 			
 			
 			trees.add(tree);
 			// System.out.println(tree);
 		}
 	}	
 	
 	/**
 	 * 
 	 * @return A list of maps so that Maps i in the list represents the 
 	 * maps for component IFS i.
 	 */
 	private Maps findMaps()
 	{
 		Maps maps = new Maps();
 
 		// * Collect all observed codes
 		Set<List<Codon>> allCodes = new LinkedHashSet<List<Codon>>();
 		for(Node root : codeTrees)
 			root.collectCodes(allCodes);
 		
 		// * For each observed code ...
 		for (List<Codon> toCode : allCodes)
 		{
 			if(toCode.size() < 1)
 				continue;
 			
 			// * find the pre-code (the code for the points that are mapped to this 
 			//   code by its first symbol.
 			List<Codon> fromCode = new ArrayList<Codon>(toCode);
 			Codon codon = fromCode.remove(0);
 
 			// * Collect the to and from set for each code
 			List<Point> to = new ArrayList<Point>(),
 			            from = new ArrayList<Point>();
 			
 			for(Node root : codeTrees)
 			{
 				to.addAll(root.points(toCode));
 				from.addAll(root.points(fromCode));
 			}
 			
 			
 			int m = Math.min(to.size(), from.size());
 			MVN toMVN = mvn(to), fromMVN = mvn(from);
 			
 			
 			if (fromMVN != null & toMVN != null)
 			{
 				if (m < COVARIANCE_THRESHOLD) // Not enough points to
 				                              // consider covariance
 				{
 					for (int i : series(from.size()))
 						maps.add(codon, fromMVN.mean(), toMVN.mean());
 				} else
 				{
 						// * Consider the covariance by taking not just the
 						//   means,
 						//   but points close to zero mapped to both
 						//   distributions
 
 						//   We generate as many points as are in the to node.
 						//   (for depth one a handful would suffice, but for
 						//   higher values the amount of points generated gives
 						//   a sort of weight to this match in the codes among 
 						//   the other points)
 						List<Point> points = 
 								new MVN(model.dimension(), spanningPointsVariance)
 								.generate(from.size());
 
 						List<Point> pf = fromMVN.map().map(points);
 						List<Point> pt = toMVN.map().map(points);
 
 						for (int i = 0; i < points.size(); i++)
 							maps.add(codon, pf.get(i), pt.get(i));
 				}
 			} else
 			{
 					// Global.log().info("Points for code " + code +
 					// " formed deficient MVN. No points added to pairs.");
 			}
 
 			// * Register the drop in frequency as the symbol t gets added
 			//   to the code
 			maps.weight(codon, from.size(), to.size());
 		}
 		
 		
 		Global.log().info("Maps found.");
 		return maps;
 	}
 
 	private MVN mvn(List<Point> points)
 	{
 		try
 		{
 			return useSphericalMVN ? MVN.findSpherical(points) : 
 				MVN.find(points);
 		} catch (RuntimeException e)
 		{
 			// * Could not find proper MVN model
 			return null;
 		}
 	}
 	
 	/**
 	 * A node in the code tree. Each code represents a path in this tree from
 	 * root to leaf. At each node, we store each point whose path visits that
 	 * node. (ie. the root node contains all points, and each node below the
 	 * root contains all points whose code starts with a given symbol).
 	 * 
 	 * The Node object also contains the search algorithm for matching codes.
 	 */
 	protected class Node implements Serializable
 	{
 		private static final long serialVersionUID = -6512700670917962320L;
 		
 		// * An MVN fitted to the point stored in this node
 		MVN mvn = null;
 
 		// * The parent in the tree
 		Node parent;
 		// * The child nodes for each symbol (represented by an Integer)
 		Map<Codon, Node> children;
 		// * How deep this node is in the tree
 		int depth = 0;
 
 		// * This node's code
 		List<Codon> code;
 
 		// * Whether this node represents a leaf node
 		boolean isLeaf = false;
 
 		// * The points stored at this node
 		Weighted<Point> points = WeightedLists.empty();
 
 		/**
 		 * Create a child node for the given symbol under this parent
 		 * 
 		 * @param symbol
 		 * @param parent
 		 */
 		public Node(Codon symbol, Node parent)
 		{
 			this.parent = parent;
 			code = new ArrayList<Codon>(
 					parent != null ? parent.code().size() + 1 : 1);
 
 			if (parent != null)
 				code.addAll(parent.code());
 			
 			if (symbol != null)
 				code.add(symbol);
 
 			if (parent != null)
 				depth = parent.depth + 1;
 			
 			children = new HashMap<Codon, Node>();
 		}
 
 		/**
 		 * Add the symbols of this node and the subtree below it to the given
 		 * frequency model.
 		 * 
 		 * @param model
 		 */
 		public void count(BasicFrequencyModel<Codon> model)
 		{
 			if (!isRoot())
 				model.add(symbol());
 
 			for (Codon i : children.keySet())
 				children.get(i).count(model);
 		}
 		
 		/**
 		 * Add the symbols of this node and the subtree below it to the given
 		 * frequency model.
 		 * 
 		 * @param model
 		 */
 		public void countIFS(BasicFrequencyModel<Integer> model)
 		{
 			if (!isRoot())
 				model.add(symbol().ifs());
 
 			for (Codon i : children.keySet())
 				children.get(i).countIFS(model);
 		}
 
 		/**
 		 * Returns whether this node is the root node of the tree.
 		 * 
 		 * @return
 		 */
 		public boolean isRoot()
 		{
 			return parent == null;
 		}
 
 		/**
 		 * How far from the root this tree is.
 		 * 
 		 * @return
 		 */
 		public int depth()
 		{
 			return depth;
 		}
 
 		/**
 		 * The code represented by this node
 		 */
 		public List<Codon> code()
 		{
 			return code;
 		}
 		
 
 		/**
 		 * The symbol for this node (ie. the last symbol in its code).
 		 * 
 		 * @return
 		 */
 		public Codon symbol()
 		{
 			return code.get(code.size() - 1);
 		}
 
 		/**
 		 * Store the given point at this node, and pass it on to the correct
 		 * child.
 		 * 
 		 * @param codeSuffix
 		 *            The suffix of the code after this node
 		 * @param point
 		 *            The point to be observed.
 		 */
 		public void observe(List<Codon> codeSuffix, Point point)
 		{
 			observe(codeSuffix, point, 1.0);
 		}
 		
 		public void observe(List<Codon> codeSuffix, Point point, double weight)
 		{
 //			if(parent == null)
 //				System.out.println(codeSuffix);
 //			
 			points.add(point, weight);
 			mvn = null; // signal that the mvn needs to be recomputed
 
 			if (codeSuffix.size() == 0)
 			{
 				isLeaf = true;
 				return;
 			}
 
 			Codon symbol = codeSuffix.get(0);
 			if (!children.containsKey(symbol))
 				children.put(symbol, new Node(symbol, this));
 
 			children.get(symbol).observe(
 					codeSuffix.subList(1, codeSuffix.size()), point);
 		}
 
 		/**
 		 * The points stored at this node
 		 * 
 		 * @return
 		 */
 		public List<Point> points()
 		{
 			return points;
 		}
 		
 		/**
 		 * Returns the points associated with the node at the given code-suffix 
 		 * (ie. the path from this node).
 		 * 
 		 * @param code
 		 * @return
 		 */
 		public List<Point> points(List<Codon> code)
 		{
 			if (code.isEmpty())
 				return points();
 			
 			Codon head = code.get(0);
 			if(! children.containsKey(head))
 				return Collections.emptyList();
 			
 			return children.get(head).points(code.subList(1, code.size()));
 		}
 
 		/**
 		 * Add all codes at and below this node to the given collection
 		 * @param codes
 		 */
 		public void collectCodes(Collection<List<Codon>> codes)
 		{
 			codes.add(code());
 			
 			for(Node child : children.values())
 				child.collectCodes(codes);
 		}
 		
 		/**
 		 * The number of times this node was visited (ie. the number of points
 		 * stored here)
 		 */
 		public double frequency()
 		{
 			return points.size();
 		}
 
 		/**
 		 * @return Whether this node is a leaf node in the tree. A node is a
 		 *         leaf if it is at the set maximum depth for this iteration of
 		 *         the EM algorithm. It may be that is has no children yet but
 		 *         they will be created by the observation of a future code.
 		 */
 		public boolean isLeaf()
 		{
 			return isLeaf;
 		}
 
 		/**
 		 * A multivariate normal distribution fitted to the points stored at
 		 * this node
 		 * 
 		 * Note: returns null if the points for this code form a deficient mvn
 		 * model.
 		 * 
 		 * @return
 		 */
 		public MVN mvn()
 		{
 			if (mvn == null)
 				try
 				{
 					mvn = useSphericalMVN ? MVN.findSpherical(points) : 
 						MVN.find(points);
 				} catch (RuntimeException e)
 				{
 					// * Could not find proper MVN model
 					return null;
 				}
 
 			return mvn;
 		}
 
 		/**
 		 * Print a lengthy (multiline) representation of this node to the given
 		 * outputstream
 		 * 
 		 * @param out
 		 * @param indent
 		 *            The number of tabs to indent with
 		 */
 		public void print(PrintStream out, int indent)
 		{
 			String ind = "";
 			for (int i : series(indent))
 				ind += "\t";
 
 			String code = "";
 			for (Codon codon : code())
 				code += codon + " ";
 
 			out.println(ind + code + " f:" + frequency() + ", p: ...");
 			for (Codon symbol : children.keySet())
 				children.get(symbol).print(out, indent + 1);
 		}
 
 		/**
 		 * Returns the node for the given code (suffix) (starting from this
 		 * node).
 		 * 
 		 * @param code
 		 *            The code suffix for which to find the node starting from
 		 *            the current node.
 		 * @return the requested Node if it exists. null otherwise.
 		 */
 		public Node find(List<Codon> code)
 		{
 			if (code.size() == 0)
 				return this;
 
 			Codon symbol = code.get(0);
 			if (! children.containsKey(symbol))
 				return null;
 
 			return children.get(symbol).find(code.subList(1, code.size()));
 		}
 		
 		public void build(SChoiceTree tree)
 		{
 			if(children == null || children.size() == 0)
 				return;
 			if(points().size() < TREE_POINTS_THRESHOLD)
 				return;
 				
 			
 			// * Find the best IFS for this node
 			
 			// * We calculate the score of an IFS by fitting an MVN to the points 
 			//   of this node, mapping it by each component transformation, and 
 			//   taking the log probability of the points of the matching node.
 			int best = -1;
 			double bestScore = Double.NEGATIVE_INFINITY;
 			
 			List<Double> probs = new ArrayList<Double>(model.size());
 			for(int i : series(model.size()))
 				probs.add(Double.NaN);
 			
 			for(int i : series(model.size()))
 			{
 				IFS<Similitude> component = model.models().get(i);
 				double logIFSPrior = log2(model.probability(i));
 			
 				double score = logIFSPrior;
 				// MVN mvn = mvn();
 				List<Point> from = points();
 				
 				// System.out.println(code() + " " + points() + " " + mvn);
 				
 				for(int j : series(component.size()))
 				{
 					// System.out.println(j);
 					// MVN mapped = mvn.transform(component.get(j));
 					
 					List<Point> mapped = component.get(j).map(from);
 					Density density = new KernelDensity(mapped, KERNEL_VAR);
 					
 					// * Retrieve all points from children with map component j
 					List<Point> to = new ArrayList<Point>();
 					for(int k : series(model.size()))
 					{
 						Codon codon = new Codon(k, j);
 						if(children.containsKey(codon))
 							to.addAll(children.get(codon).points());
 					}
 					
 					if(to.isEmpty())
 						continue;
 					
 					// score += hausdorffDistance.distance(mapped, to);
 					for(Point point : to)
 						score += log2(density.density(point));
 				}
 				
 				probs.set(i, score);
 				
 				if(score >= bestScore)
 				{
 					bestScore = score;
 					best = i;
 				}
 			}
 			
 		//	System.out.println(depth() + " " + best + " " + bestScore + " " + probs);
 			
 			// * Submit to the choice tree
 			if(best != -1)
 			{
 				SChoiceTree.Node node = tree.get(mapCode(code()));
 				node.clear();
 				
 				for(int i : Series.series(probs.size()))
 				{
 					if(! Double.isNaN(probs.get(i)))
 					node.set(i, probs.get(i));
 				}
 			}
 			
 			// * Recurse
 			for(Node child : children.values())
 				child.build(tree);
 		}
 
 		public String toString()
 		{
 			String code = depth() + ") ";
 			for (Codon c : code())
 				code += c;
 			return code;
 		}
 
 		public Node random()
 		{
 			if(children.size() == 0)
 				return this;
 			
 			List<Codon> keys = new ArrayList<Codon>(children.keySet());
 			Codon k = keys.get(Global.random.nextInt(keys.size()));
 			
 			return children.get(k).random();
 		}
 		
 	}
 
 	
 	/**
 	 * Helper class for storing paired points and paired frequencies for each
 	 * component map of a given IFS
 	 */
 	protected class Maps
 	{
 		// * The inner list stores all 'from' points. We store one such list for
 		// each component
 		private Map<Codon, List<Point>> from = new HashMap<Codon, List<Point>>();
 		// * The inner list stores all 'to' points. We store one such list for
 		// each component
 		private Map<Codon, List<Point>> to   = new HashMap<Codon, List<Point>>();
 
 		// * The same but for the weights (inner lists store frequencies)
 		private Map<Codon, List<Double>> fromWeights = new HashMap<Codon, List<Double>>();
 		private Map<Codon, List<Double>> toWeights   = new HashMap<Codon, List<Double>>();
 
 		/**
 		 * The number of point pairs stored for a given component
 		 * 
 		 * @param i
 		 *            The component index
 		 * @return The number of point pairs stored for component i
 		 */
 		public int size(Codon c)
 		{
 			ensure(c);
 			return from.get(c).size();
 		}
 
 		/**
 		 * Add a point pair to a given component
 		 * 
 		 * @param component
 		 *            The component that maps from the first to the second point
 		 * @param from
 		 *            The from point
 		 * @param to
 		 *            The to point
 		 */
 		public void add(Codon component, Point from, Point to)
 		{
 			ensure(component);
 			this.from.get(component).add(from);
 			this.to.get(component).add(to);
 		}
 
 		/**
 		 * Add a frequency pair to a given component
 		 * 
 		 * @param component
 		 *            The component that maps from the first to the second
 		 *            frequency
 		 * @param from
 		 *            The from frequency
 		 * @param to
 		 *            The to frequency
 		 */
 		public void weight(Codon component, double from, double to)
 		{
 			ensure(component);
 			this.fromWeights.get(component).add(from);
 			this.toWeights.get(component).add(to);
 		}
 
 		/**
 		 * Ensure that lists exist for the given component (and below)
 		 */
 		private void ensure(Codon codon)
 		{
 			if(! from.containsKey(codon))
 				from.put(codon, new ArrayList<Point>());
 			if(! to.containsKey(codon))
 				to.put(codon, new ArrayList<Point>());	
 			
 			if(! fromWeights.containsKey(codon))
 				fromWeights.put(codon, new ArrayList<Double>());
 			if(! toWeights.containsKey(codon))
 				toWeights.put(codon, new ArrayList<Double>());
 		}
 
 		/**
 		 * Returns a list of 'from' points which the given component should map
 		 * into the points returned by {@link to()}
 		 * 
 		 * @param component
 		 * @return
 		 */
 		public List<Point> from(Codon codon)
 		{
 			if(from.containsKey(codon))
 				return from.get(codon);
 
 			return Collections.emptyList();
 		}
 
 		/**
 		 * Returns a list of 'to' points which should be mapped by the given
 		 * component into the points returned by {@link from()}
 		 * 
 		 * @param component
 		 * @return
 		 */
 		public List<Point> to(Codon codon)
 		{
 			if (to.containsKey(codon))
 				return to.get(codon);
 
 			return Collections.emptyList();
 		}
 
 		/**
 		 * Returns a list of 'from' frequencies which the given component weight
 		 * should 'scale' into the points returned by {@link to()}
 		 * 
 		 * @param component
 		 * @return
 		 */
 		public List<Double> fromWeights(Codon codon)
 		{
 			if (fromWeights.containsKey(codon))
 				return fromWeights.get(codon);
 
 			return Collections.emptyList();
 		}
 
 		/**
 		 * Returns a list of 'to' points which should be 'scales' by the weight
 		 * of the given component into the points returned by {@link from()}
 		 * 
 		 * @param component
 		 * @return
 		 */
 		public List<Double> toWeights(Codon codon)
 		{
 			if (toWeights.containsKey(codon))
 				return toWeights.get(codon);
 
 			return Collections.emptyList();
 		}
 
 		@Override
 		public String toString()
 		{
 			String out = "";
 
 			for (Codon codon : from.keySet())
 			{
 				out += codon + ":" + from(codon).size() + "_" + to(codon).size() + " ";
 			}
 
 			return out;
 		}
 	}
 	
 	protected Similitude findMap(List<Point> from, List<Point> to, Similitude old)
 	{
 		return org.lilian.data.real.Maps.findMap(from, to);
 	}
 	
 	public static double findScalar(List<Double> x, List<Double> y)
 	{
 		double sumXX = 0.0;
 		double sumYX = 0.0;
 
 		for (int i = 0; i < x.size(); i++)
 		{
 			sumXX += x.get(i) * x.get(i);
 			sumYX += y.get(i) * x.get(i);
 		}
 
 		return sumYX / sumXX;
 	}
 	
 	public static List<Integer> ifsCode(List<Codon> code)
 	{
 		return new Wrapper(code, true);
 	}
 	
 	public static List<Integer> mapCode(List<Codon> code)
 	{
 		return new Wrapper(code, false);		
 	}
 	
 	private static class Wrapper extends AbstractList<Integer>
 	{
 		List<Codon> master;
 		boolean ifs;
 
 		public Wrapper(List<Codon> master, boolean ifs)
 		{
 			this.master = master;
 			this.ifs = ifs;
 		}
 
 		@Override
 		public Integer get(int index)
 		{
 			return ifs ? master.get(index).ifs() : master.get(index).map();
 		}
 
 		@Override
 		public int size()
 		{
 			return master.size();
 		}	
 	}
 }
