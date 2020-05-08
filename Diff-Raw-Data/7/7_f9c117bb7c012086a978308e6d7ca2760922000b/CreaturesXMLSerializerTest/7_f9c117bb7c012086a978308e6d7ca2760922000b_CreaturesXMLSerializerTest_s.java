 /*
  * Copyright (c) 2010-2011, Dmitry Sidorenko. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ru.sid0renk0.dwarfguide;
 
 import org.junit.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.sid0renk0.dwarfguide.model.Creature;
 import ru.sid0renk0.dwarfguide.model.Creatures;
 import ru.sid0renk0.dwarfguide.model.TraitInstance;
 import ru.sid0renk0.dwarfguide.model.configuration.DFHackConfiguration;
 import ru.sid0renk0.dwarfguide.model.configuration.Sex;
 
 import java.io.InputStream;
 import java.util.Calendar;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.*;
 
 /**
  * @author Dmitry Sidorenko
  */
 public class CreaturesXMLSerializerTest {
     @SuppressWarnings({"unused"})
     private static final Logger LOGGER = LoggerFactory.getLogger(CreaturesXMLSerializerTest.class);
 
 
     @Test
     public void testDeserialize() throws Exception {
         InputStream configXML = this.getClass().getClassLoader().getResourceAsStream("Memory.xml");
         InputStream dwarvesXML = this.getClass().getClassLoader().getResourceAsStream("TestDwarves.xml");
 
         DFHackConfiguration configuration = DFHackConfiguration.deserialize(configXML);
         Creatures dwarves = CreaturesXMLSerializer.deserialize(dwarvesXML, configuration.getBaseByVersion("DF2010"));
 
 
         assertThat(dwarves.getCreatures(), notNullValue());
         assertThat(dwarves.getCreatures().size(), is(110));
 
         Creature dwarfBerMedenoddom = dwarves.getCreatures().get(0);
 
         assertThat(dwarfBerMedenoddom.getName(), is("Ber Medenoddom"));
         assertThat(dwarfBerMedenoddom.getEnglishName(), is("Ber Tribecloister"));
         assertThat(dwarfBerMedenoddom.getNickname(), is("Manager"));
        assertThat(dwarfBerMedenoddom.getHappiness(), is(138));
         assertThat(dwarfBerMedenoddom.getProfession().getName(), is("ADMINISTRATOR"));
         assertThat(dwarfBerMedenoddom.getSex(), is(Sex.FEMALE));
 
         {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(dwarfBerMedenoddom.getBirthday());
 
             assertThat(calendar.get(Calendar.DAY_OF_MONTH), is(7));
             assertThat(calendar.get(Calendar.MONTH), is(9));
             assertThat(calendar.get(Calendar.YEAR), is(990));
         }
         assertThat(dwarfBerMedenoddom.getAge(), is(65));
         assertThat(dwarfBerMedenoddom.getCustomProfession(), is(""));
 
         assertThat(dwarfBerMedenoddom.getSkills().get(1).getSkill().getId(), is(24));
         assertThat(dwarfBerMedenoddom.getSkills().get(1).getExperience(), is(40));
 
        assertThat(dwarfBerMedenoddom.getMood().getId(), is(0));
        assertThat(dwarfBerMedenoddom.getMoodSkill().getId(), is(36));
 
         assertThat(dwarfBerMedenoddom.getTraits().size(), is(12));
 
         TraitInstance traitInstance = dwarfBerMedenoddom.getTraits().get(0);
         assertThat(traitInstance.getLevel(), is(2));
         assertThat(traitInstance.getValue(), is(40));
         assertThat(traitInstance.getTrait().getId(), is(1));
 
     }
 }
