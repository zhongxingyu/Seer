 
 public class Calculadora {
 	private int numero1,numero2;
 	
 	public Calculadora(int numero1, int numero2)
 	{
 		this.numero1=numero1;
 		this.numero2=numero2;
 	}
 	
 	//-----------------------------------------------
 	
 	//Getters y Setters 
 	public int getNumero1() {
 		return numero1;
 	}
 
 	public void setNumero1(int numero1) {
 		this.numero1 = numero1;
 	}
 
 	public int getNumero2() {
 		return numero2;
 	}
 
 	public void setNumero2(int numero2) {
 		this.numero2 = numero2;
 	}
 	
 	//------------------------------------------------
 	
 	//metodo Sumar; Devuelve el resultado de la suma de los numeros
 	public int sumar()
 	{
 		int resultado=numero1+numero2;
 		return resultado;
 	}
 	
 	//------------------------------------------------
 	
 	//metodo restar; Devuelve el resultado de la resta de los numeros
 	public int restar()
 	{
 		int resultado=numero1-numero2;
 		return resultado;
 	}
 	
 	//------------------------------------------------
 	
 	public int dividir()
 	{
 		int resultado=numero1/numero2;
 		return resultado;
 	}
 	}
 	
 }
