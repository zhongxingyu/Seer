 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.symbol.enums;
 
 import static com.barchart.feed.ddf.symbol.enums.DDF_ExchangeKind.FOREX;
 import static com.barchart.feed.ddf.symbol.enums.DDF_ExchangeKind.FUTURE;
 import static com.barchart.feed.ddf.symbol.enums.DDF_ExchangeKind.INDEX;
 import static com.barchart.feed.ddf.symbol.enums.DDF_ExchangeKind.STOCK;
 import static com.barchart.util.common.ascii.ASCII.DOLLAR;
 import static com.barchart.util.common.ascii.ASCII.QUEST;
 import static com.barchart.util.common.ascii.ASCII.UNDER;
 import static com.barchart.util.common.ascii.ASCII._0_;
 import static com.barchart.util.common.ascii.ASCII._1_;
 import static com.barchart.util.common.ascii.ASCII._2_;
 import static com.barchart.util.common.ascii.ASCII._3_;
 import static com.barchart.util.common.ascii.ASCII._4_;
 import static com.barchart.util.common.ascii.ASCII._5_;
 import static com.barchart.util.common.ascii.ASCII._6_;
 import static com.barchart.util.common.ascii.ASCII._7_;
 import static com.barchart.util.common.ascii.ASCII._8_;
 import static com.barchart.util.common.ascii.ASCII._9_;
 import static com.barchart.util.common.ascii.ASCII._A_;
 import static com.barchart.util.common.ascii.ASCII._B_;
 import static com.barchart.util.common.ascii.ASCII._C_;
 import static com.barchart.util.common.ascii.ASCII._D_;
 import static com.barchart.util.common.ascii.ASCII._E_;
 import static com.barchart.util.common.ascii.ASCII._F_;
 import static com.barchart.util.common.ascii.ASCII._G_;
 import static com.barchart.util.common.ascii.ASCII._H_;
 import static com.barchart.util.common.ascii.ASCII._I_;
 import static com.barchart.util.common.ascii.ASCII._J_;
 import static com.barchart.util.common.ascii.ASCII._K_;
 import static com.barchart.util.common.ascii.ASCII._L_;
 import static com.barchart.util.common.ascii.ASCII._M_;
 import static com.barchart.util.common.ascii.ASCII._N_;
 import static com.barchart.util.common.ascii.ASCII._O_;
 import static com.barchart.util.common.ascii.ASCII._P_;
 import static com.barchart.util.common.ascii.ASCII._Q_;
 import static com.barchart.util.common.ascii.ASCII._R_;
 import static com.barchart.util.common.ascii.ASCII._S_;
 import static com.barchart.util.common.ascii.ASCII._T_;
 import static com.barchart.util.common.ascii.ASCII._U_;
 import static com.barchart.util.common.ascii.ASCII._V_;
 import static com.barchart.util.common.ascii.ASCII._W_;
 import static com.barchart.util.common.ascii.ASCII._X_;
 import static com.barchart.util.common.ascii.ASCII._Y_;
 import static com.barchart.util.common.ascii.ASCII._Z_;
 import static com.barchart.util.common.ascii.ASCII._a_;
 import static com.barchart.util.common.ascii.ASCII._b_;
 import static com.barchart.util.common.ascii.ASCII._c_;
 import static com.barchart.util.common.ascii.ASCII._d_;
 import static com.barchart.util.common.ascii.ASCII._e_;
 import static com.barchart.util.common.ascii.ASCII._f_;
 import static com.barchart.util.common.ascii.ASCII._g_;
 import static com.barchart.util.common.ascii.ASCII._h_;
 import static com.barchart.util.common.ascii.ASCII._i_;
 import static com.barchart.util.common.ascii.ASCII._j_;
 import static com.barchart.util.common.ascii.ASCII._k_;
 import static com.barchart.util.common.ascii.ASCII._l_;
 import static com.barchart.util.common.ascii.ASCII._m_;
 import static com.barchart.util.common.ascii.ASCII._n_;
 import static com.barchart.util.common.ascii.ASCII._o_;
 import static com.barchart.util.common.ascii.ASCII._p_;
 import static com.barchart.util.common.ascii.ASCII._q_;
 import static com.barchart.util.common.ascii.ASCII._r_;
 import static com.barchart.util.common.ascii.ASCII._s_;
 import static com.barchart.util.common.ascii.ASCII._t_;
 import static com.barchart.util.common.ascii.ASCII._u_;
 import static com.barchart.util.common.ascii.ASCII._v_;
 import static com.barchart.util.common.ascii.ASCII._w_;
 import static com.barchart.util.common.ascii.ASCII._x_;
 import static com.barchart.util.common.ascii.ASCII._y_;
 import static com.barchart.util.common.ascii.ASCII._z_;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.feed.api.model.meta.Exchange;
 import com.barchart.feed.base.values.api.Value;
 import com.barchart.feed.inst.provider.Exchanges;
 import com.barchart.util.common.math.MathExtra;
 
 // TODO: Auto-generated Javadoc
 /**
  * dff market exchange channels.
  */
 public enum DDF_Exchange implements Value<DDF_Exchange> {
 
 	TEST(UNDER, FUTURE), //
 
 	Fix_Me_0(_0_, FUTURE), //
 	Fix_Me_1(_1_, FUTURE), //
 	Fix_Me_2(_2_, FUTURE), //
 	Fix_Me_3(_3_, FUTURE), //
 	Fix_Me_4(_4_, FUTURE), //
 	Fix_Me_5(_5_, FUTURE), //
 	Fix_Me_6(_6_, FUTURE), //
 	Fix_Me_7(_7_, FUTURE), //
 	Fix_Me_8(_8_, FUTURE), //
 	Fix_Me_9(_9_, FUTURE), //
 
 	AMEX(_A_, STOCK), // XASE
 	AMEX_Book_Top(_a_, STOCK), // 
 
 	CME_CBOT(_B_, FUTURE), // XCBT
 	CME_CBOT_XXX(_b_, FUTURE), //
 
 	ICE_US(_C_, FUTURE), // IFUS
 	CME_Special_Products(_c_, FUTURE), //
 
 	NASDAQ_OTC_BB(_D_, STOCK), //
 	NASDAQ_OTC_BB_XXX(_d_, STOCK), //
 
 	CME_COMEX(_E_, FUTURE), //
 	CME_COMEX_XXX(_e_, FUTURE), //
 
 	Mutual_Funds(_F_, INDEX), //
 	Money_Market_Funds(_f_, INDEX), //
 
 	CME_MGEX(_G_, FUTURE), //
 	CME_MGEX_XXX(_g_, FUTURE), //
 
 	Fix_Me_H(_H_, FUTURE), //
 	Fix_Me_H_XXX(_h_, FUTURE), //
 
	Index_NO_DOW_NO_SP(_I_, FUTURE), //
	Index_NO_DOW_NO_SP_XXX(_i_, FUTURE), //
 
 	CME_NYMEX(_J_, FUTURE), //
 	CME_NYMEX_Swaps(_j_, FUTURE), //
 
 	CME_KBOT(_K_, FUTURE), //
 	CME_KBOT_XXX(_k_, FUTURE), //
 
 	ICE_EU(_L_, FUTURE), //
 	ICE_EU_XXX(_l_, FUTURE), //
 
 	CME_Main(_M_, FUTURE), //
 	CME_Weather(_m_, FUTURE), //
 
 	NYSE(_N_, STOCK), //
 	NYSE_Book_Top(_n_, STOCK), //
 
 	Index_DOW(_O_, INDEX), //
 	Index_DOW_Full(_o_, INDEX), //
 
 	Index_SP(_P_, INDEX), //
 	Index_SP_XXX(_p_, INDEX), // XXX
 
 	NASDAQ(_Q_, STOCK), //
 	NASDAQ_Book_Top(_q_, STOCK), //
 
 	CBOE_Futures(_R_, FUTURE), //
 	CBOE_Index(_r_, INDEX), //  changed from FUTURE
 
 	Fix_Me_S(_S_, FUTURE), //
 	Fix_Me_S_XXX(_s_, FUTURE), //
 
 	Fix_Me_T(_T_, FUTURE), //
 	Fix_Me_T_XXX(_t_, FUTURE), //
 
 	NASDAQ_OTC_PinkSheets(_U_, STOCK), //
 	NASDAQ_OTC_PinkSheets_XXX(_u_, STOCK), //
 
 	Fix_Me_V(_V_, FUTURE), //
 	Fix_Me_V_XXX(_v_, FUTURE), //
 
 	ICE_Canada(_W_, FUTURE), //
 	ICE_Canada_XXX(_w_, FUTURE), //
 
 	Fix_Me_X(_X_, FUTURE), // XXX
 	Fix_Me_X_XXX(_x_, FUTURE), // XXX
 
 	NYSE_Metals(_Y_, FUTURE), //
 	NYSE_Metals_XXX(_y_, FUTURE), //
 
 	Futures_Books(_Z_, FUTURE), //
 	Futures_Books_XXX(_z_, FUTURE), //
 
 	Forex(DOLLAR, FOREX), //
 
 	//
 
 	UNKNOWN(QUEST, DDF_ExchangeKind.UNKNOWN), //
 
 	/** The ord. */
  ;
 
 	
 	private static final Logger log = LoggerFactory
 			.getLogger(DDF_Exchange.class);
 	
 	// ////////////////////////
 
 	/** byte sized enum ordinal */
 	public final byte ord;
 
 	/** ddf encoding of this enum. */
 	public final byte code;
 
 	/** ddf "kind" qualifier of this ddf exchange channel. */
 	public final DDF_ExchangeKind kind;
 
 	/** free style exchange description; can be used in full text search. */
 	public final String description;
 
 	// ////////////////////////
 
 	private DDF_Exchange(final byte code, final DDF_ExchangeKind kind) {
 
 		this.ord = (byte) ordinal();
 		this.code = code;
 		this.kind = kind;
 
 		this.description = description();
 	}
 
 	/** TODO use real human readable description as ENUM property */
 	private String description() {
 		return name().replaceAll("_", " ");
 	}
 
 	private final static DDF_Exchange[] ENUM_VALUES = values();
 
 	static {
 		// validate use of byte ord
 		MathExtra.castIntToByte(ENUM_VALUES.length);
 	}
 
 	/**
 	 * From code.
 	 *
 	 * @param code the code
 	 * @return the dD f_ exchange
 	 */
 	public final static DDF_Exchange fromCode(final byte code) {
 		for (final DDF_Exchange known : ENUM_VALUES) {
 			if (known.code == code) {
 				return known;
 			}
 		}
 		return UNKNOWN;
 	}
 
 	/**
 	 * From ord.
 	 *
 	 * @param ord the ord
 	 * @return the dD f_ exchange
 	 */
 	public final static DDF_Exchange fromOrd(final byte ord) {
 		return ENUM_VALUES[ord];
 	}
 	
 	//TODO
 	public static final DDF_Exchange fromMICCode(final String code) {
 		return null;
 	}
 
 	/**
 	 * Checks if is known.
 	 *
 	 * @return true, if is known
 	 */
 	public final boolean isKnown() {
 		return this != UNKNOWN;
 	}
 
 	boolean isNYSE() {
 		switch (this) {
 		case AMEX:
 		case AMEX_Book_Top:
 		case NYSE:
 		case NYSE_Book_Top:
 		case NYSE_Metals:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Checks if is nASDAQ.
 	 *
 	 * @return true, if is nASDAQ
 	 */
 	public final boolean isNASDAQ() {
 		switch (this) {
 		case NASDAQ:
 		case NASDAQ_Book_Top:
 		case NASDAQ_OTC_PinkSheets:
 		case NASDAQ_OTC_BB:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#freeze()
 	 */
 	@Override
 	public DDF_Exchange freeze() {
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#isFrozen()
 	 */
 	@Override
 	public boolean isFrozen() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.util.values.api.Value#isNull()
 	 */
 	@Override
 	public boolean isNull() {
 		return this == UNKNOWN;
 	}
 	
 	public Exchange asExchange() {
 		return Exchanges.fromCode(new String(new byte[]{code}));
 	}
 	
 }
