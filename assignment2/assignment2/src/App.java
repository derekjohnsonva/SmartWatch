import weka.core.stopwords.Null;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class App {

    /***
     * Process the data files to generate a csv with time intervals denoted by window.
     * Will contain 6 parameters if useAdditionalParams is false and will contain 12 if true.
     * @param window: size of the time sampling window, in seconds
     * @param useAdditionalParams: denotes whether to also take the median and root mean square of each dimension or not
     * @throws Exception: throws an exception if the python filter file does not run
     */
    public static void processData(String window, boolean useAdditionalParams) throws Exception {
        //create command string to run python file
        String command = "python pipeline.py";
        String param = " ProjectData/data ";
        String additionalParams = "";
        if (useAdditionalParams) {
            additionalParams = " true";
        } else {
            additionalParams = " false";
        }
        //execute command in string
        Process p = Runtime.getRuntime().exec(command + param + window + additionalParams);

        //BufferReaders to read stdout and stderr
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        BufferedReader stdErr = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        //empty buffers so next exec() call have empty buffer to work with
        String s = "";
        while ((s = stdInput.readLine()) != null) {
        }
        while ((s = stdErr.readLine()) != null) {
        }
    }

    /***
     * Returns the name of the ML classifier from the option in Weka
     * @param option: option number passed to Weka
     * @return : name of the ML classifier
     */
    public static String getClassifierNameFromOption(int option) {
        String optionString = "";
        if (option == 1) {
            optionString = "J48";
        } else if (option == 2) {
            optionString = "Random Forest";
        } else if (option == 3) {
            optionString = "SVM";
        } else {
            throw new IndexOutOfBoundsException("Option value not in list of options");
        }
        return optionString;
    }

    /***
     * Returns the CSV data file created from the python script.
     * @return : the CSV data file after filtering. Should always have the name "aggregated_data.csv"
     * @throws Exception: throws an exception if the CSV does not exist.
     */
    public static String[][] getCSVData() throws Exception {
        String csvPath = "aggregated_data.csv";
        String[][] csvData = MyWekaUtils.readCSV(csvPath);
        if (csvData == null) {
            throw new NullPointerException("CSV does not exist");
        }
        return csvData;
    }

    /***
     * Runs a Weka classifier on the data given the parameters.
     * @param window: size of the sampling time interval for the dataset, in seconds.
     * @param useAdditionalParams: denotes if ProcessData() will include median and root mean square for each dimension
     * @param useCustomIndices: denotes if using custom defined features
     * @param option: Weka classifier option
     * @param customFeatureIndices: custom defined feature array. Only used when useCustomIndices is true
     * @return : accuracy of the Weka classification
     * @throws Exception: throws exception from method calls to other methods that also throw exceptions.
     */
    public static double WekaClassify(String window, boolean useAdditionalParams, boolean useCustomIndices, int option, int[] customFeatureIndices) throws Exception {
        //format csv file using python script
        processData(window, useAdditionalParams);

        //get csv file
        String[][] csvData = getCSVData();

        //determine which set of features to use
        int[] featureIndices = new int[]{0, 1, 2, 3, 4, 5};
        int[] featureIndicesAdditional = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        String arffData = "";
        if (useCustomIndices) {
            arffData = MyWekaUtils.csvToArff(csvData, customFeatureIndices);
        } else if (useAdditionalParams) {
            arffData = MyWekaUtils.csvToArff(csvData, featureIndicesAdditional);
        } else {
            arffData = MyWekaUtils.csvToArff(csvData, featureIndices);
        }
        
        // classify the results and print the accuracy
        return MyWekaUtils.classify(arffData, option);
    }

    public static void main(String[] args) throws Exception {

        int[] dummy_indexes = new int[0]; //set of dummy feature incides for parts that don't need them

        //part 1
        System.out.println("~~~~~~~ PART 1 ~~~~~~~");
        double resultPart1 = WekaClassify(
                "1",
                false,
                false,
                1,
                dummy_indexes
        );
        System.out.println("Percentage Correct With J48, window = 1" + ": " + resultPart1);
        System.out.println();

        //part 2: going through different time slices
        System.out.println("~~~~~~~ PART 2 ~~~~~~~");

        //list of windows to test
        String[] windowArr = {"2", "3", "4"};
        String bestWindow = "1";
        double bestAccuracy = resultPart1;

        for (int i = 0; i < windowArr.length; i++) {
            double resultPart2 = WekaClassify(
                    windowArr[i],
                    false,
                    false,
                    1,
                    dummy_indexes
            );
            if (resultPart2 > bestAccuracy) {
                bestAccuracy = resultPart2;
                bestWindow = windowArr[i];
            }
            System.out.println("Percentage Correct With J48, window = " + windowArr[i] + ": " + resultPart2);
        }
        System.out.println();

        //part 3: using additional features
        System.out.println("~~~~~~~ PART 3 ~~~~~~~");
        double resultPart3 = WekaClassify(
                bestWindow,
                true,
                false,
                1,
                dummy_indexes
        );
        System.out.println("Percentage Correct With J48 + additional parameters, window = " + bestWindow + ": " + resultPart3 + "\n");

        //part 4/5: sequential feature set for Decision Tree/Random Forest/SVM
        System.out.println("~~~~~~~ PART 4/5 ~~~~~~~");
        int[] options = new int[]{1, 2, 3};
        for (int i : options) {
            int[] featuresFromSFS = MyWekaUtils.sequentialFeatureSelection(12, getCSVData(), i);
            System.out.println("Features selected by SFS: " + Arrays.toString(featuresFromSFS));

            double resultFeatureSet = WekaClassify(
                    bestWindow,
                    true,
                    true,
                    i,
                    featuresFromSFS
            );
            String optionName = getClassifierNameFromOption(i);
            System.out.println("Percentage Correct With " + optionName + " + additional parameters + feature selection, window = " +
                    bestWindow + ": " + resultFeatureSet + "\n");

        }
    }
}
