 package org.eclipse.gmf.codegen.xtend.ui.handlers;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.util.CodegenEmitters;
 import org.eclipse.gmf.codegen.util.ExtensionTemplatesProviderImpl;
 import org.eclipse.gmf.codegen.util.GMFGeneratorModule;
 import org.eclipse.gmf.codegen.util.IExtensionTemplatesProvider;
 import org.eclipse.gmf.common.UnexpectedBehaviourException;
 import org.eclipse.gmf.internal.common.codegen.BinaryEmitter;
 import org.eclipse.gmf.internal.common.codegen.TextEmitter;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 @SuppressWarnings("restriction")
 public class CodegenEmittersWithXtend2 extends CodegenEmitters {
 
 	private final Injector myInjector;
 
 	private IExtensionTemplatesProvider myExtensionTemplateProvider = null;
 	
 	@Override
 	public BinaryEmitter getShortcutImageEmitter() throws UnexpectedBehaviourException {
 		// TODO Auto-generated method stub
 		return super.getShortcutImageEmitter();
 	}
 
 	@Override
 	public BinaryEmitter getDiagramIconEmitter() throws UnexpectedBehaviourException {
 		// TODO Auto-generated method stub
 		return super.getDiagramIconEmitter();
 	}
 
 	@Override
 	public BinaryEmitter getWizardBannerImageEmitter() throws UnexpectedBehaviourException {
 		// TODO Auto-generated method stub
 		return super.getWizardBannerImageEmitter();
 	}
 
 	@Override
 	public BinaryEmitter getGroupIconEmitter() throws UnexpectedBehaviourException {
 		// TODO Auto-generated method stub
 		return super.getGroupIconEmitter();
 	}
 
 	@Override
 	public GeneratorTextEmitter getModelAccessFacilityEmitter() {
 		return getMainXtendEmitter("metamodel::Facility");
 	}
 
 	@Override
 	public GeneratorTextEmitter getActivatorEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("plugin::Activator");
 	}
 
 	@Override
 	public GeneratorTextEmitter getBundleManifestEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::plugin::manifest");
 	}
 
 	@Override
 	public GeneratorTextEmitter getPluginXmlEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::plugin::plugin");
 	}
 
 	@Override
 	public GeneratorTextEmitter getPluginPropertiesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::plugin::properties");
 	}
 
 	@Override
 	public GeneratorTextEmitter getBuildPropertiesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::plugin::build");
 	}
 
 	@Override
 	public GeneratorTextEmitter getOptionsFileEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::plugin::options");
 	}
 
 	@Override
 	public GeneratorTextEmitter getExternalizeEmitter() {
 		return getPrimaryXtendEmitter("xpt::Access");
 	}
 
 	@Override
 	public String getAbstractParserName(Object... input) throws UnexpectedBehaviourException {
 		return getText(super.newXpandEmitter("impl::parsers::AbstractParser::deprecatedQualifiedClassName"), input); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getMessagesEmitter() {
 		// TODO Auto-generated method stub
 		return getPrimaryXtendEmitter("xpt::Values");
 	}
 
 	public CodegenEmittersWithXtend2(GenEditorGenerator genModel) {
 		this(!genModel.isDynamicTemplates(), genModel.getTemplateDirectory(), genModel.getModelAccess() != null);
 	}
 
 	public CodegenEmittersWithXtend2(boolean useBaseTemplatesOnly, String templateDirectory, boolean includeDynamicModelTemplates) {
 		super(useBaseTemplatesOnly, templateDirectory, includeDynamicModelTemplates);
 		if (!useBaseTemplatesOnly) {
 			myExtensionTemplateProvider = new ExtensionTemplatesProviderImpl(templateDirectory);
 		}
 		myInjector = Guice.createInjector(new GMFGeneratorModule(myExtensionTemplateProvider));
 	}
 
 	//-----------------------------------------------------------------------------------------
 	// names emitters 
 	@Override
 	public String getPreferenceInitializerName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::diagram::preferences::PreferenceInitializer", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getValidateActionName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ValidateAction", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getValidationMarkerName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ValidationMarker", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getShortcutCreationWizardName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ShortcutCreationWizard", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getModelElementSelectionPageName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::ModelElementSelectionPage", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getNewDiagramFileWizardName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::NewDiagramFileWizard", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getDeleteElementActionName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::DeleteElementAction", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public String getDiagramEditorContextMenuProviderName(Object... input) throws UnexpectedBehaviourException {
 		return getQualifiedClassName("xpt::editor::DiagramEditorContextMenuProvider", input); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramCanonicalEditPolicyEmitter() {
 		return getMainXtendEmitter("diagram::editpolicies::DiagramCanonicalEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getChildContainerCanonicalEditPolicyEmitter() {
 		return getMainXtendEmitter("diagram::editpolicies::ChildContainerCanonicalEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCreationWizardPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::CreationWizardPage"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getParserProviderEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("parsers::ParserProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPredefinedActionEmitter() {
 		return getMainXtendEmitter("impl::actions::PredefinedAction"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDomainNavigatorLabelProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::DomainNavigatorLabelProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDomainNavigatorItemEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::DomainNavigatorItem"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDomainNavigatorContentProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::DomainNavigatorContentProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorContentProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorContentProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getEditorEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::Editor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorLinkHelperEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorLinkHelper"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorLabelProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorLabelProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorActionProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorActionProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorItemEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorItem"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorGroupEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorGroup"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNavigatorSorterEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::NavigatorSorter"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getAbstractNavigatorItemEmitter() {
 		return getPrimaryXtendEmitter("xpt::navigator::AbstractNavigatorItem"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDocumentProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::DocumentProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramContentInitializerEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::DiagramContentInitializer"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramEditorContextMenuProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::DiagramEditorContextMenuProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getActionBarContributorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::ActionBarContributor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramEditorUtilEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::DiagramEditorUtil"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getElementChooserEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::ElementChooser"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getMatchingStrategyEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::MatchingStrategy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getModelElementSelectionPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::ModelElementSelectionPage"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNewDiagramFileWizardEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::NewDiagramFileWizard"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDeleteElementActionEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::DeleteElementAction"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCreationWizardEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::CreationWizard"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getShortcutCreationWizardEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::ShortcutCreationWizard"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getShortcutPropertyTesterEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::ShortcutPropertyTester"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getURIEditorInputTesterEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::UriEditorInputTester"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getValidateActionEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::ValidateAction"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getValidationMarkerEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::ValidationMarker"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getActionBarAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::ActionBarAdvisor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getApplicationEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::Application"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPerspectiveEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::Perspective"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getWorkbenchAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::WorkbenchAdvisor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getWorkbenchWindowAdvisorEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::WorkbenchWindowAdvisor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getWizardNewFileCreationPageEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::application::WizardNewFileCreationPage"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPreferenceInitializerEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::preferences::PreferenceInitializer"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCustomPreferencePageEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("impl::preferences::CustomPage"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getStandardPreferencePageEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("impl::preferences::StandardPage"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPropertySectionEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::propsheet::PropertySection"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPropertySheetLabelProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::propsheet::LabelProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getRegexpExpressionFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::expressions::RegexpExpressionFactory"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getAbstractExpressionEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::expressions::AbstractExpression"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getOCLExpressionFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::expressions::OCLExpressionFactory"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPredefinedParserEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("parsers::PredefinedParser"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getExpressionLabelParserEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("parsers::ExpressionLabelParser"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getAbstractParserEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("impl::parsers::AbstractParser"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCustomParserEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("parsers::CustomParser"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getEditPartFactoryEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::editparts::EditPartFactory"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getBaseEditHelperEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::edithelpers::BaseEditHelper"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getEditHelperAdviceEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::edithelpers::EditHelperAdvice"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getEditHelperEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::edithelpers::EditHelper"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getReorientLinkViewCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::commands::ReorientLinkViewCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCreateShortcutDecorationsCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::commands::CreateShortcutDecorationsCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getReorientRefLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::commands::ReorientRefLinkCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getReorientLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::commands::ReorientLinkCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCreateLinkCommandEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("xpt::diagram::commands::CreateLinkCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCreateNodeCommandEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::commands::CreateNodeCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCustomActionEmitter() {
 		return getMainXtendEmitter("impl::actions::CustomAction"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNodeItemSemanticEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::NodeItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getBaseItemSemanticEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::BaseItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getLinkItemSemanticEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::LinkItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCompartmentItemSemanticEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::CompartmentItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramItemSemanticEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::DiagramItemSemanticEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNodeEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::NodeEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getCompartmentEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::CompartmentEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getChildNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::ChildNodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::DiagramEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getExternalNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::ExternalNodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getLinkEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::LinkEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getLinkLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::LinkLabelEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNodeLabelEditPartEmitter() throws UnexpectedBehaviourException {
 		return getMainXtendEmitter("diagram::editparts::NodeLabelEditPart"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getVisualIDRegistryEmitter() {
 		return getPrimaryXtendEmitter("xpt::editor::VisualIDRegistry"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getGraphicalNodeEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::GraphicalNodeEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getOpenDiagramEditPolicyEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::OpenDiagram"); //$NON-NLS-1$
 	}
 
 	/**
 	 * FIXME: [MG] make separate xtend templates calling shared code, not vise versa
 	 */
 	@Override
 	public GeneratorTextEmitter getTextNonResizableEditPolicyEmitter() throws UnexpectedBehaviourException {
		return getXtendEmitter("xpt::diagram::editpolicies::TextFeedback", "TextNonResizableEditPolicy"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * FIXME: [MG] make separate xtend templates calling shared code, not vise versa
 	 */
 	@Override
 	public GeneratorTextEmitter getTextSelectionEditPolicyEmitter() throws UnexpectedBehaviourException {
		return getXtendEmitter("xpt::diagram::editpolicies::TextFeedback", "TextSelectionEditPolicy"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	@Override
 	public GeneratorTextEmitter getVisualEffectEditPolicyEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::editpolicies::VisualEffectEditPolicy"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getPaletteEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::editor::palette::PaletteFactory"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getElementTypesEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::ElementTypes"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getDiagramUpdaterEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::updater::DiagramUpdater"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getLinkDescriptorEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::updater::LinkDescriptor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getNodeDescriptorEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::updater::NodeDescriptor"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getUpdateCommandEmitter() {
 		return getPrimaryXtendEmitter("xpt::diagram::updater::UpdateCommand"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getEditPartProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::EditPartProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getShortcutsDecoratorProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::providers::ShortcutsDecoratorProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getValidationDecoratorProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::ValidationDecoratorProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getValidationProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::ValidationProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getViewProviderEmitter() {
 		return getPrimaryXtendEmitter("xpt::providers::ViewProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getElementInitializersEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::ElementInitializers"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getMarkerNavigationProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::MarkerNavigationProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getIconProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::IconProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getModelingAssistantProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::ModelingAssistantProvider"); //$NON-NLS-1$
 	}
 
 	@Override
 	public GeneratorTextEmitter getMetricProviderEmitter() throws UnexpectedBehaviourException {
 		return getPrimaryXtendEmitter("xpt::providers::MetricProvider"); //$NON-NLS-1$
 	}
 
 	private String getFullPath(GeneratorTextEmitter emitter, Object... input) throws UnexpectedBehaviourException {
 		//FIXME: [MG] at the time of merging this 
 		//we want to defer the fullPath() related changes that come to codegen.Generator
 		//so this method should not be called for now
 		//		if (emitter instanceof Xtend2Emitter) {
 		//			return getText(emitter, "fullPath", input);
 		//		} else {
 		//			return super.getFullPath(emitter, input);
 		//		}
 		throw new UnsupportedOperationException("getFullPath WILL allow to place generated file as MyFileGen and have MyFile customized");
 	}
 
 	private GeneratorTextEmitter getMainXtendEmitter(String templateFilePath) {
 		return getXtendEmitter(templateFilePath, "Main"); //$NON-NLS-1$
 	}
 
 	private GeneratorTextEmitter getPrimaryXtendEmitter(String templateFqn) {
 		String[] parts = templateFqn.split("::");
 		return getXtendEmitter(templateFqn, parts[parts.length - 1]);
 	}
 
 	private GeneratorTextEmitter getXtendEmitter(String templateFqn, String mainMethod) {
 		String classFqn = templateFqn.replace("::", ".");
 		Class<?> clazz = null;
 		try {
 			clazz = Class.forName(classFqn);
 		} catch (ClassNotFoundException e) {
 			if (myExtensionTemplateProvider != null) {
 				List<Class<?>> customClasses = myExtensionTemplateProvider.getCustomTemplateClasses();
 				for (Class<?> _class : customClasses) {
 					String name = _class.getName();
 					if (name.equals(classFqn)) {
 						clazz = _class;
 						break;
 					}
 				}
 			}
 			if (clazz == null) {
 				throw new IllegalStateException("Can't load: " + classFqn, e);
 			}
 		}
 		return new Xtend2Emitter(myInjector, clazz, mainMethod);
 	}
 
 	/**
 	 * @deprecated copy pasted, make protected in super-class
 	 */
 	@Deprecated
 	private String getQualifiedClassName(String templateName, Object... input) throws UnexpectedBehaviourException {
 		TextEmitter emitter = getQualifiedClassNameEmitter(templateName);
 		return getText(emitter, input);
 	}
 
 	/**
 	 * @deprecated make protected in super-class and override
 	 */
 	@Deprecated
 	private GeneratorTextEmitter getQualifiedClassNameEmitter(String templateName) throws UnexpectedBehaviourException {
 		return getXtendEmitter(templateName, "qualifiedClassName");
 	}
 
 	/**
 	 * @deprecated copy pasted, make protected in super-class
 	 */
 	@Deprecated
 	private String getText(TextEmitter emitter, Object... input) throws UnexpectedBehaviourException {
 		try {
 			return emitter.generate(new NullProgressMonitor(), input).trim();
 		} catch (InterruptedException ie) {
 			return null;
 		} catch (InvocationTargetException ite) {
 			throw new UnexpectedBehaviourException(ite.getCause());
 		}
 	}
 	
 	/**
 	 * @deprecated FIXME [MG] bad name, call disposeEmitters instead
 	 */
 	@Deprecated
 	public void hookEmitters() {
 		disposeEmitters();
 	}
 	
 
 	public void disposeEmitters() {
 		if (myExtensionTemplateProvider != null) {
 			myExtensionTemplateProvider.dispose();
 		}
 	}
 	
 	@Override
 	protected TextEmitter newXpandEmitter(String definition) {
 		String[] parts = definition.split(PATH_SEPARATOR);
 		String templateFQN = createXpandPath(parts);
 		return getXtendEmitter(templateFQN, parts[parts.length-1]);
 	}
 	
 	@Override
 	protected TextEmitter getQualifiedClassNameEmitterForPrimaryTemplate(String templateName) throws UnexpectedBehaviourException {
 		return getQualifiedClassNameEmitter(createXpandPath(templateName.split(PATH_SEPARATOR)));
 	}
 	
 	private String createXpandPath(String[] parts) {
 		StringBuilder builder = new StringBuilder(parts[0]);
 		for( int i = 1 ; i < parts.length-2 ; i++ ) {
 			builder.append(PATH_SEPARATOR);
 			builder.append(parts[i]);
 		}
 		return builder.toString();
 	}
 }
