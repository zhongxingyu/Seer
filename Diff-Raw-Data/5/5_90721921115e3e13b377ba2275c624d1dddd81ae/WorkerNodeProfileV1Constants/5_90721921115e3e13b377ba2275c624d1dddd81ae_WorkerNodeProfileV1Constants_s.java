 /*
  * Copyright 2010 Members of the EGEE Collaboration.
  * See http://www.eu-egee.org/partners for details on the copyright holders. 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common.profile;
 
 /** Constants related to the Grid Worker Node Authorization Profile, version 1.0. */
 public class WorkerNodeProfileV1Constants {
 
     /** Identifier for the "execute" action. */
     public static final String ACT_EXECUTE = ProfileConstants.OPENXACML_ACTION_STEM + "/execute";
 
     /** Identifier for FQAN attributes. */
     public static final String ATT_FQAN = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/fqan";
 
     /** Identifier for group ID attributes. */
     public static final String ATT_GROUP_ID = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/group-id";
 
     /** Identifier for pilot job classifier attributes. */
     public static final String ATT_PILOT_JOB_CLASSIFIER = ProfileConstants.OPENXACML_ATTRIBUTE_STEM
             + "/pilot-job-classifier";
 
     /** Identifier for primary FQAN attributes. */
     public static final String ATT_PRIMARY_FQAN = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/fqan/primary";
 
     /** Identifier for primary group ID attributes. */
     public static final String ATT_PRIMARY_GROUP_ID = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/group-id/primary";

     /** Identifier for subject issuer attributes. */
     public static final String ATT_SUBJECT_ISSUER = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/subject-issuer";
 
     /** Identifier for user ID attributes. */
     public static final String ATT_USER_ID = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/user-id";
 
     /** Identifier for virtual organization attributes. */
     public static final String ATT_VO = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/virtual-organization";
 
     /** Identifier for VOMS issuer attributes. */
     public static final String ATT_VOMS_ISSUER = ProfileConstants.OPENXACML_ATTRIBUTE_STEM + "/voms-issuer";
 
     /** Identifier for the FQAN data type. */
     public static final String DAT_FQAN = ProfileConstants.OPENXACML_DATATYPE_STEM + "/fqan";
 
     /** Identifier for the exact FQAN matching function. */
     public static final String ALG_FQAN_EXACT = ProfileConstants.OPENXACML_ALGORITHM_STEM + "/fqan-match";
 
     /** Identifier for the regular expression FQAN matching function. */
     public static final String ALG_FQAN_REGEXP = ProfileConstants.OPENXACML_ALGORITHM_STEM + "/fqan-regexp-match";
 
     /** Identifier for the local environment mapping obligation. */
     public static final String OBL_LOCAL_ENV_MAP = ProfileConstants.OPENXACML_OBLIGATION_STEM + "/local-environment-map";
 
     /** Identifier for the local POSIX environment mapping obligation. */
     public static final String OBL_POSIX_ENV_MAP = ProfileConstants.OPENXACML_OBLIGATION_STEM
             + "/lcal-environment-map/posix";
 
     /** Identifier for this version of the Grid Worker Node Authorization Profile. */
     public static final String PRO_ID = ProfileConstants.OPENXACML_PROFILE_STEM + "/grid-wn/1.0";
 
 }
