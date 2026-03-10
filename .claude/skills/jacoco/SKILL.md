---
name: jacoco
description: Check JaCoCo code coverage
argument-hint: "[class-name]"
allowed-tools: Bash(bash *), Bash(python3 *)
---

## Check JaCoCo Code Coverage

Run `verify` to generate coverage reports, then parse and display results.

### Minimum threshold: **80%** line coverage

### Step 1: Generate coverage reports

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" verify -f pom.xml -pl spring-boot-config-json-schema-starter -q
```

### Step 2: Module-level summary

```python
python3 -c "
import xml.etree.ElementTree as ET

THRESHOLD = 80
module = 'spring-boot-config-json-schema-starter'
try:
    tree = ET.parse(f'{module}/target/site/jacoco/jacoco.xml')
    root = tree.getroot()
    for counter in root.findall('counter'):
        if counter.get('type') == 'LINE':
            missed = int(counter.get('missed'))
            covered = int(counter.get('covered'))
            total = missed + covered
            pct = covered / total * 100 if total > 0 else 0
            status = 'PASS' if pct >= THRESHOLD else 'FAIL'
            print(f'{module:<45} {covered:>8} {total:>8} {pct:>9.2f}% {status:>8}')
except FileNotFoundError:
    print(f'{module}: No coverage report found')
"
```

### Step 3: Class-level breakdown (lowest coverage first)

```python
python3 -c "
import xml.etree.ElementTree as ET

module = 'spring-boot-config-json-schema-starter'
tree = ET.parse(f'{module}/target/site/jacoco/jacoco.xml')
root = tree.getroot()
results = []
for pkg in root.findall('.//package'):
    for cls in pkg.findall('class'):
        for counter in cls.findall('counter'):
            if counter.get('type') == 'LINE':
                missed = int(counter.get('missed'))
                covered = int(counter.get('covered'))
                total = missed + covered
                if total > 0:
                    pct = covered / total * 100
                    results.append((pct, missed, covered, total, cls.get('name')))
results.sort()
print(f\"{'Coverage':>10} {'Missed':>8} {'Covered':>8} {'Total':>8}  Class\")
print('-' * 80)
for pct, missed, covered, total, name in results:
    print(f'{pct:>9.1f}% {missed:>8} {covered:>8} {total:>8}  {name}')
"
```

### Step 4: Uncovered lines in a specific class

If `$ARGUMENTS` is a class name, find uncovered lines:

```python
python3 -c "
import xml.etree.ElementTree as ET

module = 'spring-boot-config-json-schema-starter'
tree = ET.parse(f'{module}/target/site/jacoco/jacoco.xml')
root = tree.getroot()
CLASS_NAME = '$ARGUMENTS.java'
for pkg in root.findall('.//package'):
    for sf in pkg.findall('sourcefile'):
        if sf.get('name') == CLASS_NAME:
            uncovered = []
            for line in sf.findall('line'):
                mi = int(line.get('mi', 0))
                if mi > 0:
                    uncovered.append(int(line.get('nr')))
            print(f'Uncovered lines in {CLASS_NAME}: {len(uncovered)} lines')
            for ln in uncovered:
                print(f'  Line {ln}')
"
```

### Coverage improvement strategy

When coverage is below 80%:

1. Run class-level breakdown to find classes with lowest coverage
2. Run uncovered lines on those classes to find exact lines
3. Write tests targeting those lines:
   - Use `@ParameterizedTest` to efficiently cover multiple code paths
   - Focus on error/exception paths which are often uncovered
4. Re-run verify to confirm improvement
