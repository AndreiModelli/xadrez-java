package application;

import boardgame.Board;
import boardgame.Color;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;

import java.util.InputMismatchException;
import java.util.Scanner;

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
                case "4":
                    System.out.println("Abrindo modo grafico...");
                    ChessGUI.launch();
                    System.out.println("Modo grafico aberto. Pressione Enter para voltar ao menu...");
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

    private static void playPvP(Scanner sc) {
        ChessMatch match = new ChessMatch();

        UI.clearScreen();
        UI.printInstructions();
        System.out.println("\nPressione Enter para comecar...");
        sc.nextLine();

        while (!match.isCheckMate() && !match.isDraw()) {
            try {
                UI.clearScreen();
                UI.printMatch(match);

                InputReader.MoveInput moveInput = InputReader.readMove(sc);

                if (moveInput.isResignation()) {
                    handleResignation(match);
                    System.out.println("\nPressione Enter para voltar ao menu...");
                    sc.nextLine();
                    return;
                }

                Piece captured = match.performChessMove(moveInput.getSource(), moveInput.getTarget());

                if (captured != null) {
                    System.out.println("\n>> Peca capturada: " + captured);
                }

                if (match.getPromoted() != null) {
                    System.out.println("\n>> PROMOCAO! Escolha a peca (Q=Rainha, R=Torre, B=Bispo, N=Cavalo): ");
                    String promotionChoice = sc.nextLine().trim().toUpperCase();
                    while (!promotionChoice.equals("Q") && !promotionChoice.equals("R")
                            && !promotionChoice.equals("B") && !promotionChoice.equals("N")) {
                        System.out.print("Opcao invalida. Digite Q, R, B ou N: ");
                        promotionChoice = sc.nextLine().trim().toUpperCase();
                    }
                    match.replacePromotedPiece(promotionChoice);
                }

            } catch (RuntimeException e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            }
        }

        UI.clearScreen();
        UI.printMatch(match);

        if (match.isDraw()) {
            System.out.println("\n========================================");
            System.out.println("  EMPATE por insuficiencia material!");
            System.out.println("========================================");
        }

        System.out.println("\nPressione Enter para voltar ao menu...");
        sc.nextLine();
    }

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

        while (!match.isCheckMate() && !match.isDraw()) {
            try {
                UI.clearScreen();
                UI.printMatch(match);

                if (match.getCurrentPlayer() == playerColor) {

                    InputReader.MoveInput moveInput = InputReader.readMove(sc);

                    if (moveInput.isResignation()) {
                        handleResignation(match);
                        System.out.println("\nPressione Enter para voltar ao menu...");
                        sc.nextLine();
                        return;
                    }

                    match.performChessMove(moveInput.getSource(), moveInput.getTarget());

                    if (match.getPromoted() != null) {
                        System.out.println("\n>> PROMOCAO! Escolha a peca (Q=Rainha, R=Torre, B=Bispo, N=Cavalo): ");
                        String promotionChoice = sc.nextLine().trim().toUpperCase();
                        while (!promotionChoice.equals("Q") && !promotionChoice.equals("R")
                                && !promotionChoice.equals("B") && !promotionChoice.equals("N")) {
                            System.out.print("Opcao invalida. Digite Q, R, B ou N: ");
                            promotionChoice = sc.nextLine().trim().toUpperCase();
                        }
                        match.replacePromotedPiece(promotionChoice);
                    }

                } else {
                    System.out.println("\n[Bot pensando...]");

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

                    boolean moveDone = false;
                    int attempts = 0;
                    while (!moveDone && attempts < 50) {
                        try {
                            match.performChessMove(botMove[0], botMove[1]);
                            moveDone = true;

                            String from = BotPlayer.positionToAlgebraic(botMove[0]);
                            String to = BotPlayer.positionToAlgebraic(botMove[1]);
                            System.out.println("Bot jogou: " + from + " -> " + to);

                            if (match.getPromoted() != null) {
                                match.replacePromotedPiece("Q");
                                System.out.println("Bot promoveu peao para Rainha!");
                            }

                        } catch (RuntimeException e) {
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
            }
        }

        UI.clearScreen();
        UI.printMatch(match);

        if (match.isDraw()) {
            System.out.println("\n========================================");
            System.out.println("  EMPATE por insuficiencia material!");
            System.out.println("========================================");
        }

        System.out.println("\nPressione Enter para voltar ao menu...");
        sc.nextLine();
    }

    private static void handleResignation(ChessMatch match) {
        Color resigned = match.getCurrentPlayer();
        Color winner = (resigned == Color.WHITE) ? Color.BLACK : Color.WHITE;

        System.out.println("\n========================================");
        System.out.println("  DESISTENCIA!");
        System.out.println("  " + resigned + " desistiu da partida.");
        System.out.println("  Vencedor: " + winner);
        System.out.println("========================================");
    }

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
            match.performChessMove(new Position(1, 4), new Position(3, 4));
        } catch (RuntimeException e) {
            blocked = true;
        }
        if (!blocked) {
            throw new AssertionError("Deveria bloquear peca preta no turno 1.");
        }

        match.performChessMove(new Position(6, 4), new Position(4, 4));
        if (match.getTurn() != 2 || match.getCurrentPlayer() != Color.BLACK) {
            throw new AssertionError("Falha na alternancia para o jogador PRETO.");
        }
        System.out.println("OK");
    }

    private static void testSelfCheckPrevention() {
        System.out.print("3. Impedimento de Auto-Xeque: ");
        ChessMatch match = new ChessMatch();

        match.performChessMove(new Position(6, 4), new Position(4, 4));
        match.performChessMove(new Position(1, 4), new Position(3, 4));
        match.performChessMove(new Position(7, 3), new Position(3, 7));
        match.performChessMove(new Position(1, 3), new Position(2, 3));
        match.performChessMove(new Position(3, 7), new Position(1, 5));

        if (!match.isCheck()) {
            throw new AssertionError("Deveria acusar xeque.");
        }

        boolean prevented = false;
        try {
            match.performChessMove(new Position(1, 0), new Position(2, 0));
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

        match.performChessMove(new Position(6, 5), new Position(5, 5));
        match.performChessMove(new Position(1, 4), new Position(3, 4));
        match.performChessMove(new Position(6, 6), new Position(4, 6));
        match.performChessMove(new Position(0, 3), new Position(4, 7));

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

        Piece piece = match.getBoard().piece(move[0]);
        if (piece == null || piece.getColor() != Color.WHITE) {
            throw new AssertionError("Bot deveria selecionar uma peca branca.");
        }

        boolean[][] possibleMoves = piece.possibleMoves();
        if (!possibleMoves[move[1].getRow()][move[1].getColumn()]) {
            throw new AssertionError("Bot selecionou destino invalido.");
        }

        System.out.println("OK (" + BotPlayer.positionToAlgebraic(move[0]) + " -> "
                + BotPlayer.positionToAlgebraic(move[1]) + ")");
    }
}
