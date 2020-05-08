 package com.soundbite.gcharts;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 public class LiteralBuilder {
 
 	List<Column> columns = Lists.newLinkedList();
 	List<Row> rows = Lists.newLinkedList();
 
 	/**
 	 * 
 	 * @param type The data type of the values of the column. 
 	 * @return
 	 */
 	public LiteralBuilder addColumn(ColumnType type) {
 		columns.add(new Column(type));
 		return this;
 	}
 	
 	/**
 	 * 
 	 * @param type The data type of the values of the column. 
 	 * @param label A string with the label of the column. The column label is typically displayed as part of the visualization, for example as a column header in a table, or as a legend label in a pie chart. If no value is specified, an empty string is assigned.
 	 * @return
 	 */
 	public LiteralBuilder addColumn(ColumnType type, String label) {
 		columns.add(new Column(type, label));
 		return this;
 	}
 	
 	public LiteralBuilder addRow(Cell...cells ) {
 		rows.add(new Row(Arrays.asList(cells)));
 		return this;
 	}
 	
 	/**
 	 * Builds the data js literal.
 	 * @return
 	 */
 	public String buildDataLiteral() {
 		StringBuilder sb = new StringBuilder();
 		
 		// column definitions
 		sb.append("{cols:[");
 		for (Column col : columns) {
 			sb.append("{type: '"+col.getType()+"'");
 			
 			if (col.hasLabel())
 				sb.append(", label: '"+col.getLabel()+"'");
 			
 			sb.append("},");
 		}
                 if (!columns.isEmpty())
                     sb.deleteCharAt(sb.lastIndexOf(","));
 		
 		sb.append("], ");
 		
 		// row definitions
 		sb.append("rows:[");
 		for (Row row : rows) {
 			sb.append("{c:[");
 			for (Cell cell : row.getCells()) {
 				sb.append("{v: "+cell.literalValue()+"},");
 			}
                        if (!row.getCells().isEmpty())
                            sb.deleteCharAt(sb.lastIndexOf(","));

 			sb.append("]},");
 			
 		}
                 if (!rows.isEmpty())
                     sb.deleteCharAt(sb.lastIndexOf(","));
 
 		sb.append("]");
 		
 		sb.append("}");
 		return sb.toString();
 	}
 }
