 package com.seitenbau.earead;
 
 import org.sparx.Attribute;
 import org.sparx.Collection;
 import org.sparx.Element;
 import org.sparx.Package;
 import org.sparx.Repository;
 
 import com.seitenbau.earead.EaReader;
 
 public class EaReaderTest {
 
 	/**
 	 * WICHTIG: VM arguments in der Debug configuration setzen:
 	 * 
 	 * -Djava.library.path=C:\Projekte\sb-uml-parser\lib
 	 */
 	public static void main(String[] argv) throws Exception {
		String eapFilePath = "./import/import.eap";
 
 		Repository repository = EaReader.openRepository(eapFilePath);
 		Collection<Package> models = repository.GetModels();
 		for (Package packageModel : models) {
 			printPackage(packageModel, "");
 		}
 		repository.Exit();
 	}
 
 	private static void printPackage(Package packageToPrint, String indent) {
 		System.out.println(indent + packageToPrint.GetName());
 		for (Element element : packageToPrint.GetElements()) {
 			System.out.println(indent + "- " + element.GetType() + ":"
 					+ element.GetName());
 			for (Attribute attribute : element.GetAttributes()) {
 				System.out.println(indent + "- - " + attribute.GetType() + ":"
 						+ attribute.GetName());
 			}
 		}
 		for (Package child : packageToPrint.GetPackages()) {
 			printPackage(child, "  " + indent);
 		}
 	}
 
 }
