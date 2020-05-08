 package ru.andreymarkelov.atlas.plugins.datacollector.customfield;
 
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
 import com.atlassian.jira.issue.customfields.SortableCustomField;
 import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
 import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.fields.config.FieldConfig;
 import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
 import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
 import com.atlassian.jira.util.NotNull;
 import com.atlassian.templaterenderer.TemplateRenderer;
 import com.atlassian.util.concurrent.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import ru.andreymarkelov.atlas.plugins.datacollector.CollectorUtils;
 import ru.andreymarkelov.atlas.plugins.datacollector.RendererHelper;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.IssueDataKeeper;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.StatusUsers;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.Statuses;
 import ru.andreymarkelov.atlas.plugins.datacollector.struct.Users;
 
 public class StatusesTimeSumCF extends CalculatedCFType<Long, Long> implements SortableCustomField<Long> {
    private final static Long ZERO = Long.valueOf(0L);
     private final StatusesTimeSumPluginData pluginData;
     private final TemplateRenderer renderer;
 
     public StatusesTimeSumCF(
             StatusesTimeSumPluginData pluginData,
             TemplateRenderer renderer) {
         this.pluginData = pluginData;
         this.renderer = renderer;
     }
 
     @Override
     public int compare(@NotNull Long val1, @NotNull Long val2, FieldConfig fc) {
         return val1.compareTo(val2);
     }
 
     @Override
     public List<FieldConfigItemType> getConfigurationItemTypes() {
         final List<FieldConfigItemType> configurationItemTypes = new ArrayList<FieldConfigItemType>();
         configurationItemTypes.add(new StatusesTimeSumCFConfig(renderer, pluginData));
         return configurationItemTypes;
     }
 
     private Double getFieldDoubleValue(CustomField field, Issue issue) {
         Object obj = field.getValue(issue);
         if (obj != null) {
             try {
                 return Double.parseDouble(obj.toString().replaceAll(",", "."));
             } catch (Exception ex) {
                 return Double.valueOf(0);
             }
         } else {
             return Double.valueOf(0);
         }
     }
 
     private long getIssueStatusesTime(CustomField field, Issue issue, StatusesTimeSumData data) {
         List<ChangeHistoryItem> items = ComponentAccessor.getChangeHistoryManager().getAllChangeItems(issue);
         Users users = new Users(CollectorUtils.getUserRanges(items, issue));
         Statuses statuses = new Statuses(CollectorUtils.getStatusRanges(items, issue));
         List<StatusUsers> statusUsers = CollectorUtils.getStatusUsers(users, statuses);
         statusUsers = CollectorUtils.reduceStatusUsers(statusUsers, data.getStatuses());
         return new IssueDataKeeper(issue.getKey(), issue.getSummary(), statusUsers).getTotalTime();
     }
 
     @Override
     public Long getSingularObjectFromString(String str) throws FieldValidationException {
         try {
             return Long.valueOf(str);
         } catch (Exception ex) {
            return ZERO;
         }
     }
 
     @Override
     public String getStringFromSingularObject(Long val) {
         if (val != null) {
             return new RendererHelper().renderSpentTime(val.longValue());
         } else {
             return "";
         }
     }
 
     @Override
     @Nullable
     public Long getValueFromIssue(CustomField field, Issue issue) {
         if (issue == null || issue.getKey() == null || field == null || field.getRelevantConfig(issue) == null) {
             return 0L;
         }
 
         StatusesTimeSumData data = pluginData.getJSONFieldData(field.getRelevantConfig(issue));
         return getIssueStatusesTime(field, issue, data);
     }
 
     @Override
     public Map<String, Object> getVelocityParameters(
             final Issue issue,
             final CustomField field,
             final FieldLayoutItem fieldLayoutItem) {
         final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
 
         if (issue == null || issue.getKey() == null || field == null || field.getRelevantConfig(issue) == null) {
             return map;
         }
 
         StatusesTimeSumData data = pluginData.getJSONFieldData(field.getRelevantConfig(issue));
         long realVal = getIssueStatusesTime(field, issue, data);
 
         if (data.getCompareField() != null) {
             CustomField cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(data.getCompareField());
             if (cf != null) {
                 Long planVal = Double.valueOf(getFieldDoubleValue(cf, issue).doubleValue() * 60 * 60).longValue();
                 String color = (realVal <= planVal) ? "green" : "red";
                 map.put("color", color);
             }
         }
 
        map.put("zero", ZERO);
         map.put("renderhelper", new RendererHelper());
         map.put("value", realVal);
 
         return map;
     }
 }
