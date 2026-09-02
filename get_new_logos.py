import urllib.request
import subprocess
import os
import shutil

os.makedirs("app/src/main/assets/brand_logos", exist_ok=True)

# 1. Plenergy (copy plenoil.png)
shutil.copy("app/src/main/assets/brand_logos/plenoil.png", "app/src/main/assets/brand_logos/plenergy.png")

# 2. Canary Oil
canary_url = "https://canaryoil.com/wp-content/uploads/166233691_125161969618619_562152879028425106_n.webp"
tmp_canary = "/tmp/canary.webp"
urllib.request.urlretrieve(canary_url, tmp_canary)
subprocess.run(["convert", tmp_canary, "-background", "none", "-resize", "80x80>", "app/src/main/assets/brand_logos/canary_oil.png"], check=True)

# 3. H2EXAGON
h2_url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTfg6mAtk_zBABqb7c3jHDn6OCbl2pArbo_SeQi8cyyKp-ulYoDjPksOng&s=10"
tmp_h2 = "/tmp/h2.jpg"
req = urllib.request.Request(h2_url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as resp, open(tmp_h2, 'wb') as f:
    f.write(resp.read())
subprocess.run(["convert", tmp_h2, "-background", "none", "-resize", "80x80>", "app/src/main/assets/brand_logos/h2exagon.png"], check=True)

# 4. OCÉANO
oceano_url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ76sWo8dJEe_yyX7imzn8WXZuw7_5h-UoXbRLpJsB9T3XCrQMglecOs8pv&s=10"
tmp_oceano = "/tmp/oceano.jpg"
req = urllib.request.Request(oceano_url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as resp, open(tmp_oceano, 'wb') as f:
    f.write(resp.read())
subprocess.run(["convert", tmp_oceano, "-background", "none", "-resize", "80x80>", "app/src/main/assets/brand_logos/oceano.png"], check=True)

# 5. SANTANA DOMINGUEZ SL
santana_url = "https://www.combustiblesantana.com/ANAGRAMA%20LOGO%20SANTANA%20DOMINGUEZ.PNG"
tmp_santana = "/tmp/santana.png"
req = urllib.request.Request(santana_url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as resp, open(tmp_santana, 'wb') as f:
    f.write(resp.read())
subprocess.run(["convert", tmp_santana, "-background", "none", "-resize", "80x80>", "app/src/main/assets/brand_logos/santana.png"], check=True)

print("Downloaded all new logos successfully")
