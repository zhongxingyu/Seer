 /**
  * 
  */
 package game.entidade;
 
 
 
 import java.util.Scanner;
 import java.util.Random;
 import game.interfaces.*;
 
 
 /**
  * @author Bruno
  *
  */
 public class BatalhaTurno /*implements SetandoBonus*/{
 	
 	private Random randomica = new Random(System.currentTimeMillis());
 	Scanner scanIn = new Scanner(System.in);
 	public boolean resultado;
 	
 	
 
 	/**
 	 * 
 	 */
 	public BatalhaTurno() {
 		this.resultado = false;
 	}
 	
 	
 	public boolean isResultado() {
 		return resultado;
 	}
 
 	public void setResultado(boolean resultado) {
 		this.resultado = resultado;
 	}
 	
 	
 	public Player batalha(Player player, InimigoEvento inimigo) throws NumberFormatException{
 		
 		
 		Player player1 = player;
 		InimigoEvento inimigo1 = new InimigoEvento();
 		
 		
 		int x = 0, y=0;
 		int atkP, defP, hpP, escudoP;
 		int atkI, defI, hpI, escudoI;
 		int dano;
 		int resultB = 1;
 		int speedP, speedI;
 		int novaxp;
 		Item item;
 		int velholvl = player.lvl, novolvl;
 		
 		 
 		int dadospeedI = randomica.nextInt(10);
 		int dadospeedP = randomica.nextInt(10);
 		
 		inimigo1 = inimigo;
 		
 		Loot lootB = inimigo1.getLoot();
 		
 	
 		
 		atkP = player1.getAtk();
 		defP = player1.getDef();
 		hpP = player1.getHp();
 		atkI = inimigo1.getAtk();
 		defI = inimigo1.getDef();
 		hpI = inimigo1.getHp();
 		escudoP = 2*defP;
 		escudoI = 2*defI;
 		
 		speedP = player1.getSpeed() +dadospeedP;
 		speedI = inimigo1.getSpe() + dadospeedI;
 		
 		
 		
 		System.out.println("Voce encontrou um " + inimigo1.getTipo() + " lvl: " + inimigo1.getLvl() + "\n");
 		
 		System.out.println("speed inimigo : " + speedI + " speed player: " + speedP  + "\n");
 		
 		// TODO Player Ataca Primeiro
 		if(speedP>speedI){
 			
 			System.out.println(player.getNome() + "- lvl:"+ player1.getLvl() + " hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 			System.out.println(inimigo.getTipo() + "- lvl:"+ inimigo1.getLvl() + " hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI );
 			
 			do{
 				int numeroGerado;
 				
 				// TODO verificao para evitar que inimigo fique atacando o escudo do player depois que chegar a 0
 				
 				if (escudoP==0){
 					numeroGerado = randomica.nextInt(6) + 1;
 				}else{
 					numeroGerado = randomica.nextInt(9) + 1;
 				}
 				
 				//ao player
 				
 				System.out.println("voce : 1/ataca 2/ ataca escudo 3/defende 4/come(recupera hp):");
 				x = scanIn.nextInt();
 				
 				
 				try{
 					
 					if (x>4|x<1){
 						throw new java.lang.Exception();
 					}
 				}catch (Exception e){
 						do {
 							System.err.println("Entrada invalida!!!");
 							System.out.println("voce : 1/ataca 2/ ataca escudo 3/defende 4/come(recupera hp):");
 							x = scanIn.nextInt();
 						}while(x>4|x<1);
 				}
 				
 				
 				
 				
 				switch(x){
 				case 1:
 					//player atacando  diretamente
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					
 						e.printStackTrace();
 					}
 					//verificacao se inimigo esta defendendo
 					if(y == 1 | y == 2 | y == 0){
 						//calculando dano
 						dano = atkP - (escudoI/2);
 						//verificando se o dano eh minimo (evitando dano negativo)
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano do hp
 						hpI -= dano;
 						//verificacao para evitar hp negativo
 						if(hpI<0){
 							hpI=0;
 						}
 						//imprimindo dano causado
 						System.out.println("hp inimigo : -" + dano + "\n");
 					
 					}else if(y==3){
 						
 						//calculando dano
 						dano = atkP - escudoI;
 						//verificando se o dano eh minimo (evitando dano negativo)
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano do hp
 						hpI -= dano;
 						//verificacao para evitar hp negativo
 						if(hpI<0){
 							hpI=0;
 						}
 						//imprimindo dano causado
 						System.out.println("hp inimigo : -" + dano + "\n");
 					}
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 2:
 					//player atacando escudo inimigo
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP);
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//verificando se inimigo esta defendendo
 					if(y==0 | y ==1 | y ==2){
 						//calculando e subtraindo dano no escudo
 						escudoI = escudoI - (atkP/2);
 						//imprimindo dano no escudo
 						System.out.println("dano escudo inimigo : -" + atkP/2 + "\n");
 						
 					}else if (y==3){
 						//calculando e subtraindo dano no escudo
 						escudoI = escudoI -(atkP/4);
 						//imprimindo dano no escudo
 						System.out.println("dano escudo inimigo : -" + atkP/4 + "\n");
 					}
 					//verificacao para evitar escudo negativo
 					if (escudoI < 0){
 						escudoI = 0;
 					}
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP);
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 3:
 					//player defendendo
 					
 					if (y==1){
 						System.out.println("DANO OBSORVIDO");
 					}
 					//delay
 					
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 4:
 					int numeroDoAlimentoSelecionado = -1;
 					
 					
 					
 					if ( player.getMochila().size() >0){
 						
 						player.listarMochila();
 						System.out.println("voce deseja comer qual item?(insira apenas o numero que esta antes do nome do item)");
 						numeroDoAlimentoSelecionado = scanIn.nextInt();
						
 						player.verificarItem(player.getMochila().get(numeroDoAlimentoSelecionado));
 						player.getMochila().remove(numeroDoAlimentoSelecionado);
 					}else{
 						System.out.println("mochila vazia");
 					}
 					
 					break;
 				
 				}
 				
 				// TODO verificando se inimigo ainda tem hp
 				if (hpI <= 0){
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					// definindo resultado da batalha para o retorno
 					this.resultado = true;
 					
 					resultB = 2;
 					//imprimindo resultado
 					System.out.println("inimigo abatido\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					//listando loot
 					lootB.listarLoot();
 					
 					player1.addXp(inimigo1.getXp());
 					
 					/**								
 					novaxp = player1.getXp() + inimigo1.getXp();
 					
 					if(player1.getXp() > player1.getXpMax()){
 						System.out.println("LVL UP!!!!!!!\n");
 						novaxp = novaxp - player1.getXpMax();
 						player1.setLvl((player1.getLvl()+1));
 						player1.setXp(novaxp);
 					}else{
 						player1.setXp(novaxp);
 					}
 					*/
 					System.out.println("\nXp: " +player1.getXp()+"/"+player1.getXpMax());
 					
 					break;
 				}
 				
 				
 				// TODO ao inimigo.
 				
 				switch(numeroGerado){
 				case 1:
 				case 2:
 				case 3:
 					//atacando
 					y=1;
 					break;
 				case 4:
 				case 5:
 				case 6:
 					//defendendo
 					y=3;
 					break;
 				case 7:
 				case 8:
 				case 9:
 					//atacando escudo
 					y=2;
 					break;
 						
 				}
 				
 				switch(y){
 				case 1:
 					//atacando
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println("atk" + "\n");
 					//verificando se player esta defendendo
 					if(x==1 | x == 2 | x == 4){
 						//calculando dano
 						dano = atkI - (escudoP/2);
 						//verificacao para evitar dano negativo(e causar dano minimo)
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano do hp do player
 						hpP -= dano;
 						//verificacao para evitar que hp seja negativo
 						
 						if(hpP<0){
 							hpP=0;
 						}
 						//imprimindo dano
 						System.out.println("hp PLAYER : -" + dano + "\n");
 						
 					}else if(x==3){
 						//calculando dano
 						dano = atkI - escudoP;
 						//verificacao para evitar dano negativo(e causar dano minimo)
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano do hp do player
 						hpP -= dano;
 						//verificacao para evitar que hp seja negativo
 						if(hpP<0){
 							hpP=0;
 						}
 						//imprimindo dano
 						System.out.println("hp PLAYER : -" + dano + "\n");
 					}
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP + "\n");
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 2:
 					//atacando escudo
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					//imprimindo acao de inimigo para player
 					System.out.println("ataca escudo\n");
 					//verificando se player esta dedendendo
 					if(x==1 | x == 2 | x == 4){
 						//calculando dano em escudo player
 						escudoP = escudoP - (atkI/2);
 						//imprimindo dano no escudo player
 						System.out.println("dano escudo PLAYER : -" + atkI/2 + "\n");
 					}else{
 						//calculando dano ecudo player
 						escudoP = escudoP -(atkI/4);
 						//imprimindo dano escudo player
 						System.out.println("dano escudo PLAYER : -" + atkI/4 + "\n");
 					}
 					//verificacao para evitar que escudo fique negativo
 					if (escudoP<0){
 						escudoP = 0;
 					}
 					
 					
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 3:
 					//defendendo
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//imprimindo acao inimigo
 					System.out.println("def" + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//verificando se inimigo esta atacando
 					if (y==1){
 						System.out.println("DANO OBSORVIDO" + "\n");
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 			
 					default:
 						System.out.println("entrada invalida" + "\n");
 						
 				}
 				//verificacao do hp de player para avisar caso esteja abaixo da metade
 				if(hpP < player1.getHp()/2& hpP != 0){
 					System.out.println("ATENO:  HP BAIXO" + "\n");
 				}
 				
 				// TODO verificando se player continua vivo
 				if (hpP <= 0){
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					// definindo resultado da batalha para o retorno
 					this.resultado = false;
 					resultB = 3;
 					//imprimindo resultado
 					System.out.println("GAME OVER!" + "\n");
 					
 				}
 				
 			}while(resultB == 1);
 			
 			
 			
 			
 			
 			
 			
 			
 			
 		// TODO Inimigo Ataca primeiro
 		}else{
 			do{
 				int numeroGerado;
 				
 				//verificacao para evitar que inimigo ataque escudo  do player caso esteja em 0
 				if (escudoP==0){
 					numeroGerado = randomica.nextInt(6) + 1;
 				}else{
 					numeroGerado = randomica.nextInt(9) + 1;
 				}
 				
 				
 				
 				//ao inimigo.
 				
 				switch(numeroGerado){
 				case 1:
 				case 2:
 				case 3:
 					//atacando
 					y=1;
 					break;
 				case 4:
 				case 5:
 				case 6:
 					//defendendo
 					y=3;
 					break;
 				case 7:
 				case 8:
 				case 9:
 					//atacando escudo
 					y=2;
 					break;
 						
 				}
 				
 				switch(y){
 				case 1:
 					//atacando
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getNome() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//imprimindo acao inimigo
 					System.out.println("atk" + "\n");
 					if (x==0 | x==1 | x==2 | x==4){
 						//calculando dano
 						dano = atkI - (escudoP/2);
 						//verificacao para evitar dano negativo
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano de hp de player
 						hpP -= dano;
 						//verificacao para evitar hp negativo
 						if(hpP<0){
 							hpP=0;
 						}
 						//imprimindo dano em player
 						System.out.println("hp PLAYER : -" + dano + "\n");
 					
 					}else if(x==3){
 						//calculando dano 
 						dano = atkI - escudoP;
 						//verificacao para evitar dano negativo
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano de hp Player
 						hpP -= dano;
 						
 						//verificacao para evitar hp negativo
 						if(hpP<0){
 							hpP=0;
 						}
 						//imprimindo dano causado
 						System.out.println("hp PLAYER : -" + dano + "\n");
 					}
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 2:
 					//atacando escudo
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP);
 					System.out.println(inimigo.getNome() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println("ataca escudo" + "\n");
 					//verificando se player esta defendendo
 					if(x==0 | x==1 | x==2 | x==4){
 						//calculando dano em escudo
 						escudoP = escudoP - (atkI/2);
 						//imprimindo dano causado a escudo player
 						System.out.println("dano escudo PLAYER : -" + atkI/2 + "\n");
 					}else{
 						//calculando dano em escudo Player
 						escudoP = escudoP -(atkI/4);
 						//imprimindo dano causado a escudo
 						System.out.println("dano escudo PLAYER : -" + atkI/4 + "\n");
 					}
 					//verificacao para evitar escudo negativo
 					if (escudoP<0){
 						escudoP = 0;
 					}
 					
 					
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 3:
 					//defendendo
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println("def" + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					if (y==1){
 						System.out.println("DANO OBSORVIDO" + "\n");
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				default:
 					System.out.println("entrada invalida" + "\n");
 					
 			
 					
 				}
 				
 				
 				// TODO verificacao se player continua vivo
 				if (hpP <= 0){
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					this.resultado = false;
 					resultB = 3;
 					System.out.println("GAME OVER!" + "\n");
 					
 					break;
 					
 				}
 				//verificacao se o hp de player esta abaixo da metade
 				if(hpP < player1.getHp()/2 & hpP != 0){
 					System.out.println("ATENO:  HP BAIXO" + "");
 				}
 				
 				
 				// TODO ao player
 				
 				System.out.println("voce : 1/ataca 2/ ataca escudo 3/defende 4/come(recupera hp):" + "\n");
 				x = scanIn.nextInt();
 				
 				try{
 					if (x>4|x<1){
 						throw new java.lang.Exception();
 					}
 				}catch (Exception e){
 						do {
 							System.err.println("Entrada invalida!!!");
 							System.out.println("voce : 1/ataca 2/ ataca escudo 3/defende 4/come(recupera hp):");
 							x = scanIn.nextInt();
 						}while(x>4|x<1);
 				}
 				
 				switch(x){
 				case 1:
 					//atacando
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//verificacoes se inimigo esta defendendo
 					if(y==0 | y==1 | y==2){
 						//calculando dano
 						dano = atkP - (escudoI/2);
 						//verificando se dano nao esta negativo e atribuindo dano minimo
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano de hp inimigo
 						hpI -= dano;
 						//verificacao para evitar hp negativo
 						if(hpI<0){
 							hpI=0;
 						}
 						//imprimindo dano causado
 						System.out.println("hp inimigo : -" + dano + "\n");
 					}else if(y==3){
 						//calculando dano
 						dano = atkP - escudoI;
 						//cerificando se dano esta negativo e atribuindo dano minimo
 						if(dano<1){
 							dano = 1;
 						}
 						//subtraindo dano de hp inimigo
 						hpI -= dano;
 						//verificacao para evitar hp negativo
 						if(hpI<0){
 							hpI=0;
 						}
 						//imprimindo dano
 						System.out.println("hp inimigo : -" + dano + "\n");
 					}
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 2:
 					//atacando escudo
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//verificando se inimigo esta atacando
 					if(y==0| y==1|y==2){
 						//calculando e subtraindo dano no escudo inimigo
 						escudoI = escudoI - (atkP/2);
 						//imprimindo dano
 						System.out.println("dano escudo inimigo : -" + atkP/2 + "\n");
 					}else if (y==3){
 						//calculando e subtraindo dano no escudo inimigo
 						escudoI = escudoI -(atkP/4);
 						//imprimindo dano
 						System.out.println("dano escudo inimigo : -" + atkP/4 + "\n");
 					}
 					//verificacao para evitar escudo ficar negativo
 					if (escudoI < 0){
 						escudoI = 0;
 					}
 					
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 3:
 					//defendendo
 					
 					if (y==1){
 						System.out.println("DANO OBSORVIDO" + "\n");
 					}
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					System.out.println(player.getNome() +" - hp: "+ hpP +" dano: " + atkP + " escudo: " + escudoP );
 					System.out.println(inimigo.getTipo() +" - hp: "+ hpI +" dano: " + atkI + " escudo: " + escudoI + "\n");
 					break;
 					
 				case 4:
 					int numeroDoAlimentoSelecionado = -1;
 					
 					
 					
 					if ( player.getMochila().size() >0){
 						
 						player.listarMochila();
 						System.out.println("voce deseja comer qual item?(insira apenas o numero que esta antes do nome do item)");
 						numeroDoAlimentoSelecionado = scanIn.nextInt();
						
 						player.verificarItem(player.getMochila().get(numeroDoAlimentoSelecionado));
 						player.getMochila().remove(numeroDoAlimentoSelecionado);
 					}else{
 						System.out.println("mochila vazia");
 					}
 					
 					break;
 					
 				default:
 					System.out.println("entrada invalida" + "\n");
 			
 				
 				}
 				
 				// TODO verificando se inimigo foi abatido
 				if (hpI <= 0){
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//setando resultado da batalha para o return
 					this.resultado = true;
 					resultB = 2;
 					//imprimindo resultado
 					System.out.println("inimigo abatido" + "\n");
 					//delay
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 					//listando loot
 					lootB.listarLoot();
 
 					player1.addXp(inimigo1.getXp());
 					
 					/**								
 					novaxp = player1.getXp() + inimigo1.getXp();
 					
 					if(player1.getXp() > player1.getXpMax()){
 						System.out.println("LVL UP!!!!!!!\n");
 						novaxp = novaxp - player1.getXpMax();
 						player1.setLvl((player1.getLvl()+1));
 						player1.setXp(novaxp);
 					}else{
 						player1.setXp(novaxp);
 					}
 					*/
 					System.out.println("\nXp: " +player1.getXp()+"/"+player1.getXpMax());
 				}
 				
 				
 				
 				continue;
 				
 				
 			}while(resultB == 1);
 		}
 		
 		novolvl = player.getLvl();
 		
 		if(velholvl == novolvl){
 			player1.setHp(hpP);
 		}
 		int verificaPegarLoot;
 		
 		if(hpP > 0){
 			System.out.println("\nDeseja pegar Item? 1/sim 2/ nao");
 			verificaPegarLoot = scanIn.nextInt();
 			if(verificaPegarLoot == 1){
 				player.adicionaItem(lootB.getItem());
 			}
 		}
 		//retornando resultado da batalha
 		return player1;
 	}
 	
 	
 	
 }
