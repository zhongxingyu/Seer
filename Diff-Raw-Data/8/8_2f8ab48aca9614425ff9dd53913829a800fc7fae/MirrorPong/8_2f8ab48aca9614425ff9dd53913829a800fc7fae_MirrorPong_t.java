 /*
  *  MirrorPong.java
  *
  *  Copyright 2011 Kévin Gomez Pinto <contact@kevingomez.fr>
  *                 Jonathan Da Silva <Jonathan.Da_Silva1@etudiant.u-clermont1.fr>
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *  MA 02110-1301, USA.
  */
 
 package game;
 
 import game.objects.Wall;
 import game.objects.Player;
 import game.Constants.State;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.util.Random;
 
 import network.Connection;
 import network.Paquet;
 
 
 public final class MirrorPong extends PongBase {
 	/**
 	 * ID de sérialisation
 	 */
 	private static final long serialVersionUID = 7224334478468671910L;
 
 	private int serverPort = 6000;
 
 	/**
 	 * Nombre de points à atteindre pour remporter le match
 	 */
 	private int maxPoints = 1;
 
     /**
      * Niveaux de difficulté disponibles :
      *  - Noob : jeu normal, vitesse de balle fixe
      *  - Easy : la vitesse augmente en fonction du temps de jeu
      *  - Medium : un mur apparait de temps en temps
      *  - Hard : trois raquettes ?
      *
      * Bien évidemment, les difficultés s'aditionnent ...
      */
     private enum Level {
         NOOB, EASY, MEDIUM, HARD
     }
     
     /**
      * Difficulté courante
      */
     private Level level = Level.MEDIUM;
 
 
 	/**
 	 * Programme principal
 	 *
 	 * @param args arguments du programme (port)
 	 */
 	public static void main(String[] args) {
 		MirrorPong jp = new MirrorPong();
         
 		if(args.length == 1) {
 			try {
 				jp.setPort(Integer.parseInt(args[0]));
 			} catch(NumberFormatException e) {
 				System.err.println("Port incorrect : utilisation du port 6000");
 				// on ne modifie pas le port par défaut
 			}
 		}
 
 		jp.start();
 	}
 
     
     public MirrorPong() {
         setTitle("Mirror Pong");
     }
 
     /**
 	 * Initialisation du jeu. On réalise ici la création du serveur et l'attente
      * de la connexion d'un client
      *
      * Cette méthode est appelée après l'initialisation de la partie graphique
      *
      * @throws IllegalStateException Si la création du serveur est impossible
      */
     @Override
     protected void initGame() {
         // lancement du serveur
 		try {
 			sock = new Connection(serverPort);
 		} catch (Exception e) {
 			throw new IllegalStateException("Erreur au lancement du serveur : " + e.getLocalizedMessage());
 		}
 
         // attente d'un client
         waitClient();
 
 		changeState(State.READY);
     }
 	
     @Override
     protected Player getMyPlayer() {
         return player1;
     }
 
     /**
      * Attent qu'un client se connecte pour enregistrer son adresse et son
      * port
      */
     private void waitClient() {
         // attente de la connexion du second joueur
         Paquet p = null;
         String msg = "";
         while(!msg.equals("HELLO")) {
             try {
                 p = sock.receive();
             } catch (IOException e) {
                 continue;
             }
 
             if(p == null)
                 continue;
 
             msg = p.getMessage();
         }
 
         setDistantHost(p.getDatagram().getAddress());
         setDistantPort(p.getDatagram().getPort());
     }
 
     /**
      * Fixe le nombre de points à atteindre pour gagner une partie
      *
      * @param max Nombre de points à atteintre pour gagner une partie
      *
      * @throws IllegalArgumentException Si le nombre de points est strictement
      *                                  inférieur à 1
      */
     public void setMaxPoints(int max) {
         if(max < 1)
             throw new IllegalArgumentException("Nombre de points maximal incorrect : doit être supérieur à 0");
 
         maxPoints = max;
     }
 
     /**
      * Définit le port sur lequel devra fonctionner le serveur
      *
      * @param port Port d'écoute du serveur
      *
      * @throws IllegalArgumentException Si le port est inférieur ou égal à 0
      */
     public void setPort(int port) {
         if(port <= 0)
             throw new IllegalArgumentException("Port incorrect");
 
         serverPort = port;
     }
 
     /**
      * On met à jour les mouvements des joueurs dans l'affichage après avoir
      * effectué quelques vérifications sur l'état du jeu et déplacé la balle.
      *
      * @note Sera appelée par le thread.
      */
     @Override
     public void run() {
         Paquet p;
         Random r = new Random();
         while (currentState() != State.FINISHED) {
             repaint();
             
 			try {
 				p = sock.tryReceive(2);
 			} catch (IOException e) {
 				p = null;
 			}
 
             if(p != null && p.getMessage() != null)
                 executeCmd(p.getMessage());
 
             // si le jeu est en pause, pas besoin de bouger la balle ou de
             // vérifier les collisions
 			if(currentState() == State.PAUSED)
 				continue;
 
             ball.move();
 
 			checkCollisions();
 
             // gestion du mur "amovible" : seulement à partir de medium
             if(r.nextInt(200) == 34 && level.compareTo(Level.MEDIUM) >= 0)
             {
                 wall.toggleVisibility();
 
                if(wall.isVisible()) {
                     wall.move();
 
                    // pour éviter que la mur apparaisse sur la balle
                    while(wall.getZone().intersects(ball.getZone()))
                        wall.move();
                }

                 // envoi des infos du mur
                 String msg = String.format("%s %d %d %s", Constants.MSG_WALL_POS,
                                            wall.x, wall.y,
                                            wall.isVisible() ? "on" : "off");
                 sendToDistantPlayer(msg);
             }
 
             // envoi de la position de la balle
             sendToDistantPlayer(String.format("%s %d %d", Constants.MSG_BALL,
                                                           ball.x, ball.y));
 
             wait(8);
         }
 
         repaint();
 
         // partie terminée
         onGameOver();
     }
 
     /**
      * Regarde s'il n'y a pas eu de collision entre la balle et :
      *  - les murs
      *  - le "mur amovible"
      *  - les raquettes des joueurs
      */
     private void checkCollisions() {
         checkPlayerCollision(player1);
         checkPlayerCollision(player2);
         checkWallCollision(wall);
         checkWalls();
     }
 
 	/**
 	 * (Re)démarre le jeu s'il est arrêté (pas commencé ou point marqué)
 	 *
 	 * @param e Event lié à la souris
 	 *
 	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (currentState() != State.READY)
 			return;
 
 		ball.getSpeed().x = 4;
 		ball.getSpeed().y = 2;
 
         // comme ça la balle n'est pas tout le temps lancée du même côté
         if(System.currentTimeMillis() % 2 == 0)
             ball.getSpeed().x *= -1;
 
         changeState(State.STARTED); // demarre le jeu
     }
 
 	/**
 	 * Teste la collision entre la balle et un joueur,
 	 * et lance les actions associées si elle est avérée.
 	 *
 	 * @param player Joueur dont on veut tester la collision avec la balle.
 	 */
 	private void checkPlayerCollision(Player player) {
 		if(!checkCollision(player))
 			return;
 
 		int racketHit = (ball.y - (player.y + 25)) / 7;
 		speedBallUp(racketHit);
 
         sendToDistantPlayer(Constants.MSG_CONTACT);
         Sound.play(Constants.SOUND_CONTACT);
     }
 
     private void checkWallCollision(Wall wall) {
         if(!wall.isVisible() || !wall.intersects(ball.getZone()))
             return;
 
 		int racketHit = (ball.y - (wall.y + 25)) / 7;
 		speedBallUp(racketHit);
 
 		sendToDistantPlayer(Constants.MSG_CONTACT);
 		Sound.play(Constants.SOUND_CONTACT);
 	}
 
     private void speedBallUp(int hit) {
         // à partir de easy, on augmente la vitesse
         if(level.compareTo(Level.EASY) >= 0)
             ball.getSpeed().y += hit;
 
 		ball.getSpeed().x *= -1;
     }
 
 	/**
 	 * Teste la collision entre la balle et la raquette d'un joueur
 	 *
 	 * @param joueur Position de la raquette du joueur
 	 *
 	 * @return True s'il y a collision, false sinon
 	 */
 	private boolean checkCollision(Player joueur) {
 		return joueur.getZone().intersects(ball.getZone());
 	}
 
 	/**
 	 * Vérifie que la balle ne soit pas en collision avec un mur.
 	 */
 	protected void checkWalls() {
 		int ballLeft = ball.x - (int) ball.getWidth() / 2;
 		int ballTop = ball.y - (int) ball.getHeight() / 2;
 		int ballRight = ball.x + (int) ball.getWidth();
 		int ballBottom = ball.y;
 
         // haut ou bas : la balle rebondit
 		if (ballTop <= plane.y || ballBottom >= plane.height) {
 			ball.getSpeed().y *= -1;
 		// gauche ou droit
         } else if (ballLeft <= plane.x || ballRight >= plane.width) {
             // envoi de l'info "mur touché"
             sendToDistantPlayer(Constants.MSG_WALL_TOUCHED);
 			onWallTouched();
 
             // un point a été marqué
             onMiss();
 		}
 	}
 
 	/**
 	 * La balle a heurté un mur (derrière un des deux pavés).
 	 * On regarde de quel côté la balle touche le mur, on met les scores à
      * jour et on les envoie au client
 	 */
 	private void onMiss() {
 		if (ball.getSpeed().x >= 0)
 			player1.incScore();
 		else
 			player2.incScore();
 
 		// envoi des scores
         String msg1 = String.format("%s %s %d", Constants.MSG_SCORE, player1,
                                                 player1.getScore());
         String msg2 = String.format("%s %s %d", Constants.MSG_SCORE, player2,
                                                 player2.getScore());
 
         sendToDistantPlayer(msg1);
         sendToDistantPlayer(msg2);
         
         // ici, soit le jeu est terminé, soit on est en attente de la relance
         changeState((player1.getScore() == maxPoints || player2.getScore() == maxPoints)
                     ? State.FINISHED
                     : State.READY);
 
         resetBall();
     }
 
     /**
      * Chaque fois que l'état du jeu change, on prévient le client
      *
      * @param newState Nouvel état du jeu
      */
     @Override
     protected void changeState(State newState) {
         super.changeState(newState);
 
         sendToDistantPlayer(String.format("%s %s", Constants.MSG_STATE_CHANGED, newState));
     }
 }
