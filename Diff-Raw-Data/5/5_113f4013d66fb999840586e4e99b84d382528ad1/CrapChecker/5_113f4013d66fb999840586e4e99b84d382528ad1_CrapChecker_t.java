 package com.intellij.plugin.crap4j;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.plugin.crap4j.complexity.CyclomaticComplexity;
 import com.intellij.plugin.crap4j.complexity.MethodComplexity;
 import com.intellij.plugin.crap4j.coverage.CoverageException;
 import com.intellij.plugin.crap4j.coverage.EmmaCoverage;
 import com.intellij.psi.PsiJavaFile;
 import org.objectweb.asm.tree.analysis.AnalyzerException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class CrapChecker {
 
     public static Map<String, Double> crapCheckFile(PsiJavaFile psiFile, Project project) throws IOException, CoverageException, AnalyzerException {
 	FileHelper fileHelper = new FileHelper(project);
 	List<String> classFiles = fileHelper.getClassFilePaths(psiFile.getVirtualFile());
 	Map<String, Double> coverageMap = EmmaCoverage.getMethodCoverageMap(project, psiFile);
 
 	CyclomaticComplexity cc = new CyclomaticComplexity();
 	List<MethodComplexity> methodComplexities = new ArrayList<MethodComplexity>();
 	for (String classFile : classFiles) {
 	    File input = new File(classFile);
 	    methodComplexities.addAll(cc.getMethodComplexitiesFor(input));
 	}
 
 	Map<String, Double> crapMap = new HashMap<String, Double>();
 	for (MethodComplexity methodComplexity : methodComplexities) {
	    String sig = methodComplexity.getMethodName() + methodComplexity.getMethodDescriptor();
 	    double coverage = coverageMap.containsKey(sig) ? coverageMap.get(sig) : 0;
 	    double crap = (Math.pow(methodComplexity.getComplexity(), 2) * Math.pow(1 - coverage, 3)) + methodComplexity.getComplexity();
 	    if (crap >= 30) {
		crapMap.put(methodComplexity.getClassName() + "." + sig, crap);
 	    }
 	}
 
 	return crapMap;
     }
 }
