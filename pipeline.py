"""
Mostly copied from Derek's data_clean, run by providing the argument of the folder with all .csv files
"""

import os
import sys
import pandas as pd

dirname = sys.argv[1]
filenames = []
for fname in os.scandir(dirname):
    filenames.append(fname)

aggregated_data = []
for file in filenames:
    # Open the file and read the lines into a dataframe
    headerList = ['timestamp', 'dummy1', 'dummy2', 'X', 'Y', 'Z']
    # headerList = headerList.dropna(axis=)
    df = pd.read_csv(file, names=headerList)
    # drop first row which is just a dummy timestamp
    df = df[1:]
    # remove dummy columns
    df = df.drop(["dummy1", "dummy2"], axis=1)
    print(df)
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
    #print(df_split)
    # Check to see if the file has the word "not" in it
    #print("checking if not in ", file)
    LABEL = "not_hand_wash" if "not" in file.name else "hand_wash"
    # add the label as the last column of the dataframe
    df_split['label'] = LABEL
    aggregated_data.append(df_split)


# Converte the list of dataframes to a single dataframe
aggregated_data = pd.concat(aggregated_data)
print("Aggregated Data")
# remove the timestamp data from the dataframe
aggregated_data = aggregated_data.reset_index()
aggregated_data = aggregated_data.drop(columns=['timestamp'], level=0)
# Save the dataframe to a csv file
aggregated_data.columns = ['mean_x', 'std_x', 'mean_y', 'std_y', 'mean_z', 'std_z', 'Activity']
aggregated_data.to_csv('aggregated_data.csv', float_format='%.5f', index=False, header=True)