import os
blacklist = ["qcom", "nxp","pixel-framework", "lawnchair"  ,"support", "overlay","samsung", "gapps", "google", "gms", "codeaurora", "addons", "overlays", "xtras", "themes"]
vendors = os.listdir("vendor")
for vendor in vendors:
  common = "vendor/"+vendor+"/config/common.mk"
  if os.path.isfile(common) and vendor not in blacklist:
     f = open("device/samsung/universal7885-common/vendor_name", "a")
     f.write(vendor)
     f.close()
     break

