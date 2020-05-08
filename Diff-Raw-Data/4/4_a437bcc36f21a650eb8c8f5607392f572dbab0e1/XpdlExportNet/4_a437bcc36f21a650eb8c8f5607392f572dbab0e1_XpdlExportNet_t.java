 package org.processmining.plugins.xpdl.exporting;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 
 import org.processmining.contexts.cli.CLIPluginContext;
 import org.processmining.contexts.uitopia.UIPluginContext;
 import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExt;
 
 import org.processmining.plugins.xpdl.Xpdl;
 
 import org.processmining.plugins.xpdl.converter.BPMN2XPDLConversionExt;
 
 @Plugin(name = "XPDL export Bussines Notation with Artifact", returnLabels = {}, returnTypes = {}, parameterLabels = { "XPDL open",
 		 "File" }, userAccessible = true)
 @UIExportPlugin(description = "xpdl files", extension = "xpdl")
 public class XpdlExportNet {
 	
 	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export  File")
 	public void exportXPDLtoFile(UIPluginContext context, Xpdl net, File file) throws IOException {
 		
 		
 		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + net.exportElement();
 
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
 		bw.write(text);
 		bw.close();
 		 
 	}
 	
 	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export  File")
 	public void exportXPDLtoFile(UIPluginContext context, BPMNDiagramExt brnet, File file) throws IOException {
 		
 		
 		/*Xpdl net = brnet.getXpdltraslate();
 		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + net.exportElement();
 
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
 		bw.write(text);
 		bw.close();*/
 		
 		BPMN2XPDLConversionExt xpdlConversion = new BPMN2XPDLConversionExt(brnet);
 		Xpdl xpdl = xpdlConversion.fills_layout(context);
 
 		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xpdl.exportElement();
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
 		bw.write(text);
 		bw.close();
 		
 	}
 	
 	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export  File")
 	public void exportXPDLtoFile(CLIPluginContext context, BPMNDiagramExt brnet, File file) throws IOException {
 		
 		
 		/*Xpdl net = brnet.getXpdltraslate();
 		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + net.exportElement();
 
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
 		bw.write(text);
 		bw.close();*/
 		
 		BPMN2XPDLConversionExt xpdlConversion = new BPMN2XPDLConversionExt(brnet);
		Xpdl xpdl = xpdlConversion.fills_nolayout();
		//Xpdl xpdl = xpdlConversion.fills_layout(context);
 
 		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xpdl.exportElement();
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
 		bw.write(text);
 		bw.close();
 		
 	}
 }
