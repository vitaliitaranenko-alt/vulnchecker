#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root_dir"

mvn -pl vulnfix-cli -am clean package

mkdir -p dist
cp vulnfix-cli/target/vulnfix-cli-*-all.jar dist/vulnfix.jar

echo "Built dist/vulnfix.jar"
echo "Run it with: java -jar dist/vulnfix.jar --help"
