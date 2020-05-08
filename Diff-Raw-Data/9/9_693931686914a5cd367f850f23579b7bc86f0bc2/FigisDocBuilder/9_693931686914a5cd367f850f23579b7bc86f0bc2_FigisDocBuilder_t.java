 package org.fao.fi.vme.sync2.mapping.xml;
 
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 
 import org.fao.fi.figis.devcon.BiblioEntry;
 import org.fao.fi.figis.devcon.CollectionRef;
 import org.fao.fi.figis.devcon.CorporateCoverPage;
 import org.fao.fi.figis.devcon.CoverPage;
 import org.fao.fi.figis.devcon.DataEntry;
 import org.fao.fi.figis.devcon.Editor;
 import org.fao.fi.figis.devcon.FIGISDoc;
 import org.fao.fi.figis.devcon.FigisID;
 import org.fao.fi.figis.devcon.FisheryArea;
 import org.fao.fi.figis.devcon.ForeignID;
 import org.fao.fi.figis.devcon.GeoForm;
 import org.fao.fi.figis.devcon.HabitatBio;
 import org.fao.fi.figis.devcon.Impacts;
 import org.fao.fi.figis.devcon.Management;
 import org.fao.fi.figis.devcon.ManagementMethodEntry;
 import org.fao.fi.figis.devcon.ManagementMethods;
 import org.fao.fi.figis.devcon.Max;
 import org.fao.fi.figis.devcon.Measure;
 import org.fao.fi.figis.devcon.MeasureType;
 import org.fao.fi.figis.devcon.Min;
 import org.fao.fi.figis.devcon.ObjectFactory;
 import org.fao.fi.figis.devcon.ObjectSource;
 import org.fao.fi.figis.devcon.OrgRef;
 import org.fao.fi.figis.devcon.Owner;
 import org.fao.fi.figis.devcon.Range;
 import org.fao.fi.figis.devcon.Sources;
 import org.fao.fi.figis.devcon.Text;
 import org.fao.fi.figis.devcon.VME;
 import org.fao.fi.figis.devcon.VMECriteria;
 import org.fao.fi.figis.devcon.VMEIdent;
 import org.fao.fi.figis.devcon.VMEType;
 import org.fao.fi.figis.devcon.WaterAreaRef;
 import org.fao.fi.vme.domain.GeneralMeasure;
 import org.fao.fi.vme.domain.History;
 import org.fao.fi.vme.domain.InformationSource;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.Rfmo;
 import org.fao.fi.vme.domain.SpecificMeasure;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.util.Lang;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.sync2.mapping.RfmoHistory;
 import org.fao.fi.vme.sync2.mapping.VmeHistory;
 import org.purl.agmes._1.CreatorCorporate;
 import org.purl.dc.elements._1.Identifier;
 import org.purl.dc.elements._1.Title;
 import org.purl.dc.elements._1.Type;
 import org.purl.dc.terms.Abstrakt;
 import org.purl.dc.terms.BibliographicCitation;
 import org.purl.dc.terms.Created;
 
 /**
  * FigisDocBuilder, to build a FIGISDoc from VME Domain database
  * 
  * @author Emmanuel Blondel
  * @author Erik van Ingen
  * 
  */
 public class FigisDocBuilder {
 
 	private ObjectFactory f = new ObjectFactory();
 	private org.purl.dc.elements._1.ObjectFactory dcf = new org.purl.dc.elements._1.ObjectFactory();
 	private MultiLingualStringUtil u = new MultiLingualStringUtil();
 	private EnglishTextUtil ut = new EnglishTextUtil();
 	private ManagementMethodEntryBuilder mmeBuilder = new ManagementMethodEntryBuilder();
 	private CurrentDate currentDate = new CurrentDate();
 
 	/**
 	 * Adds specificMeasures to a FIGISDoc
 	 * 
 	 * measureSummary fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/fi:Measure/fi:Text
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry
 	 * 
 	 * @Focus="Vulnerable Marine Ecosystems"/dc:Title[VME-specific measures]
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 *                    /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"/fi:Measure/MeasureType
 	 *                    Value="vulnerable marine ecosystem"
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry
 	 *                    /fi:Measure/fi:Text
 	 * 
 	 *                    Source/url
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure
 	 *                    /fi:Sources/fi :BiblioEntry/dc:Identifier@Type="URI"
 	 * 
 	 *                    Source/citation
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/
 	 *                    fi:Measure/fi:Sources /fi:BiblioEntry/dcterms:bibliographicCitation
 	 * 
 	 *                    ValidityPeriod/beginYear
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure
 	 *                    /fi:Range@Type="Time"/fi:Min
 	 * 
 	 *                    ValidityPeriod/endYear
 	 *                    fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi
 	 *                    :Range@Type="Time"/fi:Max
 	 * 
 	 * 
 	 * @param yearObject
 	 * @param figisDoc
 	 */
 	public void specificMeasures(SpecificMeasure yearObject, FIGISDoc figisDoc) {
 
 		// ManagementMethodEntry
 		if (yearObject != null) {
 
 			ManagementMethodEntry entry = f.createManagementMethodEntry();
 			entry.setFocus("Vulnerable Marine Ecosystems");
 
 			// title
 			Title entryTitle = new Title();
 			entryTitle.setContent("VME-specific measures");
 			entry.setTitle(entryTitle);
 
 			// Measure
 			Measure measure = f.createMeasure();
 
 			// measureType
 			MeasureType measureType = f.createMeasureType();
 			measureType.setValue("VME-specific measures");
 			measure.getTextsAndImagesAndTables().add(measureType);
 
 			// text
 			Text measureText = ut.getEnglishText(yearObject.getVmeSpecificMeasure());
 			measure.getTextsAndImagesAndTables().add(measureText);
 
 			// range (time)
 			if (yearObject.getValidityPeriod() != null) {
 				Min min = f.createMin();
 				min.setContent(yearObject.getValidityPeriod().getBeginYear().toString());
 				JAXBElement<Min> minJAXBElement = f.createRangeMin(min);
 
 				Max max = f.createMax();
 				max.setContent(yearObject.getValidityPeriod().getEndYear().toString());
 				JAXBElement<Max> maxJAXBElement = f.createRangeMax(max);
 
 				Range range = f.createRange();
 				range.setType("Time");
 				range.getContent().add(minJAXBElement);
 				range.getContent().add(maxJAXBElement);
 				measure.getTextsAndImagesAndTables().add(range);
 			}
 
 			// sources
 			Sources sources = f.createSources();
 			BiblioEntry biblioEntry = f.createBiblioEntry();
 
 			if (yearObject.getInformationSource() != null) {
 				BibliographicCitation citation = new BibliographicCitation();
 				citation.setContent(u.getEnglish(yearObject.getInformationSource().getCitation()));
 				biblioEntry.getContent().add(citation);
 				if (yearObject.getInformationSource().getUrl() != null) {
 					Identifier identifier = new Identifier();
 					identifier.setType("URI");
 					identifier.setContent(yearObject.getInformationSource().getUrl().toString());
 					biblioEntry.getContent().add(identifier);
 				}
 			}
 
 			sources.getTextsAndImagesAndTables().add(biblioEntry);
 			measure.getTextsAndImagesAndTables().add(sources);
 
 			entry.getTextsAndImagesAndTables().add(measure); // add measure to ManagementMethodEntry
 
 			ManagementMethods managementMethods = findManagementMethods(figisDoc);
 
 			managementMethods.getManagementMethodEntriesAndTextsAndImages().add(entry);
 
 		}
 
 	}
 
 	private ManagementMethods findManagementMethods(FIGISDoc figisDoc) {
 		Management management = findManagement(figisDoc);
 
 		ManagementMethods managementMethods = null;
 		List<Object> textsAndImagesAndTablesList = management.getTextsAndImagesAndTables();
 		for (Object o : textsAndImagesAndTablesList) {
 			if (o instanceof ManagementMethods) {
 				managementMethods = (ManagementMethods) o;
 			}
 		}
 
 		if (managementMethods == null) {
 			managementMethods = f.createManagementMethods();
 			management.getTextsAndImagesAndTables().add(managementMethods);
 		}
 		return managementMethods;
 	}
 
 	private Management findManagement(FIGISDoc figisDoc) {
 		List<Object> list = figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts();
 		Management management = null;
 		for (Object object : list) {
 			if (object instanceof Management) {
 				management = (Management) object;
 			}
 		}
 		if (management == null) {
 			management = f.createManagement();
 			figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(management);
 		}
 		return management;
 	}
 
 	/**
 	 * Adds a VME history to the FIGISDoc
 	 * 
 	 * VME_history fi:FIGISDoc/fi:VME/fi:History/fi:Text
 	 * 
 	 * @param yearObject
 	 * @param figisDoc
 	 */
 	public void vmeHistory(VmeHistory yearObject, FIGISDoc figisDoc) {
 		// TODO
 
 		/*
 		 * org.fao.fi.figis.devcon.History hist = f.createHistory(); Text historyText = f.createText();
 		 * historyText.getContent().add(u.getEnglish(history.getHistory()));
 		 * hist.getTextsAndImagesAndTables().add(historyText);
 		 * figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(hist);
 		 */
 	}
 
 	/**
 	 * Adds a Fishery history to the FIGISDoc
 	 * 
 	 * @param yearObject
 	 * @param figisDoc
 	 */
 	public void rfmoHistory(RfmoHistory yearObject, FIGISDoc figisDoc) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Adds a profile to the FIGISDoc
 	 * 
 	 * descriptionBiological fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:Text
 	 * 
 	 * descriptionPhysical fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:GeoForm/fi:Text
 	 * 
 	 * descriptionImpact fi:FIGISDoc/fi:VME/fi:Impacts/fi:Text
 	 * 
 	 * 
 	 * @param profile
 	 * @param figisDoc
 	 */
 	public void profile(Profile yearObject, FIGISDoc figisDoc) {
 		// Habitat-Biological profile
 		if (yearObject != null) {
 			HabitatBio habitatBio = f.createHabitatBio();
 
 			// • VMEIdent
 			// • HabitatBio
 			// • Impacts
 			// • Management
 			// • History
 			// • FisheryAreas
 			// • AddInfo
 			// • Sources
 			// • RelatedResources
 
 			Text text1 = ut.getEnglishText(yearObject.getDescriptionBiological());
 			habitatBio.getClimaticZonesAndDepthZonesAndDepthBehavs().add(text1);
 			figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(habitatBio);
 
 			// Physical profile
 			Text text2 = ut.getEnglishText(yearObject.getDescriptionPhisical());
 			GeoForm geoform = f.createGeoForm();
 			JAXBElement<Text> geoformJAXBElement = f.createGeoFormText(text2);
 			geoform.getContent().add(geoformJAXBElement);
 			habitatBio.getClimaticZonesAndDepthZonesAndDepthBehavs().add(geoform); // geoForm is part of HabitatBio
 																					// profile
 
 			// Impacts profile
 			Impacts impacts = f.createImpacts();
 			Text text3 = ut.getEnglishText(yearObject.getDescriptionImpact());
 			impacts.getTextsAndImagesAndTables().add(text3);
 			figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(impacts);
 		}
 
 	}
 
 	/**
 	 * Adds GeneralMeasures to the FIGISDoc assuming the Management and ManagementMethods children have been yet created
 	 * by specifying the specificMeasures.
 	 * 
 	 * FishingAreas fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/dc:Title[VME general measures]
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 * /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"/fi:Measure/MeasureType@Value="Fishing_areas"
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * ExploratoryFishingProtocol fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/dc:Title[VME general measures]
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 * /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"
 	 * /fi:Measure/MeasureType@Value="Exploratory_fishing_protocol"
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * EncounterProtocols fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/dc:Title[VME general measures]
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 * /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"
 	 * /fi:Measure/MeasureType@Value="VME_encounter_protocols"
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * Threshold fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/dc:Title[VME general measures]
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 * /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"/fi:Measure/MeasureType@Value="VME_threshold"
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * IndicatorSpecies fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@Focus=
 	 * "Vulnerable Marine Ecosystems"/dc:Title[VME general measures]
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods
 	 * /fi:ManagementMethodEntry@Focus="Vulnerable Marine Ecosystems"
 	 * /fi:Measure/MeasureType@Value="VME_indicatorspecies"
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Measure/fi:Text
 	 * 
 	 * Source/url
 	 * fi:FIGISDoc/fi:VME//fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Sources/fi:BiblioEntry
 	 * /dc:Identifier@Type="URI" Source/citation
 	 * fi:FIGISDoc/fi:VME//fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry
 	 * /fi:Sources/fi:BiblioEntry/dcterms:bibliographicCitation
 	 * 
 	 * ValidityPeriod/beginYear
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Range@Type="Time"/fi:Min
 	 * ValidityPeriod/endYear
 	 * fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry/fi:Range@Type="Time"/fi:Max
 	 * 
 	 * @param yearObject
 	 * @param figisDoc
 	 */
 	public void generalMeasures(GeneralMeasure yearObject, FIGISDoc figisDoc) {
 
 		// entry
 		ManagementMethodEntry entry = f.createManagementMethodEntry();
 
 		mmeBuilder.init(entry);
 		mmeBuilder.addMeasureToEntry1(yearObject, entry);
 		mmeBuilder.addMeasureToEntry2(yearObject, entry);
 		mmeBuilder.addMeasureToEntry3(yearObject, entry);
 		mmeBuilder.addMeasureToEntry4(yearObject, entry);
 		mmeBuilder.addMeasureToEntry5(yearObject, entry);
 		mmeBuilder.addSources(yearObject, entry);
 		mmeBuilder.addRange(yearObject, entry);
 
 		ManagementMethods methods = findManagementMethods(figisDoc);
 		methods.getManagementMethodEntriesAndTextsAndImages().add(entry);
 
 		// Management m = findManagement(figisDoc);
 
 		// fi:FIGISDoc/fi:VME/fi:Management/fi:ManagementMethods/fi:ManagementMethodEntry@
 
 	}
 
 	/**
 	 * Adds a Vme to the FIGISDoc
 	 * 
 	 * VME Identifier fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:FigisID
 	 * 
 	 * inventoryIdentifier fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:ForeignID@CodeSystem="invid"/@Code
 	 * 
 	 * name fi:FIGISDoc/fi:VME/fi:VMEIdent/dc:Title
 	 * 
 	 * geographicLayerId fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:WaterAreaRef/fi:ForeignID@CodeSystem="vme"/@Code
 	 * 
 	 * areaType fi:FIGISDoc/fi:VME/fi:VMEIdent/VMEType/@Value
 	 * 
 	 * criteria fi:FIGISDoc/fi:VME/fi:VMEIdent/VMECriteria/@Value
 	 * 
 	 * ValidityPeriod/beginYear fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:Range@Type="Time"/fi:Min
 	 * 
 	 * ValidityPeriod/endYear fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:Range@Type="Time"/fi:Max
 	 * 
 	 * 
 	 * Observation/Year fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:ReportingYear
 	 * 
 	 * 
 	 * @param vmeDomain
 	 * @param i
 	 * @param figisDoc
 	 */
 	public void vme(Vme vmeDomain, int year, FIGISDoc figisDoc) {
 		VMEIdent vmeIdent = new VMEIdent();
 
 		// FigisID
 		FigisID figisID = new FigisID();
 		figisID.setContent(vmeDomain.getId().toString());
 
 		// Title
 		Title title = new Title();
 		title.setContent(vmeDomain.getName().getStringMap().get(Lang.EN));
 
 		// ForeignID
 		ForeignID vmeForeignID = new ForeignID();
 		vmeForeignID.setCodeSystem("invid");
 		vmeForeignID.setCode(vmeDomain.getInventoryIdentifier());
 
 		// WaterAreaRef
 		WaterAreaRef waterAreaRef = new WaterAreaRef();
 		ForeignID areaForeignID = new ForeignID();
 		areaForeignID.setCodeSystem("vme");
 		areaForeignID.setCode(vmeDomain.getGeoRefList().get(0).getGeographicFeatureID());
 		waterAreaRef.getFigisIDsAndForeignIDs().add(areaForeignID);
 
 		// Validity period - Range
 		Min min = f.createMin();
 		min.setContent(vmeDomain.getValidityPeriod().getBeginYear().toString());
 		JAXBElement<Min> minJAXBElement = f.createRangeMin(min);
 
 		Max max = f.createMax();
 		max.setContent(vmeDomain.getValidityPeriod().getEndYear().toString());
 		JAXBElement<Max> maxJAXBElement = f.createRangeMax(max);
 
 		Range range = f.createRange();
 		range.setType("Time");
 		range.getContent().add(minJAXBElement);
 		range.getContent().add(maxJAXBElement);
 
 		// VME Type
 		VMEType vmeType = new VMEType();
 		vmeType.setValue(vmeDomain.getAreaType());
 
 		// VME Criteria
 		VMECriteria vmeCriteria = new VMECriteria();
 		vmeCriteria.setValue(vmeDomain.getCriteria());
 
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(figisID);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeForeignID);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(waterAreaRef);
 		// OrgRef
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(title);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeType);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeCriteria);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(Integer.toString(year));
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(range);
 		// GeoReference
 
 		VME vme = new VME();
 		vme.setVMEIdent(vmeIdent);
 		figisDoc.setVME(vme);
 
 	}
 
 	/**
 	 * Adds a RFMO to the FIGISDoc
 	 * 
 	 * Rfmo fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:OrgRef/fi:ForeignID@CodeSystem="rfb"/@Code
 	 * 
 	 * @param rfmo
 	 * @param figisDoc
 	 */
 	public void rfmo(Rfmo rfmo, FIGISDoc figisDoc) {
 		ForeignID rfmoForeignID = f.createForeignID();
 		rfmoForeignID.setCodeSystem("acronym");
 		rfmoForeignID.setCode(rfmo.getId());
 
 		OrgRef rfmoOrg = f.createOrgRef();
 		rfmoOrg.getForeignIDsAndFigisIDsAndTitles().add(rfmoForeignID);
 		figisDoc.getVME().getVMEIdent().getFigisIDsAndForeignIDsAndWaterAreaReves().add(rfmoOrg);
 
 	}
 
 	/**
 	 * 
 	 * -- date fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dcterms:Created
 	 * 
 	 * committee fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/ags:CreatorCorporate
 	 * 
 	 * reportSummary fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dcterms:Abstract
 	 * 
 	 * url fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dc:Identifier@Type="URI"
 	 * 
 	 * citation fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dcterms:bibliographicCitation
 	 * 
 	 * -- type fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dc:Type
 	 * 
 	 * meetingStartDate - meetingEndDate fi:FIGISDoc/fi:VME/fi:Sources/fi:BiblioEntry/dc:Date *
 	 * 
 	 * @param infoSourceList
 	 * @param figisDoc
 	 */
 	public void informationSource(List<InformationSource> infoSourceList, FIGISDoc figisDoc) {
 		Sources sources = f.createSources();
 
 		for (InformationSource infoSource : infoSourceList) {
 
 			BiblioEntry biblioEntry = f.createBiblioEntry();
 
 			// Created
 			Created created = new Created();
 			created.setContent(currentDate.getCurrentDateYyyyMmDd());
 			biblioEntry.getContent().add(created);
 
 			CreatorCorporate cc = new CreatorCorporate();
 			cc.setContent(u.getEnglish(infoSource.getCommittee()));
 			biblioEntry.getContent().add(cc);
 
 			biblioEntry.getContent().add(Integer.toString(infoSource.getPublicationYear()));
 
 			Abstrakt bibAbstract = new Abstrakt();
 			bibAbstract.setContent(u.getEnglish(infoSource.getReportSummary()));
 			biblioEntry.getContent().add(bibAbstract);
 
 			Identifier identifier = new Identifier();
 			identifier.setType("URI");
 			identifier.setContent(infoSource.getUrl().toString());
 			biblioEntry.getContent().add(identifier);
 
 			BibliographicCitation citation = new BibliographicCitation();
 			citation.setContent(u.getEnglish(infoSource.getCitation()));
 			biblioEntry.getContent().add(citation);
 
 			Type type = dcf.createType();
 			type.setType(Integer.toString(infoSource.getSourceType()));
 			biblioEntry.getContent().add(type);
 
 			sources.getTextsAndImagesAndTables().add(biblioEntry);
 		}
 
 		figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(sources);
 	}
 
 	/**
 	 * <fi:DataEntry>
 	 * 
 	 * <fi:Editor>///RFMO Acronym///</fi:Editor>
 	 * 
 	 * <dcterms:Created>//// date of creation yyyy-mm-dd ////</dcterms:Created>
 	 * 
 	 * </fi:DataEntry>
 	 * 
 	 * <fi:ObjectSource>
 	 * 
 	 * <fi:Owner>
 	 * 
 	 * <fi:CollectionRef>
 	 * 
 	 * <fi:FigisID MetaID="267000">7300</fi:FigisID>
 	 * 
 	 * </fi:CollectionRef>
 	 * 
 	 * </fi:Owner>
 	 * 
 	 * <fi:CorporateCoverPage>
 	 * 
 	 * <fi:FigisID MetaID="280000">791</fi:FigisID>
 	 * 
 	 * </fi:CorporateCoverPage>
 	 * 
 	 * <fi:CoverPage>
 	 * 
 	 * <dcterms:Created>//// date of creation yyyy-mm-dd ////</dcterms:Created>
 	 * 
 	 * </fi:CoverPage>
 	 * 
 	 * </fi:ObjectSource>
 	 * 
 	 * 
 	 * @param figisDoc
 	 */
 	public void dataEntryObjectSource(String rfmo, FIGISDoc figisDoc) {
 
 		// dataEntry
 		Editor editor = f.createEditor();
 		editor.setContent(rfmo);
 
 		Created created = new Created();
 		created.setContent(currentDate.getCurrentDateYyyyMmDd());
 
 		DataEntry dataEntry = f.createDataEntry();
 		dataEntry.setEditor(editor);
 		dataEntry.setCreated(created);
 
 		figisDoc.setDataEntry(dataEntry);
 
 		// fi:ObjectSource (owner corporateCoverPage, coverPage)
 
 		// owner
 		FigisID figisID = new FigisID();
 		figisID.setContent("7300");
 		figisID.setMetaID("267000");
 
 		CollectionRef collectionRef = f.createCollectionRef();
 		collectionRef.getFigisIDsAndForeignIDs().add(figisID);
 
 		Owner owner = f.createOwner();
 		owner.setCollectionRef(collectionRef);
 
 		// corporateCoverPage <fi:FigisID MetaID="280000">791</fi:FigisID>
 		FigisID figisIDCC = new FigisID();
 		figisIDCC.setContent("791");
 		figisIDCC.setMetaID("280000");
 		CorporateCoverPage corporateCoverPage = f.createCorporateCoverPage();
 		corporateCoverPage.getFigisIDsAndForeignIDs().add(figisIDCC);
 
 		// coverPage
 		CoverPage coverPage = f.createCoverPage();
 		coverPage.getCreatorPersonalsAndCreatedsAndModifieds().add(currentDate.getCurrentDateYyyyMmDd());
 
 		ObjectSource objectSource = f.createObjectSource();
 		objectSource.setOwner(owner);
 		objectSource.setCoverPage(coverPage);
 		objectSource.setCorporateCoverPage(corporateCoverPage);
 
 		figisDoc.setObjectSource(objectSource);
 
 	}
 
 	public void fisheryArea(History fisheryAreasHistory, FIGISDoc figisDoc) {
 		// // FishingArea_history fi:FIGISDoc/fi:VME/fi:FisheryArea/fi:Text
 		if (fisheryAreasHistory != null) {
 			Text text = ut.getEnglishText(fisheryAreasHistory.getHistory());
 			FisheryArea fisheryArea = f.createFisheryArea();
 			fisheryArea.getTextsAndImagesAndTables().add(text);
 			figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(fisheryArea);
 		}
 
 	}
 
 	public void vmesHistory(History vmesHistory, FIGISDoc figisDoc) {
 		// // VME_history fi:FIGISDoc/fi:VME/fi:History/fi:Text
 		if (vmesHistory != null) {
 			Text text = ut.getEnglishText(vmesHistory.getHistory());
 			org.fao.fi.figis.devcon.History history = f.createHistory();
 			history.getTextsAndImagesAndTables().add(text);
 			figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(history);
 		}
 	}
 }
