 package br.com.lino.binario;
 
 public class Soma {
 
	String soma = "";
	int vaiUm = 0;
 
 	public String calcula(String valor1, String valor2) {
 
 		String maiorValor = valor1.length() > valor2.length() ? valor1 : valor2;
 		String menorValor = valor1.length() > valor2.length() ? valor2 : valor1;
 		int min = menorValor.length();
 		int max = maiorValor.length();
 
 		for (int i = min - 1; i >= 0; i--) {
 			soma(menorValor.charAt(i), maiorValor.charAt((max - min) + i));
 		}
 
 		for (int i = max - min - 1; i >= 0; i--) {
 			soma(maiorValor.charAt(i));
 		}
 
 		if (vaiUm == 1) {
 			soma = vaiUm + soma;
 		}
 
 		return soma;
 	}
 
 	private void soma(char... valores) {
 		int aux = somaValores(valores) + vaiUm;
 
 		if (aux == 3) {
 			soma = "1".concat(soma);
 		} else if (aux == 2) {
 			soma = "0".concat(soma);
 			vaiUm = 1;
 		} else if (aux == 1) {
 			soma = "1".concat(soma);
 			vaiUm = 0;
 		} else {
 			soma = "0".concat(soma);
 		}
 
 	}
 
 	private int somaValores(char... valores) {
 		int aux = 0;
 		for (char i : valores) {
 			aux = aux + Integer.parseInt(String.valueOf(i));
 		}
 
 		return aux;
 	}
 }
