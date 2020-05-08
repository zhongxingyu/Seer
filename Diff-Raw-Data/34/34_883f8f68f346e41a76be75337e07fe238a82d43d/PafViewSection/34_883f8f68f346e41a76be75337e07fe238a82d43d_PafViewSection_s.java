 /**
  * @(#) ViewSection.java
  */
 
 package com.pace.base.view;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.pace.base.IPafCompressedObj;
 import com.pace.base.PafBaseConstants;
 import com.pace.base.app.AliasMapping;
 import com.pace.base.app.SuppressZeroSettings;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.db.cellnotes.SimpleCellNote;
 import com.pace.base.db.membertags.MemberTagCommentEntry;
 import com.pace.base.db.membertags.MemberTagCommentPosition;
 import com.pace.base.db.membertags.MemberTagViewSectionData;
 import com.pace.base.mdb.SortingTuples;
 import com.pace.base.utility.CompressionUtil;
 
 public class PafViewSection implements Cloneable, IPafCompressedObj {
 		
 	public static final int MEASURE_PAGE_AXIS = 2;
 
 	public static final int VERSION_PAGE_AXIS = 4;
 
 	public static final int MEASURE_ROW_AXIS = 8;
 
 	public static final int VERSION_ROW_AXIS = 16;
 
 	public static final int MEASURE_COL_AXIS = 32;
 
 	public static final int VERSION_COL_AXIS = 64;
 
 	private String name;
 
 	private String description;
 
 	private String dataAlias;
 
 	private int primaryFormattingAxis;
 
 	private boolean readOnly;
 
 	private boolean managed;
 	
 	private boolean isEmpty = false;
 
 	private int topOffset;
 
 	private int leftOffset;
 
 	@SuppressWarnings("unused")
 	private PafScaleUnit scaleUnit = PafScaleUnit.CELL;
 
 	private PafViewHeader[] pafViewHeaders;
 
 	private String[] rowAxisDims;
 
 	private String[] colAxisDims;
 
 	private ViewTuple[] rowTuples;
 
 	private ViewTuple[] colTuples;
 
 	private PageTuple[] pageTuples;
 
 	private PafDataSlice pafDataSlice = null;
 
 	private LockedCell[] notPlannableLockedCells = null;
 
 	private LockedCell[] forwardPlannableLockedCell = null;
 	
 	private String generationFormatName;
 	
 	private String hierarchyFormatName;
 	
 	private Integer rowHeight;
 	
 	private Integer columnWidth;
 	
 	private Boolean hasAttributes;
 	
 	private String[] attributeDims;
 	
 	private String[] dimensionsPriority;
 	
 	private String[] dimensionCalcSequence;
 	
 	private String[] dimensions;
 	
 	private LockedCell[] invalidAttrIntersectionsLC;
 	
 	private Set<Intersection> invalidAttrIntersections;
 	
 	private LockedCell[] invalidMemberTagIntersectionsLC;
 
 	private SimpleCoordList[] invalidReplicationIntersections;
 
 	private boolean isCompressed;
     
     private String elementDelim;;
     
     private String groupDelim;
     
     private String compInvalidIntersectionsLC;
     
     private String compNotPlannableLockedCells;
     
     private String compForwardPlannableLockedCell;
     
 	private String compCellNotes;
     
 	private String compRowMemberTagData;
 
 	private String compColMemberTagData;
 
 	private SimpleCellNote[] cellNotes;
 	
 	private MemberTagViewSectionData[] rowMemberTagData;
     
 	private MemberTagViewSectionData[] colMemberTagData;
     
 	private MemberTagCommentEntry[] memberTagCommentEntries;
 	
 	private MemberTagCommentPosition[] rowMemberTagCommentPositions;
 	
 	private MemberTagCommentPosition[] colMemberTagCommentPositions;
 
 	private AliasMapping[] aliasMappings;
     
     private SuppressZeroSettings suppressZeroSettings;
     
     private boolean isSuppressed; 
     
     private String elementDelimiterRowMemberTag;
     
     private String groupDelimiterRowMemberTag;
     
     private String elementDelimiterColMemberTag;
     
     private String groupDelimiterColMemberTag;
     
 	private SortingTuples sortingTuples;
     
 	
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public PafDataSlice getPafDataSlice() {
 		return pafDataSlice;
 	}
 
 	public ViewTuple[] getColTuples() {
 		return colTuples;
 	}
 
 	public void setColTuples(ViewTuple[] colTuples) {
 		this.colTuples = colTuples;
 	}
 
 	public ViewTuple[] getRowTuples() {
 		return rowTuples;
 	}
 
 	public void setRowTuples(ViewTuple[] rowTuples) {
 		this.rowTuples = rowTuples;
 	}
 
 	public void setPafDataSlice(PafDataSlice pafDataSlice) {
 		this.pafDataSlice = pafDataSlice;
 	}
 
 	public String getDataAlias() {
 		return dataAlias;
 	}
 
 	public void setDataAlias(String dataAlias) {
 		this.dataAlias = dataAlias;
 	}
 
 	public boolean isManaged() {
 		return managed;
 	}
 
 	public void setManaged(boolean managed) {
 		this.managed = managed;
 	}
 
 	public int getPrimaryFormattingAxis() {
 		return primaryFormattingAxis;
 	}
 
 	public void setPrimaryFormattingAxis(int primaryFormattingAxis) {
 		this.primaryFormattingAxis = primaryFormattingAxis;
 	}
 
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	public String[] getColAxisDims() {
 		return colAxisDims;
 	}
 
 	public void setColAxisDims(String[] colAxisDims) {
 		this.colAxisDims = colAxisDims;
 	}
 
 	public PageTuple[] getPageTuples() {
 		return pageTuples;
 	}
 
 	public void setPageTuples(PageTuple[] pageTuples) {
 		this.pageTuples = pageTuples;
 	}
 
 	/**
 	 *  Return the page dimensions
 	 *
 	 * @return String[]
 	 */
 	public String[] getPageAxisDims() {	
 		int i = 0;
 		String[] pageAxisDims = new String[pageTuples.length];
 		for (PageTuple pageTuple:pageTuples) {
 			pageAxisDims[i++] = pageTuple.getAxis();
 		}
 		return pageAxisDims;
 	}
 	
 	/**
 	 *  Return the page members
 	 *
 	 * @return String[]
 	 */
 	public String[] getPageAxisMembers() {	
 		int i = 0;
 		String[] pageAxisMembers = new String[pageTuples.length];
 		for (PageTuple pageTuple:pageTuples) {
 			pageAxisMembers[i++] = pageTuple.getMember();
 		}
 		return pageAxisMembers;
 	}
 	
 	public String[] getRowAxisDims() {
 		return rowAxisDims;
 	}
 
 	public void setRowAxisDims(String[] rowAxisDims) {
 		this.rowAxisDims = rowAxisDims;
 	}
 
 	/**
 	 * @return Returns the leftOffset.
 	 */
 	public int getLeftOffset() {
 		return leftOffset;
 	}
 
 	/**
 	 * @param leftOffset
 	 *            The leftOffset to set.
 	 */
 	public void setLeftOffset(int leftOffset) {
 		this.leftOffset = leftOffset;
 	}
 
 	/**
 	 * @return Returns the topOffset.
 	 */
 	public int getTopOffset() {
 		return topOffset;
 	}
 
 	/**
 	 * @param topOffset
 	 *            The topOffset to set.
 	 */
 	public void setTopOffset(int topOffset) {
 		this.topOffset = topOffset;
 	}
 
 	public PafViewHeader[] getPafViewHeaders() {
 		return pafViewHeaders;
 	}
 
 	public void setPafViewHeaders(PafViewHeader[] pafViewHeaders) {
 		this.pafViewHeaders = pafViewHeaders;
 	}
 
 	public PafAxis getAxis(String dimensionStr) {
 
 		PafAxis axis = null;
 
 		if (pageTuples != null) {
 			for (PageTuple tuple : pageTuples) {
 				if (tuple.getAxis().equals(dimensionStr)) {
 					axis = new PafAxis(PafAxis.PAGE);
 					break;
 				}
 			}
 		}
 
 		if (colAxisDims != null && axis == null) {
 			for (String colDim : colAxisDims) {
 				if (colDim.equals(dimensionStr)) {
 					axis = new PafAxis(PafAxis.COL);
 					break;
 				}
 			}
 		}
 
 		if (rowAxisDims != null && axis == null) {
 			for (String rowDim : rowAxisDims) {
 				if (rowDim.equals(dimensionStr)) {
 					axis = new PafAxis(PafAxis.ROW);
 					break;
 				}
 			}
 		}
 
 		return axis;
 
 	}
 
 	public LockedCell[] getForwardPlannableLockedCell() {
 		return forwardPlannableLockedCell;
 	}
 
 	public void setForwardPlannableLockedCell(
 			LockedCell[] forwardPlannableLockedCell) {
 		this.forwardPlannableLockedCell = forwardPlannableLockedCell;
 	}
 
 	public LockedCell[] getNotPlannableLockedCells() {
 		return notPlannableLockedCells;
 	}
 
 	public void setNotPlannableLockedCells(LockedCell[] notPlannableLockedCells) {
 		this.notPlannableLockedCells = notPlannableLockedCells;
 	}
 
 	public String toString() {
 
 		StringBuffer strBuff = new StringBuffer();
 
 		strBuff.append("Name: " + this.name + "\n");
 		strBuff.append("Desc: " + this.description + "\n");
 		strBuff.append("read only: " + this.readOnly + "\n");
 		strBuff.append("PFA: " + this.primaryFormattingAxis + "\n");
 		strBuff.append("Headers: " + this.pafViewHeaders + "\n");
 		strBuff.append("Page Tuples:");
 
 		if (pageTuples != null) {
 			for (PageTuple pageTuple : pageTuples) {
 				strBuff.append("\n\t" + pageTuple.toString());
 			}
 		}
 
 		strBuff.append("\nColumn Tuples:");
 		if (colTuples != null) {
 			for (ViewTuple colTuple : colTuples) {
 				strBuff.append("\n\t" + colTuple.toString());
 			}
 		}
 
 		strBuff.append("\nRow Tuples:");
 		if (rowTuples != null) {
 			for (ViewTuple rowTuple : rowTuples) {
 				strBuff.append("\n\t" + rowTuple);
 			}
 		}
 		
 		return strBuff.toString();
 	}
 
 	@Override
 	public PafViewSection clone() throws CloneNotSupportedException {
 
 		PafViewSection viewSection = (PafViewSection) super.clone();
 
 		// Clone page headers
 		if (this.pafViewHeaders != null) {
 			viewSection.pafViewHeaders = new PafViewHeader[this.pafViewHeaders.length];
 			for (int i = 0; i < this.pafViewHeaders.length; i++) {
 				viewSection.pafViewHeaders[i] = this.pafViewHeaders[i].clone();
 			}
 		}
 
 		// Clone row tuples
 		if (this.rowTuples != null) {
 			viewSection.rowTuples = new ViewTuple[this.rowTuples.length];
 			for (int i = 0; i < this.rowTuples.length; i++) {
 				viewSection.rowTuples[i] = this.rowTuples[i].clone();
 			}
 		}
 
 		// Clone column tuples
 		if (this.colTuples != null) {
 			viewSection.colTuples = new ViewTuple[this.colTuples.length];
 			for (int i = 0; i < this.colTuples.length; i++) {
 				viewSection.colTuples[i] = this.colTuples[i].clone();
 			}
 		}
 		
 		// Clone page tuples
 		if (this.pageTuples != null) {
 			viewSection.pageTuples = new PageTuple[this.pageTuples.length];
 			for (int i = 0; i < this.pageTuples.length; i++) {
 				viewSection.pageTuples[i] = this.pageTuples[i].clone();
 			}
 		}
 
 //		System.out.println(this);
 //		System.out.println(viewSection);
 
 		// set attributes here
 
 		return viewSection;
 	}
 
 	public String getGenerationFormatName() {
 		return generationFormatName;
 	}
 
 	public void setGenerationFormatName(String generationFormatName) {
 		this.generationFormatName = generationFormatName;
 	}
 	
 	/**
 	 * @return Returns the hierarchyFormatName.
 	 */
 	public String getHierarchyFormatName() {
 		return hierarchyFormatName;
 	}
 
 	/**
 	 * @param hierarchyFormatName The hierarchyFormatName to set.
 	 */
 	public void setHierarchyFormatName(String hierarchyFormatName) {
 		this.hierarchyFormatName = hierarchyFormatName;
 	}
 	
 
 	public Integer getColumnWidth() {
 		return columnWidth;
 	}
 
 	public void setColumnWidth(Integer columnWidth) {
 		this.columnWidth = columnWidth;
 	}
 
 	public Integer getRowHeight() {
 		return rowHeight;
 	}
 
 	public void setRowHeight(Integer rowHeight) {
 		this.rowHeight = rowHeight;
 	}
 	
 
 	/*
 	 * Returns an array of dimension names for a view section
 	 * Used to build DimensionsPriority
 	 */
 	public String[] getDimensions() {
 		
 		if (dimensions == null){
 			int i = 0;
 			dimensions = new String[getPageTuples().length + getRowAxisDims().length + getColAxisDims().length];
 			
 			for(PageTuple tuple : getPageTuples()){
 				dimensions[i++] = tuple.getAxis();
 			}
 			
 			for(String dimName : getRowAxisDims()){
 				dimensions[i++] = dimName;
 			}
 			
 			for(String dimName : getColAxisDims()){
 				dimensions[i++] = dimName;
 			}
 		}
 		
 		return dimensions;
 	}
 	
 
 	/**
 	 *  Return the dimension index for the supplied dimension in
 	 *  whatever axis that dimension is defined in.
 	 *
 	 * @param dimension Dimension name
 	 * @return Dimension index
 	 */
 	public int getDimensionIndex(String dimension) {
 
 		PafAxis axis = getAxis(dimension);
 		String[] axisDims = null;
 		
 		// Check for bad dimension name
 		if (axis == null) {
 			String errMsg = "Unable to get the dimension index for the specified dimension [" 
 						+ dimension + "] - the dimension is not defined to the view section";
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Get the axis dimensions to search
 		switch (axis.getValue()) {
 		case PafAxis.PAGE:
 			axisDims = getPageAxisDims();
 			break;
 		case PafAxis.ROW:
 			axisDims = getRowAxisDims();
 			break;
 		default:
 			axisDims = getColAxisDims();
 		}
 				
 		// Check axis members for dimension
 		for (int i = 0; i < axisDims.length; i++) {
 			String axisDim = axisDims[i];
 			if (axisDim.equals(dimension)) {
 				return i;
 			}
 		}
 		
 		// Dimension not found - throw exception
 		String errMsg = "Unable to get the dimension index for the specified dimension [" 
 					+ dimension + "] - the dimension is defined to the view section but could not be found";
 		throw new IllegalArgumentException(errMsg);
 
 
 	}
 
 
 	/**
 	 *  Return an array of unique member names for the specified dimension name
 	 *
 	 * @param dimension Dimension name
 	 * @return String[]
 	 */
 	public String[] getDimensionMembers(String dimension) {
 
 		PafAxis axis = getAxis(dimension);
 		
 		// Check for bad dimension name
 		if (axis == null) {
 			String errMsg = "Unable to get the member names for the specified dimension [" 
 						+ dimension + "] - the dimension is not defined to the view section";
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Check page dimension
 		if (axis.getValue() == PafAxis.PAGE) {
 			for (int i = 0; i < getPageAxisDims().length; i++) {
 				String axisDim = getPageAxisDims()[i];
 				if (axisDim.equals(dimension)) {
 					String[] members = new String[]{getPageAxisMembers()[i]};
 					return members;
 				}
 			}
 		}
 		
 		// Check column dimension
 		if (axis.getValue() == PafAxis.COL) {
 			for (int i = 0; i < getColAxisDims().length; i++) {
 				String axisDim = getColAxisDims()[i];
 				if (axisDim.equals(dimension)) {
 					ViewTuple[] tuples = getColTuples();
 					Set<String> memberList = new HashSet<String>();
 					for (ViewTuple tuple : tuples) {
 						// Filter out member tags and paf blanks
 						String member = tuple.getMemberDefs()[i];
 						if (!member.equals(PafBaseConstants.PAF_BLANK) && !tuple.isMemberTag()) {
 							memberList.add(tuple.getMemberDefs()[i]);
 						}
 					}					
 					return memberList.toArray(new String[0]);						
 				}
 			}
 		}
 		
 		// Check row dimension
 		if (axis.getValue() == PafAxis.ROW) {
 			for (int i = 0; i < getRowAxisDims().length; i++) {
 				String axisDim = getRowAxisDims()[i];
 				if (axisDim.equals(dimension)) {
 					ViewTuple[] tuples = getRowTuples();
 					Set<String> memberList = new HashSet<String>();
 					for (ViewTuple tuple : tuples) {
 						// Filter out member tags and paf blanks
 						String member = tuple.getMemberDefs()[i];
 						if (!member.equals(PafBaseConstants.PAF_BLANK) && !tuple.isMemberTag()) {
 							memberList.add(tuple.getMemberDefs()[i]);
 						}
 					}					
 					return memberList.toArray(new String[0]);						
 				}
 			}
 		}
 		
 		// Member not found - throw exception
 		String errMsg = "Unable to get the member names for the specified dimension [" 
 					+ dimension + "] - the dimension is defined to the view section but could not be found";
 		throw new IllegalArgumentException(errMsg);
 
 	}
 
 
 	public void setHasAttributes(Boolean hasAttributes) {
 		this.hasAttributes = hasAttributes;
 	}
 	
 	public Boolean hasAttributes() {
 		return hasAttributes;
 	}
 	
 	
 	
 	public void setAttributeDims(String[] attributeDims) {
 		this.attributeDims = attributeDims;
 	}
 	
 	public String[] getAttributeDims() {
 		return attributeDims;
 	}
 	
 	public void setDimensionsPriority(String[] dimensionsPriority) {
 		this.dimensionsPriority = dimensionsPriority;
 	}
 	
 	public String[] getDimensionsPriority() {
 		return dimensionsPriority;
 	}
 	
 	public void setDimensionCalcSequence(String[] dimensionCalcSequence) {
 		this.dimensionCalcSequence = dimensionCalcSequence;
 	}
 
 	public String[] getDimensionCalcSequence() {
 		return dimensionCalcSequence;
 	}
 	
 	/**
 	 *	Return number of cell represented in view section
 	 *
 	 * @return Cell count
 	 */
 	public int getCellCount() {
 		return getColCount() * getRowCount();
 	}
 
 	/**
 	 *	Return number of columns in view section
 	 *
 	 * @return Column count
 	 */
 	public int getColCount() {
 		
 		int colCount = 0;
 		
 		if (colTuples!= null) {
 			colCount = colTuples.length;
 		}
 		return colCount;
 	}
 
 	/**
 	 *	Return number of rows in view section
 	 *
 	 * @return Row count
 	 */
 	public int getRowCount() {
 		
 		int rowCount = 0;
 		
 		if (rowTuples!= null) {
 			rowCount = rowTuples.length;
 		}
 		return rowCount;
 	}
 
 
 	public void setInvalidAttrIntersectionsLC(LockedCell[] invalidIntersectionsLC) {
 		this.invalidAttrIntersectionsLC = invalidIntersectionsLC;
 	}
 	
 	public LockedCell[] getInvalidAttrIntersectionsLC() {
 		return invalidAttrIntersectionsLC;
 	}
 	
 	public void invalidAttrIntersections(Set<Intersection> invalidAttrIntersections) {
 		this.invalidAttrIntersections = invalidAttrIntersections;
 	}
 	
 	public Set<Intersection> invalidAttrIntersections() {
 		if (invalidAttrIntersections != null) {
 			return invalidAttrIntersections;
 		}
 		return new HashSet<Intersection>();
 	}
 
 	public void setInvalidMemberTagIntersectionsLC(LockedCell[] invalidMemberTagIntersectionsLC) {
 		this.invalidMemberTagIntersectionsLC = invalidMemberTagIntersectionsLC;
 	}
 	
 	public LockedCell[] getInvalidMemberTagIntersectionsLC() {
 		return invalidMemberTagIntersectionsLC;
 	}
 	
 	/**
 	 * @return the invalidReplicationIntersectionsLC
 	 */
 	public SimpleCoordList[] getInvalidReplicationIntersections() {
 		return invalidReplicationIntersections;
 	}
 
 	/**
 	 * @param invalidReplicationIntersections the invalidReplicationIntersectionsLC to set
 	 */
 	public void setInvalidReplicationIntersections(SimpleCoordList[] invalidReplicationIntersections) {
 		this.invalidReplicationIntersections = invalidReplicationIntersections;
 	}
 
 	/**
 	 * @return the isEmpty
 	 */
 	public boolean isEmpty() {
 		return isEmpty;
 	}
 	/**
 	 * @param isEmpty the isEmpty to set
 	 */
 	public void setEmpty(boolean isEmpty) {
 		this.isEmpty = isEmpty;
 	}
 	
 	public String getElementDelim() {
 		return elementDelim;
 	}
 
 
 	public void setElementDelim(String elementDelim) {
 		this.elementDelim = elementDelim;
 	}
 
 
 	public String getGroupDelim() {
 		return groupDelim;
 	}
 
 
 	public void setGroupDelim(String groupDelim) {
 		this.groupDelim = groupDelim;
 	}
 	
 	public void compressData() throws IOException {
 		
 		elementDelim = PafBaseConstants.DELIMETER_ELEMENT;
 	    groupDelim = PafBaseConstants.DELIMETER_GROUP;
 		
 		if(invalidAttrIntersectionsLC != null){
 			compInvalidIntersectionsLC = compressLockedCells(invalidAttrIntersectionsLC);
 			invalidAttrIntersectionsLC = null;
 		}
 		
 		if(notPlannableLockedCells != null){
 			compNotPlannableLockedCells = compressLockedCells(notPlannableLockedCells);
 			notPlannableLockedCells = null;
 		}
 		
 		if(forwardPlannableLockedCell != null){
 			compForwardPlannableLockedCell = compressLockedCells(forwardPlannableLockedCell);
 			forwardPlannableLockedCell = null;
 		}
 		
 		if (invalidReplicationIntersections != null) {	
 			for (SimpleCoordList invalidReplicationIs : invalidReplicationIntersections) {				
 				try {
 					invalidReplicationIs.compressData();
 				} catch (IOException e) {
 					System.out.println(e.getMessage());
 				}		
 			}		
 		}
 		
 		
 		//try to compress simple cell notes
 		if ( cellNotes != null ) {			
 			for (SimpleCellNote simpleCellNote : cellNotes) {			
 				simpleCellNote.compressData();			
 			}		
 		}
 		
 		//Get Element and Group dynamic delimiters
 		setElementDelimiterRowMemberTag(CompressionUtil.generateDynamicEscapeSequenceForArrayofArrays(rowMemberTagData, PafBaseConstants.DELIMETER_ELEMENT, PafBaseConstants.ESCAPE_CHARACTER_ELEMENT) + PafBaseConstants.DELIMETER_ELEMENT);
 		setGroupDelimiterRowMemberTag(CompressionUtil.generateDynamicEscapeSequenceForArrayofArrays(rowMemberTagData, PafBaseConstants.DELIMETER_GROUP, PafBaseConstants.ESCAPE_CHARACTER_GROUP) + PafBaseConstants.DELIMETER_GROUP);
 		
 		// Compress member tag data
 		if (rowMemberTagData != null) {
 			compRowMemberTagData = CompressionUtil.compressArrayToString(rowMemberTagData,getElementDelimiterRowMemberTag(),getGroupDelimiterRowMemberTag());
 			rowMemberTagData = null;
 		}
 		
 		//Get Element and Group dynamic delimiters
 		setElementDelimiterColMemberTag(CompressionUtil.generateDynamicEscapeSequenceForArrayofArrays(colMemberTagData, PafBaseConstants.DELIMETER_ELEMENT, PafBaseConstants.ESCAPE_CHARACTER_ELEMENT) + PafBaseConstants.DELIMETER_ELEMENT);
 		setGroupDelimiterColMemberTag(CompressionUtil.generateDynamicEscapeSequenceForArrayofArrays(colMemberTagData, PafBaseConstants.DELIMETER_GROUP, PafBaseConstants.ESCAPE_CHARACTER_GROUP) + PafBaseConstants.DELIMETER_GROUP);
 		
 		if (colMemberTagData != null) {
 			compColMemberTagData = CompressionUtil.compressArrayToString(colMemberTagData,getElementDelimiterColMemberTag(),getGroupDelimiterColMemberTag());
 			colMemberTagData = null;
 		}
 		
 		// Compress view tuples
 		if (this.rowTuples != null) {
 			rowTuples = (ViewTuple[]) CompressionUtil.compressArray(rowTuples);
 		}
 		if (this.colTuples != null) {
 			colTuples = (ViewTuple[]) CompressionUtil.compressArray(colTuples);
 		}
 		
 		
 		// View section is compressed
 		isCompressed = true;
 	}
 	
 	public void uncompressData() {
 		// not needed on the server side.
 	};
 	
 	
 	
     public boolean isCompressed() {
     	return isCompressed;
     }
     
     public void setCompressed(boolean isCompressed){
     	this.isCompressed = isCompressed;
     }  
 	
 	private String compressLockedCells(LockedCell[] lockedCells) {
 		StringBuffer sb = new StringBuffer(1500);
 		
 		for (LockedCell lc : lockedCells) {
 			sb.append(lc.getRowIndex());
 			sb.append(elementDelim);
 			sb.append(lc.getColIndex());
 			sb.append(groupDelim);
 		}
 		
 		if (sb.length() > 0)
 			sb.delete( sb.length()- groupDelim.length(), sb.length() );
 		
 		return sb.toString();
 	}
 
 	public void setCompInvalidIntersectionsLC(String compInvalidIntersectionsLC) {
 		this.compInvalidIntersectionsLC = compInvalidIntersectionsLC;
 	}
 
 	public String getCompInvalidIntersectionsLC() {
 		return compInvalidIntersectionsLC;
 	}
 
 	public void setCompNotPlannableLockedCells(String compNotPlannableLockedCells) {
 		this.compNotPlannableLockedCells = compNotPlannableLockedCells;
 	}
 
 	public String getCompNotPlannableLockedCells() {
 		return compNotPlannableLockedCells;
 	}
 
 	public void setCompForwardPlannableLockedCell(String compForwardPlannableLockedCell) {
 		this.compForwardPlannableLockedCell = compForwardPlannableLockedCell;
 	}
 
 	public String getCompForwardPlannableLockedCell() {
 		return compForwardPlannableLockedCell;
 	}
 
 	/**
 	 * @return the aliasMappings
 	 */
 	public AliasMapping[] getAliasMappings() {
 		return aliasMappings;
 	}
 
 	/**
 	 * @param aliasMappings the aliasMappings to set
 	 */
 	public void setAliasMappings(AliasMapping[] aliasMappings) {
 		this.aliasMappings = aliasMappings;
 	}
 
 	public SimpleCellNote[] getCellNotes() {
 		return cellNotes;
 	}
 
 	public void setCellNotes(SimpleCellNote[] cellNotes) {
 		this.cellNotes = cellNotes;
 	}
 
 	/**
 	 * @return the memberTagData
 	 */
 	public MemberTagViewSectionData[] getColMemberTagData() {
 		return colMemberTagData;
 	}
 
 	/**
 	 * @param memberTagData the memberTagData to set
 	 */
 	public void setColMemberTagData(MemberTagViewSectionData[] memberTagData) {
 		this.colMemberTagData = memberTagData;
 	}
 
 	/**
 	 * @return the suppressZeroSettings
 	 */
 	public SuppressZeroSettings getSuppressZeroSettings() {
 		return suppressZeroSettings;
 	}
 
 	/**
 	 * @param suppressZeroSettings the suppressZeroSettings to set
 	 */
 	public void setSuppressZeroSettings(SuppressZeroSettings suppressZeroSettings) {
 		this.suppressZeroSettings = suppressZeroSettings;
 	}
 
 	/**
 	 * @return the rowMemberTagData
 	 */
 	public MemberTagViewSectionData[] getRowMemberTagData() {
 		return rowMemberTagData;
 	}
 
 	/**
 	 * @param rowMemberTagData the rowMemberTagData to set
 	 */
 	public void setRowMemberTagData(MemberTagViewSectionData[] rowMemberTagData) {
 		this.rowMemberTagData = rowMemberTagData;
 	}
 
 	/**
 	 * @return the memberTagCommentEntries
 	 */
 	public MemberTagCommentEntry[] getMemberTagCommentEntries() {
 		if (memberTagCommentEntries == null) {
 			memberTagCommentEntries = new MemberTagCommentEntry[0];
 		}
 		return memberTagCommentEntries;
 	}
 
 	/**
 	 * @param memberTagCommentEntries the memberTagCommentEntries to set
 	 */
 	public void setMemberTagCommentEntries(MemberTagCommentEntry[] memberTagCommentEntries) {
 		this.memberTagCommentEntries = memberTagCommentEntries;
 	}
 
 	/**
 	 * @return the colMemberTagCommentPositions
 	 */
 	public MemberTagCommentPosition[] getColMemberTagCommentPositions() {
 		return colMemberTagCommentPositions;
 	}
 
 	/**
 	 * @param colMemberTagCommentPositions the colMemberTagCommentPositions to set
 	 */
 	public void setColMemberTagCommentPositions(
 			MemberTagCommentPosition[] colMemberTagCommentPositions) {
 		this.colMemberTagCommentPositions = colMemberTagCommentPositions;
 	}
 
 	/**
 	 * @return the rowMemberTagCommentPositions
 	 */
 	public MemberTagCommentPosition[] getRowMemberTagCommentPositions() {
 		return rowMemberTagCommentPositions;
 	}
 
 	/**
 	 * @param rowMemberTagCommentPositions the rowMemberTagCommentPositions to set
 	 */
 	public void setRowMemberTagCommentPositions(
 			MemberTagCommentPosition[] rowMemberTagCommentPositions) {
 		this.rowMemberTagCommentPositions = rowMemberTagCommentPositions;
 	}
 
 	/**
 	 *  Return member tag comment entries in the form of a map
 	 *
 	 * @return Map<String, MemberTagCommentEntry>
 	 */
 	public Map<String, MemberTagCommentEntry> getMemberTagCommentEntryMap() {
 		
 		// Convert member tag comment entries to map
 		Map<String, MemberTagCommentEntry> entryMap = new HashMap<String, MemberTagCommentEntry>();
 		for (MemberTagCommentEntry entry:getMemberTagCommentEntries()) {
 			entryMap.put(entry.getName(), entry);
 		}
 		
 		// Return entry map
 		return entryMap;
 	}
 
 	/**
 	 *  Create an intersection object for the supplied row and column coordinates
 	 *
 	 * @param rowInx Row index
 	 * @param colInx Column index
 	 * 
 	 * @return Intersection
 	 */
 	public Intersection createDataCacheIntersection(int rowInx, int colInx) {
 		
 		String[] dimensions = getDimensionsPriority(); // This dimension order should match the data slice cache 
 		Intersection viewIs = new Intersection(dimensions);
 		
 		// Set page dimensions
 		for (int i = 0; i < getPageAxisDims().length; i++) {
 			String dimension = getPageAxisDims()[i];
 			String member = getPageAxisMembers()[i];
 			viewIs.setCoordinate(dimension, member);
 		}
 		
 		// Set row dimensions
 		for (int i = 0; i < getRowAxisDims().length; i++) {
 				String dimension = getRowAxisDims()[i];
 				String member = getRowTuples()[rowInx].getMemberDefs()[i];
 				viewIs.setCoordinate(dimension, member);
 		}
 		
 		// Set column dimensions
 		for (int i = 0; i < getColAxisDims().length; i++) {
 			String dimension = getColAxisDims()[i];
 			String member = getColTuples()[colInx].getMemberDefs()[i];
 			viewIs.setCoordinate(dimension, member);
 		}
 		
 		// Return view intersection
 		return viewIs;
 	}
 	
 	/**
 	 *  @param isSuppressed Has the view section been suppressed
 	 *
 	 */
 
 	public void setSuppressed(boolean isSuppressed) {
 		this.isSuppressed = isSuppressed;
 	}
 	
 	/**
 	 *  Has the view section been suppressed
 	 *
 	 */
 
 	public boolean isSuppressed() {
 		return isSuppressed;
 	}
 
 	/**
 	 * @return the compRowMemberTagData
 	 */
 	public String getCompRowMemberTagData() {
 		return compRowMemberTagData;
 	}
 
 	/**
 	 * @param compRowMemberTagData the compRowMemberTagData to set
 	 */
 	public void setCompRowMemberTagData(String compRowMemberTagData) {
 		this.compRowMemberTagData = compRowMemberTagData;
 	}
 
 	/**
 	 * @return the compColMemberTagData
 	 */
 	public String getCompColMemberTagData() {
 		return compColMemberTagData;
 	}
 
 	/**
 	 * @param compColMemberTagData the compColMemberTagData to set
 	 */
 	public void setCompColMemberTagData(String compColMemberTagData) {
 		this.compColMemberTagData = compColMemberTagData;
 	}
 
 	public void setElementDelimiterRowMemberTag(String elementDelimiterRowMemberTag) {
 		this.elementDelimiterRowMemberTag = elementDelimiterRowMemberTag;
 	}
 
 	public String getElementDelimiterRowMemberTag() {
 		return elementDelimiterRowMemberTag;
 	}
 
 	public void setGroupDelimiterRowMemberTag(String groupDelimiterRowMemberTag) {
 		this.groupDelimiterRowMemberTag = groupDelimiterRowMemberTag;
 	}
 
 	public String getGroupDelimiterRowMemberTag() {
 		return groupDelimiterRowMemberTag;
 	}
 
 	public void setElementDelimiterColMemberTag(String elementDelimiterColMemberTag) {
 		this.elementDelimiterColMemberTag = elementDelimiterColMemberTag;
 	}
 
 	public String getElementDelimiterColMemberTag() {
 		return elementDelimiterColMemberTag;
 	}
 
 	public void setGroupDelimiterColMemberTag(String groupDelimiterColMemberTag) {
 		this.groupDelimiterColMemberTag = groupDelimiterColMemberTag;
 	}
 
 	public String getGroupDelimiterColMemberTag() {
 		return groupDelimiterColMemberTag;
 	}
 
     public SortingTuples getSortingTuples() {
 		return sortingTuples;
 	}
 
 	public void setSortingTuples(SortingTuples sortingTuples) {
 		this.sortingTuples = sortingTuples;
 	}
 	
 }
