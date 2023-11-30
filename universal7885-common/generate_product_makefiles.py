import os

blacklist = ["qcom", "nxp","pixel-framework", "lawnchair" ,"support", "overlay","samsung", "gapps", "google", "gms", "codeaurora", "addons", "overlays", "xtras", "themes"]

vendors = os.listdir('vendor')

for vendor in vendors:
  common = f'vendor/{vendor}/config/common.mk'
  if os.path.isfile(common) and vendor not in blacklist:
     print('vendor name is', vendor)
     with open("device/samsung/universal7885-common/vendor_name", 'w') as f:
         f.write(vendor)
     break

