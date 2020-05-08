 package org.geworkbench.util.annotation;
 
 import gov.nih.nci.caBIO.util.PathwayDiagram;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * <p/>
  * Defines a contract to obtain Pathway information
  *
  * @author First Genetic Trust
  * @version 1.0
  */
 public interface Pathway {
     /**
      * Gets the name of the Pathway contained in this instance
      *
      * @return Pathway name
      */
     String getPathwayName();
 
     /**
      * Gets the <code>PathwayDiagram</code> contained in the <code>Pathway</code>
      * instance
      *
      * @return Pathway diagram
      */
    PathwayDiagram getPathwayDiagram();
 
     /**
      * Gets the Pathway Identifier of the <code>Pathway</code> instance
      *
      * @return Pathway ID
      */
     String getPathwayId();
 }
