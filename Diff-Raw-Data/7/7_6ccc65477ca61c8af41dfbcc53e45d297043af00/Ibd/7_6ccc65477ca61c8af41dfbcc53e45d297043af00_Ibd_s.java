 package com.worthsoln.ibd;
 
 import java.text.SimpleDateFormat;
 
 public class Ibd {
 
     public static final String MY_IBD_PARAM = "myIbd";
     public static final String NHS_NO_PARAM = "nhsNo";
 
     // ibd form params
     public static final String DIAGNOSIS_ID_PARAM = "diagnosisId";
     public static final String DISEASE_EXTENT_ID_PARAM = "diseaseExtentId";
     public static final String BODY_PART_AFFECTED_ID_PARAM = "bodyPartAffectedId";
     public static final String WEIGHT_PARAM = "weight";
     public static final String FAMILY_HISTORY_ID_PARAM = "familyHistoryId";
     public static final String SMOKING_ID_PARAM = "smokingId";
     public static final String SURGERY_ID_PARAM = "surgeryId";
     public static final String VACCINATION_RECORD_ID_PARAM = "vaccinationRecordId";
     public static final String COMPLICATION_IDS_PARAM = "complicationIds";
 
     // care plan params
     public static final String AREA_TO_DISCUSS_IDS_PARAM = "areaToDiscussIds";
     public static final String FURTHER_TOPICS_PARAM = "furtherTopics";
     public static final String GOALS_PARAM = "goals";
     public static final String GOAL_TO_ACHIEVE_PARAM = "goalToAchieve";
     public static final String GOAL_SCORE_PARAM = "goalScale";
     public static final String HOW_TO_ACHIEVE_GOAL_PARAM = "howToAchieveGoal";
     public static final String BARRIERS_PARAM = "barriers";
     public static final String WHAT_CAN_BE_DONE_PARAM = "whatCanBeDone";
     public static final String CONFIDENCE_SCALE_PARAM = "confidenceScale";
     public static final String REVIEW_DATE_PARAM = "reviewDate";
 
     // message keys
     public static final String NHS_NO_NOT_FOUND = "nhsNo.notFound";
     public static final String DIAGNOSIS_REQUIRED = "diagnosis.required";
     public static final String DISEASE_EXTENT_REQUIRED = "diseaseExtent.required";
 
     public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
 }
