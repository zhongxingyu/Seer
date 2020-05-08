 package game.player;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import manager.MapManager;
 
 import window.AppliWindow;
 
 import commands.market.UpgradeTower;
 import commands.selection.Move;
 import commands.selection.SelectBase;
 
 import engine.Engine;
 import game.Game;
 import game.base.Base;
 import game.tower.Tower;
 
 
 public class IAPlayer extends Player {
 	
 	private int difficulty;
 	
 	public int getDifficulty() {
 		return this.difficulty;
 	}
 	
 	@Override
 	public void run() {
 		
 		// IA : chose randomly an action to do
 		Random rand = new Random();
 		rand.setSeed(System.currentTimeMillis());
 		
 		while ((this.getIsDead() == false) || (Game.getInstance().isRunning() == true)) {
 			
 			int value = (int)(Math.random() * 15);
 			
 			switch(value) {
 			
 			case 1:
 			case 6:
 			case 7:
 				ArrayList<Tower> listTemp = new ArrayList<Tower>();
 				if(Game.getInstance().getTowerManager().getTowers() != null) {
 					for(Tower t : Game.getInstance().getTowerManager().getTowers()) {
 						if(this.equals(t.getOwner())) {
 							listTemp.add(t);
 						}
 					}
					if( listTemp.size() <= 0) {
						break;
					}
 					if(listTemp != null) {
 						int idTowerRand = rand.nextInt(listTemp.size());
 						
 						// Commande pour upgrader une tour choisi aléatoire.
 						UpgradeTower commandUp = new UpgradeTower(listTemp.get(idTowerRand));
 						Engine.getInstance().getCommands().add(commandUp);
 						System.out.println("Ce player : "+this.getName()+" a upgradé une tourelle !");
 					}
 				}
				listTemp.clear();
 				break;
 			case 8:
 			case 9:
 			case 2:
 				// Je veux poser une tour sur une de mes zones : je regarde et retiens tous les endroits disponibles
 				
 				LinkedList<Integer[]> availableAreas = new LinkedList<Integer[]>();
 				MapManager mapManager = Game.getInstance().getMapManager();
 				
 				for(int j=0; j < mapManager.getHeightMap(); ++j) {
 					for(int i=0; i < mapManager.getHeightMap(); ++i) {
 						
 						int numArea = mapManager.getMap()[i][j];
 						
 						if(numArea >= 0 && numArea < Game.getInstance().getBaseManager().getBases().size()) {
 							Base baseArea = Game.getInstance().getBaseManager().getBases().get(numArea);
 							// si la zone est disponible pour moi je l'ajoute à la liste
 							if (this.equals(baseArea.getPlayer())) {
 								Integer[] position = {i, j};
 								availableAreas.add(position);
 							}
 						}
 					}
 				}
 				
 				// si on a trouvé au moins une zone disponible on en prend une au hasard et on construit la base dessus (si l'argent le permet)
 				if (availableAreas.size() != 0) {
 					int randIndex = rand.nextInt(availableAreas.size());
 					int randX = (availableAreas.get(randIndex)[0] * AppliWindow.getInstance().getWidth()) / mapManager.getWidthMap();
 					int randY = (availableAreas.get(randIndex)[1] * AppliWindow.getInstance().getHeight()) / mapManager.getHeightMap();
 					
 					this.buyTower(this, "GunTower", randX, randY);
 				}
 				
 				break;
 			case 3: // -------------------------------------------------------------------------------------------
 				// Si je n'ai pas de base selected :
 				if(this.getSelectedBases() == null) {
 					//On crée la liste de toutes les bases de l'IA au moment présent
 					ArrayList<Base> iaBases = new ArrayList<Base>();
 					// Seulement ses bases
 					for(Base b : Game.getInstance().getBaseManager().getBases()) {
 						if(b.getPlayer() != null) {
 							if(b.getPlayer().getName() == this.getName()) {
 								iaBases.add(b);
 							}
 						}
 					}
 					// On tire un nombre entier aléatoire entre 0 et le total de ses bases 
 					if( iaBases.size() <= 0) {
 						break;
 					} else {
 						int nb = rand.nextInt(iaBases.size());
 						Base base1 = iaBases.get(nb);
 						//On ajoute la commande dans la file de commande
 						SelectBase command1 = new SelectBase(this, base1);
 						Engine.getInstance().getCommands().add(command1);
 					}
 				}
 				// Si j'ai déjà une base selected :
 				else {
 					doRandomAction("a déjà sa base prête pour déplacer ses troupes, il se tourne les pouces !");
 				}
 				break; // ------------------------------------------------------------------------------------------
 			
 			case 4: // ---------------------------------------------------------------------------------------------
 				// Je veux faire un déplacement
 				
 				//On crée la liste de toutes les bases de l'IA au moment présent
 				ArrayList<Base> iaBases = new ArrayList<Base>();
 				// Seulement ses bases
 				for(Base b : Game.getInstance().getBaseManager().getBases()) {
 					if(b.getPlayer() != null) {
 						if(b.getPlayer().getName() == this.getName()) {
 							iaBases.add(b);
 						}
 						// On retire celle déjà sélectionnée (même s'il y a un test plus tard)
 						if(b.getPlayer().getSelectedBases() == b) {
 							iaBases.remove(b);
 						}
 					}
 				}
 				// On tire un nombre entier aléatoire entre 0 et le total de ses bases 
 				if( iaBases.size() <= 0) {
 					break;
 				} else {
 					int nb = rand.nextInt(iaBases.size());
 					Base base1 = iaBases.get(nb);
 					
 					// Si j'ai déjà un base sélectionnée, je fais mon déplacement
 					if(this.getSelectedBases() != null) {
 						//On ajoute la commande Move dans la file de commande
 						Move command1 = new Move(this, this.getSelectedBases(), base1);
 						Engine.getInstance().getCommands().add(command1);
 					}
 					// Si je n'ai pas de base sélectionnée, je selectionne ma base
 					else {
 						//On ajoute la commande SelectHisBase dans la file de commande
 						SelectBase command1 = new SelectBase(this, base1);
 						Engine.getInstance().getCommands().add(command1);
 					}
 				}
 				break; // ------------------------------------------------------------------------------------------
 			
 			case 5: // ---------------------------------------------------------------------------------------------
 				// Je veux faire une attaque
 				// Si j'ai déjà une base selectionnée, je choisi une base ennemi et j'attaque
 				if(this.getSelectedBases() != null) {
 					//On crée la liste de toutes les bases ennemi à l'IA au moment présent
 					ArrayList<Base> ennemiBases = new ArrayList<Base>();
 					// Seulement les bases ennemi mais aussi les bases neutres
 					for(Base b : Game.getInstance().getBaseManager().getBases()) {
 						if(b.getPlayer() == null) {
 							ennemiBases.add(b);
 						}
 						if(b.getPlayer() != null) {
 							if(b.getPlayer().getName() != this.getName()) {
 								ennemiBases.add(b);
 							}
 						}
 					}
 
 					// On tire un nombre entier aléatoire entre 0 et le total des bases ennemis
 					if( ennemiBases.size() <= 0) {
 						break;
 					} else {
 						int nb = rand.nextInt(ennemiBases.size());
 						Base base1 = ennemiBases.get(nb);
 						//On ajoute la commande Attack dans la file de commande
 						Move command1 = new Move(this, this.getSelectedBases(), base1);
 						if(base1.getPlayer() == null) {
 							System.out.println(this.getName()+" envoie ses troupes sur une base neutre");
 						} else {
 							System.out.println(this.getName()+" envoie ses troupes sur une base de "+base1.getPlayer().getName());
 						}
 						Engine.getInstance().getCommands().add(command1);
 					}
 				}
 				// Si je n'ai pas de base selectionnée, je selectionne ma base
 				else {
 					//On crée la liste de toutes les bases de l'IA au moment présent
 					iaBases = new ArrayList<Base>();
 					// Seulement ses bases
 					for(Base b : Game.getInstance().getBaseManager().getBases()) {
 						if(b.getPlayer() != null) {
 							if(b.getPlayer().getName() == this.getName()) {
 								iaBases.add(b);
 							}
 						}
 					}
 					// On tire un nombre entier aléatoire entre 0 et le total de ses bases
 					if( iaBases.size() <= 0) {
 						break;
 					} else {
 						int nb = rand.nextInt(iaBases.size());
 						Base base1 = iaBases.get(nb);
 						//On ajoute la commande dans la file de commande
 						SelectBase command1 = new SelectBase(this, base1);
 						Engine.getInstance().getCommands().add(command1);
 					}
 				}
 				break; // ------------------------------------------------------------------------------------------
 
 				
 			default:
 				break;
 			}
 			// We create 2 variables, elle corresponde aux bornes du temps de décisions des IA
 			int maxTimeDecision = 1000;
 			int minTimeDecision = 1000;
 			// Easy
 			if(this.getDifficulty() == 1) {
 				maxTimeDecision = 3500;
 				minTimeDecision = 2000;
 			}
 			// Middle
 			else if(this.getDifficulty() == 2) {
 				maxTimeDecision = 3000;
 				minTimeDecision = 1500;
 			}
 			// Hard
 			else if(this.getDifficulty() == 3) {
 				maxTimeDecision = 2500;
 				minTimeDecision = 1000;
 			}
 			
 			int waitingForNewDecision = (int) (rand.nextFloat()*(maxTimeDecision - minTimeDecision) + minTimeDecision);
 			
 			try {
 				Thread.sleep(waitingForNewDecision);
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	/**
 	 * Returns the String which will be displayed on the panel of the player.
 	 */
 	public String getInfosPlayer() {
 		
 		int nbTotalBases = Game.getInstance().getBaseManager().getBases().size();
 				
 		StringBuilder sb = new StringBuilder("<html>");
 		
 		sb.append(this.getName());
 		sb.append("<br /> $");
 		sb.append(this.getBank().getMoney());
 		sb.append(" | ");
 		sb.append(this.getNbBases());
 		sb.append("/");
 		sb.append(nbTotalBases);
 		sb.append("</html>");
 		
 		return sb.toString();
 	}
 	
 	public IAPlayer(String name, Bank bank, Color color) {
 		super(name, bank, color);
 		Random rand = new Random();
 		rand.setSeed(System.currentTimeMillis());
 		// difficulté entre 1 et 3
 		this.difficulty = rand.nextInt(3) + 1;
 	}
 	
 	public IAPlayer(String name, Color color) {
 		super(name, color);
 		Random rand = new Random();
 		rand.setSeed(System.currentTimeMillis());
 		// difficulté entre 1 et 3
 		this.difficulty = rand.nextInt(3) + 1;
 	}
 	
 	public IAPlayer() {
 		super("unknown", Color.WHITE);
 		this.difficulty = 1;
 	}
 	
 	
 }
