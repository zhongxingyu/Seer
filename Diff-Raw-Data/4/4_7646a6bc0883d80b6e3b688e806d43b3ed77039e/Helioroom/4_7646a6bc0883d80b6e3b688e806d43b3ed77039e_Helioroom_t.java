 /*
  * Created Oct 29, 2010
  */
 package ltg.ps.phenomena.helioroom;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import ltg.ps.api.phenomena.PassivePhenomena;
 import ltg.ps.api.phenomena.PhenomenaWindow;
 import ltg.ps.phenomena.helioroom.support.HelioroomPersistence;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 
 /**
  * TODO Description
  *
  * @author Gugo
  */
 public class Helioroom extends PassivePhenomena {
 
 	// Planets representation constants
 	public final static String REP_IMAGE 		= "image";
 	public final static String REP_SPHERE 		= "sphere";
 
 	// Planets names constants
 	public final static String LABEL_NONE 		= "none";
 	public final static String LABEL_NAME 		= "name";
 	public final static String LABEL_COLOR 		= "color";
 
 	// State constants
 	public final static String STATE_RUNNING	= "running";
 	public final static String STATE_PAUSED 	= "paused";
 
 
 	// Simulation data
 	private String planetRepresentation = null;
 	private String planetNames = null;
 	private long startTime = -1;
 	private List<Planet> planets = null;
 	private String state = STATE_RUNNING;
 	private long startOfLastPauseTime = -1;
 
 	// Components
 	private HelioroomPersistence db = null;
 
 
 	public Helioroom(String instanceId) {
 		super(instanceId);
 		db = new HelioroomPersistence(this);
 		planets = new ArrayList<Planet>();
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void configure(String configXML) {
 		// reset the phenomena state
 		planetRepresentation = null;
 		planetNames = null;
 		startTime = -1;
 		planets.clear();
 		// load state from XML
 		Document doc = null;
 		try {
 			doc = DocumentHelper.parseText(configXML);
 			Element el = doc.getRootElement();
 			// Phenomena properties
 			if(el.elementTextTrim("state")!=null && !el.elementTextTrim("state").equals(""))
 				state = el.elementTextTrim("state");
 			if(el.elementTextTrim("startOfLastPauseTime")!=null && !el.elementTextTrim("startOfLastPauseTime").equals(""))
 				startOfLastPauseTime = Long.parseLong(el.elementTextTrim("startOfLastPauseTime"));
 			startTime = Long.parseLong(el.elementTextTrim("startTime"));
 			List<Element> plans = el.element("planets").elements();
 			for (Element el1: plans) {
 				planets.add(new Planet(
 						el1.elementTextTrim("name"), 
 						el1.elementTextTrim("color"),
 						el1.elementTextTrim("colorName"),
 						Integer.valueOf(el1.elementTextTrim("classOrbitalTime")),
 						Double.valueOf(el1.elementTextTrim("startPosition")),
						el1.elementTextTrim("representation"),
						el1.elementTextTrim("labelType")
 						));
 			}
 			sortPlanets();
 			db.save();
 			this.setChanged();
 		} catch (DocumentException e) {
 			log.info("Impossible to configure helioroom");
 		}
 	}
 
 
 
 	@Override
 	public void configureWindows(String windowsXML) {
 		// reset the windows
 		phenWindows.clear();
 		// create new windows
 		Document doc = null;
 		try {
 			doc = DocumentHelper.parseText(windowsXML);
 			@SuppressWarnings("unchecked")
 			List<Element>windows = doc.getRootElement().elements();
 			for(Element e: windows) {
 				if(e.attributeValue("type").equals("client")) {
 					phenWindows.add(new HelioroomWindow(
 							e.attributeValue("id"),
 							Integer.valueOf(e.elementTextTrim("viewAngleBegin")),
 							Integer.valueOf(e.elementTextTrim("viewAngleEnd"))
 							));
 				}
 				if(e.attributeValue("type").equals("control")) {
 					phenWindows.add(new HelioroomControlWindow(e.attributeValue("id")));
 				}
 				if(e.attributeValue("type").equals("notifier")) {
 					phenWindows.add(new HelioroomNotifierWindow(e.attributeValue("id")));
 				}
 			}
 			db.save();
 		} catch (DocumentException e) {
 			log.info("Impossible to configure helioroom windows");
 		}
 	}
 
 
 	@Override
 	public void restore() {
 		db.restore();
 	}
 
 
 	@Override
 	public void cleanup() {
 		db.cleanup();
 	}
 
 
 	public String toXML() {
 		Element root = DocumentHelper.createElement(instanceName);
 		// Windows
 		Element wins = DocumentHelper.createElement("windows");
 		Element e = null;
 		for(PhenomenaWindow w: phenWindows) {
 			e = DocumentHelper.createElement("win");
 			e.addAttribute("id", w.getWindowId());
 			if(w instanceof HelioroomWindow) {
 				e.addAttribute("type", "client");
 				e.addElement("viewAngleBegin").addText(String.valueOf(((HelioroomWindow) w).getViewAngleBegin()));
 				e.addElement("viewAngleEnd").addText(String.valueOf(((HelioroomWindow) w).getViewAngleEnd()));
 			}
 			if(w instanceof HelioroomControlWindow) {
 				e.addAttribute("type", "control");
 			}
 			if(w instanceof HelioroomNotifierWindow) {
 				e.addAttribute("type", "notifier");
 			}
 			wins.add(e);
 		}
 		root.add(wins);
 		// Configuration
 		Element conf = DocumentHelper.createElement("config");
 		if (state!=null)
 			conf.addElement("state").addText(state);
 		conf.addElement("startOfLastPauseTime").addText(String.valueOf(startOfLastPauseTime));
 		if(startTime!=-1)
 			conf.addElement("startTime").addText(String.valueOf(startTime));
 		// planets
 		Element plans = DocumentHelper.createElement("planets");
 		e = null;
 		for (Planet p: planets){
 			e = DocumentHelper.createElement("planet");
 			e.addElement("name").addText(p.getName());
 			e.addElement("color").addText(p.getColor());
 			e.addElement("colorName").addText(p.getColorName());
 			e.addElement("classOrbitalTime").addText(String.valueOf(p.getClassOrbitalTime()));
 			e.addElement("startPosition").addText(String.valueOf(p.getStartPosition()));
 			e.addElement("representation").addText(p.getRepresentation());
 			e.addElement("labelType").addText(p.getLabelType());
 			plans.add(e);
 		}
 		conf.add(plans);
 		root.add(conf);
 		// Create document
 		return removeXMLDeclaration(DocumentHelper.createDocument(root));
 	}
 
 
 
 	public String removeXMLDeclaration(Document doc) {
 		StringWriter w = new StringWriter();
 		OutputFormat f =  OutputFormat.createPrettyPrint();
 		f.setSuppressDeclaration(true);
 		XMLWriter xw = new XMLWriter(w, f);
 		try {
 			xw.write(doc);
 		} catch (IOException e1) {
 			log.error("Unable to print to a string? Really?");
 		}
 		return w.toString();
 	}
 
 
 
 	public String getPlanetRepresentation() {
 		return planetRepresentation;
 	}
 
 
 	public String getPlanetNames() {
 		return planetNames;
 	}
 
 	public Long getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(long resumeTS) {
 		if (resumeTS!=-1 && startOfLastPauseTime!=-1) {
 			long delta = (resumeTS-startOfLastPauseTime)/1000;
 			this.startTime += delta;
 			startOfLastPauseTime = -1;
 		}
 		db.save();
 		notifyObservers();
 	}
 
 
 	public void setStartOfLastPauseTime(long startOfLastPauseTime) {
 		this.startOfLastPauseTime = startOfLastPauseTime;
 		db.save();
 		notifyObservers();
 	}
 
 	public List<Planet> getPlanets() {
 		return planets;
 	}
 
 	public Planet getPlanet(String planetName) {
 		for (Planet pl : planets) {
 			if (pl.getName().equals(planetName))
 				return pl;
 		}
 		return null;
 	}
 
 	public String getState() {
 		return state;
 	}
 
 	public void setState(String state) {
 		this.state = state;
 	}
 
 
 	public void modifyPlanet(Planet pl) {
 		if (planets.remove(pl))
 			planets.add(pl);
 		sortPlanets();
 		db.save();
 		notifyObservers();
 	}
 
 
 	private void sortPlanets() {
 		Collections.sort(planets, new Comparator<Planet>() {
 			@Override
 			public int compare(Planet p1, Planet p2) {
 				return p1.getClassOrbitalTime() - p2.getClassOrbitalTime();
 			}
 
 		});
 	}
 
 
 }
