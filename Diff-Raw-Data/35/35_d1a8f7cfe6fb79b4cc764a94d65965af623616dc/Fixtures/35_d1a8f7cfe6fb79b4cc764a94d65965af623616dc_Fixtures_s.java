 /*******************************************************************************
  * Copyright (c) May 18, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.data.fixtures;
 
 import java.util.List;
 
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.netxforge.netxstudio.NetxstudioFactory;
 import com.netxforge.netxstudio.NetxstudioPackage;
 import com.netxforge.netxstudio.ServerSettings;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.generics.ExpansionDuration;
 import com.netxforge.netxstudio.generics.ExpansionDurationSetting;
 import com.netxforge.netxstudio.generics.ExpansionDurationValue;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.generics.Person;
 import com.netxforge.netxstudio.generics.Role;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Expression;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.Library;
 import com.netxforge.netxstudio.library.LibraryFactory;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.metrics.MetricRetentionPeriod;
 import com.netxforge.netxstudio.metrics.MetricRetentionRule;
 import com.netxforge.netxstudio.metrics.MetricRetentionRules;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.scheduling.Job;
 import com.netxforge.netxstudio.scheduling.JobState;
 import com.netxforge.netxstudio.scheduling.RetentionJob;
 import com.netxforge.netxstudio.scheduling.SchedulingFactory;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 
 /**
  * NetXStudio fixture data.
  * 
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  */
 public class Fixtures implements IFixtures {
 
 	private IDataProvider dataProvider;
 	private ModelUtils modelUtils;
 
 	public Fixtures(IDataProvider dataProvider, ModelUtils modelUtils) {
 		this.dataProvider = dataProvider;
 		this.modelUtils = modelUtils;
 	}
 
 	public void loadFixtures() {
 		loadSettings();
 		loadRoles();
 		loadRetentionRules();
 	}
 
 	private void loadSettings() {
 
 		final CDOResource settingsResource = (CDOResource) dataProvider
 				.getResource(NetxstudioPackage.Literals.SERVER_SETTINGS);
 		if (settingsResource.getContents().size() > 0) {
 			return;
 		}
 		ServerSettings serverSettings = NetxstudioFactory.eINSTANCE
 				.createServerSettings();
 		serverSettings
 				.setExportPath("/Users/dzonekl/Documents/Projects/NetXStudio/Reports");
 		serverSettings
 				.setImportPath("/Users/dzonekl/Documents/Projects/NetXStudio/TestData");
 		settingsResource.getContents().add(serverSettings);
 
 		ExpansionDurationSetting eds = GenericsFactory.eINSTANCE
 				.createExpansionDurationSetting();
 		{
 			ExpansionDurationValue edv = GenericsFactory.eINSTANCE
 					.createExpansionDurationValue();
 			edv.setExpansionDuration(ExpansionDuration.QUICK);
 			edv.setValue(7);
 			eds.setQuickDuration(edv);
 		}
 		{
 			ExpansionDurationValue edv = GenericsFactory.eINSTANCE
 					.createExpansionDurationValue();
 			edv.setExpansionDuration(ExpansionDuration.SHORT);
 			edv.setValue(30);
 			eds.setShortDuration(edv);
 		}
 		{
 			ExpansionDurationValue edv = GenericsFactory.eINSTANCE
 					.createExpansionDurationValue();
 			edv.setExpansionDuration(ExpansionDuration.MEDIUM);
 			edv.setValue(90);
 			eds.setMediumDuration(edv);
 		}
 		{
 			ExpansionDurationValue edv = GenericsFactory.eINSTANCE
 					.createExpansionDurationValue();
 			edv.setExpansionDuration(ExpansionDuration.LONG);
 			edv.setValue(180);
 			eds.setLongDuration(edv);
 		}
 
 		serverSettings.setExpansionDurationSettings(eds);
 
 	}
 
 	private void loadRetentionRules() {
 
 		Resource retentionRulesResource = dataProvider
 				.getResource(MetricsPackage.Literals.METRIC_RETENTION_RULES);
 		Resource expressionResource = dataProvider
 				.getResource(LibraryPackage.Literals.EXPRESSION);
 
 		EList<EObject> rulesContents = retentionRulesResource.getContents();
		
		// always clear rules and expressions for the rules. 
 		rulesContents.clear();
 
 		List<Expression> expToRemove = Lists.newArrayList();
 		for (EObject eo : expressionResource.getContents()) {
			Expression exp = (Expression) eo;
			if (exp.getName().endsWith("retention rule")) {
				expToRemove.add(exp);
 			}
 		}
		
 		expressionResource.getContents().removeAll(expToRemove);
		
 		Expression monthlyRetentionExpression;
 		Expression weeklyRetentionExpression;
 		Expression dailyRetentionExpression;
 		Expression hourlyRetentionExpression;
		
 		// if (contents.size() == 1) {
 		// return;
 		// } else {
 		MetricRetentionRules rules = MetricsFactory.eINSTANCE
 				.createMetricRetentionRules();
 		rulesContents.add(rules);
 		{
 
 			// TODO, Adapt to context.
 			{
 				// Monthly expression
 				monthlyRetentionExpression = LibraryFactory.eINSTANCE
 						.createExpression();
 				monthlyRetentionExpression.setName("Monthly retention rule");
 
 				// Gets the max value from a range and assigns it to
 				// another
 				// range, clears the original range.
 				final String eAsString = "this METRIC AVG MONTH = this METRIC AVG DAY.max();";
 				monthlyRetentionExpression.getExpressionLines().addAll(
 						modelUtils.expressionLines(eAsString));
 				expressionResource.getContents()
 						.add(monthlyRetentionExpression);
 			}
 			{
 				// Monthly expression
 				weeklyRetentionExpression = LibraryFactory.eINSTANCE
 						.createExpression();
 				weeklyRetentionExpression.setName("Weekly retention rule");
 
 				// Gets the max value from a range and assigns it to
 				// another
 				// range, clears the original range.
 				final String eAsString = "this METRIC AVG WEEK = this METRIC AVG DAY.max();";
 				weeklyRetentionExpression.getExpressionLines().addAll(
 						modelUtils.expressionLines(eAsString));
 				expressionResource.getContents().add(weeklyRetentionExpression);
 			}
 			{
 				dailyRetentionExpression = LibraryFactory.eINSTANCE
 						.createExpression();
 				dailyRetentionExpression.setName("Daily retention rule");
 
 				// Gets the max value from a range and assigns it to
 				// another
 				// range, clears the original range.
 				final String eAsString = "this METRIC AVG DAY = this METRIC AVG HOUR.max();";
 				dailyRetentionExpression.getExpressionLines().addAll(
 						modelUtils.expressionLines(eAsString));
 				expressionResource.getContents().add(dailyRetentionExpression);
 			}
 
 			{
 				hourlyRetentionExpression = LibraryFactory.eINSTANCE
 						.createExpression();
 				hourlyRetentionExpression.setName("Hourly retention rule");
 
 				// Gets the max value from a range and assigns it to
 				// another
 				// range, clears the original range.
 				final String eAsString = "this METRIC AVG DAY = this METRIC 15 .max();";
 				hourlyRetentionExpression.getExpressionLines().addAll(
 						modelUtils.expressionLines(eAsString));
 				expressionResource.getContents().add(hourlyRetentionExpression);
 			}
 		}
 
 		if (rules.getMetricRetentionRules().size() == 0) {
 			{
 				MetricRetentionRule r = MetricsFactory.eINSTANCE
 						.createMetricRetentionRule();
 				r.setName("Monthly values");
 				r.setPeriod(MetricRetentionPeriod.ALWAYS);
 				r.setRetentionExpression(monthlyRetentionExpression);
 				rules.getMetricRetentionRules().add(r);
 			}
 			{
 				MetricRetentionRule r = MetricsFactory.eINSTANCE
 						.createMetricRetentionRule();
 				r.setName("Weekly values");
 				r.setPeriod(MetricRetentionPeriod.ALWAYS);
 				r.setRetentionExpression(weeklyRetentionExpression);
 				rules.getMetricRetentionRules().add(r);
 			}
 			{
 				MetricRetentionRule r = MetricsFactory.eINSTANCE
 						.createMetricRetentionRule();
 				r.setName("Daily values");
 				r.setPeriod(MetricRetentionPeriod.ONE_MONTH);
 				r.setRetentionExpression(dailyRetentionExpression);
 				rules.getMetricRetentionRules().add(r);
 
 			}
 			{
 				MetricRetentionRule r = MetricsFactory.eINSTANCE
 						.createMetricRetentionRule();
 				r.setName("Hourly values");
 				r.setPeriod(MetricRetentionPeriod.ONE_WEEK);
 				r.setRetentionExpression(hourlyRetentionExpression);
 				rules.getMetricRetentionRules().add(r);
 			}
 
 			// Add the retention job, if non-existing.
 			Resource jobResource = dataProvider
 					.getResource(SchedulingPackage.Literals.JOB);
 
 			List<Job> jobsToRemove = Lists.newArrayList();
 			for (EObject eo : jobResource.getContents()) {
 				if (eo instanceof RetentionJob) {
 					jobsToRemove.add((RetentionJob) eo);
 				}
 			}
 
 			if (jobsToRemove.size() > 0) {
 				jobResource.getContents().removeAll(jobsToRemove);
 			}
 
 			// Add the retention job.
 			final RetentionJob retentionJob = SchedulingFactory.eINSTANCE
 					.createRetentionJob();
 			retentionJob.setJobState(JobState.IN_ACTIVE);
 			retentionJob.setStartTime(modelUtils.toXMLDate(modelUtils
 					.tomorrow()));
 			retentionJob.setInterval(600);
 			retentionJob.setName("Data Retention");
 			jobResource.getContents().add(retentionJob);
 		}
 
 		// }
 	}
 
 	private void loadRoles() {
 
 		final CDOResource rolesResource = (CDOResource) dataProvider
 				.getResource(GenericsPackage.Literals.ROLE);
 
 		if (rolesResource.getContents().size() > 0) {
 			return;
 		}
 
 		final CDOResource userResource = (CDOResource) dataProvider
 				.getResource(GenericsPackage.Literals.PERSON);
 
 		// Add the fixture roles.
 		{
 			final Role r = GenericsFactory.eINSTANCE.createRole();
 			r.setName(ROLE_ADMIN);
 			rolesResource.getContents().add(r);
 
 			{
 				final Person p = GenericsFactory.eINSTANCE.createPerson();
 				p.setLogin("admin");
 				p.setFirstName("admin");
 				p.setLastName("admin");
 				// p.setPassword("admin");
 				p.setActive(true);
 				p.setRoles(r);
 				userResource.getContents().add(p);
 			}
 		}
 		{
 			final Role r = GenericsFactory.eINSTANCE.createRole();
 			r.setName(ROLE_PLANNER);
 			rolesResource.getContents().add(r);
 		}
 		{
 			final Role r = GenericsFactory.eINSTANCE.createRole();
 			r.setName(ROLE_READONLY);
 			rolesResource.getContents().add(r);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void loadLibrary() {
 
 		final CDOResource res = (CDOResource) dataProvider
 				.getResource(LibraryPackage.Literals.LIBRARY);
 
 		Library lib = null;
 
 		// Should do some basic import data validation.
 		if (res.getContents() != null && (res.getContents().size() > 0)) {
 			if (res.getContents().get(0) instanceof Library) {
 				// Ok, proceed.
 				lib = (Library) res.getContents().get(0);
 			}
 		} else {
 			lib = LibraryFactory.eINSTANCE.createLibrary();
 			res.getContents().add(lib);
 		}
 
 		final NodeType sgsnType = LibraryFactory.eINSTANCE.createNodeType();
 		final Function sgsnFunction = LibraryFactory.eINSTANCE.createFunction();
 		sgsnFunction.setName("SGSN");
 		sgsnType.getFunctions().add(sgsnFunction);
 
 		lib.getNodeTypes().add(sgsnType);
 
 		final NetXResource sgsnRes = LibraryFactory.eINSTANCE
 				.createNetXResource();
 		final MetricValueRange range = MetricsFactory.eINSTANCE
 				.createMetricValueRange();
 		sgsnRes.getMetricValueRanges().add(range);
 
 		sgsnFunction.getResourceRefs().add(sgsnRes);
 
 		final Value v = GenericsFactory.eINSTANCE.createValue();
 		v.setValue(2.0);
 		v.setTimeStamp(modelUtils.toXMLDate(modelUtils.yesterday()));
 
 		final Value v1 = GenericsFactory.eINSTANCE.createValue();
 		v1.setValue(2.1);
 		v1.setTimeStamp(modelUtils.toXMLDate(modelUtils.twoDaysAgo()));
 
 		final Value v2 = GenericsFactory.eINSTANCE.createValue();
 		v2.setValue(2.1);
 		v2.setTimeStamp(modelUtils.toXMLDate(modelUtils.threeDaysAgo()));
 
 		range.getMetricValues().addAll(ImmutableList.of(v, v1, v2));
 	}
 
 }
