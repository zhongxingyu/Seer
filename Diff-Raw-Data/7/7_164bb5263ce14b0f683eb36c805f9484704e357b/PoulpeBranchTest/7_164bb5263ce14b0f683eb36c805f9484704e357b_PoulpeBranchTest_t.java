 package org.jtalks.poulpe.model.entity;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author stanislav bashkirtsev
  */
 public class PoulpeBranchTest {
     @Test(dataProvider = "provideBranchWithSection")
     public void testMoveTo_branchChangesItsSection(PoulpeBranch branch) throws Exception {
         PoulpeSection moveFrom = branch.getPoulpeSection();
         PoulpeSection addTo = new PoulpeSection("section2");
 
         assertSame(branch.moveTo(addTo), moveFrom);
        assertSame(branch.getPoulpeSection(), addTo);
         assertNotSame(moveFrom, addTo);
     }
 
     @Test(dataProvider = "provideBranchWithSection")
     public void testMoveTo_sectionUpdatesItsBranches(PoulpeBranch branch) throws Exception {
         PoulpeSection addTo = new PoulpeSection("section2");
 
         branch.moveTo(addTo);
         assertTrue(addTo.containsBranch(branch));
     }
 
     @Test(dataProvider = "provideBranchWithSection")
     public void testMoveTo_sameSection(PoulpeBranch branch) throws Exception {
         PoulpeSection addTo = branch.getPoulpeSection();
 
         branch.moveTo(addTo);
         assertSame(branch.getPoulpeSection(), addTo);
         assertTrue(addTo.containsBranch(branch));
     }
 
     @DataProvider
     public Object[][] provideBranchWithSection() {
         PoulpeBranch branch = new PoulpeBranch("test-branch", "test-description");
         PoulpeSection section = new PoulpeSection("test-section", "test-description");
         section.addBranchIfAbsent(branch);
         return new Object[][]{{branch}};
     }
 }
