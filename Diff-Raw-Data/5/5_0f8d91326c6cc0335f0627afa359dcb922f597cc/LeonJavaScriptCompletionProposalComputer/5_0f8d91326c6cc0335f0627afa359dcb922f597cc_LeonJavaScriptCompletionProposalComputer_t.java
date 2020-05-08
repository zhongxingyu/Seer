 package io.leon.eclipseintegration.ui.contentassist;
 
 import io.leon.eclipseintegration.ui.Activator;
 import io.leon.eclipseintegration.ui.natures.LeonNature;
 import io.leon.eclipseintegration.ui.properties.PreferencesHandler;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.templates.DocumentTemplateContext;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateContext;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateProposal;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IPathEditorInput;
 import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
 import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;
 
 public class LeonJavaScriptCompletionProposalComputer implements
 		IJavaCompletionProposalComputer {
 
 	private static final List<ICompletionProposal> NO_PROPOSALS = new ArrayList<ICompletionProposal>();
 
 	public LeonJavaScriptCompletionProposalComputer() {
 	}
 
 	public void sessionStarted() {
 	}
 
 	public List<ICompletionProposal> computeCompletionProposals(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		if (Activator.getDefault().getWorkbench() == null
 				|| Activator.getDefault().getWorkbench()
 						.getActiveWorkbenchWindow() == null
 				|| Activator.getDefault().getWorkbench()
 						.getActiveWorkbenchWindow().getActivePage() == null
 				|| Activator.getDefault().getWorkbench()
 						.getActiveWorkbenchWindow().getActivePage()
 						.getActiveEditor() == null) {
 			return NO_PROPOSALS;
 		}
 
 		IEditorPart editor = Activator.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		IEditorInput input = editor.getEditorInput();
 		if (input instanceof IPathEditorInput) {
 			IPathEditorInput pathInput = (IPathEditorInput) input;
 			IPath path = pathInput.getPath();
 			
 			IProject project = ((IFileEditorInput) input).getFile()
 					.getProject();
 			
			// from path.makeRelativeTo(project.getLocation()) the returned file always has "/" as separator, so use this
			String filename = "/" + path.makeRelativeTo(project.getLocation());
 			
 			String configurationFile = new PreferencesHandler(project).getPreferences().getConfigurationFile();
			configurationFile = configurationFile.replace("\\", "/");
 
 			if (filename == null || !filename.equals(configurationFile)) {
 				return NO_PROPOSALS;
 			}
 
 			try {
 				boolean hasLeonNature = project.hasNature(LeonNature.NATURE_ID);
 				if (!hasLeonNature) {
 					return NO_PROPOSALS;
 				}
 			} catch (Exception e) {
 				return NO_PROPOSALS;
 			}
 		} else {
 			return NO_PROPOSALS;
 		}
 
 		ITextViewer viewer = context.getViewer();
 		int offset = context.getInvocationOffset();
 
 		try {
 			String prefix = getPrefix(viewer, offset);
 			return getSuggestions(viewer, offset, prefix);
 		} catch (BadLocationException x) {
 			// ignore and return no proposals
 			return NO_PROPOSALS;
 		}
 	}
 
 	private String getPrefix(ITextViewer viewer, int offset)
 			throws BadLocationException {
 		IDocument doc = viewer.getDocument();
 		if (doc == null || offset > doc.getLength())
 			return null;
 
 		IRegion lineInfos = doc.getLineInformationOfOffset(offset);
 		int lineStartOffset = lineInfos.getOffset();
 
 		int parsingPointer = 0;
 		if (lineStartOffset > 0 && lineStartOffset <= offset) {
 			parsingPointer = lineStartOffset;
 		}
 
 		int length = 0;
 		while (--offset >= parsingPointer
 				&& Character.isJavaIdentifierPart(doc.getChar(offset)))
 			length++;
 
 		return doc.get(offset + 1, length);
 	}
 
 	/**
 	 * Create the array of suggestions. It scans all open text editors and
 	 * prefers suggestions from the currently open editor. It also adds the
 	 * empty suggestion at the end.
 	 * 
 	 * @param viewer
 	 *            the viewer
 	 * @param offset
 	 *            the offset
 	 * @param prefix
 	 *            the prefix to search for
 	 * @return the list of all possible suggestions in the currently open
 	 *         editors
 	 * @throws BadLocationException
 	 *             if accessing the current document fails
 	 */
 	private List<ICompletionProposal> getSuggestions(ITextViewer viewer,
 			int offset, String prefix) throws BadLocationException {
 
 		Properties properties = loadProposalFile();
 
 		Map<String, String> filteredSuggestions = new HashMap<String, String>();
 
 		for (Entry<Object, Object> property : properties.entrySet()) {
 			if (((String) property.getValue()).startsWith(prefix)) {
 				filteredSuggestions.put((String) property.getKey(),
 						(String) property.getValue());
 			}
 		}
 
 		List<ICompletionProposal> suggestions = createProposals(viewer,
 				filteredSuggestions, offset, prefix);
 
 		return suggestions;
 	}
 
 	public List<?> computeContextInformation(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		return new ArrayList<ICompletionProposal>();
 	}
 
 	public String getErrorMessage() {
 		return null;
 	}
 
 	public void sessionEnded() {
 	}
 
 	private Properties loadProposalFile() {
 		Properties proposals = new Properties();
 		try {
 			InputStream is = getClass().getResourceAsStream(
 					"proposals.properties");
 
 			if (is != null) {
 				proposals.load(is);
 			}
 
 		} catch (Exception e) {
 			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID,
 					"Error reading leon proposals", e);
 			Activator.getDefault().getLog().log(status);
 		}
 
 		return proposals;
 	}
 
 	private List<ICompletionProposal> createProposals(ITextViewer viewer,
 			Map<String, String> suggestions, int offset, String prefix) {
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		for (Entry<String, String> property : suggestions.entrySet()) {
 			String name = property.getKey();
 			String value = property.getValue();
 			String[] values = value.split(";");
 
 			if (values.length > 0) {
 				String pattern = values[0];
 				String description = "";
 				String documentation = null;
 
 				if (values.length > 1) {
 					description = values[1];
 				}
 
 				if (values.length > 2) {
 					documentation = values[2];
 				}
 
 				Template template = new Template(name, description,
 						"leon", pattern, false);
 
 				Region region = new Region(offset - prefix.length(),
 						prefix.length());
 				TemplateProposal templateProposal = new LeonTemplateProposal(
 						template, documentation, createContext(viewer, region),
 						region, Activator.getImage("leonProposalIcon"));
 
 				proposals.add(templateProposal);
 			}
 		}
 
 		return proposals;
 	}
 
 	private TemplateContext createContext(ITextViewer viewer, IRegion region) {
 		IDocument document = viewer.getDocument();
 		return new DocumentTemplateContext(new TemplateContextType("leon"), document,
 				region.getOffset(), region.getLength());
 	}
 }
