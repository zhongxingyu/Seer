 package asrsController;
 
 import order.Product;
 
 public interface BinPacking {
	public abstract void sentToBin(Byte binNummer);
	
 	public abstract void packProduct(Byte binNummer, Product product);
 }
