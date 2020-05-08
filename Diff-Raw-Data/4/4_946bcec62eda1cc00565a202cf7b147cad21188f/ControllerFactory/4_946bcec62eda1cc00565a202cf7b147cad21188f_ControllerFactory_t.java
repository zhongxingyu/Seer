 /**
  * 
  */
 package de.htwg.se.battleship.controller;
 
 import de.htwg.se.battleship.controller.impl.InitGameController;
 
 /**
  * @author Philipp Daniels<philipp.daniels@gmail.com>
  *
  */
public final class ControllerFactory {

    private ControllerFactory() {}
 
     /**
      * Returns an implementation of IInitGameController.
      * @return IInitGameController
      */
     public static IInitGameController createIInitGameController() {
         return new InitGameController();
     }
 
 }
