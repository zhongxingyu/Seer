 /**
  * Copyright (c) 2010, 2012 Darmstadt University of Technology.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Tobias Boehm - initial API and implementation.
  */
 
 package org.eclipse.recommenders.internal.codesearch.rcp.views;
 
 import java.io.IOException;
 import java.util.Set;
 
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.SearchResult;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.converter.DotNotationMethodConverter;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.converter.DotNotationTypeConverter;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.converter.PathValueConverter;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.JavaMethodProvider;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.JavaTypeProvider;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.ProjectNameProvider;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.ResourcePathProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.LuceneSearchTermExtractor;
 import org.eclipse.recommenders.codesearch.rcp.searcher.imageProvider.MethodImageProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.imageProvider.ProjectImageProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.imageProvider.TypeImageProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.proposalProvider.DefinitionProposalProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.proposalProvider.DocumentTypeProposalProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.proposalProvider.GenericQueryProposalProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.proposalProvider.ModifierQueryProposalProvider;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
 import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.LuceneQueryStandaloneSetup;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.extractors.LuceneQueryExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.extractors.ParseResultExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.ui.contentassist.LuceneQueryProposalProvider;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.ui.contentassist.QueryProposalType;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.ui.internal.LuceneQueryActivator;
 
 import com.google.inject.Injector;
 
 @SuppressWarnings("restriction")
 public class LuceneQueryEditorWrapper extends AbstractEmbeddedEditorWrapper {
 
     @Override
     void createQueryEditorInternal() {
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.TYPE, new GenericQueryProposalProvider(
                 new JavaTypeProvider(), new DotNotationTypeConverter(), new TypeImageProvider()));
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.METHOD,
                 new GenericQueryProposalProvider(new JavaMethodProvider(), new DotNotationMethodConverter(),
                         new MethodImageProvider()));
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.FILE_PATH,
                 new GenericQueryProposalProvider(new ResourcePathProvider(), new PathValueConverter()));
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.PROJECT_NAME,
                 new GenericQueryProposalProvider(new ProjectNameProvider(), null, new ProjectImageProvider()));
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.MODIFIER,
                 new ModifierQueryProposalProvider());
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.DOCUMENT_TYPE,
                 new DocumentTypeProposalProvider());
 
         LuceneQueryProposalProvider.addQueryProposalProvider(QueryProposalType.DEFINITION,
                 new DefinitionProposalProvider());
 
         final IEditedResourceProvider resourceProvider = new IEditedResourceProvider() {
 
             @Override
             public XtextResource createResource() {
                 try {
                     LuceneQueryStandaloneSetup.doSetup();
                     final ResourceSet resourceSet = new ResourceSetImpl();
                     final Resource resource = resourceSet.createResource(URI.createURI("embedded.lucenequery"));
 
                     return (XtextResource) resource;
                 } catch (final Exception e) {
                     return null; // What to return, how to create a resource?
                 }
             }
         };
 
         final LuceneQueryActivator activator = LuceneQueryActivator.getInstance();
         final Injector injector = activator
                 .getInjector(LuceneQueryActivator.ORG_ECLIPSELABS_RECOMMENDERS_CODESEARCH_RCP_DSL_LUCENEQUERY);
         final EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
         handle = factory.newEditor(resourceProvider).withParent(parent);
 
         // keep the partialEditor as instance var to read / write the edited
         // text
         partialEditor = handle.createPartialEditor(true);
         // handle.getViewer().addTextInputListener(new ITextInputListener() {
         //
         // @Override
         // public void inputDocumentChanged(final IDocument oldInput, final
         // IDocument newInput) {
         // }
         //
         // @Override
         // public void inputDocumentAboutToBeChanged(final IDocument oldInput,
         // final IDocument newInput) {
         // // no constraints...
         // }
         // });
 
         searchView.setSearchEnabled(true);
     }
 
     @Override
     SearchResult search() throws ParseException, CorruptIndexException, IOException {
         IParseResult r = handle.getDocument().readOnly(new ParseResultExtractor());
 
         if(r.hasSyntaxErrors())
         	return null;
         
        String searchQuery = handle.getDocument().readOnly(new LuceneQueryExtractor()).trim();
        
        //XXX: Quick workaround to prevent most of the "empty" result rows until I have time to fix it
        searchQuery = String.format("(%s) AND %s:*", searchQuery, Fields.QUALIFIED_NAME);
        
         resetXtextQuery();
         System.out.println("Search: " + searchQuery);
         final SearchResult searchResult = codeSearcher.lenientSearch(searchQuery);
         return searchResult;
     }
 
     @Override
     String[] getExampleQueriesInternal() {
         return new String[] {
         		"FullyQualifiedName:org.eclipse.ui.*",
                 "UsedTypes:java.util.List",
                 "ExtendedTypes:org.eclipse* AND Modifiers:public AND (UsedTypes:*ASTVisitor OR UsedTypes:*Plugin)",
                 "UsedTypes:java.util.List AND Type:method",
                 "UsedTypes:*Document Type:method Modifiers:private FriendlyName:addDoc FieldsRead:*Store.YES",
                 "/* Finde Klasse, die von AbstractButton erbt, exec.. überschreibt und nicht doOk aufruft */\n"
                         + "Type:class AND AllExtendedTypes:*AbstractButton AND OverriddenMethods:*exec AND NOT UsedMethods:*doOk" };
     }
 
     public static String getName() {
         return "Lucene Query Language";
     }
 
     @Override
     IUnitOfWork<Set<String>, XtextResource> getSearchTermExtractor() {
         return new LuceneSearchTermExtractor();
     }
 }
