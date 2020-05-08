 package gr.auth.ee.lcs.data;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ExtendedBitSet;
 import weka.core.Instances;
 
 
 public class ComplexRepresentation extends ClassifierTransformBridge {
 
   /** 
    *  The rate by which the createRandomCoveringClassifier considers a specific condition as Don't Care
    *  @deprecated
    */
   public double coveringGeneralizationRate;
 
   private Attribute[] attributeList;
   private int chromosomeSize=0;
   
   private String[] ruleConsequents;
   
   public ComplexRepresentation(Attribute[] attributes,String[] ruleConsequents){
 	  this.attributeList=attributes;
 	  this.ruleConsequents=ruleConsequents;
   }
 
   @Override
   public boolean isMatch(double[] visionVector, ExtendedBitSet chromosome) {
 	for (int i=0;i<attributeList.length;i++)
 		if (!attributeList[i].isMatch((float)visionVector[i], chromosome))
 			return false;
 	return true;
   }
 
   @Override
   public String toNaturalLanguageString(Classifier aClassifier) {
 	String nlRule="";
 	for (int i=0;i<attributeList.length;i++)
 		nlRule+=attributeList[i].toString(aClassifier.chromosome)+" AND ";
 	//Add consequence
 	nlRule+="=>"+ruleConsequents[aClassifier.actionAdvocated];
 	return nlRule;
   }
 
   @Override
   public Classifier createRandomCoveringClassifier(double[] visionVector, int advocatingAction) {
 	Classifier generatedClassifier=new Classifier();
 	for (int i=0;i<attributeList.length;i++)
 		attributeList[i].randomCoveringValue((float)visionVector[i], generatedClassifier);
 		
 	generatedClassifier.actionAdvocated=(int)Math.round(Math.random()*(ruleConsequents.length-1));
 	//generatedClassifier.actionAdvocated=advocatingAction;
 	return generatedClassifier;
   }
 
   @Override
   public boolean isMoreGeneral(Classifier baseClassifier,
 		Classifier testClassifier) {
 	//If classifiers advocate for different actions, return false
 	if (baseClassifier.actionAdvocated!=testClassifier.actionAdvocated)
 		return false;
 		
 	ExtendedBitSet baseChromosome =baseClassifier.getChromosome();
 	ExtendedBitSet testChromosome = testClassifier.getChromosome();
 	
 	for (int i=0;i<attributeList.length;i++)
 		if (!attributeList[i].isMoreGeneral(baseChromosome, testChromosome))
 			return false;
 	
 	return true;
   }
 
   @Override
   public void fixChromosome(ExtendedBitSet aChromosome) {
 	  for (int i=0;i<attributeList.length;i++)
 			attributeList[i].fixAttributeRepresentation(aChromosome);
 	
   }
 
   @Override
   public int getChromosomeSize() {
 	return chromosomeSize;
   }
 
   @Override
   public String toBitSetString(Classifier classifier) {
 	// TODO Auto-generated method stub
 	return null;
   }
 
   @Override
   public void setRepresentationSpecificClassifierData(Classifier aClassifier) {
 	// TODO Auto-generated method stub
 	
   }
 
 @Override
 public void buildRepresentationModel() {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public boolean areEqual(Classifier cl1, Classifier cl2) {
 	if (cl1.actionAdvocated!=cl2.actionAdvocated)
 		return false;
 	
 	ExtendedBitSet baseChromosome =cl1.getChromosome();
 	ExtendedBitSet testChromosome = cl2.getChromosome();
 	
 	for (int i=0;i<attributeList.length;i++)
 		if (!attributeList[i].isEqual(baseChromosome, testChromosome))
 			return false;
 	
 	return true;
 }
 
 /**
  * Arff Loader
  * TODO: In an inherited class (not representation specific)
  * @param inputArff the input .arff
  * @param precision bits used for precision
  * @throws IOException when .arff not found
  */
 public  ComplexRepresentation(String inputArff, int precision) throws IOException{
 	FileReader reader = new FileReader(inputArff);
 	Instances instances = new Instances(reader);
 	if (instances.classIndex()<0)
 		instances.setClassIndex(instances.numAttributes() - 1);
 	
 	attributeList= new Attribute[instances.numAttributes()-1];
 	
 	//Rule Consequents
 	Enumeration<?> classNames = instances.classAttribute().enumerateValues();
 	ruleConsequents=new String[instances.numClasses()];
 	for (int i=0;i<instances.numClasses();i++)
 		ruleConsequents[i]=(String) classNames.nextElement();
 	
 	for (int i=0;i<instances.numAttributes();i++){
 		if (i==instances.classIndex()) continue;
 		
 		String attributeName=instances.attribute(i).name();
 		
 		if (instances.attribute(i).isNominal()){
 			
 			String[] attributeNames=new String[instances.attribute(i).numValues()];
 			Enumeration<?> values = instances.attribute(i).enumerateValues();
 			for (int j=0;j<attributeNames.length;j++){
 				attributeNames[j]=(String)values.nextElement();
 			}
 			//Create boolean or generic nominal
 			if (attributeNames.length>2)
 				attributeList[i]=new ComplexRepresentation.NominalAttribute(this.chromosomeSize,
 						attributeName,attributeNames,0.01);
 			else
 				attributeList[i]=new ComplexRepresentation.BooleanAttribute(
 						chromosomeSize, attributeName,0.33);
 			
 		}else if (instances.attribute(i).isNumeric()){
 			//Find min-max values
 			float minValue,maxValue;
			minValue=Float.valueOf(instances.attribute(i).value(0));
 			maxValue=minValue;
 			for (int sample=0;sample<instances.numInstances();sample++){
				float currentVal=Float.valueOf(instances.attribute(i).value(sample));
 				if (currentVal>maxValue) maxValue=currentVal;
 				if (currentVal<minValue) minValue=currentVal;
 			}
 			
 			attributeList[i]=new ComplexRepresentation.IntervalAttribute(this.chromosomeSize+1
 					, attributeName, minValue, maxValue, precision,0.1);
 		}
 		
 	}
 	
 }
 
 /**
  * A private inner abstract class that represents a single attribute
  * @author Miltos Allamanis
  */
 public abstract class Attribute{
 	protected int lengthInBits; //The length in bits of the attribute in the chromosome
 	protected int positionInChromosome; //The attribute's position in the chromosome
 	protected String nameOfAttribute; //The human-readable name of the attribute
 	protected double generalizationRate;
 	
 	/**
 	 * Convert Attribute to human-readable String
 	 */
 	public abstract String toString(ExtendedBitSet convertingClassifier);
 	
 	/**
 	 * Check if the vision is a match to 
 	 * @param attributeVision
 	 * @return
 	 */
 	public abstract boolean isMatch(float attributeVision, ExtendedBitSet testedChromosome);
 	
 	public abstract void randomCoveringValue(float attributeValue,Classifier generatedClassifier);
 	
 	public abstract void fixAttributeRepresentation(ExtendedBitSet generatedClassifier);
 	
 	public Attribute(int startPosition,String attributeName, double generalizationRate){
 		nameOfAttribute=attributeName;
 		positionInChromosome=startPosition;
 		this.generalizationRate= generalizationRate;
 	}	
 	
 	public int getLengthInBits(){
 		return lengthInBits;
 	}
 	
 	/**
 	 * Tests is baseChromsome is more general than the test chromosome
 	 * @param baseChromosome
 	 * @param testChromosome
 	 * @return
 	 */
 	public abstract boolean isMoreGeneral(ExtendedBitSet baseChromosome, ExtendedBitSet testChromosome);
 	
 	public abstract boolean isEqual(ExtendedBitSet baseChromosome, ExtendedBitSet testChromosome);
 }
 
 /**
  * A nominal attribute. It is supposed that the vision vector "sees" a number from 0 to n-1
  * where n is the number of all possible values.
  * @author Miltos Allamanis
  */
 public class NominalAttribute extends Attribute{
 
 	private String[] nominalValuesNames;
 	
 	public NominalAttribute(int startPosition, String attributeName,
 			String[] nominalValuesNames, double generalizationRate) {
 		super(startPosition, attributeName,generalizationRate);
 		this.nominalValuesNames=nominalValuesNames;
 		//We are going to use one bit per possible value plus one activation bit
 		lengthInBits=nominalValuesNames.length+1;
 		chromosomeSize+=lengthInBits;
 	}
 
 	@Override
 	public String toString(ExtendedBitSet convertingChromosome) {
 		//Check if attribute is active
 		if (!convertingChromosome.get(positionInChromosome))
 			return nameOfAttribute+":#";
 		String attr;
 		attr=nameOfAttribute+" in [";
 		for (int i=0;i<nominalValuesNames.length;i++)
 			if (convertingChromosome.get(positionInChromosome+1+i))
 				attr+=nominalValuesNames[i]+", ";
 		return attr+"]";
 	}
 
 	@Override
 	public void randomCoveringValue(float attributeValue,
 			Classifier myChromosome) {
 		//Clear everything
 		myChromosome.chromosome.clear(positionInChromosome, this.lengthInBits);
 		if (Math.random()<(1-generalizationRate)) 
 			myChromosome.chromosome.set(positionInChromosome);
 		else
 			myChromosome.chromosome.clear(positionInChromosome);
 		
 		//Randomize all bits of gene
 		for (int i=1;i<lengthInBits;i++){
 			if (Math.random()<0.2) //TODO: Variable probability?
 				myChromosome.chromosome.set(positionInChromosome+i);
 			else
 				myChromosome.chromosome.clear(positionInChromosome+i);
 		}		
 		
 		
 		//and set as "1" the nominal values that we are trying to match
 		myChromosome.chromosome.set(positionInChromosome+1+(int)attributeValue);
 		
 	}	
 	
 	@Override
 	public boolean isMatch(float attributeVision, ExtendedBitSet testedChromosome) {
 		//if condition is not active
 		if (!testedChromosome.get(positionInChromosome))
 			return true;
 		int genePosition=(int)attributeVision+1+positionInChromosome;
 		if (testedChromosome.get(genePosition))
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void fixAttributeRepresentation(ExtendedBitSet chromosome) {
 		int ones=0;
 		if (chromosome.get(positionInChromosome)) //Specific
 			for(int i=1;i<this.lengthInBits;i++)
 				if (chromosome.get(positionInChromosome+i))
 						ones++;
 		if (ones==0 || ones==lengthInBits-1) //Fix (we have none or all set]		
 			chromosome.clear(positionInChromosome);
 	}
 
 	@Override
 	public boolean isMoreGeneral(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 		if(!baseChromosome.get(positionInChromosome)) return true;
 		if(!testChromosome.get(positionInChromosome)) return false;
 		if(!baseChromosome.getSubSet(positionInChromosome+1,nominalValuesNames.length).equals(
 				testChromosome.getSubSet(positionInChromosome+1,nominalValuesNames.length)))
 			return false;
 			
 		return true;
 	}
 
 	@Override
 	public boolean isEqual(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 		if (baseChromosome.get(positionInChromosome)!=testChromosome.get(positionInChromosome))
 			return false;
 		if (!baseChromosome.get(positionInChromosome)) return true;
 		if(!baseChromosome.getSubSet(positionInChromosome+1,nominalValuesNames.length).equals(
 				testChromosome.getSubSet(positionInChromosome+1,nominalValuesNames.length)))
 			return false;
 			
 		return true;
 		
 	}
 	
 }
 
 /**
  * A private inner abstract class that represents a numeric interval
  * @author Miltos Allamanis
  *
  */
 public class IntervalAttribute extends Attribute{
 
 	float minValue,maxValue; //The minimum and the maximum value that the attribute can receive
 	int precisionBits=0;
 	int totalParts=0;
 	
 	
 	public IntervalAttribute(int startPosition, String attributeName, float minValue,
 			float maxValue, int precisionBits, double generalizationRate) {
 		super(startPosition, attributeName, generalizationRate);
 		this.minValue=minValue;
 		this.maxValue=maxValue;
 		this.precisionBits=precisionBits;
 		//2 values of bits + 1 activation bit
 		lengthInBits=2*precisionBits+1;
 		for (int i=0;i<precisionBits;i++)
 			totalParts|=1<<i;
 		chromosomeSize+=lengthInBits;
 	}
 
 	private float getLowBoundValue(ExtendedBitSet chromosome){
 		int part=chromosome.getIntAt(positionInChromosome+1,precisionBits);
 		
 		return ((float)part)/((float)totalParts)*(maxValue-minValue)+minValue;
 	}
 	
 	private float getHighBoundValue(ExtendedBitSet chromosome){
 			int part=chromosome.getIntAt(positionInChromosome+1+precisionBits,precisionBits);
 			
 			return ((float)part)/((float)totalParts)*(maxValue-minValue)+minValue;
 	}
 	
 	@Override
 	public String toString(ExtendedBitSet convertingChromosome) {
 		//Check if condition active
 		if (!convertingChromosome.get(positionInChromosome))
 			return nameOfAttribute+":#";
 		String value=nameOfAttribute+" in ["+getLowBoundValue(convertingChromosome)+","+
 				getHighBoundValue(convertingChromosome)+"]";		
 		return value;
 	}
 
 	@Override
 	public boolean isMatch(float attributeVision,  ExtendedBitSet testedChromosome) {
 		if (!testedChromosome.get(positionInChromosome)) //if rule inactive
 			return true;
 		if (attributeVision>=getLowBoundValue((testedChromosome)) &&
 				attributeVision<=getHighBoundValue(testedChromosome))
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void randomCoveringValue(float attributeValue,
 			Classifier generatedClassifier) {
 		// First find a random value that is smaller than the attribute value & convert it to fraction
 		int newLowBound=(int)Math.floor(((attributeValue-minValue)*Math.random())/(maxValue-minValue)*totalParts);
 		int newMaxBound=(int)Math.ceil(((maxValue-minValue-(maxValue-attributeValue)*Math.random())/(maxValue-minValue)*totalParts));
 		
 		//Then set at chromosome
 		if (Math.random()<(1-generalizationRate))
 			generatedClassifier.chromosome.set(positionInChromosome);
 		else
 			generatedClassifier.chromosome.clear(positionInChromosome);
 		generatedClassifier.chromosome.setIntAt(positionInChromosome+1,precisionBits, newLowBound);
 		generatedClassifier.chromosome.setIntAt(positionInChromosome+1+precisionBits,precisionBits, newMaxBound);
 	}
 
 	@Override
 	public void fixAttributeRepresentation(ExtendedBitSet chromosome) {
 		if(getLowBoundValue(chromosome)>getHighBoundValue(chromosome)){ //Swap
 			ExtendedBitSet low=chromosome.getSubSet(positionInChromosome+1, precisionBits);
 			ExtendedBitSet high=chromosome.getSubSet(positionInChromosome+1+precisionBits, precisionBits);
 			chromosome.setSubSet(positionInChromosome+1, high);
 			chromosome.setSubSet(positionInChromosome+1+precisionBits, low);
 		}
 		
 	}
 
 	@Override
 	public boolean isMoreGeneral(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 		if(!baseChromosome.get(positionInChromosome)) return true;
 		if(!testChromosome.get(positionInChromosome)) return false;
 		if (getHighBoundValue(baseChromosome)>=getHighBoundValue(testChromosome) 
 				&& getLowBoundValue(baseChromosome)<=getLowBoundValue(testChromosome))
 			return true;
 		return false;
 	}
 
 	@Override
 	public boolean isEqual(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 		if (baseChromosome.get(positionInChromosome)!=testChromosome.get(positionInChromosome))
 			return false;
 		if (!baseChromosome.get(positionInChromosome)) return true;
 		if(!baseChromosome.getSubSet(positionInChromosome+1,precisionBits).equals(
 				testChromosome.getSubSet(positionInChromosome+1,precisionBits)))
 			return false;
 			
 		return true;
 	}
 	
 }
 
 /**
  * A boolean attribute
  * @author miltiadis
  *
  */
 public class BooleanAttribute extends Attribute{
 
 	public BooleanAttribute(int startPosition, String attributeName, double generalizationRate) {
 		super(startPosition, attributeName, generalizationRate);
 		lengthInBits=2;
 		chromosomeSize+=lengthInBits;
 	}
 
 	@Override
 	public String toString(ExtendedBitSet convertingClassifier) {
 		if (convertingClassifier.get(this.positionInChromosome)){
 			return convertingClassifier.get(this.positionInChromosome+1)?"1":"0";
 		}else{
 			return "#";
 		}
 		
 	}
 
 	@Override
 	public boolean isMatch(float attributeVision,
 			ExtendedBitSet testedChromosome) {
 		
 		if (testedChromosome.get(this.positionInChromosome)){
 			if ((attributeVision==0?false:true)==testedChromosome.get(this.positionInChromosome+1))
 				return true;
 			else
 				return false;
 		}else{
 			return true;
 		}
 	}
 
 	@Override
 	public void randomCoveringValue(float attributeValue,
 			Classifier generatedClassifier) {
 		if (attributeValue==0)
 			generatedClassifier.getChromosome().clear(positionInChromosome+1);
 		else
 			generatedClassifier.getChromosome().set(positionInChromosome+1);
 		
 		if (Math.random()<generalizationRate) //TODO: Configurable generalization rate
 			generatedClassifier.getChromosome().clear(positionInChromosome);
 		else
 			generatedClassifier.getChromosome().set(positionInChromosome);	
 				
 		
 	}
 
 	@Override
 	public void fixAttributeRepresentation(ExtendedBitSet generatedClassifier) {
 		return;		
 	}
 
 	@Override
 	public boolean isMoreGeneral(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 		//if the base classifier is specific and the test is # return false
 		if (baseChromosome.get(positionInChromosome) && !testChromosome.get(positionInChromosome))
 			return false;
 		if (baseChromosome.get(positionInChromosome+1)!=
 			testChromosome.get(positionInChromosome+1) && baseChromosome.get(positionInChromosome))
 			return false;
 		
 		return true;
 	}
 
 	@Override
 	public boolean isEqual(ExtendedBitSet baseChromosome,
 			ExtendedBitSet testChromosome) {
 			
 			//if the base classifier is specific and the test is # return false
 			if (baseChromosome.get(positionInChromosome)!=testChromosome.get(positionInChromosome))
 				return false;
 			else if (baseChromosome.get(positionInChromosome+1)!=
 				testChromosome.get(positionInChromosome+1) && baseChromosome.get(positionInChromosome))
 				return false;					
 		
 		return true;
 	}
 	
 }
 }
