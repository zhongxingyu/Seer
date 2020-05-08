 /*
     Copyright (C) 2012 Johan Grufman
 
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
 */
 package org.convx.examples.csv;
 
 import org.convx.fsd.SchemaBuilder;
 import org.convx.schema.Schema;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.stream.XMLEventReader;
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.text.ParseException;
 
 import static org.convx.examples.ResourceUtil.getResource;
 
 /**
  * Reads a csv file with personal data for a bunch of people and prints the name of each person that is born in the
  * seventies. It is meant as brief introductory example of how one might use convx together with jaxb to parse flat
  * file data. Check out the pom.xml to see one way of converting the flat file schema ("csv.fsd") into an XML Schema
  * that is used to generate data binding classes.
  *
  * @author johan
  * @since 2012-02-26
  */
 public class CsvExample {
 
     public void readCsv(PrintWriter writer) throws Exception {
        File schemaFile = getResource("csv/csv.fsd");
        File csvFile = getResource("csv/csv.txt");
        Schema schema = SchemaBuilder.build(schemaFile);
        XMLEventReader parser = schema.parser(new FileReader(csvFile));
         Persons persons = unmarshal(parser);
         for (Persons.Person person : persons.getPerson()) {
             if (bornInTheSeventies(person)) {
                 printPerson(person, writer);
             }
         }
     }
 
     private boolean bornInTheSeventies(Persons.Person person) throws ParseException {
         int year = person.getDateOfBirth().getYear();
         return year >= 1970 && year <= 1979;
     }
 
     private void printPerson(Persons.Person person, PrintWriter writer) {
         writer.println(String.format("Name: %s %s", person.getFirstName(), person.getLastName()));
     }
 
     private Persons unmarshal(XMLEventReader xmlEventReader) throws JAXBException, FileNotFoundException {
         JAXBContext jc = JAXBContext.newInstance("org.convx.examples.csv");
         return (Persons) jc.createUnmarshaller().unmarshal(xmlEventReader);
     }
 
 
     public static void main(String[] args) throws Exception {
         new CsvExample().readCsv(new PrintWriter(System.out));
     }
 }
