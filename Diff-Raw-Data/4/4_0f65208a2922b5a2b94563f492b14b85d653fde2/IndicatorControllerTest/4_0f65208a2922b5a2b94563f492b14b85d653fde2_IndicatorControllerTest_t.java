 package org.motechproject.carereporting.web.controller;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.carereporting.domain.CronTaskEntity;
 import org.motechproject.carereporting.domain.IndicatorClassificationEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorTypeEntity;
 import org.motechproject.carereporting.domain.dto.IndicatorDto;
 import org.motechproject.carereporting.service.CronService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 @RunWith(MockitoJUnitRunner.class)
 public class IndicatorControllerTest {
 
     private static final String CREATE_INDICATOR_JSON = "{\"level\":1,\"frequency\":\"1\",\"name\":\"name\",\"owners\":[1],\"classifications\":[1],\"reports\":[{\"reportType\":{\"id\":1,\"name\":\"Bar Chart\"}}]}";
     private static final String CREATE_INDICATOR_JSON_NO_NAME = "{\"values\":[],\"area\":1,\"frequency\":\"30\",\"indicatorType\":3,\"complexCondition\":1,\"computedField\":399,\"trend\":2,\"owners\":[1],\"classifications\":[1],\"reports\":[{\"reportType\":{\"id\":1,\"name\":\"Bar Chart\"}}]}";
     private static final String UPDATE_INDICATOR_JSON = "{\"values\":[],\"area\":1,\"frequency\":30,\"indicatorType\":3,\"complexCondition\":1,\"id\":1,\"name\":\"new name\",\"computedField\":453,\"reports\":[{\"reportType\":{\"id\":3,\"name\":\"Pie Chart\"},\"id\":1}],\"trend\":3,\"owners\":[1],\"classifications\":[2]}";
     private static final String CREATE_CLASSIFICATION_JSON = "{\"name\":\"Name\"}";
     private static final String UPDATE_CLASSIFICATION_JSON = "{\"name\":\"New name\"}";
 
     @Mock
     private IndicatorService indicatorService;
 
     @Mock
     private CronService cronService;
 
     @InjectMocks
     private IndicatorController indicatorController = new IndicatorController();
     
     private MockMvc mockMvc;
     
     @Before
     public void setup() throws Exception {
         mockMvc = MockMvcBuilders.standaloneSetup(indicatorController).build();
     }
 
     @Test
     public void testGetIndicators() throws Exception {
         String indicatorName = "test indicator";
         Integer indicatorId = 1;
 
         Set<IndicatorEntity> indicators = new LinkedHashSet<IndicatorEntity>();
         IndicatorEntity indicator = new IndicatorEntity();
         indicator.setName(indicatorName);
         indicator.setId(indicatorId);
         indicators.add(indicator);
         Mockito.when(indicatorService.getAllIndicators()).thenReturn(indicators);
 
         mockMvc.perform(get("/api/indicator"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].name").value(indicatorName))
                 .andExpect(jsonPath("$[0].id").value(indicatorId));
 
         verify(indicatorService, times(1)).getAllIndicators();
     }
 
     @Test
     public void testGetIndicatorsByClassificationId() throws Exception {
         String indicatorName = "test indicator";
         Integer indicatorId = 1;
         Integer classificationId = 1;
 
         Set<IndicatorEntity> indicators = new LinkedHashSet<IndicatorEntity>();
         IndicatorEntity indicator = new IndicatorEntity();
         indicator.setName(indicatorName);
         indicator.setId(indicatorId);
         indicators.add(indicator);
         Mockito.when(indicatorService.getIndicatorsByClassificationId(classificationId)).thenReturn(indicators);
 
         mockMvc.perform(get("/api/indicator/filter/" + classificationId))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].name").value(indicatorName))
                 .andExpect(jsonPath("$[0].id").value(indicatorId));
 
         verify(indicatorService, times(1)).getIndicatorsByClassificationId(classificationId);
     }
 
     @Test
     public void testGetIndicatorById() throws Exception {
         String indicatorName = "test indicator";
         Integer indicatorId = 1;
 
         IndicatorEntity indicator = new IndicatorEntity();
         indicator.setName(indicatorName);
         indicator.setId(indicatorId);
         Mockito.when(indicatorService.getIndicatorById(indicatorId)).thenReturn(indicator);
 
         mockMvc.perform(get("/api/indicator/" + indicatorId))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.name").value(indicatorName))
                 .andExpect(jsonPath("$.id").value(indicatorId));
 
         verify(indicatorService, times(1)).getIndicatorById(indicatorId);
     }
 
     @Ignore
     @Test
     public void testCreateIndicator() throws Exception {
         mockMvc.perform(post("/api/indicator")
                 .content(CREATE_INDICATOR_JSON)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk());
 
         verify(indicatorService, times(1)).createNewIndicator((IndicatorEntity) anyObject());
     }
 
     @Test
     public void testCreateIndicatorWithoutNameValidation() throws Exception {
         mockMvc.perform(post("/api/indicator")
                 .content(CREATE_INDICATOR_JSON_NO_NAME)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isBadRequest());
 
         verify(indicatorService, times(0)).createNewIndicator((IndicatorEntity) anyObject());
     }
 
     @Test
     public void testDeleteIndicator() throws Exception {
         Integer indicatorId = 1;
         mockMvc.perform(delete("/api/indicator/" + indicatorId))
                 .andExpect(status().isOk());
         verify(indicatorService, times(1)).deleteIndicator((IndicatorEntity) anyObject());
     }
 
     @Test
     public void testGetIndicatorTypes() throws Exception {
         String indicatorTypeName = "test indicator";
         Integer indicatorTypeId = 1;
 
         Set<IndicatorTypeEntity> indicatorTypes = new LinkedHashSet<>();
         IndicatorTypeEntity indicatorType = new IndicatorTypeEntity(indicatorTypeName);
         indicatorType.setId(indicatorTypeId);
         indicatorTypes.add(indicatorType);
 
         Mockito.when(indicatorService.getAllIndicatorTypes()).thenReturn(indicatorTypes);
 
         mockMvc.perform(get("/api/indicator/type"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].name").value(indicatorTypeName))
                 .andExpect(jsonPath("$[0].id").value(indicatorTypeId));
 
         verify(indicatorService, times(1)).getAllIndicatorTypes();
     }
 
     @Test
     public void testGetIndicatorTypeById() throws Exception {
         String indicatorTypeName = "test indicator";
         Integer indicatorTypeId = 1;
         IndicatorTypeEntity indicatorType = new IndicatorTypeEntity(indicatorTypeName);
         indicatorType.setId(indicatorTypeId);
 
         Mockito.when(indicatorService.getIndicatorTypeById(indicatorTypeId)).thenReturn(indicatorType);
 
         mockMvc.perform(get("/api/indicator/type/" + indicatorTypeId))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.name").value(indicatorTypeName))
                 .andExpect(jsonPath("$.id").value(indicatorTypeId));
 
         verify(indicatorService, times(1)).getIndicatorTypeById(indicatorTypeId);
     }
 
     @Test
     public void testGetIndicatorClassifications() throws Exception {
         String classificationName = "indicator classification";
         Integer classificationId = 1;
 
         Set<IndicatorClassificationEntity> classifications = new LinkedHashSet<>();
         IndicatorClassificationEntity classification = new IndicatorClassificationEntity(classificationName);
         classification.setId(classificationId);
         classifications.add(classification);
         Mockito.when(indicatorService.getAllIndicatorClassifications()).thenReturn(classifications);
 
         mockMvc.perform(get("/api/indicator/classification"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].name").value(classificationName))
                 .andExpect(jsonPath("$[0].id").value(classificationId));
 
         verify(indicatorService, times(1)).getAllIndicatorClassifications();
     }
 
     @Test
     public void testGetIndicatorClassificationById() throws Exception {
         String classificationName = "indicator classification";
         Integer classificationId = 1;
 
         IndicatorClassificationEntity classification = new IndicatorClassificationEntity(classificationName);
         classification.setId(classificationId);
         Mockito.when(indicatorService.getIndicatorClassificationById(classificationId)).thenReturn(classification);
 
         mockMvc.perform(get("/api/indicator/classification/" + classificationId))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.name").value(classificationName))
                 .andExpect(jsonPath("$.id").value(classificationId));
 
         verify(indicatorService, times(1)).getIndicatorClassificationById(classificationId);
     }
 
     @Test
     public void testDeleteIndicatorClassification() throws Exception {
         Integer classificationId = 1;
         mockMvc.perform(delete("/api/indicator/classification/" + classificationId))
                 .andExpect(status().isOk());
         verify(indicatorService, times(1)).deleteIndicatorClassification((IndicatorClassificationEntity) anyObject());
     }
 
     @Test
     @Ignore
     public void testUpdateIndicator() throws Exception {
         Integer indicatorId = 1;
         mockMvc.perform(put("/api/indicator/" + indicatorId)
             .content(UPDATE_INDICATOR_JSON)
             .contentType(MediaType.APPLICATION_JSON))
             .andExpect(status().isOk());
 
        verify(indicatorService, times(1)).updateIndicatorFromDto(anyInt(), (IndicatorDto) anyObject());
     }
 
     @Test
     public void testCreateIndicatorClassification() throws Exception {
         mockMvc.perform(put("/api/indicator/classification")
                 .content(CREATE_CLASSIFICATION_JSON)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk());
 
         verify(indicatorService, times(1)).createNewIndicatorClassification((IndicatorClassificationEntity) anyObject());
     }
 
     @Test
     public void testUpdateIndicatorClassification() throws Exception {
         Integer classificationId = 1;
         String classificationName = "name";
         IndicatorClassificationEntity indicatorClassification = new IndicatorClassificationEntity(classificationName);
         Mockito.when(indicatorService.getIndicatorClassificationById(classificationId)).thenReturn(indicatorClassification);
         mockMvc.perform(put("/api/indicator/classification/" + classificationId)
                 .content(UPDATE_CLASSIFICATION_JSON)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk());
 
         verify(indicatorService, times(1)).updateIndicatorClassification(indicatorClassification);
     }
 
     @Test
     public void testGetDailyTaskTime() throws Exception {
         String time = "12:34";
         CronTaskEntity cronTaskEntity = new CronTaskEntity();
         cronTaskEntity.setTime(time);
         Mockito.when(cronService.getDailyCronTask()).thenReturn(cronTaskEntity);
 
         mockMvc.perform(get("/api/indicator/calculator/frequency/daily"))
                 .andExpect(status().isOk())
                 .andExpect(content().string(time));
 
         verify(cronService).getDailyCronTask();
     }
 
     @Test
     public void testUpdateDailyTaskTime() throws Exception {
         String expr = "12:09";
 
         Mockito.when(cronService.getDailyCronTask()).thenReturn(new CronTaskEntity());
 
         mockMvc.perform(put("/api/indicator/calculator/frequency/daily")
                 .content(expr)
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk());
 
         verify(cronService).updateCronTask((CronTaskEntity) anyObject());
     }
 
     @Test
     public void testRecalculateIndicators() throws Exception {
         Mockito.doNothing().when(indicatorService).calculateAllIndicators(0);
 
         mockMvc.perform(get("/api/indicator/calculator/recalculate/0"))
                 .andExpect(status().isOk());
 
         verify(indicatorService).calculateAllIndicators(0);
     }
 
 }
