 package org.fao.fi.gis.metadata.collection.rfb;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.fao.fi.gis.metadata.collection.species.SpeciesEntity.SpeciesProperty;
 import org.fao.fi.gis.metadata.association.GeographicMetaObjectProperty;
 import org.fao.fi.gis.metadata.authority.AuthorityEntity;
 import org.fao.fi.gis.metadata.entity.EntityAddin;
 import org.fao.fi.gis.metadata.entity.GeographicEntity;
 import org.fao.fi.gis.metadata.entity.GeographicEntityImpl;
 import org.fao.fi.gis.metadata.model.MetadataConfig;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * RFB Entity
  * 
  * @author eblondel (FAO)
  *
  */
 public class RfbEntity extends GeographicEntityImpl implements GeographicEntity{
 
 	public enum RfbProperty implements GeographicMetaObjectProperty{
 		
 		FAO (AuthorityEntity.FAO, true, true),
 		FLOD (AuthorityEntity.FLOD, true, true),
 		
 		FIGIS(AuthorityEntity.FIGIS, true, false);
 		
 		private final AuthorityEntity authority;
 		private final boolean thesaurus;
 		private final boolean containsURIs;
 		
 		RfbProperty(AuthorityEntity authority, boolean thesaurus, boolean containsURIs){
 			this.authority = authority;
 			this.thesaurus = thesaurus;
 			this.containsURIs = containsURIs;
 		}
 		
 		public AuthorityEntity authority(){
 			return this.authority;
 		}
 
 		public boolean isThesaurus() {
 			return this.thesaurus;
 		}
 
 		public boolean containsURIs() {
 			return this.containsURIs;
 		}
 		
 	}
 	
 	private FLODRfbEntity FLODRfbEntity;
 
 	
 	public RfbEntity(String code, Map<EntityAddin,String> addins, MetadataConfig config) throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
 
 		super(code, addins, config);
 		
 		this.FLODRfbEntity = new FLODRfbEntity(code);
 		this.setRefName(this.FLODRfbEntity.getName());
 		
 		Map<GeographicMetaObjectProperty, List<String>>properties = new HashMap<GeographicMetaObjectProperty, List<String>>();
 		properties.put(RfbProperty.FIGIS, Arrays.asList(this.getCode(),
 													    this.FLODRfbEntity.getName()));
 		
 		properties.put(RfbProperty.FLOD, Arrays.asList(this.FLODRfbEntity.getRfbCodedEntity()));
 		properties.put(SpeciesProperty.FAO, Arrays.asList(this.getMetaIdentifier()));
 		this.setSpecificProperties(properties);
 
 		this.setFigisDomain("rfbs");
		this.setFigisId(this.FLODRfbEntity.getCode());
 		this.setFigisViewerId(this.FLODRfbEntity.getCode());
 
 		this.setRfbAbstract();
 	}
 
 
 	
 	private void setRfbAbstract() throws ParserConfigurationException, SAXException, IOException{
 		
 		String figisID = this.getAddins().get(EntityAddin.FigisId);
 		
 		System.out.println(figisID);
 		URL fsURL = new URL("http://www.fao.org/fishery/xml/organization/"+figisID);
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document doc = dBuilder.parse(fsURL.openStream());	
 
 		Element geoCoverage = (Element) doc.getDocumentElement().getElementsByTagName("fi:GeoCoverage").item(0);
 		NodeList nodeList = geoCoverage.getElementsByTagName("fi:Text");
 		if(nodeList.getLength() > 0){
 			String abstractText = nodeList.item(0).getTextContent().replaceAll("<p>", "").replaceAll("</p>", "");
 			this.getConfig().getContent().setAbstract(abstractText);
 		}
 	
 	}
 
 }
