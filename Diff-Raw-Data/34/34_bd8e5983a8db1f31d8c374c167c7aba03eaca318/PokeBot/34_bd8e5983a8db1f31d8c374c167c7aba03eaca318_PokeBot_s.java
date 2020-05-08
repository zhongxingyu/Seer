 package fr.univaix.iut.pokebattle;
 
 import twitter4j.Status;
 import fr.univaix.iut.pokebattle.smartcells.*;
 
 
 public class PokeBot implements Bot {
     /**
      * List of SmartCells the questions go through to
      * find an answer.
      */
     final SmartCell[] smartCells = new SmartCell[]{
     		new PokemonCriesCell(),
             new AttackCell(),
             new OwnerCell(),
             new PokeballCell(),
     };
 
     /**
      * Ask something to BoBot, it will respond to you.
      *
      * @param question The question you ask.
      * @return An answer... or null if it doesn't get it.
      */
     @Override
     public String ask(Status question) {
         for (SmartCell cell : smartCells) {
             String answer = cell.ask(question);
             if (answer != null)
                 return answer;
         }
         return null;
     }
 }
