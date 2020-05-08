 package br.com.caio.blackjack.util;
 
 import javax.swing.JOptionPane;
 
 import br.com.caio.blackjack.model.Baralho;
 import br.com.caio.blackjack.model.Mao;
 
 public class ProcessadorDeAcoes {
 	
 	private Baralho baralho;
 	private Mao player;
 	private Mao computador;
 	
 	public Mao getComputador() {
 		return computador;
 	}
 
 	public void setComputador(Mao computador) {
 		this.computador = computador;
 	}
 
 	public Baralho getBaralho() {
 		return baralho;
 	}
 
 	public void setBaralho(Baralho baralho) {
 		this.baralho = baralho;
 	}
 
 	public Mao getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Mao player) {
 		this.player = player;
 	}
 
 	public ProcessadorDeAcoes() {
 		MontaBaralho montaBaralho = new MontaBaralho();
 		baralho = montaBaralho.buildBaralho();
 		player = new Mao();
 		computador = new Mao();
 		player.getCartas().add(baralho.getCarta());
 		player.getCartas().add(baralho.getCarta());
 		
 	}
 	
 	public void processaContinuar(){
 		player.getCartas().add(baralho.getCarta());
 		if(player.getValorTotal()>21){
 			JOptionPane.showMessageDialog(null, "Voce Estourou !");
 		}
 	}
 	public void processaParar(){
 		computador.getCartas().add(baralho.getCarta());
 		computador.getCartas().add(baralho.getCarta());
		while(processaJogadas()){
			computador.getCartas().add(baralho.getCarta());
		}
 	}
 	
 	
 	public boolean processaJogadas(){
		if((computador.getValorTotal()<19&&player.getValorTotal()<=21) || (computador.getValorTotal()<player.getValorTotal())&&player.getValorTotal()<=21){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public String getWinner(){
 		if(computador.getValorTotal()>21){
 			return computadorEstoura();
 		}
 		else{
 			if(player.getValorTotal()>21){
 				return playerEstoura();
 			}
 			else{
 				return comparaResultadosNormais();
 			}
 		}
 		
 	}
 	
 	private String comparaResultadosNormais(){
 		if(computador.getValorTotal()==player.getValorTotal()){
 			return "Empate";
 		}
 		else{
 			if(computador.getValorTotal()>player.getValorTotal()){
 				return "Computador Ganhou !";
 			}
 			else{
 				return "Jogador Ganhou !";
 			}
 			
 		}
 	}
 	private String playerEstoura() {
 		if(computador.getValorTotal()<=21){
 			return "Computador Ganhou";
 		}
 		else{
 			return "Empate";
 		}
 	}
 
 	private String computadorEstoura(){
 		if (player.getValorTotal()<=21) {
 			return "Jogador ganhou !";
 		}
 		else{
 			return "Empate !";
 		}
 	}
 	
 }
