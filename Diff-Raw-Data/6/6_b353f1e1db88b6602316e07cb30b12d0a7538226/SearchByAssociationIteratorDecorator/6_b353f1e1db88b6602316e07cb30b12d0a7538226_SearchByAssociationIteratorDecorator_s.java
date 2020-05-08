 package gov.nih.nci.evs.browser.utils;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
 import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
 import org.LexGrid.LexBIG.DataModel.Core.Association;
 import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.LexGrid.LexBIG.Exceptions.LBException;
 import org.LexGrid.LexBIG.Exceptions.LBResourceUnavailableException;
 import org.LexGrid.LexBIG.Impl.helpers.IteratorBackedResolvedConceptReferencesIterator;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeGraph;
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.LexGrid.LexBIG.Utility.Constructors;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import org.lexevs.paging.AbstractPageableIterator;
 
 import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
 import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
 
 import org.LexGrid.LexBIG.DataModel.Collections.SortOptionList;
 import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
 
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * The Class SearchByAssociationIteratorDecorator. Decorates a
  * ResolvedConceptReferencesIterator to provide paging support for Associated
  * Concept-type searches. As the iterator advances, subconcepts are queried from
  * the decorated iterator on demand, rather than all at once. This elminates the
  * need to resolve large CodedNodeGraphs.
  */
 public class SearchByAssociationIteratorDecorator extends
         IteratorBackedResolvedConceptReferencesIterator {
     /** The Constant serialVersionUID. */
     private static final long serialVersionUID = 4126716487618136771L;
 
     /** The lbs. */
     private static LexBIGService lbs = RemoteServerUtil.createLexBIGService();
 
     /** The quick iterator. */
     private static ResolvedConceptReferencesIterator _quickIterator;
 
     /** The resolve forward. */
     private static boolean _resolveForward;
 
     /** The resolve backward. */
     private static boolean _resolveBackward;
 
     /** The resolve association depth. */
     private static int _resolveAssociationDepth;
 
     /** The max to return. */
     private static int _maxToReturn;
 
 
     private static NameAndValueList _associationNameAndValueList;
     private static NameAndValueList _associationQualifierNameAndValueList;
 
 
     /**
      * Instantiates a new search by association iterator decorator.
      *
      * @param quickIterator the quick iterator
      * @param resolveForward the resolve forward
      * @param resolveBackward the resolve backward
      * @param resolveAssociationDepth the resolve association depth
      * @param maxToReturn the max to return
      * @throws LBResourceUnavailableException
      */
     public SearchByAssociationIteratorDecorator(
         ResolvedConceptReferencesIterator quickIterator) throws LBResourceUnavailableException {
 
     	super(doBuildIterator(quickIterator), quickIterator.numberRemaining());
         _quickIterator = quickIterator;
     }
 
 
     public void setQuickIterator(ResolvedConceptReferencesIterator quickIterator) {
 		_quickIterator = quickIterator;
 	}
 
     public void setResolveForward(boolean resolveForward) {
 		_resolveForward = resolveForward;
 	}
 
     public void setResolveBackward(boolean resolveBackward) {
 		_resolveBackward = resolveBackward;
 	}
 
     public void setResolveAssociationDepth(int resolveAssociationDepth) {
 		_resolveAssociationDepth = resolveAssociationDepth;
 	}
 
     public void setMaxToReturn(int maxToReturn) {
 		_maxToReturn = maxToReturn;
 	}
 
     public void setAssociationNameAndValueList(NameAndValueList associationNameAndValueList) {
 		_associationNameAndValueList = associationNameAndValueList;
 	}
 
     public void setAssociationQualifierNameAndValueList(NameAndValueList associationQualifierNameAndValueList) {
 		_associationQualifierNameAndValueList = associationQualifierNameAndValueList;
 	}
 
 	private static Iterator<ResolvedConceptReference> doBuildIterator(
 			ResolvedConceptReferencesIterator quickIterator) {
 		return new SearchAssociationResolvedConceptReference(quickIterator);
 	}
 
 	private static class SearchAssociationResolvedConceptReference extends
 		AbstractPageableIterator<ResolvedConceptReference> {
 
 		private static final long serialVersionUID = 2158463303566749525L;
 
 		private ResolvedConceptReferencesIterator quickIterator;
 
 		private SearchAssociationResolvedConceptReference(ResolvedConceptReferencesIterator quickIterator){
 			super();
 			this.quickIterator = quickIterator;
 		}
 
 		@Override
 		protected List<? extends ResolvedConceptReference> doPage(
 				int position,
 				int pageSize) {
 
 
 			List<ResolvedConceptReference> returnList = new ArrayList<ResolvedConceptReference>();
 			try {
 				while(quickIterator.hasNext() && returnList.size() < pageSize){
 					returnList.addAll(this.resolveOneHit(quickIterator.next()));
 				}
 			} catch (LBException e) {
 				throw new RuntimeException(e);
 			}
 			return returnList;
 		}
 
         // IMPORTANT: Apply restrictions to associations for supporting advanced search (to be implemented later)
 
 		protected List<? extends ResolvedConceptReference> resolveOneHit(ResolvedConceptReference hit) throws LBException{
 			List<ResolvedConceptReference> returnList = new ArrayList<ResolvedConceptReference>();
 
 			CodedNodeGraph cng = lbs.getNodeGraph(
 					hit.getCodingSchemeName(),
 					null,
 					null);
 
/*
             Boolean restrictToAnonymous = Boolean.FALSE;
             cng = cng.restrictToAnonymous(restrictToAnonymous);
*/
 
 			if (_associationNameAndValueList != null) {
 				cng =
 					cng.restrictToAssociations(
 						_associationNameAndValueList,
 						_associationQualifierNameAndValueList);
 			}
 
 			else {
 				String scheme = hit.getCodingSchemeName();
 				boolean isMapping = DataUtils.isMapping(scheme, null);
 				if (isMapping) {
 					NameAndValueList navl = DataUtils.getMappingAssociationNames(scheme, null);
 					if (navl != null) {
 						cng = cng.restrictToAssociations(navl, null);
 					}
 				}
 			}
 
 			ConceptReference focus = new ConceptReference();
 			focus.setCode(hit.getCode());
 			focus.setCodeNamespace(hit.getCodeNamespace());
 /*
             System.out.println("****** hit.getCodingSchemeName(): " + hit.getCodingSchemeName());
             System.out.println("****** hit.getCodingSchemeVersion(): " + hit.getCodingSchemeVersion());
 			System.out.println("****** hit.getCode(): " + hit.getCode());
 			System.out.println("****** hit.getCodeNamespace(): " + hit.getCodeNamespace());
 */
 			/*
 			ResolvedConceptReferenceList list =
 				cng.resolveAsList(focus, true, true, 0, 1, null, null, null, -1);
             */
 
 			LocalNameList propertyNames = new LocalNameList();
 			CodedNodeSet.PropertyType[] propertyTypes = null;
 			SortOptionList sortCriteria = null;
 
 			ResolvedConceptReferenceList list =
 				cng.resolveAsList(focus,
 					_resolveForward, _resolveBackward, 0,
 					_resolveAssociationDepth, propertyNames, propertyTypes, sortCriteria,
 					_maxToReturn);
 
 			for(ResolvedConceptReference ref : list.getResolvedConceptReference()){
 				if(ref.getSourceOf() != null){
 					returnList.addAll(this.getAssociations(ref.getSourceOf()));
 				}
 				if(ref.getTargetOf() != null){
 					returnList.addAll(this.getAssociations(ref.getTargetOf()));
 				}
 			}
 			return returnList;
 		}
 
 		protected List<? extends ResolvedConceptReference> getAssociations(AssociationList list){
 			List<ResolvedConceptReference> returnList = new ArrayList<ResolvedConceptReference>();
 
 			for(Association assoc : list.getAssociation()){
 				for(AssociatedConcept ac :
 					assoc.getAssociatedConcepts().getAssociatedConcept()){
 					returnList.add(ac);
 				}
 			}
 
 			return returnList;
 		}
 
 
 	}
 }
