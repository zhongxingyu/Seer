 package org.rubypeople.rdt.internal.ui.text.ruby.hover;
 
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.rubypeople.rdt.core.IMember;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.util.RDocUtil;
 import org.rubypeople.rdt.internal.ui.text.HTMLPrinter;
 import org.rubypeople.rdt.internal.ui.text.HTMLTextPresenter;
 import org.rubypeople.rdt.internal.ui.text.ruby.IInformationControlExtension4;
import org.rubypeople.rdt.internal.ui.text.ruby.hover.AbstractReusableInformationControlCreator;
import org.rubypeople.rdt.internal.ui.text.ruby.hover.AbstractRubyEditorTextHover;
import org.rubypeople.rdt.internal.ui.text.ruby.hover.BrowserInformationControl;
 import org.rubypeople.rdt.ui.RubyElementLabels;
 
public class CommentHoverProvider extends AbstractRubyEditorTextHover {
 
 	private final long LABEL_FLAGS= RubyElementLabels.ALL_FULLY_QUALIFIED | RubyElementLabels.M_PARAMETER_NAMES | RubyElementLabels.USE_RESOLVED;
 	private final long LOCAL_VARIABLE_FLAGS= LABEL_FLAGS & ~RubyElementLabels.F_FULLY_QUALIFIED | RubyElementLabels.F_POST_QUALIFIED;
 	
 	/**
 	 * The hover control creator.
 	 * 
 	 * @since 1.0
 	 */
 	private IInformationControlCreator fHoverControlCreator;
 	/**
 	 * The presentation control creator.
 	 * 
 	 * @since 1.0
 	 */
 	private IInformationControlCreator fPresenterControlCreator;
 	
 	/*
 	 * @see RubyElementHover
 	 */
 	protected String getHoverInfo(IRubyElement[] result) {
 
 		StringBuffer buffer= new StringBuffer();
 		int nResults= result.length;
 		if (nResults == 0)
 			return null;
 
 		boolean hasContents= false;
 		if (nResults > 1) {
 
 			for (int i= 0; i < result.length; i++) {
 				HTMLPrinter.startBulletList(buffer);
 				IRubyElement curr= result[i];
 				if (curr instanceof IMember || curr.getElementType() == IRubyElement.LOCAL_VARIABLE) {
 					HTMLPrinter.addBullet(buffer, getInfoText(curr));
 					hasContents= true;
 				}
 				HTMLPrinter.endBulletList(buffer);
 			}
 
 		} else {
 
 			IRubyElement curr= result[0];
 			if (curr instanceof IMember) {
 				IMember member= (IMember) curr;
 				HTMLPrinter.addSmallHeader(buffer, getInfoText(member));
 				String contents = RDocUtil.getHTMLDocumentation(member);			
 				if (contents != null) {
 					HTMLPrinter.addParagraph(buffer, contents);
 				}
 				hasContents= true;
 			} else if (curr.getElementType() == IRubyElement.LOCAL_VARIABLE) {
 				HTMLPrinter.addSmallHeader(buffer, getInfoText(curr));
 				hasContents= true;
 			}
 		}
 		
 		if (!hasContents)
 			return null;
 
 		if (buffer.length() > 0) {
 			HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
 			HTMLPrinter.addPageEpilog(buffer);
 			return buffer.toString();
 		}
 
 		return null;
 	}
 
 	/*
 	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
 	 * @since 1.0
 	 */
 	public IInformationControlCreator getInformationPresenterControlCreator() {
 		if (fPresenterControlCreator == null) {
 			fPresenterControlCreator= new AbstractReusableInformationControlCreator() {
 
 				/*
 				 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
 				 */
 				public IInformationControl doCreateInformationControl(Shell parent) {
 					int shellStyle= SWT.RESIZE | SWT.TOOL;
 					int style= SWT.V_SCROLL | SWT.H_SCROLL;
 					if (BrowserInformationControl.isAvailable(parent))
 						return new BrowserInformationControl(parent, shellStyle, style);
 					else
 						return new DefaultInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
 				}
 			};
 		}
 		return fPresenterControlCreator;
 	}
 	
 	/*
 	 * @see ITextHoverExtension#getHoverControlCreator()
 	 * @since 1.0
 	 */
 	public IInformationControlCreator getHoverControlCreator() {
 		if (fHoverControlCreator == null) {
 			fHoverControlCreator= new AbstractReusableInformationControlCreator() {
 				
 				/*
 				 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
 				 */
 				public IInformationControl doCreateInformationControl(Shell parent) {
 					if (BrowserInformationControl.isAvailable(parent))
 						return new BrowserInformationControl(parent, SWT.TOOL | SWT.NO_TRIM, SWT.NONE, getTooltipAffordanceString());
 					else
 						return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true), getTooltipAffordanceString());
 				}
 				
 				/*
 				 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
 				 */
 				public boolean canReuse(IInformationControl control) {
 					boolean canReuse= super.canReuse(control);
 					if (canReuse && control instanceof IInformationControlExtension4)
 						((IInformationControlExtension4)control).setStatusText(getTooltipAffordanceString());
 					return canReuse;
 						
 				}
 			};
 		}
 		return fHoverControlCreator;
 	}
 	
 	private String getInfoText(IRubyElement member) {
 		long flags= member.getElementType() == IRubyElement.LOCAL_VARIABLE ? LOCAL_VARIABLE_FLAGS : LABEL_FLAGS;
 		String label= RubyElementLabels.getElementLabel(member, flags);
 		StringBuffer buf= new StringBuffer();
 		for (int i= 0; i < label.length(); i++) {
 			char ch= label.charAt(i);
 			if (ch == '<') {
 				buf.append("&lt;"); //$NON-NLS-1$
 			} else if (ch == '>') {
 				buf.append("&gt;"); //$NON-NLS-1$
 			} else {
 				buf.append(ch);
 			}
 		}
 		return buf.toString();
 	}
 }
