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
 
     public Content getContent() {
         return content;
     }
 }
