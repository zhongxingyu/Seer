 /*
  *	Copyright Technophobia Ltd 2012
  *
  *   This file is part of Substeps.
  *
  *    Substeps is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    Substeps is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public License
  *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.technophobia.substeps.editor;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.ui.IWorkbenchSite;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.texteditor.TextOperationAction;
 
 import com.technophobia.eclipse.transformer.SiteToJavaProjectTransformer;
 import com.technophobia.substeps.FeatureEditorPlugin;
 import com.technophobia.substeps.colour.ColourManager;
 import com.technophobia.substeps.document.content.ContentTypeDefinitionFactory;
 import com.technophobia.substeps.document.content.assist.AutoActivatingContentAssistantDecorator;
 import com.technophobia.substeps.document.content.assist.ContentAssistantFactory;
 import com.technophobia.substeps.document.content.assist.ProcessedContentAssistantFactory;
 import com.technophobia.substeps.document.content.assist.feature.StepImplementationProcessorSupplier;
 import com.technophobia.substeps.document.content.feature.FeatureContentTypeDefinition;
 import com.technophobia.substeps.document.content.feature.FeatureContentTypeDefinitionFactory;
 import com.technophobia.substeps.document.content.partition.ContentTypeRuleBasedPartitionScannerFactory;
 import com.technophobia.substeps.document.content.view.ContentTypeViewConfiguration;
 import com.technophobia.substeps.document.formatting.FormattingContextFactory;
 import com.technophobia.substeps.document.formatting.partition.PartitionedFormattingContextFactory;
 import com.technophobia.substeps.document.partition.PartitionScannedDocumentProvider;
 import com.technophobia.substeps.runner.runtime.ClassLocator;
 import com.technophobia.substeps.runner.runtime.StepClassLocator;
import com.technophobia.substeps.supplier.Callback1;
 import com.technophobia.substeps.supplier.Supplier;
 
 public class FeatureEditor extends TextEditor {
 
     private final ColourManager colourManager;
 
 
    @SuppressWarnings("unchecked")
     public FeatureEditor() {
 
         final ContentTypeDefinitionFactory contentTypeDefinitionFactory = new FeatureContentTypeDefinitionFactory();
         final FormattingContextFactory formattingContextFactory = new PartitionedFormattingContextFactory(
                 contentTypeDefinitionFactory);
         final ContentAssistantFactory contentAssistantFactory = new ProcessedContentAssistantFactory(
                 FeatureContentTypeDefinition.FEATURE.name(), processorSupplier(),
                (Callback1<IContentAssistant>) new AutoActivatingContentAssistantDecorator());
         colourManager = new ColourManager();
 
         setSourceViewerConfiguration(new ContentTypeViewConfiguration(colourManager, contentTypeDefinitionFactory,
                 formattingContextFactory, contentAssistantFactory));
         setDocumentProvider(new PartitionScannedDocumentProvider(new ContentTypeRuleBasedPartitionScannerFactory(
                 contentTypeDefinitionFactory)));
     }
 
 
     private Supplier<IContentAssistProcessor> processorSupplier() {
         return new Supplier<IContentAssistProcessor>() {
             @Override
             public IContentAssistProcessor get() {
                 final IWorkbenchSite site = getSite();
                 final String outputFolder = outputFolderForSite(site);
                 final ClassLocator classLocator = new StepClassLocator(outputFolder);
                 return new StepImplementationProcessorSupplier(outputFolder, classLocator).get();
             }
 
         };
     }
 
 
     private String outputFolderForSite(final IWorkbenchSite site) {
         try {
             final IJavaProject project = new SiteToJavaProjectTransformer().to(site);
             if (project != null) {
                 return project.getOutputLocation().toOSString();
             }
         } catch (final JavaModelException e) {
             FeatureEditorPlugin.log(Status.ERROR, "Could not get output folder for site " + site);
         }
         return null;
     }
 
 
     @Override
     public void dispose() {
         colourManager.dispose();
         super.dispose();
     }
 
 
     @Override
     protected void createActions() {
         super.createActions();
 
         final ResourceBundle resourceBundle = FeatureEditorPlugin.instance().getResourceBundle();
         final TextOperationAction action = new TextOperationAction(resourceBundle, "ContentFormatProposal.", this,
                 ISourceViewer.FORMAT);
         setAction("ContentFormatProposal", action);
         getEditorSite().getActionBars().setGlobalActionHandler("ContentFormatProposal", action);
     }
 }
