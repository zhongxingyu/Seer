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
 package eu.europeana.uim.repoxclient.utils;
 
 import java.math.BigInteger;
 
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.repoxclient.jibxbindings.Address;
 import eu.europeana.uim.repoxclient.jibxbindings.Aggregator;
 import eu.europeana.uim.repoxclient.jibxbindings.Charset;
 import eu.europeana.uim.repoxclient.jibxbindings.Country;
 import eu.europeana.uim.repoxclient.jibxbindings.Database;
 import eu.europeana.uim.repoxclient.jibxbindings.Description;
 import eu.europeana.uim.repoxclient.jibxbindings.EarliestTimestamp;
 import eu.europeana.uim.repoxclient.jibxbindings.Folder;
 import eu.europeana.uim.repoxclient.jibxbindings.FtpPath;
 import eu.europeana.uim.repoxclient.jibxbindings.IdXpath;
 import eu.europeana.uim.repoxclient.jibxbindings.IsoFormat;
 import eu.europeana.uim.repoxclient.jibxbindings.Name;
 import eu.europeana.uim.repoxclient.jibxbindings.NameCode;
 import eu.europeana.uim.repoxclient.jibxbindings.Namespace;
 import eu.europeana.uim.repoxclient.jibxbindings.NamespacePrefix;
 import eu.europeana.uim.repoxclient.jibxbindings.NamespaceUri;
 import eu.europeana.uim.repoxclient.jibxbindings.Namespaces;
 import eu.europeana.uim.repoxclient.jibxbindings.OaiSet;
 import eu.europeana.uim.repoxclient.jibxbindings.OaiSource;
 import eu.europeana.uim.repoxclient.jibxbindings.Password;
 import eu.europeana.uim.repoxclient.jibxbindings.Port;
 import eu.europeana.uim.repoxclient.jibxbindings.RecordIdPolicy;
 import eu.europeana.uim.repoxclient.jibxbindings.RecordSyntax;
 import eu.europeana.uim.repoxclient.jibxbindings.RecordXPath;
 import eu.europeana.uim.repoxclient.jibxbindings.RetrieveStrategy;
 import eu.europeana.uim.repoxclient.jibxbindings.Server;
 import eu.europeana.uim.repoxclient.jibxbindings.SplitRecords;
 import eu.europeana.uim.repoxclient.jibxbindings.Target;
 import eu.europeana.uim.repoxclient.jibxbindings.Type;
 import eu.europeana.uim.repoxclient.jibxbindings.Url;
 import eu.europeana.uim.repoxclient.jibxbindings.Source;
 import eu.europeana.uim.repoxclient.jibxbindings.User;
 import eu.europeana.uim.repoxclient.jibxbindings.Source.Choice;
 import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence;
 import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence1;
 import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence2;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 
 
 /**
  * 
  * @author Georgios Markakis
  */
 public class JibxObjectProvider {
 
 	private static final String defaultAggrgatorURL = "http://repox.ist.utl.pt";
 	private static final String defaultAggrgatorIDPostfix = "aggregatorr0";	
 	private static final String defaultcountry = "eu";	
 	
 	public static Aggregator createAggregator(String countryCode,String urlString){
 		
 		if(countryCode.equals("")){
 			countryCode ="eu";
 		}
 		
 		String aggrName = countryCode + "aggregator";
 		Aggregator aggr = new Aggregator();
 
 		Name name = new Name();
 		name.setName(aggrName);
 		aggr.setName(name);
 		NameCode namecode = new NameCode();
 		namecode.setNameCode(aggrName);
 		aggr.setNameCode(namecode);
 		Url url = new Url();
 		
 		if(urlString == null){
 			url.setUrl(defaultAggrgatorURL);
 		}
 		else{
 			url.setUrl(urlString);
 		}
 		
 		aggr.setUrl(url);
 		
 		return aggr;	
 	}
 	
 	
 	
 	
 	/**
 	 * @param uimProv
 	 * @return
 	 */
 	public static eu.europeana.uim.repoxclient.jibxbindings.Provider createProvider(Provider uimProv){
 		eu.europeana.uim.repoxclient.jibxbindings.Provider jibxProv = new eu.europeana.uim.repoxclient.jibxbindings.Provider();
 
 		Name name = new Name();
 		name.setName(uimProv.getName());
 		jibxProv.setName(name);
 		NameCode namecode = new NameCode();
 		namecode.setNameCode(uimProv.getMnemonic());
 		jibxProv.setNameCode(namecode);
 		Url url = new Url();
 		
 		String urlstr =uimProv.getValue(ControlledVocabularyProxy.PROVIDERWEBSITE);
 			
 		if(urlstr == null){
 			urlstr = defaultAggrgatorURL;
 		}
 		
 		url.setUrl(urlstr);
 		jibxProv.setUrl(url);
 		
 		Description description = new Description();
 		description.setDescription(uimProv.getValue(ControlledVocabularyProxy.PROVIDERDESCRIPTION));
 		jibxProv.setDescription(description);
 		
 		String countrystr = uimProv.getValue(ControlledVocabularyProxy.PROVIDERCOUNTRY).toLowerCase();
 		
 		if(countrystr == null){
 			countrystr = defaultcountry;
 		}
 		
 		Country country =  new Country();
 		country.setCountry(countrystr);
 		jibxProv.setCountry(country);
 		
 		Type type = new Type();
 		
 		String typevalue = mapSugar2RepoxOrgTypeValue(uimProv.getValue(ControlledVocabularyProxy.PROVIDERTYPE));
 		
 		type.setType(typevalue);
 		
 		jibxProv.setType(type);
 		
 		return jibxProv;
 	}
 	
 	
 	public static Source createDataSource(Collection col,DSType harvestingtype){
 		
 		Source ds = new Source();
 		
 		String id = (col.getMnemonic());
 		ds.setId(id);
 		Description des = new Description();
 		des.setDescription(col.getValue(ControlledVocabularyProxy.DESCRIPTION));
 		ds.setDescription(des);
 		ds.setNameCode(col.getMnemonic());
 		ds.setName(col.getName());
 		ds.setExportPath("");
 		ds.setSchema(col.getValue(ControlledVocabularyProxy.METADATA_SCHEMA));
 		ds.setNamespace(col.getValue(ControlledVocabularyProxy.METADATA_NAMESPACE));
 		
 		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
 		recordIdPolicy.setType("IdGenerated");
 		
 		ds.setRecordIdPolicy(recordIdPolicy );
 		
 		switch(harvestingtype){
 		
 		case oai_pmh:
 
 			ds.setMetadataFormat(col.getOaiMetadataPrefix(false));
 
 			Sequence seq = new Sequence();
 			OaiSet oaiSet = new OaiSet();
 			oaiSet.setOaiSet(col.getOaiSet());
 			seq.setOaiSet(oaiSet);
 			OaiSource oaiSource = new OaiSource();
 			oaiSource.setOaiSource(col.getOaiBaseUrl(true));
 			seq.setOaiSource(oaiSource);
 			ds.setSequence(seq);
 			
 			break;
 		
 		case z39_50:
 
 			Sequence2 seq2 = new Sequence2();
 			Target target = new Target();
 			Address address = new Address();
 			address.setAddress(col.getValue(ControlledVocabularyProxy.Z3950ADDRESS));
 			target.setAddress(address);
 			Port port = new Port();
 			port.setPort(BigInteger.valueOf(new Long(col.getValue(ControlledVocabularyProxy.Z3950PORT))));
 			target.setPort(port);
 			Database database = new Database();
 			database.setDatabase(col.getValue(ControlledVocabularyProxy.Z3950DATABASE));
 			target.setDatabase(database);
 			User user = new User();
 			user.setUser(col.getValue(ControlledVocabularyProxy.FTP_Z3950_USER));
 			target.setUser(user);
 			Password password = new Password();
 			password.setPassword(col.getValue(ControlledVocabularyProxy.FTP_Z3950_PASSWORD));
 			target.setPassword(password);
 			RecordSyntax recordSyntax = new RecordSyntax();
 			recordSyntax.setRecordSyntax(col.getValue(ControlledVocabularyProxy.Z3950RECORD_SYNTAX));
 			target.setRecordSyntax(recordSyntax);
 			Charset charset = new Charset();
 			charset.setCharset(col.getValue(ControlledVocabularyProxy.Z3950CHARSET));
 			target.setCharset(charset);
 			seq2.setTarget(target);
 			ds.setSequence2(seq2);
 			Choice choice = new Choice();
 			EarliestTimestamp earliestTimestamp = new EarliestTimestamp();
 			earliestTimestamp.setEarliestTimestamp(BigInteger.valueOf(new Long(col.getValue(ControlledVocabularyProxy.Z3950EARLIEST_TIMESTAMP))));
 			choice.setEarliestTimestamp(earliestTimestamp);
 			ds.setChoice(choice);
 			
 			break;
 			
 		case ftp:
 			Sequence2 seqftp2 = new Sequence2();
 			Target targetftp = new Target();
 			
 			
 			Charset charsetftp = new Charset();
 			charsetftp.setCharset(col.getValue(ControlledVocabularyProxy.Z3950CHARSET));
 			targetftp.setCharset(charsetftp);
 			seqftp2.setTarget(targetftp);
 			ds.setSequence2(seqftp2);
 			ds.setMetadataFormat(col.getValue(ControlledVocabularyProxy.METADATA_FORMAT));
 			Choice choiceftp = new Choice();
 			IsoFormat isoFormat = new IsoFormat();
 			isoFormat.setIsoFormat(col.getValue(ControlledVocabularyProxy.FTP_HTTP_ISOFORMAT));
 			choiceftp.setIsoFormat(isoFormat);
 			choiceftp.clearChoiceSelect();
 			FtpPath ftpPath = new FtpPath();
 			ftpPath.setFtpPath(col.getValue(ControlledVocabularyProxy.FTPPATH));
 			choiceftp.setFtpPath(ftpPath);
 			ds.setChoice(choiceftp);
 		
 			Sequence1 seqftp1 = new Sequence1();
 			RetrieveStrategy retrieveStrategy = new RetrieveStrategy();
 			RetrieveStrategy.Choice choiceRetStr = new RetrieveStrategy.Choice();
 			User ftpuser = new User();
 			ftpuser.setUser(col.getValue(ControlledVocabularyProxy.FTP_Z3950_USER));
 			
 			Password ftppassword = new Password();
 			ftppassword.setPassword(col.getValue(ControlledVocabularyProxy.FTP_Z3950_PASSWORD));
 			choiceRetStr.setUser(ftpuser);
 			choiceRetStr.setPassword(ftppassword);
 			Server server = new Server();
 			server.setServer(col.getValue(ControlledVocabularyProxy.FTPSERVER));
 			choiceRetStr.setServer(server);
 			retrieveStrategy.setChoice(choiceRetStr);
 			seqftp1.setRetrieveStrategy(retrieveStrategy);
 			ds.setSequence1(seqftp1);
 	
 			
 			break;
 			
 		case http:
 			Sequence2 seq2http = new Sequence2();
 			Target targethttp = new Target();
 			
 			Charset charsethttp = new Charset();
 			charsethttp.setCharset(col.getValue(ControlledVocabularyProxy.Z3950CHARSET));
 			targethttp.setCharset(charsethttp);
 			seq2http.setTarget(targethttp);
 			
 			ds.setSequence2(seq2http);
 			ds.setMetadataFormat(col.getValue(ControlledVocabularyProxy.METADATA_FORMAT));
 			Choice choicehttp = new Choice();
 			IsoFormat isoFormathttp = new IsoFormat();
 			isoFormathttp.setIsoFormat(col.getValue(ControlledVocabularyProxy.FTP_HTTP_ISOFORMAT));
 			choicehttp.setIsoFormat(isoFormathttp);
 			
 			ds.setChoice(choicehttp);
 
 			Sequence1 seq1http = new Sequence1();
 			RetrieveStrategy retrieveStrategyhttp = new RetrieveStrategy();
 			RetrieveStrategy.Choice choiceRetStrhttp = new RetrieveStrategy.Choice();
 			
 			Url url = new Url();
 			url.setUrl(col.getValue(ControlledVocabularyProxy.HTTPURL));
 			choiceRetStrhttp.setUrl(url);
 			retrieveStrategyhttp.setChoice(choiceRetStrhttp);
 			seq1http.setRetrieveStrategy(retrieveStrategyhttp);
 			ds.setSequence1(seq1http);
 			
 			break;
 		
 		case folder:
 			Sequence2 seqfolder = new Sequence2();
 			Target targetfolder = new Target();
 			
 			Charset charsetfolder = new Charset();
 			charsetfolder.setCharset(col.getValue(ControlledVocabularyProxy.Z3950CHARSET));
 			targetfolder.setCharset(charsetfolder);
 			seqfolder.setTarget(targetfolder);
 			ds.setSequence2(seqfolder);
 			ds.setMetadataFormat(col.getValue(ControlledVocabularyProxy.METADATA_FORMAT));
 			
 			Choice choicefolder = new Choice();
 			
 			IsoFormat isoFormatfolder = new IsoFormat();
 			isoFormatfolder.setIsoFormat(col.getValue(ControlledVocabularyProxy.FTP_HTTP_ISOFORMAT));
 			choicefolder.setIsoFormat(isoFormatfolder);
 			choicefolder.clearChoiceSelect();
 			Folder folder = new Folder();
 			folder.setFolder(col.getValue(ControlledVocabularyProxy.FOLDER));
 			choicefolder.setFolder(folder);
 			ds.setChoice(choicefolder);
 
 
 			break;
 		
 		}
 
 
 		
 		
 		return ds;
 	}
 	
 	
 	/**
 	 * @param sugarvalue
 	 * @return
 	 */
 	private static String mapSugar2RepoxOrgTypeValue(String sugarvalue){
 		
 		DataSetType[] enumvalues =DataSetType.values();
 		
 		for(int i=0; i<enumvalues.length; i++){
 			if(enumvalues[i].getSugarName().equals(sugarvalue)){
 				return enumvalues[i].toString();
 			}
 		}
 		
		return "UNKNOWN";
 	}
 }
