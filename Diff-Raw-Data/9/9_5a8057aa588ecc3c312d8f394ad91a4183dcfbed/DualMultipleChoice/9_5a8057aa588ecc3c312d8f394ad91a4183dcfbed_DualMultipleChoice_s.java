 package com.odea.components.dualMultipleChoice;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 
 public class DualMultipleChoice<T> extends Panel {
 
 	public ListMultipleChoice<T> originals;
 	public ListMultipleChoice<T> destinations;
 	public List<T> selectedOriginals;
 	public List<T> selectedDestinations;
 
 	public DualMultipleChoice(String id, IModel<List<T>> originalsModel, IModel<List<T>> destinationsModel) {
 		super(id);
 		
 
		originals = new ListMultipleChoice<T>("originals", new PropertyModel(this, "selectedOriginals"), originalsModel);
 		originals.setOutputMarkupId(true);
 		
 		
		destinations = new ListMultipleChoice<T>("destinations", new PropertyModel(this, "selectedDestinations"), destinationsModel);
 		destinations.setOutputMarkupId(true);
 				
 		
 		AjaxButton addButton = new AjaxButton("addButton") {
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form form) {
 				update(target, selectedOriginals, originals, destinations);
 			}
 		};	
 		
 		addButton.setOutputMarkupId(true);
 		
 		AjaxButton removeButton = new AjaxButton("removeButton") {
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form form) {
 				update(target, selectedDestinations, destinations, originals);
 			}
 		};
 		
 		removeButton.setOutputMarkupId(true);
 		
 
 		add(originals);
 		add(destinations);
 		add(addButton);
 		add(removeButton);
 		
 	}
 
 	
 	
 	private void update(AjaxRequestTarget target, List<T> selections, ListMultipleChoice<T> from, ListMultipleChoice<T> to) {
 		List<T> choicesTo;
 		List<T> choicesFrom;
 
 		for (T destination : selections) {
 			choicesTo = (List<T>) to.getChoices();
 
 			if (!choicesTo.contains(destination)) {
 				choicesTo.add(destination);
 
 				choicesFrom = (List<T>) from.getChoices();
 				choicesFrom.remove(destination);
 
 				Collections.sort((List) choicesTo);
 				Collections.sort((List) choicesFrom);
 				
				
 				from.setChoices(choicesFrom);
 				to.setChoices(choicesTo);
 			}
 		}
 
 		target.add(to);
 		target.add(from);
 	}
 
 
 
 
 
 	
 	//GETTERS & SETTERS
 	
 	public ListMultipleChoice<T> getOriginals() {
 		return originals;
 	}
 
 
 
 	public void setOriginals(ListMultipleChoice<T> originals) {
 		this.originals = originals;
 	}
 
 
 
 	public ListMultipleChoice<T> getDestinations() {
 		return destinations;
 	}
 
 
 
 	public void setDestinations(ListMultipleChoice<T> destinations) {
 		this.destinations = destinations;
 	}
 	
 	
 }
