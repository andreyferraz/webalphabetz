package com.alphabetz.webalphabetz;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebalphabetzApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void turmasHeroUsesSelectedBackgroundWithoutStackCards() throws IOException {
		String template = new ClassPathResource("templates/turmas.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String styles = new ClassPathResource("static/css/styles.css")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(template)
				.contains("<section class=\"page-hero page-hero-classes\">")
				.doesNotContain("page-hero-visual", "stack-card");
		assertThat(styles)
				.contains(".page-hero-classes")
				.contains("3013153_1_1753361936458585664001742.jpg")
				.contains(".page-hero-classes .page-hero-grid")
				.contains(".page-hero-classes p")
				.contains("max-width: none;");
	}

}
