'''
This script takes in a list of csv files with the format: timestamp, x, y, z
It outputs a csv called aggregated_data.csv with the format:
    'mean_x', 'std_x', 'mean_y', 'std_y', 'mean_z', 'std_z', 'Activity'

Entries in this csv are the mean and standard deviation of the
x, y, and z values for one second of data from the input csvs

All data taht does not make up a complete second is discarded

The Activity label is determined by the name of the file.
If the file contains the word "not" it gets a label of "not_hand_washing",
otherwise it gets a label of "hand_washing".
'''

import sys
import pandas as pd
# Accept a list of .csv files  from command line and save them to an array
files = sys.argv[1:]
# validate that the files are .csv files
for file in files:
    if not file.endswith('.csv'):
        print('Please enter only .csv files')
        sys.exit(1)

aggregated_data = []
for file in files:
    # Open the file and read the lines into a dataframe
    headerList = ['timestamp', 'X', 'Y', 'Z']
    df = pd.read_csv(file, names=headerList)
    # get the value of the first timestamp in the file
    first_timestamp = df['timestamp'].iloc[0]
    # subtract the first timestamp from all the other timestamps
    df['timestamp'] = df['timestamp'] - first_timestamp
    df['timestamp'] = df['timestamp'] // 1000

    df_split = df.groupby('timestamp').agg(
        {'X': ['mean', 'std'], 'Y': ['mean', 'std'], 'Z': ['mean', 'std']})
    # We only care about samples where we have 1 second of data,
    # thus we will drop the last row as it is incomplete
    df_split = df_split.drop(df_split.tail(1).index)
    print(df_split)
    # Check to see if the file has the word "not" in it
    LABEL = "not_hand_wash" if "not" in file else "hand_wash"
    # add the label as the last column of the dataframe
    df_split['label'] = LABEL
    aggregated_data.append(df_split)


# Converte the list of dataframes to a single dataframe
aggregated_data = pd.concat(aggregated_data)
print("Aggregated Data")
print(aggregated_data)
# remove the timestamp data from the dataframe
aggregated_data = aggregated_data.reset_index()
aggregated_data = aggregated_data.drop(columns=['timestamp'], level=0)
# Save the dataframe to a csv file
aggregated_data.columns = ['mean_x', 'std_x', 'mean_y', 'std_y', 'mean_z', 'std_z', 'Activity']
aggregated_data.to_csv('aggregated_data.csv', float_format='%.5f', index=False, header=True)
