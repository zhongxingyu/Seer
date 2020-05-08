 /*******************************************************************************
  * Copyright (c) 2013 Rudy D'hauwe @ Whizu
  * Licensed under the EUPL V.1.1
  *   
  * This Software is provided to You under the terms of the European 
  * Union Public License (the EUPL) version 1.1 as published by the 
  * European Union. Any use of this Software, other than as authorized 
  * under this License is strictly prohibited (to the extent such use 
  * is covered by a right of the copyright holder of this Software).
  *
  * This Software is provided under the License on an AS IS basis and 
  * without warranties of any kind concerning the Software, including 
  * without limitation merchantability, fitness for a particular purpose, 
  * absence of defects or errors, accuracy, and non-infringement of 
  * intellectual property rights other than copyright. This disclaimer 
  * of warranty is an essential part of the License and a condition for 
  * the grant of any rights to this Software.
  *  
  * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
  *
  * Contributors:
  *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
  *******************************************************************************/
 package org.whizu.tutorial.barchart;
 
 import java.util.Date;
 
 import org.whizu.jquery.Function;
 import org.whizu.ui.Application;
 import org.whizu.ui.ClickListener;
 import org.whizu.ui.Document;
 import org.whizu.ui.Form;
 import org.whizu.ui.Label;
 import org.whizu.ui.Layout;
 import org.whizu.ui.UI;
 import org.whizu.value.IntegerValue;
 import org.whizu.value.StringValue;
 
 /**
  * @author Rudy D'hauwe
  */
 public class BarChart implements Application {
 
 	@Override
 	public String getTitle() {
 		return "My BarChart Tutorial";
 	}
 
 	@Override
 	public void init(final UI ui) {
 		// model
 		final StringValue antwoord = new StringValue("Mijn hobby is...");
 		final IntegerValue aantal = new IntegerValue("aantal");
 		aantal.setValue(0);
 
 		// user interface
 		Document document = ui.getDocument();
 		Layout layout = ui.createHorizontalLayout();
 
 		Layout left = ui.createCssLayout();
 		left.css("left-column");
 		left.add(ui.createLabel("Welkom op deze eenvoudige rondvraag.").css("tekst"));
 		left.add(ui.createLabel("Het aantal reeds ontvangen antwoorden is:").css("tekst"));
 		left.add(ui.createLabel(aantal).css("highlight"));
 		final Layout graph = ui.createCssLayout();
 		left.add(graph);
 
 		final Layout history = ui.createVerticalLayout();
 		Layout right = ui.createCssLayout();
 		right.css("right-column");
 		right.add(ui.createLabel("Wat is jouw favoriete hobby?"));
 		Form form = ui.createForm();
 		form.add(ui.createTextField(antwoord));
 		form.css("form");
 		Label button = ui.createLabel("Verzenden").css("submit-button");
 		button.addClickListener(new ClickListener() {
 			int kort = 0;
 			int lang = 0;
 
 			@Override
 			public void click() {
 				aantal.increment();
 				if (antwoord.getValue().length() > 10) {
 					lang++;
 				} else {
 					kort++;
 				}
 				Layout detail = ui.createVerticalLayout();
 				detail.add(ui.createLabel(new Date().toString()).css("detail-date"));
 				detail.add(ui.createLabel(antwoord.getValue()));
 				detail.css("detail");
 				history.prepend(detail);
 				antwoord.clear();
 				graph.empty();
 				graph.add(ui.createBarChart(new String[]{"So cool", "So not cool"}, new Integer[]{kort, lang}));
				ui.delay(500, new Function() {
 
 					@Override
 					public void execute() {
 						aantal.increment();
 						antwoord.setValue("Wat is jouw hobby?");
 					}
 				});
 			}
 		});
 		form.add(button);
 		right.add(form);
 		right.add(history);
 
 		layout.add(left);
 		layout.add(right);
 
 		document.add(layout);
 	}
 }
