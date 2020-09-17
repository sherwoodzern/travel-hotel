
# 1st stage, build the app
FROM maven:3.6-jdk-11 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml .
RUN mkdir -p target/classes
RUN mvn package -DskipTests

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD src src
RUN mvn package -DskipTests
# RUN mkdir ATP_WALLET
# COPY ATP_Wallet/ /helidon/ATP_Wallet
ADD src src 
RUN cat src/main/resources/META-INF/persistence.xml
RUN mvn package -DskipTests

RUN echo "done!"

# 2nd stage, build the runtime image
FROM openjdk:11-jre-slim
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/target/travel-hotel.jar ./
COPY --from=build /helidon/target/libs ./libs
# COPY ./ATP_Wallet /helidon/ATP_Wallet

CMD ["java", "-cp", "travel-hotel.jar", "com.oracle.camunda.travel.HotelResource"]

EXPOSE 7502
