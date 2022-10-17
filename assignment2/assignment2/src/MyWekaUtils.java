

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;


/**
 *
 * @author mm5gg
 */
public class MyWekaUtils {

    public static double classify(String arffData, int option) throws Exception {
		StringReader strReader = new StringReader(arffData);
		Instances instances = new Instances(strReader);
		strReader.close();
		instances.setClassIndex(instances.numAttributes() - 1);
		
		Classifier classifier;
		if(option==1)
			classifier = new J48(); // Decision Tree classifier
		else if(option==2)			
			classifier = new RandomForest();
		else if(option == 3)
			classifier = new SMO();  //This is a SVM classifier
		else 
			return -1;
		
		classifier.buildClassifier(instances); // build classifier
		
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(classifier, instances, 10, new Random(1), new Object[] { });
		
		return eval.pctCorrect();
	}
    
    
    public static String[][] readCSV(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        ArrayList<String> lines = new ArrayList();
        String line;

        while ((line = br.readLine()) != null) {
            lines.add(line);;
        }


        if (lines.size() == 0) {
            System.out.println("No data found");
            return null;
        }

        int lineCount = lines.size();

        String[][] csvData = new String[lineCount][];
        String[] vals;
        int i, j;
        for (i = 0; i < lineCount; i++) {            
                csvData[i] = lines.get(i).split(",");            
        }
        
        return csvData;

    }

    public static String csvToArff(String[][] csvData, int[] featureIndices) throws Exception {
        int total_rows = csvData.length;
        int total_cols = csvData[0].length;
        int fCount = featureIndices.length;
        String[] attributeList = new String[fCount + 1];
        int i, j;
        for (i = 0; i < fCount; i++) {
            attributeList[i] = csvData[0][featureIndices[i]];
        }
        attributeList[i] = csvData[0][total_cols - 1];

        String[] classList = new String[1];
        classList[0] = csvData[1][total_cols - 1];

        for (i = 1; i < total_rows; i++) {
            classList = addClass(classList, csvData[i][total_cols - 1]);
        }

        StringBuilder sb = getArffHeader(attributeList, classList);

        for (i = 1; i < total_rows; i++) {
            for (j = 0; j < fCount; j++) {
                sb.append(csvData[i][featureIndices[j]]);
                sb.append(",");
            }            
            sb.append(csvData[i][total_cols - 1]);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static StringBuilder getArffHeader(String[] attributeList, String[] classList) {
        StringBuilder s = new StringBuilder();
        s.append("@RELATION wada\n\n");

        int i;
        for (i = 0; i < attributeList.length - 1; i++) {
            s.append("@ATTRIBUTE ");
            s.append(attributeList[i]);
            s.append(" numeric\n");
        }

        s.append("@ATTRIBUTE ");
        s.append(attributeList[i]);
        s.append(" {");
        s.append(classList[0]);

        for (i = 1; i < classList.length; i++) {
            s.append(",");
            s.append(classList[i]);
        }
        s.append("}\n\n");
        s.append("@DATA\n");
        return s;
    }

    private static String[] addClass(String[] classList, String className) {
        int len = classList.length;
        int i;
        for (i = 0; i < len; i++) {
            if (className.equals(classList[i])) {
                return classList;
            }
        }

        String[] newList = new String[len + 1];
        for (i = 0; i < len; i++) {
            newList[i] = classList[i];
        }
        newList[i] = className;

        return newList;
    }

    public static int[] sequentialFeatureSelection(int[] features, String[][] csvData, int option) throws Exception {
        // convert features into an arrayList
        ArrayList<Integer> featureList = new ArrayList<Integer>();
        for (int i = 0; i < features.length; i++) {
            featureList.add(features[i]);
        }
        ArrayList<Integer> selectedFeatures = new ArrayList<Integer>();
        // Continue to run the loop until featureList is empty or the accuracy changes less than 1%
        double previousBestAccuracy = 0;
        while(featureList.size() > 0) {
            double bestAccuracyForThisRun = 0;
            int featureToAdd = -1;
            for (int feature: featureList) {
                // copy the selectedFeatures list but add the current feature
                ArrayList<Integer> tempFeatureList = new ArrayList<Integer>(selectedFeatures);
                tempFeatureList.add(feature);
                // print the tempFeatureList
                System.out.println("Trying feature set: " + tempFeatureList.toString());
                // convert the tempFeatureList to an array
                int[] tempFeatureArray = new int[tempFeatureList.size()];

                tempFeatureArray = tempFeatureList.stream().mapToInt(Integer::intValue).toArray();
                
                // convert the csvData to arff with the tempFeatureArray
                String arffData = csvToArff(csvData, tempFeatureArray);
                // get the prediction accuracy for the current feature
                double accuracy = classify(arffData, option);
                // if the accuracy is better than the bestAccuracyForThisRun, update the bestAccuracyForThisRun and featureToAdd
                if (accuracy > bestAccuracyForThisRun) {
                    bestAccuracyForThisRun = accuracy;
                    featureToAdd = feature;
                }
            }
            // print the global best accuracy, the best accuracy for this run, and the feature to add
            System.out.println("Previous best accuracy: " + previousBestAccuracy);
            System.out.println("Best accuracy for this run: " + bestAccuracyForThisRun);
            System.out.println("Feature to add: " + featureToAdd);
            // if the accuracy from the last run is less than 1% different from the current accuracy, stop the loop
            if (Math.abs(bestAccuracyForThisRun - previousBestAccuracy) < 1) {
                break;
            }
            previousBestAccuracy = bestAccuracyForThisRun;
            // add the featureToAdd to the selectedFeatures list
            selectedFeatures.add(featureToAdd);
            // remove the featureToAdd from the featureList
            featureList.remove((Integer)featureToAdd);
        }
        // convert the selectedFeatures list to an array
        int[] selectedFeaturesArray = new int[selectedFeatures.size()];
        selectedFeaturesArray = selectedFeatures.stream().mapToInt(Integer::intValue).toArray();
        return selectedFeaturesArray;
    }
}