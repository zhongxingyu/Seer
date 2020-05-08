 /*******************************************************************************
  * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and 
  * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
  * triple-shield Mayo logo are trademarks and service marks of MFMER.
  * 
  * Except as contained in the copyright notice above, or as used to identify 
  * MFMER as the author of this software, the trade names, trademarks, service
  * marks, or product names of the copyright holder shall not be used in
  * advertising, promotion or otherwise in connection with this software without
  * prior written authorization of the copyright holder.
  *   
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *   
  *  		http://www.eclipse.org/legal/epl-v10.html
  * 
  *  		
  *******************************************************************************/
 package org.LexGrid.LexBIG.cagrid.adapters;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
 import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
 import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
 import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Core.Association;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
 import org.LexGrid.LexBIG.DataModel.cagrid.AssociationIdentification;
 import org.LexGrid.LexBIG.DataModel.cagrid.CodeState;
 import org.LexGrid.LexBIG.DataModel.cagrid.CodingSchemeCopyRight;
 import org.LexGrid.LexBIG.DataModel.cagrid.Direction;
 import org.LexGrid.LexBIG.DataModel.cagrid.DirectionalAssociationIdentification;
 import org.LexGrid.LexBIG.DataModel.cagrid.HierarchyResolutionPolicy;
 import org.LexGrid.LexBIG.Exceptions.LBException;
 import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.LexGrid.LexBIG.cagrid.Utils;
 import org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.InvalidServiceContextAccess;
 import org.LexGrid.naming.SupportedHierarchy;
 
 public class LexBIGServiceConvenienceMethodsAdapter implements LexBIGServiceConvenienceMethods {
 
 private LexBIGServiceConvenienceMethodsGridAdapter lbscm;
 	
 	public LexBIGServiceConvenienceMethodsAdapter(LexBIGServiceConvenienceMethodsGridAdapter adapter) throws RemoteException {
 		lbscm = adapter;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getRenderingDetail(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering getRenderingDetail(
 			String codingScheme, CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				return lbscm.getRenderingDetail(Utils.wrapCodingSchemeIdentifier(codingScheme), versionOrTag);
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getCodingSchemesWithSupportedAssociation(java.lang.String)
 	 */
 	public CodingSchemeRenderingList getCodingSchemesWithSupportedAssociation(
 			String associationName) throws LBException {
 		try{
 			return lbscm.getCodingSchemesWithSupportedAssociation(
 					Utils.wrapAssociationIdentification(associationName));
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyIDs(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String[] getHierarchyIDs(String codingScheme,
 			CodingSchemeVersionOrTag csvt) throws LBException {
 		try{
 			return Utils.hierarchyIdentificationToStringArray(
 					lbscm.getHierarchyIDs(Utils.wrapCodingSchemeIdentifier(codingScheme), csvt));
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getDescription()
 	 */
 	public String getDescription() {
 		throw new RuntimeException("Not a valid Grid Service Call");
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getName()
 	 */
 	public String getName() {
 		throw new RuntimeException("Not a valid Grid Service Call");
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getProvider()
 	 */
 	public String getProvider() {
 		throw new RuntimeException("Not a valid Grid Service Call");
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getVersion()
 	 */
 	public String getVersion() {
 		throw new RuntimeException("Not a valid Grid Service Call");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#createCodeNodeSet(java.lang.String[],
 	 *      java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public CodedNodeSet createCodeNodeSet(String[] conceptCodes, String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 		try {
 			CodedNodeSetGridAdapter cnsa = (CodedNodeSetGridAdapter)lbscm.createCodeNodeSet(
 					Utils.stringArrayToConceptIdentification(conceptCodes), 
 					Utils.wrapCodingSchemeIdentifier(codingScheme), versionOrTag);
 			return cnsa.getCodedNodeSetInterface();
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (Exception e) {
 			throw new LBException(e.getMessage(), e);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getAssociationForwardAndReverseNames(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String[] getAssociationForwardAndReverseNames(String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				return Utils.associationIdentificationToStringArray(
 						lbscm.getAssociationForwardAndReverseNames(
 						Utils.wrapCodingSchemeIdentifier(codingScheme), versionOrTag));
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getAssociationForwardName(java.lang.String,
 	 *      java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String getAssociationForwardName(String association, String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				AssociationIdentification id = lbscm.getAssociationForwardName(Utils.wrapAssociationIdentification(association), 
 						Utils.wrapCodingSchemeIdentifier(codingScheme), versionOrTag);
 				return id.getRelationshipName();
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getAssociationForwardNames(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String[] getAssociationForwardNames(String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {		
 		try {
 			return Utils.directionalAssociationIdentificationToStringArray(
 						lbscm.getAssociationForwardNames(
 						Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag));
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getAssociationReverseName(java.lang.String,
 	 *      java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String getAssociationReverseName(String association, String codingScheme, 
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 		try {
 			DirectionalAssociationIdentification assoc = lbscm.getAssociationReverseName(Utils.wrapAssociationIdentification(association), 
 						Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag);
 			return assoc.getRelationshipName();
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getAssociationReverseNames(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String[] getAssociationReverseNames(String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				return Utils.directionalAssociationIdentificationToStringArray(
 						lbscm.getAssociationReverseNames(Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag));
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}	
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyLevelNext(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String, java.lang.String, boolean,
 	 *      org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList)
 	 */
 	public AssociationList getHierarchyLevelNext(String codingScheme, CodingSchemeVersionOrTag versionOrTag,
 			String hierarchyID, String conceptCode, boolean resolveConcepts, 
 			NameAndValueList associationQualifiers) throws LBException {
 		
 			HierarchyResolutionPolicy policy = Utils.buildHierarchyResolutionPolicy(hierarchyID, 
 					conceptCode, resolveConcepts, associationQualifiers);
 		try {
 			return lbscm.getHierarchyLevelNext(
 					policy, Utils.wrapCodingSchemeIdentifier(codingScheme), 
 					versionOrTag);
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyLevelPrev(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String, java.lang.String, boolean,
 	 *      org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList)
 	 */
 	public AssociationList getHierarchyLevelPrev(String codingScheme, CodingSchemeVersionOrTag versionOrTag,
 			String hierarchyID, String conceptCode, boolean resolveConcepts, 
 			NameAndValueList associationQualifiers) throws LBException {
 		HierarchyResolutionPolicy policy = Utils.buildHierarchyResolutionPolicy(hierarchyID, 
 				conceptCode, resolveConcepts, associationQualifiers);
 		try {
 			return lbscm.getHierarchyLevelPrev(
 				policy, Utils.wrapCodingSchemeIdentifier(codingScheme), 
 				versionOrTag);
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyPathToRoot(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String, java.lang.String, boolean,
 	 *      org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods.HierarchyPathResolveOption,
 	 *      org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList)
 	 */
 	public AssociationList getHierarchyPathToRoot(String codingScheme, CodingSchemeVersionOrTag versionOrTag,
 			String hierarchyID, String conceptCode,
 			boolean resolveConcepts, HierarchyPathResolveOption pathResolveOption, 
 			NameAndValueList associationQualifiers)
 			throws LBException {
 		HierarchyResolutionPolicy policy = Utils.buildHierarchyResolutionPolicy(hierarchyID, 
 				conceptCode, resolveConcepts, associationQualifiers);
 		try {
 			return lbscm.getHierarchyPathToRoot(
 				policy, Utils.wrapCodingSchemeIdentifier(codingScheme), 
 				versionOrTag,
 				Utils.convertHierarchyPathResolveOption(pathResolveOption));
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyRoots(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String)
 	 */
 	public ResolvedConceptReferenceList getHierarchyRoots(String codingScheme, CodingSchemeVersionOrTag versionOrTag,
 			String hierarchyID) throws LBException {
 			try {
 				return lbscm.getHierarchyRoots(
 						Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag, Utils.wrapHierarchyIdentificationIdentification(hierarchyID));
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getHierarchyRootSet(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String)
 	 */
 	public CodedNodeSet getHierarchyRootSet(String codingScheme, CodingSchemeVersionOrTag versionOrTag,
 			String hierarchyID) throws LBException {
 			try {
 				CodedNodeSetGridAdapter cnsga = (CodedNodeSetGridAdapter)lbscm.getHierarchyRootSet(
 						Utils.wrapCodingSchemeIdentifier(codingScheme),
 						versionOrTag, Utils.wrapHierarchyIdentificationIdentification(hierarchyID));
 				return cnsga.getCodedNodeSetInterface();
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (Exception e) {
 				throw new LBException(e.getMessage(), e);
 			}
 		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#isForwardName(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String)
 	 */
 	public boolean isForwardName(String codingScheme, CodingSchemeVersionOrTag versionOrTag, String directionalName) throws LBException {
 			try {
 				Direction direction = lbscm.isForwardName(
 						Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag, Utils.wrapAssociationIdentification(directionalName));
 				return direction.getIsForward();
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#isReverseName(java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag,
 	 *      java.lang.String)
 	 */
 	public boolean isReverseName(String codingScheme, CodingSchemeVersionOrTag versionOrTag, String directionalName) throws LBException {
 		try {
 			Direction direction = lbscm.isForwardName(
 					Utils.wrapCodingSchemeIdentifier(codingScheme), 
 					versionOrTag, Utils.wrapAssociationIdentification(directionalName));
 			
 			//TODO: check into this -- must reverse this
 			return !direction.getIsForward();
 		} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (InvalidServiceContextAccess e) {
 			throw new LBException(e.getMessage(), e);
 		} catch (RemoteException e) {
 			throw new LBException(e.getMessage(), e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#isCodeRetired(java.lang.String,
 	 *      java.lang.String,
 	 *      org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public boolean isCodeRetired(String conceptCode, String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				CodeState state = lbscm.isCodeRetired(
 						Utils.wrapConceptIdentification(conceptCode), 
 						Utils.wrapCodingSchemeIdentifier(codingScheme), versionOrTag);
 				return state.getIsActive();
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getCodingSchemeCopyright(java.lang.String, org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag)
 	 */
 	public String getCodingSchemeCopyright(String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag) throws LBException {
 			try {
 				CodingSchemeCopyRight copyright = lbscm.getCodingSchemeCopyright(
 						Utils.wrapCodingSchemeIdentifier(codingScheme), 
 						versionOrTag);
 				return copyright.getCopyrightTextOrURL();
 			} catch (org.LexGrid.LexBIG.cagrid.LexEVSGridService.stubs.types.LBException e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (InvalidServiceContextAccess e) {
 				throw new LBException(e.getMessage(), e);
 			} catch (RemoteException e) {
 				throw new LBException(e.getMessage(), e);
 			}
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getLexBIGService()
 	 */
 	public LexBIGService getLexBIGService() {
 		//throw new RuntimeException("This is not a valid Grid Service Call");
 		return null;
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#setLexBIGService(org.LexGrid.LexBIG.LexBIGService.LexBIGService)
 	 */
 	public void setLexBIGService(LexBIGService arg0) {
 		//throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	/* 
 	 * Not implemented as a LexEVS Grid Service.
 	 * 
 	 * (non-Javadoc)
 	 * @see org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods#getEntityDescription(java.lang.String, org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag, java.lang.String)
 	 */
 	public String getEntityDescription(String codingScheme,
 			CodingSchemeVersionOrTag versionOrTag, String code) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public Association getAssociationForwardOneLevel(String arg0, String arg1,
 			String arg2, String arg3, CodingSchemeVersionOrTag arg4,
 			boolean arg5, NameAndValueList arg6) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public Association getAssociationReverseOneLevel(String arg0, String arg1,
 			String arg2, String arg3, CodingSchemeVersionOrTag arg4,
 			boolean arg5, NameAndValueList arg6) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public void addEntityLuceneIndexes(String arg0,
 			CodingSchemeVersionOrTag arg1, List<String> arg2)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public AssociationList getHierarchyLevelNext(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2, String arg3,
 			boolean arg4, boolean arg5, NameAndValueList arg6)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public int getHierarchyLevelNextCount(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2, ConceptReference arg3)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public ConceptReferenceList getHierarchyLevelNextCount(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2,
 			ConceptReferenceList arg3) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public AssociationList getHierarchyLevelPrev(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2, String arg3,
 			boolean arg4, boolean arg5, NameAndValueList arg6)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public int getHierarchyLevelPrevCount(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2, ConceptReference arg3)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public ConceptReferenceList getHierarchyLevelPrevCount(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2,
 			ConceptReferenceList arg3) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public SupportedHierarchy[] getSupportedHierarchies(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public void modifyEntityLuceneIndexes(String arg0,
 			CodingSchemeVersionOrTag arg1, List<String> arg2)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public void removeEntityLuceneIndexes(String arg0,
 			CodingSchemeVersionOrTag arg1, List<String> arg2)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public String getAssociationCodeFromAssociationName(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public String getAssociationNameFromAssociationCode(String arg0,
 			CodingSchemeVersionOrTag arg1, String arg2) throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 
 	public ResolvedConceptReferenceList getHierarchyOrphanedConcepts(
 			String arg0, CodingSchemeVersionOrTag arg1, String arg2)
 			throws LBException {
 		throw new RuntimeException("This is not a valid Grid Service Call");
 	}
 }
