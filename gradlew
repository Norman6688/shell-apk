#!/bin/sh
exec java -Xmx256m -Dfile.encoding=UTF-8 -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
