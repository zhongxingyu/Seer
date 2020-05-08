 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.utils;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 
 /**
  * Extends the OWLOntologyID class to provide for another variable to track the inferred ontology
  * IRI that matches this ontology ID.
  * 
  * Use getBaseOWLOntologyID to get a typical OWLOntologyID instance representing the base portion of
  * this object.
  * 
  * Use getInferredOWLOntologyID to get a typical OWLOntologyID instance representing the inferred
  * portion of this object.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class InferredOWLOntologyID extends OWLOntologyID
 {
     private static final long serialVersionUID = 30402L;
     
     private final IRI inferredOntologyIRI;
     
     /**
      * Creates an {@link InferredOWLOntologyID} using three OWLAPI {@link IRI}s.
      */
     public InferredOWLOntologyID(final IRI baseOntologyIRI, final IRI baseOntologyVersionIRI,
             final IRI inferredOntologyIRI)
     {
         super(baseOntologyIRI, baseOntologyVersionIRI);
         
         this.inferredOntologyIRI = inferredOntologyIRI;
         
         // Override hashcode if inferredOntologyIRI is not null, otherwise leave the hashcode to
         // match upstream
         if(inferredOntologyIRI != null)
         {
             // super uses 37, so to be distinct we need to use a different prime number here, ie, 41
            //this.hashCode += 41 * inferredOntologyIRI.hashCode();
         }
     }
     
     /**
      * Creates an {@link InferredOWLOntologyID} using three OpenRDF {@link URI}s.
      */
     public InferredOWLOntologyID(final URI baseOntologyIRI, final URI baseOntologyVersionIRI,
             final URI inferredOntologyIRI)
     {
         super(baseOntologyIRI, baseOntologyVersionIRI);
         
         if(inferredOntologyIRI != null)
         {
             this.inferredOntologyIRI = IRI.create(inferredOntologyIRI);
         }
         else
         {
             this.inferredOntologyIRI = null;
         }
     }
     
     /**
      * Returns the OWLOntologyID representing the base ontology, ie, without the inferred ontology,
      * so that the hashcode will match that of the real OWLOntologyID for the base ontology.
      * 
      * @return
      */
     public OWLOntologyID getBaseOWLOntologyID()
     {
         return new OWLOntologyID(this.getOntologyIRI(), this.getVersionIRI());
     }
     
     /**
      * @return the inferredOntologyIRI
      */
     public IRI getInferredOntologyIRI()
     {
         return this.inferredOntologyIRI;
     }
     
     /**
      * Returns the OWLOntologyID representing the inferred ontology. ie, without the base ontology,
      * so that the hashcode will match that of the real OWLOntologyID for the inferred ontology. <br/>
      * NOTE: We make the assumption that inferred ontologies are not versioned. They should be
      * regenerated each time with new URIs.
      * 
      * @return
      */
     public OWLOntologyID getInferredOWLOntologyID()
     {
         return new OWLOntologyID(this.getInferredOntologyIRI());
     }
     
     public Model toRDF()
     {
         return this.toRDF(new LinkedHashModel());
     }
     
     public Model toRDF(final Model result)
     {
         return OntologyUtils.ontologyIDToRDF(this, result, true);
     }
     
     @Override
     public String toString()
     {
         if(this.inferredOntologyIRI == null)
         {
             return super.toString();
         }
         else
         {
             return super.toString() + this.inferredOntologyIRI.toQuotedString();
         }
     }
 }
