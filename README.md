# Structural Code Transform Prediction on AST Nodes

This repository contains the code and data set for our approach to structurally predicting code transforms at the level of AST nodes using conditional random fields (CRFs). Our approach first learns offline a probabilistic model that captures how certain code transforms are applied to certain AST nodes, and then uses the learned model to predict transforms for new, unseen code. The approach is instantiated in the context of repair transform prediction for Java programs and a large-scale experimental evaluation has shown that the accuracy of the approach is promising.  

## Structure of the repository 

- [CRF-Transform-Prediction](./CRF-Transform-Prediction) contains the code for CRF model setup, CRF model learning and prediction.
- [Test-Data](./Test-Data) contains the data set used for testing the performance of the learned CRF models.
- [Training-Data](./Training-Data) contains the data set used for training CRF models. 
- [Transform-Extraction](./Transform-Extraction) contains the code for extracting repair transforms on AST nodes from raw patch diffs and code for analyzing the characteristics of input nodes (to establish observation-based feature functions).
- [Diff-Raw-Data](./Diff-Raw-Data) contains the raw patch diffs from which we extract training and test data sets, and the whole patch diffs are distributed in 35 folders. 
- [Seq2Seq-Prediction](./Seq2Seq-Prediction) contains the data and scripts used to build the baseline based on the Seq2Seq translation model, and the Seq2Seq baseline is built on top of OpenNMT and the scripts are run on HPC2N (https://hal.inria.fr/hal-02091516/document). 

## Notes about training and test data sets

We split the whole data set into 10 folds, with each fold having the same number of instances for each kind of repair transform (for “single-transform” case) and same number of “multiple-transform” instances. We select one fold as test data set to see the model performance. To facilate the investigation of the performance of the trained model for each kind of transform, we explictly list the instances for each kind of transform in the “Test-Data” folder. The other 9 folds are used as the training data set and we use cross-validation to investigate the impact of the parameters and select them accordingly. We evaluate the error rate on each fold by training a model on the other 8 folds. The error rate is established using the top-3 evaluation metric described in the paper. We repeat this process by using a set of different parameters and aim to identify parameters with the lowest error rate. Our CRF model contains three parameters: the regularization parameter &#963; <sup>2</sup>, the parameter *q* that determines the magnitude of the distribution-aware prior, and the L-BFGS iteration parameter *G*. The three folders “All-Training-Data”, “70%-Training-Data”, and “30%-Training-Data” in the “Training-Data” folder correspond to the case that we respectively use 100%, 70% , and 30% of the whole training data to build the model. In other words, for each kind of repair transform (including “multiple-transform” kind), we respectively select 70% and 30% of the instances in the whole training data for it to build partial models.           

## Usage and paper result reproduction

Pre-requisites:
* You will need **jdk 8** installed
* You will need **maven** installed 

### Step 0: Cloning the repository and package
```
git clone https://github.com/TransformPrediction/CodeTransform.git
cd CodeTransform
```
Note as the repository contains a lot of data, it will take quite a while to clone the repository.

After the clone process, package the programs. First, package the program for extracting repair transforms on AST nodes.
```
cd Transform-Extraction
mvn package -DskipTests=true
```
The JAR file `Transform-Extraction-0.1-SNAPSHOT-jar-with-dependencies.jar` will be created and (for simplicity) it will be referenced as `Extraction.jar` for the rest of this guide. 

Then, package the program for CRF model setup, learning, and prediction.
```
cd CRF-Transform-Prediction
mvn package -DskipTests=true
```
The JAR file `CRF-Transform-Prediction-0.1-SNAPSHOT.jar` will be created and (for simplicity) it will be referenced as `CRF.jar` for the rest of this guide. 

### Step 1: Extracting transforms on AST nodes and analysis of characteristics of AST nodes 

To train the CRF model, our approach needs training data with repair transforms attached to AST nodes. In addition, to establish the observation-based feature functions in CRF, our approach needs to analyze the characteristics of AST nodes. To achieve this, use the following command:  
```
java -cp Extraction.jar diffson.ExperimentRunnerMain /path/to/raw-diffs /path/to/store-result 
```
For a certain bug-fixing commit, the output is a json file which contains a set of json node tuples <faulty_ast, context>. Each json node tuple <faulty_ast, context> is an atomic data unit where "faulty_ast" represents the AST for the changed statement and "context" represents the analyzed characteristics of certain AST nodes. The "susp" json node attached with the "faulty_ast" represents that a certain AST node is subject to a certain repair transform, i.e., the value of the "susp" json node. As a bug-fixing commit can contain bug-fixing changes to several files and for a changed file, it can change several statements occasionally even if we have limited the number of root tree edit operations (as detailed in the paper), a json file may contain several json node tuples <faulty_ast, context>. 

By setting the path of raw diffs respectively to each folder in [Diff-Raw-Data](./Diff-Raw-Data), the training and test data sets can be obtained. As the cluster we use to conduct the experiments has a restriction on the length of the file names, the json files for the training and test data sets have been renamed with integers.

### Step 2: CRF setup

The two kinds of feature functions in our CRF and the constraint on the admissible repair transforms over AST nodes will rely on the training data. In addition, the other history probability baseline also makes use of the training data. To establish the CRF and the history probability baseline, use the following command: 
```
java -cp CRF.jar FileParse.CRFEstablish /path/to/training-data /path/to/store-initial-CRF /path/to/store-history-probability-baseline onlyobservation? onlyindicator?
```
In the above command, the first three arguments specify the path to the training data, the path to store the established initial CRF, and the path to store the established history probability baseline, the last two boolean arguments specify whether the CRF only uses observation-based feature functions and only uses indicator-based feature functions respectively. The established CRF (a json file) has representations for constraints about admissible repair transforms over AST nodes and different kinds of feature functions (the weights for them are zero and are to learn). The established history probability baseline is a txt file and contains a set of tuples <*L*, *T*, *P*> as described in section 6.2 of the paper.

For instance, to generate the CRF which uses both observation-based and indicator-based feature functions (let it be named `CRF-all.json` and in a folder named `CRF-Model`, here suppose we use all training data) and the history probability baseline (let it be named `history-probability-baseline.txt` and again in folder `CRF-Model`), use the following command (suppose the CRF.jar is not moved and the command is run in folder `CRF-Transform-Prediction`):
```
java -cp ./target/CRF.jar  FileParse.CRFEstablish ../Training-Data/All-Training-Data ../CRF-Model/CRF-all.json ../CRF-Model/history-probability-baseline.txt false false
```
By replacing the first `false` argument with `true`, the CRF which uses only observation-based feature functions will be generated. By replacing the second `false` argument with `true`, the CRF which uses only indicator-based feature functions will be generated. 

### Step 3: CRF learn

To train an established CRF in the previous step, run the following command:
```
java -cp CRF.jar xcrf.LearnTree /path/to/initial-CRF /path/to/training-data /path/to/store-learned-CRF value-regularization  value-*G*  value-*q*
```
In the above command, the first three arguments specify the path to the established CRF in the previous step, the path to the training data, and the path to store the learned CRF, the last three arguments specify the values for the three parameters &#963; <sup>2</sup>, *G*, and *q*.

As an example, to generate our overall optimal model (the CRF model which uses both observation-based and indicator-based feature functions, uses all training data, and the magnitude of the distribution-aware prior is set to be 0.5), use the following command (let the optimal model be named `CRF-all-0.5-learned.json` and in folder `CRF-Model`, again suppose the CRF.jar is not moved and the command is run in folder `CRF-Transform-Prediction`):
```
java -cp ./target/CRF.jar xcrf.LearnTree ../CRF-Model/CRF-all.json ../Training-Data/All-Training-Data ../CRF-Model/CRF-all-0.5-learned.json 500 200 0.5 
```
The other learned CRF models can be similarly generated by changing the relvant arguments. Note the learning process consumes a large amount of physical memory. For our training data set, it will consume around 350GB of physical memory for the full model. 

### Step 4: CRF prediction

To evaluate the performance of the learned CRF model on new, unseen code snippets, run the following command:
```
java -cp CRF.jar xcrf.AnnotateTrees /path/to/learned-CRF /path/to/test-data /path/to/store-CRF-prediction-result value-*k* /path/to/history-probability-baseline
```
In the above command, the fourth argument specifies the number of prediction results to output for each test instance (3 for our experiment as we use top-3 evaluation metric). The other four arguments specify the path to the learned CRF in the previous step, the path to the test data, the path to store the prediction results, and the path to the established history probability baseline in step 2. Each prediction result is a set of tuples <*I*, *T*> where *I* is the index of an AST node and *T* is an actual (i.e., non-empty) repair transform to be applied on the node. We compare the prediction results with the ground truth (i.e., the location and value of "susp" node extracted according to bug-fixing commit in step 1) to determine the model performance. 

For instance, to see the performance of the learned model `CRF-all-0.5-learned.json` for repair transform `Meth-RW-Var` when top-3 is used as the evaluation metric, run the following command (again suppose the CRF.jar is not moved and the command is run in folder `CRF-Transform-Prediction`):
```
java -cp ./target/CRF.jar xcrf.AnnotateTrees ../CRF-Model/CRF-all-0.5-learned.json ../Test-Data/Meth-RW-Var ../output 3 ../CRF-Model/history-probability-baseline.txt
```
The detailed prediction result for each test instance will be in the folder `output`, and the summary of the model performance will be as follows:
```
Method_RW_Var total:412 correct by CRF model:124 correct by history probability baseline:0
```
