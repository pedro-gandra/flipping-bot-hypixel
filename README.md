# Hypixel Skyblock - Flipping bot

Versão modificada do Mod Coder Pack (MCP) para Minecraft que permite automatizar tarefas dentro do Hypixel Skyblock.

Essa documentação tem como objetivo apenas elucidar a implementação realizada e sua aplicabilidade, mas serão incluídas breves instruções de download e utilização ao final.

## 🌎​ O ambiente

Hypixel Skyblock é um jogo de estilo MMORPG dentro do Minecraft, sendo o mais popular de seu gênero e possuindo uma comunidade ativa de centenas de milhares de jogadores.

Dentro do jogo, o método mais utilizado para negociação de items entre os jogadores é o "Auction House" (AH), que funciona como uma loja P2P, onde um jogador anuncia um item qualquer por um preço determinado e outro jogador pode comprá-lo diretamente.

O servidor responsável pelo título, Hypixel, disponibiliza uma API pública e gratuita que contém diversas informações sobre o jogo, inclusive referentes às negociações ativas e finalizadas no AH.

## ​💡 A proposta

A partir desse ambiente, esse projeto busca implementar um sistema autônomo capaz de "flippar" items, comprando-os em preços atrativos e revendendo.

Para tal, o sistema desenvolvido conta com as seguintes funcionalidades.

- Algoritmo que captura informações de vendas já realizadas e atualiza uma base de dados para uso posterior.
- Algoritmo que captura items listados recentemente, usa a base de dados já acumulada para precificá-los e identifica oportunidades.
- Script que executa a ação de compra e venda dentro do jogo.

Como existem milhares de items nesse universo, e cada um pode ter dezenas de atributos adicionais, criando virtualmente infinitas combinações, cada um desse algoritmos tem uma complexidade razoável que será explicada nessa documentação.

## 🔑​ API - Endpoints utilizados

Caminho base `https://api.hypixel.net`

- **E1:** `/v2/skyblock/auctions_ended` fornece informações das vendas realizadas no AH nos últimos 60 segundos.
- **E2:** `/v2/skyblock/auctions` fornece informações de todos os items listados para venda no momento, com paginação.

A informação específica usada para implementação inGame do item está codificada em Base64 em ambos os endpoints.

## ​📊​ Base de dados

Para armazenar de maneira prática e organizada as informações fornecidas pelo E1, é necessário categorizar os items e implementá-los como classes, mantendo como atributos apenas as características capazes de alterar seu valor, características essas que normalmente são exclusivas à categoria.

Cada categoria possui sua própria base de dados (.csv). No momento estão implementadas as seguintes categorias e atributos:

- **Items Básicos** (Esses atributos também se aplicam às próximas)
    - Nome
    - Raridade
    - Valor da venda
    - Data/hora da venda
- **Pets**  
    - Atributos básicos
    - Nível (0 a 100)
    - Skin
- **Armaduras**  
    - Atributos básicos
    - Reforge
    - Estrelas (Dungeon e Masters)
    - Buffs adicionais (HPB, AOP)
    - Cosméticos (Dye, Skin)
    - Encantamentos relevantes

Alguns outros atributos não mencionados também são coletados e armazenados para essas categorias, mas no momento não são utilizados para precificação dos items.

No caso de items idênticos (exceto informações de venda), o algoritmo permite no máximo 10 duplicatas, priorizando os mais recentes, que serão usadas na precificação. Items vendidos há mais de 20 dias são removidos da base.

## 💲​ Precificação

A precificação de um item listado em tempo real, para garantir uma operação com ambos potencial lucrativo e boa líqudez é executada em duas etapas, são elas:

**Cálculo de valor real (VR)**

Para determinar o VR dos items, são usados os dados acumulados nas bases de dados locais. Seguindo os seguintes critérios:

1. Para cada categoria, são procurados items "equivalentes" que estão registrados na base. A função de equivalência é diferente para cada categoria e nem sempre significa que os items são idênticos.
2. Para cada categoria há um número mínimo de items equivalentes, caso esse número não seja atingido o VR é descartado.
3. No máximo 10 items são selecionados para cada cálculo, sendo estes os 10 items equivalentes mais recentemente vendidos.
4. Os registros com preços extremos (inferiores e superiores) são descartados para evitar distorções e os restantes são utilizados para retornar uma média aritmética.

Caso o item alvo esteja listado por um valor suficientemente abaixo do VR, passamos à segunda etapa:

**Confirmação de liquidez**

A cada 20 minutos, outro algoritmo consulta a API (E2) e armazena em cache todos os items ativos que foram listados no AH nas últimas ~24h. Isso é utilizado da seguinte forma:

Caso o item alvo seja atrativo comparado ao seu VR, é checado no cache se há algum item equivalente listado por um valor também menor que o VR, caso haja, esse valor inferior é considerado como o novo VR, e o item listado também deve ter uma margem atrativa comparada a esse patamar mais baixo.

Dessa forma, é possível garantir que a operação terá mais liquidez, pois não será executada em um momento de mercado onde os items estão subprecificados. O objetivo desse sistema é "flippar" items, para isso, é necessário que o capital utilizado gire rapidamente, e se os items demorarem a vender devido ao momento de mercado isso normalmente implica em um grande custo de oportunidade que supera o lucro da operação.

## ​🎮​ Utilização InGame

Para começar a construir a base de dados o usuário sequer precisa estar logado no servidor Hypixel, ele precisa apenas executar o comando `".history start"` no chat do jogo e manter a instância aberta, podendo inclusive sair de qualquer mundo e permanecer no menu principal.

Após 3 dias de coleta de dados (24h), a base já estará rica o suficiente para começar as operações.

Uma vez já tendo uma base de dados mínima e logando dentro de Hypixel Skyblock, existem dois requisitos para o bom funcionamento do sistema.

1. **Booster Cookie** para permitir o acesso ao AH através do comando "/ah"
2. Quantidade de moedas mínima para ser possível fazer ao menos algumas operações.

A Thread de coleta de dados deve permanecer ativa, para continuar atualizando a base utilizada. Já a Thread de "flipping" pode ser ativada e desativada com a tecla "G".

A príncipio a aplicação funciona de maneira totalmente automática, mas lag, reboot de servidores, entre outros, podem causar problemas para a Thread de flipping e tornarem necessário um reínicio manual.

## ⚙️​ Download, configuração e demonstração

Como dito na introdução, o objetivo da documentação não é esse, e esse ambiente pode ser razoavelmente complicado de configurar e rodar por um usuário sem experiência prévia com o Minecraft Coder Pack.

No entanto, todos os recursos necessários para tal estão incluídos no repositório, incluindo o RAR original do MCP 1.8.8 e do Optifine, que são a versão base do jogo utilizada.

Será linkado no futuro um vídeo aqui mostrando como esses recursos devem ser utilizados e demonstrando o software em ação.

## 📚 Créditos

Desenvolvido por **Pedro Augusto Gandra de Andrade**.

Possibilitado por **Minecraft Coder Pack**, disponível em: http://www.modcoderpack.com/.






