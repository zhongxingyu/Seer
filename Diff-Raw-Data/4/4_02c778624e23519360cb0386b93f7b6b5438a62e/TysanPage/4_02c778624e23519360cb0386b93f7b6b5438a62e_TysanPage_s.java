 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok;
 
 import java.util.Arrays;
 import java.util.Calendar;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.head.JavaScriptHeaderItem;
 import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.time.Duration;
 import org.joda.time.LocalDate;
 import org.odlabs.wiquery.ui.dialog.Dialog;
 
 import com.tysanclan.site.projectewok.components.DebugWindow;
 import com.tysanclan.site.projectewok.components.TysanLoginPanel;
 import com.tysanclan.site.projectewok.components.TysanMemberPanel;
 import com.tysanclan.site.projectewok.components.TysanMenu;
 import com.tysanclan.site.projectewok.components.TysanUserPanel;
 import com.tysanclan.site.projectewok.entities.GlobalSetting;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.GlobalSettingDAO;
 import com.tysanclan.site.projectewok.util.AprilFools;
 import com.tysanclan.site.projectewok.util.DateUtil;
 import com.tysanclan.site.projectewok.util.MemberUtil;
 
 /**
  * Web page for use within the Tysan Clan
  * 
  * @author Jeroen Steenbeeke
  */
 public class TysanPage extends WebPage {
 	private static final long serialVersionUID = 1L;
 
 	private static final boolean ENABLE_MAGIC_PUSHTBUTTON = false;
 
 	@SpringBean
 	private GlobalSettingDAO globalSettingDAO;
 
 	private Dialog animalDialog;
 
 	private Label headerLabel;
 
 	private Label titleLabel;
 
 	private final WebMarkupContainer topPanel;
 
 	private final Dialog notificationWindow;
 
 	private boolean autoCollapse = false;
 
 	public TysanPage(String title) {
 		this(title, null);
 	}
 
 	public TysanPage(String title, IModel<?> model) {
 		super(model);
 
 		notificationWindow = new Dialog("notificationWindow");
 		notificationWindow.setTitle("Urgent Message");
 		notificationWindow.setOutputMarkupId(true)
 				.setOutputMarkupPlaceholderTag(true);
 
 		notificationWindow.add(new ComponentFeedbackPanel("messages",
 				notificationWindow).setOutputMarkupId(true)
 				.setOutputMarkupPlaceholderTag(true));
 		notificationWindow.setAutoOpen(false);
 		notificationWindow.setVisible(false);
 
 		add(notificationWindow);
 
 		headerLabel = new Label("header", title);
 		titleLabel = new Label("title", title + " - The Tysan Clan ");
 
 		headerLabel.setEscapeModelStrings(false);
 		titleLabel.setEscapeModelStrings(false);
 
 		add(headerLabel);
 		add(titleLabel);
 		add(new FeedbackPanel("feedback").setOutputMarkupId(true));
 
 		Dialog window = new Dialog("debugWindow");
 		window.setTitle("Debug Information");
 		window.add(new DebugWindow("debugPanel", this.getPageClass()));
 		window.setWidth(600);
 		window.setHeight(300);
 		window.setResizable(false);
 
 		window.add(new AjaxLink<Void>("magicpushbutton") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 
 			}
 
 		}.setVisible(ENABLE_MAGIC_PUSHTBUTTON));
 
 		add(window);
 
 		add(new AjaxLink<Dialog>("debugLink", new Model<Dialog>(window)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				Dialog _window = getModelObject();
 				target.appendJavaScript(_window.open().render().toString());
 
 			}
 
 		}.setVisible(Application.get().getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT));
 
 		User u = getTysanSession().getUser();
 		WebMarkupContainer subMenu = new WebMarkupContainer("topMenu");
 		if (u != null) {
 			if (MemberUtil.isMember(u)) {
 				topPanel = new WebMarkupContainer("topbar");
 				subMenu = new TysanMemberPanel("topMenu", u);
 			} else {
 				topPanel = new WebMarkupContainer("topbar");
 				subMenu = new TysanUserPanel("topMenu", u);
 			}
 		} else {
 			topPanel = new TysanLoginPanel("topbar");
 		}
 		add(new TysanMenu("menu", u != null));
 		add(subMenu);
 
 		add(topPanel);
 
 		add(new Label("version", TysanApplication.getApplicationVersion()));
 
 		if (u != null) {
 			get("version").add(
 					new AjaxSelfUpdatingTimerBehavior(Duration.seconds(30)) {
 						private static final long serialVersionUID = 1L;
 
 						/**
 						 * @see org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior#onPostProcessTarget(org.apache.wicket.ajax.AjaxRequestTarget)
 						 */
 						@Override
 						protected void onPostProcessTarget(
 								AjaxRequestTarget target) {
 							Dialog d = getNotificationWindow();
 							TysanSession t = TysanSession.get();
 							int i = 0;
 
 							for (SiteWideNotification swn : TysanApplication
 									.get().getActiveNotifications()) {
 								if (t != null && !t.notificationSeen(swn)) {
 									swn.display(d);
 									i++;
 								}
 							}
 
 							if (i > 0) {
 								d.setAutoOpen(true);
 								d.setVisible(true);
 								target.add(d);
 								getNotificationWindow().open(target);
 							}
 						}
 					});
 
 		}
 		addAnimalPanel();
 
 		add(new Label("year", LocalDate.now().getYear())
 				.setRenderBodyOnly(true));
 	}
 
 	public void setAutoCollapse(boolean autoCollapse) {
 		this.autoCollapse = autoCollapse;
 	}
 
 	public User getUser() {
 		return getTysanSession() != null ? getTysanSession().getUser() : null;
 	}
 
 	public TysanSession getTysanSession() {
 		return (TysanSession) getSession();
 	}
 
 	/**
 	 * @return the notificationWindow
 	 */
 	public Dialog getNotificationWindow() {
 		return notificationWindow;
 	}
 
 	public void setPageTitle(String title) {
 		remove(headerLabel);
 		remove(titleLabel);
 
 		String nTitle = title + " - The Tysan Clan";
 
 		headerLabel = new Label("header", title);
 		titleLabel = new Label("title", nTitle);
 
 		headerLabel.setEscapeModelStrings(false);
 		titleLabel.setEscapeModelStrings(false);
 
 		add(headerLabel);
 		add(titleLabel);
 
 	}
 
 	private void addAnimalPanel() {
 		Calendar cal = DateUtil.getCalendarInstance();
 
 		GlobalSetting animalSetting = globalSettingDAO
 				.get(AprilFools.KEY_ANIMALS);
 
 		boolean isAprilFoolsDay2011 = (cal.get(Calendar.MONTH) == Calendar.APRIL
 				&& cal.get(Calendar.DAY_OF_MONTH) == 1 && cal
 				.get(Calendar.YEAR) == 2011);
 
 		if (getUser() != null && MemberUtil.isMember(getUser())) {
 			if (animalSetting != null || isAprilFoolsDay2011) {
 
 				String validOption = "";
 
 				if (animalSetting != null) {
 					validOption = animalSetting.getValue();
 				} else {
 					validOption = AprilFools.getRandomAnimalOption();
 				}
 
 				int showChance = isAprilFoolsDay2011 ? 249 : 0;
 
 				boolean show = showChance > AprilFools.rand.nextInt(1000);
 
 				animalDialog = new Dialog("animals");
 				animalDialog.setAutoOpen(show);
 				animalDialog.setTitle("The animals!");
 				animalDialog.setVisible(show);
 
 				animalDialog.add(new ContextImage("picture", AprilFools
 						.getRandomAnimal()));
 
 				animalDialog.add(new AnimalOptionListView("options",
 						validOption));
 
 				add(animalDialog);
 
 			} else {
 				add(new WebMarkupContainer("animals").setVisible(false));
 			}
 		} else {
 			add(new WebMarkupContainer("animals").setVisible(false));
 		}
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		super.renderHead(response);
 
 		response.render(JavaScriptHeaderItem
 				.forReference(TysanJQueryUIInitialisationResourceReference
 						.get()));
 
 		Integer autoTabIndex = getAutoTabIndex();
 
 		if (autoTabIndex != null) {
 			response.render(OnDomReadyHeaderItem.forScript(String.format(
 					"$('.jqui-tabs-auto').tabs({ active: %d });", autoTabIndex)));
 		} else {
 			response.render(OnDomReadyHeaderItem
 					.forScript("$('.jqui-tabs-auto').tabs();"));
 		}
 
 		StringBuilder collapsibles = new StringBuilder();
 		collapsibles.append("$('.jqui-accordion-collapsible').accordion({\n");
 		collapsibles.append("\tautoHeight: true,\n");
 		collapsibles.append("\theader: 'h2',\n");
 		collapsibles.append("\theightStyle: 'content',\n");
 		collapsibles.append("\tcollapsible: true,\n");
 		if (autoCollapse) {
 			collapsibles.append("\tactive: false\n");
 		} else {
 			collapsibles.append("\tactive: 0\n");
 		}
 		collapsibles.append("});");
 
 		response.render(OnDomReadyHeaderItem.forScript(collapsibles));

 	}
 
 	protected Integer getAutoTabIndex() {
 		return null;
 	}
 
 	private class AnimalOptionListView extends ListView<String> {
 
 		private static final long serialVersionUID = 1L;
 
 		private final String validOption;
 
 		public AnimalOptionListView(String id, String validOption) {
 			super(id, Arrays.asList(AprilFools.getOptions()));
 
 			this.validOption = validOption;
 		}
 
 		@Override
 		protected void populateItem(ListItem<String> item) {
 			String option = item.getModelObject();
 
 			AjaxLink<String> optionLink = new AjaxLink<String>("choice",
 					new Model<String>(option)) {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onClick(AjaxRequestTarget target) {
 					if (!getModelObject().equals(validOption)) {
 						// Perform rickroll
 						AprilFools.performRickRoll();
 					} else {
 						// Close window
 						animalDialog.close(target);
 					}
 				}
 			};
 
 			optionLink.add(new Label("label", option));
 
 			item.add(optionLink);
 
 		}
 
 	}
 }
