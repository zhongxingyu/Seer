 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.gmap;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIPanelMeta;
 
 import org.icefaces.ace.resources.ACEResourceNames;
 import org.icefaces.resources.ICEResourceDependencies;
 import org.icefaces.resources.ICEResourceDependency;
 import org.icefaces.resources.ICEResourceLibrary;
 
 @Component(
         tagName = "gMapAutocomplete",
         componentClass = "org.icefaces.ace.component.gmap.GMapAutocomplete",
         rendererClass = "org.icefaces.ace.component.gmap.GMapAutocompleteRenderer",
         generatedClass = "org.icefaces.ace.component.gmap.GMapAutocompleteBase",
         extendsClass = "javax.faces.component.UIPanel",
         componentType = "org.icefaces.ace.component.GMapAutocomplete",
         rendererType = "org.icefaces.ace.component.GMapAutocompleteRenderer",
         componentFamily = "org.icefaces.ace.component",
         tlddoc = "An Icesoft implementation of Google's Places autocomplete tool. " +
                 "The ace:gMapAutocomplete component will create a text box which will provide locations that match the currently typed string, " +
                 "then return information about the selected location such as the types Google has assigned to it, or the url to Google's " +
                 "information page on it." +
                 " For more information, see the <a href=\"http://wiki.icefaces.org/display/ICE/GMap\">gMap</a> Wiki Documentation."
 )
 @ICEResourceLibrary(ACEResourceNames.ACE_LIBRARY)
 @ICEResourceDependencies({
         @ICEResourceDependency(name = ACEResourceNames.JQUERY_UI_CSS),
         @ICEResourceDependency(name = ACEResourceNames.JQUERY_JS),
         @ICEResourceDependency(name = ACEResourceNames.COMPONENTS_JS)
 })
 
 public class GMapAutocompleteMeta extends UIPanelMeta {
     @Property(tlddoc = "Desired size of the input box.", defaultValue = "30")
     private String size;
     @Property(tlddoc = "Styling options to be sent to the autocomplete box.")
     private String style;
     @Property(tlddoc = "Additional options to be sent to the info window displayed. " +
             "Check google maps API for more specifics at https://developers.google.com/maps/documentation/javascript/reference#AutocompleteOptions." +
             " Form is attribute:'value'.", defaultValue = "none")
     private String windowOptions;
    @Property(tlddoc = "The backing bean property to store the text value of the address selected by gMapAutocomplete.")
     private String address;
    @Property(tlddoc = "The backing bean property to store the lat/lng coordinates of the address selected by gMapAutocomplete. Format: \"(0.000000,0.000000)\".")
     private String latLng;
    @Property(tlddoc = "The backing bean property to store the array of types that Google determines match the address selected by gMapAutocomplete.")
     private String types;
    @Property(tlddoc = "The backing bean property to store the url attributed to the address selected by gMapAutocomplete.", defaultValue="https://maps.google.com/maps/place")
     private String url;
     @Property(tlddoc = "Value to shift the map after finding new location, in pixels. (useful for mobile devices) Form is x,y.", defaultValue = "0,0")
     private String offset;
     @Property(tlddoc = "Determine whether or not to display an Info Window with information on the selected point.", defaultValue="true")
     private boolean window;
 
 }
