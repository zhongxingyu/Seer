 package com.ongroa.cocktails;
 
 import java.io.Serializable;
 
 public class Osszetevo implements Comparable<Osszetevo>, Serializable {
 	
 	private static final long serialVersionUID = 4343873930214654008L;
 	public static String MENNYISEG = "mennyiseg";
 	public static String UNIT = "unit";
 	public static String NAME = "name";
 	
 	private String mennyiseg;
 	private String nev;
 	private boolean valid;
 
 	public Osszetevo() {
 		valid = false;
 	}
 	
 	public Osszetevo(String m, String n) {
 		this.mennyiseg = m;
 		this.nev = n;
 	}
 	
 	public String getMennyiseg() {
 		return mennyiseg;
 	}
 	
 	public void setMennyiseg(String mennyiseg) {
 		this.mennyiseg = mennyiseg;
 	}
 	
 	public String getNev() {
 		return nev;
 	}
 	
 	public void setNev(String name) {
 		this.nev = name;
 	}
 	
 	public boolean isValid() {
 		return valid;
 	}
 
 	public void setValid(boolean valid) {
 		this.valid = valid;
 	}
 
 	@Override
 	public String toString() {
 		String ret = String.format("\t%s %s\n",
 				mennyiseg, nev);
 		return ret;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) return false;
 		if (obj == this) return true;
 		if (! (obj instanceof Osszetevo)) return false;
 		Osszetevo o = (Osszetevo)obj;
 		return o.mennyiseg.equals(this.mennyiseg) &&
 				o.nev.equals(this.nev);
 	}
	
	@Override
	public int hashCode() {
		return mennyiseg.hashCode() + nev.hashCode();
	}
 
 	@Override
 	public int compareTo(Osszetevo o) {
 		return this.nev.compareToIgnoreCase(o.nev);
 	}
 
 }
