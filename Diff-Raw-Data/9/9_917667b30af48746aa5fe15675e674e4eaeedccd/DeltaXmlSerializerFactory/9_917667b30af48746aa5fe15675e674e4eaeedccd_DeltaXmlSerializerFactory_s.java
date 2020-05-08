 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
 import org.dom4j.Element;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 
 public class DeltaXmlSerializerFactory implements BeanFactoryAware {
     private final String PLANNED_CALENDAR_DELTA_SERIALIZER = "plannedCalendarDeltaXmlSerializer";
     private final String EPOCH_DELTA_SERIALIZER = "epochDeltaXmlSerializer";
     private final String STUDY_SEGMENT_DELTA_SERIALIZER = "studySegmentDeltaXmlSerializer";
     private final String PERIOD_DELTA_SERIALIZER = "periodDeltaXmlSerializer";
     private final String PLANNED_ACTIVITY_DELTA_SERIALIZER = "plannedActivityDeltaXmlSerializer";
     private final String POPULATION_DELTA_SERIALIZER = "populationDeltaXmlSerializer";
 
     private BeanFactory beanFactory;
     private Study study;
 
     public AbstractDeltaXmlSerializer createXmlSerializer(final Delta delta) {
         if (delta instanceof PlannedCalendarDelta) {
             return getXmlSerialzier(PLANNED_CALENDAR_DELTA_SERIALIZER);
         } else if (delta instanceof EpochDelta) {
             return getXmlSerialzier(EPOCH_DELTA_SERIALIZER);
         } else if (delta instanceof StudySegmentDelta) {
             return getXmlSerialzier(STUDY_SEGMENT_DELTA_SERIALIZER);
         } else if (delta instanceof PeriodDelta) {
             return getXmlSerialzier(PERIOD_DELTA_SERIALIZER);
         } else if (delta instanceof PlannedActivityDelta) {
             return getXmlSerialzier(PLANNED_ACTIVITY_DELTA_SERIALIZER);
        } else if (delta instanceof PopulationDelta) {
             return getXmlSerialzier(POPULATION_DELTA_SERIALIZER);
        }
        else {
             throw new StudyCalendarError("Problem importing template. Could not find delta type");
         }
     }
 
     public AbstractDeltaXmlSerializer createXmlSerializer(final Element delta) {
         if (PlannedCalendarDeltaXmlSerializer.PLANNED_CALENDAR_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(PLANNED_CALENDAR_DELTA_SERIALIZER);
         } else if (EpochDeltaXmlSerializer.EPOCH_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(EPOCH_DELTA_SERIALIZER);
         } else if (StudySegmentDeltaXmlSerializer.STUDY_SEGMENT_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(STUDY_SEGMENT_DELTA_SERIALIZER);
         } else if (PeriodDeltaXmlSerializer.PERIOD_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(PERIOD_DELTA_SERIALIZER);
         } else if(PlannedActivityDeltaXmlSerializer.PLANNED_ACTIVITY_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(PLANNED_ACTIVITY_DELTA_SERIALIZER);
         } else if (PopulationDeltaXmlSerializer.POPULATION_DELTA.equals(delta.getName())) {
             return getXmlSerialzier(POPULATION_DELTA_SERIALIZER);
         } else {
             throw new StudyCalendarError("Problem importing template. Could not find delta type %s", delta.getName());
         }
     }
 
     private AbstractDeltaXmlSerializer getXmlSerialzier(String beanName) {
         AbstractDeltaXmlSerializer serializer = (AbstractDeltaXmlSerializer) beanFactory.getBean(beanName);
         serializer.setStudy(study);
         return serializer;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 }
