 package net.rptools.maptool.client.functions;
 
 import java.awt.Image;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolVariableResolver;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenFootprint;
 import net.rptools.maptool.model.TokenProperty;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.StringUtil;
 import net.rptools.maptool.util.TokenUtil;
 import net.rptools.parser.Parser;
 import net.rptools.parser.ParserException;
 import net.rptools.parser.function.AbstractFunction;
 import net.sf.json.JSONArray;
 
 public class TokenPropertyFunctions extends AbstractFunction {
 
 	private static final TokenPropertyFunctions instance =
 		new TokenPropertyFunctions();
 
 	private TokenPropertyFunctions() {
 		super(0, 4, "getPropertyNames", "getAllPropertyNames", "getPropertyNamesRaw",
 				"hasProperty",
 				"isNPC", "isPC", "setPC", "setNPC", "getLayer", "setLayer",
 				"getSize", "setSize", "getOwners", "isOwnedByAll", "isOwner",
 				"resetProperty", "getProperty", "setProperty", "isPropertyEmpty",
 				"getPropertyDefault", "sendToBack", "bringToFront",
 				"getLibProperty", "setLibProperty", "getLibPropertyNames",
 				"setPropertyType", "getPropertyType",
 				"getRawProperty", "getTokenFacing", "setTokenFacing", "removeTokenFacing",
 				"getMatchingProperties", "getMatchingLibProperties", "isSnapToGrid",
 		"setOwner");
 	}
 
 
 	public static TokenPropertyFunctions getInstance() {
 		return instance;
 	}
 
 	@Override
 	public Object childEvaluate(Parser parser, String functionName,
 			List<Object> parameters) throws ParserException {
 
 		MapToolVariableResolver resolver =
 			(MapToolVariableResolver) parser.getVariableResolver();
 
 		// Cached for all those putToken() calls that are needed
 		ZoneRenderer zoneR = MapTool.getFrame().getCurrentZoneRenderer();
 		Zone zone = zoneR.getZone();
 
 		/*
 		 * String type = getPropertyType()
 		 */
 		if (functionName.equals("getPropertyType")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getPropertyType", parameters, 0);
 			return token.getPropertyType();
 		}
 
 		/*
 		 * String empty = setPropertyType(String propTypeName, Token id: self)
 		 */
 		if (functionName.equals("setPropertyType")) {
 			if (parameters.size() < 1)
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 
 			Token token = getTokenFromParam(resolver, "setPropertyType", parameters, 1);
 			token.setPropertyType(parameters.get(0).toString());
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);		// FJE Should this be here?  Added because other places have it...?!
 			return "";
 		}
 
 		/*
 		 * String names = getPropertyNames(String delim: ",", String tokenId: currentToken())
 		 */
 		if (functionName.equals("getPropertyNames") || functionName.equals("getPropertyNamesRaw")) {
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, functionName, parameters, 1);
 			String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
 			String pattern = ".*";
 			return getPropertyNames(token, delim, pattern, functionName.equals("getPropertyNamesRaw"));
 		}
 
 		/*
 		 * String names = getMatchingProperties(String pattern, String delim: ",", String tokenId: currentToken())
 		 */
 		if (functionName.equals("getMatchingProperties")) {
 			Token token = getTokenFromParam(resolver, "getMatchingProperties", parameters, 2);
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			}
 			else if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			String pattern = parameters.get(0).toString();
 			String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
 			return getPropertyNames(token, delim, pattern, false);
 		}
 
 		/*
 		 * String names = getAllPropertyNames(String propType: "", String delim: ",")
 		 */
 		if (functionName.equals("getAllPropertyNames")) {
 			if (parameters.size() < 1) {
 				return getAllPropertyNames(null, ",");
 			} else if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			} else {
 				return getAllPropertyNames(parameters.get(0).toString(), parameters.size() > 1 ? parameters.get(1).toString() : ",");
 			}
 		}
 
 		/*
 		 * Number zeroOne = hasProperty(String propName, String tokenId: currentToken())
 		 */
 		if (functionName.equals("hasProperty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "hasProperty", parameters, 1);
 			return hasProperty(token, parameters.get(0).toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/*
 		 * Number zeroOne = isNPC(String tokenId: currentToken())
 		 */
 		if (functionName.equals("isNPC")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "isNPC", parameters, 0);
 			return token.getType() == Token.Type.NPC ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/*
 		 * Number zeroOne = isPC(String tokenId: currentToken())
 		 */
 		if (functionName.equals("isPC")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "isPC", parameters, 0);
 			return token.getType() == Token.Type.PC ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/*
 		 * String empty = setPC(String tokenId: currentToken())
 		 */
 		if (functionName.equals("setPC")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "setPC", parameters, 0);
 			token.setType(Token.Type.PC);
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);
 			zoneR.flushLight();
 			MapTool.getFrame().updateTokenTree();
 
 			return "";
 		}
 
 		/*
 		 * String empty = setNPC(String tokenId: currentToken())
 		 */
 		if (functionName.equals("setNPC")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "setNPC", parameters, 0);
 			token.setType(Token.Type.NPC);
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);
 			zoneR.flushLight();
 			MapTool.getFrame().updateTokenTree();
 
 			return "";
 		}
 
 		/*
 		 * String layer = getLayer(String tokenId: currentToken())
 		 */
 		if (functionName.equals("getLayer")) {
			if (parameters.size() != 1) {
				throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getLayer", parameters, 0);
 			return token.getLayer().name();
 		}
 
 		/*
 		 * String layer = setLayer(String layer, String tokenId: currentToken())
 		 */
 		if (functionName.equals("setLayer")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "setLayer", parameters, 1);
 			String layer = setLayer(token, parameters.get(0).toString());
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);
 			zoneR.flushLight();
 			MapTool.getFrame().updateTokenTree();
 
 			return layer;
 		}
 
 		/*
 		 * String size = getSize(String tokenId: currentToken())
 		 */
 		if (functionName.equalsIgnoreCase("getSize")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getSize", parameters, 0);
 			return getSize(token);
 		}
 
 		/*
 		 * String size = setSize(String size, String tokenId: currentToken())
 		 */
 		if (functionName.equalsIgnoreCase("setSize")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "setSize", parameters, 1);
 			return setSize(token, parameters.get(0).toString());
 		}
 
 		/*
 		 * String owners = getOwners(String delim: ",", String tokenId: currentToken())
 		 */
 		if (functionName.equalsIgnoreCase("getOwners")) {
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getOwners", parameters, 1);
 			return getOwners(token, parameters.size() > 0 ? parameters.get(0).toString() : ",");
 		}
 
 		/*
 		 * Number zeroOne = isOwnedByAll(String tokenId: currentToken())
 		 */
 		if (functionName.equals("isOwnedByAll")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "isOwnedByAll", parameters, 0);
 			return token.isOwnedByAll() ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/*
 		 * Number zeroOne = isOwner(String player: self, String tokenId: currentToken())
 		 */
 		if (functionName.equals("isOwner")) {
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "isOwner", parameters, 1);
 			if (parameters.size() > 0) {
 				return token.isOwner(parameters.get(0).toString());
 			}
 			return token.isOwner(MapTool.getPlayer().getName()) ? BigDecimal.ONE : BigDecimal.ZERO	;
 		}
 
 		/*
 		 * String empty = resetProperty(String propName, String tokenId: currentToken())
 		 */
 		if (functionName.equals("resetProperty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "resetProperty", parameters, 1);
 			token.resetProperty(parameters.get(0).toString());
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);
 			return "";
 		}
 
 		/*
 		 * String empty = setProperty(String propName, String value, String tokenId: currentToken())
 		 */
 		if (functionName.equals("setProperty")) {
 			if (parameters.size() < 2) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
 			} else
 				if (parameters.size() > 3) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 3, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "setProperty", parameters, 2);
 			token.setProperty(parameters.get(0).toString(), parameters.get(1).toString());
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zone.putToken(token);
 			return "";
 		}
 
 		/*
 		 * {String|Number} value = getRawProperty(String propName, String tokenId: currentToken())
 		 */
 		if (functionName.equals("getRawProperty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "getRawProperty", parameters, 1);
 			Object val = token.getProperty(parameters.get(0).toString());
 			if (val == null) {
 				return "";
 			}
 
 			if (val instanceof String) {
 				// try to convert to a number
 				try {
 					return new BigDecimal(val.toString());	// XXX Localization here?
 				} catch (Exception e) {
 					return val;
 				}
 			} else {
 				return val;
 			}
 		}
 
 		/*
 		 * {String|Number} value = getProperty(String propName, String tokenId: currentToken())
 		 */
 		if (functionName.equals("getProperty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "getProperty", parameters, 1);
 			Object val =  token.getEvaluatedProperty(parameters.get(0).toString());
 
 			if (val instanceof String) {
 				// try to convert to a number
 				try {
 					return new BigDecimal(val.toString());
 				} catch (Exception e) {
 					return val;
 				}
 			} else {
 				return val;
 			}
 
 		}
 
 		/*
 		 * Number zeroOne = isPropertyEmpty(String propName, String tokenId: currentToken())
 		 */
 		if (functionName.equals("isPropertyEmpty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = getTokenFromParam(resolver, "isPropertyEmpty", parameters, 1);
 			return token.getProperty(parameters.get(0).toString()) == null ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/* pre 1.3.b64 only took a single parameter
 		 *
 		 * Number zeroOne = getPropertyDefault(String propName, String propType: currentToken().getPropertyType())
 		 */
 		if (functionName.equals("getPropertyDefault")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			Token token = resolver.getTokenInContext();
 			String name = parameters.get(0).toString();
 			String propType = parameters.size() > 1 ? parameters.get(1).toString() : token.getPropertyType();
 
 			Object val = null;
 
 			List<TokenProperty> propertyList = MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(propType);
 			if (propertyList != null) {
 				for (TokenProperty property : propertyList) {
 					if (name.equalsIgnoreCase(property.getName())) {
 						val = property.getDefaultValue();
 						break;
 					}
 				}
 			}
 
 			if (val == null) {
 				return "";
 			}
 
 			if (val instanceof String) {
 				// try to convert to a number
 				try {
 					return new BigDecimal(val.toString());
 				} catch (Exception e) {
 					return val;
 				}
 			} else {
 				return val;
 			}
 		}
 
 		/*
 		 * String empty = bringToFront(String tokenId: currentToken())
 		 */
 		if (functionName.equals("bringToFront")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Set<GUID> tokens = new HashSet<GUID>();
 			Token token = getTokenFromParam(resolver, "bringToFront", parameters, 0);
 
 			tokens.add(token.getId());
 			MapTool.serverCommand().bringTokensToFront(zone.getId(), tokens);
 
 			return "";
 		}
 
 		/*
 		 * String empty = sendToBack(String tokenId: currentToken())
 		 */
 		if (functionName.equals("sendToBack")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Set<GUID> tokens = new HashSet<GUID>();
 			Token token = getTokenFromParam(resolver, "sendToBack", parameters, 0);
 
 			tokens.add(token.getId());
 			MapTool.serverCommand().sendTokensToBack(zone.getId(), tokens);
 
 			return "";
 		}
 
 		/*
 		 * String value = getLibProperty(String propName, String tokenId: macroSource)
 		 */
 		if (functionName.equals("getLibProperty")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			String location;
 			if (parameters.size() > 1) {
 				location = parameters.get(1).toString();
 			} else {
 				location = MapTool.getParser().getMacroSource();
 			}
 			Token token = MapTool.getParser().getTokenMacroLib(location);
 
 			Object val = token.getProperty(parameters.get(0).toString());
 
 			// Attempt to convert to a number ...
 			try {
 				val = new BigDecimal(val.toString());
 			} catch (Exception e) {
 				// Ignore, use previous value of "val"
 			}
 			return val == null ? "" : val;
 		}
 
 		/*
 		 * String empty = setLibProperty(String propName, String value, String tokenId: macroSource)
 		 */
 		if (functionName.equals("setLibProperty")) {
 			if (parameters.size() < 2) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
 			} else
 				if (parameters.size() > 3) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 3, parameters.size()));
 				}
 			String location;
 			if (parameters.size() > 2) {
 				location = parameters.get(2).toString();
 			} else {
 				location = MapTool.getParser().getMacroSource();
 			}
 			Token token = MapTool.getParser().getTokenMacroLib(location);
 			token.setProperty(parameters.get(0).toString(), parameters.get(1).toString());
 			Zone z = MapTool.getParser().getTokenMacroLibZone(location);
 			MapTool.serverCommand().putToken(z.getId(), token);
 
 			return "";
 		}
 
 		/*
 		 * String names = getLibPropertyNames(String tokenId: {macroSource | "*" | "this"}, String delim: ",")
 		 */
 		if (functionName.equals("getLibPropertyNames")) {
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			String location;
 			if (parameters.size() > 0) {
 				location = parameters.get(0).toString();
 				if (location.equals("*")  || location.equalsIgnoreCase("this")) {
 					location = MapTool.getParser().getMacroSource();
 				}
 			} else {
 				location = MapTool.getParser().getMacroSource();
 			}
 			Token token = MapTool.getParser().getTokenMacroLib(location);
 			if (token == null) {
 				throw new ParserException(I18N.getText("macro.function.tokenProperty.unknownLibToken", functionName, location));
 			}
 			String delim = parameters.size() > 1 ? parameters.get(1).toString() : ",";
 
 			return getPropertyNames(token, delim, ".*", false);
 		}
 
 		/*
 		 * String names = getMatchingLibProperties(String pattern, String tokenId: {macroSource | "*" | "this"}, String delim: ",")
 		 */
 		if (functionName.equals("getMatchingLibProperties")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 3) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 3, parameters.size()));
 				}
 			String location;
 			String pattern = parameters.get(0).toString();
 			if (parameters.size() > 1) {
 				location = parameters.get(1).toString();
 				if (location.equals("*") || location.equalsIgnoreCase("this")) {
 					location = MapTool.getParser().getMacroSource();
 				}
 			} else {
 				location = MapTool.getParser().getMacroSource();
 			}
 			Token token = MapTool.getParser().getTokenMacroLib(location);
 			if (token == null) {
 				throw new ParserException(I18N.getText("macro.function.tokenProperty.unknownLibToken", functionName, location));
 			}
 			String delim = parameters.size() > 2 ? parameters.get(2).toString() : ",";
 			return getPropertyNames(token, delim, pattern, false);
 		}
 
 		/*
 		 * Number facing = getTokenFacing(String tokenId: currentToken())
 		 */
 		if (functionName.equals("getTokenFacing")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getTokenFacing", parameters, 0);
 			if (token.getFacing() == null) {
 				return "";		// XXX Should be -1 instead of a string?
 			}
 			return BigDecimal.valueOf(token.getFacing());
 		}
 
 		/*
 		 * String empty = setTokenFacing(Number facing, String tokenId: currentToken())
 		 */
 		if (functionName.equals("setTokenFacing")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			} else
 				if (parameters.size() > 2) {
 					throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 				}
 			if (!(parameters.get(0) instanceof BigDecimal)) {
 				throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName,1, parameters.get(0).toString()));
 			}
 
 			Token token = getTokenFromParam(resolver, "setTokenFacing", parameters, 1);
 			token.setFacing(((BigDecimal)parameters.get(0)).intValue());
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zoneR.flushLight();	// FJE This isn't needed unless the token had a light source, right?  Should we check for that?
 			zone.putToken(token);
 			return "";
 		}
 
 		/*
 		 * String empty = removeTokenFacing(String tokenId: currentToken())
 		 */
 		if (functionName.equals("removeTokenFacing")) {
 			if (parameters.size() != 1) {
 				throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "removeTokenFacing", parameters, 0);
 			token.setFacing(null);
 			MapTool.serverCommand().putToken(zone.getId(), token);
 			zoneR.flushLight();
 			zone.putToken(token);
 			return "";
 		}
 
 		/*
 		 * Number zeroOne = isSnapToGrid(String tokenId: currentToken())
 		 */
 		if (functionName.equals("isSnapToGrid")) {
 			if (parameters.size() > 1) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getTokenFacing", parameters, 0);
 			return token.isSnapToGrid() ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 
 		/*
 		 * String empty = setOwner(String playerName | JSONArray playerNames, String tokenId: currentToken())
 		 */
 		if (functionName.equals("setOwner")) {
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Token token = getTokenFromParam(resolver, "getTokenFacing", parameters, 1);
 			// Remove current owners
 			token.clearAllOwners();
 
 			if (parameters.get(0).equals("")) {
 				return "";
 			}
 
 			Object json = JSONMacroFunctions.asJSON(parameters.get(0));
 			if (json != null && json instanceof JSONArray) {
 				for (Object o : (JSONArray)json) {
 					token.addOwner(o.toString());
 				}
 			} else {
 				token.addOwner(parameters.get(0).toString());
 			}
 			return "";
 		}
 
 
 		throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
 	}
 
 	/**
 	 * Gets the size of the token.
 	 * @param token The token to get the size of.
 	 * @return the size of the token.
 	 */
 	private String getSize(Token token) {
 		Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
 		for (TokenFootprint footprint : grid.getFootprints()) {
 			if (token.isSnapToScale() && token.getFootprint(grid) == footprint) {
 				return footprint.getName();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Sets the size of the token.
 	 * @param token The token to set the size of.
 	 * @param size The size to set the token to.
 	 * @return The new size of the token.
 	 * @throws ParserException if the size specified is an invalid size.
 	 */
 	private String setSize(Token token, String size) throws ParserException {
 		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 		Zone zone = renderer.getZone();
 		Grid grid = zone.getGrid();
 		for (TokenFootprint footprint : grid.getFootprints()) {
 			if (token.isSnapToScale() && footprint.getName().equalsIgnoreCase(size)) {
 				token.setFootprint(grid, footprint);
 				token.setSnapToScale(true);
 				renderer.flush(token);
 				// XXX Why is the putToken() called twice?
 //				MapTool.serverCommand().putToken(zone.getId(), token);
 				renderer.repaint();
 				MapTool.getFrame().updateTokenTree();
 				MapTool.serverCommand().putToken(zone.getId(), token);
 				zone.putToken(token);
 				return getSize(token);
 			}
 		}
 		throw new ParserException(I18N.getText("macro.function.tokenProperty.invalidSize", "setSize", size));
 	}
 
 	/**
 	 * Sets the layer of the token.
 	 * @param token The token to move to a different layer.
 	 * @param layerName the name of the layer to move the token to.
 	 * @return the name of the layer the token was moved to.
 	 * @throws ParserException if the layer name is invalid.
 	 */
 	public String setLayer(Token token, String layerName) throws ParserException {
 
 		Zone.Layer layer;
 
 		if (layerName.equalsIgnoreCase(Zone.Layer.TOKEN.toString())) {
 			layer = Zone.Layer.TOKEN;
 		} else if (layerName.equalsIgnoreCase(Zone.Layer.BACKGROUND.toString())) {
 			layer = Zone.Layer.BACKGROUND;
 		} else if (layerName.equalsIgnoreCase("gm") || layerName.equalsIgnoreCase(Zone.Layer.GM.toString())) {
 			layer = Zone.Layer.GM;
 		} else if (layerName.equalsIgnoreCase(Zone.Layer.OBJECT.toString())) {
 			layer = Zone.Layer.OBJECT;
 		} else {
 			throw new ParserException(I18N.getText("macro.function.tokenProperty.unknownLayer", "setLayer", layerName));
 		}
 
 		token.setLayer(layer);
 		switch (layer) {
 		case BACKGROUND:
 		case OBJECT:
 			token.setShape(Token.TokenShape.TOP_DOWN);
 			break;
 		case GM:
 		case TOKEN:
 			Image image = ImageManager.getImage(token.getImageAssetId());
 			if (image == null || image == ImageManager.TRANSFERING_IMAGE) {
 				token.setShape(Token.TokenShape.TOP_DOWN);
 			} else {
 				token.setShape(TokenUtil.guessTokenType(image));
 			}
 			break;
 		}
 
 		return layerName;
 	}
 
 	/**
 	 * Checks to see if the token has the specified property.
 	 * @param token The token to check.
 	 * @param name The name of the property to check.
 	 * @return true if the token has the property.
 	 */
 	private boolean hasProperty(Token token, String name) {
 		Object val = token.getProperty(name);
 		if (val == null) {
 			return false;
 		}
 
 		if (StringUtil.isEmpty(val.toString())) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Gets all the property names for the specified type.
 	 * If type is null then all the property names for all types are returned.
 	 * @param type The type of property.
 	 * @param delim The list delimiter.
 	 * @return a string list containing the property names.
 	 * @throws ParserException
 	 */
 	private String getAllPropertyNames(String type, String delim) throws ParserException {
 		if (type == null || type.length() == 0 || type.equals("*")) {
 			Map<String, List<TokenProperty>> pmap =
 				MapTool.getCampaign().getCampaignProperties().getTokenTypeMap();
 			ArrayList<String> namesList = new ArrayList<String>();
 
 			for (Entry<String, List<TokenProperty>> entry : pmap.entrySet()) {
 				for (TokenProperty tp : entry.getValue()) {
 					namesList.add(tp.getName());
 				}
 			}
 			if ("json".equals(delim)) {
 				return JSONArray.fromObject(namesList).toString();
 			} else {
 				return StringFunctions.getInstance().join(namesList, delim);
 			}
 		} else {
 			List<TokenProperty> props = MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(type);
 			if (props == null) {
 				throw new ParserException(I18N.getText("macro.function.tokenProperty.unknownPropType", "getAllPropertyNames", type));
 			}
 			ArrayList<String> namesList = new ArrayList<String>();
 			for (TokenProperty tp : props) {
 				namesList.add(tp.getName());
 			}
 			if ("json".equals(delim)) {
 				return JSONArray.fromObject(namesList).toString();
 			} else {
 				return StringFunctions.getInstance().join(namesList);
 			}
 		}
 	}
 
 	/**
 	 * Creates a string list delimited by <b>delim</b> of the names of all the
 	 * properties for a given token.  Returned strings are all lowercase.
 	 * @param token The token to get the property names for.
 	 * @param delim The delimiter for the list.
 	 * @param pattern The regexp pattern to match.
 	 * @return the string list of property names.
 	 */
 	private String getPropertyNames(Token token, String delim, String pattern, boolean raw) {
 		List<String> namesList = new ArrayList<String>();
 		Pattern pat = Pattern.compile(pattern);
 		Set<String> propSet = (raw ? token.getPropertyNamesRaw() : token.getPropertyNames());
 		String[] propArray = new String[ propSet.size() ];
 		propSet.toArray(propArray);
 		Arrays.sort(propArray);
 
 		for (String name : propArray) {
 			Matcher m = pat.matcher(name);
 			if (m.matches()) {
 				namesList.add(name);
 			}
 		}
 
 		String[] names = new String[namesList.size()];
 		namesList.toArray(names);
 		if ("json".equals(delim)) {
 			return JSONArray.fromObject(names).toString();
 		} else {
 			return StringFunctions.getInstance().join(names, delim);
 		}
 	}
 
 	/**
 	 * Gets the owners for the token.
 	 * @param token The token to get the owners for.
 	 * @param delim the delimiter for the list.
 	 * @return a string list of the token owners.
 	 */
 	public String getOwners(Token token, String delim) {
 		String[] owners = new String[token.getOwners().size()];
 		token.getOwners().toArray(owners);
 		if ("json".endsWith(delim)) {
 			return JSONArray.fromObject(owners).toString();
 		} else {
 			return StringFunctions.getInstance().join(owners, delim);
 		}
 	}
 
 	/**
 	 * Gets the token from the specified index or returns the token in context. This method
 	 * will check the list size before trying to retrieve the token so it is safe to use
 	 * for functions that have the token as a optional argument.
 	 * @param res The variable resolver.
 	 * @param functionName The function name (used for generating exception messages).
 	 * @param param The parameters for the function.
 	 * @param index The index to find the token at.
 	 * @return the token.
 	 * @throws ParserException if a token is specified but the macro is not trusted, or the
 	 *                         specified token can not be found, or if no token is specified
 	 *                         and no token is impersonated.
 	 */
 	private Token getTokenFromParam(MapToolVariableResolver res, String functionName, List<Object> param, int index) throws ParserException {
 		Token token;
 		if (param.size() > index) {
 			if (!MapTool.getParser().isMacroTrusted()) {
 				throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
 			}
 			token = FindTokenFunctions.findToken(param.get(index).toString(), null);
 			if (token == null) {
 				throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, param.get(index)));
 			}
 		} else {
 			token = res.getTokenInContext();
 			if (token == null) {
 				throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
 			}
 		}
 		return token;
 	}
 }
