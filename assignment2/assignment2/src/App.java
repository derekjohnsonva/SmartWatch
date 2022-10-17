import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        // The first argument should be a path to a CSV file
        String csvPath = args[0];
        // TODO: Add verification that the file exists
        String[][] csvData = MyWekaUtils.readCSV(csvPath);
        // make an int array of the indices of the features you want to use
        // in this example, we're using all of them except the last one (class)
        int[] featureIndices = new int[]{0, 1, 2, 3, 4, 5};
        String arffData = MyWekaUtils.csvToArff(csvData, featureIndices);
        // Classify the results and print the accuracy
        double j48Results = MyWekaUtils.classify(arffData, 1);
        System.out.println("Percentage Correct With J48 = " + j48Results);

        double randomForestResults = MyWekaUtils.classify(arffData, 2);
        System.out.println("Percentage Correct With Random Forest = " + randomForestResults);

        double svmResults = MyWekaUtils.classify(arffData, 3);
        System.out.println("Percentage Correct With SVM = " + svmResults);

        int[] featuresFromSFS = MyWekaUtils.sequentialFeatureSelection(featureIndices, csvData, 1);
        System.out.println("Features selected by SFS: " + Arrays.toString(featuresFromSFS));

        String arffDataSFS = MyWekaUtils.csvToArff(csvData, featuresFromSFS);
        double j48ResultsSFS = MyWekaUtils.classify(arffDataSFS, 1);
        System.out.println("Percentage Correct With J48 Using Selected Features = " + j48ResultsSFS);
    }
}
