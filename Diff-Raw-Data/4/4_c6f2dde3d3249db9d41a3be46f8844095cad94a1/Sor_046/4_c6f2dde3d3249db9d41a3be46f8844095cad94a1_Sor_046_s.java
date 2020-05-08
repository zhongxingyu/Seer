 /**
  * Copyright (C) 2014  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.dictionary.tools.table;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lh.dmlj.schema.editor.dictionary.tools.jdbc.JdbcTools;
 import org.lh.dmlj.schema.editor.dictionary.tools.jdbc.TableColumn;
 
 public class Sor_046 {
 	
 	@TableColumn public static final String ROWID = "SOR_046_ROWID";
 	@TableColumn public static final String INDEX_DISP_046 = "INDEX_DISP_046";
 	@TableColumn public static final String INDEX_MEMBERS_046 = "INDEX_MEMBERS_046";
 	@TableColumn public static final String NXT_DBK_046 = "NXT_DBK_046";
 	@TableColumn public static final String ORD_046 = "ORD_046";
 	@TableColumn public static final String PAGE_COUNT_046 = "PAGE_COUNT_046";
	@TableColumn public static final String PAGE_COUNT_PERCENT_046 = "PAGE_COUNT_046";
	@TableColumn public static final String PAGE_OFFSET_046 = "PAGE_OFFSET_PERCENT_046";
 	@TableColumn public static final String PAGE_OFFSET_PERCENT_046 = "PAGE_OFFSET_PERCENT_046";
 	@TableColumn public static final String PRI_DBK_046 = "PRI_DBK_046";
 	@TableColumn public static final String SA_NAM_046 = "SA_NAM_046";
 	@TableColumn public static final String SET_MODE_046 = "SET_MODE_046";
 	@TableColumn public static final String SET_NAM_046 = "SET_NAM_046";
 	@TableColumn public static final String SET_ORD_046 = "SET_ORD_046";
 	@TableColumn public static final String SOR_ID_046 = "SOR_ID_046";
 	@TableColumn public static final String SUBAREA_046 = "SUBAREA_046";
 	@TableColumn public static final String SYMBOL_INDEX_046 = "SYMBOL_INDEX_046";
 	
 	public static final String COLUMNS = JdbcTools.columnsFor(Sor_046.class);
 	
 	private Srcd_113 srcd_113;
 	private List<Smr_052> smr_052s = new ArrayList<>();
 	
 	private long dbkey;
 	private short indexDisp_046;
 	private short indexMembers_046;
 	private short nxtDbk_046;
 	private short ord_046;
 	private int pageCount_046;
 	private short pageCountPercent_046;
 	private int pageOffset_046;
 	private short pageOffsetPercent_046;
 	private short priDbk_046;
 	private String saNam_046;
 	private short setMode_046;
 	private String setNam_046;
 	private short setOrd_046;
 	private short sorId_046;
 	private String subarea_046;
 	private String symbolIndex_046;
 	
 	public Sor_046() {
 		super();
 	}
 
 	public long getDbkey() {
 		return dbkey;
 	}
 
 	public short getIndexDisp_046() {
 		return indexDisp_046;
 	}
 
 	public short getIndexMembers_046() {
 		return indexMembers_046;
 	}
 
 	public short getNxtDbk_046() {
 		return nxtDbk_046;
 	}
 
 	public short getOrd_046() {
 		return ord_046;
 	}
 
 	public int getPageCount_046() {
 		return pageCount_046;
 	}
 
 	public short getPageCountPercent_046() {
 		return pageCountPercent_046;
 	}
 
 	public int getPageOffset_046() {
 		return pageOffset_046;
 	}
 
 	public short getPageOffsetPercent_046() {
 		return pageOffsetPercent_046;
 	}
 
 	public short getPriDbk_046() {
 		return priDbk_046;
 	}
 
 	public String getSaNam_046() {
 		return saNam_046;
 	}
 
 	public short getSetMode_046() {
 		return setMode_046;
 	}
 
 	public String getSetNam_046() {
 		return setNam_046;
 	}
 
 	public short getSetOrd_046() {
 		return setOrd_046;
 	}
 
 	public List<Smr_052> getSmr_052s() {
 		return smr_052s;
 	}
 
 	public short getSorId_046() {
 		return sorId_046;
 	}
 
 	public Srcd_113 getSrcd_113() {
 		return srcd_113;
 	}
 
 	public String getSubarea_046() {
 		return subarea_046;
 	}
 
 	public String getSymbolIndex_046() {
 		return symbolIndex_046;
 	}
 
 	public void setDbkey(long dbkey) {
 		this.dbkey = dbkey;
 	}
 
 	public void setIndexDisp_046(short indexDisp_046) {
 		this.indexDisp_046 = indexDisp_046;
 	}
 
 	public void setIndexMembers_046(short indexMembers_046) {
 		this.indexMembers_046 = indexMembers_046;
 	}
 
 	public void setNxtDbk_046(short nxtDbk_046) {
 		this.nxtDbk_046 = nxtDbk_046;
 	}
 
 	public void setPageCount_046(int pageCount_046) {
 		this.pageCount_046 = pageCount_046;
 	}
 
 	public void setPageCountPercent_046(short pageCountPercent_046) {
 		this.pageCountPercent_046 = pageCountPercent_046;
 	}
 
 	public void setPageOffset_046(int pageOffset_046) {
 		this.pageOffset_046 = pageOffset_046;
 	}
 
 	public void setPageOffsetPercent_046(short pageOffsetPercent_046) {
 		this.pageOffsetPercent_046 = pageOffsetPercent_046;
 	}
 
 	public void setPriDbk_046(short priDbk_046) {
 		this.priDbk_046 = priDbk_046;
 	}
 
 	public void setSaNam_046(String saNam_046) {
 		this.saNam_046 = JdbcTools.removeTrailingSpaces(saNam_046);
 	}
 
 	public void setOrd_046(short ord_046) {
 		this.ord_046 = ord_046;
 	}
 
 	public void setSetMode_046(short setMode_046) {
 		this.setMode_046 = setMode_046;
 	}
 
 	public void setSetNam_046(String setNam_046) {
 		this.setNam_046 = JdbcTools.removeTrailingSpaces(setNam_046);
 	}
 
 	public void setSetOrd_046(short setOrd_046) {
 		this.setOrd_046 = setOrd_046;
 	}
 
 	public void setSorId_046(short sorId_046) {
 		this.sorId_046 = sorId_046;
 	}
 
 	public void setSrcd_113(Srcd_113 srcd_113) {
 		this.srcd_113 = srcd_113;
 	}
 
 	public void setSubarea_046(String subarea_046) {
 		this.subarea_046 = JdbcTools.removeTrailingSpaces(subarea_046);
 	}
 
 	public void setSymbolIndex_046(String symbolIndex_046) {
 		this.symbolIndex_046 = JdbcTools.removeTrailingSpaces(symbolIndex_046);
 	}
 
 }
