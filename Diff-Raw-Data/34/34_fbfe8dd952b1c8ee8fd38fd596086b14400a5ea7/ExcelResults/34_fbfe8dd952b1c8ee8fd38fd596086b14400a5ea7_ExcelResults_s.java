 package org.linkedgov.questions.pages;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.tapestry5.StreamResponse;
 import org.apache.tapestry5.annotations.Persist;
 import org.linkedgov.questions.http.ExcelStreamResponse;
 import org.linkedgov.questions.model.Pair;
 import org.linkedgov.questions.model.Triple;
 
 import uk.me.mmt.sprotocol.SparqlResource;
 
 public class ExcelResults {
 
     /**
      * A list of triples representing the results.
      */
     @Persist
     private List<Triple> triples;
     
     /**
      * Called when the page is activated, returns an excel file containing the triples in query.
      * @return
      * @throws IOException
      */
     
     @SuppressWarnings("unused")
     public StreamResponse onActivate() throws IOException{
     	
     	final StreamResponse streamResponse;
     	final Workbook wb = new HSSFWorkbook();
     	final Sheet sh = wb.createSheet();
     	createHeaderRow(sh);
     	
     	for (int rownum = 0; rownum < triples.size(); rownum++) {
     		addRow(triples.get(rownum), sh, rownum+1);
     	}
     }
     /**
      * Create a row with the headers on it.
      * 
      * @param sh
      */
     private void createHeaderRow(final Sheet sh) {
         final Row row = sh.createRow(0);
     	row.createCell(0).setCellValue("Subject");
     	row.createCell(1).setCellValue("Predicate");;
     	row.createCell(2).setCellValue("Object");;
     }
 
     /**
      * Gets the bytes from the workbook
      * 
      * @param wb - the workbook whose bytes you want.
      * @return
      * @throws IOException
      */
     private byte[] getWorkbookBytes(final Workbook wb) throws IOException {
     	final ByteArrayOutputStream bos = new ByteArrayOutputStream();
     	wb.write(bos);
     	byte[] bytes = bos.toByteArray();
     	bos.close();
     	bos.flush();
     	return bytes;
     }
     
     /**
      * 
      * Adds a row representing the passed triple to the sheet.
      * 
      * @param triples
      * @param sh
      * @param rownum
      */
     private void addRow(Triple triple, Sheet sh, int rownum) {
     	final Row row = sh.createRow(rownum);
     	addCell(row, triple.getSubject(), 0);
     	addCell(row, triple.getPredicate(), 1);
     	addCell(row, triple.getObject(), 2);
     }
 
     /**
     * Gets the bytes from the workbook
     * 
     * @param wb - the workbook whose bytes you want.
     * @return
     * @throws IOException
     */
    private byte[] getWorkbookBytes(final Workbook wb) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        byte[] bytes = bos.toByteArray();
        bos.close();
        bos.flush();
        return bytes;
    }

    /**
     * 
     * Adds a row representing the passed triple to the sheet.
     * 
     * @param triples
     * @param sh
     * @param rownum
     */
    private void addRow(Triple triple, Sheet sh, int rownum) {
        final Row row = sh.createRow(rownum);
        addCell(row, triple.getSubject(), 0);
        addCell(row, triple.getPredicate(), 1);
        addCell(row, triple.getObject(), 2);
    }

    /**
      * Adds a cell representing the given resource to the row. 
      * 
      * @param row - the row to add to
      * @param subject - the resource that the cell will represent
      * @param index - the index along the row at which the cell will be located.
      * @return
      */
     private void addCell(Row row, Pair<SparqlResource, String> resource, int index) {
         final Cell cell = row.createCell(index);
         final String cellValue = StringUtils.isBlank(resource.getSecond()) ? resource.getFirst().getValue() : resource.getSecond();
         cell.setCellValue(cellValue);
     }
 
     /**
      * Set the triples to be outputted.
      * 
      * @param triples
      */
     public void setTriples(List<Triple> triples) {
         this.triples = triples;
     }
 }
