 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.workspace;
 
 import static org.oobium.utils.Config.MIGRATION_SERVICE;
 import static org.oobium.utils.StringUtils.blank;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Manifest;
 import java.util.regex.Pattern;
 
 import org.oobium.persist.migrate.db.AbstractDbMigration;
 import org.oobium.utils.Config;
 import org.oobium.utils.FileUtils;
 import org.oobium.utils.Config.Mode;
 
 public class Migrator extends Bundle {
 
 	/**
 	 * this Migrator class file
 	 */
 	public final File migrator;
 	
 	/**
 	 * this migrator's configuration file
 	 */
 	public final File config;
 	
 	/**
 	 * this migrator's migrations directory
 	 */
 	public final File migrations;
 	
 	/**
 	 * this migrator's generated directory
 	 */
 	public final File generated;
 	
 	/**
 	 * this migrator's main generated source directory
 	 */
 	public final File genMain;
 	
 	/**
 	 * this migrator's generated migrations directory
 	 */
 	public final File genMigrations;
 	
 	/**
 	 * the name of this migration's module bundle
 	 */
 	public final File module;
 
 	/**
 	 * the name of this migration's module bundle
 	 */
 	public final String moduleName;
 	
 	Migrator(Type type, File file, Manifest manifest) {
 		super(type, file, manifest);
 		this.migrator = new File(main, "Migrator.java");
 		this.config = new File(main, "configuration.js");
 		this.migrations = new File(main, "migrations");
 		this.generated = new File(file, "generated");
 		this.genMain = new File(generated, name.replace('.', File.separatorChar));
 		this.genMigrations = new File(genMain, "migrations");
 		this.moduleName = name.substring(0, name.length() - 9);
 		this.module = new File(file.getParent(), moduleName);
 	}
 
 	public Config loadConfiguration() {
 		return Config.loadConfiguration(config);
 	}
 
 	@Override
 	protected void addDependencies(Workspace workspace, Mode mode, Map<Bundle, List<Bundle>> dependencies) {
 		super.addDependencies(workspace, mode, dependencies);
 		
 		Config configuration = loadConfiguration();
 		
 		addDependency(workspace, mode, configuration.getString(MIGRATION_SERVICE, mode), dependencies);
 	}
 
 	protected void addDependency(Workspace workspace, Mode mode, String fullName, Map<Bundle, List<Bundle>> dependencies) {
 		if(!blank(fullName)) {
 			Bundle bundle = workspace.getBundle(fullName);
 			if(bundle != null) {
 				addDependency(dependencies, bundle);
 				bundle.addDependencies(workspace, mode, dependencies);
 			} else {
 				throw new IllegalStateException(this + " has an unresolved requirement: " + fullName);
 			}
 		}
 	}
 	
 	public boolean addMigration(String migrationName) {
 		String oldsrc = FileUtils.readFile(activator).toString();
 		String newsrc = oldsrc;
 		
 		if(!Pattern.compile("migrations.add\\s*\\(\\s*" + migrationName + ".class\\s*\\)\\s*;").matcher(newsrc).find()) {
 			newsrc = oldsrc.replaceFirst("public\\s+void\\s+addMigrations\\s*\\(\\s*Migrations\\s+migrations\\s*\\)\\s*\\{\\s*",
 											"public void addMigrations(Migrations migrations) {\n" +
 											"\t\tmigrations.add(" + migrationName + ".class); // TODO auto-generated\n\n\t\t");
 		}
 		if(!Pattern.compile("import\\s+"+packageName(migrations)+"."+migrationName).matcher(newsrc).find()) {
 			newsrc = newsrc.replaceFirst("(package\\s+[\\w\\.]+;)", "$1\n\nimport "+packageName(migrations)+"."+migrationName+";");
 		}
 
 		if(!newsrc.equals(oldsrc)) {
 			FileUtils.writeFile(activator, newsrc);
 			return true;
 		}
 		return false;
 	}
 	
 	public File createMigration(String name) {
 		boolean sessions = "CreateSessions".equals(name);
 		File migration = getMigration(name);
 		if(migration.exists()) {
 			throw new IllegalStateException("file already exists: " + file);
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append("package " + packageName(migrations) + ";\n");
 		sb.append("\n");
 		sb.append("import java.sql.SQLException;\n");
 		sb.append("import ").append(AbstractDbMigration.class.getCanonicalName()).append(";\n");
 		sb.append("\n");	
 		sb.append("public class ").append(name).append(" extends ").append(AbstractDbMigration.class.getSimpleName()).append(" {\n");
 		sb.append("\n");
 		sb.append("\t@Override\n");
 		sb.append("\tpublic void up() throws SQLException {\n");
 		if(sessions) {
 			sb.append("\t\tcreateTable(\"sessions\",\n");
 			sb.append("\t\t\tString(\"uuid\"),\n");
 			sb.append("\t\t\tText(\"data\"),\n");
			sb.append("\t\t\tDate(\"expiration\")\n");
 			sb.append("\t\t);\n");
 		} else {
 			sb.append("\t\t// TODO auto-generated method\n");
 		}
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("\t@Override\n");
 		sb.append("\tpublic void down() throws SQLException {\n");
 		if(sessions) {
 			sb.append("\t\tdropTable(\"sessions\");\n");
 		} else {
 			sb.append("\t\t// TODO auto-generated method\n");
 		}
 		sb.append("\t}\n");
 		sb.append("\n");
 		sb.append("}\n");
 		return FileUtils.writeFile(migrations, name + ".java", sb.toString());
 	}
 
 	public File getInitialMigration() {
 		return getMigration("CreateDatabase");
 	}
 	
 	public File getMigration(String name) {
 		return new File(migrations, name + ".java");
 	}
 
 }
