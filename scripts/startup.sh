#!/bin/bash
set -e

cd /app

# 1. Create debug.keystore if missing
if [ ! -f debug.keystore ]; then
    echo "Creating debug.keystore..."
    keytool -genkeypair -v \
        -keystore debug.keystore \
        -alias androiddebugkey \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=Android Debug,O=Android,C=US"
fi

# 2. Create local.properties pointing to the SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 3. Create .env from .env.example if missing (secrets gradle plugin needs it)
if [ ! -f .env ]; then
    cp .env.example .env
fi

# 4. Build and run Roborazzi screenshot tests
echo "=== Generating Gradle wrapper ==="
gradle wrapper --gradle-version 9.3.1 --no-daemon 2>/dev/null || true

echo "=== Building project and generating screenshots ==="
./gradlew test --tests "*.AllScreensScreenshotTest" --info --no-daemon || {
    echo "=== Screenshot tests failed, trying with GreetingScreenshotTest only ==="
    ./gradlew test --tests "*.GreetingScreenshotTest" --info --no-daemon
}

# 5. Set up the preview gallery
PREVIEW_DIR=/tmp/preview
mkdir -p "$PREVIEW_DIR/screenshots"

# Copy generated screenshots
if [ -d app/src/test/screenshots ]; then
    cp app/src/test/screenshots/*.png "$PREVIEW_DIR/screenshots/" 2>/dev/null || true
fi

# Also check Roborazzi default output
if [ -d app/build/outputs/roborazzi ]; then
    cp app/build/outputs/roborazzi/*.png "$PREVIEW_DIR/screenshots/" 2>/dev/null || true
fi

# 6. Generate HTML gallery
echo "=== Generating gallery ==="
SCREENSHOTS_DIR="$PREVIEW_DIR/screenshots"
HTML_FILE="$PREVIEW_DIR/index.html"

# Build gallery items from available screenshots
GALLERY_ITEMS=""
for img in "$SCREENSHOTS_DIR"/*.png; do
    [ -f "$img" ] || continue
    filename=$(basename "$img" .png)
    # Convert filename to display label
    label=$(echo "$filename" | sed 's/_/ /g' | sed 's/\b\(.\)/\u\1/g')
    GALLERY_ITEMS="$GALLERY_ITEMS
        <div class=\"screenshot\">
            <div class=\"label\">$label</div>
            <img src=\"screenshots/$filename.png\" alt=\"$label\" loading=\"lazy\">
        </div>"
done

cat > "$HTML_FILE" << 'HTMLEOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crypts &amp; Dungeons — Screen Gallery</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            background: #0d0d14;
            color: #e0d8c8;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            min-height: 100vh;
            padding: 24px;
        }
        header { text-align: center; margin-bottom: 32px; }
        header h1 {
            color: #d4af37;
            font-size: 2rem;
            letter-spacing: 2px;
            text-shadow: 0 0 20px rgba(212,175,55,0.3);
        }
        header p { color: #887755; margin-top: 6px; font-size: 0.95rem; }
        .gallery {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
            gap: 20px;
            max-width: 1400px;
            margin: 0 auto;
        }
        .screenshot {
            background: #16213e;
            border: 1px solid #2a2a40;
            border-radius: 12px;
            overflow: hidden;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .screenshot:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 24px rgba(212,175,55,0.15);
            border-color: #d4af37;
        }
        .screenshot img { width: 100%; display: block; }
        .screenshot .label {
            padding: 12px 16px;
            font-weight: 600;
            color: #d4af37;
            font-size: 0.95rem;
            letter-spacing: 0.5px;
        }
        .info-bar {
            max-width: 1400px;
            margin: 0 auto 20px;
            padding: 12px 20px;
            background: #1a1a2e;
            border: 1px solid #2a2a40;
            border-radius: 8px;
            color: #887755;
            font-size: 0.85rem;
            text-align: center;
        }
    </style>
</head>
<body>
    <header>
        <h1>⚔ Crypts &amp; Dungeons</h1>
        <p>Tactical RPG Dungeon Crawler — Rendered Android Screens (Roborazzi)</p>
    </header>
    <div class="info-bar">
        This is a native Android (Kotlin + Jetpack Compose) app. Screenshots are rendered via Robolectric + Roborazzi unit tests. They are static previews, not interactive.
    </div>
    <div class="gallery">
HTMLEOF

echo "$GALLERY_ITEMS" >> "$HTML_FILE"

cat >> "$HTML_FILE" << 'HTMLEOF'
    </div>
</body>
</html>
HTMLEOF

# 7. Start web server
echo "=== Starting preview server on port 3000 ==="
cd "$PREVIEW_DIR"
python3 -m http.server 3000
