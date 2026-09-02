import os
import glob

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    if '.collectAsState()' not in content:
        return

    content = content.replace('.collectAsState()', '.collectAsStateWithLifecycle()')
    if 'androidx.lifecycle.compose.collectAsStateWithLifecycle' not in content:
        # Find import androidx.compose.runtime.* and add it after
        content = content.replace(
            'import androidx.compose.runtime.*',
            'import androidx.compose.runtime.*\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle'
        )

    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk('app/src/main/java/com/example/ui'):
    for file in files:
        if file.endswith('.kt'):
            fix_file(os.path.join(root, file))

print("Fixed!")
