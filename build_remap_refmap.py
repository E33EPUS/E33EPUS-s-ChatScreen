"""Remap SRG->official names in the refmap JSON inside the ForgeGradle
deobfuscated cache jar of player-animation-lib. This is a build fix: the jar
is already modified by fg.deobf() (bytecode remap), this just fixes the JSON
refmap file that fg.deobf() misses."""

import json, re, os, zipfile

SRG_FILE = "build/createSrgToMcp/output.srg"
DEOBF_JAR = os.path.join(
    os.environ["USERPROFILE"],
    ".gradle/caches/forge_gradle/deobf_dependencies",
    "dev/kosmx/player-anim/player-animation-lib-forge",
    "1.0.2-rc1+1.20_mapped_official_1.20.1",
    "player-animation-lib-forge-1.0.2-rc1+1.20_mapped_official_1.20.1.jar",
)
REFMAP_PATH = "player-animation-lib-minecraft_common-refmap.json"

def load_mappings():
    m = {}
    with open(SRG_FILE) as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            if line.startswith("FD:"):
                parts = line.split()
                if len(parts) >= 3:
                    srg = parts[1].rsplit("/", 1)[-1]
                    off = parts[2].rsplit("/", 1)[-1]
                    if srg != off:
                        m[srg] = off
            elif line.startswith("MD:"):
                rest = line[3:].strip()
                first_paren = rest.index("(")
                srg_slash = rest.rfind("/", 0, first_paren)
                srg = rest[srg_slash + 1:first_paren].strip()
                second_paren = rest.index("(", first_paren + 1)
                off_slash = rest.rfind("/", 0, second_paren)
                off = rest[off_slash + 1:second_paren].strip()
                if srg != off:
                    m[srg] = off
    return m

def remap_text(text, mappings):
    text = re.sub(r";([a-zA-Z_][a-zA-Z0-9_]*)",
                  lambda m: ";" + mappings.get(m.group(1), m.group(1)), text)
    text = re.sub(r"^([a-zA-Z_][a-zA-Z0-9_]*):",
                  lambda m: mappings.get(m.group(1), m.group(1)) + ":", text)
    return text

def remap(obj, mappings):
    if isinstance(obj, dict):
        return {k: remap(v, mappings) for k, v in obj.items()}
    if isinstance(obj, list):
        return [remap(i, mappings) for i in obj]
    if isinstance(obj, str):
        return remap_text(obj, mappings)
    return obj

def main():
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    if not os.path.exists(DEOBF_JAR):
        print(f"Deobf jar not found: {DEOBF_JAR}")
        return

    mappings = load_mappings()
    print(f"Loaded {len(mappings)} SRG->official mappings")

    # Read refmap from the deobfuscated jar
    with zipfile.ZipFile(DEOBF_JAR, "r") as zf:
        raw_json = zf.read(REFMAP_PATH)
    data = json.loads(raw_json)

    # Check if already remapped (no SRG names in mappings values)
    sample = next(iter(data.get("mappings", {}).values()), {})
    sample_val = next(iter(sample.values()), "") if sample else ""
    if "m_" not in str(sample_val) and "f_" not in str(sample_val):
        print("Refmap already remapped, skipping")
        return

    data["mappings"] = remap(data.get("mappings", {}), mappings)
    if "data" in data and "searge" in data["data"]:
        data["data"]["searge"] = remap(data["data"]["searge"], mappings)

    # Write back
    tmp_path = DEOBF_JAR + ".tmp"
    with zipfile.ZipFile(DEOBF_JAR, "r") as zf_in:
        with zipfile.ZipFile(tmp_path, "w", zipfile.ZIP_DEFLATED) as zf_out:
            for item in zf_in.infolist():
                if item.filename == REFMAP_PATH:
                    zf_out.writestr(item, json.dumps(data, indent=2))
                else:
                    zf_out.writestr(item, zf_in.read(item.filename))
    os.replace(tmp_path, DEOBF_JAR)
    print("Refmap patched in deobfuscated cache jar")

if __name__ == "__main__":
    main()
