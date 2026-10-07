package org.alexmond.sample.test;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generates the schema of this sample application and checks it against the copies
 * published with the documentation in {@code docs/modules/ROOT/attachments}.
 *
 * <p>
 * The build runs on several JDKs, so this test is also the proof that the generator output
 * does not depend on the JDK: every JDK has to reproduce the same tracked files byte for
 * byte. The freshly generated files are always written to {@code target/generated-schema}.
 *
 * <p>
 * When the schema changes on purpose (new Spring Boot or dependency version, generator
 * change), refresh the tracked files and commit them:
 *
 * <pre>
 * mvn -Pdefault verify -Dschema.attachments.update=true
 * </pre>
 */
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class SimpleBootJsonSchemaGeneratorTests {

	/** System property that makes the test rewrite the tracked attachments. */
	static final String UPDATE_PROPERTY = "schema.attachments.update";

	private static final String SCHEMA_BASE_URL = "https://www.alexmond.org/spring-boot-config-json-schema-starter/current/";

	private static final Path ATTACHMENTS = Paths.get("../docs/modules/ROOT/attachments");

	private static final Path GENERATED = Paths.get("target/generated-schema");

	@Autowired
	JsonConfigSchemaConfig config;

	@Autowired
	private JsonSchemaService jsonSchemaService;

	@Test
	void generatedSchemaMatchesPublishedAttachments() throws IOException {
		config.setSchemaId(SCHEMA_BASE_URL + "boot-generic-config.json");
		String json = jsonSchemaService.generateFullSchemaJson();
		// The schema is built once and cached, so the YAML copy gets its own $id here.
		jsonSchemaService.getSchemaCache().setId(SCHEMA_BASE_URL + "boot-generic-config.yaml");
		String yaml = jsonSchemaService.generateFullSchemaYaml();

		List<String> outdated = new ArrayList<>();
		check("boot-generic-config.json", json, outdated);
		check("boot-generic-config.yaml", yaml, outdated);

		if (!outdated.isEmpty()) {
			fail("The published schema attachments differ from the schema generated on Java "
					+ System.getProperty("java.version") + ":\n" + String.join("\n", outdated)
					+ "\nIf the change is intended, refresh them with 'mvn -Pdefault verify -D" + UPDATE_PROPERTY
					+ "=true' and commit the result. If this fails on one JDK only, the generator output depends"
					+ " on the JDK, which is a bug. The generated files are in " + GENERATED.toAbsolutePath());
		}
	}

	private void check(String fileName, String content, List<String> outdated) throws IOException {
		String generated = normalize(content);
		Files.createDirectories(GENERATED);
		Files.writeString(GENERATED.resolve(fileName), generated, StandardCharsets.UTF_8);

		Path tracked = ATTACHMENTS.resolve(fileName);
		if (Boolean.getBoolean(UPDATE_PROPERTY)) {
			log.info("Updating {}", tracked);
			Files.writeString(tracked, generated, StandardCharsets.UTF_8);
			return;
		}
		String published = Files.exists(tracked) ? normalize(Files.readString(tracked, StandardCharsets.UTF_8)) : "";
		if (!published.equals(generated)) {
			outdated.add("  " + tracked + ": " + firstDifference(published, generated));
		}
	}

	/** Line endings are the only thing allowed to differ between platforms. */
	private static String normalize(String text) {
		return text.replace("\r\n", "\n");
	}

	private static String firstDifference(String published, String generated) {
		String[] publishedLines = published.split("\n", -1);
		String[] generatedLines = generated.split("\n", -1);
		int common = Math.min(publishedLines.length, generatedLines.length);
		for (int i = 0; i < common; i++) {
			if (!publishedLines[i].equals(generatedLines[i])) {
				return "first difference at line " + (i + 1) + ", published '" + publishedLines[i].strip()
						+ "', generated '" + generatedLines[i].strip() + "'";
			}
		}
		return "published has " + publishedLines.length + " lines, generated has " + generatedLines.length;
	}

}
