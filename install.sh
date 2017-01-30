#!/bin/bash

LIB_FILES="target/alvisir2-core-0.5-SNAPSHOT.jar target/lib/bbop-UNKNOWN.jar target/lib/bibliome-utils-0.0.1-SNAPSHOT.jar target/lib/lucene-analyzers-3.6.1.jar target/lib/lucene-core-3.6.1.jar target/lib/lucene-queries-3.6.1.jar target/lib/obo-UNKNOWN.jar"

INSTALL_DIR="$(readlink -m $1)"
BIN_DIR="$INSTALL_DIR/bin"
LIB_DIR="$INSTALL_DIR/lib"

if [ "$INSTALL_DIR" != "$PWD" ];
then
    mkdir -p "$INSTALL_DIR"
    rm -f -r "$BIN_DIR"
    mkdir "$BIN_DIR"
    rm -f -r "$LIB_DIR"
    mkdir "$LIB_DIR"

fi

cp -f -r $LIB_FILES "$LIB_DIR"

./make-java-launcher.sh "$LIB_DIR" "$BIN_DIR"/alvisir-index-expander fr.inra.mig_bibliome.alvisir.core.expand.index.ExpanderIndexerFactory
./make-java-launcher.sh "$LIB_DIR" "$BIN_DIR"/alvisir-search         fr.inra.mig_bibliome.alvisir.core.AlvisIRCLI
