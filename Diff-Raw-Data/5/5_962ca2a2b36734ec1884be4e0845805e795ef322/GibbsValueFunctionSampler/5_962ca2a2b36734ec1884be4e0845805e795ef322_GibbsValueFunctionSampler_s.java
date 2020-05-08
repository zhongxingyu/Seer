 package fi.smaa.libror;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.math.linear.RealVector;
 
 import fi.smaa.libror.RORModel.PrefPair;
 
 public class GibbsValueFunctionSampler extends ValueFunctionSampler {
 	
 	private static final int MAX_STARTINGPOINT_ITERS = 10000;
 	private WeightedOrdinalValueFunction[] vfs;
 	private int thinning;
 	private WeightedOrdinalValueFunction startingPoint;
 	
 	public GibbsValueFunctionSampler(RORModel model, int count, int thinning, 
 			WeightedOrdinalValueFunction startingPoint) throws InvalidStartingPointException {
 		super(model);
 		checkStartingPoint(startingPoint);
 		init(count, thinning, startingPoint);
 	}
 	
 	public GibbsValueFunctionSampler(RORModel model, int count, int thinning) throws InvalidStartingPointException {
 		super(model);
 		startingPoint = generateStartingPoint();
 		init(count, thinning, startingPoint);
 	}	
 
 	private void checkStartingPoint(WeightedOrdinalValueFunction p) throws InvalidStartingPointException {
 		if (!p.areValidWeights()) {
 			throw new InvalidStartingPointException("Weights not summing to 1.0");
 		}
 		List<OrdinalPartialValueFunction> pvfs = p.getPartialValueFunctions();
 		RealVector[] lvls = model.getPerfMatrix().getLevels();
 		if (lvls.length != pvfs.size()) {
 			throw new InvalidStartingPointException("Incorrect amount of partial value functions");
 		}
 		for (int i=0;i<lvls.length;i++) {
 			if (pvfs.get(i).getValues().length != lvls[i].getDimension()) {
 				throw new InvalidStartingPointException("Incorrect amount of levels in partial value function #" + (i+1));
 			}
 		}
 	}
 
 	private void init(int count, int thinning, WeightedOrdinalValueFunction startingPoint) {
 		if (count < 1) {
 			throw new IllegalArgumentException("PRECOND violated: count < 1");
 		}
 		if (thinning < 1) {
 			throw new IllegalArgumentException("PRECOND violated: thinning < 1");
 		}
 
 		this.startingPoint = startingPoint;
 		vfs = new WeightedOrdinalValueFunction[count];
 		this.thinning = thinning;
 	}
 	
 	public WeightedOrdinalValueFunction[] getValueFunctions() {
 		return vfs;
 	}
 	
 	public int getThinning() {
 		return thinning;
 	}
 	
 	public int getNrValueFunctions() {
 		return vfs.length;
 	}
 
 	@Override
 	public void doSample() {
 		WeightedOrdinalValueFunction currentVF = startingPoint.deepCopy();
 			
 		int nrPartVF = currentVF.getPartialValueFunctions().size();
 		int[] sizPartVF = getPartialVFSizes(currentVF.getPartialValueFunctions());
 		
 		int curVFind = 0;
 		int curPartVFind = 1; // curPartVF == (amount of part VFs) means to sample the weight
 		// and 0 is always 0.0 value so we don't sample it 
 		
 		int iter = 0;
 		
 		boolean store = true;
 		int index = 1;
 				
 		vfs[0] = currentVF.deepCopy();
 		
 		while (index < vfs.length) {
 			store = false;
 			if (curPartVFind == sizPartVF[curVFind]-1) { // update weight
 				// if it's the last one, don't do anything (it's dependent on others as \sum_i w_i=1
 				if (curVFind != (nrPartVF-1)) {
 					assert(curVFind < (nrPartVF-1));
 					double[] weights = currentVF.getWeights();
 					double newW = sampleWeight(curVFind, weights);
 					double[] oldWeights = Arrays.copyOf(weights, weights.length);
 					currentVF.setWeight(curVFind, newW);
 					setLastWeight(currentVF); // set last weight so that they sum to 1.0
 					if (failsRejectCriterion(currentVF)) {
 						currentVF.setWeights(oldWeights);
 					}
 					store = true;
 				}
 			} else { // update characteristic point
 				OrdinalPartialValueFunction vf = currentVF.getPartialValueFunctions().get(curVFind);
 				double point = samplePoint(curPartVFind, vf);
 				vf.setValue(curPartVFind, point);
 				store = true;
 			}
 			
 			// UPDATE INDICES
 			if (curPartVFind < sizPartVF[curVFind]-1) {
 				curPartVFind++;
 			}
 			else { // advance to next partial vf
 				curPartVFind = 1;
 				curVFind = (curVFind + 1) % nrPartVF;
 			}
 			if (store) {
 				iter++;
 				if (iter % thinning == 0) {
 					vfs[index] = currentVF.deepCopy();
 					index++;
 				}
 			}
 		}
 		
 		
 	}
 
 	private boolean failsRejectCriterion(WeightedOrdinalValueFunction vf) {
 		PerformanceMatrix pm = model.getPerfMatrix();		
 		for (PrefPair pref : model.getPrefPairs()) {
 			int[] alevels = pm.getLevelIndices(pref.a);
 			int[] blevels = pm.getLevelIndices(pref.b);
 			if (vf.evaluate(alevels) < vf.evaluate(blevels)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private double samplePoint(int ind, OrdinalPartialValueFunction vf) {
 		double[] vals = vf.getValues();
 		double lb = vals[ind-1];
 		double ub = vals[ind+1];
 		
 		return lb + (rng.nextDouble() * (ub - lb));
 	}
 
 	private void setLastWeight(WeightedOrdinalValueFunction vf) {
 		double[] w = vf.getWeights();
 		double sum = 0.0;
 		for (int i=0;i<w.length-1;i++) {
 			sum += w[i];
 		}
 		vf.setWeight(w.length-1, 1.0 - sum);
 	}
 
 	private double sampleWeight(int curPartVFind, double[] w) {
 		double sum = 0.0;
 		for (int i=0;i<w.length-1;i++) {
 			if (i != curPartVFind) {
 				sum += w[i];
 			}
 		}		
 		return rng.nextDouble() * (1.0 - sum);
 	}
 
 	private int[] getPartialVFSizes(List<OrdinalPartialValueFunction> levels) {
 		int[] sizes = new int[levels.size()];
 		for (int i=0;i<levels.size();i++) {
 			sizes[i] = levels.get(i).getValues().length;
 		}
 		return sizes;
 	}
 
 	public WeightedOrdinalValueFunction generateStartingPoint() throws InvalidStartingPointException {
 		RejectionValueFunctionSampler rejs = new RejectionValueFunctionSampler(model, 1, MAX_STARTINGPOINT_ITERS);
 		try {
 			rejs.sample();
 		} catch (SamplingException e) {
 			throw new InvalidStartingPointException("Cannot find starting point: infeasible preferences");
 		}
 		
 		FullCardinalValueFunction point = rejs.getValueFunctions()[0];
 		return convertCardinalVFToOrdinal(point);
 	}
 
 	private WeightedOrdinalValueFunction convertCardinalVFToOrdinal(FullCardinalValueFunction point) {
 		WeightedOrdinalValueFunction vf = new WeightedOrdinalValueFunction();
 		double[] w = point.getWeights();
 		
 		int index = 0;
 		for (CardinalPartialValueFunction f : point.getPartialValueFunctions()) {
 			double[] lvls = Arrays.copyOf(f.getEvals(), f.getEvals().length);
 			OrdinalPartialValueFunction of = new OrdinalPartialValueFunction(lvls.length);
 			for (int i=1;i<lvls.length-1;i++) {
 				of.setValue(i, lvls[i] / w[index]);
 			}
 			index++;
 			vf.addValueFunction(of);
 		}
 		vf.setWeights(w);
 		
 		return vf;
 	}
 
 	public WeightedOrdinalValueFunction getStartingPoint() {
 		return startingPoint;
 	}
 
 }
