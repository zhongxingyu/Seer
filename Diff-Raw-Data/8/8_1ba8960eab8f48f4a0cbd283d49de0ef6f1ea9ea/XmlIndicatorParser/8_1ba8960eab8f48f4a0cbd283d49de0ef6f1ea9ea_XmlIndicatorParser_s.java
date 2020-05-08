 package org.motechproject.carereporting.xml;
 
 import org.motechproject.carereporting.dao.ComparisonSymbolDao;
 import org.motechproject.carereporting.dao.ComputedFieldDao;
 import org.motechproject.carereporting.dao.DwQueryDao;
 import org.motechproject.carereporting.dao.FormDao;
 import org.motechproject.carereporting.dao.FrequencyDao;
 import org.motechproject.carereporting.dao.IndicatorClassificationDao;
 import org.motechproject.carereporting.dao.IndicatorDao;
 import org.motechproject.carereporting.dao.LevelDao;
 import org.motechproject.carereporting.dao.ReportTypeDao;
 import org.motechproject.carereporting.dao.RoleDao;
 import org.motechproject.carereporting.dao.UserDao;
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
 import org.motechproject.carereporting.domain.FormEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.GroupedByEntity;
 import org.motechproject.carereporting.domain.HavingEntity;
 import org.motechproject.carereporting.domain.IndicatorClassificationEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.LevelEntity;
 import org.motechproject.carereporting.domain.OrderByEntity;
 import org.motechproject.carereporting.domain.PeriodConditionEntity;
 import org.motechproject.carereporting.domain.ReportEntity;
 import org.motechproject.carereporting.domain.RoleEntity;
 import org.motechproject.carereporting.domain.SelectColumnEntity;
 import org.motechproject.carereporting.domain.UserEntity;
 import org.motechproject.carereporting.domain.ValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 import org.motechproject.carereporting.exception.CareRuntimeException;
 import org.motechproject.carereporting.service.ComputedFieldService;
 import org.motechproject.carereporting.xml.mapping.indicators.Classification;
 import org.motechproject.carereporting.xml.mapping.indicators.CombineWith;
 import org.motechproject.carereporting.xml.mapping.indicators.DwQuery;
 import org.motechproject.carereporting.xml.mapping.indicators.GroupBy;
 import org.motechproject.carereporting.xml.mapping.indicators.Indicator;
 import org.motechproject.carereporting.xml.mapping.indicators.OrderBy;
 import org.motechproject.carereporting.xml.mapping.indicators.Query;
 import org.motechproject.carereporting.xml.mapping.indicators.Report;
 import org.motechproject.carereporting.xml.mapping.indicators.Role;
 import org.motechproject.carereporting.xml.mapping.indicators.SelectColumn;
 import org.motechproject.carereporting.xml.mapping.indicators.User;
 import org.motechproject.carereporting.xml.mapping.indicators.WhereCondition;
 import org.motechproject.carereporting.xml.mapping.indicators.WhereGroup;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 import org.xml.sax.SAXException;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 @Component
 @Transactional(readOnly = true)
 @SuppressWarnings("PMD.AvoidDuplicateLiterals")
 public class XmlIndicatorParser {
 
     @Autowired
     private IndicatorDao indicatorDao;
 
     @Autowired
     private LevelDao levelDao;
 
     @Autowired
     private IndicatorClassificationDao indicatorClassificationDao;
 
     @Autowired
     private ComparisonSymbolDao comparisonSymbolDao;
 
     @Autowired
     private ComputedFieldDao computedFieldDao;
 
     @Autowired
     private ComputedFieldService computedFieldService;
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     private RoleDao roleDao;
 
     @Autowired
     private FrequencyDao frequencyDao;
 
     @Autowired
     private ReportTypeDao reportTypeDao;
 
     @Autowired
     private FormDao formDao;
 
     @Autowired
     private DwQueryDao dwQueryDao;
 
     private static final String ALL_ROLES_STRING = "ALL";
     private static final String WILDCARD = "*";
 
     @Transactional
     public IndicatorEntity parse(InputStream is) throws JAXBException {
         JAXBContext context = JAXBContext.newInstance(Indicator.class);
         Unmarshaller unmarshaller = context.createUnmarshaller();
         unmarshaller.setSchema(getSchema());
         Indicator indicator = (Indicator) unmarshaller.unmarshal(is);
         return createIndicatorEntityFromXmlIndicator(indicator);
     }
 
     private Schema getSchema() {
         try {
             Resource schemaFile = new ClassPathResource("indicator.xsd");
             return SchemaFactory
                     .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                     .newSchema(schemaFile.getFile());
         } catch (IOException | SAXException e) {
             throw new CareRuntimeException("Cannot open indicator schema file.", e);
         }
     }
 
     protected IndicatorEntity createIndicatorEntityFromXmlIndicator(Indicator indicator) {
         IndicatorEntity indicatorEntity = new IndicatorEntity();
         indicatorEntity.setName(indicator.getName());
         if (indicator.getOwners().getUser() != null) {
             indicatorEntity.setOwner(prepareOwner(indicator.getOwners().getUser()));
         }
         if (indicator.getOwners().getRoles() != null) {
             indicatorEntity.setRoles(prepareRoles(indicator.getOwners().getRoles()));
         }
         indicatorEntity.setAreaLevel(findAreaLevelByLevelName(indicator.getArea().getLevel().toString()));
         indicatorEntity.setClassifications(prepareIndicatorClassifications(indicator.getClassifications()));
         indicatorEntity.setDefaultFrequency(findFrequencyById(indicator.getDefaultFrequency().getValue()));
         DwQueryEntity num = dwQueryDao.getByField("name", getNameWithPrefix(indicatorEntity, "num", 0));
         if (num != null) {
             indicatorEntity.setNumerator(num);
         } else {
             indicatorEntity.setNumerator(prepareQuery(indicator.getNumerator()));
             updateIndicatorQueryNamesAndParents(indicatorEntity, indicatorEntity.getNumerator(), "num", 0);
         }
         indicatorEntity.setTrend(indicator.getTrend());
         indicatorEntity.setReports(prepareReports(indicator.getReports()));
         indicatorEntity.setAdditive(indicator.getAdditive());
         indicatorEntity.setCategorized(indicator.isCategorized());
         for (ReportEntity report: indicatorEntity.getReports()) {
             report.setIndicator(indicatorEntity);
         }
         if (indicator.getDenominator() != null) {
             DwQueryEntity denom = dwQueryDao.getByField("name", getNameWithPrefix(indicatorEntity, "denom", 0));
             if (denom != null) {
                 indicatorEntity.setDenominator(denom);
             } else {
                 indicatorEntity.setDenominator(prepareQuery(indicator.getDenominator()));
                 updateIndicatorQueryNamesAndParents(indicatorEntity, indicatorEntity.getDenominator(), "denom", 0);
             }
         }
 
         return indicatorEntity;
     }
 
     private void updateIndicatorQueryNamesAndParents(IndicatorEntity indicatorEntity, DwQueryEntity dwQueryEntity, String prefix, Integer index) {
         dwQueryEntity.setName(getNameWithPrefix(indicatorEntity, prefix, index));
         if (dwQueryEntity.getCombination() != null) {
             DwQueryEntity query = dwQueryEntity.getCombination().getDwQuery();
             query.setParentQuery(dwQueryEntity);
             updateIndicatorQueryNamesAndParents(indicatorEntity, query, prefix, index + 1);
         }
     }
 
     private String getNameWithPrefix(IndicatorEntity indicatorEntity, String prefix, Integer index) {
         return indicatorEntity.getName() + "_" + prefix + (index != 0 ? index : "");
     }
 
     private LevelEntity findAreaLevelByLevelName(String levelName) {
         return levelDao.getByField("name", levelName.toLowerCase());
     }
 
     private FrequencyEntity findFrequencyById(int id) {
         return frequencyDao.getById(id);
     }
 
     private UserEntity prepareOwner(User user) {
         return userDao.getByField("username", user.getLogin());
     }
 
     private Set<RoleEntity> prepareRoles(List<Role> roles) {
         Set<RoleEntity> roleEntities = new HashSet<>();
         for (Role role: roles) {
             if (role.getName().equals(ALL_ROLES_STRING)) {
                 roleEntities.addAll(roleDao.getAll());
             } else {
                 roleEntities.add(roleDao.getByField("name", role.getName()));
             }
         }
         return roleEntities;
     }
 
     private Set<IndicatorClassificationEntity> prepareIndicatorClassifications(List<Classification> classifications) {
         Set<IndicatorClassificationEntity> indicatorClassifications = new HashSet<>();
         for (Classification classification: classifications) {
             IndicatorClassificationEntity indicatorClassification = indicatorClassificationDao.getByField("name", classification.getName());
             indicatorClassifications.add(indicatorClassification);
         }
         return indicatorClassifications;
     }
 
     private Set<ReportEntity> prepareReports(List<Report> reports) {
         Set<ReportEntity> reportEntities = new HashSet<>();
         if (reports != null) {
             for (Report report: reports) {
                 reportEntities.add(prepareReport(report));
             }
         }
         return reportEntities;
     }
 
     private ReportEntity prepareReport(Report report) {
         ReportEntity reportEntity = new ReportEntity();
         reportEntity.setReportType(reportTypeDao.getByField("name", report.getType()));
         reportEntity.setLabelX(report.getLabelX());
         reportEntity.setLabelY(report.getLabelY());
         return reportEntity;
     }
 
     private Set<ConditionEntity> prepareWhereConditions(List<WhereCondition> whereConditions) {
         Set<ConditionEntity> conditions = new LinkedHashSet<>();
         for (WhereCondition condition: whereConditions) {
             ConditionEntity conditionEntity = createCondition(condition);
             conditions.add(conditionEntity);
         }
         return conditions;
     }
 
     private ConditionEntity createCondition(WhereCondition condition) {
         ConditionEntity conditionEntity = null;
         switch (condition.getType()) {
             case VALUE_COMPARISON:
                 conditionEntity = createValueComparisonCondition(condition);
                 break;
             case DATE_DIFF:
                 conditionEntity = createDateDiffComparisonCondition(condition);
                 break;
             case FIELD_COMPARISON:
                 conditionEntity = createFieldComparisonCondition(condition);
                 break;
             case PERIOD:
                 conditionEntity = createPeriodCondition(condition);
                 break;
             case CALCULATE_VALUES_START_DATE:
                 conditionEntity = createCalculationEndDateCondition(condition);
                 break;
             case DATE_WITH_OFFSET_DIFF:
                 conditionEntity = createDateDiffWithOffsetComparisonCondition(condition);
                 break;
             case DATE_RANGE:
                 conditionEntity = createDateRangeComparisonCondition(condition);
                 break;
             case DATE_VALUE:
                 conditionEntity = createDateValueComparisonCondition(condition);
                 break;
             case ENUM_RANGE:
                 conditionEntity = createEnumRangeComparisonCondition(condition);
                 break;
             default:
         }
 
         addField1ToConditionEntity(condition, conditionEntity);
         return conditionEntity;
     }
 
     private void addField1ToConditionEntity(WhereCondition condition, ConditionEntity conditionEntity) {
         Map<String, Object> findBy = new HashMap<>();
         findBy.put("name", condition.getField());
         FormEntity form = formDao.getByField("tableName", condition.getTableName());
         findBy.put("form", form);
         ComputedFieldEntity field = computedFieldDao.getByFields(findBy);
         if (field == null) {
             throw new CareRuntimeException("This indicator depends on computed field: " +
                     condition.getTableName() + "." + condition.getField() + ". Please, define this computed field first.");
         }
         conditionEntity.setField1(field);
     }
 
     private ConditionEntity createValueComparisonCondition(WhereCondition condition) {
         ValueComparisonConditionEntity conditionEntity = new ValueComparisonConditionEntity();
         conditionEntity.setOperator(comparisonSymbolDao.getByField("name", condition.getOperator()));
         conditionEntity.setValue(condition.getValue());
         return conditionEntity;
     }
 
     private ConditionEntity createDateDiffComparisonCondition(WhereCondition condition) {
         DateDiffComparisonConditionEntity dateDiffComparisonConditionEntity = new DateDiffComparisonConditionEntity();
         dateDiffComparisonConditionEntity.setField2(computedFieldDao.getByField("name", condition.getSecondField()));
         dateDiffComparisonConditionEntity.setValue(Integer.valueOf(condition.getValue()));
         return dateDiffComparisonConditionEntity;
     }
 
     private ConditionEntity createDateDiffWithOffsetComparisonCondition(WhereCondition condition) {
         DateDiffComparisonConditionEntity dateDiffComparisonConditionEntity = new DateDiffComparisonConditionEntity();
         dateDiffComparisonConditionEntity.setField2(computedFieldDao.getByField("name", condition.getSecondField()));
         dateDiffComparisonConditionEntity.setValue(Integer.valueOf(condition.getValue()));
         dateDiffComparisonConditionEntity.setOffset1(condition.getOffset1());
         dateDiffComparisonConditionEntity.setOffset2(condition.getOffset2());
         return dateDiffComparisonConditionEntity;
     }
 
     private ConditionEntity createDateRangeComparisonCondition(WhereCondition condition) {
         Integer offset = (condition.getOffset1() != null) ? condition.getOffset1() : 0;
 
         DateRangeComparisonConditionEntity dateRangeComparisonConditionEntity = new DateRangeComparisonConditionEntity();
         dateRangeComparisonConditionEntity.setDate1(condition.getDate1());
         dateRangeComparisonConditionEntity.setDate2(condition.getDate2());
         dateRangeComparisonConditionEntity.setOffset1(offset);
         return dateRangeComparisonConditionEntity;
     }
 
     private ConditionEntity createDateValueComparisonCondition(WhereCondition condition) {
         DateValueComparisonConditionEntity dateValueComparisonConditionEntity = new DateValueComparisonConditionEntity();
         dateValueComparisonConditionEntity.setOperator(comparisonSymbolDao.getByField("name", condition.getOperator()));
         dateValueComparisonConditionEntity.setOffset1(condition.getOffset1());
         dateValueComparisonConditionEntity.setValue(Date.valueOf(condition.getValue()));
         return dateValueComparisonConditionEntity;
     }
 
     private ConditionEntity createFieldComparisonCondition(WhereCondition condition) {
         FieldComparisonConditionEntity fieldComparisonConditionEntity = new FieldComparisonConditionEntity();
         Map<String, Object> fields = new HashMap<>();
         fields.put("name", condition.getSecondField());
         fields.put("form", formDao.getByField("tableName", condition.getTableName()));
 
         fieldComparisonConditionEntity.setOperator(comparisonSymbolDao.getByField("name", condition.getOperator()));
         fieldComparisonConditionEntity.setField2(computedFieldDao.getByFields(fields));
         fieldComparisonConditionEntity.setOffset1(condition.getOffset1());
         fieldComparisonConditionEntity.setOffset2(condition.getOffset2());
         return fieldComparisonConditionEntity;
     }
 
     private ConditionEntity createEnumRangeComparisonCondition(WhereCondition condition) {
         EnumRangeComparisonConditionEntity enumRangeComparisonConditionEntity = new EnumRangeComparisonConditionEntity();
         Set<EnumRangeComparisonConditionValueEntity> values = new LinkedHashSet<>();
 
         for (String value : condition.getValues()) {
             EnumRangeComparisonConditionValueEntity enumValue = new EnumRangeComparisonConditionValueEntity(value);
             //enumValue.setCondition(enumRangeComparisonConditionEntity);
             values.add(enumValue);
         }
         enumRangeComparisonConditionEntity.setValues(values);
 
         return enumRangeComparisonConditionEntity;
     }
 
     private ConditionEntity createPeriodCondition(WhereCondition condition) {
         PeriodConditionEntity periodConditionEntity = new PeriodConditionEntity();
         periodConditionEntity.setColumnName(condition.getField());
         periodConditionEntity.setOffset1(condition.getOffset1() != null ? condition.getOffset1() : 0);
         periodConditionEntity.setOffset2(condition.getOffset2() != null ? condition.getOffset2() : 0);
         periodConditionEntity.setTableName(condition.getTableName());
         return periodConditionEntity;
     }
 
     private ConditionEntity createCalculationEndDateCondition(WhereCondition condition) {
         CalculationEndDateConditionEntity conditionEntity = new CalculationEndDateConditionEntity();
         conditionEntity.setTableName(condition.getTableName());
         conditionEntity.setColumnName(condition.getField());
         conditionEntity.setOffset(condition.getOffset1());
         return conditionEntity;
     }
 
     private DwQueryEntity prepareQuery(Query query) {
         if (query.getIndicatorName() != null) {
             IndicatorEntity indicator = indicatorDao.getIndicatorByName(query.getIndicatorName());
             if (indicator == null) {
                 throw new CareRuntimeException("This indicator depends on indicator '" +
                         query.getIndicatorName() + "' which has not been added yet. Please, add this indicator first.");
             }
 
             return indicator.getNumerator();
         } else {
             return prepareDwQuery(query.getDwQuery());
         }
     }
 
     private DwQueryEntity prepareDwQuery(DwQuery dwQuery) {
         DwQueryEntity dwQueryEntity = new DwQueryEntity();
         dwQueryEntity.setTableName(dwQuery.getDimension().getName());
 
         if (dwQuery.getGroupBy() != null) {
             GroupedByEntity groupedByEntity = prepareGroupBy(dwQuery);
             dwQueryEntity.setGroupedBy(groupedByEntity);
         }
         if (dwQuery.getWhereGroup() != null) {
             WhereGroupEntity whereGroup = prepareWhereGroup(dwQuery.getWhereGroup());
             dwQueryEntity.setWhereGroup(whereGroup);
             dwQueryEntity.setHasPeriodCondition(hasPeriodCondition(whereGroup));
         }
         dwQueryEntity.setSelectColumns(new LinkedHashSet<SelectColumnEntity>());
         for (SelectColumn selectColumn: dwQuery.getSelectColumns()) {
             dwQueryEntity.getSelectColumns().add(prepareSelectColumn(selectColumn, null));
         }
         if (dwQuery.getCombineWith() != null) {
             dwQueryEntity.setCombination(prepareCombination(dwQuery.getCombineWith()));
         }
         if (dwQuery.getOrderBy() != null && dwQuery.getOrderBy().size() > 0) {
             dwQueryEntity.setOrderBy(prepareOrderBy(dwQueryEntity, dwQuery.getOrderBy()));
         }
         dwQueryEntity.setLimit(dwQuery.getLimit());
 
         return dwQueryEntity;
     }
 
     private Set<OrderByEntity> prepareOrderBy(DwQueryEntity dwQueryEntity, List<OrderBy> orderByList) {
         Set<OrderByEntity> orderByEntitySet = new LinkedHashSet<>();
         for (OrderBy orderBy : orderByList) {
             OrderByEntity orderByEntity = new OrderByEntity();
             orderByEntity.setDwQuery(dwQueryEntity);
             orderByEntity.setSelectColumn(prepareSelectColumn(orderBy.getSelectColumn(), null));
             orderByEntity.setType(orderBy.getType());
             orderByEntitySet.add(orderByEntity);
         }
 
         return orderByEntitySet;
     }
 
     private CombinationEntity prepareCombination(CombineWith combineWith) {
         CombinationEntity combination = new CombinationEntity();
         combination.setDwQuery(prepareQuery(combineWith));
         combination.setForeignKey(combineWith.getForeignKey());
         combination.setReferencedKey(combineWith.getDimensionKey());
         combination.setType(combineWith.getType());
         return combination;
     }
 
     private boolean hasPeriodCondition(WhereGroupEntity whereGroup) {
         for (ConditionEntity condition: whereGroup.getConditions()) {
             if (condition instanceof PeriodConditionEntity) {
                 return true;
             }
         }
         for (WhereGroupEntity whereGroupEntity: whereGroup.getWhereGroups()) {
             if (hasPeriodCondition(whereGroupEntity)) {
                 return true;
             }
         }
         return false;
     }
 
     private SelectColumnEntity prepareSelectColumn(SelectColumn selectColumn, String defaultTableName) {
         if (StringUtils.isEmpty(selectColumn.getTableName())) {
             selectColumn.setTableName(defaultTableName);
         }
         SelectColumnEntity selectColumnEntity = new SelectColumnEntity();
         selectColumnEntity.setFunctionName(selectColumn.getAggregation());
         Map<String, Object> findBy = new HashMap<>();
         findBy.put("name", selectColumn.getFieldName());
         FormEntity form = formDao.getByField("tableName", selectColumn.getTableName());
         findBy.put("form", form);
         ComputedFieldEntity field = computedFieldDao.getByFields(findBy);
         selectColumnEntity.setComputedField(field);
         return selectColumnEntity;
     }
 
     private WhereGroupEntity prepareWhereGroup(WhereGroup whereGroup) {
         WhereGroupEntity whereGroupEntity = new WhereGroupEntity();
         if (whereGroup.getConditions() != null) {
             whereGroupEntity.setConditions(prepareWhereConditions(whereGroup.getConditions()));
         }
         if (whereGroup.getGroups() != null) {
             whereGroupEntity.setWhereGroups(prepareWhereGroups(whereGroup.getGroups()));
         }
         whereGroupEntity.setOperator(whereGroup.getJoin());
         return whereGroupEntity;
     }
 
     private Set<WhereGroupEntity> prepareWhereGroups(List<WhereGroup> whereGroups) {
         Set<WhereGroupEntity> whereGroupEntities = new LinkedHashSet<>();
         for (WhereGroup whereGroup: whereGroups) {
             whereGroupEntities.add(prepareWhereGroup(whereGroup));
         }
         return whereGroupEntities;
     }
 
     private GroupedByEntity prepareGroupBy(DwQuery dwQuery) {
         GroupedByEntity groupedByEntity = new GroupedByEntity();
         groupedByEntity.setComputedField(computedFieldService.getComputedFieldByFormAndFieldName(
                 dwQuery.getGroupBy().getTableName(), dwQuery.getGroupBy().getFieldName()));
        if (groupedByEntity.getHaving() != null) {
             groupedByEntity.setHaving(prepareHaving(dwQuery.getGroupBy()));
         }
         return groupedByEntity;
     }
 
     private HavingEntity prepareHaving(GroupBy groupBy) {
         HavingEntity having = new HavingEntity();
         having.setOperator(groupBy.getHaving().getOperator().getValue());
         having.setValue(groupBy.getHaving().getValue());
         SelectColumnEntity selectCol = new SelectColumnEntity();
         selectCol.setFunctionName(groupBy.getHaving().getFunction().toString());
 
         if (isNotBlank(groupBy.getHaving().getTableName()) && isNotBlank(groupBy.getHaving().getFieldName())) {
             String fieldName = (groupBy.getHaving().getFieldName() == null) ? WILDCARD : groupBy.getHaving().getFieldName();
 
             selectCol.setComputedField(computedFieldService.getComputedFieldByFormAndFieldName(
                     groupBy.getHaving().getTableName(), fieldName));
         }
 
         having.setSelectColumnEntity(selectCol);
         return having;
     }
 
 }
