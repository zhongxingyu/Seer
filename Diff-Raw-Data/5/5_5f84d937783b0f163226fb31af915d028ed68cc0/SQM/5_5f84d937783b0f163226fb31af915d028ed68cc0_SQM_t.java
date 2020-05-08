 package de.avdclan.arma2mapconverter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 public class SQM {
 
 	private TypeClass rootType = new TypeClass("units", null);
 	private TypeClass markers = new TypeClass("markers", null);
 	private static Logger logger = Logger.getLogger(SQM.class);
 	private BufferedReader reader;
 	private int groupCountWest = 0;
 	private int groupCountEast = 0;
 	private int groupCountGuer = 0;
 	private int groupCountCiv = 0;
 	private File source;
 	
 	public void load(File mission) throws FileNotFoundException {
 		logger.debug("Loading SQM Mission: " + mission.getAbsolutePath());
 		this.source = mission;
 		reader = new BufferedReader(new FileReader(mission));
 		String line;
 		try {
 			while((line = reader.readLine()) != null) {
 				String input = line.replaceAll("^\\s+", "");
 				String type = null;
 				if(input.startsWith("class")) {
 					
 					String[] spl = line.split(" ", 2);
 					type = spl[1];
 				}
 				if(type != null) {
 					if(type.equals("Groups")) {
 						logger.debug("Processing groups... ");
 						parse(line, rootType);
 						logger.debug("Groups processed. " + rootType.getFullCount() + " Groups processed.");
 					}
 					if(type.equals("Markers")) {
 						logger.debug("Processing markers... ");
 						parse(line, markers);
 						logger.debug("Markers processed. " + markers.getFullCount() + " Markers processed.");
 					}
 				}
 				
 				
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			logger.error(e);
 		}
 		
 		
 		logger.debug("Loaded.");
 	}
 	
 	/**
 	 * This is the parsing algorithm.
 	 * If you know a better way, feel free to change it.
 	 * 
 	 * Please also send your changes to the author.
 	 * 
 	 * 
 	 * @param input
 	 * @throws IOException
 	 */
 	private void parse(String input, TypeClass parent) throws IOException {
 		String line = input.replaceAll("^\\s+", "");
 		if(line.startsWith("class")) {
 			
 			String[] spl = line.split(" ", 2);
 			TypeClass typeClass = new TypeClass(spl[1], parent);
 			parent.getChilds().add(typeClass);			
 			
 			while(! (line = reader.readLine().replaceAll("^\\s+", "")).startsWith("}")) {
 				parse(line, typeClass);
 			}
 			
 		}	
 		
 		if(parent.toString().startsWith("Vehicles")) {
 			if(parent.getObject() == null) {
 				parent.setObject(new Vehicle());
 			}
 			TypeClass p = parent.getParent();
 			if(p.toString().startsWith("Item")) {
 				
 				((Vehicle)parent.getObject()).setSide(((Item)p.getObject()).getSide());
 				
 			}
 			
 			
 		} else if(parent.toString().startsWith("Markers")) {
 			if(parent.getObject() == null) {
 				parent.setObject(new Markers());
 			}
 			TypeClass p = parent.getParent();
 			if(p.toString().startsWith("Item")) {
 				
 				((Markers)parent.getObject()).setSide(((Item)p.getObject()).getSide());
 				
 			}
 			
 			
 		} else if(parent.toString().startsWith("Item")) {
 			if(parent.getObject() == null) {
 				parent.setObject(new Item(parent.toString()));
 			}
 			if(line.startsWith("position[]=")) {
 				String[] tmp = line.split("=", 2);
 				tmp = tmp[1].split(",", 3);
 				String x, y, z;
 				x = tmp[0].replaceAll("\\{", "");
 				z = tmp[1];
 				y = tmp[2].replaceAll("\\}\\;", "");
 				((Item)parent.getObject()).setPosition(new Position(x, y, "0"));
 			}
 			if(line.startsWith("id=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setId(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("side=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setSide(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("vehicle=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setVehicle(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("skill=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setSkill(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("leader=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setLeader(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("init=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setInit(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("name=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setName(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("markerType=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setMarkerType(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("type=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setType(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("rank=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setRank(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("presenceCondition=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setPresenceCondition(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("azimut=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setAzimut(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("colorName=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setColorName(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("fillName=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setFillName(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("a=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setA(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("b=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setB(tmp[1].replaceAll("\\;", ""));
 			} else
 			if(line.startsWith("angle=")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setAngle(tmp[1].replaceAll("\\;", ""));
 			}
			else
				if(line.startsWith("text=")) {
					String[] tmp = line.split("=", 2);
					((Item)parent.getObject()).setText(tmp[1].replaceAll("\\;", ""));
				}
 		} else {
 			// unsupported class
 		}
 		
 	}
 
 	
 
 	public SQF toSQF() {
 		SQF sqf = new SQF();
 		String code = "" +
 				"/* converted with Arma2MapConverter v" + Arma2MapConverter.VERSION + "\n" +
 				" *\n" +
 				" * Source: " + source.getAbsolutePath() + "\n" +
 				" * Date: " + DateFormat.getInstance().format(Calendar.getInstance().getTime()) + "\n" +
 				" */\n\n";
 		code +=	"_westHQ = createCenter west;\n" +
 				"_eastHQ = createCenter east;\n" +
 				"_guerHQ = createCenter resistance;\n" +
 				"_civHQ  = createCenter civilian;\n";
 		
 		code += "\n\n// UNIT CREATION\n";
 		code += generateSQF(rootType);
 		code += "\n\n// MARKER CREATION\n";
 		code += generateSQF(markers);
 		sqf.setCode(code);
 		return sqf;
 	}
 
 	private String generateSQF(TypeClass typeClass) {
 		String code = "";
 						
 		int groupCount = 0;
 		for(TypeClass tc : typeClass.getChilds()) {
 			if(tc.equals("Markers")) {
 				String side = ((Markers)tc.getObject()).getSide().toLowerCase();
 				for(TypeClass items : tc.getChilds()) {
 					Item item = (Item) items.getObject();
 					if(item.getName() == null) {
 						// generate unique unit name
 						item.setName("marker_" + UUID.randomUUID().toString().replaceAll("-", ""));
 					}
 					code += "_marker = createMarker[" + item.getName() + ", [" + item.getPosition().getX() + ", " + item.getPosition().getY() + "]];\n" +
 							"_marker setMarkerShape " + item.getType() + ";\n" +
 							"_marker setMarkerType " + item.getMarkerType() + ";\n" +
 							"_marker setMarkerSize [" + item.getA() + ", " + item.getB() + "];\n";
 					
 					/*
 					_this setMarkerText "This is a Text";
 					_this setMarkerType "Faction_BLUFOR_EP1";
 					_this setMarkerColor "ColorYellow";
 					_this setMarkerBrush "Horizontal";
 					_this setMarkerSize [12, 144];
 					*/
 					
 					if(item.getText() != null) {
 						code += "_marker setMarkerText " + item.getText() + ";\n";
 					}
 					if(item.getColorName() != null) {
 						code += "_marker setMarkerColor " + item.getColorName() +";\n";
 					}
 					if(item.getFillName() != null) {
 						code += "_marker setMarkerBrush " + item.getFillName() +";\n";
 					}
 					
 					
 					
 				}
 			}
 			if(tc.equals("Vehicles")) {
 				
 				String side = ((Vehicle)tc.getObject()).getSide().toLowerCase();
 				String group = "_group_" + side + "_" + getGroupCound(side);
 				
 				code +=	"\n// group " + group + "\n" +
 						group + " = createGroup _" + side +"HQ;\n";
 					
 				for(TypeClass items : tc.getChilds()) {
 					
 					Item item = (Item) items.getObject();
 					
 					
 					if(item.getName() == null) {
 						// generate unique unit name
 						item.setName("autogen_" + UUID.randomUUID().toString().replaceAll("-", ""));
 					}
 					code += "\n// begin " + item.getName() +", part of group " + group + "\n";
 					code += "if (" + item.getPresenceCondition() + ") then\n{";
 					if(item.getSide().equals("EMPTY")) {
 						code += "\n" +
 							"\t" + item.getName() + " = createVehicle [" + item.getVehicle() + ", " + item.getPosition() + ", [], 0, \"CAN_COLLIDE\"];\n";
 					} else {
 						code += "\n" +
 								"\t" + item.getName() + " = " + group + " createUnit [" + item.getVehicle() + ", " + item.getPosition() + ", [], 0, \"CAN_COLLIDE\"];\n" +
 								// this is VERY dirty....
 								"\t// this is VERY dirty and only used because I don't want to create\n" +
 								"\t// arrays for vehicles, units and stuff to check if the classname\n" +
 								"\t// is a vehicle, an unit, and so on. this just works.\n" +
 								"\t// what it does is if the unit is not alive after creation (because it should be a manned vehicle)\n" +
 								"\t// it will be created with createVehicle and manned with the BIS_fnc_spawnCrew function.\n" +
 								"\tif(!alive " + item.getName() + ") then {\n" +
 								"\t\t" + item.getName() + " = createVehicle [" + item.getVehicle() + ", " + item.getPosition() + ", [], 0, \"CAN_COLLIDE\"];\n" + 
 								"\t\t_group = createGroup _" + item.getSide().toLowerCase() + "HQ;\n" +
 								"\t\t[" + item.getName() + ", _group] call BIS_fnc_spawnCrew;\n" +
 								"\t};\n";
 						
 						
 					}
 					if(item.getInit() != null) {
 						code += "\t" + item.getName() + " setVehicleInit " + item.getInit() + ";\n";
 					}
 					
 					if(item.getAzimut() != null) {
 						code += "\t" + item.getName() + " setDir " + item.getAzimut() + ";\n";
 					}
 					
 					if(item.getSkill() != null && !item.getSide().equals("EMPTY")) {
 						code += "\t" + item.getName() + " setUnitAbility " + item.getSkill() +";\n";
 					}
 					if(item.getRank() != null && !item.getSide().equals("EMPTY")) {
 						code += "\t" + item.getName() + " setRank " + item.getRank() + ";\n";
 					}
 					if(item.getLeader() != null && !item.getSide().equals("EMPTY")) {
 						code += "\tif(true) then { " + group + " selectLeader " + item.getName() + "; };\n";
 					}
 					
 					
 					code += "};\n// end of " + item.getName() + "\n";
 					
 				}
 				
 				
 			} else {
 				code += generateSQF(tc); 
 			}
 		}
 		
 		return code;
 	}
 
 	private String getGroupCound(String side) {
 		if(side.equals("west")) {
 			++groupCountWest;
 			return String.valueOf(groupCountWest);
 		}
 		if(side.equals("east")) {
 			++groupCountEast;
 			return String.valueOf(groupCountEast);
 		}
 		if(side.equals("guer")) {
 			++groupCountGuer;
 			return String.valueOf(groupCountGuer);
 		}
 		
 		++groupCountCiv;
 		return String.valueOf(groupCountCiv);
 		
 		
 	}
 	
 
 }
