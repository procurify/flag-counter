deploy:
	./gradlew clean test build
	serverless deploy

build:
	./gradlew clean test build

test:
	./gradlew test