 package org.fao.fi.vme.sync2.mapping.xml;
 
 import javax.xml.bind.JAXBElement;
 
 import org.fao.fi.figis.devcon.BiblioEntry;
 import org.fao.fi.figis.devcon.ManagementMethodEntry;
 import org.fao.fi.figis.devcon.Max;
 import org.fao.fi.figis.devcon.Measure;
 import org.fao.fi.figis.devcon.MeasureType;
 import org.fao.fi.figis.devcon.Min;
 import org.fao.fi.figis.devcon.ObjectFactory;
 import org.fao.fi.figis.devcon.Range;
 import org.fao.fi.figis.devcon.Sources;
 import org.fao.fi.figis.devcon.Text;
 import org.fao.fi.vme.domain.model.GeneralMeasure;
 import org.fao.fi.vme.domain.model.InformationSource;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.purl.dc.elements._1.Identifier;
 import org.purl.dc.elements._1.Title;
 import org.purl.dc.terms.BibliographicCitation;
 
 /**
  * 
  * Building up the ManagementMethodEntry, using the vme yearobjects from the vme
  * domain.
  * 
  * 
  * @author Erik van Ingen
  * 
  */
 public class ManagementMethodEntryBuilder {
 
 	public final static String TITLE_GM = "VME general measures";
 	public final static String FISHING_AREAS = "Fishing_areas";
 	public final static String EXPLORATORY_FISHING_PROTOCOL = "Exploratory_fishing_protocol";
 	public final static String VME_ENCOUNTER_PROTOCOLS = "VME_encounter_protocols";
 	public final static String VME_THRESHOLD = "VME_threshold";
 	public final static String VME_INDICATORSPECIES = "VME_indicatorspecies";
 	public final static String URI = "URI";
 	public final static String TIME = "Time";
 
 	private ObjectFactory f = new ObjectFactory();
 	private MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 	public void initGM(ManagementMethodEntry entry) {
 		entry.setFocus(FigisDocBuilder.VULNERABLE_MARINE_ECOSYSTEMS);
 		Title entryTitle = new Title();
 		entryTitle.setContent(TITLE_GM);
 		entry.setTitle(entryTitle);
 
 	}
 
 	public void addMeasureToEntry1(GeneralMeasure gm, ManagementMethodEntry entry) {
 		// Measures
 		// 1. FishingAreas
 		if (gm != null) {
 			Measure measure1 = f.createMeasure();
 			MeasureType measureType1 = f.createMeasureType();
 			measureType1.setValue(FISHING_AREAS);
 			Text measureText1 = f.createText();
 
 			new AddWhenContentRule<Object>().check(u.getEnglish(gm.getFishingArea())).beforeAdding(measure1)
 					.to(measureText1.getContent());
 			new AddWhenContentRule<Object>().check(u.getEnglish(gm.getFishingArea())).beforeAdding(measureType1)
 					.beforeAdding(measureText1).to(measure1.getTextsAndImagesAndTables());

			new AddWhenContentRule<Object>().check(u.getEnglish(gm.getFishingArea())).beforeAdding(measure1)
					.to(entry.getTextsAndImagesAndTables());

 		}
 
 	}
 
 	/**
 	 * 
 	 * <fi:Management> <fi:ManagementMethods>
 	 * 
 	 * --<fi:ManagementMethodEntry Focus="Vulnerable Marine Ecosystems">
 	 * 
 	 * ----<dc:Title>VME general measures</dc:Title>
 	 * 
 	 * ----<fi:Measure>
 	 * 
 	 * ------<fi:MeasureType Value="Exploratory_fishing_protocol"/>
 	 * 
 	 * ------<fi:Text><![CDATA[Exploratory fishing covers all bottom fishing
 	 * activities (a) outside of the existing bottom fishing area and (b) to
 	 * fisheries within the existing bottom fishing area that show significant
 	 * change. (Art 15.8). Exploratory fisheries must be conducted according to
 	 * an exploratory fisheries protocol (Art 18; Annex 1E.I-IV) and are subject
 	 * to review FC and SC. Exploritory fisheries will be allowed only if there
 	 * are adequate mitigation measures to prevent SAI to VMEs (Art 19).
 	 * ]]></fi:Text>
 	 * 
 	 * ----</fi:Measure>
 	 * 
 	 * 
 	 * 
 	 * 
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:
 	 * ManagementMethodEntry@Focus= "Vulnerable Marine Ecosystems"/dc:Title[VME
 	 * general measures]
 	 * 
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:
 	 * ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/fi:Measure/MeasureType
 	 * 
 	 * @Value="VME_encounter_protocols"
 	 * 
 	 *                                  fi:FIGISDoc/fi:VME/fi:Management/fi:
 	 *                                  ManagementMethods/fi:
 	 *                                  ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * @param generalMeasure
 	 * @param entry
 	 */
 
 	public void addMeasureToEntry2(GeneralMeasure generalMeasure, ManagementMethodEntry entry) {
 		if (generalMeasure != null) {
 			// 2. ExploratoryFishingProtocol
 			Measure measure2 = f.createMeasure();
 			MeasureType measureType2 = f.createMeasureType();
 			measureType2.setValue(EXPLORATORY_FISHING_PROTOCOL);
 			Text measureText2 = f.createText();
 			measureText2.getContent().add(u.getEnglish(generalMeasure.getExplorataryFishingProtocol()));
 
 			measure2.getTextsAndImagesAndTables().add(measureType2);
 
 			measure2.getTextsAndImagesAndTables().add(measureText2);
 
 			new AddWhenContentRule<Object>().check(u.getEnglish(generalMeasure.getExplorataryFishingProtocol()))
 					.beforeAdding(measure2).to(entry.getTextsAndImagesAndTables());
 
 		}
 
 	}
 
 	public void addMeasureToEntry3(GeneralMeasure yearObject, ManagementMethodEntry entry) {
 		if (yearObject != null) {
 			// 3. EncounterProtocol
 			Measure measure3 = f.createMeasure();
 			MeasureType measureType3 = f.createMeasureType();
 			measureType3.setValue(VME_ENCOUNTER_PROTOCOLS);
 			Text measureText3 = f.createText();
 			measureText3.getContent().add(u.getEnglish(yearObject.getVmeEncounterProtocol()));
 
 			measure3.getTextsAndImagesAndTables().add(measureType3);
 			measure3.getTextsAndImagesAndTables().add(measureText3);
 
 			new AddWhenContentRule<Object>().check(u.getEnglish(yearObject.getVmeEncounterProtocol()))
 					.beforeAdding(measure3).to(entry.getTextsAndImagesAndTables());
 
 		}
 
 	}
 
 	public void addMeasureToEntry4(GeneralMeasure yearObject, ManagementMethodEntry entry) {
 		if (yearObject != null) {
 			// 4. Threshold
 			Measure measure4 = f.createMeasure();
 			MeasureType measureType4 = f.createMeasureType();
 			measureType4.setValue(VME_THRESHOLD);
 			Text measureText4 = f.createText();
 			measureText4.getContent().add(u.getEnglish(yearObject.getVmeThreshold()));
 
 			measure4.getTextsAndImagesAndTables().add(measureType4);
 			measure4.getTextsAndImagesAndTables().add(measureText4);
 
 			new AddWhenContentRule<Object>().check(u.getEnglish(yearObject.getVmeThreshold())).beforeAdding(measure4)
 					.to(entry.getTextsAndImagesAndTables());
 
 		}
 
 	}
 
 	void addMeasureToEntry5(GeneralMeasure yearObject, ManagementMethodEntry entry) {
 		// 5. IndicatorSpecies
 		if (yearObject != null) {
 			Measure measure5 = f.createMeasure();
 			MeasureType measureType5 = f.createMeasureType();
 			measureType5.setValue(VME_INDICATORSPECIES);
 			Text measureText5 = f.createText();
 			measureText5.getContent().add(u.getEnglish(yearObject.getVmeIndicatorSpecies()));
 
 			measure5.getTextsAndImagesAndTables().add(measureType5);
 			measure5.getTextsAndImagesAndTables().add(measureText5);
 
 			new AddWhenContentRule<Object>().check(u.getEnglish(yearObject.getVmeIndicatorSpecies()))
 					.beforeAdding(measure5).to(entry.getTextsAndImagesAndTables());
 		}
 
 	}
 
 	public void addSources(GeneralMeasure yearObject, ManagementMethodEntry entry) {
 		// ManagementMethodEntry Sources
 		if (yearObject != null) {
 			Sources sources = f.createSources();
 
 			AddWhenContentRule<Object> rule = new AddWhenContentRule<Object>();
 
 			for (InformationSource infoSource : yearObject.getInformationSourceList()) {
 				BiblioEntry biblioEntry = f.createBiblioEntry();
 
 				Identifier identifier = new Identifier();
 				identifier.setType(URI);
 				identifier.setContent(infoSource.getUrl().toString());
 				rule.check(infoSource.getUrl());
 
 				BibliographicCitation citation = new BibliographicCitation();
 				citation.setContent(u.getEnglish(infoSource.getCitation()));
 				rule.check(infoSource.getCitation());
 
 				biblioEntry.getContent().add(identifier);
 				biblioEntry.getContent().add(citation);
 				sources.getTextsAndImagesAndTables().add(biblioEntry);
 
 			}
 			rule.beforeAdding(sources).to(entry.getTextsAndImagesAndTables());
 
 		}
 
 	}
 
 	public void addRange(GeneralMeasure yearObject, ManagementMethodEntry entry) {
 		// ManagementMethodEntry ValidityPeriod
 		if (yearObject != null) {
 			Min min = f.createMin();
 			min.setContent(yearObject.getValidityPeriod().getBeginYear().toString());
 			JAXBElement<Min> minJAXBElement = f.createRangeMin(min);
 
 			Max max = f.createMax();
 			max.setContent(yearObject.getValidityPeriod().getEndYear().toString());
 			JAXBElement<Max> maxJAXBElement = f.createRangeMax(max);
 
 			Range range = f.createRange();
 			range.setType(TIME);
 			range.getContent().add(minJAXBElement);
 			range.getContent().add(maxJAXBElement);
 			new AddWhenContentRule<Object>().check(yearObject.getValidityPeriod().getBeginYear().toString())
 					.check(yearObject.getValidityPeriod().getEndYear().toString()).beforeAdding(range)
 					.to(entry.getTextsAndImagesAndTables());
 
 		}
 	}
 }
