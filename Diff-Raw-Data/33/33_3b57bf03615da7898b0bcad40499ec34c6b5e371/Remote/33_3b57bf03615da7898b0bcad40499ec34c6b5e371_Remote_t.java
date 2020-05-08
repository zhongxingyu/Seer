 package ru.pondohva.player;
 
 import ru.pondohva.game.Field;
 import ru.pondohva.game.Game;
 
 public class Remote extends Player {
 
     private static Field newPole;
     private static Game newGame;
     private char[][] field;
     private char sign;
 
     public Remote(Field pole, Game game, char sign) {
         this.newPole = pole;
         this.newGame = game;
         this.sign = sign;
     }
 
    public void step() {}
 }
