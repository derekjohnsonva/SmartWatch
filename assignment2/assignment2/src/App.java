import javax.xml.soap.SOAPPart;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class App {
    public static void main(String[] args) throws Exception {
        // The first argument should be a path to a CSV file

        String command = "python pipeline.py";
        String param = " ProjectData/data";
//        Runtime.getRuntime().exec(command + param );
        Process p = Runtime.getRuntime().exec(command + param);

        BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s = "";

        //print errors
        System.out.println("Errors: \n");
        while (s != null) {
            s = stderr.readLine();
            System.out.println(s);
        }

        String csvPath = "aggregated_data.csv";

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
