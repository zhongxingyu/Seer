 package org.ow2.mind.adl.annotations;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.objectweb.fractal.adl.interfaces.Interface;
 import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
 import org.ow2.mind.adl.ast.ASTHelper;
 import org.ow2.mind.adl.ast.Component;
 import org.ow2.mind.adl.ast.DefinitionReference;
 import org.ow2.mind.adl.ast.MindInterface;
 import org.ow2.mind.adl.ast.Source;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Definition;
 import org.ow2.mind.adl.ast.Binding;
 import org.ow2.mind.adl.implementation.BasicImplementationLocator;
 import org.objectweb.fractal.adl.types.TypeInterface;
 
 
 public class DotWriter {
 	//public static final String            DUMP_DOT = "DumpDot";
 	private PrintWriter currentPrinter;
 	private String compName;
 	private String localName;
 	private String buildDir;
 	private String fileName;
 	private String srcs="{\ncolor=none;\n";
 	private int srcNb=0;
 	private String srvItfs="{rank=source; color=none; ";
 	private int srvItfsNb=0;
 	private String cltItfs="{rank=sink; color=none; ";;
 	private int cltItfsNb=0;
 	private int maxItf=0; // Used to adapt the size of composite interface boxes
 	private int color=1;
 	private Map<Object, Object> context;
 
 	//@Inject
 	//@Named(DUMP_DOT)
 	public BasicImplementationLocator implementationLocatorItf = new BasicImplementationLocator();
 	
 	public DotWriter(String dir, String name, Component component, Map<Object, Object> cont) {
 		context=cont;
 		try {
 			compName = name;
 			final int i = name.lastIndexOf('.');
 			if (i == -1 ) { 
 				localName = name;
 			} else {
 				localName = name.substring(i + 1);
 			}
 			buildDir = dir;
 			fileName = buildDir + File.separator + compName + ".gv";
 			currentPrinter = new PrintWriter( new FileWriter( fileName ) );
 			String adlSource = null;
 			if (component!=null)
 				try {
 					adlSource = ASTHelper.getResolvedDefinition(component.getDefinitionReference(),null,null).astGetSource().split(":")[0];
 				} catch (ADLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			writeHeader(adlSource);
 		} catch ( final IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void writeHeader(String adlSource) {
 		currentPrinter.println("digraph " + localName + " {");
 		currentPrinter.println("rankdir=LR;");
 		currentPrinter.println("ranksep=3;");
 		currentPrinter.println("subgraph cluster_membrane {" );
 		if (adlSource != null) currentPrinter.println("URL=\"" + adlSource + "\"");
 		currentPrinter.println("penwidth=15;");
 		currentPrinter.println("color=blue;");
 		currentPrinter.println("style=rounded;");
 		//currentPrinter.println("height=20;"); // max number of itf /50*18 
 
 
 	}
 
 	public void addSubComponent(Component component) {
 		try {
 			int clientItf = 0;
 			int serverItf = 0;
 			DefinitionReference defRef = component.getDefinitionReference();
 			final Definition definition = ASTHelper.getResolvedDefinition(defRef, null, null);
 			currentPrinter.print(component.getName() + "Comp [URL=\"" + compName + "." + component.getName() + ".gv\",shape=Mrecord,style=filled,fillcolor=lightgrey,label=\"" + component.getName() + " | {{ " );
 			if (definition instanceof InterfaceContainer) {
 
 				TreeSet<MindInterface> interfaces = new TreeSet<MindInterface>(new MindInterfaceComparator());
 				for (Interface itf : ((InterfaceContainer) definition).getInterfaces())
 					interfaces.add((MindInterface) itf); 
 				//final Interface[] interfaces = ((InterfaceContainer) definition).getInterfaces();
 				//			for (int i = 0; i < interfaces.length; i++) {
 				//				final MindInterface itf = (MindInterface) interfaces[i];
 				for (MindInterface itf : interfaces) {
 					if (itf.getRole()==TypeInterface.SERVER_ROLE) {
 						if ( serverItf !=0 ) currentPrinter.print(" | ");
 						currentPrinter.print("<" + itf.getName() + "> " + itf.getName());
 						serverItf++;
 						//itf.getSignature()); //TODO might put this info somwhere latter
 					}
 				}
 				currentPrinter.print(" } | | { ");
 				//			for (int i = 0; i < interfaces.length; i++) {
 				//				final MindInterface itf = (MindInterface) interfaces[i];	
 				for (MindInterface itf : interfaces) {	
 					if (itf.getRole()==TypeInterface.CLIENT_ROLE) {
 						if ( clientItf !=0 ) currentPrinter.print(" | ");
 						currentPrinter.print("<" + itf.getName() + "> " + itf.getName());
 						clientItf++;
 						//itf.getSignature());
 					}
 				}
 				currentPrinter.print(" }} | \" ];");
 				currentPrinter.println("");
 				if (clientItf > maxItf) maxItf = clientItf;
 				if (serverItf > maxItf) maxItf = serverItf;
 			}
 		} catch (final ADLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void addBinding(Binding binding) {
 		color++;
 		if (color >= 11) color=1;
 		String fc = binding.getFromComponent();
 		String fi = binding.getFromInterface();
 		String tc = binding.getToComponent();
 		String ti = binding.getToInterface();
 		String from = null;
 		String to = null;
 		if (fc == "this")
 			from = "Srv" + fi;
 		else
 			from = fc + "Comp:" + fi;
 
 		if (tc == "this") 
 			to = "Clt" + ti;
 		else
 			to = tc + "Comp:" + ti;
 		currentPrinter.println( from + "->" + to + "[tailport=e headport=w colorscheme=\"paired12\" color=" + color + "];");	
 	}
 
 	public void addSource(Source source) {
		String srcPath=source.getPath();
		if (srcPath != null) {
			URL url = implementationLocatorItf.findSource(srcPath, context);
 			String s;
 			try {
 				File f;
				f = new File( URLDecoder.decode( url.getFile().replace("+","%2B"), "UTF-8" ));
 				s = "\", URL=\"" + f.getAbsolutePath() + "\"";
 			} catch (UnsupportedEncodingException e) {
 				s = "";
 			}
 			srcs=srcs + srcNb + "[shape=note,label=\"" + source.getPath() + s + "];\n";
 			srcNb++;
 		}
 	}
 
 	public void addServer(String itfName, String itfURI) {
 		srvItfs=srvItfs + "Srv" + itfName + " [shape=Mrecord,style=filled,fillcolor=red,label=\"" + itfName + "\", URL=\"" + itfURI + "\", height=1 ];";
 		srvItfsNb++;
 	}
 
 	public void addClient(String itfName, String itfURI) {
 		cltItfs=cltItfs + "Clt" + itfName + " [shape=Mrecord,style=filled,fillcolor=green,label=\"" + itfName + "\", URL=\"" + itfURI + "\", height=1 ];";
 		cltItfsNb++;	
 	}
 
 	public void close() {
 		writeFooter();
 
 	}
 
 	private void writeFooter() {
 		if (cltItfsNb > maxItf) maxItf=cltItfsNb;
 		if (srvItfsNb > maxItf) maxItf=srvItfsNb;
 		if (srcNb > maxItf) maxItf=srcNb;
 		srvItfs=srvItfs + "}";
 		cltItfs=cltItfs + "}";
 		srcs=srcs + "}\n";
 		if (srvItfsNb > 0) currentPrinter.println(srvItfs);
 		if (cltItfsNb > 0) currentPrinter.println(cltItfs);
 		if (srcNb > 0) currentPrinter.println(srcs);
 		currentPrinter.println("}");
 		currentPrinter.println("}");
 		currentPrinter.close();
 	}
 }
