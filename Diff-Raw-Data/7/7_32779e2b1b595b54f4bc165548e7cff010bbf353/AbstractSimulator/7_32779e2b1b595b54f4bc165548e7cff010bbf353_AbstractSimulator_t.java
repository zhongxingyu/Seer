 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2012-2013  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.simulator;
 
 import java.util.*;
 
 public abstract class AbstractSimulator
 {
 	/**
 	*The first number with seperated Digits that we will simulate the arithmetic operation
 	*/
 	protected byte telestis1[]=null;
 
 	/**
 	*The second number with seperated Digits that we will simulate the arithmetic operation
 	*/
 	protected byte telestis2[]=null;
 
 	/**
 	*Arraylist that we will keep the carry
 	*/
 	protected byte kratoumeno=0;
 
 	/**
 	*Array that will keep the final result
 	*/
 	protected byte[] result=null;
 
 	/**
 	*Flag that positions in what digit we will apply the arithmetic operation
 	*/
 	protected int telestis1Index;
 
 	/**
 	*Flag that positions in what digit we will apply the arithmetic operation
 	*/
 	protected int telestis2Index;
 
 	/**
 	*Flag that positions in what digit we will apply the arithmetic operation
 	*/
 	protected int resultIndex;
 
 	/**
 	*Stores temporaly the result
 	*/
 	protected int temp=0;
 
 	/**
 	*Show a messages to be displayed during the simulation
 	*/
 	protected String message="";
 
 	/**
 	*Method that seperates the digits from a Number
 	*/
 	public  static byte[] seperateDigits(int number)
 	{
 		byte[] intermediate=new byte[String.valueOf(number).length()];
 		
 		for(int i=0;i<intermediate.length;i++)
 		{
 			intermediate[intermediate.length-1-i]=(byte)(number%10);
 			number/=10;
 		}		
 		return intermediate;
 	}
 
 	/**
 	*Method that merges a number with seperated digits
 	*@param digits: Number with seperated ditits	
 	*/
 	public static long mergeDigits(byte[] digits)
 	{
 		long merged=0;
 		
		for(int i=0;i<digits.length;i++)
 		{
			int pow=digits.length-1-i;
			System.out.println(digits[i]*Math.pow(10,pow));
			merged+=digits[i]*(long)Math.pow(10,pow);
 		}
 
 		return merged;
 	}
 
 	
 	/**
 	*Constructor Method
 	*@param telestis1: the first operator of the number that we will simulate the first operation
 	*@param telestis2: the second operator of the number that we will simulate the first operation
 	*/
 	public AbstractSimulator(int telestis1, int telestis2)
 	{
 		this.telestis1= AbstractSimulator.seperateDigits(telestis1);
 		this.telestis2= AbstractSimulator.seperateDigits(telestis2);
 
 		telestis1Index=this.telestis1.length-1;
 		telestis2Index=this.telestis2.length-1;
 		
 		int maxLength=	(int)((this.telestis1.length>this.telestis2.length)? this.telestis1.length : this.telestis2.length);
 		
 		resultIndex=maxLength;
 		result=new byte[resultIndex+1];
 		Arrays.fill(result,(byte)0);
 		temp=0;	
 	}
 
 	/**
 	*Returns the carry
 	*/
 	public int getCarry()
 	{
 		return kratoumeno;
 	}
 
 	/**
 	*Returns the message
 	*/
 	public String toString()
 	{
 		return message;
 	}
 
 	/**
 	*Returns the position of the result digit 
 	*/
 	public int getResDigit()
 	{
 		return resultIndex;
 	}
 
 	/**
 	*Returns the position of the  digit of first Operator 
 	*/
 	public int getTelests1Digit()
 	{
 		return telestis1Index;
 	}
 
 	/**
 	*Returns the position of the  digit of second Operator
 	*/
 	public int getTelests2Digit()
 	{
 		return telestis2Index;
 	}
 
 	/**
 	*A way to return the operatos as String with distinct space between tht digits
 	*/
 	private String getTelestis(byte[] num)
 	{
 		String s="";
 		for(int i=0;i<num.length;i++)
 		{
 			s+="\t"+num[i];
 		}
 		return s;
 	}
 	
 	/**
 	*Returns the first operator
 	*/
 	public String getTelestis1()
 	{
 		return getTelestis(telestis1);
 	}
 
 	/**
 	*Returns the first operator
 	*/
 	public String getTelestis2()
 	{
 		return getTelestis(telestis2);
 	}
 	
 	public String getResult()
 	{
 		return getTelestis(result);
 	}
 
 	/**
 	*This Method does the next step of an arithmetic praxis Simulation
 	*Returns true if it has next step to do
 	*/
 	public abstract boolean next();
 
 	
 }
