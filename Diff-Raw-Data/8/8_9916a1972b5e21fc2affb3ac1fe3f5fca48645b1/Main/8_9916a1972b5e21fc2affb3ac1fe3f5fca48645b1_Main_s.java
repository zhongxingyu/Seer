 package test.cases;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.zkoss.zul.Hbox;
 
 import test.util.XMLConverter;
 
 public class Main {
 	private static int count = 0;
 	private static final List<String> comps = Arrays.asList(new String[] {
 			"Div", "Panel", "Window", "Groupbox", "Tabbox", "Hlayout", "Hbox",
 			"Vlayout", "Vbox" });
 	private static final List<String> inps = Arrays.asList(new String[] {
 			"Bandbox", "Calendar", "Chosenbox", "Colorbox", "Combobox",
 			"Datebox", "Decimalbox", "Doublebox", "Intbox", "Longbox",
 			"Textbox", "Timebox", "Doublespinner", "Spinner", "Slider" });
 
 	public static void main(String[] args) throws Exception {
 
 		for (String comp : comps) {
 			outputCase(new VFlexCase(comp, false));
 
 			outputCase(new LayoutCase(comp, "Hlayout", false), 
 					   new LayoutCase(comp, "Hbox", false));
 			outputCase(new LayoutCase(comp, "Vlayout", false), 
 					   new LayoutCase(comp, "Vbox", false));
 
 			outputCase(new MinFlexCase(comp, "Hlayout", false),
 					   new MinFlexCase(comp, "Hbox", false));
 			outputCase(new MinFlexCase(comp, "Vlayout", false),
 					   new MinFlexCase(comp, "Vbox", false));
 		}
 
 		for (String inp : inps) {
 
 			outputCase(new VFlexCase(inp, true), new VFlexCase(inp, false));
 
 			outputCase(new LayoutCase(inp, "Hlayout", false), 
 					   new LayoutCase(inp, "Hbox", false), 
 					   new LayoutCase(inp, "Hlayout", true),
 					   new LayoutCase(inp, "Hbox", true));
 			outputCase(new LayoutCase(inp, "Vlayout", false), 
 					   new LayoutCase(inp, "Vbox", false), 
 					   new LayoutCase(inp, "Vlayout", true),
 					   new LayoutCase(inp, "Vbox", true));
 
 			outputCase(new MinFlexCase(inp, "Hlayout", false), 
 					   new MinFlexCase(inp, "Hbox", false), 
					   new MinFlexCase(inp, "Vlayout", true),
					   new MinFlexCase(inp, "Vbox", true));
			outputCase(new MinFlexCase(inp, "Hlayout", false), 
					   new MinFlexCase(inp, "Hbox", false), 
 					   new MinFlexCase(inp, "Vlayout", true),
 					   new MinFlexCase(inp, "Vbox", true));
 
 			outputCase(new InputHFlexCase(inp, false), 
 					   new InputHFlexCase(inp, true));
 		}
 
 	}
 
 	private static void save(String content, File file) {
 		FileWriter fileWriter = null;
 		try {
 			fileWriter = new FileWriter(file);
 			fileWriter.write(content);
 		} catch (IOException ex) {
 		} finally {
 			try {
 				fileWriter.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static File generateZTL() throws IOException {
 		String path = new StringBuffer("WebContent/Z65-Flex-")
 				.append(String.format("%03d", ++count)).append(".zul")
 				.toString();
 		File tmp = new File(path);
 		tmp.createNewFile();
 		return tmp;
 	}
 
 	private static void outputCase(FlexCase... flexCases) throws Exception {
 		StringBuffer flexCaseXML = new StringBuffer("<zk>");
 		Hbox hbox1 = new Hbox();
 		Hbox hbox2 = new Hbox();
 		for (int i = 0; i < flexCases.length; i++) {
 			FlexCase flexCase = flexCases[i];
 
 			switch (flexCases.length) {
 			case 1:
 				flexCaseXML.append(flexCase.getViewXML());
 				break;
 			case 2:
 				hbox1.appendChild(flexCase.getView());
 				if (i == 1)
 					flexCaseXML.append(new XMLConverter(hbox1).toXML());
 				break;
 			case 4:
 				if (i < 2)
 					hbox1.appendChild(flexCase.getView());
 				else if (i > 1)
 					hbox2.appendChild(flexCase.getView());
 				if (i == 1)
 					flexCaseXML.append(new XMLConverter(hbox1).toXML());
 				if (i == 3)
 					flexCaseXML.append(new XMLConverter(hbox2).toXML());
 				break;
 			}
 
 		}
 		flexCaseXML.append("</zk>");
 
 		File ztl = generateZTL();
 		save(flexCaseXML.toString(), ztl);
 		System.out.println("save to file " + ztl.getName());
 		System.out.println("file contnet: \n" + flexCaseXML + "\n");
 	}
 }
