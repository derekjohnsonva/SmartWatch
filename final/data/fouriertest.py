import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.fft import fft, fftfreq

MINIMUM_BPM = 40
MAXIMUM_BPM = 200

# read data in from a 6-column csv file into a pandas dataframe, ignoring the first row
# returns a pandas dataframe
def read_from_file(name):
    df = pd.read_csv(name, header=None, skiprows=1, names=['time', 'a', 'b', 'x', 'y', 'z'])
    return df

# trims rows off the top and bottom of the dataframe
def trim_rows(df, start, end):
    df = df[start:end]
    return df

def process_dimension(data, dimension, show_graph=False):


    N = len(data['time'])

    # find the average time to take 1 sample (usually around 0.005 seconds for smartwatch)
    T = (data['time'].iloc[-1] - data['time'].iloc[0]) / N / 1000
    print("spacing: ", T)

    # calculate the FFT of the given dimension, store in yf
    yf = fft(np.array(data[dimension]))

    # generate the labels for the x-axis
    xf = fftfreq(N, T)[:N//2]

    # keep only the positive frequencies
    modified_yf = 2.0/N * np.abs(yf[0:N//2])

    min_bps = MINIMUM_BPM / 60
    max_bps = MAXIMUM_BPM / 60

    # remove values in xf that are not in the range of min_bps to max_bps
    xf, modified_yf = xf[(xf >= min_bps) & (xf <= max_bps)], modified_yf[(xf >= min_bps) & (xf <= max_bps)]
    largest_index = np.argmax(modified_yf)
    largest_x = xf[largest_index]
    # calculate uncertainty by taking the standard deviation of all values except the largest one
    uncertainty = np.std(np.delete(modified_yf, largest_index))

    print("uncertainty for", dimension, "largest: ", largest_x, "uncertainty", uncertainty)

    """#sort xf by magnitude of modified_yf
    sorted_xf = [x for _,x in sorted(zip(modified_yf, xf), reverse=True)]
    print(sorted_xf)"""

    #find top 3 peaks of the fourier transform

    if show_graph:
        # create a plot with two subplots
        fig, ax = plt.subplots(2)
        ax[0].plot(xf, modified_yf, linestyle="None", marker='o')
        ax[1].plot(data['time'], data[dimension])
        maxes = np.argpartition(modified_yf, -3)[-3:]
        print("maxes: ", maxes)
        peak = np.where(modified_yf == np.amax(modified_yf))
        print(xf[maxes])
        print(xf[peak])
        plt.grid()
        plt.show()

    
    return largest_x, uncertainty

def poll_all_dimensions(filename, start, end, show_graph=False):
    data = read_from_file(filename)
    data = trim_rows(data, start, end)
    print(data)

    x, x_uncertainty = process_dimension(data, "x", show_graph)
    y, y_uncertainty = process_dimension(data, "y", show_graph)
    z, z_uncertainty = process_dimension(data, "z", show_graph)
    # return the axis (x, y, z) with the minimum corresponding uncertainty
    min_uncertainty = min(x_uncertainty, y_uncertainty, z_uncertainty)
    if min_uncertainty == x_uncertainty:
        return x
    elif min_uncertainty == y_uncertainty:
        return y
    else:
        return z




#df = read_from_file("80bpm.csv")
#poll_all_dimensions("80bpm.csv", 1000, -1000, show_graph=True)
result = poll_all_dimensions("/Users/noahholloway/Documents/SmartWatch/final/data/80bpm.csv", 1000, -1000, show_graph=False)
print("Predicted BPM: ", result * 60)