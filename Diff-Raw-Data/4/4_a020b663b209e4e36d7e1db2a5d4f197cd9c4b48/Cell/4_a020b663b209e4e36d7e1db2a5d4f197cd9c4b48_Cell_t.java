 package ru.spbau.bioinf.tagfinder.view;
 
 public class Cell {
     private int row;
     private int col;
     private Content content;
 
     public Cell(int row, int col, Content content) {
         this.row = row;
         this.col = col;
         this.content = content;
     }
 
    public int getCol() {
        return col;
    }

     public Content getContent() {
         return content;
     }
 }
