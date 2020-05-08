 package logic;
 
 import java.io.Serializable;
 
 public class Literal implements Serializable{
 	private char name;
 	private boolean value;
 	public Literal(char n)
 	{
 		name = n;
 		value = true;
 	}
 	public Literal(Literal lit)
 	{
 		name=lit.getName();
 		value=lit.getValue();
 	}
 	public Literal(String in)
 	{
 		if (in.charAt(0)=='~')
 		{
 			value=false;
 			name=in.charAt(1);
 		}
 		else
 		{
 			value=true;
 			name=in.charAt(0);
 		}
 	}
 	public Literal(Clause c) {
 		 this.value = c.getLiterals().get(0).getValue();
 		 this.name = c.getLiterals().get(0).getName();
 	}
 	public Literal(char n_name, boolean n_value)
 	{
 		name=n_name;
 		value=n_value;
 	}
 	public Literal not()
 	{
 		this.value = (this.value == true ? false : true);
 		return this;
 	}
 	public boolean getValue()
 	{
 		return value;
 	}
 	public char getName()
 	{
 		return name;
 	}
 	public boolean equalLetter(Object other) {
 		if(!(other instanceof Literal))
 			return false;
 		Literal l = (Literal) other;
 		return getName() == l.getName();
 	}
 	public boolean equals(Object other) {
 		if(!(other instanceof Literal))
 			return false;
 		Literal l = (Literal)other;
 		return (getName() == l.getName() && getValue() == l.getValue());
 	}
 }
