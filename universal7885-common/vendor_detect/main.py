import os
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("-d", help="Debug", type=int)
args = parser.parse_args()
var = vars(args)
DEBUG = var['d'] == 1
blacklist = ["qcom", "nxp","pixel-framework", "lawnchair"  ,"support", "overlay","samsung", "gapps", "google", "gms", "codeaurora", "addons", "overlays", "xtras", "themes"]
vendors = os.listdir("vendor")
for vendor in vendors:
  common = "vendor/"+vendor+"/config/common.mk"
  if os.path.isfile(common) and vendor not in blacklist:
     if DEBUG:
        print("Found vendor " + vendor)
     f = open("device/samsung/universal7885-common/vendor_name", "a")
     f.write(vendor)
     f.close()
     break
  else:
     if DEBUG:
        print(vendor + " is on the blacklist or does not have common.mk. Skipping...")

