 package org.pharmgkb;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.*;
 import org.pharmgkb.util.ExcelUtils;
 
 import java.util.Iterator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 public class SubjectIterator implements Iterator {
   private static final Logger sf_logger = Logger.getLogger(SubjectIterator.class);
  private static final Integer sf_columnCount = 214;
 
   private Sheet m_sheet = null;
   private Integer m_currentRow = 3;
   private FormulaEvaluator m_formulaEvaluator = null;
 
   public SubjectIterator(Sheet sheet) throws Exception {
     if (sheet == null) {
       throw new Exception("No sheet specified");
     }
     setSheet(sheet);
 
     Workbook wb = sheet.getWorkbook();
     setFormulaEvaluator(wb.getCreationHelper().createFormulaEvaluator());
 
     if (sf_logger.isDebugEnabled()) {
       sf_logger.debug("Sheet has "+sheet.getLastRowNum()+" rows");
     }
   }
 
   @Override
   public boolean hasNext() {
     return getCurrentRow()<=getSheet().getLastRowNum();
   }
 
   @Override
   public Subject next() {
     Subject subject = new Subject();
 
     Row row = getSheet().getRow(getCurrentRow());
 
     if (sf_logger.isDebugEnabled()) {
       sf_logger.debug("row " + getCurrentRow() + " length: " + row.getLastCellNum());
     }
 
     try {
       copyFromRowToSubject(row, subject);
     }
     catch (Exception ex) {
       sf_logger.error("Couldn't copy subject data for row "+getCurrentRow(), ex);
       subject = null;
     }
 
     bumpCurrentRow();
     return subject;
   }
 
   protected void copyFromRowToSubject(Row row, Subject subject) throws Exception {
     Preconditions.checkNotNull(row);
     Preconditions.checkNotNull(subject);
     Preconditions.checkArgument(
         row.getLastCellNum()>=(sf_columnCount-1),
         "Found insufficient columns, expected %s, found %s",
         sf_columnCount,
         (row.getLastCellNum()+1));
 
     for (int colIdx=0; colIdx<sf_columnCount; colIdx++) {
       String cellStringValue;
       try {
         cellStringValue = ExcelUtils.getStringValue(row.getCell(colIdx), getFormulaEvaluator());
       } catch (Exception ex) {
         sf_logger.warn("Error getting value for cell "+ExcelUtils.getAddress(row.getCell(colIdx)));
         throw ex;
       }
 
       switch(colIdx) {
 
         case 0:
           String subjectId = row.getCell(colIdx).getStringCellValue();
           subjectId = StringUtils.replace(subjectId, ",", "");
           subject.setSubjectId(StringUtils.strip(subjectId));
           break;
 
         case 1:
           subject.setGenotyping(convertToValue(cellStringValue));
           break;
 
         case 2:
           subject.setPhenotyping(convertToValue(cellStringValue));
           break;
 
         case 3:
          if (StringUtils.isNotBlank(cellStringValue)) {
            for (String value : Splitter.on(";").trimResults().split(cellStringValue)) {
              subject.addSampleSource(SampleSource.lookupByName(value));
            }
           }
           break;
 
         case 4:
           subject.setProject(cellStringValue);
           break;
 
         case 5:
           switch(cellStringValue) {
             case "1":
               subject.setGender(Gender.MALE);
               break;
             case "2":
               subject.setGender(Gender.FEMALE);
               break;
             default:
               subject.setGender(Gender.UNKNOWN);
               break;
           }
           break;
 
         case 6:
           subject.setRaceself(cellStringValue);
           break;
 
         case 7:
           subject.setRaceOMB(cellStringValue);
           break;
 
         case 8:
           subject.setEthnicityreported(cellStringValue);
           break;
 
         case 9:
           subject.setEthnicityOMB(cellStringValue);
           break;
 
         case 10:
           subject.setCountry(cellStringValue);
           break;
 
         case 11:
           subject.setAge(getNumber(row.getCell(colIdx)));
           break;
 
         case 12:
           // binned age ignored
           break;
 
         case 13:
           subject.setHeight(getNumber(row.getCell(colIdx)));
           break;
 
         case 14:
           subject.setWeight(getNumber(row.getCell(colIdx)));
           break;
 
         case 15:
           subject.setBMI(getNumber(row.getCell(colIdx)));
           break;
 
         case 16:
           subject.setComorbidities(cellStringValue);
           break;
 
         case 17:
           switch(cellStringValue) {
             case "1":
               subject.setDiabetes(DiabetesStatus.TYPE1);
               break;
             case "2":
               subject.setDiabetes(DiabetesStatus.TYPE2);
               break;
             case "0":
               subject.setDiabetes(DiabetesStatus.NONE);
               break;
             default:
               subject.setDiabetes(DiabetesStatus.UNKNOWN);
               break;
           }
           break;
 
         case 18:
           subject.setEverSmoked(convertToValue(cellStringValue));
           break;
 
         case 19:
           subject.setCurrentsmoker(convertToValue(cellStringValue));
           break;
 
         case 20:
           switch(cellStringValue) {
             case "0":
               subject.setAlcohol(AlcoholStatus.NONE);
               break;
             case "1":
               subject.setAlcohol(AlcoholStatus.INFREQUENT);
               break;
             case "2":
               subject.setAlcohol(AlcoholStatus.MODERATE);
               break;
             case "3":
               subject.setAlcohol(AlcoholStatus.FREQUENT);
               break;
             default:
               subject.setAlcohol(AlcoholStatus.UNKNOWN);
               break;
           }
           break;
 
         case 21:
           subject.setBloodPressure(convertToValue(cellStringValue));
           break;
 
         case 22:
           subject.setDiastolicBPMax(getNumber(row.getCell(colIdx)));
           break;
 
         case 23:
           subject.setDiastolicBPMedian(getNumber(row.getCell(colIdx)));
           break;
 
         case 24:
           subject.setSystolicBPMax(getNumber(row.getCell(colIdx)));
           break;
 
         case 25:
           subject.setSystolicBPMedian(getNumber(row.getCell(colIdx)));
           break;
 
         case 26:
           subject.setCRP(getNumber(row.getCell(colIdx)));
           break;
 
         case 27:
           subject.setBUN(getNumber(row.getCell(colIdx)));
           break;
 
         case 28:
           subject.setCreatinine(getNumber(row.getCell(colIdx)));
           break;
 
         case 29:
           subject.setEjectionfraction(convertToValue(cellStringValue));
           break;
 
         case 30:
           subject.setLeftVentricle(getNumber(row.getCell(colIdx)));
           break;
 
         case 31:
           subject.setPlaceboRCT(convertToValue(cellStringValue));
           break;
 
         case 32:
           subject.setClopidogrel(convertToValue(cellStringValue));
           break;
 
         case 33:
           subject.setDoseClopidogrel(getNumber(row.getCell(colIdx)));
           break;
 
         case 34:
           subject.setDurationClopidogrel(getNumber(row.getCell(colIdx)));
           break;
 
         case 35:
           subject.setAspirin(convertToValue(cellStringValue));
           break;
 
         case 36:
           subject.setDoseAspirin(getNumber(row.getCell(colIdx)));
           break;
 
         case 37:
           subject.setDurationAspirin(getNumber(row.getCell(colIdx)));
           break;
 
         case 38:
           subject.setStatins(convertToValue(cellStringValue));
           break;
 
         case 39:
           subject.setPPI(convertToValue(cellStringValue));
           break;
 
         case 40:
           if (StringUtils.isBlank(cellStringValue)) {
             break;
           }
           if (cellStringValue.contains("1")) {
             subject.addPPIname("esomeprazole");
           }
           if (cellStringValue.contains("2")) {
             subject.addPPIname("lansoprazole");
           }
           if (cellStringValue.contains("3")) {
             subject.addPPIname("omeprazole");
           }
           if (cellStringValue.contains("4")) {
             subject.addPPIname("pantoprazole");
           }
           if (cellStringValue.contains("5")) {
             subject.addPPIname("rabeprazole");
           }
           if (cellStringValue.contains("6")) {
             subject.addPPIname("other");
           }
           break;
 
         case 41:
           subject.setCalciumblockers(convertToValue(cellStringValue));
           break;
 
         case 42:
           subject.setBetablockers(convertToValue(cellStringValue));
           break;
 
         case 43:
           subject.setACEInh(convertToValue(cellStringValue));
           break;
 
         case 44:
           subject.setAnginhblockers(convertToValue(cellStringValue));
           break;
 
         case 45:
           subject.setEzetemib(convertToValue(cellStringValue));
           break;
 
         case 46:
           subject.setGlycoproteinIIaIIIbinhibitor(convertToValue(cellStringValue));
           break;
 
         case 47:
           subject.setActivemetabolite(getNumber(row.getCell(colIdx)));
           break;
 
         case 48:
           subject.setBleeding(convertToValue(cellStringValue));
           break;
 
         case 49:
           subject.setMajorBleeding(convertToValue(cellStringValue));
           break;
 
         case 50:
           subject.setMinorBleeding(convertToValue(cellStringValue));
           break;
 
         case 51:
           subject.setDaysMajorBleeding(ExcelUtils.getNumericValue(row.getCell(colIdx),getFormulaEvaluator()));
           break;
 
         case 52:
           subject.setDaysMinorBleeding(ExcelUtils.getNumericValue(row.getCell(colIdx),getFormulaEvaluator()));
           break;
 
         case 53:
           subject.setCVevents(convertToValue(cellStringValue));
 
         case 54:
           subject.setTimeMACE(getNumber(row.getCell(colIdx)));
           break;
 
         case 55:
           subject.setSTEMI(convertToValue(cellStringValue));
           break;
 
         case 56:
           subject.setTimeSTEMI(getNumber(row.getCell(colIdx)));
           break;
 
         case 57:
           subject.setNSTEMI(convertToValue(cellStringValue));
           break;
 
         case 58:
           subject.setTimeNSTEMI(getNumber(row.getCell(colIdx)));
           break;
 
         case 59:
           subject.setAngina(convertToValue(cellStringValue));
           break;
 
         case 60:
           subject.setTimeAngina(getNumber(row.getCell(colIdx)));
           break;
 
         case 61:
           subject.setREVASC(convertToValue(cellStringValue));
           break;
 
         case 62:
           subject.setTimeREVASC(getNumber(row.getCell(colIdx)));
           break;
 
         case 63:
           subject.setStroke(convertToValue(cellStringValue));
           break;
 
         case 64:
           subject.setTimestroke(getNumber(row.getCell(colIdx)));
           break;
 
         case 65:
           subject.setOtherischemic(convertToValue(cellStringValue));
           break;
 
         case 66:
           subject.setCongestiveHeartFailure(convertToValue(cellStringValue));
           break;
 
         case 67:
           subject.setTimeheartFailure(getNumber(row.getCell(colIdx)));
           break;
 
         case 68:
           subject.setMechanicalValveReplacement(convertToValue(cellStringValue));
           break;
 
         case 69:
           subject.setTimeMechValve(getNumber(row.getCell(colIdx)));
           break;
 
         case 70:
           subject.setTissueValveReplacement(convertToValue(cellStringValue));
           break;
 
         case 71:
           subject.setTimeMechValve(getNumber(row.getCell(colIdx)));
           break;
 
         case 72:
           subject.setStentthromb(convertToValue(cellStringValue));
           break;
 
         case 73:
           subject.setTimestent(getNumber(row.getCell(colIdx)));
           break;
 
         case 74:
           subject.setAllcausemortality(convertToValue(cellStringValue));
           break;
 
         case 75:
           subject.setTimemortality(getNumber(row.getCell(colIdx)));
           break;
 
         case 76:
           subject.setCardiovasculardeath(convertToValue(cellStringValue));
           break;
 
         case 77:
           subject.setTimedeath(getNumber(row.getCell(colIdx)));
           break;
 
         case 78:
           subject.setAtrialfibrillation(convertToValue(cellStringValue));
           break;
 
         case 79:
           subject.setTimeAF(getNumber(row.getCell(colIdx)));
           break;
 
         case 80:
           subject.setLeftventricularhypertrophy(convertToValue(cellStringValue));
           break;
 
         case 81:
           subject.setTimevenHypertrophy(getNumber(row.getCell(colIdx)));
           break;
 
         case 82:
           subject.setPeripheralvasculardisease(convertToValue(cellStringValue));
           break;
 
         case 83:
           subject.setTimePeriVascular(getNumber(row.getCell(colIdx)));
           break;
 
         case 84:
           subject.setDurationfollowupclinicaloutcomes(getNumber(row.getCell(colIdx)));
           break;
 
         case 85:
           subject.setBloodCell(convertToValue(cellStringValue));
           break;
 
         case 86:
           subject.setWhitecellcount(getNumber(row.getCell(colIdx)));
           break;
 
         case 87:
           subject.setRedcellcount(getNumber(row.getCell(colIdx)));
           break;
 
         case 88:
           subject.setPlateletcount(getNumber(row.getCell(colIdx)));
           break;
 
         case 89:
           subject.setMeanplateletvolume(getNumber(row.getCell(colIdx)));
           break;
 
         case 90:
           subject.setHematocrit(getNumber(row.getCell(colIdx)));
           break;
 
         case 91:
           subject.setChol(convertToValue(cellStringValue));
           break;
 
         case 92:
           subject.setLDL(getNumber(row.getCell(colIdx)));
           break;
 
         case 93:
           subject.setHDL(getNumber(row.getCell(colIdx)));
           break;
 
         case 94:
           subject.setTotalCholesterol(getNumber(row.getCell(colIdx)));
           break;
 
         case 95:
           subject.setTriglycerides(getNumber(row.getCell(colIdx)));
           break;
 
         case 96:
           subject.setPlatAggrPheno(cellStringValue);
           break;
 
         case 97:
           subject.setInstrument(cellStringValue);
           break;
 
         case 98:
           subject.setInterassayvariation(getNumber(row.getCell(colIdx)));
           break;
 
         case 99:
           subject.setBloodcollectiontype(cellStringValue);
           break;
 
         case 100:
           subject.setSampletype(cellStringValue);
           break;
 
         case 101:
           subject.setVerifyNowbase(convertToValue(cellStringValue));
           break;
 
         case 102:
           subject.setVerifyNowpostloading(convertToValue(cellStringValue));
           break;
 
         case 103:
           subject.setVerifyNowwhileonclopidogrel(convertToValue(cellStringValue));
           break;
 
         case 104:
           subject.setOpticalPlateletAggregometry(getNumber(row.getCell(colIdx)));
           break;
 
         case 105:
           subject.setPreclopidogrelplateletaggregometrybase(convertToValue(cellStringValue));
           break;
 
         case 106:
           subject.setPostclopidogrelplateletaggregometry(convertToValue(cellStringValue));
           break;
 
         case 107:
           subject.setAggregometryagonist(cellStringValue);
           break;
 
         case 108:
           subject.setADP(cellStringValue);
           break;
 
         case 109:
           subject.setArachadonicacid(cellStringValue);
           break;
 
         case 110:
           subject.setCollagen(cellStringValue);
           break;
 
         case 111:
           subject.setPlateletaggregometrymethod(cellStringValue);
           break;
 
         case 112:
           subject.setClopidogrelloadingdose(getNumber(row.getCell(colIdx)));
           break;
 
         // XXX: skipping 113-199 for now. will implement soon.
 
         case 200:
           if (StringUtils.isNotBlank(cellStringValue)) {
             for (String genotype : Splitter.on("/").split(cellStringValue)) {
               subject.addCyp2c19genotype(genotype);
             }
           }
           break;
 
         case 201:
           subject.setRs4244285(cellStringValue);
           break;
 
         case 202:
           subject.setRs4986893(cellStringValue);
           break;
 
         case 203:
           subject.setRs28399504(cellStringValue);
           break;
 
         case 204:
           subject.setRs56337013(cellStringValue);
           break;
 
         case 205:
           subject.setRs72552267(cellStringValue);
           break;
 
         case 206:
           subject.setRs72558186(cellStringValue);
           break;
 
         case 207:
           subject.setRs41291556(cellStringValue);
           break;
 
         case 208:
           subject.setRs6413438(cellStringValue);
           break;
 
         case 209:
           subject.setRs12248560(cellStringValue);
           break;
 
         case 210:
           subject.setRs662(cellStringValue);
           break;
 
         case 211:
           subject.setRs854560(cellStringValue);
           break;
 
         case 212:
           subject.setRs1045642(cellStringValue);
           break;
 
         case 213:
           subject.setOthergenotypes(cellStringValue);
 
         default:
           if (sf_logger.isDebugEnabled()) {
             sf_logger.debug("column "+colIdx);
           }
           break;
       }
 
     }
   }
 
   @Override
   public void remove() {
     throw new UnsupportedOperationException("remove() not implemented for SubjectIterator");
   }
 
   public Sheet getSheet() {
     return m_sheet;
   }
 
   public void setSheet(Sheet sheet) {
     m_sheet = sheet;
   }
 
   protected Integer getCurrentRow() {
     return m_currentRow;
   }
 
   protected void bumpCurrentRow() {
     m_currentRow++;
   }
 
   protected Value convertToValue(String string) {
     Value value = Value.Unknown;
 
     if (StringUtils.isNotBlank(string)) {
       value = Value.lookupByName(string);
       if (value == null) {
         value = Value.Unknown;
       }
     }
     return value;
   }
 
   public FormulaEvaluator getFormulaEvaluator() {
     return m_formulaEvaluator;
   }
 
   public void setFormulaEvaluator(FormulaEvaluator formulaEvaluator) {
     m_formulaEvaluator = formulaEvaluator;
   }
 
   public Double getNumber(Cell cell) {
     return ExcelUtils.getNumericValue(cell, getFormulaEvaluator());
   }
 }
