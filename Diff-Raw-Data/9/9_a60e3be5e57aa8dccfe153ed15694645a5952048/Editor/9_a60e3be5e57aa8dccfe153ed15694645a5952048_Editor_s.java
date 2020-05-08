 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.rit.se.antipattern.htmleditor.models;
 
 /**
  * Handles dealing with the functions such as indent and insert
  * 
  * @author Zach, Wayne
  */
 public class Editor {
     
     /**
      * Instantiates an editor construct.
      */
     public Editor () {
         
     }
     
     /**
      * Indents a given buffer with the given start and end indexes
      * @param toIndent
      * @param startIndex
      * @param endIndex 
      */
     public void indent ( Buffer toIndent ) {
         EditorStrategy i = new Indent( toIndent );
         i.execute();
     }
     
     /**
      * Auto indents a new line when given a cursor position
      * @param toIndent
      * @param cursor 
      */
     public int autoIndent (Buffer toIndent) {
         int i = 0, cursorPos = toIndent.getCursorStartPos();
         String text = toIndent.getText();
        int numTabs = Indent.calulateTabs(text, cursorPos-1);
         for (i = 0 ; i < numTabs; i++) {
             toIndent.insertText("\t", cursorPos);
         }
         
         return cursorPos + numTabs;
     }
     
     /**
      * Insert a given tag at the given index in the given buffer. Flat insert.
      * @param toInsert
      * @param name
      * @param startIndex 
      */
     public void insert (Buffer toInsert, String name ) {
         int tabs = Indent.calulateTabs(toInsert.getText(), toInsert.getCursorStartPos());
         EditorStrategy ins = new Insert( toInsert, name, tabs );
         ins.execute();
     }
     
     /**
      * Inserts a given layered tag with a sub tag name at the given index for
      * n sub tags.
      * @param toInsert
      * @param name
      * @param subName
      * @param startIndex
      * @param subTags 
      */
     public void insert (Buffer toInsert, String name, String subName, int subTags) {
         int tabs = Indent.calulateTabs(toInsert.getText(), toInsert.getCursorStartPos());
         EditorStrategy ins = new Insert( toInsert, name, subName, subTags, tabs );
         ins.execute();
     }
     
     /**
      * Inserts a table of given rows and columns at the given index
      * @param toInsert
      * @param startIndex
      * @param rows
      * @param cols 
      */
     public void insert (Buffer toInsert, int rows, int cols) {
         int tabs = Indent.calulateTabs(toInsert.getText(), toInsert.getCursorStartPos());
         EditorStrategy ins = new Insert( toInsert, rows, cols, tabs );
         ins.execute();
     }
 }
