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

	@Test
	void blogHeroUsesSelectedBackgroundAndPreservesStructure() throws IOException {
		String template = new ClassPathResource("templates/blog.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String styles = new ClassPathResource("static/css/styles.css")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(template)
				.contains("<section class=\"page-hero page-hero-blog\">")
				.contains("<div class=\"blog-hero-card reveal\" aria-hidden=\"true\">")
				.contains("<div class=\"editorial-card\">")
				.contains("Ver destaques")
				.contains("Acessar blog oficial");
		assertThat(styles)
				.contains(".page-hero-blog {\n  background:")
				.contains("3013153_1_054758958225.jpg")
				.contains(".page-hero-blog > .page-hero-grid > .reveal > .kicker")
				.contains(".page-hero-blog .hero-cta .btn-outline");
	}

}
