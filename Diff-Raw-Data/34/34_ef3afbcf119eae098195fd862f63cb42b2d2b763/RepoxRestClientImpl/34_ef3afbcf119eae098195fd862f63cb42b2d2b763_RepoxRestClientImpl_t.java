 /*
  * Copyright 2007 EDL FOUNDATION
  *
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 package eu.europeana.uim.repox.rest.client;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.joda.time.DateTime;
 
 import eu.europeana.uim.repox.RepoxException;
 import eu.europeana.uim.repox.model.IngestFrequency;
 import eu.europeana.uim.repox.rest.client.xml.Aggregator;
 import eu.europeana.uim.repox.rest.client.xml.Aggregators;
 import eu.europeana.uim.repox.rest.client.xml.DataProviders;
 import eu.europeana.uim.repox.rest.client.xml.DataSources;
 import eu.europeana.uim.repox.rest.client.xml.HarvestingStatus;
 import eu.europeana.uim.repox.rest.client.xml.Log;
 import eu.europeana.uim.repox.rest.client.xml.Provider;
 import eu.europeana.uim.repox.rest.client.xml.RecordResult;
 import eu.europeana.uim.repox.rest.client.xml.Response;
 import eu.europeana.uim.repox.rest.client.xml.RunningTasks;
 import eu.europeana.uim.repox.rest.client.xml.ScheduleTasks;
 import eu.europeana.uim.repox.rest.client.xml.Source;
 
 /**
  * Class implementing REST functionality for accessing the REPOX repository.uikjul.kcv bnm
  * 
  * @author Georgios Markakis
  * @author Yorgos Mamakis
  */
 public class RepoxRestClientImpl implements RepoxRestClient {
     /**
      * base uri specifying the repox installation
      */
     private String uri;
 
     /**
      * Creates a new instance of this class.
      */
     public RepoxRestClientImpl() {
         this(null);
     }
 
     /**
      * Creates a new instance of this class.
      * 
      * @param uri
      *            base uri specifying the repox installation
      */
     public RepoxRestClientImpl(String uri) {
         this.uri = uri;
     }
 
     /*
      * Aggregator related operations
      */
     @Override
     public Aggregator createAggregator(Aggregator aggregator) throws RepoxException {
         StringBuffer name = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer homepage = new StringBuffer();
 
         name.append("name=");
         name.append(aggregator.getName());
         nameCode.append("nameCode=");
         nameCode.append(aggregator.getNameCode());
         homepage.append("homepage=");
         homepage.append(aggregator.getUrl());
 
         Response resp = invokeRestCall("/aggregators/create", Response.class, name.toString(),
                 nameCode.toString(), homepage.toString());
 
         if (resp.getAggregator() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create aggregator! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during creation of aggregator!");
             }
         } else {
             return resp.getAggregator();
         }
     }
 
     @Override
     public String deleteAggregator(String aggregatorId) throws RepoxException {
         StringBuffer id = new StringBuffer();
         id.append("id=");
         id.append(aggregatorId);
 
         Response resp = invokeRestCall("/aggregators/delete", Response.class, id.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not delete aggregator! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during deletion of aggregator!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     @Override
     public Aggregator updateAggregator(Aggregator aggregator) throws RepoxException {
         StringBuffer id = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer homepage = new StringBuffer();
 
         id.append("id=");
         id.append(aggregator.getId());
         name.append("name=");
         name.append(aggregator.getName());
         nameCode.append("nameCode=");
         nameCode.append(aggregator.getNameCode());
         homepage.append("homepage=");
         homepage.append(aggregator.getUrl());
 
         Response resp = invokeRestCall("/aggregators/update", Response.class, id.toString(),
                 name.toString(), nameCode.toString(), homepage.toString());
 
         if (resp.getAggregator() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update aggregator! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of aggregator!");
             }
         } else {
             return resp.getAggregator();
         }
     }
 
     @Override
     public Aggregators retrieveAggregators() throws RepoxException {
         Response resp = invokeRestCall("/aggregators/list", Response.class);
 
         if (resp.getAggregators() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieving aggregators! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during retrieving of aggregators!");
             }
         } else {
             return resp.getAggregators();
         }
     }
 
     @Override
     public Aggregator retrieveAggregator(String aggrID) throws RepoxException {
         Aggregator aggregator = null;
         Aggregators aggregators = retrieveAggregators();
         for (Aggregator aggr : aggregators.getAggregator()) {
             if (aggr.getId().equals(aggrID)) {
                 aggregator = aggr;
                 break;
             }
         }
         return aggregator;
     }
     @Override
     public Aggregator retrieveAggregatorByNameCode(String mnemonic) throws RepoxException {
         Aggregator aggregator = null;
         Aggregators aggregators = retrieveAggregators();
         for (Aggregator aggr : aggregators.getAggregator()) {
             if (aggr.getNameCode().equals(mnemonic)) {
                 aggregator = aggr;
                 break;
             }
         }
         return aggregator;
     }
 
     /*
      * Provider related operations
      */
     @Override
     public Provider createProvider(Provider prov, Aggregator agr) throws RepoxException {
         StringBuffer aggregatorId = new StringBuffer();
        StringBuffer providerId = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer country = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer homepage = new StringBuffer();
         StringBuffer datasetType = new StringBuffer();
 
         aggregatorId.append("aggregatorId=");
         aggregatorId.append(agr.getId());
        
        providerId.append("dataProviderId=");
        providerId.append(prov.getId());
        
         name.append("name=");
         name.append(prov.getName());
        
         description.append("description=");
         if (prov.getDescription() != null) {
             description.append(prov.getDescription());
         } else {
             description.append("NONE");
         }
 
         country.append("country=");
         if (prov.getCountry() != null) {
             country.append(prov.getCountry());
         } else {
             country.append("eu");
         }
 
         nameCode.append("nameCode=");
         nameCode.append(prov.getNameCode());
 
         homepage.append("url=");
         if (prov.getUrl() != null) {
             homepage.append(prov.getUrl());
         } else {
             homepage.append("http://europeana.eu");
         }
 
         datasetType.append("dataSetType=");
         if (prov.getType() != null) {
             datasetType.append(prov.getType());
         } else {
             datasetType.append("UNKNOWN");
         }
 
         Response resp = invokeRestCall("/dataProviders/create", Response.class,
                aggregatorId.toString(), providerId.toString(), name.toString(), description.toString(),
                 country.toString(), nameCode.toString(), homepage.toString(),
                 datasetType.toString());
 
         if (resp.getProvider() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create provider! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during creation of provider!");
             }
         } else {
             return resp.getProvider();
         }
     }
 
     @Override
     public String deleteProvider(String provID) throws RepoxException {
         StringBuffer providerId = new StringBuffer();
         providerId.append("id=");
         providerId.append(provID);
 
         Response resp = invokeRestCall("/dataProviders/delete", Response.class,
                 providerId.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not delete provider! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during deletion of provider!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     @Override
     public Provider updateProvider(Provider prov) throws RepoxException {
         StringBuffer provId = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer country = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer homepage = new StringBuffer();
         StringBuffer datasetType = new StringBuffer();
 
         provId.append("id=");
         provId.append(prov.getId());
 
         name.append("name=");
         name.append(prov.getName());
 
         description.append("description=");
 
         if (prov.getDescription() != null) {
             description.append(prov.getDescription());
         } else {
             description.append("NONE");
         }
 
         country.append("country=");
         if (prov.getCountry() != null) {
             country.append(prov.getCountry());
         } else {
             country.append("eu");
         }
 
         nameCode.append("nameCode=");
         nameCode.append(prov.getNameCode());
 
         homepage.append("url=");
         if (prov.getUrl() != null) {
             homepage.append(prov.getUrl());
         } else {
             homepage.append("http://europeana.eu");
         }
 
         datasetType.append("dataSetType=");
         if (prov.getType() != null) {
             datasetType.append(prov.getType());
         } else {
             datasetType.append("UNKNOWN");
         }
 
         Response resp = invokeRestCall("/dataProviders/update", Response.class, provId.toString(),
                 name.toString(), description.toString(), country.toString(), nameCode.toString(),
                 homepage.toString(), datasetType.toString());
 
         if (resp.getProvider() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update provider! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of provider!");
             }
         } else {
             return resp.getProvider();
         }
 
     }
 
     @Override
     public DataProviders retrieveProviders() throws RepoxException {
         Response resp = invokeRestCall("/dataProviders/list", Response.class);
 
         if (resp.getDataProviders() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieving providers! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during retrieving of providers!");
             }
         } else {
             return resp.getDataProviders();
         }
     }
 
     @Override
     public Provider retrieveProvider(String id) throws RepoxException {
         Provider provider = null;
         DataProviders providers = retrieveProviders();
         for (Provider prov : providers.getProvider()) {
             if (prov.getId().equals(id)) {
                 provider = prov;
                 break;
             }
         }
         return provider;
     }
 
     @Override
     public Provider retrieveProviderByNameCode(String mnemonic) throws RepoxException {
         Provider provider = null;
         DataProviders providers = retrieveProviders();
         for (Provider prov : providers.getProvider()) {
             if (prov.getNameCode().equals(mnemonic)) {
                 provider = prov;
                 break;
             }
         }
         return provider;
     }
 
     /*
      * Datasources related operations
      */
     @Override
     public DataSources retrieveDataSources() throws RepoxException {
         Response resp = invokeRestCall("/dataSources/list", Response.class);
 
         if (resp.getDataSources() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieving sources! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during retrieving of sources!");
             }
         } else {
             return resp.getDataSources();
         }
     }
 
     @Override
     public Source retrieveDataSource(String id) throws RepoxException {
         Source source = null;
         DataSources sources = retrieveDataSources();
         for (Source src : sources.getSource()) {
             if (src.getId().equals(id)) {
                 source = src;
                 break;
             }
         }
         return source;
     }
 
     @Override
     public Source retrieveDataSourceByNameCode(String mnemonic) throws RepoxException {
         Source source = null;
         DataSources sources = retrieveDataSources();
         for (Source src : sources.getSource()) {
             if (src.getNameCode().equals(mnemonic)) {
                 source = src;
                 break;
             }
         }
         return source;
     }
 
     @Override
     public Source createDatasourceOAI(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSOAI("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create OAI source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during creating of OAI source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceZ3950Timestamp(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateZ3950Timestamp("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during creating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceZ3950IdFile(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSZ3950IdFile("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during creating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceZ3950IdSequence(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSZ3950IdSequence("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during creating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceFtp(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSFtp("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create ftp source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during creating of ftp source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceHttp(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSHttp("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create http source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during creating of http source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source createDatasourceFolder(Source ds, Provider prov) throws RepoxException {
         Response resp = createUpdateDSFolder("create", ds, prov);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not create folder source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during creating of folder source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceOAI(Source ds) throws RepoxException {
         Response resp = createUpdateDSOAI("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update OAI source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of OAI source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceZ3950Timestamp(Source ds) throws RepoxException {
         Response resp = createUpdateZ3950Timestamp("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during updating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceZ3950IdFile(Source ds) throws RepoxException {
         Response resp = createUpdateDSZ3950IdFile("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during updating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceZ3950IdSequence(Source ds) throws RepoxException {
         Response resp = createUpdateDSZ3950IdSequence("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update Z3950 source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during updating of Z3950 source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceFtp(Source ds) throws RepoxException {
         Response resp = createUpdateDSFtp("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update ftp source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of ftp source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceHttp(Source ds) throws RepoxException {
         Response resp = createUpdateDSHttp("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update http source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of http source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public Source updateDatasourceFolder(Source ds) throws RepoxException {
         Response resp = createUpdateDSFolder("update", ds, null);
 
         if (resp.getSource() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not update source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during updating of source!");
             }
         } else {
             return resp.getSource();
         }
     }
 
     @Override
     public String deleteDatasource(String dsID) throws RepoxException {
         StringBuffer id = new StringBuffer();
         id.append("id=");
         id.append(dsID);
 
         Response resp = invokeRestCall("/dataSources/delete", Response.class, id.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not delete source! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during deletion of source!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     /*
      * Record related operations
      */
     @Override
     public RecordResult retrieveRecord(String recordString) throws RepoxException {
         throw new UnsupportedOperationException("Not implemented yet...");
     }
 
     @Override
     public String saveRecord(String recordID, Source ds, String recordXML) throws RepoxException {
         throw new UnsupportedOperationException("Not implemented yet...");
     }
 
     @Override
     public String markRecordAsDeleted(String recordID) throws RepoxException {
         throw new UnsupportedOperationException("Not implemented yet...");
 
     }
 
     @Override
     public String eraseRecord(String recordID) throws RepoxException {
         throw new UnsupportedOperationException("Not implemented yet...");
     }
 
     /*
      * Harvest Control/Monitoring operations
      */
     @Override
     public String initiateHarvesting(String dsID, boolean isfull) throws RepoxException {
         StringBuffer id = new StringBuffer();
         id.append("id=");
         id.append(dsID);
         StringBuffer fullIngest = new StringBuffer();
         fullIngest.append("fullIngest=");
         fullIngest.append(isfull);
 
         Response resp = invokeRestCall("/dataSources/startIngest", Response.class, id.toString(),
                 fullIngest.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not initiate harvesting! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during initiating of harvesting!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     @Override
     public String cancelHarvesting(String dsID) throws RepoxException {
         StringBuffer id = new StringBuffer();
         id.append("id=");
         id.append(dsID);
 
         Response resp = invokeRestCall("/dataSources/stopIngest", Response.class, id.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not cancel harvesting! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during canceling of harvesting!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     @Override
     public String scheduleHarvesting(String dsID, DateTime ingestionDate,
             IngestFrequency frequencyobj, boolean isfull) throws RepoxException {
         StringBuffer id = new StringBuffer();
         StringBuffer firstRunDate = new StringBuffer();
         StringBuffer firstRunHour = new StringBuffer();
         StringBuffer frequency = new StringBuffer();
         StringBuffer xmonths = new StringBuffer();
         StringBuffer fullIngest = new StringBuffer();
 
         id.append("id=");
         id.append(dsID);
 
         firstRunDate.append("firstRunDate=");
         firstRunDate.append(ingestionDate.getDayOfMonth() + "-" + ingestionDate.getMonthOfYear() +
                             "-" + ingestionDate.getYear());
 
         firstRunHour.append("firstRunHour=");
         firstRunHour.append(ingestionDate.getHourOfDay() + ":" + ingestionDate.getMinuteOfHour());
 
         frequency.append("frequency=");
         frequency.append(frequencyobj.toString());
 
         xmonths.append("xmonths=");
         // xmonths.append(frequencyobj.toString());
 
         frequency.append("fullIngest=");
         frequency.append(fullIngest.toString());
 
         Response resp = invokeRestCall("/dataSources/scheduleIngest", Response.class,
                 id.toString(), firstRunDate.toString(), firstRunHour.toString(),
                 frequency.toString(), xmonths.toString(), fullIngest.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not schedule harvesting! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during scheduling of harvesting!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     @Override
     public HarvestingStatus getHarvestingStatus(String dsID) throws RepoxException {
         StringBuffer id = new StringBuffer();
 
         id.append("id=");
         id.append(dsID);
 
         Response resp = invokeRestCall("/dataSources/harvestStatus", Response.class, id.toString());
 
         if (resp.getHarvestingStatus() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieve harvesting status! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during retrieving of harvesting status!");
             }
         } else {
             return resp.getHarvestingStatus();
         }
     }
 
     @Override
     public RunningTasks getActiveHarvestingSessions() throws RepoxException {
         Response resp = invokeRestCall("/dataSources/harvesting", Response.class);
 
         if (resp.getRunningTasks() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieve active harvesting sessions! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during retrieving of active harvesting sessions!");
             }
         } else {
             return resp.getRunningTasks();
         }
     }
 
     @Override
     public ScheduleTasks getScheduledHarvestingSessions(String dsID) throws RepoxException {
         StringBuffer id = new StringBuffer();
         id.append("id=");
         id.append(dsID);
 
         Response resp = invokeRestCall("/dataSources/scheduleList", Response.class, id.toString());
 
         if (resp.getScheduleTasks() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException(
                         "Could not retrieve scheduled harvesting sessions! Reason: " +
                                 resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during retrieving of scheduled harvesting sessions!");
             }
         } else {
             return resp.getScheduleTasks();
         }
     }
 
     @Override
     public Log getHarvestLog(String dsID) throws RepoxException {
         StringBuffer id = new StringBuffer();
 
         id.append("id=");
         id.append(dsID);
 
         Response resp = invokeRestCall("/dataSources/log", Response.class, id.toString());
 
         if (resp.getLog() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not retrieve harvesting log! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException(
                         "Unidentified repox error during retrieving of harvesting log!");
             }
         } else {
             return resp.getLog();
         }
     }
 
     @Override
     public String initializeExport(String dsID, int records) throws RepoxException {
         StringBuffer id = new StringBuffer();
         StringBuffer recordsPerFile = new StringBuffer();
 
         id.append("id=");
         id.append(dsID);
 
         recordsPerFile.append("recordsPerFile=");
 
         if (records == 0) {
             recordsPerFile.append("ALL");
         } else {
             recordsPerFile.append(records);
         }
 
         Response resp = invokeRestCall("/dataSources/startExport", Response.class, id.toString(),
                 recordsPerFile.toString());
 
         if (resp.getSuccess() == null) {
             if (resp.getError() != null) {
                 throw new RepoxException("Could not initialize export! Reason: " +
                                          resp.getError().getCause());
             } else {
                 throw new RepoxException("Unidentified repox error during initializing of export!");
             }
         } else {
             return resp.getSuccess();
         }
     }
 
     // Private Methods
     /**
      * context for JAXB
      */
     private static JAXBContext jaxbContext;
 
     /**
      * initializes jaxb context with root class of MACS records
      */
     static {
         try {
             jaxbContext = JAXBContext.newInstance(Response.class);
         } catch (JAXBException e) {
             throw new RuntimeException("Failed to initialize jaxb.", e);
         }
     }
 
     /**
      * Auxiliary method for invoking a REST operation with parameters
      * 
      * @param <S>
      *            the return type
      * @param wsOperation
      * @return casted rest response
      * @throws RepoxException
      */
     private <S> S invokeRestCall(String restOperation, Class<S> responseClass, String... params)
             throws RepoxException {
         StringBuffer operation = new StringBuffer();
         operation.append(uri);
         operation.append(restOperation);
         if (params.length > 0) {
             operation.append("?");
         }
 
         for (int i = 0; i < params.length; i++) {
             if (i != 0 && params[i] != null && params[i].length() > 0) {
                 operation.append("&");
             }
             operation.append(params[i].replaceAll(" ", "%20"));
         }
 
         String urlStr = operation.toString();
         URL url;
         try {
             url = new URL(urlStr);
         } catch (Exception e) {
             throw new RepoxException("Could not creat url of '" + urlStr + "'!", e);
         }
 
         HttpURLConnection conn;
         try {
             conn = (HttpURLConnection)url.openConnection();
         } catch (IOException e) {
             throw new RepoxException("Could not open url connection!", e);
         }
 
         conn.disconnect();
 
         Unmarshaller u;
         try {
             u = jaxbContext.createUnmarshaller();
         } catch (JAXBException e) {
             throw new RepoxException("Could not create unmarshaller!", e);
         }
         S response;
         try {
             response = responseClass.cast(u.unmarshal(new InputStreamReader(conn.getInputStream())));
         } catch (Exception e) {
             throw new RepoxException("Could not unmarshall rest response!", e);
         }
         return response;
     }
 
     // Getters & Setters
     /**
      * @param uri
      *            repox location for this client
      */
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     @Override
     public String getUri() {
         return uri;
     }
 
     private Response createUpdateDSOAI(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer metadataFormat = new StringBuffer();
         StringBuffer oaiURL = new StringBuffer();
         StringBuffer oaiSet = new StringBuffer();
 
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         // description.append(ds.getDescription());
         description.append("NONE");
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         metadataFormat.append("metadataFormat=");
         metadataFormat.append(ds.getMetadataFormat());
         oaiURL.append("oaiURL=");
         oaiURL.append(ds.getOaiSource());
         oaiSet.append("oaiSet=");
         oaiSet.append(ds.getOaiSet());
         if (action.equals("create")) {
             return invokeRestCall("/dataSources/createOai", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), metadataFormat.toString(), oaiURL.toString(),
                     oaiSet.toString());
         } else {
             return invokeRestCall("/dataSources/updateOai", Response.class, id.toString(),
                     description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     metadataFormat.toString(), oaiURL.toString(), oaiSet.toString());
         }
     }
 
     private Response createUpdateZ3950Timestamp(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer address = new StringBuffer();
         StringBuffer port = new StringBuffer();
         StringBuffer database = new StringBuffer();
         StringBuffer user = new StringBuffer();
         StringBuffer password = new StringBuffer();
         StringBuffer recordSyntax = new StringBuffer();
         StringBuffer charset = new StringBuffer();
         StringBuffer earliestTimestamp = new StringBuffer();
         StringBuffer recordIdPolicy = new StringBuffer();
 
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
 
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
 
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
 
         if (ds.getTarget() != null) {
             address.append("address=");
             address.append(ds.getTarget().getAddress());
             port.append("port=");
             port.append(ds.getTarget().getPort());
             database.append("database=");
             database.append(ds.getTarget().getDatabase());
 
             user.append("user=");
             user.append(ds.getTarget().getUser());
             password.append("password=");
             password.append(ds.getTarget().getPassword());
             recordSyntax.append("recordSyntax=");
             recordSyntax.append(ds.getTarget().getRecordSyntax());
 
             charset.append("charset=");
             charset.append(ds.getTarget().getCharset());
         }
 
         earliestTimestamp.append("earliestTimestamp=");
         earliestTimestamp.append(ds.getEarliestTimestamp());
 
         if (ds.getRecordIdPolicy() != null) {
             recordIdPolicy.append("recordIdPolicy=");
             recordIdPolicy.append(ds.getRecordIdPolicy().getType());
             if (ds.getRecordIdPolicy().getType().equals("idExported")) {
                 idXpath.append("idXpath=");
                 idXpath.append(ds.getRecordIdPolicy().getIdXpath());
 
                 namespacePrefix.append("namespacePrefix=");
                 namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
                 namespaceUri.append("namespaceUri=");
                 namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
             }
         }
 
         if (action.equals("create")) {
             Response resp = invokeRestCall("/dataSources/createZ3950Timestamp", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), address.toString(), port.toString(), database.toString(),
                     user.toString(), password.toString(), recordSyntax.toString(),
                     charset.toString(), earliestTimestamp.toString(), recordIdPolicy.toString(),
                     idXpath.toString(), namespacePrefix.toString(), namespaceUri.toString());
             return resp;
         } else {
             Response resp = invokeRestCall("/dataSources/updateZ3950Timestamp", Response.class,
                     id.toString(), description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     address.toString(), port.toString(), database.toString(), user.toString(),
                     password.toString(), recordSyntax.toString(), charset.toString(),
                     earliestTimestamp.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString());
             return resp;
         }
     }
 
     private Response createUpdateDSZ3950IdFile(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer address = new StringBuffer();
         StringBuffer port = new StringBuffer();
         StringBuffer database = new StringBuffer();
         StringBuffer user = new StringBuffer();
         StringBuffer password = new StringBuffer();
         StringBuffer recordSyntax = new StringBuffer();
         StringBuffer charset = new StringBuffer();
 
         StringBuffer filePath = new StringBuffer();
         StringBuffer recordIdPolicy = new StringBuffer();
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         address.append("address=");
         address.append(ds.getTarget().getAddress());
         port.append("port=");
         port.append(ds.getTarget().getPort());
         database.append("database=");
         database.append(ds.getTarget().getDatabase());
 
         user.append("user=");
         user.append(ds.getTarget().getUser());
         password.append("password=");
         password.append(ds.getTarget().getPassword());
         recordSyntax.append("recordSyntax=");
         recordSyntax.append(ds.getTarget().getRecordSyntax());
 
         charset.append("charset=");
         charset.append(ds.getTarget().getCharset());
         filePath.append("filePath=");
         filePath.append(ds.getFilePath());
         recordIdPolicy.append("recordIdPolicy=");
         recordIdPolicy.append(ds.getRecordIdPolicy().getType());
         if (ds.getRecordIdPolicy().getType().equals("idExported")) {
             idXpath.append("idXpath=");
             idXpath.append(ds.getRecordIdPolicy().getIdXpath());
 
             namespacePrefix.append("namespacePrefix=");
             namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
             namespaceUri.append("namespaceUri=");
             namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
         }
 
         if (action.equals("create")) {
             Response resp = invokeRestCall("/dataSources/createZ3950IdList", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), address.toString(), port.toString(), database.toString(),
                     user.toString(), password.toString(), recordSyntax.toString(),
                     charset.toString(), filePath.toString(), recordIdPolicy.toString(),
                     idXpath.toString(), namespacePrefix.toString(), namespaceUri.toString());
             return resp;
         } else {
             Response resp = invokeRestCall("/dataSources/updateZ3950IdList", Response.class,
                     id.toString(), description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     address.toString(), port.toString(), database.toString(), user.toString(),
                     password.toString(), recordSyntax.toString(), charset.toString(),
                     filePath.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString());
             return resp;
         }
     }
 
     private Response createUpdateDSZ3950IdSequence(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer address = new StringBuffer();
         StringBuffer port = new StringBuffer();
         StringBuffer database = new StringBuffer();
         StringBuffer user = new StringBuffer();
         StringBuffer password = new StringBuffer();
         StringBuffer recordSyntax = new StringBuffer();
         StringBuffer charset = new StringBuffer();
 
         StringBuffer maximumId = new StringBuffer();
 
         StringBuffer recordIdPolicy = new StringBuffer();
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         address.append("address=");
         address.append(ds.getTarget().getAddress());
         port.append("port=");
         port.append(ds.getTarget().getPort());
         database.append("database=");
         database.append(ds.getTarget().getDatabase());
 
         user.append("user=");
         user.append(ds.getTarget().getUser());
         password.append("password=");
         password.append(ds.getTarget().getPassword());
         recordSyntax.append("recordSyntax=");
         recordSyntax.append(ds.getTarget().getRecordSyntax());
 
         charset.append("charset=");
         charset.append(ds.getTarget().getCharset());
 
         maximumId.append("maximumId=");
         maximumId.append(ds.getMaximumId());
 
         recordIdPolicy.append("recordIdPolicy=");
         recordIdPolicy.append(ds.getRecordIdPolicy().getType());
         if (ds.getRecordIdPolicy().getType().equals("idExported")) {
             idXpath.append("idXpath=");
             idXpath.append(ds.getRecordIdPolicy().getIdXpath());
 
             namespacePrefix.append("namespacePrefix=");
             namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
             namespaceUri.append("namespaceUri=");
             namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
         }
 
         if (action.equals("create")) {
             return invokeRestCall("/dataSources/createZ3950IdSequence", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), address.toString(), port.toString(), database.toString(),
                     user.toString(), password.toString(), recordSyntax.toString(),
                     charset.toString(), maximumId.toString(), recordIdPolicy.toString(),
                     idXpath.toString(), namespacePrefix.toString(), namespaceUri.toString());
         } else {
             return invokeRestCall("/dataSources/updateZ3950IdSequence", Response.class,
                     id.toString(), description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     address.toString(), port.toString(), database.toString(), user.toString(),
                     password.toString(), recordSyntax.toString(), charset.toString(),
                     maximumId.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString());
         }
     }
 
     private Response createUpdateDSFtp(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer metadataFormat = new StringBuffer();
         StringBuffer isoFormat = new StringBuffer();
         StringBuffer charset = new StringBuffer();
         StringBuffer recordIdPolicy = new StringBuffer();
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
         StringBuffer recordXPath = new StringBuffer();
         StringBuffer server = new StringBuffer();
         StringBuffer user = new StringBuffer();
         StringBuffer password = new StringBuffer();
         StringBuffer ftpPath = new StringBuffer();
 
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         metadataFormat.append("metadataFormat=");
         metadataFormat.append(ds.getMetadataFormat());
         // TODO: isoFormat and charset and ftpPath are missing
         isoFormat.append("isoFormat=");
         isoFormat.append(ds.getIsoFormat());
         // isoFormat.append("test");
 
         if (ds.getTarget() != null) {
             charset.append("charset=");
             charset.append(ds.getTarget().getCharset());
         }
 
         recordIdPolicy.append("recordIdPolicy=");
         recordIdPolicy.append(ds.getRecordIdPolicy().getType());
         if (ds.getRecordIdPolicy().getType().equals("idExported")) {
             idXpath.append("idXpath=");
             idXpath.append(ds.getRecordIdPolicy().getIdXpath());
             namespacePrefix.append("namespacePrefix=");
             namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
             namespaceUri.append("namespaceUri=");
             namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
         }
         recordXPath.append("recordXPath=");
         recordXPath.append(ds.getSplitRecords().getRecordXPath());
 
         server.append("server=");
         server.append(ds.getRetrieveStrategy().getServer());
         user.append("user=");
         user.append(ds.getRetrieveStrategy().getUser());
         password.append("password=");
         password.append(ds.getRetrieveStrategy().getPassword());
 
         ftpPath.append("ftpPath=");
         // ftpPath.append("/test");
         ftpPath.append(ds.getFtpPath());
 
         if (action.equals("create")) {
             return invokeRestCall("/dataSources/createFtp", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), metadataFormat.toString(), isoFormat.toString(),
                     charset.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString(), recordXPath.toString(),
                     server.toString(), user.toString(), password.toString(), ftpPath.toString());
         } else {
             return invokeRestCall("/dataSources/updateFtp", Response.class, id.toString(),
                     description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     metadataFormat.toString(), isoFormat.toString(), charset.toString(),
                     recordIdPolicy.toString(), idXpath.toString(), namespacePrefix.toString(),
                     namespaceUri.toString(), recordXPath.toString(), server.toString(),
                     user.toString(), password.toString(), ftpPath.toString());
         }
     }
 
     private Response createUpdateDSHttp(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer metadataFormat = new StringBuffer();
         StringBuffer isoFormat = new StringBuffer();
         StringBuffer charset = new StringBuffer();
         StringBuffer recordIdPolicy = new StringBuffer();
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
         StringBuffer recordXPath = new StringBuffer();
 
         StringBuffer url = new StringBuffer();
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         metadataFormat.append("metadataFormat=");
         metadataFormat.append(ds.getMetadataFormat());
         // TODO: not supported
         isoFormat.append("isoFormat=");
         isoFormat.append(ds.getIsoFormat());
         charset.append("charset=");
         charset.append(ds.getTarget().getCharset());
         recordIdPolicy.append("recordIdPolicy=");
         recordIdPolicy.append(ds.getRecordIdPolicy().getType());
         if (ds.getRecordIdPolicy().getType().equals("idExported")) {
             idXpath.append("idXpath=");
             idXpath.append(ds.getRecordIdPolicy().getIdXpath());
             namespacePrefix.append("namespacePrefix=");
             namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
             namespaceUri.append("namespaceUri=");
             namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
 
             recordXPath.append("recordXPath=");
             recordXPath.append(ds.getSplitRecords().getRecordXPath());
         }
         url.append("url=");
         url.append(ds.getRetrieveStrategy().getUrl());
 
         if (action.equals("create")) {
             return invokeRestCall("/dataSources/createHttp", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), metadataFormat.toString(), isoFormat.toString(),
                     charset.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString(), recordXPath.toString(),
                     url.toString());
         } else {
             return invokeRestCall("/dataSources/updateHttp", Response.class, id.toString(),
                     description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     metadataFormat.toString(), isoFormat.toString(), charset.toString(),
                     recordIdPolicy.toString(), idXpath.toString(), namespacePrefix.toString(),
                     namespaceUri.toString(), recordXPath.toString(), url.toString());
         }
 
     }
 
     private Response createUpdateDSFolder(String action, Source ds, Provider prov)
             throws RepoxException {
         StringBuffer dataProviderId = new StringBuffer();
         StringBuffer id = new StringBuffer();
         StringBuffer description = new StringBuffer();
         StringBuffer nameCode = new StringBuffer();
         StringBuffer name = new StringBuffer();
         StringBuffer exportPath = new StringBuffer();
         StringBuffer schema = new StringBuffer();
         StringBuffer namespace = new StringBuffer();
 
         // Method specific
         StringBuffer metadataFormat = new StringBuffer();
         StringBuffer isoFormat = new StringBuffer();
         StringBuffer charset = new StringBuffer();
         StringBuffer recordIdPolicy = new StringBuffer();
         StringBuffer idXpath = new StringBuffer();
         StringBuffer namespacePrefix = new StringBuffer();
         StringBuffer namespaceUri = new StringBuffer();
         StringBuffer recordXPath = new StringBuffer();
 
         StringBuffer folder = new StringBuffer();
         if (action.equals("create")) {
             dataProviderId.append("dataProviderId=");
             dataProviderId.append(prov.getId());
         }
         id.append("id=");
         id.append(ds.getId());
         description.append("description=");
         description.append(ds.getDescription());
         nameCode.append("nameCode=");
         nameCode.append(ds.getNameCode());
         name.append("name=");
         name.append(ds.getName());
         exportPath.append("exportPath=");
         exportPath.append(ds.getExportPath());
         schema.append("schema=");
         schema.append(ds.getSchema());
         namespace.append("namespace=");
         namespace.append(ds.getNamespace());
         metadataFormat.append("metadataFormat=");
         metadataFormat.append(ds.getMetadataFormat());
         // todo: isoFormat charset folder are missing
         isoFormat.append("isoFormat=");
         isoFormat.append(ds.getIsoFormat());
         // isoFormat.append("test");
         if (ds.getTarget() != null) {
             charset.append("charset=");
             charset.append(ds.getTarget().getCharset());
         }
         recordIdPolicy.append("recordIdPolicy=");
         recordIdPolicy.append(ds.getRecordIdPolicy().getType());
         if (ds.getRecordIdPolicy().getType().equals("idExported")) {
             idXpath.append("idXpath=");
 
             idXpath.append(ds.getRecordIdPolicy().getIdXpath());
             namespacePrefix.append("namespacePrefix=");
             namespacePrefix.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespacePrefix());
             namespaceUri.append("namespaceUri=");
             namespaceUri.append(ds.getRecordIdPolicy().getNamespaces().getNamespace().getNamespaceUri());
 
             recordXPath.append("recordXPath=");
             recordXPath.append(ds.getSplitRecords().getRecordXPath());
         }
         folder.append("folder=");
         folder.append("/tmp");
         // folder.append(ds.getFolder());
 
         if (action.equals("create")) {
             return invokeRestCall("/dataSources/createFolder", Response.class,
                     dataProviderId.toString(), id.toString(), description.toString(),
                     nameCode.toString(), name.toString(), exportPath.toString(), schema.toString(),
                     namespace.toString(), metadataFormat.toString(), isoFormat.toString(),
                     charset.toString(), recordIdPolicy.toString(), idXpath.toString(),
                     namespacePrefix.toString(), namespaceUri.toString(), recordXPath.toString(),
                     folder.toString());
         } else {
             return invokeRestCall("/dataSources/updateFolder", Response.class, id.toString(),
                     description.toString(), nameCode.toString(), name.toString(),
                     exportPath.toString(), schema.toString(), namespace.toString(),
                     metadataFormat.toString(), isoFormat.toString(), charset.toString(),
                     recordIdPolicy.toString(), idXpath.toString(), namespacePrefix.toString(),
                     namespaceUri.toString(), recordXPath.toString(), folder.toString());
         }
     }
 }
