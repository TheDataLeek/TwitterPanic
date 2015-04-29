#!/usr/bin/env python

import numpy as np
import time
import cartopy
import cartopy.crs as ccrs
import matplotlib.pyplot as plt

plt.ion()
fig = plt.figure(figsize=(20, 20))
ax = plt.axes(projection=ccrs.PlateCarree())
#ax.set_extent([-130, -60, 20, 50])
ax.set_extent([0, 100, 0, 100])
ax.coastlines()

while True:
    time.sleep(0.5)
    data = 100 * np.random.random((10, 2))
    ax.scatter(data[:, 0], data[:, 1], transform=ccrs.PlateCarree())
    plt.draw()
