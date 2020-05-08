 package tests;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import messages.MessageFromPraat;
 import messages.MessageToPraat;
 import monitoring.Curve;
 import monitoring.MonitoringCSV;
 
 import org.uncommons.maths.random.MersenneTwisterRNG;
 import org.uncommons.maths.random.Probability;
 
 import praatGestion.OrderToPraat;
 import praatGestion.Praat;
 import communication.CloseServer;
 import communication.ServerThread;
 import elements.Formant;
 import elements.FormantSequence;
 import elements.GlobalAlphabet;
 import elements.Sequence;
 import exceptions.CastFormantException;
 import exceptions.FormantNumberexception;
 import exceptions.PraatScriptException;
 import exceptions.SequenceArrayException;
 import geneticAlogrithm.GeneticAlgorithmCall;
 import geneticAlogrithm.SequenceCrossOver;
 import geneticAlogrithm.SequenceEvaluator;
 import geneticAlogrithm.SequenceFactory;
 import geneticAlogrithm.SequenceMutation;
 
 /** <p>Class where i put my unitary test to be sure that if i made modifications on the source code of a class, it will continue
  * to work properly. I will make small functions for each. It is only unitary test, another class is available for integration test</p>
  * 
  *  
  * @author Pierre-Yves Hervo
  * @version 0.1
  */
 public class UnitaryTestsGA {
 	
 	/**
 	 * private constructor to forbid to instanciate
 	 */
 	private UnitaryTestsGA(){
 		
 	}
 	
 	/**
 	* Test if the server launch and close in a new thread as it should.
 	*
 	
 	* @since 0.1
 	*
 	*/
 	public static void servertest(){
 		GeneticAlgorithmCall ga;
 		try {
 			ga = new GeneticAlgorithmCall(13);
 			ServerThread serverJavaGa= ServerThread.getInstance(ga);
 			Thread t = new Thread(serverJavaGa,"ThreadServer");
 			t.start();
 			CloseServer.envoyerMessageFermeture();
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	* Test of formants and FormantsSequence, if the method  works and if the exception are raised when needed
 	*
 	* @since 0.1
 	*
 	*/
 	public static void testFormantAndFormantSequence(){
 		//1st the formants
 		Formant f1= new Formant();
 		System.out.println("1 : must return all values at 0.0  =>    "+f1.toString());
 		f1.setAmplitude(0.1);
 		//f1.setBandwith(0.1);
 		f1.setFrequency(0.1);
 		System.out.println("2 : must return all values at 0.1  =>    "+f1.toString());
 		Formant f2 = new Formant(0.0, 0.2, 0.3);
 		System.out.println("3 : must return the values 0.0  0.2  0.3  =>    "+f2.toString());
 		Formant f3 = new Formant(0.0, 0.2, 0.3);
 		System.out.println();//space in the display
 		
 		//2nd the FormantSequence
 		FormantSequence fs1;
 		try {
 			fs1 = new FormantSequence(1);
 			System.out.println("4 : must return a sentence with 1 formant and 0.0 values");
 			fs1.displayFormantSequence();
 			System.out.println();//space in the display
 			
 			System.out.println("4.5 : must launch a exception for 0 size");
 			fs1 = new FormantSequence(0);
 			fs1.displayFormantSequence();
 			
 		} catch (FormantNumberexception e1) {
 			// TODO Auto-generated catch block
 			e1.display();
 		}
 		System.out.println();//space in the display
 		
 		try {
 			System.out.println("5 : must return a sentence with 1 formant and 0.1 values");
 			fs1 = new FormantSequence(1);
 			fs1.setFormantAt(0, new Formant(0.1,0.1,0.1));
 			fs1.displayFormantSequence();
 			System.out.println();//space in the display
 			
 			System.out.println("5.5 : must launch a exception for wrong index");
 			fs1.setFormantAt(1, new Formant(0.1,0.1,0.1));
 			fs1.displayFormantSequence();
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		
 		System.out.println();//space in the display
 		
 		System.out.println("6 : must return a sentence with 2 formant, 0.0 values and test as a name");
 		FormantSequence fs2 = new FormantSequence("test");
 		fs2.displayFormantSequence();
 		System.out.println("6.5 : must return a sentence with 2 formant, 0.0 values and default name");
 		fs2= new FormantSequence("");
 		fs2.displayFormantSequence();
 		System.out.println();//space in the display
 		
 		ArrayList<Formant> l= new ArrayList<Formant>();
 		l.add(f1);
 		l.add(f2);
 		l.add(f3);
 		try {
 			System.out.println("7 : must return a sentence with 3 formant, values from f1 f2 f3 and nomBidon name");
 			FormantSequence fs3 = new FormantSequence("nomBidon",3,l);
 			fs3.displayFormantSequence();
 			System.out.println();//space in the display
 			System.out.println("7.5 : must launch a exception for too much object in the list in parameter");
 			fs3 = new FormantSequence("nomBidon",2,l);
 			System.out.println();//space in the display
 			
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		try {
 			System.out.println("7.5.2 : must launch a exception for not enough box in the list");
 			FormantSequence fs3 = new FormantSequence("nomBidon",4,l);
 			System.out.println();//space in the display
 			
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		
 		try {
 			System.out.println("8 : must display a value");
 			FormantSequence fs3 = new FormantSequence("nomBidon",3,l);
 			System.out.println(fs3.getFormantAt(2).getAmplitude());
 			System.out.println();//space in the display
 			System.out.println("8.5 : must raise en exception for wrong index");
 			System.out.println(fs3.getFormantAt(3).getAmplitude());
 	
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		
 		try {
 			System.out.println("9 : must print a value");
 			fs1 = new FormantSequence(1);
 			fs1.setFormantAt(0, new Formant());
 			fs1.displayFormantSequence();
 			System.out.println();//space in the display
 			System.out.println("9.5 : must raise en exception for wrong index");
 			fs1.setFormantAt(1, new Formant());
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		
 		try {
 			System.out.println("10 : must raise a exception for empty list");
 			ArrayList<Formant> list = new ArrayList<Formant>();
 			fs1 = new FormantSequence("test",1,list);
 			System.out.println();//space in the display
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		
 	}
 	
 	/**
 	* Test of the methods from Sequence, if the method  works and if the exception are raised when needed
 	*
 	* @since 0.1
 	*
 	*/
 	public static void testSequence(){
 		
 		Sequence s1;
 		try {
 			System.out.println("1 : must print 0.0 0.0");
 			s1 = new Sequence(2);
 			s1.displaySeq();
 			System.out.println();//space in the display
 			System.out.println("1.5 : must raise an exception for 0 length");
 			s1 = new Sequence(0);
 		} catch (SequenceArrayException e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		double[] d={1.0,1.0};
 		try {
 			System.out.println("2 : must print 1.0 1.0");
 			s1= new Sequence(2, d);
 			s1.displaySeq();
 			System.out.println();//space in the display
 			System.out.println("2.5 : must raise an exception for wrong length");
 			s1= new Sequence(1, d);
 		} catch (SequenceArrayException e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		System.out.println();//space in the display
 		
 		try {
 			System.out.println("3 : must print 1.0");
 			s1= new Sequence(2, d);
 			System.out.println(s1.getValuesAt(1));
 			System.out.println("3.5 : must raise an exception for wrong index");
 			System.out.println(s1.getValuesAt(2));
 			
 		} catch (SequenceArrayException e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 		
 		try {
 			System.out.println("4 : must print 0.2 in 2nd position");
 			s1= new Sequence(2, d);
 			s1.setValues(1,0.2);
 			s1.displaySeq();
 			System.out.println("4.5 : must raise an exception for wrong index");
 			s1.setValues(2,0.2);
 			
 		} catch (SequenceArrayException e) {
 			// TODO Auto-generated catch block
 			e.display();
 		}
 	}
 	
 	/**
 	* Test of the methods from the GA. We can't see the result because there is no getter nor setter but we can see if
 	* it compile and if the instruction go in the good order.
 	* We can't use start() here case there is no praat instance nor java server to listen. It will be in the integration part.
 	*
 	* @since 0.1
 	*
 	*/
 	public static void testGA(){
 		GeneticAlgorithmCall ga;
 		try {
 			ga = new GeneticAlgorithmCall(10);
 			ga.generateAlphabet();
 			ga.buildTarget();
 			ga.createCandidateFactory();
 			ga.createEvolutionaryOperator();
 			ga.createFitnessEvalutator();
 			ga.createSelection();
 			ga.createRandomGenerator();
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	* Test of the SequenceFactory class and in particular the method to generateRandomSequence.
 	* As it is generated by random, we cant give value to compare.
 	* We can just see that the two sequence create are different but it is not the case all the time because of the random and the small alphabet.
 	* So if it happened, just call the method again.
 	*
 	* @since 0.1
 	*
 	*/
 	public static void testSequenceFactory(){
 		GlobalAlphabet glba=new GlobalAlphabet();
 		System.out.println("alphabet");
 		//glba.displayAllAlphabets();
 		SequenceFactory c = new SequenceFactory(glba, 20);
 		System.out.println("1st sequence");
 		Random rng=new MersenneTwisterRNG();
 		Sequence newSec1 = c.generateRandomCandidate(rng);
 		Sequence newSec2 = c.generateRandomCandidate(rng);
 		newSec1.displaySeq();
 		System.out.println("2 nd sequence");
 		newSec2.displaySeq();
 	}
 	
 	/**
 	* Test of the Mutation with SequenceMutation. Same as for testSequenceFactory(), it is generated by random so you can't have the same result twice.
 	* It is just a method to show that it works. Furthermore, there is a little probability of mutation (0.02), so nothing guarantee that the mutation will happened.
 	* If it is not the case, just reload the method.
 	* @since 0.1
 	*
 	*/
 	public static void testMutation(){
 		
 		Random rng=new MersenneTwisterRNG();
 		GlobalAlphabet glba=new GlobalAlphabet();
 		SequenceFactory c = new SequenceFactory(glba, 20);
 		SequenceCrossOver sqco= new SequenceCrossOver(); // 1 point cross over
 		System.out.println("parent 0 avant mutation");
 		Sequence newSec1 = c.generateRandomCandidate(rng);
 		newSec1.displaySeq();
 		System.out.println("parent 1 avant mutation");
 		Sequence newSec2 = c.generateRandomCandidate(rng);
 		newSec2.displaySeq();
 		SequenceMutation sqm= new SequenceMutation(glba,new Probability(0.02));
 		List<Sequence> l1= sqco.mate(newSec1, newSec2, 1, rng);
 		List<Sequence> l2=sqm.apply(l1, rng);
 		for(int i=0;i<l2.size();i++){
 			System.out.println("parent n "+i+" after mutation");
 			l2.get(i).displaySeq();
 		}
 	}
 	
 	/**
 	* Test of the Cross Over functions with SequenceCrossOver. Same as for two other above, there is no guarantee to se a difference each time,
 	* Don't hesitate to call again the method if it is the case.
 	* @since 0.1
 	*
 	*/
 	public static void testCrossOver(){
 		Random rng=new MersenneTwisterRNG();
 		GlobalAlphabet glba=new GlobalAlphabet();
 		SequenceFactory c = new SequenceFactory(glba, 20);
 		SequenceCrossOver sqco= new SequenceCrossOver(); // 1 point cross over
 		System.out.println("parent 0 before cross over");
 		Sequence newSec1 = c.generateRandomCandidate(rng);
 		newSec1.displaySeq();
 		System.out.println("parent 1 before cross over");
 		Sequence newSec2 = c.generateRandomCandidate(rng);
 		newSec2.displaySeq();
 		
 		List<Sequence> l1= sqco.mate(newSec1, newSec2, 1, rng);
 		for(int i=0;i<l1.size();i++){
 			System.out.println("parent n "+i+" after crossOver");
 			l1.get(i).displaySeq();
 		}
 		
 	}
 	
 	public static void testSequenceEvaluator(){
 		//GeneticAlgorithmCall ga;
 		/*try {
 			ga = new GeneticAlgorithmCall(10);
 			ga.buildTarget();
 			FormantSequence fs= new FormantSequence(2);
 			SequenceEvaluator seqeval=new SequenceEvaluator(fs,ga);
 			double res=seqeval.getFitness(fs, null);
 			System.out.println(res);
 		} catch (FormantNumberexception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		// cant be called without a Server and a Praat instance running, will be put in the integration part 
 	}
 	
 	/**
 	* Show if the praat scipt is correctly build
 	* @since 0.1
 	*
 	*/
 	public static void PraatSCript(){
 		try {
 			Sequence seq= new Sequence(13);
 			String script= MessageToPraat.writePraatScriptWithCandidates(seq);
 			System.out.println(script);
 		} catch (SequenceArrayException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (PraatScriptException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static void castFormantTest(){
 		String chaine = "4.0 8.0 3.0 5.0"; 
 		FormantSequence fs;
 		try {
 			System.out.println("1 : must print 4.0 8.0 3.0 5.0");
 			fs = MessageFromPraat.splitChaineToFormantSequence(chaine);
 			fs.displayFormantSequence();
 			System.out.println("2 : must print 0.0 0.0 0.0 0.0");
 			chaine = "4.0 8.0 3.0 --undefined--"; 
 			fs = MessageFromPraat.splitChaineToFormantSequence(chaine);
 			fs.displayFormantSequence();
 			System.out.println("3 : must print 0.0 0.0 0.0 0.0");
 			chaine = "FIN"; 
 			fs = MessageFromPraat.splitChaineToFormantSequence(chaine);
 			fs.displayFormantSequence();
 			System.out.println("4 : must raise an exception for not enought parameters");
 			chaine = "0.1 0.2 0.3"; 
 			fs = MessageFromPraat.splitChaineToFormantSequence(chaine);
 		} catch (CastFormantException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			e.display();
 		}
 		try{
 			System.out.println("5 : must raise an exception for not a double");
 			chaine = "0.1 0.2 0.3 toto"; 
 			fs = MessageFromPraat.splitChaineToFormantSequence(chaine);
 		}catch(CastFormantException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			e.display();
 		}
 	}
 	
 	public static void testGlobalAlphabet(){
 		GlobalAlphabet gla = new GlobalAlphabet();
 		gla.displayAllAlphabets();
 	}
 	//test des alphabets a mettre egalement
 	
 	
 	/**
 	 * test of the different operations from praat : open, header, close
 	 */
 	/*public static void praatLaunchHeaderClose(){
 			System.out.printf("tout d abord je suis ici : %s",Thread.currentThread().getName());   
 			try {
 				for(int i=0;i<10;i++){
 					OrderToPraat.launchPraat();
 					Thread.sleep(500);
 					OrderToPraat.closePraat();
 				}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		//OrderToPraat.reLaunchPraat();
 	}*/
 	
 	public static void testStatePattern(){
 		Praat p = new Praat(); //close
 		p.launch();
 		//p.reLaunch();
 	}
 	
 	public static void testGraph(FormantSequence target){
 		MonitoringCSV.displayCSV(target);
 	}
 	
 	public static void loadAllSounds(){
 		OrderToPraat.launchAllScripts();
 	}
 	
 	 private static class ClasseInterne{ // internal class for a main
 		 public static void main(String[] args){
 			 //UnitaryTestsGA.testFormantAndFormantSequence();
 			 //UnitaryTestsGA.testSequence();
 			 //UnitaryTestsGA.testPraat();
 			 //UnitaryTestsGA.testSequenceFactory();
 			 //UnitaryTestsGA.testCrossOver();
 			 //UnitaryTestsGA.testMutation();
 			 //UnitaryTestsGA.PraatSCript();
 			 //UnitaryTestsGA.castFormantTest();
 			 //praatLaunchHeaderClose();
 			 //testStatePattern();
 			 testGraph(new FormantSequence("e"));
			 //loadAllSounds();
 		 }
 		 
 	 }
 	
 }
