 package org.jboss.tools.jst.jsp.test.ca;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor;
 import org.jboss.tools.jst.jsp.test.TestUtil;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.WorkbenchUtils;
 
 public class ContentAssistantTestCase extends TestCase {
 	protected IProject project = null;
 	protected JSPMultiPageEditor jspEditor = null;
 	protected JSPTextEditor jspTextEditor = null;
 	protected StructuredTextViewer viewer = null;
 	protected IContentAssistant contentAssistant = null;
 	protected IDocument document = null;
 
 	protected void openEditor(String fileName) {
 		IEditorPart editorPart = WorkbenchUtils.openEditor(project.getName()+"/"+ fileName);

 		if (editorPart instanceof JSPMultiPageEditor)
 			jspEditor = (JSPMultiPageEditor) editorPart;
 
 		jspTextEditor = jspEditor.getJspEditor();
 		viewer = jspTextEditor.getTextViewer();
 		document = viewer.getDocument();
 		SourceViewerConfiguration config = TestUtil
 				.getSourceViewerConfiguration(jspTextEditor);
 		contentAssistant = (config == null ? null : config
 				.getContentAssistant(viewer));
 
 		assertTrue(
 				"Cannot get the Content Assistant instance for the editor for page \""
 						+ fileName + "\"", (contentAssistant != null));
 
		assertTrue("The IDocument is not instance of IStructuredDocument",
 				(document instanceof IStructuredDocument));
 
 	}
 
 	protected ICompletionProposal[] checkProposals(String fileName, int offset, String[] proposals, boolean exactly) {
         return checkProposals(fileName, null, offset, proposals, exactly);
     }
 
 	protected ICompletionProposal[] checkProposals(String fileName, String substring, int offset, String[] proposals, boolean exactly){
 		openEditor(fileName);
 
         int position = 0;
         if (substring != null) {
             String documentContent = document.get();
             position = documentContent.indexOf(substring);
         }
 
         ICompletionProposal[] result = null;
 
         IContentAssistProcessor p = TestUtil.getProcessor(viewer, position + offset, contentAssistant);
         if (p != null) {
             try {
                 result = p.computeCompletionProposals(viewer, position + offset);
             } catch (Throwable x) {
                 x.printStackTrace();
             }
         }
 
         assertTrue("Content Assistant returned no proposals", (result != null && result.length > 0));
 
         // for (int i = 0; i < result.length; i++) {
         // System.out.println("proposal - "+result[i].getDisplayString());
         // }
 
         for (int i = 0; i < proposals.length; i++) {
             assertTrue("Proposal " + proposals[i] + " not found!", compareProposal(proposals[i], result));
         }
 
         if (exactly) {
             assertTrue("Some other proposals was found!", result.length == proposals.length);
         }
 		return result;
 	}
 
 	protected boolean compareProposal(String proposalName, ICompletionProposal[] proposals){
 		for (int i = 0; i < proposals.length; i++) {
 			if (proposals[i] instanceof AutoContentAssistantProposal) {
 				AutoContentAssistantProposal ap = (AutoContentAssistantProposal)proposals[i];
 				String replacementString = ap.getReplacementString().toLowerCase();
 				if (replacementString.equalsIgnoreCase(proposalName)) return true;
 				
 				// For a tag proposal there will be not only the the tag name but all others characters like default attributes, tag ending characters and so on
 				String[] replacementStringParts = replacementString.split(" ");
 				if (replacementStringParts != null && replacementStringParts.length > 0) {
 					if (replacementStringParts[0].equalsIgnoreCase(proposalName)) return true;
 				}
 				
 				// for an attribute proposal there will be a pare of attribute-value (i.e. attrName="attrValue")
 				replacementStringParts = replacementString.split("=");
 				if (replacementStringParts != null && replacementStringParts.length > 0) {
 					if (replacementStringParts[0].equalsIgnoreCase(proposalName)) return true;
 				}
 				
 				// For an attribute value proposal there will be the quote characters
 				replacementString = Utils.trimQuotes(replacementString);
 				if (replacementString.equalsIgnoreCase(proposalName)) return true;
 				
 				
 				
 			} else {
 				if(proposals[i].getDisplayString().toLowerCase().equals(proposalName.toLowerCase())) return true;
 			}
 		}
 		return false;
 	}
 
 	protected void closeEditor() {
 		if (jspEditor != null) {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().closeEditor(jspEditor, false);
 			jspEditor = null;
 		}
 	}
 }
