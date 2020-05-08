 package org.eclipse.iee.editor.core.container;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.text.ITextPresentationListener;
 import org.eclipse.jface.text.ITextViewerExtension4;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GlyphMetrics;
 
 
 class StyledTextManager {
 	private final StyledText fStyledText;
 	private final ContainerManager fContainerManager;
 	private final ISourceViewer fSourceViewer;
 	
 	//private PaintObjectListener fPaintObjectListener;
 
 	private enum IterationStatus {
 		NOT_UPDATED,
 		UPDATED_ONCE,
 		UPDATED_TWICE,
 		UPDATED_MORE_THAN_TWICE,
 	};
 	
 	private IterationStatus fStylesIterationStatus;
 	private IterationStatus fPresentationIterationStatus;	
 	
 	private boolean fAreStylesUpdated = true;
 	private boolean fArePresentationsUpdated = true;
 	
 	public StyledTextManager(ContainerManager containerManager) {
 		fContainerManager = containerManager;
 		fSourceViewer = containerManager.getSourceViewer();
 		fStyledText = fSourceViewer.getTextWidget();
 		
 		initListeners();
 	}
 		
 	public void updateStyles(boolean doInitiateTextPresentationUpdate) {
 
 		System.out.println("updateStyles()");
 		
 		fStylesIterationStatus = IterationStatus.NOT_UPDATED;
 		fPresentationIterationStatus = IterationStatus.NOT_UPDATED;
 				
 		fAreStylesUpdated = false;
 		fArePresentationsUpdated = false;
 		
 		if (doInitiateTextPresentationUpdate) { // Now used when container is resized
 			fSourceViewer.invalidateTextPresentation();
 		}
 	}
 	
 	public void updatePresentations() {
 		fArePresentationsUpdated = false;
	} 
 	
 	protected void initListeners() {
 		
 		((ITextViewerExtension4) fSourceViewer).addTextPresentationListener(new ITextPresentationListener() {			
 			@Override
 			public void applyTextPresentation(TextPresentation textPresentation) {
 
 				// XXX
 				//StopWatch stylesUpdateSW = new LoggingStopWatch("stylesUpdateSW");
 					
 				switch (fStylesIterationStatus) {
 				
 				case NOT_UPDATED:
 					
 					//   .
 					//    applyTextPresentation() -    jdt,
 					//    ,    (     ~ 1 )
 					textPresentation.mergeStyleRanges(fStyledText.getStyleRanges());
 					
 					injectStylesToTextPresentation(textPresentation, getContainersStyleRanges());
 
 					fStylesIterationStatus = IterationStatus.UPDATED_ONCE;
 					break;
 					
 				case UPDATED_ONCE:
 					injectStylesToTextPresentation(textPresentation, getContainersStyleRanges());
 					
 					fStylesIterationStatus = IterationStatus.UPDATED_TWICE;
 					break;
 					
 				case UPDATED_TWICE:
 				case UPDATED_MORE_THAN_TWICE:
 					injectStylesToTextPresentation(textPresentation, getContainersStyleRanges());
 					
 					fStylesIterationStatus = IterationStatus.UPDATED_MORE_THAN_TWICE;
 					break;
 				}
 				
 				fAreStylesUpdated = true;
 				
 				System.out.println("Applying styles: " + fStylesIterationStatus.toString());
 				
 				// XXX
 				//stylesUpdateSW.stop();
 			}
 		});
 		
 		fStyledText.addPaintListener(new PaintListener() {
 			@Override
 			public void paintControl(PaintEvent e) {
 				
 				// XXX
 				//StopWatch updatePresentationSW = new LoggingStopWatch("updatePresentationSW");
 				
 				/*
 				if (!fAreStylesUpdated) {
 					fSourceViewer.invalidateTextPresentation();
 					return; // ???
 				}*/
 				
 				switch (fStylesIterationStatus) {
 
 				case NOT_UPDATED:
 				case UPDATED_ONCE:
 					fSourceViewer.invalidateTextPresentation();
 					return;
 					
 				default:
 					/* go ahead */
 				}
 				
 				switch (fPresentationIterationStatus) {
 				
 				case NOT_UPDATED:
 					updateContainerPresentations();
 					
 					fPresentationIterationStatus = IterationStatus.UPDATED_ONCE;
 					break;
 					
 				case UPDATED_ONCE:					
					updateContainerPresentations();
 					
 					fPresentationIterationStatus = IterationStatus.UPDATED_TWICE;
 					break;
 					
 				case UPDATED_TWICE:
 				case UPDATED_MORE_THAN_TWICE:
					updateContainerPresentations();
 					
 					fPresentationIterationStatus = IterationStatus.UPDATED_MORE_THAN_TWICE;
 					break;
 				}
 				
 				System.out.println("Cpontainer presentations: " + fPresentationIterationStatus.toString());
 				
 				// XXX
 				//updatePresentationSW.stop();
 			}
 		});
 	}
 	
 	protected void updateContainerPresentations() {
 		int topLineIndex = fSourceViewer.getTopIndex();
 		if (topLineIndex > 0) {
 			topLineIndex--;
 		}
 		
 		int bottomLineIndex = fSourceViewer.getBottomIndex();
 		if (bottomLineIndex < fStyledText.getLineCount() - 1) {
 			bottomLineIndex++;
 		}
 						
 		int topVisibleOffset = fStyledText.getOffsetAtLine(topLineIndex);
 		int bottomVisibleOffset =
 			fStyledText.getOffsetAtLine(bottomLineIndex) +
 			fStyledText.getLine(bottomLineIndex).length();
 						
 		boolean isVisible = false;
 		for (Container c : fContainerManager.getContainers()) {
 			if (c.getPosition().getOffset() >= topVisibleOffset) {
 				isVisible = true;
 			}
 			if (c.getPosition().getOffset() + c.getPosition().getLength() > bottomVisibleOffset) {
 				isVisible = false;
 			}
 			c.setVisible(isVisible);
 			if (isVisible) {
 				c.updatePresentation();
 			}
 		}
 	}
 	
 	protected void injectStylesToTextPresentation(TextPresentation tp, StyleRange[] containersStyleRanges) {
 		
 		List<StyleRange> allowedStyleRanges = new ArrayList<StyleRange>();
 		
 		Iterator it = tp.getAllStyleRangeIterator();
 		while (it.hasNext()) {
 			StyleRange styleRange = (StyleRange) it.next();
 			
 			boolean overlap = false;
 			for (StyleRange containersStyleRange : containersStyleRanges) {
 				if (doStyleRangesOverlap(styleRange, containersStyleRange)) {
 					overlap = true;
 					break;
 				}
 			}
 			
 			if (!overlap) {		
 				allowedStyleRanges.add(styleRange);
 			}
 		}
 		
 		/* Convert to array */
 		StyleRange[] allowedStyleRangesArray = new StyleRange[allowedStyleRanges.size()];
 		for (int i = 0; i < allowedStyleRanges.size(); i++) {
 			allowedStyleRangesArray[i] = allowedStyleRanges.get(i);
 		}
 		
 		StyleRange defaultStyleRange = tp.getDefaultStyleRange();
 		
 		tp.clear();
 		tp.setDefaultStyleRange(defaultStyleRange);		
 		tp.mergeStyleRanges(allowedStyleRangesArray);
 		tp.mergeStyleRanges(containersStyleRanges);		
 	}
 	
 	protected boolean doStyleRangesOverlap(StyleRange a, StyleRange b) {
 		
 		if (a.start <= b.start) {
 			if (b.start < a.start + a.length) {
 				return true;
 			}			
 		}
 		else {
 			if (a.start < b.start + b.length) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void printTextPresentationStyleRanges(TextPresentation tp) {
 		System.out.println("\n== TextPresentation== ");
 		Iterator it = tp.getAllStyleRangeIterator();
 		while (it.hasNext()) {
 			StyleRange styleRange = (StyleRange) it.next();
 			System.out.println(styleRange);
 		}
 		System.out.println("\n");
 	}
 		
 	protected StyleRange[] getContainersStyleRanges() {	
 		
 		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		for (Container c : fContainerManager.getContainers()) {
 			styleRanges.addAll(getContainerStyles(c));
 		}
 
 		StyleRange[] rangeArray = new StyleRange[styleRanges.size()];
 		for (int i = 0; i < styleRanges.size(); i++) {
 			rangeArray[i] = styleRanges.get(i);
 		}
 				
 		return rangeArray;
 	}	
 	
 	protected List<StyleRange> getContainerStyles(Container c) {
 		List<StyleRange> styles = new ArrayList<StyleRange>();
 		
 		Position p = c.getPosition();
 
 		int descent = // TODO: Quickly fix it!!!
 			(c.getComposite().getSize().y < 10)
 				? 0
 				: c.getComposite().getSize().y - 10;
 
 		/* First symbol is shaped by container's geometry */
 		StyleRange firstSymbol = new StyleRange();
 		firstSymbol.start = p.getOffset();
 		firstSymbol.length = 1;
 		firstSymbol.metrics = new GlyphMetrics(0, descent, c.getComposite().getSize().x);
 
 		/* Setting data */
 		firstSymbol.data = c;
 
 		styles.add(firstSymbol);
 
 		/* Other symbols in container's text region becomes invisible */
 		StyleRange hiddenText = new StyleRange();
 		hiddenText.start = p.getOffset() + 1;
 		hiddenText.length = p.getLength() - 1;
 		hiddenText.metrics = new GlyphMetrics(0, 0, 0);
 
 		styles.add(hiddenText);
 		
 		return styles;
 	}
 	
 }
