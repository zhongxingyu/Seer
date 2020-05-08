 package tests;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import easyaccept.EasyAcceptFacade;
 import facade.easyAccept.MyEasyAcceptFacade;
 
 public class EasyAcceptTest {
 
 	public static void main(String[] args) {
 		List<String> files = new ArrayList<String>();
 
 		files.add("us/US01.txt");
 		files.add("us/US02.txt");
 		files.add("us/US03.txt");
 		files.add("us/US04.txt");
 		files.add("us/US05.txt");
 		files.add("us/US06.txt");
 		files.add("us/US07.txt");
 		files.add("us/US08.txt");// ultima US da segunda entrega =p
 		// files.add("us/US09.txt");
 		// files.add("us/US10.txt");
 		// files.add("us/US11.txt");
 
 		// Instanciate aplication Facade
 		MyEasyAcceptFacade myTestFacade = new MyEasyAcceptFacade();
 
 		// Instanciate EasyAcceptFacade
 		EasyAcceptFacade eaFacade = new EasyAcceptFacade(myTestFacade, files);
 
 		// Execute USs
 		eaFacade.executeTests();
 
 		// Print the results
 		System.out.println(eaFacade.getCompleteResults());
 	}
 
 }
