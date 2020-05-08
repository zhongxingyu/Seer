 package org.motechproject.carereporting.indicator;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.ConditionEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionValueEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 import org.motechproject.carereporting.service.AreaService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public abstract class IndicatorValueCalculator {
 
     private static final int SCALE = 4;
     private static final Integer SUPER_USER_AREA_ID = 1;
 
     @Autowired
     private IndicatorService indicatorService;
 
     @Autowired
     private AreaService areaService;
 
     protected IndicatorService getIndicatorService() {
         return indicatorService;
     }
 
     public void calculateAndPersistIndicatorValue(IndicatorEntity indicator, FrequencyEntity frequency, Date from, Date to) {
         for (AreaEntity area : areaService.getAllAreas()) {
             if (area.getId().equals(SUPER_USER_AREA_ID)) {
                     continue;
             }
             if (indicator.isCategorized()) {
                 EnumRangeComparisonConditionEntity enumRangeCondition = getEnumCategoriesForIndicator(indicator);
                 List<IndicatorValueEntity> values = calculateCategorizedIndicatorValues(indicator, enumRangeCondition, frequency, from, to, area);
                 for (IndicatorValueEntity value : values) {
                     persistIndicatorValue(value);
                 }
             } else {
                 IndicatorValueEntity value = calculateIndicatorValue(indicator, frequency, from, to, area, null);
                 persistIndicatorValue(value);
             }
         }
     }
 
     private List<IndicatorValueEntity> calculateCategorizedIndicatorValues(IndicatorEntity indicator,
                                                                            EnumRangeComparisonConditionEntity enumCategories,
                                                                            FrequencyEntity frequency, Date from, Date to,
                                                                            AreaEntity area) {
         List<IndicatorValueEntity> values = new ArrayList<>();
         Set<EnumRangeComparisonConditionValueEntity> enumValues = new HashSet<>(enumCategories.getValues());
         for (EnumRangeComparisonConditionValueEntity enumValue : enumValues) {
             enumCategories.getValues().clear();
             enumCategories.getValues().add(enumValue);
             values.add(calculateIndicatorValue(indicator, frequency, from, to, area, enumValue.getValue()));
         }
         enumCategories.setValues(enumValues);
         return values;
     }
 
     private IndicatorValueEntity calculateIndicatorValue(IndicatorEntity indicator, FrequencyEntity frequency, Date from, Date to, AreaEntity area, String category) {
         IndicatorValueEntity value = calculateIndicatorValueForArea(indicator, frequency, area, from, to, category);
         value.setArea(area);
         value.setDate(DateUtils.addSeconds(to, -1));
         value.setFrequency(frequency);
         value.setIndicator(indicator);
         value.setCategory(category);
 
         if (isPercentageIndicator(indicator)) {
             value.setValue(BigDecimal.valueOf(100).multiply(value.getValue()));
         }
 
         return value;
     }
 
     private EnumRangeComparisonConditionEntity getEnumCategoriesForIndicator(IndicatorEntity indicator) {
         return getEnumCategoriesForDwQuery(indicator.getNumerator());
     }
 
     private EnumRangeComparisonConditionEntity getEnumCategoriesForDwQuery(DwQueryEntity dwQuery) {
         if (dwQuery.getWhereGroup() != null) {
             EnumRangeComparisonConditionEntity condition = getEnumCategoriesForWhereGroup(dwQuery.getWhereGroup());
             if (condition != null) {
                 return condition;
             }
         }
         return getEnumCategoriesForDwQuery(dwQuery.getCombination().getDwQuery());
     }
 
     private EnumRangeComparisonConditionEntity getEnumCategoriesForWhereGroup(WhereGroupEntity whereGroup) {
         for (ConditionEntity condition : whereGroup.getConditions()) {
             if (condition instanceof EnumRangeComparisonConditionEntity) {
                 return (EnumRangeComparisonConditionEntity) condition;
             }
         }
         for (WhereGroupEntity whereGroupChild : whereGroup.getWhereGroups()) {
            return getEnumCategoriesForWhereGroup(whereGroupChild);
         }
         return null;
     }
 
     private boolean isPercentageIndicator(IndicatorEntity indicator) {
         return indicator.getName().startsWith("%");
     }
 
     protected IndicatorValueEntity prepareIndicatorValueEntity(BigDecimal numeratorValue, BigDecimal denominatorValue) {
         IndicatorValueEntity value = new IndicatorValueEntity();
         value.setNumerator(numeratorValue);
         value.setDenominator(denominatorValue);
 
         BigDecimal indicatorValue = numeratorValue.divide(denominatorValue, SCALE, RoundingMode.HALF_UP);
 
         value.setValue(indicatorValue);
         return value;
     }
 
     protected void persistIndicatorValue(IndicatorValueEntity value) {
         indicatorService.createNewIndicatorValue(value);
     }
 
     protected abstract IndicatorValueEntity calculateIndicatorValueForArea(IndicatorEntity indicator, FrequencyEntity frequency, AreaEntity area, Date from, Date to,
                                                                            String category);
 
 }
