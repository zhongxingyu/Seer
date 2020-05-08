 package helpers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Connection;
 import org.encog.ensemble.Ensemble.TrainingAborted;
 import org.encog.ensemble.aggregator.WeightedAveraging.WeightMismatchException;
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 
 import techniques.AdaBoostET.RequiresWeightedAggregatorException;
 import techniques.EvaluationTechnique;
 
 public class Evaluator
 {
 
 	private EvaluationTechnique technique;
 	private DataLoader dataLoader;
 	
 	Evaluator(EvaluationTechnique technique, DataMapper mapper, int inputCols, int inputs, String dataFile, boolean inputsReversed, int nFolds, double targetTrainingError, double selectionError, int fold, int maxIterations, boolean hasSeparateTestSet) throws FileNotFoundException, RequiresWeightedAggregatorException
 	{
 		this.setTechnique(technique);
 		dataLoader = new DataLoader(mapper,inputCols,inputs,inputsReversed,nFolds,hasSeparateTestSet);
 		dataLoader.readData(dataFile);
 		this.technique.init(dataLoader,fold,maxIterations);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(false);
 	}
 	
 	public Evaluator(EvaluationTechnique technique, DataLoader dataLoader, double targetTrainingError, double selectionError, boolean verbose, int fold, int maxIterations) throws RequiresWeightedAggregatorException
 	{
 		this.setTechnique(technique);
 		this.dataLoader = dataLoader;
 		this.technique.init(dataLoader,fold,maxIterations);
 		this.technique.setParams(targetTrainingError, selectionError);
 		this.technique.train(verbose);
 	}
 	
 	public void makeLine(boolean isTest, double trainingError, ChainParams chainPars, BasicNeuralDataSet dataSet, Statement sqlStatement, long chainId) throws SQLException, WeightMismatchException
 	{
 		DataMapper dataMapper = dataLoader.getMapper();
 		PerfResults perf = this.technique.testPerformance(dataSet, dataMapper,false);
 		long runId = 0;
 		if(sqlStatement != null)
 		{
			sqlStatement.executeUpdate("INSERT INTO runs ("
					+ "chain, ml_technique, training_error, selection_error, dataset_size, misclassified_samples," +
 					"is_test, macro_accuracy, macro_precision, macro_recall, macro_f1, micro_accuracy, micro_precision," +
 					"micro_recall, micro_f1, misclassification, ensemble_size) VALUES (" + chainId +
 					", '" + chainPars.getMLF() + "'" +
 					", " + trainingError +
 					", " + this.technique.getSelectionError() +
 					", " + this.technique.getTrainingSet().size() +
					", " + this.technique.getMisclassificationCount(dataSet,dataMapper) +
 					", " + (isTest ? 1 : 0) +
 					", " + perf.getAccuracy(PerfResults.AveragingMethod.MACRO) +
 					", " + perf.getPrecision(PerfResults.AveragingMethod.MACRO) +
 					", " + perf.getRecall(PerfResults.AveragingMethod.MACRO) +
 					", " + perf.FScore(1.0, PerfResults.AveragingMethod.MACRO) +
 					", " + perf.getAccuracy(PerfResults.AveragingMethod.MICRO) +
 					", " + perf.getPrecision(PerfResults.AveragingMethod.MICRO) +
 					", " + perf.getRecall(PerfResults.AveragingMethod.MICRO) +
 					", " + perf.FScore(1.0, PerfResults.AveragingMethod.MICRO) +
 					", " + this.technique.getMisclassification(dataSet,dataMapper) +
 					", " + technique.getCurrentSize() +
 					");"
 					, Statement.RETURN_GENERATED_KEYS
 			);
 			ResultSet rs = sqlStatement.getGeneratedKeys();
 			if(rs.next())
 			{
 				runId = rs.getLong(1);
 			}
 			rs.close();
 		}
 		else
 		{
 			System.out.println();
 			System.out.println(isTest ? "TEST" : "TRAINING");
 			System.out.println("size = " + technique.getCurrentSize());
 			System.out.println("ml_technique = " + chainPars.getMLF());
 			System.out.println("training_error = " + trainingError);
 			System.out.println("misclassified = " + this.technique.getMisclassificationCount(dataSet,dataMapper));
 			System.out.println("set size = " + dataSet.size());
			System.out.println("misclassification rate = " + this.technique.getMisclassification(dataSet,dataMapper));
 			System.out.println("precision = " + perf.getPrecision(PerfResults.AveragingMethod.MACRO));
 			System.out.println("recall = " + perf.getRecall(PerfResults.AveragingMethod.MACRO));
 			System.out.println("accuracy = " + perf.getAccuracy(PerfResults.AveragingMethod.MACRO));
 		}
 		int outputs = dataSet.getIdealSize();
 		if(sqlStatement!=null)
 		{
 			for (int output = 0; output < outputs; output ++)
 			{
 				sqlStatement.executeUpdate(
 						"INSERT INTO class_details (run, class, is_test, tp, tn, fp, fn) VALUES (" + runId +
 						", '" + dataMapper.getClassLabel(output) + "'" +
 						", " + (isTest ? 1 : 0) +
 						", " + perf.getTP(output) + 
 						", " + perf.getTN(output) +
 						", " + perf.getFP(output) +
 						", " + perf.getFN(output) +
 						");"
 				);
 			}
 		}
 	}
 
 	public void getResults (ChainParams prefix, double te, DBConnect reconnect, long chainId, boolean noSQL) throws SQLException, FileNotFoundException, IOException, WeightMismatchException
 	{
 		while(technique.hasStepsLeft())
 		{
 			if(!noSQL)
 			{
 				Connection sqlConnection = reconnect.connect();
 				Statement sqlStatement = sqlConnection.createStatement();
 				makeLine(false,te,prefix,this.dataLoader.getTrainingSet(), sqlStatement, chainId);
 				makeLine(true,te,prefix,this.dataLoader.getTestSet(), sqlStatement, chainId);
 				sqlConnection.close();
 			}
 			else
 			{
 				makeLine(false,te,prefix,this.dataLoader.getTrainingSet(), null, chainId);
 				makeLine(true,te,prefix,this.dataLoader.getTestSet(), null, chainId);				
 			}
 			try
 			{
 				technique.step(false);
 			}
 			catch (TrainingAborted e)
 			{
 				System.out.println("Training aborted on E_t = " + te + ", in chain" + chainId);
 			}
 		}
 	}
 
 	public EvaluationTechnique getTechnique()
 	{
 		return technique;
 	}
 
 	public void setTechnique(EvaluationTechnique technique)
 	{
 		this.technique = technique;
 	}
 	
 }
