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
 package org.jtalks.poulpe.web.controller.branch;
 
 import com.google.common.collect.Lists;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.permissions.BranchPermission;
 import org.jtalks.poulpe.model.dto.PermissionsMap;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.service.BranchService;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.ZkHelper;
 import org.jtalks.poulpe.web.controller.utils.ObjectsFactory;
 import org.jtalks.poulpe.web.controller.zkmacro.PermissionManagementBlock;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 /**
  * Tests for {@link BranchPermissionManagementVm}.
  *
  * @author Vyacheslav Zhivaev
  * @author Maxim Reshetov
  */
 public class BranchPermissionManagementVmTest {
 
 	private static final String MANAGE_GROUPS_DIALOG_ZUL = "WEB-INF/pages/forum/EditGroupsForBranchPermission.zul";
 
 	// context related
 	@Mock
 	private WindowManager windowManager;
 	@Mock
 	private BranchService branchService;
 	@Mock
 	ZkHelper zkHelper;
 
 	// SUT
 	private BranchPermissionManagementVm sut;
 
 
 	@BeforeMethod
 	public void beforeMethod() {
 		MockitoAnnotations.initMocks(this);
 		PoulpeBranch branch = new PoulpeBranch("branch");
 
 		SelectedEntity<Object> selectedEntity = new SelectedEntity<Object>();
 		selectedEntity.setEntity(branch);
 
 		sut = new BranchPermissionManagementVm(windowManager, branchService, selectedEntity);
 		sut.setZkHelper(zkHelper);
 	}
 
 	/**
 	 * Check that dialog really opens.
 	 */
 	@Test(dataProvider = "provideTypeOfPermissionsToBranch")
 	public void testShowGroupsDialog(BranchPermission permission) {
 		sut.showGroupsDialog(permission, "allow");
 		sut.showGroupsDialog(permission, "restrict");
 
 		verify(windowManager, times(2)).open(MANAGE_GROUPS_DIALOG_ZUL);
 	}
 
 	@Test(expectedExceptions = {IllegalArgumentException.class}, dataProvider = "provideTypeOfPermissionsToBranch")
 	public void testShowGroupsDialog_IllegalFormat(BranchPermission permission) {
 		sut.showGroupsDialog(permission, "HERE_ILLEGAL_FORMATTED_STRING");
 		verify(windowManager, never()).open(MANAGE_GROUPS_DIALOG_ZUL);
 	}
 
 
 	/**
 	 * Check method for generate data of view.
 	 */
 	@Test(dataProvider = "provideInitDataForView")
 	public void testInitDataForView(PermissionsMap<BranchPermission> permissionsMap, Group allowedGroup, Group restrictedGroup) {
 		PoulpeBranch branch = (PoulpeBranch) sut.getSelectedEntity().getEntity();
 		when(branchService.getPermissionsFor(branch)).thenReturn(permissionsMap);
 		sut.initDataForView();
 		assertEquals(sut.getBranch(), branch);
 
 		List<PermissionManagementBlock> blocks = sut.getBlocks();
		assertTrue(blocks.get(1).getAllowRow().getGroups().contains(allowedGroup));
		assertTrue(blocks.get(0).getRestrictRow().getGroups().contains(restrictedGroup));
 		assertTrue(blocks.size() == 2);
 	}
 
 
 	/*
 	* Data providers
 	*/
 	@DataProvider
 	public Object[][] provideTypeOfPermissionsToBranch() {
 		return new Object[][]{
 				{BranchPermission.CREATE_TOPICS},
 				{BranchPermission.CLOSE_TOPICS},
 				{BranchPermission.VIEW_TOPICS},
 				{BranchPermission.DELETE_TOPICS},
 				{BranchPermission.MOVE_TOPICS},
 				{BranchPermission.SPLIT_TOPICS},
 				{BranchPermission.CREATE_POSTS},
 				{BranchPermission.DELETE_OTHERS_POSTS},
 				{BranchPermission.DELETE_OWN_POSTS},
 
 		};
 	}
 
 	@DataProvider
 	public Object[][] provideInitDataForView() {
 
 		Group allowedGroup = ObjectsFactory.fakeGroup();
 		Group restrictedGroup = ObjectsFactory.fakeGroup();
 
 		List<PermissionManagementBlock> blocks = Lists.newArrayList();
 		BranchPermission allowedPermission = BranchPermission.CREATE_TOPICS;
 		BranchPermission restrictPermission = BranchPermission.CLOSE_TOPICS;
 
 		PermissionsMap<BranchPermission> permissionsMap = new PermissionsMap<BranchPermission>();
 		permissionsMap.addAllowed(allowedPermission, allowedGroup);
 		permissionsMap.addRestricted(restrictPermission, restrictedGroup);
 
 		for (BranchPermission permission : permissionsMap.getPermissions()) {
 			blocks.add(new PermissionManagementBlock(permission, permissionsMap, "allow", "restrict"));
 		}
 
 		return new Object[][]{
 				{permissionsMap, allowedGroup, restrictedGroup}
 
 		};
 	}
 
 
 }
