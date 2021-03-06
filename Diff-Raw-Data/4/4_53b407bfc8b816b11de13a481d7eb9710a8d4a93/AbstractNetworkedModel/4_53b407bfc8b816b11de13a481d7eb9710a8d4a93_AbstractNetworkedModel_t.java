 /**
  * Copyright (C) 2012 Hal Hildebrand. All rights reserved.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as 
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.hellblazer.CoRE.meta.models;
 
 import static com.hellblazer.CoRE.ExistentialRuleform.DEDUCE_NEW_NETWORK_RULES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_CLASSIFIED_ATTRIBUTE_VALUES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_GROUPED_ATTRIBUTE_ATHORIZATIONS_FOR_ATTRIBUTE_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_GROUPED_ATTRIBUTE_ATHORIZATIONS_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.FIND_GROUPED_ATTRIBUTE_VALUES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.GATHER_EXISTING_NETWORK_RULES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.GENERATE_NETWORK_INVERSES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.GET_CHILDREN_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.INFERENCE_STEP_FROM_LAST_PASS_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.INFERENCE_STEP_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.INSERT_NEW_NETWORK_RULES_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.UNLINKED_SUFFIX;
 import static com.hellblazer.CoRE.ExistentialRuleform.USED_RELATIONSHIPS_SUFFIX;
 
 import java.lang.reflect.ParameterizedType;
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import javax.persistence.criteria.Selection;
 
 import org.postgresql.pljava.Session;
 import org.postgresql.pljava.SessionManager;
 import org.postgresql.pljava.TransactionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hellblazer.CoRE.ExistentialRuleform;
 import com.hellblazer.CoRE.Ruleform;
 import com.hellblazer.CoRE.agency.Agency;
 import com.hellblazer.CoRE.attribute.Attribute;
 import com.hellblazer.CoRE.attribute.AttributeValue;
 import com.hellblazer.CoRE.attribute.ClassifiedAttributeAuthorization;
 import com.hellblazer.CoRE.authorization.AccessAuthorization;
 import com.hellblazer.CoRE.kernel.Kernel;
 import com.hellblazer.CoRE.meta.NetworkedModel;
 import com.hellblazer.CoRE.network.Aspect;
 import com.hellblazer.CoRE.network.Facet;
 import com.hellblazer.CoRE.network.NetworkRuleform;
 import com.hellblazer.CoRE.network.Relationship;
 
 /**
  * @author hhildebrand
  * 
  */
 abstract public class AbstractNetworkedModel<RuleForm extends ExistentialRuleform<RuleForm, Network>, Network extends NetworkRuleform<RuleForm>, AttributeAuthorization extends ClassifiedAttributeAuthorization<RuleForm>, AttributeType extends AttributeValue<?>>
         implements
         NetworkedModel<RuleForm, Network, AttributeAuthorization, AttributeType> {
 
     private static Logger log = LoggerFactory.getLogger(AbstractNetworkedModel.class);
 
     /**
      * @param attr
      */
     public static void defaultValue(AttributeValue<?> attr) {
         switch (attr.getAttribute().getValueType()) {
             case BINARY: {
                 attr.setBinaryValue(new byte[0]);
                 break;
             }
             case BOOLEAN: {
                 attr.setBooleanValue(false);
                 break;
             }
             case INTEGER: {
                 attr.setIntegerValue(-1);
                 break;
             }
             case NUMERIC: {
                 attr.setNumericValue(new BigDecimal(-1));
                 break;
             }
             case TEXT: {
                 attr.setTextValue("");
                 break;
             }
             case TIMESTAMP: {
                 attr.setTimestampValue(new Timestamp(0));
                 break;
             }
         }
     }
 
     public static boolean markPropagated(final String inPropagateKey)
                                                                      throws SQLException {
         final Session session = SessionManager.current();
         Boolean propagated = (Boolean) session.getAttribute(inPropagateKey);
         if (propagated != null) {
             return false;
         }
         session.setAttribute(inPropagateKey, Boolean.TRUE);
         session.addTransactionListener(new TransactionListener() {
 
             @Override
             public void onAbort(Session session) throws SQLException {
                 session.removeAttribute(inPropagateKey);
             }
 
             @Override
             public void onCommit(Session session) throws SQLException {
                 session.removeAttribute(inPropagateKey);
             }
 
             @Override
             public void onPrepare(Session session) throws SQLException {
                 // nothing to do
             }
         });
         return true;
     }
 
     /**
      * @param entity
      * @return
      */
     private static String tableName(Class<?> product) {
         StringBuilder builder = new StringBuilder();
         boolean first = true;
         for (char c : product.getSimpleName().toCharArray()) {
             if (Character.isUpperCase(c)) {
                 if (!first) {
                     builder.append('_');
                 } else {
                     first = false;
                 }
                 builder.append(Character.toLowerCase(c));
             } else {
                 builder.append(c);
             }
         }
         return builder.toString();
     }
 
     private final Class<AttributeType>             attribute;
     private final Class<AttributeAuthorization>    authorization;
     private final Class<RuleForm>                  entity;
     private final Class<NetworkRuleform<RuleForm>> network;
     private final String                           networkPrefix;
     private final String                           networkTable;
     private final String                           prefix;
     private final String                           unqualifiedNetworkTable;
     protected final EntityManager                  em;
 
     protected final Kernel                         kernel;
 
     @SuppressWarnings("unchecked")
     public AbstractNetworkedModel(EntityManager em, Kernel kernel) {
         this.em = em;
         this.kernel = kernel;
         entity = extractedEntity();
         authorization = extractedAuthorization();
         attribute = extractedAttribute();
         network = (Class<NetworkRuleform<RuleForm>>) extractedNetwork();
         prefix = ModelImpl.prefixFor(entity);
         networkPrefix = ModelImpl.prefixFor(network);
         unqualifiedNetworkTable = tableName(network);
         networkTable = String.format("ruleform.%s", unqualifiedNetworkTable);
     }
 
     public void createInverseRelationship(RuleForm parent, Relationship r,
                                           RuleForm child, Agency updatedBy) {
         child.link(r.getInverse(), parent, updatedBy,
                    kernel.getInverseSoftware(), em);
     }
 
     public void generateInverses() {
         Query query = em.createNamedQuery(String.format("%s%s", networkPrefix,
                                                         GENERATE_NETWORK_INVERSES_SUFFIX));
         query.setParameter(1, kernel.getInverseSoftware().getId());
         long then = System.currentTimeMillis();
         int created = query.executeUpdate();
         if (log.isInfoEnabled()) {
             log.info(String.format("created %s inverse rules of %s in %s ms",
                                    created, networkPrefix,
                                    System.currentTimeMillis() - then));
         }
     }
 
     @Override
     public <ValueType> List<ValueType> getAllowedValues(Attribute attribute,
                                                         Agency groupingAgency) {
         return getAllowedValues(attribute,
                                 getAttributeAuthorizations(groupingAgency,
                                                            attribute));
     }
 
     @Override
     public <ValueType> List<ValueType> getAllowedValues(Attribute attribute,
                                                         Aspect<RuleForm> aspect) {
         return getAllowedValues(attribute,
                                 getAttributeAuthorizations(aspect, attribute));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getAttributeAuthorizations(com
      * .hellblazer.CoRE.agency.Agency)
      */
     @Override
     public List<AttributeAuthorization> getAttributeAuthorizations(Agency groupingAgency) {
         TypedQuery<AttributeAuthorization> query = em.createNamedQuery(prefix
                                                                                + FIND_GROUPED_ATTRIBUTE_ATHORIZATIONS_SUFFIX,
                                                                        authorization);
         query.setParameter("groupingAgency", groupingAgency);
         return query.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getAttributeAuthorizations(com
      * .hellblazer.CoRE.agency.Agency, com.hellblazer.CoRE.attribute.Attribute)
      */
     @Override
     public List<AttributeAuthorization> getAttributeAuthorizations(Agency groupingAgency,
                                                                    Attribute attribute) {
         TypedQuery<AttributeAuthorization> query = em.createNamedQuery(prefix
                                                                                + FIND_GROUPED_ATTRIBUTE_ATHORIZATIONS_FOR_ATTRIBUTE_SUFFIX,
                                                                        authorization);
         query.setParameter("groupingAgency", groupingAgency);
         query.setParameter("attribute", attribute);
         return query.getResultList();
     }
 
     @Override
     public List<AttributeAuthorization> getAttributeAuthorizations(Aspect<RuleForm> aspect) {
         TypedQuery<AttributeAuthorization> query = em.createNamedQuery(prefix
                                                                                + FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_SUFFIX,
                                                                        authorization);
         query.setParameter("classification", aspect.getClassification());
         query.setParameter("classifier", aspect.getClassifier());
         return query.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getAttributeAuthorizations(com
      * .hellblazer.CoRE.meta.Aspect, com.hellblazer.CoRE.attribute.Attribute)
      */
     @Override
     public List<AttributeAuthorization> getAttributeAuthorizations(Aspect<RuleForm> aspect,
                                                                    Attribute attribute) {
         TypedQuery<AttributeAuthorization> query = em.createNamedQuery(prefix
                                                                                + FIND_CLASSIFIED_ATTRIBUTE_AUTHORIZATIONS_FOR_ATTRIBUTE_SUFFIX,
                                                                        authorization);
         query.setParameter("classification", aspect.getClassification());
         query.setParameter("classifier", aspect.getClassifier());
         query.setParameter("attribute", attribute);
         return query.getResultList();
     }
 
     @Override
     public List<AttributeType> getAttributesClassifiedBy(RuleForm ruleform,
                                                          Agency groupingAgency) {
         TypedQuery<AttributeType> query = em.createNamedQuery(prefix
                                                                       + FIND_GROUPED_ATTRIBUTE_VALUES_SUFFIX,
                                                               attribute);
         query.setParameter("ruleform", ruleform);
         query.setParameter("agency", groupingAgency);
         return query.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getAttributesClassifiedBy(com
      * .hellblazer.CoRE.ExistentialRuleform, com.hellblazer.CoRE.meta.Aspect)
      */
     @Override
     public List<AttributeType> getAttributesClassifiedBy(RuleForm ruleform,
                                                          Aspect<RuleForm> aspect) {
         TypedQuery<AttributeType> query = em.createNamedQuery(prefix
                                                                       + FIND_CLASSIFIED_ATTRIBUTE_VALUES_SUFFIX,
                                                               attribute);
         query.setParameter("ruleform", ruleform);
         query.setParameter("classifier", aspect.getClassifier());
         query.setParameter("classification", aspect.getClassification());
         return query.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getAttributesGroupedBy(com.hellblazer
      * .CoRE.ExistentialRuleform, com.hellblazer.CoRE.agency.Agency)
      */
     @Override
     public List<AttributeType> getAttributesGroupedBy(RuleForm ruleform,
                                                       Agency groupingAgency) {
         TypedQuery<AttributeType> query = em.createNamedQuery(prefix
                                                                       + FIND_GROUPED_ATTRIBUTE_VALUES_SUFFIX,
                                                               attribute);
         query.setParameter("ruleform", ruleform);
         query.setParameter("agency", groupingAgency);
         return query.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getNetwork(com.hellblazer.CoRE
      * .network.Networked, com.hellblazer.CoRE.network.Relationship)
      */
     @Override
     public List<RuleForm> getChildren(RuleForm parent, Relationship relationship) {
         String prefix = parent.getClass().getSimpleName().toLowerCase()
                         + "Network";
         @SuppressWarnings("unchecked")
         TypedQuery<RuleForm> q = (TypedQuery<RuleForm>) em.createNamedQuery(prefix
                                                                                     + ExistentialRuleform.GET_CHILDREN_SUFFIX,
                                                                             parent.getClass());
         return q.getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getFacet(com.hellblazer.CoRE.
      * ExistentialRuleform, com.hellblazer.CoRE.meta.Aspect)
      */
     @Override
     public Facet<RuleForm, AttributeType> getFacet(RuleForm ruleform,
                                                    Aspect<RuleForm> aspect) {
         return new Facet<RuleForm, AttributeType>(
                                                   aspect,
                                                   ruleform,
                                                   getAttributesClassifiedBy(ruleform,
                                                                             aspect)) {
         };
     }
 
     @Override
     public Collection<Network> getImmediateNetworkEdges(RuleForm parent) {
         List<Network> edges = new ArrayList<Network>();
         for (Network edge : parent.getNetworkByParent()) {
             if (!edge.isInferred()) {
                 edges.add(edge);
             }
         }
         return edges;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getImmediateRelationships(com
      * .hellblazer.CoRE.ExistentialRuleform)
      */
     @Override
     public Collection<Relationship> getImmediateRelationships(RuleForm parent) {
         Set<Relationship> relationships = new HashSet<Relationship>();
         for (Network network : parent.getNetworkByChild()) {
            if (!network.isInferred()) {
                relationships.add(network.getRelationship());
            }
         }
         return relationships;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RuleForm> getInGroup(RuleForm parent, Relationship relationship) {
         /*
          * select n.child from <networkTable> n where n.parent = :parent and
          * n.relationship = :relationship and n.child <> :parent
          */
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<RuleForm> query = cb.createQuery(entity);
         Root<NetworkRuleform<RuleForm>> networkForm = query.from(network);
         query.select((Selection<? extends RuleForm>) networkForm.fetch("child"));
         query.where(cb.equal(networkForm.get("relationship"), relationship),
                     cb.notEqual(networkForm.get("child"), parent));
         return em.createQuery(query).getResultList();
     }
 
     @Override
     public List<RuleForm> getNotInGroup(RuleForm parent,
                                         Relationship relationship) {
         /*
          * SELECT e FROM product AS e, ProductNetwork AS n WHERE n.parent <>
          * :parent AND n.relationship = :relationship AND n.child <> e;
          */
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<RuleForm> query = cb.createQuery(entity);
         Root<RuleForm> form = query.from(entity);
         Root<NetworkRuleform<RuleForm>> networkForm = query.from(network);
         query.where(cb.equal(networkForm.get("parent"), parent),
                     cb.equal(networkForm.get("relationship"), relationship),
                     cb.notEqual(networkForm.get("child"), form));
         query.select(form);
         return em.createQuery(query).getResultList();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getChild(com.hellblazer.CoRE.
      * ExistentialRuleform, com.hellblazer.CoRE.network.Relationship)
      */
     @Override
     public RuleForm getSingleChild(RuleForm parent, Relationship r) {
         TypedQuery<RuleForm> query = em.createNamedQuery(prefix
                                                                  + GET_CHILDREN_SUFFIX,
                                                          entity);
         query.setParameter("p", parent);
         query.setParameter("r", r);
         try {
             return query.getSingleResult();
         } catch (NoResultException e) {
             if (log.isTraceEnabled()) {
                 log.trace(String.format("%s has no child for relationship %s",
                                         parent, r));
             }
             return null;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.hellblazer.CoRE.meta.NetworkedModel#getTransitiveRelationships(com
      * .hellblazer.CoRE.ExistentialRuleform)
      */
     @Override
     public Collection<Relationship> getTransitiveRelationships(RuleForm parent) {
         Set<Relationship> relationships = new HashSet<Relationship>();
         Set<RuleForm> visited = new HashSet<RuleForm>();
         visited.add(parent);
         for (Network network : parent.getNetworkByParent()) {
             addTransitiveRelationships(network, visited, relationships);
         }
         return relationships;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.hellblazer.CoRE.meta.NetworkedModel#getUnlinked()
      */
     @Override
     public List<RuleForm> getUnlinked() {
         return em.createNamedQuery(prefix + UNLINKED_SUFFIX, entity).getResultList();
     }
 
     @Override
     public List<Relationship> getUsedRelationships() {
         return em.createNamedQuery(prefix + USED_RELATIONSHIPS_SUFFIX,
                                    Relationship.class).getResultList();
     }
 
     /* (non-Javadoc)
      * @see com.hellblazer.CoRE.meta.NetworkedModel#isAccessible(com.hellblazer.CoRE.ExistentialRuleform, com.hellblazer.CoRE.network.Relationship, com.hellblazer.CoRE.network.Relationship, com.hellblazer.CoRE.ExistentialRuleform, com.hellblazer.CoRE.network.Relationship)
      */
     @Override
     public boolean isAccessible(RuleForm parent,
                                 Relationship parentRelationship,
                                 Relationship authorizingRelationship,
                                 ExistentialRuleform<?, ?> child,
                                 Relationship childRelationship) {
         String queryPrefix = constructQueryPrefix(parent, child);
         return isRuleformAccessible(parent, parentRelationship,
                                     authorizingRelationship, child,
                                     childRelationship, queryPrefix);
     }
 
     @Override
     public void link(RuleForm parent, Relationship r, RuleForm child,
                      Agency updatedBy) {
         parent.link(r, child, updatedBy, kernel.getInverseSoftware(), em);
     }
 
     @Override
     public void networkEdgeDeleted(long parent, long relationship) {
         em.createNativeQuery(String.format("DELETE FROM %s WHERE parent = %s AND relationship = %s",
                                            networkTable, parent, relationship));
 
     }
 
     @Override
     public void propagate() {
         createDeductionTemporaryTables();
         boolean firstPass = true;
         do {
             int newRules;
             // Deduce all possible rules
             if (firstPass) {
                 newRules = em.createNamedQuery(networkPrefix
                                                        + INFERENCE_STEP_SUFFIX).executeUpdate();
                 firstPass = false;
             } else {
                 newRules = em.createNamedQuery(networkPrefix
                                                        + INFERENCE_STEP_FROM_LAST_PASS_SUFFIX).executeUpdate();
             }
             if (log.isTraceEnabled()) {
                 log.trace(String.format("inferred %s new rules", newRules));
             }
             if (newRules == 0) {
                 break;
             }
             // Gather all rules which exist
             int existing = em.createNamedQuery(networkPrefix
                                                        + GATHER_EXISTING_NETWORK_RULES_SUFFIX).executeUpdate();
             if (log.isTraceEnabled()) {
                 log.trace(String.format("gathered %s existing rules", existing));
             }
             // Deduce the new rules
             int deduced = em.createNamedQuery(networkPrefix
                                                       + DEDUCE_NEW_NETWORK_RULES_SUFFIX).executeUpdate();
             if (log.isTraceEnabled()) {
                 log.trace(String.format("deduced %s rules", deduced));
             }
             // Insert the new rules
             Query insert = em.createNamedQuery(networkPrefix
                                                + INSERT_NEW_NETWORK_RULES_SUFFIX);
             insert.setParameter(1, kernel.getPropagationSoftware().getId());
             int inserted = insert.executeUpdate();
             if (log.isTraceEnabled()) {
                 log.trace(String.format("inserted %s new rules", inserted));
             }
             alterDeductionTablesForNextPass();
             generateInverses();
         } while (true);
     }
 
     private void addTransitiveRelationships(Network edge,
                                             Set<RuleForm> visited,
                                             Set<Relationship> relationships) {
         if (!relationships.add(edge.getRelationship())) {
             return;
         }
         RuleForm child = edge.getChild();
         for (Network network : child.getNetworkByParent()) {
             RuleForm traversing = network.getChild();
             if (visited.add(traversing)) {
                 addTransitiveRelationships(network, visited, relationships);
             }
         }
     }
 
     private void alterDeductionTablesForNextPass() {
         em.createNativeQuery("TRUNCATE TABLE last_pass_rules").executeUpdate();
         em.createNativeQuery("ALTER TABLE current_pass_rules RENAME TO temp_last_pass_rules").executeUpdate();
         em.createNativeQuery("ALTER TABLE last_pass_rules RENAME TO current_pass_rules").executeUpdate();
         em.createNativeQuery("ALTER TABLE temp_last_pass_rules RENAME TO last_pass_rules").executeUpdate();
         em.createNativeQuery("TRUNCATE current_pass_existing_rules").executeUpdate();
         em.createNativeQuery("TRUNCATE working_memory").executeUpdate();
     }
 
     /**
      * @param parent
      * @param child
      * @return
      */
     private String constructQueryPrefix(RuleForm parent,
                                         ExistentialRuleform<?, ?> child) {
         return parent.getClass().getSimpleName().toLowerCase()
                + child.getClass().getSimpleName() + "AccessAuthorization";
     }
 
     private void createCurrentPassExistingRules() {
         em.createNativeQuery("CREATE TEMPORARY TABLE current_pass_existing_rules ("
                                      + "id BIGINT NOT NULL,"
                                      + "parent BIGINT NOT NULL,"
                                      + "relationship BIGINT NOT NULL,"
                                      + "child BIGINT NOT NULL,"
                                      + "premise1 BIGINT NOT NULL,"
                                      + "premise2 BIGINT NOT NULL)").executeUpdate();
     }
 
     private void createCurrentPassRules() {
         em.createNativeQuery("CREATE TEMPORARY TABLE current_pass_rules ("
                                      + "id BIGINT NOT NULL,"
                                      + "parent BIGINT NOT NULL,"
                                      + "relationship BIGINT NOT NULL,"
                                      + "child BIGINT NOT NULL)").executeUpdate();
     }
 
     private void createDeductionTemporaryTables() {
         Query exists = em.createNativeQuery("SELECT public.iftableexists('working_memory')",
                                             Boolean.class);
         if ((Boolean) exists.getSingleResult()) {
             em.createNativeQuery("TRUNCATE last_pass_rules").executeUpdate();
             em.createNativeQuery("TRUNCATE current_pass_rules").executeUpdate();
             em.createNativeQuery("TRUNCATE last_pass_rules").executeUpdate();
             em.createNativeQuery("TRUNCATE current_pass_existing_rules").executeUpdate();
             em.createNativeQuery("TRUNCATE working_memory").executeUpdate();
             return;
         }
         createWorkingMemory();
         createCurrentPassRules();
         createCurrentPassExistingRules();
         createLastPassRules();
     }
 
     private void createLastPassRules() {
         em.createNativeQuery("CREATE TEMPORARY TABLE last_pass_rules ("
                                      + "id BIGINT NOT NULL,"
                                      + "parent BIGINT NOT NULL,"
                                      + "relationship BIGINT NOT NULL,"
                                      + "child BIGINT NOT NULL)").executeUpdate();
     }
 
     private void createWorkingMemory() {
         em.createNativeQuery("CREATE TEMPORARY TABLE working_memory("
                                      + "parent BIGINT NOT NULL,"
                                      + "relationship BIGINT NOT NULL,"
                                      + "child BIGINT NOT NULL,"
                                      + "premise1 BIGINT NOT NULL,"
                                      + "premise2 BIGINT NOT NULL )").executeUpdate();
     }
 
     @SuppressWarnings("unchecked")
     private Class<AttributeType> extractedAttribute() {
         return (Class<AttributeType>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[3];
     }
 
     @SuppressWarnings("unchecked")
     private Class<AttributeAuthorization> extractedAuthorization() {
         return (Class<AttributeAuthorization>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2];
     }
 
     @SuppressWarnings("unchecked")
     private Class<RuleForm> extractedEntity() {
         return (Class<RuleForm>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
     }
 
     @SuppressWarnings("unchecked")
     private Class<Network> extractedNetwork() {
         return (Class<Network>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
     }
 
     private boolean isRuleformAccessible(RuleForm parent,
                                          Relationship parentRelationship,
                                          Relationship authorizingRelationship,
                                          Ruleform child,
                                          Relationship childRelationship,
                                          String queryPrefix) {
         Query query;
 
         if (parentRelationship == null && childRelationship == null) {
             query = em.createNamedQuery(queryPrefix
                                         + AccessAuthorization.FIND_ALL_AUTHS_FOR_PARENT_RELATIONSHIP_CHILD_SUFFIX);
             query.setParameter("parent", parent);
             query.setParameter("relationship", authorizingRelationship);
             query.setParameter("child", child);
         } else if (childRelationship == null) {
             query = em.createNamedQuery(queryPrefix
                                         + AccessAuthorization.FIND_AUTHS_FOR_INDIRECT_PARENT_SUFFIX);
             query.setParameter("relationship", authorizingRelationship);
             query.setParameter("child", child);
             query.setParameter("netRelationship", parentRelationship);
             query.setParameter("netChild", parent);
 
         } else if (parentRelationship == null) {
             query = em.createNamedQuery(queryPrefix
                                         + AccessAuthorization.FIND_AUTHS_FOR_INDIRECT_CHILD_SUFFIX);
             query.setParameter("relationship", authorizingRelationship);
             query.setParameter("parent", parent);
             query.setParameter("netRelationship", childRelationship);
             query.setParameter("netChild", child);
 
         } else {
             query = em.createNamedQuery(queryPrefix
                                         + AccessAuthorization.FIND_AUTHS_FOR_INDIRECT_PARENT_AND_CHILD_SUFFIX);
             query.setParameter("relationship", authorizingRelationship);
             query.setParameter("parentNetRelationship", parentRelationship);
             query.setParameter("parentNetChild", parent);
             query.setParameter("childNetRelationship", childRelationship);
             query.setParameter("childNetChild", child);
 
         }
         List<?> results = query.getResultList();
 
         return results.size() > 0;
 
     }
 
     /**
      * @param attribute
      * @param authorizations
      * @return
      */
     @SuppressWarnings("unchecked")
     protected <ValueType> List<ValueType> getAllowedValues(Attribute attribute,
                                                            List<AttributeAuthorization> authorizations) {
         switch (attribute.getValueType()) {
             case BOOLEAN: {
                 return (List<ValueType>) Arrays.asList(Boolean.TRUE,
                                                        Boolean.FALSE);
             }
             case BINARY: {
                 return Collections.EMPTY_LIST;
             }
             default:
         }
 
         List<ValueType> allowedValues = new ArrayList<ValueType>();
         for (AttributeAuthorization authorization : authorizations) {
             switch (attribute.getValueType()) {
                 case INTEGER: {
                     allowedValues.add((ValueType) authorization.getIntegerValue());
                     break;
                 }
                 case NUMERIC: {
                     allowedValues.add((ValueType) authorization.getNumericValue());
                     break;
                 }
                 case TEXT: {
                     allowedValues.add((ValueType) authorization.getTextValue());
                     break;
                 }
                 case TIMESTAMP: {
                     allowedValues.add((ValueType) authorization.getTimestampValue());
                     break;
                 }
                 default:
             }
         }
         return allowedValues;
     }
 }
