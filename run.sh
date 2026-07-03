#!/bin/bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Usando Java: $(java -version 2>&1 | head -1)"

if [ -x ./mvnw ]; then
  exec ./mvnw spring-boot:run "$@"
else
  exec mvn spring-boot:run "$@"
fi
#!/bin/bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Usando Java: $(java -version 2>&1 | head -1)"

if [ -x ./mvnw ]; then
	exec ./mvnw spring-boot:run "$@"
else
	exec mvn spring-boot:run "$@"
fi

##comando para dar permissão de execução no arquivo run.sh
##chmod +x run.sh