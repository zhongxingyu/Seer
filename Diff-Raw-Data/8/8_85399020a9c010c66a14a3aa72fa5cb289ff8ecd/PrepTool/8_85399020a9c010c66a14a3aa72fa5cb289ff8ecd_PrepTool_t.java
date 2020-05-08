 package ch.sbs.plugin.preptools;
 
 import java.awt.Container;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ComponentInputMap;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.plaf.ActionMapUIResource;
 import javax.swing.text.Document;
 
 import ro.sync.exml.editor.EditorPageConstants;
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.vform.VFormUtil;
 
 abstract class PrepTool {
 
 	protected final PrepToolsPluginExtension prepToolsPluginExtension;
 	protected final int menuItemNr;
 
 	/**
 	 * @param thePrepToolsPluginExtension
 	 * @param theMenuItemNr
 	 */
 	PrepTool(final PrepToolsPluginExtension thePrepToolsPluginExtension,
 			int theMenuItemNr) {
 		prepToolsPluginExtension = thePrepToolsPluginExtension;
 		menuItemNr = theMenuItemNr;
 	}
 
 	public void activate() {
 		makeToolbar();
 		final DocumentMetaInfo documentMetaInfo = prepToolsPluginExtension
 				.getDocumentMetaInfo();
 		if (documentMetaInfo != null) {
 			documentMetaInfo.setCurrentPrepTool(this);
 		}
 		prepToolsPluginExtension.selectPrepToolItem(menuItemNr);
 	}
 
 	private void makeToolbar() {
 		final JComponent[] components = getComponents();
 		prepToolsPluginExtension.toolbarPanel.removeAll();
 		for (final JComponent component : components) {
 			prepToolsPluginExtension.toolbarPanel.add(component);
 		}
 		prepToolsPluginExtension.toolbarPanel.add(new JLabel(" " + getLabel()));
 		setCurrentState(prepToolsPluginExtension.getDocumentMetaInfo());
 		relayout();
 	}
 
 	private void relayout() {
 		Container parent = prepToolsPluginExtension.toolbarPanel;
 		while (parent.getParent() != null) {
 			parent = parent.getParent();
 			parent.invalidate();
 			parent.validate();
 		}
 	}
 
 	/**
 	 * Utility method. Makes button.
 	 * 
 	 * @param theAction
 	 *            The action associated with the button
 	 * @param theLabel
 	 *            The label for the button.
 	 * @return the newly created button.
 	 */
 	protected JButton makeButton(final Action theAction, final String theLabel,
 			int theKeyEvent) {
 		// assign accelerator key to JButton
 		// http://www.stratulat.com/assign_accelerator_key_to_a_JButton.html
 		final JButton jButton = new JButton(theAction);
 		jButton.setText(theLabel);
 		assignAcceleratorKey(theAction, theLabel, theKeyEvent, jButton);
 		return jButton;
 	}
 
 	private void assignAcceleratorKey(final Action theAction,
 			final String theLabel, int theKeyEvent, final JComponent jComponent) {
 		final InputMap keyMap = new ComponentInputMap(jComponent);
 		keyMap.put(
 				KeyStroke.getKeyStroke(theKeyEvent, InputEvent.CTRL_DOWN_MASK
 						| InputEvent.ALT_DOWN_MASK), theLabel);
 		final ActionMap actionMap = new ActionMapUIResource();
 		actionMap.put(theLabel, theAction);
 		SwingUtilities.replaceUIActionMap(jComponent, actionMap);
 		SwingUtilities.replaceUIInputMap(jComponent,
 				JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);
 	}
 
 	public void setAllActionsEnabled(boolean enabled) {
 		final Action[] allActions = getAllActions();
 		for (final Action action : allActions) {
 			action.setEnabled(enabled);
 		}
 		final JComponent[] additionalComponents = getAdditionalComponents();
 		for (final JComponent component : additionalComponents) {
 			component.setEnabled(enabled);
 		}
 		if (enabled) {
 			enableStuff();
 		}
 		else {
 			disableStuff();
 		}
 	}
 
 	/**
 	 * optional hookto enable gui stuff
 	 */
 	protected void enableStuff() {
 
 	}
 
 	/**
 	 * optional hook to disable gui stuff
 	 */
 	protected void disableStuff() {
 
 	}
 
 	/**
 	 * hook to provide label
 	 */
 	protected abstract String getLabel();
 
 	/**
 	 * hook to provide components
 	 */
 	protected abstract JComponent[] getComponents();
 
 	/**
 	 * hook to provide all actions
 	 */
 	protected abstract Action[] getAllActions();
 
 	/**
 	 * optional hook to provide all additional components that aren't covered
 	 * with hook getAllActions().
 	 */
 	protected JComponent[] getAdditionalComponents() {
 		return new JComponent[0];
 	}
 
 	/**
 	 * hook to provide mnemonic for this tool
 	 */
 	public abstract int getMnemonic();
 
 	public void setCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
		if (theDocumentMetaInfo != null && theDocumentMetaInfo.isDtBook()) {
 			doSetCurrentState(theDocumentMetaInfo);
 		}
 		else {
 			setAllActionsEnabled(false);
 		}
 	}
 
 	/**
 	 * optional hook to updated current state of this tool
 	 */
 	protected void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 
 	}
 }
 
 class VFormPrepTool extends PrepTool {
 
 	static class MetaInfo implements DocumentMetaInfo.MetaInfo {
 		private boolean hasStartedChecking;
 		private boolean isDoneChecking;
 		private Pattern currentPattern;
 
 		public MetaInfo() {
 			setPatternTo3rdPP();
 		}
 
 		/**
 		 * 
 		 * @return true if vform pattern is set to all.
 		 */
 		public boolean patternIsAll() {
 			return currentPattern == VFormUtil.getAllPattern();
 		}
 
 		/**
 		 * Sets vform pattern to all.
 		 */
 		public void setPatternToAll() {
 			currentPattern = VFormUtil.getAllPattern();
 		}
 
 		/**
 		 * Sets vform pattern to 3rdPersonPlural.
 		 */
 		public void setPatternTo3rdPP() {
 			currentPattern = VFormUtil.get3rdPPPattern();
 		}
 
 		/**
 		 * True when done checking vforms.
 		 */
 		public void done() {
 			setDoneChecking(true);
 		}
 
 		/**
 		 * 
 		 * @return if currently processing.
 		 */
 		@Override
 		public boolean isProcessing() {
 			return isProcessingVform();
 		}
 
 		/**
 		 * 
 		 * @return if currently processing.
 		 */
 		private boolean isProcessingVform() {
 			return hasStartedChecking() && !doneChecking();
 		}
 
 		/**
 		 * True if has started checking vform.
 		 * 
 		 * @param theHasStartedChecking
 		 */
 		void setHasStartedChecking(final boolean theHasStartedChecking) {
 			hasStartedChecking = theHasStartedChecking;
 		}
 
 		/**
 		 * True if has started checking vform.
 		 * 
 		 * @return True if has started checking vform.
 		 */
 		public boolean hasStartedChecking() {
 			return hasStartedChecking;
 		}
 
 		/**
 		 * True if has finished checking vform.
 		 * 
 		 * @param setDoneChecking
 		 */
 		public void setDoneChecking(final boolean setDoneChecking) {
 			isDoneChecking = setDoneChecking;
 		}
 
 		/**
 		 * True if has finished checking vform.
 		 * 
 		 * @return
 		 */
 		public boolean doneChecking() {
 			return isDoneChecking;
 		}
 
 		/**
 		 * 
 		 * @return current vform-pattern.
 		 */
 		public Pattern getCurrentPattern() {
 			return currentPattern;
 		}
 
 	}
 
 	static final String LABEL = "VForms";
 
 	VFormPrepTool(PrepToolsPluginExtension prepToolsPluginExtension,
 			int theMenuItemNr) {
 		super(prepToolsPluginExtension, theMenuItemNr);
 	}
 
 	private TrafficLight trafficLight;
 
 	private JCheckBox allForms;
 
 	private final Action startAction = new VFormStartAction(
 			prepToolsPluginExtension);
 	private final Action findAction = new VFormFindAction(
 			prepToolsPluginExtension);
 	private final Action acceptAction = new VFormAcceptAction(
 			prepToolsPluginExtension);
 
 	@Override
 	protected String getLabel() {
 		return LABEL;
 	}
 
 	@Override
 	protected JComponent[] getComponents() {
 		return new JComponent[] {
 				makeButton(startAction, "Start", KeyEvent.VK_7),
 				makeButton(findAction, "Find", KeyEvent.VK_8),
 				makeButton(acceptAction, "Accept", KeyEvent.VK_9),
 				allForms = makeCheckbox(), trafficLight = new TrafficLight(26) };
 	}
 
 	@Override
 	public int getMnemonic() {
 		return KeyEvent.VK_V;
 	}
 
 	@Override
 	public void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 		allForms.setSelected(theDocumentMetaInfo.vform.patternIsAll());
 
 		// TODO: this belongs in superclass
 		final boolean isTextPage = theDocumentMetaInfo.getCurrentEditorPage()
 				.equals(EditorPageConstants.PAGE_TEXT);
 
 		/*
 		 TODO: make this table driven or something
 			 We have:
 			 - isTextPage: true/false
 			                           possible states
 			 - hasStartedCheckingVform  0 1 1
 			 - doneCheckingVform        0 0 1
 			 UI elements
 			 - trafficLight:      go/inProgress/stop/done
 			 - vformStartAction:  enabled/disabled
 			 - vformFindAction:   enabled/disabled
 			 - vformAcceptAction: enabled/disabled
 		 */
 		if (!theDocumentMetaInfo.vform.hasStartedChecking()) {
 			if (isTextPage) {
 				trafficLight.go();
 				startAction.setEnabled(true);
 				allForms.setEnabled(true);
 			}
 			else {
 				trafficLight.stop();
 				startAction.setEnabled(false);
 				allForms.setEnabled(false);
 			}
 			findAction.setEnabled(false);
 			acceptAction.setEnabled(false);
 		}
 		else if (theDocumentMetaInfo.vform.doneChecking()) {
 			trafficLight.done();
 			if (isTextPage) {
 				startAction.setEnabled(true);
 			}
 			else {
 				startAction.setEnabled(false);
 			}
 			findAction.setEnabled(false);
 			acceptAction.setEnabled(false);
 		}
 		else {
 			if (isTextPage) {
 				trafficLight.inProgress();
 				setAllActionsEnabled(true);
 			}
 			else {
 				trafficLight.stop();
 				setAllActionsEnabled(false);
 			}
 		}
 	}
 
 	private JCheckBox makeCheckbox() {
 		final JCheckBox checkBox = new JCheckBox("All");
 		checkBox.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					prepToolsPluginExtension.getDocumentMetaInfo().vform
 							.setPatternToAll();
 				}
 				else {
 					prepToolsPluginExtension.getDocumentMetaInfo().vform
 							.setPatternTo3rdPP();
 				}
 			}
 		});
 		return checkBox;
 	}
 
 	@Override
 	protected Action[] getAllActions() {
 		return new Action[] { startAction, acceptAction, findAction };
 	}
 
 	@Override
 	protected JComponent[] getAdditionalComponents() {
 		return new JComponent[] { allForms };
 	}
 
 	@Override
 	protected void disableStuff() {
 		trafficLight.off();
 	}
 }
 
 class ParensPrepTool extends PrepTool {
 
 	static class MetaInfo implements DocumentMetaInfo.MetaInfo {
 		private boolean hasStartedChecking;
 		private boolean isDoneChecking;
 		private Iterator<Match.PositionMatch> orphanedParensIterator;
 		private final Document document;
 
 		MetaInfo(final Document theDocument) {
 			document = theDocument;
 		}
 
 		/**
 		 * Sets orphaned parens.
 		 * 
 		 * @param theOrphanedParens
 		 */
 		public void set(final List<Match> theOrphanedParens) {
 			final List<Match.PositionMatch> pml = new ArrayList<Match.PositionMatch>();
 			for (final Match match : theOrphanedParens) {
 				Match.PositionMatch mp = new Match.PositionMatch(document,
 						match);
 				pml.add(mp);
 			}
 			orphanedParensIterator = pml.iterator();
 		}
 
 		/**
 		 * Iterator-function.
 		 * 
 		 * @return true if iterator has more orphaned parens.
 		 */
 		public boolean hasNext() {
 			return orphanedParensIterator.hasNext();
 		}
 
 		/**
 		 * Iterator-function.
 		 * 
 		 * @return next orphaned paren of this iterator.
 		 */
 		public Match.PositionMatch next() {
 			return orphanedParensIterator.next();
 		}
 
 		@Override
 		public boolean isProcessing() {
 			return hasStartedChecking && !isDoneChecking;
 		}
 
 		public void setHasStartedChecking(boolean theHasStartedChecking) {
 			hasStartedChecking = theHasStartedChecking;
 		}
 
 		public boolean hasStartedChecking() {
 			return hasStartedChecking;
 		}
 
 		public void setDoneChecking(boolean theIsDoneChecking) {
 			isDoneChecking = theIsDoneChecking;
 		}
 
 		public boolean isDoneChecking() {
 			return isDoneChecking;
 		}
 
 	}
 
 	static final String LABEL = "Parens";
 
 	ParensPrepTool(PrepToolsPluginExtension prepToolsPluginExtension,
 			int theMenuItemNr) {
 		super(prepToolsPluginExtension, theMenuItemNr);
 	}
 
 	private final Action startAction = new OrphanParenStartAction(
 			prepToolsPluginExtension);
 	private final Action findNextAction = new OrphanParenFindNextAction(
 			prepToolsPluginExtension);
 
 	@Override
 	protected String getLabel() {
 		return LABEL;
 	}
 
 	@Override
 	protected JComponent[] getComponents() {
 		return new JComponent[] {
 				makeButton(startAction, "Start", KeyEvent.VK_7),
 				makeButton(findNextAction, "Find", KeyEvent.VK_8) };
 	}
 
 	@Override
 	public int getMnemonic() {
 		return KeyEvent.VK_P;
 	}
 
 	@Override
 	public void doSetCurrentState(final DocumentMetaInfo theDocumentMetaInfo) {
 		startAction.setEnabled(true);
 		findNextAction.setEnabled(theDocumentMetaInfo.orphanedParens
 				.isProcessing());
 	}
 
 	@Override
 	protected Action[] getAllActions() {
 		return new Action[] { startAction, findNextAction };
 	}
 }
