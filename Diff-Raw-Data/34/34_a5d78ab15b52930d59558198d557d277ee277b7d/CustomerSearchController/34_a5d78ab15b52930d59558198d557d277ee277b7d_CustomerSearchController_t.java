 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.demo.client.customer.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.demo.client.customer.model.SearchResultContainer;
 import org.eclipse.riena.demo.customer.common.CustomerRecordOverview;
 import org.eclipse.riena.demo.customer.common.CustomerSearchBean;
 import org.eclipse.riena.demo.customer.common.CustomerSearchResult;
 import org.eclipse.riena.demo.customer.common.ICustomerDemoService;
 import org.eclipse.riena.internal.demo.customer.client.Activator;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget.SelectionType;
 
 /**
  * customer search view controller
  */
 public class CustomerSearchController extends SubModuleController {
 	private CustomerSearchBean customerSearchBean = new CustomerSearchBean();
 	private CustomerSearchResult ergebnis;
 	private ICustomerDemoService customerDemoService;
 
 	public void bind(ICustomerDemoService customerDemoService) {
 		this.customerDemoService = customerDemoService;
 	}
 
 	public void unbind(ICustomerDemoService customerDemoService) {
 		this.customerDemoService = null;
 	}
 
 	@Override
 	public void configureRidgets() {
 		Inject.service(ICustomerDemoService.class).into(this).andStart(Activator.getDefault().getBundle().getBundleContext());
 
 		ITextRidget suchName = (ITextRidget) getRidget("suchName");
 		suchName.bindToModel(customerSearchBean, "lastName");
 		suchName.setMandatory(true);
 
 		((ITextRidget) getRidget("suchVorname")).bindToModel(customerSearchBean, "firstName");
 		((ITextRidget) getRidget("suchPlz")).bindToModel(customerSearchBean, "zipcode");
 		((ITextRidget) getRidget("suchOrt")).bindToModel(customerSearchBean, "city");
 		((ITextRidget) getRidget("suchStrasse")).bindToModel(customerSearchBean, "street");
 		// ((ILabelRidget) getRidget("treffer")).bindToModel(customerSearchBean,
 		// "treffer");
 
 		final ITableRidget kunden = ((ITableRidget) getRidget("ergebnis"));
 		String[] columnNames = { "lastname", "firstname", "custno.", "birthdate", "street", "zip", "city", "status", "salesrep", "phone" };
 		String[] propertyNames = { "lastName", "firstName", "customerNumber", "birthdate", "street", "zipcode", "city", "status", "salesrepno",
 				"telefoneNumber" };
 		final SearchResultContainer searchResultContainer = new SearchResultContainer();
 		kunden.bindToModel(searchResultContainer, "customerList", CustomerRecordOverview.class, propertyNames, columnNames);
 
 		((IActionRidget) getRidget("openCustomer")).addListener(new IActionListener() {
 
 			public void callback() {
 				int selectionIndex = kunden.getSelectionIndex();
 				if (selectionIndex == 0) {
					CustomerLoader.setFirstName(searchResultContainer.getCustomerList().get(selectionIndex).getFirstName());
					CustomerLoader.setLastName(searchResultContainer.getCustomerList().get(selectionIndex).getLastName());
 				}
 				getNavigationNode().navigate(
 						new NavigationNodeId("org.eclipse.riena.demo.client.module.CustomerRecord", Integer.valueOf(selectionIndex).toString()));
 			}
 		});
 
 		((IActionRidget) getRidget("reset")).addListener(new IActionListener() {
 
 			public void callback() {
 				searchResultContainer.setCustomerList(null);
 				((IRidget) getRidget("ergebnis")).updateFromModel();
 			}
 		});
 		((IActionRidget) getRidget("search")).addListener(new IActionListener() {
 
 			public void callback() {
 				searchResultContainer.setCustomerList(null);
 				((IRidget) getRidget("ergebnis")).updateFromModel();
 				ergebnis = customerDemoService.suche(getSuchPerson());
 				// List<PersonenSucheErgebnisBean> kundenRows = new
 				// ArrayList<PersonenSucheErgebnisBean>();
 				if (ergebnis == null || ergebnis.getFehler()) {
 					((ILabelRidget) getRidget("treffer")).setText("Keine Treffer");
 					((IRidget) getRidget("treffer")).updateFromModel();
 					return;
 				}
 				((ILabelRidget) getRidget("treffer")).setText(ergebnis.getErgebnismenge() + " Treffer");
 				((IRidget) getRidget("treffer")).updateFromModel();
 				List<CustomerRecordOverview> result = new ArrayList<CustomerRecordOverview>();
 				for (CustomerRecordOverview cust : ergebnis.getErgebnis()) {
 					result.add(cust);
 				}
 				searchResultContainer.setCustomerList(result);
 				((IRidget) getRidget("ergebnis")).updateFromModel();
 			}
 		});
 	}
 
 	public CustomerSearchBean getSuchPerson() {
 		return customerSearchBean;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.navigation.ui.controllers.SubModuleController#afterBind
 	 * ()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
 
 		ITableRidget kunden = ((ITableRidget) getRidget("ergebnis"));
 		kunden.setSelectionType(SelectionType.MULTI);
 		for (int i = 0; i < 9; i++) {
 			kunden.setColumnSortable(i, true);
 		}
 	}
 }
