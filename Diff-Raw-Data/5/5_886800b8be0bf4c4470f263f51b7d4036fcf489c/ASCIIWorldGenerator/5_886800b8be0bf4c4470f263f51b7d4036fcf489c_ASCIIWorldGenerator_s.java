 package uk.co.lrnk.self_esteem_snake.ui;
 
 import uk.co.lrnk.self_esteem_snake.BookwormSpace;
 import uk.co.lrnk.self_esteem_snake.BookwormWorld;
 import uk.co.lrnk.self_esteem_snake.Space;
 import uk.co.lrnk.self_esteem_snake.World;
 
 public class ASCIIWorldGenerator {
 
     public String getWorldString(World world) {
         int h = world.getNumRows();
         int w = world.getNumColumns();
 
         String placeholderWorld = getPlaceholderWorld(w, h);
         return fillInWorld(placeholderWorld, world);
     }
 
     public String getWorldString(BookwormWorld world) {
         int h = world.getNumRows();
         int w = world.getNumColumns();
 
         String placeholderWorld = getPlaceholderWorld(w, h);
         return fillInWorld(placeholderWorld, world);
     }
 
     private String fillInWorld(String placeholderWorld, World world) {
 
         String resultingWorld = placeholderWorld;
         for (Space space : world.getAllSpaces()) {
 
             Character stateChar;
 
             switch (space.getState()) {
                 case EMPTY:
                     stateChar = ' ';
                     break;
                 case SNAKE:
                     stateChar = 'O';
                     break;
                 case FOOD:
                     stateChar = '~';
                     break;
                 default:
                     throw new RuntimeException("ASCIIWorldGenerator.fillInWorld(String, World): Attempted to draw space with unimplemented state");
             }
 
             String placeHolder = space.getX() + "-" + space.getY();
             resultingWorld = resultingWorld.replaceFirst(placeHolder, stateChar.toString());
         }
 
         return resultingWorld;
     }
 
     private String fillInWorld(String placeholderWorld, BookwormWorld world) {
 
         String resultingWorld = placeholderWorld;
         for (BookwormSpace space : world.getAllBookwormSpaces()) {
 
             Character stateChar;
 
             switch (space.getState()) {
                 case EMPTY:
                     stateChar = ' ';
                     break;
                 case SNAKE:
                     if(space.hasCharacter()) {
                         stateChar = (space.getCharacter() == ' ') ? '\u00B7' : space.getCharacter();
                     } else {
                         stateChar = '\u00B7';
                     }
                     break;
                 case FOOD:
                     stateChar = (space.getCharacter() == ' ') ? '\u00B7' : space.getCharacter();
                     break;
                 default:
                     throw new RuntimeException("ASCIIWorldGenerator.fillInWorld(String, BookwormWorld): Attempted to draw space with unimplemented state");
             }
 
             String placeHolder = space.getX() + "-" + space.getY();
             resultingWorld = resultingWorld.replaceFirst(placeHolder, stateChar.toString());
         }
 
         return resultingWorld;
     }
 
 
     private String getPlaceholderWorld(int w, int h) {
 
         StringBuilder worldString = new StringBuilder();
 
         addTopBorder(w, worldString);
 
        worldString.append(System.getProperty("line.separator"));
 
         for (int rowNumber = 0; rowNumber < h; rowNumber++) {
             addRowWithPlaceholders(w, rowNumber, worldString);
            worldString.append(System.getProperty("line.separator"));
         }
 
         addBottomBorder(w, worldString);
 
         return worldString.toString();
     }
 
     private void addTopBorder(int w, StringBuilder worldString) {
         for (int i = 0; i < w; i++) {
             worldString.append(" _");
         }
     }
 
     private void addBottomBorder(int w, StringBuilder worldString) {
         for (int i = 0; i < w; i++) {
             worldString.append(" \u00AF");
         }
     }
 
     private void addRowWithPlaceholders(int w, int rowNumber, StringBuilder worldString) {
         worldString.append('|');
         for (int i = 0; i < w; i++) {
             worldString.append(i).append("-").append(rowNumber);
             if (i < w - 1) {
                 worldString.append(' ');
             }
         }
         worldString.append('|');
     }
 
 }
