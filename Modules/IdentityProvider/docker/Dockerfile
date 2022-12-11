# Fetch basic image
FROM maven:3.8-jdk-11

# Application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app

# Resolve maven dependencies
COPY pom.xml /opt/app/
COPY docker/local-maven-repo /opt/app/local-maven-repo
RUN mvn install

# Source code
COPY src /opt/app/src
RUN mvn package

# Copy CA Certificate
COPY docker/cacerts ${JAVA_HOME}/lib/security/cacerts

# Expose port for communications and command for execution
EXPOSE 9091
CMD ["mvn", "exec:java"]