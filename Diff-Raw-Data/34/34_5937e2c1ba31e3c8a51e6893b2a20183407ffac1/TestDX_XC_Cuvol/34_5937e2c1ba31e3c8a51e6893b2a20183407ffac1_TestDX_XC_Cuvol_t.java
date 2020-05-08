 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.provider;
 
 import static com.barchart.util.ascii.ASCII.ASCII_CHARSET;
 import static com.barchart.util.values.provider.ValueBuilder.newPrice;
 import static com.barchart.util.values.provider.ValueBuilder.newSize;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.barchart.feed.base.cuvol.api.MarketCuvolEntry;
 import com.barchart.feed.base.provider.DefCuvolEntry;
 import com.barchart.feed.ddf.message.enums.DDF_MessageType;
 import com.barchart.feed.ddf.message.enums.DDF_Session;
 import com.barchart.feed.ddf.message.enums.DDF_TradeDay;
 import com.barchart.feed.ddf.symbol.enums.DDF_Exchange;
 import com.barchart.feed.ddf.symbol.enums.DDF_SpreadType;
 import com.barchart.feed.ddf.util.enums.DDF_Fraction;
 import com.barchart.util.values.api.PriceValue;
 import com.barchart.util.values.api.SizeValue;
 import com.barchart.util.values.provider.ValueBuilder;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TestDX_XC_Cuvol.
  */
 public class TestDX_XC_Cuvol extends TestDDFBase {
 
 	/**
 	 * Sets the up.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * Tear down.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	// XC spread
	final byte[] baXC0sp = "%<CV symbol=\"GEM0_GEZ0\" basecode=\"A\" tickincrement=\"25\" last=\"109975\" lastsize=\"2\" lastcvol=\"12\" date=\"20100615123857\" count=\"54\" data=\"109275,1076:108900,437:109950,3813:109325,1429:108950,899:109925,6629:109875,5131:109225,1007:109800,5046:109125,1327:109075,1865:109750,5931:110050,2579:109025,1616:109975,2886:109200,1619:109550,875:109300,2010:109525,1691:109375,1504:109850,5318:109450,1163:109500,1899:109000,1906:110150,209:109625,2055:108850,38:109700,3787:110175,219:109350,1901:108975,476:109600,3659:109575,1799:110025,3751:110075,3451:110000,5387:109825,5224:109900,10427:109775,4809:109650,2887:109050,1817:109725,4396:109175,1807:109100,1835:108875,110:109400,1777:109425,1617:109150,1662:109475,938:110125,446:109250,1215:110100,2035:108925,751:109675,2493\"/>"
 			.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode spread xml.
 	 */
 	@Test
 	public void testDecodeSpreadXML() {
 
 		final DX_XC_Cuvol msg = new DX_XC_Cuvol();
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXC0sp);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
		assertEquals(msg.getId(), "GEM0_GEZ0");
 		assertEquals(msg.getSpreadType(), DDF_SpreadType.DEFAULT);
 		assertEquals(msg.getFraction(), DDF_Fraction.N2);
 		assertEquals(msg.getExchange(), DDF_Exchange.UNKNOWN);
 		assertEquals(msg.getMessageType(), DDF_MessageType.CUVOL_SNAP_XML);
 		assertEquals(msg.getTradeDay(), DDF_TradeDay.D15);
 		assertEquals(msg.getSession(), DDF_Session.FUT_COMBO);
 		assertEquals(msg.getDelay(), 0);
 
 	}
 
 	// XC
	final byte[] baXC0 = "%<CV symbol=\"GEM1\" basecode=\"A\" tickincrement=\"25\" last=\"109975\" lastsize=\"2\" lastcvol=\"12\" date=\"20100615123857\" count=\"54\" data=\"109275,1076:108900,437:109950,3813:109325,1429:108950,899:109925,6629:109875,5131:109225,1007:109800,5046:109125,1327:109075,1865:109750,5931:110050,2579:109025,1616:109975,2886:109200,1619:109550,875:109300,2010:109525,1691:109375,1504:109850,5318:109450,1163:109500,1899:109000,1906:110150,209:109625,2055:108850,38:109700,3787:110175,219:109350,1901:108975,476:109600,3659:109575,1799:110025,3751:110075,3451:110000,5387:109825,5224:109900,10427:109775,4809:109650,2887:109050,1817:109725,4396:109175,1807:109100,1835:108875,110:109400,1777:109425,1617:109150,1662:109475,938:110125,446:109250,1215:110100,2035:108925,751:109675,2493\"/>"
 			.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode xml.
 	 */
 	@Test
 	public void testDecodeXML() {
 
 		final DX_XC_Cuvol msg = new DX_XC_Cuvol();
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXC0);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
		assertEquals(msg.getId(), "GEM1");
 		assertEquals(msg.getSpreadType(), DDF_SpreadType.UNKNOWN);
 		assertEquals(msg.getFraction(), DDF_Fraction.N2);
 		assertEquals(msg.getExchange(), DDF_Exchange.CME_Main);
 		assertEquals(msg.getMessageType(), DDF_MessageType.CUVOL_SNAP_XML);
 		assertEquals(msg.getTradeDay(), DDF_TradeDay.D15);
 		assertEquals(msg.getSession(), DDF_Session.FUT_COMBO);
 		assertEquals(msg.getDelay(), 0);
 
 		// TODO
 		// assertEquals(msg.getTime(), newTime(0));
 
 		//
 
 		assertEquals(msg.getPriceStep(), newPrice(25, -2));
 
 		assertEquals(msg.getPriceLast(), newPrice(109975, -2));
 		assertEquals(msg.getSizeLast(), newSize(2));
 		assertEquals(msg.getSizeLastCuvol(), newSize(12));
 
 		//
 
 		final MarketCuvolEntry[] entries = msg.entries();
 
 		assertEquals(54, entries.length);
 
 		assertEquals(entries[0], newEntry(1, 108850, -2, 38));
 		assertEquals(entries[1], newEntry(2, 108875, -2, 110));
 		assertEquals(entries[2], newEntry(3, 108900, -2, 437));
 		assertEquals(entries[3], newEntry(4, 108925, -2, 751));
 		assertEquals(entries[4], newEntry(5, 108950, -2, 899));
 		assertEquals(entries[5], newEntry(6, 108975, -2, 476));
 		assertEquals(entries[6], newEntry(7, 109000, -2, 1906));
 		assertEquals(entries[7], newEntry(8, 109025, -2, 1616));
 		assertEquals(entries[8], newEntry(9, 109050, -2, 1817));
 		assertEquals(entries[9], newEntry(10, 109075, -2, 1865));
 
 		/*
 		 */
 
 		/*
 		 */
 
 	}
 
 	private MarketCuvolEntry newEntry(final int place, final long mant,
 			final int exp, final long sizeValue) {
 		final PriceValue price = ValueBuilder.newPrice(mant, exp);
 		final SizeValue size = ValueBuilder.newSize(sizeValue);
 		final MarketCuvolEntry entry = new DefCuvolEntry(place, price, size);
 		return entry;
 	}
 
	final byte[] baXC1 = "%<CV basecode=\"A\" count=\"54\" data=\"108850,38:108875,110:108900,437:108925,751:108950,899:108975,476:109000,1906:109025,1616:109050,1817:109075,1865:109100,1835:109125,1327:109150,1662:109175,1807:109200,1619:109225,1007:109250,1215:109275,1076:109300,2010:109325,1429:109350,1901:109375,1504:109400,1777:109425,1617:109450,1163:109475,938:109500,1899:109525,1691:109550,875:109575,1799:109600,3659:109625,2055:109650,2887:109675,2493:109700,3787:109725,4396:109750,5931:109775,4809:109800,5046:109825,5224:109850,5318:109875,5131:109900,10427:109925,6629:109950,3813:109975,2886:110000,5387:110025,3751:110050,2579:110075,3451:110100,2035:110125,446:110150,209:110175,219\" date=\"20100615123857\" last=\"109975\" lastcvol=\"12\" lastsize=\"2\" symbol=\"GEM0\" tickincrement=\"25\"/>"
 			.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode encode xml.
 	 */
 	@Test
 	public void testDecodeEncodeXML() {
 
 		final DX_XC_Cuvol msg = new DX_XC_Cuvol();
 
 		final ByteBuffer source = ByteBuffer.wrap(baXC1);
 
 		source.get(); // position + 1
 
 		msg.decodeXML(source);
 
 		final ByteBuffer target = ByteBuffer.allocate(baXC1.length);
 
 		target.put((byte) '%'); // position + 1
 
 		msg.encodeXML(target);
 
 		// System.out.println(new String(target.array()));
 
 		final byte[] arraySource = source.array();
 		final byte[] arrayTarget = target.array();
 
 		// System.out.println("source=" + new String(arraySource));
 		// System.out.println("target=" + new String(arrayTarget));
 
 		assertTrue(Arrays.equals(arraySource, arrayTarget));
 
 	}
 
 	/**
 	 * Test encode empty.
 	 */
 	@Test
 	public void testEncodeEmpty() {
 
 		final DX_XC_Cuvol msg = new DX_XC_Cuvol();
 
 		final ByteBuffer buffer = ByteBuffer.allocate(46);
 
 		msg.encodeXML(buffer);
 
 		System.out.println("place=" + buffer.position());
 		System.out.println("limit=" + buffer.limit());
 		System.out.println("" + new String(buffer.array()));
 
 		final String result = "<CV basecode=\"?\" count=\"0\" data=\"\" symbol=\"\"/>";
 
 		final byte[] arraySource = buffer.array();
 		final byte[] arrayTarget = result.getBytes(ASCII_CHARSET);
 		assertTrue(Arrays.equals(arraySource, arrayTarget));
 
 	}
 
 }
