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
 
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 
 /**
  * Interface containing URI constants for the Ontologies needed in PODD.
  * 
  * @author kutila
  * 
  */
 public interface PODD
 {
     public static final ValueFactory VF = ValueFactoryImpl.getInstance();
     
     public static final String DATA_REPOSITORY = "http://purl.org/podd/ns/dataRepository#";
     
     public static final String PROPERTY_USER_MANAGEMENT_GRAPH = "podd.graph.usermanagement";
    /** Default value is urn:podd:default:graph:usermanagement */
     public static final URI DEFAULT_USER_MANAGEMENT_GRAPH = PODD.VF.createURI("urn:podd:default:graph:usermanagement");
     
     public static final String PROPERTY_ARTIFACT_MANAGEMENT_GRAPH = "podd.graph.artifactmanagement";
    /** Default value is urn:podd:default:graph:artifactmanagement */
     public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PODD.VF
             .createURI("urn:podd:default:graph:artifactmanagement");
     
     public static final String PROPERTY_DATA_REPOSITORY_MANAGEMENT_GRAPH = "podd.graph.datarepositorymanagement";
    /** Default value is urn:podd:default:graph:datarepositorymanagement */
     public static final URI DEFAULT_DATA_REPOSITORY_MANAGEMENT_GRAPH = PODD.VF
             .createURI("urn:podd:default:graph:datarepositorymanagement");
     
     public static final String PROPERTY_REPOSITORY_MANAGEMENT_GRAPH = "podd.graph.repositorymanagement";
    /** Default value is urn:podd:default:graph:repositorymanagement */
     public static final URI DEFAULT_REPOSITORY_MANAGEMENT_GRAPH = PODD.VF
             .createURI("urn:podd:default:graph:repositorymanagement");
     
     public static final String PROPERTY_SCHEMA_MANAGEMENT_GRAPH = "podd.schemagraph";
     /** Default value is urn:podd:default:graph:schemamanagement */
     public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PODD.VF
             .createURI("urn:podd:default:graph:schemamanagement");
     
     /** http://purl.org/podd/ns/err#contains */
     public static final URI ERR_CONTAINS = PODD.VF.createURI(PODD.PODD_ERROR, "contains");
     
     /** The Exception class http://purl.org/podd/ns/err#exceptionClass */
     public static final URI ERR_EXCEPTION_CLASS = PODD.VF.createURI(PODD.PODD_ERROR, "exceptionClass");
     
     /** What/who identified the error http://purl.org/podd/ns/err#identifier */
     public static final URI ERR_IDENTIFIER = PODD.VF.createURI(PODD.PODD_ERROR, "identifier");
     
     /** Error source/what caused the error http://purl.org/podd/ns/err#source */
     public static final URI ERR_SOURCE = PODD.VF.createURI(PODD.PODD_ERROR, "source");
     
     /** Type to identify an Error http://purl.org/podd/ns/err#Error */
     public static final URI ERR_TYPE_ERROR = PODD.VF.createURI(PODD.PODD_ERROR, "Error");
     
     /** Type to identify the TopError http://purl.org/podd/ns/err#TopError */
     public static final URI ERR_TYPE_TOP_ERROR = PODD.VF.createURI(PODD.PODD_ERROR, "TopError");
     
     public static final String HTTP = "http://www.w3.org/2011/http#";
     
     /** http://www.w3.org/2011/http#reasonPhrase */
     public static final URI HTTP_REASON_PHRASE = ValueFactoryImpl.getInstance().createURI(PODD.HTTP, "reasonPhrase");
     
     /** http://www.w3.org/2011/http#statusCodeValue */
     public static final URI HTTP_STATUS_CODE_VALUE = ValueFactoryImpl.getInstance().createURI(PODD.HTTP,
             "statusCodeValue");
     
     /**
      * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
      * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
      * using the version IRI of the original ontology, which must be unique.
      */
     public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";
     
     /**
      * The key used in the podd.properties file to locate the data repository aliases file.
      */
     public static final String KEY_ALIASES = "podd.datarepository.aliases";
     
     /**
      * The key used in the podd.properties file to locate the schema manifest file.
      */
     public static final String KEY_SCHEMAS = "podd.schemas.manifest";
     
     /**
      * The OMV vocabulary defines a property for the current version of an ontology, so we are
      * reusing it here.
      */
     public static final URI OMV_CURRENT_VERSION = PODD.VF.createURI("http://omv.ontoware.org/ontology#",
             "currentVersion");
     
     public static final URI OWL_MAX_QUALIFIED_CARDINALITY = PODD.VF
             .createURI("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
     
     public static final URI OWL_MIN_QUALIFIED_CARDINALITY = PODD.VF
             .createURI("http://www.w3.org/2002/07/owl#minQualifiedCardinality");
     
     public static final URI OWL_QUALIFIED_CARDINALITY = PODD.VF
             .createURI("http://www.w3.org/2002/07/owl#qualifiedCardinality");
     
     public static final URI OWL_VERSION_IRI = OWL.VERSIONIRI;
     
     /** Path to default alias file */
     public static final String PATH_DEFAULT_ALIASES_FILE = "/com/github/podd/api/file/default-file-repositories.ttl";
     
     /** Path to default alias file */
     public static final String PATH_DEFAULT_SCHEMAS = "/podd-schema-manifest.ttl";
     
     public static final String PATH_BASE_ONTOLOGIES_VERSION_1 = "/ontologies/version/1/";
     
     /** Path to poddAnimal.owl */
     public static final String PATH_PODD_ANIMAL_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddAnimal.owl";
     
     /** Path to poddBase.owl */
     public static final String PATH_PODD_BASE_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddBase.owl";
     
     /**
      * Path to poddDataRepository.owl.
      * 
      * This ontology is NOT part of the standard schema ontologies. It is a separate ontology used
      * to validate Data Repository configurations.
      */
     public static final String PATH_PODD_DATA_REPOSITORY_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1
             + "poddDataRepository.owl";
     
     /** Path to dcTerms.owl */
     public static final String PATH_PODD_DCTERMS_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "dcTerms.owl";
     
     /** Path to foaf.owl */
     public static final String PATH_PODD_FOAF_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "foaf.owl";
     
     /** Path to poddPlant.owl */
     public static final String PATH_PODD_PLANT_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddPlant.owl";
     
     /** Path to poddScience.owl */
     public static final String PATH_PODD_SCIENCE_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddScience.owl";
     
     /** Path to poddUser.owl */
     public static final String PATH_PODD_USER_V1 = PODD.PATH_BASE_ONTOLOGIES_VERSION_1 + "poddUser.owl";
     
     public static final String PATH_BASE_ONTOLOGIES_VERSION_2 = "/ontologies/version/2/";
     
     /** Path to poddAnimal.owl */
     public static final String PATH_PODD_ANIMAL_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "poddAnimal.owl";
     
     /** Path to poddBase.owl */
     public static final String PATH_PODD_BASE_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "poddBase.owl";
     
     /**
      * Path to poddDataRepository.owl.
      * 
      * This ontology is NOT part of the standard schema ontologies. It is a separate ontology used
      * to validate Data Repository configurations.
      */
     public static final String PATH_PODD_DATA_REPOSITORY_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2
             + "poddDataRepository.owl";
     
     /** Path to dcTerms.owl */
     public static final String PATH_PODD_DCTERMS_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "dcTerms.owl";
     
     /** Path to foaf.owl */
     public static final String PATH_PODD_FOAF_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "foaf.owl";
     
     /** Path to poddPlant.owl */
     public static final String PATH_PODD_PLANT_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "poddPlant.owl";
     
     /** Path to poddScience.owl */
     public static final String PATH_PODD_SCIENCE_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "poddScience.owl";
     
     /** Path to poddUser.owl */
     public static final String PATH_PODD_USER_V2 = PODD.PATH_BASE_ONTOLOGIES_VERSION_2 + "poddUser.owl";
     
     public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";;
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One */
     public static final URI PODD_BASE_CARDINALITY_EXACTLY_ONE = PODD.VF.createURI(PODD.PODD_BASE,
             "Cardinality_Exactly_One");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ONE_OR_MANY = PODD.VF.createURI(PODD.PODD_BASE,
             "Cardinality_One_Or_Many");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_MANY = PODD.VF.createURI(PODD.PODD_BASE,
             "Cardinality_Zero_Or_Many");
     
     /** http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_One */
     public static final URI PODD_BASE_CARDINALITY_ZERO_OR_ONE = PODD.VF.createURI(PODD.PODD_BASE,
             "Cardinality_Zero_Or_One");
     
     public static final URI PODD_BASE_CONTAINS = PODD.VF.createURI(PODD.PODD_BASE, "contains");
     
     /** http://purl.org/podd/ns/poddBase#createdAt */
     public static final URI PODD_BASE_CREATED_AT = PODD.VF.createURI(PODD.PODD_BASE, "createdAt");
     
     /**
      * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
      * ontology when linking from the ontology IRI.
      */
     public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PODD.VF.createURI(PODD.PODD_BASE,
             "currentInferredVersion");
     
     /** http://purl.org/podd/ns/poddBase#DataReference */
     public static final URI PODD_BASE_DATA_REFERENCE_TYPE = PODD.VF.createURI(PODD.PODD_BASE, "DataReference");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileReference */
     public static final URI PODD_BASE_DATA_REFERENCE_TYPE_SPARQL = PODD.VF.createURI(PODD.PODD_BASE,
             "SPARQLDataReference");
     
     /** http://purl.org/podd/ns/poddBase#hasDisplayType */
     public static final URI PODD_BASE_DISPLAY_TYPE = PODD.VF.createURI(PODD.PODD_BASE, "hasDisplayType");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_CheckBox */
     public static final URI PODD_BASE_DISPLAY_TYPE_CHECKBOX = PODD.VF.createURI(PODD.PODD_BASE, "DisplayType_CheckBox");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_DropDownList */
     public static final URI PODD_BASE_DISPLAY_TYPE_DROPDOWN = PODD.VF.createURI(PODD.PODD_BASE,
             "DisplayType_DropDownList");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_Table */
     public static final URI PODD_BASE_DISPLAY_TYPE_FIELDSET = PODD.VF.createURI(PODD.PODD_BASE, "DisplayType_FieldSet");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_LongText */
     public static final URI PODD_BASE_DISPLAY_TYPE_LONGTEXT = PODD.VF.createURI(PODD.PODD_BASE, "DisplayType_LongText");
     
     /** http://purl.org/podd/ns/poddBase#DisplayType_ShortText */
     public static final URI PODD_BASE_DISPLAY_TYPE_SHORTTEXT = PODD.VF.createURI(PODD.PODD_BASE,
             "DisplayType_ShortText");
     
     public static final URI PODD_BASE_DO_NOT_DISPLAY = PODD.VF.createURI(PODD.PODD_BASE, "doNotDisplay");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileReference */
     public static final URI PODD_BASE_FILE_REFERENCE_TYPE_SSH = PODD.VF.createURI(PODD.PODD_BASE, "SSHFileReference");
     
     // ----- custom representation of cardinalities -----
     
     /**
      * http://purl.org/podd/ns/poddBase#hasAlias.
      * 
      * This property is used to specify an "alias" value found inside a DataReference.
      */
     public static final URI PODD_BASE_HAS_ALIAS = PODD.VF.createURI(PODD.PODD_BASE, "hasAlias");
     
     /** http://purl.org/podd/ns/poddBase#hasAllowedValue */
     public static final URI PODD_BASE_HAS_ALLOWED_VALUE = ValueFactoryImpl.getInstance().createURI(PODD.PODD_BASE,
             "hasAllowedValue");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasCardinality. Represents a <b>hasCardinality</b> property.
      */
     public static final URI PODD_BASE_HAS_CARDINALITY = PODD.VF.createURI(PODD.PODD_BASE, "hasCardinality");
     
     /** http://purl.org/podd/ns/poddBase#hasDataReference */
     public static final URI PODD_BASE_HAS_DATA_REFERENCE = PODD.VF.createURI(PODD.PODD_BASE, "hasDataReference");
     
     /** http://purl.org/podd/ns/poddBase#hasPath */
     public static final URI PODD_BASE_HAS_FILE_PATH = PODD.VF.createURI(PODD.PODD_BASE, "hasPath");
     
     // ----- file reference constants -----
     
     /** http://purl.org/podd/ns/poddBase#hasFileName */
     public static final URI PODD_BASE_HAS_FILENAME = PODD.VF.createURI(PODD.PODD_BASE, "hasFileName");
     
     public static final URI PODD_BASE_HAS_PRINCIPAL_INVESTIGATOR = PODD.VF.createURI(PODD.PODD_BASE,
             "hasPrincipalInvestigator");
     
     public static final URI PODD_BASE_HAS_PROJECT_OBSERVER = PODD.VF.createURI(PODD.PODD_BASE, "hasProjectObserver");
     
     public static final URI PODD_BASE_HAS_PROJECT_MEMBER = PODD.VF.createURI(PODD.PODD_BASE, "hasProjectMember");
     
     public static final URI PODD_BASE_HAS_PROJECT_ADMINISTRATOR = PODD.VF.createURI(PODD.PODD_BASE,
             "hasProjectAdministrator");
     
     public static final URI PODD_BASE_HAS_PUBLICATION_STATUS = PODD.VF
             .createURI(PODD.PODD_BASE, "hasPublicationStatus");
     
     /** http://purl.org/podd/ns/poddBase#hasSPARQLGraph */
     public static final URI PODD_BASE_HAS_SPARQL_GRAPH = PODD.VF.createURI(PODD.PODD_BASE, "hasSPARQLGraph");
     
     public static final URI PODD_BASE_HAS_TOP_OBJECT = PODD.VF.createURI(PODD.PODD_BASE, "artifactHasTopObject");
     
     /**
      * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
      * a particular versioned ontology.
      */
     public static final URI PODD_BASE_INFERRED_VERSION = PODD.VF.createURI(PODD.PODD_BASE, "inferredVersion");
     
     /** http://purl.org/podd/ns/poddBase#lastModified */
     public static final URI PODD_BASE_LAST_MODIFIED = PODD.VF.createURI(PODD.PODD_BASE, "lastModified");
     
     public static final URI PODD_BASE_NOT_PUBLISHED = PODD.VF.createURI(PODD.PODD_BASE, "NotPublished");
     
     public static final URI PODD_BASE_PUBLISHED = PODD.VF.createURI(PODD.PODD_BASE, "Published");
     
     /** http://purl.org/podd/ns/poddBase#refersTo */
     public static final URI PODD_BASE_REFERS_TO = PODD.VF.createURI(PODD.PODD_BASE, "refersTo");
     
     public static final URI PODD_BASE_WEIGHT = PODD.VF.createURI(PODD.PODD_BASE, "weight");
     
     /** http://purl.org/podd/ns/poddBase#DataRepository */
     public static final URI PODD_DATA_REPOSITORY = PODD.VF.createURI(PODD.DATA_REPOSITORY, "DataRepository");
     
     /**
      * http://purl.org/podd/ns/poddBase#hasDataRepositoryAlias
      * 
      * This property is ONLY used in the Data Repository management implementations.
      */
     public static final URI PODD_DATA_REPOSITORY_ALIAS = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "hasDataRepositoryAlias");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryHost */
     public static final URI PODD_DATA_REPOSITORY_HOST = PODD.VF
             .createURI(PODD.DATA_REPOSITORY, "hasDataRepositoryHost");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryPath */
     public static final URI PODD_DATA_REPOSITORY_PATH = PODD.VF
             .createURI(PODD.DATA_REPOSITORY, "hasDataRepositoryPath");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryPort */
     public static final URI PODD_DATA_REPOSITORY_PORT = PODD.VF
             .createURI(PODD.DATA_REPOSITORY, "hasDataRepositoryPort");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryProtocol */
     public static final URI PODD_DATA_REPOSITORY_PROTOCOL = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "hasDataRepositoryProtocol");
     
     public static final String PODD_DCTERMS = "http://purl.org/podd/ns/dcTerms#";
     
     /**
      * TODO: Temporary domain for specifying error messages in RDF
      */
     public static final String PODD_ERROR = "http://purl.org/podd/ns/err#";
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryFingerprint */
     public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "hasDataRepositoryFingerprint");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositorySecret */
     public static final URI PODD_FILE_REPOSITORY_SECRET = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "hasDataRepositorySecret");
     
     /** http://purl.org/podd/ns/poddBase#hasDataRepositoryUsername */
     public static final URI PODD_FILE_REPOSITORY_USERNAME = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "hasDataRepositoryUsername");
     
     public static final String PODD_FOAF = "http://purl.org/podd/ns/foaf#";
     
     /** http://purl.org/podd/ns/poddBase#HTTPFileRepository */
     public static final URI PODD_HTTP_FILE_REPOSITORY = PODD.VF.createURI(PODD.DATA_REPOSITORY, "HTTPFileRepository");
     
     public static final String PODD_PLANT = "http://purl.org/podd/ns/poddPlant#";
     
     public static final URI PODD_REPLACED_TEMP_URI_WITH = PODD.VF.createURI(PODD.PODD_BASE, "replacedTempUriWith");
     
     /** http://purl.org/podd/ns/poddUser#roleMappedObject */
     public static final URI PODD_ROLEMAPPEDOBJECT = PODD.VF.createURI(PODD.PODD_USER, "roleMappedObject");
     
     public static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";
     
     /** http://purl.org/podd/ns/poddScience#Project */
     public static final URI PODD_SCIENCE_PROJECT = PODD.VF.createURI(PODD.PODD_SCIENCE, "Project");
     
     /** http://purl.org/podd/ns/poddScience#Investigation */
     public static final URI PODD_SCIENCE_INVESTIGATION = PODD.VF.createURI(PODD.PODD_SCIENCE, "Investigation");
     
     /** http://purl.org/podd/ns/poddScience#Container */
     public static final URI PODD_SCIENCE_CONTAINER = PODD.VF.createURI(PODD.PODD_SCIENCE, "Container");
     
     /** http://purl.org/podd/ns/poddScience#Tray */
     public static final URI PODD_SCIENCE_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE, "Tray");
     
     /** http://purl.org/podd/ns/poddScience#Pot */
     public static final URI PODD_SCIENCE_POT = PODD.VF.createURI(PODD.PODD_SCIENCE, "Pot");
     
     /** http://purl.org/podd/ns/poddScience#Material */
     public static final URI PODD_SCIENCE_MATERIAL = PODD.VF.createURI(PODD.PODD_SCIENCE, "Material");
     
     /** http://purl.org/podd/ns/poddScience#hasMaterial */
     public static final URI PODD_SCIENCE_HAS_MATERIAL = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasMaterial");
     
     /** http://purl.org/podd/ns/poddScience#refersToMaterial */
     public static final URI PODD_SCIENCE_REFERS_TO_MATERIAL = PODD.VF.createURI(PODD.PODD_SCIENCE, "refersToMaterial");
     
     /** http://purl.org/podd/ns/poddScience#Genotype */
     public static final URI PODD_SCIENCE_GENOTYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "Genotype");
     
     /** http://purl.org/podd/ns/poddScience#hasBarcode */
     public static final URI PODD_SCIENCE_HAS_BARCODE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasBarcode");
     
     /** http://purl.org/podd/ns/poddScience#hasPosition */
     public static final URI PODD_SCIENCE_HAS_POSITION = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasPosition");
     
     /** http://purl.org/podd/ns/poddScience#hasReplicate */
     public static final URI PODD_SCIENCE_HAS_REPLICATE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasReplicate");
     
     /** http://purl.org/podd/ns/poddScience#hasGenotype */
     public static final URI PODD_SCIENCE_HAS_GENOTYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasGenotype");
     
     /** http://purl.org/podd/ns/poddScience#refersToGenotype */
     public static final URI PODD_SCIENCE_REFERS_TO_GENOTYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "refersToGenotype");
     
     /** http://purl.org/podd/ns/poddScience#hasGenus */
     public static final URI PODD_SCIENCE_HAS_GENUS = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasGenus");
     
     /** http://purl.org/podd/ns/poddScience#hasControl */
     public static final URI PODD_SCIENCE_HAS_CONTROL = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasControl");
     
     /** http://purl.org/podd/ns/poddScience#HasControl_Yes */
     public static final URI PODD_SCIENCE_HAS_CONTROL_YES = PODD.VF.createURI(PODD.PODD_SCIENCE, "HasControl_Yes");
     
     /** http://purl.org/podd/ns/poddScience#HasControl_No */
     public static final URI PODD_SCIENCE_HAS_CONTROL_NO = PODD.VF.createURI(PODD.PODD_SCIENCE, "HasControl_No");
     
     /** http://purl.org/podd/ns/poddScience#HasControl_Unknown */
     public static final URI PODD_SCIENCE_HAS_CONTROL_UNKNOWN = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "HasControl_Unknown");
     
     /** http://purl.org/podd/ns/poddScience#hasSpecies */
     public static final URI PODD_SCIENCE_HAS_SPECIES = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasSpecies");
     
     /** http://purl.org/podd/ns/poddScience#hasLine */
     public static final URI PODD_SCIENCE_HAS_LINE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasLine");
     
     /** http://purl.org/podd/ns/poddScience#hasLineNumber */
     public static final URI PODD_SCIENCE_HAS_LINE_NUMBER = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasLineNumber");
     
     /** http://purl.org/podd/ns/poddScience#hasContainer */
     public static final URI PODD_SCIENCE_HAS_CONTAINER = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasContainer");
     
     /** http://purl.org/podd/ns/poddScience#hasTray */
     public static final URI PODD_SCIENCE_HAS_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasTray");
     
     /** http://purl.org/podd/ns/poddScience#hasPot */
     public static final URI PODD_SCIENCE_HAS_POT = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasPot");
     
     /** http://purl.org/podd/ns/poddScience#hasContainerType */
     public static final URI PODD_SCIENCE_HAS_CONTAINER_TYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasContainerType");
     
     /** http://purl.org/podd/ns/poddScience#hasTrayType */
     public static final URI PODD_SCIENCE_HAS_TRAY_TYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasTrayType");
     
     /** http://purl.org/podd/ns/poddScience#hasTrayNumber */
     public static final URI PODD_SCIENCE_HAS_TRAY_NUMBER = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasTrayNumber");
     
     /** http://purl.org/podd/ns/poddScience#hasTrayRowNumber */
     public static final URI PODD_SCIENCE_HAS_TRAY_ROW_NUMBER = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasTrayRowNumber");
     
     /** http://purl.org/podd/ns/poddScience#hasPotNumber */
     public static final URI PODD_SCIENCE_HAS_POT_NUMBER = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasPotNumber");
     
     /** http://purl.org/podd/ns/poddScience#hasPotType */
     public static final URI PODD_SCIENCE_HAS_POT_TYPE = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasPotType");
     
     /** http://purl.org/podd/ns/poddScience#hasPotColumnNumberOverall */
     public static final URI PODD_SCIENCE_HAS_POT_COLUMN_NUMBER_OVERALL = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotColumnNumberOverall");
     
     /** http://purl.org/podd/ns/poddScience#hasPotColumnNumberReplicate */
     public static final URI PODD_SCIENCE_HAS_POT_COLUMN_NUMBER_REPLICATE = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotColumnNumberReplicate");
     
     /** http://purl.org/podd/ns/poddScience#hasPotColumnNumberTray */
     public static final URI PODD_SCIENCE_HAS_POT_COLUMN_NUMBER_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotColumnNumberTray");
     
     /** http://purl.org/podd/ns/poddScience#hasPotColumnLetterTray */
     public static final URI PODD_SCIENCE_HAS_POT_COLUMN_LETTER_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotColumnLetterTray");
     
     /** http://purl.org/podd/ns/poddScience#hasPotPositionTray */
     public static final URI PODD_SCIENCE_HAS_POT_POSITION_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotPositionTray");
     
     /** http://purl.org/podd/ns/poddScience#hasPotNumberTray */
     public static final URI PODD_SCIENCE_HAS_POT_NUMBER_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE, "hasPotNumberTray");
     
     /** http://purl.org/podd/ns/poddScience#hasPotNumberReplicate */
     public static final URI PODD_SCIENCE_HAS_POT_NUMBER_REPLICATE = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotNumberReplicate");
     
     /** http://purl.org/podd/ns/poddScience#hasPotRowNumberReplicate */
     public static final URI PODD_SCIENCE_HAS_POT_ROW_NUMBER_REPLICATE = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotRowNumberReplicate");
     
     /** http://purl.org/podd/ns/poddScience#hasPotRowNumberTray */
     public static final URI PODD_SCIENCE_HAS_POT_ROW_NUMBER_TRAY = PODD.VF.createURI(PODD.PODD_SCIENCE,
             "hasPotRowNumberTray");
     
     /** http://purl.org/podd/ns/poddBase#SPARQLDataRepository */
     public static final URI PODD_SPARQL_DATA_REPOSITORY = PODD.VF.createURI(PODD.DATA_REPOSITORY,
             "SPARQLDataRepository");
     
     /** http://purl.org/podd/ns/poddBase#SSHFileRepository */
     public static final URI PODD_SSH_FILE_REPOSITORY = PODD.VF.createURI(PODD.DATA_REPOSITORY, "SSHFileRepository");
     
     public static final String PODD_USER = "http://purl.org/podd/ns/poddUser#";
     
     /** http://purl.org/podd/ns/poddUser#address */
     public static final URI PODD_USER_ADDRESS = PODD.VF.createURI(PODD.PODD_USER, "address");
     
     /** http://purl.org/podd/ns/poddUser#homepage */
     public static final URI PODD_USER_HOMEPAGE = PODD.VF.createURI(PODD.PODD_USER, "homepage");
     
     /** http://purl.org/podd/ns/poddUser#oldSecret */
     public static final URI PODD_USER_OLDSECRET = PODD.VF.createURI(PODD.PODD_USER, "oldSecret");
     
     /** http://purl.org/podd/ns/poddUser#orcid */
     public static final URI PODD_USER_ORCID = PODD.VF.createURI(PODD.PODD_USER, "orcid");
     
     /** http://purl.org/podd/ns/poddUser#organization */
     public static final URI PODD_USER_ORGANIZATION = PODD.VF.createURI(PODD.PODD_USER, "organization");
     
     /** http://purl.org/podd/ns/poddUser#phone */
     public static final URI PODD_USER_PHONE = PODD.VF.createURI(PODD.PODD_USER, "phone");
     
     /** http://purl.org/podd/ns/poddUser#position */
     public static final URI PODD_USER_POSITION = PODD.VF.createURI(PODD.PODD_USER, "position");
     
     /** http://purl.org/podd/ns/poddUser#status */
     public static final URI PODD_USER_STATUS = PODD.VF.createURI(PODD.PODD_USER, "status");
     
     /** http://purl.org/podd/ns/poddUser#title */
     public static final URI PODD_USER_TITLE = PODD.VF.createURI(PODD.PODD_USER, "title");
     
     /**
      * @Deprecated Unused. Remove if not needed.
      */
     public static final URI SCOPE_ARTIFACT = PODD.VF.createURI("http://purl.org/podd/poddBase#PoddArtifact");
     
     /**
      * @Deprecated Unused. Remove if not needed.
      */
     public static final URI SCOPE_REPOSITORY = PODD.VF.createURI("http://purl.org/podd/poddBase#PoddRepository");
     
     /**
      * [http://purl.org/podd/ns/artifact/artifact89]
      * 
      * A dummy Artifact URI for test purposes.
      */
     public static final URI TEST_ARTIFACT = PODD.VF.createURI("http://purl.org/podd/ns/artifact/artifact89");
     
     public static final URI PODD_SCHEMA_CLASSPATH = PODD.VF.createURI("http://purl.org/podd/ns/schema#classpath");
     
     public static final URI PODD_REPOSITORY_MANAGER = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#RepositoryManager");
     
     public static final URI PODD_REPOSITORY = PODD.VF.createURI("http://purl.org/podd/ns/repository#Repository");
     
     public static final URI PODD_REPOSITORY_MANAGER_CONTAINS_REPOSITORY = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#repositoryManagerContainsRepository");
     
     public static final URI PODD_REPOSITORY_ID_IN_MANAGER = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#repositoryIdInManager");
     
     public static final URI PODD_REPOSITORY_MANAGER_TYPE = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#repositoryManagerType");
     
     public static final URI PODD_REPOSITORY_MANAGER_TYPE_LOCAL = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#RepositoryManagerTypeLocal");
     
     public static final URI PODD_REPOSITORY_MANAGER_LOCAL_DIRECTORY = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#repositoryManagerLocalDirectory");
     
     public static final URI PODD_REPOSITORY_MANAGER_REMOTE_SERVER_URL = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#repositoryManagerRemoteServerUrl");
     
     public static final URI PODD_REPOSITORY_MANAGER_TYPE_REMOTE = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#RepositoryManagerTypeRemote");
     
     public static final URI PODD_REPOSITORY_CONTAINS_SCHEMA_IRI = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#containsSchemaIRI");
     public static final URI PODD_REPOSITORY_CONTAINS_SCHEMA_VERSION = PODD.VF
             .createURI("http://purl.org/podd/ns/repository#containsSchemaVersion");
     
 }
