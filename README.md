# AlvisIR

AlvisIR semantic search engine

# Prerequisites

* Java >= 7
* Maven >= 3.0.5

# Download

Clone the git repository or download from https://github.com/Bibliome/alvisir

# Build and install from the package homedir

`mvn clean package`

# Command-line interface

## Install from the package homedir

`./install.sh DIR`

*DIR* is the base directory of your AlvisIR install.

## Using the expander indexer

The expander indexer reads resources for query expansion.

`DIR/bin/alvisir-index-expander INDEX SPEC`

*DIR* is the base directory of your AlvisIR install. You migh also add the *bin* sub-directory to the *PATH* environment variable.

*INDEX* is the path to the expander index. If *INDEX* already contains an index, then it will be cleared before creating the new expander index.

*SPEC* is the expander specification file.

## Using the command-line search

`DIR/bin/alvisir-search SPEC QUERY`

*DIR* is the base directory of your AlvisIR install. You migh also add the *bin* sub-directory to the *PATH* environment variable.

*SPEC* is the index and search specification file.

*QUERY* is the query.

# Web service

## Deploy

Deploy the the `target/alvisir-web-0.5-SNAPSHOT.war` file in your favourite application container.

For instance, on *glassfish*, run:

`asadmin deploy --contextroot CONTEXT --name NAME target/alvisir-web-0.5-SNAPSHOT.war`

## Set context parameters

Set the following context parameters:

| Variable | Description |
| --- | --- |
| `configPath` | Absolute path to the UI specification file. |

## Use it

From a browser open the URL `http[s]://HOST/CONTEXT/webapi/search`
