 package classification;
 
 import java.util.Iterator;
 
 import moa.core.InstancesHeader;
 import moa.options.ClassOption;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.Utils;
 
 public class NaiveBayesMoaClassifier extends Classifier {
 
 	private moa.classifiers.Classifier learner;
 	
 	public NaiveBayesMoaClassifier() {
 		ClassOption learnerOption = new ClassOption("learner", 'l',
 				"Classifer to train", moa.classifiers.Classifier.class,
 				"NaiveBayes");
 		//learnerOption.setValueViaCLIString("NaiveBayes");
 		learner = (moa.classifiers.Classifier) learnerOption.
 					materializeObject(null, null);
 		learner.prepareForUse();
 	}
 	
 	@Override
 	public void buildClassifier(Instances trainingSet) {
 		InstancesHeader header = new InstancesHeader(trainingSet);
 		learner.setModelContext(header);
 		
		for (int i=0;i<trainingSet.numInstances();++i){
			Instance trainInst = trainingSet.instance(i);
 			learner.trainOnInstance(trainInst);
 		}
 	}
 
 	@Override
 	public double classifyInstance(Instance instance) {
 		double[] prediction = learner.getVotesForInstance(instance);
 		return Utils.maxIndex(prediction);
 	}
 
 	@Override
 	public double[] distributionForFeaturesVector(Instance instance) {
 		return learner.getVotesForInstance(instance);
 	}
 
 }
