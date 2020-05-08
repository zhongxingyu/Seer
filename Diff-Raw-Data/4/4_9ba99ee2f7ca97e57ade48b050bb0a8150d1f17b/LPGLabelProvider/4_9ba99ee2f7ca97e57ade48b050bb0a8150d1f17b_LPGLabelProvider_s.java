 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 /*
  * Created on Jul 7, 2006
  */
 package org.eclipse.imp.lpg.editor;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.imp.editor.ModelTreeNode;
 import org.eclipse.imp.lpg.ILPGResources;
 import org.eclipse.imp.lpg.LPGRuntimePlugin;
 import org.eclipse.imp.lpg.parser.LPGParser.ASTNode;
 import org.eclipse.imp.lpg.parser.LPGParser.ASTNodeToken;
 import org.eclipse.imp.lpg.parser.LPGParser.AliasSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.DefineSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.EofSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.ExportSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.GlobalsSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.HeadersSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.IdentifierSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.ImportSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.IncludeSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.KeywordsSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.LPG;
 import org.eclipse.imp.lpg.parser.LPGParser.LPG_itemList;
 import org.eclipse.imp.lpg.parser.LPGParser.NoticeSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.RulesSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.SYMBOLList;
 import org.eclipse.imp.lpg.parser.LPGParser.StartSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.TerminalsSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.TrailersSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.TypesSeg;
 import org.eclipse.imp.lpg.parser.LPGParser.action_segment;
 import org.eclipse.imp.lpg.parser.LPGParser.action_segmentList;
 import org.eclipse.imp.lpg.parser.LPGParser.defineSpec;
 import org.eclipse.imp.lpg.parser.LPGParser.defineSpecList;
 import org.eclipse.imp.lpg.parser.LPGParser.drop_command0;
 import org.eclipse.imp.lpg.parser.LPGParser.drop_command1;
 import org.eclipse.imp.lpg.parser.LPGParser.drop_commandList;
 import org.eclipse.imp.lpg.parser.LPGParser.drop_rule;
 import org.eclipse.imp.lpg.parser.LPGParser.drop_ruleList;
 import org.eclipse.imp.lpg.parser.LPGParser.import_segment;
 import org.eclipse.imp.lpg.parser.LPGParser.include_segment;
 import org.eclipse.imp.lpg.parser.LPGParser.keywordSpec;
 import org.eclipse.imp.lpg.parser.LPGParser.keywordSpecList;
 import org.eclipse.imp.lpg.parser.LPGParser.nonTerm;
 import org.eclipse.imp.lpg.parser.LPGParser.nonTermList;
 import org.eclipse.imp.lpg.parser.LPGParser.option;
 import org.eclipse.imp.lpg.parser.LPGParser.optionList;
 import org.eclipse.imp.lpg.parser.LPGParser.option_spec;
 import org.eclipse.imp.lpg.parser.LPGParser.option_specList;
 import org.eclipse.imp.lpg.parser.LPGParser.rule;
 import org.eclipse.imp.lpg.parser.LPGParser.rules_segment;
 import org.eclipse.imp.lpg.parser.LPGParser.start_symbol0;
 import org.eclipse.imp.lpg.parser.LPGParser.symWithAttrsList;
 import org.eclipse.imp.lpg.parser.LPGParser.terminal;
 import org.eclipse.imp.lpg.parser.LPGParser.terminalList;
 import org.eclipse.imp.lpg.parser.LPGParser.type_declarations;
 import org.eclipse.imp.lpg.parser.LPGParser.type_declarationsList;
 import org.eclipse.imp.lpg.preferences.LPGConstants;
 import org.eclipse.imp.model.ISourceEntity;
 import org.eclipse.imp.preferences.PreferencesService;
 import org.eclipse.imp.services.ILabelProvider;
 import org.eclipse.imp.utils.MarkerUtils;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 
 public class LPGLabelProvider implements ILabelProvider {
     private Set<ILabelProviderListener> fListeners= new HashSet<ILabelProviderListener>();
 
     private static ImageRegistry sImageRegistry= LPGRuntimePlugin.getInstance().getImageRegistry();
 
     private static Image DEFAULT_IMAGE= sImageRegistry.get(ILPGResources.DEFAULT_AST);
 
     private static Image GRAMMAR_FILE_IMAGE= sImageRegistry.get(ILPGResources.GRAMMAR_FILE);
 
     private static Image GRAMMAR_FILE_ERROR_IMAGE= sImageRegistry.get(ILPGResources.GRAMMAR_FILE_ERROR);
 
     private static Image GRAMMAR_FILE_WARNING_IMAGE= sImageRegistry.get(ILPGResources.GRAMMAR_FILE_WARNING);
 
     public Image getImage(Object element) {
         if (element instanceof ISourceEntity) {
             ISourceEntity entity= (ISourceEntity) element;
 
             return getImageFor(entity.getResource());
         }
         if (element instanceof IResource) {
             return getImageFor((IResource) element);
         }
         ASTNode n= (element instanceof ModelTreeNode) ?
                 (ASTNode) ((ModelTreeNode) element).getASTNode() :
                     (ASTNode) element;
 
         return getImageFor(n);
     }
 
     private Image getImageFor(IResource res) {
         if (res instanceof IFile) {
             IFile file= (IFile) res;
             final PreferencesService preferencesService= LPGRuntimePlugin.getInstance().getPreferencesService();
             if (!preferencesService.getStringPreference(LPGConstants.P_SOURCEFILEEXTENSIONS).contains(file.getLocation().getFileExtension()) &&
                 !preferencesService.getStringPreference(LPGConstants.P_INCLUDEFILEEXTENSIONS).contains(file.getLocation().getFileExtension()))
                 return null;
             int sev= MarkerUtils.getMaxProblemMarkerSeverity(file, IResource.DEPTH_ONE);
             switch (sev) {
             case IMarker.SEVERITY_ERROR:
                 return GRAMMAR_FILE_ERROR_IMAGE;
             case IMarker.SEVERITY_WARNING:
                 return GRAMMAR_FILE_WARNING_IMAGE;
             default:
                 return GRAMMAR_FILE_IMAGE;
             }
         }
         return null;
     }
 
     public static Image getImageFor(ASTNode n) {
         return DEFAULT_IMAGE;
     }
 
     public String getText(Object element) {
         ASTNode n= (element instanceof ModelTreeNode) ?
                 (ASTNode) ((ModelTreeNode) element).getASTNode() :
                 (ASTNode) element;
 
         return getLabelFor(n);
     }
 
     public static String getLabelFor(ASTNode n) {
         if (n instanceof LPG)
             return "grammar";
         if (n instanceof option_specList)
             return "options";
         if (n instanceof AliasSeg)
             return "aliases";
         if (n instanceof DefineSeg)
             return "defines";
         if (n instanceof EofSeg)
             return "eof";
         if (n instanceof ExportSeg)
             return "export";
         if (n instanceof GlobalsSeg)
             return "globals";
         if (n instanceof HeadersSeg)
             return "headers";
         if (n instanceof IdentifierSeg)
             return "identifiers";
         if (n instanceof ImportSeg)
             return "imports";
         if (n instanceof IncludeSeg)
             return "includes";
         if (n instanceof LPG_itemList)
             return "item list";
        if (n instanceof KeywordsSeg)
            return "keywords";
         if (n instanceof NoticeSeg)
             return "notice";
         if (n instanceof StartSeg)
             return "start symbol";
         if (n instanceof RulesSeg)
             return "rules";
         if (n instanceof TerminalsSeg)
             return "terminals";
         if (n instanceof TrailersSeg)
             return "trailers";
         if (n instanceof TypesSeg)
             return "types";
 
         if (n instanceof option_spec)
             return "option spec";
         if (n instanceof optionList)
             return "%option " + ((optionList) n).getoptionAt(0).getSYMBOL() + "...";
         if (n instanceof nonTermList)
             return "non-terminals";
         if (n instanceof option) {
             option o= (option) n;
             return o.getSYMBOL().toString() + (o.getoption_value() != null ? o.getoption_value().toString() : "");
         }
         if (n instanceof defineSpecList)
             return "defines";
         if (n instanceof defineSpec)
             return /*"macro " +*/((defineSpec) n).getmacro_name_symbol().toString();
         if (n instanceof nonTerm)
             return /*"non-terminal " +*/((nonTerm) n).getruleNameWithAttributes().getSYMBOL().toString();
         if (n instanceof terminal)
             return /*"terminal " +*/((terminal) n).getterminal_symbol().toString();
         if (n instanceof include_segment)
             return ((include_segment) n).getSYMBOL().toString();
         if (n instanceof action_segmentList)
             return "actions";
         if (n instanceof action_segment)
             return ((action_segment) n).getBLOCK().toString();
         if (n instanceof terminalList)
             return "terminals";
         if (n instanceof start_symbol0)
             return ((start_symbol0) n).getSYMBOL().toString();
         if (n instanceof drop_commandList)
             return "drop";
         if (n instanceof drop_command0)
             return "drop symbols";
         if (n instanceof drop_command1)
             return "drop rules";
         if (n instanceof drop_rule)
             return ((drop_rule) n).getSYMBOL().toString();
         if (n instanceof drop_ruleList)
             return "rules";
         if (n instanceof rule) {
             rule r= (rule) n;
             nonTerm nt= (nonTerm) r.getParent().getParent();
             String nonTermName= nt.getruleNameWithAttributes().getSYMBOL().toString();
             return nonTermName + " " + nt.getproduces() + " " + r.getsymWithAttrsList().toString();
         }
         if (n instanceof symWithAttrsList)
             return ((symWithAttrsList) n).toString();
         if (n instanceof keywordSpecList)
             return "keywords";
         if (n instanceof keywordSpec) {
             keywordSpec kspec= (keywordSpec) n;
             return kspec.getterminal_symbol().toString() + (kspec.getname() != null ? " ::= " + kspec.getname().toString() : "");
         }
         if (n instanceof rules_segment)
             return "rules";
         //	if (n instanceof types_segment1)
         //	    return "types???";
         if (n instanceof SYMBOLList)
             return n.toString();
         if (n instanceof type_declarationsList)
             return "types";
         if (n instanceof type_declarations)
             return ((type_declarations) n).getSYMBOL().toString();
         if (n instanceof import_segment)
             return "import " + ((import_segment) n).getSYMBOL().toString();
         if (n instanceof ASTNodeToken)
             return ((ASTNodeToken) n).toString();
 
         return "<???>";
     }
 
     public void addListener(ILabelProviderListener listener) {
         fListeners.add(listener);
     }
 
     public void dispose() {}
 
     public boolean isLabelProperty(Object element, String property) {
         return false;
     }
 
     public void removeListener(ILabelProviderListener listener) {
         fListeners.remove(listener);
     }
 }
