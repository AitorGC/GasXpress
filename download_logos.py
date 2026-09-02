import urllib.request
import subprocess
import os

brands = {
    "repsol": "http://repsol.com",
    "cepsa": "http://cepsa.com",
    "shell": "http://shell.com",
    "galp": "http://galp.com",
    "petroprix": "http://petroprix.com",
    "alcampo": "http://alcampo.es",
    "carrefour": "http://carrefour.es",
    "eroski": "http://eroski.es",
    "campsa": "http://repsol.com",
    "bp": "https://www.bp.com",
    "plenoil": "https://plenoil.es/",
    "disa": "https://www.disagrupo.es/"
}

os.makedirs("app/src/main/assets/brand_logos", exist_ok=True)
headers = {'User-Agent': 'Mozilla/5.0 (Android; Mobile)'}

for name, domain in brands.items():
    url = f"https://t3.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url={domain}&size=128"
    tmp_path = f"/tmp/{name}.png"
    dest_path = f"app/src/main/assets/brand_logos/{name}.png"
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as resp:
            with open(tmp_path, "wb") as f:
                f.write(resp.read())
        subprocess.run(["convert", tmp_path, "-background", "none", "-resize", "80x80>", dest_path], check=True)
        print(f"Success: {name}")
    except Exception as e:
        print(f"Failed {name}: {e}")

