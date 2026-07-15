# Cores das etiquetas de idade dos programas

## Objetivo

Fazer cada etiqueta `.program-age` usar a mesma cor sólida exibida no fundo da imagem do respectivo card, substituindo o amarelo atual.

## Desenho aprovado

- Cada `.program-card` definirá uma variável CSS `--program-color` com a cor do programa.
- `.program-visual` e `.program-age` usarão `background: var(--program-color)` para evitar duplicação e manter as duas áreas sincronizadas.
- As cores existentes serão preservadas: `#F8C7DE`, `#7AC6BA`, `#004E4A`, `#64A0EE` e `#215FC4`.
- O texto da idade continuará azul-marinho nos cards 1, 2 e 4.
- O texto da idade será branco nos cards 3 e 5, cujos fundos são escuros.
- O HTML, os textos, o espaçamento e o restante do layout não serão alterados.

## Verificação

- Confirmar no CSS que imagem e etiqueta consomem a mesma variável de cor.
- Confirmar que não resta `background: var(--yellow)` na regra `.program-age` desses cards.
- Executar os testes disponíveis do projeto para detectar regressões.
