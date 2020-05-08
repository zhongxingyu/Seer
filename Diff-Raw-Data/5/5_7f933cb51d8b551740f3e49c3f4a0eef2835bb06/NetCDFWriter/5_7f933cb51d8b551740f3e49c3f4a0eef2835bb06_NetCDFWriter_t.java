 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
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
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
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
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
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
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.application.arraydata;
 
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.file.FileManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.NetcdfFileWriteable;
 
 /**
  * Provides NetCDF formatting and storage capabilities.
  */
 class NetCDFWriter extends AbstractNetCdfFileHandler {
 
     private final ArrayDataValues values;
     private NetcdfFileWriteable writer;
 
     NetCDFWriter(ArrayDataValues values, FileManager fileManager) {
         super(fileManager);
         this.values = values;
     }
 
     void storeValues() {
         openNetCdfFileWriteable();
         writeValues();
         closeNetCdfFileWriteable();
     }
 
     private void openNetCdfFileWriteable() {
         File file = getFile(values);
         if (file.exists()) {
             openNetCdfFileWriteable(file);
         } else {
             createNetCdfFile(file);
         }
     }
 
     private void openNetCdfFileWriteable(File file) {
         try {
             writer = NetcdfFileWriteable.openExisting(file.getAbsolutePath(), false);
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't open the NetCDF file for writing", e);
         }
     }
 
     private void createNetCdfFile(File file) {
         try {
             writer = NetcdfFileWriteable.createNew(file.getAbsolutePath(), true);
             Dimension reporterDimension = writer.addDimension(REPORTER_DIMENSION_NAME, values.getReporters().size());
             Dimension arrayDataDimension = writer.addUnlimitedDimension(ARRAY_DATA_DIMENSION_NAME);
             Dimension[] dimensions = new Dimension[] {arrayDataDimension, reporterDimension};
             for (ArrayDataValueType valueType : values.getTypes()) {
                 writer.addVariable(valueType.name(), getDataType(valueType), dimensions);
             }
             writer.addVariable(ARRAY_DATA_IDS_VARIABLE, DataType.INT, new Dimension[] {arrayDataDimension});
             writer.create();
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't create the NetCDF file", e);
         }
     }
 
     private void closeNetCdfFileWriteable() {
         try {
             writer.close();
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't close the NetCDF file.", e);
         }
     }
 
     private void writeValues() {
         try {
             writeAllValues();
         } catch (IOException e) {
            throw new ArrayDataStorageException("Couldn't writes values. IOException.", e);
         } catch (InvalidRangeException e) {
            throw new ArrayDataStorageException("Couldn't writes values. InvalidRangeException", e);
         }
     }
 
     private void writeAllValues() throws IOException, InvalidRangeException {
         int[] shape = new int[] {1, values.getReporters().size()};
         Array arrayDataIdArray = Array.factory(DataType.INT, new int[] {1});
         int[] valuesOrigin = new int[] {0, 0};
         int[] arrayIdOrigin = new int[] {0};
         List<ArrayData> arrayDatas = values.getOrderedArrayDatas();
         for (ArrayData arrayData : arrayDatas) {
             valuesOrigin[0] = getArrayDataOffset(arrayData);
             if (!getArrayDataOffsets().containsKey(arrayData.getId())) {
                 arrayIdOrigin[0] = getArrayDataOffset(arrayData);
                 arrayDataIdArray.setLong(arrayDataIdArray.getIndex(), arrayData.getId());
                 writer.write(ARRAY_DATA_IDS_VARIABLE, arrayIdOrigin, arrayDataIdArray);
             }
             for (ArrayDataValueType valueType : values.getTypes()) {
                 Array valuesArray = Array.factory(valueType.getTypeClass(), shape);
                 Index valuesIndex = valuesArray.getIndex();
                 for (AbstractReporter reporter : values.getReporters()) {
                     valuesIndex.set(0, reporter.getIndex());
                     setValue(valuesArray, valuesIndex, arrayData, reporter, valueType);
                 }
                 writer.write(valueType.name(), valuesOrigin, valuesArray);
             }
           }
         }
 
     private void setValue(Array valuesArray, Index valuesIndex, ArrayData arrayData, AbstractReporter reporter, 
             ArrayDataValueType type) {
         if (Float.class.equals(type.getTypeClass())) {
             valuesArray.setFloat(valuesIndex, values.getFloatValue(arrayData, reporter, type));
         } else {
             throw new ArrayDataStorageException("Unsupported data type " + type.getTypeClass().getName());
         }
 
     }
 
     private int getArrayDataOffset(ArrayData arrayData) throws IOException {
         if (getArrayDataOffsets().containsKey(arrayData.getId())) {
             return getArrayDataOffsets().get(arrayData.getId());
         } else {
             return writer.findDimension(ARRAY_DATA_DIMENSION_NAME).getLength();
         }
     }
 
     @Override
     NetcdfFile getNetCdfFile() {
         return writer;
     }
 
 }
