 import java.util.ArrayList;
 
 /**
  * Agencia
  * 
  * @author gabriel
  */
 public class Agencia { 
 	
 /**
  * Nmero mximo de contas
  */
 	private static byte MAX_CONTAS = 20;
 	
 /**
  * Lista das contas da agncia
  */
 	public static ArrayList<Conta> contas = new ArrayList<Conta>();
 	
 /**
  * Contrutor da classe agncia
  */
 	public Agencia() {}
 	
 /**
  * Cria conta
  * 
  * @param numero
  * @param proprietario
  * @param saldo
  * @return
  */
 	public static String criarConta(int numero, String proprietario, float saldo) {
 		if (contas.size() >= MAX_CONTAS) {
 			return "Numero maximo de contas ja cadastrado no sistema.";
 		}
 		
		for (int i = 0; i <= contas.size(); i++) {
 			if (contas.get(i).getNumero() == numero) {
 				return "Ja existe uma conta com esse numero";
 			}
 		}
 			
 		if (saldo < 0 ) {
 			return "Impossivel cadastrar valor negativo como saldo.";
 		}
 		
 		contas.add(new Conta(numero, proprietario, saldo));
 		
 		return "Conta cadastrada com sucesso.";
 	}	
 /**
  * Cancela conta
  * 
  * @param numero
  * @return
  */
 	public static String cancelarConta(int numero) {
		for (int i = 0; i <= contas.size(); i++) {
 			if (contas.get(i).getNumero() == numero) {
 				contas.remove(i);
 				return "Conta cancelada com sucesso.";
 			}
 		}
 		
 		return "Conta inexistente.";
 	}
 	
 }
