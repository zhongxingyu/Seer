 package fr.cg95.cvq.business.users;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Enumeration of all coutries.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public enum CountryType {
 
      UNKNOWN("Unknown"),
      AF("af"),
      ZA("za"),
      AL("al"),
      DZ("dz"),
      DE("de"),
      AD("ad"),
      AO("ao"),
      AI("ai"),
      AQ("aq"),
      AG("ag"),
      AN("an"),
      SA("sa"),
      AR("ar"),
      AM("am"),
      AW("aw"),
      AU("au"),
      AT("at"),
      AZ("az"),
      BJ("bj"),
      BS("bs"),
      BH("bh"),
      BD("bd"),
      BB("bb"),
      PW("pw"),
      BE("be"),
      BZ("bz"),
      BM("bm"),
      BT("bt"),
      BY("by"),
      MM("mm"),
      BO("bo"),
      BA("ba"),
      BW("bw"),
      BR("br"),
      BN("bn"),
      BG("bg"),
      BF("bf"),
      BI("bi"),
      CI("ci"),
      KH("kh"),
      CM("cm"),
      CA("ca"),
      CV("cv"),
      CL("cl"),
      CN("cn"),
      CY("cy"),
      CO("co"),
      KM("km"),
      CG("cg"),
      KP("kp"),
      KR("kr"),
      CR("cr"),
      HR("hr"),
      CU("cu"),
      DK("dk"),
      DJ("dj"),
      DM("dm"),
      EG("eg"),
      AE("ae"),
      EC("ec"),
      ER("er"),
      ES("es"),
      EE("ee"),
      US("us"),
      ET("et"),
      FI("fi"),
      FR("fr"),
      GE("ge"),
      GA("ga"),
      GM("gm"),
      GH("gh"),
      GI("gi"),
      GR("gr"),
      GD("gd"),
      GL("gl"),
      GP("gp"),
      GU("gu"),
      GT("gt"),
      GN("gn"),
      GQ("gq"),
      GW("gw"),
      GY("gy"),
      GF("gf"),
      HT("ht"),
      HN("hn"),
      HK("hk"),
      HU("hu"),
      CK("ck"),
      FJ("fj"),
      MH("mh"),
      SB("sb"),
      IN("in"),
      ID("id"),
      IR("ir"),
      IQ("iq"),
      IE("ie"),
      IS("is"),
      IL("il"),
      IT("it"),
      JM("jm"),
      JP("jp"),
      JO("jo"),
      KZ("kz"),
      KE("ke"),
      KG("kg"),
      KI("ki"),
      KW("kw"),
      LA("la"),
      LS("ls"),
      LV("lv"),
      LB("lb"),
      LR("lr"),
      LY("ly"),
      LI("li"),
      LT("lt"),
      LU("lu"),
      MG("mg"),
      MY("my"),
      MW("mw"),
      MV("mv"),
      ML("ml"),
      MT("mt"),
      MA("ma"),
      MU("mu"),
      MR("mr"),
      MX("mx"),
      FM("fm"),
      MD("md"),
      MC("mc"),
      MN("mn"),
      MZ("mz"),
      NP("np"),
      NA("na"),
      NR("nr"),
      NI("ni"),
      NE("ne"),
      NG("ng"),
      NU("nu"),
      NO("no"),
      NZ("nz"),
      OM("om"),
      UG("ug"),
      UZ("uz"),
      PE("pe"),
      PK("pk"),
      PA("pa"),
      PG("pg"),
      PY("py"),
      NL("nl"),
      PH("ph"),
      PL("pl"),
      PT("pt"),
      QA("qa"),
      CF("cf"),
      CD("cd"),
      DO("do"),
      CZ("cz"),
      RO("ro"),
      GB("gb"),
      RU("ru"),
      RW("rw"),
      SN("sn"),
      KN("kn"),
      SM("sm"),
      VA("va"),
      VC("vc"),
      LC("lc"),
      SV("sv"),
      WS("ws"),
      ST("st"),
      SC("sc"),
      SL("sl"),
      SG("sg"),
      SI("si"),
      SK("sk"),
      SO("so"),
      SD("sd"),
      LK("lk"),
      SE("se"),
      CH("ch"),
      SR("sr"),
      SZ("sz"),
      SY("sy"),
      TW("tw"),
      TJ("tj"),
      TZ("tz"),
      TD("td"),
      TH("th"),
      TL("tl"),
      TG("tg"),
      TO("to"),
      VT("vt"),
      TN("tn"),
      TM("tm"),
      TR("tr"),
      TV("tv"),
      UA("ua"),
      UY("uy"),
      VU("vu"),
      VE("ve"),
      VN("vn"),
      YE("ye"),
      ZM("zm"),
      ZW("zw"),
      MK("mk");
 
     public static CountryType[] allCountryTypes = CountryType.values();
 
     private String legacyLabel;
 
     public String getLegacyLabel() {
         return legacyLabel;
     }
 
     private CountryType(String legacyLabel) {
        this.legacyLabel = legacyLabel;
     }
 
     public static CountryType getDefaultCountryType() {
         return UNKNOWN;
     }
 
     public static CountryType forString(final String enumAsString) {
         if (enumAsString == null || enumAsString.equals(""))
             return getDefaultCountryType();
 
         if (enumAsString.equals(UNKNOWN.toString()))
             return UNKNOWN;
         else if (enumAsString.equals(AF.toString()))
             return AF;
         else if (enumAsString.equals(ZA.toString()))
             return ZA;
         else if (enumAsString.equals(AL.toString()))
             return AL;
         else if (enumAsString.equals(DZ.toString()))
             return DZ;
         else if (enumAsString.equals(DE.toString()))
             return DE;
         else if (enumAsString.equals(AD.toString()))
             return AD;
         else if (enumAsString.equals(AO.toString()))
             return AO;
         else if (enumAsString.equals(AI.toString()))
             return AI;
         else if (enumAsString.equals(AQ.toString()))
             return AQ;
         else if (enumAsString.equals(AG.toString()))
             return AG;
         else if (enumAsString.equals(AN.toString()))
             return AN;
         else if (enumAsString.equals(SA.toString()))
             return SA;
         else if (enumAsString.equals(AR.toString()))
             return AR;
         else if (enumAsString.equals(AM.toString()))
             return AM;
         else if (enumAsString.equals(AW.toString()))
             return AW;
         else if (enumAsString.equals(AU.toString()))
             return AU;
         else if (enumAsString.equals(AT.toString()))
             return AT;
         else if (enumAsString.equals(AZ.toString()))
             return AZ;
         else if (enumAsString.equals(BJ.toString()))
             return BJ;
         else if (enumAsString.equals(BS.toString()))
             return BS;
         else if (enumAsString.equals(BH.toString()))
             return BH;
         else if (enumAsString.equals(BD.toString()))
             return BD;
         else if (enumAsString.equals(BB.toString()))
             return BB;
         else if (enumAsString.equals(PW.toString()))
             return PW;
         else if (enumAsString.equals(BE.toString()))
             return BE;
         else if (enumAsString.equals(BZ.toString()))
             return BZ;
         else if (enumAsString.equals(BM.toString()))
             return BM;
         else if (enumAsString.equals(BT.toString()))
             return BT;
         else if (enumAsString.equals(BY.toString()))
             return BY;
         else if (enumAsString.equals(MM.toString()))
             return MM;
         else if (enumAsString.equals(BO.toString()))
             return BO;
         else if (enumAsString.equals(BA.toString()))
             return BA;
         else if (enumAsString.equals(BW.toString()))
             return BW;
         else if (enumAsString.equals(BR.toString()))
             return BR;
         else if (enumAsString.equals(BN.toString()))
             return BN;
         else if (enumAsString.equals(BG.toString()))
             return BG;
         else if (enumAsString.equals(BF.toString()))
             return BF;
         else if (enumAsString.equals(BI.toString()))
             return BI;
         else if (enumAsString.equals(CI.toString()))
             return CI;
         else if (enumAsString.equals(KH.toString()))
             return KH;
         else if (enumAsString.equals(CM.toString()))
             return CM;
         else if (enumAsString.equals(CA.toString()))
             return CA;
         else if (enumAsString.equals(CV.toString()))
             return CV;
         else if (enumAsString.equals(CL.toString()))
             return CL;
         else if (enumAsString.equals(CN.toString()))
             return CN;
         else if (enumAsString.equals(CY.toString()))
             return CY;
         else if (enumAsString.equals(CO.toString()))
             return CO;
         else if (enumAsString.equals(KM.toString()))
             return KM;
         else if (enumAsString.equals(CG.toString()))
             return CG;
         else if (enumAsString.equals(KP.toString()))
             return KP;
         else if (enumAsString.equals(KR.toString()))
             return KR;
         else if (enumAsString.equals(CR.toString()))
             return CR;
         else if (enumAsString.equals(HR.toString()))
             return HR;
         else if (enumAsString.equals(CU.toString()))
             return CU;
         else if (enumAsString.equals(DK.toString()))
             return DK;
         else if (enumAsString.equals(DJ.toString()))
             return DJ;
         else if (enumAsString.equals(DM.toString()))
             return DM;
         else if (enumAsString.equals(EG.toString()))
             return EG;
         else if (enumAsString.equals(AE.toString()))
             return AE;
         else if (enumAsString.equals(EC.toString()))
             return EC;
         else if (enumAsString.equals(ER.toString()))
             return ER;
         else if (enumAsString.equals(ES.toString()))
             return ES;
         else if (enumAsString.equals(EE.toString()))
             return EE;
         else if (enumAsString.equals(US.toString()))
             return US;
         else if (enumAsString.equals(ET.toString()))
             return ET;
         else if (enumAsString.equals(FI.toString()))
             return FI;
         else if (enumAsString.equals(FR.toString()))
             return FR;
         else if (enumAsString.equals(GE.toString()))
             return GE;
         else if (enumAsString.equals(GA.toString()))
             return GA;
         else if (enumAsString.equals(GM.toString()))
             return GM;
         else if (enumAsString.equals(GH.toString()))
             return GH;
         else if (enumAsString.equals(GI.toString()))
             return GI;
         else if (enumAsString.equals(GR.toString()))
             return GR;
         else if (enumAsString.equals(GD.toString()))
             return GD;
         else if (enumAsString.equals(GL.toString()))
             return GL;
         else if (enumAsString.equals(GP.toString()))
             return GP;
         else if (enumAsString.equals(GU.toString()))
             return GU;
         else if (enumAsString.equals(GT.toString()))
             return GT;
         else if (enumAsString.equals(GN.toString()))
             return GN;
         else if (enumAsString.equals(GQ.toString()))
             return GQ;
         else if (enumAsString.equals(GW.toString()))
             return GW;
         else if (enumAsString.equals(GY.toString()))
             return GY;
         else if (enumAsString.equals(GF.toString()))
             return GF;
         else if (enumAsString.equals(HT.toString()))
             return HT;
         else if (enumAsString.equals(HN.toString()))
             return HN;
         else if (enumAsString.equals(HK.toString()))
             return HK;
         else if (enumAsString.equals(HU.toString()))
             return HU;
         else if (enumAsString.equals(CK.toString()))
             return CK;
         else if (enumAsString.equals(FJ.toString()))
             return FJ;
         else if (enumAsString.equals(MH.toString()))
             return MH;
         else if (enumAsString.equals(SB.toString()))
             return SB;
         else if (enumAsString.equals(IN.toString()))
             return IN;
         else if (enumAsString.equals(ID.toString()))
             return ID;
         else if (enumAsString.equals(IR.toString()))
             return IR;
         else if (enumAsString.equals(IQ.toString()))
             return IQ;
         else if (enumAsString.equals(IE.toString()))
             return IE;
         else if (enumAsString.equals(IS.toString()))
             return IS;
         else if (enumAsString.equals(IL.toString()))
             return IL;
         else if (enumAsString.equals(IT.toString()))
             return IT;
         else if (enumAsString.equals(JM.toString()))
             return JM;
         else if (enumAsString.equals(JP.toString()))
             return JP;
         else if (enumAsString.equals(JO.toString()))
             return JO;
         else if (enumAsString.equals(KZ.toString()))
             return KZ;
         else if (enumAsString.equals(KE.toString()))
             return KE;
         else if (enumAsString.equals(KG.toString()))
             return KG;
         else if (enumAsString.equals(KI.toString()))
             return KI;
         else if (enumAsString.equals(KW.toString()))
             return KW;
         else if (enumAsString.equals(LA.toString()))
             return LA;
         else if (enumAsString.equals(LS.toString()))
             return LS;
         else if (enumAsString.equals(LV.toString()))
             return LV;
         else if (enumAsString.equals(LB.toString()))
             return LB;
         else if (enumAsString.equals(LR.toString()))
             return LR;
         else if (enumAsString.equals(LY.toString()))
             return LY;
         else if (enumAsString.equals(LI.toString()))
             return LI;
         else if (enumAsString.equals(LT.toString()))
             return LT;
         else if (enumAsString.equals(LU.toString()))
             return LU;
         else if (enumAsString.equals(MG.toString()))
             return MG;
         else if (enumAsString.equals(MY.toString()))
             return MY;
         else if (enumAsString.equals(MW.toString()))
             return MW;
         else if (enumAsString.equals(MV.toString()))
             return MV;
         else if (enumAsString.equals(ML.toString()))
             return ML;
         else if (enumAsString.equals(MT.toString()))
             return MT;
         else if (enumAsString.equals(MA.toString()))
             return MA;
         else if (enumAsString.equals(MU.toString()))
             return MU;
         else if (enumAsString.equals(MR.toString()))
             return MR;
         else if (enumAsString.equals(MX.toString()))
             return MX;
         else if (enumAsString.equals(FM.toString()))
             return FM;
         else if (enumAsString.equals(MD.toString()))
             return MD;
         else if (enumAsString.equals(MC.toString()))
             return MC;
         else if (enumAsString.equals(MN.toString()))
             return MN;
         else if (enumAsString.equals(MZ.toString()))
             return MZ;
         else if (enumAsString.equals(NP.toString()))
             return NP;
         else if (enumAsString.equals(NA.toString()))
             return NA;
         else if (enumAsString.equals(NR.toString()))
             return NR;
         else if (enumAsString.equals(NI.toString()))
             return NI;
         else if (enumAsString.equals(NE.toString()))
             return NE;
         else if (enumAsString.equals(NG.toString()))
             return NG;
         else if (enumAsString.equals(NU.toString()))
             return NU;
         else if (enumAsString.equals(NO.toString()))
             return NO;
         else if (enumAsString.equals(NZ.toString()))
             return NZ;
         else if (enumAsString.equals(OM.toString()))
             return OM;
         else if (enumAsString.equals(UG.toString()))
             return UG;
         else if (enumAsString.equals(UZ.toString()))
             return UZ;
         else if (enumAsString.equals(PE.toString()))
             return PE;
         else if (enumAsString.equals(PK.toString()))
             return PK;
         else if (enumAsString.equals(PA.toString()))
             return PA;
         else if (enumAsString.equals(PG.toString()))
             return PG;
         else if (enumAsString.equals(PY.toString()))
             return PY;
         else if (enumAsString.equals(NL.toString()))
             return NL;
         else if (enumAsString.equals(PH.toString()))
             return PH;
         else if (enumAsString.equals(PL.toString()))
             return PL;
         else if (enumAsString.equals(PT.toString()))
             return PT;
         else if (enumAsString.equals(QA.toString()))
             return QA;
         else if (enumAsString.equals(CF.toString()))
             return CF;
         else if (enumAsString.equals(CD.toString()))
             return CD;
         else if (enumAsString.equals(DO.toString()))
             return DO;
         else if (enumAsString.equals(CZ.toString()))
             return CZ;
         else if (enumAsString.equals(RO.toString()))
             return RO;
         else if (enumAsString.equals(GB.toString()))
             return GB;
         else if (enumAsString.equals(RU.toString()))
             return RU;
         else if (enumAsString.equals(RW.toString()))
             return RW;
         else if (enumAsString.equals(SN.toString()))
             return SN;
         else if (enumAsString.equals(KN.toString()))
             return KN;
         else if (enumAsString.equals(SM.toString()))
             return SM;
         else if (enumAsString.equals(VA.toString()))
             return VA;
         else if (enumAsString.equals(VC.toString()))
             return VC;
         else if (enumAsString.equals(LC.toString()))
             return LC;
         else if (enumAsString.equals(SV.toString()))
             return SV;
         else if (enumAsString.equals(WS.toString()))
             return WS;
         else if (enumAsString.equals(ST.toString()))
             return ST;
         else if (enumAsString.equals(SC.toString()))
             return SC;
         else if (enumAsString.equals(SL.toString()))
             return SL;
         else if (enumAsString.equals(SG.toString()))
             return SG;
         else if (enumAsString.equals(SI.toString()))
             return SI;
         else if (enumAsString.equals(SK.toString()))
             return SK;
         else if (enumAsString.equals(SO.toString()))
             return SO;
         else if (enumAsString.equals(SD.toString()))
             return SD;
         else if (enumAsString.equals(LK.toString()))
             return LK;
         else if (enumAsString.equals(SE.toString()))
             return SE;
         else if (enumAsString.equals(CH.toString()))
             return CH;
         else if (enumAsString.equals(SR.toString()))
             return SR;
         else if (enumAsString.equals(SZ.toString()))
             return SZ;
         else if (enumAsString.equals(SY.toString()))
             return SY;
         else if (enumAsString.equals(TW.toString()))
             return TW;
         else if (enumAsString.equals(TJ.toString()))
             return TJ;
         else if (enumAsString.equals(TZ.toString()))
             return TZ;
         else if (enumAsString.equals(TD.toString()))
             return TD;
         else if (enumAsString.equals(TH.toString()))
             return TH;
         else if (enumAsString.equals(TL.toString()))
             return TL;
         else if (enumAsString.equals(TG.toString()))
             return TG;
         else if (enumAsString.equals(TO.toString()))
             return TO;
         else if (enumAsString.equals(VT.toString()))
             return VT;
         else if (enumAsString.equals(TN.toString()))
             return TN;
         else if (enumAsString.equals(TM.toString()))
             return TM;
         else if (enumAsString.equals(TR.toString()))
             return TR;
         else if (enumAsString.equals(TV.toString()))
             return TV;
         else if (enumAsString.equals(UA.toString()))
             return UA;
         else if (enumAsString.equals(UY.toString()))
             return UY;
         else if (enumAsString.equals(VU.toString()))
             return VU;
         else if (enumAsString.equals(VE.toString()))
             return VE;
         else if (enumAsString.equals(VN.toString()))
             return VN;
         else if (enumAsString.equals(YE.toString()))
             return YE;
         else if (enumAsString.equals(ZM.toString()))
             return ZM;
         else if (enumAsString.equals(ZW.toString()))
             return ZW;
         else if (enumAsString.equals(MK.toString()))
             return MK;
 
         return getDefaultCountryType();
     }

    @Override
    public String toString() {
        return legacyLabel;
    }
 }
