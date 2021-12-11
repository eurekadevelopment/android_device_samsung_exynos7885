import os
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("-d", help="Debug", type=int)
args = parser.parse_args()
var = vars(args)
DEBUG = var['d'] == 1
blacklist = ["qcom", "nxp","pixel-framework", "lawnchair"  ,"support", "overlay","samsung", "gapps", "google", "gms", "codeaurora", "addons", "overlays"]
vendors = os.listdir("vendor")
for vendor in vendors:
  if DEBUG:
     print("Found vendor " + vendor)
  if vendor in blacklist:
     if DEBUG:
         print("Vendor " + vendor + " in blacklist. Skipping...")
  else:   
     if DEBUG:
         print("Vendor " + vendor + " not in blacklist. Writing...")
     f = open("device/samsung/universal7885-common/vendor_name", "a")
     f.write(vendor)
     f.close()
     break
     
     
  
