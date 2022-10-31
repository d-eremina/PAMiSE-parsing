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
data["url"] = data["url"].astype('string').fillna("")
data["request"] = data["request"].astype('string').fillna("")
data["response"] = data["response"].astype('string').fillna("")
data["message"] = data["message"].astype('string').fillna("")
data["status"] = data["status"].fillna(0).astype('int32')
data["elapsed"] = data["elapsed"].fillna(0)

with open("./summary.csv", 'w', newline='') as csvfile:
    writer = csv.writer(csvfile, delimiter=';')
    writer.writerow(["timestamp", "url", "status", "elapsed", "request", "response", "message"])
    for _, row in data.iterrows():
        writer.writerow(row)
