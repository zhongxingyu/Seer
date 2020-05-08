 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetab.checks.idf;
 
 import org.hamcrest.Matchers;
 import uk.ac.ebi.fg.annotare2.magetab.checker.CheckApplicationType;
 import uk.ac.ebi.fg.annotare2.magetab.checker.MageTabCheck;
 import uk.ac.ebi.fg.annotare2.magetab.model.Cell;
 import uk.ac.ebi.fg.annotare2.magetab.model.FileLocation;
 import uk.ac.ebi.fg.annotare2.magetab.model.idf.*;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static java.lang.Boolean.FALSE;
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static uk.ac.ebi.fg.annotare2.magetab.checker.CheckApplicationType.MICRO_ARRAY_ONLY;
 import static uk.ac.ebi.fg.annotare2.magetab.checker.CheckPositionKeeper.setCheckPosition;
 import static uk.ac.ebi.fg.annotare2.magetab.checker.CheckModality.WARNING;
 import static uk.ac.ebi.fg.annotare2.magetab.checks.matchers.IsDateString.isDateString;
 import static uk.ac.ebi.fg.annotare2.magetab.checks.matchers.IsValidFileLocation.isValidFileLocation;
 import static uk.ac.ebi.fg.annotare2.magetab.checks.matchers.RegExpMatcher.matches;
 import static uk.ac.ebi.fg.annotare2.magetab.checks.idf.IdfConstants.DATE_FORMAT;
 import static uk.ac.ebi.fg.annotare2.magetab.checks.idf.IdfConstants.SUBMITTER_ROLE;
 
 /**
  * @author Olga Melnichuk
  */
 public class IdfSimpleChecks {
 
     @MageTabCheck("Experiment title must be specified")
     public void investigationTitleRequired(final Info info) {
         assertNotEmptyString(info.getTitle());
     }
 
     @MageTabCheck("Experiment description must be specified")
     public void experimentDescriptionRequired(Info info) {
         assertNotEmptyString(info.getExperimentDescription());
     }
 
    @MageTabCheck(value = "Experiment date should be specified", modality = WARNING)
     public void dateOfExperimentShouldBeSpecified(Info info) {
         assertNotEmptyString(info.getDateOfExperiment());
     }
 
    @MageTabCheck("Experiment date must be in 'YYYY-MM-DD' format")
     public void dateOfExperimentFormat(Info info) {
         Cell<String> cell = info.getDateOfExperiment();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue(), isDateString(DATE_FORMAT));
     }
 
     @MageTabCheck("Experiment public release date must be specified")
     public void publicReleaseDateRequired(Info info) {
         assertNotEmptyString(info.getPublicReleaseDate());
     }
 
     @MageTabCheck("Experiment public release date must be in 'YYYY-MM-DD' format")
     public void publicReleaseDateFormat(Info info) {
         Cell<String> cell = info.getPublicReleaseDate();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue(), isDateString(DATE_FORMAT));
     }
 
     @MageTabCheck("Reference to the SDRF file must be specified")
     public void sdrfFileMustBeSpecified(Info info) {
         Cell<FileLocation> cell = info.getSdrfFile();
         setPosition(cell);
         assertThat(cell.getValue(), notNullValue());
         assertThat(cell.getValue().isEmpty(), is(FALSE));
     }
 
     @MageTabCheck("Reference to the SDRF file must be valid file location")
     public void sdrfFileMustBeValidLocation(Info info) {
         Cell<FileLocation> cell = info.getSdrfFile();
         FileLocation loc = cell.getValue();
         if (loc == null || loc.isEmpty()) {
             return;
         }
         setPosition(cell);
         assertThat(loc, isValidFileLocation());
     }
 
     @MageTabCheck("A contact must have last name specified")
     public void contactMustHaveLastName(Person person) {
         assertNotEmptyString(person.getLastName());
     }
 
     @MageTabCheck(value = " A contact should have first name specified", modality = WARNING)
     public void contactShouldHaveFirstName(Person person) {
         assertNotEmptyString(person.getFirstName());
     }
 
     @MageTabCheck(value = "A contact should have an affiliation specified", modality = WARNING)
     public void contactShouldHaveAffiliation(Person person) {
         assertNotEmptyString(person.getAffiliation());
     }
 
     @MageTabCheck(value = "A contact role(s) should have a term source specified", modality = WARNING)
     public void contactRolesShouldHaveTermSource(Person person) {
         TermList roles = person.getRoles();
         if (roles == null || roles.isEmpty()) {
             return;
         }
         assertNotNull(roles.getSource());
     }
 
     @MageTabCheck(
             value = "A contact with '" + SUBMITTER_ROLE + "' role must have affiliation specified",
             application = CheckApplicationType.HTS_ONLY)
     public void submitterMustHaveAffiliation(Person person) {
         TermList roles = person.getRoles();
         if (roles == null || roles.isEmpty()) {
             return;
         }
         if (roles.getNames().getValue().contains(SUBMITTER_ROLE)) {
             assertNotEmptyString(person.getAffiliation());
         }
     }
 
     @MageTabCheck(value = "An experimental design should be defined by a term", modality = WARNING, application = MICRO_ARRAY_ONLY)
     public void experimentDesignShouldHaveName(ExperimentalDesign exd) {
         assertNotEmptyString(exd.getName());
     }
 
     @MageTabCheck(value = "An experimental design term should have a term source", modality = WARNING, application = MICRO_ARRAY_ONLY)
     public void experimentalDesignShouldHaveTermSource(ExperimentalDesign exd) {
         assertNotNull(exd.getSource());
     }
 
     @MageTabCheck("An experimental factor must have name specified")
     public void experimentalFactorMustHaveName(ExperimentalFactor exf) {
         assertNotEmptyString(exf.getName());
     }
 
     @MageTabCheck(value = "An experimental factor should have a type specified")
     public void experimentalFactorShouldHaveType(ExperimentalFactor exf) {
         assertNotEmptyString(exf.getType().getName());
     }
 
     @MageTabCheck(value = "An experimental factor type should have term source specified", modality = WARNING)
     public void experimentalFactorTypeShouldHaveSource(ExperimentalFactor exf) {
         assertNotNull(exf.getType().getSource());
     }
 
     @MageTabCheck(value = "A quality control type should be defined by a term", modality = WARNING)
     public void qualityControlTypeShouldHaveName(QualityControlType qt) {
         assertNotEmptyString(qt.getName());
     }
 
     @MageTabCheck(value = "A quality control type should have term source specified", modality = WARNING)
     public void qualityControlTypeShouldHaveSource(QualityControlType qt) {
         assertNotNull(qt.getSource());
     }
 
     @MageTabCheck(value = "A replicate type should be defined by a term", modality = WARNING)
     public void replicateTypeShouldHaveName(ReplicateType rt) {
         assertNotEmptyString(rt.getName());
     }
 
     @MageTabCheck(value = "A replicate type should have term source specified", modality = WARNING)
     public void replicateTypeShouldHaveSource(ReplicateType rt) {
         assertNotNull(rt.getSource());
     }
 
     @MageTabCheck(value = "A normalization type should be defined by a term", modality = WARNING)
     public void normalizationTypeShouldHaveName(NormalizationType nt) {
         assertNotEmptyString(nt.getName());
     }
 
     @MageTabCheck(value = "A normalization type should have term source specified", modality = WARNING)
     public void normalizationTypeShouldHaveSource(NormalizationType nt) {
         assertNotNull(nt.getSource());
     }
 
     @MageTabCheck(value = "A publication should have at least one of PubMed ID or Publication DOI specified", modality = WARNING)
     public void publicationShouldHavePubMedIDOrDOI(Publication pub) {
         setPosition(pub.getPubMedId());
         assertThat(asList(pub.getPubMedId().getValue(), pub.getPublicationDOI().getValue()),
                 Matchers.<String>hasItem(not(isEmptyOrNullString())));
     }
 
     @MageTabCheck("PubMed Id must be numeric")
     public void pubMedIdMustBeNumeric(Publication pub) {
         Cell<String> cell = pub.getPubMedId();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(pub.getPubMedId().getValue(), matches("[0-9]+"));
     }
 
     @MageTabCheck(value = "A publication authors should be specified", modality = WARNING)
     public void publicationShouldHaveAuthorsSpecified(Publication pub) {
         assertNotEmptyString(pub.getAuthorList());
     }
 
     @MageTabCheck(value = "A publication title should be specified", modality = WARNING)
     public void publicationShouldHaveTitleSpecified(Publication pub) {
         assertNotEmptyString(pub.getTitle());
     }
 
     @MageTabCheck(value = "A publication status should be specified", modality = WARNING)
     public void publicationShouldHaveStatusSpecified(Publication pub) {
         assertNotEmptyString(pub.getStatus().getName());
     }
 
     @MageTabCheck(value = "A publication status should have term source specified", modality = WARNING)
     public void publicationStatusShouldHaveTermSourceSpecified(Publication pub) {
         assertNotNull(pub.getStatus().getSource());
     }
 
     @MageTabCheck("Name of a protocol must be specified")
     public void protocolMustHaveName(Protocol prot) {
         assertNotEmptyString(prot.getName());
     }
 
     @MageTabCheck("A protocol type must be specified")
     public void protocolMustHaveType(Protocol prot) {
         assertNotEmptyString(prot.getType().getName());
     }
 
     @MageTabCheck(value = "A protocol type should have term source specified", modality = WARNING)
     public void protocolTypeShouldHaveSource(Protocol prot) {
         assertNotNull(prot.getType().getSource());
     }
 
     @MageTabCheck(value = "Description of a protocol should be specified", modality = WARNING)
     public void protocolShouldHaveDescription(Protocol prot) {
         assertNotEmptyString(prot.getDescription());
     }
 
     @MageTabCheck(value = "Description of a protocol should be over 50 characters long", modality = WARNING)
     public void protocolDescriptionShouldBeOver50CharsLong(Protocol prot) {
         Cell<String> cell = prot.getDescription();
         if (isNullOrEmpty(cell.getValue())) {
             return;
         }
         setPosition(cell);
         assertThat(cell.getValue().length(), greaterThan(50));
     }
 
     @MageTabCheck(value = "A protocol should have parameters", modality = WARNING)
     public void protocolShouldHaveParameters(Protocol prot) {
         setPosition(prot.getParameters());
         assertThat(prot.getParameters().getValue(), is(not(empty())));
     }
 
     @MageTabCheck("Name of a term source must be specified")
     public void termSourceMustHaveName(TermSource ts) {
         assertNotEmptyString(ts.getName());
     }
 
     @MageTabCheck(value = "URL/File of a term source should be specified", modality = WARNING)
     public void termSourceShouldHaveFile(TermSource ts) {
         assertNotEmptyString(ts.getFile());
     }
 
     @MageTabCheck(value = "Version of a term source should be specified", modality = WARNING)
     public void termSourceShouldHaveVersion(TermSource ts) {
         assertNotEmptyString(ts.getVersion());
     }
 
     private static <T> void assertNotNull(Cell<T> cell) {
         setPosition(cell);
         assertThat(cell.getValue(), notNullValue());
     }
 
     private static void assertNotEmptyString(Cell<String> cell) {
         setPosition(cell);
         assertThat(cell.getValue(), not(isEmptyOrNullString()));
     }
 
     private static <T> void setPosition(Cell<T> cell) {
         setCheckPosition(cell.getFileName(), cell.getLine(), cell.getColumn());
     }
 }
