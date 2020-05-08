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
 package gov.nih.nci.protexpress.ui.actions.experiment.create;
 
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

 import gov.nih.nci.protexpress.domain.protocol.InputOutputObject;
 import gov.nih.nci.protexpress.domain.protocol.ProtocolApplication;
 import gov.nih.nci.protexpress.util.SessionHelper;
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 import com.opensymphony.xwork2.validator.annotations.Validation;
 
 /**
  * An abstract base action class for all actions related to the Create Experiment process.
  *
  * @author Krishna Kanchinadam
  */
 
 @Validation
 public abstract class AbstractProtocolApplicationAction extends AbstractCreateExperimentAction implements Preparable {
     private static final long serialVersionUID = 1L;
 
     private ProtocolApplication protocolApplication = new ProtocolApplication(
             "ProtocolApplication", null, null, null);
     private Long protocolApplicationId;
     private Long deleteIndex;
 
     /**
      * Action Constructor.
      */
     public AbstractProtocolApplicationAction() {
         super();
     }
 
     /**
      * {@inheritDoc}
      */
     public void prepare() throws Exception {
         setExperimentInformation();
         if (SessionHelper.getProtocolApplicationFromSession() != null) {
             setProtocolApplication(SessionHelper.getProtocolApplicationFromSession());
         }
     }
 
     /**
      * Gets the protocolApplication.
      *
      * @return the protocolApplication.
      */
     public ProtocolApplication getProtocolApplication() {
         return protocolApplication;
     }
 
     /**
      * Gets the protocolApplicationId.
      *
      * @return the protocolApplicationId.
      */
     public Long getProtocolApplicationId() {
         return protocolApplicationId;
     }
 
     /**
      * Sets the protocolApplicationId.
      *
      * @param protocolApplicationId the protocolApplicationId to set.
      */
     public void setProtocolApplicationId(Long protocolApplicationId) {
         this.protocolApplicationId = protocolApplicationId;
     }
 
     /**
      * Sets the protocolApplication.
      *
      * @param protocolApplication the protocolApplication to set.
      */
     public void setProtocolApplication(ProtocolApplication protocolApplication) {
         this.protocolApplication = protocolApplication;
     }
 
     /**
      * Resets the protocolApplication.
      */
     public void resetProtocolApplication() {
         this.protocolApplication = new ProtocolApplication("ProtocolApplication", null, null, null);
     }
 
     /**
      * Update the protocol inputs/outputs, save the protocol application object to session.
      *
      * @param lst the list of inputs/outputs.
      * @return the directive for the next action/page to be directed to.
      */
     @SkipValidation
     public String saveInputsOutputsToSession(List<InputOutputObject> lst) {
         ListIterator<InputOutputObject> listIter = lst.listIterator();
         while (listIter.hasNext()) {
             InputOutputObject ioObject = listIter.next();
             if (StringUtils.isBlank(ioObject.getName())
                     && StringUtils.isBlank(ioObject.getDataFileURL())
                     && StringUtils.isBlank(ioObject.getNotes())) {
                 listIter.remove();
             }
         }
         SessionHelper.saveProtocolApplicationInSession(getProtocolApplication());
         return ActionSupport.SUCCESS;
     }
 
     /**
      * Deletes the specified input from the protocol application.
      *
      * @return the directive for the next action / page to be directed to.
      */
     @SkipValidation
     public String deleteInput() {
         if (getDeleteIndex().intValue() > 0) {
             deleteInputOutput(getProtocolApplication().getInputs(), getDeleteIndex().intValue());
         }
         return ActionSupport.INPUT;
     }
 
     /**
      * Deletes the specified output from the protocol application.
      *
      * @return the directive for the next action / page to be directed to
      */
     @SkipValidation
     public String deleteOutput() {
         if (getDeleteIndex().intValue() > 0) {
             deleteInputOutput(getProtocolApplication().getOutputs(), getDeleteIndex().intValue());
         }
         return ActionSupport.INPUT;
     }
 
     /**
      * Deletes the object at the specified index from the list.
      *
      * @param lst the list.
      * @param index the index of the object to be deleted.
      */
     private void deleteInputOutput(List<InputOutputObject> lst, int index) {
         if (lst != null) {
             lst.remove(index);
             SessionHelper.saveProtocolApplicationInSession(getProtocolApplication());
         }
     }
 
     /**
      * Gets the deleteIndex.
      *
      * @return the deleteIndex.
      */
     public Long getDeleteIndex() {
         return deleteIndex;
     }
 
     /**
      * Sets the deleteIndex.
      *
      * @param deleteIndex the deleteIndex to set.
      */
     public void setDeleteIndex(Long deleteIndex) {
         this.deleteIndex = deleteIndex;
     }
 
 }
