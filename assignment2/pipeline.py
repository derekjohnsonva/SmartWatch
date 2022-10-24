"""
This script takes a folder with csv files, all with the format: timestamp, x, y, z
It outputs a csv called aggregated_data.csv with the format:
    'mean_x', 'std_x', 'mean_y', 'std_y', 'mean_z', 'std_z', 'Activity'

Entries in this csv are the mean and standard deviation of the
x, y, and z values for one second of data from the input csvs

All data that does not make up a complete second is discarded

The Activity label is determined by the name of the file.
If the file contains the word "not" it gets a label of "not_hand_washing",
otherwise it gets a label of "hand_washing".
"""

import os
import sys
import pandas as pd
import math

dirname = sys.argv[1]
window = int(sys.argv[2])
use_additional_features = "true" == sys.argv[3]
print(use_additional_features)
# https://stackoverflow.com/questions/40963659/root-mean-square-of-a-function-in-python
def rms(x):
    x_np = x.to_numpy()
    ms = 0
    for i in range(1, len(x_np)):
        ms = ms + x_np[i]**2
    ms = ms / len(x_np)
    return math.sqrt(ms)

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
    df['timestamp'] = df['timestamp'] // (1000 * window)

    if not use_additional_features:
        df_split = df.groupby('timestamp').agg(
            {'X': ['mean', 'std'], 'Y': ['mean', 'std'], 'Z': ['mean', 'std']})
    else:
        df_split = df.groupby('timestamp').agg(
                    {
                    'X': ['mean', 'std', 'median', rms],
                    'Y': ['mean', 'std', 'median', rms],
                    'Z': ['mean', 'std', 'median', rms]
                    })
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
if not use_additional_features:
    aggregated_data.columns = ['mean_x', 'std_x', 'mean_y', 'std_y', 'mean_z', 'std_z', 'Activity']
else:
    aggregated_data.columns = [
    'mean_x', 'std_x', 'median_x', 'rms_x',
    'mean_y', 'std_y', 'median_y', 'rms_y',
    'mean_z', 'std_z', 'median_z', 'rms_z',
    'Activity']
aggregated_data.to_csv('aggregated_data.csv', mode="w+", float_format='%.5f', index=False, header=True)
print("finished")