 package com.hekta.chdynmap.core.functions;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import com.laytonsmith.abstraction.MCLocation;
 import com.laytonsmith.abstraction.MCWorld;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.constructs.CArray;
 import com.laytonsmith.core.constructs.CBoolean;
 import com.laytonsmith.core.constructs.CInt;
 import com.laytonsmith.core.constructs.CDouble;
 import com.laytonsmith.core.constructs.CNull;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.CString;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.constructs.CVoid;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.AbstractFunction;
 import com.laytonsmith.core.functions.Exceptions.ExceptionType;
 import com.laytonsmith.core.ObjectGenerator;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.PureUtilities.Common.StringUtils;
 import com.laytonsmith.PureUtilities.Version;
 
 import com.hekta.chdynmap.abstraction.MCDynmapAreaMarker;
 import com.hekta.chdynmap.abstraction.MCDynmapCircleMarker;
 import com.hekta.chdynmap.abstraction.MCDynmapIcon;
 import com.hekta.chdynmap.abstraction.MCDynmapIconMarker;
 import com.hekta.chdynmap.abstraction.MCDynmapMarker;
 import com.hekta.chdynmap.abstraction.MCDynmapMarkerFillStyle;
 import com.hekta.chdynmap.abstraction.MCDynmapMarkerLineStyle;
 import com.hekta.chdynmap.abstraction.MCDynmapMarkerSet;
 import com.hekta.chdynmap.abstraction.MCDynmapPolyLineMarker;
 import com.hekta.chdynmap.abstraction.enums.MCDynmapMarkerType;
 import com.hekta.chdynmap.core.CHDynmapStatic;
 
 /**
  *
  * @author Hekta
  */
 public class DynmapMarkers {
 
 	public static String docs() {
 		return "A class of functions to manage the Dynmap markers.";
 	}
 
 	public static abstract class DynmapMarkerFunction extends AbstractFunction {
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 	}
 
 	public static abstract class DynmapMarkerGetterFunction extends DynmapMarkerFunction {
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException};
 		}
 	}
 
 	public static abstract class DynmapMarkerSetterFunction extends DynmapMarkerFunction {
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException, ExceptionType.CastException};
 		}
 	}
 
 	@api
 	public static class dm_all_markers extends DynmapMarkerFunction {
 
 		public String getName() {
 			return "dm_all_markers";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1, 2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public String docs() {
 			return "array {setID, [type]} Returns an associative array containing the ID of all markers in the markerset."
 					+ " If the type is given, only the markers of this type are returne."
 					+ " Type can be one of " + StringUtils.Join(MCDynmapMarkerType.values(), ", ", ", or ", " or ") + ".";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarkerSet set = CHDynmapStatic.getMarkerSet(args[0].val(), t);
 			MCDynmapMarkerType type;
 			if ((args.length == 1) || (args[1] instanceof CNull)) {
 				type = null;
 			} else {
 				try {
 					type = MCDynmapMarkerType.valueOf(args[1].val().toUpperCase());
 				} catch (IllegalArgumentException exception) {
 					throw new ConfigRuntimeException("Invalid marker type: " + args[1].val() + ".", ExceptionType.PluginInternalException, t);
 				}
 			}
 			CArray markerArray = new CArray(t);
 			if ((type == null) || (type == MCDynmapMarkerType.AREA)) {
 				for (MCDynmapAreaMarker areaMarker : set.getAreaMarkers()) {
 					markerArray.push(new CString(areaMarker.getId(), t));
 				}
 			}
 			if ((type == null) || (type == MCDynmapMarkerType.CIRCLE)) {
 				for (MCDynmapCircleMarker circleMarker : set.getCircleMarkers()) {
 					markerArray.push(new CString(circleMarker.getId(), t));
 				}	
 			}
 			if ((type == null) || (type == MCDynmapMarkerType.ICON)) {
 				for (MCDynmapIconMarker iconMarker : set.getIconMarkers()) {
 					markerArray.push(new CString(iconMarker.getId(), t));
 				}	
 			}
 			if ((type == null) || (type == MCDynmapMarkerType.POLYLINE)) {
 				for (MCDynmapPolyLineMarker polyLineMarker : set.getPolyLineMarkers()) {
 					markerArray.push(new CString(polyLineMarker.getId(), t));
 				}	
 			}
 			return markerArray;
 		}
 	}
 
 	@api
 	public static class dm_create_marker extends DynmapMarkerFunction {
 
 		public String getName() {
 			return "dm_create_marker";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1, 2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public String docs() {
 			return "string {setID, [optionArray]} Creates a marker and returns its ID."
 					+ " ---- The option array is associative and not required, and all its keys are optional."
 					+ " <li>KEY - DEFAULT - DESCRIPTION - COMMENT</li>"
 					+ " <li>center - world spawn - the center of the marker - only for circle markers, world is ignored</li>"
 					+ " <li>corners - world spawn - the corners of the marker - only for area or polyline markers, world is ignored (and also y for area markers)</li>"
 					+ " <li>icon - null - the icon ID of the marker, null for the markerset default icon - only for icon markers</li>"
 					+ " <li>id - random - ID of the marker, must be unique within the set, if null or not given, an unique ID is generated</li>"
 					+ " <li>label - markerID - the label of the marker</li>"
 					+ " <li>label_is_html - false - sets if the label is processing as HTML</li>"
 					+ " <li>location - world spawn - the location of the marker - only for icon markers, world is ignored</li>"
 					+ " <li>persistent - false - sets if the label is persistent (saved and reloaded on restart), the markerset must be persistent - can not be changed later</li>"
 					+ " <li>radius - 0 0 - the radius of the marker - only for circle markers</li>"
 					+ " <li>type - ICON - the type of the marker, can be one of " + StringUtils.Join(MCDynmapMarkerType.values(), ", ", ", or ", " or ") + " - can not be changed later</li>"
 					+ " <li>world - first world - the world of the marker</li>";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarkerSet set = CHDynmapStatic.getMarkerSet(args[0].val(), t);
 			//create the option array
 			CArray optionArray;
 			if (args.length == 1) {
 				optionArray = new CArray(t);
 			} else {
 				optionArray = Static.getArray(args[1], t);
 			}
 			Set keys = optionArray.keySet();
 			//set optional values
 			//type
 			MCDynmapMarkerType type;
 			if ((!keys.contains("type")) || (optionArray.get("type", t) instanceof CNull)) {
 				type = MCDynmapMarkerType.ICON;
 			} else {
 				try {
 					type = MCDynmapMarkerType.valueOf(optionArray.get("type", t).val().toUpperCase());
 				} catch (IllegalArgumentException exception) {
 					throw new ConfigRuntimeException("Invalid marker type: " + optionArray.get("type", t).val(), ExceptionType.PluginInternalException, t);
 				}
 			}
 			//id
 			String id;
 			if (keys.contains("id")) {
 				id = optionArray.get("id").val();
 				if (set.getMarker(id) != null) {
 					throw new ConfigRuntimeException("\"" + id + "\" is already an existing marker.", ExceptionType.PluginInternalException, t);
 				}
 			} else {
 				id = null;
 			}
 			//world
 			MCWorld world;
 			if (keys.contains("world")) {
 				world = Static.getServer().getWorld(optionArray.get("world").val());
 				if (world == null) {
 					throw new ConfigRuntimeException("Unknown world: " + optionArray.get("world").val(), ExceptionType.InvalidWorldException, t);
 				}
 			} else {
 				world = Static.getServer().getWorlds().get(0);
 			}
 			MCLocation spawnLocation = world.getSpawnLocation();
 			//label
 			String label;
 			if (keys.contains("label")) {
 				label = optionArray.get("label").val();
 			} else {
 				label = id;
 			}
 			//label_is_html
 			boolean labelIsHTML;
 			if (keys.contains("label_is_html")) {
 				labelIsHTML = Static.getBoolean(optionArray.get("label_is_html"));
 			} else {
 				labelIsHTML = false;
 			}
 			//persistent
 			boolean isPersistent;
 			if (keys.contains("persistent")) {
 				isPersistent = Static.getBoolean(optionArray.get("persistent"));
 			} else {
 				isPersistent = false;
 			}
 			//center
 			MCLocation center;
 			if (type == MCDynmapMarkerType.CIRCLE) {
 				if (keys.contains("center")) {
 					center = ObjectGenerator.GetGenerator().location(optionArray.get("center"), world, t);
 				} else {
 					center = spawnLocation;
 				}
 			} else {
 				center = null;
 			}
 			//corners
 			List<MCLocation> corners;
 			if (type == MCDynmapMarkerType.AREA) {
 				corners = new ArrayList<MCLocation>();
 				if (keys.contains("corners")) {
 					CArray givenCorners = Static.getArray(optionArray.get("corners"), t);
 					if (givenCorners.inAssociativeMode()) {
 						throw new ConfigRuntimeException("The corners array must not be associative.", ExceptionType.CastException, t);
 					}
 					for (Construct corner : givenCorners.asList()) {
 						corners.add(ObjectGenerator.GetGenerator().location(corner, world, t));
 					}
 				} else {
 					corners.add(spawnLocation);
 				}
 			} else if (type == MCDynmapMarkerType.POLYLINE) {
 				corners = new ArrayList<MCLocation>();
 				if (keys.contains("corners")) {
 					CArray givenCorners = Static.getArray(optionArray.get("corners"), t);
 					if (givenCorners.inAssociativeMode()) {
 						throw new ConfigRuntimeException("The corners array must not be associative.", ExceptionType.CastException, t);
 					}
 					for (Construct corner : givenCorners.asList()) {
 						corners.add(ObjectGenerator.GetGenerator().location(corner, world, t));
 					}
 				} else {
 					corners.add(spawnLocation);
 				}
 			} else {
 				corners = null;
 			}
 			//icon
 			MCDynmapIcon icon;
 			if (type == MCDynmapMarkerType.ICON) {
 				if (keys.contains("icon")) {
 					icon = CHDynmapStatic.getIcon(optionArray.get("icon").val(), t);
 				} else {
 					icon = null;
 				}
 			} else {
 				icon = null;
 			}
 			//location
 			MCLocation iconLocation;
 			if (type == MCDynmapMarkerType.ICON) {
 				if (keys.contains("location")) {
 					iconLocation = ObjectGenerator.GetGenerator().location(optionArray.get("location"), world, t);
 				} else {
 					iconLocation = spawnLocation;
 				}
 			} else {
 				iconLocation = null;
 			}
 			//radius
 			double radiusX;
 			double radiusZ;
 			if (type == MCDynmapMarkerType.CIRCLE) {
 				if (keys.contains("radius")) {
 					CArray radius = Static.getArray(optionArray.get("radius"), t);
 					radiusX = Static.getDouble(radius.get("x", t), t);
 					radiusZ = Static.getDouble(radius.get("z", t), t);
 				} else {
 					radiusX = 0;
 					radiusZ = 0;
 				}
 			} else {
 				radiusX = 0;
 				radiusZ = 0;
 			}
 			//create the marker
 			MCDynmapMarker marker;
 			switch (type) {
 				case AREA:
 					marker = set.createAreaMarker(id, label, labelIsHTML, world, corners, isPersistent);
 					break;
 				case CIRCLE:
 					marker = set.createCircleMarker(id, label, labelIsHTML, center, radiusX, radiusZ, isPersistent);
 					break;
 				case ICON:
 					marker = set.createIconMarker(id, label, labelIsHTML, iconLocation, icon, isPersistent);
 					break;
 				case POLYLINE:
 					marker = set.createPolyLineMarker(id, label, labelIsHTML, world, corners, isPersistent);
 					break;
 				default:
 					marker = null;
 					break;
 			}
 			if (marker == null) {
 				throw new ConfigRuntimeException("The marker creation failed.", ExceptionType.PluginInternalException, t);
 			}
 			return new CString(marker.getId(), t);
 		}
 	}
 
 	@api
 	public static class dm_delete_marker extends DynmapMarkerFunction {
 
 		public String getName() {
 			return "dm_delete_marker";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID} Deletes a marker in the set.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).delete();
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_boosted extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_boosted";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns if the marker resolution is boosted. Only for area and circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			switch (marker.getType()) {
 				case AREA:
 					return new CBoolean(((MCDynmapAreaMarker) marker).isBoosted(), t);
 				case CIRCLE:
 					return new CBoolean(((MCDynmapCircleMarker) marker).isBoosted(), t);
 				default:
 					throw new ConfigRuntimeException("There is no existing area or circle markers with this id.", ExceptionType.NotFoundException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_boosted extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_boosted";
 		}
 
 		public String docs() {
 			return "void {setID, markerID, boolean} Sets if the marker resolution is boosted. Only for area and circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			switch (marker.getType()) {
 				case AREA:
 					((MCDynmapAreaMarker) marker).setBoosted(Static.getBoolean(args[2]));
 					break;
 				case CIRCLE:
 					((MCDynmapCircleMarker) marker).setBoosted(Static.getBoolean(args[2]));
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area or circle markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_center extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_center";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the location of the marker center. Only for circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return ObjectGenerator.GetGenerator().location(CHDynmapStatic.getCircleMarker(args[0].val(), args[1].val(), t).getCenter(), false);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_center extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_center";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, locationArray} Sets the center of a marker. Only for circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapCircleMarker marker = CHDynmapStatic.getCircleMarker(args[0].val(), args[1].val(), t);
 			marker.setCenter(ObjectGenerator.GetGenerator().location(args[2], marker.getWorld(), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_corners extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_corners";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the corners location of the marker. Only for area and polyline markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			List<MCLocation> corners;
 			switch (marker.getType()) {
 				case AREA:
 					corners = ((MCDynmapAreaMarker) marker).getCorners();
 					break;
 				case POLYLINE:
 					corners = ((MCDynmapPolyLineMarker) marker).getCorners();
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area or polyline markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			CArray cornerArray = new CArray(t);
 			for (MCLocation location : corners) {
				cornerArray.push(ObjectGenerator.GetGenerator().location(location, false));
 			}
 			return cornerArray;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_corners extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_corners";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the location of the marker corners (array of location arrays, world is ignored, and for area markers y is ignored). Only for area and polyline markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			CArray givenCorners = Static.getArray(args[2], t);
 			if (givenCorners.inAssociativeMode()) {
 				throw new ConfigRuntimeException("The array must not be associative.", ExceptionType.CastException, t);
 			}
 			List<MCLocation> corners = new ArrayList<MCLocation>();
 			MCWorld world = marker.getWorld();
 			for (Construct corner : givenCorners.asList()) {
 				corners.add(ObjectGenerator.GetGenerator().location(corner, world, t));
 			}
 			switch (marker.getType()) {
 				case AREA:
 					((MCDynmapAreaMarker) marker).setCorners(corners);
 					break;
 				case POLYLINE:
 					((MCDynmapPolyLineMarker) marker).setCorners(corners);
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area or polyline markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_description extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_label";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the description of the marker.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).getDescription(), t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_description extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_description";
 		}
 
 		public String docs() {
 			return "void {setID, markerID, htmlDescription} Sets the description of the marker (in HTML).";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).setDescription(args[2].val());
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_fill_style extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_fill_style";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the fill style array of the marker. Only for area and circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			MCDynmapMarkerFillStyle fillStyle;
 			switch (marker.getType()) {
 				case AREA:
 					fillStyle = ((MCDynmapAreaMarker) marker).getFillStyle();
 					break;
 				case CIRCLE:
 					fillStyle = ((MCDynmapCircleMarker) marker).getFillStyle();
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area or circle markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			CArray styleArray = new CArray(t);
 			styleArray.set("color", ObjectGenerator.GetGenerator().color(fillStyle.getColor(), t), t);
 			styleArray.set("opacity", new CDouble(fillStyle.getOpacity(), t), t);
 			return styleArray;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_fill_style extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_fill_style";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the marker fill style (array with \"color\" and \"opacity\" optional keys, color is a color r g b array, and opacity a number between 0 and 1 inclusive)."
 					+ " Only for area and circle markers";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			CArray styleArray = Static.getArray(args[2], t);
 			Set keys = styleArray.keySet();
 			MCDynmapMarkerFillStyle fillStyle;
 			switch (marker.getType()) {
 				case AREA:
 					fillStyle = ((MCDynmapAreaMarker) marker).getFillStyle();
 					break;
 				case CIRCLE:
 					fillStyle = ((MCDynmapCircleMarker) marker).getFillStyle();
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area or circle markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			if (keys.contains("color")) {
 				fillStyle.setColor(ObjectGenerator.GetGenerator().color(Static.getArray(styleArray.get("color"), t), t));
 			}
 			if (keys.contains("opacity")) {
 				double opacity = Static.getDouble(styleArray.get("opacity"), t);
 				if ((opacity < 0) || (opacity > 1)) {
 					throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.RangeException, t);
 				} else {
 					fillStyle.setOpacity(opacity);
 				}
 			}
 			switch (marker.getType()) {
 				case AREA:
 					((MCDynmapAreaMarker) marker).setFillStyle(fillStyle);
 					break;
 				case CIRCLE:
 					((MCDynmapCircleMarker) marker).setFillStyle(fillStyle);
 					break;
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_icon extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_icon";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the icon ID of the marker. Only for icon markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getIconMarker(args[0].val(), args[1].val(), t).getIcon().getId(), t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_icon extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_icon";
 		}
 
 		public String docs() {
 			return "void {setID, markerID, iconID} Sets the icon of a marker. Only for icon markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapIconMarker marker = CHDynmapStatic.getIconMarker(args[0].val(), args[1].val(), t);
 			MCDynmapIcon icon = CHDynmapStatic.getIcon(args[2].val(), t);
 			if (marker.getSet().iconIsAllowed(icon)) {
 				marker.setIcon(icon);
 			} else {
 				throw new ConfigRuntimeException("The icon is not allowed for the markerset.", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_label extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_label";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the label of the marker.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).getLabel(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_label_is_html extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_label_is_html";
 		}
 
 		public String docs() {
 			return "boolean {setID, markerID} Returns if the label of the marker is processed as HTML.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CBoolean(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).isLabelMarkup(), t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_label extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_label";
 		}
 
 		public String docs() {
 			return "void {setID, markerID, label, [isHTML]} Sets the label of the marker, isHTML is a boolean, if true, label will be processed as HTML.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			boolean isHTML;
 			if (args.length == 3) {
 				isHTML = false;
 			} else {
 				isHTML = Static.getBoolean(args[3]);
 			}
 			marker.setLabel(args[2].val(), isHTML);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_line_style extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_line_style";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the line style array of the marker. Only for area, circle and polyline markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			MCDynmapMarkerLineStyle lineStyle;
 			switch (marker.getType()) {
 				case AREA:
 					lineStyle = ((MCDynmapAreaMarker) marker).getLineStyle();
 					break;
 				case CIRCLE:
 					lineStyle = ((MCDynmapCircleMarker) marker).getLineStyle();
 					break;
 				case POLYLINE:
 					lineStyle = ((MCDynmapPolyLineMarker) marker).getLineStyle();
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area, circle or polyline markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			CArray styleArray = new CArray(t);
 			styleArray.set("color", ObjectGenerator.GetGenerator().color(lineStyle.getColor(), t), t);
 			styleArray.set("opacity", new CDouble(lineStyle.getOpacity(), t), t);
 			styleArray.set("weight", new CInt(lineStyle.getWeight(), t), t);
 			return styleArray;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_line_style extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_line_style";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.NotFoundException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the marker line style (array with \"color\", \"opacity\" and \"weight\" optional keys, color is a color r g b array, opacity a number between 0 and 1 inclusive and weight is an integer)."
 					+ " Only for area, circle and polyline markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			CArray styleArray = Static.getArray(args[2], t);
 			Set keys = styleArray.keySet();
 			MCDynmapMarkerLineStyle lineStyle;
 			switch (marker.getType()) {
 				case AREA:
 					lineStyle = ((MCDynmapAreaMarker) marker).getLineStyle();
 					break;
 				case CIRCLE:
 					lineStyle = ((MCDynmapCircleMarker) marker).getLineStyle();
 					break;
 				case POLYLINE:
 					lineStyle = ((MCDynmapPolyLineMarker) marker).getLineStyle();
 					break;
 				default:
 					throw new ConfigRuntimeException("There is no existing area, circle or polyline markers with this id.", ExceptionType.NotFoundException, t);
 			}
 			if (keys.contains("color")) {
 				lineStyle.setColor(ObjectGenerator.GetGenerator().color(Static.getArray(styleArray.get("color"), t), t));
 			}
 			if (keys.contains("opacity")) {
 				double opacity = Static.getDouble(styleArray.get("opacity"), t);
 				if ((opacity < 0) || (opacity > 1)) {
 					throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.RangeException, t);
 				} else {
 					lineStyle.setOpacity(opacity);
 				}
 			}
 			if (keys.contains("weight")) {
 				lineStyle.setWeight(Static.getInt32(styleArray.get("weight"), t));
 			}
 			switch (marker.getType()) {
 				case AREA:
 					((MCDynmapAreaMarker) marker).setLineStyle(lineStyle);
 					break;
 				case CIRCLE:
 					((MCDynmapCircleMarker) marker).setLineStyle(lineStyle);
 					break;
 				case POLYLINE:
 					((MCDynmapPolyLineMarker) marker).setLineStyle(lineStyle);
 					break;
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_loc extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_loc";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the location of the marker. Only for icon markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return ObjectGenerator.GetGenerator().location(CHDynmapStatic.getIconMarker(args[0].val(), args[1].val(), t).getLocation(), false);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_loc extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_loc";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, locationArray} Sets the icon location of a marker. Only for icon markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapIconMarker marker = CHDynmapStatic.getIconMarker(args[0].val(), args[1].val(), t);
 			marker.setLocation(ObjectGenerator.GetGenerator().location(args[2], marker.getWorld(), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_markerset extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_markerset";
 		}
 
 		public String docs() {
 			return "void {setID, markerID, newSetID} Changes the markerset of the marker.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapMarker marker = CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t);
 			MCDynmapMarkerSet newSet = CHDynmapStatic.getMarkerSet(args[2].val(), t);
 			if (newSet.getMarker(marker.getId()) != null) {
 				throw new ConfigRuntimeException("An other marker with the same ID already exists in the new markerset.", ExceptionType.PluginInternalException, t);
 			}
 			marker.setSet(newSet);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_normalized_world extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_normalized_world";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the normalized world of the marker (used for directory and URL names in Dynmap).";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).getNormalizedWorld(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_persistent extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_persistent";
 		}
 
 		public String docs() {
 			return "boolean {setID, markerID} Returns if the marker is persistent.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CBoolean(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).isPersistent(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_radius extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_radius";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the radius of the marker. Only for circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapCircleMarker marker = CHDynmapStatic.getCircleMarker(args[0].val(), args[1].val(), t);
 			CArray radius = new CArray(t);
 			radius.set("x", new CDouble(marker.getRadiusX(), t), t);
 			radius.set("z", new CDouble(marker.getRadiusZ(), t), t);
 			return radius;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_radius extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_radius";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the radius of the marker (array with \"x\" and \"z\" keys). Only for circle markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CArray radius = Static.getArray(args[2], t);
 			CHDynmapStatic.getCircleMarker(args[0].val(), args[1].val(), t).setRadius(Static.getDouble(radius.get("x", t), t), Static.getDouble(radius.get("z", t), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_range_height extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_range_height";
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the range height of the marker. Only for area markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCDynmapAreaMarker marker = CHDynmapStatic.getAreaMarker(args[0].val(), args[1].val(), t);
 			CArray range = new CArray(t);
 			range.set("bottom", new CDouble(marker.getBottomY(), t), t);
 			range.set("top", new CDouble(marker.getTopY(), t), t);
 			return range;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_range_height extends DynmapMarkerSetterFunction {
 
 		public String getName() {
 			return "dm_set_marker_range_height";
 		}
 
 		@Override
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the range height of a marker (array with \"top\" and \"bottom\" keys). Only for area markers.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CArray range = Static.getArray(args[2], t);
 			CHDynmapStatic.getAreaMarker(args[0].val(), args[1].val(), t).setRangeY(Static.getDouble(range.get("top", t), t), Static.getDouble(range.get("bottom", t), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_type extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_type";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the type of the marker. Can be one of " + StringUtils.Join(MCDynmapMarkerType.values(), ", ", ", ", " or ") + ", or UNKNOWN.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).getType().name(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_world extends DynmapMarkerGetterFunction {
 
 		public String getName() {
 			return "dm_marker_world";
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the world of the marker.";
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CString(CHDynmapStatic.getMarker(args[0].val(), args[1].val(), t).getWorld().getName(), t);
 		}
 	}
 }
