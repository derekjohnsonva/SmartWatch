import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.fft import fft, fftfreq

# read data in from a 6-column csv file into a pandas dataframe, ignoring the first row
def read_from_file(name):
    df = pd.read_csv(name, header=None, skiprows=1, names=['time', 'a', 'b', 'x', 'y', 'z'])
    return df


def trim_rows(df, start, end):
    df = df[start:end]
    return df

"""# graph the fourier transform of the data in the given column
def graph_fourier_transform(df, col):
    x = fourier_transform(df, col)
    plt.plot(x)
    print(x)
    plt.show()"""

data = read_from_file('data2.csv')
data = trim_rows(data, 1000, -500)

N = len(data['time'])

# find the average spacing between data points in the time column
T = (data['time'].iloc[-1] - data['time'].iloc[0]) / N / 1000
print("spacing: ", T)

yf = fft(np.array(data['x']))
xf = fftfreq(N, T)[:N//2]
print(xf)
modified_yf = 2.0/N * np.abs(yf[0:N//2])

# create a plot with two subplots
fig, ax = plt.subplots(2)
ax[0].plot(xf, modified_yf)
ax[1].plot(data['time'], data['x'])

#find top 3 peaks of the fourier transform
maxes = np.argpartition(modified_yf, -3)[-3:]
print("maxes: ", maxes)
peak = np.where(modified_yf == np.amax(modified_yf))
print(xf[maxes])
print(xf[peak])
plt.grid()
plt.show()