 package org.eclipse.gmf.codegen.templates.providers;
 
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.gmf.common.codegen.*;
 
 public class PaletteProviderGenerator
 {
   protected static String nl;
   public static synchronized PaletteProviderGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     PaletteProviderGenerator result = new PaletteProviderGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = NL + "/*" + NL + " * ";
   protected final String TEXT_3 = NL + " */";
  protected final String TEXT_4 = NL + NL + "import org.eclipse.core.runtime.IConfigurationElement;" + NL + "import org.eclipse.gef.palette.PaletteRoot;" + NL + "import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;" + NL + "import org.eclipse.gmf.runtime.common.core.service.IOperation;" + NL + "import org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.IPaletteProvider;" + NL + "import org.eclipse.ui.IEditorPart;";
   protected final String TEXT_5 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
  protected final String TEXT_6 = " extends AbstractProvider implements IPaletteProvider {" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void contributeToPalette(IEditorPart editor, Object content, PaletteRoot root) {";
   protected final String TEXT_7 = NL + "\t\t";
   protected final String TEXT_8 = " factory = new ";
   protected final String TEXT_9 = "();" + NL + "\t\tfactory.fillPalette(root);";
   protected final String TEXT_10 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void setContributions(IConfigurationElement configElement) {" + NL + "\t\t// no configuration" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic boolean provides(IOperation operation) {" + NL + "\t\treturn false; // all logic is done in the service" + NL + "\t}" + NL + "}";
   protected final String TEXT_11 = NL;
 
   public String generate(Object argument)
   {
     StringBuffer stringBuffer = new StringBuffer();
     
 final GenDiagram genDiagram = (GenDiagram) ((Object[]) argument)[0];
 final ImportAssistant importManager = (ImportAssistant) ((Object[]) argument)[1];
 
     stringBuffer.append(TEXT_1);
     
 String copyrightText = genDiagram.getEditorGen().getCopyrightText();
 if (copyrightText != null && copyrightText.trim().length() > 0) {
 
     stringBuffer.append(TEXT_2);
     stringBuffer.append(copyrightText.replaceAll("\n", "\n * "));
     stringBuffer.append(TEXT_3);
     }
     importManager.emitPackageStatement(stringBuffer);
     stringBuffer.append(TEXT_4);
     importManager.markImportLocation(stringBuffer);
     stringBuffer.append(TEXT_5);
     stringBuffer.append(genDiagram.getPaletteProviderClassName());
     stringBuffer.append(TEXT_6);
     if (genDiagram.getPalette() != null) {
     stringBuffer.append(TEXT_7);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPalette().getFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_8);
     stringBuffer.append(importManager.getImportedName(genDiagram.getPalette().getFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_9);
     }
     stringBuffer.append(TEXT_10);
     importManager.emitSortedImports();
     stringBuffer.append(TEXT_11);
     return stringBuffer.toString();
   }
 }
