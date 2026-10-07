#!/usr/bin/env python3
"""Applies the ALEX tunnel branding on top of a fresh v2rayNG checkout (GPLv3 upstream).
Usage: python3 branding/apply.py <path-to-v2rayNG-checkout>
Every step is defensive: if a pattern is not found it only prints a warning, it never aborts the build."""
import glob, os, re, sys
from PIL import Image, ImageDraw

ROOT = sys.argv[1]
HERE = os.path.dirname(os.path.abspath(__file__))
APP_NAME = "ALEX tunnel"
APP_ID = "com.alextunnel.app"
PRIMARY, PRIMARY_DARK, ACCENT = "#7C3AED", "#5B21B6", "#C026D3"

def read(p): return open(p, encoding="utf-8").read()
def write(p, s): open(p, "w", encoding="utf-8").write(s)
def warn(m): print("WARN:", m)

app = None
for cand in ("V2rayNG/app", "app"):
    if os.path.isdir(os.path.join(ROOT, cand)): app = os.path.join(ROOT, cand); break
if not app: warn("app module not found"); sys.exit(0)
res = os.path.join(app, "src/main/res")

# 1) app name (all languages)
n = 0
for f in glob.glob(res + "/values*/strings.xml"):
    s = read(f)
    t = re.sub(r'(<string\s+name="app_name"[^>]*>)[^<]*(</string>)', r"\g<1>%s\g<2>" % APP_NAME, s)
    if t != s: write(f, t); n += 1
print("app_name patched in", n, "files") if n else warn("app_name not found")

# 2) applicationId (code namespace stays the same, so nothing else breaks)
done = False
for f in glob.glob(app + "/build.gradle*"):
    s = read(f)
    t = re.sub(r'(applicationId\s*=?\s*)"com\.v2ray\.ang"', r'\g<1>"%s"' % APP_ID, s)
    if t != s: write(f, t); done = True
print("applicationId ->", APP_ID) if done else warn("applicationId not patched")

# 3) purple theme: theme items + named colors
items = ("colorPrimary", "colorPrimaryDark", "colorPrimaryVariant", "colorAccent", "colorSecondary",
         "colorSecondaryVariant", "colorPrimaryContainer", "colorTertiary")
vals = {"colorPrimary": PRIMARY, "colorPrimaryDark": PRIMARY_DARK, "colorPrimaryVariant": PRIMARY_DARK,
        "colorAccent": ACCENT, "colorSecondary": ACCENT, "colorSecondaryVariant": PRIMARY_DARK,
        "colorPrimaryContainer": PRIMARY_DARK, "colorTertiary": ACCENT}
cnt = 0
for f in glob.glob(res + "/values*/themes.xml") + glob.glob(res + "/values*/styles.xml") + glob.glob(res + "/values*/colors.xml"):
    s = read(f); o = s
    for k in items:
        s = re.sub(r'(<item\s+name="%s">)[^<]*(</item>)' % k, r"\g<1>%s\g<2>" % vals[k], s)
        s = re.sub(r'(<color\s+name="%s">)[^<]*(</color>)' % k, r"\g<1>%s\g<2>" % vals[k], s)
    if s != o: write(f, s); cnt += 1
print("theme colors patched in", cnt, "files") if cnt else warn("no theme colors found")

# 3b) dark purple surfaces (night theme) + status/navigation bars, same palette as the first app version
BG, SURFACE = "#140A23", "#1E1233"
dark = {"colorBackground": BG, "android:colorBackground": BG, "colorSurface": SURFACE,
        "android:statusBarColor": BG, "android:navigationBarColor": BG}
cnt = 0
for f in glob.glob(res + "/values-night*/themes.xml") + glob.glob(res + "/values-night*/styles.xml"):
    s = read(f); o = s
    for k, v in dark.items():
        s = re.sub(r'(<item\s+name="%s">)[^<]*(</item>)' % re.escape(k), r"\g<1>%s\g<2>" % v, s)
    if s != o: write(f, s); cnt += 1
print("dark surfaces patched in", cnt, "files") if cnt else warn("no night theme found")
for f in glob.glob(res + "/values/themes.xml") + glob.glob(res + "/values/styles.xml"):
    s = read(f); o = s
    for k in ("android:statusBarColor", "android:navigationBarColor"):
        s = re.sub(r'(<item\s+name="%s">)[^<]*(</item>)' % k, r"\g<1>%s\g<2>" % BG, s)
    if s != o: write(f, s)

# 4) launcher icon from the logo (drop adaptive/webp variants so the png wins)
for f in glob.glob(res + "/mipmap-*/ic_launcher*"):
    os.remove(f)
logo = Image.open(os.path.join(HERE, "logo.png")).convert("RGB")
sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
for d, px in sizes.items():
    out = os.path.join(res, "mipmap-" + d); os.makedirs(out, exist_ok=True)
    sq = logo.resize((px, px), Image.LANCZOS); sq.save(out + "/ic_launcher.png")
    big = logo.resize((px * 4, px * 4), Image.LANCZOS)
    mask = Image.new("L", big.size, 0); ImageDraw.Draw(mask).ellipse((0, 0) + big.size, fill=255)
    rd = Image.new("RGBA", big.size, (0, 0, 0, 0)); rd.paste(big, (0, 0), mask)
    rd.resize((px, px), Image.LANCZOS).save(out + "/ic_launcher_round.png")
print("launcher icons written")
