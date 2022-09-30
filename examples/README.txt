==============
Format Data
==============
command:

java -jar Wada.jar acl raw_data formatted_data

The raw .wada files are kept in the raw_data folder. The formatted files are saved in formatted_data folder


==============
csv to arff
==============
Command:

java -jar Wada.jar arff features.csv

The features.csv file is converted to features.arff

Note: 
* Use the tool to convert csv format of feature data to arrf format. Not the formatted csv files. You need to extract features.csv file from the formatted csv files. 
** In this example, the values of features.csv are random, they are not derived from the formatted data files. But, in the assignment, the rows of features.csv file is derived from the formatted data files.  