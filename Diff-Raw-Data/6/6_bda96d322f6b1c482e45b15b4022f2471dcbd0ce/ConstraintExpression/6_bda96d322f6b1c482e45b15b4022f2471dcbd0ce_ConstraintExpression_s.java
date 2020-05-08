 /*
  * Copyright (c) 2010, Regents of the University of Colorado
  * 
  * All rights reserved. Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided that the following 
  * conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution. 
  * Neither the name of the University of Colorado nor the names of its
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package lasp.tss.constraint;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import lasp.tss.util.RegEx;
 
 /**
  * Representation of the constraint expression portion of the OPeNDAP URL (following the '?').
  * It's responsible for parsing the constraints (separated by '&') and constructing the 
  * appropriate implementation of Constraint.
  * 
  * @author Doug Lindholm
  */
 public class ConstraintExpression {
 
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(ConstraintExpression.class);
     
     private String _constraintExpression;
     private List<Constraint> _constraints;
     
     /**
      * Parse the constraint expression (everything after the "?" in the request URL).
      * There are three types of constraints:
      * 1) ProjectionConstraint: 
      *    Optional list of variable names to be served. 
      *    May also have a hyperslab (index range) definition which will be treated as a unique HyperslabConstraint.
      *    Immediately after the "?" and up to the first "&".
      * 2) SelectionConstraint:
      *    Compares a variable's values for acceptance by applying operators: >, >=, <, <=, !=
      * 3) FilterConstraint:
      *    Looks like a function with (optionally) arguments in "()".
      *    
      */
     public ConstraintExpression(String constraintExpression) {
         _constraintExpression = constraintExpression;
         _constraints = new ArrayList<Constraint>();
         
         if (constraintExpression != null) parseConstraints(constraintExpression);
     }
     
     /**
      * Construct Constraint objects for each component of the constraint expression.
      */
     private void parseConstraints(String constraintExpression) {
         
         //Convert encoded symbols such as "<" and ">"
         String ce = decodeURL(constraintExpression);
         
         //Separate the expressions, all but field projection start with "&"
         String[] expressions = ce.split("&");
         int n = expressions.length;
         
         //First component (before first "&") is the field projection clause.
         //Will be "" if none given.
         String projection = expressions[0];
         //Construct the ProjectionConstraint if there is a projection clause.
         if (projection.length() > 0) {
             ProjectionConstraint pc = new ProjectionConstraint(projection);
             _constraints.add(pc);
             
             //Handle hyperslab if defined in the projection clause.
             String slab = pc.getHyperslab();
             if (slab != null) {
                 HyperslabConstraint hc = new HyperslabConstraint(slab);
                 _constraints.add(hc);
             }
         }
         
         //Handle the rest of the constraints (selections and functions)
         for (int i=1; i<n; i++) {
             String s = expressions[i];
             
             //Try to match selection constraint
             if (s.matches(RegEx.SELECTION)) {
                 SelectionConstraint sc = new SelectionConstraint(s);
                 _constraints.add(sc);
             }
             //Filter           
             else if (s.matches(RegEx.FUNCTION)) {
                 FilterConstraint fc = new FilterConstraint(s);
                 _constraints.add(fc);
            }            
         }
         
     }
 
     /**
      * Return a List of the Constraints.
      */
     public List<Constraint> getConstraints() {
         return _constraints;
     }
     
     /**
      * Return the projection constraint (i.e. list of variables to serve).
      */
     public ProjectionConstraint getProjectionConstraint() {
         ProjectionConstraint pc = null;
         
         for (Constraint c : _constraints) {
             if (c instanceof ProjectionConstraint) {
                 pc = (ProjectionConstraint) c;
                 break;
             }
         }
         
         return pc;
     }
     
     /**
      * Deal with URL encoding (e.g. space = %20)
      */
     private String decodeURL(String url) {
         try {
             url = URLDecoder.decode(url, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             String msg = "Failed to decode the URL: " + url;
             _logger.error(msg, e);
         }
         return url;
     }
     
     public String toString() {
         return _constraintExpression;
     }
 }
