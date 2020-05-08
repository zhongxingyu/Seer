 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The ProtExpress
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This ProtExpress Software License (the License) is between NCI and You. You (or
  * Your) shall mean a person or an entity, and all other entities that control,
  * are controlled by, or are under common control with the entity. Control for
  * purposes of this definition means (i) the direct or indirect power to cause
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares,
  * or (iii) beneficial ownership of such entity.
  *
  * This License is granted provided that You agree to the conditions described
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  * no-charge, irrevocable, transferable and royalty-free right and license in
  * its rights in the ProtExpress Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the ProtExpress Software; (ii) distribute and
  * have distributed to and by third parties the ProtExpress Software and any
  * modifications and derivative works thereof; and (iii) sublicense the
  * foregoing rights set out in (i) and (ii) to third parties, including the
  * right to license such rights to further third parties. For sake of clarity,
  * and not by way of limitation, NCI shall have no right of accounting or right
  * of payment from You or Your sub-licensees for the rights granted under this
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the
  * above copyright notice, this list of conditions and the disclaimer and
  * limitation of liability of Article 6, below. Your redistributions in object
  * code form must reproduce the above copyright notice, this list of conditions
  * and the disclaimer of Article 6 in the documentation and/or other materials
  * provided with the distribution, if any.
  *
  * Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: This product includes software
  * developed by 5AM and the National Cancer Institute. If You do not include
  * such end-user documentation, You shall include this acknowledgment in the
  * Software itself, wherever such third-party acknowledgments normally appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
  * to endorse or promote products derived from this Software. This License does
  * not authorize You to use any trademarks, service marks, trade names, logos or
  * product names of either NCI or 5AM, except as required to comply with the
  * terms of this License.
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this
  * Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software
  * into such third party proprietary programs and for informing Your
  * sub-licensees, including without limitation Your end-users, of their
  * obligation to secure any required permissions from such third parties before
  * incorporating the Software into such third party proprietary software
  * programs. In the event that You fail to obtain such permissions, You agree
  * to indemnify NCI for any claims against NCI by such third parties, except to
  * the extent prohibited by law, resulting from Your failure to obtain such
  * permissions.
  *
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the Software, or any derivative works of the
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.protexpress.service.impl;
 
 import gov.nih.nci.protexpress.data.persistent.Experiment;
 import gov.nih.nci.protexpress.data.persistent.ExperimentRun;
 import gov.nih.nci.protexpress.service.FormatConversionService;
 import gov.nih.nci.protexpress.xml.xar2_2.ExperimentArchiveType;
 import gov.nih.nci.protexpress.xml.xar2_2.ExperimentRunType;
 import gov.nih.nci.protexpress.xml.xar2_2.ExperimentType;
 import gov.nih.nci.protexpress.xml.xar2_2.ObjectFactory;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 
 /**
  * Format conversion service for the Xar.xml 2.2 format.
  *
  * @author Scott Miller
  */
 public class Xar22FormatConversionServiceImpl implements FormatConversionService {
 
     private static final QName QNAME = new QName("http://cpas.fhcrc.org/exp/xml", "ExperimentArchive");
     private static final String SCHEMA_LOCATION = "https://www.labkey.org/download/XarSchema/V2.2/expTypes.xsd";
 
     private JAXBContext jaxbContext;
 
     /**
      * Default constructor.
      *
      * @throws JAXBException thrown when the jaxb context can not be initialized
      */
     public Xar22FormatConversionServiceImpl() throws JAXBException {
         jaxbContext = JAXBContext.newInstance(ExperimentArchiveType.class.getPackage().getName());
     }
 
     /**
      * {@inheritDoc}
      */
     public void marshallExperiments(List<Experiment> experiments, File output) throws JAXBException {
         getNewMarshaller().marshal(convertToExperimentArchive(experiments), output);
     }
 
     /**
      * {@inheritDoc}
      */
     public void marshallExperiments(List<Experiment> experiments, OutputStream output) throws JAXBException {
         getNewMarshaller().marshal(convertToExperimentArchive(experiments), output);
     }
 
     /**
      * Method to get a new, configured marshaller.
      *
      * @return the marchaller
      * @throws JAXBException
      */
     private Marshaller getNewMarshaller() throws JAXBException {
         Marshaller marshaller = jaxbContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
         return marshaller;
     }
 
     /**
      * Converts the list of experiments to xar 2.2 archive data.
     *
      * @param experiments the experiments to convert
      * @return the xar 2.2 jaxb ready data
      */
     private JAXBElement<ExperimentArchiveType> convertToExperimentArchive(List<Experiment> experiments) {
         ObjectFactory objectFactory = new ObjectFactory();
         ExperimentArchiveType experimentArchive = objectFactory.createExperimentArchiveType();
         for (Experiment experiment : experiments) {
             ExperimentType xarExperiment = objectFactory.createExperimentType();
             xarExperiment.setName(experiment.getName());
             xarExperiment.setComments(experiment.getDescription());
             xarExperiment.setHypothesis(experiment.getHypothesis());
             xarExperiment.setExperimentDescriptionURL(experiment.getUrl());
             experimentArchive.getExperiment().add(xarExperiment);
 
            ExperimentArchiveType.ExperimentRuns xarExperimentRuns = objectFactory
                    .createExperimentArchiveTypeExperimentRuns();
            for (ExperimentRun expRun : experiment.getExperimentRuns()) {
                 ExperimentRunType xarExperimentRun = objectFactory.createExperimentRunType();
                 xarExperimentRun.setName(expRun.getName());
                 xarExperimentRun.setAbout(expRun.getAbout());
                 xarExperimentRun.setComments(expRun.getComments());
                 xarExperimentRuns.getExperimentRun().add(xarExperimentRun);
             }
             experimentArchive.setExperimentRuns(xarExperimentRuns);
         }
 
         return new JAXBElement<ExperimentArchiveType>(QNAME, ExperimentArchiveType.class, experimentArchive);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public List<Experiment> unmarshallExperiments(File input) throws JAXBException {
         Unmarshaller u = jaxbContext.createUnmarshaller();
         JAXBElement<ExperimentArchiveType> experimentArchive = (JAXBElement<ExperimentArchiveType>) u.unmarshal(input);
         return convertToExperiments(experimentArchive.getValue());
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public List<Experiment> unmarshallExperiments(InputStream input) throws JAXBException {
         Unmarshaller u = jaxbContext.createUnmarshaller();
         JAXBElement<ExperimentArchiveType> experimentArchive = (JAXBElement<ExperimentArchiveType>) u.unmarshal(input);
         return convertToExperiments(experimentArchive.getValue());
     }
 
     /**
      * converts the xar 2.2 experiment archive data to the internal data model.
      *
      * @param experimentArchive the experiment archive
      * @return the list of experiments.
      */
     private List<Experiment> convertToExperiments(ExperimentArchiveType experimentArchive) {
         List<ExperimentType> xarExperiments = experimentArchive.getExperiment();
         List<Experiment> experiments = new ArrayList<Experiment>();
         for (ExperimentType xarExperimentType : xarExperiments) {
             Experiment experiment = new Experiment(xarExperimentType.getName());
             experiment.setDescription(xarExperimentType.getComments());
             experiment.setHypothesis(xarExperimentType.getHypothesis());
             experiment.setUrl(xarExperimentType.getExperimentDescriptionURL());
 
            // Get ExperimentRuns.
             ExperimentArchiveType.ExperimentRuns xarExpRuns = experimentArchive.getExperimentRuns();
            for (ExperimentRunType xarExpRunType : xarExpRuns.getExperimentRun()) {
                 ExperimentRun expRun = new ExperimentRun(xarExpRunType.getName());
                 expRun.setComments(xarExpRunType.getComments());
                 expRun.setAbout(xarExpRunType.getAbout());
                experiment.getExperimentRuns().add(expRun);
             }
            experiments.add(experiment);
         }
 
         return experiments;
     }
 }
