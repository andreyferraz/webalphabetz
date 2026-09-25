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

CREATE TABLE IF NOT EXISTS fundo_topo (
	id TEXT PRIMARY KEY,
	nome_pagina TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS turmas_imagens (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS equipe_membros (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	cargo TEXT NOT NULL,
	imagem_url TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_equipe_membros_cargo_nome
	ON equipe_membros(cargo, nome);

CREATE TABLE IF NOT EXISTS documentos_institucionais (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL,
	nome_arquivo TEXT NOT NULL,
	tipo_conteudo TEXT NOT NULL,
	tamanho INTEGER NOT NULL,
	conteudo BLOB NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_documentos_institucionais_nome
	ON documentos_institucionais(nome COLLATE NOCASE);

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

CREATE TABLE IF NOT EXISTS matricula_documentos (
	id TEXT PRIMARY KEY,
	tipo TEXT NOT NULL UNIQUE,
	nome_arquivo TEXT NOT NULL,
	tipo_conteudo TEXT NOT NULL,
	tamanho INTEGER NOT NULL,
	conteudo BLOB NOT NULL
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

CREATE TABLE IF NOT EXISTS equipe_cargos (
	id TEXT PRIMARY KEY,
	nome TEXT NOT NULL COLLATE NOCASE UNIQUE,
	ordem INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_equipe_cargos_ordem_nome
	ON equipe_cargos(ordem, nome COLLATE NOCASE);

INSERT OR IGNORE INTO equipe_cargos (id, nome, ordem)
SELECT id, nome, ordem
FROM (
	SELECT '00000000-0000-0000-0000-000000000101' AS id, 'Presidente do Conselho' AS nome, 1 AS ordem
	UNION ALL SELECT '00000000-0000-0000-0000-000000000102', 'Diretor Executivo', 2
	UNION ALL SELECT '00000000-0000-0000-0000-000000000103', 'Diretora de Ensino', 3
	UNION ALL SELECT '00000000-0000-0000-0000-000000000104', 'Coordenadora Pedagógica', 4
	UNION ALL SELECT '00000000-0000-0000-0000-000000000105', 'Assistente Administrativo', 5
	UNION ALL SELECT '00000000-0000-0000-0000-000000000106', 'Professora Regente', 6
	UNION ALL SELECT '00000000-0000-0000-0000-000000000107', 'Professora de Inglês', 7
	UNION ALL SELECT '00000000-0000-0000-0000-000000000108', 'Auxiliar de Sala', 8
	UNION ALL SELECT '00000000-0000-0000-0000-000000000109', 'Berçarista', 9
	UNION ALL SELECT '00000000-0000-0000-0000-000000000110', 'Atelierista', 10
	UNION ALL SELECT '00000000-0000-0000-0000-000000000111', 'Porteiro', 11
	UNION ALL SELECT '00000000-0000-0000-0000-000000000112', 'Auxiliar de Serviços Gerais', 12
	UNION ALL SELECT '00000000-0000-0000-0000-000000000113', 'Auxiliar de Cozinha', 13
)
WHERE NOT EXISTS (
	SELECT 1 FROM app_settings WHERE chave = 'equipe_cargos_initialized'
);

INSERT OR IGNORE INTO app_settings (chave, valor)
VALUES ('equipe_cargos_initialized', 'true');

UPDATE equipe_membros SET cargo = 'Presidente do Conselho' WHERE cargo = 'PRESIDENTE_CONSELHO';
UPDATE equipe_membros SET cargo = 'Diretor Executivo' WHERE cargo IN ('DIRETOR_EXECUTIVO', 'DIRETOR_EXECUTIVO_CEO');
UPDATE equipe_membros SET cargo = 'Diretora de Ensino' WHERE cargo = 'DIRETORA_ENSINO';
UPDATE equipe_membros SET cargo = 'Coordenadora Pedagógica' WHERE cargo = 'COORDENADORA_PEDAGOGICA';
UPDATE equipe_membros SET cargo = 'Assistente Administrativo' WHERE cargo = 'ASSISTENTE_ADMINISTRATIVO';
UPDATE equipe_membros SET cargo = 'Professora Regente' WHERE cargo = 'PROFESSORA_REGENTE';
UPDATE equipe_membros SET cargo = 'Professora de Inglês' WHERE cargo = 'PROFESSORA_INGLES';
UPDATE equipe_membros SET cargo = 'Auxiliar de Sala' WHERE cargo = 'AUXILIAR_SALA';
UPDATE equipe_membros SET cargo = 'Berçarista' WHERE cargo = 'BERCARISTA';
UPDATE equipe_membros SET cargo = 'Atelierista' WHERE cargo = 'ATELIERISTA';
UPDATE equipe_membros SET cargo = 'Porteiro' WHERE cargo = 'PORTEIRO';
UPDATE equipe_membros SET cargo = 'Auxiliar de Serviços Gerais' WHERE cargo = 'AUXILIAR_SERVICOS_GERAIS';
UPDATE equipe_membros SET cargo = 'Auxiliar de Cozinha' WHERE cargo = 'AUXILIAR_COZINHA';


