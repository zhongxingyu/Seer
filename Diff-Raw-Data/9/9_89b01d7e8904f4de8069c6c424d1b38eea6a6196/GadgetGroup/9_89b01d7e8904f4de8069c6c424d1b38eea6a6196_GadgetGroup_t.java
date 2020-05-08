 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.gadgetcontainer.domain;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.LinkedList;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.JoinColumn;
 import javax.persistence.Table;
 
 import com.globant.katari.hibernate.coreuser.domain.CoreUser;
 import com.globant.katari.shindig.domain.Application;
 
 /** Represents a group of gadgets that can be displayed on a web page.
  *
  * Gadgets are intended to be shown in a page in a column layout. Each gadget
  * defines the column and the position (order) in that column.
  *
  * Groups can be static (shared) or customizable. Static gadget groups do not
  * have an owner, and are accesible to everybody. Customizable gadget groups
  * always have an owner and can be moved by him.
  *
  * Groups can be created on demand, from scratch, or copied from a template
  * gadget group. Template gadget groups are not intended to be shown to the
  * user, they serve as the basis to create new gadget groups for users.
  *
  * @author waabox(emiliano[dot]arango[at]globant[dot]com)
  */
 @Entity
 @Table(name = "gadget_groups")
 public class GadgetGroup {
 
   /** The enumeration for the type of gadget group.
    */
   public static enum Type {
 
     /** A shared group is shared by many users and cannot be customizable.
      */
     SHARED,
 
     /** A customizable group is owned by only one user, and users can add, move
      * and remove gadgets.
      */
     CUSTOMIZABLE,
 
     /** A template is never used by final users, it is used to create other
      * customizable gadget groups.
      */
     TEMPLATE
   };
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(GadgetGroup.class);
 
   /** The id of the gadget group, 0 for a newly created gadget group.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
 
   /** The type of group.
    *
    * A customizable group must have an owner. A shared group never has an
    * owner. A template never has an owner, and cannot be shown to the final
    * user.
    *
    * This is never null.
    *
    * TODO This should probably be mapped with a hibernate user type. Or to a
    * string. This is now mapped as an int, the position of the enum in the list
    * which is very error prone.
    */
   @Column(nullable = false)
   private Type type;
 
   /** The name of the group.
    *
    * This is never null.
    */
   @Column(nullable = false)
   private String name;
 
   /** The owner of this gadget group.
    *
    * If null, this is a static group.
    */
   @ManyToOne(optional = true, fetch = FetchType.EAGER)
   private CoreUser owner = null;
 
   /** The view of this gadget group.
    *
    * This will only hold gadgtes that support this view or the default view. It
    * is never null.
    */
   @Column(name = "view_name", nullable = false)
   private String view;
 
   /** The gadgets in this group.
    *
    * It is never null.
    */
   @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
   @JoinColumn(name = "gadget_group_id")
   private Set<GadgetInstance> gadgets = new HashSet<GadgetInstance>();
 
   /** The number of columns of the gadget group.
    */
   @Column(name = "number_of_columns", nullable = false)
   private int numberOfColumns = 1;
 
   /** Hibernate constructor.
    */
   GadgetGroup() {
   }
 
   /** Constructor.
    *
    * @param user the user that owns the gadget group. If null, it is a static
    * group.
    *
    * @param groupName name of the group. It cannot be null
    *
    * @param viewName name of the view. This gadget will only contain gadgets
    * that support this view or the default view. It cannot be null
    *
    * @param columns the number of columns in the group. It must be 1 or
    * greater.
    */
   public GadgetGroup(final CoreUser user, final String groupName,
       final String viewName, final int columns) {
     Validate.notEmpty(groupName, "Group name can not be null nor empty.");
     Validate.notEmpty(viewName, "View name can not be null nor empty.");
     Validate.isTrue(columns >= 1, "Number of columns must be 1 or greater.");
 
     if (user != null) {
       type = Type.CUSTOMIZABLE;
     } else {
       type = Type.SHARED;
     }
 
     name = groupName;
     view = viewName;
     owner = user;
     numberOfColumns = columns;
   }
 
   /** Builds a gadget group template.
    *
    * @param groupName name of the group. It cannot be null
    *
    * @param viewName name of the view. This gadget will only contain gadgets
    * that support this view or the default view. It cannot be null
    *
    * @param columns the number of columns in the group. It must be 1 or
    * greater.
    */
   public GadgetGroup(final String groupName, final String viewName,
       final int columns) {
     Validate.notEmpty(groupName, "Group name can not be null nor empty.");
     Validate.notEmpty(viewName, "View name can not be null nor empty.");
     Validate.isTrue(columns >= 1, "Number of columns must be 1 or greater.");
     name = groupName;
     view = viewName;
     numberOfColumns = columns;
 
     type = Type.TEMPLATE;
   }
 
   /** Obtains the group id.
    *
    * @return the id, 0 for a newly created object.
    */
   public long getId() {
     return id;
   }
 
   /** Obtains the group name.
    *
    * @return the group name, never null.
    */
   public String getName() {
     return name;
   }
 
   /** Returns the name of the view.
   *
    * This gadget will only contain gadgets that support this view or the
    * default view.
    *
    * @return the view name, never null.
    */
   public String getView() {
     return view;
   }
 
   /** Returns the owner of a customizable group, or null if this is as static
    * group.
    *
    * If the group is customizable, this never returns null.
    *
    * @return the group owner, null for a static gadget group.
    */
   public CoreUser getOwner() {
     return owner;
   }
 
   /** Returns an unmodifiable view of the gadget instances of this group.
    *
    * @return the gadgets. It never returns null.
    */
   public Set<GadgetInstance> getGadgets() {
     return Collections.unmodifiableSet(gadgets);
   }
 
   /** Adds a gadget to the group.
    *
    * The gadget's column must be between 0 and the number of columns in this
    * group. The gadget must support this column's view.
    *
    * @param instance the gadget to add to this group. It cannot be null.
    */
   public void add(final GadgetInstance instance) {
     Validate.notNull(instance, "The gadget instance cannot be null.");
     Validate.isTrue(instance.getColumn() < numberOfColumns,
         "You cannot add a gadget for column greater than the gadget group's.");
     Validate.isTrue(instance.getApplication().isViewSupported(view),
         "The gadget does not support this group's view.");
     for (GadgetInstance gadget: gadgets) {
       if (gadget.getColumn() == instance.getColumn()
           && gadget.getOrder() >= instance.getOrder()) {
         gadget.move(gadget.getColumn(), gadget.getOrder() + 1);
       }
     }
     gadgets.add(instance);
   }
 
   /** Removes the gadget instance with the provided id from the group.
    *
    * @param instanceId the id of the gadget instance to remove.
    *
    * @return true if the gadget was removed, false if it was not in the group.
    */
   public boolean remove(final long instanceId) {
     for (GadgetInstance gadget: gadgets) {
       if (gadget.getId() == instanceId) {
         gadgets.remove(gadget);
         return true;
       }
     }
     return false;
   }
 
   /** Returns the number of columns.
    *
    * @return the number of columns, 1 or greater.
    */
   public int getNumberOfColumns() {
     return numberOfColumns;
   }
 
   /** Tells if this gadget group is customizable.
    *
    * A customizable gadget group allows the user to move, add and remove
    * gadgets.
    *
    * @return true if the gadget group is customizable, false otherwise.
    */
   public boolean isCustomizable() {
     return owner != null;
   }
 
   /** Moves a gadget in the group to a new column and position in the column.
    *
    * The gadget instance id must exist in the gadget group, this group must be
    * customizable, and the column must be lower than numberOfColumns.
    *
    * @param gadgetInstanceId The id of the gadget instance to move.
    *
    * @param column The column to move the gadget to, starting from 0.
    *
    * @param order The position of the gadget in the column, starting from 0.
    */
   public void move(final long gadgetInstanceId, final int column, final int
       order) {
     Validate.isTrue(isCustomizable(), "The group is not customizable");
     Validate.isTrue(column < numberOfColumns,
         "You cannot move past the last column");
     Validate.isTrue(column >= 0, "Negative columns are not accepted.");
     Validate.isTrue(column >= 0, "Negative orders are not accepted.");
 
     log.trace("Entering move({}, ...)", gadgetInstanceId);
 
     // The list of gadgets in the target column.
     List<GadgetInstance> gadgetsInColumn = new LinkedList<GadgetInstance>();
 
     // Finds all the gagdets in the same column. Also find the gadget to move.
     // The gadget to move is not in gadgetsInColumn.
     GadgetInstance gadgetToMove = null;
     for (GadgetInstance gadget : gadgets) {
       if (gadget.getId() == gadgetInstanceId) {
         gadgetToMove = gadget;
       } else if (gadget.getColumn() == column) {
         log.debug("Adding gadget id = {} for column {}.", gadget.getId(),
             column);
         gadgetsInColumn.add(gadget);
       }
     }
 
     Validate.notNull(gadgetToMove, "The gadget to move was not found.");
 
     // Sorts them by order.
     Collections.sort(gadgetsInColumn, new Comparator<GadgetInstance>() {
       public int compare(final GadgetInstance g1, final GadgetInstance g2) {
         return g1.getOrder() - g2.getOrder();
       }
     });
 
     // Inserts the new gadget.
     if (order < gadgetsInColumn.size()) {
       log.debug("Inserting gadget id = {} in position {}.",
           gadgetToMove.getId(), order);
       gadgetsInColumn.add(order, gadgetToMove);
     } else {
       log.debug("Adding gadget id = {} at the end of the column.",
           gadgetToMove.getId());
       gadgetsInColumn.add(gadgetToMove);
     }
 
     // Renumbers the gadgets.
     int newOrder = 0;
     for (GadgetInstance gadget : gadgetsInColumn) {
       log.debug("Moving gadget with id {} to {}.", gadget.getId(), newOrder);
       gadget.move(column, newOrder);
       ++newOrder;
     }
     log.trace("Leaving move");
   }
 
   /** Creates a new gadget group from this template for the provided owner.
    *
    * This operation can only be called on gadget group templates.
    *
    * @param user the user that will own the new gadget group. It cannot be
    * null.
    *
    * @return a new gadged group, never null.
    */
   public GadgetGroup createFromTemplate(final CoreUser user) {
     Validate.notNull(user, "The user cannot be null.");
     Validate.isTrue(type == Type.TEMPLATE, "The group is not a template");
     GadgetGroup group = new GadgetGroup(user, name, view, numberOfColumns);
 
     // Adds all the gadgetInstances.
     for (GadgetInstance gadget: gadgets) {
       group.add(new GadgetInstance(gadget));
     }
 
     return group;
   }
 
   /** Finds if there is a gadget in the group for the application.
    *
    * @param application The application to look for in the gadget group. It
    * cannot be null.
    *
    * @return true if the application was found in the group, false otherwise.
    */
   public boolean contains(final Application application) {
     Validate.notNull(application, "The application cannot be null.");
     for (GadgetInstance instance: gadgets) {
       if (application.getUrl().equals(instance.getApplication().getUrl())) {
         // The application is already in the group.
         return true;
       }
     }
     return false;
   }
 }
 
