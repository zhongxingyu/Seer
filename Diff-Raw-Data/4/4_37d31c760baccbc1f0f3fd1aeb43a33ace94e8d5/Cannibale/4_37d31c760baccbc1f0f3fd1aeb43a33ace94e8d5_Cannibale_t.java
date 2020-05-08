 package com.ei3info.tp.lofteurs;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.LinkedList;
 
 public class Cannibale extends Neuneu {
 
 	public Cannibale(Loft loft, int x, int y) {
 		super(loft, x, y);
 		couleur = Color.RED;
 	}
 
 	public void seDeplacer() {
 		// Recherche de la nourriture la plus proche
 		int[] nextF = new int[2];
 		nextF = trouverNourriturePlusProche();
 		// Recherche du neuneu le plus proche
 		int[] nextN = new int[2];
 		nextN = trouverNeuneuPlusProche();
 		// Dtermination de la source la plus proche
		double distF = Math.sqrt(Math.pow(nextF[0], 2) + Math.pow(nextF[1], 2));
		double distN = Math.sqrt(Math.pow(nextN[0], 2) + Math.pow(nextN[1], 2));
 		if (distF < distN) {
 			nextN[0] = 0; // Il fallait bien mettre quelque chose.
 		}
 		else {
 			nextF = nextN;
 		}
 		// Pathfinding basique
 		int mvHandleX = nextF[0]/Math.abs(nextF[0]);
 		int mvHandleY = nextF[1]/Math.abs(nextF[1]);
 		// Dplacement
 		this.posX = this.posX + mvHandleX;
 		this.posY = this.posY + mvHandleY;
 	}
 	
 	public void manger() {
 		//Vrification de la prsence d'une source de nourriture sur la case
 		double nextN = trouverNeuneuPlusProche();
 		double nextF = trouverNourriturePlusProche();
 		
 		if (nextN*nextF == 0) {
 			//On parcourt la liste des ObjetDessinable pour dterminer lequel est sur la mme case que le cannibale
 			LinkedList<ObjetDessinable> localListeObjet = loft.getListeObjet();
 			for (ObjetDessinable obj : localListeObjet) {
 				double dist = Math.sqrt(Math.pow(obj.posX, 2) + Math.pow(obj.posY, 2));
 				
 				if (dist == 0) {
 					//Diffrenciation Neuneu/Nourriture: cas Neuneu
 					if (obj instanceof Neuneu) {
 						Neuneu neuneu = (Neuneu) obj;
 						int enerT = (this.energieMax - this.energie) - neuneu.getEnergie();
 						if (enerT <= 0) {
 							this.energie = this.energieMax;
 						}
 						else {
 							this.energie = this.energie + neuneu.getEnergie();
 						}
 						//Mort du neuneu qui s'est fait bouffer
 						neuneu.mourir();						
 					}
 					
 					//Diffrenciation Neuneu/Nourriture: cas Nourriture
 					if (obj instanceof Nourriture) {
 						Nourriture miam = (Neuneu) obj;
 						int enerT = (this.energieMax - this.energie) - miam.getEnergie();
 						if (enerT <= 0) {
 							this.energie = this.energieMax;
 							miam.consommer(miam.energie + this.energie - this.energieMax);									
 						}
 						else {
 							this.energie = this.energie + miam.getEnergie();
 							miam.consommer(-1*enerT);
 						}
 					}	
 				}
 			}
 		}
 		
 	}
 	//Fin de manger()
 }
