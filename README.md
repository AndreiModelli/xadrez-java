# ♟ Xadrez em Java (CLI)

Jogo de xadrez completo implementado em Java com interface via linha de comando (terminal). Projeto desenvolvido como trabalho acadêmico com foco em **Programação Orientada a Objetos**.

## Funcionalidades

- Partida completa de xadrez com todas as peças e movimentos padrão
- Modo **Jogador vs Jogador** (PvP)
- Modo **Jogador vs Computador** (Bot com movimentos aleatórios)
- Detecção de **Xeque** e **Xeque-mate**
- Validação completa de jogadas (impede movimentos ilegais e auto-xeque)
- Tabuleiro renderizado em ASCII com cores no terminal
- Destaque visual de movimentos possíveis
- Exibição de peças capturadas
- Notação algébrica flexível (`e2e4` ou `e2` + `e4`)
- Opção de desistência a qualquer momento
- Bateria de testes automatizados integrada

## Estrutura do Projeto

```
Core/src/
├── boardgame/              # Camada genérica de tabuleiro
│   ├── Board.java          # Tabuleiro 8x8 (matriz de peças)
│   ├── Piece.java          # Classe abstrata base para todas as peças
│   └── Position.java       # Representação de posição (linha, coluna)
│
├── chess/                  # Regras e peças do xadrez
│   ├── Color.java          # Enum (WHITE, BLACK)
│   ├── ChessMatch.java     # Motor do jogo (turnos, validações, xeque/mate)
│   ├── King.java           # Rei
│   ├── Queen.java          # Rainha
│   ├── Rook.java           # Torre
│   ├── Bishop.java         # Bispo
│   ├── Knight.java         # Cavalo
│   └── Pawn.java           # Peão
│
└── application/            # Interface e fluxo principal
    ├── Program.java        # Main - menu, loop de jogo, testes
    ├── UI.java             # Renderização do tabuleiro (ASCII + cores ANSI)
    ├── InputReader.java    # Parser de notação algébrica e comandos
    └── BotPlayer.java      # IA básica (movimento aleatório válido)
```

## Conceitos de POO Aplicados

| Conceito | Aplicação |
|----------|-----------|
| **Herança** | `King`, `Queen`, `Rook`, etc. estendem `Piece` |
| **Polimorfismo** | Cada peça implementa `possibleMoves()` com sua lógica própria |
| **Encapsulamento** | Atributos privados com acesso via getters; `Board` protege a matriz interna |
| **Abstração** | `Piece` é abstrata — obriga subclasses a definir movimentos |
| **Composição** | `ChessMatch` compõe `Board`; `Board` contém `Piece[][]` |
| **Enumeração** | `Color` define as duas cores de forma type-safe |

## Como Compilar e Executar

### Pré-requisitos

- **JDK 8+** instalado e configurado no PATH

### Compilação

```bash
cd Core/src
javac -encoding UTF-8 boardgame/*.java chess/*.java application/*.java
```

### Execução

```bash
java application.Program
```

## Como Jogar

### Menu Principal

```
╔═══════════════════════════════════╗
║         XADREZ EM JAVA           ║
╠═══════════════════════════════════╣
║  1 - Jogador vs Jogador (PvP)    ║
║  2 - Jogador vs Computador (Bot) ║
║  3 - Executar Testes             ║
║  0 - Sair                        ║
╚═══════════════════════════════════╝
```

### Notação de Movimentos

- Digite a posição de **origem** e **destino** usando notação algébrica
- Formato separado: `e2` (enter) `e4` (enter)
- Formato direto: `e2e4` (enter)
- Colunas: `a` até `h` (esquerda → direita)
- Linhas: `1` até `8` (baixo → cima)

### Representação das Peças

| Símbolo | Peça |
|---------|------|
| K / k | Rei (King) |
| Q / q | Rainha (Queen) |
| R / r | Torre (Rook) |
| B / b | Bispo (Bishop) |
| N / n | Cavalo (Knight) |
| P / p | Peão (Pawn) |

> Maiúsculas = Brancas | Minúsculas = Pretas

### Comandos Especiais

- `desistir`, `quit` ou `sair` — encerra a partida (derrota por desistência)

## Exemplo de Tabuleiro

```
    a   b   c   d   e   f   g   h
  +---+---+---+---+---+---+---+---+
8 | r | n | b | q | k | b | n | r | 8
  +---+---+---+---+---+---+---+---+
7 | p | p | p | p | p | p | p | p | 7
  +---+---+---+---+---+---+---+---+
6 | . | . | . | . | . | . | . | . | 6
  +---+---+---+---+---+---+---+---+
5 | . | . | . | . | . | . | . | . | 5
  +---+---+---+---+---+---+---+---+
4 | . | . | . | . | . | . | . | . | 4
  +---+---+---+---+---+---+---+---+
3 | . | . | . | . | . | . | . | . | 3
  +---+---+---+---+---+---+---+---+
2 | P | P | P | P | P | P | P | P | 2
  +---+---+---+---+---+---+---+---+
1 | R | N | B | Q | K | B | N | R | 1
  +---+---+---+---+---+---+---+---+
    a   b   c   d   e   f   g   h
```

## Testes Integrados

O programa inclui uma bateria de testes acessível pelo menu (opção 3):

1. **Estado inicial** — verifica configuração correta do tabuleiro
2. **Alternância de turnos** — bloqueia peças do oponente
3. **Prevenção de auto-xeque** — impede jogadas que expõem o rei
4. **Detecção de xeque-mate** — reconhece Fool's Mate (mate em 2 lances)
5. **Movimento do Bot** — valida que a IA escolhe jogadas legais

## Arquitetura em Camadas

```
┌─────────────────────────────────┐
│   application (UI + Fluxo)      │  ← Interface CLI, Bot, Loop principal
├─────────────────────────────────┤
│   chess (Motor do Jogo)         │  ← Regras, validações, xeque/mate
├─────────────────────────────────┤
│   boardgame (Modelo Base)       │  ← Tabuleiro e peças genéricos
└─────────────────────────────────┘
```

## Tecnologias

- Java 8+
- Sem dependências externas (apenas biblioteca padrão)
- Cores ANSI para terminais compatíveis

## Autores

Projeto acadêmico desenvolvido em equipe para a disciplina de Programação Orientada a Objetos.
