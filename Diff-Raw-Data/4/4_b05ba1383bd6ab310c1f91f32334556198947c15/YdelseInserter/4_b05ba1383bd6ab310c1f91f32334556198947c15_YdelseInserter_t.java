 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.sdm4.ydelse.parser;
 
 import dk.nsi.sdm4.core.parser.ParserException;
 import dk.nsi.sdm4.ydelse.dao.SSRWriteDAO;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.annotation.AsyncResult;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Future;
 
 /**
  * Udfører de egentlige indsættelser af SSR-instanser i databasen ud fra en fil med SSR-linier.
  * Er særskilt Spring Bean for at kunne køre som @Async og dermed understøtte multitrådet-udførsel.
  */
 public class YdelseInserter {
 	private static final Logger log = Logger.getLogger(YdelseInserter.class);
 
 	@Autowired
 	SSRWriteDAO dao;
 
 	@Autowired
 	TransactionTemplate transactionTemplate;
 
	@Value("${spooler.ydelseimporter.batchsize}")
 	protected int batchSize = 1;
 
 	private int progressBatchSize = 10000;
 
 	List<SsrAction> batch = new ArrayList<SsrAction>(batchSize);
 
 	/**
 	 * Læser den angivne fil, parser hver linie og udfører de angivne operationer
 	 * @return Future, der kan bruges til at holde styr på, om processen er færdig
 	 */
 	public Future<Void> readFileAndPerformDatabaseOperations(File file) {
 		BufferedReader bf = null;
 		try {
 			bf = new BufferedReader(new FileReader(file));
 
 			long counter = 0;
 			String line;
 			while ((line = bf.readLine()) != null) {
 				SsrAction ssrAction = SSRLineParser.parseLine(line);
 				batch.add(ssrAction);
 				counter++;
 				if (counter % progressBatchSize == 0) {
 					log.info("Progress: " + counter);
 				}
 				if (batch.size() == batchSize) {
 					commitBatch();
 				}
 			}
 
 			commitBatch(); // commit den rest der kan være fra sidste gennemløb
 		} catch (Exception e) {
 			throw new ParserException("Could not parse file " + file.getAbsolutePath(), e);
 		} finally {
 			IOUtils.closeQuietly(bf);
 		}
 
 		return new AsyncResult<Void>(null); // bruges bare til at signalere completion
 	}
 
 	private void commitBatch() {
 		transactionTemplate.execute(new TransactionCallback<Void>() {
 			@Override
 			public Void doInTransaction(TransactionStatus status) {
 				if (batch.size() > 0) {
 					log.info("Committing batch of size " + batch.size());
 					for (SsrAction ssrAction : batch) {
 						ssrAction.execute(dao);
 					}
 					batch.clear();
 				}
 				return null; // kun for at gøre TransactionCallback-interfacet glad, ingen bruger en returværdi til noget
 			}
 		});
 	}
 }
