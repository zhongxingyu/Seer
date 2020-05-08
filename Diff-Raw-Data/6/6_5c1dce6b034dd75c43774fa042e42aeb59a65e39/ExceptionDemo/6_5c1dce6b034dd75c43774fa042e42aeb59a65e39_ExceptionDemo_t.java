 package fi.oletus;
 
 /* This file is released to the public domain */
 
 public class ExceptionDemo {
 	
 	public static int[] virhe() throws ArrayIndexOutOfBoundsException, ArithmeticException
 	{
 		int[] a = new int[10];
 		if (Math.random() < 0.5)
 		{
 			a[10] = 0;
 		}
 		else {
 			int b = 0;
 			int c = 5 / b;
 		}
 		return a;
 	}
 	
 	public static void toimiva()
 	{
 		System.out.println("Success!");
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		/* TODO: Lisää sopivaan kohtaan poikkeuskäsittelijät 
		 * (try-catch) virhe-funktion heittämille poikkeuksille,
		 * jotka tulostavat virheviestin ja tämän jälkeen jatkavat ohjelman toimintaa. */
 		
 		int[] b = null;
 		
 		b = virhe();
 		
 		System.out.println(b.length);
 
 		toimiva();
 	}
 
 }
