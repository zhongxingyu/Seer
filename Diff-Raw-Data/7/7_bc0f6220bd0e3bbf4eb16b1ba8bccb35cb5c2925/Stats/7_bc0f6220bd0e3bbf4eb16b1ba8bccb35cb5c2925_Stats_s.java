 package cm.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import cm.util.DnD4eRules;
 
 /**
  * Defines a D&D 4e statblock.
  * 
  * @author Matthew Rinehart &lt;gomamon2k at yahoo.com&gt;
  * @since 1.0
  */
 public class Stats {
 	/**
 	 * The name.
 	 */
 	private String _name = "";
 
 	/**
 	 * The type and keywords.
 	 */
 	private String _type = "";
 
 	/**
 	 * The primary role, e.g. Artillery, Controller.
 	 */
 	private String _role = "";
 
 	/**
 	 * The secondary role, e.g. Elite, Solo.
 	 */
 	private String _role2 = "";
 
 	/**
 	 * Extra senses, e.g., low-light vision, darkvision.
 	 */
 	private String _senses = "";
 
 	/**
 	 * Speed data.
 	 */
 	private String _speed = "";
 
 	/**
 	 * Resistances.
 	 */
 	private String _resist = "";
 
 	/**
 	 * Immunities.
 	 */
 	private String _immune = "";
 
 	/**
 	 * Vulnerabilities.
 	 */
 	private String _vuln = "";
 
 	/**
 	 * Regeneration.
 	 */
 	private String _regen = "";
 
 	/**
 	 * Alignment.
 	 */
 	private String _alignment = "";
 
 	/**
 	 * Skills with bonuses.
 	 */
 	private String _skills = "";
 
 	/**
 	 * Known languages.
 	 */
 	private String _languages = "";
 
 	/**
 	 * Possessed equipment.
 	 */
 	private String _equipment = "";
 
 	/**
 	 * Source, e.g. WotC Character Builder, WotC Adventure Tools, etc.
 	 */
 	private String _source = "";
 
 	/**
 	 * List of feats.
 	 */
 	private String _feats = "";
 
 	/**
 	 * Level.
 	 */
 	private Integer _level = 0;
 
 	/**
 	 * Experience point value.
 	 */
 	private Integer _XP = 0;
 
 	/**
 	 * Initiative bonus.
 	 */
 	private Integer _init = 0;
 
 	/**
 	 * Maximum hit points.
 	 */
 	private Integer _maxHP = 0;
 
 	/**
 	 * Saving throw bonus.
 	 */
 	private Integer _saveBonus = 0;
 
 	/**
 	 * Total action points.
 	 */
 	private Integer _actionPoints = 0;
 
 	/**
 	 * Total power points.
 	 */
 	private Integer _powerPoints = 0;
 
 	/**
 	 * Total healing surges.
 	 */
 	private Integer _surges = 0;
 
 	/**
 	 * Armor class defense.
 	 */
 	private Integer _AC = 0;
 
 	/**
 	 * Fortitude defense.
 	 */
 	private Integer _fort = 0;
 
 	/**
 	 * Reflex defense.
 	 */
 	private Integer _ref = 0;
 
 	/**
 	 * Will defense.
 	 */
 	private Integer _will = 0;
 
 	/**
 	 * Strength ability score.
 	 */
 	private Integer _str = 0;
 
 	/**
 	 * Constitution ability score.
 	 */
 	private Integer _con = 0;
 
 	/**
 	 * Dexterity ability score.
 	 */
 	private Integer _dex = 0;
 
 	/**
 	 * Intelligence ability score.
 	 */
 	private Integer _int = 0;
 
 	/**
 	 * Wisdom ability score.
 	 */
 	private Integer _wis = 0;
 
 	/**
 	 * Charisma ability score.
 	 */
 	private Integer _cha = 0;
 
 	/**
 	 * Indicates if the statblock is considered a leader.
 	 */
 	private Boolean _leader = false;
 
 	/**
 	 * A list of {@link Power}s the statblock can use.
 	 */
 	private List<Power> _powerList = new ArrayList<Power>();
 
 	/**
 	 * A list of {@link EffectBase}s the statblock can generate in an encounter.
 	 */
 	private Hashtable<String, EffectBase> _presetEffects = new Hashtable<String, EffectBase>();
 
 	/**
 	 * Coded notes stored for the statblock.
 	 */
 	private String _notesBase = "";
 
 	/**
 	 * Creates a new blank set of stats.
 	 */
 	public Stats() {
 		clearAll();
 	}
 
 	/**
 	 * Creates a copy of another statblock.
 	 * 
 	 * @param orig
 	 *            the original {@link Stats}
 	 */
 	public Stats(Stats orig) {
 		setName(orig.getName());
 		setType(orig.getType());
 		setRole(orig.getRole());
 		setRole2(orig.getRole2());
 		setSenses(orig.getSenses());
 		setSpeed(orig.getSpeed());
 		setResistance(orig.getResistance());
 		setImmunity(orig.getImmunity());
 		setVulnerability(orig.getVulnerability());
 		setRegen(orig.getRegen());
 		setAlignment(orig.getAlignment());
 		setSkills(orig.getSkills());
 		setLanguages(orig.getLanguages());
 		setEquipment(orig.getEquipment());
 		setSource(orig.getSource());
 		setFeats(orig.getFeats());
 		setLevel(orig.getLevel());
 		setXP(orig.getXP());
 		setInit(orig.getInit());
 		setMaxHP(orig.getMaxHP());
 		setSaveBonus(orig.getSaveBonus());
 		setActionPoints(orig.getActionPoints());
 		setPowerPoints(orig.getPowerPoints());
 		setSurges(orig.getSurges());
 		setAC(orig.getAC());
 		setFort(orig.getFort());
 		setRef(orig.getRef());
 		setWill(orig.getWill());
 		setStr(orig.getStr());
 		setCon(orig.getCon());
 		setDex(orig.getDex());
 		setInt(orig.getInt());
 		setWis(orig.getWis());
 		setCha(orig.getCha());
 		setLeader(orig.isLeader());
 		setNotesBase(orig.getNotesBase());
 
 		for (Power p : orig.getPowerList()) {
 			Power pow = new Power(p);
 			getPowerList().add(pow);
 		}
 
 		for (EffectBase e : orig.getPresetEffects().values()) {
 			EffectBase eff = new EffectBase(e);
 			getPresetEffects().put(eff.getEffectBaseID(), eff);
 		}
 	}
 
 	/**
 	 * Returns armor class.
 	 * 
 	 * @return armor class
 	 */
 	public Integer getAC() {
 		return _AC;
 	}
 
 	/**
 	 * Sets the armor class defense.
 	 * 
 	 * @param ac
 	 */
 	public void setAC(Integer ac) {
 		_AC = ac;
 	}
 
 	/**
 	 * Return action points.
 	 * 
 	 * @return count of action points
 	 */
 	public Integer getActionPoints() {
 		return _actionPoints;
 	}
 
 	/**
 	 * Sets the number of action points.
 	 * 
 	 * @param actionPoints
 	 *            a non-negative integer
 	 */
 	public void setActionPoints(Integer actionPoints) {
 		if (actionPoints < 0) {
 			_actionPoints = 0;
 		} else {
 			_actionPoints = actionPoints;
 		}
 	}
 
 	/**
 	 * Returns alignment.
 	 * 
 	 * @return alignment
 	 */
 	public String getAlignment() {
 		return _alignment;
 	}
 
 	/**
 	 * Sets the alignment.
 	 * 
 	 * @param alignment
 	 */
 	public void setAlignment(String alignment) {
 		_alignment = alignment;
 	}
 
 	/**
 	 * Returns the bloodied value.
 	 * 
 	 * @return the bloodied value
 	 */
 	public Integer getBloodyHP() {
 		return getMaxHP() / 2;
 	}
 
 	/**
 	 * Returns the CHA ability score.
 	 * 
 	 * @return CHA
 	 */
 	public Integer getCha() {
 		return _cha;
 	}
 
 	/**
 	 * Sets CHA ability score.
 	 * 
 	 * @param cha
 	 */
 	public void setCha(Integer cha) {
 		_cha = cha;
 	}
 
 	/**
 	 * Returns the CON ability score.
 	 * 
 	 * @return CON
 	 */
 	public Integer getCon() {
 		return _con;
 	}
 
 	/**
 	 * Sets the CON ability score.
 	 * 
 	 * @param con
 	 */
 	public void setCon(Integer con) {
 		_con = con;
 	}
 
 	/**
 	 * Returns the DEX ability score.
 	 * 
 	 * @return DEX
 	 */
 	public Integer getDex() {
 		return _dex;
 	}
 
 	/**
 	 * Sets the DEX ability score.
 	 * 
 	 * @param dex
 	 */
 	public void setDex(Integer dex) {
 		_dex = dex;
 	}
 
 	/**
 	 * Returns equipment.
 	 * 
 	 * @return equipment
 	 */
 	public String getEquipment() {
 		return _equipment;
 	}
 
 	/**
 	 * Sets the equipment list for this statblock.
 	 * 
 	 * @param equipment
 	 */
 	public void setEquipment(String equipment) {
 		_equipment = equipment;
 	}
 
 	/**
 	 * Returns feats.
 	 * 
 	 * @return feats
 	 */
 	public String getFeats() {
 		return _feats;
 	}
 
 	/**
 	 * Sets the list of feats.
 	 * 
 	 * @param feats
 	 */
 	public void setFeats(String feats) {
 		_feats = feats;
 	}
 
 	/**
 	 * Returns fortitude defense.
 	 * 
 	 * @return fortitude
 	 */
 	public Integer getFort() {
 		return _fort;
 	}
 
 	/**
 	 * Sets the Fortitude defense.
 	 * 
 	 * @param fort
 	 */
 	public void setFort(Integer fort) {
 		_fort = fort;
 	}
 
 	/**
 	 * Returns a string identifying the statblock.
 	 * 
 	 * @return the handle
 	 */
 	public String getHandle() {
 		if (isValid()) {
 			String handle = getRole().substring(0, 2) + getLevel().toString();
 
 			if (!getRole2().isEmpty()) {
 				handle += getRole2().substring(0, 1);
 			}
 
 			if (isPC()) {
 				return "* " + getName() + " (" + handle + ")";
 			} else {
 				return getName() + " (" + handle + ")";
 			}
 		} else {
 			return "(invalid)";
 		}
 	}
 
 	/**
 	 * Returns immunity.
 	 * 
 	 * @return immunity
 	 */
 	public String getImmunity() {
 		return _immune;
 	}
 
 	/**
 	 * Sets immunity information.
 	 * 
 	 * @param immunity
 	 */
 	public void setImmunity(String immunity) {
 		_immune = immunity;
 	}
 
 	/**
 	 * Returns initiative modifier.
 	 * 
 	 * @return initiative modifier
 	 */
 	public Integer getInit() {
 		return _init;
 	}
 
 	/**
 	 * Sets the initiative bonus.
 	 * 
 	 * @param init
 	 */
 	public void setInit(Integer init) {
 		_init = init;
 	}
 
 	/**
 	 * Returns the INT ability score.
 	 * 
 	 * @return INT
 	 */
 	public Integer getInt() {
 		return _int;
 	}
 
 	/**
 	 * Sets the INT ability score.
 	 * 
 	 * @param intScore
 	 */
 	public void setInt(Integer intScore) {
 		_int = intScore;
 	}
 
 	/**
 	 * Returns languages.
 	 * 
 	 * @return languages
 	 */
 	public String getLanguages() {
 		return _languages;
 	}
 
 	/**
 	 * Sets the list of languages for this statblock.
 	 * 
 	 * @param languages
 	 */
 	public void setLanguages(String languages) {
 		_languages = languages;
 	}
 
 	/**
 	 * Indicates if the character is a leader.
 	 * 
 	 * @return true if the character is a leader
 	 */
 	public Boolean isLeader() {
 		return _leader;
 	}
 
 	/**
 	 * Sets an indicator of the character being a leader.
 	 * 
 	 * @param leader
 	 */
 	public void setLeader(Boolean leader) {
 		_leader = leader;
 	}
 
 	/**
 	 * Returns the character's level.
 	 * 
 	 * @return the level
 	 */
 	public Integer getLevel() {
 		return _level;
 	}
 
 	/**
 	 * Sets the character level.
 	 * 
 	 * @param level
 	 */
 	public void setLevel(Integer level) {
 		_level = level;
 	}
 
 	/**
 	 * Returns a string specifying the level and role information.
 	 * 
 	 * @return the level and role string
 	 */
 	private String getLevelRole() {
 		String levelrole = "Level " + getLevel().toString();
 
 		if (!getRole2().isEmpty()) {
 			levelrole += " " + getRole2();
 		}
 
 		levelrole += " " + getRole();
 
 		if (isLeader()) {
 			levelrole += " (Leader)";
 		}
 
 		return levelrole;
 	}
 
 	/**
 	 * Sets the level and role from a single line.
 	 * 
 	 * @param line
 	 *            the line
 	 */
 	private void setLevelRole(String line) {
 		line = line.toLowerCase().trim();
 
 		setLevel(Integer.valueOf(line.substring(line.indexOf(" ") + 1).replaceAll("[^0-9]", "")));
 
 		if (line.contains("soldier")) {
 			setRole("Soldier");
 		} else if (line.contains("brute")) {
 			setRole("Brute");
 		} else if (line.contains("skirmisher")) {
 			setRole("Skirmisher");
 		} else if (line.contains("lurker")) {
 			setRole("Lurker");
 		} else if (line.contains("artillery")) {
 			setRole("Artillery");
 		} else if (line.contains("controller")) {
 			setRole("Controller");
 		} else if (line.contains("hero")) {
 			setRole("Hero");
 		} else if (line.contains("minion")) {
 			setRole("Minion");
 		} else {
 			setRole("Unspecified");
 		}
 
 		if (line.contains("elite")) {
 			setRole2("Elite");
 		} else if (line.contains("solo")) {
 			setRole2("Solo");
 		} else {
 			setRole2("");
 		}
 
 		setLeader(line.contains("(leader)"));
 	}
 
 	/**
 	 * Returns the maximum HP.
 	 * 
 	 * @return the max HP
 	 */
 	public Integer getMaxHP() {
 		return _maxHP;
 	}
 
 	/**
 	 * Sets the maximum HP.
 	 * 
 	 * @param maxHP
 	 */
 	public void setMaxHP(Integer maxHP) {
 		_maxHP = maxHP;
 	}
 
 	/**
 	 * Indicates if the character is a minion.
 	 * 
 	 * @return true if the max HP is 1
 	 */
 	public Boolean isMinion() {
 		return (getMaxHP() == 1);
 	}
 
 	/**
 	 * Returns the character's name.
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return _name;
 	}
 
 	/**
 	 * Sets the name.
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		_name = name;
 	}
 
 	/**
 	 * Returns stored character notes.
 	 * 
 	 * @return the notes
 	 */
 	public String getNotes() {
 		return getNotesBase().replace("###", "\n");
 	}
 
 	/**
 	 * Sets the character's stored notes.
 	 * 
 	 * @param notes
 	 *            the notes
 	 */
 	public void setNotes(String notes) {
 		setNotesBase(notes.replace("\n", "###"));
 	}
 
 	/**
 	 * Returns the coded notes.
 	 * 
 	 * @return the coded notes
 	 */
 	private String getNotesBase() {
 		return _notesBase;
 	}
 
 	/**
 	 * Sets the character's stored coded notes.
 	 * 
 	 * @param notesBase
 	 *            the coded notes
 	 */
 	private void setNotesBase(String notesBase) {
 		_notesBase = notesBase;
 	}
 
 	/**
 	 * Returns notes in HTML format.
 	 * 
 	 * @return notes
 	 */
 	private String getNotesHTML() {
 		return _notesBase.replace("###", "<br>");
 	}
 
 	/**
 	 * Returns an indicator of the characters PC-ness.
 	 * 
 	 * @return true if the role is "Hero"
 	 */
 	public Boolean isPC() {
 		return (getRole().contentEquals("Hero"));
 	}
 
 	/**
 	 * Returns the power list.
 	 * 
 	 * @return the power list.
 	 */
 	public List<Power> getPowerList() {
 		return _powerList;
 	}
 
 	/**
 	 * Returns the total power points.
 	 * 
 	 * @return the power points
 	 */
 	public Integer getPowerPoints() {
 		return _powerPoints;
 	}
 
 	/**
 	 * Sets the total number of power points.
 	 * 
 	 * @param powerPoints
 	 */
 	public void setPowerPoints(Integer powerPoints) {
 		_powerPoints = powerPoints;
 	}
 
 	/**
 	 * Returns a table of preset effects.
 	 * 
 	 * @return the table of effects
 	 */
 	public Hashtable<String, EffectBase> getPresetEffects() {
 		return _presetEffects;
 	}
 
 	/**
 	 * Returns reflex defense.
 	 * 
 	 * @return reflex
 	 */
 	public Integer getRef() {
 		return _ref;
 	}
 
 	/**
 	 * Sets the Reflex defense.
 	 * 
 	 * @param ref
 	 */
 	public void setRef(Integer ref) {
 		_ref = ref;
 	}
 
 	/**
 	 * Returns the amount and type of regeneration.
 	 * 
 	 * @return the regen
 	 */
 	public String getRegen() {
 		return _regen;
 	}
 
 	/**
 	 * Sets the regeneration amount.
 	 * 
 	 * @param regen
 	 */
 	public void setRegen(String regen) {
 		_regen = regen;
 	}
 
 	/**
 	 * Returns resistance.
 	 * 
 	 * @return resistance
 	 */
 	public String getResistance() {
 		return _resist;
 	}
 
 	/**
 	 * Sets the resistance information.
 	 * 
 	 * @param resist
 	 */
 	public void setResistance(String resist) {
 		_resist = resist;
 	}
 
 	/**
 	 * Returns primary role.
 	 * 
 	 * @return the primary role
 	 */
 	public String getRole() {
 		return _role;
 	}
 
 	/**
 	 * Sets the primary role.
 	 * 
 	 * @param role
 	 */
 	public void setRole(String role) {
 		_role = role;
 	}
 
 	/**
 	 * Returns the secondary role (e.g., minion, elite, solo).
 	 * 
 	 * @return the secondary role
 	 */
 	public String getRole2() {
 		return _role2;
 	}
 
 	/**
 	 * Sets the secondary role for the character.
 	 * 
 	 * @param role2
 	 *            the secondary role
 	 */
 	public void setRole2(String role2) {
 		_role2 = role2;
 	}
 
 	/**
 	 * Returns save bonus.
 	 * 
 	 * @return save bonus
 	 */
 	public Integer getSaveBonus() {
 		return _saveBonus;
 	}
 
 	/**
 	 * Sets the save bonus.
 	 * 
 	 * @param saveBonus
 	 */
 	public void setSaveBonus(Integer saveBonus) {
 		_saveBonus = saveBonus;
 	}
 
 	/**
 	 * Returns senses.
 	 * 
 	 * @return senses
 	 */
 	public String getSenses() {
 		return _senses;
 	}
 
 	/**
 	 * Sets the senses information.
 	 * 
 	 * @param senses
 	 */
 	public void setSenses(String senses) {
 		_senses = senses;
 	}
 
 	/**
 	 * Returns skills.
 	 * 
 	 * @return skills
 	 */
 	public String getSkills() {
 		return _skills;
 	}
 
 	/**
 	 * Sets the list of skills.
 	 * 
 	 * @param skills
 	 */
 	public void setSkills(String skills) {
 		_skills = skills;
 	}
 
 	/**
 	 * Returns source.
 	 * 
 	 * @return source
 	 */
 	public String getSource() {
 		return _source;
 	}
 
 	/**
 	 * Sets the source of this statblock.
 	 * 
 	 * @param source
 	 */
 	public void setSource(String source) {
 		_source = source;
 	}
 
 	/**
 	 * Returns speed.
 	 * 
 	 * @return speed
 	 */
 	public String getSpeed() {
 		return _speed;
 	}
 
 	/**
 	 * Sets the speed information.
 	 * 
 	 * @param speed
 	 */
 	public void setSpeed(String speed) {
 		_speed = speed;
 	}
 
 	/**
 	 * Returns an HTML-formatted version of the statblock.
 	 * 
 	 * @return the HTML statblock
 	 */
 	public String getStatsHTML() {
 		if (getName().isEmpty()) {
 			return "";
 		}
 
 		String value = "";
 		value += "<html><head><style type='text/css'>html {font-family: Arial, Sans-Serif; font-size: 9.5pt}  body {margin: 0px; padding: 0px; text-align:left; font-weight: normal;}  DIV.symbol {font-family: DnD4Attack} DIV.mbwrap {width: 100%; max-width: 570px;}  DIV.ggdark {width: 100%; padding-left:5px; padding-right:5px; padding-top:2px; padding-bottom:2px; background-color: #374b27; min-height: 14px;}  DIV.mbheadleft {float: left;  align: left; color: #ffffff; font-size: 13px;}  DIV.mbheadright {float: right; align: right; color: #ffffff; font-size: 13px;}  DIV.mbsubleft {float: left; align: left; color: #ffffff;}  DIV.mbsubright {float: right; align: right; color: #ffffff;}  DIV.gglt {width: 100%; padding-left:5px; padding-right:5px; padding-top:1px; padding-bottom:1px; background-color: #e1e6c4; min-height: 14px;}  DIV.ggmed {width: 100%; padding-left:5px; padding-right:5px; padding-top:1px; padding-bottom:1px; background-color: #9fa48c; min-height: 14px; }  DIV.ggtype {width: 100%; padding-left:5px; padding-right:5px; padding-top:1px; padding-bottom:1px; background-color: #727c55; min-height: 14px; }  DIV.ggindent {padding-left:20px;} TD.indlt {padding-left: 4px; padding-right: 4px; font-size: 9.5pt; background-color: #e1e6c4;} TD.indmed {padding-left: 4px; padding-right: 4px; font-size: 9.5pt; background-color: #9fa48c;}</style></head><body>";
 		value += "<div class='ggdark'><div class='mbheadleft'><b>" + getName() + "</b></div><div class='mbheadright'><b>"
 				+ getLevelRole() + "</b></div></div>";
 		value += "<div class='ggdark'><div class='mbsubleft'>" + getType() + "</div><div class='mbsubright'>XP " + getXP()
 				+ "</div></div>";
 		value += "<table width='100%' border='0' cellpadding='0' cellspacing='0'>";
 		value += "<tr><td class='indlt'><b>HP</b> ";
 
 		if (getMaxHP() == 1) {
 			value += "1, a missed attack never damages a minion.";
 		} else {
 			value += getMaxHP() + "; <b>Bloodied</b> " + getBloodyHP();
 		}
 		if (isPC()) {
 			value += "; <b>Surge</b> " + getSurgeValue();
 		}
 		if (!_regen.isEmpty()) {
 			value += "<br><b>Regeneration</b> " + getRegen();
 		}
 
 		value += "<br><b>AC</b> " + getAC() + ", <b>Fortitude</b> " + getFort() + ", <b>Reflex</b> " + getRef() + ", <b>Will</b> "
 				+ getWill() + "<br><b>Speed</b> " + getSpeed();
 
 		if (!(getImmunity().isEmpty() && getResistance().isEmpty() && getVulnerability().isEmpty())) {
 			value += "<br>";
 		}
 		if (!getImmunity().isEmpty()) {
 			value += "<b>Immune</b> " + getImmunity();
 		}
 		if (!getImmunity().isEmpty() && !(getResistance().isEmpty() && getVulnerability().isEmpty())) {
 			value += "; ";
 		}
 		if (!getResistance().isEmpty()) {
 			value += "<b>Resist</b> " + getResistance();
 		}
 		if (!getResistance().isEmpty() && !getVulnerability().isEmpty()) {
 			value += "; ";
 		}
 		if (!getVulnerability().isEmpty()) {
 			value += "<b>Vulnerability</b> " + getVulnerability();
 		}
 		if (getSaveBonus() > 0 || getActionPoints() > 0 || getPowerPoints() > 0) {
 			value += "<br>";
 		}
 		if (getSaveBonus() > 0) {
 			value += "<b>Saving Throws</b> " + getSaveBonus().toString();
 		}
 		if (getSaveBonus() > 0 && (getActionPoints() > 0 || getPowerPoints() > 0)) {
 			value += "; ";
 		}
 		if (getActionPoints() > 0) {
 			value += "<b>Action Points</b> " + getActionPoints().toString();
 		}
 		if (getActionPoints() > 0 && getPowerPoints() > 0) {
 			value += "; ";
 		}
 		if (getPowerPoints() > 0) {
 			value += "<b>Power Points</b> " + getPowerPoints().toString();
 		}
 		value += "</td>";
 		value += "<td class='indlt' align='right'>";
 		value += "<b>Initiative</b> ";
 		if (getInit() > 0) {
 			value += "+";
 		}
 		value += getInit().toString() + "<br>";
 		if (!getSenses().isEmpty()) {
 			value += getSenses().replace("Perception", "<b>Perception</b>").replace("Insight", "<b>Insight</b>")
 					.replace(",", "<br>").replace(";", "<br>");
 		}
 		value += "</td></tr></table>";
 
 		Boolean flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.isAura() || pow.getAction().isEmpty()) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Traits</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.getAction().toLowerCase().contains("standard")) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Standard Actions</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.getAction().toLowerCase().contains("move")) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Move Actions</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.getAction().toLowerCase().contains("minor")) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Minor Actions</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.getAction().toLowerCase().contains("free")) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Free Actions</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (pow.getAction().toLowerCase().contains("triggered")) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Triggered Actions</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		flag = false;
 		for (Power pow : getPowerList()) {
 			if (!(pow.getAction().toLowerCase().contains("standard") || pow.getAction().toLowerCase().contains("move")
 					|| pow.getAction().toLowerCase().contains("minor") || pow.getAction().toLowerCase().contains("free")
 					|| pow.getAction().toLowerCase().contains("triggered") || pow.getAction().isEmpty() || pow.getAura() > 0 || pow
 					.getAction().toLowerCase().contains("item"))) {
 				if (!flag) {
 					value += "<div class='ggtype'><div class='mbsubleft'><b>Other Powers</b></div></div>";
 					flag = true;
 				}
 				value += pow.getPowerHTML();
 			}
 		}
 
 		value += "<div class='ggmed'><table width='100%' border='0' cellpadding='0' cellspacing='0'>";
 		if (!getSkills().isEmpty()) {
 			value += "<tr><td colspan='3' class='indmed'><b>Skills</b> " + getSkills() + "</td></tr>";
 		}
 		if (!getFeats().isEmpty()) {
 			value += "<tr><td colspan='3' class='indmed'><b>Feats</b> " + getFeats() + "</td></tr>";
 		}
 
 		if (getStr() > 0) {
 			value += "<tr><td class='indmed'><b>Str</b> " + DnD4eRules.statBonus(getStr()) + "</td>";
 			value += "<td class='indmed'><b>Dex</b> " + DnD4eRules.statBonus(getDex()) + "</td>";
 			value += "<td class='indmed'><b>Wis</b> " + DnD4eRules.statBonus(getWis()) + "</td></tr>";
 			value += "<tr><td class='indmed'><b>Con</b> " + DnD4eRules.statBonus(getCon()) + "</td>";
 			value += "<td class='indmed'><b>Int</b> " + DnD4eRules.statBonus(getInt()) + "</td>";
 			value += "<td class='indmed'><b>Cha</b> " + DnD4eRules.statBonus(getCha()) + "</td></tr>";
 		}
 
 		value += "</table></div>";
 		value += "<div class='gglt'>";
 		if (!getAlignment().isEmpty()) {
 			value += "<b>Alignment</b> " + getAlignment() + " &nbsp;";
 			if (getLanguages().isEmpty()) {
 				value += "<br>";
 			}
 		}
 		if (!getLanguages().isEmpty()) {
 			value += "<b>Languages</b> " + getLanguages() + "<br>";
 		}
 		if (!getEquipment().isEmpty()) {
 			value += "<b>Equipment</b> " + getEquipment() + "<br>";
 		}
 		if (!getSource().isEmpty()) {
 			value += "<b>Source</b> " + getSource() + "<br>";
 		}
 		if (!getNotes().isEmpty()) {
 			value += "<b>Notes:</b><br>";
 			value += getNotesHTML();
 		}
 		value += "</div></body></html>";
 		return value;
 	}
 
 	/**
 	 * Returns the STR ability score.
 	 * 
 	 * @return STR
 	 */
 	public Integer getStr() {
 		return _str;
 	}
 
 	/**
 	 * Sets the STR ability score.
 	 * 
 	 * @param str
 	 */
 	public void setStr(Integer str) {
 		_str = str;
 	}
 
 	/**
 	 * Returns the number of surges.
 	 * 
 	 * @return the surges
 	 */
 	public Integer getSurges() {
 		return _surges;
 	}
 
 	/**
 	 * Sets number of healing surges.
 	 * 
 	 * @param surges
 	 */
 	public void setSurges(Integer surges) {
 		_surges = surges;
 	}
 
 	/**
 	 * Returns the surge value.
 	 * 
 	 * @return the surge value
 	 */
 	public Integer getSurgeValue() {
 		if (getType().contains("Dragonborn")) {
 			return getMaxHP() / 4 + ((getCon() - 10) / 2);
 		} else {
 			return getMaxHP() / 4;
 		}
 	}
 
 	/**
 	 * Returns the character's type and keywords.
 	 * 
 	 * @return the type and keywords
 	 */
 	public String getType() {
 		return _type;
 	}
 
 	/**
 	 * Sets the type.
 	 * 
 	 * @param type
 	 */
 	public void setType(String type) {
 		_type = type;
 	}
 
 	/**
 	 * Indicates if the character is valid.
 	 * 
 	 * @return true if the name and role are non-blank and the level is 1 or
 	 *         higher
 	 */
 	public Boolean isValid() {
 		return (!getName().isEmpty() && getLevel() >= 1 && !getRole().isEmpty());
 	}
 
 	/**
 	 * Returns vulnerability.
 	 * 
 	 * @return vulnerability
 	 */
 	public String getVulnerability() {
 		return _vuln;
 	}
 
 	/**
 	 * Sets the vulnerability information.
 	 * 
 	 * @param vuln
 	 */
 	public void setVulnerability(String vuln) {
 		_vuln = vuln;
 	}
 
 	/**
 	 * Returns will defense.
 	 * 
 	 * @return will
 	 */
 	public Integer getWill() {
 		return _will;
 	}
 
 	/**
 	 * Sets the Will defense.
 	 * 
 	 * @param will
 	 */
 	public void setWill(Integer will) {
 		_will = will;
 	}
 
 	/**
 	 * Returns the WIS ability score.
 	 * 
 	 * @return WIS
 	 */
 	public Integer getWis() {
 		return _wis;
 	}
 
 	/**
 	 * Sets the WIS ability score.
 	 * 
 	 * @param wis
 	 */
 	public void setWis(Integer wis) {
 		_wis = wis;
 	}
 
 	/**
 	 * Returns the character's XP value.
 	 * 
 	 * @return the XP
 	 */
 	public Integer getXP() {
 		return _XP;
 	}
 
 	/**
 	 * Sets the experience value of this statblock.
 	 * 
 	 * @param xp
 	 */
 	public void setXP(Integer xp) {
 		_XP = xp;
 	}
 
 	/**
 	 * Clears all properties of the statblock.
 	 */
 	private void clearAll() {
 		setName("");
 		setRole("");
 		setRole2("");
 		setLeader(false);
 		setType("");
 		setSenses("");
 		setResistance("");
 		setImmunity("");
 		setVulnerability("");
 		setSpeed("");
 		setRegen("");
 		setAlignment("");
 		setSkills("");
 		setLanguages("");
 		setEquipment("");
 		setSource("");
 		setFeats("");
 		setNotes("");
 		setLevel(0);
 		setXP(0);
 		setInit(0);
 		setMaxHP(0);
 		setAC(0);
 		setFort(0);
 		setRef(0);
 		setWill(0);
 		setSaveBonus(0);
 		setActionPoints(0);
 		setPowerPoints(0);
 		setSurges(0);
 		setStr(0);
 		setCon(0);
 		setDex(0);
 		setInt(0);
 		setWis(0);
 		setCha(0);
 		getPowerList().clear();
 		getPresetEffects().clear();
 	}
 
 	/**
 	 * Stores a preset effect for the character.
 	 * 
 	 * @param eff
 	 *            the {@link Effect}
 	 */
 	public void presetEffectAdd(EffectBase eff) {
 		if (eff.isValid() && !getPresetEffects().contains(eff.getEffectBaseID())) {
 			getPresetEffects().put(eff.getEffectBaseID(), eff);
 		}
 	}
 
 	/**
 	 * Writes the statblock to an XML stream.
 	 * 
 	 * @param writer
 	 *            the XML stream
 	 * @throws XMLStreamException
 	 *             from the writer
 	 */
 	public void exportXML(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeStartElement("statblock");
 
 		writer.writeStartElement("name");
 		writer.writeCharacters(getName());
 		writer.writeEndElement();
 
 		writer.writeStartElement("type");
 		writer.writeCharacters(getType());
 		writer.writeEndElement();
 
 		writer.writeStartElement("levelrole");
 		writer.writeCharacters(getLevelRole());
 		writer.writeEndElement();
 
 		writer.writeStartElement("xp");
 		writer.writeCharacters(getXP().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("senses");
 		writer.writeCharacters(getSenses());
 		writer.writeEndElement();
 
 		writer.writeStartElement("speed");
 		writer.writeCharacters(getSpeed());
 		writer.writeEndElement();
 
 		writer.writeStartElement("immune");
 		writer.writeCharacters(getImmunity());
 		writer.writeEndElement();
 
 		writer.writeStartElement("resist");
 		writer.writeCharacters(getResistance());
 		writer.writeEndElement();
 
 		writer.writeStartElement("vuln");
 		writer.writeCharacters(getVulnerability());
 		writer.writeEndElement();
 
 		writer.writeStartElement("regen");
 		writer.writeCharacters(getRegen());
 		writer.writeEndElement();
 
 		writer.writeStartElement("init");
 		writer.writeCharacters(getInit().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("max_hp");
 		writer.writeCharacters(getMaxHP().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("save");
 		writer.writeCharacters(getSaveBonus().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("a_pts");
 		writer.writeCharacters(getActionPoints().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("p_pts");
 		writer.writeCharacters(getPowerPoints().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("surges");
 		writer.writeCharacters(getSurges().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("d_ac");
 		writer.writeCharacters(getAC().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("d_fort");
 		writer.writeCharacters(getFort().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("d_ref");
 		writer.writeCharacters(getRef().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("d_will");
 		writer.writeCharacters(getWill().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("align");
 		writer.writeCharacters(getAlignment());
 		writer.writeEndElement();
 
 		writer.writeStartElement("skills");
 		writer.writeCharacters(getSkills());
 		writer.writeEndElement();
 
 		writer.writeStartElement("lang");
 		writer.writeCharacters(getLanguages());
 		writer.writeEndElement();
 
 		writer.writeStartElement("equip");
 		writer.writeCharacters(getEquipment());
 		writer.writeEndElement();
 
 		writer.writeStartElement("source");
 		writer.writeCharacters(getSource());
 		writer.writeEndElement();
 
 		writer.writeStartElement("feats");
 		writer.writeCharacters(getFeats());
 		writer.writeEndElement();
 
 		writer.writeStartElement("notes");
 		writer.writeCharacters(getNotesBase());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_str");
 		writer.writeCharacters(getStr().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_con");
 		writer.writeCharacters(getCon().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_dex");
 		writer.writeCharacters(getDex().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_int");
 		writer.writeCharacters(getInt().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_wis");
 		writer.writeCharacters(getWis().toString());
 		writer.writeEndElement();
 
 		writer.writeStartElement("s_cha");
 		writer.writeCharacters(getCha().toString());
 		writer.writeEndElement();
 
 		for (Power pow : getPowerList()) {
 			pow.exportXML(writer);
 		}
 
 		for (EffectBase eff : getPresetEffects().values()) {
 			eff.exportXML(writer);
 		}
 
 		writer.writeEndElement();
 	}
 
 	/**
 	 * Imports statblock information from an XML stream.
 	 * 
 	 * @param reader
 	 *            the XML stream
 	 * @return true on success
 	 * @throws XMLStreamException
 	 *             from the reader
 	 */
 	public Boolean importXML(XMLStreamReader reader) throws XMLStreamException {
 		String elementName = "";
 		Power newPower;
 		EffectBase newEffect;
 
 		if (reader.isStartElement() && reader.getName().toString().contentEquals("statblock")) {
 			clearAll();
 
 			while (reader.hasNext()) {
 				reader.next();
 				if (reader.isStartElement()) {
 					if (reader.getName().toString().contentEquals("power")) {
 						newPower = new Power();
 						newPower.importXML(reader);
 						getPowerList().add(newPower);
 					} else if (reader.getName().toString().contentEquals("effectbase")) {
 						newEffect = new EffectBase();
 						newEffect.importXML(reader);
 						presetEffectAdd(newEffect);
 					} else {
 						elementName = reader.getName().toString();
 					}
 				} else if (reader.isCharacters()) {
 					if (elementName.contentEquals("name")) {
 						setName(reader.getText());
 					} else if (elementName.contentEquals("type")) {
 						setType(reader.getText());
 					} else if (elementName.contentEquals("levelrole")) {
 						setLevelRole(reader.getText());
 					} else if (elementName.contentEquals("xp")) {
 						setXP(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("senses")) {
 						setSenses(reader.getText());
 					} else if (elementName.contentEquals("speed")) {
 						setSpeed(reader.getText());
 					} else if (elementName.contentEquals("immune")) {
 						setImmunity(reader.getText());
 					} else if (elementName.contentEquals("resist")) {
 						setResistance(reader.getText());
 					} else if (elementName.contentEquals("vuln")) {
 						setVulnerability(reader.getText());
 					} else if (elementName.contentEquals("regen")) {
 						setRegen(reader.getText());
 					} else if (elementName.contentEquals("init")) {
 						setInit(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("max_hp")) {
 						setMaxHP(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("save")) {
 						setSaveBonus(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("a_pts")) {
 						setActionPoints(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("p_pts")) {
 						setPowerPoints(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("surges")) {
 						setSurges(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("d_ac")) {
 						setAC(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("d_fort")) {
 						setFort(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("d_ref")) {
 						setRef(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("d_will")) {
 						setWill(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("align")) {
 						setAlignment(reader.getText());
 					} else if (elementName.contentEquals("skills")) {
 						setSkills(reader.getText());
 					} else if (elementName.contentEquals("lang")) {
 						setLanguages(reader.getText());
 					} else if (elementName.contentEquals("equip")) {
 						setEquipment(reader.getText());
 					} else if (elementName.contentEquals("source")) {
 						setSource(reader.getText());
 					} else if (elementName.contentEquals("feats")) {
 						setFeats(reader.getText());
 					} else if (elementName.contentEquals("notes")) {
 						setNotes(reader.getText());
 					} else if (elementName.contentEquals("s_str")) {
 						setStr(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("s_con")) {
 						setCon(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("s_dex")) {
 						setDex(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("s_int")) {
 						setInt(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("s_wis")) {
 						setWis(Integer.valueOf(reader.getText()));
 					} else if (elementName.contentEquals("s_cha")) {
 						setCha(Integer.valueOf(reader.getText()));
 					}
 				} else if (reader.isEndElement()) {
 					elementName = "";
 					if (reader.getName().toString().contentEquals("statblock")) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Loads a statblock from a Character Builder dnd4e file.
 	 * 
 	 * @param filename
 	 *            the location of the .dnd4e file
 	 * @return true on success
 	 */
 	public Boolean loadFromCBFile(String filename) {
 		File dnd4e = new File(filename);
 		Node node = null;
 		NodeList nodelist = null;
 
 		if (dnd4e.exists()) {
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 			domFactory.setNamespaceAware(true);
 			try {
 				DocumentBuilder builder = domFactory.newDocumentBuilder();
 				Document doc = builder.parse(dnd4e);
 				XPath xpath = XPathFactory.newInstance().newXPath();
 
 				// name
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/Details/name/text()", doc, XPathConstants.NODE);
 				setName(node.getNodeValue().trim());
 
 				// role
 				setRole("Hero");
 
 				// type
 				String type = "";
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Gender\"]/@name",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					type += node.getNodeValue().trim();
 				}
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Race\"]/@name",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					type += " " + node.getNodeValue().trim();
 				}
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Class\"]/@name",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					type += " " + node.getNodeValue().trim();
 				}
 
 				node = (Node) xpath.evaluate(
 						"/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Power Source\"]/@name", doc,
 						XPathConstants.NODE);
 				if (node != null) {
 					type += " (" + node.getNodeValue().trim();
 				}
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Role\"]/@name",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					type += " " + node.getNodeValue().trim() + ")";
 				}
 
 				setType(type.trim());
 
 				// senses
 				String senses = "";
 				node = (Node) xpath.evaluate(
 						"/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Passive Perception\"]/@value", doc,
 						XPathConstants.NODE);
 				senses += "Perception " + node.getNodeValue().trim();
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Passive Insight\"]/@value",
 						doc, XPathConstants.NODE);
 				senses += "; Insight " + node.getNodeValue().trim();
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Vision\"]/@name",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					senses += "; Vision: " + node.getNodeValue().trim();
 				}
 
 				setSenses(senses);
 
 				// TODO: resistance
 
 				// TODO: immunity
 
 				// TODO: vulnerability
 
 				// speeds
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Speed\"]/@value", doc,
 						XPathConstants.NODE);
 				setSpeed(node.getNodeValue().trim());
 
 				// TODO: regeneration
 
 				// alignment
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/Details/Alignment/text()", doc, XPathConstants.NODE);
 				setAlignment(node.getNodeValue().trim());
 
 				// feats
 				String feats = "";
 				nodelist = (NodeList) xpath.evaluate(
 						"/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Feat\"]/@name", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					feats += nodelist.item(i).getNodeValue().trim() + ", ";
 				}
 				setFeats(feats.replaceAll(", $", ""));
 
 				// skills
 				String skills = "";
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Acrobatics\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += "Acrobatics " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Arcana\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Arcana " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Athletics\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Athletics " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Bluff\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Bluff " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Diplomacy\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Diplomacy " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Dungeoneering\"]/@value",
 						doc, XPathConstants.NODE);
 				skills += ", Dungeoneering " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Endurance\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Endurance " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Heal\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Heal " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"History\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", History " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Insight\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Insight " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Intimidate\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Intimidate " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Nature\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Nature " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Perception\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Perception " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Religion\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Religion " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Stealth\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Stealth " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Streetwise\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Streetwise " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Thievery\"]/@value", doc,
 						XPathConstants.NODE);
 				skills += ", Thievery " + DnD4eRules.integerFormatForPlus(node.getNodeValue().trim());
 
 				setSkills(skills);
 
 				// languages
 				String languages = "";
 				nodelist = (NodeList) xpath.evaluate(
 						"/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Language\"]/@name", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					languages += nodelist.item(i).getNodeValue().trim() + ", ";
 				}
 				setLanguages(languages.replaceAll(", $", ""));
 
 				// equipment
 				String equipment = "";
 				nodelist = (NodeList) xpath.evaluate("/D20Character/CharacterSheet/LootTally/loot[@count > 0]/RulesElement/@name",
 						doc, XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String name = nodelist.item(i).getNodeValue();
 					Node n = (Node) xpath.evaluate("/D20Character/CharacterSheet/LootTally/loot[RulesElement/@name=\"" + name
 							+ "\"]/@count", doc, XPathConstants.NODE);
 					if (Integer.valueOf(n.getNodeValue().trim()) > 0) {
 						equipment += name.trim() + " x" + n.getNodeValue().trim() + ", ";
 					}
 				}
 				setEquipment(equipment.replaceAll(", $", ""));
 
 				// source
 				setSource("WotC Character Builder");
 
 				// notes
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/Details/Notes/text()", doc, XPathConstants.NODE);
 				setNotes(node.getNodeValue().trim());
 
 				// level
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/Details/Level/text()", doc, XPathConstants.NODE);
 				setLevel(Integer.valueOf(node.getNodeValue().trim()));
 
 				// experience points
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/Details/Experience/text()", doc, XPathConstants.NODE);
 				setXP(Integer.valueOf(node.getNodeValue().trim()));
 
 				// initiative
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Initiative\"]/@value", doc,
 						XPathConstants.NODE);
 				setInit(Integer.valueOf(node.getNodeValue().trim()));
 
 				// hit points
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Hit Points\"]/@value", doc,
 						XPathConstants.NODE);
 				setMaxHP(Integer.valueOf(node.getNodeValue().trim()));
 
 				// defenses
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"AC\"]/@value", doc,
 						XPathConstants.NODE);
 				setAC(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Fortitude\"]/@value", doc,
 						XPathConstants.NODE);
 				setFort(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Reflex\"]/@value", doc,
 						XPathConstants.NODE);
 				setRef(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Will\"]/@value", doc,
 						XPathConstants.NODE);
 				setWill(Integer.valueOf(node.getNodeValue().trim()));
 
 				// TODO: save bonus
 
 				// action points
 				setActionPoints(1);
 
 				// power points
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Power Points\"]/@value",
 						doc, XPathConstants.NODE);
 				if (node != null) {
 					setPowerPoints(Integer.valueOf(node.getNodeValue().trim()));
 				}
 
 				// healing surges
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"Healing Surges\"]/@value",
 						doc, XPathConstants.NODE);
 				setSurges((Integer.valueOf(node.getNodeValue().trim())));
 
 				// ability scores
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"str\"]/@value", doc,
 						XPathConstants.NODE);
 				setStr(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"con\"]/@value", doc,
 						XPathConstants.NODE);
 				setCon(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"dex\"]/@value", doc,
 						XPathConstants.NODE);
 				setDex(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"int\"]/@value", doc,
 						XPathConstants.NODE);
 				setInt(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"wis\"]/@value", doc,
 						XPathConstants.NODE);
 				setWis(Integer.valueOf(node.getNodeValue().trim()));
 				node = (Node) xpath.evaluate("/D20Character/CharacterSheet/StatBlock/Stat[alias/@name=\"cha\"]/@value", doc,
 						XPathConstants.NODE);
 				setCha(Integer.valueOf(node.getNodeValue().trim()));
 
 				// powers
 				nodelist = (NodeList) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power/@name", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					Node n = null;
 					Power pow = new Power();
 
 					// name
 					String name = nodelist.item(i).getNodeValue();
 					pow.setName(name.trim());
 
 					// action
 					String action = "";
 					n = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 							+ "\"]/specific[@name=\"Action Type\"]/text()", doc, XPathConstants.NODE);
 					action += n.getNodeValue().toLowerCase().replaceAll(" action", "").trim() + "; ";
 					n = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 							+ "\"]/specific[@name=\"Power Usage\"]/text()", doc, XPathConstants.NODE);
 					action += n.getNodeValue().toLowerCase().trim();
 					pow.setAction(action);
 
 					// description
 					String description = "";
 					NodeList nl = (NodeList) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 							+ "\"]/Weapon", doc, XPathConstants.NODESET);
 					for (int j = 0; j < nl.getLength(); j++) {
 						String weapon = nl.item(j).getAttributes().getNamedItem("name").getNodeValue();
 						Node o = null;
 
 						// attack bonus
 						String atkBonus = "";
 						o = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 								+ "\"]/Weapon/AttackBonus/text()", doc, XPathConstants.NODE);
 						if (o != null) {
 							atkBonus += DnD4eRules.integerFormatForPlus(o.getNodeValue().trim());
 						}
 
 						// attack stat
 						String atkStat = "";
 						o = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 								+ "\"]/Weapon/AttackStat/text()", doc, XPathConstants.NODE);
 						if (o != null) {
 							atkStat += o.getNodeValue().trim().substring(0, 3);
 						}
 
 						// defense
 						String defense = "";
 						o = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 								+ "\"]/Weapon/Defense/text()", doc, XPathConstants.NODE);
 						if (o != null) {
 							defense += o.getNodeValue().trim();
 						}
 
 						// damage
 						String damage = "";
 						o = (Node) xpath.evaluate("/D20Character/CharacterSheet/PowerStats/Power[@name=\"" + name
 								+ "\"]/Weapon/Damage/text()", doc, XPathConstants.NODE);
 						if (o != null) {
 							damage += o.getNodeValue().trim();
 						}
 
 						// compile description
 						description += weapon.trim() + ": ";
 						if (!atkBonus.isEmpty()) {
 							description += atkBonus;
 							if (!atkStat.isEmpty()) {
 								description += " (" + atkStat + ")";
 							}
 						}
 						if (!defense.isEmpty()) {
 							description += " vs. " + defense;
 						}
 						if (!damage.isEmpty()) {
 							description += "; " + damage;
 						}
 						description += "\n";
 					}
 					pow.setDesc(description.trim());
 					getPowerList().add(pow);
 				}
 
 				// power URLs
 				for (Power pow : getPowerList()) {
 					node = (Node) xpath.evaluate(
 							"/D20Character/CharacterSheet/RulesElementTally/RulesElement[@name=\"" + pow.getName()
 									+ "\" and @type=\"Power\"]/@url", doc, XPathConstants.NODE);
 					if (node != null) {
 						pow.setURL(node.getNodeValue().trim().replace("&amp;", "&").replace("aspx", "php")
 								.replace("www.wizards.com/dndinsider/compendium", "localhost/ddi"));
 					}
 				}
 
 				// traits
 				nodelist = (NodeList) xpath.evaluate(
 						"/D20Character/CharacterSheet/RulesElementTally/RulesElement[@type=\"Racial Trait\" "
 								+ "or @type=\"Class Feature\"]", doc, XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					Power pow = new Power();
 					String name = nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue();
 					pow.setName(name.trim());
 
 					node = (Node) xpath.evaluate("/D20Character/CharacterSheet/RulesElementTally/RulesElement[@name=\"" + name
 							+ "\"]/specific[@name=\"Short Description\"]/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						pow.setDesc(node.getNodeValue().trim());
 					} else {
 						pow.setDesc("");
 					}
 
 					getPowerList().add(pow);
 				}
 
 				// magic items
 				nodelist = (NodeList) xpath.evaluate(
 						"/D20Character/CharacterSheet/LootTally/loot[@count>0]/RulesElement[@type=\"Magic Item\"]", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					Power pow = new Power();
 					String name = nodelist.item(i).getAttributes().getNamedItem("name").getNodeValue();
 					pow.setName(name.trim());
 					
					String url = nodelist.item(i).getAttributes().getNamedItem("url").getNodeValue();
					pow.setURL(url.trim().replace("&amp;", "&").replace("aspx", "php")
 							.replace("www.wizards.com/dndinsider/compendium", "localhost/ddi"));
 					
 					pow.setAction("item");
 					
 					getPowerList().add(pow);
 				}
 
 				// preset effects
 				getPresetEffects().clear();
 
 				return true;
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 				return false;
 			} catch (SAXException e) {
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			} catch (XPathExpressionException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Loads a statblock from an Adventure Tools monster file.
 	 * 
 	 * @param filename
 	 *            the location of the .monster file
 	 * @return true on success
 	 */
 	public boolean loadFromMonsterFile(String filename) {
 		File monster = new File(filename);
 		Node node = null;
 		NodeList nodelist = null;
 
 		if (monster.exists()) {
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 			domFactory.setNamespaceAware(true);
 			try {
 				DocumentBuilder builder = domFactory.newDocumentBuilder();
 				Document doc = builder.parse(monster);
 				XPath xpath = XPathFactory.newInstance().newXPath();
 
 				// name
 				node = (Node) xpath.evaluate("/Monster/Name/text()", doc, XPathConstants.NODE);
 				setName(node.getNodeValue());
 
 				// role
 				node = (Node) xpath.evaluate("/Monster/Role/ReferencedObject/Name/text()", doc, XPathConstants.NODE);
 				setRole(node.getNodeValue());
 
 				// role 2 (group role)
 				node = (Node) xpath.evaluate("/Monster/GroupRole/ReferencedObject/Name/text()", doc, XPathConstants.NODE);
 				setRole2(node.getNodeValue().replace("Standard", ""));
 
 				// leader
 				node = (Node) xpath.evaluate("/Monster/IsLeader/text()", doc, XPathConstants.NODE);
 				setLeader(Boolean.valueOf(node.getNodeValue()));
 
 				// type
 				node = (Node) xpath.evaluate("/Monster/Origin/ReferencedObject/Name/text()", doc, XPathConstants.NODE);
 				String typeKeywords = node.getNodeValue();
 				node = (Node) xpath.evaluate("/Monster/Type/ReferencedObject/Name/text()", doc, XPathConstants.NODE);
 				typeKeywords += " " + node.getNodeValue();
 				nodelist = (NodeList) xpath.evaluate("/Monster/Keywords/ObjectReference/ReferencedObject/Name/text()", doc,
 						XPathConstants.NODESET);
 				if (nodelist.getLength() > 0) {
 					typeKeywords += " (";
 				}
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					typeKeywords += nodelist.item(i).getNodeValue().toLowerCase() + ", ";
 				}
 				setType(typeKeywords.replaceAll(", $", ")"));
 
 				// senses
 				nodelist = (NodeList) xpath.evaluate("/Monster/Senses/SenseReference/ReferencedObject/Name/text()", doc,
 						XPathConstants.NODESET);
 				String senses = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String sense = nodelist.item(i).getNodeValue();
 					node = (Node) xpath.evaluate("/Monster/Senses/SenseReference/ReferencedObject[Name=\"" + sense
 							+ "\"]/DefaultRange/text()", doc, XPathConstants.NODE);
 					senses += sense;
 					if (node != null && !node.getNodeValue().contentEquals("0")) {
 						senses += " " + node.getNodeValue();
 					}
 					senses += ", ";
 				}
 				setSenses(senses.replaceAll(", $", ""));
 
 				// resistance
 				nodelist = (NodeList) xpath.evaluate("/Monster/Resistances/CreatureSusceptibility/ReferencedObject/Name/text()",
 						doc, XPathConstants.NODESET);
 				String resists = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String resist = nodelist.item(i).getNodeValue();
 					Node amount = (Node) xpath.evaluate("/Monster/Resistances/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ resist + "\"]/Amount/@FinalValue", doc, XPathConstants.NODE);
 					Node details = (Node) xpath.evaluate("/Monster/Resistances/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ resist + "\"]/Details/text()", doc, XPathConstants.NODE);
 					if (amount != null) {
 						resist += " " + amount.getNodeValue();
 					}
 					if (details != null) {
 						resist += " " + details.getNodeValue();
 					}
 					resists += resist + ", ";
 				}
 				setResistance(resists.replaceAll(", $", ""));
 
 				// immunity
 				nodelist = (NodeList) xpath.evaluate("/Monster/Immunities/CreatureSusceptibility/ReferencedObject/Name/text()",
 						doc, XPathConstants.NODESET);
 				String immuns = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String immun = nodelist.item(i).getNodeValue();
 					Node amount = (Node) xpath.evaluate("/Monster/Immunities/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ immun + "\"]/Amount/@FinalValue", doc, XPathConstants.NODE);
 					Node details = (Node) xpath.evaluate("/Monster/Immunities/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ immun + "\"]/Details/text()", doc, XPathConstants.NODE);
 					if (amount != null) {
 						immun += " " + amount.getNodeValue();
 					}
 					if (details != null) {
 						immun += " " + details.getNodeValue();
 					}
 					immuns += immun + ", ";
 				}
 				setImmunity(immuns.replaceAll(", $", ""));
 
 				// vulnerability
 				nodelist = (NodeList) xpath.evaluate("/Monster/Weaknesses/CreatureSusceptibility/ReferencedObject/Name/text()",
 						doc, XPathConstants.NODESET);
 				String vulns = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String vuln = nodelist.item(i).getNodeValue();
 					Node amount = (Node) xpath.evaluate("/Monster/Weaknesses/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ vuln + "\"]/Amount/@FinalValue", doc, XPathConstants.NODE);
 					Node details = (Node) xpath.evaluate("/Monster/Weaknesses/CreatureSusceptibility[ReferencedObject/Name=\""
 							+ vuln + "\"]/Details/text()", doc, XPathConstants.NODE);
 					if (amount != null) {
 						vuln += " " + amount.getNodeValue();
 					}
 					if (details != null) {
 						vuln += " " + details.getNodeValue();
 					}
 					vulns += vuln + ", ";
 				}
 				setVulnerability(vulns.replaceAll(", $", ""));
 
 				// speeds
 				node = (Node) xpath.evaluate("/Monster/LandSpeed/Speed/@FinalValue", doc, XPathConstants.NODE);
 				String speeds = node.getNodeValue();
 				node = (Node) xpath.evaluate("/Monster/LandSpeed/Details/text()", doc, XPathConstants.NODE);
 				if (node != null) {
 					speeds += " " + node.getNodeValue();
 				}
 				nodelist = (NodeList) xpath.evaluate("/Monster/Speeds/CreatureSpeed/ReferencedObject/Name/text()", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String speed = nodelist.item(i).getNodeValue();
 					Node amount = (Node) xpath.evaluate("/Monster/Speeds/CreatureSpeed[ReferencedObject/Name=\"" + speed
 							+ "\"]/Speed/@FinalValue", doc, XPathConstants.NODE);
 					if (amount != null) {
 						speed += " " + amount.getNodeValue();
 					}
 
 					Node details = (Node) xpath.evaluate("/Monster/Speeds/CreatureSpeed[ReferencedObject/Name=\"" + speed
 							+ "\"]/Details/text()", doc, XPathConstants.NODE);
 					if (details != null) {
 						speed += " " + details.getNodeValue();
 					}
 
 					speeds += ", " + speed;
 				}
 				setSpeed(speeds);
 
 				// regeneration
 				node = (Node) xpath.evaluate("/Monster/Regeneration/Details/text()", doc, XPathConstants.NODE);
 				if (node != null) {
 					setRegen(node.getNodeValue());
 				}
 
 				// alignment
 				node = (Node) xpath.evaluate("/Monster/Alignment/ReferencedObject/Name/text()", doc, XPathConstants.NODE);
 				if (node != null) {
 					setAlignment(node.getNodeValue());
 				}
 
 				// skills
 				nodelist = (NodeList) xpath.evaluate("/Monster/Skills/Values/SkillNumber/Name/text()", doc, XPathConstants.NODESET);
 				String skills = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String skill = nodelist.item(i).getNodeValue();
 					Node bonus = (Node) xpath.evaluate("/Monster/Skills/Values/SkillNumber[Name=\"" + skill + "\"]/@FinalValue",
 							doc, XPathConstants.NODE);
 					skills += skill + " +" + bonus.getNodeValue() + ", ";
 				}
 				setSkills(skills.replaceAll(", $", ""));
 
 				// languages
 				nodelist = (NodeList) xpath.evaluate("/Monster/Languages/ObjectReference/ReferencedObject/Name/text()", doc,
 						XPathConstants.NODESET);
 				String langs = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					langs += nodelist.item(i).getNodeValue() + ", ";
 				}
 				setLanguages(langs.replaceAll(", $", ""));
 
 				// equipment
 				nodelist = (NodeList) xpath.evaluate("/Monster/Items/ItemAndQuantity/Item/ReferencedObject/Name/text()", doc,
 						XPathConstants.NODESET);
 				String equips = "";
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String equip = nodelist.item(i).getNodeValue();
 					Node quantity = (Node) xpath.evaluate("/Monster/Items/ItemAndQuantity[Item/ReferencedObject/Name=\"" + equip
 							+ "\"]/Quantity/text()", doc, XPathConstants.NODE);
 					equips += equip + " x" + quantity.getNodeValue() + ", ";
 				}
 				setEquipment(equips.replaceAll(", $", ""));
 
 				// source
 				setSource("Adventure Tools Monster Builder");
 
 				// notes
 				node = (Node) xpath.evaluate("/Monster/Tactics/text()", doc, XPathConstants.NODE);
 				if (node != null) {
 					setNotes(node.getNodeValue());
 				}
 
 				// level
 				node = (Node) xpath.evaluate("/Monster/Level/text()", doc, XPathConstants.NODE);
 				setLevel(Integer.valueOf(node.getNodeValue()));
 
 				// experience points
 				node = (Node) xpath.evaluate("/Monster/Experience/@FinalValue", doc, XPathConstants.NODE);
 				setXP(Integer.valueOf(node.getNodeValue()));
 
 				// initiative
 				node = (Node) xpath.evaluate("/Monster/Initiative/@FinalValue", doc, XPathConstants.NODE);
 				setInit(Integer.valueOf(node.getNodeValue()));
 
 				// hit points
 				node = (Node) xpath.evaluate("/Monster/HitPoints/@FinalValue", doc, XPathConstants.NODE);
 				setMaxHP(Integer.valueOf(node.getNodeValue()));
 
 				// defenses
 				nodelist = (NodeList) xpath.evaluate("/Monster/Defenses/Values/SimpleAdjustableNumber/Name/text()", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String defense = nodelist.item(i).getNodeValue();
 					Node value = (Node) xpath.evaluate("/Monster/Defenses/Values/SimpleAdjustableNumber[Name=\"" + defense
 							+ "\"]/@FinalValue", doc, XPathConstants.NODE);
 					if (defense.contentEquals("AC")) {
 						setAC(Integer.valueOf(value.getNodeValue()));
 					} else if (defense.contentEquals("Fortitude")) {
 						setFort(Integer.valueOf(value.getNodeValue()));
 					} else if (defense.contentEquals("Reflex")) {
 						setRef(Integer.valueOf(value.getNodeValue()));
 					} else if (defense.contentEquals("Will")) {
 						setWill(Integer.valueOf(value.getNodeValue()));
 					}
 				}
 
 				// save bonus
 				node = (Node) xpath.evaluate("/Monster/SavingThrows/MonsterSavingThrow/@FinalValue", doc, XPathConstants.NODE);
 				if (node != null) {
 					setSaveBonus(Integer.valueOf(node.getNodeValue()));
 				} else {
 					setSaveBonus(0);
 				}
 
 				// action points
 				node = (Node) xpath.evaluate("/Monster/ActionPoints/@FinalValue", doc, XPathConstants.NODE);
 				if (node != null) {
 					setActionPoints(Integer.valueOf(node.getNodeValue()));
 				} else {
 					setActionPoints(0);
 				}
 
 				// power points
 				setPowerPoints(0);
 
 				// healing surges
 				if (getRole2().contentEquals("Solo")) {
 					setSurges(2);
 				} else if (getRole2().contentEquals("Elite")) {
 					setSurges(1);
 				} else {
 					setSurges(0);
 				}
 
 				// ability scores
 				nodelist = (NodeList) xpath.evaluate("/Monster/AbilityScores/Values/AbilityScoreNumber/Name/text()", doc,
 						XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					String ability = nodelist.item(i).getNodeValue();
 					Node score = (Node) xpath.evaluate("/Monster/AbilityScores/Values/AbilityScoreNumber[Name=\"" + ability
 							+ "\"]/@FinalValue", doc, XPathConstants.NODE);
 					if (ability.contentEquals("Strength")) {
 						setStr(Integer.valueOf(score.getNodeValue()));
 					} else if (ability.contentEquals("Constitution")) {
 						setCon(Integer.valueOf(score.getNodeValue()));
 					} else if (ability.contentEquals("Dexterity")) {
 						setDex(Integer.valueOf(score.getNodeValue()));
 					} else if (ability.contentEquals("Intelligence")) {
 						setInt(Integer.valueOf(score.getNodeValue()));
 					} else if (ability.contentEquals("Wisdom")) {
 						setWis(Integer.valueOf(score.getNodeValue()));
 					} else if (ability.contentEquals("Charisma")) {
 						setCha(Integer.valueOf(score.getNodeValue()));
 					}
 				}
 
 				// powers
 				getPowerList().clear();
 				nodelist = (NodeList) xpath.evaluate("/Monster/Powers/MonsterPower/Name/text()", doc, XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					Power pow = new Power();
 
 					// power name
 					String name = nodelist.item(i).getNodeValue();
 					pow.setName(name);
 					String base = "/Monster/Powers/MonsterPower[Name=\"" + name + "\"]";
 
 					// power type
 					Boolean basic = false;
 					node = (Node) xpath.evaluate(base + "/IsBasic/text()", doc, XPathConstants.NODE);
 					basic = Boolean.valueOf(node.getNodeValue());
 					node = (Node) xpath.evaluate(base + "/Type/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						if (basic) {
 							pow.setType("basic " + node.getNodeValue());
 						} else {
 							pow.setType(node.getNodeValue());
 						}
 					}
 
 					// power action
 					String action = "";
 					node = (Node) xpath.evaluate(base + "/Action/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						action += node.getNodeValue().toLowerCase() + "; ";
 					}
 					node = (Node) xpath.evaluate(base + "/Usage/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						action += node.getNodeValue().toLowerCase();
 					}
 					node = (Node) xpath.evaluate(base + "/UsageDetails/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						action += " " + node.getNodeValue();
 					}
 					pow.setAction(action);
 
 					// power keywords
 					String keywords = "";
 					NodeList nl = (NodeList) xpath.evaluate(base + "/Keywords/ObjectReference/ReferencedObject/Name/text()", doc,
 							XPathConstants.NODESET);
 					if (nl != null) {
 						for (int j = 0; j < nl.getLength(); j++) {
 							keywords += nl.item(j).getNodeValue() + ", ";
 						}
 						pow.setKeywords(keywords.replaceAll(", $", ""));
 					}
 
 					// power description
 					String desc = "";
 
 					// requirements
 					node = (Node) xpath.evaluate(base + "/Requirements/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						desc += "Requirements: " + node.getNodeValue() + "\n";
 					}
 
 					// trigger
 					node = (Node) xpath.evaluate(base + "/Trigger/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						desc += "Trigger: " + node.getNodeValue() + "\n";
 					}
 					desc += parseMonsterAttack(xpath, doc, base + "/Attacks/MonsterAttack");
 					pow.setDesc(desc);
 
 					// add to list
 					getPowerList().add(pow);
 				}
 
 				// traits
 				nodelist = (NodeList) xpath.evaluate("/Monster/Powers/MonsterTrait/Name/text()", doc, XPathConstants.NODESET);
 				for (int i = 0; i < nodelist.getLength(); i++) {
 					Power pow = new Power();
 
 					// trait name
 					String name = nodelist.item(i).getNodeValue();
 					pow.setName(name);
 					String base = "/Monster/Powers/MonsterTrait[Name=\"" + name + "\"]";
 
 					// trait range (aura)
 					node = (Node) xpath.evaluate(base + "/Range/@FinalValue", doc, XPathConstants.NODE);
 					if (node != null) {
 						pow.setAura(Integer.valueOf(node.getNodeValue()));
 					}
 
 					// trait keywords
 					String keywords = "";
 					NodeList nl = (NodeList) xpath.evaluate(base + "/Keywords/ObjectReference/ReferencedObject/Name/text()", doc,
 							XPathConstants.NODESET);
 					if (nl != null) {
 						for (int j = 0; j < nl.getLength(); j++) {
 							keywords += nl.item(j).getNodeValue().toLowerCase() + ", ";
 						}
 						pow.setKeywords(keywords.replaceAll(", $", ""));
 					}
 
 					// trait description
 					String desc = "";
 					node = (Node) xpath.evaluate(base + "/Range/Details/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						desc += "Aura: " + node.getNodeValue() + "\n";
 					}
 					node = (Node) xpath.evaluate(base + "/Details/text()", doc, XPathConstants.NODE);
 					if (node != null) {
 						desc += node.getNodeValue() + "\n";
 					}
 					pow.setDesc(desc.substring(0, 1).toUpperCase() + desc.substring(1));
 
 					// add to list
 					getPowerList().add(pow);
 				}
 
 				// preset effects
 				getPresetEffects().clear();
 				return true;
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 				return false;
 			} catch (SAXException e) {
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			} catch (XPathExpressionException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a parsed power attack.
 	 * 
 	 * @param xpath
 	 *            the XPath to use for queries
 	 * @param doc
 	 *            the document against which to query
 	 * @param base
 	 *            the base XML path queries are executed under
 	 * @return the parsed power attack
 	 * @throws XPathExpressionException
 	 *             from the XPath
 	 */
 	private String parseMonsterAttack(XPath xpath, Document doc, String base) throws XPathExpressionException {
 		Node n;
 
 		// general attack description
 		String genDesc = "";
 		n = (Node) xpath.evaluate(base + "/Description/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			genDesc = n.getNodeValue();
 		}
 
 		// range
 		String range = "";
 		n = (Node) xpath.evaluate(base + "/Range/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			range = n.getNodeValue();
 		}
 
 		// targets
 		String targets = "";
 		n = (Node) xpath.evaluate(base + "/Targets/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			targets = n.getNodeValue();
 		}
 
 		// hit/miss/effect
 		String hit = parseMonsterEffect(xpath, doc, base + "/Hit");
 		String miss = parseMonsterEffect(xpath, doc, base + "/Miss");
 		String effect = parseMonsterEffect(xpath, doc, base + "/Effect");
 
 		String atkDesc = "";
 		n = (Node) xpath.evaluate(base + "/AttackBonuses/MonsterPowerAttackNumber/Defense/ReferencedObject/DefenseName/text()",
 				doc, XPathConstants.NODE);
 		if (n != null) {
 			Node bonus = (Node) xpath.evaluate(base + "/AttackBonuses/MonsterPowerAttackNumber/@FinalValue", doc,
 					XPathConstants.NODE);
 			if (bonus != null) {
 				atkDesc += "+" + bonus.getNodeValue() + " vs. " + n.getNodeValue();
 			}
 		}
 
 		String desc = "";
 
 		if (!genDesc.isEmpty()) {
 			desc += genDesc + "\n";
 		}
 
 		if (!atkDesc.isEmpty()) {
 			desc += "Attack: " + range;
 			if (!targets.isEmpty()) {
 				desc += " (" + targets + "); ";
 			}
 			desc += atkDesc + "\n";
 		}
 
 		if (!atkDesc.isEmpty() && !hit.trim().isEmpty()) {
 			desc += "Hit: " + hit;
 		}
 		if (!miss.trim().isEmpty()) {
 			desc += "Miss: " + miss;
 		}
 		if (!effect.trim().isEmpty()) {
 			desc += "Effect: ";
 			if (atkDesc.isEmpty()) {
 				desc += range;
 				if (!targets.isEmpty()) {
 					desc += " (" + targets + "); ";
 				}
 			}
 			desc += effect;
 		}
 
 		return desc.trim();
 	}
 
 	/**
 	 * Returns a parsed power effect.
 	 * 
 	 * @param xpath
 	 *            the XPath to use for queries
 	 * @param doc
 	 *            the document against which to query
 	 * @param base
 	 *            the base XML path queries are executed under
 	 * @return the parsed power effect
 	 * @throws XPathExpressionException
 	 *             from the XPath
 	 */
 	private String parseMonsterEffect(XPath xpath, Document doc, String base) throws XPathExpressionException {
 		String aftereffect = "";
 		String sustain = "";
 		String damage = "";
 		String subattack = "";
 		String failedsaves = "";
 		String special = "";
 
 		Node n;
 		NodeList nl;
 
 		// aftereffect
 		n = (Node) xpath.evaluate(base + "/Aftereffects/MonsterAttackEntry", doc, XPathConstants.NODE);
 		if (n != null) {
 			aftereffect += "Aftereffect: \n";
 			aftereffect += "\t"
 					+ parseMonsterEffect(xpath, doc, base + "/Aftereffects/MonsterAttackEntry").replaceAll("\n", "\n\t");
 		}
 
 		// sustain
 		n = (Node) xpath.evaluate(base + "/Aftereffects/MonsterSustainEffect/Action/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			sustain += "Sustain " + n.getNodeValue() + ": ";
 			sustain += parseMonsterAttack(xpath, doc, base + "/Aftereffects/MonsterSustainEffect");
 		}
 
 		// damage
 		n = (Node) xpath.evaluate(base + "/Description/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			Node dmg = (Node) xpath.evaluate(base + "/Damage/Expression/text()", doc, XPathConstants.NODE);
 			if (dmg != null) {
 				damage += dmg.getNodeValue();
 			}
 			damage += " " + n.getNodeValue();
 		}
 
 		// sub-attack
 		n = (Node) xpath.evaluate(base + "/Attacks/MonsterAttack", doc, XPathConstants.NODE);
 		if (n != null) {
 			subattack += "\t" + parseMonsterAttack(xpath, doc, base + "/Attacks/MonsterAttack").replaceAll("\n", "\n\t");
 			subattack.trim();
 		}
 
 		// failed saving throws
 		nl = (NodeList) xpath.evaluate(base + "/FailedSavingThrows/MonsterAttackEntry/Name/text()", doc, XPathConstants.NODESET);
 		for (int i = 0; i < nl.getLength(); i++) {
 			String name = nl.item(i).getNodeValue();
 			failedsaves += name + ": "
 					+ parseMonsterEffect(xpath, doc, base + "/FailedSavingThrows/MonsterAttackEntry[Name=\"" + name + "\"]").trim()
 					+ ". ";
 		}
 
 		// special
 		n = (Node) xpath.evaluate(base + "/Special/text()", doc, XPathConstants.NODE);
 		if (n != null) {
 			special += "Special: " + n.getNodeValue();
 		}
 
 		// compile information
 		String desc = "";
 		if (!damage.trim().isEmpty()) {
 			desc += damage.trim();
 		}
 		if (!failedsaves.trim().isEmpty()) {
 			desc += "; " + failedsaves.trim();
 		}
 		desc += "\n";
 		if (!aftereffect.trim().isEmpty()) {
 			desc += aftereffect.trim() + "\n";
 		}
 		if (!subattack.trim().isEmpty()) {
 			desc += subattack + "\n";
 		}
 		if (!sustain.trim().isEmpty()) {
 			desc += sustain.trim() + "\n";
 		}
 		if (!special.trim().isEmpty()) {
 			desc += special.trim() + "\n";
 		}
 
 		return desc;
 	}
 }
