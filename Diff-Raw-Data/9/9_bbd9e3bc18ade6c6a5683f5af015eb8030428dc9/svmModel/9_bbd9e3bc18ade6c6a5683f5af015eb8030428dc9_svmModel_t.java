 package PatternGame;
 
 import libsvm.*;
 
 /**
  * @author Bing
  *
  */
 public class svmModel extends svm {
 	
 	public svm_problem problem;
 	public svm_parameter parameters;
 	public svm_model model;
 	public int sample_size;
 	public static int framespersecond = 128;
 	
 	
 
 /*  This is is copied from the official SVM documentation
  * 	svm_type can be one of C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR.
 
     C_SVC:		C-SVM classification
     NU_SVC:		nu-SVM classification
     ONE_CLASS:		one-class-SVM
     EPSILON_SVR:	epsilon-SVM regression
     NU_SVR:		nu-SVM regression
 
     kernel_type can be one of LINEAR, POLY, RBF, SIGMOID.
 
     LINEAR:	u'*v
     POLY:	(gamma*u'*v + coef0)^degree
     RBF:	exp(-gamma*|u-v|^2)
     SIGMOID:	tanh(gamma*u'*v + coef0)
     PRECOMPUTED: kernel values in training_set_file
 
     cache_size is the size of the kernel cache, specified in megabytes.
     C is the cost of constraints violation. 
     eps is the stopping criterion. (we usually use 0.00001 in nu-SVC,
     0.001 in others). nu is the parameter in nu-SVM, nu-SVR, and
     one-class-SVM. p is the epsilon in epsilon-insensitive loss function
     of epsilon-SVM regression. shrinking = 1 means shrinking is conducted;
     = 0 otherwise. probability = 1 means model with probability
     information is obtained; = 0 otherwise.
 
     nr_weight, weight_label, and weight are used to change the penalty
     for some classes (If the weight for a class is not changed, it is
     set to 1). This is useful for training classifier using unbalanced
     input data or with asymmetric misclassification cost.
 
     nr_weight is the number of elements in the array weight_label and
     weight. Each weight[i] corresponds to weight_label[i], meaning that
     the penalty of class weight_label[i] is scaled by a factor of weight[i].
     
     If you do not want to change penalty for any of the classes,
     just set nr_weight to 0.
     */
 	/**
 	 * 
 	 */
 	public svmModel() {
 		super();
 		this.problem = new svm_problem();
 		
 		this.parameters = new svm_parameter();
 		parameters.svm_type = svm_parameter.C_SVC;
 		parameters.kernel_type = svm_parameter.LINEAR;
 		parameters.degree = 0;
 		parameters.gamma = 0;
 		parameters.coef0 = 0;
 		
 		parameters.cache_size = 500f;
 		parameters.eps = 0.001f;
 		parameters.C = 0;
 		parameters.nr_weight = 0;
 		parameters.shrinking = 0;
 		parameters.probability = 1;
 		
 		System.err.println(svm.svm_check_parameter(problem, parameters));
 		
 		this.model = null;
 		
 		this.sample_size = 1;
 	}
 	
 	/**
 	 * This method only takes in a 1-second sample of 128 frames as an array of doubles
 	 * @param input
 	 * @return a list of probabilities 
 	 */
 	public double[] predict(double[] input)
 	{
		//int [] labels = null;
		System.out.println("length:"+input.length);
		int [] labels = new int[input.length];
 		svm_get_labels(model, labels);
 		if ( model == null ) // if model doesn't exist or there are no classes
 		{
 			System.err.println("Invalid Model");
 			System.exit(0);
 		}
 		
 		if (svm_check_probability_model(model) == 0) // if probability is not enabled
 		{
 			System.err.println("Model cannot make probability estitames, please check model parameters");
 			System.exit(0);
 			return null;
 		}
 
 		svm_node[] testData = new svm_node[input.length];
 		double[] probabilities = new double[3];
 	
 		
 		for (int i = 0; i < input.length; i++)
		{	
			testData[i] = new svm_node();
			
 			testData[i].index = i + 1;
 			testData[i].value = input[i];
 		}
 		
 		svm_predict_values(model, testData, probabilities);
 		
 		
 		return probabilities;
 	}
 	
 	/**
 	 * Use this method to create the model
 	 * Can use prediction after the model is created
 	 * @param a CombineSvmMatrix object
 	 */
 	public void train(CombineSvmMatrix input){
 		if (model != null)
 		{
 			System.err.println("There is an existing model");
 		}
 		this.problem.l = input.row;
 		this.problem.y = input.svmLabel;
 		this.problem.x = convertMatrix(input);
 		//this.problem.x = convertMatrix2(input.getSvmMatrix(), input.row, input.col);
 		
 		this.model = svm_train(problem, parameters);
 		
 		if ( this.model != null ){
 			System.err.println("Training successful");
 		}
 		else
 		{
 			System.err.println("Training failed, please verify model parameters and training data!");
 			System.exit(0);
 		}
 		
 		
 	}
 	
 	/**
 	 * @param a CombineSvmMatrix object
 	 */
 	public void retrain(CombineSvmMatrix input)
 	{
 		model = null;
 		problem = null;
 		this.train(input);
 	}
 	
 /*	The format of training and testing data file is:
 
 		<label> <index1>:<value1> <index2>:<value2> ...
 		.
 		.
 		.
 		
 	Gonna use this private method to convert provide matrices to training data
 
 	*/
 	/**
 	 * @param input
 	 * @return an array of svm_node
 	 */
 	private svm_node[][] convertMatrix(CombineSvmMatrix input){
 		
 		svm_node[][] data = new svm_node[input.row][input.col];
 		//svm_node data = new svm_node();
 		
 		for ( int i = 0 ; i < input.row; i++  )
 		{
 			for ( int s =  0; s < input.col; s++)
 			{
 				data[i][s] = new svm_node();
 				
 				data[i][s].index = s + 1;
 				data[i][s].value = input.svm[i][s];
 			}
 			
 		}
 		
 		return data;
 	}
 	
 
 }
