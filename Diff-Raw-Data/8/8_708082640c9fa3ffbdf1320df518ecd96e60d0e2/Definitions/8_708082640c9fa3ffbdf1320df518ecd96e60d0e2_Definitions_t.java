 /*
  *
  * Redberry: symbolic tensor computations library.
  * Copyright (C) 2010-2011  Stanislav Poslavsky <stvlpos@mail.ru>
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package redberryphysics.oneloop;
 
 import redberry.core.context.CC;
 import redberry.core.tensor.Tensor;
 import redberry.core.transformation.Transformation;
 import redberry.core.transformation.substitutions.SubstitutionsFactory;
 
 /**
  * All hat tensors have prefix "HAT".
  * 
  * @author Stanislav Poslavsky
  */
 public class Definitions {
     public final static Tensor COUNTERTERMS = CC.parse("G1");
     public final static Transformation COUNTERTERMS_SUBSTITUTION =
             SubstitutionsFactory.createSubstitution("G1 = "
             + "Flat + WR + SR + SSR + FF + FR + RR ");
     //temporary L=2
     public final static Transformation RR_SUBSTITUTION =
             SubstitutionsFactory.createSubstitution("RR = "
             + "(1/10)*L*L*HATK^{d}*DELTA^{mnab}*HATK^{g}*n_s*n_r*R^s_abg*R^r_mnd + "
             + "L*L*(L-1)*(L-1)*(L-2)*HATK^{bgd}*DELTA^a*HATK^{mn}*n_s*n_r*((2/45)*R^r_adn*R^s_bmg-(1/120)*R^r_dan*R^s_bmg) + "
            + "L*L*(L-1)*HATK^{d}*DELTA^{abg}*HATK^{mn}*n_s*n_r*(-(1/10)*R^r_mgn*R^s_adb+(1/15)*R^r_dan*R^s_bmg+(1/60)*R^r_bdn*R^s_gma)+"
             + "L*L*(L-1)*(L-1)*HATK^{gd}*DELTA^{ab}*HATK^{mn}*n_s*n_r*(-(1/20)*R^r_mbn*R^s_dag+(1/180)*R^r_anb*R^s_gdm-(7/360)*R^r_mgn*R^s_adb-(1/240)*R^r_dbn*R^s_gam-(1/120)*R^r_bgn*R^s_adm-(1/30)*R^r_dbn*R^s_agm)+"
             + "L*L*(L-1)*HATK^{mn}*DELTA^{abg}*HATK^d*n_s*n_r*((7/120)*R^r_bgn*R^s_mad-(3/40)*R^r_bgd*R^s_man+(1/120)*R^r_dgn*R^s_abm)+"
             + "L*L*HATK^m*DELTA^{abg}*HATK^n*n_r*(-(1/8)*R_bg*R^r_nam+(3/20)*R_bg*R^r_man+(3/40)*R_am*R^r_bgn+(1/40)*R^s_bgm*R^r_nas-(3/20)*R^s_abm*R^r_gns+(1/10)*R^s_abn*R^r_gms)+"
            + "L*L*(L-1)*HATK^{g}*DELTA^{ab}*HATK^{mn}*n_r*((1/20)*R_an*R^r_gbm+(1/20)*R_ag*R^r_mbn+(1/10)*R_ab*R^r_mgn+(1/20)*R^s_ang*R^r_sbm-(1/60)*R^s_man*R^r_bsg+(1/10)*R^s_abg*R^r_msn-(1/12)*R^s_abn*R^r_msg)+"
             + "L*L*(L-1)*(L-1)*HATK^{ab}*DELTA^{g}*HATK^{mn}*n_r*((1/60)*R_am*R^r_bng-(1/20)*R_am*R^r_gnb+(1/120)*R_ab*R^r_mng+(3/40)*R_ag*R^r_nbm+(1/20)*R^s_gma*R^r_nsb+(1/120)*R^s_amg*R^r_bns-(1/40)*R^s_amg*R^r_snb+(1/40)*R^s_amb*R^r_sng-(1/20)*R^s_amb*R^r_gns-(1/40)*R^s_mbn*R^r_gsa)+"
            + "L*L*(L-1)*HATK^{ab}*DELTA^{mn}*HATK^{g}*n_r*((1/20)*R^s_mnb*R^r_gsa-(7/60)*R^s_bma*R^r_gns+(1/20)*R^s_bma*R^r_sng+(1/10)*R^s_mbg*R^r_nas+(1/60)*R^s_mbg*R^r_ans+(7/120)*R_ab*R^r_ngm+(11/60)*R_bm*R^r_nag)");
 
     public static void main(String[] args) {
     }
 }
