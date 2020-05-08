 /*
  * ========================================================================
  *
  * Copyright (c) 2005 Unpublished Work of Novell, Inc. All Rights Reserved.
  * 
  * THIS WORK IS AN UNPUBLISHED WORK AND CONTAINS CONFIDENTIAL,
  * PROPRIETARY AND TRADE SECRET INFORMATION OF NOVELL, INC. ACCESS TO
  * THIS WORK IS RESTRICTED TO (I) NOVELL, INC. EMPLOYEES WHO HAVE A NEED
  * TO KNOW HOW TO PERFORM TASKS WITHIN THE SCOPE OF THEIR ASSIGNMENTS AND
  * (II) ENTITIES OTHER THAN NOVELL, INC. WHO HAVE ENTERED INTO
  * APPROPRIATE LICENSE AGREEMENTS. NO PART OF THIS WORK MAY BE USED,
  * PRACTICED, PERFORMED, COPIED, DISTRIBUTED, REVISED, MODIFIED,
  * TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, COMPILED,
  * LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  * CONSENT OF NOVELL, INC. ANY USE OR EXPLOITATION OF THIS WORK WITHOUT
  * AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  * LIABILITY.
  *
  * ========================================================================
  */
 package org.spiffyui.mvsb.samples.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.spiffyui.client.rest.RESTObjectCallBack;
 import org.spiffyui.client.widgets.multivaluesuggest.MultivalueSuggestBoxBase;
 
 public class LocalSuggestBox extends MultivalueSuggestBoxBase
 {
     private static final List<String> BASIC_COLORS;
     
     static {
         BASIC_COLORS = new ArrayList<String>();
         BASIC_COLORS.add("Red");
         BASIC_COLORS.add("Orange");
         BASIC_COLORS.add("Yellow");
         BASIC_COLORS.add("Green");
         BASIC_COLORS.add("Blue");
         BASIC_COLORS.add("Purple");
         BASIC_COLORS.add("White");
         BASIC_COLORS.add("Brown");
         BASIC_COLORS.add("Black");
     }
     /**
      * Constructor
      * @param isMultivalued - whether or not to allow multiple values
      */
     public LocalSuggestBox(boolean isMultivalued)
     {
         super(null, isMultivalued);
     }
 
     @Override
     protected void queryOptions(String query, int from, int to, RESTObjectCallBack<OptionResultSet> callback)
     {
         OptionResultSet options = new OptionResultSet(BASIC_COLORS.size()); // this size isn't correct
         int totalSize = 0;
         for (String color : BASIC_COLORS) {
             if (color.toLowerCase().indexOf(query.toLowerCase()) >= 0 || query.equals("*")) {
                 Option option = createOption(color);
                 options.addOption(option);
                 totalSize++;
             }
         }
         options.setTotalSize(totalSize);
         callback.success(options);
     }
     
     private Option createOption(String color)
     {
         Option option = new Option();
         option.setName(color);
         option.setValue(color);
         return option;
     }
     
 
 }
