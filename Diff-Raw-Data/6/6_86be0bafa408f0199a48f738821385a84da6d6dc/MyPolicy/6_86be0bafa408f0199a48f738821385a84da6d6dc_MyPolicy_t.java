 package myPolicy;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 
 import exploChallenge.logs.yahoo.YahooArticle;
 import exploChallenge.logs.yahoo.YahooVisitor;
 import exploChallenge.policies.ContextualBanditPolicy;
 
 /**
  * Projects the users to a smaller dimension based on a predefined projection matrix.
  * Actually the projection is the cosine similarity between the features that represents 
  * the user and the rows of the matrix rather than just the multiplication.
  * @author István Hegedűs
  *
  */
 public class MyPolicy implements ContextualBanditPolicy<YahooVisitor, YahooArticle, Boolean> {
   /**
    * The projection matrix
    */
   private static final byte[][] defaultDirections = {
     {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
     {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
     {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
     {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
   };
   //private static final String modelName = "P2Pegasos";
   private static final String modelName = "LogReg";
   private static final double ageTH = 10.0;
   
   private long age;
   private Random r;
   private Map<Integer, List<User>> article2users;
   private Map<Integer, Model> article2model;
   private double[] dist;
   
 	public MyPolicy() {
 	  r = new Random(1234567890);
 	  article2users = new HashMap<Integer, List<User>>();
 	  article2model = new HashMap<Integer, Model>();
 	  age = 0;
 	}
 	
 	/**
 	 * For all of possibleActions (items) computes a fitness value that represents the 
 	 * cumulative cosine similarity, multiplied by -1 if the reward was false and 
 	 * scaled to [0-1] and multiplied by the number of rates plus 1. If there is no 
 	 * reward for an item the fitness is 1. The item will be selected by the "roulette" 
 	 * selection algorithm based on the fitness values.
 	 */
 	@Override
 	public YahooArticle getActionToPerform(YahooVisitor visitor, List<YahooArticle> possibleActions) {
 	  //System.out.println("Predict: " + visitor + " " + possibleActions.size());
 	  age++;
 	  /*if (age >= 1000000) {
 	    throw new RuntimeException("End of simulation");
 	  }*/
 	  
 	  List<User> list;
 	  // projects the specified user
 	  User v = new User(project(visitor.getFeatures()), false);
 	  if (dist == null || dist.length < possibleActions.size()) {
 	    dist = new double[possibleActions.size()];
 	  }
 	  int d = 0;
 	  double comp;
 	  double sr;
 	  double s;
 	  // for all items
 	  for (YahooArticle a : possibleActions) {
 	    sr = 0.0;
 	    s = 0.0;
 	    // gets the list of users that have reward
 	    list = article2users.get(a.getID());
       if (list != null) {
         // for all users
         for (User u : list) {
           // computes the similarity and summarizes
           comp = v.compare(u);
           sr += comp * (u.reward ? 1.0 : -1.0);
           s += Math.abs(comp);
         }
         // cf based rate
         if (s == 0) {
           sr = 0.0;
         } else {
           sr /= s;
           // scaled to [0-1] and multiplied by the number of users, were rated
           sr = ((1.0 + sr) / 2.0);// * list.size();
         }
       }
       // predict model
       Model m = article2model.get(a.getID());
       if (m != null && m.getAge() >= ageTH) {
         sr = m.distributionForInstance(v.features)[1];
       }
       
       // compute final rating based on the number of users rated the given a item and the CF rating
      sr = MyPolicy.rateMapping((m != null) ? m.getAge() : 0.0, sr);
      //System.out.println("AGE: " + (list == null ? "null" : list.size()) + "\t" + (m == null ? "null" : m.getAge()));
       // stores cumulative fitness
       if (d == 0) {
         dist[d] = sr;
       } else {
         dist[d] = dist[d - 1] + sr;
       }
       d++;
 	  }
 	  
 	  // gets a random item based on the fitness (roulette selection algorithm)
 	  double rand = r.nextDouble() * dist[possibleActions.size()-1];
 	  int id = possibleActions.size()-1;
 	  for (int i = 0; i < possibleActions.size(); i++) {
 	    if (dist[i] >= rand) {
 	      id = i;
 	      break;
 	    }
 	  }
 	  return possibleActions.get(id);
 	  //return possibleActions.get(r.nextInt(possibleActions.size()));
 	  //return possibleActions.get(possibleActions.size() - 1);
 	  //return possibleActions.get(0);
 	}
 
 	@SuppressWarnings("unchecked")
   @Override
 	public void updatePolicy(YahooVisitor c, YahooArticle a, Boolean reward) {
 	  //System.out.println("Revard: " + c + "\t" + a.getID() + "\t" + reward);
 	  
 	  User u = new User(project(c.getFeatures()), reward);
 	  
 	  // updates model
     Model m = article2model.get(a.getID());
     if (m == null) {
       try {
         Class<?>[] classes = getClass().getDeclaredClasses();
         Class<? extends Model> modelClass = null;
         for (int i = 0; classes != null && i < classes.length; i ++) {
           if (classes[i] != null && classes[i].getCanonicalName() != null && classes[i].getCanonicalName().endsWith(modelName)) {
             modelClass = (Class<? extends Model>) classes[i];
             break;
           }
         }
         if (modelClass == null) {
           throw new ClassNotFoundException(modelName + " class not found!");
         }
         // the constructor of the inner class should receive a reference to the actual object (this)!
         Constructor<? extends Model> modelConstructor = modelClass.getConstructor(getClass());
         if (modelConstructor == null) {
           throw new ClassNotFoundException("Default constructor not found!");
         }
         m = modelConstructor.newInstance(this);
        article2model.put(a.getID(), m);
       } catch (Exception e) {
         throw new RuntimeException(e);
       }
     }
     m.update(u.features, reward ? 1.0 : 0.0);
 	  
     if (m.getAge() < ageTH) {
   	  // gets the users that correspond to the specified article
   	  List<User> list = article2users.get(a.getID());
   	  if (list == null) {
   	    list = new LinkedList<User>();
   	    article2users.put(a.getID(), list);
   	  }
   	  // projects the feature vector of the user and stores it
   	  list.add(u);
     } else {
       article2users.remove(a.getID());
     }
 	  
 	}
 	
 	private static double[] project(byte[] u) {
 	  double[] result = new double[defaultDirections.length];
 	  for (int i = 0; i < result.length; i++) {
 	    result[i] = cosSim(defaultDirections[i], u);
 	  }
 	  return result;
 	}
 	
 	private static double cosSim(byte[] a, byte[] b) {
     double result = 0.0;
     double a2 = 0.0;
     double b2 = 0.0;
     for (int i = 0; i < a.length; i++) {
       result += a[i] * b[i];
       a2 += a[i] * a[i];
       b2 += b[i] * b[i];
     }
     if (a2 == 0 || b2 == 0) {
       return 0;
     }
     return result / Math.sqrt(a2 * b2);
   }
 	
 	private static double cosSim(double[] a, double[] b) {
 	  double result = 0.0;
 	  double a2 = 0.0;
 	  double b2 = 0.0;
 	  for (int i = 0; i < a.length; i++) {
 	    result += a[i] * b[i];
 	    a2 += a[i] * a[i];
 	    b2 += b[i] * b[i];
 	  }
 	  if (a2 == 0 || b2 == 0) {
 	    return 0;
 	  }
 	  return result / Math.sqrt(a2 * b2);
 	}
 	
 	public static double innerProduct(final Map<Integer, Double> x, final Map<Integer, Double> y) {
     if (x == null || y == null || x.size() == 0 || y.size() == 0) {
       return 0.0;
     }
     double ret = 0.0;
     for (int id : x.keySet()) {
       if (y.containsKey(id)) {
         ret += x.get(id) * y.get(id);
       }
     }
     return ret;
   }
 	
 	public static int findMaxIdx(final Map<Integer, Double> a, final Map<Integer, Double> b) {
     int max = Integer.MIN_VALUE;
     for (int d : a.keySet()) {
       if (d > max) {
         max = d;
       }
     }
     for (int d : b.keySet()) {
       if (d > max) {
         max = d;
       }
     }
     return max;
 	}
 	
 	public static double rateMapping(double n, double r) {
 	  if (n <= 0.0 || n == 1.0) {
 	    // base case 1
 	    return 1.0;
 	  } else if ((n <= 3.0 && r <= 0.5) || (n == 2.0)) {
 	    // base case 2
 	    return 2.0 *  r;
 	  } else if (n <= 3.0) {
 	    // base case 3
 	    return 3.0 * r;
 	  } else {
 	    // general solution => e^(c(x-a)-b)
 	    final double qn = (n+Math.sqrt(n*n-4.0*(n-1)))/2.0;
 	    final double cn = Math.log(qn*qn);
 	    final double bn = 1.0/(qn-1.0);
 	    final double an = Math.log(qn-1.0)/cn;
 	    return Math.exp(cn*(r-an))-bn;
 	  }
 	}
 	
 	public static double computeSimilarity(final Map<Integer, Double> x, final Map<Integer, Double> y) {
     if (x != null && x.size() == 0 && y != null && y.size() == 0) {
       return 1.0;
     } else if (x.size() == 0 || y.size() == 0 || x == null || y == null) {
       return -1.0;
     }
 
     double yN = 0.0, xN = 0.0;
     double innerP = 0.0;
     for (int i : x.keySet()) {
       double xI = x.get(i);
       if (y.containsKey(i)) {
         innerP += xI * y.get(i);
       }
       xN += xI * xI;
     }
     for (int i : y.keySet()) {
       double yI = y.get(i);
       yN += yI * yI;
     }
     return innerP / Math.sqrt(xN * yN);
   }
 	
 	public static Map<Integer, Double> normalize(final Map<Integer, Double> vector){
     double norm = 0.0;
     for (int i : vector.keySet()){
       norm += vector.get(i) * vector.get(i);
     }
     norm = Math.sqrt(norm);
     Map<Integer, Double> normalized = new TreeMap<Integer, Double>();
     for (int i : vector.keySet()){
       normalized.put(i, vector.get(i) / norm);
     }
     return normalized;
   }
 	
 	private class User {
 	  final double[] features;
 	  final boolean reward;
 	  User(double[] features, boolean reward) {
 	    this.features = features;
 	    this.reward = reward;
 	  }
 	  public double compare(User u) {
 	    return cosSim(features, u.features);
 	  }
 	}
 	
 	private abstract class Model {
 	  public void update(double[] instance, double label) {
 	    update(array2map(instance), label);
 	  }
 	  public abstract void update(Map<Integer, Double> instance, double label);
 	  public double predict(double[] instance) {
 	    return predict(array2map(instance));
 	  }
 	  public abstract double predict(Map<Integer, Double> instance);
 	  public double[] distributionForInstance(double[] instance) {
       return distributionForInstance(array2map(instance));
     }
     public abstract double[] distributionForInstance(Map<Integer, Double> instance);
     public abstract double getAge();
 	  private Map<Integer, Double> array2map(double[] instance) {
 	    Map<Integer, Double> inst = new HashMap<Integer, Double>();
       for (int i = 0; i < instance.length; i++) {
         inst.put(i, instance[i]);
       }
       return inst;
 	  }
 	}
 	
 	private class P2Pegasos extends Model {
 	  protected static final double lambda = 0.0001;
 	  
 	  protected Map<Integer, Double> w;
 	  protected double age;
 	  
 	  /**
 	   * Creates a default model with age=0 and the separating hyperplane is the 0 vector.
 	   */
 	  public P2Pegasos(){
 	    w = new TreeMap<Integer, Double>();
 	    age = 0.0;
 	  }
 	  
 	  /**
 	   * Returns a new P2Pegasos object that initializes its variables with 
 	   * the deep copy of the specified parameters.
 	   * @param w hyperplane
 	   * @param age model age
 	   * @param lambda learning parameter
 	   */
 	  protected P2Pegasos(Map<Integer, Double> w, double age){
 	    this.w = new TreeMap<Integer, Double>();
 	    for (int k : w.keySet()){
 	      this.w.put(k, (double)w.get(k));
 	    }
 	    this.age = age;
 	  }
 	  
 	  public Object clone(){
 	    return new P2Pegasos(w, age);
 	  }
 
 	  /**
 	   * The official Pegasos update with the specified instances and corresponding label.
 	   */
 	  @Override
 	  public void update(final Map<Integer, Double> instance, double label) {
 	    label = (label == 0.0) ? -1.0 : label;
 	    age ++;
 	    double nu = 1.0 / (lambda * age);
 	    boolean isSV = label * innerProduct(w, instance) < 1.0;
 	    int max = findMaxIdx(w, instance);
 	    for (int i = 0; i <= max; i ++) {
 	      Double wOldCompD = w.get(i);
 	      Double xCompD = instance.get(i);
 	      if (wOldCompD != null || xCompD != null) {
 	        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
 	        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
 	        if (isSV) {
 	          // the current point in the current model is a SV
 	          // => applying the SV-based update rule
 	          w.put(i, (1.0 - 1.0 / age) * wOldComp + nu * label * xComp);
 	        } else {
 	          // the current point is not a SV in the currently stored model
 	          // => applying the normal update rule
 	          if (wOldCompD != null) {
 	            w.put(i, (1.0 - 1.0 / age) * wOldComp);
 	          }
 	        }
 	      }
 	    }
 	  }
 
 	  /**
 	   * Computes the inner product of the hyperplane and the specified instance. 
 	   * If it is greater than 0 then the label is positive (1.0), otherwise the label is
 	   * negative (0.0).
 	   */
 	  @Override
 	  public double predict(final Map<Integer, Double> instance) {
 	    double innerProd = innerProduct(w, instance);
 	    return innerProd > 0.0 ? 1.0 : 0.0;
 	  }
 	  
 	  public double[] distributionForInstance(Map<Integer, Double> instance) {
 	    double predict = predict(instance);
 	    return new double[] {1.0 - predict, predict};
 	  }
 	  
 	  public double getAge() {
 	    return age;
 	  }
 
 	  /**
 	   * Returns the cosine similarity of the hyperplanes of the current and the specified models. 
 	   */
 	  public double computeSimilarity(final P2Pegasos model) {
 	    return MyPolicy.computeSimilarity(w, model.w);
 	  }
 	  
 	  /**
 	   * It returns the string representation of the hyperplane.
 	   * 
 	   * @return String representation
 	   */
 	  public String toString() {
 	    return w.toString() + ", age: " + age;
 	  }
 	}
 	
 	private class LogReg extends Model {
 	  protected static final double lambda = 0.0001;
 	  protected Map<Integer, Double> w;
 	  protected double age;
 	  
 	  /**
 	   * Initializes the hyperplane as 0 vector.
 	   */
 	  public LogReg(){
 	    this.w = new TreeMap<Integer, Double>();
 	    this.age = 0.0;
 	  }
 	  
 	  /**
 	   * Returns a new logistic regression object that initializes its variable with 
 	   * the deep copy of the specified parameters.
 	   * @param w hyperplane
 	   * @param age model age
 	   */
 	  protected LogReg(Map<Integer, Double> w, double age){
 	    this.w = new TreeMap<Integer, Double>();
 	    for (int k : w.keySet()){
 	      this.w.put(k, (double)w.get(k));
 	    }
 	    this.age = age;
 	  }
 	  
 	  /**
 	   * Clones the object.
 	   */
 	  public Object clone(){
 	    return new LogReg(w, age);
 	  }
 
 	  @Override
 	  public void update(Map<Integer, Double> instance, double label) {
 	    double prob = getPositiveProbability(instance);
 	    double err = label - prob;
 	    age ++;
 	    double nu = 1.0 / (lambda * age);
 	    int max = findMaxIdx(w, instance);
 	    for (int i = -1; i <= max; i ++) {
 	      Double wOldCompD = w.get(i);
 	      Double xCompD = instance.get(i);
 	      // using w0 as bias
 	      if (i == -1) {
 	        xCompD = 1.0;
 	      }
 	      if (wOldCompD != null || xCompD != null) {
 	        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
 	        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
 	        w.put(i, (1.0 - nu * lambda) * wOldComp - nu * err * xComp);
 	      }
 	    }
 	  }
 	  
 	  /**
 	   * Computes the probability that the specified instance belongs to the positive class i.e. 
 	   * P(Y=1 | X=x, w) = 1 / (1 + e^(w'x + b)).
 	   * @param instance instance to compute the probability
 	   * @return positive label probability of the instance
 	   */
 	  private double getPositiveProbability(Map<Integer, Double> instance){
 	    double b = 0.0;
 	    if (w.containsKey(-1)){
 	      b = w.get(-1);
 	    }
 	    double predict = innerProduct(w, normalize(instance)) + b;
 	    predict = Math.exp(predict) + 1.0;
 	    return 1.0 / predict;
 	  }
 	  
 	  @Override
 	  public double[] distributionForInstance(Map<Integer, Double> instance) {
 	    double[] distribution = new double[2];
 	    distribution[1] = getPositiveProbability(instance);
 	    distribution[0] = 1.0 - distribution[1];
 	    return distribution;
 	  }
 	  
 	  public final double predict(Map<Integer, Double> instance) {
 	    int maxLabelIndex = -1;
 	    double maxValue = Double.NEGATIVE_INFINITY;
 	    double[] distribution = distributionForInstance(instance);
 	    for (int i = 0; i < 2; i++){
 	      if (distribution[i] > maxValue){
 	        maxValue = distribution[i];
 	        maxLabelIndex = i;
 	      }
 	    }
 	    return maxLabelIndex;
 	  }
 	  
 	  public double getAge() {
 	    return age;
 	  }
 
 	  public double computeSimilarity(LogReg model) {
 	    return MyPolicy.computeSimilarity(w, model.w);
 	  }
 	}
 
 }
