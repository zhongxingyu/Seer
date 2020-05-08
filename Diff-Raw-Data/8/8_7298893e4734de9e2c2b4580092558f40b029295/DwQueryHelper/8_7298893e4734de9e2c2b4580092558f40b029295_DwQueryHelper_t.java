 package org.motechproject.carereporting.indicator;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.text.StrSubstitutor;
 import org.dwQueryBuilder.builders.ComplexDwQueryBuilder;
 import org.dwQueryBuilder.builders.DwQueryCombinationBuilder;
 import org.dwQueryBuilder.builders.FactBuilder;
 import org.dwQueryBuilder.builders.GroupByConditionBuilder;
 import org.dwQueryBuilder.builders.HavingConditionBuilder;
 import org.dwQueryBuilder.builders.SelectColumnBuilder;
 import org.dwQueryBuilder.builders.SimpleDwQueryBuilder;
 import org.dwQueryBuilder.builders.WhereConditionBuilder;
 import org.dwQueryBuilder.builders.WhereConditionGroupBuilder;
 import org.dwQueryBuilder.data.GroupBy;
 import org.dwQueryBuilder.data.conditions.where.WhereCondition;
 import org.dwQueryBuilder.data.enums.CombineType;
 import org.dwQueryBuilder.data.enums.OperatorType;
 import org.dwQueryBuilder.data.enums.SelectColumnFunctionType;
 import org.dwQueryBuilder.data.enums.WhereConditionJoinType;
 import org.dwQueryBuilder.data.queries.ComplexDwQuery;
 import org.dwQueryBuilder.data.queries.DwQuery;
 import org.dwQueryBuilder.data.queries.SimpleDwQuery;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.CombinationEntity;
 import org.motechproject.carereporting.domain.ComplexDwQueryEntity;
 import org.motechproject.carereporting.domain.ConditionEntity;
 import org.motechproject.carereporting.domain.DateDiffComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.FactEntity;
 import org.motechproject.carereporting.domain.GroupedByEntity;
 import org.motechproject.carereporting.domain.HavingEntity;
 import org.motechproject.carereporting.domain.PeriodConditionEntity;
 import org.motechproject.carereporting.domain.SelectColumnEntity;
 import org.motechproject.carereporting.domain.SimpleDwQueryEntity;
 import org.motechproject.carereporting.domain.ValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class DwQueryHelper {
 
     private static final int SECONDS_PER_DAY = 86_400;
 
     public DwQuery buildDwQuery(DwQueryEntity dwQueryEntity, AreaEntity area) {
         DwQuery dwQuery = buildDwQuery(dwQueryEntity);
         addAreaWhereCondition(dwQuery, area);
         return dwQuery;
     }
 
     public String formatFromDateAndToDate(String query, Date from, Date to) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         Map<String, String> params = new HashMap<>();
         params.put("fromDate", dateFormat.format(from));
         params.put("toDate", dateFormat.format(to));
         return formatNamesParams(query, params);
     }
 
     private String formatNamesParams(String strToFormat, Map<String, String> params) {
         StrSubstitutor sub = new StrSubstitutor(params, "%(", ")");
         return sub.replace(strToFormat);
     }
 
     private DwQuery buildDwQuery(DwQueryEntity dwQueryEntity) {
         if (dwQueryEntity instanceof ComplexDwQueryEntity) {
             return buildComplexDwQuery((ComplexDwQueryEntity) dwQueryEntity);
         } else {
             return buildSimpleDwQuery((SimpleDwQueryEntity) dwQueryEntity);
         }
     }
 
     private SimpleDwQuery buildSimpleDwQuery(SimpleDwQueryEntity simpleDwQueryEntity) {
         SimpleDwQueryBuilder dwQueryBuilder = new SimpleDwQueryBuilder();
         if (simpleDwQueryEntity.getCombination() != null) {
             dwQueryBuilder.withCombination(prepareCombination(simpleDwQueryEntity.getCombination()));
         }
         if (simpleDwQueryEntity.getGroupedBy() != null) {
             dwQueryBuilder.withGroupBy(prepareGroupBy(simpleDwQueryEntity.getGroupedBy()));
         }
         for (SelectColumnEntity selectColumn: simpleDwQueryEntity.getSelectColumns()) {
             dwQueryBuilder.withSelectColumn(prepareSelectColumn(selectColumn));
         }
         dwQueryBuilder.withTableName(simpleDwQueryEntity.getTableName());
         if (simpleDwQueryEntity.getWhereGroup() != null) {
             dwQueryBuilder.withWhereConditionGroup(prepareWhereConditionGroup(simpleDwQueryEntity.getWhereGroup()));
         }
         return dwQueryBuilder.build();
     }
 
     private ComplexDwQuery buildComplexDwQuery(ComplexDwQueryEntity complexDwQueryEntity) {
         ComplexDwQueryBuilder dwQueryBuilder = new ComplexDwQueryBuilder();
 
         for (FactEntity fact: complexDwQueryEntity.getFacts()) {
             dwQueryBuilder.withFact(buildFact(fact));
         }
         dwQueryBuilder.withDimension(complexDwQueryEntity.getDimension());
         if (complexDwQueryEntity.getGroupedBy() != null) {
             dwQueryBuilder.withGroupBy(prepareGroupBy(complexDwQueryEntity.getGroupedBy()));
         }
         if (complexDwQueryEntity.getCombination() != null) {
             dwQueryBuilder.withCombination(prepareCombination(complexDwQueryEntity.getCombination()));
         }
         for (SelectColumnEntity selectColumn: complexDwQueryEntity.getSelectColumns()) {
             dwQueryBuilder.withSelectColumn(prepareSelectColumn(selectColumn));
         }
         if (complexDwQueryEntity.getWhereGroup() != null) {
             dwQueryBuilder.withWhereConditionGroup(prepareWhereConditionGroup(complexDwQueryEntity.getWhereGroup()));
         }
         dwQueryBuilder.withKeys(complexDwQueryEntity.getDimensionKey(), complexDwQueryEntity.getFactKey());
         return dwQueryBuilder.build();
     }
 
     private FactBuilder buildFact(FactEntity fact) {
         FactBuilder builder = new FactBuilder()
                 .withTable(buildSimpleDwQuery(fact.getTable()));
         if (fact.getCombineType() != null) {
             builder.withCombineType(CombineType.valueOf(fact.getCombineType()));
         }
         return builder;
     }
 
     private GroupBy prepareGroupBy(GroupedByEntity groupByEntity) {
         return new GroupByConditionBuilder()
                 .withField(groupByEntity.getTableName(), groupByEntity.getFieldName())
                 .withHaving(prepareHaving(groupByEntity.getHaving()))
                 .build();
     }
 
     private HavingConditionBuilder prepareHaving(HavingEntity havingEntity) {
         return new HavingConditionBuilder()
                 .withComparison(OperatorType.fromSymbol(havingEntity.getOperator()), havingEntity.getValue())
                 .withSelectColumn(prepareSelectColumn(havingEntity.getSelectColumnEntity()));
     }
 
     private SelectColumnBuilder prepareSelectColumn(SelectColumnEntity selectColumnEntity) {
         SelectColumnBuilder builder = new SelectColumnBuilder()
                 .withField(
                         selectColumnEntity.getTableName(),
                         selectColumnEntity.getName());
         if (StringUtils.isNotEmpty(selectColumnEntity.getFunctionName())) {
             builder.withFunction(
                     SelectColumnFunctionType.valueOf(selectColumnEntity.getFunctionName()));
         }
         return builder;
     }
 
     private DwQueryCombinationBuilder prepareCombination(CombinationEntity combinationEntity) {
         return new DwQueryCombinationBuilder()
                 .withCombineType(CombineType.valueOf(combinationEntity.getType()))
                 .withDwQuery(buildDwQuery(combinationEntity.getDwQuery()))
                 .withKeys(combinationEntity.getForeignKey(), combinationEntity.getReferencedKey());
     }
 
     private WhereConditionGroupBuilder prepareWhereConditionGroup(WhereGroupEntity whereGroupEntity) {
         WhereConditionGroupBuilder builder = new WhereConditionGroupBuilder();
         for (WhereGroupEntity nestedGroup: whereGroupEntity.getWhereGroups()) {
             builder.withGroup(prepareWhereConditionGroup(nestedGroup));
         }
         for (ConditionEntity condition: whereGroupEntity.getConditions()) {
             addCondition(builder, condition);
         }
         if (whereGroupEntity.getOperator() != null) {
             builder.withJoinType(WhereConditionJoinType.valueOf(whereGroupEntity.getOperator()));
         }
         return builder;
     }
 
     private void addCondition(WhereConditionGroupBuilder builder, ConditionEntity condition) {
         if (condition instanceof ValueComparisonConditionEntity) {
             builder.withCondition(prepareValueComparisonCondition((ValueComparisonConditionEntity) condition));
             return;
         } else if (condition instanceof DateDiffComparisonConditionEntity) {
             builder.withCondition(prepareDateDiffComparisonCondition((DateDiffComparisonConditionEntity) condition));
             return;
         } else if (condition instanceof PeriodConditionEntity) {
             PeriodConditionEntity periodCondition = (PeriodConditionEntity) condition;
             if (periodCondition.getOffset() != null) {
                 builder.withCondition(preparePeriodConditionWithOffset(periodCondition));
                 builder.withCondition(preparePeriodCondition(periodCondition));
             } else {
                 builder.withCondition(prepareDateBetweenCondition(periodCondition));
             }
             return;
         }
         throw new IllegalArgumentException("Condition type not supported.");
     }
 
     private WhereConditionBuilder prepareValueComparisonCondition(ValueComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withValueComparison(
                         condition.getField1().getForm().getTableName(),
                         condition.getField1().getName(),
                         OperatorType.fromSymbol(condition.getComparisonSymbol().getName()),
                         condition.getValue());
     }
 
     private WhereConditionBuilder prepareDateDiffComparisonCondition(DateDiffComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateDiffComparison(
                         condition.getField1().getForm().getTableName(),
                         condition.getField1().getName(),
                         OperatorType.fromSymbol(condition.getComparisonSymbol().getName()),
                         condition.getField2().getForm().getTableName(),
                         condition.getField2().getName(),
                         SECONDS_PER_DAY * condition.getValue());
     }
 
     private WhereConditionBuilder preparePeriodConditionWithOffset(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         condition.getTableName(),
                         condition.getColumnName(),
                        OperatorType.Less,
                         "%(fromDate)",
                        -Math.abs(condition.getOffset()));
     }
 
     private WhereConditionBuilder preparePeriodCondition(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         condition.getTableName(),
                         condition.getColumnName(),
                        OperatorType.GreaterEqual,
                         "%(toDate)");
     }
 
     private WhereConditionBuilder prepareDateBetweenCondition(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateRangeComparison(
                         condition.getTableName(),
                         condition.getColumnName(),
                         "%(fromDate)",
                         "%(toDate)");
     }
 
     @SuppressWarnings("PMD.UnusedFormalParameter")
     private void addAreaWhereCondition(DwQuery dwQuery, AreaEntity area) {
         //dwQuery.getWhereConditionGroup().addCondition(prepareAreaWhereCondition(area));
     }
 
     @SuppressWarnings("PMD.UnusedPrivateMethod")
     private WhereCondition prepareAreaWhereCondition(AreaEntity area) {
         return new WhereConditionBuilder()
                 .withValueComparison("flw", "state", OperatorType.Equal, area.getName())
                 .build();
     }
 }
