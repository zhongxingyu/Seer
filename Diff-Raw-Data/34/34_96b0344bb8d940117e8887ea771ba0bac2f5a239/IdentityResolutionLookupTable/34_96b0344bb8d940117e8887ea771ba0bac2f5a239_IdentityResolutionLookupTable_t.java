 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.fqe.mpi.impl.resolution;
 
import static edu.utah.further.core.query.domain.SearchCriteria.simpleExpression;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.lang.invoke.MethodHandles;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.Validate;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.utah.further.core.api.collections.CollectionUtil;
import edu.utah.further.core.data.hibernate.adapter.CriteriaType;
import edu.utah.further.core.data.hibernate.query.QueryBuilderHibernateImpl;
import edu.utah.further.core.query.domain.Relation;
import edu.utah.further.core.query.domain.SearchCriteria;
import edu.utah.further.core.query.domain.SearchCriterion;
import edu.utah.further.core.query.domain.SearchQuery;
import edu.utah.further.core.query.domain.SearchType;
 import edu.utah.further.fqe.ds.api.domain.QueryContext;
 import edu.utah.further.fqe.mpi.api.Identifier;
 import edu.utah.further.fqe.mpi.api.IdentityResolutionStrategy;
 import edu.utah.further.fqe.mpi.api.service.IdentifierService;
 import edu.utah.further.fqe.mpi.impl.domain.IdentifierEntity;
 import edu.utah.further.fqe.mpi.impl.domain.LookupEntity;
 
 /**
  * An identity resolution strategy that resolves identities by looking up
  * <p>
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2012 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
  * @version Oct 22, 2013
  */
 @Service
 public class IdentityResolutionLookupTable implements IdentityResolutionStrategy
 {
 
 	// ========================= CONSTANTS =================================
 
 	/**
 	 * A logger that helps identify this class' printouts.
 	 */
 	private static final Logger log = getLogger(MethodHandles.lookup().lookupClass());
 
 	// ========================= DEPENDENCIES ==============================
 
 	/**
 	 * {@link SessionFactory} for the lookup table
 	 */
 	@Autowired
 	private SessionFactory lookupSessionFactory;
 
 	/**
 	 * Identifier related operations
 	 */
 	@Autowired
 	private IdentifierService identifierService;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.utah.further.fqe.mpi.api.IdentityResolutionStrategy#doIdentityResolution(edu
 	 * .utah.further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	@Transactional(value = "lookupTransactionManager")
 	public void doIdentityResolution(final QueryContext queryContext)
 	{
 		if (queryContext.getParent() == null)
 		{
 			log.warn("doIdentityResolution called with "
 					+ "Parent QueryContext. Only child QueryContext "
 					+ "are supported. Ignoring but this may indicate a bug or misuse.");
 			return;
 		}
 
 		final List<Identifier> identifiers = identifierService
 				.getUnresolvedIdentifiers(queryContext.getExecutionId());
 
 		// Maintain a map of sourceId to identifier so we can easily set the federated id
 		final Map<Long, IdentifierEntity> identifiersMap = new HashMap<>();
 		for (final Identifier identifier : identifiers)
 		{
 			identifiersMap.put(Long.valueOf(identifier.getSourceId()),
 					(IdentifierEntity) identifier);
 		}
 
 		final Long namespaceId = queryContext.getTargetNamespaceId();
 		Validate.notNull(namespaceId, "namespaceId required for identity resolution");
 
		final SearchCriterion rootAnd = SearchCriteria.junction(SearchType.CONJUNCTION);
		rootAnd.addCriterion(simpleExpression(Relation.EQ, "namespaceId", namespaceId));
		rootAnd.addCriterion(SearchCriteria.collection(SearchType.IN, "sourceId",
				identifiersMap.keySet().toArray()));
 
		final SearchQuery query = SearchCriteria.query(rootAnd, "LookupEntity");
		final List<LookupEntity> lookups = QueryBuilderHibernateImpl.convert(
				CriteriaType.CRITERIA, "edu.utah.further.fqe.mpi.impl.domain",
				lookupSessionFactory, query).list();
 
 		// Set the federated id for the identifier and save
 		for (final LookupEntity lookup : lookups)
 		{
 			final IdentifierEntity identifierEntity = identifiersMap.get(lookup
 					.getSourceId());
 			identifierEntity.setCommonId(lookup.getCommonId());
 		}

		identifierService.updateSavedIdentifiers(CollectionUtil.newList(identifiersMap
				.values()));
 	}
 
 	/**
 	 * Return the lookupSessionFactory property.
 	 * 
 	 * @return the lookupSessionFactory
 	 */
 	public SessionFactory getLookupSessionFactory()
 	{
 		return lookupSessionFactory;
 	}
 
 	/**
 	 * Set a new value for the lookupSessionFactory property.
 	 * 
 	 * @param lookupSessionFactory
 	 *            the lookupSessionFactory to set
 	 */
 	public void setLookupSessionFactory(final SessionFactory lookupSessionFactory)
 	{
 		this.lookupSessionFactory = lookupSessionFactory;
 	}
 
 	/**
 	 * Return the identifierService property.
 	 * 
 	 * @return the identifierService
 	 */
 	public IdentifierService getIdentifierService()
 	{
 		return identifierService;
 	}
 
 	/**
 	 * Set a new value for the identifierService property.
 	 * 
 	 * @param identifierService
 	 *            the identifierService to set
 	 */
 	public void setIdentifierService(final IdentifierService identifierService)
 	{
 		this.identifierService = identifierService;
 	}
 
 }
