 package de.hypoport.einarbeitung;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 public abstract class ShowList extends Panel {
 
 	public ShowList(String id, IModel<List<String>> liste) {
 		super(id);
 
 		add(new ListView<String>("list", liste) {
 
 			@Override
 			protected void populateItem(ListItem<String> item) {
 				final String teilnehmer = item.getModelObject();
 
 				Label label = new Label("name", teilnehmer); // Setzen eines simplen Labels
 				item.add(label);
 
 				// Setzen eines Labels innerhalb eines Links
				Link<Void> link = new Link<Void>("link") {
 
 					@Override
 					public void onClick() {
 						PageParameters p = new PageParameters();
 						p.add("name", teilnehmer);
 						// p.add("name", teilnehmer);
 
 						// Wir holen aus einer extern zu erstellenden Funktion das Sprungziel
 						setResponsePage(getZielPageHook(p));
 					}
 				};
 
 				Label label2 = new Label("name", teilnehmer);
 				link.add(label2);
 				item.add(link);
 
 			}
 		});
 
 	}
 
 	public abstract Page getZielPageHook(PageParameters p);
 
 	public static List<String> createList() {
 
 		// Füllen einer Pseudo-Liste
		List<String> liste = new ArrayList<String>();
 		String[] strings = {"Anakin Skywalker", "Ben Obi-Wan Kenobi", "Luke Skywalker", "Leia Organa", "Han Solo",
 				"Darth Vader", "Boba Fett", "Jabba the Hutt", "R2 D2", "C-3P0", "Lando Calrissian", "Qui-Gon Jinn",
 				"Padme Amidala", "Darth Maul", "Mace Windu", "Yoda", "Chewbacca"};
 
 		for (String f : strings) {
 			String name = f;
 			liste.add(name);
 
 			try {
 				Thread.sleep(2 * 1000 / strings.length);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return liste;
 
 	}
 }
