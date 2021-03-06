 /*
  * This program is part of the OpenLMIS logistics management information system platform software.
  * Copyright © 2013 VillageReach
  *
  *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  *  You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
  */
 
 package org.openlmis.rnr.service;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.openlmis.core.domain.Money;
 import org.openlmis.core.domain.ProcessingPeriod;
 import org.openlmis.core.service.ProcessingScheduleService;
 import org.openlmis.db.categories.UnitTests;
 import org.openlmis.rnr.builder.RnrLineItemBuilder;
 import org.openlmis.rnr.calculation.DefaultStrategy;
 import org.openlmis.rnr.calculation.EmergencyRnrCalcStrategy;
 import org.openlmis.rnr.calculation.RnrCalculationStrategy;
 import org.openlmis.rnr.calculation.VirtualFacilityStrategy;
 import org.openlmis.rnr.domain.*;
 import org.openlmis.rnr.repository.RequisitionRepository;
 
 import java.util.*;
 
 import static com.natpryce.makeiteasy.MakeItEasy.*;
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.ArgumentCaptor.forClass;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.openlmis.core.builder.ProcessingPeriodBuilder.defaultProcessingPeriod;
 import static org.openlmis.core.builder.ProcessingPeriodBuilder.numberOfMonths;
 import static org.openlmis.rnr.builder.RequisitionBuilder.defaultRequisition;
 import static org.openlmis.rnr.builder.RequisitionBuilder.period;
 import static org.openlmis.rnr.builder.RnrLineItemBuilder.defaultRnrLineItem;
 import static org.openlmis.rnr.domain.RnrStatus.SUBMITTED;
 
 @Category(UnitTests.class)
 public class CalculationServiceTest {
 
   private Rnr rnr;
 
   @Mock
   List<LossesAndAdjustmentsType> lossesAndAdjustmentsTypes;
 
   @Mock
   RequisitionRepository requisitionRepository;
   @Mock
   ProcessingScheduleService processingScheduleService;
 
   @InjectMocks
   CalculationService calculationService;
 
   @Before
   public void setUp() throws Exception {
     initMocks(this);
     rnr = make(a(defaultRequisition));
     when(requisitionRepository.getLossesAndAdjustmentsTypes()).thenReturn(lossesAndAdjustmentsTypes);
   }
 
   @Test
   public void shouldCallValidateOnEachLineItem() throws Exception {
     final RnrLineItem rnrLineItem1 = mock(RnrLineItem.class);
     final RnrLineItem rnrLineItem2 = mock(RnrLineItem.class);
 
     when(rnrLineItem1.calculateCost()).thenReturn(new Money("10"));
     when(rnrLineItem2.calculateCost()).thenReturn(new Money("10"));
 
     rnr.setFullSupplyLineItems(asList(rnrLineItem1));
     rnr.setNonFullSupplyLineItems(asList(rnrLineItem2));
 
     List<RnrColumn> programRnrColumns = new ArrayList<>();
     ProgramRnrTemplate template = new ProgramRnrTemplate(programRnrColumns);
 
     calculationService.perform(rnr, template);
 
     verify(rnrLineItem1).validateMandatoryFields(template);
     verify(rnrLineItem1).validateCalculatedFields(template);
 
     verify(rnrLineItem2).validateNonFullSupply();
   }
 
   @Test
   public void shouldCalculateCalculatedFieldsAccordingToProgramTemplate() throws Exception {
     ArrayList<RnrColumn> programRequisitionColumns = new ArrayList<>();
     ProcessingPeriod period = new ProcessingPeriod();
     RnrLineItem firstLineItem = mock(RnrLineItem.class);
     RnrLineItem secondLineItem = mock(RnrLineItem.class);
 
     rnr.setFullSupplyLineItems(asList(firstLineItem));
     rnr.setNonFullSupplyLineItems(asList(secondLineItem));
     rnr.setPeriod(period);
     rnr.setStatus(SUBMITTED);
 
     when(firstLineItem.calculateCost()).thenReturn(new Money("10"));
     when(secondLineItem.calculateCost()).thenReturn(new Money("20"));
     ProgramRnrTemplate template = new ProgramRnrTemplate(programRequisitionColumns);
 
     calculationService.perform(rnr, template);
 
     ArgumentCaptor<DefaultStrategy> capture = forClass(DefaultStrategy.class);
 
     verify(firstLineItem).calculateForFullSupply(capture.capture(), eq(period), eq(template), eq(SUBMITTED), eq(lossesAndAdjustmentsTypes));
     assertThat(capture.getValue().getClass(), is(DefaultStrategy.class.getClass()));
     verify(firstLineItem).calculateCost();
     verify(secondLineItem).calculateCost();
     verify(secondLineItem).calculatePacksToShip(capture.capture());
     assertThat(capture.getValue().getClass(), is(DefaultStrategy.class.getClass()));
     assertThat(rnr.getFullSupplyItemsSubmittedCost(), is(new Money("10")));
     assertThat(rnr.getNonFullSupplyItemsSubmittedCost(), is(new Money("20")));
   }
 
   @Test
   public void shouldCalculateForEmergencyRequisitionUsingEmergencyStrategy() throws Exception {
     rnr.setEmergency(true);
     final RnrLineItem rnrLineItem1 = mock(RnrLineItem.class);
     final RnrLineItem rnrLineItem2 = mock(RnrLineItem.class);
     ProgramRnrTemplate template = new ProgramRnrTemplate(Collections.<Column>emptyList());
 
 
     when(rnrLineItem1.calculateCost()).thenReturn(new Money("10"));
     when(rnrLineItem2.calculateCost()).thenReturn(new Money("10"));
     rnr.setFullSupplyLineItems(asList(rnrLineItem1));
     rnr.setNonFullSupplyLineItems(asList(rnrLineItem2));
 
     calculationService.perform(rnr, template);
 
     ArgumentCaptor<EmergencyRnrCalcStrategy> captor = forClass(EmergencyRnrCalcStrategy.class);
     verify(rnrLineItem1).calculateForFullSupply(captor.capture(), eq(rnr.getPeriod()), eq(template), eq(rnr.getStatus()), eq(lossesAndAdjustmentsTypes));
     assertThat(captor.getValue().getClass(), is(EmergencyRnrCalcStrategy.class.getClass()));
 
     verify(rnrLineItem2).calculatePacksToShip(captor.capture());
     assertThat(captor.getValue().getClass(), is(EmergencyRnrCalcStrategy.class.getClass()));
   }
 
   @Test
   public void shouldCalculateForVirtualRequisitionUsingVirtualStrategy() throws Exception {
     rnr.getFacility().setVirtualFacility(true);
     final RnrLineItem rnrLineItem1 = mock(RnrLineItem.class);
     ProgramRnrTemplate template = new ProgramRnrTemplate(Collections.<Column>emptyList());
 
 
     when(rnrLineItem1.calculateCost()).thenReturn(new Money("10"));
     rnr.setFullSupplyLineItems(asList(rnrLineItem1));
 
     calculationService.perform(rnr, template);
 
     ArgumentCaptor<VirtualFacilityStrategy> captor = forClass(VirtualFacilityStrategy.class);
     verify(rnrLineItem1).calculateForFullSupply(captor.capture(), eq(rnr.getPeriod()), eq(template), eq(rnr.getStatus()), eq(lossesAndAdjustmentsTypes));
     verify(rnrLineItem1).validateMandatoryFields(template);
     verify(rnrLineItem1).validateCalculatedFields(template);
     assertThat(captor.getValue().getClass(), is(VirtualFacilityStrategy.class.getClass()));
   }
 
   @Test
   public void shouldAvoidCalculationForSkippedFullSupplyLineItems() throws Exception {
     ProcessingPeriod period = new ProcessingPeriod();
     RnrLineItem skippedLineItem = mock(RnrLineItem.class);
     when(skippedLineItem.getSkipped()).thenReturn(true);
 
     RnrLineItem nonSkippedLineItem = mock(RnrLineItem.class);
 
     rnr.setFullSupplyLineItems(asList(skippedLineItem, nonSkippedLineItem));
     rnr.setPeriod(period);
     rnr.setStatus(SUBMITTED);
 
     when(nonSkippedLineItem.calculateCost()).thenReturn(new Money("20"));
     ProgramRnrTemplate template = new ProgramRnrTemplate(new ArrayList<RnrColumn>());
 
     calculationService.perform(rnr, template);
 
     verify(skippedLineItem, never()).calculateForFullSupply(any(RnrCalculationStrategy.class),
         any(ProcessingPeriod.class),
         any(ProgramRnrTemplate.class),
         any(RnrStatus.class),
         anyListOf(LossesAndAdjustmentsType.class));
 
     verify(skippedLineItem, never()).calculateCost();
     verify(nonSkippedLineItem).calculateCost();
     assertThat(rnr.getFullSupplyItemsSubmittedCost(), is(new Money("20")));
   }
 
   @Test
   public void shouldCalculateDaysDifferenceUsingCurrentPeriodIfPreviousPeriodNotPresent() throws Exception {
     Date createdDateOfPreviousLineItem = setLineItemDatesAndReturnDate();
 
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(Collections.EMPTY_LIST);
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), rnr.getPeriod().getStartDate())).thenReturn(createdDateOfPreviousLineItem);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertThat(lineItem.getDaysSinceLastLineItem(), is(5));
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), rnr.getPeriod().getStartDate());
   }
 
   @Test
   public void shouldCalculateDaysDifferenceUsingPreviousPeriodIfPreviousPeriodPresentButSecondPreviousPeriodIsNotPresent() throws Exception {
     Date createdDateOfPreviousLineItem = setLineItemDatesAndReturnDate();
 
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 2, "previousPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod));
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), previousPeriod.getStartDate())).thenReturn(createdDateOfPreviousLineItem);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertThat(lineItem.getDaysSinceLastLineItem(), is(5));
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), previousPeriod.getStartDate());
   }
 
   @Test
   public void shouldCalculateDaysDifferenceUsingSecondPreviousPeriodIfPreviousPeriodAndSecondPreviousPeriodPresent() throws Exception {
     Date createdDateOfPreviousLineItem = setLineItemDatesAndReturnDate();
 
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 2, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, new Date(), new Date(), 2, "secondLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod, secondLastPeriod));
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate())).thenReturn(createdDateOfPreviousLineItem);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertThat(lineItem.getDaysSinceLastLineItem(), is(5));
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate());
   }
 
   @Test
   public void shouldCalculateDaysDifferenceUsingPreviousPeriodIfPreviousPeriodPresentAndNumberOfMonthsIsGreaterThanOrEqualToThree() throws Exception {
     Date createdDateOfPreviousLineItem = setLineItemDatesAndReturnDate();
 
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 4, "previousPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod));
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), previousPeriod.getStartDate())).thenReturn(createdDateOfPreviousLineItem);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertThat(lineItem.getDaysSinceLastLineItem(), is(5));
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), previousPeriod.getStartDate());
   }
 
   @Test
   public void shouldCalculateDaysDifferenceUsingSecondPreviousPeriodIfMIsSmallerThanThree() throws Exception {
     Date createdDateOfPreviousLineItem = setLineItemDatesAndReturnDate();
 
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 2, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, new Date(), new Date(), 2, "secondLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod, secondLastPeriod));
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate())).thenReturn(createdDateOfPreviousLineItem);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertThat(lineItem.getDaysSinceLastLineItem(), is(5));
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate());
   }
 
   @Test
   public void shouldNotCalculateDaysDifferenceIfPreviousAuthorizedLineItemIsNotPresent() throws Exception {
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 4, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, new Date(), new Date(), 2, "secondLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod, secondLastPeriod));
     when(requisitionRepository.getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate())).thenReturn(null);
 
     calculationService.calculateDaysDifference(rnr);
 
     assertNull(lineItem.getDaysSinceLastLineItem());
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate());
   }
 
   @Test
   public void shouldNotCalculateDaysDifferenceIfCurrentLineItemIsSkipped() throws Exception {
     RnrLineItem lineItem = rnr.getFullSupplyLineItems().get(0);
     lineItem.setSkipped(true);
     rnr.setFullSupplyLineItems(asList(lineItem));
 
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 4, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, new Date(), new Date(), 2, "secondLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2)).thenReturn(asList(previousPeriod, secondLastPeriod));
 
     calculationService.calculateDaysDifference(rnr);
 
     assertNull(lineItem.getDaysSinceLastLineItem());
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(rnr.getPeriod(), 2);
     verify(requisitionRepository, never()).getCreatedDateForPreviousLineItem(rnr, lineItem.getProductCode(), secondLastPeriod.getStartDate());
   }
 
   @Test
   public void shouldGetPreviousOneNormalizedConsumptionFor3MonthsInCurrentPeriodIfPreviousPeriodNotExists() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
     requisition.setPeriod(make(a(defaultProcessingPeriod, with(numberOfMonths, 3))));
 
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(null, programTemplate, regimenTemplate);
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(Collections.EMPTY_LIST);
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, requisition.getPeriod().getStartDate())).thenReturn(asList(4));
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, requisition.getPeriod().getStartDate());
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
   }
 
   @Test
   public void shouldGetPreviousOneNormalizedConsumptionFor2MonthsInCurrentPeriodIfPreviousPeriodNotExists() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
     requisition.setPeriod(make(a(defaultProcessingPeriod, with(numberOfMonths, 2))));
 
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(null, programTemplate, regimenTemplate);
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(Collections.EMPTY_LIST);
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, requisition.getPeriod().getStartDate())).thenReturn(asList(4));
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, requisition.getPeriod().getStartDate());
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
   }
 
   @Test
   public void shouldGetPreviousTwoNormalizedConsumptionFor1MonthInCurrentPeriodIfPreviousPeriodNotExists() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
     requisition.setPeriod(make(a(defaultProcessingPeriod, with(numberOfMonths, 1))));
 
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(null, programTemplate, regimenTemplate);
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(Collections.EMPTY_LIST);
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 2, requisition.getPeriod().getStartDate())).thenReturn(asList(4, 9));
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 2, requisition.getPeriod().getStartDate());
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4, 9)));
   }
 
   @Test
   public void shouldGetPreviousOneNormalizedConsumptionFor3MonthsInPreviousPeriod() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
 
     Date trackingStartDate = new Date();
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, trackingStartDate, new Date(), 3, "previousPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(asList(previousPeriod));
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, previousPeriod.getStartDate())).thenReturn(asList(4));
 
     Rnr previousRnr = make(a(defaultRequisition, with(period, make(a(defaultProcessingPeriod, with(numberOfMonths, 3))))));
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
 
     when(requisitionRepository.getRegularRequisitionWithLineItems(requisition.getFacility(), requisition.getProgram(), previousPeriod)).thenReturn(previousRnr);
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(previousRnr, programTemplate, regimenTemplate);
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, previousPeriod.getStartDate());
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
   }
 
   @Test
   public void shouldGetPreviousOneNormalizedConsumptionFor2MonthsInPreviousPeriod() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
 
     Date trackingStartDate = DateTime.now().minusMonths(2).toDate();
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 2, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, trackingStartDate, new Date(), 2, "secondLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(asList(previousPeriod, secondLastPeriod));
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate)).thenReturn(asList(4));
 
     Rnr previousRnr = make(a(defaultRequisition, with(period, make(a(defaultProcessingPeriod, with(numberOfMonths, 3))))));
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
 
     when(requisitionRepository.getRegularRequisitionWithLineItems(requisition.getFacility(), requisition.getProgram(), previousPeriod)).thenReturn(previousRnr);
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(previousRnr, programTemplate, regimenTemplate);
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate);
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
   }
 
   @Test
  public void shouldGetPreviousOneNormalizedConsumptionFor2MonthsInPreviousPeriodAndTrackFromPreviousPeriodStartDateIfOnly1PreviousPeriodExists() throws Exception {
    Rnr requisition = getVirtualFacilityRnr();
    String productCode = "Code1";
    requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));

    Date trackingStartDate = DateTime.now().minusMonths(2).toDate();
    ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, trackingStartDate, new Date(), 2, "previousPeriod");

    when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(asList(previousPeriod));
    when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate)).thenReturn(asList(4));

    Rnr previousRnr = make(a(defaultRequisition, with(period, make(a(defaultProcessingPeriod, with(numberOfMonths, 3))))));
    ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
    RegimenTemplate regimenTemplate = new RegimenTemplate();

    when(requisitionRepository.getRegularRequisitionWithLineItems(requisition.getFacility(), requisition.getProgram(), previousPeriod)).thenReturn(previousRnr);
    doNothing().when(requisition).setFieldsAccordingToTemplateFrom(previousRnr, programTemplate, regimenTemplate);

    calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);

    verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
    verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate);
    assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
  }

  @Test
   public void shouldGetPreviousOneNormalizedConsumptionFor2MonthsInPreviousPeriodAndShouldTrackFromLast2Periods() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
 
     Date trackingStartDate = DateTime.now().minusMonths(2).toDate();
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 2, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, trackingStartDate, new Date(), 2, "secondLastPeriod");
     ProcessingPeriod thirdLastPeriod = new ProcessingPeriod(4l, new Date(), new Date(), 1, "thirdLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(asList(previousPeriod, secondLastPeriod, thirdLastPeriod));
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate)).thenReturn(asList(4));
 
     Rnr previousRnr = make(a(defaultRequisition, with(period, make(a(defaultProcessingPeriod, with(numberOfMonths, 3))))));
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
 
     when(requisitionRepository.getRegularRequisitionWithLineItems(requisition.getFacility(), requisition.getProgram(), previousPeriod)).thenReturn(previousRnr);
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(previousRnr, programTemplate, regimenTemplate);
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 1, trackingStartDate);
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4)));
   }
 
   @Test
   public void shouldGetPreviousTwoNormalizedConsumptionsFor1MonthInPreviousPeriodAndShouldTrackFromLast5Periods() throws Exception {
     Rnr requisition = getVirtualFacilityRnr();
     String productCode = "Code1";
     requisition.setFullSupplyLineItems(asList(make(a(defaultRnrLineItem, with(RnrLineItemBuilder.productCode, productCode)))));
 
     Date trackingStartDate = DateTime.now().minusMonths(2).toDate();
     ProcessingPeriod previousPeriod = new ProcessingPeriod(2l, new Date(), new Date(), 1, "previousPeriod");
     ProcessingPeriod secondLastPeriod = new ProcessingPeriod(3l, new Date(), new Date(), 1, "secondLastPeriod");
     ProcessingPeriod thirdLastPeriod = new ProcessingPeriod(4l, new Date(), new Date(), 1, "thirdLastPeriod");
     ProcessingPeriod fourthLastPeriod = new ProcessingPeriod(5l, new Date(), new Date(), 1, "fourthLastPeriod");
     ProcessingPeriod fifthLastPeriod = new ProcessingPeriod(6l, trackingStartDate, new Date(), 1, "fifthLastPeriod");
 
     when(processingScheduleService.getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5)).thenReturn(asList(previousPeriod, secondLastPeriod, thirdLastPeriod, fourthLastPeriod, fifthLastPeriod));
     when(requisitionRepository.getNNormalizedConsumptions(productCode, requisition, 2, trackingStartDate)).thenReturn(asList(4, 5));
 
     Rnr previousRnr = make(a(defaultRequisition, with(period, make(a(defaultProcessingPeriod, with(numberOfMonths, 3))))));
     ProgramRnrTemplate programTemplate = new ProgramRnrTemplate();
     RegimenTemplate regimenTemplate = new RegimenTemplate();
 
     when(requisitionRepository.getRegularRequisitionWithLineItems(requisition.getFacility(), requisition.getProgram(), previousPeriod)).thenReturn(previousRnr);
     doNothing().when(requisition).setFieldsAccordingToTemplateFrom(previousRnr, programTemplate, regimenTemplate);
 
     calculationService.fillFieldsForInitiatedRequisition(requisition, programTemplate, regimenTemplate);
 
     verify(processingScheduleService).getNPreviousPeriodsInDescOrder(requisition.getPeriod(), 5);
     verify(requisitionRepository).getNNormalizedConsumptions(productCode, requisition, 2, trackingStartDate);
     assertThat(requisition.getFullSupplyLineItems().get(0).getPreviousNormalizedConsumptions(), is(asList(4, 5)));
   }
 
   private Rnr getVirtualFacilityRnr() {
     //TODO: Inline method when calculating for regular
     Rnr spy = spy(rnr);
     spy.getFacility().setVirtualFacility(true);
     return spy;
   }
 
   private Date setLineItemDatesAndReturnDate() {
     Calendar currentDate = Calendar.getInstance();
     Calendar previousDate = (Calendar) currentDate.clone();
     previousDate.add(Calendar.DATE, -5);
 
     Date createdDateOfPreviousLineItem = new Date(previousDate.getTimeInMillis());
     Date createdDateOfCurrentLineItem = new Date(currentDate.getTimeInMillis());
     rnr.setCreatedDate(createdDateOfCurrentLineItem);
     return createdDateOfPreviousLineItem;
   }
 
 }
