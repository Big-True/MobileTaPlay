from pathlib import Path
p = Path(r"D:\Project\Tower\android\app\src\main\java\com\mobiletaplay\app\ui\detail\DetailScreen.kt")
lines = p.read_text(encoding="utf-8").splitlines(keepends=True)
out = []
inserted = False
for i, line in enumerate(lines):
    if (not inserted) and "Column(modifier.padding(12.dp))" in line and i > 180:
        out.append("    var expanded by remember { mutableStateOf(false) }\n")
        inserted = True
    out.append(line)
p.write_text("".join(out), encoding="utf-8")
print("inserted", inserted)
