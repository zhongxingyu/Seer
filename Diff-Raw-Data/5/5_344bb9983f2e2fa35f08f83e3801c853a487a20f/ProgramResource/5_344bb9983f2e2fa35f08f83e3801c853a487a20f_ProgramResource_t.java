 /**
  * Copyright 2013 ArcBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.jci.client.resource.program;
 
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 
 public interface ProgramResource extends ClientBundle {
     public static int COL_WIDTH = 145;
     public static int COL_HEIGTH = 80;
     public static int BEFORE_LEFT = 0;
     public static int BEFORE_TOP = 30;
 
     public interface Style extends CssResource {
         String hoursTable();
 
         String carouselInner();
 
         String carouselInnerDiv();
 
         String rooms();
 
         String visioProgram();
 
         String odd();
 
         String l1();
 
         String l2();
 
         String event();
 
         String t800();
 
         String h400();
 
         String w1();
 
         String w2();
 
         String h500();
 
         String t1400();
 
         String t1800();
 
         String t2300();
 
         String l5();
 
         String t900();
 
         String t1200();
 
         String h100();
 
         String h275();
 
         String t1375();
 
         String l3();
 
         String h125();
 
         String t1050();
 
         String t1350();
 
         String l4();
 
         String t1525();
 
         String h150();
 
         String h025();
 
        String h050();

         String h300();
 
         String t1700();
 
         String h225();
 
         String h250();
 
         String t2050();
 
         String l6();
 
         String t750();
 
         String h325();
 
         String cBlue();
 
         String cRed();
 
         String cGreen();
 
         String cYellow();
 
         String cPurple();
 
         String t1025();
 
         String t1500();
 
         String t1575();
 
         String active();
 
         String tooltip();
 
         String tooltipActive();
 
         String arrow();
 
         String half();
 
         String pagerButtonActive();
 
         String third();
 
         String overflow();
 
         String down();
 
         String pagerContainer();
 
         String pagerDiv();
 
         String pagerDivContainer();
 
         String pagerButtonContainer();
 
         String pagerButton();
 
         String pagerCommons();
 
         String bio();
 
         String noUnderline();
 
         String noMarginP();
 
         String content();

        String t1550();
     }
 
     @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
     ImageResource tuile();
 
     ImageResource arrGrey();
 
     ImageResource arrGreyRevert();
 
     ImageResource arrBlue();
 
     ImageResource arrBlueRevert();
 
     ImageResource arrYellow();
 
     ImageResource arrYellowRevert();
 
     ImageResource arrRed();
 
     ImageResource arrRedRevert();
 
     ImageResource arrGreen();
 
     ImageResource arrGreenRevert();
 
     ImageResource arrPurple();
 
     ImageResource arrPurpleRevert();
 
     ImageResource img_awardNight_1();
 
     ImageResource img_awardNight_2();
 
     ImageResource img_tradeShow_1();
 
     ImageResource img_tradeShow_2();
 
     ImageResource img_presidentGala_1();
 
     ImageResource img_presidentGala_2();
 
     ImageResource img_excursionArbraska_1();
 
     ImageResource img_excursionShopping_1();
 
     ImageResource img_excursionShopping_2();
 
     ImageResource img_excursionCruise_1();
 
     ImageResource img_excursionCruise_2();
 
     ImageResource img_excursionTours_1();
 
     ImageResource img_excursionTours_2();
 
     ImageResource img_excursionBeer_1();
 
     ImageResource img_excursionKarting_1();
 
     ImageResource img_excursionFerrari_1();
 
     ImageResource img_excursionFerrari_2();
 
     ImageResource btn_exc_dn();
 
     ImageResource btn_exc_up();
 
     ImageResource img_training_drZhanSu();
 
     ImageResource img_training_jannVandermeulen();
 
     ImageResource img_training_joseeMasson();
 
     ImageResource img_training_margieHibbard();
 
     ImageResource img_training_mathieuVigneault();
 
     ImageResource img_training_sylvainRochon();
 
     ImageResource img_training_hernandoGomez();
 
     ImageResource img_training_joeyHoeschmann();
 
     ImageResource img_training_brianKavanaugh();
 
     ImageResource img_training_christianGoudreau();
 
     ImageResource img_charlotteLounge_1();
 
     ImageResource img_charlotteLounge_2();
 
     ImageResource img_brazilNight_1();
 
     ImageResource img_registration();
 
     ImageResource img_opening_1();
 
     ImageResource img_opening_2();
 
     ImageResource img_awards_1();
 
     ImageResource img_awards_2();
 
     ImageResource img_chaire_1();
 
     ImageResource img_chaire_2();
 
     @CssResource.NotStrict
     Style style();
 }
