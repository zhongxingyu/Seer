 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.dataexporter;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.el.MethodExpression;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIData;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import org.icefaces.ace.component.datatable.DataTable;
 
 /*
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.Font;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.pdf.PdfPTable;
 import com.lowagie.text.pdf.PdfWriter;
 */
 import java.lang.reflect.*;
 
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.row.Row;
 
 import org.icefaces.application.ResourceRegistry;
 import java.io.ByteArrayInputStream;
 import java.util.Map;
 
 import java.util.logging.Logger;
 
 public class PDFExporter extends Exporter {
 
 	private final static Logger logger = Logger.getLogger(PDFExporter.class.getName());
 	
 	private Class documentClass;
 	private Class fontClass;
 	private Class fontFactoryClass;
 	private Class paragraphClass;
 	private Class phraseClass;
 	private Class pdfPTableClass;
 	private Class elementClass;
 	private Class pdfWriterClass;
 	
 	private Method addCellMethod;
 	private Constructor paragraphConstructor;
 	
 	private void loadClasses() throws ClassNotFoundException {
 		documentClass = Class.forName("com.lowagie.text.Document");
 		fontClass = Class.forName("com.lowagie.text.Font");
 		fontFactoryClass = Class.forName("com.lowagie.text.FontFactory");
 		paragraphClass = Class.forName("com.lowagie.text.Paragraph");
 		phraseClass = Class.forName("com.lowagie.text.Phrase");
 		pdfPTableClass = Class.forName("com.lowagie.text.pdf.PdfPTable");
 		elementClass = Class.forName("com.lowagie.text.Element");
 		pdfWriterClass = Class.forName("com.lowagie.text.pdf.PdfWriter");
 	}
 	
 	private void loadMethods() throws NoSuchMethodException {
 		addCellMethod = pdfPTableClass.getMethod("addCell", new Class[] { phraseClass });
 		paragraphConstructor = paragraphClass.getConstructor(new Class[] { String.class, fontClass });
 	}
 	
 	@Override
 	public String export(FacesContext facesContext, DataTable table, String filename, boolean pageOnly, int[] excludeColumns, String encodingType, MethodExpression preProcessor, MethodExpression postProcessor, boolean includeHeaders, boolean includeFooters, boolean selectedRowsOnly) throws IOException { 
 		try {
 			loadClasses();
 			loadMethods();
 			//Document document = new Document();
 			Object document = documentClass.newInstance();
 	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			//PdfWriter.getInstance(document, baos);
 	        pdfWriterClass.getMethod("getInstance", new Class[] { documentClass, OutputStream.class }).invoke(null, new Object[] { document, baos });
 	        
 	        if (preProcessor != null) {
 	    		preProcessor.invoke(facesContext.getELContext(), new Object[]{document});
 	    	}
 
             //if (!document.isOpen()) {
             //    document.open();
             //}
 			Boolean isOpen = (Boolean) documentClass.getMethod("isOpen").invoke(document);
             if (!isOpen) {
                 documentClass.getMethod("open").invoke(document);
             }
 	        
 			Constructor pdfPTableConstructor = pdfPTableClass.getConstructor(new Class[] { int.class });
 			Method add = documentClass.getMethod("add", elementClass);
 			
 			List<UIColumn> columns = getColumnsToExport(table, excludeColumns);
			if (columns.size() > 0) {
 				//PdfPTable pdfTable = exportPDFTable(table, pageOnly,excludeColumns, encodingType, includeHeaders, includeFooters, selectedRowsOnly);
 				Object pdfTable = pdfPTableConstructor.newInstance(new Object[] { new Integer(columns.size()) });
 				exportPDFTable(facesContext, pdfTable, table, pageOnly,excludeColumns, encodingType, includeHeaders, includeFooters, selectedRowsOnly);
 				//document.add(pdfTable);
 				add.invoke(document, new Object[] { pdfTable });
 			} else {
 				//PdfPTable pdfTable = new PdfPTable(1);
 				Object pdfTable = pdfPTableConstructor.newInstance(new Object[] { new Integer(1) });
 				//pdfTable.addCell(new Paragraph(""));
 				Object paragraph = paragraphClass.getConstructor(new Class[] { String.class }).newInstance(new Object[] { "" });
 				addCellMethod.invoke(pdfTable, new Object[] { paragraph });
 				//document.add(pdfTable);
 				add.invoke(document, new Object[] { pdfTable });
 			}
 			
 	    	if (postProcessor != null) {
 	    		postProcessor.invoke(facesContext.getELContext(), new Object[]{document});
 	    	}
 	    	
 	        //document.close();
 			documentClass.getMethod("close").invoke(document);
 			
 			byte[] bytes = baos.toByteArray();
 			//ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
 			//ExporterResource resource = new ExporterResource(bais);
 			ExporterResource resource = new ExporterResource(bytes);
 			resource.setContentType("application/pdf");
 			Map<String, String> headers = resource.getResponseHeaders();
 			headers.put("Expires", "0");
 			headers.put("Cache-Control","must-revalidate, post-check=0, pre-check=0");
 			headers.put("Pragma", "public");
 			headers.put("Content-disposition", "attachment;filename=" + filename + ".pdf");
 			String path = ResourceRegistry.addSessionResource(resource);
 			
 			return path;
 	        
 		} catch (ClassNotFoundException e) {
 			logger.severe("Exporting data to PDF format was attempted by a user, but the iText library was not found.");
 			return "unsupported format";
 		} catch (Exception e) {
 			throw new IOException(e.getMessage());
 		}
 	}
 	
 	private void exportPDFTable(FacesContext facesContext, Object pdfTable, DataTable table, boolean pageOnly, int[] excludeColumns, String encoding, boolean includeHeaders, boolean includeFooters, boolean selectedRowsOnly) 
 		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
 		List<UIColumn> columns = getColumnsToExport(table, excludeColumns);
     	int numberOfColumns = columns.size();
     	//PdfPTable pdfTable = new PdfPTable(numberOfColumns);
     	//Font font = FontFactory.getFont(FontFactory.TIMES, encoding);
 		
 		Object font = fontFactoryClass.getMethod("getFont", new Class[] { String.class, String.class }).invoke(null, new Object[] { "Times", encoding });
     	//Font headerFont = FontFactory.getFont(FontFactory.TIMES, encoding, Font.DEFAULTSIZE, Font.BOLD);
 		
 		Object headerFont = fontFactoryClass.getMethod("getFont", new Class[] { String.class, String.class, float.class, int.class }).invoke(null, new Object[] { "Times", encoding, new Integer(12), new Integer(1) });
     	
 		int rowCount = table.getRowCount();
     	int first = pageOnly ? table.getFirst() : 0;
     	int size = pageOnly ? (first + table.getRows()) : rowCount;
 		size = size > rowCount ? rowCount : size;
     	
 		if (includeHeaders) {
 			ColumnGroup columnGroup = getColumnGroupHeader(table);
 			if (columnGroup != null) {
 				List<Row> rows = getRows(columnGroup);
 				for (Row row : rows) {
 					List<UIColumn> rowColumns = getRowColumnsToExport(row, table, excludeColumns);
 					addFacetColumns(pdfTable, rowColumns, headerFont, ColumnType.HEADER);
 				}
 			} else {
 				addFacetColumns(pdfTable, columns, headerFont, ColumnType.HEADER);
 			}
 		}
 
 		Object originalData = null;
 		if (selectedRowsOnly) {
 			originalData = table.getModel().getWrappedData();
 			table.getModel().setWrappedData(table.getStateMap().getSelected());
 			first = 0;
 			size = table.getRowCount();
 		}
 
 		String rowIndexVar = table.getRowIndexVar();
 		rowIndexVar = rowIndexVar == null ? "" : rowIndexVar;
     	for (int i = first; i < size; i++) {
     		table.setRowIndex(i);
 			if (!"".equals(rowIndexVar)) {
 				facesContext.getExternalContext().getRequestMap().put(rowIndexVar, i);
 			}
 			for (int j = 0; j < numberOfColumns; j++) {
                 addColumnValue(pdfTable, columns.get(j).getChildren(), j, font);
 			}
 		}
 		
 		if (selectedRowsOnly) {
 			table.getModel().setWrappedData(originalData);
 		}
 
         if (hasColumnFooter(columns) && includeFooters) {
             addFacetColumns(pdfTable, columns, headerFont, ColumnType.FOOTER);
         }
     	
     	table.setRowIndex(-1);
 	}
 	
 	private void addFacetColumns(Object pdfTable, List<UIColumn> columns, Object font, ColumnType columnType) 
 		throws IllegalAccessException, InvocationTargetException, InstantiationException {
         for (int i = 0; i < columns.size(); i++) {
             UIColumn uiColumn = (UIColumn) columns.get(i);
 			UIComponent facet = uiColumn.getFacet(columnType.facet());
 
             if (facet != null) {
 				addColumnValue(pdfTable, facet, font);
 			} else {
 				String value = "";
 				if (uiColumn instanceof Column) {
 					Column column = (Column) uiColumn;
 					if (columnType == ColumnType.HEADER) {
 						String headerText = column.getHeaderText();
 						value = headerText != null ? headerText : "";
 					} else if (columnType == ColumnType.FOOTER) {
 						String footerText = column.getFooterText();
 						value = footerText != null ? footerText : "";
 					}
 				}
 				//pdfTable.addCell(new Paragraph(value, font));
 				Object paragraph = paragraphConstructor.newInstance(new Object[] { value, font });
 				addCellMethod.invoke(pdfTable, new Object[] { paragraph });
 			}
         }
 	}
 	
     private void addColumnValue(Object pdfTable, UIComponent component, Object font)
 		throws IllegalAccessException, InvocationTargetException, InstantiationException {
     	String value = component == null ? "" : exportValue(FacesContext.getCurrentInstance(), component);
             
         //pdfTable.addCell(new Paragraph(value, font));
 		Object paragraph = paragraphConstructor.newInstance(new Object[] { value, font });
 		addCellMethod.invoke(pdfTable, new Object[] { paragraph });
     }
     
     private void addColumnValue(Object pdfTable, List<UIComponent> components, int index, Object font)
 		throws IllegalAccessException, InvocationTargetException, InstantiationException {
         StringBuilder builder = new StringBuilder();
         
         for (UIComponent component : components) {
         	if (component.isRendered() ) {
         		String value = exportValue(FacesContext.getCurrentInstance(), component);
                 
                 if (value != null)
                 	builder.append(value);
             }
 		}  
         
         //pdfTable.addCell(new Paragraph(builder.toString(), font));
 		Object paragraph = paragraphConstructor.newInstance(new Object[] { builder.toString(), font });
 		addCellMethod.invoke(pdfTable, new Object[] { paragraph });
     }
 }
