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

CREATE TABLE IF NOT EXISTS blog(
	id TEXT PRIMARY KEY,
	titulo TEXT NOT NULL,
	categoria TEXT NOT NULL,
	conteudo TEXT NOT NULL,
	imagem_url TEXT NOT NULL
)
