 package org.jboss.tools.jst.jsp.jspeditor.dnd;
 
 import java.util.Properties;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
 import org.jboss.tools.jst.web.tld.URIConstants;
 import org.jboss.tools.jst.web.tld.model.helpers.TLDToPaletteHelper;
 
 public class JSPPaletteInsertHelper extends PaletteInsertHelper {
     public static final String PROPOPERTY_ADD_TAGLIB = TLDToPaletteHelper.ADD_TAGLIB;
 	public static final String PROPOPERTY_TAGLIBRARY_URI = URIConstants.LIBRARY_URI;
 	public static final String PROPOPERTY_TAGLIBRARY_VERSION = URIConstants.LIBRARY_VERSION;
 	public static final String PROPOPERTY_DEFAULT_PREFIX = URIConstants.DEFAULT_PREFIX;
 
 	static JSPPaletteInsertHelper instance = new JSPPaletteInsertHelper();
 
 	public static JSPPaletteInsertHelper getInstance() {
 		return instance;
 	}
 
     static PaletteTaglibInserter PaletteTaglibInserter = new PaletteTaglibInserter();
 
     public JSPPaletteInsertHelper() {}
 
 	protected void modify(ISourceViewer v, Properties p, String[] texts) {
 		p.put("viewer", v);
 		String tagname = p.getProperty(PROPOPERTY_TAG_NAME);
 		String uri = p.getProperty(PROPOPERTY_TAGLIBRARY_URI);
 		String startText = texts[0];
 		if(startText != null && startText.startsWith("<%@ taglib")) { //$NON-NLS-1$
 			if(PaletteTaglibInserter.inserTaglibInXml(v.getDocument(), p)) {
 				texts[0] = "";
 				return;
 			}
 		} else {
 			p = PaletteTaglibInserter.inserTaglib(v.getDocument(), p);
 		}
 	
 		String defaultPrefix = p.getProperty(PROPOPERTY_DEFAULT_PREFIX);
 		IDocument d = v.getDocument();
 		applyPrefix(texts, d, tagname, uri, defaultPrefix);						
 	}
 
 
 
 	/**
 	 * adding prefix to tag
 	 */
 	public static void applyPrefix(String[] text, ITextEditor editor, String tagname, String uri, String defaultPrefix) {
 		if(defaultPrefix == null || defaultPrefix.length() == 0) return;
 		IDocument doc = null;
     	if(editor != null && editor.getDocumentProvider() != null) {
     		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
     	}
     	applyPrefix(text, doc, tagname, uri, defaultPrefix);
 	}
 
 	public static void applyPrefix(String[] text, IDocument doc, String tagname, String uri, String defaultPrefix) {
         if(doc == null) return;
         String body = doc.get();
         applyPrefix(text, body, tagname, uri, defaultPrefix);
 	}
 
 	public static void applyPrefix(String[] text, String body, String tagname, String uri, String defaultPrefix) {
 		if(uri == null || uri.length() == 0) return;
 		Properties p = getPrefixes(body);
 		String prefix = p.getProperty(uri, defaultPrefix);
 		if(prefix == null || prefix.length() == 0) return;				
 		for (int i = 0; i < text.length; i++) text[i] = applyPrefix(text[i], tagname, prefix, p);
 	}
 
 	static String applyPrefix(String text, String tagname, String prefix, Properties prefixes) {
 		if(text == null || text.length() == 0) return text;
 		if(tagname == null || tagname.length() == 0) return text;
 		while(true) {
 			int i = text.indexOf("%prefix|"); //$NON-NLS-1$
 			if(i < 0) break;
 			int j = text.indexOf("%", i + 8); //$NON-NLS-1$
 			if(j < 0) break;
 			int j1 = text.indexOf("|", i + 8); //$NON-NLS-1$
 			String uri = ""; //$NON-NLS-1$
 			String defaultPrefix = ""; //$NON-NLS-1$
 			String pr = ""; //$NON-NLS-1$
 			uri = text.substring(i + 8, j1);
 			defaultPrefix = text.substring(j1 + 1, j);
 			pr = prefixes.getProperty(uri, defaultPrefix);
 			if(pr.length() > 0) {
 				text = text.substring(0, i) + pr + ":" + text.substring(j + 1); //$NON-NLS-1$
 			} else {
 				text = text.substring(0, i) + text.substring(j + 1);
 			}
 		}
 
 		int k = text.toLowerCase().indexOf(":" + tagname.toLowerCase()); //$NON-NLS-1$
 		if(k >= 0) {
 			int g = text.indexOf("</"); //$NON-NLS-1$
 			if(g >= 0 && g < k) {
 				return text.substring(0, g + 2) + prefix + text.substring(k);
 			}
 			g = text.indexOf("<"); //$NON-NLS-1$
 			if(g >= 0 && g < k) {
 				return text.substring(0, g + 1) + prefix + text.substring(k);
 			}
 		}
 		k = text.toLowerCase().indexOf("<" + tagname.toLowerCase()); //$NON-NLS-1$
 		if(k >= 0) {
 			return text.substring(0, k + 1) + prefix + ":" + text.substring(k + 1); //$NON-NLS-1$
 		}
 		k = text.toLowerCase().indexOf("</" + tagname.toLowerCase()); //$NON-NLS-1$
 		if(k >= 0) {
 			return text.substring(0, k + 2) + prefix + ":" + text.substring(k + 2); //$NON-NLS-1$
 		}
 		return text;
 	}
 
 	static Properties getPrefixes(String body) {
 		Properties p = new Properties();
 		int i = 0;
 		while(i >= 0 && i < body.length()) {
 			i = body.indexOf("<%@ taglib ", i); //$NON-NLS-1$
 			if(i < 0) break;
 			int j = body.indexOf("%>", i); //$NON-NLS-1$
 			if(j < 0) j = body.length();
 			String taglib = body.substring(i, j);
 			getPrefix(p, taglib);
 			i = j + 1;
 		}
 		return p;
 	}
 
 	static void getPrefix(Properties p, String taglib) {
 		int i = taglib.indexOf("uri=\""); //$NON-NLS-1$
 		if(i < 0) return;
 		int j = taglib.indexOf("\"", i + 5); //$NON-NLS-1$
 		if(j < 0) return;
 		String uri = taglib.substring(i + 5, j);
 		i = taglib.indexOf("prefix=\""); //$NON-NLS-1$
 		if(i < 0) return;
 		j = taglib.indexOf("\"", i + 8); //$NON-NLS-1$
 		if(j < 0) return;
 		String prefix = taglib.substring(i + 8, j);
 		p.setProperty(uri, prefix);
 	}
 
 }
