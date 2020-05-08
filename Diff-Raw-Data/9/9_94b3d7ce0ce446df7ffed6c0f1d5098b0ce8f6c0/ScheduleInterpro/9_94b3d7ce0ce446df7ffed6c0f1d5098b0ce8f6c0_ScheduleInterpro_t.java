 package nl.surfsara.wur.interpro;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.UUID;
 
 import joptsimple.OptionSet;
 import nl.vu.psy.rite.operations.Recipe;
 import nl.vu.psy.rite.operations.Step;
 import nl.vu.psy.rite.operations.implementations.fileresolution.CheckFileExistsOperation;
 import nl.vu.psy.rite.operations.implementations.fileresolution.CopyInMongoFile;
 import nl.vu.psy.rite.operations.implementations.fileresolution.CopyOutMongoFile;
 import nl.vu.psy.rite.operations.implementations.shell.RunBashScriptOperation;
 import nl.vu.psy.rite.persistence.mongo.MongoRecipeStore;
 
 import org.bson.types.ObjectId;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 
 public class ScheduleInterpro {
 	private static String PROPERTIES = "scheduleinterpro.properties";
 
 	public enum PropertyKeys {
 		CLIENTID("clientid", ""), RHOST("relic.hostname", ""), RPORT("relic.port", ""), RDBNAME("relic.dbname", ""), RAUTH("relic.auth", "false"), RUSER("relic.user", ""), RPASS("relic.pass", ""), JHOST("rite.hostname", ""), JPORT("rite.port", ""), JDBNAME("rite.dbname", ""), JAUTH("rite.auth", "false"), JUSER("rite.user", ""), JPASS("rite.pass", "");
 
 		private final String key;
 		private final String defaultValue;
 
 		private PropertyKeys(String key, String defaultValue) {
 			this.key = key;
 			this.defaultValue = defaultValue;
 		}
 
 		public String getDefaultValue() {
 			if (this == CLIENTID) {
 				return UUID.randomUUID().toString();
 			}
 			return defaultValue;
 		}
 
 		public String getKey() {
 			return key;
 		}
 
 		public String getProperty(Properties properties) {
 			return properties.getProperty(this.getKey(), this.getDefaultValue());
 		}
 
 	}
 
 	private static Properties properties;
 	private static String version;
 
 	public static void main(String[] args) {
 		ScheduleInterproOptionParser optionParser = new ScheduleInterproOptionParser();
 		OptionSet optionsInEffect = null;
 		version = ScheduleInterpro.class.getPackage().getImplementationVersion();
 		StringTokenizer st = null;
 		if (version != null) {
 			st = new StringTokenizer(version, "_");
 		} else {
 			st = new StringTokenizer("");
 			version = "undetermined";
 		}
 		System.out.println();
 		System.out.println("+-++-++-++-++-++-++-++-++-++-++-++-++-++-++-++-+");
 		System.out.println("|S||c||h||e||d||u||l||e||I||n||t||e||r||p||r||o|");
 		System.out.println("+-++-++-++-++-++-++-++-++-++-++-++-++-++-++-++-+");
 		if (version != null && st.countTokens() >= 2) {
 			System.out.print("version: " + st.nextToken() + " build " + st.nextToken() + "\n");
 		} else {
 			System.out.print("version: " + version + "\n");
 		}
 		System.out.println();
 		System.out.println("-------------------------------------------------------------------------------");
 		System.out.println("Using the following application properties: ");
 		properties = new Properties();
 		try {
 			properties.load(new FileInputStream(new File(PROPERTIES)));
 		} catch (Exception e1) {
 			// Absorb
 			System.out.println("Could not read properties files. Assuming programmed default values for client settings.");
 		}
 		if (isNullOrEmpty(properties.getProperty(PropertyKeys.CLIENTID.key))) {
 			properties.setProperty(PropertyKeys.CLIENTID.key, PropertyKeys.CLIENTID.getDefaultValue());
 			try {
 				properties.store(new FileOutputStream(new File(PROPERTIES)), "Added auto-generated clientid.");
 			} catch (Exception e) {
 				showErrorAndExit(optionParser, e);
 			}
 		}
 		for (PropertyKeys p : PropertyKeys.values()) {
 			System.out.println("\t " + p.getKey() + ": " + getProperty(p));
 		}
 		System.out.println("-------------------------------------------------------------------------------");
 
 		try {
 			optionsInEffect = optionParser.parse(args);
 			boolean showUsage = false;
 			if (optionsInEffect.has(optionParser.help) || args.length == 0) {
 				showUsage = true;
 			}
 			if (optionsInEffect.has(optionParser.get) && optionsInEffect.has(optionParser.projectId)) {
 				String projectid = optionsInEffect.valueOf(optionParser.projectId);
 				System.out.println("Downloading output files project: " + projectid + "...");
 				String host = getProperty(PropertyKeys.JHOST);
 				int port = Integer.parseInt(getProperty(PropertyKeys.JPORT));
 				String dbName = getProperty(PropertyKeys.JDBNAME);
 				Mongo mongo = new Mongo(host, port);
 				DB db = mongo.getDB(dbName);
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					db.authenticate(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS).toCharArray());
 				}
 				GridFS gfs = new GridFS(db);
 				//String regex = ".*" + projectid + "\\].*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				String regex = ".*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				BasicDBObject q = new BasicDBObject();
 				q.put("filename", new BasicDBObject("$regex", regex));
 				List<GridFSDBFile> find = gfs.find(q);
 				for (GridFSDBFile file : find) {
 					// Check recipe is complete and not failed
 					System.out.println("Downloading: " + file.getFilename());
 					file.writeTo(new File(file.getFilename()));
 				}
				mongo.close();
 			} else if (optionsInEffect.has(optionParser.report) && optionsInEffect.has(optionParser.projectId)) {
 				String projectid = optionsInEffect.valueOf(optionParser.projectId);
 				System.out.println("Gathering job report for project: " + projectid + "...");
 				//String regex = ".*" + projectid + "\\].*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				String regex = ".*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				String host = getProperty(PropertyKeys.JHOST);
 				int port = Integer.parseInt(getProperty(PropertyKeys.JPORT));
 				String dbName = getProperty(PropertyKeys.JDBNAME);
 				Mongo mongo = new Mongo(host, port);
 				DB db = mongo.getDB(dbName);
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					db.authenticate(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS).toCharArray());
 				}
 				DBCollection recipeCollection = db.getCollection("recipes");
 				BasicDBObject q = new BasicDBObject();
 				q.put("recipe", new BasicDBObject("$regex", regex));
 				long total = recipeCollection.count(q);
 				q = new BasicDBObject();
 				q.put("recipe", new BasicDBObject("$regex", regex));
 				q.put("completed", Boolean.valueOf(true));
 				q.put("failed", Boolean.valueOf(false));
 				long completed = recipeCollection.count(q);
 				q = new BasicDBObject();
 				q.put("recipe", new BasicDBObject("$regex", regex));
 				q.put("completed", Boolean.valueOf(true));
 				q.put("failed", Boolean.valueOf(true));
 				long failed = recipeCollection.count(q);
 				q = new BasicDBObject();
 				q.put("recipe", new BasicDBObject("$regex", regex));
 				q.put("completed", Boolean.valueOf(false));
 				q.put("clientid", new BasicDBObject("$type", Integer.valueOf(2)));
 				long locked = recipeCollection.count(q);
 				q = new BasicDBObject();
 				q.put("recipe", new BasicDBObject("$regex", regex));
 				q.put("completed", Boolean.valueOf(false));
 				q.put("clientid", new BasicDBObject("$type", Integer.valueOf(10)));
 				long unlocked = recipeCollection.count(q);
 				System.out.println("Progress report: ");
 				System.out.println((new StringBuilder("Total: ")).append(total).toString());
 				System.out.println((new StringBuilder("Completed: ")).append(completed).toString());
 				System.out.println((new StringBuilder("Failed: ")).append(failed).toString());
 				System.out.println((new StringBuilder("Locked: ")).append(locked).toString());
 				System.out.println((new StringBuilder("Unlocked: ")).append(unlocked).toString());
 				System.out.println();
				mongo.close();
 			} else if (optionsInEffect.has(optionParser.retract) && optionsInEffect.has(optionParser.projectId)) {
 				String projectid = optionsInEffect.valueOf(optionParser.projectId);
 				System.out.println("Deleting jobs for project: " + projectid + "...");
 
 				System.out.println("Removing jobs...");
 				MongoRecipeStore mrs;
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					mrs = new MongoRecipeStore(getProperty(PropertyKeys.JHOST), Integer.parseInt(getProperty(PropertyKeys.JPORT)), getProperty(PropertyKeys.JDBNAME), "recipes", getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 				} else {
 					mrs = new MongoRecipeStore(getProperty(PropertyKeys.JHOST), Integer.parseInt(getProperty(PropertyKeys.JPORT)), getProperty(PropertyKeys.JDBNAME), "recipes");
 				}
 				
 				//String recipeId = ".*" + projectid + "\\].*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				String recipeId = ".*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				mrs.removeRecipe(recipeId);
 				
 				System.out.println("Removing user and generated files...");
 				String host = getProperty(PropertyKeys.JHOST);
 				int port = Integer.parseInt(getProperty(PropertyKeys.JPORT));
 				String dbName = getProperty(PropertyKeys.JDBNAME);
 				Mongo mongo = new Mongo(host, port);
 				DB db = mongo.getDB(dbName);
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					db.authenticate(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS).toCharArray());
 				}
 				GridFS gfs = new GridFS(db);
 				String regex = ".*" + projectid + "\\].*" + getProperty(PropertyKeys.CLIENTID).replaceAll("-", "\\-") + ".*";
 				BasicDBObject q = new BasicDBObject();
 				q.put("filename", new BasicDBObject("$regex", regex));
 				List<GridFSDBFile> find = gfs.find(q);
 				for (GridFSDBFile file : find) {
 					System.out.println("Removing: " + file.getFilename());
 					gfs.remove(ObjectId.massageToObjectId(file.getId()));
 				}
				mongo.close();
 			} else if (optionsInEffect.has(optionParser.projectId) && optionsInEffect.has(optionParser.sampleFile)) {
 				String projectid = optionsInEffect.valueOf(optionParser.projectId);
 				File sampleFile = optionsInEffect.valueOf(optionParser.sampleFile);
 				
 				System.out.println("Uploading user files...");
 
 				// Upload and samplefile
 				String host = getProperty(PropertyKeys.JHOST);
 				int port = Integer.parseInt(getProperty(PropertyKeys.JPORT));
 				String dbName = getProperty(PropertyKeys.JDBNAME);
 				Mongo mongo = new Mongo(host, port);
 				DB db = mongo.getDB(dbName);
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					db.authenticate(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS).toCharArray());
 				}
 				GridFS gfs = new GridFS(db);
 				String sampleFileName = "[INTERPRO]_[" + projectid + "]" + "_[" + getProperty(PropertyKeys.CLIENTID) + "]_[" + sampleFile.getName() + "]";
 				GridFSInputFile gsampleFile = gfs.createFile(sampleFile);
 				gsampleFile.setFilename(sampleFileName);
 				gsampleFile.save();
 
 				System.out.println("Creating jobs for project: " + projectid + "...");
 				System.out.println("Creating jobs...");
 
 				MongoRecipeStore mrs;
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					mrs = new MongoRecipeStore(getProperty(PropertyKeys.JHOST), Integer.parseInt(getProperty(PropertyKeys.JPORT)), getProperty(PropertyKeys.JDBNAME), "recipes", getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 				} else {
 					mrs = new MongoRecipeStore(getProperty(PropertyKeys.JHOST), Integer.parseInt(getProperty(PropertyKeys.JPORT)), getProperty(PropertyKeys.JDBNAME), "recipes");
 				}
 
 				// Projectid, Relicid, Clientid
 				String recipeId = "[INTERPRO]_[" + projectid + "]" + "_[" + getProperty(PropertyKeys.CLIENTID) + "]_[" + sampleFile.getName() + "]";
 
 				Recipe recipe = new Recipe(recipeId);
 
 				// Retrieve fasta (sample) file from db
 				Step s = new Step("download_fasta");
 				CopyInMongoFile cm = new CopyInMongoFile();
 				cm.setHostname(getProperty(PropertyKeys.JHOST));
 				cm.setPort(Integer.parseInt(getProperty(PropertyKeys.JPORT)));
 				cm.setDbName(getProperty(PropertyKeys.JDBNAME));
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					cm.setAuthCredentials(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 				}
 				cm.setFileName(sampleFileName);
 
 				s.add(cm);
 				recipe.add(s);
 
 				s = new Step("run_interpro");
 				// Run interpro
 				RunBashScriptOperation bco = new RunBashScriptOperation();
 				StringBuffer script = new StringBuffer();
 				//#Interpro run
 				// ./interproscan-5-RC7/interproscan.sh -goterms --disable-precalc -pa -f TSV -i $PWD/5f341d1761fdef400389340116fedccf.fasta -o $PWD/5f341d1761fdef400389340116fedccf.interpro
 				
 				String identifier = sampleFileName.replaceAll(".fasta", "");
 				script.append("#!/bin/bash\n");
 				script.append("./interproscan-5-RC7/interproscan.sh -goterms --disable-precalc -pa -f TSV -i " + sampleFileName + " -o " + identifier + ".interpro\n");
 				bco.setScript(script.toString());
 
 				s.add(bco);
 				recipe.add(s);
 
 				// Check interpro output file exists
 				s = new Step("check_interpro_output");
 				CheckFileExistsOperation cfeo = new CheckFileExistsOperation();
 				cfeo.setPath(identifier + ".interpro");
 				
 				s.add(cfeo);
 				recipe.add(s);
 				
 				// Merge output
 				s = new Step("merge_interpro_results");
 				// Merge output cat $PWD/5f341d1761fdef400389340116fedccf.fasta $PWD/5f341d1761fdef400389340116fedccf.interpro > $PWD/5f341d1761fdef400389340116fedccf.output
 				bco = new RunBashScriptOperation();
 				script = new StringBuffer();
 				script.append("#!/bin/bash\n");
 				script.append("cat " + sampleFileName + " " + identifier + ".interpro > " + identifier + ".output\n");
 				bco.setScript(script.toString());
 				
 				s.add(bco);
 				recipe.add(s);
 
 				// Copy back
 
 				/*
 				// Unzip binaries and make executable plink and snptest
 				Step s = new Step("setup_binaries");
 				RunBashScriptOperation bco = new RunBashScriptOperation();
 				StringBuffer script = new StringBuffer();
 				script.append("#!/bin/bash\n");
 				script.append("unzip -o binaries.zip\n");
 				script.append("chmod +x plink\n");
 				script.append("chmod +x snptest_v2.4.1\n");
 				bco.setScript(script.toString());
 
 				s.add(bco);
 				recipe.add(s);
 
 				// Copy in relic
 				s = new Step("copyin_relic");
 				CopyInOperation co = new CopyInOperation();
 				co.setRelicId(r.getIdentifier());
 				co.setNumTries(3);
 
 				s.add(co);
 				recipe.add(s);
 
 				// Copy in user files
 				s = new Step("copyin_sample");
 				CopyInMongoFile cm = new CopyInMongoFile();
 				cm.setHostname(getProperty(PropertyKeys.JHOST));
 				cm.setPort(Integer.parseInt(getProperty(PropertyKeys.JPORT)));
 				cm.setDbName(getProperty(PropertyKeys.JDBNAME));
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					cm.setAuthCredentials(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 				}
 				cm.setFileName(sampleFileName);
 
 				s.add(cm);
 				recipe.add(s);
 
 				// Optional copy in cov
 				if (covarFile != null && covarFile.exists()) {
 					s = new Step("copyin_covar");
 					String covarFileName = "[GWAS]_[" + projectid + "]" + "_[" + getProperty(PropertyKeys.CLIENTID) + "]_[" + covarFile.getName() + "]";
 					cm = new CopyInMongoFile();
 					cm.setHostname(getProperty(PropertyKeys.JHOST));
 					cm.setPort(Integer.parseInt(getProperty(PropertyKeys.JPORT)));
 					cm.setDbName(getProperty(PropertyKeys.JDBNAME));
 					if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 						cm.setAuthCredentials(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 					}
 					cm.setFileName(covarFileName);
 
 					s.add(cm);
 					recipe.add(s);
 				}
 
 				// Do the work
 				if (snptest) {
 					if (covarFile != null && covarFile.exists()) {
 						// Run snptest
 						s = new Step("run_snptest");
 						bco = new RunBashScriptOperation();
 						script = new StringBuffer();
 						script.append("#!/bin/bash\n");
 						String sampleFileNameSnptest = sampleFileName.replaceAll("\\[", "\\\\[");
 						command = "./snptest_v2.4.1 -data " + r.getFileName() + " " + sampleFileNameSnptest + " -frequentist 1 -pheno pheno1 -method score -hwe -cov_all " + userOptions + " -log " + recipeId + ".screen -o " + recipeId + "\n";
 						script.append(command);
 						bco.setScript(script.toString());
 
 						s.add(bco);
 						recipe.add(s);
 					} else {
 
 						// Run snptest
 						s = new Step("run_snptest");
 						bco = new RunBashScriptOperation();
 						script = new StringBuffer();
 						script.append("#!/bin/bash\n");
 						String sampleFileNameSnptest = sampleFileName.replaceAll("\\[", "\\\\[");
 						command = "./snptest_v2.4.1 -data " + r.getFileName() + " " + sampleFileNameSnptest + " -frequentist 1 -pheno pheno1 -method score -hwe " + userOptions + " -log " + recipeId + ".screen -o " + recipeId + "\n";
 						script.append(command);
 						bco.setScript(script.toString());
 
 						s.add(bco);
 						recipe.add(s);
 
 					}
 				} else if (plink) {
 					if (covarFile != null && covarFile.exists()) {
 						String covarFileName = "[GWAS]_[" + projectid + "]" + "_[" + getProperty(PropertyKeys.CLIENTID) + "]_[" + covarFile.getName() + "]";
 						String covarFileNamePlink = covarFileName.replaceAll("[", "\\\\[");
 						String sampleFileNamePlink = sampleFileName.replaceAll("\\[", "\\\\[");
 						if (useLog) {
 							// Run plink
 							s = new Step("run_plink");
 							bco = new RunBashScriptOperation();
 							script = new StringBuffer();
 							script.append("#!/bin/bash\n");
 							command = "./plink --dosage " + r.getFileName() + " Zin noheader skip0=1 skip1=1 format=3 --fam " + sampleFileNamePlink + " --covar " + covarFileNamePlink + " --logistic " + userOptions + " --noweb --out " + recipeId + "\n";
 							script.append(command);
 							bco.setScript(script.toString());
 
 							s.add(bco);
 							recipe.add(s);
 						} else {
 							// Run plink
 							s = new Step("run_plink");
 							bco = new RunBashScriptOperation();
 							script = new StringBuffer();
 							script.append("#!/bin/bash\n");
 							command = "./plink --dosage " + r.getFileName() + " Zin noheader skip0=1 skip1=1 format=3 --fam " + sampleFileNamePlink + " --covar " + covarFileNamePlink + " --linear " + userOptions + " --noweb --out " + recipeId + "\n";
 							script.append(command);
 							bco.setScript(script.toString());
 
 							s.add(bco);
 							recipe.add(s);
 						}
 					} else {
 						String sampleFileNamePlink = sampleFileName.replaceAll("\\[", "\\\\[");
 						if (useLog) {
 							// Run plink
 							s = new Step("run_plink");
 							bco = new RunBashScriptOperation();
 							script = new StringBuffer();
 							script.append("#!/bin/bash\n");
 							command = "./plink --dosage " + r.getFileName() + " Zin noheader skip0=1 skip1=1 format=3 --fam " + sampleFileNamePlink + " --logistic " + userOptions + " --noweb --out " + recipeId + "\n";
 							script.append(command);
 							bco.setScript(script.toString());
 
 							s.add(bco);
 							recipe.add(s);
 						} else {
 							// Run plink
 							s = new Step("run_plink");
 							bco = new RunBashScriptOperation();
 							script = new StringBuffer();
 							script.append("#!/bin/bash\n");
 							command = "./plink --dosage " + r.getFileName() + " Zin noheader skip0=1 skip1=1 format=3 --fam " + sampleFileNamePlink + " --linear " + userOptions + " --noweb --out " + recipeId + "\n";
 							script.append(command);
 							bco.setScript(script.toString());
 
 							s.add(bco);
 							recipe.add(s);
 						}
 					}
 				}
 				*/
 				// Tar.gz output
 				//				s = new Step("zip_output");
 				//				String escapedRid = recipeId.replaceAll("\\[", "\\\\[");
 				//				bco = new RunBashScriptOperation();
 				//				script = new StringBuffer();
 				//				script.append("#!/bin/bash\n");
 				//				script.append("tar -cvzf " + recipeId + ".tar.gz " + escapedRid + "*\n");
 				//				bco.setScript(script.toString());
 				//
 				//				s.add(bco);
 				//				recipe.add(s);
 
 				// Copy output to mongo
 				s = new Step("copyout_result");
 				CopyOutMongoFile cmo = new CopyOutMongoFile();
 				cmo.setHostname(getProperty(PropertyKeys.JHOST));
 				cmo.setPort(Integer.parseInt(getProperty(PropertyKeys.JPORT)));
 				cmo.setDbName(getProperty(PropertyKeys.JDBNAME));
 				if (Boolean.parseBoolean(getProperty(PropertyKeys.JAUTH))) {
 					cmo.setAuthCredentials(getProperty(PropertyKeys.JUSER), getProperty(PropertyKeys.JPASS));
 				}
 				cmo.setFileName(identifier + ".output");
 
 				s.add(cmo);
 				recipe.add(s);
 
 				recipe.setTimeout(43200000);
 				recipe.setResetOnFailure(true);
 				recipe.setResetOnTimeout(true);
 				mrs.putRecipe(recipe);
 
 				System.out.println("Created: " + recipeId);
				mongo.close();
 			} else {
 				showUsage = true;
 			}
 			if (showUsage) {
 				showUsage(optionParser);
 			}
 			System.out.println("-------------------------------------------------------------------------------");
 			System.out.println("Done!");
 			System.out.println("-------------------------------------------------------------------------------");
 		} catch (Exception e) {
 			showErrorAndExit(optionParser, e);
 		}
 	}
 
 	private static void showErrorAndExit(ScheduleInterproOptionParser optionParser, Exception e) {
 		System.out.println("Something didn't quite work like expected: [" + e.getMessage() + "]");
 		showUsage(optionParser);
 		System.exit(1);
 	}
 
 	private static void showUsage(ScheduleInterproOptionParser optionParser) {
 		try {
 			optionParser.printHelpOn(System.out);
 		} catch (IOException e) {
 			// Should never happen in this case. I wonder how the sysout below
 			// would fare..
 			System.out.println("Yikes, could not print to System.out");
 			e.printStackTrace();
 		}
 	}
 
 	private static String getProperty(PropertyKeys prop) {
 		return prop.getProperty(properties);
 	}
 
 	private static boolean isNullOrEmpty(String s) {
 		if (s == null || "".equals(s)) {
 			return true;
 		}
 		return false;
 	}
 }
