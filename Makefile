deploy:
	./gradlew clean test build
	serverless deploy

build:
	./gradlew clean test build

docker-build:
	docker build -t flagcounter:latest .

docker-save:
	docker run --rm -v $(PWD)/build:/build --entrypoint=/bin/bash flagcounter:latest /bin/bash -c 'cp /app/*.jar /build/'

run: docker-build docker-save
	docker run -it --rm -v $(PWD)/build:/build --entrypoint=/bin/bash flagcounter:latest

test:
	./gradlew test
