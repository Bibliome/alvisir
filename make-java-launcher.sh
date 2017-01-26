#!/bin/bash

read -r -d '' TEMPLATE <<'EOF'

#!/bin/bash

# qsub options
#$ -S /bin/bash
#$ -V
#$ -cwd

export CLASSPATH="__LIBSPATH__/*:$CLASSPATH"

JVMOPTS=
OPTERR=0
while getopts J: o; do
    case "$o" in
	J) JVMOPTS="$JVMOPTS $OPTARG";;
	?) break;;
    esac
done
shift $(($OPTIND-1))

cmd="java $JVMOPTS __MAINCLASS__ ""$@"
#echo $CLASSPATH
#echo $cmd
$cmd
EOF

if [ $# -ne 3 ]; then
    echo "Usage: $0 LIBSPATH BINFILE MAINCLASS" >&2
    exit 1
fi

LIBSPATH=$1
BINFILE=$2
MAINCLASS=$3

sed -e "s,__LIBSPATH__,$LIBSPATH," -e "s,__MAINCLASS__,$MAINCLASS," <<<"$TEMPLATE" >"$BINFILE"
chmod +x "$BINFILE"

