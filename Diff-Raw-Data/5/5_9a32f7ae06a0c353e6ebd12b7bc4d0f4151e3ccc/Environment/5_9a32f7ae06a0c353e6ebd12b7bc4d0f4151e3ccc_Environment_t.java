 package models;
 
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.TreeSet;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import models.json.EnvironmentChange;
 import models.json.Subsystem;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 @Entity
 public class Environment extends Model implements Comparable<Environment> {
 	
 	private static Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7798106921949909680L;
 
 	@Id
 	public Long id;
 
 	@Constraints.Required
 	@Column(name="envDate")
 	public Date date;
 
 	@Constraints.Required
 	public String name;
 
 	@Constraints.Required
 	public String okapy;
 
 	@Constraints.Required
 	public String home;
 
 	@Constraints.Required
 	public String karmaVersion;
 
 	@Constraints.Required
	public String karmaBuildNumber = "";
 
 	@Constraints.Required
 	public String referenceDateOfComputation;
 	
 	@Constraints.Required
 	public String jsonVersions;
 
 	@Constraints.Required
 	public String jsonChangeBefore;
 
 	@Constraints.Required
 	public String jsonChangeAfter;
 
 	public String description;
 
 	@Constraints.Required
 	public Integer levelOfChange = 0;
 
 
 	public Environment(String dateS, String message) throws WrongMessageFormatException, ParseException {
 		String[] split = message.split("[|]");
 		
 //		Logger.debug("-->"+split.length);
 //		for (int i = 0; i < split.length; i++) {
 //			Logger.debug(i+" "+split[i]);
 //		}
 		
 		if (split.length <= 5) {
 			throw new WrongMessageFormatException("wrong message : '"+message+"'");
 		}
 		
 		this.name = split[0];
 		this.okapy = split[1];
 		this.home = split[2];
 		this.referenceDateOfComputation = split[3];
 		
 		Set<Subsystem> subSystems = new TreeSet<Subsystem>();
 		
 		for (int i = 4; i < split.length; i++) {
 			String subsyst = split[i].replaceAll("-.*", "");
 			String version = split[i].replaceFirst("[^-]*-", "").replaceAll(":.*", "");
 			String build = split[i].replaceAll(".*:", "").replaceAll("[^-]*-", "");
 			if (!build.matches("[0-9]*") || subsyst.equals("DATA")) {
 				build = "";
 			}
 			Subsystem s = new Subsystem(subsyst, version, build);
 			subSystems.add(s);
 			
 			if (subsyst.equals("FULL")) {
 				this.karmaVersion = version;
 				this.karmaBuildNumber = build;
 			}
 			//System.out.println(subsyst+" "+version+" "+build);
 		}
 
 		this.jsonVersions = gson.toJson(subSystems);
 		//Logger.debug(jsonVersions);
 		
 		date = getDate(dateS);
 
 	}
 	
 	/**
 	 * Check if environment has changed
 	 * @return
 	 */
 	public boolean hasChanged() {
 		Environment env = getLast(name);
 		return ((env == null) || (compareTo(env) != 0));
 	}
 	
 	@Override
 	public void save() {
 		
 		Environment before = getLastBefore(name, date);
 		Set<EnvironmentChange> changesBefore = calculateChanges(this, before);
 		jsonChangeBefore = gson.toJson(changesBefore);
 		Environment after = getFirstAfter(name, date);
 		Set<EnvironmentChange> changesAfter = calculateChanges(this, after);
 		jsonChangeAfter = gson.toJson(changesAfter);
 		
 		
 		
 		boolean isChanged = ((_ebean_intercept.getChangedProps() != null) && (_ebean_intercept.getChangedProps().size() != 0));
 		boolean isChangedBefore = isChanged && _ebean_intercept.getChangedProps().contains("jsonChangeBefore");
 		boolean isChangedAfter = isChanged && _ebean_intercept.getChangedProps().contains("jsonChangeAfter");
 		boolean isNew = (id == null);
 
 		
 		
 		super.save();
 		
 		if ((isNew || isChangedBefore) && (before!= null)) {
 			before.save();
 		}
 		if ((isNew || isChangedAfter) && (after!= null)) {
 			after.save();
 		}
 		
 	}
 	
 	private Set<EnvironmentChange> calculateChanges(Environment env1, Environment env2) {
 		TreeSet<EnvironmentChange> changes = new TreeSet<EnvironmentChange>();
 		
 		if ((env1 == null) || (env2 == null)) {
 			return changes;
 		}
 		
 		if (!env1.okapy.equalsIgnoreCase(env2.okapy)) {
 			changes.add(new EnvironmentChange("Okapy", env1.okapy, "", env2.okapy, ""));
 		}
 		env1.referenceDateOfComputation = (env1.referenceDateOfComputation == null ? "" : env1.referenceDateOfComputation);
 		env2.referenceDateOfComputation = (env2.referenceDateOfComputation == null ? "" : env2.referenceDateOfComputation);
 		if (!env1.referenceDateOfComputation.equalsIgnoreCase(env2.referenceDateOfComputation)) {
 			changes.add(new EnvironmentChange("Ref. date", env1.referenceDateOfComputation, "", env2.referenceDateOfComputation, ""));
 		}
 		
 		Iterator<Subsystem> env1Subsystems = env1.getSubsystems().iterator();
 		Iterator<Subsystem> env2Subsystems = env2.getSubsystems().iterator();
 		Subsystem s1 = null;
 		Subsystem s2 = null;
 		while (true) {
 			if ((s1 == null) && (env1Subsystems.hasNext())) {
 				s1 = env1Subsystems.next();
 			}
 			if ((s2 == null) && (env2Subsystems.hasNext())) {
 				s2 = env2Subsystems.next();
 			}
 			if ((s1 == null) && (s2 == null)) {
 				break;
 			} else if ((s1 == null) || (s1.name.compareTo((s2==null?"zzzzzzzzzzz":s2.name)) > 0)) {
 				changes.add(new EnvironmentChange(s2.name, "", "", s2.version, s2.build));
 				s2 = null;
 			} else if ((s2 == null) || (s2.name.compareTo((s1==null?"zzzzzzzzzzz":s1.name))  > 0)) {
 				changes.add(new EnvironmentChange(s1.name, s1.version, s1.build, "", ""));
 				s1 = null;
 			} else {
 				if (s1.compareTo(s2) != 0) {
 					changes.add(new EnvironmentChange(s1.name, s1.version, s1.build, s2.version, s2.build));
 				}
 				s1 = null;
 				s2 = null;
 			}
 		}
 
 		return changes;
 	}
 
 	public static SimpleDateFormat[] dfs = {
 			// new SimpleDateFormat(Messages.get("date.format")),
 			new SimpleDateFormat("yy/MM/dd HH:mm:ss"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), new SimpleDateFormat("yyyy/MM/dd HH:mm:ss") };
 
 	/**
 	 * Get the date from the String
 	 * 
 	 * @param dateS
 	 * @return
 	 * @throws ParseException
 	 */
 	public static Date getDate(String dateS) throws ParseException {
 
 		Date date = null;
 		//Logger.debug("DATE : '"+dateS+"'");
 		for (int i = 0; (date == null) && (i < dfs.length); i++) {
 			try {
 				dfs[i].setTimeZone(TimeZone.getTimeZone("GMT"));
 				date = dfs[i].parse(dateS);
 			} catch (ParseException e) {
 			}
 		}
 
 		if (date == null) {
 			System.err.println("Format de date inconnue : '" + dateS + "'");
 		}
 
 		return date;
 	}
 
 	
 	@Override
 	public int compareTo(Environment o) {
 		
 		int ret = name.compareTo(o.name);
 		
 		if (ret == 0) {
 			ret = (referenceDateOfComputation==null ? "" : referenceDateOfComputation).compareTo((o.referenceDateOfComputation == null ? "" : o.referenceDateOfComputation));
 		}
 		if (ret == 0) {
 			ret = karmaVersion.compareTo(o.karmaVersion);
 		}
 		if (ret == 0) {
			ret = (karmaBuildNumber==null ? "" : karmaBuildNumber).compareTo((o.karmaBuildNumber==null ? "" : o.karmaBuildNumber));
 		}
 		if (ret == 0) {
 			ret = home.compareTo(o.home);
 		}
 		if (ret == 0) {
 			ret = okapy.compareTo(o.okapy);
 		}
 		if (ret == 0) {
 			ret = (jsonVersions== null ? "" : jsonVersions).compareTo((o.jsonVersions== null ? "" : o.jsonVersions));
 		}
 
 		return ret;
 	}
 
 	public Set<Subsystem> getSubsystems() {
 		Type collectionType = new com.google.gson.reflect.TypeToken<Set<Subsystem>>(){}.getType();
 
 		Set<Subsystem> ret = gson.fromJson(jsonVersions, collectionType);
 		if (ret == null) {
 			return new TreeSet<Subsystem>();
 		} else {
 			return ret;
 		}
 	}
 	
 	public Set<EnvironmentChange> getChangeBefore() {
 		Type collectionType = new com.google.gson.reflect.TypeToken<Set<EnvironmentChange>>(){}.getType();
 
 		Set<EnvironmentChange> ret = gson.fromJson(jsonChangeBefore, collectionType);
 		if (ret == null) {
 			return new TreeSet<EnvironmentChange>();
 		} else {
 			return ret;
 		}
 	}
 	
 	/** 
 	 * Get last environment version
 	 * @param name the name
 	 * @return the last environment for the name
 	 */
 	public static Environment getLast(String aName) {
 		return find.setMaxRows(1).where().eq("name", aName).orderBy("envdate desc").findUnique();
 	}
 	
 	/** 
 	 * Get last environment version before a date
 	 * @param name the name
 	 * @param name the date
 	 * @return the last environment before a date
 	 */
 	public static Environment getLastBefore(String aName, Date date) {
 		return find.setMaxRows(1).where().eq("name", aName).lt("envdate", date).orderBy("envdate desc").findUnique();
 	}
 	
 	/** 
 	 * Get first environment version after a date
 	 * @param name the name
 	 * @param name the date
 	 * @return the first environment after a date
 	 */
 	public static Environment getFirstAfter(String aName, Date date) {
 		return find.setMaxRows(1).where().eq("name", aName).gt("envdate", date).orderBy("envdate asc").findUnique();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	// NOTE: play framework was bugging out without this method even though it's
 	// supposed to be automatic
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * finder
 	 */
 	public static Finder<Long, Environment> find = new Finder<Long, Environment>(Long.class, Environment.class);
 
 	
 
 
 }
