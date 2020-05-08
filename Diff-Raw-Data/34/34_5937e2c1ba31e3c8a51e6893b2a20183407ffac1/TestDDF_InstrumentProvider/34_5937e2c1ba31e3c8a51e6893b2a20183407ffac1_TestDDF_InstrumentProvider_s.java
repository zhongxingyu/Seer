 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.instrument.provider;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.barchart.feed.ddf.instrument.api.DDF_Instrument;
 import com.barchart.feed.ddf.instrument.enums.DDF_InstrumentField;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TestDDF_InstrumentProvider.
  */
 public class TestDDF_InstrumentProvider {
 
 	/**
 	 * Test0.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void test0() throws Exception {
 
 		final List<String> symbolList = new ArrayList<String>();
 
 		symbolList.add("IBM");
 		symbolList.add("ORCL");
 		symbolList.add("MSFT");
		symbolList.add("ESZ2");
		symbolList.add("RJZ2");
		symbolList.add("YGZ2");
 		symbolList.add("_S_FX_A6H2_A6Z1");
 
 		final List<DDF_Instrument> list = DDF_InstrumentProvider
 				.remoteLookup(symbolList);
 
		assertEquals(7, list.size());
 
 		for (final DDF_Instrument instrument : list) {
 
 			System.out.println(instrument);
 
 		}
 
 	}
 
 	/**
 	 * Test1.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void test1() throws Exception {
 
 		final List<String> symbolList = new ArrayList<String>();
 
 		symbolList.add("IBM");
 		symbolList.add("ORCL");
 		symbolList.add("MSFT");
		symbolList.add("ESZ2");
		symbolList.add("RJZ2");
		symbolList.add("YGZ2");
 		symbolList.add("_S_FX_A6H2_A6Z1");
 		symbolList.add("_S_BF_ZSQ2_ZSU2_ZSX2");
 
 		final List<DDF_Instrument> list = DDF_InstrumentProvider
 				.find(symbolList);
 
		assertEquals(8, list.size());
 
 		for (final DDF_Instrument instrument : list) {
 
 			System.out.println("EXP Month Year DDF = "
 					+ instrument.get(DDF_InstrumentField.DDF_EXPIRE_MONTH)
 							.toString()
 					+ " "
 					+ instrument.get(DDF_InstrumentField.DDF_EXPIRE_YEAR)
 							.toString());
 
 			System.out.println(instrument);
 
 		}
 
 	}
 
 }
