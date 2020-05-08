 package org.motechproject.carereporting.indicator;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.text.StrSubstitutor;
 import org.dwQueryBuilder.builders.ComputedColumnBuilder;
 import org.dwQueryBuilder.builders.DwQueryBuilder;
 import org.dwQueryBuilder.builders.DwQueryCombinationBuilder;
 import org.dwQueryBuilder.builders.GroupByConditionBuilder;
 import org.dwQueryBuilder.builders.HavingConditionBuilder;
 import org.dwQueryBuilder.builders.SelectColumnBuilder;
 import org.dwQueryBuilder.builders.WhereConditionBuilder;
 import org.dwQueryBuilder.builders.WhereConditionGroupBuilder;
 import org.dwQueryBuilder.builders.steps.ComputedColumnBuilderOperationStep;
 import org.dwQueryBuilder.data.ComputedColumn;
 import org.dwQueryBuilder.data.DwQuery;
 import org.dwQueryBuilder.data.DwQueryCombination;
 import org.dwQueryBuilder.data.GroupBy;
 import org.dwQueryBuilder.data.SelectColumn;
 import org.dwQueryBuilder.data.conditions.where.WhereCondition;
 import org.dwQueryBuilder.data.enums.CombineType;
 import org.dwQueryBuilder.data.enums.ComparisonType;
 import org.dwQueryBuilder.data.enums.OperatorType;
 import org.dwQueryBuilder.data.enums.OrderByType;
 import org.dwQueryBuilder.data.enums.SelectColumnFunctionType;
 import org.dwQueryBuilder.data.enums.WhereConditionJoinType;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.CalculationEndDateConditionEntity;
 import org.motechproject.carereporting.domain.CombinationEntity;
 import org.motechproject.carereporting.domain.ComputedFieldEntity;
 import org.motechproject.carereporting.domain.ConditionEntity;
 import org.motechproject.carereporting.domain.DateDiffComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionValueEntity;
 import org.motechproject.carereporting.domain.FieldComparisonConditionEntity;
 import org.motechproject.carereporting.domain.FieldOperationEntity;
 import org.motechproject.carereporting.domain.GroupedByEntity;
 import org.motechproject.carereporting.domain.HavingEntity;
 import org.motechproject.carereporting.domain.OrderByEntity;
 import org.motechproject.carereporting.domain.PeriodConditionEntity;
 import org.motechproject.carereporting.domain.SelectColumnEntity;
 import org.motechproject.carereporting.domain.ValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 import org.motechproject.carereporting.domain.dto.DwQueryDto;
 import org.motechproject.carereporting.domain.dto.GroupByDto;
 import org.motechproject.carereporting.domain.dto.HavingDto;
 import org.motechproject.carereporting.domain.dto.SelectColumnDto;
 import org.motechproject.carereporting.domain.dto.WhereConditionDto;
 import org.motechproject.carereporting.domain.dto.WhereGroupDto;
 import org.motechproject.carereporting.domain.types.ConditionType;
 import org.motechproject.carereporting.service.ComputedFieldService;
 import org.motechproject.carereporting.utils.configuration.ConfigurationLocator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @SuppressWarnings("PMD.AvoidDuplicateLiterals")
 @Component
 public class DwQueryHelper {
 
     private static final int SECONDS_PER_DAY = 86_400;
     private static final String WILDCARD = "*";
 
     @Autowired
     private ComputedFieldService computedFieldService;
 
     public DwQueryHelper() {
 
     }
 
     public DwQuery buildDwQuery(DwQueryEntity dwQueryEntity, AreaEntity area) {
         DwQuery dwQuery = buildDwQuery(dwQueryEntity);
         String dwQueryTableName = dwQuery.getTableName();
         if (dwQueryTableName.equals("flw") || dwQueryTableName.endsWith("_case") || dwQueryTableName.endsWith("_form")) {
             addAreaJoinAndCondition(dwQuery, area);
         }
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
         DwQueryBuilder dwQueryBuilder = new DwQueryBuilder();
         if (dwQueryEntity.getCombination() != null) {
             dwQueryBuilder.withCombination(prepareCombination(dwQueryEntity.getCombination()));
         }
         if (dwQueryEntity.getGroupedBy() != null) {
             dwQueryBuilder.withGroupBy(prepareGroupBy(dwQueryEntity.getGroupedBy()));
         }
         for (SelectColumnEntity selectColumn: dwQueryEntity.getSelectColumns()) {
             dwQueryBuilder.withSelectColumn(prepareSelectColumn(selectColumn, dwQueryEntity.getTableName(), null));
         }
         dwQueryBuilder.withTableName(dwQueryEntity.getTableName());
         if (dwQueryEntity.getWhereGroup() != null) {
             dwQueryBuilder.withWhereConditionGroup(prepareWhereConditionGroup(dwQueryEntity.getWhereGroup()));
         }
         if (dwQueryEntity.getOrderBy() != null && dwQueryEntity.getOrderBy().size() > 0) {
             List<org.dwQueryBuilder.data.OrderBy> orderByList = new ArrayList<>();
             for (OrderByEntity orderByEntity : dwQueryEntity.getOrderBy()) {
                 orderByList.add(new org.dwQueryBuilder.data.OrderBy(
                         prepareSelectColumn(orderByEntity.getSelectColumn(), dwQueryEntity.getTableName(), null).build(),
                         OrderByType.valueOf(orderByEntity.getType().getValue())
                 ));
             }
             dwQueryBuilder.withOrderBy(orderByList);
         }
         if (dwQueryEntity.getLimit() != null) {
             dwQueryBuilder.withLimit(dwQueryEntity.getLimit());
         }
 
         return dwQueryBuilder.build();
     }
 
     private GroupBy prepareGroupBy(GroupedByEntity groupByEntity) {
         String tableName = groupByEntity.getComputedField().getForm().getTableName();
         String fieldName = groupByEntity.getComputedField().getName();
 
         GroupByConditionBuilder builder = new GroupByConditionBuilder().withField(tableName, fieldName);
         if (groupByEntity.getHaving() != null) {
             builder.withHaving(prepareHaving(groupByEntity.getHaving()));
         }
         return builder.build();
     }
 
     private HavingConditionBuilder prepareHaving(HavingEntity havingEntity) {
         String tableName = (havingEntity.getSelectColumnEntity().getComputedField() == null)
                 ? null : havingEntity.getSelectColumnEntity().getComputedField().getForm().getTableName();
         String fieldName = (havingEntity.getSelectColumnEntity().getComputedField() == null)
                 ? WILDCARD : havingEntity.getSelectColumnEntity().getComputedField().getName();
 
         return new HavingConditionBuilder()
                 .withComparison(ComparisonType.fromSymbol(havingEntity.getOperator()), havingEntity.getValue())
                 .withSelectColumn(prepareSelectColumn(havingEntity.getSelectColumnEntity(), tableName, fieldName));
     }
 
     private SelectColumnBuilder prepareSelectColumn(SelectColumnEntity selectColumnEntity,
                                                     String defaultTableName, String defaultFieldName) {
         SelectColumnBuilder builder;
         if (selectColumnEntity.getComputedField() == null) {
             builder = new SelectColumnBuilder().withColumn(
                     defaultTableName, defaultFieldName != null ? defaultFieldName : "*");
         } else {
             if (selectColumnEntity.getComputedField().getName().equals(WILDCARD)
                     && selectColumnEntity.getComputedField().getForm() == null) {
                 builder = new SelectColumnBuilder().withColumn(null, WILDCARD);
             } else {
                 ComputedFieldEntity computedFieldEntity = computedFieldService.getComputedFieldById(
                         selectColumnEntity.getComputedField().getId());
 
                 builder = new SelectColumnBuilder().withColumn(prepareComputedColumn(computedFieldEntity));
             }
 
         }
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
             if (condition == null) {
                 continue;
             }
 
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
         } else if (condition instanceof DateDiffComparisonConditionEntity) {
             builder.withCondition(prepareDateDiffComparisonCondition((DateDiffComparisonConditionEntity) condition));
         } else if (condition instanceof DateRangeComparisonConditionEntity) {
             builder.withCondition(prepareDateRangeComparisonCondition((DateRangeComparisonConditionEntity) condition));
         } else if (condition instanceof DateValueComparisonConditionEntity) {
             builder.withCondition(prepareDateValueComparisonCondition((DateValueComparisonConditionEntity) condition));
         } else if (condition instanceof EnumRangeComparisonConditionEntity) {
             builder.withCondition(prepareEnumRangeComparisonCondition((EnumRangeComparisonConditionEntity) condition));
         } else if (condition instanceof FieldComparisonConditionEntity) {
             builder.withCondition(prepareFieldComparisonCondition((FieldComparisonConditionEntity) condition));
         } else if (condition instanceof PeriodConditionEntity) {
             PeriodConditionEntity periodCondition = (PeriodConditionEntity) condition;
             if ((periodCondition.getOffset1() != 0 || periodCondition.getOffset2() != 0) &&
                     !periodCondition.getOffset1().equals(periodCondition.getOffset2())) {
                 builder.withCondition(preparePeriodConditionFromDateWithTwoOffsets(periodCondition));
                 builder.withCondition(preparePeriodConditionToDateWithTwoOffsets(periodCondition));
             } else {
                 builder.withCondition(prepareDateBetweenCondition(periodCondition));
             }
         } else if (condition instanceof CalculationEndDateConditionEntity) {
             CalculationEndDateConditionEntity conditionEntity = (CalculationEndDateConditionEntity) condition;
             builder.withCondition(prepareCalculationEndDateCondition(conditionEntity));
         } else {
             throw new IllegalArgumentException("Condition type not supported.");
         }
     }
 
     private WhereConditionBuilder prepareValueComparisonCondition(ValueComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         condition.getValue());
     }
 
     private WhereConditionBuilder prepareFieldComparisonCondition(FieldComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withFieldComparison(
                         condition.getField1().getForm().getTableName(),
                         condition.getField1().getName(),
                         condition.getOffset1() != null ? condition.getOffset1().toString() : "0",
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         condition.getField2().getForm().getTableName(),
                         condition.getField2().getName(),
                         condition.getOffset2() != null ? condition.getOffset2().toString() : "0");
     }
 
     private WhereConditionBuilder prepareDateDiffComparisonCondition(DateDiffComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateDiffComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         prepareComputedField(condition.getField2()),
                         SECONDS_PER_DAY * condition.getValue(),
                         condition.getOffset1(),
                         condition.getOffset2());
     }
 
     private WhereConditionBuilder prepareDateRangeComparisonCondition(DateRangeComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateRangeComparison(
                         prepareComputedField(condition.getField1()),
                         condition.getDate1().toString(),
                         condition.getDate2().toString(),
                         condition.getOffset1()
                 );
     }
 
     private WhereConditionBuilder prepareDateValueComparisonCondition(DateValueComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         condition.getValue().toString(),
                         condition.getOffset1()
                 );
     }
 
     private WhereConditionBuilder prepareEnumRangeComparisonCondition(EnumRangeComparisonConditionEntity condition) {
         Set<String> values = new LinkedHashSet<>();
         for (EnumRangeComparisonConditionValueEntity value : condition.getValues()) {
             values.add(value.getValue());
         }
 
         return new WhereConditionBuilder()
                 .withEnumRangeComparison(
                         prepareComputedField(condition.getField1()),
                         values
                 );
     }
 
     private WhereConditionBuilder preparePeriodConditionToDateWithTwoOffsets(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.Less,
                         "%(toDate)",
                         condition.getOffset1() < condition.getOffset2() ?
                                 condition.getOffset1() : condition.getOffset2());
     }
 
     private WhereConditionBuilder preparePeriodConditionFromDateWithTwoOffsets(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.GreaterEqual,
                         "%(fromDate)",
                         condition.getOffset1() > condition.getOffset2() ?
                                 condition.getOffset1() : condition.getOffset2());
     }
 
     private WhereConditionBuilder prepareCalculationEndDateCondition(CalculationEndDateConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.Less,
                         "%(toDate)",
                         (condition.getOffset() == null) ? 0 : condition.getOffset());
     }
 
     private WhereConditionBuilder prepareDateBetweenCondition(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateRangeComparison(prepareComputedField(condition.getField1()),
                         "%(fromDate)",
                         "%(toDate)");
     }
 
     private SelectColumnBuilder prepareComputedField(ComputedFieldEntity computedFieldEntity) {
        ComputedColumn computedColumn
                = prepareComputedColumn(computedFieldService.getComputedFieldById(computedFieldEntity.getId()));
        return new SelectColumnBuilder().withColumn(computedColumn);
     }
 
     private ComputedColumn prepareComputedColumn(ComputedFieldEntity computedFieldEntity) {
         if (computedFieldEntity.isRegularField()) {
             return new ComputedColumnBuilder().withComputedColumn(computedFieldEntity.getForm().getTableName(),
                     computedFieldEntity.getName()).build();
         } else {
             ComputedColumnBuilder builder = new ComputedColumnBuilder();
             Iterator<FieldOperationEntity> operations = computedFieldEntity.getFieldOperations().iterator();
             FieldOperationEntity operation = operations.next();
             ComputedColumnBuilderOperationStep stepBuilder = builder.withComputedColumn(prepareComputedColumn(operation.getField1()))
                     .withComputedColumn(OperatorType.fromName(operation.getOperatorType().getName()),
                             prepareComputedColumn(operation.getField2()));
             while (operations.hasNext()) {
                 operation = operations.next();
                 stepBuilder.withComputedColumn(OperatorType.fromName(operation.getOperatorType().getName()),
                         prepareComputedColumn(operation.getField2()));
 
             }
             return stepBuilder.build();
         }
     }
 
     private void addAreaJoinAndCondition(DwQuery dwQuery, AreaEntity area) {
         Boolean useTestFlwRole = Boolean.parseBoolean(ConfigurationLocator.getCareConfiguration().getProperty(
                 "care.flw.useTestFlwRole"));
 
         DwQueryCombination areaJoinCombination;
         if (dwQuery.getTableName().equals("flw")) {
             areaJoinCombination = prepareAreaJoin("id");
         } else {
             areaJoinCombination = prepareAreaJoin("user_id");
         }
 
         if (dwQuery.getCombineWith() != null) {
             for (DwQueryCombination dwQueryCombination : dwQuery.getCombineWith()) {
                 addAreaJoinAndCondition(dwQueryCombination.getDwQuery(), area);
             }
         } else {
             dwQuery.setCombineWith(new LinkedHashSet<DwQueryCombination>());
         }
 
         dwQuery.getCombineWith().add(areaJoinCombination);
         if (dwQuery.getWhereConditionGroup() == null) {
             dwQuery.setWhereConditionGroup(
                     new WhereConditionGroupBuilder()
                             .withCondition(prepareAreaWhereCondition(area))
                             .build()
             );
         } else {
             if (dwQuery.getWhereConditionGroup().getConditions() == null) {
                 dwQuery.getWhereConditionGroup().setConditions(new LinkedHashSet<WhereCondition>());
             }
 
             dwQuery.getWhereConditionGroup().addCondition(prepareAreaWhereCondition(area));
         }
 
         if (!useTestFlwRole && dwQuery.getWhereConditionGroup() != null) {
             dwQuery.getWhereConditionGroup().addCondition(prepareSkipTestFlwRole());
         }
     }
 
     private DwQueryCombination prepareAreaJoin(String flwIdColumnName) {
         return new DwQueryCombinationBuilder()
                 .withKeys("id", flwIdColumnName)
                 .withCombineType(CombineType.Join)
                 .withDwQuery(
                         new DwQueryBuilder()
                                 .withSelectColumn(
                                         new SelectColumnBuilder()
                                                 .withColumn("*")
                                 )
                                 .withTableName("flw")
                 )
                 .withAlias("flwareajoin")
                 .build();
     }
 
     private WhereCondition prepareAreaWhereCondition(AreaEntity area) {
         SelectColumn selectColumn = new SelectColumnBuilder()
                 .withColumn("flwareajoin", area.getLevel().getName())
                 .withValueToLowerCase(true)
                 .build();
 
         return new WhereConditionBuilder()
                 .withValueComparison(selectColumn, ComparisonType.Equal, area.getName())
                 .build();
     }
 
     private WhereCondition prepareSkipTestFlwRole() {
         SelectColumn selectColumn = new SelectColumnBuilder()
                 .withColumn("flwareajoin", "role")
                 .withValueToLowerCase(true)
                 .build();
 
         return new WhereConditionBuilder()
                 .withValueComparison(selectColumn, ComparisonType.NotEqual, "test")
                 .build();
     }
 
     public DwQueryDto dwQueryEntityToDto(DwQueryEntity dwQueryEntity) {
 
         DwQueryDto dwQueryDto = new DwQueryDto();
         dwQueryDto.setName(dwQueryEntity.getName());
         dwQueryDto.setDimension(dwQueryEntity.getTableName());
 
         if (dwQueryEntity.getWhereGroup() != null) {
             dwQueryDto.setWhereGroup(convertWhereGroupToDto(dwQueryEntity.getWhereGroup()));
         }
 
         if (dwQueryEntity.getCombination() != null && dwQueryEntity.getCombination().getDwQuery() != null) {
             DwQueryDto combineWith = dwQueryEntityToDto(dwQueryEntity.getCombination().getDwQuery());
             combineWith.setKey1(dwQueryEntity.getCombination().getReferencedKey());
             combineWith.setKey2(dwQueryEntity.getCombination().getForeignKey());
             combineWith.setJoinType(dwQueryEntity.getCombination().getType());
 
             dwQueryDto.setCombineWith(combineWith);
         }
 
         for (SelectColumnEntity selectColumnEntity : dwQueryEntity.getSelectColumns()) {
             dwQueryDto.getSelectColumns().add(new SelectColumnDto(selectColumnEntity.getComputedField(),
                     selectColumnEntity.getFunctionName(), selectColumnEntity.getNullValue()));
         }
 
         if (dwQueryEntity.getGroupedBy() != null) {
             HavingDto havingDto = (dwQueryEntity.getGroupedBy().getHaving() == null) ? null : new HavingDto(
                     dwQueryEntity.getGroupedBy().getHaving().getSelectColumnEntity().getFunctionName(),
                     dwQueryEntity.getGroupedBy().getHaving().getOperator(),
                     dwQueryEntity.getGroupedBy().getHaving().getValue());
 
             dwQueryDto.setGroupBy(new GroupByDto(dwQueryEntity.getGroupedBy().getComputedField(), havingDto));
         }
 
         return dwQueryDto;
     }
 
     private WhereGroupDto convertWhereGroupToDto(WhereGroupEntity whereGroupEntity) {
         WhereGroupDto whereGroupDto = new WhereGroupDto();
         whereGroupDto.setOperator(whereGroupEntity.getOperator());
 
         if (whereGroupEntity.getConditions() != null) {
             for (ConditionEntity condition : whereGroupEntity.getConditions()) {
                 whereGroupDto.getConditions().add(convertConditionToDto(condition));
             }
         }
 
         if (whereGroupEntity.getWhereGroups() != null) {
             for (WhereGroupEntity group : whereGroupEntity.getWhereGroups()) {
                 whereGroupDto.getGroups().add(convertWhereGroupToDto(group));
             }
         }
 
         return whereGroupDto;
     }
 
     private WhereConditionDto convertConditionToDto(ConditionEntity conditionEntity) {
         WhereConditionDto whereConditionDto = new WhereConditionDto();
         whereConditionDto.setType(conditionEntity.getType());
 
         switch (conditionEntity.getType()) {
             case ConditionType.FIELD_COMPARISON:
                 convertFieldComparisonConditionToDto((FieldComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.VALUE_COMPARISON:
                 convertValueComparisonConditionToDto((ValueComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.CALCULATION_END_DATE:
                 convertCalculationEndDateConditionToDto((CalculationEndDateConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.DATE_DIFF_COMPARISON:
                 convertDateDiffComparisonConditionToDto((DateDiffComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.DATE_RANGE_COMPARISON:
                 convertDateRangeComparisonConditionToDto((DateRangeComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.DATE_VALUE_COMPARISON:
                 convertDateValueComparisonConditionToDto((DateValueComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.ENUM_RANGE_COMPARISON:
                 convertEnumRangeComparisonConditionToDto((EnumRangeComparisonConditionEntity) conditionEntity, whereConditionDto);
                 break;
             case ConditionType.PERIOD:
                 convertPeriodComparisonConditionToDto((PeriodConditionEntity) conditionEntity, whereConditionDto);
                 break;
             default:
                 return null;
         }
 
         return whereConditionDto;
     }
 
     private void convertFieldComparisonConditionToDto(FieldComparisonConditionEntity condition,
                                                       WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         whereConditionDto.setField2(condition.getField2());
         whereConditionDto.setOffset1(condition.getOffset1());
         whereConditionDto.setOffset2(condition.getOffset2());
         whereConditionDto.setOperator(condition.getOperator().getName());
     }
 
     private void convertValueComparisonConditionToDto(ValueComparisonConditionEntity condition,
                                                       WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         whereConditionDto.setOperator(condition.getOperator().getName());
         whereConditionDto.setValue(condition.getValue());
     }
 
     private void convertCalculationEndDateConditionToDto(CalculationEndDateConditionEntity condition,
                                                          WhereConditionDto whereConditionDto) {
         ComputedFieldEntity computedFieldEntity = computedFieldService.getComputedFieldByFormAndFieldName(
                 condition.getTableName(), condition.getColumnName());
 
         whereConditionDto.setField1(computedFieldEntity);
         whereConditionDto.setOffset1(condition.getOffset());
     }
 
     private void convertDateDiffComparisonConditionToDto(DateDiffComparisonConditionEntity condition,
                                                          WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         whereConditionDto.setField2(condition.getField2());
         whereConditionDto.setOffset1(condition.getOffset1());
         whereConditionDto.setOffset2(condition.getOffset2());
         whereConditionDto.setOperator(condition.getOperator().getName());
         whereConditionDto.setValue(condition.getValue().toString());
     }
 
     private void convertDateRangeComparisonConditionToDto(DateRangeComparisonConditionEntity condition,
                                                           WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         whereConditionDto.setOffset1(condition.getOffset1());
         whereConditionDto.setDate1(condition.getDate1());
         whereConditionDto.setDate2(condition.getDate2());
     }
 
     private void convertDateValueComparisonConditionToDto(DateValueComparisonConditionEntity condition,
                                                           WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         whereConditionDto.setOffset1(condition.getOffset1());
         whereConditionDto.setOperator(condition.getOperator().getName());
         whereConditionDto.setValue(condition.getValue().toString());
     }
 
     private void convertEnumRangeComparisonConditionToDto(EnumRangeComparisonConditionEntity condition,
                                                           WhereConditionDto whereConditionDto) {
         whereConditionDto.setField1(condition.getField1());
         if (condition.getValues() != null) {
             whereConditionDto.setValues(new ArrayList<String>());
             for (EnumRangeComparisonConditionValueEntity value : condition.getValues()) {
                 whereConditionDto.getValues().add(value.getValue());
             }
         }
     }
 
     private void convertPeriodComparisonConditionToDto(PeriodConditionEntity condition,
                                                        WhereConditionDto whereConditionDto) {
         ComputedFieldEntity computedFieldEntity = computedFieldService.getComputedFieldByFormAndFieldName(
                 condition.getTableName(), condition.getColumnName());
 
         whereConditionDto.setField1(computedFieldEntity);
         whereConditionDto.setOffset1(condition.getOffset1());
         whereConditionDto.setOffset2(condition.getOffset2());
     }
 }
