 package org.fao.fi.vme.sync2.mapping.xml;
 
 import javax.xml.bind.JAXBElement;
 
 import org.fao.fi.figis.devcon.FIGISDoc;
 import org.fao.fi.figis.devcon.FigisID;
 import org.fao.fi.figis.devcon.ForeignID;
 import org.fao.fi.figis.devcon.GeoForm;
 import org.fao.fi.figis.devcon.HabitatBio;
 import org.fao.fi.figis.devcon.Impacts;
 import org.fao.fi.figis.devcon.Max;
 import org.fao.fi.figis.devcon.Min;
 import org.fao.fi.figis.devcon.ObjectFactory;
 import org.fao.fi.figis.devcon.OrgRef;
 import org.fao.fi.figis.devcon.Range;
 import org.fao.fi.figis.devcon.Text;
 import org.fao.fi.figis.devcon.VME;
 import org.fao.fi.figis.devcon.VMECriteria;
 import org.fao.fi.figis.devcon.VMEIdent;
 import org.fao.fi.figis.devcon.VMEType;
 import org.fao.fi.figis.devcon.WaterAreaRef;
 import org.fao.fi.vme.domain.GeneralMeasures;
 import org.fao.fi.vme.domain.History;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.Rfmo;
 import org.fao.fi.vme.domain.SpecificMeasures;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.util.Lang;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.sync2.mapping.RfmoHistory;
 import org.purl.dc.elements._1.Title;
 
 public class FigisDocBuilder {
 
 	ObjectFactory f = new ObjectFactory();
 	MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 	public void specificMeasures(SpecificMeasures yearObject, FIGISDoc figisDoc) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * VME_history 	fi:FIGISDoc/fi:VME/fi:History/fi:Text 
 	 * 
 	 * @param history
 	 * @param figisDoc
 	 */
 	public void vmeHistory(History history, FIGISDoc figisDoc) {	
 		org.fao.fi.figis.devcon.History hist = f.createHistory();
 		Text historyText = f.createText();
 		historyText.getContent().add(u.getEnglish(history.getHistory()));
 		hist.getTextsAndImagesAndTables().add(historyText);
 		figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(hist);
 	}
 
 	
 	public void rfmoHistory(RfmoHistory yearObject, FIGISDoc figisDoc) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * 
 	 * descriptionBiological fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:Text
 	 * 
 	 * descriptionPhysical fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:GeoForm/fi:Text
 	 * 
 	 * descriptionImpact fi:FIGISDoc/fi:VME/fi:Impacts/fi:Text
 	 * 
 	 * TODO finish this one.
 	 * 
 	 * 
 	 * @param profile
 	 * @param figisDoc
 	 */
 	public void profile(Profile profile, FIGISDoc figisDoc) {
 		//Habitat-Biological profile
 		HabitatBio habitatBio = f.createHabitatBio();
 
 		Text text1 = f.createText();
 		text1.getContent().add(u.getEnglish(profile.getDescriptionBiological()));
 		habitatBio.getClimaticZonesAndDepthZonesAndDepthBehavs().add(text1);
 		figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(habitatBio);
 
 		//Physical profile
 		Text text2 = f.createText();
 		text2.getContent().add(u.getEnglish(profile.getDescriptionPhisical()));
 		GeoForm geoform = f.createGeoForm();
 		JAXBElement<Text> geoformJAXBElement = f.createGeoFormText(text2);
 		geoform.getContent().add(geoformJAXBElement);
 		habitatBio.getClimaticZonesAndDepthZonesAndDepthBehavs().add(geoform); //geoForm is part of HabitatBio profile
 		
 		//Impacts profile
 		Impacts impacts = f.createImpacts();
 		Text text3 = f.createText();
 		text3.getContent().add(u.getEnglish(profile.getDescriptionImpact()));
 		impacts.getTextsAndImagesAndTables().add(text3);
 		figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts().add(impacts);
 
 	}
 
 	public void generalMeasures(GeneralMeasures yearObject, FIGISDoc figisDoc) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
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
 	 * @param vme
 	 * @param figisDoc
 	 */
 	public void vme(Vme vmeDomain, FIGISDoc figisDoc) {
 		VMEIdent vmeIdent = new VMEIdent();
 		
 		//FigisID
 		FigisID figisID = new FigisID();
 		figisID.setContent(vmeDomain.getId().toString());
 		
 		//Title
 		Title title = new Title();
 		title.setContent(vmeDomain.getName().getStringMap().get(Lang.EN));
 		
 		//ForeignID
 		ForeignID vmeForeignID = new ForeignID();
 		vmeForeignID.setCodeSystem("invid");
 		vmeForeignID.setCode(vmeDomain.getInventoryIdentifier());
 		
 		//WaterAreaRef
 		WaterAreaRef waterAreaRef = new WaterAreaRef();
 		ForeignID areaForeignID = new ForeignID();
 		areaForeignID.setCodeSystem("vme");
 		areaForeignID.setCode(vmeDomain.getGeoRefList().get(0).getGeographicFeatureID());
 		waterAreaRef.getFigisIDsAndForeignIDs().add(areaForeignID);
 		
 		//Validity period - Range
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
 
 		//VME Type
 		VMEType vmeType = new VMEType();
 		vmeType.setValue(vmeDomain.getAreaType());
 		
 		//VME Criteria
 		VMECriteria vmeCriteria = new VMECriteria();
 		vmeCriteria.setValue(vmeDomain.getCriteria());
 		
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(figisID);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(title);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeForeignID);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(waterAreaRef);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeCriteria);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(vmeType);
 		vmeIdent.getFigisIDsAndForeignIDsAndWaterAreaReves().add(range);
 
 		VME vme = new VME();
 		vme.setVMEIdent(vmeIdent);
 		figisDoc.setVME(vme);
 
 	}
 
 	/**
 	 * 	Observation/Year	fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:ReportingYear 
 	 * 
 	 * @param year
 	 * @param figisDoc
 	 */
 	public void year(Object year, FIGISDoc figisDoc) {
 		figisDoc.getVME().getVMEIdent().getFigisIDsAndForeignIDsAndWaterAreaReves().add(year);
 	}
 	
 	
 	/**
 	 * Rfmo fi:FIGISDoc/fi:VME/fi:VMEIdent/fi:OrgRef/fi:ForeignID@CodeSystem="rfb"/@Code
 	 * 
 	 * @param rfmo
 	 * @param figisDoc
 	 */
 	public void rfmo(Rfmo rfmo, FIGISDoc figisDoc) {
 		ForeignID rfmoForeignID = f.createForeignID();
 		rfmoForeignID.setCodeSystem("rfb");
 		rfmoForeignID.setCode(rfmo.getId());
 
 		OrgRef rfmoOrg = f.createOrgRef();
 		rfmoOrg.getForeignIDsAndFigisIDsAndTitles().add(rfmoForeignID);
 		figisDoc.getVME().getVMEIdent()
 				.getFigisIDsAndForeignIDsAndWaterAreaReves().add(rfmoOrg);
 	}
 
 }
