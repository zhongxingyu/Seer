 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import com.allen_sauer.gwt.dnd.client.DragContext;
 import com.allen_sauer.gwt.dnd.client.DragHandler;
 import com.allen_sauer.gwt.dnd.client.DragEndEvent;
 import com.allen_sauer.gwt.dnd.client.DragStartEvent;
 import com.allen_sauer.gwt.dnd.client.PickupDragController;
 import com.allen_sauer.gwt.dnd.client.VetoDragException;
 import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
 import com.allen_sauer.gwt.dnd.client.drop.DropController;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Events
     extends Composite
     implements ValueChangeHandler<List<JSCharacter>> {
 
     VerticalPanel vpanel = new VerticalPanel();
     VerticalPanel vmsgpanel = new VerticalPanel();
     VerticalPanel veventpanel = new VerticalPanel();
     final DateTimeFormat time_formatter = DateTimeFormat.getFormat("EEE MMM dd, yyyy hh:mm aaa");
 
     private List<JSCharacter> characters = new ArrayList<JSCharacter>();
     private String account_key;
     private JSGuild current_guild;
 
     PickupDragController dragController;
 
     public ValueChangeHandler<JSGuild> guildChange
                                         = new ValueChangeHandler<JSGuild>() {
         public void onValueChange(ValueChangeEvent<JSGuild> event) {
             current_guild = event.getValue();
             refresh();
         }
     };
 
     private final CharacterCache chr_cache = new CharacterCache();
 
     public class Event
         extends Composite
         implements DragHandler {
 
         static final String SPACE_AVAILABLE = "available";
 
         JSEvent event = null;
         JSEventSignups signups = null;
 
         VerticalPanel vpanel = new VerticalPanel();
         HorizontalPanel hchrpanel = new HorizontalPanel();
         HorizontalPanel hchractions = new HorizontalPanel();
         FlexTable flex = new FlexTable();
         boolean have_character_signed_up = false;
         Label signup_error = new Label();
 
         private Set<DropController> registered_drop_controllers = new HashSet<DropController>();
         Map<String, RoleSignup> role_signups = new HashMap<String, RoleSignup>();
         private DropController removeSignupDC;
 
         public Event(JSEvent event) {
             this.event = event;
 
             signup_error.addStyleName("error");
             signup_error.addStyleName("gwt-Button-bottom");
             signup_error.addStyleName("gwt-Button-right");
 
             vpanel.setWidth("100%");
             flex.setWidth("100%");
 
             vpanel.add(new Label(event.name));
             vpanel.add(new Label(time_formatter.format(event.start_time)));
             vpanel.add(flex);
 
             hchractions.setWidth("100%");
             vpanel.add(hchractions);
 
             hchrpanel.setSpacing(10);
             hchractions.add(hchrpanel);
             hchractions.add(signup_error);
 
             setSignups(null);
             fetchSignups();
 
             initWidget(vpanel);
 
             setStyleName("Event");
         }
 
         void setEvent(JSEvent event) {
             JSEvent old_event = this.event;
             this.event = event;
             if (event == null || !event.equals(old_event)) {
                 showSignups(false);
             }
         }
 
         void remakeFlex(FlexTable newFlex) {
             // recreate the flex table; clear() doesn't remove columns
             int index = vpanel.getWidgetIndex(flex);
             vpanel.remove(flex);
             flex = newFlex;
             flex.setWidth("100%");
             vpanel.insert(flex, index);
             resizeRows();
         }
 
         void styleColumn(FlexCellFormatter formatter, int column) {
             formatter.addStyleName(1, column, "coming");
             formatter.setVerticalAlignment(1, column, HasVerticalAlignment.ALIGN_TOP);
             formatter.addStyleName(2, column, "standby");
             formatter.setVerticalAlignment(2, column, HasVerticalAlignment.ALIGN_TOP);
             formatter.addStyleName(3, column, "maybe");
             formatter.setVerticalAlignment(3, column, HasVerticalAlignment.ALIGN_TOP);
             formatter.addStyleName(4, column, "notcoming");
             formatter.setVerticalAlignment(4, column, HasVerticalAlignment.ALIGN_TOP);
         }
 
         class Signup extends Label {
             String eventKey;
             JSCharacter chr;
             JSEventSignup sup;
 
             public Signup(String eventKey, JSCharacter chr, JSEventSignup sup) {
                 this(eventKey, chr);
                 this.sup = sup;
             }
 
             public Signup(String eventKey, JSCharacter chr) {
                 super(chr.name);
                 this.eventKey = eventKey;
                 this.chr = chr;
                 dragController.makeDraggable(this);
                 if (chr.account_key.equals(account_key)) {
                     addStyleName("draggable-character");
                 }
                 else {
                     addStyleName("non-draggable-character");
                 }
                 addStyleName(chr.clazz.replace(' ', '-'));
             }
 
             public JSCharacter getCharacter() {
                 return chr;
             }
         }
 
         void setSignups(JSEventSignups signups) {
             this.signups = signups;
             showSignups(true);
         }
 
         AsyncCallback<JSEventSignups> signupCallback = new AsyncCallback<JSEventSignups>() {
             public void onSuccess(JSEventSignups signups) {
                 if (signups == null) {
                     // unchanged
                     return;
                 }
 
                 setSignups(signups);
             }
 
             public void onFailure(Throwable caught) {
                 // re-setSignups to rebuild interface, otherwise a failed
                 // drop will leave a signup missing
                 setSignups(signups);
                 signup_error.setText(caught.getMessage());
             }
         };
 
         class RoleDropController extends AbstractDropController {
             String eventKey;
             JSEventRole role;
             Widget dropTarget;
             int signupType;
             String highlightStyle;
 
             RoleDropController(String eventKey, JSEventRole role,
                                Widget widget, int signupType,
                                String highlightStyle) {
                 super(widget);
                 this.eventKey = eventKey;
                 this.role = role;
                 this.dropTarget = widget;
                 this.signupType = signupType;
                 this.highlightStyle = highlightStyle;
             }
 
             boolean allowDrop(Signup signup) {
                 JSCharacter chr = signup.getCharacter();
 
                 if (role == null) {
                     return true;
                 }
 
                 // character must have this role
                 if (!chr.hasRole(role.role_key)) {
                     signup_error.setText(chr.name + " is missing the role '"
                                          + role.name + ".'");
                     return false;
                 }
 
                 // signups must have started
                 long now = System.currentTimeMillis();
                 long signups_start = Event.this.event.signups_start.getTime();
                 if (now < signups_start) {
                     // character needs to qualify for an early signup for this
                     // role
                     boolean found = false;
                     boolean found_early = false;
                     long earliest_signup = signups_start;
 
                     for (JSEventBadge badge : Event.this.event.badges) {
                         if (!chr.hasBadge(badge.badge_key)) {
                             continue;
                         }
 
                         // if this badge applies to a role, it must apply to
                         // the drop target role to count
                         if (   badge.applyToRole != null
                             && !badge.applyToRole.equals(role.name)) {
                             continue;
                         }
 
                         // character qualifies, but has it started?
                         long badge_signups_start = signups_start - badge.earlySignup * 3600000L;
                         if (now < badge_signups_start) {
                             found_early = true;
                             earliest_signup = Math.min(earliest_signup, badge_signups_start);
                             continue;
                         }
 
                         found = true;
                         break;
                     }
 
                     if (!found) {
                         if (found_early) {
                             signup_error.setText(chr.name + " can't sign up"
                                                 + " for this role yet, but"
                                                 + " early signups start at "
                                                 + time_formatter.format(
                                                     new Date(earliest_signup))
                                                 + ".");
                         }
                         else {
                             signup_error.setText(chr.name + " can't sign up"
                                                 + " for this role yet,"
                                                 + " signups start at "
                                                 + time_formatter.format(
                                                     new Date(earliest_signup))
                                                 + ".");
                         }
                         return false;
                     }
                 }
 
                 signup_error.setText("");
 
                 return true;
             }
 
             @Override
             public void onEnter(DragContext context) {
                 super.onEnter(context);
                 Signup sup = (Signup)context.selectedWidgets.get(0);
 
                 // ignore drag events for other raid events
                 if (!sup.eventKey.equals(eventKey)) {
                     return;
                 }
 
                 if (allowDrop(sup)) {
                     dropTarget.addStyleName(highlightStyle);
                 }
                 else {
                     dropTarget.addStyleName("bad-drop-target");
                 }
             }
 
             @Override
             public void onLeave(DragContext context) {
                 dropTarget.removeStyleName(highlightStyle);
                 dropTarget.removeStyleName("bad-drop-target");
                 super.onLeave(context);
             }
 
             @Override
             public void onPreviewDrop(DragContext context)
                 throws VetoDragException {
                 super.onPreviewDrop(context);
                 Signup sup = (Signup)context.selectedWidgets.get(0);
 
                 // reject drag events for other raid events
                 if (!sup.eventKey.equals(eventKey)) {
                     throw new VetoDragException();
                 }
 
                 // is this signup allowed to happen?
                 if (!allowDrop(sup)) {
                     throw new VetoDragException();
                 }
 
                 // is this no-op drop?
                 if (   sup.sup != null
                     && role != null
                     && sup.sup.role_key.equals(role.role_key)
                     && sup.sup.signup_type == signupType) {
                     throw new VetoDragException();
                 }
             }
 
             @Override
             public void onDrop(DragContext context) {
                 super.onDrop(context);
                 Signup sup = (Signup)context.selectedWidgets.get(0);
                 sup.removeFromParent();
                 signup_error.setText("");
                 if (sup.sup == null) {
                     // new signup
                     GoteFarm.goteService.signupForEvent(event.key,
                                                         sup.chr.key, role.role_key,
                                                         signupType,
                                                         signupCallback);
                 }
                 else if (signupType < 0) {
                     // deleting signup
                     GoteFarm.goteService.removeEventSignup(GoteFarm.sessionID,
                                                            event.key,
                                                            sup.sup.key,
                                                            signupCallback);
                 }
                 else
                 {
                     // changing signup
                     GoteFarm.goteService.changeEventSignup(GoteFarm.sessionID,
                                                            event.key,
                                                            sup.sup.key,
                                                            role.role_key,
                                                            signupType,
                                                            signupCallback);
                 }
             }
         }
 
         class RoleSignup {
             JSEvent event;
             JSEventRole role;
             int column = -1;
             int spaces_left;
             Set<JSEventBadge> badges_required = new HashSet<JSEventBadge>();
             Map<JSEventBadge, Integer> badges_needed = new HashMap<JSEventBadge, Integer>();
             List<JSEventSignup> coming = new ArrayList<JSEventSignup>();
             List<JSEventSignup> standby = new ArrayList<JSEventSignup>();
             List<JSEventSignup> maybe = new ArrayList<JSEventSignup>();
             List<JSEventSignup> not_coming = new ArrayList<JSEventSignup>();
             DropController coming_dc;
             DropController maybe_dc;
             DropController not_coming_dc;
 
             RoleSignup(JSEvent event, JSEventRole role) {
                 this.event = event;
                 this.role = role;
             }
         }
 
         void showSignups(final boolean call_characters_changed) {
             try {
                 unregisterDropControllers();
                 remakeFlex(makeNewSignupTable());
                 if (call_characters_changed) {
                     charactersChanged();
                 }
             }
             catch (CharacterNotCachedException e) {
                 // Fetch the missing character and try again
                 // TODO: indicate progress in the UI somehow
                 final String key = e.getCharacterKey();
                 GWT.log("Requesting missing cached character: " + key, e);
                 GoteFarm.goteService.getCharacter(
                                             key,
                                             new AsyncCallback<JSCharacter>() {
                     public void onSuccess(JSCharacter chr) {
                         chr_cache.put(key, chr);
                         // recurse (sort of)
                         showSignups(call_characters_changed);
                     }
 
                     public void onFailure(Throwable caught) {
                         // TODO: what to do here?
                     }
                 });
             }
         }
 
         FlexTable makeNewSignupTable() throws CharacterNotCachedException {
             FlexTable flex = new FlexTable();
 
             have_character_signed_up = false;
 
             List<JSEventSignup> limbo = new ArrayList<JSEventSignup>();
             role_signups.clear();
 
             // For each role, figure out the badge requirements
             for (JSEventRole role : event.roles) {
                 RoleSignup rsup = new RoleSignup(event, role);
                 rsup.spaces_left = role.max;
                 role_signups.put(role.role_key, rsup);
 
                 for (JSEventBadge badge : event.badges) {
                     if (   badge.requireForSignup
                         && (   badge.applyToRole == null
                             || badge.applyToRole.equals(role.name))) {
                         if (badge.applyToRole == null) {
                             rsup.badges_required.add(badge);
                         }
                         else {
                             rsup.badges_needed.put(badge, badge.numSlots);
                         }
                     }
                 }
             }
 
             int spots_left = event.size;
 
             if (signups != null) {
                 // Categorize the current signups
                 for (JSEventSignup es : signups.signups) {
                     JSCharacter chr = chr_cache.get(es.chr_key);
 
                     if (chr.account_key.equals(account_key)) {
                         have_character_signed_up = true;
                     }
 
                     RoleSignup rsup = role_signups.get(es.role_key);
 
                     // role does not exist: limbo
                     if (rsup == null) {
                         limbo.add(es);
                         continue;
                     }
 
                     // user not coming
                     if (es.signup_type == JSEventSignup.SIGNUP_TYPE_MAYBE) {
                         rsup.maybe.add(es);
                         continue;
                     }
                     else if (es.signup_type == JSEventSignup.SIGNUP_TYPE_NOT_COMING) {
                         rsup.not_coming.add(es);
                         continue;
                     }
 
                     // event full?
                     if (spots_left == 0) {
                         rsup.standby.add(es);
                         continue;
                     }
 
                     // role full?
                     if (rsup.spaces_left == 0) {
                         rsup.standby.add(es);
                         continue;
                     }
 
                     // character missing required badges?
                     boolean standby = false;
                     for (JSEventBadge req_badge : rsup.badges_required) {
                         if (!chr.hasBadge(req_badge.badge_key)) {
                             rsup.standby.add(es);
                             standby = true;
                             break;
                         }
                     }
 
                     // check for reserved spots
                     int role_spots_left = Math.min(spots_left, rsup.spaces_left);
                     for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                         if (needed.getValue() >= role_spots_left) {
                             // character must have this badge to sign up
                             if (!chr.hasBadge(needed.getKey().badge_key)) {
                                 rsup.standby.add(es);
                                 standby = true;
                                 break;
                             }
                         }
                     }
 
                     if (standby) {
                         continue;
                     }
 
                     // this character is allowed in!
                     --spots_left;
                     --rsup.spaces_left;
 
                     // subtract out any badges requirements this character fulfills
                     List<JSEventBadge> to_delete = new ArrayList<JSEventBadge>();
                     for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                         int num = needed.getValue();
                         if (chr.hasBadge(needed.getKey().badge_key)) {
                             if (num == 1) {
                                 to_delete.add(needed.getKey());
                             }
                             else {
                                 needed.setValue(num - 1);
                             }
                         }
                     }
 
                     for (JSEventBadge badge : to_delete) {
                         rsup.badges_needed.remove(badge);
                     }
 
                     rsup.coming.add(es);
                 }
             }
 
             FlexCellFormatter formatter = flex.getFlexCellFormatter();
 
             flex.setText(1, 0, JSEventSignup.SIGNUP_LABEL_COMING);
             flex.setText(2, 0, "Standby");
             flex.setText(3, 0, JSEventSignup.SIGNUP_LABEL_MAYBE);
             flex.setText(4, 0, JSEventSignup.SIGNUP_LABEL_NOT_COMING);
 
             styleColumn(formatter, 0);
 
             SimplePanel sp;
 
             int column = 1;
             for (JSEventRole role : event.roles) {
                 RoleSignup rsup = role_signups.get(role.role_key);
                 rsup.column = column;
 
                 flex.setText(0, column, role.name);
 
                 styleColumn(formatter, column);
 
                 sp = new SimplePanel();
                 VerticalPanel vsign = new VerticalPanel();
 
                 // show signups
                 for (JSEventSignup sup : rsup.coming) {
                     JSCharacter chr = chr_cache.get(sup.chr_key);
                     vsign.add(new Signup(event.key, chr, sup));
                 }
 
                 // show remaining slots
                 int eff_spots_left = Math.min(rsup.spaces_left, spots_left);
                 for (int i = 0; i < eff_spots_left; ++i) {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     {
                         Label l = new Label(SPACE_AVAILABLE);
                         l.addStyleName("available-character");
                         hpanel.add(l);
                     }
 
                     for (JSEventBadge badge : rsup.badges_required) {
                         Label l = new Label("!");
                         l.setTitle("Badge required: " + badge.name);
                         hpanel.add(l);
                     }
 
                     for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                         int num = needed.getValue();
 
                         if (num >= eff_spots_left) {
                             Label l = new Label("!");
                             l.setTitle("Badge required: " + needed.getKey().name);
                             hpanel.add(l);
                         }
                         // mark the last spots as reserved, they will switch to
                         // required as more people sign up who are missing this
                         // badge
                         else if (num >= eff_spots_left - i) {
                             Label l = new Label("*");
                             l.setTitle("Reserved for: " + needed.getKey().name);
                             hpanel.add(l);
                         }
                     }
 
                     vsign.add(hpanel);
                 }
 
                 sp.add(vsign);
                 flex.setWidget(1, column, sp);
                 rsup.coming_dc = new RoleDropController(event.key, role, sp,
                                                         JSEventSignup.SIGNUP_TYPE_COMING,
                                                         "coming-hi");
                 dragController.registerDropController(rsup.coming_dc);
                 registered_drop_controllers.add(rsup.coming_dc);
 
                 // show standby
                 VerticalPanel vstand = new VerticalPanel();
 
                 for (JSEventSignup sup : rsup.standby) {
                     JSCharacter chr = chr_cache.get(sup.chr_key);
                     vstand.add(new Signup(event.key, chr, sup));
                 }
 
                 flex.setWidget(2, column, vstand);
 
                 // show maybe
                 sp = new SimplePanel();
                 VerticalPanel vmaybe = new VerticalPanel();
 
                 for (JSEventSignup sup : rsup.maybe) {
                     JSCharacter chr = chr_cache.get(sup.chr_key);
                     vmaybe.add(new Signup(event.key, chr, sup));
                 }
 
                 if (rsup.maybe.isEmpty()) {
                     vmaybe.add(new HTML("&nbsp;"));
                 }
 
                 sp.add(vmaybe);
                 flex.setWidget(3, column, sp);
                 rsup.maybe_dc = new RoleDropController(event.key, role, sp,
                                                        JSEventSignup.SIGNUP_TYPE_MAYBE,
                                                        "maybe-hi");
                 dragController.registerDropController(rsup.maybe_dc);
                 registered_drop_controllers.add(rsup.maybe_dc);
 
                 // show not coming
                 sp = new SimplePanel();
                 VerticalPanel vnotcoming = new VerticalPanel();
 
                 for (JSEventSignup sup : rsup.not_coming) {
                     JSCharacter chr = chr_cache.get(sup.chr_key);
                     vnotcoming.add(new Signup(event.key, chr, sup));
                 }
 
                 if (rsup.not_coming.isEmpty()) {
                     vnotcoming.add(new HTML("&nbsp;"));
                 }
 
                 sp.add(vnotcoming);
                 flex.setWidget(4, column, sp);
                 rsup.not_coming_dc = new RoleDropController(event.key, role, sp,
                                                             JSEventSignup.SIGNUP_TYPE_NOT_COMING,
                                                             "notcoming-hi");
                 dragController.registerDropController(rsup.not_coming_dc);
                 registered_drop_controllers.add(rsup.not_coming_dc);
 
                 ++column;
             }
 
             if (!limbo.isEmpty()) {
                 styleColumn(formatter, column);
 
                 // create limbo column
                 flex.setText(0, column, "Limbo");
 
                 VerticalPanel vstand = new VerticalPanel();
 
                 for (JSEventSignup sup : limbo) {
                     JSCharacter chr = chr_cache.get(sup.chr_key);
                     vstand.add(new Signup(event.key, chr, sup));
                 }
 
                 flex.setWidget(2, column, vstand);
 
                 ++column;
             }
 
             return flex;
         }
 
         void resizeRows() {
             int column = flex.getCellCount(1);
 
             // Each role <td> must have the same number of items in it to size
             // the drop area so it fills the <td>. This hack should be removed
             // if a way to get the VerticalPanel to fill its space vertically
             // is found.
             int most_coming = 0;
             int most_maybe = 0;
             int most_not_coming = 0;
             SimplePanel sp;
             VerticalPanel vp;
             UIObject uio;
 
             for (int c = 1; c < column; ++c) {
                 sp = (SimplePanel)flex.getWidget(1, c);
                 if (sp != null) {
                     vp = (VerticalPanel)sp.getWidget();
                     most_coming = Math.max(most_coming, vp.getOffsetHeight());
                 }
 
                 sp = (SimplePanel)flex.getWidget(3, c);
                 if (sp != null) {
                     vp = (VerticalPanel)sp.getWidget();
                     most_maybe = Math.max(most_maybe, vp.getOffsetHeight());
                 }
 
                 sp = (SimplePanel)flex.getWidget(4, c);
                 if (sp != null) {
                     vp = (VerticalPanel)sp.getWidget();
                     most_not_coming = Math.max(most_not_coming, vp.getOffsetHeight());
                 }
             }
 
             for (int c = 1; c < column; ++c) {
                 uio = (UIObject)flex.getWidget(1, c);
                 if (uio != null) {
                     uio.setHeight("" + most_coming + "px");
                 }
 
                 uio = (UIObject)flex.getWidget(3, c);
                 if (uio != null) {
                     uio.setHeight("" + most_maybe + "px");
                 }
 
                 uio = (UIObject)flex.getWidget(4, c);
                 if (uio != null) {
                     uio.setHeight("" + most_not_coming + "px");
                 }
             }
         }
 
         public void charactersChanged() {
             hchrpanel.clear();
             if (removeSignupDC != null) {
                 dragController.unregisterDropController(removeSignupDC);
                 removeSignupDC = null;
             }
 
             if (have_character_signed_up) {
                 Label l = new Label("You are signed up for this event. Drag to here to delete your signup.");
                 hchrpanel.add(l);
                 removeSignupDC = new RoleDropController(event.key, null, l, -1, "coming");
                 dragController.registerDropController(removeSignupDC);
                 return;
             }
 
             if (characters.isEmpty()) {
                 hchrpanel.add(new Label("You have no characters enrolled."));
                 hchrpanel.add(new Hyperlink("Manage characters", "characters"));
                 return;
             }
 
             // FIXME: the user's account_key is already known from calling
             // getAccount, replace this hack
             account_key = characters.get(0).account_key;
 
             for (JSCharacter c : characters) {
                 hchrpanel.add(new Signup(event.key, c));
             }
         }
 
         void populateRole(JSEventRole role, int column) {
             flex.setText(0, column, role.name + " (" + role.min + "/" + role.max + ")");
 
             HashMap<String, Integer> role_badges = new HashMap<String, Integer>();
 
             // find the badge requirements
             for (JSEventBadge badge : event.badges) {
                 if (   badge.requireForSignup
                     && (   badge.applyToRole == null
                         || badge.applyToRole.equals(role.name))) {
                     int num_slots;
                     if (badge.applyToRole == null) {
                         num_slots = role.max;
                     }
                     else {
                         num_slots = badge.numSlots;
                     }
                     role_badges.put(badge.name, num_slots);
                 }
             }
 
             int spaces_left = role.max;
             for (int row = 1; row <= role.max; ++row) {
 
                 StringBuilder sb = new StringBuilder(64).append(SPACE_AVAILABLE);
 
                 for (Map.Entry<String, Integer> en : role_badges.entrySet()) {
                     int val = en.getValue();
                     if (val > 0) {
                         if (val >= spaces_left) {
                             sb.append(" - requires ")
                               .append(en.getKey());
                         }
                         else {
                             sb.append(" - reserved for ")
                               .append(en.getKey());
                         }
 
                         en.setValue(val - 1);
                     }
                 }
 
                 flex.setText(row, column, sb.toString());
 
                 --spaces_left;
             }
         }
 
         void fetchSignups() {
             Date asof;
             if (signups != null) {
                 asof = signups.asof;
             }
             else {
                 asof = new Date(0L);
             }
 
             GoteFarm.goteService.getEventSignups(event.key, asof,
                                                  signupCallback);
         }
 
         public void onDragEnd(DragEndEvent event) {
             DragContext c = event.getContext();
             Signup sup = (Signup)c.selectedWidgets.get(0);
 
             // ignore drag events for other raid events
             if (!sup.eventKey.equals(Event.this.event.key)) {
                 return;
             }
 
             // Reset drop targets
             FlexCellFormatter formatter = flex.getFlexCellFormatter();
             for (RoleSignup rsup : role_signups.values()) {
                 formatter.removeStyleName(0, rsup.column, "bad-drop-target");
             }
         }
 
         public void onDragStart(DragStartEvent event) {
             DragContext c = event.getContext();
             Signup sup = (Signup)c.selectedWidgets.get(0);
 
             // ignore drag events for other raid events
             if (!sup.eventKey.equals(Event.this.event.key)) {
                 return;
             }
 
             JSCharacter chr = sup.getCharacter();
 
             FlexCellFormatter formatter = flex.getFlexCellFormatter();
 
             // show which drop targets are viable
             for (Map.Entry<String, RoleSignup> rsup : role_signups.entrySet()) {
                 if (!chr.hasRole(rsup.getKey())) {
                     formatter.addStyleName(0, rsup.getValue().column, "bad-drop-target");
                 }
             }
         }
 
         public void onPreviewDragEnd(DragEndEvent event)
             throws VetoDragException {
             DragContext c = event.getContext();
             Signup sup = (Signup)c.selectedWidgets.get(0);
 
             // ignore drag events for other raid events
             if (!sup.eventKey.equals(Event.this.event.key)) {
                 return;
             }
 
             // reject a drop outside the drop zones for a character that
             // isn't signed up
             if (   c.vetoException == null
                 && !(c.finalDropController instanceof RoleDropController)) {
 
                 if (sup.sup == null) {
                     throw new VetoDragException();
                 }
             }
         }
 
         public void onPreviewDragStart(DragStartEvent event)
             throws VetoDragException {
             DragContext c = event.getContext();
             Signup sup = (Signup)c.selectedWidgets.get(0);
 
             // ignore drag events for other raid events
             if (!sup.eventKey.equals(Event.this.event.key)) {
                 return;
             }
 
             long now = System.currentTimeMillis();
 
             // are signups over?
             if (now >= Event.this.event.signups_end.getTime()) {
                 signup_error.setText("Signups for this event are closed.");
                 throw new VetoDragException();
             }
 
             JSCharacter chr = sup.getCharacter();
 
             // must be at least this high to ride
             if (chr.level < Event.this.event.minimumLevel) {
                 signup_error.setText(chr.name + " is level " + chr.level
                                         + ", this event requires level "
                                         + Event.this.event.minimumLevel + ".");
                 throw new VetoDragException();
             }
 
             // character must have at least one role
             boolean role_found = false;
             for (JSEventRole role : Event.this.event.roles) {
                 for (JSChrRole chrrole : chr.roles) {
                     if (role.role_key.equals(chrrole.key)) {
                         role_found = true;
                         break;
                     }
                 }
             }
             if (!role_found) {
                 signup_error.setText(chr.name + " does not perform any of the"
                                      + " needed roles to sign up for this"
                                      + " event.");
                 throw new VetoDragException();
             }
 
             // check if chr is able to signup yet
             long signups_start = Event.this.event.signups_start.getTime();
             if (now < signups_start) {
                 // character needs to qualify for an early signup
                 boolean found = false;
                 boolean found_early = false;
                 long earliest_signup = signups_start;
 
                 for (JSEventBadge badge : Event.this.event.badges) {
                     if (!chr.hasBadge(badge.badge_key)) {
                         continue;
                     }
 
                     if (badge.applyToRole != null && !chr.hasRole(badge.applyToRole)) {
                         continue;
                     }
 
                     // character qualifies, but has it started?
                     long badge_signups_start = signups_start - badge.earlySignup * 3600000L;
                     if (now < badge_signups_start) {
                         found_early = true;
                         earliest_signup = Math.min(earliest_signup, badge_signups_start);
                         continue;
                     }
 
                     found = true;
                     break;
                 }
 
                 if (!found) {
                     if (found_early) {
                         signup_error.setText(chr.name + " can't sign up yet,"
                                             + " but early signups start at "
                                             + time_formatter.format(
                                                 new Date(earliest_signup))
                                             + ".");
                     }
                     else {
                         signup_error.setText(chr.name + " can't sign up yet,"
                                             + " signups start at "
                                             + time_formatter.format(
                                                 new Date(earliest_signup))
                                             + ".");
                     }
                     throw new VetoDragException();
                 }
             }
 
             signup_error.setText("");
         }
 
         public void unregisterDropControllers() {
             for (DropController dc : registered_drop_controllers) {
                 dragController.unregisterDropController(dc);
             }
             registered_drop_controllers.clear();
         }
     }
 
     public Events() {
         vpanel.setWidth("100%");
         vmsgpanel.setWidth("100%");
         veventpanel.setWidth("100%");
 
         vpanel.add(vmsgpanel);
         vpanel.add(veventpanel);
 
         veventpanel.setSpacing(20);
 
         vmsgpanel.add(new Label("You are not signed in."));
 
         dragController = new PickupDragController(RootPanel.get(), true);
         dragController.setBehaviorBoundaryPanelDrop(false);
         dragController.setBehaviorConstrainedToBoundaryPanel(true);
         dragController.setBehaviorDragStartSensitivity(1);
 
         initWidget(vpanel);
 
         // update the events list once a minute
         Timer refreshTimer = new Timer() {
             public void run() {
                 refresh();
             }
         };
         refreshTimer.scheduleRepeating(60000);
 
         setStyleName("Characters");
     }
 
     private ArrayList<Event> events = new ArrayList<Event>();
 
     public void refresh() {
         vmsgpanel.clear();
 
         if (current_guild == null) {
             vmsgpanel.add(new Label("Loading..."));
             return;
         }
         else if (current_guild.key == null) {
             vmsgpanel.add(new Label("Select a guild to see events."));
             return;
         }
 
         // update the list of events, and also refresh the signup lists of
         // each event
 
         GoteFarm.goteService.getEvents(current_guild.key,
                                        new AsyncCallback<List<JSEvent>>() {
             public void onSuccess(List<JSEvent> result) {
                 vmsgpanel.clear();
 
                 events.ensureCapacity(result.size());
 
                 // merge new events into existing event list, to reuse widgets
 
                 int old_index = 0;
                 int new_index = 0;
                 for (JSEvent e : result) {
                     // is this event already in the events list?
                     Event old_event = null;
 
                     if (old_index < events.size()) {
                         old_event = events.get(old_index);
                     }
 
                     if (   old_event != null
                         && e.key.equals(old_event.event.key)) {
                         // give existing event widget new JSEvent
                         old_event.setEvent(e);
                     }
                     else {
                         // new event, insert
                         Event event = new Event(e);
                         dragController.addDragHandler(event);
                         events.add(old_index, event);
                         veventpanel.insert(event, old_index);
                     }
 
                     ++old_index;
                     ++new_index;
                 }
 
                 // remove remaining old events
                 while (old_index < events.size()) {
                     Event event = events.remove(old_index);
                     event.unregisterDropControllers();
                     dragController.removeDragHandler(event);
                     veventpanel.remove(event);
                 }
 
                 if (result.size() == 0) {
                     vmsgpanel.add(new Label("No events"));
                 }
 
                 for (Event e : events) {
                     e.fetchSignups();
                 }
             }
 
             public void onFailure(Throwable caught) {
                 vmsgpanel.add(new Label(caught.getMessage()));
             }
         });
     }
 
     public void onValueChange(ValueChangeEvent<List<JSCharacter>> event) {
         characters = event.getValue();
         for (Event e : events) {
             e.charactersChanged();
         }
     }
 
     public void resizeRows() {
         for (Event e : events) {
             e.resizeRows();
         }
     }
 }
