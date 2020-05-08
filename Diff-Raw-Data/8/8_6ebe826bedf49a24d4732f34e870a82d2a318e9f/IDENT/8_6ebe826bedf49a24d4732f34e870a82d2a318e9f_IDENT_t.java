 public class IDENT {
 
 	public static void  main(String arg []) throws IDENTException {
		try{
			Calculation calc = new Calculation("1.txt", 3);
		}catch (Exception e) {
			// TODO: handle exception
			throw new IDENTException ();
		}
		
 	}
 
 }
