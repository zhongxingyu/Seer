 /**
 *============================================================================
*  The Ohio State University Research Foundation, The University of Chicago -
*  Argonne National Laboratory, Emory University, SemanticBits LLC, 
*  and Ekagra Software Technologies Ltd.
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
*  See http://ncip.github.com/cagrid-core/LICENSE.txt for details.
 *============================================================================
 **/
 package gov.nih.nci.cagrid.portal.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class EmailValidator {
 
     public static boolean validateEmail(String email) {
         //Set the email pattern string
         Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
 
         //Match the given string with the pattern
         Matcher m = p.matcher(email);
 
         //check whether match is found
         return m.matches();
     }
 }
