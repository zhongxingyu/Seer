 /*
  * Copyright (C) 2012 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.glom.web.server.libglom.Document;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.Field;
 import org.glom.web.shared.libglom.Relationship;
 import org.glom.web.shared.libglom.layout.LayoutItemField;
 import org.glom.web.shared.libglom.layout.SortClause;
 import org.glom.web.shared.libglom.layout.UsesRelationship;
 import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
 import org.jooq.AggregateFunction;
 import org.jooq.Condition;
 import org.jooq.SQLDialect;
 import org.jooq.SelectFinalStep;
 import org.jooq.SelectJoinStep;
 import org.jooq.SelectSelectStep;
 import org.jooq.conf.RenderKeywordStyle;
 import org.jooq.conf.RenderNameStyle;
 import org.jooq.conf.Settings;
 import org.jooq.impl.Factory;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class SqlUtils {
 
 	// TODO: Change to final ArrayList<LayoutItem_Field> fieldsToGet
 	public static String build_sql_select_with_key(final Connection connection, final String tableName,
 			final List<LayoutItemField> fieldsToGet, final Field primaryKey, final TypedDataItem primaryKeyValue) {
 
 		Condition whereClause = null; // Note that we ignore quickFind.
 		if (primaryKeyValue != null) {
 			whereClause = build_simple_where_expression(tableName, primaryKey, primaryKeyValue);
 		}
 
 		final SortClause sortClause = null; // Ignored.
 		return build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause, sortClause);
 	}
 
 	public static Condition build_simple_where_expression(final String tableName, final Field primaryKey,
 			final TypedDataItem primaryKeyValue) {
 
 		Condition result = null;
 
 		if (primaryKey == null) {
 			return result;
 		}
 
 		final String fieldName = primaryKey.getName();
 		if (StringUtils.isEmpty(fieldName)) {
 			return result;
 		}
 
 		final org.jooq.Field<Object> field = createField(tableName, fieldName);
 		result = field.equal(primaryKeyValue.getNumber()); // TODO: Handle other types too.
 		return result;
 	}
 
 	/*
 	 * private static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
 	 * final LayoutFieldVector fieldsToGet) { final Condition whereClause = null; return
 	 * build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause); }
 	 */
 
 	/*
 	 * private static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
 	 * final LayoutFieldVector fieldsToGet, final Condition whereClause) { final SortClause sortClause = null; return
 	 * build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause, sortClause); }
 	 */
 
 	public static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
 			final List<LayoutItemField> fieldsToGet, final Condition whereClause, final SortClause sortClause) {
 		final SelectFinalStep step = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
 				whereClause, sortClause);
 		if (step == null) {
 			return "";
 		}
 
 		final String query = step.getQuery().getSQL(true);
 		// Log.info("Query: " + query);
 		return query;
 	}
 
 	private static SelectSelectStep createSelect(final Connection connection) {
 		final Factory factory = new Factory(connection, SQLDialect.POSTGRES);
 		final Settings settings = factory.getSettings();
 		settings.setRenderNameStyle(RenderNameStyle.QUOTED); // TODO: This doesn't seem to have any effect.
 		settings.setRenderKeywordStyle(RenderKeywordStyle.UPPER); // TODO: Just to make debugging nicer.
 
 		final SelectSelectStep selectStep = factory.select();
 		return selectStep;
 	}
 
 	private static SelectFinalStep build_sql_select_step_with_where_clause(final Connection connection,
 			final String tableName, final List<LayoutItemField> fieldsToGet, final Condition whereClause,
 			final SortClause sortClause) {
 
 		final SelectSelectStep selectStep = createSelect(connection);
 
 		// Add the fields, and any necessary joins:
 		final List<UsesRelationship> listRelationships = build_sql_select_add_fields_to_get(selectStep, tableName,
 				fieldsToGet, sortClause, false /* extraJoin */);
 
 		final SelectJoinStep joinStep = selectStep.from(tableName);
 
 		// LEFT OUTER JOIN will get the field values from the other tables,
 		// and give us our fields for this table even if there is no corresponding value in the other table.
 		for (final UsesRelationship usesRelationship : listRelationships) {
 			builder_add_join(joinStep, usesRelationship);
 		}
 
 		SelectFinalStep finalStep = joinStep;
 		if (whereClause != null) {
 			finalStep = joinStep.where(whereClause);
 		}
 
 		return finalStep;
 	}
 
 	public static String build_sql_count_select_with_where_clause(final Connection connection, final String tableName,
 			final List<LayoutItemField> fieldsToGet) {
 		final SelectFinalStep selectInner = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
 				null, null);
 		return build_sql_select_count_rows(connection, selectInner);
 	}
 
 	public static String build_sql_count_select_with_where_clause(final Connection connection, final String tableName,
 			final List<LayoutItemField> fieldsToGet, final Condition whereClause) {
 		final SelectFinalStep selectInner = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
 				whereClause, null);
 		return build_sql_select_count_rows(connection, selectInner);
 	}
 
 	private static String build_sql_select_count_rows(final Connection connection, final SelectFinalStep selectInner) {
 		// TODO: Find a way to do this with the jOOQ API:
 		final SelectSelectStep select = createSelect(connection);
 
 		final org.jooq.Field<?> field = Factory.field("*");
 		final AggregateFunction<?> count = Factory.count(field);
 		select.select(count).from(selectInner);
 		return select.getQuery().getSQL(true);
 		// return "SELECT COUNT(*) FROM (" + query + ") AS glomarbitraryalias";
 	}
 
 	private static List<UsesRelationship> build_sql_select_add_fields_to_get(SelectSelectStep step,
 			final String tableName, final List<LayoutItemField> fieldsToGet, final SortClause sortClause,
 			final boolean extraJoin) {
 
 		// Get all relationships used in the query:
 		final List<UsesRelationship> listRelationships = new ArrayList<UsesRelationship>();
 
 		final int layoutFieldsSize = Utils.safeLongToInt(fieldsToGet.size());
 		for (int i = 0; i < layoutFieldsSize; i++) {
 			final UsesRelationship layout_item = fieldsToGet.get(i);
 			add_to_relationships_list(listRelationships, layout_item);
 		}
 
 		if (sortClause != null) {
 			final int sortFieldsSize = Utils.safeLongToInt(sortClause.size());
 			for (int i = 0; i < sortFieldsSize; i++) {
 				final SortClause.SortField pair = sortClause.get(i);
 				final UsesRelationship layout_item = pair.field;
 				add_to_relationships_list(listRelationships, layout_item);
 			}
 		}
 
 		boolean one_added = false;
 		for (int i = 0; i < layoutFieldsSize; i++) {
 			final LayoutItemField layout_item = fieldsToGet.get(i);
 
 			if (layout_item == null) {
 				// g_warn_if_reached();
 				continue;
 			}
 
 			// Get the parent, such as the table name, or the alias name for the join:
 			// final String parent = layout_item.get_sql_table_or_join_alias_name(tableName);
 
 			/*
 			 * TODO: const LayoutItem_FieldSummary* fieldsummary = dynamic_cast<const
 			 * LayoutItem_FieldSummary*>(layout_item.obj()); if(fieldsummary) { const Gnome::Gda::SqlBuilder::Id
 			 * id_function = builder->add_function( fieldsummary->get_summary_type_sql(),
 			 * builder->add_field_id(layout_item->get_name(), tableName)); builder->add_field_value_id(id_function); }
 			 * else {
 			 */
 			final org.jooq.Field<?> field = createField(tableName, layout_item);
 			if (field != null) {
 				step = step.select(field);
 
 				// Avoid duplicate records with doubly-related fields:
 				// TODO: if(extra_join)
 				// builder->select_group_by(id);
 			}
 			// }
 
 			one_added = true;
 		}
 
 		if (!one_added) {
 			// TODO: std::cerr << G_STRFUNC << ": No fields added: fieldsToGet.size()=" << fieldsToGet.size() <<
 			// std::endl;
 			return listRelationships;
 		}
 
 		return listRelationships;
 	}
 
 	private static org.jooq.Field<Object> createField(final String tableName, final String fieldName) {
 		final String sql_field_name = get_sql_field_name(tableName, fieldName);
 		if (StringUtils.isEmpty(sql_field_name)) {
 			return null;
 		}
 
 		return Factory.field(sql_field_name);
 	}
 
 	private static org.jooq.Field<Object> createField(final String tableName, final LayoutItemField layoutField) {
 		final String sql_field_name = get_sql_field_name(tableName, layoutField);
 		if (StringUtils.isEmpty(sql_field_name)) {
 			return null;
 		}
 
 		return Factory.field(sql_field_name);
 	}
 
 	private static String get_sql_field_name(final String tableName, final String fieldName) {
 
 		if (StringUtils.isEmpty(tableName)) {
 			return "";
 		}
 
 		if (StringUtils.isEmpty(fieldName)) {
 			return "";
 		}
 
 		// TODO: Quoting, escaping, etc:
 		return tableName + "." + fieldName;
 	}
 
 	private static String get_sql_field_name(final String tableName, final LayoutItemField layoutItemField) {
 
 		if (layoutItemField == null) {
 			return "";
 		}
 
 		if (StringUtils.isEmpty(tableName)) {
 			return "";
 		}
 
 		// TODO: Quoting, escaping, etc:
 		return get_sql_field_name(layoutItemField.getSqlTableOrJoinAliasName(tableName),
 				layoutItemField.getName());
 	}
 
 	private static void add_to_relationships_list(final List<UsesRelationship> listRelationships,
 			final UsesRelationship layout_item) {
 
 		if (layout_item == null) {
 			return;
 		}
 
 		if (!layout_item.getHasRelationshipName()) {
 			return;
 		}
 
 		// If this is a related relationship, add the first-level relationship too, so that the related relationship can
 		// be defined in terms of it:
 		// TODO: //If the table is not yet in the list:
 		if (layout_item.getHasRelatedRelationshipName()) {
 			final UsesRelationship usesRel = new UsesRelationshipImpl();
 			usesRel.setRelationship(layout_item.getRelationship());
 
 			// Remove any UsesRelationship that has only the same relationship (not related relationship),
 			// to avoid adding that part of the relationship to the SQL twice (two identical JOINS).
 			// listRemoveIfUsesRelationship(listRelationships, usesRel.getRelationship());
 
 			if (!listRelationships.contains(usesRel)) {
 				// These need to be at the front, so that related relationships can use
 				// them later in the SQL statement.
 				listRelationships.add(usesRel);
 			}
 
 		}
 
 		// Add the relationship to the list:
 		final UsesRelationship usesRel = new UsesRelationshipImpl();
 		usesRel.setRelationship(layout_item.getRelationship());
 		usesRel.setRelatedRelationship(layout_item.getRelatedRelationship());
 		if (!listRelationships.contains(usesRel)) {
 			listRelationships.add(usesRel);
 		}
 
 	}
 
 	/**
 	 * @param listRelationships
 	 * @param relationship
 	 */
 	/*
 	 * private static void listRemoveIfUsesRelationship(final List<UsesRelationship> listRelationships, final
 	 * Relationship relationship) { if (relationship == null) { return; }
 	 * 
 	 * final Iterator<UsesRelationship> i = listRelationships.iterator(); while (i.hasNext()) { final UsesRelationship
 	 * eachUsesRel = i.next(); if (eachUsesRel == null) continue;
 	 * 
 	 * // Ignore these: if (eachUsesRel.getHasRelatedRelationshipName()) { continue; }
 	 * 
 	 * final Relationship eachRel = eachUsesRel.getRelationship(); if (eachRel == null) { continue; }
 	 * 
 	 * Log.info("Checking: rel name=" + relationship.get_name() + ", eachRel name=" + eachRel.get_name());
 	 * 
 	 * if (UsesRelationship.relationship_equals(relationship, eachRel)) { i.remove(); Log.info("  Removed"); } else {
 	 * Log.info(" not equal"); }
 	 * 
 	 * } }
 	 */
 
 	private static void builder_add_join(SelectJoinStep step, final UsesRelationship uses_relationship) {
 		final Relationship relationship = uses_relationship.getRelationship();
 		if (!relationship.getHasFields()) { // TODO: Handle related_record has_fields.
 			if (relationship.getHasToTable()) {
 				// It is a relationship that only specifies the table, without specifying linking fields:
 
 				// TODO: from() takes SQL, not specifically a table name, so this is unsafe.
 				// TODO: stepResult = step.from(relationship.get_to_table());
 			}
 
 			return;
 		}
 
 		// Define the alias name as returned by get_sql_join_alias_name():
 
 		// Specify an alias, to avoid ambiguity when using 2 relationships to the same table.
 		final String alias_name = uses_relationship.getSqlJoinAliasName();
 
 		// Add the JOIN:
 		if (!uses_relationship.getHasRelatedRelationshipName()) {
 
 			final org.jooq.Field<Object> fieldFrom = createField(relationship.getFromTable(),
 					relationship.getFromField());
 			final org.jooq.Field<Object> fieldTo = createField(alias_name, relationship.getToField());
 			final Condition condition = fieldFrom.equal(fieldTo);
 
 			// TODO: join() takes SQL, not specifically an alias name, so this is unsafe.
 			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
 			step = step.leftOuterJoin(relationship.getToTable() + " AS " + alias_name).on(condition);
 		} else {
 			final UsesRelationship parent_relationship = new UsesRelationshipImpl();
 			parent_relationship.setRelationship(relationship);
 			final Relationship relatedRelationship = uses_relationship.getRelatedRelationship();
 
 			final org.jooq.Field<Object> fieldFrom = createField(parent_relationship.getSqlJoinAliasName(),
 					relatedRelationship.getFromField());
 			final org.jooq.Field<Object> fieldTo = createField(alias_name, relatedRelationship.getToField());
 			final Condition condition = fieldFrom.equal(fieldTo);
 
 			// TODO: join() takes SQL, not specifically an alias name, so this is unsafe.
 			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
 			step = step.leftOuterJoin(relatedRelationship.getToTable() + " AS " + alias_name).on(condition);
 		}
 	}
 
 	public static Condition get_find_where_clause_quick(final Document document, final String tableName,
 			final TypedDataItem quickFindValue) {
 		if (StringUtils.isEmpty(tableName)) {
 			return null;
 		}
 
 		// TODO: if(Conversions::value_is_empty(quick_search))
 		// return Gnome::Gda::SqlExpr();
 
 		Condition condition = null;
 
 		// TODO: Cache the list of all fields, as well as caching (m_Fields) the list of all visible fields:
 		final List<Field> fields = document.getTableFields(tableName);
 
 		final int fieldsSize = Utils.safeLongToInt(fields.size());
 		for (int i = 0; i < fieldsSize; i++) {
 			final Field field = fields.get(i);
 			if (field == null) {
 				continue;
 			}
 
 			if (field.getGlomType() != Field.GlomFieldType.TYPE_TEXT) {
 				continue;
 			}
 
 			final org.jooq.Field<Object> jooqField = createField(tableName, field.getName());
			final Condition thisCondition = jooqField.equal(quickFindValue.getText()); // TODO: Handle other types.
 
 			if (condition == null) {
 				condition = thisCondition;
 			} else {
 				condition = condition.or(thisCondition);
 			}
 		}
 
 		return condition;
 	}
 }
