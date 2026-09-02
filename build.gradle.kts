plugins {
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.12.0"
	id("com.google.protobuf") version "0.9.4"
}

group = "com.saasybyte"
version = "0.0.1"
description = "Core backend service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

	// DB
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Rate Limiting
	implementation("com.giffing.bucket4j.spring.boot.starter:bucket4j-spring-boot-starter:0.14.0-RC1")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.github.ben-manes.caffeine:caffeine")
	implementation("com.github.ben-manes.caffeine:jcache")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// OpenAPI
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.0")

	// gRPC
	implementation(platform("org.springframework.grpc:spring-grpc-dependencies:1.0.1"))
	implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
	implementation("io.grpc:grpc-kotlin-stub:1.4.1")
	implementation("com.google.protobuf:protobuf-kotlin:4.33.1")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

openApiGenerate {
	generatorName.set("kotlin-spring")
	inputSpec.set("$rootDir/api/openapi.yaml")
	outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
	apiPackage.set("com.saasybyte.saasycore.api")
	modelPackage.set("com.saasybyte.saasycore.api.model")
	configOptions.set(mapOf(
		"interfaceOnly" to "true",
		"useSpringBoot3" to "true",
		"documentationProvider" to "none",
		"useTags" to "true"
	))
}

sourceSets {
	main {
		kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
		proto {
			srcDir("saasy-proto/protos/core")
		}
	}
}

tasks.compileKotlin {
	dependsOn("openApiGenerate")
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:4.33.1"
	}
	plugins {
		create("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.78.0"
		}
		create("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				create("grpc")
				create("grpckt")
			}
			it.builtins {
				create("kotlin")
			}
		}
	}
}
