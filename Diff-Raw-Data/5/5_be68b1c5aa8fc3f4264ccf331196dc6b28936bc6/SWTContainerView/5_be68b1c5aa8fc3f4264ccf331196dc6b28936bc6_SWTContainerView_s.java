 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.views;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.wazaabi.engine.core.CoreSingletons;
 import org.eclipse.wazaabi.engine.core.editparts.AbstractComponentEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.ContainerEditPart;
 import org.eclipse.wazaabi.engine.core.gef.EditPart;
 import org.eclipse.wazaabi.engine.core.views.AbstractComponentView;
 import org.eclipse.wazaabi.engine.core.views.ContainerView;
 import org.eclipse.wazaabi.engine.core.views.WidgetView;
 import org.eclipse.wazaabi.engine.swt.editparts.stylerules.managers.StackLayoutStyleRuleManager;
 import org.eclipse.wazaabi.mm.core.Orientation;
 import org.eclipse.wazaabi.mm.core.Position;
 import org.eclipse.wazaabi.mm.core.styles.BarLayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.BlankRule;
 import org.eclipse.wazaabi.mm.core.styles.BooleanRule;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesPackage;
 import org.eclipse.wazaabi.mm.core.styles.ExpandLayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.LayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.SashFormLayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.StackLayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.StringRule;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
 import org.eclipse.wazaabi.mm.core.styles.TabbedLayoutRule;
 import org.eclipse.wazaabi.mm.core.styles.impl.BarLayoutRuleImpl;
 import org.eclipse.wazaabi.mm.core.widgets.AbstractComponent;
 import org.eclipse.wazaabi.mm.swt.descriptors.SWTDescriptorsPackage;
 
 public class SWTContainerView extends SWTControlView implements ContainerView {
 
 	public EClass getWidgetViewEClass() {
 		return SWTDescriptorsPackage.Literals.COMPOSITE;
 	}
 
 	protected Widget createSWTWidget(Widget parent, int swtStyle, int index) {
 		for (StyleRule rule : ((StyledElement) getHost().getModel())
 				.getStyleRules()) {
 			if (rule instanceof BarLayoutRuleImpl
 					&& ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 							.getPropertyName())) {
 				if (((BarLayoutRule) rule).isDraggable()) {
 					// If the elements are draggable, then we need a coolbar
 					CoolBar bar = new CoolBar(
 							(org.eclipse.swt.widgets.Composite) parent,
 							computeSWTCreationStyle(getHost()));
 					bar.setLocked(false);
 					return bar;
 				} else {
 					// If the elements are not draggable, we need a toolbar
 					return new ToolBar(
 							(org.eclipse.swt.widgets.Composite) parent,
 							computeSWTCreationStyle(getHost()));
 				}
 			} else if (rule instanceof TabbedLayoutRule
 					&& ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 							.getPropertyName())) {
 				CTabFolder folder = new CTabFolder(
 						(org.eclipse.swt.widgets.Composite) parent,
 						computeSWTCreationStyle(getHost()));
 				folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 						false));
 				folder.setMaximizeVisible(((TabbedLayoutRule) rule)
 						.isMaximizeVisible());
 				folder.setMinimizeVisible(((TabbedLayoutRule) rule)
 						.isMinimizeVisible());
 				return folder;
 			} else if (rule instanceof ExpandLayoutRule
 					&& ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 							.getPropertyName())) {
 				ExpandBar expandBar = new ExpandBar(
 						(org.eclipse.swt.widgets.Composite) parent,
 						computeSWTCreationStyle(getHost()) | SWT.V_SCROLL);
 				expandBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 						false));
 				return expandBar;
 			} else if (rule instanceof SashFormLayoutRule
 					&& ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 							.getPropertyName())) {
 				SashForm sashForm = new SashForm(
 						(org.eclipse.swt.widgets.Composite) parent,
 						computeSWTCreationStyle(getHost()));
 				sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 						false));
 				return sashForm;
 			}
 		}
 
 		StringRule containerTitleRule = (StringRule) ((StyledElement) getHost()
 				.getModel()).getFirstStyleRule(
 				ContainerEditPart.TITLE_VALUE_PROPERTY_NAME, null);
 		BooleanRule containerBorderRule = (BooleanRule) ((StyledElement) getHost()
 				.getModel()).getFirstStyleRule(
 				ContainerEditPart.TITLE_BORDER_PROPERTY_NAME, null);
 		Composite composite;
 		if (containerTitleRule != null && containerBorderRule != null
 				&& !containerTitleRule.getValue().equalsIgnoreCase("")) {
 			composite = new org.eclipse.swt.widgets.Group((Composite) parent,
 					computeSWTCreationStyle(getHost()));
 			((Group) composite).setText(containerTitleRule.getValue());
 		} else {
 			composite = new org.eclipse.swt.widgets.Composite(
 					(org.eclipse.swt.widgets.Composite) parent,
 					computeSWTCreationStyle(getHost()));
 		}
 		return checkParentLayout((Composite) parent, composite);
 
 	}
 
 	private LayoutRule currentLayoutRule = null;
 
 	protected void setLayout(LayoutRule rule) {
 		if (!(rule instanceof BlankRule))
 			currentLayoutRule = rule;
 		else
 			currentLayoutRule = (LayoutRule) ((StyledElement) getHost()
 					.getModel()).getFirstStyleRule(
 					ContainerEditPart.LAYOUT_PROPERTY_NAME,
 					CoreStylesPackage.Literals.LAYOUT_RULE);
 
 		if (currentLayoutRule != null)
 			CoreSingletons.getComposedStyleRuleManagerFactory()
 					.platformSpecificRefresh(this, currentLayoutRule);
 		else
 			((Composite) getSWTControl()).setLayout(null);
 		revalidate();
 	}
 
 	@Override
 	protected int computeSWTCreationStyle(StyleRule rule) {
 		if (ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 				.getPropertyName()) && rule instanceof SashFormLayoutRule) {
 			if (((SashFormLayoutRule) rule).getOrientation() == Orientation.VERTICAL)
 				return SWT.VERTICAL;
 			return SWT.HORIZONTAL;
 		} else if (ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 				.getPropertyName()) && rule instanceof TabbedLayoutRule) {
 			int style = SWT.None;
 			if (((TabbedLayoutRule) rule).getPosition() == Position.TOP) {
 				style |= SWT.TOP;
 			}
 			if (((TabbedLayoutRule) rule).getPosition() == Position.BOTTOM) {
 				style |= SWT.BOTTOM;
 			}
 			return style;
 		}
 		return super.computeSWTCreationStyle(rule);
 	}
 
 	@Override
 	public void updateStyleRule(StyleRule rule) {
 		if (rule == null)
 			return;
 		if (ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(rule
 				.getPropertyName())) {
 			if (rule instanceof LayoutRule) {
 				setLayout((LayoutRule) rule);
 			} else
 				setLayout(null);
 		} else
 			super.updateStyleRule(rule);
 	}
 
 	@Override
 	public boolean needReCreateWidgetView(StyleRule styleRule) {
 		if (styleRule == null) {
 			return false;
 		}
 		org.eclipse.swt.widgets.Widget widget = getSWTWidget();
 		if ((widget instanceof SashForm)
 				&& styleRule instanceof SashFormLayoutRule) {
 			return !(isStyleBitCorrectlySet(widget,
 					org.eclipse.swt.SWT.HORIZONTAL,
 					Orientation.HORIZONTAL == ((SashFormLayoutRule) styleRule)
 							.getOrientation()) & isStyleBitCorrectlySet(widget,
 					org.eclipse.swt.SWT.VERTICAL,
 					Orientation.VERTICAL == ((SashFormLayoutRule) styleRule)
 							.getOrientation()));
 			// we catch the border rule since apparently this SWT widget does
 			// not manage it
 
 		} else if (ContainerEditPart.LAYOUT_PROPERTY_NAME.equals(styleRule
 				.getPropertyName())
 				&& (styleRule instanceof BarLayoutRule
 						|| styleRule instanceof TabbedLayoutRule
 						|| styleRule instanceof ExpandLayoutRule || styleRule instanceof SashFormLayoutRule)) {
 			return true;
 		} else if (ContainerEditPart.TITLE_VALUE_PROPERTY_NAME.equals(styleRule
 				.getPropertyName())
 				&& styleRule instanceof StringRule
 				&& !((StringRule) styleRule).getValue().equalsIgnoreCase("")) {
 			BooleanRule containerBorderRule = (BooleanRule) ((StyledElement) getHost()
 					.getModel()).getFirstStyleRule(
 					ContainerEditPart.TITLE_BORDER_PROPERTY_NAME, null);
 			if (containerBorderRule != null) {
 				return true;
 			} else {
 				return super.needReCreateWidgetView(styleRule);
 			}
 		} else if (ContainerEditPart.TITLE_BORDER_PROPERTY_NAME
 				.equals(styleRule.getPropertyName())
 				&& styleRule instanceof BooleanRule) {
 			StringRule containerTitleRule = (StringRule) ((StyledElement) getHost()
 					.getModel()).getFirstStyleRule(
 					ContainerEditPart.TITLE_VALUE_PROPERTY_NAME, null);
 			if (containerTitleRule != null
 					&& !containerTitleRule.getValue().equalsIgnoreCase("")) {
 				return true;
 			} else {
 				return super.needReCreateWidgetView(styleRule);
 			}
 		} else {
 			return super.needReCreateWidgetView(styleRule);
 		}
 	}
 
 	/**
 	 * Indicates that this SWTComposite should make itself valid. Validation
 	 * includes invoking layout on a LayoutManager if present, and then
 	 * validating all children figures. Default validation uses pre-order,
 	 * depth-first ordering.
 	 */
 
 	@Override
 	public void add(WidgetView view, int index) {
 		// first we create the widget
 		super.add(view, index);
 		if (index != ((Composite) getSWTWidget()).getChildren().length - 1)
 			if (view instanceof SWTControlView)
 				reorderChild((SWTControlView) view, index);
 	}
 
 	public void reorderChild(AbstractComponentView child, int index) {
 
 		if (!(((SWTWidgetView) child).getSWTWidget() instanceof org.eclipse.swt.widgets.Control)
 				|| ((SWTWidgetView) child).getSWTWidget().isDisposed())
 			return;
 
 		// get the SWT Control child
 		final org.eclipse.swt.widgets.Control childControl = (org.eclipse.swt.widgets.Control) ((SWTWidgetView) child)
 				.getSWTWidget();
 		// get the SWT Composite (this)
 		final org.eclipse.swt.widgets.Composite composite = (org.eclipse.swt.widgets.Composite) getSWTWidget();
 
 		EditPart parentModel = (EditPart) getHost();
 		if (parentModel instanceof ContainerEditPart
 				&& parentModel.getModel() != null) {
 			StyleRule parentLayoutRule = ((StyledElement) parentModel
 					.getModel()).getFirstStyleRule(
 					ContainerEditPart.LAYOUT_PROPERTY_NAME,
 					CoreStylesPackage.Literals.SASH_FORM_LAYOUT_RULE);
 			if (parentLayoutRule != null) {
 				return;
 			}
 		}
 
 		if (childControl.getParent() != composite)
 			return;
 		int oldIndex = -1;
 		for (int i = 0; i < composite.getChildren().length; i++)
 			if (composite.getChildren()[i] == childControl) {
 				oldIndex = i;
 				break;
 			}
 		if (index == oldIndex)
 			return;
 
 		if (oldIndex < index)
 			childControl.moveBelow(composite.getChildren()[index]);
 		else
 			childControl.moveAbove(composite.getChildren()[index]);
 
 	}
 
 	@Override
 	public void validate() {
 		if (currentLayoutRule instanceof StackLayoutRule)
 			StackLayoutStyleRuleManager.platformSpecificRefresh(this,
 					(StackLayoutRule) currentLayoutRule);
 		super.validate();
 	}
 
 	public void refreshTabIndexes() {
 		if (getSWTWidget().isDisposed())
 			return;
 		SortedMap<Integer, Control> tabList = null;
 		for (EditPart child : getHost().getChildren()) {
 			if (child.getModel() instanceof AbstractComponent
 					&& ((AbstractComponentEditPart) child).getWidgetView() instanceof SWTWidgetView) {
 				int index = ((AbstractComponent) child.getModel())
 						.getTabIndex();
 				if (index != -1) {
 					if (tabList == null)
 						tabList = new TreeMap<Integer, Control>();
 					tabList.put(
 							index,
 							(Control) ((SWTWidgetView) ((AbstractComponentEditPart) child)
 									.getWidgetView()).getSWTWidget());
 				}
 			}
 		}
		((Composite) getSWTWidget()).setTabList((Control[]) tabList.values()
				.toArray(new Control[] {}));
 	}
 
 }
