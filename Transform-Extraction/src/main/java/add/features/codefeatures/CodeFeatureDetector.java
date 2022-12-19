package add.features.codefeatures;

import java.util.ArrayList;
import java.util.List;

import add.features.codefeatures.codeanalyze.AbstractCodeAnalyzer;
import add.features.codefeatures.codeanalyze.BinaryOperatorAnalyzer;
import add.features.codefeatures.codeanalyze.ConstantAnalyzer;
import add.features.codefeatures.codeanalyze.ConstructorAnalyzer;
import add.features.codefeatures.codeanalyze.LogicalExpressionAnalyzer;
import add.features.codefeatures.codeanalyze.MethodAnalyzer;
import add.features.codefeatures.codeanalyze.TypeaccessAnalyzer;
import add.features.codefeatures.codeanalyze.VariableAnalyzer;
import add.features.codefeatures.codeanalyze.WholeStatementAnalyzer;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;


public class CodeFeatureDetector {
	
	public Cntx<?> analyzeFeatures(CtElement element, List<CtExpression> allExpressions, 
			List<CtExpression> allrootlogicalexpers, List<CtBinaryOperator> allBinOperators) {
		
		CodeElementInfo infoElementStudy = new CodeElementInfo (element, allExpressions, allrootlogicalexpers, allBinOperators);
		
		List<AbstractCodeAnalyzer> analyzers = new ArrayList<>();
		
		analyzers.add(new VariableAnalyzer(infoElementStudy));
		
		analyzers.add(new BinaryOperatorAnalyzer(infoElementStudy));
		
		analyzers.add(new ConstantAnalyzer(infoElementStudy));

		analyzers.add(new ConstructorAnalyzer(infoElementStudy));

	//	analyzers.add(new ExpressionAnalyzer(infoElementStudy));

		analyzers.add(new LogicalExpressionAnalyzer(infoElementStudy));
		
		analyzers.add(new TypeaccessAnalyzer(infoElementStudy));

		analyzers.add(new MethodAnalyzer(infoElementStudy));

		analyzers.add(new WholeStatementAnalyzer(infoElementStudy));
		
		for(int index=0; index<analyzers.size(); index++) {
			analyzers.get(index).analyze();
		}

		return infoElementStudy.context;
	}
}
