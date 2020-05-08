 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.beans.common.TestBean;
 import org.eclipse.riena.beans.common.WordNode;
 import org.eclipse.riena.example.client.views.TextSubModuleView;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.model.SimpleNavigationNodeAdapter;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.core.marker.AttentionMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.ICompositeTableRidget;
 import org.eclipse.riena.ui.ridgets.IDateTextRidget;
 import org.eclipse.riena.ui.ridgets.IDateTimeRidget;
 import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
 import org.eclipse.riena.ui.ridgets.IGroupedTreeTableRidget;
 import org.eclipse.riena.ui.ridgets.IListRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.INumericTextRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRowRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree2.ITreeNode;
 import org.eclipse.riena.ui.ridgets.tree2.TreeNode;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 
 /**
  * Controller for the {@link TextSubModuleView} example.
  */
 public class MarkerSubModuleController extends SubModuleController {
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 		final ITextRidget textName = (ITextRidget) getRidget("textName"); //$NON-NLS-1$
 		textName.setText("Chateau Schaedelbrummer"); //$NON-NLS-1$
 
 		final IDecimalTextRidget textPrice = (IDecimalTextRidget) getRidget("textPrice"); //$NON-NLS-1$
 		textPrice.setGrouping(true);
		textPrice.setText(Double.toString(-29.99));
 
 		final INumericTextRidget textAmount = (INumericTextRidget) getRidget("textAmount"); //$NON-NLS-1$
 		textAmount.setSigned(false);
 		textAmount.setGrouping(true);
 		textAmount.setText("1001"); //$NON-NLS-1$
 
 		final IDateTextRidget textDate = (IDateTextRidget) getRidget("textDate"); //$NON-NLS-1$
 		textDate.setFormat(IDateTextRidget.FORMAT_DDMMYYYY);
 		textDate.setText("04.12.2008"); //$NON-NLS-1$
 
 		final IDateTimeRidget dtDate = (IDateTimeRidget) getRidget("dtDate"); //$NON-NLS-1$
 		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
 		try {
 			Date date = dateFormat.parse("04.12.2008"); //$NON-NLS-1$
 			dtDate.setDate(date);
 		} catch (ParseException e) {
 			dtDate.setDate(new Date());
 		}
 
 		final IComboRidget comboAge = (IComboRidget) getRidget("comboAge"); //$NON-NLS-1$
 		List<String> ages = Arrays.asList(new String[] { "<none>", "young", "moderate", "aged", "old" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		comboAge.bindToModel(new WritableList(ages, String.class), String.class, null, new WritableValue());
 		comboAge.updateFromModel();
 		comboAge.setEmptySelectionItem("<none>"); //$NON-NLS-1$
 		comboAge.setSelection(1);
 
 		final ISingleChoiceRidget choiceType = (ISingleChoiceRidget) getRidget("choiceType"); //$NON-NLS-1$
 		choiceType.bindToModel(Arrays.asList("red", "white", "rose"), (List<String>) null, new TestBean(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				TestBean.PROPERTY);
 		choiceType.updateFromModel();
 		choiceType.setSelection("red"); //$NON-NLS-1$
 
 		final IMultipleChoiceRidget choiceFlavor = (IMultipleChoiceRidget) getRidget("choiceFlavor"); //$NON-NLS-1$
 		choiceFlavor.bindToModel(Arrays.asList("dry", "sweet", "sour", "spicy"), (List<String>) null, new TestBean(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 				TestBean.PROPERTY);
 		choiceFlavor.updateFromModel();
 		choiceFlavor.setSelection(Arrays.asList("dry")); //$NON-NLS-1$
 
 		final IListRidget listPersons = (IListRidget) getRidget("listPersons"); //$NON-NLS-1$
 		listPersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		listPersons.bindToModel(createPersonList(), Person.class, "listEntry"); //$NON-NLS-1$
 		listPersons.updateFromModel();
 
 		final ITableRidget tablePersons = (ITableRidget) getRidget("tablePersons"); //$NON-NLS-1$
 		tablePersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		String[] colValues = new String[] { "lastname", "firstname" }; //$NON-NLS-1$ //$NON-NLS-2$
 		String[] colHeaders = new String[] { "Last Name", "First Name" }; //$NON-NLS-1$ //$NON-NLS-2$
 		tablePersons.bindToModel(createPersonList(), Person.class, colValues, colHeaders);
 		tablePersons.updateFromModel();
 
 		final ICompositeTableRidget compTable = (ICompositeTableRidget) getRidget("compTable"); //$NON-NLS-1$
 		WritableList input = new WritableList(PersonFactory.createPersonList(), Person.class);
 		compTable.bindToModel(input, Person.class, RowRidget.class);
 		compTable.updateFromModel();
 
 		final ITreeRidget treePersons = (ITreeRidget) getRidget("treePersons"); //$NON-NLS-1$
 		treePersons.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		treePersons.bindToModel(createTreeRoots(), ITreeNode.class, ITreeNode.PROPERTY_CHILDREN,
 				ITreeNode.PROPERTY_PARENT, ITreeNode.PROPERTY_VALUE);
 		treePersons.updateFromModel();
 
 		final IGroupedTreeTableRidget treeWCols = (IGroupedTreeTableRidget) getRidget("treeWCols"); //$NON-NLS-1$
 		treeWCols.setSelectionType(ISelectableRidget.SelectionType.MULTI);
 		treeWCols.setGroupingEnabled(true);
 		colValues = new String[] { "word", "ACount" }; //$NON-NLS-1$ //$NON-NLS-2$
 		colHeaders = new String[] { "Word", "#A" }; //$NON-NLS-1$ //$NON-NLS-2$
 		treeWCols.bindToModel(createTreeTableRoots(), WordNode.class, ITreeNode.PROPERTY_CHILDREN,
 				ITreeNode.PROPERTY_PARENT, colValues, colHeaders);
 		treeWCols.updateFromModel();
 
 		final IToggleButtonRidget buttonToggle = (IToggleButtonRidget) getRidget("buttonToggle"); //$NON-NLS-1$
 		buttonToggle.setText("Toggle"); //$NON-NLS-1$
 		buttonToggle.setSelected(true);
 		final IActionRidget buttonPush = (IActionRidget) getRidget("buttonPush"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonRadioA = (IToggleButtonRidget) getRidget("buttonRadioA"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonRadioB = (IToggleButtonRidget) getRidget("buttonRadioB"); //$NON-NLS-1$
 		final IToggleButtonRidget buttonCheck = (IToggleButtonRidget) getRidget("buttonCheck"); //$NON-NLS-1$
 
 		final IRidget[] markables = new IRidget[] { textName, textPrice, textAmount, textDate, dtDate, comboAge,
 				choiceType, choiceFlavor, listPersons, tablePersons, compTable, treePersons, treeWCols, buttonToggle,
 				buttonPush, buttonRadioA, buttonRadioB, buttonCheck };
 
 		final IToggleButtonRidget checkMandatory = (IToggleButtonRidget) getRidget("checkMandatory"); //$NON-NLS-1$
 		final IToggleButtonRidget checkError = (IToggleButtonRidget) getRidget("checkError"); //$NON-NLS-1$
 		final IToggleButtonRidget checkDisabled = (IToggleButtonRidget) getRidget("checkDisabled"); //$NON-NLS-1$
 		final IToggleButtonRidget checkOutput = (IToggleButtonRidget) getRidget("checkOutput"); //$NON-NLS-1$
 		final IToggleButtonRidget checkHidden = (IToggleButtonRidget) getRidget("checkHidden"); //$NON-NLS-1$
 		final IToggleButtonRidget checkHiddenParent = (IToggleButtonRidget) getRidget("checkHiddenParent"); //$NON-NLS-1$
 
 		checkMandatory.setText("&mandatory"); //$NON-NLS-1$
 		checkMandatory.addListener(new IActionListener() {
 			public void callback() {
 				boolean isMandatory = checkMandatory.isSelected();
 				for (IRidget ridget : markables) {
 					if (ridget instanceof IMarkableRidget) {
 						((IMarkableRidget) ridget).setMandatory(isMandatory);
 					} else {
 						String name = ridget.getClass().getSimpleName();
 						System.out.println("No mandatory marker support on " + name); //$NON-NLS-1$
 					}
 				}
 				if (isMandatory) {
 					textName.setText(""); //$NON-NLS-1$
 					textPrice.setText(""); //$NON-NLS-1$
 					textAmount.setText(null);
 					textDate.setText(null);
 					comboAge.setSelection("<none>"); //$NON-NLS-1$
 					choiceType.setSelection(null);
 					choiceFlavor.setSelection(null);
 					listPersons.setSelection((Object) null);
 					tablePersons.setSelection((Object) null);
 					compTable.clearSelection();
 					treePersons.setSelection((Object) null);
 					treeWCols.setSelection((Object) null);
 					buttonToggle.setSelected(false);
 					buttonRadioA.setSelected(false);
 					buttonRadioB.setSelected(false);
 					buttonCheck.setSelected(false);
 				}
 			}
 		});
 
 		checkError.setText("&error"); //$NON-NLS-1$
 		checkError.addListener(new IActionListener() {
 			private IValidator alwaysWrong = new AlwaysWrongValidator();
 
 			public void callback() {
 				boolean isError = checkError.isSelected();
 				for (IRidget ridget : markables) {
 					if (ridget instanceof IMarkableRidget) {
 						((IMarkableRidget) ridget).setErrorMarked(isError);
 					}
 				}
 				// using this "always wrong" validator for purposes of this
 				// demo. It prevents the error marker being removed from the
 				// text field on the next revalidation (i.e. when the user
 				// types).
 				if (isError) {
 					textName.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 					textPrice.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 					textAmount.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 					textDate.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 				} else {
 					textName.removeValidationRule(alwaysWrong);
 					textPrice.removeValidationRule(alwaysWrong);
 					textAmount.removeValidationRule(alwaysWrong);
 					textDate.removeValidationRule(alwaysWrong);
 				}
 			}
 		});
 
 		checkDisabled.setText("&disabled"); //$NON-NLS-1$
 		checkDisabled.addListener(new DisabledActionListener(markables, checkDisabled));
 
 		checkOutput.setText("&output"); //$NON-NLS-1$
 		checkOutput.addListener(new OutputActionListener(markables, checkOutput));
 
 		checkHidden.setText("&hidden"); //$NON-NLS-1$
 		checkHidden.addListener(new HiddenActionListener(checkHidden, markables));
 
 		checkHiddenParent.setText("&hidden parent"); //$NON-NLS-1$
 		checkHiddenParent.addListener(new HiddenParentActionListener(checkHiddenParent, markables));
 
 		getNavigationNode().addSimpleListener(new SimpleNavigationNodeAdapter() {
 			@Override
 			public void afterDeactivated(INavigationNode<?> node) {
 				super.afterDeactivated(node);
 				Collection<AttentionMarker> markers = node.getMarkersOfType(AttentionMarker.class);
 				for (AttentionMarker marker : markers) {
 					node.removeMarker(marker);
 				}
 			}
 		});
 	}
 
 	// helping methods
 	// ////////////////
 
 	private WritableList createPersonList() {
 		return new WritableList(PersonFactory.createPersonList(), Person.class);
 	}
 
 	private ITreeNode[] createTreeRoots() {
 		ITreeNode rootA = new TreeNode("A"); //$NON-NLS-1$
 		new TreeNode(rootA, new Person("Albinus", "Albert")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootA, new Person("Aurelius", "Mark")); //$NON-NLS-1$ //$NON-NLS-2$
 		ITreeNode rootB = new TreeNode("B"); //$NON-NLS-1$
 		new TreeNode(rootB, new Person("Barker", "Clyve")); //$NON-NLS-1$ //$NON-NLS-2$
 		new TreeNode(rootB, new Person("Barclay", "Bob")); //$NON-NLS-1$ //$NON-NLS-2$
 		return new ITreeNode[] { rootA, rootB };
 	}
 
 	private WordNode[] createTreeTableRoots() {
 		WordNode rootA = new WordNode("A"); //$NON-NLS-1$
 		WordNode rootB = new WordNode("B"); //$NON-NLS-1$
 		new WordNode(rootA, "Astoria"); //$NON-NLS-1$
 		new WordNode(rootA, "Ashland"); //$NON-NLS-1$
 		new WordNode(rootA, "Aurora"); //$NON-NLS-1$
 		new WordNode(rootA, "Alpine"); //$NON-NLS-1$
 		new WordNode(rootB, "Boring"); //$NON-NLS-1$
 		new WordNode(rootB, "Bend"); //$NON-NLS-1$
 		new WordNode(rootB, "Beaverton"); //$NON-NLS-1$
 		new WordNode(rootB, "Bridgeport"); //$NON-NLS-1$
 		return new WordNode[] { rootA, rootB };
 	}
 
 	// helping classes
 	// ////////////////
 
 	private static final class DisabledActionListener implements IActionListener {
 
 		private final IRidget[] markables;
 		private final IToggleButtonRidget checkDisabled;
 
 		private DisabledActionListener(IRidget[] markables, IToggleButtonRidget checkDisabled) {
 			this.markables = markables;
 			this.checkDisabled = checkDisabled;
 		}
 
 		public void callback() {
 			boolean isEnabled = !checkDisabled.isSelected();
 			for (IRidget ridget : markables) {
 				ridget.setEnabled(isEnabled);
 			}
 		}
 
 	}
 
 	private static final class OutputActionListener implements IActionListener {
 
 		private final IRidget[] markables;
 		private final IToggleButtonRidget checkOutput;
 
 		private OutputActionListener(IRidget[] markables, IToggleButtonRidget checkOutput) {
 			this.markables = markables;
 			this.checkOutput = checkOutput;
 		}
 
 		public void callback() {
 			boolean isOutput = checkOutput.isSelected();
 			for (IRidget ridget : markables) {
 				if (ridget instanceof IMarkableRidget) {
 					((IMarkableRidget) ridget).setOutputOnly(isOutput);
 				} else {
 					String name = ridget.getClass().getSimpleName();
 					System.out.println("No output marker support on " + name); //$NON-NLS-1$
 				}
 			}
 		}
 
 	}
 
 	private static final class HiddenActionListener implements IActionListener {
 
 		private final IToggleButtonRidget checkHidden;
 		private final IRidget[] markables;
 
 		private HiddenActionListener(IToggleButtonRidget checkHidden, IRidget[] markables) {
 			this.checkHidden = checkHidden;
 			this.markables = markables;
 		}
 
 		public void callback() {
 			boolean isVisible = !checkHidden.isSelected();
 			for (IRidget ridget : markables) {
 				ridget.setVisible(isVisible);
 			}
 		}
 
 	}
 
 	private static final class HiddenParentActionListener implements IActionListener {
 
 		private final IToggleButtonRidget checkHiddenParent;
 		private final IRidget[] markables;
 
 		private HiddenParentActionListener(IToggleButtonRidget checkHiddenParent, IRidget[] markables) {
 			this.checkHiddenParent = checkHiddenParent;
 			this.markables = markables;
 		}
 
 		public void callback() {
 			Composite parent = ((Control) markables[0].getUIControl()).getParent();
 			boolean isVisible = !checkHiddenParent.isSelected();
 			parent.setVisible(isVisible);
 		}
 
 	}
 
 	/**
 	 * Validator that always returns an error status.
 	 */
 	private static final class AlwaysWrongValidator implements IValidator {
 		public IStatus validate(Object value) {
 			return ValidationRuleStatus.error(false, ""); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * A row ridget with two text ridgets for use with
 	 * {@link ICompositeTableRidget}.
 	 */
 	public static final class RowRidget extends AbstractCompositeRidget implements IRowRidget {
 		private Person rowData;
 
 		public void setData(Object rowData) {
 			this.rowData = (Person) rowData;
 		}
 
 		@Override
 		public void configureRidgets() {
 			ITextRidget txtLast = (ITextRidget) getRidget("txtLast"); //$NON-NLS-1$
 			txtLast.bindToModel(rowData, Person.PROPERTY_FIRSTNAME);
 			txtLast.updateFromModel();
 			ITextRidget txtFirst = (ITextRidget) getRidget("txtFirst"); //$NON-NLS-1$
 			txtFirst.bindToModel(rowData, Person.PROPERTY_FIRSTNAME);
 			txtFirst.updateFromModel();
 		}
 	}
 
 }
