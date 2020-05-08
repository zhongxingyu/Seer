 package com.soat.rover.command;
 
 import com.soat.rover.model.Rover;
 
 /**
  * Interface representant les commandes du robot.
 * Utilisation du design pattern Command
  * 
  * @author Julien
  *
  */
 public interface ICommand
 {
 	/**
 	 * Execute une commande sur le robot
 	 * 
 	 * @param rover <strong>Rover</strong> robot
 	 */
 	public void execute(Rover rover);
 }
