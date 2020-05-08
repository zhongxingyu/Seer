 /**
  *  PADLoader - Loads NYC Property Address Directory into a Database
  *  Copyright (C) 2011  Skye Book
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.skyebook.padloader.record;
 
 /**
  * A Borough, Block, Lot Record
  * @author Skye Book
  *
  */
 public class BBLRecord extends Record{
 
 	public enum Fields{
 		loboro,
 		loblock,
 		lolot,
 		lobblscc,
 		hiboro,
 		hiblock,
 		hilot,
 		hibblscc,
 		boro,
 		block,
 		lot,
 		bblscc,
 		billboro,
 		billblock,
 		billlot,
 		billbblscc,
 		condoflag,
 		condonum,
 		coopnum,
 		numbf,
 		numaddr,
 		vacant,
 		interior
 	}
 
 	private short loboro;
 	private int loblock;
 	private int lolot;
 	private short lobblscc;
 
 	private short hiboro;
 	private int hiblock;
 	private int hilot;
 	private int hibblscc;
 
 	// Foreign Keys to the ADR table
 	private short boro;
 	private int block;
 	private int lot;
 
 	private short bblscc;
 	private short billboro;
 	private int billblock;
 	private int billlot;
 	private short billbblscc;
 
 	private char condoflag;
 	private int condonum;
 	private int coopnum;
 	private short numbf;
 	private int numaddr;
 	private char vacant;
 	private char interior;
 
 
 	/**
 	 * 
 	 */
 	public BBLRecord(){
 	}
 
 
 	/**
 	 * @return the loboro
 	 */
 	public short getLoboro() {
 		return loboro;
 	}
 
 
 	/**
 	 * @param loboro the loboro to set
 	 */
 	public void setLoboro(short loboro) {
 		this.loboro = loboro;
 	}
 
 
 	/**
 	 * @return the loblock
 	 */
 	public int getLoblock() {
 		return loblock;
 	}
 
 
 	/**
 	 * @param loblock the loblock to set
 	 */
 	public void setLoblock(int loblock) {
 		this.loblock = loblock;
 	}
 
 
 	/**
 	 * @return the lolot
 	 */
 	public int getLolot() {
 		return lolot;
 	}
 
 
 	/**
 	 * @param lolot the lolot to set
 	 */
 	public void setLolot(int lolot) {
 		this.lolot = lolot;
 	}
 
 
 	/**
 	 * @return the lobblscc
 	 */
 	public short getLobblscc() {
 		return lobblscc;
 	}
 
 
 	/**
 	 * @param lobblscc the lobblscc to set
 	 */
 	public void setLobblscc(short lobblscc) {
 		this.lobblscc = lobblscc;
 	}
 
 
 	/**
 	 * @return the hiboro
 	 */
 	public short getHiboro() {
 		return hiboro;
 	}
 
 
 	/**
 	 * @param hiboro the hiboro to set
 	 */
 	public void setHiboro(short hiboro) {
 		this.hiboro = hiboro;
 	}
 
 
 	/**
 	 * @return the hiblock
 	 */
 	public int getHiblock() {
 		return hiblock;
 	}
 
 
 	/**
 	 * @param hiblock the hiblock to set
 	 */
 	public void setHiblock(int hiblock) {
 		this.hiblock = hiblock;
 	}
 
 
 	/**
 	 * @return the hilot
 	 */
 	public int getHilot() {
 		return hilot;
 	}
 
 
 	/**
 	 * @param hilot the hilot to set
 	 */
 	public void setHilot(int hilot) {
 		this.hilot = hilot;
 	}
 
 
 	/**
 	 * @return the hibblscc
 	 */
 	public int getHibblscc() {
 		return hibblscc;
 	}
 
 
 	/**
 	 * @param hibblscc the hibblscc to set
 	 */
 	public void setHibblscc(int hibblscc) {
 		this.hibblscc = hibblscc;
 	}
 
 
 	/**
 	 * @return the boro
 	 */
 	public short getBoro() {
 		return boro;
 	}
 
 
 	/**
 	 * @param boro the boro to set
 	 */
 	public void setBoro(short boro) {
 		this.boro = boro;
 	}
 
 
 	/**
 	 * @return the block
 	 */
 	public int getBlock() {
 		return block;
 	}
 
 
 	/**
 	 * @param block the block to set
 	 */
 	public void setBlock(int block) {
 		this.block = block;
 	}
 
 
 	/**
 	 * @return the lot
 	 */
 	public int getLot() {
 		return lot;
 	}
 
 
 	/**
 	 * @param lot the lot to set
 	 */
 	public void setLot(int lot) {
 		this.lot = lot;
 	}
 
 
 	/**
 	 * @return the bblscc
 	 */
 	public short getBblscc() {
 		return bblscc;
 	}
 
 
 	/**
 	 * @param bblscc the bblscc to set
 	 */
 	public void setBblscc(short bblscc) {
 		this.bblscc = bblscc;
 	}
 
 
 	/**
 	 * @return the billboro
 	 */
 	public short getBillboro() {
 		return billboro;
 	}
 
 
 	/**
 	 * @param billboro the billboro to set
 	 */
 	public void setBillboro(short billboro) {
 		this.billboro = billboro;
 	}
 
 
 	/**
 	 * @return the billblock
 	 */
 	public int getBillblock() {
 		return billblock;
 	}
 
 
 	/**
 	 * @param billblock the billblock to set
 	 */
 	public void setBillblock(int billblock) {
 		this.billblock = billblock;
 	}
 
 
 	/**
 	 * @return the billlot
 	 */
 	public int getBilllot() {
 		return billlot;
 	}
 
 
 	/**
 	 * @param billlot the billlot to set
 	 */
 	public void setBilllot(int billlot) {
 		this.billlot = billlot;
 	}
 
 
 	/**
 	 * @return the billbblscc
 	 */
 	public short getBillbblscc() {
 		return billbblscc;
 	}
 
 
 	/**
 	 * @param billbblscc the billbblscc to set
 	 */
 	public void setBillbblscc(short billbblscc) {
 		this.billbblscc = billbblscc;
 	}
 
 
 	/**
 	 * @return the condoflag
 	 */
 	public char getCondoflag() {
 		return condoflag;
 	}
 
 
 	/**
 	 * @param condoflag the condoflag to set
 	 */
 	public void setCondoflag(char condoflag) {
 		this.condoflag = condoflag;
 	}
 
 
 	/**
 	 * @return the condonum
 	 */
 	public int getCondonum() {
 		return condonum;
 	}
 
 
 	/**
	 * @param condonum the condonum to set
 	 */
	public void setCondonum(int condonum) {
 		this.condonum = condonum;
 	}
 
 
 	/**
 	 * @return the coopnum
 	 */
 	public int getCoopnum() {
 		return coopnum;
 	}
 
 
 	/**
 	 * @param coopnumber the coopnum to set
 	 */
 	public void setCoopnum(int coopnum) {
 		this.coopnum = coopnum;
 	}
 
 
 	/**
 	 * @return the numbf
 	 */
 	public short getNumbf() {
 		return numbf;
 	}
 
 
 	/**
 	 * @param numbf the numbf to set
 	 */
 	public void setNumbf(short numbf) {
 		this.numbf = numbf;
 	}
 
 
 	/**
 	 * @return the numaddr
 	 */
 	public int getNumaddr() {
 		return numaddr;
 	}
 
 
 	/**
 	 * @param numaddr the numaddr to set
 	 */
 	public void setNumaddr(int numaddr) {
 		this.numaddr = numaddr;
 	}
 
 
 	/**
 	 * @return the vacant
 	 */
 	public char getVacant() {
 		return vacant;
 	}
 
 
 	/**
 	 * @param vacant the vacant to set
 	 */
 	public void setVacant(char vacant) {
 		this.vacant = vacant;
 	}
 
 
 	/**
 	 * @return the interior
 	 */
 	public char getInterior() {
 		return interior;
 	}
 
 
 	/**
 	 * @param interior the interior to set
 	 */
 	public void setInterior(char interior) {
 		this.interior = interior;
 	}
 
 }
