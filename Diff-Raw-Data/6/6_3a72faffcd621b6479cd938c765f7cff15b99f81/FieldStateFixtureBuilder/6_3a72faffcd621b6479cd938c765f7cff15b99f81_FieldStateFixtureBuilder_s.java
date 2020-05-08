 package ru.alepar.minesweeper.testsupport;
 
 import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
 import ru.alepar.minesweeper.model.Cell;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class FieldStateFixtureBuilder {
 
     private static final Map<Character, Cell> translateMap;
 
     static {
         translateMap = new HashMap<Character, Cell>();
         translateMap.put('.', Cell.CLOSED);
         translateMap.put('x', Cell.BOMB);
         translateMap.put(' ', Cell.valueOf(0));
         translateMap.put('1', Cell.valueOf(1));
         translateMap.put('2', Cell.valueOf(2));
         translateMap.put('3', Cell.valueOf(3));
         translateMap.put('4', Cell.valueOf(4));
         translateMap.put('5', Cell.valueOf(5));
         translateMap.put('6', Cell.valueOf(6));
         translateMap.put('7', Cell.valueOf(7));
         translateMap.put('8', Cell.valueOf(8));
     }
 
     private final List<String> rows = new LinkedList<String>();
 
     public FieldStateFixtureBuilder row(String row) {
         rows.add(row);
         return this;
     }
 
     public ArrayFieldState build() {
         Integer rowLength = null;
         Cell[][] cells = null;
         int i = 0;
         for (String row : rows) {
             if (cells == null) {
                 cells = new Cell[rows.size()][];
                 rowLength = row.length();
             }
 
             if (rowLength != row.length()) {
                 throw new IllegalArgumentException("inconsistent row lengthes");
             }
             cells[i] = new Cell[rowLength];
 
             for (int j = 0; j < row.length(); j++) {
                 cells[i][j] = translate(row.charAt(j));
             }
             i++;
         }
         return new ArrayFieldState(cells);
     }
 
     private static Cell translate(char c) {
        return translateMap.get(c);
     }
 }
