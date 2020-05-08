 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.registration;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * Exposes information to uniquely identify an AUT. This information can 
  * be encoded to / decoded from a String.
  *
  * @author BREDEX GmbH
  * @created Dec 7, 2009
  */
 public final class AutIdentifier {
 
     /** name of "executableName" property for databinding support */
     public static final String PROP_EXECUTABLE_NAME = "executableName"; //$NON-NLS-1$
     
     /** the delimiter used when encoding/decoding identifiers */
     private static final String DELIMITER = "\001"; //$NON-NLS-1$
     
     /** name of executable used to start the AUT */
     private String m_executableName;
 
    /**
     * Default constructor.
     * Do nothing (required by XStream).
     */
    private AutIdentifier() {
        // Nothing to initialize
    }
    
     /** 
      * Constructor
      * 
      * @param executableName The name of the executable used to start the AUT.
      */
     public AutIdentifier(String executableName) {
         m_executableName = executableName;
     }
 
     /**
      * 
      * @return the name of the executable used to start the AUT
      */
     public String getExecutableName() {
         return m_executableName;
     }
     
     /**
      * @param autInfoString A String containing all necessary information about
      *                      the AUT.
      * @return an {@link AutIdentifier} representing the information contained
      *         in the given String.
      * 
      * @throws IllegalArgumentException if the given String cannot be properly 
      *                                  decoded (i.e. contains too much or too
      *                                  little information).
      * 
      * @see #encode()
      */
     public static AutIdentifier decode(String autInfoString) 
         throws IllegalArgumentException {
         
         String [] info = autInfoString.split(DELIMITER);
 
         // verify that the array is of sufficient length
         if (info.length != 1) {
             throw new IllegalArgumentException(
                     "The information string '" + autInfoString + "' could not be decoded.");  //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         AutIdentifier id = new AutIdentifier(info[0]);
         
         return id;
     }
 
     /**
      * 
      * @return A String containing all of the information represented by the
      *         receiver.
      * 
      * @see #decode(String)
      */
     public String encode() {
         StringBuffer sb = new StringBuffer();
         sb.append(m_executableName);
         return sb.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean equals(Object obj) {
         if (obj instanceof AutIdentifier) {
             AutIdentifier otherId = (AutIdentifier)obj;
             return new EqualsBuilder().append(
                 getExecutableName(), otherId.getExecutableName()).isEquals();
         }
 
         return false;
     }
     
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         return new HashCodeBuilder().append(getExecutableName()).toHashCode();
     }
 }
