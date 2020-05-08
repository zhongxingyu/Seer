 package net.todd.biblestudy.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.widgets.Button;
 
 public class ViewerUtils {
	public static <T> void setSingleSelection(StructuredViewer structuredViewer, T element) {
 		if (element != null) {
 			structuredViewer.setSelection(new StructuredSelection(element));
 		}
 	}
 
	public static <T> void setSelectionList(StructuredViewer structuredViewer, List<T> elements) {
		if (elements != null) {
			structuredViewer.setSelection(new StructuredSelection(elements));
		}
	}

 	public static int getButtonWidth(Button button) {
 		GC gc = new GC(button);
 		gc.setFont(button.getFont());
 		FontMetrics fontMetrics = gc.getFontMetrics();
 
 		int minimumSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
 		int widthHint = (fontMetrics.getAverageCharWidth() * IDialogConstants.BUTTON_WIDTH + 2) / 4;
 
 		return Math.max(minimumSize, widthHint);
 	}
 
 	public static <T> T getSelection(StructuredViewer structuredViewer, Class<T> clazz) {
 		StructuredSelection structuredSelection = (StructuredSelection) structuredViewer.getSelection();
 		T selectedObject = null;
 		if (structuredSelection != null) {
 			selectedObject = clazz.cast(structuredSelection.getFirstElement());
 		}
 		return selectedObject;
 	}
 
 	public static <T> List<T> getListSelection(TableViewer structuredViewer, Class<T> clazz) {
 		StructuredSelection structuredSelection = (StructuredSelection) structuredViewer.getSelection();
 		List<?> selection = structuredSelection.toList();
 
 		List<T> castedSelection = new ArrayList<T>();
 		for (Object object : selection) {
 			castedSelection.add(clazz.cast(object));
 		}
 
 		return castedSelection;
 	}
 }
