 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.xml;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import screen.tools.sbs.context.ContextException;
 import screen.tools.sbs.context.ContextHandler;
 import screen.tools.sbs.context.defaults.ContextKeys;
 import screen.tools.sbs.context.defaults.EnvironmentVariablesContext;
 import screen.tools.sbs.context.defaults.RepositoryContext;
 import screen.tools.sbs.objects.Dependency;
 import screen.tools.sbs.objects.Description;
 import screen.tools.sbs.objects.EnvironmentVariables;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.Flag;
 import screen.tools.sbs.objects.Import;
 import screen.tools.sbs.objects.Library;
 import screen.tools.sbs.objects.Pack;
 import screen.tools.sbs.repositories.RepositoryComponent;
 import screen.tools.sbs.repositories.RepositoryFilter;
 import screen.tools.sbs.utils.FieldBool;
 import screen.tools.sbs.utils.FieldBuildMode;
 import screen.tools.sbs.utils.FieldBuildType;
 import screen.tools.sbs.utils.FieldFile;
 import screen.tools.sbs.utils.FieldPath;
 import screen.tools.sbs.utils.FieldPathType;
 import screen.tools.sbs.utils.FieldString;
 import screen.tools.sbs.utils.Logger;
 import screen.tools.sbs.utils.Utilities;
 
 public class SBSDomDataFiller {
 	private ContextHandler contextHandler;
 	private Pack pack;
 	private Pack testPack;
 	private FieldPath sbsXmlPath;
 	private static String propertyNameQuery = "//properties/name/text()";
 	private static String propertyVersionQuery = "//properties/version/text()";
 	private static String propertyBuildTypeQuery = "//properties/buildtype/text()";
 	private String propertyName;
 	private String propertyVersion;
 	private String propertyBuildType;
 	private boolean isRelease;
 	
 	public SBSDomDataFiller(ContextHandler contextHandler, Pack pack, Pack testPack, FieldPath sbsXmlPath) {
 		this.contextHandler = contextHandler;
 		this.sbsXmlPath = sbsXmlPath;
 		this.pack = pack;
 		this.testPack = testPack;
 	}
 	
 	public void fill(Document doc, boolean isTest) throws ContextException{
 		//ErrorList errList = GlobalSettings.getGlobalSettings().getErrorList();
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 
 		isRelease = true;
		if(variables.contains("_COMPILE_MODE")){
			isRelease = "Release".equals(variables.getValue("_COMPILE_MODE"));
 		}
 		
 		Element root = doc.getDocumentElement();
 		XPathFactory xFactory = XPathFactory.newInstance();
 		XPath query = xFactory.newXPath();
 		
 		try {
 			if(pack == null)
 				pack = new Pack();
 			if(testPack == null)
 				testPack = new Pack();
 			//properties
 			
 			//name
 			propertyName = (String) query.compile(propertyNameQuery).evaluate(root);
 			Logger.debug("propertyName : "+propertyName);
 			pack.getProperties().setName(new FieldString(propertyName));
 			
 			//version
 			propertyVersion = (String) query.compile(propertyVersionQuery).evaluate(root);
 			Logger.debug("propertyVersion : "+propertyVersion);
 			pack.getProperties().setVersion(new FieldString(propertyVersion));
 
 			//build type
 			propertyBuildType = (String) query.compile(propertyBuildTypeQuery).evaluate(root);
 			Logger.debug("propertyBuildType : "+propertyBuildType);
 			pack.getProperties().setBuildType(new FieldString(propertyBuildType));
 
 			if(isTest){
 				//test
 				NodeList test = root.getElementsByTagName("test");
 				if(test.getLength() == 1){
 					testPack.getProperties().setName(new FieldString(propertyName+"/Test"));
 					testPack.getProperties().setVersion(new FieldString(propertyVersion));
 					testPack.getProperties().setBuildType(new FieldString("executable"));
 					FieldPath path = new FieldPath(sbsXmlPath.getOriginalString()+"/test");
 					processDependencies((Element) test.item(0), testPack, path);
 					
 					//descriptions
 					processDescriptions(root, testPack, path);
 					
 					//imports
 					processImports(root, testPack, path);
 				}
 			}
 			else{
 				processAll(root, pack, sbsXmlPath);
 			}
 			
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void processAll(Element root, Pack pack, FieldPath xmlPath) throws ContextException {
 		//main
 		NodeList main = root.getElementsByTagName("main");
 		if(main.getLength() == 1){
 			processDependencies((Element) main.item(0),pack,xmlPath);
 			processFlags((Element) main.item(0),pack,xmlPath);
 		}
 		
 		//descriptions
 		processDescriptions(root, pack, xmlPath);
 		
 		//imports
 		processImports(root, pack, xmlPath);
 	}
 
 	private void processDependencies(Element root, Pack pack, FieldPath xmlPath) throws ContextException {
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		
 		//dependencies
 		Logger.debug("dependencies");
 		NodeList depsRoot = root.getElementsByTagName("dependencies");
 		if(depsRoot.getLength() == 1){
 			NodeList deps = ((Element) depsRoot.item(0)).getElementsByTagName("dependency");
 			for(int i=0; i<deps.getLength(); i++){
 				List<Library> tmpLibList = new ArrayList<Library>();
 				
 				//dependency
 				Logger.debug("\tdependency");
 				Element dep = (Element) deps.item(i);
 				Dependency newDep = new Dependency();
 				
 				String name = dep.getAttribute("name");
 				newDep.setName(new FieldString(name));
 
 				String version = dep.getAttribute("version");
 				version = ("".equals(version)) ? null : version;
 				newDep.setVersion(new FieldString(version));
 				
 				String export = dep.getAttribute("export");
 				export = ("".equals(export)) ? null : export;
 				newDep.setExport(new FieldBool(export));
 				
 				// includes
 				NodeList inclRoot = dep.getElementsByTagName("includes");
 				if(inclRoot.getLength() == 1){
 					Logger.debug("\t\tincludes");
 					NodeList paths = ((Element) inclRoot.item(0)).getElementsByTagName("path");
 					for(int j=0; j<paths.getLength(); j++){
 						//path
 						Logger.debug("\t\t\tpath");
 						Element path = (Element) paths.item(j);
 						
 						String pathString = path.getTextContent();
 						Logger.debug("\t\t\t\ttext : "+pathString);
 						
 						String type = path.getAttribute("type");
 						FieldPathType pType = new FieldPathType();
 						pType.set(type);
 						Logger.debug("\t\t\t\ttype : "+type);
 						
 						String buildMode = path.getAttribute("build");
 						Logger.debug("\t\t\t\tbuild : "+buildMode);
 						
 						FieldPath fieldPath = pType.getFieldPath(xmlPath.getOriginalString(), pathString);
 						fieldPath.setBuildMode(new FieldBuildMode(buildMode));
 						
 						if(fieldPath.getBuildMode().isSameMode(isRelease))
 							newDep.addIncludePath(fieldPath);
 					}
 				}
 				
 				//libraries
 				NodeList libsRoot = dep.getElementsByTagName("libraries");
 				if(libsRoot.getLength() == 1){
 					Logger.debug("\t\tlibraries");
 					NodeList paths = ((Element) libsRoot.item(0)).getElementsByTagName("path");
 					for(int j=0; j<paths.getLength(); j++){
 						//path
 						Logger.debug("\t\t\tpath");
 						Element path = (Element) paths.item(j);
 						String pathString = path.getTextContent();
 						Logger.debug("\t\t\t\ttext : "+pathString);
 						
 						String type = path.getAttribute("type");
 						FieldPathType pType = new FieldPathType();
 						pType.set(type);
 						Logger.debug("\t\t\t\ttype : "+type);
 						
 						String buildMode = path.getAttribute("build");
 						Logger.debug("\t\t\t\tbuild : "+buildMode);
 						
 						FieldPath fieldPath = pType.getFieldPath(xmlPath.getOriginalString(), pathString);
 						fieldPath.setBuildMode(new FieldBuildMode(buildMode));
 						
 						if(fieldPath.getBuildMode().isSameMode(isRelease))
 							newDep.addLibraryPath(fieldPath);
 					}
 					NodeList libs = ((Element) libsRoot.item(0)).getElementsByTagName("lib");
 					for(int j=0; j<libs.getLength(); j++){
 						//lib
 						Logger.debug("\t\t\tlib");
 						Element lib = (Element) libs.item(j);
 						String libString = lib.getTextContent();
 						Logger.debug("\t\t\t\ttext : "+libString);
 
 						String libVersion = lib.getAttribute("version");
 						libVersion = ("".equals(libVersion)) ? null : libVersion;
 						
 						Library library = new Library();
 						library.setName(new FieldString(libString));
 						library.setVersion(new FieldString(libVersion));
 						
 						newDep.addLibrary(library);
 						tmpLibList.add(library);
 					}
 				}
 				
 				if(newDep.getSbs()){
 					//retrieve dependency file in SBS repository
 					String packName = newDep.getName().getString();
 					String packVersion = newDep.getVersion().getString();
 					
 					if(!variables.contains("ENV_NAME")){
 						ErrorList.instance.addError("undefined variable : ENV_NAME");
 					}
 					String compiler = variables.getValue("ENV_NAME");
 					FieldString compilerField = new FieldString(compiler);
 					
 					RepositoryComponent finder = new RepositoryComponent(newDep.getName(), newDep.getVersion(), compilerField);
 					RepositoryFilter retrieved = finder.retrieve(contextHandler.<RepositoryContext>get(ContextKeys.REPOSITORIES).getRepositoryFilterTable());
 					if(retrieved == null){
 						ErrorList.instance.addError("Unable to retrieve component into repositories :\n"+
 									"- component name : "+packName+"\n"+
 									"- component version : "+packVersion+"\n"+
 									"- compiler : "+compiler);
 						return;
 					}
 					String fullPath = retrieved.getData().getPath().getString()+"/"+packName+"/"+packVersion;
 					
 					//String fullPath = repoRoot +"/"+packName+"/"+packVersion;					
 					
 					if(new File(fullPath+"/component.xml").exists()){
 						//if component.xml exists, retrieve contents into pack
 						Document doc = SBSDomParser.parserFile(new File(fullPath+"/component.xml"));
 						Element root2 = doc.getDocumentElement();
 						Logger.debug("import "+fullPath+"/component.xml");
 						
 						processAll(root2, pack, new FieldPath(fullPath));
 					}
 					else { 
 						if(Utilities.isWindows())
 							ErrorList.instance.addError("Can't retrieve file component.xml in "+fullPath+" folder : component "+packName+" with version "+packVersion+" doesn't exist");
 						else {
 							ErrorList.instance.addWarning("Can't retrieve file component.xml in "+fullPath+" folder : component "+packName+" with version "+packVersion+" doesn't exist => Uses default settings");
 							for(int j=0; j<tmpLibList.size(); j++){
 								//add default library description
 								Description description = new Description();
 								Library lib = tmpLibList.get(j);
 								description.setName(lib.getName().getString());
 								
 								EnvironmentVariables additionalVars = new EnvironmentVariables();
 								additionalVars.put("LIB_NAME", lib.getName().getOriginalString().replaceAll("/", ""));
 								
 								FieldString fs = new FieldString("${DEFAULT_SHARED_LIB_COMPILE_NAME}");
 								description.setCompileName(fs.getString(additionalVars));
 								
 								fs = new FieldString("${DEFAULT_SHARED_LIB_FULL_NAME}");
 								description.setFullName(fs.getString(additionalVars));
 								
 								description.setBuildMode(FieldBuildMode.Type.ALL);
 								description.setBuildType(FieldBuildType.Type.SHARED_LIBRARY);
 								pack.addDescription(description);
 							}
 						}
 					}
 				}
 				
 				pack.addDependency(newDep);
 			}
 		}
 	}
 
 	private void processFlags(Element root, Pack pack, FieldPath xmlPath) {
 		//flags
 		Logger.debug("flags");
 		NodeList optsRoot = root.getElementsByTagName("flags");
 		if(optsRoot.getLength() == 1){
 			NodeList opts = ((Element) optsRoot.item(0)).getElementsByTagName("flag");
 			for(int i=0; i<opts.getLength(); i++){
 				//dependency
 				Logger.debug("\tflag");
 				Element opt = (Element) opts.item(i);
 				Flag flag = new Flag();
 				
 				String flagValue = opt.getAttribute("flag");
 				flag.setFlag(new FieldString(flagValue));
 				Logger.debug("\t\t\tflag : "+flagValue);
 				
 				String value = opt.getAttribute("value");
 				flag.setValue(new FieldString(value));
 				Logger.debug("\t\t\tvalue : "+value);
 				
 				String buildMode = opt.getAttribute("build");
 				Logger.debug("\t\t\t\tbuild : "+buildMode);
 				flag.setBuildMode(new FieldBuildMode(buildMode));
 				
 				if(flag.getBuildMode().isSameMode(isRelease))
 					pack.addFlag(flag);
 			}
 		}
 	}
 		
 	void processDescriptions(Element root, Pack pack, FieldPath xmlPath) throws ContextException{
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		boolean isRelease = true;
 		if(variables.contains("_COMPILE_MODE")){
 			isRelease = "Release".equals(variables.getValue("_COMPILE_MODE"));
 		}
 		
 		//descriptions
 		Logger.debug("descriptions");
 		NodeList descRoot = root.getElementsByTagName("descriptions");
 		if(descRoot.getLength() == 1){
 			NodeList descs = ((Element) descRoot.item(0)).getElementsByTagName("library");
 			for(int i=0; i<descs.getLength(); i++){
 				//dependency
 				Logger.debug("\tlibrary");
 				Element lib = (Element) descs.item(i);
 				Description description = new Description();
 				
 				String name = lib.getAttribute("name");
 				description.setName(name);
 				Logger.debug("\t\t\tname : "+name);
 				
 				String fullName = lib.getAttribute("full-name");
 				description.setFullName(fullName);
 				Logger.debug("\t\t\tfull-name : "+fullName);
 				
 				String compileName = lib.getAttribute("compile-name");
 				description.setCompileName(compileName);
 				Logger.debug("\t\t\tcompile-name : "+compileName);
 
 				String buildType = lib.getAttribute("type");
 				description.setBuildType(buildType);
 				Logger.debug("\t\t\ttype : "+buildType);
 
 				String buildMode = lib.getAttribute("build");
 				description.setBuildMode(buildMode);
 				Logger.debug("\t\t\tbuild : "+buildMode);
 
 				if(description.getBuildMode().isSameMode(isRelease))
 					pack.addDescription(description);
 			}
 		}
 	}
 	
 	private void processImports(Element root, Pack pack, FieldPath xmlPath) throws ContextException {
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		boolean isRelease = true;
 		if(variables.contains("_COMPILE_MODE")){
 			isRelease = "Release".equals(variables.getValue("_COMPILE_MODE"));
 		}
 		
 		//descriptions
 		Logger.debug("imports");
 		NodeList importRoot = root.getElementsByTagName("imports");
 		if(importRoot.getLength() == 1){
 			NodeList imports = ((Element) importRoot.item(0)).getElementsByTagName("import");
 			for(int i=0; i<imports.getLength(); i++){
 				//dependency
 				Logger.debug("\timport");
 				Element imp = (Element) imports.item(i);
 				Import import_ = new Import();
 				
 				String buildMode = imp.getAttribute("build");
 				import_.setBuildMode(buildMode);
 				Logger.debug("\t\tbuild : "+buildMode);
 				
 				//path
 				String file = imp.getAttribute("file");
 				Logger.debug("\t\tfile : "+file);
 				
 				String type = imp.getAttribute("pathtype");
 				FieldPathType pType = new FieldPathType();
 				pType.set(type);
 				Logger.debug("\t\tpathtype : "+type);
 				
 				FieldFile fieldFile = pType.getFieldFile(xmlPath.getOriginalString(), file);
 				import_.setFile(fieldFile);
 				
 				if(import_.getBuildMode().isSameMode(isRelease)){
 					String file2 = import_.getFile().getString();
 					File importFile = new File(file2);
 					if(importFile.exists()){
 						//if component.xml exists, retrieve contents into pack
 						Document doc = SBSDomParser.parserFile(importFile);
 						Element root2 = doc.getDocumentElement();
 						Logger.debug("import "+file2);
 						
 						processAll(root2, pack, new FieldPath(importFile.getParent()));
 					}
 					else{
 						
 					}
 				}
 			}
 		}
 	}
 }
