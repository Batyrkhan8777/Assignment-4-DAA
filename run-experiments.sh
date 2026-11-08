#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java -name '*.java' -print | sort > build/main-sources.txt
javac -Xlint:all -d build/classes @build/main-sources.txt
java -cp build/classes edu.assignment44.experiments.ExperimentRunner results
python3 scripts/generate_plots.py results
