import com.bayesserver.*;
import com.bayesserver.data.ColumnValueType;
import com.bayesserver.data.DataReaderCommand;
import com.bayesserver.data.DataRow;
import com.bayesserver.data.DataTable;
import com.bayesserver.data.DataTableDataReaderCommand;
import com.bayesserver.data.DefaultEvidenceReaderCommand;
import com.bayesserver.data.EvidenceReaderCommand;
import com.bayesserver.data.TemporalReaderOptions;
import com.bayesserver.data.TimeValueType;
import com.bayesserver.data.VariableReference;
import com.bayesserver.data.discovery.Clustering;
import com.bayesserver.data.discovery.DiscretizationOptions;
import com.bayesserver.inference.*;
import com.bayesserver.learning.parameters.ParameterLearning;
import com.bayesserver.learning.parameters.ParameterLearningOptions;
import com.bayesserver.learning.parameters.ParameterLearningOutput;
import com.bayesserver.learning.parameters.TimeSeriesMode;
import com.bayesserver.learning.structure.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.Collections;

public class DBNslSteps {
	public void solve(int step, int set, BufferedWriter writerTrain, BufferedWriter writerTest, BufferedWriter writerA, BufferedWriter writerF)
	throws InconsistentEvidenceException, FileNotFoundException, IOException {
		long startTrain = System.currentTimeMillis();
		Network network = new Network("DBN");

		// Number of total sensors in data
		int SENSORSREAL = 42;

		// Vector to hold only the sensors with nonzero std
		Vector<Integer> validSensors = new Vector<>();

		File train = new File(
			"./data/trainList" + set + ".txt");
		Scanner sc = new Scanner(train);

		//if(set == 2){
			String firstWafer = sc.next();
			File checkSensors = new File("./data/stat/" + firstWafer + "_step" + step + ".txt");
			Scanner sc2 = new Scanner(checkSensors);
			int line = 0;
			while(sc2.hasNext()){
				String l = sc2.nextLine();
				line++;
				if(line < 5) continue;
				String[] splitArray = l.split("\\s+");

				for(int i = 0; i < SENSORSREAL; i++){
					if(splitArray[i+1].compareTo("0.00") > 0){
						validSensors.add(i);
						//System.out.print(i + " ");
					}
				}
				System.out.println();
			}
			sc2.close();
		//}

		int SENSORS = validSensors.size();


		System.out.println("Step " + step + ", set " + set + " : SENSORS = " + SENSORS);

		// DataTable with columns CASE, STATUS, TIME, 0(SENSOR INDEX), ... , SENSORS-1
		DataTable table = new DataTable();
		table.getColumns().add("Case", String.class);
		table.getColumns().add("StatusContinuous", Double.class);
		table.getColumns().add("Time", Integer.class);
		for (int i = 0; i < SENSORS; i++) {
			table.getColumns().add(String.valueOf(i), Float.class);
		}

		// Creating a node with one temporal variable, holding the status of the wafer.
		Variable varTransition = new Variable("Transition");
		varTransition = new Variable("Transition", VariableValueType.CONTINUOUS);
		Node nodeTransition = new Node("Transition", varTransition);
		nodeTransition.setTemporalType(TemporalType.TEMPORAL);
		network.getNodes().add(nodeTransition);

		// Creating continuous temporal variables holding the value of each sensor.
		Variable[] sensors = new Variable[SENSORS];
		for (int i = 0; i < SENSORS; i++) {
			String sensorNumber = String.valueOf(i);
			sensors[i] = new Variable(sensorNumber,
				VariableValueType.CONTINUOUS);
		}

		// Creating nodes that each hold one temporal variable corresponding to each sensor.
		Node[] nodeObservation = new Node[SENSORS];
		for(int i = 0;i < SENSORS; i++){
			nodeObservation[i] = new Node("sensor" + i, sensors[i]);
			nodeObservation[i].setTemporalType(TemporalType.TEMPORAL);
			network.getNodes().add(nodeObservation[i]);
		}

		// Object for learning the structure of the network.
		PCStructuralLearning sLearning = new PCStructuralLearning();

		int TIME = 0;
		train = new File("./data/trainList" + set + ".txt");
		sc = new Scanner(train);
		while (sc.hasNext()) {
			String waferName = sc.next();
			String waferStatus = sc.next();
			int localTime = -1;
			File waferData = new File("./data/" + waferName + "_step" + step  + ".txt");
			Scanner sc3 = new Scanner(waferData);
			while (sc3.hasNext()) {
				String timestep = sc3.nextLine();
				localTime++;
				if (localTime == 0)	continue; // Skip line with column names
				String[] splitArray = timestep.split("\\s+");

				// A new row to add to the DataTable.
				DataRow r = table.newRow();
				r.set(0, waferName);
				r.set(1, Double.valueOf(waferStatus));
				r.set(2, localTime - 1); // time is in index, not value. starts from 0

				// Add valid sensor value to the corresponding column in row r.
				int sensorVar = 0;
				for (int k = 0; k < SENSORSREAL; k++) {
					if(validSensors.contains(new Integer(k))){
						String s = splitArray[k+1];
						r.set(sensorVar+3, Float.valueOf(s));
						sensorVar++;
						if(sensorVar >= SENSORS) break;
					}
				}
			table.getRows().add(r);
			}
			sc3.close();
			// Taking as inference time the longest time of all wafers in the trainList if step duration is different for wafers
			if(TIME < localTime) TIME = localTime;
		}

		System.out.println("DataTable complete for step " + step + ", set " + set + ".");
		sc.close();

		// Reader for the DataTable.
		DataReaderCommand temporalDataReaderCommand = new DataTableDataReaderCommand(
			table);
		TemporalReaderOptions temporalReaderOptions = new TemporalReaderOptions(
			"Case", "Time", TimeValueType.INDEX);

		// Mapping variables to database columns
		VariableReference[] temporalVariableReferences = new VariableReference[SENSORS + 1];
		temporalVariableReferences[0] = new VariableReference(varTransition,
			ColumnValueType.VALUE, "StatusContinuous");
		for (int i = 1; i < SENSORS + 1; i++) {
			temporalVariableReferences[i] = new VariableReference(
				sensors[i - 1], ColumnValueType.VALUE,
				String.valueOf(i - 1));
		}

		// Reader to read evidence of each variable from the DataTable.
		EvidenceReaderCommand evidenceReaderCommand = new DefaultEvidenceReaderCommand(
			temporalDataReaderCommand,
			Arrays.asList(temporalVariableReferences),
			temporalReaderOptions);
		PCStructuralLearningOptions options = new PCStructuralLearningOptions();
		PCStructuralLearningOutput output = (PCStructuralLearningOutput) sLearning.learn(evidenceReaderCommand, network.getNodes(), options);

		// At this point the structural specification is complete.
		System.out.println("Structural specification complete for step " + step + ", set " + set + ".");

		// Object for learning the parameters of the network.
		ParameterLearning pLearning = new ParameterLearning(network,
			new RelevanceTreeInferenceFactory());
		ParameterLearningOptions learningOptions = new ParameterLearningOptions();
		learningOptions.setTimeSeriesMode(TimeSeriesMode.PINNED);

		ParameterLearningOutput result = pLearning.learn(evidenceReaderCommand,
			learningOptions);

		// Optional check to validate network.
		network = pLearning.getNetwork();
		network.validate(new ValidationOptions());

		// At this point the network has been fully specified.
		System.out.println("Parameter Learning complete for step " + step + ", set " + set + ".");

		long endTrain   = System.currentTimeMillis();
		long totalTrain = endTrain - startTrain;

		writerTrain.write(totalTrain + " ");

		// Now set some temporal evidence for testing the network.

		long startTest = System.currentTimeMillis();
		File test = new File("./data/testList" + set + ".txt");
		sc = new Scanner(test);
		Vector<Double> means = new Vector<Double>();	// Vector to hold predicted wafer status.
		Vector<Double> real = new Vector<Double>();		// Vector to hold actual wafer status.

		double zero_match = 0, one_match = 0, zero_one = 0, one_zero = 0;	// Parameters for accuracy and f-measure calculation.

		System.out.println("Results for step " + step + ", set " + set + ":");
		System.out.print("Real      : ");

		// Object for inference on the network with given evidence.
		Inference inference = new RelevanceTreeInference(network);
		QueryOptions queryOptions = new RelevanceTreeQueryOptions();
		QueryOutput queryOutput = new RelevanceTreeQueryOutput();
		CLGaussian gaussianFuture;
		while (sc.hasNext()) {
			Double[][] evidenceSensors = new Double[SENSORS][TIME];

			String waferName = sc.next();
			Double waferStatus = Double.valueOf(sc.next());
			real.add(waferStatus);
			int localTime = -1;
			File evidence = new File("./data/" + waferName + "_step" + step + ".txt");
			Scanner sce = new Scanner(evidence);

			while (sce.hasNext()) {
				String timestep = sce.nextLine();
				localTime++;
				if (localTime == 0) continue; // Skip line with column name
				if (localTime == TIME + 1) break;
				String[] splitArray = timestep.split("\\s+");

				int sensorCnt = 0;
				for (int k = 0; k < SENSORSREAL; k++) {
					if(validSensors.contains(new Integer(k))){
						String s = splitArray[k+1];
						evidenceSensors[sensorCnt][localTime - 1] = Double.valueOf(s);
						sensorCnt++;
						if(sensorCnt >= SENSORS) break;
					}
				}
				splitArray = null;
			}
			sce.close();
			int predictTime = TIME;

			// Mapping the temporal evidence with each sensor variable.
			for (int i = 0; i < SENSORS; i++) {
				inference.getEvidence().set(sensors[i], evidenceSensors[i],
					0, 0, TIME);
			}

			// Calculating the Gaussian distribution of variable varTransition.
			gaussianFuture = new CLGaussian(nodeTransition
				.getVariables().get(0), predictTime);
			inference.getQueryDistributions().add(gaussianFuture);

			// Query
			inference.query(queryOptions, queryOutput);

			// Taking the mean of the distribution and adding to the means vector for accuracy and f-measure calculation.
			double guessed = gaussianFuture.getMean(varTransition,
				predictTime);
			means.add(guessed);
			System.out.print(waferStatus.intValue() + " ");

			evidenceSensors = null;
			inference.getEvidence().clear();
		}
		sc.close();
		inference = null;
		gaussianFuture = null;
		queryOutput = null;

		System.out.println();

		// Manually setting the threshold of good/bad wafers may only fit in a specific case,
		// so we use clustering to divide the means into two bins(intervals).
		Clustering cl = new Clustering();
		DiscretizationOptions op = new DiscretizationOptions();
		op.setSuggestedBinCount(2);
		List<Interval<Double>> l = cl.discretize((Iterable<Double>) means, op,
			"StatusContinuous");
		// for(Interval<Double> e : l) System.out.println(e);
		System.out.print("Predicted : ");
		for(int i =  0; i < means.size(); i++){
			if(l.get(0).contains(means.get(i))) means.set(i, 0.0);	// Belonging to the lower interval indicates a zero.
			else means.set(i, 1.0);									// Otherwise, the predicted status indicates a one.
			System.out.print(means.get(i).intValue() + " ");
		}
		System.out.println();
		int correct = 0;

		// Compare the entries in the two Vectors means and real, to calculate accuracy and f-measure.
		for(int i = 0; i < means.size(); i++){
			if(Double.compare(real.get(i), means.get(i)) == 0){
				if(means.get(i).equals(0d)) zero_match = zero_match + 1;
				else one_match = one_match + 1;
			}
			else{
				if(means.get(i).equals(0d)) one_zero = one_zero + 1;
				else zero_one = zero_one + 1;
			}
		}
		double precision = zero_match / (zero_match+one_zero);
		double recall = zero_match / (zero_match+zero_one);
		double accuracy =  (one_match + zero_match) / (one_match + zero_match + one_zero + zero_one);
		double F1 =  2 * (precision * recall) / (precision + recall);

		System.out.println("Accuracy  : " + accuracy);
		System.out.println("F1 Score  : " + F1);

		writerA.write(accuracy + " ");
		writerF.write(F1 + " ");

		long endTest   = System.currentTimeMillis();
		long totalTest = endTest - startTest;

		writerTest.write(totalTest + " ");

		// Cleaning up
		means.removeAllElements();
		real.removeAllElements();
		l = null;
		table.getRows().clear();
		table.getColumns().clear();
		network.getLinks().clear();
		for(Node e : network.getNodes()) e.getVariables().clear();
		network.getNodes().clear();
		validSensors.removeAllElements();
		return;
	}

	public static void main(String[] args)
	throws InconsistentEvidenceException, FileNotFoundException, IOException {
		System.out.print("Enter number of train/test sets : ");
		Scanner s = new Scanner(System.in);
		int sets = s.nextInt();
		s.close();

		 BufferedWriter writerTrain = new BufferedWriter(new FileWriter("DBN_traintime.txt"));
		 BufferedWriter writerTest = new BufferedWriter(new FileWriter("DBN_testtime.txt"));
		 BufferedWriter writerA = new BufferedWriter(new FileWriter("DBN_accuracy.txt"));
		 BufferedWriter writerF = new BufferedWriter(new FileWriter("DBN_fmeasure.txt"));

		for(int step = 1; step <= 25; step++){
			// Skipping theses two steps because some wafers fail in parameter learning or inference,
			// due to time / memory constraints.
			if(step==1 || step==21) {
				writerTrain.newLine();
				writerTest.newLine();
				writerA.newLine();
				writerF.newLine();
				continue;
			}
			for(int set = 1; set <= sets; set++){
				DBNslSteps DBN = new DBNslSteps();
				DBN.solve(step, set, writerTrain, writerTest, writerA, writerF);
				DBN = null;
			}
			writerTrain.newLine();
			writerTest.newLine();
			writerA.newLine();
			writerF.newLine();
		}

		writerTrain.close();
		writerTest.close();
		writerA.close();
		writerF.close();
	}
}
