 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.control.tools.structurepanel;
 
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.tools.Tool;
 import es.eucm.eadventure.editor.gui.structurepanel.StructureElement;
 import es.eucm.eadventure.editor.gui.structurepanel.StructureListElement;
 
 public class AddElementTool extends Tool {
 
     private int type;
 
     private StructureListElement element;
 
     private JTable table;
 
     private StructureElement newElement;
 
     public AddElementTool( StructureListElement element, JTable table ) {
 
         this.type = element.getDataControl( ).getAddableElements( )[0];
         this.element = element;
         this.table = table;
     }
 
     @Override
     public boolean canUndo( ) {
 
         return true;
     }
 
     @Override
     public boolean doTool( ) {
 
         if( element.getDataControl( ).canAddElement( type ) ) {
             String defaultId = element.getDataControl( ).getDefaultId( type );
            defaultId.replaceAll( " ", "_" );
            if ( defaultId!=null && defaultId.length( )>0 && Character.isDigit( defaultId.charAt( 0 ) ) ){
                defaultId="MH"+defaultId;
            } else if (defaultId==null || defaultId.length( )==0){
                defaultId="";
            }
             String id = defaultId;
             int count = 0;
             while( !Controller.getInstance( ).isElementIdValid( id, false ) ) {
                 count++;
                 id = defaultId + count;
             }
             if( element.getDataControl( ).addElement( type, id ) ) {
                 //( (StructureElement) table.getModel( ).getValueAt( element.getChildCount( ) - 1, 0 ) ).setJustCreated( true );
                 ( (AbstractTableModel) table.getModel( ) ).fireTableDataChanged( );
                 /*SwingUtilities.invokeLater( new Runnable( ) {
 
                     public void run( ) {
 
                         if( table.editCellAt( element.getChildCount( ) - 1, 0 ) )
                             ( (StructureElementCell) table.getEditorComponent( ) ).requestFocusInWindow( );
                     }
                 } );*/
                 table.changeSelection( element.getChildCount( ) - 1, 0, false, false );
                 newElement = element.getChild( element.getChildCount( ) - 1 );
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String getToolName( ) {
 
         return "Add child";
     }
 
     @Override
     public boolean undoTool( ) {
 
         newElement.delete( false );
         Controller.getInstance( ).updateStructure( );
         return true;
     }
 
     @Override
     public boolean canRedo( ) {
 
         return false;
     }
 
     @Override
     public boolean redoTool( ) {
 
         return false;
     }
 
     @Override
     public boolean combine( Tool other ) {
 
         return false;
     }
 
 }
