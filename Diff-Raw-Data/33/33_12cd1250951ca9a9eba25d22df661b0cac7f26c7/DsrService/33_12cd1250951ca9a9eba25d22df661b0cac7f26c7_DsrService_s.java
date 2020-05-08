 package org.chai.kevin.dsr;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.EnumOption;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.LocationEntity;
 import org.chai.kevin.reports.ReportObjective;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.reports.ReportTarget;
 import org.chai.kevin.reports.ReportValue;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.DataValue;
 import org.chai.kevin.value.ValueService;
 import org.hisp.dhis.period.Period;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DsrService {
 	private static final Log log = LogFactory.getLog(DsrService.class);
 	
 	private LocationService locationService;
 	private ReportService reportService;
 	private ValueService valueService;
 	private DataService dataService;
 	private LanguageService languageService;
 	private String groupLevel;
 	
 	@Cacheable("dsrCache")
 	@Transactional(readOnly = true)
	public DsrTable getDsrTable(LocationEntity entity, ReportObjective objective, Period period, Set<DataEntityType> types) {
		if (log.isDebugEnabled())  log.debug("getDsrTable(period="+period+",entity="+entity+",objective="+objective+")");
						
		List<DataLocationEntity> facilities = locationService.getDataEntities(entity, types.toArray(new DataEntityType[types.size()]));
 		Map<LocationEntity, List<DataLocationEntity>> locationMap = reportService.getParents(facilities, locationService.findLocationLevelByCode(groupLevel));
 		
 		Map<DataLocationEntity, Map<DsrTarget, ReportValue>> valueMap = new HashMap<DataLocationEntity, Map<DsrTarget, ReportValue>>();
 		List<DsrTarget> targets = reportService.getReportTargets(DsrTarget.class, objective);
 		for (DataLocationEntity facility : facilities) {
 			Map<DsrTarget, ReportValue> targetMap = new HashMap<DsrTarget, ReportValue>();			
 			for (DsrTarget target : targets) {
 				targetMap.put(target, getDsrValue(target, facility, period));
 			}
 			valueMap.put(facility, targetMap);
 		}
 		
		DsrTable dsrTable = new DsrTable(valueMap, targets, locationMap);
 		if (log.isDebugEnabled()) log.debug("getDsrTable(...)="+dsrTable);
 		return dsrTable;
 	}
 
 
 	private ReportValue getDsrValue(DsrTarget target, DataLocationEntity facility, Period period){
 		String value = null;
 		
 		Set<String> targetUuids = Utils.split(target.getTypeCodeString());
 		if (targetUuids.contains(facility.getType().getCode())) {
 			DataValue dataValue = valueService.getDataElementValue(target.getDataElement(), facility, period);
 			
 			if (dataValue != null && !dataValue.getValue().isNull()) {
 				// TODO put this in templates ?
 				switch (target.getDataElement().getType().getType()) {
 				case BOOL:
 					if (dataValue.getValue().getBooleanValue()) value = "&#10003;";
 					else value = "";
 					break;
 				case STRING:
 					value = dataValue.getValue().getStringValue();
 					break;
 				case NUMBER:
 					value = getFormat(target, dataValue.getValue().getNumberValue().doubleValue());
 					break;
 				case ENUM:
 					String code = target.getDataElement().getType().getEnumCode();
 					Enum enume = dataService.findEnumByCode(code);
 					if (enume != null) {
 						EnumOption option = enume.getOptionForValue(dataValue.getValue().getEnumValue());
 						if (option != null) value = languageService.getText(option.getNames());
 						else value = dataValue.getValue().getEnumValue();
 					}
 					else value = "";
 					break;
 				default:
 					value = "";
 					break;
 				}
 			}
 		}
 		return new ReportValue(value);
 	}
 
 	
 	private static String getFormat(DsrTarget target, Double value) {
 		String format = target.getFormat();
 		if (format == null) format = "#";
 		
 		DecimalFormat frmt = new DecimalFormat(format);
 		return frmt.format(value).toString();
 
 	}
 
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 	
 	public void setGroupLevel(String groupLevel) {
 		this.groupLevel = groupLevel;
 	}
 	
 }
