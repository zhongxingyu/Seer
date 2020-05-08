 package com.qut.spc.calculations;
 
 import javax.persistence.EntityNotFoundException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.qut.spc.api.ElectricityCalculationApi;
 import com.qut.spc.api.TotalCostCalculationAPI;
 import com.qut.spc.db.Database;
 import com.qut.spc.model.Battery;
 import com.qut.spc.model.Inverter;
 import com.qut.spc.model.Panel;
 import com.qut.spc.postcode.PostcodeUtil;
 import com.qut.spc.weather.DailySunProvider;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Database.class,DailySunProvider.class,PostcodeUtil.class})
 public class SolarSystemTest {
 	private SolarSystem container;
 	private Panel panel;
 	private Inverter inverter;
 	private Battery battery;
 	private ElectricityCalculationApi calc;
 	private TotalCostCalculationAPI costcalc;
 	
 	@Before
 	public void setup(){
 		calc = mock(ElectricityCalculationApi.class);
 		costcalc=mock(TotalCostCalculationAPI.class);
 		
 		container=spy(new SolarSystem(calc,costcalc,new ROICalculator()));
 		when(container.getLocation()).thenReturn("4000");
 		
 		
 		PowerMockito.mockStatic(Database.class);
 		PowerMockito.mockStatic(DailySunProvider.class);
 		PowerMockito.mockStatic(PostcodeUtil.class);
 
 		
 		panel=mock(Panel.class);
 		inverter=mock(Inverter.class);
 		battery=mock(Battery.class);
 		
 		when(panel.getCapacity()).thenReturn(23d);
 		when(inverter.getEfficiency()).thenReturn(4242d);
 		
 		PowerMockito.when(Database.loadComponent(25, Panel.class)).thenReturn(panel);
 		PowerMockito.when(Database.loadComponent(-411, Panel.class)).thenThrow(EntityNotFoundException.class);
 
 		PowerMockito.when(Database.loadComponent(25, Inverter.class)).thenReturn(inverter);
 		PowerMockito.when(Database.loadComponent(-411, Inverter.class)).thenThrow(EntityNotFoundException.class);
 
 		PowerMockito.when(Database.loadComponent(25, Battery.class)).thenReturn(battery);
 		PowerMockito.when(Database.loadComponent(-411, Battery.class)).thenThrow(EntityNotFoundException.class);
 		
 		PowerMockito.when(PostcodeUtil.transformPostcode("4000")).thenReturn("4000");
 		
 		
 		
 
 	}
 	
 	@Test
 	public void testSetPanelId_PanelIsntSetAndValidPanelIdAsParameter_PanelIsSet(){
 		container.setPanelId(25);
 		assertEquals(panel, container.getPanel());
 	}
 	
 	@Test(expected = EntityNotFoundException.class)
 	public void testSetPanelId_nonExistingPanelId_ExceptionIsThrown(){
 		container.setPanelId(-411);
 	}
 	
 	@Test
 	public void testSetInverterId_InverterIsntSetAndValidInverterIdAsParameter_InverterIsSet(){
 		container.setInverterId(25);
 		assertEquals(inverter, container.getInverter());
 	}
 	
 	@Test(expected = EntityNotFoundException.class)
 	public void testSetInverterId_nonExistingInverterId_ExceptionIsThrown(){
 		container.setInverterId(-411);
 	}
 	
 	
 	@Test
 	public void testSetBatteryId_BatteryIsntSetAndValidBatteryIdAsParameter_BatteryIsSet(){
 		container.setBatteryId(25);
 		assertEquals(battery, container.getBattery());
 	}
 
 	@Test(expected = EntityNotFoundException.class)
 	public void testSetBatteryId_nonExistingBatteryId_ExceptionIsThrown(){
 		container.setBatteryId(-411);
 	}
 	
 	
 	@Test
 	public void testSetLocation_validLocation_locationIsSet(){
 		container.setLocation("4560");
 		
 		assertEquals("4000", container.getLocation());
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testSetLocation_invalidLocation_ExceptionIsThrown(){
 		when(PostcodeUtil.validatePostcode("SDIFW")).thenThrow(IllegalArgumentException.class);
 		container.setLocation("SDIFW");
 	}
 	
 	
 	
 	@Test
 	public void getElectricityProduction_validInput_dailySunIsFetchedAndUsed(){
 
 		PowerMockito.when(DailySunProvider.getDailySunByPostcode("4000")).thenReturn(24d);
 		PowerMockito.when(DailySunProvider.getDailySunLight("4000")).thenReturn(276d);
 		PowerMockito.when(PostcodeUtil.validatePostcode("4000")).thenReturn(true);

 		
 		when(calc.getElectricityProduction(anyDouble(), anyDouble(), anyDouble(), anyDouble(),anyDouble(), anyDouble())).thenReturn(42d);
 		
 		container.setBatteryId(25);
 		container.setInverterId(25);
 		container.setPanelId(25);
 		container.setLocation("4000");
 
 		container.getElectricityProduction();
 
 		
 		PowerMockito.verifyStatic();
 		DailySunProvider.getDailySunByPostcode("4000");
 		
 
 	}
 	
 	@Test
 	public void getElectricityProduction_validInput_sunIntensityIsFetchedAndUsed(){
 		PowerMockito.when(DailySunProvider.getDailySunByPostcode("4000")).thenReturn(24d);
 		PowerMockito.when(DailySunProvider.getDailySunLight("4000")).thenReturn(276d);
 		PowerMockito.when(PostcodeUtil.validatePostcode("4000")).thenReturn(true);
 
 		
 		when(calc.getElectricityProduction(anyDouble(), anyDouble(), anyDouble(), anyDouble(),anyDouble(), anyDouble())).thenReturn(42d);
 		
 		container.setBatteryId(25);
 		container.setInverterId(25);
 		container.setPanelId(25);
 		container.setLocation("4000");

 		container.getElectricityProduction();
 		
 		PowerMockito.verifyStatic();
 		DailySunProvider.getDailySunLight("4000");
 		
 
 	}
 	
 	@Test
 	public void parseDimensions_validDimension_CorrectResultIsReturned(){
 		
 		double res=container.parseDimension("2x5x3");
 		assertEquals(30/1000, res,0.00001);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void parseDimensions_IncorrectNumberOfParameters_ExceptionIsThrown(){
 		
 		container.parseDimension("2x2x5x3");
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void parseDimensions_NotNumber_ExceptionIsThrown(){
 		
 		container.parseDimension("fx5x3");
 	}
 }
