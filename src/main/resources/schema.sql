CREATE TABLE IF NOT EXISTS admin (
	id TEXT PRIMARY KEY,
	username TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS slides (
	id TEXT PRIMARY KEY,
	titulo TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS depoimentos (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	depoimento TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS turmas_imagens (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS slide_images (
	id TEXT PRIMARY KEY,
	slide_id TEXT NOT NULL,
	imagem_url TEXT NOT NULL,
	ordem INTEGER NOT NULL DEFAULT 0,
	FOREIGN KEY (slide_id) REFERENCES slides(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_slide_images_slide_id_order
	ON slide_images(slide_id, ordem);

CREATE TABLE IF NOT EXISTS blog_categories (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL COLLATE NOCASE UNIQUE
);

CREATE TABLE IF NOT EXISTS app_settings (
	chave TEXT PRIMARY KEY,
	valor TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS career_applications (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	email TEXT NOT NULL,
	telefone TEXT NOT NULL,
	area TEXT NOT NULL,
	formacao TEXT NOT NULL,
	disponibilidade TEXT NOT NULL,
	experiencia TEXT NOT NULL,
	linkedin TEXT NOT NULL,
	consentimento INTEGER NOT NULL,
	enviado_em TEXT NOT NULL,
	curriculo_nome TEXT NOT NULL,
	curriculo_tipo TEXT NOT NULL,
	curriculo_tamanho INTEGER NOT NULL,
	curriculo_conteudo BLOB NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_career_applications_enviado_em
	ON career_applications(enviado_em DESC);

CREATE TABLE IF NOT EXISTS ouvidoria_manifestacoes (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	sobrenome TEXT NOT NULL,
	email TEXT NOT NULL,
	celular TEXT NOT NULL,
	perfil TEXT NOT NULL,
	area_atuacao TEXT NOT NULL,
	assunto TEXT NOT NULL,
	detalhes TEXT NOT NULL,
	receber_retorno INTEGER NOT NULL,
	enviado_em TEXT NOT NULL,
	arquivo_nome TEXT,
	arquivo_tipo TEXT,
	arquivo_tamanho INTEGER NOT NULL DEFAULT 0,
	arquivo_conteudo BLOB,
	documento_imagem_nome TEXT,
	documento_imagem_tipo TEXT,
	documento_imagem_tamanho INTEGER NOT NULL DEFAULT 0,
	documento_imagem_conteudo BLOB
);

CREATE INDEX IF NOT EXISTS idx_ouvidoria_manifestacoes_enviado_em
	ON ouvidoria_manifestacoes(enviado_em DESC);

INSERT OR IGNORE INTO blog_categories (id, nome)
SELECT id, nome
FROM (
	SELECT '00000000-0000-0000-0000-000000000001' AS id, 'Pedagogia' AS nome
	UNION ALL SELECT '00000000-0000-0000-0000-000000000002', 'Família'
	UNION ALL SELECT '00000000-0000-0000-0000-000000000003', 'Rotina'
	UNION ALL SELECT '00000000-0000-0000-0000-000000000004', 'Projetos'
)
WHERE NOT EXISTS (
	SELECT 1 FROM app_settings WHERE chave = 'blog_categories_initialized'
);

INSERT OR IGNORE INTO app_settings (chave, valor)
VALUES ('blog_categories_initialized', 'true');

CREATE TABLE IF NOT EXISTS blog(
	id TEXT PRIMARY KEY,
	titulo TEXT NOT NULL,
	categoria TEXT NOT NULL,
	conteudo TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

INSERT OR IGNORE INTO blog_categories (id, nome)
SELECT lower(
	hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' ||
	hex(randomblob(2)) || '-' || hex(randomblob(2)) || '-' || hex(randomblob(6))
), trim(categoria)
FROM blog
WHERE categoria IS NOT NULL AND trim(categoria) <> '';
