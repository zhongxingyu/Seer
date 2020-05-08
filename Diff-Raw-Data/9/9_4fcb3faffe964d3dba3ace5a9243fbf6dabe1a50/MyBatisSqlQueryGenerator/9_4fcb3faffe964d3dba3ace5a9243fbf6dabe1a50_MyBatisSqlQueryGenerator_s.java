 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.generator.processors.database.myBatis;
 
 import java.util.ArrayList;
 import org.uaithne.annotations.Comparator;
 import static org.uaithne.annotations.Comparator.CONTAINS;
 import static org.uaithne.annotations.Comparator.CONTAINS_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.END_WITH;
 import static org.uaithne.annotations.Comparator.END_WITH_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.EQUAL;
 import static org.uaithne.annotations.Comparator.EQUAL_NULLABLE;
 import static org.uaithne.annotations.Comparator.IN;
 import static org.uaithne.annotations.Comparator.LARGER;
 import static org.uaithne.annotations.Comparator.LARGER_AS;
 import static org.uaithne.annotations.Comparator.LIKE;
 import static org.uaithne.annotations.Comparator.LIKE_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.NOT_CONTAINS;
 import static org.uaithne.annotations.Comparator.NOT_CONTAINS_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.NOT_END_WITH;
 import static org.uaithne.annotations.Comparator.NOT_END_WITH_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.NOT_EQUAL;
 import static org.uaithne.annotations.Comparator.NOT_EQUAL_NULLABLE;
 import static org.uaithne.annotations.Comparator.NOT_IN;
 import static org.uaithne.annotations.Comparator.NOT_LIKE;
 import static org.uaithne.annotations.Comparator.NOT_LIKE_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.NOT_START_WITH;
 import static org.uaithne.annotations.Comparator.NOT_START_WITH_INSENSITIVE;
 import static org.uaithne.annotations.Comparator.SMALLER;
 import static org.uaithne.annotations.Comparator.SMALL_AS;
 import static org.uaithne.annotations.Comparator.START_WITH;
 import static org.uaithne.annotations.Comparator.START_WITH_INSENSITIVE;
 import org.uaithne.annotations.sql.CustomSqlQuery;
 import org.uaithne.generator.commons.FieldInfo;
 import org.uaithne.generator.processors.database.sql.SqlQueryGenerator;
 
 public abstract class MyBatisSqlQueryGenerator extends SqlQueryGenerator {
     
     //<editor-fold defaultstate="collapsed" desc="Order by">
     @Override
     public void appendOrderBy(StringBuilder result, ArrayList<FieldInfo> orderBys, CustomSqlQuery customQuery) {
         if (customQuery != null) {
             if (hasQueryValue(customQuery.orderBy())) {
                 result.append("\norder by");
                 appendToQueryln(result, customQuery.beforeOrderByExpression(), "    ");
                 appendToQueryln(result, customQuery.orderBy(), "    ");
                 appendToQueryln(result, customQuery.afterOrderByExpression(), "    ");
             } else if (hasQueryValue(customQuery.beforeOrderByExpression()) || hasQueryValue(customQuery.afterOrderByExpression())) {
                 result.append("\norder by");
                 appendToQueryln(result, customQuery.beforeOrderByExpression(), "    ");
                 appendOrderByContent(result, orderBys, "\n    ", ",\n    ");
                 appendToQueryln(result, customQuery.afterOrderByExpression(), "    ");
             } else {
                 appendOptionalOrderByContent(result, orderBys, "\n", "", ", ", "order by ", " ");
             }
         } else {
             appendOptionalOrderByContent(result, orderBys, "\n", "", ", ", "order by ", " ");
         }
     }
     
     public void appendOrderByContent(StringBuilder result, ArrayList<FieldInfo> orderBys, String separator, String commaSeparator) {
         boolean requireComma = false;
         for (FieldInfo orderBy : orderBys) {
             if (requireComma) {
                 result.append(commaSeparator);
             } else {
                 result.append(separator);
             }
             if (orderBy.isOptional()) {
                 result.append("<if test='");
                 result.append(orderBy.getName());
                 result.append(" != null'> ${");
                 result.append(orderBy.getName());
                 result.append("} </if>");
             } else {
                 result.append("${");
                 result.append(orderBy.getName());
                 result.append("}");
             }
         }
     }
     
     public void appendOptionalOrderByContent(StringBuilder result, ArrayList<FieldInfo> orderBys, String start, String separator, String commaSeparator, String startOrderBy, String endOrderBy) {
         if (orderBys.isEmpty()) {
             return;
         }
         
         boolean requireComma = false;
         boolean optional = false;
         result.append(start);
         for (FieldInfo orderBy : orderBys) {
             optional = optional || orderBy.isOptional();
         }
         if (optional) {
             result.append("<if test='");
             boolean requireOr = false;
             for (FieldInfo orderBy : orderBys) {
                 if (requireOr) {
                     result.append(" or ");
                 }
                 result.append(orderBy.getName());
                 result.append(" != null");
             }
             result.append("'> ");
         }
         result.append(startOrderBy);
         for (FieldInfo orderBy : orderBys) {
             if (requireComma) {
                 result.append(commaSeparator);
             } else {
                 result.append(separator);
             }
             if (orderBy.isOptional() && orderBys.size() > 1) {
                 result.append("<if test='");
                 result.append(orderBy.getName());
                 result.append(" != null'> ${");
                 result.append(orderBy.getName());
                 result.append("} </if>");
             } else {
                 result.append("${");
                 result.append(orderBy.getName());
                 result.append("}");
             }
         }
         result.append(endOrderBy);
         if (optional) {
             result.append("</if>");
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Parameter">
     @Override
     public String getParameterValue(FieldInfo field) {
         return "#{" + field.getName() + MyBatisUtils.getJdbcType(field) + MyBatisUtils.getTypeHandler(processingEnv, field) + "}";
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Where">
     @Override
     public void appendStartWhere(StringBuilder result) {
         result.append("<where>");
     }
 
     @Override
     public void appendEndWhere(StringBuilder result, String separator) {
         result.append(separator);
         result.append("</where>");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Set">
     @Override
     public void appendStartSet(StringBuilder result) {
         result.append("<set>");
     }
 
     @Override
     public void appendEndSet(StringBuilder result, String separator) {
         result.append(separator);
         result.append("</set>");
     }
     
     @Override
     public void appendStartSetValueIfNotNull(StringBuilder result, FieldInfo field) {
         appendConditionStartIfNotNull(result, field, "");
         result.append(getColumnName(field));
         result.append(" = ");
         result.append(getParameterValue(field));
     }
     
     @Override
     public void appendEndSetValueIfNotNull(StringBuilder result, boolean requireComma) {
         if (requireComma) {
             result.append(",");
         }
         appendConditionEndIf(result);
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="If (not) null">
     @Override
     public void appendConditionStartIfNull(StringBuilder result, FieldInfo field, String separator) {
         result.append("<if test='");
         result.append(field.getName());
         result.append(" == null'>");
         result.append(separator);
     }
 
     @Override
     public void appendConditionStartIfNotNull(StringBuilder result, FieldInfo field, String separator) {
         result.append("<if test='");
         result.append(field.getName());
         result.append(" != null'>");
         result.append(separator);
     }
 
     @Override
     public void appendConditionEndIf(StringBuilder result) {
         result.append("</if>");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Comparators">
     @Override
     public String translateComparator(Comparator comparator) {
         if (comparator == null) {
             return null;
         }
         switch (comparator) {
             case EQUAL:      return "[[column]] = [[value]]";
             case NOT_EQUAL:  return "[[column]] &lt;&gt; [[value]]";
             case EQUAL_NULLABLE:         return "<if test='[[name]] != null'> [[column]] = [[value]] </if> <if test='[[name]] == null'> [[column]] is null </if>";
             case EQUAL_NOT_NULLABLE:     return "<if test='[[name]] != null'> [[column]] = [[value]] </if> <if test='[[name]] == null'> [[column]] is not null </if>";
             case NOT_EQUAL_NULLABLE:     return "<if test='[[name]] != null'> [[column]] &lt;&gt; [[value]] </if> <if test='[[name]] == null'> [[column]] is null </if>";
             case NOT_EQUAL_NOT_NULLABLE: return "<if test='[[name]] != null'> [[column]] &lt;&gt; [[value]] </if> <if test='[[name]] == null'> [[column]] is not null </if>";
             case SMALLER:    return "[[column]] &lt; [[value]]";
             case LARGER:     return "[[column]] &gt; [[value]]";
             case SMALL_AS:   return "[[column]] &lt;= [[value]]";
             case LARGER_AS:  return "[[column]] &gt;= [[value]]";
             case IN:         return "[[column]] in <foreach collection='[[name]]' open='(' separator=',' close=')' item='_item_[[name]]'> #{_item_[[name]][[jdbcType]][[typeHandler]]} </foreach>";
             case NOT_IN:     return "[[column]] not in <foreach collection='[[name]]' open='(' separator=',' close=')' item='_item_[[name]]'> #{_item_[[name]][[jdbcType]][[typeHandler]]} </foreach>";
             case LIKE:       return "[[column]] like [[value]]";
             case NOT_LIKE:   return "[[column]] not like [[value]]";
             case LIKE_INSENSITIVE:       return "lower([[column]]) like lower([[value]])";
             case NOT_LIKE_INSENSITIVE:   return "lower([[column]]) not like lower([[value]])";
             case START_WITH:     return "[[column]] like ([[value]] || '%')";
             case NOT_START_WITH: return "[[column]] not like ([[value]] || '%')";
            case END_WITH:       return "[[column]] like ('%' || [[value]]";
            case NOT_END_WITH:   return "[[column]] not like ('%' || [[value]]";
             case START_WITH_INSENSITIVE:     return "lower([[column]]) like (lower([[value]]) || '%')";
             case NOT_START_WITH_INSENSITIVE: return "lower([[column]]) not like (lower([[value]]) || '%')";
            case END_WITH_INSENSITIVE:       return "lower([[column]]) like ('%' || lower([[value]])";
            case NOT_END_WITH_INSENSITIVE:   return "lower([[column]]) not like ('%' || lower([[value]])";
             case CONTAINS:       return "[[column]] like ('%' || [[value]] || '%')";
             case NOT_CONTAINS:   return "[[column]] not like ('%' || [[value]] || '%')";
             case CONTAINS_INSENSITIVE:       return "lower([[column]]) like ('%' || lower([[value]]) || '%')";
             case NOT_CONTAINS_INSENSITIVE:   return "lower([[column]]) not like ('%' || lower([[value]]) || '%')";
             default:
                 throw new IllegalArgumentException("Unimplemented comparator " + comparator);
         }
     }
 
     @Override
     public String getConditionElementValue(String rule, FieldInfo field, CustomSqlQuery customQuery) {
         if (rule == null) {
             return null;
         } else if ("column".equals(rule) || "COLUMN".equals(rule)) {
             return getColumnNameForWhere(field, customQuery);
         } else if ("name".equals(rule) || "NAME".equals(rule)) {
             return field.getName();
         } else if ("value".equals(rule) || "VALUE".equals(rule)) {
             return getParameterValue(field);
         } else if ("jdbcType".equals(rule) || "JDBC_TYPE".equals(rule)) {
             return MyBatisUtils.getJdbcType(field);
         } else if ("typeHandler".equals(rule) || "TYPE_HANDLER".equals(rule)) {
             return MyBatisUtils.getTypeHandler(processingEnv, field);
         } else {
             return null;
         }
     }
     //</editor-fold>
 
 }
