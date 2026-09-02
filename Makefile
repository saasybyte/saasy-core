.PHONY: run clean-run build clean-build clean generate proto api bruno

# Run the API server (local profile)
run:
	./gradlew bootRun --args='--spring.profiles.active=local' -q

# Clean and run the API server
clean-run:
	./gradlew clean bootRun --args='--spring.profiles.active=local' -q

# Build without tests
build:
	./gradlew build -x test

# Clean and rebuild
clean-build:
	./gradlew clean build -x test

# Clean build artifacts
clean:
	./gradlew clean

# Full codegen: protobuf, OpenAPI, Bruno
generate: proto api bruno

# Regenerate protobuf/gRPC
proto:
	./gradlew generateProto

# Regenerate OpenAPI server
api:
	./gradlew openApiGenerate

# Regenerate Bruno collection from OpenAPI
bruno:
	./scripts/bruno-gen.sh
