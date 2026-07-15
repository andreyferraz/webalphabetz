# Hero da home com imagem de fundo

## Objetivo

Atualizar o hero da página inicial para usar a fotografia `brinca11.jpg` como plano de fundo, remover a imagem lateral atual e permitir que o conteúdo textual ocupe toda a largura disponível da grade.

## Estrutura

- Remover do `index.html` o bloco `.hero-media`, incluindo a imagem `capahome.png`.
- Manter o conteúdo existente, seus links e sua hierarquia semântica.
- Alterar `.hero-grid` para uma única coluna e deixar `.hero-content` ocupar toda a largura do container.

## Tratamento visual

- Aplicar `/assets/slide/brinca11.jpg` ao background de `.hero`.
- Usar `background-size: cover` e posicionamento responsivo para preencher a seção sem distorcer a imagem.
- Sobrepor um degradê horizontal baseado em `#01273E`, mais intenso atrás do texto e progressivamente mais transparente sobre o restante da fotografia.
- Usar somente as cores institucionais informadas: `#E95032`, `#FFA700`, `#FFDD00`, `#01273E`, `#215FC4`, `#64A0EE`, `#004E4A`, `#7AC6BA` e `#F8C7DE`. Transparências dessas cores são permitidas para o overlay.
- Ajustar as cores do texto, das marcações e do botão secundário dentro do hero para garantir contraste sobre o fundo.

## Responsividade e acessibilidade

- No desktop, preservar a leitura do conteúdo e a visibilidade da atividade retratada na foto.
- Em telas menores, reforçar o degradê e ajustar o ponto focal do background para manter texto e fotografia legíveis.
- Tratar a imagem como decorativa, pois ela será aplicada em CSS; o título da seção continuará identificando seu conteúdo para tecnologias assistivas.
- Preservar o foco visível, os rótulos e os destinos dos botões existentes.

## Verificação

- Confirmar que não resta a imagem lateral no HTML.
- Confirmar que o conteúdo do hero ocupa uma única coluna em todos os breakpoints.
- Verificar que o CSS referencia corretamente `brinca11.jpg`.
- Executar os testes disponíveis do projeto e revisar o diff final.
