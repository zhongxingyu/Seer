 /**
  *   The caMOD Software License, Version 1.0
  *
  *   Copyright 2005-2006 SAIC. This software was developed in conjunction with the National Cancer
  *   Institute, and so to the extent government employees are co-authors, any rights in such works
  *   shall be subject to Title 17 of the United States Code, section 105.
  *
  *   Redistribution and use in source and binary forms, with or without modification, are permitted
  *   provided that the following conditions are met:
  *
  *   1. Redistributions of source code must retain the above copyright notice, this list of conditions
  *   and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *
  *   2.  The end-user documentation included with the redistribution, if any, must include the
  *   following acknowledgment:
  *
  *   "This product includes software developed by the SAIC and the National Cancer
  *   Institute."
  *
  *   If no such end-user documentation is to be included, this acknowledgment shall appear in the
  *   software itself, wherever such third-party acknowledgments normally appear.
  *
  *   3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or
  *   promote products derived from this software.
  *
  *   4. This license does not authorize the incorporation of this software into any third party proprietary
  *   programs.  This license does not authorize the recipient to use any trademarks owned by either
  *   NCI or SAIC.
  *
  *
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *   WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *   DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR
  *   THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  *   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  *   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *   
 * $Id: NameValueList.java,v 1.6 2008-05-22 18:21:06 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.5  2008/05/21 19:04:36  pandyas
  * Modified advanced search to prevent SQL injection
  * Concolidated all utility methods in new class
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.4  2008/05/05 15:07:45  pandyas
  * NCI security scan changes - could not get value to work without spaces - trim did not work so added another entry to validate
  *
  * Revision 1.3  2008/02/18 16:19:58  pandyas
  * Fixed typo in rat scientific name
  *
  * Revision 1.2  2007/10/18 18:26:36  pandyas
  * Added to prevent cross--site scripting attacks
  *
  *
  */
 package gov.nih.nci.camod.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class NameValueList
 {
     // stores a list of species
     private static List approvedSpeciesList = new ArrayList();
 
     // stores a list of induced mutation agents
     private static List inducedMutationAgentList = new ArrayList();
 
     // stores a list of carcinogenic interventions
     private static List carcinogenicInterventionList = new ArrayList();
 
     // stores a list of external Sources
     private static List externalSourceList = new ArrayList(); 
     
     // stores a list of Table of Content query names
     private static List tableOfContentsList = new ArrayList();    
     
 
     
     public static void generateTableOfContentsList() {
 
     	tableOfContentsList = new ArrayList();
     	tableOfContentsList.add(new NameValue("Cardiovascular_System_Query","Cardiovascular_System_Query"));
     	tableOfContentsList.add(new NameValue("Digestive_System_Query","Digestive_System_Query"));    	
     	tableOfContentsList.add(new NameValue("Endocrine_Gland_Query","Endocrine_Gland_Query"));
     	tableOfContentsList.add(new NameValue("Integument_System_Query","Integument_System_Query"));
     	tableOfContentsList.add(new NameValue("Lymphohematopoietic_System_Query","Lymphohematopoietic_System_Query"));
     	tableOfContentsList.add(new NameValue("Musculoskeletal_System_Query","Musculoskeletal_System_Query"));
     	tableOfContentsList.add(new NameValue("Nervous_System_Query","Nervous_System_Query"));
     	tableOfContentsList.add(new NameValue("Reproductive_System_Query","Reproductive_System_Query"));
     	tableOfContentsList.add(new NameValue("Respratory_System_Query","Respratory_System_Query"));
     	tableOfContentsList.add(new NameValue("Special_Sensory_Organs_Query","Special_Sensory_Organs_Query"));
     	tableOfContentsList.add(new NameValue("Urinary_System_Query","Urinary_System_Query"));
     	tableOfContentsList.add(new NameValue("Head_or_Neck_Query","Head_or_Neck_Query"));
     	tableOfContentsList.add(new NameValue("Prostate_Lesion_Query","Prostate_Lesion_Query"));
     	tableOfContentsList.add(new NameValue("Mammary_Gland_Query","Mammary_Gland_Query"));
     	tableOfContentsList.add(new NameValue("Brain_Tumor_Query","Brain_Tumor_Query"));
     	tableOfContentsList.add(new NameValue("Liver_Tumor_Query","Liver_Tumor_Query"));
     	tableOfContentsList.add(new NameValue("Metastases_Query","Metastases_Query"));
     	tableOfContentsList.add(new NameValue("Rattus_Norvegicus_Query","Rattus_Norvegicus_Query"));
     	tableOfContentsList.add(new NameValue("Rattus_Rattus_Query","Rattus_Rattus_Query"));
     	tableOfContentsList.add(new NameValue("Transplant_Query","Transplant_Query"));
     	
     }
 
     public static List getTableOfContentsList()  {
     	generateTableOfContentsList();
         return  tableOfContentsList ;
     }    
     
     public static void generateExternalSourceList() {
 
         externalSourceList = new ArrayList();
         externalSourceList.add(new NameValue("Jax MTB","Jax MTB"));
     }
 
     public static List getExternalSourceList()  {
         generateExternalSourceList();
         return  externalSourceList ;
     }    
     
     public static void generateApprovedSpeciesList() {
         approvedSpeciesList = new ArrayList();
         // Put in order of most entered
         approvedSpeciesList.add(new NameValue("Mus musculus","Mus musculus"));  
         approvedSpeciesList.add(new NameValue("Rattus norvegicus","Rattus norvegicus"));
         approvedSpeciesList.add(new NameValue("Rattus rattus","Rattus rattus"));
         approvedSpeciesList.add(new NameValue("Danio rerio","Danio rerio"));  
         approvedSpeciesList.add(new NameValue("Mesocricetus auratus","Mesocricetus auratus"));
         approvedSpeciesList.add(new NameValue("Felis catus","Felis catus"));
         approvedSpeciesList.add(new NameValue("Bos taurus","Bos taurus"));  
         approvedSpeciesList.add(new NameValue("Canis familiaris","Canis familiaris"));         
         approvedSpeciesList.add(new NameValue("Homo sapiens","Homo sapiens")); 
         approvedSpeciesList.add(new NameValue("Meriones unguiculatus","Meriones unguiculatus")); 
         approvedSpeciesList.add(new NameValue("Cavia porcellus","Cavia porcellus")); 
         approvedSpeciesList.add(new NameValue("Capra hircus","Capra hircus"));         
        approvedSpeciesList.add(new NameValue("Equus Caballus","Equus Caballus")); 
         approvedSpeciesList.add(new NameValue("Ovis aries","Ovis aries")); 
         approvedSpeciesList.add(new NameValue("Sus scrofa","Sus scrofa"));         
         approvedSpeciesList.add(new NameValue("Saccharomyces cerevisiae","Saccharomyces cerevisiae"));     
 
     }
     public static List getApprovedSpeciesList() {
         generateApprovedSpeciesList();
         return  approvedSpeciesList ;
     } 
     
     public static void generateInducedMutationAgentList() {
 
         inducedMutationAgentList = new ArrayList();
         inducedMutationAgentList.add(new NameValue("ENU","ENU"));
         inducedMutationAgentList.add(new NameValue("ethylnitrosourea","ethylnitrosourea"));
         inducedMutationAgentList.add(new NameValue("name of inducing agent","name of inducing agent"));        
     }
 
     public static List getInducedMutationAgentList()  {
         generateInducedMutationAgentList();
         return  inducedMutationAgentList ;
     } 
     
     public static void generateCarcinogenicInterventionList() {
 
         carcinogenicInterventionList = new ArrayList();
         carcinogenicInterventionList.add(new NameValue("Chemical / Drug","Chemical / Drug"));
         carcinogenicInterventionList.add(new NameValue("Environment","Environment"));
         carcinogenicInterventionList.add(new NameValue("Growth Factor","Growth Factor")); 
         carcinogenicInterventionList.add(new NameValue("Hormone","Hormone"));
         carcinogenicInterventionList.add(new NameValue("Nutrition","Nutrition")); 
         carcinogenicInterventionList.add(new NameValue("Other","Other"));
         carcinogenicInterventionList.add(new NameValue("Radiation","Radiation")); 
         carcinogenicInterventionList.add(new NameValue("Viral","Viral"));
         carcinogenicInterventionList.add(new NameValue("Antibody","Antibody")); 
         
         carcinogenicInterventionList.add(new NameValue("Bacteria","Bacteria"));
         carcinogenicInterventionList.add(new NameValue("Plasmid","Plasmid")); 
         carcinogenicInterventionList.add(new NameValue("Signaling Molecule","Signaling Molecule"));        
         carcinogenicInterventionList.add(new NameValue("Transposon","Transposon"));        
         
     }
 
     public static List getCarcinogenicInterventionList()  {
         generateCarcinogenicInterventionList();
         return  carcinogenicInterventionList ;
     }   
     
  
  
 }
