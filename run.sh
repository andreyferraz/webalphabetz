#!/bin/bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Usando Java: $(java -version 2>&1 | head -1)"
./mvnw spring-boot:run "$@"

##comando para dar permissão de execução no arquivo run.sh
##chmod +x run.sh