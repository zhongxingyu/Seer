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
 
 package com.jci.client.application.program;
 
import javax.inject.Inject;

 import com.arcbees.core.client.mvp.ViewImpl;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.jci.client.resource.CommonResource;
 import com.jci.client.resource.program.ProgramResource;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 public class ProgramPageView extends ViewImpl implements ProgramPagePresenter.MyView, AttachEvent.Handler {
     interface Binder extends UiBinder<Widget, ProgramPageView> {
     }
 
     @UiField
     DivElement divButtons;
     @UiField
     HTMLPanel divCalendar;
     @UiField
     DivElement excursion1;
     @UiField
     DivElement excursion2;
     @UiField
     DivElement excursion3;
     @UiField
     SpanElement excursionDiv1;
     @UiField
     SpanElement excursionDiv2;
     @UiField
     SpanElement excursionDiv3;
     @UiField
     SpanElement awardsNightDiv;
     @UiField
     DivElement awardsNight;
 
     private final String activeStyleName;
     private final String activeStyleNameProgram;
     private final String eventStyleNameProgram;
     private final String activeTooltipStyleNameProgram;
     private final String tooltipStyleNameProgram;
     private final String activeExcursionStyleNameProgram;
     private final String excursionStyleNameProgram;
     private final String overflowNameProgram;
     private final String downNameProgram;
 
     private boolean isBound;
 
     @Inject
     public ProgramPageView(Binder uiBinder,
                            CommonResource commonResource,
                            ProgramResource programResource) {
         initWidget(uiBinder.createAndBindUi(this));
 
         activeStyleName = commonResource.style().active();
         activeStyleNameProgram = programResource.style().active();
         eventStyleNameProgram = programResource.style().event();
         activeTooltipStyleNameProgram = programResource.style().tooltipActive();
         tooltipStyleNameProgram = programResource.style().tooltip();
         activeExcursionStyleNameProgram = programResource.style().excursionsActive();
         excursionStyleNameProgram = programResource.style().excursions();
         overflowNameProgram = programResource.style().overflow();
         downNameProgram = programResource.style().down();
 
         asWidget().addAttachHandler(this);
     }
 
     @Override
     public void onAttachOrDetach(AttachEvent attachEvent) {
         if (attachEvent.isAttached()) {
             bindGwtQuery();
             isBound = true;
         }
     }
 
     @Override
     public void untoggleTooltipClick(Element e) {
         $("." + activeStyleNameProgram).removeClass(activeStyleNameProgram);
         $("." + activeTooltipStyleNameProgram).removeClass(activeTooltipStyleNameProgram);
     }
 
     @Override
     public void untoggleTooltip() {
         $("." + activeStyleNameProgram).removeClass(activeStyleNameProgram);
         $("." + activeTooltipStyleNameProgram).removeClass(activeTooltipStyleNameProgram);
     }
 
     @Override
     public void pauseCarousel() {
         pauseCarouselNative();
     }
 
     public static native void pauseCarouselNative() /*-{
         $wnd.$('#myCarouselProgram').bind('slid', function () {
             $wnd.$('#myCarouselProgram').carousel('pause');
         });
     }-*/;
 
     private void bindGwtQuery() {
         $("." + tooltipStyleNameProgram).unbind("click");
         $("." + tooltipStyleNameProgram).click(new Function() {
             @Override
             public void f(Element e) {
                 getEvent().stopPropagation();
             }
         });
 
         $("a", divButtons).unbind("click");
         $("a", divButtons).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeStyleName).removeClass(activeStyleName);
                 $("." + downNameProgram).removeClass(downNameProgram);
 
                 if ($(e).hasClass("firstButton")) {
                     $("." + overflowNameProgram).addClass(downNameProgram);
                 }
 
                 $(e).addClass(activeStyleName);
                 pauseCarousel();
             }
         });
 
         $(divCalendar).unbind("click");
         $(divCalendar).click(new Function() {
             @Override
             public void f(Element e) {
                 untoggleTooltipClick(e);
             }
         });
 
         $("." + eventStyleNameProgram, divCalendar).unbind("click");
         $("." + eventStyleNameProgram, divCalendar).click(new Function() {
             @Override
             public void f(Element e) {
                 if ($(e).hasClass(activeStyleNameProgram)) {
                     untoggleTooltipClick(e);
                 } else {
                     untoggleTooltipClick(e);
                     $(e).addClass(activeStyleNameProgram);
                     $("." + tooltipStyleNameProgram, e).addClass(activeTooltipStyleNameProgram);
                 }
                 getEvent().stopPropagation();
             }
         });
 
         $("." + excursionStyleNameProgram + " a", excursion1).unbind("click");
         $("." + excursionStyleNameProgram + " a", excursion1).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeExcursionStyleNameProgram, excursion1).removeClass(activeExcursionStyleNameProgram);
                 $("span", excursionDiv1).hide();
                 $("#" + e.getId() + "Div").show();
                 $(e).addClass(activeExcursionStyleNameProgram);
             }
         });
 
         $("." + excursionStyleNameProgram + " a", excursion2).unbind("click");
         $("." + excursionStyleNameProgram + " a", excursion2).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeExcursionStyleNameProgram, excursion2).removeClass(activeExcursionStyleNameProgram);
                 $("span", excursionDiv2).hide();
                 $("#" + e.getId() + "Div").show();
                 $(e).addClass(activeExcursionStyleNameProgram);
             }
         });
 
         $("." + excursionStyleNameProgram + " a", excursion3).unbind("click");
         $("." + excursionStyleNameProgram + " a", excursion3).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeExcursionStyleNameProgram, excursion3).removeClass(activeExcursionStyleNameProgram);
                 $("span", excursionDiv3).hide();
                 $("#" + e.getId() + "Div").show();
                 $(e).addClass(activeExcursionStyleNameProgram);
             }
         });
 
         $("." + excursionStyleNameProgram + " a", awardsNight).unbind("click");
         $("." + excursionStyleNameProgram + " a", awardsNight).click(new Function() {
             @Override
             public void f(Element e) {
                 $("." + activeExcursionStyleNameProgram, awardsNight).removeClass(activeExcursionStyleNameProgram);
                 $("span", awardsNightDiv).hide();
                 $("#" + e.getId() + "Div").show();
                 $(e).addClass(activeExcursionStyleNameProgram);
             }
         });
     }
 }
