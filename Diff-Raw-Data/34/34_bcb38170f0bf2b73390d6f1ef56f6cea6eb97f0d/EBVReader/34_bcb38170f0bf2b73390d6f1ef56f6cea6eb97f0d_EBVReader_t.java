 
 package edu.nps.moves.siso;
 
 import java.io.*;
import java.math.*;
 import java.util.*;
 import javax.xml.bind.*;
 
 import edu.nps.moves.siso.jaxb.*;
 
 /**
  * Reads the SISO EBV XML document and turns it into java enumeration objects.
  * The SISO EBV document is available (as of this writing) at the SISO enum
  * mailing list, http://discussions.sisostds.org/default.asp?action=10&fid=31
  * 
  * 
  * @author DMcG, Jason Nelson
  */
 public class EBVReader 
 {
     /** Location of EBV document. This should match up with the schema available
      * in the data directory. JAXB was used to generate the classes in 
      * edu.nps.moves.siso.jaxb, which is used to parse and databind the 
      * xml document--if the schema changes, you'll have to regenerate those
      * classes.
      */
     public static final String EBV_XML_DOCUMENT = "data/siso-std-010.xml";
     
     public static void main(String args[])
     {
         try
         {
             
             // Parse the EBV XML document
              JAXBContext context = JAXBContext.newInstance("edu.nps.moves.siso.jaxb");
              Unmarshaller unmarshaller = context.createUnmarshaller();
              Ebv data = (Ebv)unmarshaller.unmarshal(new FileInputStream("data/siso-std-010.xml"));
              
              // Retrieve the enumerations
              List<GenerictableT> genericList = data.getEnumOrBitmaskOrCet();
              for(int idx = 0; idx < genericList.size(); idx++)
              {
                  GenerictableT gen = genericList.get(idx);
 
                // System.out.println("generic table ID " + gen.getId() + " Name=" + gen.getName() + " cname=" + gen.getCname());
                  // For each enumeration in the XML that we are interested in, 
                  // generate a Java enumeration class
                  if(gen instanceof EnumT)
                  {
                      //System.out.println("generic table is an EnumT wtih cname " + gen.getCname());
                      EnumT en = (EnumT)gen;
                      List<EnumrowT> rows = en.getEnumrow();
                      //System.out.println("row ID=" + rows.get(0).getId());
                      //System.out.println("header length is " + en.getHeader().size());
                      //if(en.getHeader().size() == 0)
                      //    continue;
                      //HeaderT header = en.getHeader().get(0);
                      //ColT col = header.getCol().get(0);
                      
                      //System.out.println("column:" + gen.getCname());
                      
                     // Pdu Type
                      if(gen.getCname().equalsIgnoreCase("pduheader.pdutype"))
                      {
                          //System.out.println("PDU Type");
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PduType", "src/edu/nps/moves/disenum/PduType", en);
                      }
                      
                      // Country. this uses a special writer so that we can also pick up the internet code for
                      // that country, eg US, UK, DE, etc.
                      if(gen.getCname().equalsIgnoreCase("es.type.country"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeCountryEnumeration("CountryType", "src/edu/nps/moves/disenum/CountryType", en); 
                      }
                      
                      // Protocol family (entity interaction, logistics, etc
                      if(gen.getCname().equalsIgnoreCase("pduheader.protocolfamily"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ProtocolFamily", "src/edu/nps/moves/disenum/ProtocolFamily", en); 
                      }
                      
                      // Force ID (friendly, enemy, neutral, etc.)
                      if(gen.getCname().equalsIgnoreCase("es.forceid"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ForceID", "src/edu/nps/moves/disenum/ForceID", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("EntityKind", "src/edu/nps/moves/disenum/EntityKind", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain.1.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatformLand", "src/edu/nps/moves/disenum/PlatformLand", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain.2.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatformAir", "src/edu/nps/moves/disenum/PlatformAir", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain.3.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatformSurface", "src/edu/nps/moves/disenum/PlatformSurface", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain.4.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatformSubSurface", "src/edu/nps/moves/disenum/PlatformSubSurface", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain.5.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatformSpace", "src/edu/nps/moves/disenum/PlatformSpace", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.2.domain"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("MunitionDomain", "src/edu/nps/moves/disenum/MunitionDomain", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.2.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("MunitionCategory", "src/edu/nps/moves/disenum/MunitionCategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.225.kind.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("USWeaponsForLifeForms", "src/edu/nps/moves/disenum/USWeaponsForLifeForms", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.222.kind.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CISWeaponsForLifeForms", "src/edu/nps/moves/disenum/CISWeaponsForLifeForms", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.224.kind.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("UKWeaponsForLifeForms", "src/edu/nps/moves/disenum/UKWeaponsForLifeForms", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.71.kind.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FrenchWeaponsForLifeForms", "src/edu/nps/moves/disenum/FrenchWeaponsForLifeForms", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.78.kind.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("GermanWeaponsForLifeForms", "src/edu/nps/moves/disenum/GermanWeaponsForLifeForms", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("pduheader.protocolversion"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ProtocolVersion", "src/edu/nps/moves/disenum/ProtocolVersion", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.1.domain"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("EntityDomain", "src/edu/nps/moves/disenum/EntityDomain", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.4.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("EnvironmentalKind", "src/edu/nps/moves/disenum/EnvironmentalKind", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.type.kind.7.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RadioCategory", "src/edu/nps/moves/disenum/RadioCategory", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.type.kind.7.version"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RadioNomenclatureVersion", "src/edu/nps/moves/disenum/RadioNomenclatureVersion", en); 
                      }
                     
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.7.nomenclature"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RadioNomenclature", "src/edu/nps/moves/disenum/RadioNomenclature", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.type.kind.8.domain.2.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ExpendableAirCategory", "src/edu/nps/moves/disenum/ExpendableAirCategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.8.domain.3.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ExpendableSurfaceCategory", "src/edu/nps/moves/disenum/ExpendableSurfaceCategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.8.domain.4.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ExpendableSubsurfaceCategory", "src/edu/nps/moves/disenum/ExpendableSubsurfaceCategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.type.kind.9.cat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SensorEmitterCategory", "src/edu/nps/moves/disenum/SensorEmitterCategory", en); 
                      }
                                           
                      if(gen.getCname().equalsIgnoreCase("es.dra"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DeadReckoningAlgorithm", "src/edu/nps/moves/disenum/DeadReckoningAlgorithm", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("EntityMarking", "src/edu/nps/moves/disenum/EntityMarking", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.div"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DivisionCorpsDesignation", "src/edu/nps/moves/disenum/DivisionCorpsDesignation", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.1cavunit"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FirstCavHighLevelUnit", "src/edu/nps/moves/disenum/FirstCavHighLevelUnit", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.1infunit"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FirstInfHighLevelUnit", "src/edu/nps/moves/disenum/FirstInfHighLevelUnit", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.company"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CompanyBatteryTroop", "src/edu/nps/moves/disenum/CompanyBatteryTroop", en); 
                      }
                     
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.platoon"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PlatoonSection", "src/edu/nps/moves/disenum/PlatoonSection", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.vehicle"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Vehicle", "src/edu/nps/moves/disenum/Vehicle", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.symbol1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Bytes8_9_10_12", "src/edu/nps/moves/disenum/Bytes8_9_10_12", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.cctt.symbol2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Byte11", "src/edu/nps/moves/disenum/Byte11", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.markingtext.chevron.symbol"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DigitChevronCode", "src/edu/nps/moves/disenum/DigitChevronCode", en); 
                      }
                     
                     if(gen.getCname().equalsIgnoreCase("es.vp.type"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ParameterTypeDesignator", "src/edu/nps/moves/disenum/ParameterTypeDesignator", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.vp.type.1.attached"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AttachedParts", "src/edu/nps/moves/disenum/AttachedParts", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.vp.type.0.articulated.offset"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ArticulatedPartsOffsetNumber", "src/edu/nps/moves/disenum/ArticulatedPartsOffsetNumber", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("es.vp.type.0.articulated.index"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ArticulatedPartsIndexNumber", "src/edu/nps/moves/disenum/ArticulatedPartsIndexNumber", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("warfare.burstdescriptor.warhead"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Warhead", "src/edu/nps/moves/disenum/Warhead", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("warfare.burstdescriptor.fuse"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Fuse", "src/edu/nps/moves/disenum/Fuse", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("warfare.detonationresult"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetonationResult", "src/edu/nps/moves/disenum/DetonationResult", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.servicerequest"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ServiceRequestPDU", "src/edu/nps/moves/disenum/ServiceRequestPDU", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.general"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("GeneralRepairCode", "src/edu/nps/moves/disenum/GeneralRepairCode", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.drivetrain"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DriveTrain", "src/edu/nps/moves/disenum/DriveTrain", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.hull"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("HullAirframeBody", "src/edu/nps/moves/disenum/HullAirframeBody", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.environment"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("InterfacesWithEnvironment", "src/edu/nps/moves/disenum/InterfacesWithEnvironment", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.weapons"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Weapons", "src/edu/nps/moves/disenum/Weapons", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.fuelsystem"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FuelSystems", "src/edu/nps/moves/disenum/FuelSystems", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.electronics"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Electronics", "src/edu/nps/moves/disenum/Electronics", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.lifesupport"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("LifeSupportSystems", "src/edu/nps/moves/disenum/LifeSupportSystems", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.hydraulic"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("HydraulicSystemsAndActuators", "src/edu/nps/moves/disenum/HydraulicSystemsAndActuators", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repaircomplete.auxilary"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AuxiliaryCraft", "src/edu/nps/moves/disenum/AuxiliaryCraft", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("log.repairresponse"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RepairResponsePDU", "src/edu/nps/moves/disenum/RepairResponsePDU", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.datumid"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DatumSpecificationRecord", "src/edu/nps/moves/disenum/DatumSpecificationRecord", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.stop.reason"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Reason", "src/edu/nps/moves/disenum/Reason", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.ack.ackflag"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AcknowledgeFlag", "src/edu/nps/moves/disenum/AcknowledgeFlag", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.ack.responseflag"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ResponseFlag", "src/edu/nps/moves/disenum/ResponseFlag", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.actionrequest.actionid"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ActionID", "src/edu/nps/moves/disenum/ActionID", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.actionresponse.status"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RequestStatus", "src/edu/nps/moves/disenum/RequestStatus", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.eventreport.eventtype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("EventType", "src/edu/nps/moves/disenum/EventType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("simman.reliability.service"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RequiredReliabilityService", "src/edu/nps/moves/disenum/RequiredReliabilityService", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.name.electro"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ElectromagneticEmitters", "src/edu/nps/moves/disenum/ElectromagneticEmitters", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.name.acoustic"))
                      {
                          EBVReader reader = new EBVReader();
                         System.out.println("Enumeration row list length = " + en.getEnumrow().size());
                          reader.writeStandardEnumeration("AcousticEmitters", "src/edu/nps/moves/disenum/AcousticEmitters", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.name.other"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("OtherAcousticEmitters", "src/edu/nps/moves/disenum/OtherAcousticEmitters", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.function"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Function", "src/edu/nps/moves/disenum/Function", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.stateupdate"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("StateUpdateIndicator", "src/edu/nps/moves/disenum/StateUpdateIndicator", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.beamfunction"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("BeamFunction", "src/edu/nps/moves/disenum/BeamFunction", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.emission.hdtj"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("HighDensityTrackJam", "src/edu/nps/moves/disenum/HighDensityTrackJam", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.designator.codename"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CodeName", "src/edu/nps/moves/disenum/CodeName", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.designator.code"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DesignatorCode", "src/edu/nps/moves/disenum/DesignatorCode", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SystemType", "src/edu/nps/moves/disenum/SystemType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.name"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SystemName", "src/edu/nps/moves/disenum/SystemName", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.mode"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SystemMode", "src/edu/nps/moves/disenum/SystemMode", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.layerspecific"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("LayerSpecificInformation", "src/edu/nps/moves/disenum/LayerSpecificInformation", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.1.fop.altp4"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type1AltParameter4", "src/edu/nps/moves/disenum/Type1AltParameter4", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.1.sop.param1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type1OperationalParameter1", "src/edu/nps/moves/disenum/Type1OperationalParameter1", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.1.sop.param2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type1OperationalParameter2", "src/edu/nps/moves/disenum/Type1OperationalParameter2", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.2.fop.altp4"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type2AltParameter4", "src/edu/nps/moves/disenum/Type2AltParameter4", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.2.sop.param1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type2OperationalParameter1", "src/edu/nps/moves/disenum/Type2OperationalParameter1", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.2.sop.param2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type2OperationalParameter2", "src/edu/nps/moves/disenum/Type2OperationalParameter2", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.3.fop.altp4"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type3AltParameter4", "src/edu/nps/moves/disenum/Type3AltParameter4", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.3.sop.param1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type3OperationalParameter1", "src/edu/nps/moves/disenum/Type3OperationalParameter1", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.3.sop.param2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type3OperationalParameter2", "src/edu/nps/moves/disenum/Type3OperationalParameter2", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.4.fop.altp4"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type4AltParameter4", "src/edu/nps/moves/disenum/Type4AltParameter4", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.4.sop.param1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type4OperationalParameter1", "src/edu/nps/moves/disenum/Type4OperationalParameter1", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.4.sop.param2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type4OperationalParameter2", "src/edu/nps/moves/disenum/Type4OperationalParameter2", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.5.fop.altp4"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type5AltParameter4", "src/edu/nps/moves/disenum/Type5AltParameter4", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.5.sop.param1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type5OperationalParameter1", "src/edu/nps/moves/disenum/Type5OperationalParameter1", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.iff.type.5.sop.param2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Type5OperationalParameter2", "src/edu/nps/moves/disenum/Type5OperationalParameter2", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.statechangeupdate"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("StateChangeUpdateIndicator", "src/edu/nps/moves/disenum/StateChangeUpdateIndicator", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.systemname"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AcousticSystemName", "src/edu/nps/moves/disenum/AcousticSystemName", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.function"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Function", "src/edu/nps/moves/disenum/Function", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.activeparameterindex"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ActiveEmissionParameterIndex", "src/edu/nps/moves/disenum/ActiveEmissionParameterIndex", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.scanpattern"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ScanPattern", "src/edu/nps/moves/disenum/ScanPattern", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.passiveparameterindex"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("PassiveParameterIndex", "src/edu/nps/moves/disenum/PassiveParameterIndex", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.ua.apaparameterindex"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AdditionalPassiveActivity", "src/edu/nps/moves/disenum/AdditionalPassiveActivity", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.sees.power.aircraft"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Aircraft", "src/edu/nps/moves/disenum/Aircraft", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.sees.power.helicopters"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Helicopters", "src/edu/nps/moves/disenum/Helicopters", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("der.sees.power.tanks"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Tanks", "src/edu/nps/moves/disenum/Tanks", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("MajorModulation", "src/edu/nps/moves/disenum/MajorModulation", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.1.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModAmpMod", "src/edu/nps/moves/disenum/DetailedModAmpMod", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.2.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModAmpAndAngle", "src/edu/nps/moves/disenum/DetailedModAmpAndAngle", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.3.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModAngleMod", "src/edu/nps/moves/disenum/DetailedModAngleMod", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.4.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModCombinationMod", "src/edu/nps/moves/disenum/DetailedModCombinationMod", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.5.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModPulseMod", "src/edu/nps/moves/disenum/DetailedModPulseMod", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.6.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModUnmodulatedMod", "src/edu/nps/moves/disenum/DetailedModUnmodulatedMod", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.major.7.detail"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DetailedModCarrierPhaseShift", "src/edu/nps/moves/disenum/DetailedModCarrierPhaseShift", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.mod.system"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RadioSystem", "src/edu/nps/moves/disenum/RadioSystem", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.state"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("TransmitState", "src/edu/nps/moves/disenum/TransmitState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.inputsource"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("InputSource", "src/edu/nps/moves/disenum/InputSource", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.cryptosystem"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CryptoSystem", "src/edu/nps/moves/disenum/CryptoSystem", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.antennapatterntype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AntennaPatternType", "src/edu/nps/moves/disenum/AntennaPatternType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.referencesystem"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ReferenceSystem", "src/edu/nps/moves/disenum/ReferenceSystem", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.param.cctt.start"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("MessageStart", "src/edu/nps/moves/disenum/MessageStart", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.param.cctt.clear"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ClearChannel", "src/edu/nps/moves/disenum/ClearChannel", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.param.jtids.mode1"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("XmitTermPriMode", "src/edu/nps/moves/disenum/XmitTermPriMode", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.param.jtids.mode2"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("XmitTermSecMode", "src/edu/nps/moves/disenum/XmitTermSecMode", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.param.jtids.sync"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SynchronizationState", "src/edu/nps/moves/disenum/SynchronizationState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.protocolid"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("UserProtocolIDNum", "src/edu/nps/moves/disenum/UserProtocolIDNum", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.tx.tdltype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("TDLType", "src/edu/nps/moves/disenum/TDLType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.rx.state"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ReceiverState", "src/edu/nps/moves/disenum/ReceiverState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.controltype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ControlType", "src/edu/nps/moves/disenum/ControlType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.commtype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CommunicationType", "src/edu/nps/moves/disenum/CommunicationType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.command"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Command", "src/edu/nps/moves/disenum/Command", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.transmitstate"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("XmitLineState", "src/edu/nps/moves/disenum/XmitLineState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.deststate"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DestLineState", "src/edu/nps/moves/disenum/DestLineState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("radio.ic.param.type"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RecordType", "src/edu/nps/moves/disenum/RecordType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.collision.type"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CollisionType", "src/edu/nps/moves/disenum/CollisionType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SensorTypes", "src/edu/nps/moves/disenum/SensorTypes", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.1.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Optical", "src/edu/nps/moves/disenum/Optical", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.2.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FLIR", "src/edu/nps/moves/disenum/FLIR", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.3.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RADAR", "src/edu/nps/moves/disenum/RADAR", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.4.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Magnetic", "src/edu/nps/moves/disenum/Magnetic", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.5.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Laser", "src/edu/nps/moves/disenum/Laser", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.6.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SONAR", "src/edu/nps/moves/disenum/SONAR", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.7.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Physical", "src/edu/nps/moves/disenum/Physical", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("entity.mine.sensortype.8.subcat"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Multispectral", "src/edu/nps/moves/disenum/Multispectral", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.aggregate.state"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AggregateState", "src/edu/nps/moves/disenum/AggregateState", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.aggregate.formation"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Formation", "src/edu/nps/moves/disenum/Formation", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.aggregate.type.kind"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("AggregateKind", "src/edu/nps/moves/disenum/AggregateKind", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.aggregate.type.subcategory"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Subcategory", "src/edu/nps/moves/disenum/Subcategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.aggregate.type.specific"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Specific", "src/edu/nps/moves/disenum/Specific", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.ispartof.nature"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Nature", "src/edu/nps/moves/disenum/Nature", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.ispartof.position"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("Position", "src/edu/nps/moves/disenum/Position", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.ispartof.stationname"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("StationName", "src/edu/nps/moves/disenum/StationName", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.isgroupof.category"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("GroupedEntityCategory", "src/edu/nps/moves/disenum/GroupedEntityCategory", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.isgroupof.reststatus"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("RestStatus", "src/edu/nps/moves/disenum/RestStatus", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("eman.tc.transfertype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("TransferType", "src/edu/nps/moves/disenum/TransferType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.obj.objecttype.kind"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ObjectKind", "src/edu/nps/moves/disenum/ObjectKind", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.gridded.fieldnumber"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("FieldNumber", "src/edu/nps/moves/disenum/FieldNumber", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.gridded.coordinatesystem"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("CoordinateSystem", "src/edu/nps/moves/disenum/CoordinateSystem", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.gridded.constantgrid"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ConstantGrid", "src/edu/nps/moves/disenum/ConstantGrid", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.gridded.sampletype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("SampleType", "src/edu/nps/moves/disenum/SampleType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.gridded.datarepresentation"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("DataRepresentation", "src/edu/nps/moves/disenum/DataRepresentation", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.process.modeltype"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("ModelType", "src/edu/nps/moves/disenum/ModelType", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.process.type.geometryrecord"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("GeometryRecordTypeField", "src/edu/nps/moves/disenum/GeometryRecordTypeField", en); 
                      }
                      
                      if(gen.getCname().equalsIgnoreCase("env.process.type.staterecord"))
                      {
                          EBVReader reader = new EBVReader();
                          reader.writeStandardEnumeration("StateRecordTypeField", "src/edu/nps/moves/disenum/StateRecordTypeField", en); 
                      }
                      
                      
                  }
                  
              }
                  
         }
         catch(Exception e)
         {
             System.out.println("oops, problem creating files");
         }
         
     }
     
     private void writeStandardEnumeration(String enumerationName, String enumerationFile, EnumT anEnumeration)
     {
        System.out.println("Writing standard enumeration " + enumerationFile + ".java");
         try
         {
               File outputFile = new File(enumerationFile + ".java");
               outputFile.createNewFile();
               PrintWriter pw = new PrintWriter(outputFile);
               int maxValue = 0;
             
               pw.println("package edu.nps.moves.disenum;");
               pw.println();
               pw.println("import java.util.HashMap;");
               pw.println("import edu.nps.moves.siso.EnumNotFoundException;");
               pw.println();
               pw.println("/** Enumeration values for " + enumerationName);
               pw.println(" * The enumeration values are generated from the SISO DIS XML EBV document (R35), which was");
               pw.println(" * obtained from http://discussions.sisostds.org/default.asp?action=10&fd=31<p>");
               pw.println(" *");
               pw.println(" * Note that this has two ways to look up an enumerated instance from a value: a fast");
               pw.println(" * but brittle array lookup, and a slower and more garbage-intensive, but safer, method.");
               pw.println(" * if you want to minimize memory use, get rid of one or the other.<p>");
               pw.println(" *");
               pw.println(" * Copyright 2008-2009. This work is licensed under the BSD license, available at");
               pw.println(" * http://www.movesinstitute.org/licenses<p>");
               pw.println(" *");
               pw.println(" * @author DMcG, Jason Nelson");
               pw.println(" */");
               pw.println();
              pw.flush();
               
               pw.println("public enum " + enumerationName + " \n{\n");
            
              BigInteger val = anEnumeration.getId();
            
            boolean ofInterest = false;
            if(val.intValue() == 83)
                ofInterest = true;
               
               // enumeration names we have used so far
               Set enumNamesUsed = new HashSet();
               int usedCount = 1;
               
               List<EnumrowT> l = anEnumeration.getEnumrow();
               for(int idx = 0; idx < l.size(); idx++)
               {
                  EnumrowT er = l.get(idx);
                  
                  // Some entries in the EBV have missing descriptions. If that's the case, we just
                  // make up a description; we may get something on the wire with that enumerated
                  // value, and we want a valid enumeration object to match that, even if we don't
                  // have a good name or description for it.
                  String description = er.getDescription();
                  if((description == null) || description.equals(""))
                      description = "Missing Description";
                  String enumName = this.enumifyString(description);
                  int enumValue = (int)er.getId();
                                   
                  // If we've seen this enumeration name before, add some exra text
                  // onto the end to make it unique
                  if(enumNamesUsed.contains(enumName))
                  {
                      enumName = enumName + "_" + usedCount; // eg M1_RIFLE to M1_RIFLE_1
                      usedCount++;
                  }
                  enumNamesUsed.add(enumName);
                  
                  // Remove embedded quotes from the description, screws up generated code
                  description = description.replace("\"", "");
 
                  
                  if(maxValue < enumValue)
                  {
                      maxValue = enumValue;
                  }
                  
                  pw.print("    " + enumName + "(" + enumValue + ", " + "\"" + description + "\")" ) ;
                  if(idx != l.size() -1)
                  {
                      pw.print(",");
                  }
                  else
                  {
                      pw.print(";");
                  }
                  pw.println();
                  
              }
             pw.println();
            pw.flush();
             
             pw.println("    /** The enumerated value */");
             pw.println("    public final int value;");
             pw.println("");
             pw.println("    /** Text/english description of the enumerated value */");
             pw.println("    public final String description;");
             pw.println();
             
             pw.println("/** This is an array, with each slot corresponding to an enumerated value. This is a fast but brittle way to look up");
             pw.println(" * enumerated values. If there is no enumeration corresponding to the value it will fail, and it will also fail if the");
             pw.println(" * index it out of range of the array. But it is fast, and generates less garbage than the alternative of using ");
             pw.println(" * getEnumerationForValue(). It should be used only in real-time environments, and be careful even then.<p>");
             pw.println(" * Use as " + enumerationName + ".lookup[aVal] to get the enumeration that corresponds to a value.<p>");
             pw.println(" * In non-realtime environments, the prefered method is getEnumerationForValue().");
             pw.println(" */");
             maxValue++;
             pw.println("static public " + enumerationName + " lookup[] = new " + enumerationName + "[" + maxValue + "];");
             pw.println();
             
             pw.println("static private HashMap<Integer, " + enumerationName + ">enumerations = new HashMap<Integer, " + enumerationName + ">();");
             pw.println();
             
             
             pw.println("/* initialize the array and hash table at class load time */");
             pw.println("static \n{");
             pw.println("    for(" + enumerationName + " anEnum:" +  enumerationName + ".values())");
             pw.println("    {");
             pw.println("        lookup[anEnum.value] = anEnum;");
             pw.println("        enumerations.put(new Integer(anEnum.getValue()), anEnum);");
             pw.println("    }");
             pw.println("}\n");
             
             pw.println("/** Constructor */");
             pw.println(enumerationName + "(int value, String description)");
             pw.println("{");
             pw.println("    this.value = value;");
             pw.println("    this.description = description;");
             pw.println("}");
             
             pw.println();
             pw.println("/** Returns the string description associated with the enumerated instance with this value. ");
             pw.println(" * If there is no enumerated instance for this value, the string Invalid enumeration: <val> is returned.");
             pw.println("*/");
             pw.println("static public String getDescriptionForValue(int aVal)");
             pw.println("{");
             pw.println("  String desc;");
             pw.println();
             pw.println("    " + enumerationName + " val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("        desc = \"Invalid enumeration: \" + (new Integer(aVal)).toString();");
             pw.println("      else");
             pw.println("         desc = val.getDescription();");
             pw.println();
             pw.println("      return desc;");
             pw.println("}");
 
             pw.println();
             pw.println("/** Returns the enumerated instance with this value. ");
             pw.println(" * If there is no enumerated instance for this value, the exception is thrown.");
             pw.println("*/");
             pw.println("static public " + enumerationName + " getEnumerationForValue(int aVal) throws EnumNotFoundException");
             pw.println("{");
             pw.println("    " + enumerationName + " val;");
             pw.println("      val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("         throw new EnumNotFoundException(\"no enumeration found for value \" + aVal + \" of enumeration " + enumerationName + "\");");
             pw.println("      return val;");
             pw.println("}");
 
             pw.println();
             pw.println("/** Returns true if there is an enumerated instance for this value, false otherwise. ");
             pw.println("*/");
             pw.println("static public boolean enumerationForValueExists(int aVal)");
             pw.println("{");
             pw.println("    " + enumerationName + " val;");
             pw.println("      val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("         return false;");
             pw.println("      return true;");
             pw.println("}");
                        
 
             pw.println();
             pw.println("/** Returns the enumerated value for this enumeration */");
             pw.println("public int getValue()");
             pw.println("{");
             pw.println("  return value;");
             pw.println("}");
             pw.println();
             
             pw.println();
             pw.println("/** Returns a text descriptioni for this enumerated value. This is usually used as the basis for the enumeration name. */");
             pw.println("public String getDescription()");
             pw.println("{");
             pw.println("  return description;");
             pw.println("}");
             pw.println();
               
               pw.println("}");
             
           
               pw.flush();
               pw.close();
         }
         catch(Exception e)
         {
            System.out.println("writeStandard exception:" + e);
         }
         
     }
     
     /** Special case of country enumeration--we also want the two character internet code
      * for the country, eg "US", "UK", "FR", etc. The lookup algorithm for finding the
      * country is not perfect.
      * 
      * @param enumerationName
      * @param enumerationFile
      * @param anEnumeration
      */
     private void writeCountryEnumeration(String enumerationName, String enumerationFile, EnumT anEnumeration)
     {
         try
         {
               File outputFile = new File(enumerationFile + ".java");
               outputFile.createNewFile();
               PrintWriter pw = new PrintWriter(outputFile);
               int maxValue = 0;
               
               // Properties file containing the key (two character internet domain name for
               // the country) and the value (text description of the country). 
               Properties internetCountries = new Properties();
               FileInputStream fis = new FileInputStream(new File("data/countryCodes.properties"));
               internetCountries.load(fis);
               
             
               pw.println("package edu.nps.moves.disenum;");
               pw.println();
                pw.println("import java.util.HashMap;");
               pw.println("import edu.nps.moves.siso.EnumNotFoundException;");
               pw.println();
               pw.println("/** Enumeration values for " + enumerationName);
               pw.println(" * The enumeration values are generated from the SISO DIS XML EBV document (R35), which was");
               pw.println(" * obtained from http://discussions.sisostds.org/default.asp?action=10&fd=31<p>");
               pw.println(" *");
               pw.println(" * Note that this has two ways to look up an enumerated instance from a value: a fast");
               pw.println(" * but brittle array lookup, and a slower and more garbage-intensive, but safer, method.");
               pw.println(" * if you want to minimize memory use, get rid of one or the other.<p>");
               pw.println(" *");
               pw.println(" * Copyright 2008-2009. This work is licensed under the BSD license, available at");
               pw.println(" * http://www.movesinstitute.org/licenses<p>");
               pw.println(" *");
               pw.println(" * @author DMcG, Jason Nelson");
               pw.println(" */");
               pw.println();
               
               pw.println("public enum " + enumerationName + " \n{\n");
               
               List<EnumrowT> l = anEnumeration.getEnumrow();
               for(int idx = 0; idx < l.size(); idx++)
               {
                  EnumrowT er = l.get(idx);
                  
                  String description = er.getDescription();
                  String enumName = this.enumifyString(description);
                  int enumValue = (int)er.getId();
                  String internetDomainCode = "Unknown";
                  
                  
                  Set entrySet = internetCountries.entrySet();
                  Iterator it = entrySet.iterator();
                  while(it.hasNext())
                  {
                      Map.Entry<String, String> anEntry = (Map.Entry<String, String>)it.next();
                      if(anEntry.getValue().equalsIgnoreCase(description))
                      {
                          internetDomainCode = anEntry.getKey();
                          break;
                      }
                  }
                  
                  // Keep track of this to find max index for lookup array
                  if(maxValue < enumValue)
                  {
                      maxValue = enumValue;
                  }
                  
                  pw.print("    " + enumName + "(" + enumValue + ", " + "\"" + description + "\"" + ", " + "\"" + internetDomainCode + "\")") ;
                  if(idx != l.size() -1)
                  {
                      pw.print(",");
                  }
                  else
                  {
                      pw.print(";");
                  }
                  pw.println();
                  
              }
             pw.println();
             
             pw.println("    /** The enumerated value */");
             pw.println("    public final int value;");
             pw.println("");
             pw.println("    /** Text/english description of the enumerated value */");
             pw.println("    public final String description;");
             pw.println();
             pw.println("    /** Internet domain code (US, FR, UK, CA, etc). This is a guess for many countries */");
             pw.println("    public final String internetDomainCode;");
             pw.println();
             
             pw.println("/** This is an array, with each slot corresponding to an enumerated value");
             pw.println(" * and that slot containing the enumerated object. Use to look up enumerated object when you have the value");
             pw.println(" */");
             maxValue++;
             pw.println("static public " + enumerationName + " lookup[] = new " + enumerationName + "[" + maxValue + "];");
             pw.println("static private HashMap<Integer, " + enumerationName + ">enumerations = new HashMap<Integer, " + enumerationName + ">();");
             pw.println();
 
 
             pw.println("/* initialize the array and hash table at class load time */");
             pw.println("static \n{");
             pw.println("    for(" + enumerationName + " anEnum:" +  enumerationName + ".values())");
             pw.println("    {");
             pw.println("        lookup[anEnum.value] = anEnum;");
             pw.println("        enumerations.put(new Integer(anEnum.getValue()), anEnum);");
             pw.println("    }");
             pw.println("}\n");
             
             pw.println("/** Constructor */");
             pw.println(enumerationName + "(int value, String description, String internetDomainCode)");
             pw.println("{");
             pw.println("    this.value = value;");
             pw.println("    this.description = description;");
             pw.println("    this.internetDomainCode = internetDomainCode;");
             pw.println("}");
 
              pw.println();
             pw.println("/** Returns the string description associated with the enumerated instance with this value. ");
             pw.println(" * If there is no enumerated instance for this value, the string Invalid enumeration: <val> is returned.");
             pw.println("*/");
             pw.println("static public String getDescriptionForValue(int aVal)");
             pw.println("{");
             pw.println("  String desc;");
             pw.println();
             pw.println("    " + enumerationName + " val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("        desc = \"Invalid enumeration: \" + (new Integer(aVal)).toString();");
             pw.println("      else");
             pw.println("         desc = val.getDescription();");
             pw.println();
             pw.println("      return desc;");
             pw.println("}");
 
             pw.println();
             pw.println("/** Returns the enumerated instance with this value. ");
             pw.println(" * If there is no enumerated instance for this value, the exception is thrown.");
             pw.println("*/");
             pw.println("static public " + enumerationName + " getEnumerationForValue(int aVal) throws EnumNotFoundException");
             pw.println("{");
             pw.println("    " + enumerationName + " val;");
             pw.println("      val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("         throw new EnumNotFoundException(\"no enumeration found for value \" + aVal + \" of enumeration " + enumerationName + "\");");
             pw.println("      return val;");
             pw.println("}");
 
             pw.println();
             pw.println("/** Returns true if there is an enumerated instance for this value, false otherwise. ");
             pw.println("*/");
             pw.println("static public boolean enumerationForValueExists(int aVal)");
             pw.println("{");
             pw.println("    " + enumerationName + " val;");
             pw.println("      val = enumerations.get(new Integer(aVal));");
             pw.println("      if(val == null)");
             pw.println("         return false;");
             pw.println("      return true;");
             pw.println("}");
             
             pw.println();
             pw.println("/** Returns the enumerated value for this enumeration */");
             pw.println("public int getValue()");
             pw.println("{");
             pw.println("  return value;");
             pw.println("}");
             
             pw.println();
             pw.println("/** Returns the string description for this enumeration. */");
             pw.println("public String getDescription()");
             pw.println("{");
             pw.println("  return description;");
             pw.println("}");
             pw.println();
             
             pw.println("/** As an alternative, returns the internet domain code for this country, if it exists. */");
             pw.println("public String getInternetDomainCode()");
             pw.println("{");
             pw.println("  return internetDomainCode;");
             pw.println("}");
             pw.println();
               
               pw.println("}");
             
           
               pw.flush();
               pw.close();
         }
         catch(Exception e)
         {
             System.out.println();
         }
         
     }
     
     /**
      *  Changes an input string like "Entity State PDU" into "ENTITY_STATE_PDU"
      * 
      * @param text
      * @return
      */
     public String enumifyString(String text)
     {
         String enumValue = text.trim();
         enumValue = enumValue.toUpperCase();
         enumValue = enumValue.replace(" ", "_");
         enumValue = enumValue.replace("-", "_");
         enumValue = enumValue.replace("/", "_");
         enumValue = enumValue.replace("(", "");
         enumValue = enumValue.replace(")", "");
         enumValue = enumValue.replace(",", "");
         enumValue = enumValue.replace("'", "");
         enumValue = enumValue.replace("\"", "");
         enumValue = enumValue.replace(".", "");
         enumValue = enumValue.replace(";", "");
         enumValue = enumValue.replace(":", "");
         enumValue = enumValue.replace("&", "");
         enumValue = enumValue.replace("{", "_");
         enumValue = enumValue.replace("}", "_");
         enumValue = enumValue.replace("#", "_");
         enumValue = enumValue.replace("^", "_CARET_");
         enumValue = enumValue.replace("<", "_LT_");
         enumValue = enumValue.replace(">", "_GT_");
         
         
         // If it starts with a number, that's not a valid identifier. 
         // replace it with a leading character. Ack--there should be
         // a better regexp to cover this, but getting back references
         // working is not working for me.
         
         enumValue = enumValue.replaceAll("^0", "X_0");
         enumValue = enumValue.replaceAll("^1", "X_1");
         enumValue = enumValue.replaceAll("^2", "X_2");
         enumValue = enumValue.replaceAll("^3", "X_3");
         enumValue = enumValue.replaceAll("^4", "X_4");
         enumValue = enumValue.replaceAll("^5", "X_5");
         enumValue = enumValue.replaceAll("^6", "X_6");
         enumValue = enumValue.replaceAll("^7", "X_7");
         enumValue = enumValue.replaceAll("^8", "X_8");
         enumValue = enumValue.replaceAll("^9", "X_9");
         
         // Finally, replace repeated instances of _ with a single underscore
         enumValue = enumValue.replaceAll("(_+)", "_");
         
         return enumValue;
     }
     
     
 
 }
