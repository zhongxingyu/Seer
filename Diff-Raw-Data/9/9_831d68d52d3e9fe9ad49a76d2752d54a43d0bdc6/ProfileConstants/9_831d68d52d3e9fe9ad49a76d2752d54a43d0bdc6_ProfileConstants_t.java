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
 
 /** Profile-related constants. */
 public class ProfileConstants {
 
     /** Base URN NSS for MACE XACML identifiers. */
    public static final String GLITE_ORG_STEM = "http://glite.org/xacml";
 
     /** Base URN NSS for MACE XACML action identifiers. */
     public static final String GLITE_ORG_ACTION_STEM = GLITE_ORG_STEM + "/action";
 
     /** Base URN NSS for MACE XACML attribute identifiers. */
     public static final String GLITE_ORG_ATTRIBUTE_STEM = GLITE_ORG_STEM + "/attribute";
 
     /** Base URN NSS for MACE XACML datatype identifiers. */
     public static final String GLITE_ORG_DATATYPE_STEM = GLITE_ORG_STEM + "/datatype";
 
     /** Base URN NSS for MACE XACML algorithm identifiers. */
     public static final String GLITE_ORG_ALGORITHM_STEM = GLITE_ORG_STEM + "/algorithm";
 
     /** Base URN NSS for MACE XACML obligation identifiers. */
     public static final String GLITE_ORG_OBLIGATION_STEM = GLITE_ORG_STEM + "/obligation";
 
     /** Base URN NSS for MACE XACML profile identifiers. */
     public static final String GLITE_ORG_PROFILE_STEM = GLITE_ORG_STEM + "/profile";
 }
