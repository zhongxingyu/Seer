 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.application.arraydata;
 
 import gov.nih.nci.caintegrator.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator.domain.genomic.DnaAnalysisReporter;
 import gov.nih.nci.caintegrator.domain.genomic.Gene;
 import gov.nih.nci.caintegrator.domain.genomic.Platform;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterTypeEnum;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Loader for Affymetrix Copy Number array designs.
  */
 
 class AffymetrixCnPlatformLoader extends AbstractPlatformLoader {
 
     static final String DBSNP_RS_ID_HEADER = "dbSNP RS ID";
     private static final Logger LOGGER = Logger.getLogger(AffymetrixCnPlatformLoader.class);
     private static final String CHIP_TYPE_HEADER = "chip_type";
     private static final String CHIP_TYPE_SNP6 = "GenomeWideSNP_6";
     private static final String VERSION_HEADER = "netaffx-annotation-netaffx-build";
     private static final String GENOME_VERSION_HEADER = "genome-version";
     private static final String PROBE_SET_ID_HEADER = "Probe Set ID";
     private static final String GENE_SYMBOL_HEADER = "Associated Gene";
     private static final String NO_VALUE_INDICATOR = "---";
     private static final String CHROMOSOME_HEADER = "Chromosome";
     private static final String POSITION_HEADER = "Physical Position";
     private static final String POSITION_HEADER_SNP_6 = "Chromosome Start";
     private Map<String, String> fileHeaders;
 
     AffymetrixCnPlatformLoader(AffymetrixCnPlatformSource source) {
         super(source);
     }
 
     @Override
     Platform load(CaIntegrator2Dao dao) throws PlatformLoadingException {
         Platform platform = createPlatform(PlatformVendorEnum.AFFYMETRIX);
         platform.setName(getPlatformName());
         loadAnnotationFiles(platform, dao);
         return platform;
     }
 
     @Override
     public String getPlatformName() throws PlatformLoadingException {
         return getSource().getPlatformName();
     }
 
     @Override
     void handleAnnotationFile(File annotationFile, Platform platform, CaIntegrator2Dao dao)
     throws PlatformLoadingException {
         try {
             setAnnotationFileReader(new CSVReader(new FileReader(annotationFile)));
             loadHeaders();
             platform.setVersion(getHeaderValue(VERSION_HEADER));
             ReporterList reporterList =
                 platform.addReporterList(getHeaderValue(CHIP_TYPE_HEADER), ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
             reporterList.setGenomeVersion(getHeaderValue(GENOME_VERSION_HEADER));
             loadAnnotations(reporterList, dao);
             reporterList.sortAndLoadReporterIndexes();
         } catch (IOException e) {
             throw new PlatformLoadingException("Couldn't read annotation file " + annotationFile.getName(), e);
         }
     }
 
     private void loadAnnotations(ReporterList reporterList, CaIntegrator2Dao dao) throws IOException {
         String[] fields;
         while ((fields = getAnnotationFileReader().readNext()) != null) {
             loadAnnotations(fields, reporterList, dao);
         }
     }
 
     private void loadAnnotations(String[] fields, ReporterList reporterList, CaIntegrator2Dao dao) {
         String[] symbols = getSymbols(fields);
         String probeSetName = getAnnotationValue(fields, PROBE_SET_ID_HEADER);
         Set<Gene> genes = getGenes(symbols, fields, dao);
         handleProbeSet(probeSetName, genes, fields, reporterList);
     }
 
     private void handleProbeSet(String probeSetName, Set<Gene> genes, String[] fields, ReporterList reporterList) {
         DnaAnalysisReporter reporter = new DnaAnalysisReporter();
         reporter.setName(probeSetName);
         reporterList.getReporters().add(reporter);
         reporter.setReporterList(reporterList);
         reporter.getGenes().addAll(genes);
         reporter.setChromosome(getAnnotationValue(fields, CHROMOSOME_HEADER, NO_VALUE_INDICATOR));
         reporter.setPosition(getIntegerValue(fields, getPositionHeader()));
     }
 
    private Integer getIntegerValue(String[] fields, String header) {
         String value = getAnnotationValue(fields, header);
         if (!NumberUtils.isNumber(value)) {
            return null;
         }
         return Integer.parseInt(value);
     }
 
     private Set<Gene> getGenes(String[] symbols, String[] fields, CaIntegrator2Dao dao) {
         Set<Gene> genes = new HashSet<Gene>(symbols.length);
         for (String symbol : symbols) {
             Gene gene = getSymbolToGeneMap().get(symbol.toUpperCase(Locale.getDefault()));
             if (gene == null && !symbol.equals(NO_VALUE_INDICATOR)) {
                 gene = lookupOrCreateGene(fields, symbol, dao);
             }
             if (gene != null) {
                 genes.add(gene);
             }
         }
         return genes;
     }
 
     private String[] getSymbols(String[] fields) {
         //
         // This method parses the value from the gene symbol column
         // which is obtained from the manufacturers platform annotation file.
         // This involves breaking the string down into substrings and
         // then finally extracting the gene symbol.
         //
         // An example of this value is as follows:
         // "NM_181714 // intron // 0 // Hs.21945 // LCA5 // 167691 // Leber congenital amaurosis 5
         // /// NM_001122769 // intron // 0 // Hs.21945 // LCA5 // 167691 // Leber congenital amaurosis 5
         //
         // Note in the above string, the top level separator is /// (3 forward slashes)
         // and the second level separator is // (2 forward slashes)
         // A second example of this value is as follows:
         // LCA5 /// LCA5
 
         // Get the gene symbol field and separate into substrings.
         String[] subField = getAnnotationValue(fields, GENE_SYMBOL_HEADER).split("///");
 
         // extract the symbols from the array of substrings
         Set<String> symbolsSet = parseSubString(subField);
 
         // convert to array
         String[] symbols = symbolsSet.toArray(new String[symbolsSet.size()]);
 
         return symbols;
     }
 
     private Set<String> parseSubString(String[] subField) {
         Set<String> symbols = new HashSet<String>();
         for (String subfield : subField) {
             String tempStr = parseSubString2(subfield);
             if (!StringUtils.isBlank(tempStr)) {
                 symbols.add(tempStr);
             }
         }
         return symbols;
      }
 
 
     private String parseSubString2(String subField) {
         String[] holdSymbols = subField.split("//");
         String symbol = "";
 
         if (holdSymbols.length == 1) {
             if (!holdSymbols[0].trim().equalsIgnoreCase(NO_VALUE_INDICATOR)) {
                 symbol = holdSymbols[0].trim();
             }
         } else if ((holdSymbols.length > 4)
                     && (!holdSymbols[4].trim().equalsIgnoreCase(NO_VALUE_INDICATOR))) {
             symbol = holdSymbols[4].trim();
         }
 
         return symbol;
     }
 
     private String getHeaderValue(String headerName) {
         return fileHeaders.get(headerName);
     }
 
     private void loadHeaders() throws PlatformLoadingException, IOException {
         AffymetrixAnnotationHeaderReader headerReader = new AffymetrixAnnotationHeaderReader(
                 getAnnotationFileReader());
         fileHeaders = headerReader.getFileHeaders();
         loadAnnotationHeaders(headerReader.getDataHeaders(), getRequiredHeaders());
     }
 
     @Override
     Logger getLogger() {
         return LOGGER;
     }
 
     /**
      * @return the positionHeader
      */
     public String getPositionHeader() {
         return CHIP_TYPE_SNP6.equals(getHeaderValue(CHIP_TYPE_HEADER))
             ? POSITION_HEADER_SNP_6 : POSITION_HEADER;
     }
 
     /**
      * @return the requiredHeaders
      */
     private String[] getRequiredHeaders() {
         String [] holdString = {PROBE_SET_ID_HEADER, GENE_SYMBOL_HEADER,
                 CHROMOSOME_HEADER, getPositionHeader()};
         return holdString;
     }
 
 }
