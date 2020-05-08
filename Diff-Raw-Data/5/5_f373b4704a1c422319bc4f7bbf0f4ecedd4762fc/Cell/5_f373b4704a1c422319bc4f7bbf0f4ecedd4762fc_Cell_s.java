 package com.comp5541.spreadsheet.model;
 
 import com.comp5541.spreadsheet.exceptions.InvalidFormulaException;
 import com.comp5541.spreadsheet.exceptions.InvalidValueException;
 
 import de.congrace.exp4j.Calculable;
 import de.congrace.exp4j.ExpressionBuilder;
 import de.congrace.exp4j.UnknownFunctionException;
 import de.congrace.exp4j.UnparsableExpressionException;
 
 /**
  * Cell class - basic unit of the spreadsheet
  * @author Huantao, Hojabr, Santhosh, Yi, Nick, Amy
  */
 public class Cell {
 	protected String sCellname;
 	protected final Double nDefaultValue = 0.0;
 	protected String sFormula = null;
 	protected int nColumn;
 	protected int nRow;
 	protected Double nValue = null;
 	boolean bValid = false;
 	protected String sFormatting = null;
 
 	/**
 	 * Method to retrieve the name of a cell
 	 * @return cell name ("A1", "B1", ...)
 	 */
 	public String getCellname()
 	{
 		return sCellname;
 	}
 
 	/**
 	 * Method to set the name of a cell
 	 * @param sCellname cell name ("A1", "B1", ...)
 	 */
 	public void setCellname(String sCellname)
 	{
 		this.sCellname = sCellname;
 	}
 
 	/**
 	 * Method to get a cell's formula
 	 * @return cell's formula
 	 */
 	public String getFormula()
 	{
 		return sFormula;
 	}
 
 	/**
 	 * Method to set a cell's formula
 	 * @param sFormula cell's formula
 	 */
 	public void setFormula(String sFormula)
 	{
 		this.sFormula = sFormula;
 	}
 
 	/**
 	 * Method to get a cell's value
 	 * @return cell's value (Double)
 	 */
 	public Double getValue()
 	{
 		return nValue;
 	}
 
 	/**
 	 * Method to set the cell's value (Double)
 	 * @param nValue Cell's value (Double)
 	 */
 	public void setValue(Double nValue)
 	{
 		this.nValue = nValue;
 	}
 
 	/**
 	 * Method to check cell content validity
 	 * @return True if valid
 	 */
 	public boolean isValid()
 	{
 		return bValid;
 	}
 
 	/**
 	 * Method to set cell content validity
 	 * @param bValid True if valid
 	 */
 	public void setValid(boolean bValid)
 	{
 		this.bValid = bValid;
 	}
 
 	public String getFormatting()
 	{
 		return sFormatting;
 	}
 	
 	/**
 	 * Method to get a cell's default value (0.0)
 	 * @return cell's default value (0.0)
 	 */
 	public Double getDefaultValue()
 	{
 		return nDefaultValue;
 	}
 
 	/**
 	 * Method to get a cell's column index
 	 * @return cell's column index
 	 */
 	public int getColumn()
 	{
 		return nColumn;
 	}
 
 	/**
 	 * Method to set a cell's column index
 	 * @param nColumn cell's column index
 	 */
 	public void setColumn(int nColumn)
 	{
 		this.nColumn = nColumn;
 	}
 	
 	/**
 	 * Method to get a cell's row index
 	 * @return cell's row index
 	 */
 	public int getRow()
 	{
 		return nRow;
 	}
 
 	/**
 	 * Method to set a cell's row index
 	 * @param nRow cell's row index
 	 */
 	public void setRow(int nRow)
 	{
 		this.nRow = nRow;
 	}
 
 	/**
 	 * Constructor to create a new Cell
 	 * @param cellname Name of the cell ("A1", "B1", ...)
 	 */
 	public Cell(String cellname){ 
 		this.sCellname = cellname.trim();
 	}
 
 	/**
 	 * Method to check if the cell has a formula
 	 * @return True if the cell has a formula
 	 */
 	public boolean hasFormula(){
 		if(this.sFormula !=null){
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	/**
 	 * Method to check if the cell has a value
 	 * @return True if the cell has a value
 	 */
 	public boolean hasValue(){
 		if(this.nValue !=null){
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	/**
 	 * Method to check if the cell has a computed value
 	 * @return True if the cell has a computed value
 	 */
 	public boolean hasComputedValue(){
 		boolean ret=false;
 		if(this.sFormula !=null && this.nValue == null){
 			ret = false;
 		}else if(this.sFormula !=null && this.nValue != null){
 			ret = true;
 		}else if(this.sFormula ==null && this.nValue != null){
 			ret = true;
 		}else if(this.sFormula ==null && this.nValue == null){
 			ret = true;
 		}
 		return ret;
 	}
 	/**
 	 * @function To parse the formula if the cell has formula, 
 	 * and translate cell names into their corresponding values except the cell itself
 	 * @param cells
 	 * @return parsed formula (1+3+3, ...)
 	 */
 	public String parseFormula(Cell cells[][]){
 		//replace variable with computed value
 		String selfname =this.sCellname;
 		String str = this.sFormula; 
 		boolean flag = true;
 		int n=1;
 		loop:
 			while(flag){
 				for(int i=999-1;i>=0;i--){		
 					for(int j=0;j<26;j++){ 			    //get each cell's name,and replace them with primitive value				   
 						if(str.contains(selfname)){		//get the name of cell according to the formula's variable
 							this.bValid = false;
 							break loop;
 						}
 						if(str.contains(cells[i][j].sCellname)&&( !selfname.equals(cells[i][j].sCellname) )){
 							if(cells[i][j].hasComputedValue()){//if has computedvalue, replace the variable with the value
 
 								if(cells[i][j].nValue==null){//nothing in the cell, use default value
 									str = str.replace(cells[i][j].sCellname, Double.toString(cells[i][j].nDefaultValue));
 								}else{							    // the cell has nValue
 									str = str.replace(cells[i][j].sCellname, Double.toString(cells[i][j].nValue));
 								}							
 							}else{// can only replace variable with another formula
 								str = str.replace(cells[i][j].sCellname, "("+cells[i][j].sFormula.trim().substring(1)+")");
 							}
 							//System.out.println(str);
 						}
 						//check whether str has selfname
 						if(str.contains(selfname)){
 							this.bValid = false;
 							break loop;
 						}
 					}
 				}
 				if(str.matches(".*[A-Z].*")){//still have cellname in str, do while again
 					if(n>50){
 						flag = false;
 						this.bValid = false;
 					}
 				}else{
 					this.bValid = true;
 					flag = false;
 				}
 				n++;
 			}
 		return str;
 
 	}
 	/**
 	 * 
 	 * @function: cell's computeValue, cleanFormula doesn't have any variables
 	 */
 	public void computeValue(String cleanFormula) throws InvalidFormulaException{
 		if(!cleanFormula.trim().equals("")){
 			ExpressionBuilder eb = new ExpressionBuilder(cleanFormula.substring(1));
 			//eb.withVariable("x", 2).withVariable("y", 3);
 			Calculable calc;
 			try {
 				calc = eb.build();
 				this.nValue=calc.calculate();
 			} catch (UnknownFunctionException e) {
 				throw new InvalidFormulaException(e.getMessage());
 			} catch (UnparsableExpressionException e) {
 				throw new InvalidFormulaException(e.getMessage());
 			}
 			
 		}else{
 			throw new InvalidFormulaException("Empty formula");
 		}
 	}
 
 	/**
 	 * Method to validate cell content (called before setting the cell content)
 	 * @return "formula" if it is a formula, "value" if it is a primitive value, "error" if there is an error
 	 * @throws InvalidFormulaException thrown if the formula is invalid
 	 * @throws InvalidValueException thrown if the value is invalid
 	 */
 	public static String validateContent(String content) throws InvalidFormulaException, InvalidValueException{
 		String ret ="error";
 		String cont[] = content.trim().split("");
 		if(cont[1].equals("=")&&cont.length>2){//check if it's a formula
 			ret = "formula";
 			for(int i=2;i<cont.length;i++){
 				if(!cont[i].matches( "^[A-Z0-999+\\-*/()]$" )){
 					throw new InvalidFormulaException("The formula you entered does not match the syntax of a valid formula.");
 				}
 			}
			if(content.trim().substring(1).matches(".*[A-K].*")){
 				//check for formula with too large cell range
				if(!content.trim().substring(1).matches(".*[A-k]([1-9][+\\-*/()].*|[1-9]|10[+\\-*/()].*)")){
 					throw new InvalidFormulaException("A cell name in the formula that you entered is out of range.");
 				}
 			}
 		
 		}else if(cont[1].equals("=")&&cont.length<=2){
 			throw new InvalidFormulaException("Did you mean to enter a cell value? If so, do not add '=' at the start.");
 		}else{//check if it's an value
 			ret = "value";
 			//check if formating is assigned
 			int formIndx = content.indexOf(':');
 			if (formIndx>0)
 			{
 				String formatting = content.substring(formIndx+1);
 				if (!formatting.matches("^[msiMSI]$"))
 					throw new InvalidValueException("Invalid formating. Possible values for the formating M,S,I.");
 				content = content.substring(0, formIndx);
 			}
 			if(!content.matches("\\d{1,5}(\\.\\d{1,5})?")){//( "^[0-9.]$" )){
 				throw new InvalidValueException("Invalid value. Did you mean to enter a formula? If so, please add '=' at the start of the formula.");
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * Method to set cell content
 	 * @param content Cell content
 	 * @return True if valid and set
 	 * @throws InvalidFormulaException thrown if the formula is invalid (caught/handled in controller)
 	 * @throws InvalidValueException thrown if the value is invalid (caught/handled in controller)
 	 */
 	public boolean setCellContent(String content) throws InvalidFormulaException, InvalidValueException{
 		boolean ret;
 				
 		String result = validateContent(content);
 		if(result.equals("value")){
 			int formIndx = content.indexOf(':');
 			if (formIndx>0)
 			{ 
 				this.sFormatting = content.substring(formIndx+1).toUpperCase();
 				content = content.substring(0, formIndx);
 			} else this.sFormatting = "";			
 			this.nValue = Double.parseDouble(content);
 			this.bValid = true;
 			ret = true;
 		}else if(result.equals("formula")){
 			this.sFormula = content;
 			this.bValid = true;
 			ret = true;
 		}else{
 			ret = false;
 		}	
 		return ret;
 	}
 
 	/**
 	 * For user to view content of cell value, to be used for usecase 2
 	 * @throws InvalidFormulaException thrown if formula is invalid (caught/handled in controller)
 	 * @throws InvalidValueException thrown if value is invalid (caught/handled in controller)
 	 */
 	public String getCellValue(Cell cells[][]) throws InvalidFormulaException, InvalidValueException{
 		String ret = "";
 		if(this.sFormula != null) {// if this cell has formula,
 			// first validate it
 			if (!Cell.validateContent(this.sFormula).equals("error")) {
 				//then parse it
 				String result = this.parseFormula(cells);
 				if (this.bValid) {
 					this.computeValue(result);
 					ret = this.nValue.toString();
 				}else{
 					throw new InvalidFormulaException();
 				}
 				
 			}else{
 				throw new InvalidFormulaException();
 			}
 						
 		}else if (this.nValue == null) {// if this cell is empty
 			this.bValid = true;
 			this.nValue = this.nDefaultValue;
 			ret = this.nValue.toString();
 		}else{
 			ret = this.nValue.toString();
 		}
 		
 		return ret;
 	}
 	
 
 }
