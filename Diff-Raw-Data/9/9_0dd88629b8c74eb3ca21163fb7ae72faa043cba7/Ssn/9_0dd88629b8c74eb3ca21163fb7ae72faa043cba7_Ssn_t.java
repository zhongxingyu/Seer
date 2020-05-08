 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ****************************************************************************/
 
 
 package org.nchelp.meteor.util;
 
 import org.nchelp.meteor.logging.Logger;
 
 /**
  *   Validate an SSN
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Jul 16, 2002
  */
 public class Ssn {
 	private static final Logger log = Logger.create(Ssn.class);
 	
 	
 	/**
	 * Validate the SSN according to the rules at ssa\.gov.
 	 * <a href="http://ssa-custhelp.ssa.gov/cgi-bin/ssa.cfg/
 	 * php/enduser/std_adp.php?p_faqid=425">SSA.gov</a>
 	 * 
 	 * @param ssn String SSN to validate
 	 * @return boolean boolean result. True if this is a 
 	 * valid ssn, False otherwise
 	 */
 	public static boolean validate(String ssn) {
 		// According to: http://ssa-custhelp.ssa.gov/cgi-bin/
 		// ssa.cfg/php/enduser/std_adp.php?p_faqid=425
 		
 		if (ssn.length() != 9 && ssn.length() != 11) { 
 			return false; 
 		}
 		
 		if (ssn.length() == 11) { 
 			ssn = trimdashes(ssn); 
 		}
 		
 		// If anything isn't a digit, return false
 		for (int i = 0; i < ssn.length(); i++) {
 			if (!Character.isDigit(ssn.charAt(i))) { 
 				return false; 
 			}
 		}
 		
 		int areaNumber = Integer.parseInt(ssn.substring(0, 3));
 		int groupNumber = Integer.parseInt(ssn.substring(3, 5));
 		int serialNumber = Integer.parseInt(ssn.substring(5));
 		
 		if (areaNumber == 0) { return false; }
 		if (areaNumber >= 800) { return false; }
 		
 		if (areaNumber > 728 && areaNumber < 764) { return false; }
 		if (areaNumber > 772 && areaNumber <= 799) { return false; }
 		
 		if (areaNumber == 666) { return false; }
 		
 		if (groupNumber == 0) { return false; }
 		
 		if (serialNumber == 0) { return false; }
 		
 		return true;
 	}
 	
 	/**
 	 * Remove all of the '-' from the String
 	 * @param str String that the '-' are to be removed from
 	 * @return String
 	 */
 	private static String trimdashes(String str) {
 
 		// Remove all of the '-'
 		String ssn = "";
 		int index = str.indexOf('-');
 		while (index >= 0) {
 			ssn += str.substring(0, index);
 			str = str.substring(index + 1, str.length());
 			index = str.indexOf('-');
 		}
 		// Now that there aren't any '-' in the string, add
 		// what's left to the ssn
 		ssn += str;
 		
 		// Set the value back to the complete SSN
 		// since all of the values are stored
 		// in a hash to send to xslt.
 		return ssn;
 		
 		
 	}
 
 }
