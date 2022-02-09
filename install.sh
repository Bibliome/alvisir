#!/bin/bash

LIB_FILES="alvisir-core/target/*.jar alvisir-core/target/lib/*.jar"

INSTALL_DIR="$(readlink -m $1)"

if [ -z $1 ];
then
    INSTALL_DIR = "$PWD"
fi

BIN_DIR="$INSTALL_DIR/bin"
LIB_DIR="$INSTALL_DIR/lib"

mkdir -p "$INSTALL_DIR"
rm -f -r "$BIN_DIR"
mkdir "$BIN_DIR"
rm -f -r "$LIB_DIR"
mkdir "$LIB_DIR"

cp -f -r $LIB_FILES "$LIB_DIR"

./make-java-launcher.sh "$LIB_DIR" "$BIN_DIR"/alvisir-index-expander fr.inra.maiage.bibliome.alvisir.core.expand.index.ExpanderIndexerFactory
./make-java-launcher.sh "$LIB_DIR" "$BIN_DIR"/alvisir-search         fr.inra.maiage.bibliome.alvisir.core.AlvisIRCLI
