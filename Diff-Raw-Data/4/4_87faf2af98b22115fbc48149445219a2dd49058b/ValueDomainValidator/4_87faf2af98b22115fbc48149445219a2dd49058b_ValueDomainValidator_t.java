 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.validator;
 
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ext.*;
 
 import org.apache.log4j.Logger;
 
 /**
  * Validate that Value Domains requested for load validate required rules: <ul>
  * <li>Must not already Exist
  * </ul>
  */
 public class ValueDomainValidator implements Validator, CadsrModuleListener {
 
   private ElementsLists elements = ElementsLists.getInstance();
 
   private ValidationItems items = ValidationItems.getInstance();
 
   private ProgressListener progressListener;
 
   private CadsrModule cadsrModule;
 
   private Logger logger = Logger.getLogger(ValueDomainValidator.class.getName());
 
   private static int SIW_MODE = 1, 
     UMLLOADER_MODE = 2;
 
   private int mode;
 
   public ValueDomainValidator(int mode) {
     this.mode = mode;
   }
 
   public void addProgressListener(ProgressListener l) {
     progressListener = l;
   }
   
   private void fireProgressEvent(ProgressEvent evt) {
     if(progressListener != null)
       progressListener.newProgressEvent(evt);
   }
 
   /**
    * returns a list of Validation errors.
    */
   public ValidationItems validate() {
 
     Boolean ignoreVD = (Boolean)UserSelections.getInstance().getProperty("ignore-vd");
     if(ignoreVD == null)
       ignoreVD = false;
     
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     if(vds != null) {
       ProgressEvent evt = new ProgressEvent();
       evt.setMessage("Validating Value Domains ...");
       evt.setGoal(vds.size());
       evt.setStatus(0);
       fireProgressEvent(evt);
       int count = 1;
       for(ValueDomain vd : vds) {
         evt = new ProgressEvent();
         evt.setMessage(" Validating " + vd.getLongName());
         evt.setStatus(count++);
         fireProgressEvent(evt);
         
           
 
         if(!StringUtil.isEmpty(vd.getPublicId()) && vd.getVersion() != null) {
           Map<String, Object> queryFields =
             new HashMap<String, Object>();
           queryFields.put(CadsrModule.PUBLIC_ID, vd.getPublicId());
           queryFields.put(CadsrModule.VERSION, vd.getVersion());
           List<ValueDomain> queryById = null;
           try {
             // if VD.id is populated, no need to go to db again.
             if(StringUtil.isEmpty(vd.getId()))
               queryById =  new ArrayList<ValueDomain>(cadsrModule.findValueDomain(queryFields));
             
             // vd.id not populated, and can't retrieve from db
             if(StringUtil.isEmpty(vd.getId()) && (queryById == null | queryById.size() == 0)) {
               items.addItem(new ValidationError
                         (PropertyAccessor.getProperty
                          ("local.vd.doesnt.exist", "",
                           vd.getPublicId() + "v" + vd.getVersion()), vd));
               
             } else {
            	vd.setId(queryById.get(0).getId());
            }
               List<PermissibleValue> cadsrPVs = cadsrModule.getPermissibleValues(vd);
               List<PermissibleValue> localPVs = vd.getPermissibleValues();
               
               if(!comparePVLists(cadsrPVs, localPVs)) {
                 //               if(ignoreVD)
                 items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.already.exist", vd.getLongName()), vd));
                 //               else {
                 
                 
                 // not doing theoption thing for now
                 //                 String[] options = PropertyAccessor.getProperty("local.vd.reuse.options").split("<-->");
                 //                 ValidationQuestion question = 
                 //                   new ValidationQuestion(
                 //                     PropertyAccessor.getProperty("local.vd.reuse.message"),
                 //                     vd,
                 //                     options);
                 //                 items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.already.exist", vd.getLongName()), vd));
               }
           } catch (Exception e) {
             logger.error(e);
             e.printStackTrace();
           } // end of try-catch
         } else {
           ValueDomain cadsrVD = null;          
           
           {
             Map<String, Object> queryFields = 
               new HashMap<String, Object>();
             queryFields.put(CadsrModule.LONG_NAME, vd.getLongName());
             if(mode == UMLLOADER_MODE)
               queryFields.put("context.name", UMLDefaults.getInstance().getContext().getName());
             
             try {
               Collection<ValueDomain> result =  cadsrModule.findValueDomain(queryFields);
               
               if(result.size() > 0) 
                 cadsrVD = result.iterator().next();
               
             } catch (Exception e){
               logger.error(e);
             } 
           }
           
           if(StringUtil.isEmpty(vd.getPreferredDefinition())) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.missing.definition", vd.getLongName()), vd));
           } else if (cadsrVD != null ) {
             if(!cadsrVD.getPreferredDefinition().equals(vd.getPreferredDefinition())) {
               if(ignoreVD)
                 items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.definition.mismatch", vd.getLongName()), vd));
               else
                 items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.definition.mismatch", vd.getLongName()), vd));
             }
           }
           
           if(StringUtil.isEmpty(vd.getVdType())) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.missing.vdtype", vd.getLongName()), vd));
           } else if(!vd.getVdType().equals(ValueDomain.VD_TYPE_ENUMERATED) && !vd.getVdType().equals(ValueDomain.VD_TYPE_NON_ENUMERATED)) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.wrong.vdtype", vd.getLongName()), vd));
           } else if (cadsrVD != null ) {
             if(cadsrVD.getVdType() != null)
               if(!cadsrVD.getVdType().equals(vd.getVdType())) {
                 if(ignoreVD)
                   items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.type.mismatch", vd.getLongName()), vd));
                 else
                   items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.type.mismatch", vd.getLongName()), vd));
               }
           }
         
           // START: OPTIONAL PANEL VALIDATIONS GOES HERE
           String[] defaultList = PropertyAccessor.getProperty("vd.unit.of.measures").split(",");
           String valueToBeTested = vd.getUOMName();
           if(!StringUtil.isEmpty(valueToBeTested)){
             boolean contains = contains(defaultList, valueToBeTested);
             if(!contains){
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.mismatch.uom", vd.getUOMName()), vd));
             }
           }
 
           defaultList = PropertyAccessor.getProperty("vd.display.format").split(",");
           valueToBeTested = vd.getFormatName();
           if(!StringUtil.isEmpty(valueToBeTested)){
             boolean contains = contains(defaultList, valueToBeTested);
             if(!contains){
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.mismatch.displayFormat", vd.getFormatName()), vd));
             }
           }
           
           if(vd.getMinimumLength() != null) {
             if(vd.getMinimumLength() > 9999999) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.min.length.validation.msg"), vd));
             }
           }            
           
           if(vd.getMaximumLength() != null) {
             if(vd.getMaximumLength() > 9999999) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.max.length.validation.msg"), vd));
             }
           }
           
           if(vd.getMaximumLength() != null && vd.getMinimumLength() != null) {
             if(vd.getMaximumLength() < vd.getMinimumLength()) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.min.higher.than.max.validation.msg"), vd));
             }
           }            
           
           if(vd.getDecimalPlace() != null) {
             if(vd.getDecimalPlace() > 99) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.decimal.place.validation.msg"), vd));
             }
           }
           
           // Validate that Low and High are NUMBER
           if(!StringUtil.isEmpty(vd.getLowValue())) {
             if(!vd.getLowValue().matches("\\d*\\.?\\d*"))
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.low.value.number.validation.msg"), vd));
             if(vd.getLowValue().length() > 255) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.low.value.length.validation.msg"), vd));
             }
           }
           if(!StringUtil.isEmpty(vd.getHighValue())) {
             if(!vd.getHighValue().matches("\\d*\\.?\\d*"))
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.high.value.number.validation.msg"), vd));
             if(vd.getHighValue().length() > 255) {
              items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.high.value.length.validation.msg"), vd));
             }
           }          
 
         // END: OPTIONAL PANEL VALIDATIONS 
         
           if(StringUtil.isEmpty(vd.getDataType())) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.missing.datatype", vd.getLongName()), vd));
           } else {
             Collection<String> datatypes = cadsrModule.getAllDatatypes();
             if(!datatypes.contains(vd.getDataType()))
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.invalid.datatype", vd.getLongName()), vd));
             else if (cadsrVD != null ) {
               if(!cadsrVD.getDataType().equals(vd.getDataType())) {
                 if(ignoreVD)
                   items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.datatype.mismatch", vd.getLongName()), vd));
                 else
                   items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.datatype.mismatch", vd.getLongName()), vd));
               }
             }
           }
           
           if(!StringUtil.isEmpty(vd.getRepresentation().getPublicId())) {
             if (cadsrVD != null ) {
               if(cadsrVD.getRepresentation() == null ||
                  StringUtil.isEmpty(cadsrVD.getRepresentation().getPublicId())) {
                 items.addItem
                   (new ValidationError
                    (PropertyAccessor.getProperty
                     ("cadsrvd.missing.repTerm", vd.getLongName()), vd));
               } else if(!cadsrVD.getRepresentation().getPublicId().equals(vd.getRepresentation().getPublicId())
                         ) {
                 if(ignoreVD)
                   items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.repterm.id.mismatch", vd.getLongName()), vd));
                 else
                   items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.repterm.id.mismatch", vd.getLongName()), vd));
               }
             }
           }
           if(vd.getRepresentation().getVersion() != null) {
             if (cadsrVD != null ) {
               if(cadsrVD.getRepresentation() == null ||
                  StringUtil.isEmpty(cadsrVD.getRepresentation().getPublicId())) {
                 items.addItem
                   (new ValidationError
                    (PropertyAccessor.getProperty
                     ("cadsrvd.missing.repTerm", vd.getLongName()), vd));
               } else if(!cadsrVD.getRepresentation().getVersion().equals(vd.getRepresentation().getVersion())
                         ) {
                 if(ignoreVD)
                   items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.repterm.version.mismatch", vd.getLongName()), vd));
                 else
                   items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.repterm.version.mismatch", vd.getLongName()), vd));
               }
             }
           }
           
 
           if(StringUtil.isEmpty(vd.getConceptualDomain().getPublicId())) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.missing.cdId", vd.getLongName()), vd));
           } else if (cadsrVD != null ) {
             if(!cadsrVD.getConceptualDomain().getPublicId().equals(vd.getConceptualDomain().getPublicId())
                ) {
               if(ignoreVD)
                 items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.cd.id.mismatch", vd.getLongName()), vd));
               else
                 items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.cd.id.mismatch", vd.getLongName()), vd));
             }
           }
 
           if(vd.getConceptualDomain().getVersion() == null) {
             items.addItem
               (new ValidationError
                (PropertyAccessor.getProperty
                 ("vd.missing.cdVersion", vd.getLongName()), vd));
           } else if (cadsrVD != null ) {
             if(!cadsrVD.getConceptualDomain().getVersion().equals(vd.getConceptualDomain().getVersion())
                ) {
               if(ignoreVD)
                 items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.cd.version.mismatch", vd.getLongName()), vd));
               else
                 items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.cd.version.mismatch", vd.getLongName()), vd));
             }
           }
 
 
           if(vd.getConceptualDomain().getVersion() != null && 
              vd.getConceptualDomain().getPublicId() != null) {
           
             Map<String, Object> queryFields = 
               new HashMap<String, Object>();
             queryFields.put(CadsrModule.PUBLIC_ID, vd.getConceptualDomain().getPublicId());
             try {
               queryFields.put(CadsrModule.VERSION, vd.getConceptualDomain().getVersion());
               Collection<ConceptualDomain> cds = cadsrModule.findConceptualDomain(queryFields);
               if(cds.size() != 1) {
                 items.addItem
                   (new ValidationError
                    (PropertyAccessor.getProperty
                     ("vd.cd.match.incorrect", vd.getLongName()), vd));
               } else {
                 vd.setConceptualDomain(cds.iterator().next());
               }
             } catch (NumberFormatException e) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.missing.cdId", vd.getLongName()), vd));
             } catch (Exception e){
               logger.error("Cannot query cadsr for CD " + e);
             } // end of try-catch
           }
 
           if(vd.getRepresentation().getVersion() != null && 
              vd.getRepresentation().getPublicId() != null) {
           
             Map<String, Object> queryFields = 
               new HashMap<String, Object>();
             queryFields.put(CadsrModule.PUBLIC_ID, vd.getRepresentation().getPublicId());
             try {
               queryFields.put(CadsrModule.VERSION, vd.getRepresentation().getVersion());
               Collection<Representation> repTerms = cadsrModule.findRepresentation(queryFields);
               if(repTerms.size() != 1) {
                 items.addItem
                   (new ValidationError
                    (PropertyAccessor.getProperty
                     ("vd.repTerm.match.incorrect", vd.getLongName()), vd));
               } else {
                 vd.setRepresentation(repTerms.iterator().next());
               }
             } catch (NumberFormatException e) {
               items.addItem
                 (new ValidationError
                  (PropertyAccessor.getProperty
                   ("vd.missing.repVersionId", vd.getLongName()), vd));
             } catch (Exception e){
               logger.error("Cannot query cadsr for Representation " + e);
             } 
           }
 
 
           if(cadsrVD != null) {
             // Check if VD in caDSR is the same by comparing it's permissible values
             
             try {
               List<PermissibleValue> cadsrPVs = cadsrModule.getPermissibleValues(cadsrVD);
               List<PermissibleValue> localPVs = vd.getPermissibleValues();
               
               if(!comparePVLists(cadsrPVs, localPVs)) {
                 if(ignoreVD)
                   items.addItem(new ValidationWarning(PropertyAccessor.getProperty("vd.already.exist", vd.getLongName()), vd));
                 else
                   items.addItem(new ValidationError(PropertyAccessor.getProperty("vd.already.exist", vd.getLongName()), vd));
               } else {
                 // same VD, update field in the localVD
                 //               vd.setPermissibleValues(cadsrPVs);
 
                 if(mode == UMLLOADER_MODE) {
                   vd.setPublicId(cadsrVD.getPublicId());
                   vd.setVersion(cadsrVD.getVersion());
                 }
               }
             } catch (Exception e) {
               logger.error(e);
             } // end of try-catch
           }
         }
       }    
         
     List<DataElement> des = elements.getElements(DomainObjectFactory.newDataElement());
     boolean match = false;
     
     if(des != null) {
       for(ValueDomain vd : vds) {
         match = false;
         for(DataElement de : des) {
           if(vd.getLongName().equals(de.getValueDomain().getLongName())) {
             match = true;
             break;
           }
         }
         if(!match)
           items.addItem(new ValidationWarning
                         (PropertyAccessor.getProperty
                           ("vd.not.used", vd.getLongName()), vd));
       }
     }
 
     evt = new ProgressEvent();
     evt.setMessage("Validating Value Domains ...");
     evt.setGoal(vds.size());
     evt.setStatus(0);
     fireProgressEvent(evt);
     count = 1;
     for(ValueDomain vd : vds) {
       evt = new ProgressEvent();
       evt.setMessage(" Validating " + vd.getLongName());
       evt.setStatus(count++);
       fireProgressEvent(evt);
       
       if(vd.getConceptDerivationRule() != null) {
         List<ComponentConcept> vdList = vd.getConceptDerivationRule().getComponentConcepts();
         List<Concept> vdConceptList = new ArrayList<Concept>();
         for(ComponentConcept cc : vdList)
           vdConceptList.add(cc.getConcept());
         if(vdConceptList.isEmpty())
           continue;
         for(ValueDomain currentVd : vds)     
           if(vd != currentVd && currentVd.getConceptDerivationRule() != null) {
             List<ComponentConcept> currentVdList = currentVd.getConceptDerivationRule().getComponentConcepts();
             List<Concept> currentVdConceptList = new ArrayList<Concept>();
             for(ComponentConcept cc : currentVdList)
               currentVdConceptList.add(cc.getConcept());
             if(currentVdConceptList.isEmpty())
               continue;
             if(vdConceptList.size() == currentVdConceptList.size()
                && vdConceptList.containsAll(currentVdConceptList)) 
               items.addItem(new ValidationError
                             (PropertyAccessor.getProperty
                              ("vd.same.concepts", vd.getLongName(), currentVd.getLongName()),vd));         
           }
       }
     }
     }
     return items;
   }
   
   private boolean contains(String[] defaultList, String valueToBeTested){
       boolean contains = false;  
       for (String uomValue : defaultList)
         if(uomValue.equals(valueToBeTested))
             contains = true;
       return contains;
   }
 
   public void setCadsrModule(CadsrModule module) {
     this.cadsrModule = module;
   }
 
   private boolean comparePVLists(List<PermissibleValue> pvList1, List<PermissibleValue> pvList2) 
   {
     if(pvList1 == null || pvList2 == null)
       return false;
 
     if(pvList1.size() != pvList2.size()) 
       return false;
 
     if(pvList1.size() == 0 && pvList2.size() == 0)
       return true;
 
     boolean found = true;
     Iterator<PermissibleValue> it1 = pvList1.iterator();
     //    while(it1.hasNext() && found == true) {
     while(it1.hasNext() && found) {
       found = false;
       PermissibleValue pv1 = it1.next();
       Iterator<PermissibleValue> it2 = pvList2.iterator();
       while(found == false && it2.hasNext()) {
         PermissibleValue pv2 = it2.next();
         if(pv2.getValue().equalsIgnoreCase(pv1.getValue())) {
           found = true;
         }
       }
       if(!found)
         return false;
     }
     // return true if we found the last match 
     return (found == true && !it1.hasNext());
   
 }  
 
 
 
 }
