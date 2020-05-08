 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.test.fixtures;
 
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.lang3.RandomStringUtils;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.entity.Property;
 import org.jtalks.common.model.entity.Rank;
 import org.jtalks.poulpe.model.entity.*;
 
 import com.google.common.collect.Lists;
 
 /**
  * Provides unified way for creating text fixtures.<br>
  * <br>
  * 
  * This class is not under test source folder because it should be accessible for all components.
  * 
  * @author Kirill Afonin
  * @author Alexey Grigorev
  */
 public final class TestFixtures {
 
     private static final Random RANDOM = new Random();
 
     public static PoulpeBranch branch() {
         PoulpeBranch newBranch = new PoulpeBranch(random(), random());
         newBranch.setSection(section());
         newBranch.setModeratorsGroup(group());
         return newBranch;
     }
 
     public static TopicType topicType() {
         return new TopicType(random(), random());
     }
 
     public static Component component(ComponentType type) {
         BaseComponent base = new BaseComponent(type);
         return base.newComponent(random(), random());
     }
     
     public static List<Component> allComponents() {
         List<Component> result = Lists.newArrayList();
         
         for (ComponentType componentType : ComponentType.values()) {
             result.add(component(componentType));
         }
         
         return result;
     }
 
     public static Jcommune jcommune() {
         return (Jcommune) component(ComponentType.FORUM);
     }
 
     public static Jcommune jcommuneWithSections(int sectionsAmount) {
         Jcommune jcommune = jcommune();
 
         for (int i = 0; i < sectionsAmount; i++) {
             PoulpeSection section = sectionWithBranches();
             jcommune.addSection(section);
         }
 
         return jcommune;
     }
     
     public static Jcommune jcommuneWithSections() {
         return jcommuneWithSections(10);
     }
 
     public static Component randomComponent() {
         return component(randomComponentType());
     }
 
     public static ComponentType randomComponentType() {
         ComponentType[] types = ComponentType.values();
         return types[randomInt(types.length)];
     }
     
     public static BaseComponent baseComponent() {
         return new BaseComponent(randomComponentType());
     }
 
     public static PoulpeSection sectionWithBranches() {
        return sectionWithBranches(randomInt(10));
     }
 
     public static Property property() {
         Property property = new Property(random(), random());
         property.setValidationRule(random());
         return property;
     }
     
     public static PoulpeSection sectionWithBranches(int branchesAmount) {
         PoulpeSection section = new PoulpeSection(random());
 
         for (int i = 0; i < branchesAmount; i++) {
             PoulpeBranch branch = branch();
             branch.setSection(section);
             section.addOrUpdateBranch(branch);
         }
 
         return section;
     }
 
     public static PoulpeSection section() {
         return new PoulpeSection(random());
     }
 
     public static PoulpeUser user(String username) {
         String email = username + "@" + random() + ".com";
         return new PoulpeUser(username, email, random(), "");
     }
 
     public static PoulpeUser user() {
         return user(random());
     }
 
     public static List<PoulpeUser> bannedUsersListOf(int n) {
         List<PoulpeUser> users = usersListOf(n);
 
         for (PoulpeUser user : users) {
             user.setBanReason("anyBanReason");
         }
 
         return users;
     }
 
     public static List<PoulpeUser> usersListOf(int n) {
         List<PoulpeUser> result = Lists.newArrayListWithCapacity(n);
 
         while (n > 0) {
             result.add(user());
             n--;
         }
 
         return result;
     }
 
     public static Group group() {
         return new Group(random(), random());
     }
 
     public static Rank rank() {
         return new Rank(random(), randomInt(1000));
     }
 
     private static String random() {
         return RandomStringUtils.randomAlphanumeric(10);
     }
 
     private static int randomInt(int max) {
         return RANDOM.nextInt(max);
     }
 
 }
