 package org.protege.editor.owl.model.entity;
 
 import org.apache.log4j.Logger;
 import org.protege.editor.owl.model.OWLModelManager;
 import org.semanticweb.owl.model.*;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 /*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
 
 /**
  * Author: drummond<br>
  * http://www.cs.man.ac.uk/~drummond/<br><br>
  * <p/>
  * The University Of Manchester<br>
  * Bio Health Informatics Group<br>
  * Date: Jul 28, 2008<br><br>
  */
 public class CustomOWLEntityFactory implements OWLEntityFactory {
 
     private Logger logger = Logger.getLogger(LabelledOWLEntityFactory.class);
 
     private OWLModelManager mngr;
 
     private AutoIDGenerator autoIDGenerator;
 
     private LabelDescriptor labelDescriptor;
 
 
     public CustomOWLEntityFactory(OWLModelManager mngr) {
         this.mngr = mngr;
     }
 
 
     public OWLEntityCreationSet<OWLClass> createOWLClass(String shortName, URI baseURI) {
         return createOWLEntity(OWLClass.class, shortName, baseURI);
     }
 
 
     public OWLEntityCreationSet<OWLObjectProperty> createOWLObjectProperty(String shortName, URI baseURI) {
         return createOWLEntity(OWLObjectProperty.class, shortName, baseURI);
     }
 
 
     public OWLEntityCreationSet<OWLDataProperty> createOWLDataProperty(String shortName, URI baseURI) {
         return createOWLEntity(OWLDataProperty.class, shortName, baseURI);
     }
 
 
     public OWLEntityCreationSet<OWLIndividual> createOWLIndividual(String shortName, URI baseURI) {
         return createOWLEntity(OWLIndividual.class, shortName, baseURI);
     }
 
 
     public boolean isValidNewID(String shortName, URI baseURI) {
        return true;
     }
 
 
     private <T extends OWLEntity> OWLEntityCreationSet<T> createOWLEntity(Class<T> type, String shortName, URI baseURI) {
         try {
             List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
 
             URI uri;
             String id = null;
             if (EntityCreationPreferences.isFragmentAutoGenerated()){
                 do{
                     id = getAutoIDGenerator().getNextID(type);
                     uri = createURI(id, baseURI);
                 } while (isURIAlreadyUsed(uri));
             }
             else {
                 uri = createURI(shortName, baseURI);
 
                 if (EntityCreationPreferences.isGenerateIDLabel()){
                     id = getAutoIDGenerator().getNextID(type);
                 }
             }
 
             T entity = getOWLEntity(type, uri);
 
             if (EntityCreationPreferences.isGenerateIDLabel()){
                 changes.addAll(createLabel(entity, id));
             }
 
             if (EntityCreationPreferences.isGenerateNameLabel()){
                 changes.addAll(createLabel(entity, shortName));
             }
             if (changes.isEmpty()) {
                 OWLDataFactory df = mngr.getOWLDataFactory();
                 OWLAxiom ax = df.getOWLDeclarationAxiom(entity);
                 changes.add(new AddAxiom(mngr.getActiveOntology(), ax));
             }
 
             return new OWLEntityCreationSet<T>(entity, changes);
         }
         catch (Exception e) {
             logger.error("Cannot create new entity: " + e.getMessage());
         }
         return null;
     }
 
 
     protected URI createURI(String fragment, URI baseURI) {
         fragment = fragment.replace(" ", "_");
         if (baseURI == null){
             if (EntityCreationPreferences.useDefaultBaseURI()){
                 baseURI = EntityCreationPreferences.getDefaultBaseURI();
             }
             else{
                 baseURI = mngr.getActiveOntology().getURI();
             }
         }
         String base = baseURI.toString().replace(" ", "_");
         if (!base.endsWith("#") && !base.endsWith("/")) {
             base += "#";
         }
         return URI.create(base + fragment);
     }
 
 
     private List<? extends OWLOntologyChange> createLabel(OWLEntity owlEntity, String value) {
         LabelDescriptor descr = getLabelDescriptor();
         URI uri = descr.getURI();
         String lang = descr.getLanguage();
 
         OWLDataFactory df = mngr.getOWLDataFactory();
         OWLConstant con;
         if (lang == null){
             con = df.getOWLUntypedConstant(value);
         }
         else{
             con = df.getOWLUntypedConstant(value, lang);
         }
         OWLAnnotation anno = df.getOWLConstantAnnotation(uri, con);
         OWLAxiom ax = df.getOWLEntityAnnotationAxiom(owlEntity, anno);
         return Collections.singletonList(new AddAxiom(mngr.getActiveOntology(), ax));
     }
 
 
     private LabelDescriptor getLabelDescriptor() {
         Class<? extends LabelDescriptor> cls = EntityCreationPreferences.getLabelDescriptorClass();
         if (labelDescriptor == null || !cls.equals(labelDescriptor.getClass())){
             try {
                 labelDescriptor = cls.newInstance();
             }
             catch (InstantiationException e) {
                 logger.error("Cannot create label descriptor", e);
             }
             catch (IllegalAccessException e) {
                 logger.error("Cannot create label descriptor", e);
             }
         }
         return labelDescriptor;
     }
 
 
     private AutoIDGenerator getAutoIDGenerator() {
         final Class<? extends AutoIDGenerator> prefAutoIDClass = EntityCreationPreferences.getAutoIDGeneratorClass();
         if (autoIDGenerator == null || !prefAutoIDClass.equals(autoIDGenerator.getClass())){
             try {
                 autoIDGenerator = prefAutoIDClass.newInstance();
             }
             catch (InstantiationException e) {
                 logger.error("Cannot create auto ID generator", e);
             }
             catch (IllegalAccessException e) {
                 logger.error("Cannot create auto ID generator", e);
             }
         }
         return autoIDGenerator;
     }
 
 
     private boolean isURIAlreadyUsed(URI uri) {
         for (OWLOntology ont : mngr.getOntologies()){
             if (ont.containsClassReference(uri) ||
                 ont.containsObjectPropertyReference(uri) ||
                 ont.containsDataPropertyReference(uri) ||
                 ont.containsIndividualReference(uri)){
                 return true;
             }
         }
         return false;
     }
 
     private <T extends OWLEntity> T getOWLEntity(Class<T> type, URI uri) {
         if (type.equals(OWLClass.class)){
             return (T)mngr.getOWLDataFactory().getOWLClass(uri);
         }
         else if (type.equals(OWLObjectProperty.class)){
             return (T)mngr.getOWLDataFactory().getOWLObjectProperty(uri);
         }
         else if (type.equals(OWLDataProperty.class)){
             return (T)mngr.getOWLDataFactory().getOWLDataProperty(uri);
         }
         else if (type.equals(OWLIndividual.class)){
             return (T)mngr.getOWLDataFactory().getOWLIndividual(uri);
         }
         return null;
     }
 }
