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
 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 
import gov.nih.nci.ncicb.cadsr.loader.util.LookupUtil;

 /**
  * utility to build role name for OCRs that are missing one
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class OCRRoleNameBuilder  {
 
 
   public String buildRoleName(ObjectClassRelationship ocr) 
   {
     return buildRoleName(ocr, true);
   }
 
   public String buildDisplayRoleName(ObjectClassRelationship ocr) 
   {
     return buildRoleName(ocr, false);
   }
 
   private String buildRoleName(ObjectClassRelationship ocr, boolean longName) {
 
     if(ocr.getType().equals(ocr.TYPE_IS)) {
      return LookupUtil.lookupFullName(ocr.getSource()) + "-->" + LookupUtil.lookupFullName(ocr.getTarget());
     }
 
     StringBuffer roleName = new StringBuffer();
     
     if(ocr.getDirection().equals(ObjectClassRelationship.DIRECTION_BOTH)) {
       if(longName) {
         roleName.append(ocr.getSource().getLongName()); }
       if (ocr.getSourceRole() != null) {
         if(longName)
           roleName.append(".")
             .append(ocr.getSourceRole());
         else
           roleName.append(ocr.getSourceRole());
       }
       
       String cardinality = "" + ocr.getSourceLowCardinality();
       if(ocr.getSourceLowCardinality() != ocr.getSourceHighCardinality()) {
         cardinality += ".."; 
         if(ocr.getSourceHighCardinality()==-1) {
           cardinality += "*";
         } else {
           cardinality += ocr.getSourceHighCardinality();
         }
       }
       
       roleName.append("(")
         .append(cardinality)
         .append(")::");
     }
     
     if(longName) {
     roleName.append(ocr.getTarget().getLongName()); }
 
     if (ocr.getTargetRole() != null) {
       if(longName)
         roleName.append(".")
           .append(ocr.getTargetRole());
       else
         roleName.append(ocr.getTargetRole());
     }
 
     String cardinality = "" + ocr.getTargetLowCardinality();
     if(ocr.getTargetLowCardinality() != ocr.getTargetHighCardinality()) {
       cardinality += ".."; 
       if(ocr.getTargetHighCardinality()==-1) {
 	cardinality += "*";
       } else {
 	cardinality += ocr.getTargetHighCardinality();
       }
     }    
 
     roleName.append("(")
       .append(cardinality)
       .append(")");
 
     return roleName.toString();
   }
 
 }
