plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "1.9.25" // Добавляем Kotlin plugin
}

group = "com.creazione"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://oss.sonatype.org/content/repositories/snapshots") // Для SNAPSHOT-версий
	google()
}

dependencies {
	// Telegram Bot
	implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1") {
		exclude(group = "org.telegram", module = "telegrambots")
	}
	implementation("org.telegram:telegrambots:6.9.7.1")
	// Для работы с GetChatHistory
	implementation("org.telegram:telegrambots-meta:6.9.7.1")

	// Для подключения .env
	implementation ("me.paulschwarz:spring-dotenv:4.0.0")

	// для работы с классом Instant
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	// Liquibase
	implementation("org.liquibase:liquibase-core:4.27.0") // Проверьте актуальную версию

	// Emoji-Java
	implementation("com.vdurmont:emoji-java:5.1.1")

	// Spring Boot Starter Web (обязательно для Telegram Bot)
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Остальные зависимости
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.redisson:redisson-spring-boot-starter:3.23.4")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.apache.commons:commons-lang3:3.15.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-logging")

	// Hibernate Types для JSONB
	implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

	// Для работы с JSON
	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
}

configurations.all {
	exclude(group = "commons-logging", module = "commons-logging")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Добавляем Kotlin-компилятор
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "21"
	}
}