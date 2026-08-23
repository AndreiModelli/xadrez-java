package application;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;
import chess.Color;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Classe principal do sistema de xadrez.
 * Gerencia o menu, modos de jogo (PvP e PvBot), loop de turnos,
 * desistência e condições de fim de jogo.
 */
public class Program {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            UI.clearScreen();
            UI.printMenu();

            String option = sc.nextLine().trim();

            switch (option) {
                case "1":
                    playPvP(sc);
                    break;
                case "2":
                    playPvBot(sc);
                    break;
                case "3":
                    runTests();
                    System.out.println("\nPressione Enter para voltar ao menu...");
                    sc.nextLine();
                    break;
                case "0":
                    System.out.println("Encerrando o programa. Ate logo!");
                    sc.close();
                    return;
                default:
                    System.out.println("Opcao invalida. Pressione Enter...");
                    sc.nextLine();
            }
        }
    }

    // =========================================================================
    // MODO PvP (Jogador vs Jogador)
    // =========================================================================
    private static void playPvP(Scanner sc) {
        ChessMatch match = new ChessMatch();

        UI.clearScreen();
        UI.printInstructions();
        System.out.println("\nPressione Enter para comecar...");
        sc.nextLine();

        while (!match.isCheckMate()) {
            try {
                UI.clearScreen();
                UI.printMatch(match);

                // Lê o movimento do jogador
                InputReader.MoveInput moveInput = InputReader.readMove(sc);

                // Verifica desistência
                if (moveInput.isResignation()) {
                    handleResignation(match);
                    System.out.println("\nPressione Enter para voltar ao menu...");
                    sc.nextLine();
                    return;
                }

                // Executa o movimento
                Piece captured = match.performChessMove(moveInput.getSource(), moveInput.getTarget());

                if (captured != null) {
                    System.out.println("\n>> Peca capturada: " + captured);
                }

            } catch (RuntimeException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            }
        }

        // Fim de jogo por xeque-mate
        UI.clearScreen();
        UI.printMatch(match);
        System.out.println("\nPressione Enter para voltar ao menu...");
        sc.nextLine();
    }

    // =========================================================================
    // MODO PvBot (Jogador vs Computador)
    // =========================================================================
    private static void playPvBot(Scanner sc) {
        System.out.print("\nEscolha sua cor (1 = Brancas, 2 = Pretas): ");
        String colorChoice = sc.nextLine().trim();

        Color playerColor;
        Color botColor;

        if (colorChoice.equals("2")) {
            playerColor = Color.BLACK;
            botColor = Color.WHITE;
        } else {
            playerColor = Color.WHITE;
            botColor = Color.BLACK;
        }

        ChessMatch match = new ChessMatch();
        BotPlayer bot = new BotPlayer(botColor);

        UI.clearScreen();
        UI.printInstructions();
        System.out.println("Voce joga com: " + (playerColor == Color.WHITE ? "BRANCAS" : "PRETAS"));
        System.out.println("Bot joga com: " + (botColor == Color.WHITE ? "BRANCAS" : "PRETAS"));
        System.out.println("\nPressione Enter para comecar...");
        sc.nextLine();

        while (!match.isCheckMate()) {
            try {
                UI.clearScreen();
                UI.printMatch(match);

                // Verifica de quem é a vez
                if (match.getCurrentPlayer() == playerColor) {
                    // Vez do jogador humano
                    InputReader.MoveInput moveInput = InputReader.readMove(sc);

                    if (moveInput.isResignation()) {
                        handleResignation(match);
                        System.out.println("\nPressione Enter para voltar ao menu...");
                        sc.nextLine();
                        return;
                    }

                    match.performChessMove(moveInput.getSource(), moveInput.getTarget());

                } else {
                    // Vez do Bot
                    System.out.println("\n[Bot pensando...]");

                    // Pequena pausa para simular "reflexão"
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    Position[] botMove = bot.chooseMove(match);

                    if (botMove == null) {
                        System.out.println("Bot sem movimentos disponiveis.");
                        break;
                    }

                    // Tenta executar o movimento do bot (pode falhar por auto-xeque)
                    boolean moveDone = false;
                    int attempts = 0;
                    while (!moveDone && attempts < 50) {
                        try {
                            match.performChessMove(botMove[0], botMove[1]);
                            moveDone = true;

                            String from = BotPlayer.positionToAlgebraic(botMove[0]);
                            String to = BotPlayer.positionToAlgebraic(botMove[1]);
                            System.out.println("Bot jogou: " + from + " -> " + to);

                        } catch (RuntimeException e) {
                            // Movimento inválido (auto-xeque), escolhe outro
                            botMove = bot.chooseMove(match);
                            if (botMove == null) {
                                break;
                            }
                            attempts++;
                        }
                    }

                    if (!moveDone) {
                        System.out.println("Bot nao encontrou jogada valida.");
                        break;
                    }

                    System.out.println("Pressione Enter para continuar...");
                    sc.nextLine();
                }

            } catch (RuntimeException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            }
        }

        // Fim de jogo
        UI.clearScreen();
        UI.printMatch(match);
        System.out.println("\nPressione Enter para voltar ao menu...");
        sc.nextLine();
    }

    // =========================================================================
    // DESISTÊNCIA
    // =========================================================================
    private static void handleResignation(ChessMatch match) {
        Color resigned = match.getCurrentPlayer();
        Color winner = (resigned == Color.WHITE) ? Color.BLACK : Color.WHITE;

        System.out.println("\n========================================");
        System.out.println("  DESISTENCIA!");
        System.out.println("  " + resigned + " desistiu da partida.");
        System.out.println("  Vencedor: " + winner);
        System.out.println("========================================");
    }

    // =========================================================================
    // MODO TESTES (mantido do código original)
    // =========================================================================
    private static void runTests() {
        System.out.println("\n--- INICIANDO BATERIA DE TESTES ---\n");

        testInitialBoard();
        testTurnAlternationAndWrongPiece();
        testSelfCheckPrevention();
        testFoolCheckmate();
        testBotMovement();

        System.out.println("\n[SUCESSO] Todos os testes passaram!");
    }

    private static void testInitialBoard() {
        System.out.print("1. Estado inicial do tabuleiro: ");
        ChessMatch match = new ChessMatch();
        assert match.getTurn() == 1 : "Turno inicial deve ser 1";
        assert match.getCurrentPlayer() == Color.WHITE : "Brancas devem comecar";
        assert !match.isCheck() : "Nao deve estar em xeque";
        assert !match.isCheckMate() : "Nao deve estar em xeque-mate";

        if (match.getTurn() != 1 || match.getCurrentPlayer() != Color.WHITE
                || match.isCheck() || match.isCheckMate()) {
            throw new AssertionError("Falha no estado inicial.");
        }
        System.out.println("OK");
    }

    private static void testTurnAlternationAndWrongPiece() {
        System.out.print("2. Turnos e bloqueio de peca inimiga: ");
        ChessMatch match = new ChessMatch();

        boolean blocked = false;
        try {
            match.performChessMove(new Position(1, 4), new Position(3, 4)); // Preta no turno branco
        } catch (RuntimeException e) {
            blocked = true;
        }
        if (!blocked) {
            throw new AssertionError("Deveria bloquear peca preta no turno 1.");
        }

        match.performChessMove(new Position(6, 4), new Position(4, 4)); // e2 -> e4
        if (match.getTurn() != 2 || match.getCurrentPlayer() != Color.BLACK) {
            throw new AssertionError("Falha na alternancia para o jogador PRETO.");
        }
        System.out.println("OK");
    }

    private static void testSelfCheckPrevention() {
        System.out.print("3. Impedimento de Auto-Xeque: ");
        ChessMatch match = new ChessMatch();

        match.performChessMove(new Position(6, 4), new Position(4, 4)); // e4
        match.performChessMove(new Position(1, 4), new Position(3, 4)); // e5
        match.performChessMove(new Position(7, 3), new Position(3, 7)); // Qh5
        match.performChessMove(new Position(1, 3), new Position(2, 3)); // d6
        match.performChessMove(new Position(3, 7), new Position(1, 5)); // Qxf7+ (xeque)

        if (!match.isCheck()) {
            throw new AssertionError("Deveria acusar xeque.");
        }

        boolean prevented = false;
        try {
            match.performChessMove(new Position(1, 0), new Position(2, 0)); // a7-a6 (nao sai do xeque)
        } catch (RuntimeException e) {
            prevented = true;
        }
        if (!prevented) {
            throw new AssertionError("Deveria barrar jogadas que mantem o rei em xeque.");
        }
        System.out.println("OK");
    }

    private static void testFoolCheckmate() {
        System.out.print("4. Xeque-Mate (Fool's Mate): ");
        ChessMatch match = new ChessMatch();

        match.performChessMove(new Position(6, 5), new Position(5, 5)); // f3
        match.performChessMove(new Position(1, 4), new Position(3, 4)); // e5
        match.performChessMove(new Position(6, 6), new Position(4, 6)); // g4
        match.performChessMove(new Position(0, 3), new Position(4, 7)); // Qh4#

        if (!match.isCheckMate()) {
            throw new AssertionError("Deveria acusar xeque-mate.");
        }
        System.out.println("OK");
    }

    private static void testBotMovement() {
        System.out.print("5. Bot seleciona movimento valido: ");
        ChessMatch match = new ChessMatch();
        BotPlayer bot = new BotPlayer(Color.WHITE);

        Position[] move = bot.chooseMove(match);
        if (move == null || move.length != 2) {
            throw new AssertionError("Bot deveria encontrar pelo menos um movimento.");
        }

        // Verifica se a posição de origem contém uma peça branca
        Piece piece = match.getBoard().piece(move[0]);
        if (piece == null || piece.getColor() != Color.WHITE) {
            throw new AssertionError("Bot deveria selecionar uma peca branca.");
        }

        // Verifica se o destino é um movimento possível da peça
        boolean[][] possibleMoves = piece.possibleMoves();
        if (!possibleMoves[move[1].getRow()][move[1].getColumn()]) {
            throw new AssertionError("Bot selecionou destino invalido.");
        }

        System.out.println("OK (" + BotPlayer.positionToAlgebraic(move[0]) + " -> "
                + BotPlayer.positionToAlgebraic(move[1]) + ")");
    }
}
