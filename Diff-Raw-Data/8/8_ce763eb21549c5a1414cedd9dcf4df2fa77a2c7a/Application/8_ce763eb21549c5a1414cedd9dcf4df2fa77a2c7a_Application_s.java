package fr.hbis.ircs;

 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 /**
  * The class <code>Application</code> represents the main object used to start
  * hbIRCS.
  * 
  * @author bhuisgen
  */
 public class Application
 {
 	/**
 	 * The default entry point of the program.
 	 * 
 	 * @param args
 	 *            the command line arguments array.
 	 */
 	public static void main (String[] args)
 	{
 		System.out.printf ("%s version %d.%d.%d\n", Constants.HBIRCS_NAME,
 				Constants.HBIRCS_VERSION_MAJOR, Constants.HBIRCS_VERSION_MINOR,
 				Constants.HBIRCS_VERSION_PATCH);
 		System.out.printf ("%s\n", Constants.HBIRCS_COPYRIGHT);
 		System.out.printf ("%s\n", Constants.HBIRCS_NOTICE);
		
 		Manager.main (args);
 	}
 }
