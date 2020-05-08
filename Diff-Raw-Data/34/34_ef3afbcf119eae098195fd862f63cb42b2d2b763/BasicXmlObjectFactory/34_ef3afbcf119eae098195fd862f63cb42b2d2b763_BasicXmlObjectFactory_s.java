 package eu.europeana.uim.repox.rest.utils;
 
 import org.theeuropeanlibrary.model.common.qualifier.Country;
 
 import eu.europeana.uim.repox.rest.client.xml.Aggregator;
 import eu.europeana.uim.repox.rest.client.xml.Source;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.StandardControlledVocabulary;
 
 /**
  * Utility functions to create xml generated objects.
  * 
  * @author Georgios Markakis
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Jan 24, 2012
  */
 public class BasicXmlObjectFactory implements XmlObjectFactory {
     @Override
     public Aggregator createAggregator(Provider<?> provider) {
         String countryCode = provider.getValue(StandardControlledVocabulary.COUNTRY);
         String countryName = null;
         if (countryCode == null) {
             countryCode = "eu";
             countryName = "European Union";
         } else {
             countryCode = countryCode.toLowerCase();
             if (countryCode.equals("xxx")) {
                 countryCode = "eu";
                 countryName = "European Union";
             } else {
                 Country country = Country.lookupCountry(countryCode, false);
                 if (country != null) {
                     countryCode = country.getIso2();
                     countryName = country.getName();
                 } else {
                     countryName = countryCode;
                 }
             }
         }
 
         Aggregator aggr = DummyXmlObjectCreator.createAggregator(countryName);
         aggr.setName(countryName);
         aggr.setNameCode(countryCode);
 
         return aggr;
     }
 
     @Override
     public eu.europeana.uim.repox.rest.client.xml.Provider createProvider(Provider<?> provider) {
        eu.europeana.uim.repox.rest.client.xml.Provider jaxbProvider = DummyXmlObjectCreator.createProvider(provider.getName());
 
         jaxbProvider.setName(provider.getName());
         jaxbProvider.setNameCode(provider.getMnemonic());
 
         String countrystr = provider.getValue(StandardControlledVocabulary.COUNTRY);
         if (countrystr != null) {
             Country country = Country.lookupCountry(countrystr.toLowerCase(), false);
             if (country != null) {
                 jaxbProvider.setCountry(country.getIso2());
             }
         }
 
         return jaxbProvider;
     }
 
     @Override
     public void updateProvider(Provider<?> provider,
             eu.europeana.uim.repox.rest.client.xml.Provider jaxbProvider) {
         jaxbProvider.setName(provider.getName());
         jaxbProvider.setNameCode(provider.getMnemonic());
 
         String countrystr = provider.getValue(StandardControlledVocabulary.COUNTRY);
         if (countrystr != null) {
             Country country = Country.lookupCountry(countrystr.toLowerCase(), false);
             if (country != null) {
                 jaxbProvider.setCountry(country.getIso2());
             }
         }
     }
 
     @Override
     public Source createDataSource(Collection<?> collection) {
         Source jaxbSource = DummyXmlObjectCreator.createOAIDataSource(collection.getMnemonic());
         jaxbSource.setId(collection.getMnemonic());
         jaxbSource.setNameCode(collection.getMnemonic());
         jaxbSource.setName(collection.getName());
         return jaxbSource;
     }
 
     @Override
     public void updateDataSource(Collection<?> collection, Source jaxbSource) {
         jaxbSource.setId(collection.getMnemonic());
         jaxbSource.setNameCode(collection.getMnemonic());
         jaxbSource.setName(collection.getName());
     }
 }
