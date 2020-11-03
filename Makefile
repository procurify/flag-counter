deploy:
	./gradlew clean build
	serverless deploy

build:
	./gradlew clean build

test:
	./gradlew test