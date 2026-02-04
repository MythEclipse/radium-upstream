#!/bin/bash
# Script untuk menjalankan Minecraft dengan logging debug Lithium

echo "=========================================="
echo "Starting Minecraft with Lithium Debug Mode"
echo "=========================================="
echo ""
echo "Features enabled:"
echo "- Detailed task logging"
echo "- Deadlock detector (checks every 5 seconds)"
echo "- Timeout warnings"
echo "- Thread dumps on issues"
echo ""
echo "Logs will show [Lithium-EntityTick] and [Lithium-ChunkTick] prefixes"
echo "=========================================="
echo ""

# Set Java system property untuk enable debug
export JAVA_TOOL_OPTIONS="-Dlithium.debug=true"

# Run Minecraft
./gradlew runClient --no-daemon 2>&1 | tee minecraft-debug.log
