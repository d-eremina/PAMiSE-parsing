import os
import pandas as pd
folder = "./data"

import csv

data = pd.DataFrame()
for file in os.listdir(folder):
  if file.endswith(".csv"):
    x = pd.read_csv(os.path.join(folder, file), low_memory=False, sep=';')
    data = pd.concat([data,x],axis=0)

data = data.sort_values(by=["timestamp"], ascending=True)
