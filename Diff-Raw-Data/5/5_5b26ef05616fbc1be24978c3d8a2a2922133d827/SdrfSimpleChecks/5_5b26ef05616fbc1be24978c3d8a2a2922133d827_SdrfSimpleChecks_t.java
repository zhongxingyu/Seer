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
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.checks.sdrf;
 
 import com.google.common.base.Predicate;
 import com.google.inject.Inject;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.MageTabCheck;
 import uk.ac.ebi.fg.annotare2.magetabcheck.efo.MageTabCheckEfo;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.FileLocation;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.Protocol;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.TermSource;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.*;
 
 import javax.annotation.Nullable;
 import java.util.*;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.newHashSet;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.HTS_ONLY;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckApplicationType.MICRO_ARRAY_ONLY;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckModality.WARNING;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckPositionSetter.setCheckPosition;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf.IdfConstants.DATE_FORMAT;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.matchers.IsDateString.isDateString;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checks.matchers.IsValidFileLocation.isValidFileLocation;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.extension.KnownTermSource.NCBI_TAXONOMY;
 
 /**
  * @author Olga Melnichuk
  */
 public class SdrfSimpleChecks {
 
     MageTabCheckEfo efo;
 
     @Inject
     public SdrfSimpleChecks(MageTabCheckEfo efo) {
         this.efo = efo;
     }
 
     @MageTabCheck(
             ref = "SR01",
             value = "A source node must have name specified")
     public void sourceNodeMustHaveName(SdrfSourceNode sourceNode) {
         assertNotEmptyName(sourceNode);
     }
 
     @MageTabCheck(
             ref = "SR02",
             value = "A source node should have 'Material Type' attribute specified",
             modality = WARNING)
     public void sourceNodeShouldHaveMaterialTypeAttribute(SdrfSourceNode sourceNode) {
         setPosition(sourceNode);
         assertNotNull(sourceNode.getMaterialType());
     }
 
     @MageTabCheck(
             ref = "SR03",
             value = "A source node should have 'Provider' attribute specified",
             modality = WARNING)
     public void sourceNodeShouldHaveProviderAttribute(SdrfSourceNode sourceNode) {
         setPosition(sourceNode);
         assertNotNull(sourceNode.getProvider());
         assertNotEmptyString(sourceNode.getProvider().getValue());
     }
 
     @MageTabCheck(
             ref = "SR04",
             value = "A source node must have an 'Organism' characteristic specified")
     public void sourceNodeMustHaveOrganismCharacteristic(SdrfSourceNode sourceNode) {
         setPosition(sourceNode);
         Collection<SdrfCharacteristicAttribute> characteristics = sourceNode.getCharacteristics();
         assertNotNull(characteristics);
         assertThat(characteristics.isEmpty(), is(Boolean.FALSE));
         assertNotNull(getOrganism(characteristics));
     }
 
     private SdrfCharacteristicAttribute getOrganism(Collection<SdrfCharacteristicAttribute> characteristics) {
         for (SdrfCharacteristicAttribute attr : characteristics) {
             if ("Organism".equalsIgnoreCase(attr.getType())) {
                 TermSource ts = attr.getTermSource();
                 if (ts == null || NCBI_TAXONOMY.matches(ts.getFile().getValue())) {
                     return attr;
                 }
             }
         }
         return null;
     }
 
     @MageTabCheck(
             ref = "SR05",
             value = "A source node should have more than 2 characteristic attributes",
             modality = WARNING)
     public void sourceNodeShouldHaveMoreThan2Characteristics(SdrfSourceNode sourceNode) {
         setPosition(sourceNode);
         Collection<SdrfCharacteristicAttribute> characteristics = sourceNode.getCharacteristics();
         assertNotNull(characteristics);
         assertThat(characteristics.size(), greaterThanOrEqualTo(2));
     }
 
     @MageTabCheck(
             ref = "SR07",
             value = "A source node should be described by a protocol",
             modality = WARNING)
     public void sourceNodeShouldBeDescribedByProtocol(SdrfSourceNode sourceNode) {
         assertNodeIsDescribedByProtocol(sourceNode);
     }
 
     @MageTabCheck(
             ref = "SM01",
             value = "A sample node must have name specified")
     public void sampleNodeMustHaveName(SdrfSampleNode sampleNode) {
         assertNotEmptyName(sampleNode);
     }
 
     @MageTabCheck(
             ref = "SM02",
             value = "A sample node should have 'Material Type' attribute specified",
             modality = WARNING)
     public void sampleNodeShouldHaveMaterialTypeAttribute(SdrfSampleNode sampleNode) {
         setPosition(sampleNode);
         assertNotNull(sampleNode.getMaterialType());
     }
 
     @MageTabCheck(
             ref = "SM03",
             value = "A sample node should be described by a protocol",
             modality = WARNING)
     public void sampleNodeShouldBeDescribedByProtocol(SdrfSampleNode sampleNode) {
         assertNodeIsDescribedByProtocol(sampleNode);
     }
 
     @MageTabCheck(
             ref = "EX01",
             value = "An extract node must have name specified")
     public void extractNodeMustHaveName(SdrfExtractNode extractNode) {
         assertNotEmptyName(extractNode);
     }
 
     @MageTabCheck(
             ref = "EX02",
             value = "An extract node should have 'Material Type' attribute specified",
             modality = WARNING)
     public void extractNodeShouldHaveMaterialTypeAttribute(SdrfExtractNode extractNode) {
         setPosition(extractNode);
         assertNotNull(extractNode.getMaterialType());
     }
 
     @MageTabCheck(
             ref = "EX03",
             value = "An extract node should be described by a protocol",
             modality = WARNING)
     public void extractNodeShouldBeDescribedByProtocol(SdrfExtractNode extractNode) {
         assertNodeIsDescribedByProtocol(extractNode);
     }
 
     @MageTabCheck(
             ref = "EX04",
             value = "An extract node must be described by a 'library construction' protocol",
             application = HTS_ONLY)
     public void extractNodeMustBeDescribedByLibraryConstructionProtocol(SdrfExtractNode extractNode) {
         setPosition(extractNode);
 
         SdrfProtocolNode found = null;
         for (SdrfProtocolNode protocolNode : getParentProtocolNodes(extractNode)) {
             Protocol protocol = protocolNode.getProtocol();
             if (protocol == null) {
                 continue;
             }
             if (efo.isLibraryConstructionProtocol(protocol.getType())) {
                 found = protocolNode;
                 break;
             }
         }
         assertNotNull(found);
     }
 
     @MageTabCheck(
             ref = "LE02",
             value = "A labeled extract node must have name specified",
             application = MICRO_ARRAY_ONLY)
     public void labeledExtractNodeMustHaveName(SdrfLabeledExtractNode labeledExtractNode) {
         assertNotEmptyName(labeledExtractNode);
     }
 
     @MageTabCheck(
             ref = "LE03",
             value = "A labeled extract node should have 'Material Type' attribute specified",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void labeledExtractNodeShouldHaveMaterialTypeAttribute(SdrfLabeledExtractNode labeledExtractNode) {
         setPosition(labeledExtractNode);
         assertNotNull(labeledExtractNode.getMaterialType());
     }
 
     @MageTabCheck(
             ref = "LE04",
             value = "A labeled extract node must have 'Label' attribute specified",
             application = MICRO_ARRAY_ONLY)
     public void labeledExtractNodeMustHaveLabelAttribute(SdrfLabeledExtractNode labeledExtractNode) {
         setPosition(labeledExtractNode);
         assertNotNull(labeledExtractNode.getLabel());
     }
 
     @MageTabCheck(
             ref = "LE05",
             value = "A labeled extract node should be described by a protocol",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void labeledExtractNodeShouldBeDescribedByProtocol(SdrfLabeledExtractNode labeledExtractNode) {
         assertNodeIsDescribedByProtocol(labeledExtractNode);
     }
 
     @MageTabCheck(
             ref = "L01",
             value = "A label attribute should have name specified",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void labelAttributeShouldHaveName(SdrfLabelAttribute labelAttribute) {
         assertNotEmptyName(labelAttribute);
     }
 
     @MageTabCheck(
             ref = "L02",
             value = "A label attribute should have term source specified",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void labelAttributeShouldHaveTermSource(SdrfLabelAttribute la) {
         setPosition(la);
         assertNotEmptyString(la.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "L03",
             value = "Term source of a label attribute must be defined in IDF",
             application = MICRO_ARRAY_ONLY)
     public void termSourceOfLabelAttributeMustBeValid(SdrfLabelAttribute la) {
         setPosition(la);
         assertTermSourceIsValid(la);
     }
 
     @MageTabCheck(
             ref = "MT01",
             value = "A material type attribute should have name specified",
             modality = WARNING)
     public void materialTypeAttributeShouldHaveName(SdrfMaterialTypeAttribute materialTypeAttribute) {
         assertNotEmptyName(materialTypeAttribute);
     }
 
     @MageTabCheck(
             ref = "MT02",
             value = "A material type attribute should have term source specified",
             modality = WARNING)
     public void materialTypeAttributeShouldHaveTermSource(SdrfMaterialTypeAttribute mta) {
         setPosition(mta);
         assertNotEmptyString(mta.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "MT03",
             value = "Term source of a material type attribute must be defined in IDF")
     public void termSourceOfMaterialTypeAttributeMustBeValid(SdrfMaterialTypeAttribute mta) {
         setPosition(mta);
         assertTermSourceIsValid(mta);
     }
 
     @MageTabCheck(
             ref = "PN01",
             value = "A protocol node must have name specified")
     public void protocolNodeMustHaveName(SdrfProtocolNode protocolNode) {
         assertNotEmptyName(protocolNode);
     }
 
     @MageTabCheck(
             ref = "PN04",
             value = "A protocol node should have term source specified",
             modality = WARNING)
     public void protocolNodeShouldHaveTermSource(SdrfProtocolNode protocolNode) {
         setPosition(protocolNode);
         assertNotEmptyString(protocolNode.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "PN05",
             value = "Term source value of a protocol node must be defined in IDF")
     public void termSourceOfProtocolMustBeValid(SdrfProtocolNode protocolNode) {
         setPosition(protocolNode);
         assertTermSourceIsValid(protocolNode);
     }
 
     @MageTabCheck(
             ref = "PN03",
             value = "A protocol's date must be in 'YYYY-MM-DD' format")
     public void protocolNodeDateFormat(SdrfProtocolNode protocolNode) {
         String date = protocolNode.getDate();
         if (isNullOrEmpty(date)) {
             return;
         }
         setPosition(protocolNode);
         assertThat(date, isDateString(DATE_FORMAT));
     }
 
     @MageTabCheck(
             ref = "PN06",
             value = "A nucleic acid sequencing protocol must have a 'performer' attribute specified",
             application = HTS_ONLY)
    public void sequencingProtocolNodeMustHavePerformerAttribute(SdrfProtocolNode protocolNode) {
         Protocol protocol = protocolNode.getProtocol();
         if (protocol != null && efo.isSequencingProtocol(protocol.getType())) {
             assertProtocolHasPerformerAttribute(protocolNode);
         }
     }
 
     @MageTabCheck(
             ref = "PN07",
             value = "A protocol should have a 'performer' attribute specified",
             modality = WARNING,
             application = MICRO_ARRAY_ONLY)
     public void protocolNodeShouldHavePerformerAttribute(SdrfProtocolNode protocolNode) {
         assertProtocolHasPerformerAttribute(protocolNode);
     }
 
     private void assertProtocolHasPerformerAttribute(SdrfProtocolNode protocolNode) {
         setPosition(protocolNode);
         SdrfPerformerAttribute attr = protocolNode.getPerformer();
         assertNotNull(attr);
 
         setPosition(attr);
         assertNotEmptyString(attr.getValue());
     }
 
     @MageTabCheck(
             ref = "AN01",
             value = "An assay node must have name specified")
     public void assayNodeMustHaveName(SdrfAssayNode assayNode) {
         assertNotEmptyName(assayNode);
     }
 
     @MageTabCheck(
             ref = "AN02",
             value = "An assay node must have 'Technology Type' attribute specified")
     public void assayNodeMustHaveTechnologyTypeAttribute(SdrfAssayNode assayNode) {
         setPosition(assayNode);
         assertNotNull(assayNode.getTechnologyType());
     }
 
 
     @MageTabCheck(
             ref = "AN04",
             value = "'Technology Type' attribute must be equal to 'array assay' in micro-array submissions",
             application = MICRO_ARRAY_ONLY)
     public void assayNodeTechnologyTypeIsArrayAssay(SdrfAssayNode assayNode) {
         setPosition(assayNode);
         assertNotNull(assayNode.getTechnologyType());
         assertThat(assayNode.getTechnologyType().getValue().trim(), equalToIgnoringCase("array assay"));
     }
 
     @MageTabCheck(
             ref = "AN03",
             value = "An assay node must be described by a 'sequencing' protocol",
             application = HTS_ONLY)
     public void assayNodeMustBeDescribedBySequencingProtocol(SdrfAssayNode assayNode) {
         setPosition(assayNode);
 
         SdrfProtocolNode found = null;
         for (SdrfProtocolNode protocolNode : getParentProtocolNodes(assayNode)) {
             Protocol protocol = protocolNode.getProtocol();
             if (protocol == null) {
                 continue;
             }
             if (efo.isSequencingProtocol(protocol.getType())) {
                 found = protocolNode;
                 break;
             }
         }
         assertNotNull(found);
     }
 
     @MageTabCheck(
             ref = "AN05",
             value = "If 'Technology Type' value = 'array assay' then incoming nodes must be 'Labeled Extract' nodes only",
             application = MICRO_ARRAY_ONLY)
     public void assayNodeMustBeDerrivedFromLabeledExtracts(SdrfAssayNode assayNode) {
         setPosition(assayNode);
         Collection<SdrfGraphNode> parents = getParentNodes(assayNode);
         Collection<SdrfGraphNode> filtered = filter(parents, new Predicate<SdrfGraphNode>() {
             @Override
             public boolean apply(@Nullable SdrfGraphNode input) {
                 return SdrfLabeledExtractNode.class.isAssignableFrom(input.getClass());
             }
         });
         assertThat(parents.size(), equalTo(filtered.size()));
     }
 
     @MageTabCheck(
             ref = "AN06",
             value = "If 'Technology Type' value = 'array assay' then incoming 'Labeled Extract nodes must have distinct labels",
             application = MICRO_ARRAY_ONLY)
     public void assayNodeMustHaveDistinctLabeledExtracts(SdrfAssayNode assayNode) {
         setPosition(assayNode);
         Collection<SdrfGraphNode> parentNodes = filter(getParentNodes(assayNode), new Predicate<SdrfGraphNode>() {
             @Override
             public boolean apply(@Nullable SdrfGraphNode input) {
                 return SdrfLabeledExtractNode.class.isAssignableFrom(input.getClass());
             }
         });
         Set<String> labels = newHashSet();
         for(SdrfGraphNode node : parentNodes) {
             String label = ((SdrfLabeledExtractNode)node).getLabel().getValue();
             assertThat(labels.add(label), is(true));
         }
     }
 
     @MageTabCheck(
             ref = "TT01",
             value = "Technology type attribute must have name specified")
     public void technologyTypeMustHaveName(SdrfTechnologyTypeAttribute technologyTypeAttribute) {
         assertNotEmptyName(technologyTypeAttribute);
     }
 
     @MageTabCheck(
             ref = "TT02",
             value = "Technology type attribute should have term source specified",
             modality = WARNING)
     public void technologyTypeShouldHaveTermSource(SdrfTechnologyTypeAttribute technologyTypeAttribute) {
         setPosition(technologyTypeAttribute);
         assertNotEmptyString(technologyTypeAttribute.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "TT03",
             value = "Term source of a technology type attribute must be defined in IDF")
     public void termSourceOfTechnologyTypeMustBeValied(SdrfTechnologyTypeAttribute technologyTypeAttribute) {
         setPosition(technologyTypeAttribute);
         assertTermSourceIsValid(technologyTypeAttribute);
     }
 
     @MageTabCheck(
             ref = "PV01",
             value = "A parameter value attribute (of a protocol) should have name specified",
             modality = WARNING)
     public void parameterValueAttributeShouldHaveName(SdrfParameterValueAttribute parameterValueAttribute) {
         setPosition(parameterValueAttribute);
         assertNotEmptyString(parameterValueAttribute.getType());
     }
 
     @MageTabCheck(
             ref = "PV02",
             value = "A parameter value attribute (of a protocol) should have unit specified",
             modality = WARNING)
     public void parameterValueAttributeShouldHaveUnit(SdrfParameterValueAttribute parameterValueAttribute) {
         setPosition(parameterValueAttribute);
         assertNotNull(parameterValueAttribute.getUnit());
     }
 
     @MageTabCheck(
             ref = "UA01",
             value = "A unit attribute should have name specified",
             modality = WARNING)
     public void unitAttributeShouldHaveName(SdrfUnitAttribute unitAttribute) {
         setPosition(unitAttribute);
         assertNotEmptyString(unitAttribute.getType());
     }
 
     @MageTabCheck(
             ref = "UA02",
             value = "A unit attribute should have term source specified",
             modality = WARNING)
     public void unitAttributeShouldHaveTermSource(SdrfUnitAttribute unitAttribute) {
         setPosition(unitAttribute);
         assertNotEmptyString(unitAttribute.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "UA03",
             value = "Term source of a unit attribute must be declared in IDF")
     public void termSourceOfUnitAttributeMustBeValid(SdrfUnitAttribute unitAttribute) {
         setPosition(unitAttribute);
         assertTermSourceIsValid(unitAttribute);
     }
 
     @MageTabCheck(
             ref = "CA01",
             value = "A characteristic attribute should have name specified",
             modality = WARNING)
     public void characteristicAttributeShouldHaveName(SdrfCharacteristicAttribute attribute) {
         setPosition(attribute);
         assertNotEmptyString(attribute.getType());
     }
 
     @MageTabCheck(
             ref = "CA02",
             value = "A characteristic attribute should have term source specified",
             modality = WARNING)
     public void characteristicAttributeShouldHaveTermSource(SdrfCharacteristicAttribute attribute) {
         setPosition(attribute);
         assertNotEmptyString(attribute.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "CA03",
             value = "Term source of a characteristic attribute must be declared in IDF")
     public void termSourceOfCharacteristicAttributeMustBeValid(SdrfCharacteristicAttribute attribute) {
         setPosition(attribute);
         assertTermSourceIsValid(attribute);
     }
 
     @MageTabCheck(
             ref = "FV01",
             value = "A factor value attribute should have name specified",
             modality = WARNING)
     public void factorValueAttributeShouldHaveName(SdrfFactorValueAttribute fvAttribute) {
         setPosition(fvAttribute);
         assertNotEmptyString(fvAttribute.getType());
     }
 
     @MageTabCheck(
             ref = "FV02",
             value = "A factor value attribute should have term source specified",
             modality = WARNING)
     public void factorValueAttributeShouldHaveTermSource(SdrfFactorValueAttribute fvAttribute) {
         setPosition(fvAttribute);
         assertNotEmptyString(fvAttribute.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "FV03",
             value = "Term source of a factor value attribute must be declared in IDF")
     public void termSourceOfFactorValueAttributeMustBeValid(SdrfFactorValueAttribute fvAttribute) {
         setPosition(fvAttribute);
         assertTermSourceIsValid(fvAttribute);
     }
 
     @MageTabCheck(
             ref = "AD01",
             value = "An array design attribute must have name specified")
     public void arrayDesignAttributeMustHaveName(SdrfArrayDesignAttribute adAttribute) {
         assertNotEmptyName(adAttribute);
     }
 
     @MageTabCheck(
             ref = "AD02",
             value = "An array design should have term source specified",
             modality = WARNING)
     public void arrayDesignAttributeShouldHaveTermSource(SdrfArrayDesignAttribute adAttribute) {
         setPosition(adAttribute);
         assertNotEmptyString(adAttribute.getTermSourceRef());
     }
 
     @MageTabCheck(
             ref = "AD03",
             value = "Term source of an array design attribute must be declared in IDF")
     public void termSourceOfArrayDesignAttributeMustBeValid(SdrfArrayDesignAttribute adAttribute) {
         setPosition(adAttribute);
         assertTermSourceIsValid(adAttribute);
     }
 
     @MageTabCheck(
             ref = "NN01",
             value = "A normalization node should have a name",
             modality = WARNING)
     public void normalizationNodeShouldHaveName(SdrfNormalizationNode normalizationNode) {
         assertNotEmptyName(normalizationNode);
     }
 
     @MageTabCheck(
             ref = "SC01",
             value = "A scan node should have name specified",
             modality = WARNING)
     public void scanNodeShouldHaveName(SdrfScanNode scanNode) {
         assertNotEmptyName(scanNode);
     }
 
     @MageTabCheck(
             ref = "ADN01",
             value = "An array data node must have a name")
     public void arrayDataNodeMustHaveName(SdrfArrayDataNode arrayDataNode) {
         assertNotEmptyName(arrayDataNode);
     }
 
     /*@MageTabCheck(
             ref = "ADN02",
             value = "Name of an array data node must be a valid file location")
     public void nameOfArrayDataNodeMustBeValidFileLocation(SdrfArrayDataNode arrayDataNode) {
         assertFileLocationIsValid(arrayDataNode);
     }*/
 
     @MageTabCheck(
             ref = "ADN03",
             value = "An array data node should be described by a protocol",
             modality = WARNING)
     public void arrayDataNodeShouldBeDescribedByProtocol(SdrfArrayDataNode arrayDataNode) {
         setPosition(arrayDataNode);
         assertNodeIsDescribedByProtocol(arrayDataNode);
     }
 
     @MageTabCheck(
             ref = "DADN01",
             value = "A derived array data node must have name specified")
     public void derivedArrayDataNodeMustHaveName(SdrfDerivedArrayDataNode derivedArrayDataNode) {
         assertNotEmptyName(derivedArrayDataNode);
     }
 
    /* @MageTabCheck(
             ref = "DADN02",
             value = "Name of a derived array data node must be a valid file location")
     public void nameOfDerivedArrayDataNodeMustBeValidFileLocation(SdrfDerivedArrayDataNode derivedArrayDataNode) {
         assertFileLocationIsValid(derivedArrayDataNode);
     }*/
 
     @MageTabCheck(
             ref = "DADN03",
             value = "A derived array data node should be described by a protocol",
             modality = WARNING)
     public void derivedArrayDataNodeShouldBeDescribedByProtocol(SdrfDerivedArrayDataNode derivedArrayDataNode) {
         assertNodeIsDescribedByProtocol(derivedArrayDataNode);
     }
 
     @MageTabCheck(
             ref = "ADMN01",
             value = "An array data matrix node must have name specified")
     public void arrayDataMatrixNodeMustHaveName(SdrfArrayDataMatrixNode arrayDataMatrixNode) {
         assertNotEmptyName(arrayDataMatrixNode);
     }
 
     /*@MageTabCheck(
             ref = "ADMN02",
             value = "Name of an array data matrix node must be valid file location")
     public void nameOfArrayDataMatrixNodeMustBeValidFileLocation(SdrfArrayDataMatrixNode arrayDataMatrixNode) {
         assertFileLocationIsValid(arrayDataMatrixNode);
     }*/
 
     @MageTabCheck(
             ref = "ADMN03",
             value = "An array data matrix node should be described by a protocol",
             modality = WARNING)
     public void arrayDataMatrixNodeShouldBeDescribedByProtocol(SdrfArrayDataMatrixNode arrayDataMatrixNode) {
         assertNodeIsDescribedByProtocol(arrayDataMatrixNode);
     }
 
     @MageTabCheck(
             ref = "DADMN01",
             value = "A derived array data matrix node must have name specified")
     public void derivedArrayDataMatrixNodeMustHaveName(SdrfDerivedArrayDataMatrixNode derivedArrayDataMatrixNode) {
         assertNotEmptyName(derivedArrayDataMatrixNode);
     }
 
     /*@MageTabCheck(
             ref = "DADMN02",
             value = "Name of derived data matrix node must be valid file location")
     public void nameOfDerivedArrayDataMatrixNodeMustBeValidFileLocation(
             SdrfDerivedArrayDataMatrixNode derivedArrayDataMatrixNode) {
         assertFileLocationIsValid(derivedArrayDataMatrixNode);
     }*/
 
     @MageTabCheck(
             ref = "DADMN03",
             value = "A derived array data matrix node should be described by protocol",
             modality = WARNING)
     public void derivedArrayDataMatrixNodeShouldBeDescribedByProtocol(
             SdrfDerivedArrayDataMatrixNode derivedArrayDataMatrixNode) {
         assertNodeIsDescribedByProtocol(derivedArrayDataMatrixNode);
     }
 
     private static <T> void assertNotNull(T obj) {
         assertThat(obj, notNullValue());
     }
 
     private static void assertNotEmptyString(String str) {
         assertThat(str, notNullValue());
         assertThat(str.trim(), not(isEmptyString()));
     }
 
     private static void assertTermSourceIsValid(HasTermSource t) {
         if (isNullOrEmpty(t.getTermSourceRef())) {
             return;
         }
         assertNotNull(t.getTermSource());
     }
 
     private static void assertNotEmptyName(SdrfGraphEntity node) {
         setPosition(node);
         assertNotEmptyString(node.getName());
     }
 
     private static void assertNodeIsDescribedByProtocol(SdrfGraphNode node) {
         setPosition(node);
         assertThat(getParentProtocolNodes(node), is(not(empty())));
     }
 
     private static void assertFileLocationIsValid(SdrfDataNode dataNode) {
         FileLocation location = dataNode.getLocation();
         if (!location.isEmpty()) {
             setPosition(dataNode);
             assertThat(location, isValidFileLocation());
         }
     }
 
     private static Collection<SdrfProtocolNode> getParentProtocolNodes(SdrfGraphNode node) {
         List<SdrfProtocolNode> protocols = newArrayList();
         Queue<SdrfGraphNode> queue = new ArrayDeque<SdrfGraphNode>();
         queue.addAll(node.getParentNodes());
         while (!queue.isEmpty()) {
             SdrfGraphNode p = queue.poll();
             if (SdrfProtocolNode.class.isAssignableFrom(p.getClass())) {
                 protocols.add((SdrfProtocolNode) p);
                 queue.addAll(p.getParentNodes());
             }
         }
         return protocols;
     }
 
     private static Collection<SdrfGraphNode> getParentNodes(SdrfGraphNode node) {
         List<SdrfGraphNode> parents = newArrayList();
         for (SdrfGraphNode p : node.getParentNodes()) {
             if (SdrfProtocolNode.class.isAssignableFrom(p.getClass())) {
                 parents.addAll(getParentNodes(p));
             } else {
                 parents.add(p);
             }
         }
         return parents;
     }
 
     private static <T extends HasLocation> void setPosition(T t) {
         setCheckPosition(t.getFileName(), t.getLine(), t.getColumn());
     }
 }
