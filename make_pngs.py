import subprocess
import os

os.makedirs("app/src/main/assets/brand_logos", exist_ok=True)

# Ballenoil
subprocess.run([
    "convert", "-size", "80x80", "xc:none",
    "-fill", "#1976D2", "-draw", "roundrectangle 4,4,76,76,16,16",
    "-fill", "white", "-pointsize", "34", "-font", "DejaVu-Sans-Bold",
    "-gravity", "center", "-draw", "text 0,0 'B'",
    "app/src/main/assets/brand_logos/ballenoil.png"
], check=True)

# Avia
subprocess.run([
    "convert", "-size", "80x80", "xc:none",
    "-fill", "#D81B60", "-draw", "roundrectangle 4,14,76,66,12,12",
    "-fill", "white", "-pointsize", "22", "-font", "DejaVu-Sans-Bold",
    "-gravity", "center", "-draw", "text 0,0 'AVIA'",
    "app/src/main/assets/brand_logos/avia.png"
], check=True)

# Other
subprocess.run([
    "convert", "-size", "80x80", "xc:none",
    "-fill", "#455A64", "-draw", "roundrectangle 4,4,76,76,16,16",
    "-fill", "white", "-pointsize", "22", "-font", "DejaVu-Sans-Bold",
    "-gravity", "center", "-draw", "text 0,0 'GAS'",
    "app/src/main/assets/brand_logos/other.png"
], check=True)

print("Generated pngs directly via imagemagick!")
