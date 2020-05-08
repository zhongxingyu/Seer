 package esmj3dfo3.j3d.j3drecords.type;
 
 import java.util.ArrayList;
 
 import nif.character.NifCharacter;
 import utils.ESConfig;
 import utils.source.MediaSources;
 import esmLoader.common.data.record.IRecordStore;
 import esmLoader.common.data.record.Record;
 import esmj3d.data.shared.subrecords.CNTO;
 import esmj3d.data.shared.subrecords.MODL;
 import esmj3d.j3d.j3drecords.type.J3dRECOType;
 import esmj3dfo3.data.records.ARMO;
 import esmj3dfo3.data.records.LVLI;
 import esmj3dfo3.data.records.NPC_;
 import esmj3dfo3.data.records.RACE;
 import esmj3dfo3.data.records.WEAP;
 import esmj3dfo3.data.subrecords.LVLO;
 
 public class J3dNPC_ extends J3dRECOType
 {
 	private String helmetStr = null;
 
 	private String headStr = null;
 
 	private String bodyStr = null;
 
 	private String handLStr = null;
 
 	private String handRStr = null;
 
 	// TODO: worn vs carried weaps
 	private String weapStr = null;
 
 	private boolean female = false;
 
 	private NifCharacter nifCharacter;
 
 	public J3dNPC_(NPC_ npc_, IRecordStore master, MediaSources mediaSources)
 	{
 		super(npc_, null);
 
 		female = npc_.ACBS.isFemale();
 
 		Record rrec = master.getRecord(npc_.RNAM.formId);
 		RACE race = new RACE(rrec);
 		MODL[] modls = race.MODLs;
 
 		// the defaults
 		if (female)
 		{
 			headStr = ESConfig.TES_MESH_PATH + "characters\\head\\headfemale.nif";
 			//All beast races are just humans with a different texture
 			bodyStr = ESConfig.TES_MESH_PATH + "characters\\_male\\femaleupperbody.nif";
 			handLStr = ESConfig.TES_MESH_PATH + "characters\\_male\\femalelefthand.nif";
			handRStr = ESConfig.TES_MESH_PATH + "characters\\_male\\femalerighthand.nif";
 		}
 		else
 		{
 			headStr = modls[0].model.str;
 			bodyStr = ESConfig.TES_MESH_PATH + "characters\\_male\\upperbody.nif";
 			handLStr = ESConfig.TES_MESH_PATH + "characters\\_male\\lefthand.nif";
			handRStr = ESConfig.TES_MESH_PATH + "characters\\_male\\righthand.nif";
 		}
 
 		CNTO[] cntos = npc_.CNTOs;
 		for (int i = 0; i < cntos.length; i++)
 		{
 			//	int count = cntos[i].count;
 			Record rec = master.getRecord(cntos[i].itemFormId);
 			if (rec.getRecordType().equals("WEAP"))
 			{
 				WEAP weap = new WEAP(rec);
 				addWEAP(weap);
 			}
 			else if (rec.getRecordType().equals("ARMO"))
 			{
 				ARMO armo = new ARMO(rec);
 				addARMO(armo);
 			}
 			else if (rec.getRecordType().equals("AMMO"))
 			{
 				//AMMO ammo = new AMMO(rec);
 			}
 			else if (rec.getRecordType().equals("MISC"))
 			{
 				//MISC misc = new MISC(rec);
 			}
 			else if (rec.getRecordType().equals("KEYM"))
 			{
 				//KEYM keym = new KEYM(rec);
 			}
 			else if (rec.getRecordType().equals("INGR"))
 			{
 				//INGR keym = new INGR(rec);
 			}
 			else if (rec.getRecordType().equals("LIGH"))
 			{
 				//LIGH ligh = new LIGH(rec);
 			}
 			else if (rec.getRecordType().equals("ALCH"))
 			{
 				//ALCH alch = new ALCH(rec);
 			}
 			else if (rec.getRecordType().equals("BOOK"))
 			{
 				//BOOK book = new BOOK(rec);
 			}
 			else if (rec.getRecordType().equals("NOTE"))
 			{
 				//NOTE book = new NOTE(rec);
 			}
 			else if (rec.getRecordType().equals("LVLI"))
 			{
 				LVLI lvli = new LVLI(rec);
 				LVLO[] LVLOs = lvli.LVLOs;
 
 				int idx = (int) (Math.random() * LVLOs.length);
 				idx = idx == LVLOs.length ? 0 : idx;
 
 				Record baseRecord = master.getRecord(LVLOs[idx].itemFormId);
 				if (baseRecord.getRecordType().equals("ARMO"))
 				{
 					ARMO armo = new ARMO(baseRecord);
 					armo.getClass();
 				}
 				else if (baseRecord.getRecordType().equals("LVLI"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("MISC"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("AMMO"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("INGR"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("ALCH"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("SLGM"))
 				{
 				}
 				else if (baseRecord.getRecordType().equals("WEAP"))
 				{
 					WEAP weap = new WEAP(baseRecord);
 					addWEAP(weap);
 				}
 				else
 				{
 					System.out.println("LVLI record type not converted to j3d " + baseRecord.getRecordType());
 				}
 			}
 			else
 			{
 				System.out.println("NPC_ has unknown contained item " + rec.getRecordType());
 			}
 
 		}
 
 		String skeletonNifFile = ESConfig.TES_MESH_PATH + "characters\\_male\\skeleton.nif";
 
 		ArrayList<String> fileNames = new ArrayList<String>();
 		fileNames.add(headStr);
 		fileNames.add(helmetStr);
 		fileNames.add(bodyStr);
		System.out.println("l hend added " + handLStr);
 		fileNames.add(handLStr);
 		fileNames.add(handRStr);
 		fileNames.add(weapStr);
 
 		ArrayList<String> idleAnimations = new ArrayList<String>();
 		idleAnimations.add(ESConfig.TES_MESH_PATH + "characters\\_male\\idleanims\\lookingaround.kf");
 
 		nifCharacter = new NifCharacter(skeletonNifFile, fileNames, mediaSources, idleAnimations);
 		addChild(nifCharacter);
 
 	}
 
 	private void addARMO(ARMO armo)
 	{
 		MODL m = armo.MODL;
 		if (female && armo.MOD3 != null)
 		{
 			m = armo.MOD3;
 		}
 
 		helmetStr = armo.BMDT.isHair() ? m.model.str : helmetStr;
 		bodyStr = armo.BMDT.isUpperBody() ? m.model.str : bodyStr;
 		handLStr = armo.BMDT.isHand() ? m.model.str : handLStr;
 		handRStr = armo.BMDT.isHand() ? m.model.str : handRStr;
 	}
 
 	private void addWEAP(WEAP weap)
 	{
 		if (weap.MODL != null)
 		{
 			weapStr = weap.MODL.model.str;
 		}
 		else
 		{
 			//TODO:  male female H2H models		 see WEAP	
 		}
 
 	}
 
 	@Override
 	public void renderSettingsUpdated()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
