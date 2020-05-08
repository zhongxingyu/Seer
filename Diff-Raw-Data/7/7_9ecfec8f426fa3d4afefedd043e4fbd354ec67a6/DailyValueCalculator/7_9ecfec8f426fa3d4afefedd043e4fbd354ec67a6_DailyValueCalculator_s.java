 package org.motechproject.carereporting.indicator;
 
 import org.apache.log4j.Logger;
 import org.dwQueryBuilder.builders.QueryBuilder;
 import org.dwQueryBuilder.data.queries.DwQuery;
 import org.jooq.SQLDialect;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.math.BigDecimal;
 import java.util.Date;
 
 @Component
 public class DailyValueCalculator extends IndicatorValueCalculator {
 
     private static final Logger LOG = Logger.getLogger(IndicatorCalculator.class);
 
     private static final SQLDialect SQL_DIALECT = SQLDialect.POSTGRES;
 
     @Value("${care.jdbc.schema}")
     private String schemaName;
 
     @Resource(name = "careDataSource")
     private DataSource careDataSource;
 
     private DwQueryHelper dwQueryHelper = new DwQueryHelper();
 
     @Override
     protected IndicatorValueEntity calculateIndicatorValueForArea(IndicatorEntity indicator, FrequencyEntity frequency, AreaEntity area, Date from, Date to) {
         BigDecimal numeratorValue = calculateDwQueryValue(indicator.getNumerator(), area, from, to);
         BigDecimal denominatorValue = BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;
         if (indicator.getDenominator() != null) {
             result = calculateDwQueryValue(indicator.getDenominator(), area, from, to);
         }
 
        if (result == null || result.equals(BigDecimal.ZERO)) {
             numeratorValue = BigDecimal.ZERO;
             denominatorValue = BigDecimal.ONE;
         } else {
             denominatorValue = result;
         }
 
         return prepareIndicatorValueEntity(numeratorValue, denominatorValue);
     }
 
     private BigDecimal calculateDwQueryValue(DwQueryEntity dwQueryEntity, AreaEntity area, Date from, Date to) {
         DwQuery query = dwQueryHelper.buildDwQuery(dwQueryEntity, area);
         String sqlQuery = QueryBuilder.getDwQueryAsSQLString(SQL_DIALECT,
                 schemaName, query, false);
         sqlQuery = dwQueryHelper.formatFromDateAndToDate(sqlQuery, from, to);
         return executeQuery(sqlQuery);
     }
 
     private BigDecimal executeQuery(String query) {
         JdbcTemplate jdbcTemplate = new JdbcTemplate(careDataSource);
         LOG.debug("Executing reporting db query: " + query);
 
         BigDecimal result = BigDecimal.ZERO;
         SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
         if (sqlRowSet.next() && !sqlRowSet.wasNull()) {
             BigDecimal tmpResult = sqlRowSet.getBigDecimal(1);
             if (tmpResult != null) {
                 result = tmpResult;
             }
         }
 
         LOG.debug("Query executed, the result is: " + result.toString());
         return result;
     }
 
 }
