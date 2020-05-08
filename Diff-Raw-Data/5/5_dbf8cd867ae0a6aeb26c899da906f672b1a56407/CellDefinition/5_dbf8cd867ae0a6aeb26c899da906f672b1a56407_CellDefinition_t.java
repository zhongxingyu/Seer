 /*
  * Copyright 2013 ENERKO Informatik GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.enerko.reports2.engine;
 
 import java.lang.reflect.Method;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.poi.hssf.usermodel.HSSFDateUtil;
 import org.apache.poi.ss.usermodel.Cell;
 
 /**
  * Represents a worksheet cell and corresponds with the 
  * PL/SQL object type t_hre_cell_definition
  * @author Michael J. Simons, 2013-06-17
  */
 public class CellDefinition {	
 	/**
 	 * Points to another cell by sheetname, columan and row
 	 */
 	public static class CellPointer {
 		public final String sheetname;
 		public final int column;
 		public final int row;
 		
 		public CellPointer(String sheetname, int column, int row) {
 			this.sheetname = sheetname;
 			this.column = column;
 			this.row = row;
 		}
 
 		@Override
 		public String toString() {
 			return "CellPointer [sheetname=" + sheetname + ", column=" + column
 					+ ", row=" + row + "]";
 		}					
 	}
 	
 	private static class CellValue {
 		public final String type;
 		public final String representation;
 				
 		public CellValue(String type, String representation) {
 			this.type = type;
 			this.representation = representation;
 		}		
 	}
 	
 	/** Pattern to split the {@link #type} into the actual type and a reference cell */
 	public final static Pattern FORMAT_PATTERN = Pattern.compile("(\\w+)(\\s*;\\s*\"([^\"]+)\"\\s*(\\w{1,3}\\d{1,}))?");
 	
 	/** The name of the sheet inside the Excel document */
 	public final String sheetname;
 	/** Column Index (0-based) */
 	public final int column;
 	/** Row Index (0-based) */
 	public final int row;
 	/** Cellreference ("A1" notation), only used for output) */
 	public final String name;
 	/** Contains either type or type and a reference cell as  'datentyp; "SHEETNAME" CELLREFERENCE' */
 	private final String type;
 	/** A string representation of the value */
 	public final String value;
 	/** An optional cell comment */
 	public final CommentDefinition comment;
 	
 	/** Actual used datatype */
 	private String actualType;
 	/** A reference cell */
 	private CellPointer referenceCell;
 	
 	public CellDefinition(String sheetname, int column, int row, String name, String type, String value) {
 		this(sheetname, column, row, name, type, value, null);
 	}
 	
 	public CellDefinition(String sheetname, int column, int row, String name, String type, String value, CommentDefinition comment) {
 		this.sheetname = sheetname;
 		this.column = column;
 		this.row = row;
 		this.name = name;
 		this.type = type;
 		this.value = value;
 		this.comment = comment;
 	}	
 	
 	public CellDefinition(final String sheetname, final Cell cell) {
 		final int ct = cell.getCellType();
 	
 		Method m = null;
 		try {
 			m = this.getClass().getDeclaredMethod("parse_" + Report.IMPORTABLE_CELL_TYPES.get(new Integer(ct)), new Class[]{Cell.class});
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(m == null)
 				throw new RuntimeException("Invalid type " + ct);
 		}	
 		
 		try {
 			final CellValue cellValue =  (CellValue) m.invoke(this, new Object[]{cell});
 			this.sheetname = sheetname;
 			this.column = cell.getColumnIndex();
 			this.row = cell.getRowIndex();
 			this.name = CellReferenceHelper.getCellReference(cell.getColumnIndex(), cell.getRowIndex());
 			this.type = cellValue.type;
 			this.value = cellValue.representation;
 			if(cell.getCellComment() == null || cell.getCellComment().getString() == null)
 				this.comment = null;
 			else 				
 				this.comment = new CommentDefinition(cell.getCellComment());
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} 			
 	}
 	
 	protected CellValue parse_string(Cell in) {			
 		return new CellValue("string", in.getStringCellValue());
 	}
 	
 	protected CellValue parse_number(Cell in) {
 		CellValue rv = null;
 
 		try {
 			if(HSSFDateUtil.isCellDateFormatted(in)) {
 				rv = new CellValue("datetime", Report.DATEFORMAT_OUT.format(in.getDateCellValue()));
 			} else {
 				rv = new CellValue("number", Double.toString(in.getNumericCellValue()));				
 			}
 		} catch(IllegalStateException e) {
 			// Siehe Dokumentation getNumericCellValue
 			rv = new CellValue("string", in.getStringCellValue());
 		}
 
 		return rv;
 	}
 	
 	public String getType() {
 		if(this.actualType == null)
 			this.computeActualTypeAndReferenceCell();	
 		return this.actualType;
 	}
 	
 	public CellPointer getReferenceCell() {
 		if(this.actualType == null)
 			this.computeActualTypeAndReferenceCell();
 		return referenceCell;
 	}
 
 	public void setReferenceCell(CellPointer referenceCell) {
 		this.referenceCell = referenceCell;
 	}
 
 	private void computeActualTypeAndReferenceCell() {
 		final Matcher m = FORMAT_PATTERN.matcher(this.type);
 		if(!m.matches())
 			throw new RuntimeException("Invalid type definition: " + type);
 		this.actualType = m.group(1);		
 		this.referenceCell = m.group(2) == null ? null :new CellPointer(m.group(3), CellReferenceHelper.getColumn(m.group(4)), CellReferenceHelper.getRow(m.group(4)));		
 	}
 	
 	public Object[] toSQLStructObject() {
 		return new Object[] {
 				this.sheetname,
 				this.column,
 				this.row,
 				this.name,
 				this.type,
 				this.value,
				this.comment == null ? null : this.comment.toSQLStructObject()
 		};
 	}
 	
 	public boolean hasComment() {
 		return this.comment != null && this.comment.text != null && this.comment.text.trim().length() != 0;
 	}
 }
