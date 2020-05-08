 package net.bioclipse.brunn.ui.editors.plateEditor.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.plate.IPlateManager;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.SampleMarker;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.editors.plateEditor.PlateEditor;
 
 import org.eclipse.swt.graphics.Point;
 
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableCellEditor;
 import de.kupzog.ktable.KTableCellRenderer;
 import de.kupzog.ktable.KTableDefaultModel;
 import de.kupzog.ktable.renderers.FixedCellRenderer;
 import de.kupzog.ktable.renderers.TextCellRenderer;
 
 public class PlateTableModel extends KTableDefaultModel {
 
 	/*
      * a representation of the matrix
      */
     private Well[][] matrix;
     private int rows;
     private int cols;
     private TextCellRenderer   renderer      = new TextCellRenderer(  TextCellRenderer.INDICATION_FOCUS_ROW );
     private KTableCellRenderer fixedRenderer = new FixedCellRenderer( FixedCellRenderer.STYLE_PUSH       |
                                                                       FixedCellRenderer.INDICATION_FOCUS );
     private net.bioclipse.brunn.pojos.Plate plate; 
     private KTable plateTable;
     private PlateEditor plateEditor;
     
 	public PlateTableModel( net.bioclipse.brunn.pojos.Plate plate,
 			                KTable platetable,
 			                PlateEditor plateEditor,
 			                String wellFunctionToBeShown, 
 			                PlateResults plateResults) {
 		
 		/*
 		 * set up the matrix from the plate 
 		 */
 		rows = plate.getRows();
 		cols = plate.getCols();
 		matrix = new Well [ cols ] [ rows ];
 		
 		for ( net.bioclipse.brunn.pojos.Well well : plate.getWells() ) {
 			
 			Well localWell = new Well(well);
 			matrix[ well.getCol()-1 ][ well.getRow()-'a' ] = localWell;
 			
 			for ( SampleMarker m : well.getSampleMarkers() ) {
 				localWell.markers.add( m.getName() );
 				localWell.concentrations.add( 
 						m.getSample() == null ? 0 
                                               : ( (DrugSample)m.getSample() )
                                                 .getConcentration() );
                localWell.concUnits.add( m.getSample() == null ? "" 
                                                               : ( (DrugSample)m.getSample() )
                                                                     .getConcUnit().toString() );
 			}
 
 			try {
 				localWell.value = plateResults.getValue(well.getCol(), 
 						                                well.getRow(), 
 						                                wellFunctionToBeShown);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		this.plate       = plate;
 		this.plateTable  = platetable;
 		this.plateEditor = plateEditor;
 		initialize();
 	}
 
 	@Override
 	public KTableCellEditor doGetCellEditor(int col, int row) {
 //		if (isFixedCell(col, row)) {
 //            return null; // no editor
 //		}
 //		else {
 //			CellEditorActionsCombo editor = new CellEditorActionsCombo();
 //			editor.setItems(createMarkerEditactions(col, row, plateEditor, plateTable));
 //			return editor;
 //		}
 		return null;
 	}
 
 	@Override
 	public KTableCellRenderer doGetCellRenderer(int col, int row) {
 //		return KTableCellRenderer.defaultRenderer;
 //		return renderer;
 		if (isFixedCell(col, row))
             return fixedRenderer;
         else
             return renderer;
 	}
 
 	@Override
 	public int doGetColumnCount() {
 		return cols+1;
 	}
 
 	@Override
 	public Object doGetContentAt(int col, int row) {
 		if( col >= 1 && row >= 1 ) {
 			return matrix[col-1][row-1];	
 		}
 		if( col == 0 ) {
 			if ( row == 0) {
 				return "";
 			}
 			StringBuilder label = new StringBuilder();
 			do {
 				label.append( (char)( (row>26 ? row % 26 : row) + 'a' -1 ) );
 		   		row = (int)(row/26);
 			}
 			while(col > 26);
 				
 			return label.reverse().toString();
 		}
 		if( row == 0 ) {
 			return col;
 		}
 		throw new IllegalArgumentException(col +" or " + row + " not a legal argument");
 	}
 	
 	public String getValueAt(int col, int row) {
 		if( col >= 1 && row >= 1 ) {
 			return matrix[col-1][row-1].value + "";	
 		}
 		if( col == 0 ) {
 			if ( row == 0) {
 				return "";
 			}
 			StringBuilder label = new StringBuilder();
 			do {
 				label.append( (char)( (row>26 ? row % 26 : row) + 'a' -1 ) );
 		   		row = (int)(row/26);
 			}
 			while(col > 26);
 				
 			return label.reverse().toString();
 		}
 		if( row == 0 ) {
 			return col+"";
 		}
 		throw new IllegalArgumentException(col +" or " + row + " not a legal argument");
 	}			
 	
 	@Override
 	public int doGetRowCount() {
 		return rows+1;
 	}
 
 	@Override
 	public void doSetContentAt(int col, int row, Object value) {
 	
 	}
 
 	@Override
 	public int getInitialColumnWidth(int column) {
 		return 120;
 	}
 
 	@Override
 	public int getInitialRowHeight(int row) {
 		return 60;
 	}
 
 	public int getFixedHeaderColumnCount() {
 		return 1;
 	}
 
 	public int getFixedHeaderRowCount() {
 		return 1;
 	}
 
 	public int getFixedSelectableColumnCount() {
 		return 0;
 	}
 
 	public int getFixedSelectableRowCount() {
 		return 0;
 	}
 
 	public int getRowHeightMinimum() {
 		return 30;
 	}
 
 	public boolean isColumnResizable(int col) {
 		return true;
 	}
 
 	public boolean isRowResizable(int row) {
 		return true;
 	}
 	
 	public String doGetTooltipAt(int col, int row) {
         return null;
     }
 
 	public Well[][] getMatrix() {
 		return matrix;
 	}
 	
 	public static class Well {
 
 		private net.bioclipse.brunn.pojos.Well domainWell;
 		
 		public Well( net.bioclipse.brunn.pojos.Well domainWell ) {
 			this.domainWell = domainWell;
 		}
 		
 		public void setOutlier(boolean outlier) {
 			domainWell.setOutlier(outlier);
 		}
 		
 		List<String> markers = new ArrayList<String>();
 		List<Double> concentrations = new ArrayList<Double>();
		List<String> concUnits = new ArrayList<String>();
 		double value = -1;
 		
 		public String toString() {
 			
 			if( markers.size() == 0 ) {
 				return "";
 			}
 			
 			StringBuilder s = new StringBuilder();
 			
 			String valuePart 
 				= ( (value==-1) 
 			      || (Double.compare( Double.NaN, value) == 0 ) ? "" 
 				                                                : Math.round(value) + "" );
 			if( domainWell.isOutlier() ) {
 				valuePart = "outlier";
 			}
 			valuePart = valuePart + "\n";
 			for (int i = 0; i < markers.size(); i++) {
 				s.append( markers.get(i) );
 				if( markers.get(i).matches("M\\d+") && concentrations.get(i) != 0 ) {
 					s.append( "[" );
 					s.append( String.format( "%.2f", concentrations.get(i) ) ); 
					s.append( " " );
					s.append( concUnits.get(i) );
 					s.append( "]" );
 				}
 				s.append("\n");
 			}
 			return valuePart + s.toString();
 		}
 	}
 
 	public Collection<Point> getPointsWithMarker(String m) {
 		Collection<Point> points = new HashSet<Point>();
 		for (int i = 0; i < matrix.length; i++) {
 			for (int j = 0; j < matrix[i].length; j++) {
 				if( matrix[i][j].markers.contains(m) ) {
 					points.add( new Point(i+1, j+1) );
 				}
 			}
 		}
 		return points;
 	}
 
 	public Collection<String> getMarkerNames(Point selection) {
 		return matrix[selection.x-1][selection.y-1].markers;
 	}
 
 //	public Collection<String> getMarkerNames(Point selection) {
 //		HashSet<String> markers = new HashSet<String>(); 
 ////		matrix[ selection.x - 1] [selection.y - 1 ].
 //	}
 }
