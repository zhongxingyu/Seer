 package org.provoysa12th.directory.service;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.util.List;
 import java.util.UUID;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.provoysa12th.directory.domain.Organization;
 import org.provoysa12th.directory.domain.Unit;
 import org.provoysa12th.directory.domain.Unit.Type;
 import org.provoysa12th.directory.domain.UnitOrganization;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration(classes = {ServiceComponentTestConfiguration.class})
 @RunWith(SpringJUnit4ClassRunner.class)
 public class UnitServiceImplComponentTest {
 
 	@Autowired
 	UnitService unitService;
 
 	@Autowired
 	OrganizationService organizationService;
 
 	private Unit testUnit() {
 		Unit newUnit = new Unit();
 		newUnit.setName("Test Unit");
 		newUnit.setType(Type.Ward);
 		newUnit.setUnitNumber(1234);
 		return newUnit;
 	}
 
 	@Test
 	public void testFindAll() throws Exception {
 		unitService.createOrUpdate(new Unit());
 
 		List<Unit> units = unitService.findAll();
 		assertThat(units, is(notNullValue()));
 		assertThat(units, is(not(empty())));
 	}
 
 	@Test
 	public void testFindById() throws Exception {
 		Unit unit = unitService.createOrUpdate(new Unit());
 
 		Unit actual = unitService.findById(unit.getNodeId());
 		assertThat(actual, is(notNullValue()));
 	}
 
 	@Test
 	public void testFindById_notFound() throws Exception {
 		Unit actual = unitService.findById(1234L);
 		assertThat(actual, is(nullValue()));
 	}
 
 	@Test
 	public void testFindByUnitNumber() throws Exception {
 		Unit newUnit = new Unit();
		newUnit.setUnitNumber(13);
 
 		unitService.createOrUpdate(newUnit);
 
		Unit actual = unitService.findByUnitNumber(13);
 		assertThat(actual, is(notNullValue()));
 	}
 
 	@Test
 	public void testFindByUUID() throws Exception {
 		Unit unit = unitService.createOrUpdate(new Unit());
 
 		UUID uuid = unit.getUuid();
 
 		Unit actual = unitService.findByUUID(uuid);
 		assertThat(actual, is(equalTo(unit)));
 	}
 
 	@Test
 	public void testFindByUUID_notFound() throws Exception {
 		Unit actual = unitService.findByUUID(UUID.randomUUID());
 		assertThat(actual, is(nullValue()));
 	}
 
 	@Test
 	public void testFindByUnitNumber_notFound() throws Exception {
 		Unit actual = unitService.findByUnitNumber(3456);
 		assertThat(actual, is(nullValue()));
 	}
 
 	@Test
 	public void testCreateOrSave() throws Exception {
 		Unit newUnit = testUnit();
 
 		Unit actual = unitService.createOrUpdate(newUnit);
 		assertThat(actual, is(notNullValue()));
 		assertThat(actual, equalTo(newUnit));
 	}
 
 	@Test
 	public void testAddOrganization() throws Exception {
 		Unit newUnit = testUnit();
 		Unit unit = unitService.createOrUpdate(newUnit);
 
 		Organization newOrganization = new Organization();
 		newOrganization = organizationService.createOrUpdate(newOrganization);
 
 		UnitOrganization unitOrganization = unitService.addOrganization(unit, newOrganization, false, -1);
 		unit = unitOrganization.getUnit();
 
 		assertThat(unit.getUnitOrganizations(), is(not(empty())));
 
 		for(UnitOrganization unitOrg : unit.getUnitOrganizations()) {
 			Organization organization = unitOrg.getOrganization();
 			assertThat(organization, is(notNullValue()));
 			assertThat(organization.getNodeId(), is(notNullValue()));
 			assertThat(organization.getUnit(), is(unit));
 			assertThat(organization.getUnitOrganization().getUnit(), is(unit));
 		}
 	}
 
 	@Test
 	public void testAddOrganization_nonPersistentOrganization() throws Exception {
 		Unit newUnit = testUnit();
 		Unit unit = unitService.createOrUpdate(newUnit);
 
 		Organization newOrganization = new Organization();
 
 		UnitOrganization unitOrganization = unitService.addOrganization(unit, newOrganization, false, -1);
 		unit = unitOrganization.getUnit();
 
 		assertThat(unit.getUnitOrganizations(), is(not(empty())));
 
 		for(UnitOrganization unitOrg : unit.getUnitOrganizations()) {
 			Organization organization = unitOrg.getOrganization();
 			assertThat(organization, is(notNullValue()));
 			assertThat(organization.getNodeId(), is(notNullValue()));
 			assertThat(organization.getUnit(), is(unit));
 			assertThat(organization.getUnitOrganization().getUnit(), is(unit));
 		}
 	}
 
 }
