 /**
 * *****************************************************************************
 * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************
 */
 
 package eu.trentorise.opendata.ckanalyze.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.ckan.Client;
 import org.ckan.Connection;
 import org.ckan.resource.impl.Dataset;
 import org.ckan.resource.impl.Resource;
 import org.slf4j.Logger;
 
 import eu.trentorise.opendata.ckanalyze.analyzers.CatalogAnalyzer;
 import eu.trentorise.opendata.ckanalyze.analyzers.resources.CSVAnalyzer;
 import eu.trentorise.opendata.ckanalyze.analyzers.resources.CSVAnalyzer.Datatype;
 import eu.trentorise.opendata.ckanalyze.downloader.Downloader;
 import eu.trentorise.opendata.ckanalyze.exceptions.CKAnalyzeException;
 import eu.trentorise.opendata.ckanalyze.jpa.Catalog;
 import eu.trentorise.opendata.ckanalyze.jpa.CatalogStringDistribution;
 import eu.trentorise.opendata.ckanalyze.jpa.ResourceDatatypesCount;
 import eu.trentorise.opendata.ckanalyze.jpa.ResourceStringDistribution;
 import eu.trentorise.opendata.ckanalyze.utility.ResourcesUtility;
 
 /**
  * 
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  *Last modified by azanella On 10/ott/2013
  */
 public class AnalysisManager {
 	
 	private String downloadDirPath;
 	private Logger applicationLogger;
 	private boolean updatedResources = false;
 	
 	public AnalysisManager(String downloadDirPath, Logger applicationLogger) {
 		super();
 		this.downloadDirPath = downloadDirPath;
 		this.applicationLogger = applicationLogger;
 	}
 
 	public void processCatalog(String hostname, List<String> dss)
 	{
 		//Security check for inconsistencies
 		if(PersistencyManager.isUpdatingCatalog(hostname))
 		{
			System.out.println("entro");
 			PersistencyManager.deleteCatalogIfExists(hostname);
			System.out.println("esco");
 		}
 		
 		PersistencyManager.setIsUpdatingCatalog(hostname, true);
 		Catalog catSave = PersistencyManager.getCatalogByName(hostname);
 		boolean updating = catSave != null;
 		if(!updating)
 		{
 			catSave = new Catalog();
 		}
 		catSave.setUrl(hostname);
 		Client c = new Client(new Connection(hostname),null);
 		List<String> dsList = dss;
 		catSave.setTotalDatasetsCount(dsList.size());
 		catSave.setTotalResourcesCount(0);
 		catSave.setTotalFileSizeCount(0);
 		if(!updating)
 		{
 			PersistencyManager.insert(catSave);
 		}
 		for (String dsname : dsList) {
 			try {
 				Dataset ds = c.getDataset(dsname);
 				catSave.setTotalResourcesCount(catSave.getTotalResourcesCount()
 						+ ds.getResources().size());
 				for (Resource r : ds.getResources()) {
 					String format = r.getFormat().toLowerCase();
 					if ((format.contains("csv")) || (format.contains("tsv")))
 					{
 					catSave.setTotalFileSizeCount(catSave
 							.getTotalFileSizeCount() + r.getSize());
 					PersistencyManager.update(catSave);
 					applicationLogger.info("%%ds:\t" + dsname + " res:" + r.getName());
 					processResource(r, catSave);
 					}
 				}
 			} catch (Exception e) {
 				applicationLogger.error("error in dataset {}", dsname, e.getMessage());
 			}
 		}
 		if(updatedResources || 	(!updating))
 		{
 			CatalogAnalyzer catanalyze = new CatalogAnalyzer();
 			catanalyze.analyze();
 			catSave.setAvgColumnCount(catanalyze.getAvgColumnCount());
 			catSave.setAvgRowCount(catanalyze.getAvgRowCount());
 			catSave.setAvgStringLength(catanalyze.getAvgStringLength());
 			if(updating)
 			{
 				PersistencyManager.deleteAllStringDistributions(hostname);
 				PersistencyManager.merge(catSave);
 			}
 			else
 			{
 				PersistencyManager.update(catSave);
 			}
 			for (CatalogStringDistribution csd : catanalyze
 					.getCatalogStringDistribution()) {
 				csd.setCatalog(catSave);
 					PersistencyManager.insert(csd);
 			}
 		}
 		PersistencyManager.setIsUpdatingCatalog(hostname, false);
 	}
 	
 	private void processResource(Resource r, Catalog catSave) throws IOException
 	{
 		Downloader dwn = Downloader.getInstance();
 		dwn.setFilepath(downloadDirPath);
 		dwn.setUrl(r.getUrl());
 		dwn.download();
 		eu.trentorise.opendata.ckanalyze.jpa.Resource resSave = PersistencyManager.getResourcesByCkanId(r.getId(),catSave.getUrl());
 		if(resSave != null)
 		{
 			if(!ResourcesUtility.computeSHA(downloadDirPath + dwn.getFilename()).equals(resSave.getFileSha()))
 			{
 				updatedResources = true;
 				PersistencyManager.delete(resSave);
 				analyzeResource(r, catSave, dwn);
 				applicationLogger.info("Resource Recomputed");
 			}
 			else
 			{
 				applicationLogger.info("Resource Skipped");
 			}
 		}
 		else
 		{
 			applicationLogger.info("Resource added");
 			analyzeResource(r, catSave, dwn);
 		}
 		File f = new File(downloadDirPath + dwn.getFilename());
 		f.delete();
 	}
 	
 	private void analyzeResource(Resource r, Catalog catSave, Downloader dwn) throws IOException
 	{
 		try {
 			if (dwn.getFilename().toLowerCase().trim().endsWith(".zip")) {
 				throw new CKAnalyzeException("ZIP File -- Skipped");
 			}
 			CSVAnalyzer ca = new CSVAnalyzer(downloadDirPath + dwn.getFilename(),
 					r.getId());
 			ca.analyze();
 			eu.trentorise.opendata.ckanalyze.jpa.Resource resSave = new eu.trentorise.opendata.ckanalyze.jpa.Resource();
 			resSave.setCatalog(catSave);
 			resSave.setCkanId(r.getId());
 			resSave.setColumnCount(ca.getColumnCount());
 			resSave.setFileFormat(r.getFormat());
 			resSave.setFileName(dwn.getFilename());
 			resSave.setFileSize(dwn.getSize());
 			resSave.setRowCount(ca.getRowCount());
 			resSave.setStringAvg(ca.getStringLengthAvg());
 			resSave.setUrl(dwn.getUrl());
 			resSave.setFileSha(ResourcesUtility.computeSHA(downloadDirPath + dwn.getFilename()));
 			PersistencyManager.insert(resSave);
 			for (Datatype dt : ca.getColsPerType().keySet()) {
 				eu.trentorise.opendata.ckanalyze.jpa.Datatype dtSave = PersistencyManager
 						.getDatatypeByName(dt.toString());
 				if (dtSave == null) {
 					dtSave = new eu.trentorise.opendata.ckanalyze.jpa.Datatype();
 					dtSave.setName(dt.toString());
 					PersistencyManager.insert(dtSave);
 				}
 				ResourceDatatypesCount dtc = new ResourceDatatypesCount();
 				dtc.setFreq(ca.getColsPerType().get(dt));
 				dtc.setResource(resSave);
 				dtc.setDatatype(dtSave);
 				PersistencyManager.insert(dtc);
 			}
 			for (Long length : ca.getStringLengthDistribution().keySet()) {
 				ResourceStringDistribution distr = new ResourceStringDistribution();
 				distr.setResource(resSave);
 				distr.setLength(length);
 				distr.setFreq(ca.getStringLengthDistribution().get(length));
 				PersistencyManager.insert(distr);
 			}
 		} catch (CKAnalyzeException e) {
 			applicationLogger.error("Error processing resource {}" + r.getName(), e);
 			File f = new File(downloadDirPath + dwn.getFilename());
 			f.delete();
 		}
 	}
 }
