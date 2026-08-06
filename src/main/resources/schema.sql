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
