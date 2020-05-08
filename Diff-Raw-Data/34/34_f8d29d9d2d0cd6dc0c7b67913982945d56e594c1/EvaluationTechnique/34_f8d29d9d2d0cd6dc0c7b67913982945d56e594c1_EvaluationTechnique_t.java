 package techniques;
 
 import helpers.DataLoader;
 import helpers.DataMapper;
 import helpers.ChainParams;
 import helpers.PerfResults;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.encog.ensemble.Ensemble;
 import org.encog.ensemble.Ensemble.TrainingAborted;
 import org.encog.ensemble.EnsembleAggregator;
 import org.encog.ensemble.EnsembleMLMethodFactory;
 import org.encog.ensemble.EnsembleTrainFactory;
 import org.encog.ensemble.aggregator.WeightedAveraging.WeightMismatchException;
 import org.encog.ensemble.data.EnsembleDataSet;
 import org.encog.ml.data.MLData;
 import org.encog.ml.data.MLDataPair;
 import org.encog.ml.data.MLDataSet;
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 
 import techniques.AdaBoostET.RequiresWeightedAggregatorException;
 
 public abstract class EvaluationTechnique {
 
 	protected EnsembleDataSet trainingSet;
 	protected EnsembleDataSet selectionSet;
 	protected EnsembleMLMethodFactory mlMethod;
 	protected EnsembleTrainFactory trainFactory;
 	protected EnsembleAggregator aggregator;
 	protected Ensemble ensemble;
 	protected ChainParams label;
 	protected List<Integer> sizes;
 	protected int currentSizeIndex = 0;
 	protected double trainToError;
 	protected double selectionError;
 	protected boolean hasStepsLeft = true;
 	protected int maxIterations = 2000;
 	
 	public int getMisclassificationCount(BasicNeuralDataSet evalSet, DataMapper dataMapper) throws WeightMismatchException {
 		int bad = 0;
 		for(int i = 0; i < evalSet.getRecordCount(); i++)
 		{
 			MLDataPair pair = evalSet.get(i);
 			MLData output = compute(pair.getInput());
 			ArrayList<String> result = dataMapper.unmap(output);
 			ArrayList<String> expected = dataMapper.unmap(pair.getIdeal());
 			if (!dataMapper.compare(result,expected,false))
 				bad++;
 		}
 		return bad;
 	}
 	
	public double getMisclassification(BasicNeuralDataSet evalSet, DataMapper dataMapper) throws WeightMismatchException {
		return (double) getMisclassificationCount(evalSet, dataMapper) / (double) evalSet.size();
	}
	
 	public void setParams(double trainToError, double selectionError) {
 		this.trainToError = trainToError;
 		this.selectionError = selectionError;
 	}
 	
 	public double getSelectionError() {
 		return selectionError;
 	}
 
 	public void train(boolean verbose) {
 		try {
 			ensemble.train(trainToError, selectionError, maxIterations, (EnsembleDataSet) selectionSet,verbose);
 		} catch (TrainingAborted e) {
 			System.out.println("Reached training iterations limit on E_t = " + trainToError);
 		}
 	}
 
 	public void trainStep() {		
 	}
 	
 	public abstract void step(boolean verbose) throws TrainingAborted;
 	
 	public double trainError() {
 		return ensemble.getMember(0).getError(trainingSet);
 	}
 	
 	public double testError() {
 		return ensemble.getMember(0).getError(selectionSet);
 	}	
 	
 	public abstract void init(DataLoader dataLoader, int fold) throws RequiresWeightedAggregatorException;
 	
 	public void init(DataLoader dataLoader, int fold, int maxIterations) throws RequiresWeightedAggregatorException
 	{
 		this.maxIterations = maxIterations;
 		this.init(dataLoader,fold);
 	}
 	public String getLabel() {
 		return label.get(sizes.get(currentSizeIndex));
 	}
 	
 	public MLDataSet getTrainingSet() {
 		return trainingSet;
 	}
 
 	public MLDataSet getTestSet() {
 		return selectionSet;
 	}
 
 	public void setTrainingSet(MLDataSet trainingSet) {
 		this.trainingSet = new EnsembleDataSet(trainingSet);
 	}
 
 	public void setSelectionSet(MLDataSet testSet) {
 		this.selectionSet = new EnsembleDataSet(testSet);
 	}
 	
 	public MLData compute(MLData input) throws WeightMismatchException {
 		return ensemble.compute(input);
 	}
 	
 	public PerfResults testPerformance(BasicNeuralDataSet evalSet, DataMapper dataMapper, boolean debug) throws WeightMismatchException {
 		int outputs = evalSet.getIdealSize();
 		long size = evalSet.getRecordCount();
 		int tp[] = new int[outputs];
 		int tn[] = new int[outputs];
 		int fp[] = new int[outputs];
 		int fn[] = new int[outputs];
 		for(int i = 0; i < size; i++)
 		{
 			MLDataPair pair = evalSet.get(i);
 			MLData output = compute(pair.getInput());
 			if (debug) {
 				System.out.println("Computed class " + dataMapper.unmap(output).toString() + 
 						" for " + dataMapper.unmap(pair.getIdeal()).toString());
 			}
 			for(int thisClass = 0; thisClass < outputs; thisClass++) {
 				if (output.getData(thisClass) > 0.5) {
 					if (pair.getIdeal().getData(thisClass) > 0.5) {
 						tp[thisClass]++;
 					} else {
 						fp[thisClass]++;
 					}
 				} else {
 					if (pair.getIdeal().getData(thisClass) < 0.5) {
 						tn[thisClass]++;
 					} else {
 						fn[thisClass]++;						
 					}
 				}
 			}
 		}
 		return new PerfResults(tp,fp,tn,fn,outputs);
 	}
 
 	public boolean hasStepsLeft() {
 		return hasStepsLeft;
 	}
 
 	public int getCurrentSize() {
 		return sizes.get(currentSizeIndex);
 	}	
 }
