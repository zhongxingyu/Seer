 /*
  * Int.java
  * 
  * Created on Jul 27, 2005
  *
  * Copyright (C) 2003 - 2006 
  * Computational Intelligence Research Group (CIRG@UP)
  * Department of Computer Science 
  * University of Pretoria
  * South Africa
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */
 package net.sourceforge.cilib.type.types;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 
 import net.sourceforge.cilib.math.MathUtil;
 
 
 /**
  * 
  * @author Gary Pampara
  *
  */
 public class Int extends Numeric {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 271271478995857543L;
 	private int value;
 	
 	public Int() {
 		this(Integer.MIN_VALUE, Integer.MAX_VALUE);		
 	}
 
 	public Int(int lower, int upper) {
		value = Double.valueOf(Math.random()*(upper-lower)).intValue() + lower;
 		setLowerBound(lower);
 		setUpperBound(upper);
 	}
 
 	public Int(int value) {
 		this.value = value;
 		setLowerBound(Integer.MIN_VALUE);
 		setUpperBound(Integer.MAX_VALUE);
 	}
 	
 	
 	public Int(Int copy) {
 		this.value = copy.value;
 		this.setLowerBound(copy.getLowerBound());
 		this.setUpperBound(copy.getUpperBound());
 	}
 	
 	
 	public Int clone() {
 		return new Int(this);
 	}
 
 	public boolean equals(Object other) {
 		if (other instanceof Int) {
 			Int i = (Int) other;
 			
 			if (this.value == i.value)
 				return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Return the value of the object itself. This is accordance to the manner
 	 * in which {@see java.lang.Integer#hashCode()} operates.
 	 * 
 	 * @return The value of this Int representation.
 	 */
 	public int hashCode() {
 		return Integer.valueOf(this.value).hashCode();
 	}
 
 	public boolean getBit() {
 		return (this.value == 0) ? false : true;
 	}
 
 	public void setBit(boolean value) {
 		if (value == false)
 			this.value = 0;
 		else this.value = 1;
 	}
 
 	public int getInt() {
 		return this.value;
 	}
 
 	public void setInt(int value) {
 		this.value = value;
 	}
 
 	public double getReal() {
 		return Integer.valueOf(value).doubleValue();
 	}
 
 	public void setReal(double value) {
 		this.value = Double.valueOf(value).intValue();
 	}
 
 	public int compareTo(Numeric other) {
 		if (this.value == other.getInt())
 			return 0;
 		else
 			return (other.getInt() < this.value) ? 1 : -1;
 	}
 
 	public void randomise() {
		double tmp = MathUtil.random()*(getUpperBound()-getLowerBound()) + getLowerBound();
 		this.value = Double.valueOf(tmp).intValue();
 	}
 	
 	
 	public void reset() {
 		this.setInt(0);
 	}
 
 	
 	public String toString() {
 		return String.valueOf(this.value);
 	}
 
 	public String getRepresentation() {
 		return "Z(" + Double.valueOf(getLowerBound()).intValue() + "," +
 			Double.valueOf(getUpperBound()).intValue() +")";
 	}
 
 	
 	public void writeExternal(ObjectOutput oos) throws IOException {
 		oos.writeInt(this.value);		
 	}
 
 	public void readExternal(ObjectInput ois) throws IOException, ClassNotFoundException {
 		this.value = ois.readInt();		
 	}
 }
