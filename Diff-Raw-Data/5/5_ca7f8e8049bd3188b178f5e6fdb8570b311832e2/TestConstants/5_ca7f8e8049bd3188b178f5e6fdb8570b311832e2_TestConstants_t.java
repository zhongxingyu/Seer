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
 package com.github.podd.api.test;
 
 /**
  * Interface containing constant values for use in test verification.
  */
 public interface TestConstants
 {
     
     /* Expected number of triples in schema ontologies */
     
     public static final int EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE = 43;
     public static final int EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED = 8;
     
     public static final int EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE = 38;
     public static final int EXPECTED_TRIPLE_COUNT_FOAF_INFERRED = 19;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE = 217;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED = 35;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE = 352;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED = 70;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE = 1338;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED = 209;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE = 214;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED = 355;
     
     /** Test resource: artifact with no internal objects */
     public static final String TEST_ARTIFACT_BASIC_PROJECT_1 = "/test/artifacts/basicProject-1.rdf";
     public static final int TEST_ARTIFACT_BASIC_PROJECT_1_CONCRETE_TRIPLES = 21;
     public static final int TEST_ARTIFACT_BASIC_PROJECT_1_INFERRED_TRIPLES = 295;
     
     /** Test resource: artifact with 1 internal object */
     public static final String TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT = "/test/artifacts/basic-1-internal-object.rdf";
     public static final int TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES = 29;
     public static final int TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES = 326;
     
     /** Test resource: artifact with 1 internal object, slightly different */
     public static final String TEST_ARTIFACT_BASIC_PROJECT_2 = "/test/artifacts/basicProject-2.rdf";
     public static final int TEST_ARTIFACT_BASIC_PROJECT_2_CONCRETE_TRIPLES = 25;
     public static final int TEST_ARTIFACT_BASIC_PROJECT_2_INFERRED_TRIPLES = 320;
     
     /** Test resource: (in RDF/XML) artifact with published status */
     public static final String TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED = "/test/artifacts/basicProject-1-published.rdf";
     public static final int TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_CONCRETE_TRIPLES = 25;
     public static final int TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_INFERRED_TRIPLES = 320;
     
     public static final String TEST_ARTIFACT_PURLS_2_FILE_REFS = "/test/artifacts/project-purls-file-ref.rdf";
     
     public static final String TEST_ARTIFACT_PURLS_2_SPARQL_DATA_REFS = "/test/artifacts/project-purls-sparql-ref.rdf";
     
     /** Test resource (in RDF/XML): invalid artifact with 2 lead institutes */
     public static final String TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES = "/test/artifacts/bad-twoLeadInstitutions.rdf";
     
     /** Test resource (in Turtle): invalid artifact with 3 top objects */
     public static final String TEST_ARTIFACT_BAD_3_TOP_OBJECTS = "/test/artifacts/bad-3-topobjects.ttl";
     
     /** Test resource (in RDF/XML): invalid artifact not in OWL-DL profile */
     public static final String TEST_ARTIFACT_BAD_NOT_OWL_DL = "/test/artifacts/bad-not-owl-dl.rdf";
     
     /** Test resource: invalid with empty file */
     public static final String TEST_INVALID_ARTIFACT_EMPTY = "/test/ontologies/empty.owl";
     
     /** Test resource (in RDF/XML): artifact imports v1 of PODD Science schema ontology */
     public static final String TEST_ARTIFACT_IMPORT_PSCIENCEv1 =
             "/test/artifacts/project-imports-sciencev1-version.rdf";
     public static final int TEST_ARTIFACT_IMPORT_PSCIENCEv1_CONCRETE_TRIPLES = 29;
     public static final int TEST_ARTIFACT_IMPORT_PSCIENCEv1_INFERRED_TRIPLES = 326;
     
     /** Test resource (in RDF/XML): artifact imports v1 of PODD Science schema ontology */
     public static final String TEST_ARTIFACT_NO_VERSION_INFO = "/test/artifacts/project-with-no-version-info.rdf";
     public static final int TEST_ARTIFACT_NO_VERSION_INFO_CONCRETE_TRIPLES = 29;
    public static final int TEST_ARTIFACT_NO_VERSION_INFO_INFERRED_TRIPLES = 326;
     
     /** Test resource (in RDF/XML): artifact imports v1 of PODD Science schema ontology */
     public static final String TEST_ARTIFACT_PURLS_v1 = "/test/artifacts/project-with-purls-v1.rdf";
     public static final int TEST_ARTIFACT_PURLS_v1_CONCRETE_TRIPLES = 25;
    public static final int TEST_ARTIFACT_PURLS_v1_INFERRED_TRIPLES = 320;
     
     /** Test resource (in Turtle): artifact with temporary URIs and 1 internal object having a PURL */
     public static final String TEST_ARTIFACT_TTL_1_INTERNAL_OBJECT = "/test/artifacts/connected-1-object.ttl";
     
     /** Test resource: artifact with PURLs and multiple internal objects in Turtle format */
     public static final String TEST_ARTIFACT_20130206 = "/test/artifacts/basic-20130206.ttl";
     public static final int TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES = 91;
     public static final int TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES = 558;
     
     /** Test resource: artifact with PURLs and a refersTo link in Turtle format */
     public static final String TEST_ARTIFACT_WITH_REFERSTO = "/test/artifacts/artifact-with-refers-to.ttl";
     public static final int TEST_ARTIFACT_WITH_REFERSTO_CONCRETE_TRIPLES = 82;
     public static final int TEST_ARTIFACT_WITH_REFERSTO_INFERRED_TRIPLES = 552;
     
     /** Test resource (in Turtle): inferred artifact with PURLs and multiple internal objects */
     public static final String TEST_ARTIFACT_20130206_INFERRED = "/test/artifacts/basic-20130206-inferred.ttl";
     
     /** Test resource: fragment containing a Publication object in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT =
             "/test/artifacts/fragment-new-publication.ttl";
     
     /** Test resource: fragment containing a Publication object in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_INCONSISTENT_OBJECT =
             "/test/artifacts/fragment-inconsistent-object.ttl";
     
     /** Test resource: fragment containing a Publication object in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT =
             "/test/artifacts/fragment-modified-publication.ttl";
     
     /** Test resource: fragment containing a new File Reference object in RDF/XML format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT = "/test/artifacts/fragment-new-file-ref.rdf";
     
     /** Test resource: fragment containing a new File Reference object in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT_TTL =
             "/test/artifacts/fragment-new-file-ref.ttl";
     
     /** Test resource: fragment containing multiple objects with temporary URIs in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MULTIPLE_OBJECTS_TTL =
             "/test/artifacts/fragment-new-multiple-objects.ttl";
     
     /** Test resource: fragment containing platform objects with temporary URIs in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_PLATFORM_OBJECTS =
             "/test/artifacts/fragment-new-platform-objects.ttl";
     
     /**
      * Test resource: fragment modifying Demo_Investigation object to no longer contain
      * SqueekeeMaterial. In Turtle format
      */
     public static final String TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION =
             "/test/artifacts/fragment-modify-demo-investigation.ttl";
     
     /** Test resource: fragment moves SqueekeeMaterial to under My_Treatment1. In Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MOVE_DEMO_INVESTIGATION =
             "/test/artifacts/fragment-move-demo-investigation.ttl";
     
     /** Test resource: an alias file which is disconnected */
     public static final String TEST_ALIAS_BAD = "/test/bad-alias.ttl";
     
     /** Test resource: any file which exists and can be used as a file reference */
     public static final String TEST_FILE_REFERENCE_PATH = "/test/artifacts/basic-2.rdf";
     
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE =
             "/test/artifacts/fragment-new-file-ref-verifiable.rdf";
     
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE_TTL =
             "/test/artifacts/fragment-new-file-ref-verifiable.ttl";
     
     /**
      * Test resource: represents a file which is used to represent a resource stored on a remote
      * file repository
      */
     public static final String TEST_REMOTE_FILE_PATH = "/test";
     public static final String TEST_REMOTE_FILE_NAME = "sample-resource.txt";
     
 }
