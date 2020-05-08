 package gr.auth.ee.lcs.tests;
 
 import static org.junit.Assert.*;
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ExtendedBitSet;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.ComplexRepresentation;
 import gr.auth.ee.lcs.data.ComplexRepresentation.Attribute;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ComplexRepresentationTest {
 
 	ComplexRepresentation rep;
 	
 	@Before
 	public void setUp() throws Exception {
 		ComplexRepresentation.Attribute list[]=new Attribute[3];
 		String[] names={"Good","Bad"};
 		rep=new ComplexRepresentation(list,names);
 		ClassifierTransformBridge.setInstance(rep);
 		String[] attribute={"A","B","A+"};
		list[0]=rep.new NominalAttribute(rep.getChromosomeSize(), "nom", attribute);
		list[1]=rep.new IntervalAttribute(rep.getChromosomeSize(),"int",(float)-2.3,(float)5.785,10);
		list[2]=rep.new NominalAttribute(rep.getChromosomeSize(), "nom2", attribute);
 		
 		
 	}
 	
 	@Test
 	public void checkSize(){
 		assertTrue(rep.getChromosomeSize()==29);
 	}
 	
 	@Test 
 	public void coverageMatches(){
 		double visionVector[]={0,1.1256,2};
 		for (int i=0;i<1000;i++){ //Random check 1000 of these instances
 			Classifier cover=rep.createRandomCoveringClassifier(visionVector, 0);
 			assertTrue(rep.isMatch(visionVector, cover.chromosome));
 		}		
 	}
 	
 	@Test
 	public void checkStringOutput(){
 		ExtendedBitSet set=new ExtendedBitSet("11101111111111000000000011011");
 		Classifier ex=new Classifier();
 		ex.chromosome=set;
 		//System.out.println(rep.toNaturalLanguageString(ex)); //TODO: Fix and test
 	}
 	
 	@Test
 	public void moreGeneralTest1(){
 		ExtendedBitSet set1=new ExtendedBitSet("11101111111111000000000011011");
 		Classifier ex1=new Classifier();
 		ex1.chromosome=set1;
 		
 		ExtendedBitSet set2=new ExtendedBitSet("11111111111111000000000011011");
 		Classifier ex2=new Classifier();
 		ex2.chromosome=set2;
 		
 		assertFalse(rep.isMoreGeneral(ex2, ex1));
 		assertTrue(rep.isMoreGeneral(ex1, ex2));
 					
 	}
 	
 	@Test
 	public void moreGeneralTest2(){
 		ExtendedBitSet set1=new ExtendedBitSet("11101111111111000000000001010");
 		Classifier ex1=new Classifier();
 		ex1.chromosome=set1;
 		
 		ExtendedBitSet set2=new ExtendedBitSet("11111111111111000000000011011");
 		Classifier ex2=new Classifier();
 		ex2.chromosome=set2;
 		
 		
 		assertFalse(rep.isMoreGeneral(ex2, ex1));
 		assertTrue(rep.isMoreGeneral(ex1, ex2));
 	}
 	
 	@Test
 	public void moreGeneralTest3(){
 		ExtendedBitSet set1=new ExtendedBitSet("11101111111111000000000011010");
 		Classifier ex1=new Classifier();
 		ex1.chromosome=set1;
 		
 		ExtendedBitSet set2=new ExtendedBitSet("00000000010001000000001010010");
 		Classifier ex2=new Classifier();
 		ex2.chromosome=set2;
 		
 		assertFalse(rep.isMoreGeneral(ex2, ex1));
 		assertTrue(rep.isMoreGeneral(ex1, ex2));
 	}
 	
 	@Test
 	public void fixChromosomeTest(){
 		ExtendedBitSet set1=new ExtendedBitSet("11111111111110111111111111010");
 		Classifier ex1=new Classifier();
 		ex1.chromosome=set1;
 		
 		//Does fix work correctly?
		ExtendedBitSet fixed=new ExtendedBitSet("11111111111111111111111011010");
 		rep.fixChromosome(set1);
 		assertTrue(set1.equals(fixed));
 		
 		//Does fix work correctly on correct chromosomes?
 		rep.fixChromosome(fixed);
 		assertTrue(set1.equals(fixed));
 		
 	}
 	
 
 }
