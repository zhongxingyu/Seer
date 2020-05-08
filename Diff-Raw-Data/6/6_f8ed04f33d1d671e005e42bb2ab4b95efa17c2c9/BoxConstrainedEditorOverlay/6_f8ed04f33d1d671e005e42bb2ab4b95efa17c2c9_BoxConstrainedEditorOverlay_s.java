 package edu.berkeley.eduride.editoroverlay;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewerExtension5;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationModelEvent;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IAnnotationModelListener;
 import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CaretEvent;
 import org.eclipse.swt.custom.CaretListener;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.ColorDialog;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ide.ResourceUtil;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.jface.text.Position;
 
 import edu.berkeley.eduride.base_plugin.isafile.ISAUtil;
 import edu.berkeley.eduride.base_plugin.util.Console;
 import edu.berkeley.eduride.base_plugin.util.IPartListenerInstaller;
 import edu.berkeley.eduride.editoroverlay.marker.Util;
 import edu.berkeley.eduride.editoroverlay.model.Box;
 import edu.berkeley.eduride.editoroverlay.model.InlineBox;
 import edu.berkeley.eduride.editoroverlay.model.MultilineBox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 // one of these per editor that can be constrained by our boxes!
 public class BoxConstrainedEditorOverlay {
 
 	// the initial state of the boxes, when editor first opened
 	private final boolean START_TURNED_ON = true;
 
 	
 	private ITextEditor editor = null;
 	private IDocument doc = null;
 	private ITextSelection sel = null;
 	private static HashMap<IEditorPart, BoxConstrainedEditorOverlay> installedOn = new HashMap<IEditorPart, BoxConstrainedEditorOverlay>();
 	ISourceViewer srcViewer;
 	IAnnotationModel annotationModel;
 	ITextViewerExtension5 txtViewerExt;
 
 	private ArrayList<MultilineBox> multilineBoxes = new ArrayList<MultilineBox>();
 	private ArrayList<InlineBox> inlineBoxes = new ArrayList<InlineBox>();
 
 	private boolean turnedOn = false;
 
 	private StyledText styledText;
 	private BoxPaintListener boxPaint;
 	private CaretListener caretListener;
 	private EditorOverlayVerifyKeyListener verifyKeyListener;
 	private BCEOAnnotationModelListener annotationModelListener;
 	private IResource res;
 	private int caretOffset = 0;
 
 	
 	public static BoxConstrainedEditorOverlay getBCEO(IEditorPart editor) {
 		BoxConstrainedEditorOverlay bceo = null;
 		if (editor != null) {
 			bceo = installedOn.get(editor);
 		}
 		return bceo;
 	}
 	
 	/*
 	 * Determines whether this is an appropriate editor in which to use BCEO
 	 * 
 	 * @param editor could be null
 	 */
 	public static boolean shouldInstall(IEditorPart editor) {
 		// TODO worry if the editor is 'immature'?
 		if (editor != null) {
 			// first, needs to be a text editor
 			if (editor instanceof AbstractDecoratedTextEditor) {
 				IEditorInput input = editor.getEditorInput();
 				// Check that the editor is reading from a file
 				IFile file = (input instanceof FileEditorInput) ? ((FileEditorInput) input)
 						.getFile() : null;
 				// check that this file is appropriate
 				return shouldInstall(file);
 			}
 		}
 		return false;
 	}
 
 	public static boolean shouldInstall(IFile file) {
 
 		boolean shouldInstall = true;
 		
 		// check that it is a java file -- we ignore .class files, why not
 		IJavaElement je = JavaCore.create(file);
 		shouldInstall &= (je != null && je.getElementType() == IJavaElement.COMPILATION_UNIT);
 		
 		// check that it lives in a ISAProject, why not...
 		shouldInstall &= ISAUtil.withinISAProject(file);
 		
 		return shouldInstall;
 	}
 
 	/*
 	 * Given that this is an appropriate editor for a BCEO, makes sure that a
 	 * BCEO is present and ready for togglin'
 	 */
 	public static BoxConstrainedEditorOverlay ensureInstalled(IEditorPart editor) {
 		if (shouldInstall(editor)) { // is it an ISA File?
 			BoxConstrainedEditorOverlay ekpl;
 			if (installedOn.containsKey(editor)) { // Don't install if already
 													// installed on
 				ekpl = installedOn.get(editor);
 				//Console.msg("Already Installed!");
 			} else {
 				ekpl = new BoxConstrainedEditorOverlay(editor);
 				if (ekpl != null) {
 					installedOn.put(editor, ekpl);
 				}
 			}
 			return ekpl;
 		}
 		return null;
 	}
 
 	// ////////
 
 	public BoxConstrainedEditorOverlay(IEditorPart editor) {
 		boolean success = this.installMe(editor);
 
 		if (success) {
 			makeListeners();
 				
 			if (START_TURNED_ON) {
 				turnOn();
 			}
 			
 			this.res = ResourceUtil.getResource(editor.getEditorInput());
 		}
 	}
 
 	// shouldInstall() and ensureInstalled() are *not* used here -- whatever
 	// calls this should use them to make sure.
 	// Currently, this is handled in ToggleBoxes.java.
 	@SuppressWarnings("restriction")
 	private boolean installMe(IEditorPart editor) {
 		if (editor == null) {
 			return false; // failed
 		}
 		StyledText text = null;
 		text = (StyledText) editor.getAdapter(Control.class);
 		if (text == null) {
 			return false; // failed
 		}
 		this.styledText = text;
 		try {
 			this.editor = (ITextEditor) editor;
 			CompilationUnitEditor cuEditor = (CompilationUnitEditor) editor;
 			// above might throw ClassCastException for non-java files, yo
 			// which shouldn't happen with shouldInstall() above.
 			
 			IDocumentProvider dp = this.editor.getDocumentProvider();
 			this.doc = dp.getDocument(editor.getEditorInput());
 			ISelectionProvider sp = this.editor.getSelectionProvider();
 			
 			// this crashes if not called in UI thread (when installing at startup
 			// on existing editors).
 			this.sel = (ITextSelection) sp.getSelection();
 			sp.addSelectionChangedListener(new selChanged(this));
 
 			srcViewer = cuEditor.getViewer();
 			annotationModel = srcViewer.getAnnotationModel();
 			// we can use this to get line numbers with folding
 			txtViewerExt = (ITextViewerExtension5) srcViewer;
 		} catch (ClassCastException e) {
 			return false;  // not looking at a java file, yo
 		} catch (Exception e) {
 			Console.err("Some strange exception in BCEO install... hm...");
 			Console.err(e);
 			return false;
 		}
 		
 		return true;
 	}
 
 	
 	
 	private void makeListeners() {
 		this.boxPaint = new BoxPaintListener();
 		this.caretListener = new CaretPositionListener();
 		this.verifyKeyListener = new EditorOverlayVerifyKeyListener();
 		this.annotationModelListener = new BCEOAnnotationModelListener();
 	}
 	
 	
 	private void installListeners() {
 		styledText.addPaintListener(boxPaint);
 		styledText.addCaretListener(caretListener);
 		styledText.addVerifyKeyListener(verifyKeyListener);
 		annotationModel.addAnnotationModelListener(annotationModelListener);
 	}
 	
 	private void uninstallListeners() {
 		if (boxPaint != null) {
 			styledText.removePaintListener(boxPaint);
 		}
 		if (caretListener != null) {
 			styledText.removeCaretListener(caretListener);
 		}
 		if (verifyKeyListener != null) {
 			styledText.removeVerifyKeyListener(verifyKeyListener);
 		}
 		if (annotationModelListener != null) {
 			annotationModel
 					.removeAnnotationModelListener(annotationModelListener);
 		}
 	}
 	
 	
 	
 	
 	// This keeps the last selection around, but it is currently unused.
 	private class selChanged implements ISelectionChangedListener {
 
 		BoxConstrainedEditorOverlay ed;
 
 		public selChanged(BoxConstrainedEditorOverlay kpie) {
 			this.ed = kpie;
 		}
 
 		@Override
 		public void selectionChanged(SelectionChangedEvent event) {
 			ed.sel = (ITextSelection) event.getSelection();
 		}
 
 	}
 
 	// //////////////////
 	// ///////// OVERLAY CONTROLS
 
 
 	public void turnOn() {
 		validateAnnotations(annotationModel);
 		turnedOn = true;
 		decorate();
 	}
 
 	
 	public void turnOff() {
 		turnedOn = false;
 		undecorate();
 	}
 
 	
 	public void toggle() {
 		if (!turnedOn) {
 			turnOn();
 		} else {
 			turnOff();
 		}
 	}
 
 	public boolean isTurnedOn() {
 		return turnedOn;
 	}
 
 	// install any listeners for drawing
 	private void decorate() {
 		createBoxes();
 		if (hasBoxes()) {
 			installListeners();
 			drawBoxes();        //moved inside of if statement
 		} else {
 			turnedOn = false;  //no boxes, don't install listeners or turn on
 			clearBackground();
 		}
 	}
 
 	
 	// stop listening when turned off
 	private void undecorate() {
 		uninstallListeners();
 		clearBackground();
 	}
 
 	private class BoxPaintListener implements PaintListener {
 		@Override
 		public void paintControl(PaintEvent e) {
 			drawBoxes();
 		}
 	}
 
 	private class CaretPositionListener implements CaretListener {
 
 		@Override
 		public void caretMoved(CaretEvent event) {
 			caretOffset = event.caretOffset;
 		}
 	}
 
 	private class EditorOverlayVerifyKeyListener implements VerifyKeyListener {
 
 		// Intercept key presses, if turned on stop key presses
 
 		// TODO detect/catch/stop paste events outside of boxes
 
 		@Override
 		public void verifyKey(VerifyEvent event) {
 			//Console.msg("verifyKey called: " + event.character);
 
 			if (!turnedOn) {
 				// allow it, begrudgingly.
 				return;
 			}
 
 			// int keyCode = event.keyCode;
 			char character = event.character;
 			int int_char = (int) character;
 
 			// back to using int_char... checking keycode instead allowed keypad
 			// and weird things
 			// TODO better way to do this?? I think this gets all those keys...
 			if (int_char == 0) {
 				event.doit = true;
 				return;
 			}
 
 			int widgetOffset = txtViewerExt
 					.widgetOffset2ModelOffset(caretOffset); // account for
 															// folding
 
 			for (MultilineBox b : multilineBoxes) {
 				int bStartOffset = b.getStartStyledTextOffset();
 				int bStopOffset = b.getStopStyledTextOffset();
 
 				if (keyEventInBox(widgetOffset, bStartOffset - 1, bStopOffset)) {
 					event.doit = allowKeyEventInBox(widgetOffset, b,
 							bStartOffset - 1, bStopOffset, character);
 					return;
 				}
 			}
 			for (InlineBox b : inlineBoxes) {
 				int bStartOffset = b.getStartStyledTextOffset();
 				int bStopOffset = b.getStopStyledTextOffset();
 
 				if (keyEventInBox(widgetOffset, bStartOffset, bStopOffset)) {
 					event.doit = allowKeyEventInBox(widgetOffset, b,
 							bStartOffset, bStopOffset, character);
 
 					return;
 				}
 			}
 
 			event.doit = false;
 			return;
 		}
 
 		private boolean keyEventInBox(int offset, int bStartOffset,
 				int bStopOffset) {
 			boolean inBox = ((offset > bStartOffset) && (offset < bStopOffset - 1));
 
 			if (inBox) { // if we have a selection that crosses an annotation,
 							// don't allow typing!
 				if (!(sel.getLength() == 0)) {
 					if ((sel.getOffset() <= bStartOffset)
 							|| (sel.getOffset() + sel.getLength() >= bStopOffset - 1)) {
 						inBox = false;
 					}
 				}
 			}
 
 			return inBox;
 		}
 
 		// might need to specialize for box type, so kept Box argument
 		private boolean allowKeyEventInBox(int offset, Box b, int bStartOffset,
 				int bStopOffset, char character) {
 			boolean allowed = true;
 
 			// the annotation for a multiline box is *within* the box.  As such, we can't 
 			// allow key events at the first position of the box, because the annotation will get mucked.
 			// TODO lets fix this, and move the annotation outside of the box.
 			if (character == SWT.BS) {
 				// backspace key
 				if (offset == (bStartOffset + 1)) {
 					// start of the box, must resist or we'll delete the
 					// annotation
 					allowed = false;
 				}
 
 			} else if (character == SWT.DEL) {
 				// delete key
 				if (offset == (bStopOffset - 2)) {
 					// end of the box, you can't delete that!
 					allowed = false;
 				}
 				//TODO: Fix this hack!
 				//Don't allow "enter" on first character...  it moves the annotation down a line
 				//and then causes super bad box deleting issues if user Ctrl+Z after moving annotation
 				//No idea what causes the deletion problem
 			} else if (offset == (bStartOffset + 1) && (character == SWT.CR || character == SWT.LF)) {
 				allowed = false;
 			}
 			return allowed;
 		}
 
 		// for Inline box -- try to allow key presses at right?
 		private boolean allowKeyEventInBox(int offset, InlineBox b,
 				int bStartOffset, int bStopOffset, char character) {
 			boolean allowed = true;
 
 			if (character == SWT.BS) {
 				// backspace key
 				if (offset == (bStartOffset + 1)) {
 					// start of the box, must resist or we'll delete the
 					// annotation
 					allowed = false;
 				}
 
 			} else if (character == SWT.DEL) {
 				// delete key
 				if (offset == (bStopOffset - 2)) {
 					// end of the box, you can't delete that!
 					allowed = false;
 				}
 			} else if (character == SWT.CR || character == SWT.LF) {
 				allowed = false;
 			}
 			return allowed;
 		}
 
 	}
 
 	
 	
 	
 	
 	///////////////////
 	//  annotations
 	
 	
 	public class BCEOAnnotationModelListener implements
 			IAnnotationModelListener, IAnnotationModelListenerExtension {
 
 		//if the boxes are 'turned on', we shouldn't ever get removed events.
 		//  ha.  maybe assume it could happen, why not...
 		//  but, we will get moved events for the endAnnotation of a multiline
 		//if boxes are turned off, we'll get removes and adds and changes
 		// the document.  We should set a dirty bit of some sort so that
 		// validateAnnotations() can do everything at once, I think.
 		
 
 		@Override
 		public void modelChanged(AnnotationModelEvent event) {
 			//Console.msg ("Hello.  Got a annotation model change!");
 			
 			//Util.fixMultiLine(annotationModel, styledText);
 
 			if (event.isValid() && !event.isEmpty()) {
 				boolean uhOh = false;
 				Annotation[] removedAnnotations = event.getRemovedAnnotations();
 				for (Annotation removedAnnotation : removedAnnotations) {
 					if (Util.isBCEOAnnoation(removedAnnotation)) {
 						uhOh = true;
 					}
 				}
 				if (uhOh) {
 					// seems overly dramatic, but hey
 					createBoxes();
 				}
 			}
 
 		}
 
 		@Override
 		public void modelChanged(IAnnotationModel model) {
 			// Console.msg("I don't care about this model Changed event");
 			// this is around just so we can add this listener, sigh
 		}
 
 	}
 
 	
 	
 	
 	
 	
 	// This is called before boxes are 'turned on', because the annotations might
 	// have gotten mucked up:
 	//  - one of the multiline annotations was deleted...
 	//  - multiline annotations might not be at the start of a line, put them there
 	public static void validateAnnotations(IAnnotationModel annotationModel) {
 		//TODO
 	}
 	
 	
 	
 	
 	// /////////////////////////
 
 	// Make multilineBoxes and inlineBpoxes based on annotations in editor
 	// viewer
 	private void createBoxes() {
 		// ISourceViewer viewer = ((CompilationUnitEditor)editor).getViewer();
 		// IAnnotationModel annotationModel = viewer.getAnnotationModel();
 		
 		Util.fixMultiLine(annotationModel, styledText);
 
 		// MULTILINE
 		List<Annotation[]> multiline = Util
 				.getMultilineAnnotations(annotationModel);
 		MultilineBox mb;
 		multilineBoxes.clear();
 
 		for (int i = 0; i < multiline.size(); i++) {
 			mb = new MultilineBox(annotationModel, multiline.get(i)[0],
 					multiline.get(i)[1]);
 			multilineBoxes.add(mb);
 		}
 
 		// INLINE
 		List<Annotation> inline = Util.getInlineAnnotations(annotationModel);
 		InlineBox ib;
 		inlineBoxes.clear();
 
 		for (Annotation ann : inline) {
 			ib = new InlineBox(annotationModel, ann);
 			inlineBoxes.add(ib);
 		}
 
 		// TODO hack to fix something or other in authoring. we should remediate
 		if (!hasBoxes()) {
 			clearBackground();
 			turnOff();
 		}
 	}
 
 	// are there any boxes to draw?
 	private boolean hasBoxes() {
 		return (!(multilineBoxes.size() == 0 && inlineBoxes.size() == 0));
 	}
 
 	// //////////////
 
 	// private LinkedList<Integer> oldDrawParameters; //Stores some set of
 	// numbers related to things we're drawing, not for use outside Draw!
 
 	private int oldEditorWidth = 0;
 	private int oldEditorHeight = 0;
 	
 	// draws boxes from boxList
 	public void drawBoxes() {
 		// big picture: create an image (newImage), edit with the gc, then set
 		// as styledtext background later
 		Rectangle editorRectangle = styledText.getClientArea();
 		if (editorRectangle.width==0 || editorRectangle.height==0) {
 			// if either is 0, then we aren't drawing, are we?
 			// and, the new Image() throws an exception
 			return;
 		}
 
 		// Do we need to redraw? if any box has changed anywhere, we do it all
 		// again
 		boolean redraw = false;
 		boolean somethingIsFolded = false;
 
 		// if the editor window gets wider or narrower we've got to redraw it no
 		// matter what... don't out here!
 		if (editorRectangle.width != oldEditorWidth) {
 			redraw = true;
 			oldEditorWidth = editorRectangle.width;
 		}
 		if (editorRectangle.height != oldEditorHeight) {
 			redraw = true;
 			oldEditorHeight = editorRectangle.height;
 		}
 
 		// collect all the drawing locations for all the boxes.
 		for (MultilineBox b : multilineBoxes) {
 
 			int startWidgetOffset, stopWidgetOffset;
 			int startPixelY = -1, stopPixelY = -1;
 
 			startWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(b
 					.getStartStyledTextOffset());
 			if (startWidgetOffset != -1) {
 				startPixelY = styledText.getLocationAtOffset(startWidgetOffset).y;
 			} else {
 				somethingIsFolded = true;
 			}
 
 			stopWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(b
 					.getStopStyledTextOffset());
 			if (stopWidgetOffset != -1) {
 				stopPixelY = styledText.getLocationAtOffset(stopWidgetOffset).y;
 			} else {
 				somethingIsFolded = true;
 			}
 
 			// no need to check/store widget offsets... right?
 			if (b.getStartPixelY() != startPixelY) {
 				redraw = true;
 				b.setStartPixelY(startPixelY);
 			}
 			if (b.getStopPixelY() != stopPixelY) {
 				redraw = true;
 				b.setStopPixelY(stopPixelY);
 			}
 
 		}
 
 		for (InlineBox b : inlineBoxes) {
 
 			int startWidgetOffset, stopWidgetOffset;
 
 			Position stPos = b.getStyledTextPosition();
 
 			startWidgetOffset = txtViewerExt.modelOffset2WidgetOffset(stPos
 					.getOffset()) + 1; // adjusting to match keyverify event
 			int x = -1;
 			int y = -1;
 			int width = -1;
 			if (startWidgetOffset != -1) {
 				Point startLoc = styledText
 						.getLocationAtOffset(startWidgetOffset);
 				x = startLoc.x;
 				y = startLoc.y;
 			} else {
 				somethingIsFolded = true;
 			}
 
 			stopWidgetOffset = startWidgetOffset + stPos.getLength() - 3; // adjusting
 																			// to
 																			// match
 																			// keyverify
 																			// event
 			if (stopWidgetOffset != -1) {
 				width = styledText.getLocationAtOffset(stopWidgetOffset).x - x;
 			} else {
 				somethingIsFolded = true;
 			}
 
 			// no need to check/store widget offsets... right?
 			if (b.x != x) {
 				redraw = true;
 				b.x = x;
 			}
 			if (b.y != y) {
 				redraw = true;
 				b.y = y;
 			}
 			if (b.width != width) {
 				redraw = true;
 				b.width = width;
 			}
 
 		}
 
 		// compare old drawing locations with new locations. If unchanged,
 		// simply return.
 		if (!redraw) {
 			return; // short circuit if we don't need to redraw
 		}
 
 		// // OKAY, we need to draw
 
 		// TODO speed keep old size around and only recreate when necessary?
 		Image newImage = new Image(null, editorRectangle.width,
 				editorRectangle.height);
 		GC gc = new GC(newImage);
 
 		IRegion[] visibleRegions = txtViewerExt
 				.getCoveredModelRanges(txtViewerExt.getModelCoverage());
 
 		if (turnedOn) {
 			gc.setLineWidth(2);
 			// assuming all lines have the same height, yo.
 			int lineHeight = styledText.getLineHeight();
 
 			for (InlineBox b : inlineBoxes) {
 				if (b.x != -1) {
 					// its visible (not folded up)
 					gc.setForeground(b.color);
 					if (b.width == 0) {
 						gc.drawRectangle(b.x - 2, b.y, b.width + 5, lineHeight);
 					} else {
 						gc.drawRectangle(b.x, b.y, b.width, lineHeight);
 					}
 				}
 			}
 
 			if (!somethingIsFolded) { // Don't go through big slow ifs/loops
 										// when no folding problems (aka correct
 										// use of boxes)
 				for (MultilineBox b : multilineBoxes) {
 					int startY = b.getStartPixelY();
 					int stopY = b.getStopPixelY();
 
 					gc.setForeground(b.color);
 					gc.drawRectangle(1, startY, editorRectangle.width - 4,
 							stopY - startY);
 				}
 
 			} else { // something is folded, so we need to check starts/ends and
 						// recalculate
 				for (MultilineBox b : multilineBoxes) {
 					int startY = b.getStartPixelY();
 					int stopY = b.getStopPixelY();
 
 					int index = 0;
 
 					if (startY == -1) { // start marker is folded, recompute
 						startY = b.getStartStyledTextOffset();
 						while ((index < visibleRegions.length)
 								&& (startY >= visibleRegions[index].getOffset())) {
 							index++;
 						}
 
 						startY = visibleRegions[index].getOffset();
 
 						if (b.getStopStyledTextOffset() < startY) { // no region
 																	// to draw,
 																	// stop
 																	// found
 																	// before
 																	// start
 							continue;
 						}
 						startY = txtViewerExt.modelOffset2WidgetOffset(startY);
 						startY = styledText.getLocationAtOffset(startY).y;
 					}
 
 					if (stopY == -1) { // stop marker is folded, recompute
 						stopY = b.getStopStyledTextOffset();
 
 						while ((index < visibleRegions.length)
 								&& (stopY > visibleRegions[index].getOffset())) {
 							index++;
 						}
 
 						stopY = visibleRegions[index].getOffset()
 								+ visibleRegions[index].getLength() - 1;
 						stopY = txtViewerExt.modelOffset2WidgetOffset(stopY);
 						stopY = styledText.getLocationAtOffset(stopY).y;
 					}
 
 					gc.setForeground(b.color);
 					gc.drawRectangle(1, startY, editorRectangle.width - 4,
 							stopY - startY);
 				}
 			}
 		}
 
 		Image oldImage = styledText.getBackgroundImage(); // (so we can null
 															// check)
 		styledText.setBackgroundImage(newImage); // draw our boxes! :D
 		if (oldImage != null)
 			oldImage.dispose(); // if we had boxes before, clean up after
 								// ourselves
 		gc.dispose();
 	}
 
 	private void clearBackground() {
 		Display.getDefault().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Image oldImage = styledText.getBackgroundImage();
 				styledText.setBackgroundImage(null);
 				if (oldImage != null)
 					oldImage.dispose();
 				styledText.setBackgroundImage(null);
 			}
 		}
 
 		);
 	}
 
 	// ////////////////////
 	// AUTHORING
 
 	// These methods are called through the authoring/instructor plugin.
 	// we could refactor, but they use sooooo many local fields...
 
 	// Problem: can't draw boxes at very start or very end of editor... only a
 	// minor issue
 
 	// Create a box around the currently selected region.
 	// Prompts user for confirmation, also allows optional color picking.
 	// Adds new markers.
 	public void createMarkers() {
 		// Apparently, this is how we get a shell for dialogs
 		Shell shell = editor.getSite().getWorkbenchWindow().getShell();
 
 		if (sel.getLength() == 0) {
 			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
 			dialog.setText("Box Creation Error");
 			dialog.setMessage("Error: Please select (highlight) a region before clicking \"Create Box\".");
 			int returnCode = dialog.open();
 
 			return;
 		}
 
 		int startLine = sel.getStartLine();
 		int stopLine = sel.getEndLine();
 
 		if (startLine == stopLine) {
 			promptInLine(startLine, shell);
 
 		} else {
 			promptMultiLine(startLine, stopLine, shell);
 		}
 
 		createBoxes();
 		drawBoxes();
 	}
 
 	private void promptInLine(int startLine, Shell shell) {
 		int startOff = sel.getOffset() - 1;
 		int stopOff = startOff + sel.getLength() + 3;
 
 		if (!isValidBox(startOff + 1, stopOff - 2)) {
 			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
 			dialog.setText("Box Creation Error");
 			dialog.setMessage("Error: Boxes cannot overlap.  Please select a valid region.");
 			int returnCode = dialog.open();
 			return;
 		}
 
 		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES
 				| SWT.NO | SWT.CANCEL);
 		dialog.setText("Create Box");
 		dialog.setMessage("Creating new In-line Box around current selection.\nUse default color?");
 		int returnCode = dialog.open();
 
 		if (returnCode == SWT.NO) { // get color, cram it into annotation's
 									// text, read it back later
 			Color c = promptColor(shell);
 			Util.createInlineMarker(res, startOff, stopOff, "ibox" + startOff
 					+ Util.colorToString(c));
 		}
 		if (returnCode == SWT.YES) {
 			Util.createInlineMarker(res, startOff, stopOff, "ibox" + startOff);
 		}
 	}
 
 	private void promptMultiLine(int startLine, int stopLine, Shell shell) {
 		int startOff = styledText.getOffsetAtLine(startLine);
 		int stopOff = styledText.getOffsetAtLine(stopLine);
 
 		if (!isValidBox(startOff, stopOff)) {
 			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
 			dialog.setText("Box Creation Error");
 			dialog.setMessage("Error: Boxes cannot overlap.  Please select a valid region.");
 			int returnCode = dialog.open();
 			return;
 		}
 
 		//startLine += 1;
 		stopLine += 2;
 
 		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES
 				| SWT.NO | SWT.CANCEL);
 		dialog.setText("Create Box");
 		dialog.setMessage("Creating new Multi-line Box around current selection.\nUse default color?");
 		int returnCode = dialog.open();
 
 		if (returnCode == SWT.NO) {// get color, cram it into annotation's text,
 									// read it back later
 			Color c = promptColor(shell);
 			Util.createMultiLine(res, styledText.getOffsetAtLine(startLine) - 1, stopLine, "mbox" + startLine
 					+ stopLine + Util.colorToString(c));
 		}
 		if (returnCode == SWT.YES) {
 			Util.createMultiLine(res, styledText.getOffsetAtLine(startLine) - 1, stopLine, "mbox" + startLine
 					+ stopLine);
 		}
 	}
 
 	// make sure our new box doesn't overlap others before we draw it
 	private boolean isValidBox(int startOff, int stopOff) {
 		for (Box b : multilineBoxes) {
 			int bStartOffset = b.getStartStyledTextOffset();
 			int bStopOffset = b.getStopStyledTextOffset();
 
 			if (((startOff > bStartOffset) && (startOff < bStopOffset - 1))
 					|| ((stopOff > bStartOffset) && (stopOff < bStopOffset - 1))
 					|| (startOff < bStartOffset) && (stopOff > bStopOffset - 1)) {
 				return false;
 			}
 		}
 
 		for (Box b : inlineBoxes) {
 			int bStartOffset = b.getStartStyledTextOffset();
 			int bStopOffset = b.getStopStyledTextOffset();
 
 			if (((startOff > bStartOffset) && (startOff < bStopOffset - 1))
 					|| ((stopOff > bStartOffset) && (stopOff < bStopOffset - 1))
 					|| (startOff < bStartOffset) && (stopOff > bStopOffset - 1)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	// Pop up a fancy color dialog, returns user's color choice
 	private Color promptColor(Shell shell) {
 		ColorDialog dlg = new ColorDialog(shell);
 		dlg.setRGB(new RGB(200, 120, 255));
 		dlg.setText("Choose a Color");
 		RGB rgb = dlg.open();
 		return new Color(null, rgb);
 	}
 
 	// Deletes the box the caret (blinky cursor line thing) is currently inside
 	// of
 	public void deleteMarker() {
 		// Apparently, this is how we get a shell for dialogs
 		Shell shell = editor.getSite().getWorkbenchWindow().getShell();
 
 		// caretOffset = offset of last click/cursor position
 		int offset = txtViewerExt.widgetOffset2ModelOffset(caretOffset);
 
 		Box deleteThisBox = null; // don't delete in the middle of the for-each,
 									// delete calls createboxes which crashes
 									// stuff
 
 		for (Box b : multilineBoxes) {
 			int bStartOffset = b.getStartStyledTextOffset();
 			int bStopOffset = b.getStopStyledTextOffset();
 
 			if ((offset > bStartOffset) && (offset < bStopOffset - 1)) {
 				deleteThisBox = b;
 			}
 		}
 
 		for (Box b : inlineBoxes) {
 			int bStartOffset = b.getStartStyledTextOffset();
 			int bStopOffset = b.getStopStyledTextOffset();
 
 			if ((offset > bStartOffset) && (offset < bStopOffset - 1)) {
 				deleteThisBox = b;
 			}
 		}
 		if (deleteThisBox != null) {
 			MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION
 					| SWT.YES | SWT.NO);
 			dialog.setText("Delete Box");
 			dialog.setMessage("Would you like to delete the box your caret/cursor is currently in?");
 			int returnCode = dialog.open();
 
 			if (returnCode == SWT.YES) {
 				deleteThisBox.delete();
 			}
 		} else {
 			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
 			dialog.setText("Deletion Error");
 			dialog.setMessage("Error: Please click inside the box you're removing before clicking \"Delete\".");
 			int returnCode = dialog.open();
 		}
 	}
 
 }
