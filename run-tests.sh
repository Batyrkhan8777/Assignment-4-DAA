#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java src/test/java -name '*.java' -print | sort > build/test-sources.txt
javac -Xlint:all -d build/classes @build/test-sources.txt
java -cp build/classes edu.assignment44.AlgorithmTests
