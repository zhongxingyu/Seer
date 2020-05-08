 package com.jde.ui.wizard;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu.Separator;
 import javax.swing.JSeparator;
 
 /**
  * class Wizard - Java implementation of a wizard (as seen in most of
  * installation tools)
  * 
  * The purpose of the Wizard class is to provide a component able to navigate
  * through generic pages @see WizardPage as most of wizards are designed.
  * 
  */
 public class Wizard extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 2859686714323907243L;
 
	private String NEXT_TEXT = "Next >";
	private String PREV_TEXT = "< Previous";
	private String CANCEL_TEXT = "Cancel";
	private String FINISH_TEXT = "Finish";

 	private JPanel cards;
 	private JButton btnNext, btnPrev, btnCancel;
 	private List<WizardPage> pages;
 
 	private int currentPage, maxPages;
 	private String img;
 
 	private final Collection<WizardEventListener> listeners;
 
 	// TODO handle the locale
 	@SuppressWarnings("unused")
 	private Locale locale;
 
 	/**
 	 * Wizard default constructor
 	 * 
 	 * @param imgPath
 	 *            - path the image available on the left side of the wizard (can
 	 *            be null)
 	 * @param pages
 	 *            - list of @see WizardPage to be loaded by the wizard
 	 * @param l
 	 *            - Locale to be used (default is used if set to null)
 	 */
 	public Wizard(String imgPath, List<WizardPage> pages, Locale l) {
 		super();
 
		btnNext = createButton(NEXT_TEXT, null, this);
		btnPrev = createButton(PREV_TEXT, null, this);
		btnCancel = createButton(CANCEL_TEXT, null, this);
 
 		btnPrev.setEnabled(false);
 
 		this.pages = pages;
 		this.img = imgPath;
 		this.locale = l;
 
 		currentPage = 0;
 		maxPages = pages.size() - 1;
 
 		cards = new JPanel(new CardLayout());
 		listeners = new ArrayList<WizardEventListener>();
 		updateUi();
 	}
 
 	/**
 	 * Wizard constructor
 	 * 
 	 * @param imgPath
 	 *            - path the image available on the left side of the wizard (can
 	 *            be null)
 	 * @param pages
 	 *            - list of @see WizardPage to be loaded by the wizard
 	 */
 	public Wizard(String imgPath, List<WizardPage> pages) {
 		this(imgPath, pages, Locale.getDefault());
 	}
 
 	/**
 	 * Wizard constructor
 	 */
 	public Wizard() {
 		this(null, new ArrayList<WizardPage>(), Locale.getDefault());
 	}
 
 	/**
 	 * @{hide - under test
 	 * 
 	 *        addWizardPage() - adds WizardPages to the list of WizardPages at
 	 *        runtime
 	 * 
 	 * @warning not yet tested
 	 * 
 	 * @param p
 	 *            - the page to be added
 	 */
 	public void addWizardPage(WizardPage p) {
 		if (pages != null) {
 			pages.add(p);
 			maxPages++;
 		}
 		updateUi();
 	}
 
 	/**
 	 * @{hide - under test
 	 * 
 	 *        removeWizardPage() - removes WizardPages to the list of
 	 *        WizardPages
 	 * 
 	 * @warning not yet tested
 	 * 
 	 * @param p
 	 *            - the page to be added
 	 */
 	public void removeWizardPage(WizardPage p) {
 		if (pages != null) {
 			pages.remove(p);
 			maxPages--;
 		}
 		updateUi();
 	}
 
 	/**
 	 * @{hide - under test
 	 * 
 	 *        removeWizardPage() - removes WizardPages to the list of
 	 *        WizardPages
 	 * 
 	 * @warning not yet tested
 	 * 
 	 * @param p
 	 *            - the page to be added
 	 */
 	public void clearWizardPages() {
 		if (pages != null) {
 			pages.clear();
 			maxPages = 0;
 		}
 		updateUi();
 	}
 
 	public void setCancelButtonText(String text) {
		CANCEL_TEXT = text;
		btnCancel.setText(CANCEL_TEXT);
 	}
 
 	public void setPreviousButtonText(String text) {
		PREV_TEXT = text;
		btnPrev.setText(PREV_TEXT);
 	}
 
 	public void setNextButtonText(String text) {
		NEXT_TEXT = text;
		btnNext.setText(NEXT_TEXT);
 	}
 
 	public void setNextButtonEnable(boolean enabled) {
 		btnNext.setEnabled(enabled);
 	}
 
 	public void setPrevButtonEnable(boolean enabled) {
 		btnPrev.setEnabled(enabled);
 	}
 
 	public void setCancelButtonEnable(boolean enabled) {
 		btnCancel.setEnabled(enabled);
 	}
 
 	/* -------- Wizard internal API -------- */
 
 	private void updateUi() {
 		this.setLayout(new BorderLayout());
 
 		this.add(buildLeftPane(), BorderLayout.WEST);
 		this.add(buildMainPane(), BorderLayout.CENTER);
 
 	}
 
 	private JComponent buildLeftPane() {
 		JPanel p = new JPanel(new BorderLayout());
 
 		if (img != null)
 			p.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
 
 		return p;
 	}
 
 	/** @{hide */
 	private JButton createButton(String s, ImageIcon i, ActionListener l) {
 		JButton b = new JButton();
 		b.setText(s);
 		if (i != null)
 			b.setIcon(i);
 		b.addActionListener(l);
 		return b;
 	}
 
 	/** @{hide */
 	private JComponent buildBottomPane() {
 		JPanel p = new JPanel(new BorderLayout());
 		JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 
 		f.add(btnCancel);
 		f.add(btnPrev);
 		f.add(btnNext);
 
 		p.add(new JSeparator(Separator.HORIZONTAL), BorderLayout.NORTH);
 		p.add(f, BorderLayout.SOUTH);
 
 		return p;
 	}
 
 	/** @{hide */
 	private JComponent buildMainPane() {
 		JPanel p = new JPanel(new BorderLayout());
 
 		p.add(buildBottomPane(), BorderLayout.SOUTH);
 		for (WizardPage wp : pages)
 			cards.add(wp.name, wp.getComp());
 		p.add(cards, BorderLayout.CENTER);
 
 		return p;
 	}
 
 	/** @{hide */
 	private void loadPage() {
 		CardLayout cl = (CardLayout) (cards.getLayout());
 		cl.show(cards, pages.get(currentPage).getName());
 	}
 
 	/**
 	 * addWizardEventListener() - Adds a WizardEventListener
 	 * 
 	 * @param wel
 	 *            - the WizardEventListener to be added to the list of listeners
 	 */
 	public void addWizardEventListener(WizardEventListener wel) {
 		listeners.add(wel);
 	}
 
 	/**
 	 * removeWizardEventListener() - Removes a WizardEventListener
 	 * 
 	 * @param wel
 	 *            - the WizardEventListener to be removed from the list of
 	 *            listeners
 	 */
 	public void removeWizardEventListener(WizardEventListener wel) {
 		listeners.remove(wel);
 	}
 
 	/**
 	 * getWizardEventListeners() - Returns the list of WizardEventListener
 	 * 
 	 * @return WizardEventListener[] - the list of WizardEventListener
 	 */
 	public WizardEventListener[] getWizardEventListeners() {
 		return listeners.toArray(new WizardEventListener[0]);
 	}
 
 	/**
 	 * fireWizardEvent() - fires a WizardEvent to registered listeners
 	 * 
 	 * @param we
 	 *            - the WizardEvent
 	 */
 	protected void fireWizardEvent(WizardEvent we) {
 		for (WizardEventListener l : listeners)
 			l.actionPerformed(we);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ev) {
 		if (ev.getSource().equals(btnCancel)) {
 			fireWizardEvent(new WizardEvent(WizardEvent.WIZARD_EVENT_CANCEL));
 		} else if (ev.getSource().equals(btnNext)
				&& ev.getActionCommand().equals(FINISH_TEXT)) {
 			fireWizardEvent(new WizardEvent(WizardEvent.WIZARD_EVENT_FINISH));
 		} else if (ev.getSource().equals(btnPrev)) {
 			// walk through pages until page0
 			if (currentPage >= 0) {
 				if (currentPage == maxPages)
					btnNext.setText(NEXT_TEXT);
 
 				currentPage--;
 
 				loadPage();
 				fireWizardEvent(new WizardEvent(
 						WizardEvent.WIZARD_EVENT_PREV_PAGE));
 			}
 		} else if (ev.getSource().equals(btnNext)) {
 			// walk through pages until pageMAX
 			if (currentPage < maxPages) {
 				currentPage++;
 
 				loadPage();
 
 				btnPrev.setEnabled(currentPage != 0);
 
 				if (currentPage == maxPages)
					btnNext.setText(FINISH_TEXT);
 
 				fireWizardEvent(new WizardEvent(
 						WizardEvent.WIZARD_EVENT_NEXT_PAGE));
 			}
 		}
 		btnPrev.setEnabled(currentPage != 0);
 	}
 }
