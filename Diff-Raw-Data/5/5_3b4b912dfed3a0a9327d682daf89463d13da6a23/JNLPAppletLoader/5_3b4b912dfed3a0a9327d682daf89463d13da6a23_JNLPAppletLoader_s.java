 package org.lwjgl.util.jnlp.applet;
 
 import java.applet.Applet;
 import java.applet.AppletStub;
 import java.awt.BorderLayout;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JEditorPane;
 
 import org.lwjgl.util.applet.AppletLoader;
 
 public class JNLPAppletLoader extends Applet implements AppletStub {
 
 	private static final long serialVersionUID = -2459790398016588477L;
 
 	URL codeBase;
 
 	Map<String, String> appletParameters = new HashMap<String, String>();
 
 	static String jnlpParameterName = "al_jnlp";
 
 	private JNLPParser jnlpParser;
 
 	private URLBuilder urlBuilder;
 
 	public void setJnlpParser(JNLPParser jnlpParser) {
 		this.jnlpParser = jnlpParser;
 	}
 
 	public void setUrlBuilder(URLBuilder urlBuilder) {
 		this.urlBuilder = urlBuilder;
 	}
 
 	public JNLPAppletLoader() {
 		urlBuilder = new URLBuilder();
 		jnlpParser = new JNLPParser(urlBuilder);
 	}
 
 	@Override
 	public void init() {
 
 		try {
 			// starts using the default codebase
 			codeBase = super.getCodeBase();
 
 			String jnlpHref = getParameter(jnlpParameterName);
 
 			if (jnlpHref == null)
 				throw new RuntimeException("Missing required parameter " + jnlpParameterName);
 
 			URL jnlpUrl = urlBuilder.build(codeBase, jnlpHref);
 
 			JNLPInfo jnlpInfo = getMergedJnlp(jnlpUrl);
 			
 			new JnlpPrinter().printJnlpInfo(jnlpInfo);
 
 			// replaces codebase with jnlp codebase
 			// codeBase = new URL(jnlpInfo.codeBase);
			codeBase = urlBuilder.build(codeBase, jnlpInfo.codeBase);
 
 			AppletLoaderParametersBuilder appletLoaderParametersBuilder = new AppletLoaderParametersBuilder(jnlpInfo);
 			appletParameters.putAll(appletLoaderParametersBuilder.getParametersFromJnlpInfo());
 
 			System.out.println(appletParameters);
 
 			AppletLoader appletLoader = new AppletLoader();
 			appletLoader.setStub(this);
 
 			appletLoader.init();
 			appletLoader.start();
 
 			setLayout(new BorderLayout());
 
 			this.add(appletLoader);
 
 		} catch (Exception e) {
 
 			setLayout(new BorderLayout());
 
 			String html = MessageFormat.format("<div align=\"center\">{0}</div>", e.getMessage());
 
 			JEditorPane jEditorPane = new JEditorPane("text/html", html);
 			jEditorPane.setEditable(false);
 			add(jEditorPane);
 
 			e.printStackTrace(System.out);
 		}
 
 	}
 
 	public JNLPInfo getMergedJnlp(URL jnlpUrl) {
 		JNLPInfo jnlpInfo = jnlpParser.parseJnlp(jnlpUrl);
 		
 		JnlpMerger jnlpMerger = new JnlpMerger();
 		jnlpMerger.setJnlpParser(jnlpParser);
 		jnlpMerger.setUrlBuilder(urlBuilder);
 		
 		jnlpMerger.mergeWithExtensions(jnlpInfo, codeBase);
 		
 		return jnlpInfo;
 	}
 
 	@Override
 	public void appletResize(int width, int height) {
 		resize(width, height);
 	}
 
 	@Override
 	public URL getCodeBase() {
 		return codeBase;
 	}
 
 	@Override
 	public String getParameter(String name) {
 		if (appletParameters.containsKey(name))
 			return appletParameters.get(name);
 		return super.getParameter(name);
 	}
 
 }
