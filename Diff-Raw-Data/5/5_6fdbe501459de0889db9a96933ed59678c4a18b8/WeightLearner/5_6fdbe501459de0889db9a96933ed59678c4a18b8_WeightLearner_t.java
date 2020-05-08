 package edu.umd.cs.linqs;
 
 import java.util.Map;
 import java.util.Random;
 
 import org.slf4j.Logger;
 
 import com.google.common.collect.Iterables;
 
 import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxLikelihoodMPE;
 import edu.umd.cs.psl.application.learning.weight.maxlikelihood.MaxPseudoLikelihood;
 import edu.umd.cs.psl.application.learning.weight.maxmargin.MaxMargin;
 import edu.umd.cs.psl.application.learning.weight.random.FirstOrderMetropolisRandOM;
 import edu.umd.cs.psl.application.learning.weight.random.HardEMRandOM;
 import edu.umd.cs.psl.config.ConfigBundle;
 import edu.umd.cs.psl.database.Database;
 import edu.umd.cs.psl.model.Model;
 import edu.umd.cs.psl.model.kernel.CompatibilityKernel;
 import edu.umd.cs.psl.model.parameters.PositiveWeight;
 import edu.umd.cs.psl.model.parameters.Weight;
 
 public class WeightLearner {
 	public static void learn(String method, Model m, Database db, Database labelsDB, Map<CompatibilityKernel,Weight> initWeights, ConfigBundle config, Logger log)
 			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
 		/* init weights */
 		for (CompatibilityKernel k : Iterables.filter(m.getKernels(), CompatibilityKernel.class))
 			k.setWeight(initWeights.get(k));
 
 		/* learn/set the weights */
 		if (method.equals("MLE")) {
 			MaxLikelihoodMPE mle = new MaxLikelihoodMPE(m, db, labelsDB, config);
 			mle.learn();
			mle.close();
 		}
 		else if (method.equals("MPLE")) {
 			MaxPseudoLikelihood mple = new MaxPseudoLikelihood(m, db, labelsDB, config);
 			mple.learn();
			mple.close();
 		}
 		else if (method.equals("MM")) {
 			MaxMargin mm = new MaxMargin(m, db, labelsDB, config);
 			mm.learn();
			mm.close();
 		}
 //		else if (method.equals("HEMRandOM")) {
 //			HardEMRandOM hardRandOM = new HardEMRandOM(m, db, labelsDB, config);
 //			hardRandOM.setSlackPenalty(10000);
 //			hardRandOM.learn();
 //		}
 //		else if (method.equals("RandOM")) {
 //			FirstOrderMetropolisRandOM randOM = new FirstOrderMetropolisRandOM(m, db, labelsDB, config);
 //			randOM.learn();
 //		}
 		else if (method.equals("SET_TO_ONE")) {
 			for (CompatibilityKernel k : Iterables.filter(m.getKernels(), CompatibilityKernel.class))
 				k.setWeight(new PositiveWeight(1.0));
 		}
 		else if (method.equals("RAND")) {
 			Random rand = new Random();
 			for (CompatibilityKernel k : Iterables.filter(m.getKernels(), CompatibilityKernel.class))
 				k.setWeight(new PositiveWeight(rand.nextDouble()));
 		}
 		else if (method.equals("NONE"))
 			;
 		else {
 			log.error("Invalid method ");
 			throw new ClassNotFoundException("Weight learning method " + method + " not found");
 		}
 	}
 }
