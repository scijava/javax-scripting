#!/bin/bash

whence()
{
    local path=

    for cmd
    do
       path=$(builtin type -path $cmd)
       if [ "$path" ] ; then
	  echo $path
       else
	  case "$cmd" in
	  /*) if [ -x "$cmd" ]; then
	        echo "$cmd"
	      fi
	      ;;
	  *) case "$(builtin type -type $cmd)" in
	     "") ;;
	     *) echo "$cmd"
		 ;;
	     esac
	     ;;
	  esac
	fi
    done
    return 0
}

PNUTS_HOME=${PNUTS_HOME:-$(dirname $(whence $0))/..}

let i=0
let j=0

if [ "x${HTTP_PROXY_HOST}" != "x" ] && [ "x${HTTP_PROXY_PORT}" != "x" ]; then
   flags[$i]="-Dhttp.proxyHost=${HTTP_PROXY_HOST}"
   let i++
   flags[$i]="-Dhttp.proxyPort=${HTTP_PROXY_PORT}"
   let i++
fi

for a in "$@"
do
    case "$a" in
    -J*)
	flags[$i]=${a#-J}
	let i++
	;;
    -vd|-d|-v)
	_g=true
	export JAVA_COMPILER=NONE
	args[$j]=$a
	let j++
	;;
    *)
	args[$j]=$a
	let j++
	;;
    esac
done

sysname=$(uname -s)
case $sysname in
    CYGWIN*)
      PNUTS_HOME=$(cygpath -m ${PNUTS_HOME})
      PATHSEP=";"
      ;;
    Darwin*)
      export DYN_LIBRARY_PATH=${DYN_LIBRARY_PATH}:${PNUTS_HOME}/lib
      PATHSEP=":"
      ;;
    *)
      export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${PNUTS_HOME}/lib
      PATHSEP=":"
      ;;
esac

if [ "x${CLASSPATH}" = "x" ]; then
      CLASSPATH="${PNUTS_HOME}/lib/pnuts.jar"
else
      CLASSPATH="${PNUTS_HOME}/lib/pnuts.jar${PATHSEP}${CLASSPATH}"
fi


for a in ${PNUTS_HOME}/modules/*.jar ${PNUTS_HOME}/modules/*.zip ${PNUTS_HOME}/modules/*.JAR ${PNUTS_HOME}/modules/*.ZIP
do
    if [ -f $a ]; then
       CLASSPATH=${CLASSPATH}${PATHSEP}${a}
    fi
done

if [ "${PNUTS_JDK11_COMPATIBLE}" = "true" ]; then
  CLASSPATH=${PNUTS_HOME}/lib/pnuts.jar${PATHSEP}${CLASSPATH}
else
  flags[$i]="-Xbootclasspath/a:${PNUTS_HOME}/lib/pnuts.jar"
fi

export CLASSPATH

if [ "x${PNUTS_MODULE}" = "x" ]; then
  module=pnuts.tools
else
  module=${PNUTS_MODULE}
fi

if [ "x${PNUTS_JAVA_COMMAND}" = "x" ]; then
   java="java"
else
   java=${PNUTS_JAVA_COMMAND}
fi
exec ${java} "${flags[@]}" "-Dpnuts.home=${PNUTS_HOME}" pnuts.tools.Main -m "${module}" "${args[@]}"
