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

	@Test
	void ctaKickersUseWhiteText() throws IOException {
		String index = new ClassPathResource("templates/index.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String blog = new ClassPathResource("templates/blog.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String styles = new ClassPathResource("static/css/styles.css")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(index).contains("<span class=\"kicker\">Próximo passo</span>");
		assertThat(blog).contains("<span class=\"kicker\">Estratégia de conteúdo</span>");
		assertThat(styles).contains(".cta-box .kicker {\n  color: #fff;\n}");
	}

	@Test
	void careerHeroUsesSelectedBackgroundAndPreservesStructure() throws IOException {
		String template = new ClassPathResource("templates/trabalhe-conosco.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String styles = new ClassPathResource("static/css/styles.css")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(template)
				.contains("<section class=\"page-hero page-hero-career\">")
				.contains("<div class=\"career-visual reveal\" aria-hidden=\"true\">")
				.contains("<div class=\"career-badge\">APZ</div>")
				.contains("Escuta ativa", "Afeto e rotina", "Educação infantil")
				.contains("Enviar currículo", "curriculo.alphabetz@alphabetz.com.br");
		assertThat(styles)
				.contains(".page-hero-career {\n  background:")
				.contains("3013153_1_054759271173.jpg")
				.contains(".page-hero-career > .page-hero-grid > .reveal > .kicker")
				.contains(".page-hero-career .hero-cta .btn-outline");
	}

	@Test
	void careerCardsUseFontAwesomeIconsInsteadOfEmoji() throws IOException {
		String template = new ClassPathResource("templates/trabalhe-conosco.html")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(template)
				.contains("<i class=\"fa-solid fa-heart\" aria-hidden=\"true\"></i>")
				.contains("<i class=\"fa-solid fa-book-open\" aria-hidden=\"true\"></i>")
				.contains("<i class=\"fa-solid fa-people-group\" aria-hidden=\"true\"></i>")
				.doesNotContain("🧡", "📚", "🤝");
	}

	@Test
	void contactHeroUsesSelectedBackgroundAndPreservesStructure() throws IOException {
		String template = new ClassPathResource("templates/contato.html")
				.getContentAsString(StandardCharsets.UTF_8);
		String styles = new ClassPathResource("static/css/styles.css")
				.getContentAsString(StandardCharsets.UTF_8);

		assertThat(template)
				.contains("<section class=\"page-hero page-hero-contact\">")
				.contains("<div class=\"page-hero-visual reveal\" aria-hidden=\"true\">")
				.contains("<div class=\"stack-card\"><strong>WhatsApp</strong><span>(27) 3029-1110</span></div>")
				.contains("<div class=\"stack-card\"><strong>Endereço</strong><span>Rua Florentina Faller, 45</span></div>")
				.contains("<div class=\"stack-card\"><strong>Horário</strong><span>7:20h às 18:45h</span></div>");
		assertThat(styles)
				.contains(".page-hero-contact {\n  background:")
				.contains("cuida5.jpg")
				.contains(".page-hero-contact > .page-hero-grid > .reveal > .kicker");
	}

}
