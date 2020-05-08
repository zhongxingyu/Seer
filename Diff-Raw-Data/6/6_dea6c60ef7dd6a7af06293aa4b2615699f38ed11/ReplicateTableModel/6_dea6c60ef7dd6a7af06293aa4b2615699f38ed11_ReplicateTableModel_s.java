 package net.bioclipse.brunn.ui.editors.plateEditor.model;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.bioclipse.brunn.pojos.AbstractSample;
 import net.bioclipse.brunn.pojos.CellSample;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.PatientSample;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.editors.plateEditor.Replicates;
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableCellEditor;
 import de.kupzog.ktable.KTableCellRenderer;
 import de.kupzog.ktable.KTableDefaultModel;
 import de.kupzog.ktable.renderers.FixedCellRenderer;
 import de.kupzog.ktable.renderers.TextCellRenderer;
 
 public class ReplicateTableModel extends KTableDefaultModel {
 
 	/*
      * a representation of the matrix
      */
     private String[][] matrix;
     private int rows;
     private int cols;
     private TextCellRenderer   renderer      = new TextCellRenderer(  TextCellRenderer.INDICATION_FOCUS_ROW );
     private KTableCellRenderer fixedRenderer = new FixedCellRenderer( FixedCellRenderer.STYLE_PUSH       |
                                                                       FixedCellRenderer.INDICATION_FOCUS );
     private ArrayList<String> columnNames;
     private Map<String, String> cvmap = new HashMap<String, String>();
     
 	public ReplicateTableModel( net.bioclipse.brunn.pojos.Plate plate,
 			                    KTable table,
 			                    Replicates editor,
 			                    PlateResults plateResults ) {
 		
 		columnNames = new ArrayList<String>();
 		Collections.addAll( columnNames, new String[] {"Cell Type", "Compound Names", "Concentration"} );
 		columnNames.addAll( plate.getWellFunctionNames() );
 		columnNames.add( "CV%" );
 		columnNames.add( "no. replicates" );
 		
 		/*
 		 * set up the matrix from the plate 
 		 */
 		List<List<Well>> values = groupWells( plate.getWells() );
 		List<String[]> rows = new ArrayList<String[]>();
 		
 		for( List<Well> wells : values ) {
 			rows.add( createRow(wells, plateResults) );
 		}
 		
 		Collections.sort(rows, new Comparator<String[]>() {
 			public int compare(String[] o1, String[] o2) {
 				int c = o1[1].compareTo( o2[1] );
 				if ( c != 0 ) 
 					return c;
 				c = Double.compare( Double.parseDouble( 
 						                o1[2].contains(" ") ? o1[2].substring( 0, 
 						                		                               o1[2].indexOf(' '))
 						                		            : o1[2]), 
 						            Double.parseDouble(
						                o1[2].contains(" ") ? o1[2].substring( 0, 
						                									   o1[2].indexOf(' '))
						                				    : o1[2]) );
 				if ( c != 0 )
 					return c;
 				return o1[3].compareTo( o2[3] );
 			}
 		});
 		
 		matrix = rows.toArray(new String[0][0]);
 
 		this.rows = matrix.length;
 		this.cols = columnNames.size();
 		
 		initialize();
 	}
 
 	private String[] createRow(List<Well> wells, PlateResults plateResults) {
 		String[] row = new String[columnNames.size()];
 		//Cell Type
 		row[0] = getCellType( wells.get(0) );
 		
 		DrugSample[] drugSamples = getDrugSamples( wells.get(0) );
 		
 		//Compound Names
 		row[1] = getCompoundNames(drugSamples);
 		
 		//Concentration
 		row[2] = getConcentrations(drugSamples);
 		
 		Map<String, List<Double>> wellFunctions = new HashMap<String, List<Double>>();
 		for ( Well well : wells ) {
 			for( String wellFunctionName : columnNames.subList(3, columnNames.size()-2) ) {
 				if( wellFunctions.get(wellFunctionName) == null ) {
 					wellFunctions.put(wellFunctionName, new ArrayList<Double>());
 				}
 				wellFunctions.get(wellFunctionName)
 	                         .add( plateResults
 	            	               .getValue( well.getCol(), 
 	            			                  well.getRow(), 
 	            			                  wellFunctionName ) );
 			}
 		}
 
 		int i = 3;
 		for ( List<Double> list : wellFunctions.values() ) {
 			double sum = 0;
 			for( double d : list) {
 				sum += d;
 			}
 			DecimalFormat df = new DecimalFormat("0");
 			row[i++] = df.format(sum / list.size());
 		}
 
         //CV%
 		double sum = 0;
 		int numberOfNan = 0;
 		for( Double rawValue : wellFunctions.get("raw") ) {
 			if( !Double.isNaN( rawValue ) ) {
 				sum += rawValue;
 			}
 			else {
 				numberOfNan++;
 			}
 		}
 		int numberOfReplicates = (wellFunctions.get("raw").size() - numberOfNan); 
         double avg =  sum / numberOfReplicates;
             
         double sumOfDiffs = 0;
         for( Double rawValue : wellFunctions.get("raw") ) {
           	sumOfDiffs += (rawValue-avg)*(rawValue-avg);
         }
 
         double stddev = Math.sqrt( (1.0/(wellFunctions.get("raw").size()-1 - numberOfNan)) * sumOfDiffs );
         DecimalFormat df = new DecimalFormat("0");
 		row[columnNames.size()-2] = df.format( (stddev/avg) * 100 );
 		cvmap.put( row[1]+row[2], row[columnNames.size()-2] );
 		row[columnNames.size()-1] = numberOfReplicates + "";
 		return row;
 	}
 
 	private List<List<Well>> groupWells(Set<Well> wells) {
 		
 		Map<String, List<Well>> result = new HashMap<String, List<Well>>();
 		
 		for ( Well well : wells) {
 			
 			//We are only interested in wells with some drug compounds
 			if(well.getSampleContainer().getSamples().size() < 2) {
 				continue;
 			}
 			
 			AbstractSample[] keyComponents = well.getSampleContainer()
                                                  .getSamples()
                                                  .toArray( new AbstractSample[0] );
 			
 			Arrays.sort(keyComponents, new Comparator<AbstractSample>() {
 				public int compare(AbstractSample o1, AbstractSample o2) {
 					return o1.getName().compareTo( o2.getName() );
 				}
 			});
 
 			StringBuilder keyBuilder = new StringBuilder();
 			for( AbstractSample s : keyComponents ) {
 				if( s instanceof DrugSample ) {
 					keyBuilder.append( s.getName() );
 					keyBuilder.append( ( (DrugSample)s ).getConcentration() );
 				}
 			}
 			String key = keyBuilder.toString();
 			
 			if ( result.get(key) == null ) {
 				List<Well> list = new ArrayList<Well>();
 				list.add(well);
 				result.put(key, list);
 			}
 			else {
 				result.get(key).add(well);
 			}
 		}
 		return new ArrayList< List<Well> >( result.values() );
 	}
 
 	private String getConcentrations(DrugSample[] drugSamples) {
 
 		StringBuffer result = new StringBuffer();
 		
 		for( DrugSample drugSample : drugSamples ) {
 			result.append( drugSample.getConcentration() );
 			result.append(" ");
 			result.append( drugSample.getConcUnit() == null ? "?"
 			    		                                    : drugSample
 			    		                                      .getConcUnit()
 			    		                                      .toString() );
 			result.append(" | ");
 		}
 		//remove the last " | " and return
 		return result.substring(0, result.length() - 3);
 	}
 
 	private DrugSample[] getDrugSamples(Well well) {
 		ArrayList<DrugSample> result = new ArrayList<DrugSample>();
 		for( AbstractSample s : well.getSampleContainer().getSamples() ) {
 			if(s instanceof DrugSample) {
 				result.add(( (DrugSample)s ));
 			}
 		}
 		Collections.sort(result, new Comparator<DrugSample>() {
 			public int compare(DrugSample o1, DrugSample o2) {
 				return o1.getName().compareTo( o2.getName() );
 			}
 		});
 		return result.toArray( new DrugSample[0] );
 	}
 
 	private String getCellType(Well well) {
 		
 		for( AbstractSample s : well.getSampleContainer().getSamples() ) {
 			if(s instanceof CellSample) {
 				return ( (CellSample)s ).getCellOrigin().getName();
 			}
 			if(s instanceof PatientSample) {
 				return ( (PatientSample) s).getPatientOrigin().getName();
 			}
 		}
 		return "";
 	}
 
 	private String getCompoundNames(DrugSample[] drugSamples) {
 		
 		String result = "";
 		for( DrugSample drugSample : drugSamples ) {
 			result += drugSample.getName() + " | ";
 		}
 		result = result.substring(0, result.length()-3);
 		return result;
 	}
 
 	@Override
 	public KTableCellEditor doGetCellEditor(int col, int row) {
 		return null;
 	}
 
 	@Override
 	public KTableCellRenderer doGetCellRenderer(int col, int row) {
 		if (isFixedCell(col, row))
             return fixedRenderer;
         else
             return renderer;
 	}
 
 	@Override
 	public int doGetColumnCount() {
 		return cols;
 	}
 
 	@Override
 	public Object doGetContentAt(int col, int row) {
 		if( col >= 0 && row >= 1 ) {
 			return matrix[row-1][col];
 		}
 		if( row == 0 ) {
 			return columnNames.get(col);
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
 		return 150;
 	}
 
 	@Override
 	public int getInitialRowHeight(int row) {
 		return 20;
 	}
 
 	public int getFixedHeaderColumnCount() {
 		return 0;
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
 
 	public Map<String, String> getCVMap() {
 		return cvmap;
 	}
 }
