 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
  *
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  *
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  *
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package rslb2.blockadeloader;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 import rescuecore2.log.Logger;
 import rescuecore2.messages.control.KSCommands;
 import rescuecore2.standard.components.StandardSimulator;
 import rescuecore2.standard.entities.Blockade;
 import rescuecore2.standard.entities.Road;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.worldmodel.ChangeSet;
 import rescuecore2.worldmodel.EntityID;
 
 /**
  * Main class of the blockades loader simulator.
 *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class BlockadeLoader extends StandardSimulator {
 
     /** Key of the configuration setting that specifies which scenario to run */
     public final String KEY_SCENARIO = "gis.map.scenario";
 
     @Override
     public void postConnect() {
         super.postConnect();
         Logger.info("connected");
     }
 
     @Override
     protected void processCommands(KSCommands c, ChangeSet changes) {
         long start = System.currentTimeMillis();
         int time = c.getTime();
         Logger.info("Timestep " + time);
 
         if (time == 1) {
             deployBlockades(changes);
         }
 
         long end = System.currentTimeMillis();
         Logger.info("Timestep " + time + " took " + (end - start) + " ms");
     }
 
     /**
      * Create the blockades as specified in the scenario file.
      *
      * @param changes changeset where blockades will be added to notify the
      *                kernel about their existence.
      */
     private void deployBlockades(ChangeSet changes) {
         Logger.warn("Scenario: " + config.getValue(KEY_SCENARIO));
         Document scenario = loadDocument();
 
         ArrayList<BlockadeData> blockades = new ArrayList<>();
         for (Object node : scenario.selectNodes("//scenario:scenario/scenario:blockade")) {
             if (node instanceof Element) {
                 blockades.add(new BlockadeData((Element)node));
             }
         }
 
         try {
             List<EntityID> idList = requestNewEntityIDs(blockades.size());
             Iterator<EntityID> ids = idList.iterator();
             for (BlockadeData bd : blockades) {
                 createBlockade(changes, bd, ids.next());
             }
         } catch (InterruptedException ex) {
             Logger.fatal(ex.getLocalizedMessage(), ex);
         }
     }
 
     /**
      * Creates a new blockade and introduces it in the simulation.
      *
      * @param changes ChangeSet where the new blockade will be introduced, to
      *                notify the kernel of the blockade creation.
      * @param bd information about the blockade to create.
      * @param id identifier to give to the new blockade (must be obtained from
      *           the kernel).
      */
     private void createBlockade(ChangeSet changes, BlockadeData bd, EntityID id) {
 
         StandardEntity entity = model.getEntity(bd.location);
         if (entity == null) {
             Logger.fatal("Error loading blockades: location " + bd.location + " does not exist.");
             return;
         }
         if (!(entity instanceof Road)) {
             Logger.fatal("Error loading blockades: location " + bd.location + " is not a road.");
             return;
         }
 
         Road road = (Road)entity;
 
         // Instantiate the blockade
         Blockade blockade = new Blockade(id);
         blockade.setApexes(road.getApexList());
         blockade.setX(road.getX());
         blockade.setY(road.getY());
         blockade.setRepairCost(bd.cost);
         blockade.setPosition(road.getID());
 
         // Report the addition to the kernel
        List<EntityID> roadBlockades = new ArrayList<>();
         roadBlockades.add(blockade.getID());
         road.setBlockades(roadBlockades);
         changes.addChange(road, road.getBlockadesProperty());
         changes.addAll(Arrays.asList(new Blockade[]{blockade}));
     }
 
     /**
      * Loads the scenario file as a DOM document.
      *
      * @return the DOM Document representation of the scenario file.
      */
     private Document loadDocument() {
         SAXReader reader = new SAXReader();
         try {
             Document document = reader.read(getScenarioPath());
             return document;
         } catch (DocumentException ex) {
             Logger.fatal(ex.getLocalizedMessage(), ex);
         }
         return null;
     }
 
     /**
      * Retrieve the absolute path to the scenario file specified when calling
      * the simulator.
      *
      * @return absolute path to the scenario file.
      */
     private String getScenarioPath() {
         // We need to remove the two first back-directories if its relative
         String path = config.getValue(KEY_SCENARIO);
         if (path.startsWith(".")) {
             path = path.substring(path.indexOf('/')+1);
             path = path.substring(path.indexOf('/')+1);
         }
         Logger.trace("Loading scenario: " + path);
         return path;
     }
 
     /**
      * Holder class to carry the details of a blockade.
      */
     private class BlockadeData {
         private EntityID location;
         private int cost;
 
         public BlockadeData(Element node) {
             location = new EntityID(Integer.valueOf(node.attribute("location").getValue()));
             cost = Integer.valueOf(node.attribute("cost").getValue());
         }
     }
 
 }
