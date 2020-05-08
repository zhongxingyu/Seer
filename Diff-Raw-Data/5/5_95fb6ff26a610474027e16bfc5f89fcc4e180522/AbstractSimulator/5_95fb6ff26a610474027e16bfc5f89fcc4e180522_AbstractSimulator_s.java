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
 import guma.arithmetic.Praxis;
 import guma.simulator.*;
 
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
 	*Variable that tells what type of operation simulates
 	*/
 	protected char type;
 	
 	/**
 	*Method that seperates the digits from a Number
 	*/
 	public static byte[] seperateDigits(int number)
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
 	public static int mergeDigits(byte[] digits)
 	{
 		int merged=0;
 		
 		for(int i=0;i<digits.length;i++)
 		{
 			int pow=digits.length-1-i;
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
 		
 		temp=0;	
 	}
 	
 	
 	/**
 	*Counts how many zeros has on the end a number with seperated digits.
 	*If parameter num is null then it returns -1
 	*@param num: number with seperated digits
 	*/
 	public static int zeroEndCount(byte[] num)
 	{
 		int zeros=0;
 		
 		try
 		{
 			for(int i=num.length-1;i>=0;i--)
 			{
 				if(num[i]==0)
 				{
 					zeros++;
 				}
 				else
 				{
 					break;
 				}
 			} 
 		}
 		catch(NullPointerException n)
 		{
 			zeros=-1;
 		}
 		
 		return zeros;	
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
 	public String getMessage()
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
 	*A way to return the operatos as String with distinct space (tab) between tht digits
 	*@param num: the number with seperated Digits
 	*/
 	public static String getTelestis(byte[] num)
 	{
 		return getTelestis(num,"\t","");
 	}
 	
 	/**
 	*A way to return the operatos as String with distinct space between tht digits
 	*@param num: the number with seperated Digits
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*/
 	public static String getTelestis(byte[] num,String front, String back)
 	{
 		
 		return getTelestis(num,front,back,0,front,back);
 	}
 	
 	/**
 	*A way to return the operatos as String with distinct space between tht digits
 	*@param num: the number with seperated Digits
 	*@param front:  The sting you want to be on the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*@param pos: select a specified position that will have seperate texnt on the front and back
 	*@param posFront: The sting you want to be on the front of a digit at specified positions, given by pos paramenter
 	*@param posBack: The sting you want to be at the back of a digit at specified positions, given by pos parameter
 	*/
 	public static String getTelestis(byte[] num,String front, String back, int pos, String posFront, String posBack)
 	{
 		String s="";
 		for(int i=0;i<num.length;i++)
 		{
 			if(i==pos)
 			{
 				s+=posFront+num[i]+posBack;
 			}
 			else
 			{
 				s+=front+num[i]+back;
 			}
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
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*/
 	public String getTelestis1(String front, String back)
 	{
 		return getTelestis(telestis1,front,back);
 	}
 	
 	/**
 	*Returns the first operator
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*@param pos: select a specified position that will have seperate texnt on the front and back
 	*@param posFront: The sting you want to be on the front of a digit at specified positions, given by pos paramenter
 	*@param posBack: The sting you want to be at the back of a digit at specified positions, given by pos parameter
 	*/
 	public String getTelestis1(String front, String back, String posFront, String posBack)
 	{
 		return getTelestis(telestis1,front,back,telestis1Index, posFront, posBack);
 	}
 	
 	/**
 	*Returns the second operator
 	*/
 	public String getTelestis2()
 	{
 		return getTelestis2("","");
 	}
 	
 	/**
 	*Returns the second operator
 	*/
 	public String getTelestis2(String front, String back)
 	{
 		return getTelestis2(front,back,front,back);
 	}
 	
 	/**
 	*Returns the first operator
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*@param pos: select a specified position that will have seperate texnt on the front and back
 	*@param posFront: The sting you want to be on the front of a digit at specified positions, given by pos paramenter
 	*@param posBack: The sting you want to be at the back of a digit at specified positions, given by pos parameter
 	*/
 	public String getTelestis2(String front, String back, String posFront, String posBack)
 	{
 		if(telestis2!=null)
 		{
 			return getTelestis(telestis2,front,back,telestis2Index, posFront, posBack);
 		}
 		else
 		{
 			return front+"0"+back;
 		}
 	}
 	
 	/**
 	*Returns as String the result
 	*/
 	public String getResult()
 	{
 		return getResult("","");
 	}
 	
 	/**
 	*Returns as String the result
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*/
 	public String getResult(String front, String back)
 	{
 		return getResult(front,back,front,back);
 	}
 	
 	/**
 	*Returns the first operator
 	*@param front:  The sting you want to be be th the front of a digit
 	*@param back: The string you want to be at the back of a digit
 	*@param pos: select a specified position that will have seperate texnt on the front and back
 	*@param posFront: The sting you want to be on the front of a digit at specified positions, given by pos paramenter
 	*@param posBack: The sting you want to be at the back of a digit at specified positions, given by pos parameter
 	*/
 	public String getResult(String front, String back, String posFront, String posBack)
 	{
 		if(result!=null)
 		{
 			return getTelestis(result,front,back,resultIndex, posFront, posBack);
 		}
 		else
 		{
 			return front+"0"+back;
 		}
 	}
 	
 	/**
 	*Creates a Simulator Bazed on the praxisType is given on Praxis Type
 	*@param telestis1: the first operator (depending in the operation) of the operation we want to simulate
 	*@param telesits2: the second operator (depending in the operation) of the operation we want to simulate
 	*@param praxisType: The type of Operation that tells what kind of simulator we want
 	*/
 	public static AbstractSimulator makeSimulator(int telestis1, int telestis2,char praxisType)
 	{
 		AbstractSimulator a=null;
 		switch(praxisType)
 		{
 			case Praxis.ADDING: a= new AddingSimulator(telestis1,telestis2);
 			break;
 			
 			case Praxis.SUBSTRACTION:
 				if(telestis1>telestis2)
 				{
 					a= new SubstractionSimulator(telestis1,telestis2);
 				}
 				else
 				{
					a= new SubstractionSimulator(telestis1,telestis2);
 				}
 			break;
 			
 			case Praxis.DIVISION:
 				if(telestis1>telestis2)
 				{
 					a= new DivisionSimulator(telestis1,telestis2);
 				}
 				else
 				{
					a= new DivisionSimulator(telestis1,telestis2);
 				}
 				break;
 			
 			case Praxis.MULTIPLICATION: a= new MultiplicationSimulator(telestis1,telestis2,false);
 			break;
 			
 			default: a=null;		
 		}
 		
 		return a;
 	}
 
 	/**
 	*This Method does the next step of an arithmetic praxis Simulation
 	*Returns true if it has next step to do
 	*/
 	public abstract boolean next();
 
 	/**
 	*This method shows tas String the operation of simulator
 	*@param: html: Shows if the utput will be html or not
 	*/
 	public abstract String toString(boolean html);
 }
