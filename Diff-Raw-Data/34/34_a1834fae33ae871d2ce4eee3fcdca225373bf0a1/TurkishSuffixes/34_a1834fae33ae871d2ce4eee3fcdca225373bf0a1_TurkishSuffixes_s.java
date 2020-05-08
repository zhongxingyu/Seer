 package zemberek3.lexicon;
 
 import com.google.common.collect.Sets;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class TurkishSuffixes {
 
     static Map<String, SuffixFormSet> suffixSets = new HashMap<String, SuffixFormSet>();
 
     public static TurkishSuffix Pl = new TurkishSuffix("Pl");
     public static SuffixFormSet Pl_lAr = newSet(Pl, "lAr");
 
     public static TurkishSuffix Dat = new TurkishSuffix("Dat");
     public static SuffixFormSet Dat_yA = newSet(Dat, "+yA");
     public static SuffixFormSet Dat_nA = newSet(Dat, "nA");
 
     public static TurkishSuffix Loc = new TurkishSuffix("Loc");
     public static SuffixFormSet Loc_dA = newSet(Loc, ">dA");
     public static SuffixFormSet Loc_ndA = newSet(Loc, "ndA");
 
     public static TurkishSuffix Abl = new TurkishSuffix("Abl");
     public static SuffixFormSet Abl_dAn = newSet(Abl, ">dAn");
     public static SuffixFormSet Abl_ndAn = newSet(Abl, "ndAn");
 
     public static TurkishSuffix Gen = new TurkishSuffix("Gen");
     public static SuffixFormSet Gen_nIn = newSet(Gen, "+nIn");
 
     public static TurkishSuffix Acc = new TurkishSuffix("Acc");
     public static SuffixFormSet Acc_yI = newSet(Acc, "+yI");
     public static SuffixFormSet Acc_nI = newSet(Acc, "nI");
 
     public static TurkishSuffix Inst = new TurkishSuffix("Inst");
     public static SuffixFormSet Inst_ylA = newSet(Inst, "+ylA");
 
     public static TurkishSuffix P1sg = new TurkishSuffix("P1sg");
     public static SuffixFormSet P1sg_Im = newSet(P1sg, "Im");
 
     public static TurkishSuffix P2sg = new TurkishSuffix("P2sg");
     public static SuffixFormSet P2sg_In = newSet(P2sg, "In");
 
     public static TurkishSuffix P3sg = new TurkishSuffix("P3sg");
     public static SuffixFormSet P3sg_sI = newSet(P3sg, "+sI");
 
     public static TurkishSuffix P1pl = new TurkishSuffix("P1pl");
     public static SuffixFormSet P1pl_ImIz = newSet(P1pl, "ImIz");
 
     public static TurkishSuffix P2pl = new TurkishSuffix("P2pl");
     public static SuffixFormSet P2pl_InIz = newSet(P2pl, "InIz");
 
     public static TurkishSuffix P3pl = new TurkishSuffix("P3pl");
     public static SuffixFormSet P3pl_lArI = newSet(P3pl, "lArI");
 
     public static TurkishSuffix Dim = new TurkishSuffix("Dim");
     public static SuffixFormSet Dim_cIk = newSet(Dim, ">cIk");
     public static SuffixFormSet Dim_cIg = newSet(Dim, ">cIğ", false);
 
     public static TurkishSuffix Dim2 = new TurkishSuffix("Dim2");
     public static SuffixFormSet Dim2_cAgIz = newSet(Dim2, "cAğIz");
 
     public static TurkishSuffix With = new TurkishSuffix("With");
     public static SuffixFormSet With_lI = newSet(With, "lI");
 
     public static TurkishSuffix Without = new TurkishSuffix("Without");
     public static SuffixFormSet Without_sIz = newSet(Without, "sIz");
 
     public static TurkishSuffix Rel = new TurkishSuffix("Rel");
     public static SuffixFormSet Rel_ki = newSet(Rel, "ki"); // masa-da-ki
 
     public static TurkishSuffix A1sg = new TurkishSuffix("A1sg");
     public static SuffixFormSet A1sg_yIm = newSet(A1sg, "+yIm"); // gel-e-yim
     public static SuffixFormSet A1sg_m = newSet(A1sg, "m"); // gel-se-m
 
     public static TurkishSuffix A2sg = new TurkishSuffix("A2sg");
     public static SuffixFormSet A2sg_sIn = newSet(A2sg, "sIn"); // gel-ecek-sin
     public static SuffixFormSet A2sg_n = newSet(A2sg, "n"); // gel-di-n
     public static SuffixFormSet A2sg_EMPTY = newSet("A2sg_EMPTY", A2sg, ""); // gel-
 
     public static TurkishSuffix A2sg2 = new TurkishSuffix("A2sg2");
     public static SuffixFormSet A2sg2_sAnA = newSet(A2sg2, "sAnA"); //gel-sene
 
     public static TurkishSuffix A2sg3 = new TurkishSuffix("A2sg3");
     public static SuffixFormSet A2sg3_yInIz = newSet(A2sg3, "+yInIz"); //gel-iniz
 
     public static TurkishSuffix A3sg = new TurkishSuffix("A3sg");
     public static SuffixFormSet A3sg_EMPTY = newSet(A3sg, ""); // gel-di-
     public static SuffixFormSet A3sg_sIn = newSet(A3sg, "sIn"); // gel-sin
 
     public static TurkishSuffix A1pl = new TurkishSuffix("A1pl");
     public static SuffixFormSet A1pl_yIz = newSet(A1pl, "+yIz"); // geliyor-uz
     public static SuffixFormSet A1pl_k = newSet(A1pl, "k"); // gel-di-k
     public static SuffixFormSet A1pl_lIm = newSet(A1pl, "lIm"); // gel-e-lim
 
     public static TurkishSuffix A2pl = new TurkishSuffix("A2pl");
     public static SuffixFormSet A2pl_sInIz = newSet(A2pl, "sInIz"); // gel-ecek-siniz
     public static SuffixFormSet A2pl_nIz = newSet(A2pl, "nIz"); // gel-di-niz
     public static SuffixFormSet A2pl_yIn = newSet(A2pl, "+yIn"); // gel-me-yin
 
     public static TurkishSuffix A2pl2 = new TurkishSuffix("A2pl2");
     public static SuffixFormSet A2pl2_sAnIzA = newSet(A2pl2, "sAnIzA"); // gel-senize
 
     public static TurkishSuffix A3pl = new TurkishSuffix("A3pl");
     public static SuffixFormSet A3pl_lAr = newSet(A3pl, "lAr"); // gel-ecek-ler
     public static SuffixFormSet A3pl_sInlAr = newSet(A3pl, "sInlAr"); // gel-sinler
 
     public static TurkishSuffix Agt = new TurkishSuffix("Agt");
     public static SuffixFormSet Agt_cI = newSet(Agt, ">cI"); // araba-cı. Converts to another Noun.
     public static SuffixFormSet Agt_yIcI = newSet(Agt, "+yIcI"); // otur-ucu. converts to both Noun and Adj
 
     public static TurkishSuffix Ness = new TurkishSuffix("Ness");
     public static SuffixFormSet Ness_lIk = newSet(Ness, "lIk");
     public static SuffixFormSet Ness_lIg = newSet(Ness, "lIğ", false);
 
     public static TurkishSuffix Become = new TurkishSuffix("Become");
     public static SuffixFormSet Become_lAs = newSet(Become, "lAş");
 
     public static TurkishSuffix Resemb = new TurkishSuffix("Resemb");
     public static SuffixFormSet Resemb_ImsI = newSet(Resemb, "ImsI"); // udunumsu
     public static SuffixFormSet Resemb_msI = newSet(Resemb, "+msI"); // odunsu
 
     public static TurkishSuffix Related = new TurkishSuffix("Related");
     public static SuffixFormSet Related_sAl = newSet(Related, "sAl");
 
     public static TurkishSuffix Aor = new TurkishSuffix("Aor");
     public static SuffixFormSet Aor_Ir = newSet(Aor, "+Ir"); //gel-ir
     public static SuffixFormSet Aor_Ar = newSet(Aor, "+Ar"); //ser-er
     public static SuffixFormSet Aor_z = newSet(Aor, "z"); // gel-me-z
     public static SuffixFormSet Aor_EMPTY = newSet("Aor_EMPTY", Aor, "", false); // gel-me--yiz
 
     public static TurkishSuffix AorPart = new TurkishSuffix("AorPart"); // convert to an Adjective
     public static SuffixFormSet AorPart_Ir = newSet(AorPart, "+Ir"); //gel-ir
     public static SuffixFormSet AorPart_Ar = newSet(AorPart, "+Ar"); //ser-er
     public static SuffixFormSet AorPart_z = newSet(AorPart, "z"); // gel-me-z
 
     public static TurkishSuffix Prog = new TurkishSuffix("Prog");
     public static SuffixFormSet Prog_Iyor = newSet(Prog, "Iyor");
 
     public static TurkishSuffix Prog2 = new TurkishSuffix("Prog2");
     public static SuffixFormSet Prog2_mAktA = newSet(Prog2, "mAktA");
 
     public static TurkishSuffix Fut = new TurkishSuffix("Fut");
     public static SuffixFormSet Fut_yAcAk = newSet(Fut, "+yAcAk");
     public static SuffixFormSet Fut_yAcAg = newSet(Fut, "+yAcAğ", false);
 
     public static TurkishSuffix FutPart = new TurkishSuffix("FutPart");
     public static SuffixFormSet FutPart_yAcAk = newSet(FutPart, "+yAcAk");
     public static SuffixFormSet FutPart_yAcAg = newSet(FutPart, "+yAcAğ", false);
 
     public static TurkishSuffix Past = new TurkishSuffix("Past");
     public static SuffixFormSet Past_dI = newSet(Past, ">dI");
 
     public static TurkishSuffix PastPart = new TurkishSuffix("PastPart");
     public static SuffixFormSet PastPart_dIk = newSet(PastPart, ">dIk");
     public static SuffixFormSet PastPart_dIg = newSet(PastPart, ">dIğ", false);
 
     public static TurkishSuffix Evid = new TurkishSuffix("Evid");
     public static SuffixFormSet Evid_mIs = newSet(Evid, "mIş");
 
     public static TurkishSuffix EvidPart = new TurkishSuffix("EvidPart");
     public static SuffixFormSet EvidPart_mIs = newSet(EvidPart, "mIş");
 
     public static TurkishSuffix PresPart = new TurkishSuffix("PresPart");
     public static SuffixFormSet PresPart_yAn = newSet(PresPart, "+yAn");
 
     public static TurkishSuffix Neg = new TurkishSuffix("Neg");
     public static SuffixFormSet Neg_mA = newSet(Neg, "mA"); //gel-me
     public static SuffixFormSet Neg_m = newSet(Neg, "m", false); // gel-m-iyor
 
     public static TurkishSuffix Cond = new TurkishSuffix("Cond");
     public static SuffixFormSet Cond_ysA = newSet(Cond, "+ysA");
 
     public static TurkishSuffix Necess = new TurkishSuffix("Necess");
     public static SuffixFormSet Necess_mAlI = newSet(Necess, "mAlI");
 
     public static TurkishSuffix Opt = new TurkishSuffix("Opt");
     public static SuffixFormSet Opt_yA = newSet(Opt, "+yA");
 
     public static TurkishSuffix Pass = new TurkishSuffix("Pass");
     public static SuffixFormSet Pass_In = newSet(Pass, "+In");
     public static SuffixFormSet Pass_nIl = newSet(Pass, "+nIl");
     public static SuffixFormSet Pass_Il = newSet(Pass, "Il");
 
     public static TurkishSuffix Caus = new TurkishSuffix("Caus");
     public static SuffixFormSet Caus_t = newSet(Caus, "t");
     public static SuffixFormSet Caus_tIr = newSet(Caus, ">dIr");
 
     public static TurkishSuffix Imp = new TurkishSuffix("Imp");
     public static SuffixFormSet Imp_EMPTY = newSet(Imp, "");
 
     public static TurkishSuffix Recip = new TurkishSuffix("Recip");
     public static SuffixFormSet Recip_Is = newSet(Recip, "+Iş");
 
     public static TurkishSuffix Reflex = new TurkishSuffix("Reflex");
     public static SuffixFormSet Reflex_In = newSet(Reflex, "+In");
 
     public static TurkishSuffix Abil = new TurkishSuffix("Abil");
     public static SuffixFormSet Abil_yAbil = newSet(Abil, "+yAbil");
     public static SuffixFormSet Abil_yA = newSet(Abil, "+yA", false);
 
     public static TurkishSuffix Cop = new TurkishSuffix("Cop");
     public static SuffixFormSet Cop_dIr = newSet(Cop, ">dIr");
 
     public static TurkishSuffix PastCop = new TurkishSuffix("PastCop");
     public static SuffixFormSet PastCop_ydI = newSet(PastCop, "+ydI");
 
     public static TurkishSuffix EvidCop = new TurkishSuffix("EvidCop");
     public static SuffixFormSet EvidCop_ymIs = newSet(EvidCop, "+ymIş");
 
     public static TurkishSuffix CondCop = new TurkishSuffix("CondCop");
     public static SuffixFormSet CondCop_ysA = newSet(CondCop, "+ysA");
 
     public static TurkishSuffix While = new TurkishSuffix("While");
     public static SuffixFormSet While_ken = newSet(While, "+yken");
 
     public static TurkishSuffix NotState = new TurkishSuffix("NotState");
     public static SuffixFormSet NotState_mAzlIk = newSet(NotState, "mAzlIk");
     public static SuffixFormSet NotState_mAzlIg = newSet(NotState, "mAzlIğ", false);
 
     public static TurkishSuffix ActOf = new TurkishSuffix("ActOf");
     public static SuffixFormSet ActOf_mAcA = newSet(ActOf, "mAcA");
 
     public static TurkishSuffix AsIf = new TurkishSuffix("AsIf");
     public static SuffixFormSet AsIf_cAsInA = newSet(AsIf, ">cAsInA");
 
     // Converts to an Adverb.
     public static TurkishSuffix AsLongAs = new TurkishSuffix("AsLongAs");
     public static SuffixFormSet AsLongAs_dIkcA = newSet(AsLongAs, ">dIkçA");
 
     public static TurkishSuffix When = new TurkishSuffix("When");
     public static SuffixFormSet When_yIncA = newSet(When, "+yIncA");
 
     // It also may have "worthy of doing" meaning after passive. Converts to an Adjective.
     public static TurkishSuffix FeelLike = new TurkishSuffix("FeelLike");
     public static SuffixFormSet FeelLike_yAsI = newSet(FeelLike, "+yAsI");
 
     // Converts to an Adverb.
     public static TurkishSuffix SinceDoing = new TurkishSuffix("SinceDoing");
     public static SuffixFormSet SinceDoing_yAlI = newSet(SinceDoing, "+yAlI");
 
     // Converts to an Adverb.
     public static TurkishSuffix ByDoing = new TurkishSuffix("ByDoing");
     public static SuffixFormSet ByDoing_yArAk = newSet(ByDoing, "+yArAk");
 
     // Converts to an Adverb.
     public static TurkishSuffix WithoutDoing = new TurkishSuffix("WithoutDoing");
     public static SuffixFormSet WithoutDoing_mAdAn = newSet(WithoutDoing, "mAdAn");
 
     public static TurkishSuffix WithoutDoing2 = new TurkishSuffix("WithoutDoing2");
     public static SuffixFormSet WithoutDoing2_mAksIzIn = newSet(WithoutDoing2, "mAksIzIn");
 
     // Converts to an Adverb.
     public static TurkishSuffix AfterDoing = new TurkishSuffix("AfterDoing");
     public static SuffixFormSet AfterDoing_yIp = newSet(AfterDoing, "+yIp");
 
     public static TurkishSuffix UnableToDo = new TurkishSuffix("UnableToDo");
     public static SuffixFormSet UnableToDo_yAmAdAn = newSet(UnableToDo, "+yAmAdAn");
 
     public static TurkishSuffix InsteadOfDoing = new TurkishSuffix("InsteadOfDoing");
     public static SuffixFormSet InsteadOfDoing_mAktAnsA = newSet(InsteadOfDoing, "mAktAnsA");
 
     // Converts to an Adverb.
     public static TurkishSuffix KeepDoing = new TurkishSuffix("KeepDoing");
     public static SuffixFormSet KeepDoing_yAgor = newSet(KeepDoing, "+yAgör");
 
     public static TurkishSuffix KeepDoing2 = new TurkishSuffix("KeepDoing2");
     public static SuffixFormSet KeepDoing2_yAdur = newSet(KeepDoing2, "+yAdur");
 
     public static TurkishSuffix EverSince = new TurkishSuffix("EverSince");
     public static SuffixFormSet EverSince_yAlI = newSet(EverSince, "+yAlI");
 
     public static TurkishSuffix Almost = new TurkishSuffix("Almost");
     public static SuffixFormSet Almost_yAyAz = newSet(Almost, "+yAyaz");
 
     public static TurkishSuffix Hastily = new TurkishSuffix("Hastily");
     public static SuffixFormSet Hastily_yIver = newSet(Hastily, "+yIver");
 
     public static TurkishSuffix Stay = new TurkishSuffix("Stay");
     public static SuffixFormSet Stay_yAkal = newSet(Stay, "+yAkal");
 
     public static TurkishSuffix Inf1 = new TurkishSuffix("Inf1");
     public static SuffixFormSet Inf1_mAk = newSet(Inf1, "mAk");
 
     public static TurkishSuffix Inf2 = new TurkishSuffix("Inf2");
     public static SuffixFormSet Inf2_mA = newSet(Inf2, "mA");
 
     public static TurkishSuffix Inf3 = new TurkishSuffix("Inf3");
     public static SuffixFormSet Inf3_yIs = newSet(Inf3, "+yIş");
 
     public static TurkishSuffix Ly = new TurkishSuffix("Ly");
     public static SuffixFormSet Ly_cA = newSet(Ly, ">cA");
 
     public static TurkishSuffix Quite = new TurkishSuffix("Quite");
     public static SuffixFormSet Quite_cA = newSet(Quite, ">cA");
 
     public static TurkishSuffix NounRoot = new TurkishSuffix("NounRoot");
     public static SuffixFormSet Noun_Main = newSet("Noun_Main", NounRoot, "");
     public static SuffixFormSet Noun_Exp_C = newSet("Noun_Exp_C", NounRoot, "");
     public static SuffixFormSet Noun_Exp_V = newSet("Noun_Exp_V", NounRoot, "");
     public static SuffixFormSet Noun_Comp_P3sg = newSet("Noun_Comp_P3sg", NounRoot, "");
     public static SuffixFormSet Noun_Comp_P3sg_Root = newSet("Noun_Comp_P3sg_Root", NounRoot, "");
 
     public static TurkishSuffix AdjRoot = new TurkishSuffix("AdjRoot");
     public static SuffixFormSet Adj_Main = newSet("Adj_Main", AdjRoot, "");
     public static SuffixFormSet Adj_Exp_C = newSet("Adj_Exp_C", AdjRoot, "");
     public static SuffixFormSet Adj_Exp_V = newSet("Adj_Exp_V", AdjRoot, "");
 
     public static TurkishSuffix AdvRoot = new TurkishSuffix("AdvRoot");
     public static SuffixFormSet Adv_Main = newSet("Adv_Main", AdvRoot, "");
 
     public static TurkishSuffix InterjRoot = new TurkishSuffix("InterjRoot");
     public static SuffixFormSet Interj_Main = newSet("Interj_Main", InterjRoot, "");
 
     public static TurkishSuffix VerbRoot = new TurkishSuffix("VerbRoot");
     public static SuffixFormSet Verb_Main = newSet("Verb_Main", VerbRoot, "");
     public static SuffixFormSet Verb_Prog_Drop = newSet("Verb_Prog_Drop", VerbRoot, "");
     public static SuffixFormSet Verb_Exp_C = newSet("Verb_Exp_C", VerbRoot, "");
     public static SuffixFormSet Verb_Exp_V = newSet("Verb_Exp_V", VerbRoot, "");
 
     public static TurkishSuffix PronounRoot = new TurkishSuffix("Pronoun");
     public static SuffixFormSet Pron_Main = newSet(PronounRoot, "");
     public static SuffixFormSet Pron_BenSen = newSet("Pron_BenSen", PronounRoot, "");
     public static SuffixFormSet Pron_BanSan = newSet("Pron_BanSan", PronounRoot, "");
 
     public static final SuffixFormSet[] CASE_FORMS = {Dat_yA, Loc_dA, Abl_dAn, Gen_nIn, Acc_yI, Inst_ylA};
     public static final SuffixFormSet[] POSSESSIVE_FORMS = {P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, P3pl_lArI};
     public static final SuffixFormSet[] PERSON_FORMS_N = {A1sg_yIm, A2sg_sIn, A3sg_EMPTY, A1pl_yIz, A2pl_sInIz, A3pl_lAr};
     public static final SuffixFormSet[] PERSON_FORMS_COP = {A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr};
     public static final SuffixFormSet[] COPULAR_FORMS = {Cop_dIr, PastCop_ydI, EvidCop_ymIs, CondCop_ysA, While_ken};
 
     static SuffixFormSet newSet(TurkishSuffix suffix, String generation) {
         String id = suffix + "_" + generation;
         return newSet(id, suffix, generation, true);
     }
 
     static SuffixFormSet newSet(TurkishSuffix suffix, String generation, boolean endSuffix) {
         String id = suffix + "_" + generation;
         return newSet(id, suffix, generation, endSuffix);
     }
 
     static SuffixFormSet newSet(String id, TurkishSuffix suffix, String generation, boolean endSuffix) {
         if (suffixSets.containsKey(id))
             throw new IllegalArgumentException("There is already a suffix set with same id:" + id);
         SuffixFormSet newSet = new SuffixFormSet(id, suffix, generation, endSuffix);
         suffixSets.put(id, newSet);
         return newSet;
 
     }
 
     static SuffixFormSet newSet(String id, TurkishSuffix suffix, String generation) {
         return newSet(id, suffix, generation, true);
     }
 
     public Iterable<SuffixFormSet> getSets() {
         return suffixSets.values();
     }
 
     public TurkishSuffixes() {
 
         Noun_Main.add(CASE_FORMS, POSSESSIVE_FORMS, COPULAR_FORMS, PERSON_FORMS_N)
                 .add(Pl_lAr, Dim_cIg, Dim_cIk, Dim2_cAgIz, With_lI, Without_sIz, A3sg_EMPTY, Agt_cI, Resemb_msI,
                         Resemb_ImsI, Ness_lIg, Ness_lIk, Related_sAl);
         Noun_Exp_V.add(Dat_yA, Acc_yI, Gen_nIn, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, A1sg_yIm, A1pl_yIz, Resemb_ImsI);
         Noun_Exp_C.add(Noun_Main.getSuccSetCopy()).remove(Noun_Exp_V.getSuccSetCopy());
         Noun_Comp_P3sg.add(COPULAR_FORMS, POSSESSIVE_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
         Noun_Comp_P3sg_Root.add(With_lI, Without_sIz, Agt_cI, Resemb_msI, Resemb_ImsI, Ness_lIg, Ness_lIk, Related_sAl);
 
         Verb_Main.add(Prog_Iyor, Prog2_mAktA, Fut_yAcAg, Fut_yAcAk, Past_dI, Evid_mIs, Aor_Ir, AorPart_Ir)
                 .add(Neg_mA, Neg_m, Abil_yAbil, Abil_yA, Caus_tIr, Opt_yA, Imp_EMPTY, Agt_yIcI)
                 .add(Pass_In, NotState_mAzlIk, NotState_mAzlIg, ActOf_mAcA, PastPart_dIg, PastPart_dIk, EvidPart_mIs)
                 .add(FutPart_yAcAg, FutPart_yAcAk, PresPart_yAn, AsLongAs_dIkcA)
                 .add(Inf1_mAk, Inf2_mA, Inf3_yIs)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing_mAdAn, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, UnableToDo_yAmAdAn, InsteadOfDoing_mAktAnsA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAlI, Almost_yAyAz, Hastily_yIver, Stay_yAkal);
 
         Verb_Exp_V.add(Opt_yA, Fut_yAcAg, Fut_yAcAg, Aor_Ar, AorPart_Ar, Prog_Iyor);
         Verb_Exp_C.add(Verb_Main.getSuccSetCopy()).remove(Verb_Exp_V.getSuccSetCopy()).remove(Aor_Ir, AorPart_Ir);
 
         Verb_Prog_Drop.add(Prog_Iyor);
 
         Pron_Main.add(CASE_FORMS);
         Pron_BenSen.add(CASE_FORMS).remove(Dat_yA);
         Pron_BanSan.add(Dat_yA);
 
         Adj_Main.add(Noun_Main.getSuccSetCopy()).add(Ly_cA, Become_lAs, Quite_cA);
         Adj_Exp_C.add(Noun_Exp_C.getSuccSetCopy()).add(Ly_cA, Become_lAs, Quite_cA);
         Adj_Exp_V.add(Noun_Exp_V.getSuccSetCopy());
         Become_lAs.add(Verb_Main.getSuccSetCopy());
 
         Pl_lAr.add(CASE_FORMS, COPULAR_FORMS)
                 .add(P1sg_Im, P2sg_In, P1pl_ImIz, P2pl_InIz, A1pl_yIz, A2pl_sInIz);
 
         P1sg_Im.add(CASE_FORMS, COPULAR_FORMS).add(A2sg_sIn, A2pl_sInIz);
         P2sg_In.add(CASE_FORMS, COPULAR_FORMS).add(A1sg_yIm, A1pl_yIz);
         P3sg_sI.add(COPULAR_FORMS)
                 .add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA)
                 .add(A1sg_yIm, A1pl_yIz, A2sg_sIn, A2pl_sInIz);
         P1pl_ImIz.add(CASE_FORMS, COPULAR_FORMS);
         P2pl_InIz.add(CASE_FORMS, COPULAR_FORMS);
         P3pl_lArI.add(P3sg_sI.getSuccessorsIterable());
 
         Rel_ki.add(COPULAR_FORMS, PERSON_FORMS_N).add(Dat_nA, Loc_ndA, Abl_ndAn, Gen_nIn, Acc_nI, Inst_ylA);
         Dat_yA.add(COPULAR_FORMS);
         Dat_nA.add(COPULAR_FORMS);
 
         Loc_dA.add(COPULAR_FORMS).add(Rel_ki);
         Loc_ndA.add(COPULAR_FORMS);
 
         Abl_dAn.add(COPULAR_FORMS);
         Abl_ndAn.add(COPULAR_FORMS);
 
         Gen_nIn.add(COPULAR_FORMS).add(Rel_ki);
 
         Dim_cIg.add(Dat_yA, Acc_yI, Gen_nIn, P1sg_Im, P2sg_In, P3sg_sI, P1pl_ImIz, P2pl_InIz, A1sg_yIm, A1pl_yIz);
         Dim_cIk.add(Loc_dA, Abl_dAn, Inst_ylA, P3pl_lArI, A2sg_sIn, A2pl_sInIz, A3pl_lAr).add(COPULAR_FORMS);
         Dim2_cAgIz.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N);
 
         With_lI.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).add(Pl_lAr);
         Without_sIz.add(CASE_FORMS, COPULAR_FORMS, POSSESSIVE_FORMS, PERSON_FORMS_N).add(Pl_lAr);
 
         Resemb_msI.add(CASE_FORMS, PERSON_FORMS_N, COPULAR_FORMS, POSSESSIVE_FORMS)
                 .add(Pl_lAr, Ness_lIg, Ness_lIk, With_lI, Without_sIz);
         Resemb_msI.add(Resemb_ImsI.getSuccSetCopy());
 
         Ness_lIk.add(CASE_FORMS, POSSESSIVE_FORMS, COPULAR_FORMS).add(Pl_lAr, Agt_cI);
         Ness_lIg.add(Ness_lIk).retain(Noun_Exp_V.getSuccSetCopy());
         Ness_lIk.remove(Ness_lIg.getSuccSetCopy());
 
         PastCop_ydI.add(PERSON_FORMS_COP);
         EvidCop_ymIs.add(A1sg_yIm, A2sg_sIn, A3sg_EMPTY, A1pl_yIz, A2pl_sInIz, A3pl_lAr, AsIf_cAsInA);
         CondCop_ysA.add(PERSON_FORMS_COP);
 
         Neg_mA.add(Aor_z, AorPart_z, Aor_EMPTY, Prog2_mAktA, Imp_EMPTY, Opt_yA,
                 Fut_yAcAk, Fut_yAcAg, Past_dI, Evid_mIs, Cond_ysA, Abil_yAbil, Necess_mAlI, NotState_mAzlIg, NotState_mAzlIk,
                 ActOf_mAcA, PastPart_dIg, PastPart_dIk, FutPart_yAcAg, FutPart_yAcAk, EvidPart_mIs)
                 .add(AsLongAs_dIkcA, PresPart_yAn)
                 .add(Inf1_mAk, Inf2_mA, Inf3_yIs)
                 .add(When_yIncA, FeelLike_yAsI, SinceDoing_yAlI, ByDoing_yArAk, WithoutDoing2_mAksIzIn)
                 .add(AfterDoing_yIp, When_yIncA, InsteadOfDoing_mAktAnsA)
                 .add(KeepDoing2_yAdur, KeepDoing_yAgor, EverSince_yAlI, Hastily_yIver);
 
         Neg_m.add(Prog_Iyor);
 
         Aor_Ar.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_ysA);
         Aor_Ir.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_ysA);
         Aor_z.add(COPULAR_FORMS).add(A3sg_sIn, Cond_ysA);
         Aor_EMPTY.add(A1sg_m, A1pl_yIz);
 
         Set<SuffixFormSet> noParticipleSuff =
                 Sets.newHashSet(Become_lAs, Ness_lIk, Dim_cIg, Dim_cIg, Dim2_cAgIz, With_lI, Without_sIz, Related_sAl,
                         Resemb_msI, Resemb_msI);
 
         AorPart_Ar.add(Adj_Main.getSuccSetCopy()).remove(noParticipleSuff).add(AsIf_cAsInA);
         AorPart_Ir.add(AorPart_Ar.getSuccSetCopy());
         AorPart_z.add(AorPart_Ar.getSuccSetCopy());
 
         FutPart_yAcAk.add(Adj_Exp_C.getSuccSetCopy());
         FutPart_yAcAg.add(Adj_Exp_V.getSuccSetCopy());
 
         EvidPart_mIs.add(AorPart_Ar.getSuccSetCopy());
 
         PastPart_dIk.add(Adj_Exp_C.getSuccSetCopy()).remove(AsIf_cAsInA).remove(noParticipleSuff);
         PastPart_dIg.add(Adj_Exp_V.getSuccSetCopy()).remove(noParticipleSuff);
 
         Prog_Iyor.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_ysA);
         Prog2_mAktA.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_ysA);
 
         Fut_yAcAg.add(A1sg_yIm, A1pl_yIz);
         Fut_yAcAk.add(PERSON_FORMS_N, COPULAR_FORMS).add(Cond_ysA, AsIf_cAsInA).remove(Fut_yAcAg.getSuccSetCopy());
 
         Past_dI.add(A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr, CondCop_ysA, PastCop_ydI);
         Evid_mIs.add(PERSON_FORMS_N).add(CondCop_ysA, PastCop_ydI, EvidCop_ymIs, While_ken, Cop_dIr);
 
         Cond_ysA.add(A1sg_m, A2sg_n, A3sg_EMPTY, A1pl_k, A2pl_nIz, A3pl_lAr);
 
         Imp_EMPTY.add(A2sg_EMPTY, A2sg2_sAnA, A2sg3_yInIz, A2pl2_sAnIzA, A2pl_yIn, A3sg_sIn, A3pl_sInlAr);
         Agt_cI.add(CASE_FORMS, PERSON_FORMS_N, POSSESSIVE_FORMS, COPULAR_FORMS).add(Pl_lAr, Become_lAs, With_lI, Without_sIz);
         Agt_yIcI.add(Agt_cI.getSuccSetCopy());
 
         Abil_yAbil.add(Verb_Main.getSuccSetCopy()).remove(Abil_yAbil, Abil_yA, Neg_mA);
         Abil_yA.add(Neg_mA, Neg_m);
 
         Opt_yA.add(A1sg_yIm, A2sg_sIn, A3sg_EMPTY, A1pl_lIm, A2pl_sInIz, A3pl_lAr, PastCop_ydI, EvidCop_ymIs);
 
         Caus_t.add(Verb_Main.getSuccSetCopy());
         Caus_tIr.add(Verb_Main.getSuccSetCopy()).remove(Caus_tIr).add(Caus_t);
 
         Pass_Il.add(Verb_Main).remove(Pass_In, Pass_nIl);
         Pass_In.add(Verb_Main).remove(Pass_Il, Pass_nIl);
 
         Reflex_In.add(Verb_Main);
         Recip_Is.add(Verb_Main);
 
         Inf1_mAk.add(COPULAR_FORMS).add(Abl_dAn, Loc_dA, Inst_ylA);
         Inf2_mA.add(Noun_Main.getSuccessors());
         Inf3_yIs.add(Noun_Main.getSuccessors());
 
         When_yIncA.add(Dat_yA);
         FeelLike_yAsI.add(POSSESSIVE_FORMS);
 
         KeepDoing_yAgor.add(Neg_mA.getSuccSetCopy());
         KeepDoing_yAgor.add(Neg_mA.getSuccSetCopy());
         Almost_yAyAz.add(Neg_mA.getSuccSetCopy());
         Hastily_yIver.add(Neg_mA.getSuccSetCopy());
         Necess_mAlI.add(COPULAR_FORMS, PERSON_FORMS_N);
     }
 }
