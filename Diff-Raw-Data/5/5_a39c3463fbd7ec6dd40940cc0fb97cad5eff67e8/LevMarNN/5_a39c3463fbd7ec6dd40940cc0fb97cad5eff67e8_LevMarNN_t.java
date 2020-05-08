 package org.esa.beam.waterradiance.realoptimizers;
 
 import Jama.Matrix;
 import org.esa.beam.ocnnrd.Sensor;
 import org.esa.beam.ocnnrd.SensorContext;
 import org.esa.beam.siocs.abstractprocessor.BreakingCriterion;
 import org.esa.beam.siocs.abstractprocessor.CostFunction;
 import org.esa.beam.siocs.abstractprocessor.ForwardModel;
 import org.esa.beam.siocs.abstractprocessor.support.DefaultBreakingCriterion;
 import org.esa.beam.siocs.abstractprocessor.support.LevenbergMarquardtOptimizer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class LevMarNN {
 
     private static final int FR_TAB = 3700;
     private static final int RR_TAB = 925;
 
     private static final double DEG_2_RAD = (3.1415927 / 180.0);
     private final CostFunction costFunction;
     private final SensorContext sensorContext;
 
     private double[][] frlam = new double[FR_TAB][15];
     private double[][] fredtoa = new double[FR_TAB][15];
     private double[][] rrlam = new double[FR_TAB][15];
     private double[][] rredtoa = new double[FR_TAB][15];
 
     private static final int NLAM = 40;
 
     private static final double[] H_2_O_COR_POLY = new double[]{0.3832989, 1.6527957, -1.5635101, 0.5311913};
 
     private static final double[] MERBAND_12 = {412.3, 442.3, 489.7, 509.6, 559.5, 619.4, 664.3, 680.6, 708.1, 753.1, 778.2, 864.6};
     //    private static final double[] MODBAND_9 = {412.5, 443, 488, 531, 551, 667, 678, 748, 869.5};
 //    private static final double[] SWFSBAND_8 = {412, 443, 490, 510, 555, 670, 765, 865};
     private static final int[] MERBAND_12_INDEX = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12};
     private static final int[] MERIS_11_OUTOF_12_IX = new int[]{0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11};
 
 //    private static final double[] modisReflectanceScales = {1.7005885E-5f, 9.557186E-6f, 6.1540863E-6f, 4.824122E-6f,
 //            3.8021312E-6f, 2.2562692E-6f, 2.3084365E-6f, 2.1856329E-6f, 1.9265376E-6f};
 //    private static final double[] modisReflectanceOffsets = {316.9722f, 316.9722f, 316.9722f, 316.9722f, 316.9722f,
 //            316.9722f, 316.9722f, 316.9722f, 316.9722f};
 
     double M_PI = 3.1416;
 
     //@todo adapt to modis and seawifs
     double[] ozon_meris12 = {0.0002179, 0.002814, 0.02006, 0.04081, 0.104, 0.109, 0.0505, 0.03526, 0.01881, 0.008897, 0.007693, 0.002192}; // L.Bourg 2010
     double[] ozon_modis9 = {1.987E-03, 3.189E-03, 2.032E-02, 6.838E-02, 8.622E-02, 4.890E-02, 3.787E-02, 1.235E-02, 1.936E-03};
     double[] ozon_seawifs8 = {4.114E-04, 3.162E-03, 2.346E-02, 4.094E-02, 9.568E-02, 4.649E-02, 8.141E-03, 3.331E-03};
 
     double[] no2_modis9 = {5.814E-19, 4.985E-19, 2.878E-19, 1.525E-19, 1.194E-19, 7.065E-21, 8.304E-21, 2.157E-21,
             7.872E-23};
     double[] no2_seawifs8 = {6.004E-19, 4.963E-19, 2.746E-19, 2.081E-19, 9.411E-20, 9.234E-21, 1.078E-21, 1.942E-21};
 
     private final NnResources nnResources;
     //private final a_nn norm_net;
     private final double[] trans_ozon;
     private final double[] solar_flux;
     private final double[] rl_toa;
 
     private double[] x;
     private final AlphaTab alphaTab;
     private final double[] lb;
     private final double[] ub;
     private final BreakingCriterion breakingCriterion;
     private final nn_atmo_watForwardModel model;
     private final LevenbergMarquardtOptimizer optimizer;
     private double[] p;
     private double[] p_init;
 
     private final double[] nn_in;
     private final double[] rlw1;
     private final double[] rlw2;
     private final double[] L_toa;
     private final double[] Ed_toa;
     private final double[] tau_rayl_standard;
     private final double[] tau_rayl_toa_tosa;
     private final double[] tau_rayl_smile;
     private final double[] L_rayl_toa_tosa;
     private final double[] L_rayl_smile;
     private final double[] rho_tosa_corr;
     private final double[] Ed_toa_smile_rat;
     private final double[] Ed_toa_smile_corr;
     private final double[] L_tosa;
     private final double[] Ed_tosa;
     private final double[] trans_rayl_press;
     private final double[] trans_ozond;
     private final double[] trans_ozonu;
 
     private double[] nn_out;
     private final NnAtmoWat nnAtmoWat;
     private s_nn_atdata nn_at_data;
     private double[] x11;
 
 
     public LevMarNN(SensorContext sensorContext) throws IOException {
         x = new double[NLAM];
         trans_ozon = new double[NLAM];
         solar_flux = new double[NLAM];
         rl_toa = new double[NLAM];
         nn_in = new double[NLAM];
         nn_out = new double[40];
 
 //        double[] rwn1 = new double[40];
 //        double[] rwn2 = new double[40];
         rlw1 = new double[40];
         rlw2 = new double[40];
 
         L_toa = new double[15];
         Ed_toa = new double[15];
         //double[] L_toa_ocz = new double[15];
 
         tau_rayl_standard = new double[15];
         tau_rayl_toa_tosa = new double[15];
         tau_rayl_smile = new double[15];
         L_rayl_toa_tosa = new double[15];
         L_rayl_smile = new double[15];
         rho_tosa_corr = new double[15];
         Ed_toa_smile_rat = new double[15];
         Ed_toa_smile_corr = new double[15];
         L_tosa = new double[15];
         Ed_tosa = new double[15];
         //double[] trans_extra = new double[15];
         trans_rayl_press = new double[15];
         //double[] trans_rayl_smile = new double[15];
         //double[] trans_rayl_pressd = new double[15];
         //double[] trans_rayl_smiled = new double[15];
         //double[] trans_rayl_pressu = new double[15];
         //double[] trans_rayl_smileu = new double[15];
         trans_ozond = new double[15];
         trans_ozonu = new double[15];
 
         // lower and upper boundary for variables aot, ang, wind, log_conc_chl, log_conc_det, log_conc_gelb, log_conc_min
         // lb = new double[]{0.001, 0.001, 0.001, -13.96, -15.42, -16.38, -15.87, 0.0};
         lb = new double[]{
                 -4.6,   // aot
                 -3.8,   // ang
                 -2.3,   // wind
                 -19.9,  // apig
                 -15.89, // apart
                 -17.23, // agelb
                 -15.8,  // bpart
                 -14.92  // bwit
         };
 
         //ub = new double[]{1.0, 2.2, 10.0, 3.9, 2.294, 1.599, 4.594, 1.1};
         ub = new double[]{
                 0.0,   // aot
                 0.788, // ang
                 2.3,   // wind
                 0.685, // apig
                 2.297, // apart
                 1.6,   // agelb
                 4.598, // bpart
                 4.599  // bwit
         };
         p = new double[8];
         for (int i = 0; i < p.length; i++) {
             if (lb[i] < 0.0)
                 p[i] = lb[i] - lb[i] * 0.2;
             else
                 p[i] = lb[i] + lb[i] * 0.2;
         }
         p_init = new double[8];
         p_init[0] = Math.log(0.1);   // tau550
         p_init[1] = Math.log(1.0);   // ang
         p_init[2] = Math.log(3.0);   // wind
         p_init[3] = Math.log(0.005); // apig
         p_init[4] = Math.log(0.005); // adet
         p_init[5] = Math.log(0.005); // agelb
         p_init[6] = Math.log(0.01);  // bspm
         p_init[7] = Math.log(0.01);  // bwit
 
         this.sensorContext = sensorContext;
 
         if (sensorContext.getSensor() == Sensor.MERIS) {
             x11 = new double[11];
         } else if (sensorContext.getSensor() == Sensor.MODIS) {
             x11 = new double[9];
         } else if (sensorContext.getSensor() == Sensor.SEAWIFS) {
             x11 = new double[8];
         }
 
         nnResources = new NnResources();
         alphaTab = new AlphaTab();
 
         //norm_net = prepare_a_nn(nnResources.getNormNetPath());
 
         smile_tab_ini();
         model = new nn_atmo_watForwardModel();
         breakingCriterion = new DefaultBreakingCriterion(150, 1e-10);
         costFunction = new OcNnRdCostFunction();
         optimizer = new LevenbergMarquardtOptimizer(p.length, x11.length);
         optimizer.init(model, costFunction, breakingCriterion);
 
         nnAtmoWat = new NnAtmoWat(alphaTab, sensorContext.getSensor());
         nn_at_data = new s_nn_atdata();
         nn_at_data.prepare = -1;
     }
 
 //    private double angleDiff(double a, double b) {
 //        double pifak = DEG_2_RAD;
 //        return Math.acos(Math.cos((a - b) * pifak)) / pifak;
 //    }
 
     public int levmar_nn(int detector, double[] input, double[] output) {
         double view_zeni, azi_diff_hl, temperature, salinity, ozone;
 
         // @todo 2 tb/** can this be a field - check when all tests run green tb 2013-05-14
         int SMILE;
 
         double surf_press, rayl_rel_mass_toa_tosa;
         double cos_scat_ang, phase_rayl_min;
 
         double sun_zenith, view_zenith, cos_teta_sun, sin_teta_sun, cos_teta_view, sin_teta_view, cos_azi_diff;
         double sun_azimuth, view_azimuth, surf_pressure;
         int nlam, ilam, ix;
 
         double[] conc_at = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 
         double trans708, X2;
 
         double smile_lam;
 
         // input data
         sun_zenith = input[0];
         sun_azimuth = input[1];
         view_zenith = input[2];
         view_azimuth = input[3];
         surf_pressure = input[4];
         ozone = input[5];
         temperature = input[8];
         salinity = input[9];
 
         final double cos_sun_zenith = Math.cos(sun_zenith * DEG_2_RAD);
 
 
         int countOfSpectralBands = sensorContext.getNumSpectralInputBands();
         for (int i = 0; i < countOfSpectralBands; i++) {
             L_toa[i] = input[i + 10];
             solar_flux[i] = input[i + 25];
 
             //todo tf/** perform daily correction for MODIS and SeaWiFS
 
             Ed_toa[i] = solar_flux[i] * cos_sun_zenith;
             rl_toa[i] = L_toa[i] / Ed_toa[i];
         }
         // end of input data
 
         sun_azimuth = sensorContext.correctSunAzimuth(sun_azimuth);
         view_azimuth = sensorContext.correctViewAzimuth(view_azimuth);
         double delta_azimuth = Math.abs(view_azimuth - sun_azimuth);
         if (delta_azimuth > 180.0) {
             delta_azimuth = 180.0 - delta_azimuth;
         }
 
         // nn_at_data[0]= -1.0; // prepare
         nn_at_data.setSun_thet(sun_zenith);
         nn_at_data.view_zeni = view_zeni = view_zenith;
         nn_at_data.view_azi = view_azimuth;
         nn_at_data.azi_diff_hl = azi_diff_hl = delta_azimuth;
         nn_at_data.temperature = temperature;
         nn_at_data.salinity = salinity;
 
         /* ++++ angles ++++ */
 
         cos_teta_sun = Math.cos(sun_zenith * DEG_2_RAD);
         cos_teta_view = Math.cos(view_zenith * DEG_2_RAD);
         sin_teta_sun = Math.sin(sun_zenith * DEG_2_RAD);
         sin_teta_view = Math.sin(view_zenith * DEG_2_RAD);
         cos_azi_diff = Math.cos(delta_azimuth * DEG_2_RAD);
 
 //        double pifak = DEG_2_RAD;
 //        double sensor_azi = view_azimuth * 0.01;
 //        sensor_azi = Math.acos(Math.cos(sensor_azi * pifak)) / pifak;
 //        double sensor_zen = view_zenith * 0.01;
 //
 //        double solar_azi = sun_azimuth * 0.01;
 //        solar_azi = Math.acos(Math.cos(solar_azi * pifak)) / pifak;
 //        double solar_zen = sun_zenith * 0.01;
 //
 //        double azi_diff = angleDiff(sensor_azi, solar_azi);
 //
 //        double x_new = Math.sin(sensor_zen * pifak) * Math.cos(sensor_azi * pifak);
 //        double y = Math.sin((sensor_zen) * pifak) * Math.sin(sensor_azi * pifak);
 //        double z = Math.cos((sensor_zen) * pifak);
 
         /*+++ ozone correction +++*/
         //@todo tf/** adapt to Modis and SeaWiFS -> See Ocean Colour Web Site Forum
 
         nlam = 12;
         if (sensorContext.getSensor() == Sensor.MERIS) {
             for (int i = 0; i < nlam; i++) {
                 //trans_ozon[i]= exp(-ozon_meris12[i]* ozone / 1000.0 *(1.0/cos_teta_sun+1.0/cos_teta_view));
                 trans_ozond[i] = Math.exp(-ozon_meris12[i] * ozone / 1000.0 * (1.0 / cos_teta_sun));
                 trans_ozonu[i] = Math.exp(-ozon_meris12[i] * ozone / 1000.0 * (1.0 / cos_teta_view));
                 trans_ozon[i] = trans_ozond[i] * trans_ozonu[i];
             }
         } else if (sensorContext.getSensor() == Sensor.MODIS) {
             nlam = 9;
             for (int i = 0; i < nlam; i++) {
                 //trans_ozon[i]= exp(-ozon_meris12[i]* ozone / 1000.0 *(1.0/cos_teta_sun+1.0/cos_teta_view));
                 trans_ozond[i] = Math.exp(-ozon_modis9[i] * ozone / 1000.0 * (1.0 / cos_teta_sun));
                 trans_ozonu[i] = Math.exp(-ozon_modis9[i] * ozone / 1000.0 * (1.0 / cos_teta_view));
                 trans_ozon[i] = trans_ozond[i] * trans_ozonu[i];
             }
         } else if (sensorContext.getSensor() == Sensor.SEAWIFS) {
             nlam = 8;
             for (int i = 0; i < nlam; i++) {
                 //trans_ozon[i]= exp(-ozon_meris12[i]* ozone / 1000.0 *(1.0/cos_teta_sun+1.0/cos_teta_view));
                 trans_ozond[i] = Math.exp(-ozon_seawifs8[i] * ozone / 1000.0 * (1.0 / cos_teta_sun));
                 trans_ozonu[i] = Math.exp(-ozon_seawifs8[i] * ozone / 1000.0 * (1.0 / cos_teta_view));
                 trans_ozon[i] = trans_ozond[i] * trans_ozonu[i];
             }
         }
 
 //        for (int i = 0; i < 12; ++i) {
 //            ix = MERBAND_12_INDEX[i];
 //            L_toa_ocz[i] = L_toa[ix] / trans_ozon[i]; // shall be both ways RD20120318
 //        }
 
         //@todo tf/** check which gases need to be considered for SeaWiFS and MODIS -> See Ocean Colour Web Site Forum
         //consider relative humidity in NCEP aux data
         if (sensorContext.getSensor() == Sensor.MERIS) {
 
         /* +++ water vapour correction for band 9 +++++ */
 
             //X2=rho_900/rho_885;
 
             X2 = rl_toa[14] / rl_toa[13];
             trans708 = H_2_O_COR_POLY[0] + H_2_O_COR_POLY[1] * X2 + H_2_O_COR_POLY[2] * X2 * X2 + H_2_O_COR_POLY[3] * X2 * X2 * X2;
 
             L_toa[8] /= trans708;
         }
 
         double[] trans_no2 = new double[nlam];
         if (sensorContext.getSensor() == Sensor.MODIS) {
             double[] trans_no2_u = new double[nlam];
             double[] trans_no2_d = new double[nlam];
             double no2_tropo = input[40];
             double no2_strato = input[41];
             double no2_frac = input[42];
             double no2_tropo_200 = 0;
             if (no2_tropo > 0) {
                 no2_tropo_200 = no2_frac * no2_tropo;
             }
             for (int i = 0; i < nlam; i++) {
                 // @todo 1 tb/tf - PLEASe find names for those constants.... what is 285, 294 ??? tb 2013-11-14
                 double a285 = no2_modis9[i] * (1.0 - 0.003 * (285.0 - 294.0));
                 double a225 = no2_modis9[i] * (1.0 - 0.003 * (225.0 - 294.0));
                 double tau_to_200 = a285 * no2_tropo_200 + a225 * no2_strato;
                 trans_no2_d[i] = Math.exp(-tau_to_200 * (1.0 / (1000.0 * (1.0 / cos_teta_sun))));
                 trans_no2_u[i] = Math.exp(-tau_to_200 * 1.0 / ((1000.0 * (1.0 / cos_teta_view))));
                 trans_no2[i] = trans_no2_d[i] * trans_no2_u[i];
             }
         }
         if (sensorContext.getSensor() == Sensor.SEAWIFS) {
             double[] trans_no2_u = new double[nlam];
             double[] trans_no2_d = new double[nlam];
             double no2_tropo = input[40];
             double no2_strato = input[41];
             double no2_frac = input[42];
             double no2_tropo_200 = 0;
             if (no2_tropo > 0) {
                 no2_tropo_200 = no2_frac * no2_tropo;
             }
             for (int i = 0; i < nlam; i++) {
                 // @todo 1 tb/tf - PLEASe find names for those constants.... what is 285, 294 ??? tb 2013-11-14
                 double a285 = no2_seawifs8[i] * (1.0 - 0.003 * (285.0 - 294.0));
                 double a225 = no2_seawifs8[i] * (1.0 - 0.003 * (225.0 - 294.0));
                 double tau_to_200 = a285 * no2_tropo_200 + a225 * no2_strato;
                 trans_no2_d[i] = Math.exp(-tau_to_200 * (1.0 / (1000.0 * (1.0 / cos_teta_sun))));
                 trans_no2_u[i] = Math.exp(-tau_to_200 * 1.0 / ((1000.0 * (1.0 / cos_teta_view))));
                 trans_no2[i] = trans_no2_d[i] * trans_no2_u[i];
             }
         }
 
         if (sensorContext.getSensor() == Sensor.MERIS) {
 
         /*+++ smile and pressure correction +++*/
 
         /* calculate relative airmass rayleigh correction for correction layer*/
             surf_press = surf_pressure; // @todo tb/** why the hell?? tb 2013-11-14
             rayl_rel_mass_toa_tosa = (surf_press - 1013.2) / 1013.2; //?? oder rayl_mass_toa_tosa =surf_press - 1013.2; // RD20120105
 
         /* calculate phase function for rayleigh path radiance*/
             cos_scat_ang = -cos_teta_view * cos_teta_sun - sin_teta_view * sin_teta_sun * cos_azi_diff; // this is the scattering angle without fresnel reflection
             phase_rayl_min = 0.75 * (1.0 + cos_scat_ang * cos_scat_ang);
 
         /* calculate optical thickness of rayleigh for correction layer, lam in micrometer */
 
             for (ilam = 0; ilam < nlam; ilam++) {
                 ix = MERBAND_12_INDEX[ilam];
                 tau_rayl_standard[ilam] = 0.008735 * Math.pow(MERBAND_12[ilam] / 1000.0, -4.08);/* lam in �m */
                 tau_rayl_toa_tosa[ilam] = tau_rayl_standard[ilam] * rayl_rel_mass_toa_tosa;
                 //tau_rayl_toa_tosa[ilam] = tau_rayl_standard[ilam] * rayl_mass_toa_tosa; // RD20120105
                 L_rayl_toa_tosa[ilam] = Ed_toa[ix] * tau_rayl_toa_tosa[ilam] * phase_rayl_min / (4 * M_PI) * (1.0 / cos_teta_view);
                 trans_rayl_press[ilam] = Math.exp(-tau_rayl_toa_tosa[ilam] * (1.0 / cos_teta_view + 1.0 / cos_teta_sun));
                 //trans_rayl_pressd[ilam] = Math.exp(-tau_rayl_toa_tosa[ilam] * (1.0 / cos_teta_sun));
                 //trans_rayl_pressu[ilam] = Math.exp(-tau_rayl_toa_tosa[ilam] * (1.0 / cos_teta_view));
             }
 
         /* calculate rayleigh for correction of smile, lam in micrometer */
 
             //todo only for MERIS
             for (ilam = 0; ilam < nlam; ilam++) {
                 ix = MERBAND_12_INDEX[ilam];
                 smile_lam = rrlam[detector][ix];
                 tau_rayl_smile[ilam] = 0.008735 * Math.pow(smile_lam / 1000.0, -4.08);
                 L_rayl_smile[ilam] = Ed_toa[ix] * (tau_rayl_smile[ilam] - tau_rayl_standard[ilam]) * phase_rayl_min / (4 * M_PI) * (1.0 / cos_teta_view);
                 //trans_rayl_smile[ilam] = Math.exp(-(tau_rayl_smile[ilam] - tau_rayl_standard[ilam]) * (1.0 / cos_teta_view + 1.0 / cos_teta_sun));
                 //trans_rayl_smiled[ilam] = Math.exp(-(tau_rayl_smile[ilam] - tau_rayl_standard[ilam]) * (1.0 / cos_teta_sun));
                 //trans_rayl_smileu[ilam] = Math.exp(-(tau_rayl_smile[ilam] - tau_rayl_standard[ilam]) * (1.0 / cos_teta_view));
             }
 
         /* +++++ Esun smile correction ++++++ */
             for (ilam = 0; ilam < nlam; ilam++) {
                 ix = MERBAND_12_INDEX[ilam];
                 Ed_toa_smile_rat[ilam] = rredtoa[detector][ix];///nomi_sun[ix];
                 Ed_toa_smile_corr[ilam] = Ed_toa[ix] * Ed_toa_smile_rat[ilam]; // RD20120105 geaendert von / in *, wieder zurueck 20120119
             }
             SMILE = 1;
             if (SMILE == 1) {
             /* subtract all correcting radiances */
                 for (ilam = 0; ilam < nlam; ilam++) {
                     ix = MERBAND_12_INDEX[ilam];
                     // L_tosa[ilam] = ((L_toa[ix]-L_rayl_smile[ilam])-L_rayl_toa_tosa[ilam])/(trans_ozon[ilam]*trans_rayl_smile[ilam]);
                     L_tosa[ilam] = L_toa[ix] / (trans_ozon[ilam] * trans_rayl_press[ilam]/**trans_rayl_smile[ilam]*/)
                             - L_rayl_toa_tosa[ilam] + L_rayl_smile[ilam];//*trans_rayl_smile[ilam]);
                     Ed_tosa[ilam] = Ed_toa_smile_corr[ilam];//*trans_rayl_smiled[ilam]*trans_rayl_pressd[ilam];
                     rho_tosa_corr[ilam] = L_tosa[ilam] / Ed_tosa[ilam] * M_PI;
                     x[ilam] = rho_tosa_corr[ilam];
                 }
             } else { /* subtract only correction for ozone */
                 for (ilam = 0; ilam < nlam; ilam++) {
                     ix = MERBAND_12_INDEX[ilam];
                     L_tosa[ilam] = L_toa[ix] / trans_ozon[ilam];//-L_rayl_toa_tosa[ilam]-L_rayl_smile[ilam];
                     Ed_tosa[ilam] = Ed_toa[ix];
                     rho_tosa_corr[ilam] = L_tosa[ilam] / Ed_tosa[ilam] * M_PI;
                     x[ilam] = rho_tosa_corr[ilam];
                 }
             }
 
         } else if (sensorContext.getSensor() == Sensor.MODIS) {
             for (ilam = 0; ilam < nlam; ilam++) {
                 L_tosa[ilam] = L_toa[ilam] / trans_ozon[ilam];//-L_rayl_toa_tosa[ilam]-L_rayl_smile[ilam];
                 L_tosa[ilam] = L_tosa[ilam] / trans_no2[ilam];//-L_rayl_toa_tosa[ilam]-L_rayl_smile[ilam];
                 L_tosa[ilam] *= Math.pow(sensorContext.getEarthSunDistanceInAU(), 2);
                 Ed_tosa[ilam] = Ed_toa[ilam];
                rho_tosa_corr[ilam] = L_tosa[ilam] / Ed_tosa[ilam] * M_PI;
                 x[ilam] = rho_tosa_corr[ilam];
             }
         } else if (sensorContext.getSensor() == Sensor.SEAWIFS) {
             for (ilam = 0; ilam < nlam; ilam++) {
                 L_tosa[ilam] = L_toa[ilam] / trans_ozon[ilam];//-L_rayl_toa_tosa[ilam]-L_rayl_smile[ilam];
                 L_tosa[ilam] = L_tosa[ilam] / trans_no2[ilam];//-L_rayl_toa_tosa[ilam]-L_rayl_smile[ilam];
                 L_tosa[ilam] *= Math.pow(sensorContext.getEarthSunDistanceInAU(), 2);
                 Ed_tosa[ilam] = Ed_toa[ilam];
                rho_tosa_corr[ilam] = L_tosa[ilam] / Ed_tosa[ilam] * M_PI;
                 x[ilam] = rho_tosa_corr[ilam];
             }
         }
 
         /* extra trans for rho_water: ozon, rayl_smile, rayl_press */
 //        for (ilam = 0; ilam < nlam; ilam++) {
 //            trans_extra[ilam] = trans_ozon[ilam] * trans_rayl_press[ilam] * trans_rayl_smile[ilam];
 //        }
 
         /* +++++ vicarious adjustment +++++*/
 //        if (0) {
 //            x[10] /= 0.98;
 //            xb[10] = x[10];
 //
 //            x[11] /= 0.93;
 //            xb[11] = x[11];
 //        }
 
         /*
       input  5 is log_aot in [-4.605000,0.000000]
       input  6 is log_angstrom in [-3.817000,0.788500]
       input  7 is log_wind in [-2.303000,2.303000]
       input  8 is temperature in [0.000047,36.000000]
       input  9 is salinity in [0.000004,43.000000]
       */
 
 //        for (int i = 0; i < m; i++) {
 //            double lub = Math.abs(ub[i] - lb[i]);
 //            /*
 //        if(lb[i]<0.0)
 //            p[i]=lb[i]-lb[i]*0.2;
 //        else
 //            p[i]=lb[i]+lb[i]*0.2;
 //            */
 //            if (ub[i] < 0.0) {
 //                p[i] = ub[i] - lub * 0.2;
 //            } else {
 //                p[i] = ub[i] - lub * 0.2;
 //            }
 //        }
 
         System.arraycopy(p_init, 0, p, 0, p.length);
 
         if (sensorContext.getSensor() == Sensor.MERIS) {
             // select the 11 bands for iterations
             for (int i = 0; i < 11; i++) {
                 ix = MERIS_11_OUTOF_12_IX[i];
                 x11[i] = x[ix];
             }
         } else if (sensorContext.getSensor() == Sensor.MODIS) {
             System.arraycopy(x, 0, x11, 0, 9);
         } else if (sensorContext.getSensor() == Sensor.SEAWIFS) {
             System.arraycopy(x, 0, x11, 0, 8);
         }
         /* optimization control parameters; passing to levmar NULL instead of opts reverts to defaults */
         //  opts[0]=LM_INIT_MU; opts[1]=1E-15; opts[2]=1E-15; opts[3]=1E-20;
 //        final double LM_INIT_MU = 1E-03;
 //        final int LM_OPTS_SZ = 5; /* max(4, 5) */
 //        final double[] opts = new double[LM_OPTS_SZ];
 //        opts[0] = LM_INIT_MU;
 //        opts[1] = 1E-10;
 //        opts[2] = 1E-10;
 //        opts[3] = 1E-10;
 //        //  opts[4]=LM_DIFF_DELTA; // relevant only if the finite difference Jacobian version is used
 //        //  opts[4]= 0.2; // relevant only if the finite difference Jacobian version is used
 //        opts[4] = -0.1; // relevant only if the finite difference Jacobian version is used
 
         /* invoke the optimization function */
 //        ret = dlevmar_bc_dif(nn_atmo_wat, p, x11, m, n, lb, ub, 150, opts, info, NULL, & covar_out[0][0],&nn_at_data)
 //        ; // without Jacobian
 
         model.init(nn_at_data);
         model.setReferenceSignal(x11);
         costFunction.setReferenceSignal(x11);
         p = optimizer.optimize(p, x11, lb, ub);
 
         System.arraycopy(p, 0, conc_at, 0, p.length);
 //        for (int i = 0; i < m; i++) {
 //            conc_at[i] = p[i];
 //            // p_alt[i] = p[i];
 //        }
 
 
         model.setReferenceSignal(x);
         x = model.getModeledSignal(conc_at);
         nn_at_data = model.getNn_data();
 
         /**********************************************************/
 //        if (NORMALIZE) {
         /* normalize water leaving radiance reflectances */
 
         // requires first to make RLw again
         for (int i = 0; i < 12; i++) {
             rlw1[i] = 0.0;
             rlw2[i] = 0.0;
         }
 
         nn_in[0] = sun_zenith;
         nn_in[1] = view_zeni;
         nn_in[2] = azi_diff_hl;
         nn_in[3] = temperature;
         nn_in[4] = salinity;
         System.arraycopy(rlw1, 0, nn_in, 5, 12);
 
 //        nn_out = use_the_nn(norm_net, nn_in, nn_out, alphaTab);
         nn_out = nn_in;    // requested by CB, 20130517
 
 //        for (int i = 0; i < 12; i++) {
 //            rwn1[i] = nn_out[i] * M_PI;
 //        }
 
         System.arraycopy(rlw2, 0, nn_in, 5, 12);
 
 //        nn_out = use_the_nn(norm_net, nn_in, nn_out, alphaTab);
         nn_out = nn_in;    // requested by CB, 20130517
 
 //        for (int i = 0; i < 12; i++) {
 //            rwn2[i] = nn_out[i] * M_PI;
 //        }
 
 //        } // end normalize
 
         /*********************************************************/
 
         // put all results into output
         final int[] sensorLambdas = sensorContext.getNnOutputIndices();
         nlam = sensorLambdas.length;
 
         for (int i = 0; i < nlam; i++) {
             ix = sensorLambdas[i];
             output[i] = rho_tosa_corr[i] / M_PI;
             output[i + nlam] = nn_at_data.rpath_nn[ix];
             output[i + nlam * 2] = nn_at_data.rw_nn[ix];
             output[i + nlam * 3] = nn_at_data.tdown_nn[ix];
             output[i + nlam * 4] = nn_at_data.tup_nn[ix];
         }
         int offset = 5 * nlam;
 
         output[offset] = Math.exp(p[0]);        // aot_550
         output[offset + 1] = Math.exp(p[1]);    // ang_865_443
         output[offset + 2] = Math.exp(p[3]);    // a_pig
         output[offset + 3] = Math.exp(p[4]);    // a_part
         output[offset + 4] = Math.exp(p[5]);    // a_gelb
         output[offset + 5] = Math.exp(p[6]);    // b_part
         output[offset + 6] = Math.exp(p[7]);    // b_part
         output[offset + 7] = optimizer.getCost();  // sum_sq
         output[offset + 8] = optimizer.getNumberOfIterations();
 
         return (0);
     }
 
     static a_nn prepare_a_nn(String filename) throws IOException {
         File fp;
         char ch;
         int i;
         a_nn res = new a_nn();
 
 //        res = new a_nn[](a_nn*)malloc(sizeof(a_nn));
 //        res->filename=strdup(filename);
 
         fp = open_auxfile(filename);
 
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(fp));
             String line = reader.readLine();
             boolean startReadingAnn = false;
             while (line != null) {
                 if (line.startsWith("#")) {
                     startReadingAnn = true;
 //                    line = reader.readLine();
                     line = reader.readLine();
                 }
                 if (startReadingAnn) {
                     final int nnin = Integer.parseInt(line);
                     res.setNnin(nnin);
                     double[] inmin = new double[nnin];
                     double[] inmax = new double[nnin];
                     for (int j = 0; j < nnin; j++) {
                         line = reader.readLine().trim();
                         final String[] range = line.split(" ");
                         inmin[j] = Double.parseDouble(range[0]);
                         inmax[j] = Double.parseDouble(range[1]);
                     }
                     res.setInmin(inmin);
                     res.setInmax(inmax);
                     line = reader.readLine();
                     final int nnout = Integer.parseInt(line);
                     res.setNnout(nnout);
                     double[] outmin = new double[nnout];
                     double[] outmax = new double[nnout];
                     for (int j = 0; j < nnout; j++) {
                         line = reader.readLine().trim();
                         final String[] range = line.split(" ");
                         outmin[j] = Double.parseDouble(range[0]);
                         outmax[j] = Double.parseDouble(range[1]);
                     }
                     res.setOutmin(outmin);
                     res.setOutmax(outmax);
 //                    startReadingAnn = false;
                     break;
                 }
                 line = reader.readLine();
             }
 
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         res.setNn(make_ff_from_file(filename));
         return res;
     }
 
     private static File open_auxfile(String fileName) {
 //        String path;
 //        String home;
 ////        char[] home = System.getenv("HOME").toCharArray();
 //        char[] mapred_home = "/home/mapred".toCharArray();
 //        File fd;
 //
 //        if (System.getProperty("os.name").contains("indows")) {
 //            home = System.getenv("HOMEPATH");
 //        } else {
 //            home = System.getenv("HOME");
 //        }
 //
 ////        SystemUtils.getApplicationDataDir()
 //
 //        StringBuilder builder = new StringBuilder(home);
 //        builder.append("/Projekte/beam-waterradiance/beam-waterradiance-processor/");
 //        builder.append(fileName);
 ////        path = concat_path(home, fileName);
 
         return new File(fileName);
     }
 
     private static feedforward make_ff_from_file(String filename) {
         long pl, n, id1, id2, id3;
         long[] s;
         feedforward ff = new feedforward();
         File fp;
         char ch;
 
         fp = open_auxfile(filename);
 
         BufferedReader reader;
         try {
             reader = new BufferedReader(new FileReader(fp));
             String line = reader.readLine();
             boolean startReadingFeedForward = false;
             while (line != null) {
                 if (line.startsWith("$")) {
                     startReadingFeedForward = true;
                     line = reader.readLine();
                 }
                 if (startReadingFeedForward) {
                     final String[] initLine = line.split(" ");
                     int numberOfPlanes = Integer.parseInt(initLine[0].substring(initLine[0].indexOf("=") + 1).trim());
                     int[] size = new int[numberOfPlanes];
                     int[] subSize = new int[numberOfPlanes - 1];
                     for (int i = 0; i < numberOfPlanes; i++) {
                         size[i] = Integer.parseInt(initLine[i + 1]);
                         if (i > 0) {
                             subSize[i - 1] = size[i];
                         }
                     }
                     double[][] bias = make_vecv(numberOfPlanes - 1, subSize);
                     double[][][] wgt = make_mtxv(numberOfPlanes, size);
                     double[][] act = make_vecv(numberOfPlanes, size);
                     for (int i = 0; i < numberOfPlanes - 1; i++) {
                         line = reader.readLine();
                         for (int j = 0; j < subSize[i]; j++) {
                             line = reader.readLine();
                             bias[i][j] = Double.parseDouble(line);
                         }
                     }
                     for (int i = 0; i < numberOfPlanes - 1; i++) {
                         line = reader.readLine();
                         for (int j = 0; j < size[i + 1]; j++) {
                             for (int k = 0; k < size[i]; k++) {
                                 line = reader.readLine();
                                 wgt[i][j][k] = Double.parseDouble(line);
                             }
                         }
                     }
                     ff.setNplanes(numberOfPlanes);
                     ff.setSize(size);
                     ff.setBias(bias);
                     ff.setWgt(wgt);
                     ff.setAct(act);
                     break;
                 }
                 line = reader.readLine();
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return ff;
     }
 
     private static double[][][] make_mtxv(int n, int[] s) {
         int i, j;
         double[][][] wgt = new double[n - 1][][];
         for (i = 0; i < n - 1; i++) {
             wgt[i] = new double[s[i + 1]][];
             for (j = 0; j < s[i + 1]; j++) {
                 wgt[i][j] = new double[s[i]];
             }
         }
         return wgt;
     }
 
     private static double[][] make_vecv(int n, int[] s) {
         int i;
         double[][] bias;
         bias = new double[n][];
         for (i = 0; i < n; i++) {
             bias[i] = new double[s[i]];
         }
         return bias;
     }
 
     private void smile_tab_ini() throws IOException {
         File fp_ini, fp_tab;
         char[] name = new char[500];
         char[] buf = new char[500];
         int i, k, iband, ipix, num;
         double[] nomi_lam = new double[15];
         double[] nomi_sun = new double[15];
         String smile_name = "";
 
         /* read the tables */
         fp_tab = open_auxfile(nnResources.getCentralWavelengthFrPath());
         BufferedReader reader;
 
         reader = new BufferedReader(new FileReader(fp_tab));
         String line = reader.readLine(); // header
         for (int count = 0; count < FR_TAB; count++) {
             line = reader.readLine();
             final String[] tableLine = line.split("\t");
             for (int count2 = 0; count2 < 15; count2++) {
                 frlam[count][count2] = Double.parseDouble(tableLine[count2 + 1]);
             }
         }
         reader.close();
 
         fp_tab = open_auxfile(nnResources.getSunSpectralFluxFrPath());
         reader = new BufferedReader(new FileReader(fp_tab));
         line = reader.readLine(); // header
         for (int count = 0; count < FR_TAB; count++) {
             line = reader.readLine();
             final String[] tableLine = line.split("\t");
             for (int count2 = 0; count2 < 15; count2++) {
                 fredtoa[count][count2] = Double.parseDouble(tableLine[count2 + 1]);
             }
         }
         reader.close();
 
 
         fp_tab = open_auxfile(nnResources.getCentralWavelengthRrPath());
         reader = new BufferedReader(new FileReader(fp_tab));
         line = reader.readLine(); // header
         for (int count = 0; count < RR_TAB; count++) {
             line = reader.readLine();
             final String[] tableLine = line.split("\t");
             for (int count2 = 0; count2 < 15; count2++) {
                 rrlam[count][count2] = Double.parseDouble(tableLine[count2 + 1]);
             }
         }
         reader.close();
 
         fp_tab = open_auxfile(nnResources.getSunSpectralFluxRrPath());
 
         reader = new BufferedReader(new FileReader(fp_tab));
         line = reader.readLine(); // header
         for (int count = 0; count < RR_TAB; count++) {
             line = reader.readLine();
             final String[] tableLine = line.split("\t");
             for (int count2 = 0; count2 < 15; count2++) {
                 rredtoa[count][count2] = Double.parseDouble(tableLine[count2 + 1]);
             }
         }
         reader.close();
 
         fp_tab = open_auxfile(nnResources.getNominalLamSunPath());
 
         reader = new BufferedReader(new FileReader(fp_tab));
         for (int count = 0; count < 15; count++) {
             line = reader.readLine();
             final String[] tableLine = line.split("\t");
             nomi_lam[count] = Double.parseDouble(tableLine[0]);
             nomi_sun[count] = Double.parseDouble(tableLine[1]);
         }
         reader.close();
 
         /* make ed ratio tab, i.e. compute the ratio between the Ed_toa for each pixel relative to ed-toa at the mean pixel for each camera */
 
         /* compute ratio */
         for (ipix = 0; ipix < RR_TAB; ipix++) {
             for (iband = 0; iband < 15; iband++) {
                 rredtoa[ipix][iband] /= nomi_sun[iband];
             }
         }
 
         /* compute ratio */
         for (ipix = 0; ipix < FR_TAB; ipix++) {
             for (iband = 0; iband < 15; iband++) {
                 fredtoa[ipix][iband] /= nomi_sun[iband];
             }
         }
     }
 
 
     static double[] use_the_nn(a_nn a_net, double[] nn_in, double[] nn_out, AlphaTab alphaTab) {
         final long anetNnin = a_net.getNnin();
         final feedforward nn = a_net.getNn();
         final double[] inmin = a_net.getInmin();
         final double[] inmax = a_net.getInmax();
 
         for (int i = 0; i < anetNnin; i++) {
             final double value = (nn_in[i] - inmin[i]) / (inmax[i] - inmin[i]);
             nn.setInput(i, value);
         }
 
         ff_proc(nn, alphaTab);
 
         final double[] nnOutput = nn.getOutput();
         final double[] outmax = a_net.getOutmax();
         final double[] outmin = a_net.getOutmin();
 
         for (int i = 0; i < a_net.getNnout(); i++) {
             nn_out[i] = nnOutput[i] * (outmax[i] - outmin[i]) + outmin[i];
         }
         return nn_out;
     }
 
     private static void ff_proc(feedforward ff, AlphaTab alphaTab) {
         double[] bias;
         double[] act_plus;
         double[][] wgt;
 
         for (int pl = 0; pl < ff.getNplanes() - 1; pl++) {
             final double[][] act = ff.getAct();
             final int[] size = ff.getSize();
             bias = ff.getBias()[pl];
             wgt = ff.getWgt()[pl];
             act_plus = act[pl + 1];
             for (int i = 0; i < size[pl + 1]; i++) {
                 final double x = bias[i] + scp(wgt[i], act[pl], size[pl]);
                 act_plus[i] = alphaTab.get(x);
             }
         }
     }
 
     static double scp(double[] x, double[] y, long n) {
         double sum = 0.;
         for (int i = 0; i < n; i++) {
             sum += x[i] * y[i];
         }
         return sum;
     }
 
     private class nn_atmo_watForwardModel implements ForwardModel {
         private double[] rtosa_nn;
         private s_nn_atdata nn_data;
         private NNReturnData nnReturnData;
 
         private nn_atmo_watForwardModel() {
             nnReturnData = new NNReturnData();
         }
 
         public s_nn_atdata getNn_data() {
             return nn_data;
         }
 
         @Override
         public void init(Object o) {
             this.nn_data = (s_nn_atdata) o;
         }
 
         @Override
         public void setReferenceSignal(double[] doubles) {
             this.rtosa_nn = doubles.clone();
         }
 
         @Override
         public double[] getModeledSignal(double[] variables) {
             nnReturnData = nnAtmoWat.nn_atmo_wat(variables, rtosa_nn.clone(), nn_data, nnReturnData);
             nn_data = nnReturnData.getNn_atdata();
             return nnReturnData.getOutputValues();
         }
 
         @Override
         public Matrix getJacobianMatrix() {
             return null;
         }
     }
 }
