 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.historical.provider;
 
 import com.barchart.feed.ddf.historical.api.DDF_Entry;
 import com.barchart.feed.ddf.historical.api.DDF_EntryBarMin;
 import com.barchart.feed.ddf.historical.api.DDF_EntryTick;
 import com.barchart.feed.ddf.historical.api.DDF_EntryTickFormT;
 import com.barchart.feed.ddf.historical.api.DDF_EntryTrend;
 import com.barchart.feed.ddf.historical.enums.DDF_QueryType;
 import com.barchart.feed.ddf.instrument.api.DDF_Instrument;
 
 enum Builder {
 
 	TICKS {
 		@Override
 		public DDF_EntryTick newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryTicksDetail entry = new EntryTicksDetail(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 	
 	TICKS_FORM_T {
 		@Override
 		public DDF_EntryTickFormT newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryTicksFormT entry = new EntryTicksFormT(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	MINUTES {
 		@Override
 		public DDF_EntryBarMin newEntry(final int index,
 				final String inputLine, final DDF_Instrument instrument) {
 			final EntryMins entry = new EntryMins(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	MINUTES_NEARBY {
 		@Override
 		public EntryMinsNearby newEntry(final int index,
 				final String inputLine, final DDF_Instrument instrument) {
 			final EntryMinsNearby entry = new EntryMinsNearby(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	MINUTES_FORM_T {
 		@Override
 		public EntryMinsFormT newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryMinsFormT entry = new EntryMinsFormT(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	END_OF_DAY {
 		@Override
 		public EntryEod newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryEod entry = new EntryEod(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	//
 
 	TICKS_TREND {
 		@Override
 		public DDF_EntryTrend newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryTicksTrend entry = new EntryTicksTrend(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	MINUTES_TREND {
 		@Override
 		public DDF_EntryTrend newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryMinsTrend entry = new EntryMinsTrend(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	END_OF_DAY_TREND {
 		@Override
 		public DDF_EntryTrend newEntry(final int index, final String inputLine,
 				final DDF_Instrument instrument) {
 			final EntryEodTrend entry = new EntryEodTrend(instrument);
 			entry.decode(inputLine);
 			entry.index = index;
 			return entry;
 		}
 	}, //
 
 	;
 
 	abstract DDF_Entry newEntry(int index, final String inputLine,
 			final DDF_Instrument instrument);
 
 	static final Builder from(final DDF_QueryType<?> queryType) {
 
 		if (queryType.is(DDF_QueryType.TICKS)) {
 			return TICKS;
 		}
 
 		if (queryType.is(DDF_QueryType.MINUTES)) {
 			return MINUTES;
 		}
 
 		if (queryType.is(DDF_QueryType.MINUTES_NEARBY)) {
 			return MINUTES_NEARBY;
 		}
 
 		if (queryType.is(DDF_QueryType.MINUTES_FORM_T)) {
 			return MINUTES_FORM_T;
 		}
 
 		if (queryType.is(DDF_QueryType.END_OF_DAY)) {
 			return END_OF_DAY;
 		}
 
 		//
 
 		if (queryType.is(DDF_QueryType.TICKS_TREND)) {
 			return TICKS_TREND;
 		}
 
 		if (queryType.is(DDF_QueryType.MINUTES_TREND)) {
 			return MINUTES_TREND;
 		}
 
 		if (queryType.is(DDF_QueryType.END_OF_DAY_TREND)) {
 			return END_OF_DAY_TREND;
 		}
 
 		//
 
 		throw new IllegalArgumentException("unknonw queryType=" + queryType);
 
 	}
 
 }
