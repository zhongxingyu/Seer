 package com.redcareditor.mate;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.CompositeRuler;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IVerticalRulerColumn;
 import org.eclipse.jface.text.source.LineNumberRulerColumn;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.text.source.VerticalRuler;
 import com.redcareditor.onig.NullRx;
 import com.redcareditor.onig.Rx;
 import com.redcareditor.theme.Theme;
 
 public class MateText extends SourceViewer {
 	public Parser parser;
 	
 	public MateText(Composite parent, CompositeRuler ruler, int style) {
 		super(parent, ruler, SWT.FULL_SELECTION | SWT.VERTICAL | SWT.HORIZONTAL);
 		
 		IDocument document = new Document();
 		setDocument(document);
 	}
 	
 	public static Composite constructContents(Composite parent) {
 		Composite contents = new Composite(parent, SWT.NONE);
 		contents.setLayout(new FillLayout());
 		return contents;
 	}
 	
 	public static CompositeRuler constructRuler() {
 		CompositeRuler ruler = new CompositeRuler();
 		ruler.addDecorator(0, new LineNumberRulerColumn());
 		return ruler;
 	}
 	
 	
 	
 	// Sets the grammar explicitly by name.
 	// TODO: restore the uncolouring stuff
 	public boolean setGrammarByName(String name) {
 		if (this.parser != null && this.parser.grammar.name.equals(name))
 			return true;
 	  
 		for (Bundle bundle : Bundle.bundles) {
 			for (Grammar grammar : bundle.grammars) {
 				if (grammar.name.equals(name)) {
 //					int parsed_upto = 150;
 					Theme theme;
 //					if (this.parser != null) {
 //						theme = this.parser.colourer.theme;
 //						this.parser.colourer.uncolour_scope(this.parser.root, true);
 //						parsed_upto = this.parser.parsed_upto;
 //						this.parser.close();
 //					}
 					this.parser = new Parser(grammar, this);
 //					this.parser.last_visible_line_changed(parsed_upto);
 //					GLib.Signal.emit_by_name(this, "grammar_changed", gr.name);
 //					if (theme != null)
 //					  this.parser.change_theme(theme);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	// Sets the grammar by the file extension. If unable to find
 	// a grammar, sets the grammar to null. Returns the grammar
 	// name or null.
 	public String setGrammarByFilename(String fileName) {
 		String bestName = null;
 		long bestLength = 0;
 		for (Bundle bundle : Bundle.bundles) {
 			for (Grammar grammar : bundle.grammars) {
 				for (String ext : grammar.fileTypes) {
 					if (fileName.endsWith(ext) && (bestName == null || ext.length() > bestLength)) {
 						bestName = grammar.name;
 						bestLength = ext.length();
 					}
 				}
 			}
 		}
 		if (bestName != null) {
 			if (this.parser == null || this.parser.grammar.name != bestName) {
 				setGrammarByName(bestName);
 			}
 			return bestName;
 		}
 		return null;
 	}
 
 	// Sets the grammar by examining the first line. If unable to find
 	// a grammar, sets the grammar to null. Returns the grammar
 	// name or null.
 	public String setGrammarByFirstLine(String firstLine) {
 		Rx re;
 		for (Bundle bundle : Bundle.bundles) {
 			for (Grammar grammar : bundle.grammars) {
 				re = grammar.firstLineMatch;
 				if (re instanceof NullRx) {
 				}
 				else {
 					if (re.search(firstLine, 0, (int) firstLine.length()) != null) {
 						System.out.printf("matched: %s '%s' %s\n", grammar.name, firstLine, re.pattern);
 						setGrammarByName(grammar.name);
 						return grammar.name;
 					}
 				}
 			}
 		}
 		return null;
 	}
 }
 
 
 
