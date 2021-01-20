deploy:
	@./gradlew clean build
	@serverless deploy

build:
	@./gradlew clean build

docker-build:
	@docker build -t flagcounter:latest .

docker-save: docker-build
	@docker run --rm -v $(PWD)/build:/build --entrypoint="/usr/bin/bash" flagcounter:latest -c "cp /app/*.jar /build/"

test:
	@./gradlew test
