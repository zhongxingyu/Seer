 package jp.hishidama.xtext.dmdl_editor.ui.wizard.page.operator.gen;
 
 import java.util.List;
 
 import jp.hishidama.eclipse_plugin.asakusafw_wrapper.operator.OperatorType;
 import jp.hishidama.xtext.dmdl_editor.ui.wizard.page.operator.OperatorInputModelRow;
 import jp.hishidama.xtext.dmdl_editor.ui.wizard.page.operator.OperatorOutputModelRow;
 
 import org.eclipse.jdt.core.dom.Annotation;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.Javadoc;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 
 public class CoGroupOperatorGenerator extends OperatorGenerator {
 
 	@Override
 	protected Annotation getAnnotation() {
 		return newMarkerAnnotation(OperatorType.CO_GROUP.getTypeName());
 	}
 
 	@Override
 	protected String getReturnTypeName(Javadoc javadoc) {
 		return "void";
 	}
 
 	@Override
 	protected void getParameters(List<SingleVariableDeclaration> plist, Javadoc javadoc) {
 		List<OperatorInputModelRow> ilist = getInputModelList();
 		for (OperatorInputModelRow row : ilist) {
			plist.add(newListParameter(row.modelClassName, row.name, row.keyList, row.orderList));
 			addJavadocParam(javadoc, row.name, row.getLabel());
 		}
 
 		List<OperatorOutputModelRow> olist = getOutputModelList();
 		for (OperatorOutputModelRow row : olist) {
 			plist.add(newResultParameter(row.modelClassName, row.name));
 			addJavadocParam(javadoc, row.name, row.getLabel());
 		}
 	}
 
 	@Override
 	protected Block getBody() {
 		return newEmptyBlock();
 	}
 }
