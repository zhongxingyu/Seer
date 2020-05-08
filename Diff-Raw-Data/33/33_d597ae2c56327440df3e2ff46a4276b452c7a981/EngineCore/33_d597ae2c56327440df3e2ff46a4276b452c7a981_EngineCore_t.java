 package sg.edu.nus.iss.yunakti.engine;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.PackageFragment;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.text.edits.MalformedTreeException;
 
 import sg.edu.nus.iss.yunakti.engine.search.YSearch;
 import sg.edu.nus.iss.yunakti.engine.util.ConsoleStreamUtil;
 import sg.edu.nus.iss.yunakti.engine.util.WorkspaceUtils;
 import sg.edu.nus.iss.yunakti.engine.util.YConstants;
 import sg.edu.nus.iss.yunakti.engine.writer.YPersister;
 import sg.edu.nus.iss.yunakti.model.YClass;
 import sg.edu.nus.iss.yunakti.model.YModel;
 
 public class EngineCore {
 
 	private static Logger logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	
     static{
     	logger.setLevel(Level.FINER);
     }
 
 	public List<YModel> populateModel(IStructuredSelection selection) {
 		
 		List<YModel> searchResults = null;
 		try {
 			YSearch search=new YSearch();
 			List<IJavaElement> allSearchElements = search.gatherAllSearchElementsFromSelection(selection);
 			search.search(allSearchElements);
 			searchResults = search.getResults(allSearchElements,false);
 			//if the selection is a class under test, then there wont be any results - do a full scan
 			//if the selection is a class under test and there are not mappings at all, then return a dummy YModel
 			//this is done in the filterModels of the getResults method
 			if (searchResults.size()==0){
 				searchResults = doFullScanOfProject(search, allSearchElements);
 			}
 			
 			//logger.fine("Search results:"+searchResults);
 		} catch (Exception e) {
 			ConsoleStreamUtil.print(e);
 			e.printStackTrace();
 		}
 		
 		//ConsoleStreamUtil.print("Printing....xxxxx"+(searchResults.get(0).toString()));
 		
 		
 		/*for (YModel yModel : searchResults) {
 			writeAnnotation(yModel);
 		}*/
 		
 		return searchResults;
 		
 		
 		
 	}
 
 	private List<YModel> doFullScanOfProject(YSearch search, List<IJavaElement> allSearchElements) throws JavaModelException, CoreException {
 		List<YModel> searchResults;
 		ConsoleStreamUtil.print("**************************Full scan**************************");
 
 		//convert Package fragments into corresponding compilation units. Say, user could select a package or a combination of package and 
 		//class files. We need to split them up for safe filtering
 		allSearchElements=breakUpAllSearchElements(allSearchElements);
 		
 		//No Search results. Let's search the entire project for class references
 		IJavaProject javaProject = allSearchElements.get(0).getJavaProject();
 		IPackageFragment[] packageFragments = javaProject.getPackageFragments();
 		
 		
 		search.search(getAllJavaElementsFromPackageFragments(packageFragments));
 		searchResults = search.getResults(allSearchElements, true);
 		return searchResults;
 	}
 
	//breaks up packages into class elements - compilation units
 	private List<IJavaElement> breakUpAllSearchElements(List<IJavaElement> allSearchElements) throws JavaModelException {
 
 		List<IJavaElement> granularSearchElements=new ArrayList<IJavaElement>(); //10 should be good.
 		
 		if (allSearchElements!=null){
 			for (IJavaElement iJavaElement : allSearchElements) {
 				if (iJavaElement instanceof PackageFragment){
 					IPackageFragment packageFragment=(IPackageFragment)iJavaElement;
 					granularSearchElements.addAll(Arrays.asList(packageFragment.getCompilationUnits()));
 				}
 				else{
 					granularSearchElements.add(iJavaElement);
 				}
 			}
 		}
 		ConsoleStreamUtil.println("All granular search elements :" + granularSearchElements);
 		return granularSearchElements;
 	}
 
 	//
 	private List<IJavaElement> getAllJavaElementsFromPackageFragments(IPackageFragment[] packageFragments) throws JavaModelException {
 		List<IJavaElement> allCompilationUnits=new ArrayList<IJavaElement>(30);
 		for (IPackageFragment iPackageFragment : packageFragments) {
 			allCompilationUnits.addAll(Arrays.asList(iPackageFragment.getCompilationUnits()));
 		}
 		ConsoleStreamUtil.println("All compilation units" + allCompilationUnits);
 		return allCompilationUnits;
 	}
 
 	public Set<YClass> getUniqueTestCases(List<YModel> models) {
 		
 		logger.fine("Calling getUniqueTestCases");
 		Set<YClass> uniqueTestCases=new HashSet<YClass>();
 		if (models!=null && models.size()>0){
 			List<YClass> allTestCasesInModel = null;
 			for (YModel eachModel : models) {
 				allTestCasesInModel = eachModel.getTestCases();
 				uniqueTestCases.addAll(allTestCasesInModel);
 			}
 			
 		}
 		logger.fine("Unique testcase size : "+uniqueTestCases.size());
 		return uniqueTestCases;
 	}
 	
 	public Set<YClass> getUniqueHelpers(List<YModel> models) {
 		
 		logger.fine("Calling getUniqueHelpers");
 		Set<YClass> uniqueHelpers=new HashSet<YClass>();
 		if (models!=null && models.size()>0){
 			List<YClass> allTestCasesInModel = null;
 			
 			for (YModel eachModel : models) {
 				allTestCasesInModel = eachModel.getTestCases();
 				
 				if (allTestCasesInModel!=null && allTestCasesInModel.size()>0){
 					for (YClass eachTestCase : allTestCasesInModel) {
 						uniqueHelpers.addAll(eachTestCase.getMembers());		
 					}
 				}
 				
 				
 			}
 			
 		}
 		logger.fine("Unique helpers size : "+uniqueHelpers.size());
 		return uniqueHelpers;
 	}
 	
 	
 	public Map<String,List<YModel>> getModelsByPackageName(List<YModel> models) {
 		
 		logger.fine("Calling getModelsByPackageName");
 		
 		Map<String,List<YModel>> modelsByPackageName=new HashMap <String,List<YModel>>();
 		
 		if (models!=null && models.size()>0){
 			
 			YClass classUnderTest = null;
 			String packageName=null;
 			for (YModel eachModel : models) {
 				classUnderTest = eachModel.getClassUnderTest();
 				packageName=StringUtils.substringBeforeLast(classUnderTest.getFullyQualifiedName(), YConstants.DOT);
 				
 				addPackageAndModel(modelsByPackageName,packageName,eachModel);
 			}
 			
 		}
 		logger.fine("Unique testcase size : "+modelsByPackageName.size());
 		return modelsByPackageName;
 	}
 
 	private void addPackageAndModel(Map<String, List<YModel>> modelsByPackageName, String packageName, YModel eachModel) {
 		
 		if (modelsByPackageName.containsKey(packageName)){
 			List<YModel> existingModelsInPackage = modelsByPackageName.get(packageName);
 			existingModelsInPackage.add(eachModel);
 		}
 		else{
 			List<YModel> newModelList=new ArrayList<YModel>();
 			newModelList.add(eachModel);
 			modelsByPackageName.put(packageName, newModelList);
 		}
 		
 	}
 	
 	
 	public void writeAnnotation(YModel yModel){
 		//TODO persist only if needed. Need to investigate Memento
 		
 		YPersister persister=new YPersister();
 		
 		try {
 			persister.writeAnnotation(yModel);
 		} catch (MalformedTreeException e) {
 			e.printStackTrace();
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public List<YClass> getAllClassesInWorkspace(){
 		
 		List<YClass> allClassesInWorkspace=new ArrayList<YClass>(); //to avoid nullpointers in front end
 		WorkspaceUtils workspaceUtils=WorkspaceUtils.getInstance();
 		try {
 			workspaceUtils.gatherAllClassesInWorkspace();
 			allClassesInWorkspace = workspaceUtils.getAllClasses();
 			
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		logger.fine("All classes in workspace : "+allClassesInWorkspace);
 		return allClassesInWorkspace;
 		
 	}
 	
 	
 	
 	
 }
