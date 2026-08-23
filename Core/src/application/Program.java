package application;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;
import chess.Color;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Program {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=================================");
            System.out.println("       SISTEMA DE XADREZ        ");
            System.out.println("=================================");
            System.out.println("1 - Iniciar Partida (Jogar)");
            System.out.println("2 - Executar Bateria de Testes");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opcao: ");

            String option = sc.nextLine().trim();

            if (option.equals("1")) {
                playGame(sc);
            } else if (option.equals("2")) {
                runTests();
            } else if (option.equals("0")) {
                System.out.println("Encerrando o programa...");
                break;
            } else {
                System.out.println("Opcao invalida. Tente novamente.");
            }
        }

        sc.close();
    }

    // =========================================================================
    // MODO JOGO
    // =========================================================================
    private static void playGame(Scanner sc) {
        ChessMatch chessMatch = new ChessMatch();

        while (!chessMatch.isCheckMate()) {
            try {
                printMatch(chessMatch);

                System.out.print("\nOrigem (ex: e2): ");
                Position source = readPosition(sc);

                System.out.print("Destino (ex: e4): ");
                Position target = readPosition(sc);

                Piece capturedPiece = chessMatch.performChessMove(source, target);

                if (capturedPiece != null) {
                    System.out.println("Peca capturada: " + capturedPiece);
                }

            } catch (Exception e) {
                System.out.println("\n[ERRO] " + e.getMessage());
                System.out.println("Pressione Enter para tentar novamente...");
                sc.nextLine();
            }
        }

        printMatch(chessMatch);
        System.out.println("\nXEQUE-MATE! Vencedor: " + chessMatch.getCurrentPlayer());
        System.out.println("Pressione Enter para voltar ao menu...");
        sc.nextLine();
    }

    private static Position readPosition(Scanner sc) {
        String s = sc.nextLine().trim().toLowerCase();
        if (s.length() < 2) {
            throw new InputMismatchException("Formato invalido. Use notacao algebrica (ex: e2, a7).");
        }
        char columnChar = s.charAt(0);
        int rowNum = Character.getNumericValue(s.charAt(1));

        int row = 8 - rowNum;
        int column = columnChar - 'a';

        return new Position(row, column);
    }

    private static void printMatch(ChessMatch chessMatch) {
        Board board = chessMatch.getBoard();
        System.out.println("\n---------------------------------");
        for (int i = 0; i < board.getRows(); i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < board.getColumns(); j++) {
                Piece piece = board.piece(i, j);
                if (piece == null) {
                    System.out.print(" - ");
                } else {
                    String symbol = piece.toString();
                    if (piece.getColor() == Color.BLACK) {
                        System.out.print(" " + symbol.toLowerCase() + " ");
                    } else {
                        System.out.print(" " + symbol.toUpperCase() + " ");
                    }
                }
            }
            System.out.println();
        }
        System.out.println("   a  b  c  d  e  f  g  h");
        System.out.println("---------------------------------");
        System.out.println("Turno: " + chessMatch.getTurn());
        System.out.println("Vez do jogador: " + chessMatch.getCurrentPlayer());

        if (chessMatch.isCheck()) {
            System.out.println(">>> ATENCAO: VOCE ESTA EM XEQUE! <<<");
        }
    }

    // =========================================================================
    // MODO TESTES
    // =========================================================================
    private static void runTests() {
        System.out.println("\n--- INICIANDO TESTES DO MOTOR ---");

        testInitialBoard();
        testTurnAlternationAndWrongPiece();
        testSelfCheckPrevention();
        testFoolCheckmate();

        System.out.println("\n[SUCESSO] Todos os testes passaram sem falhas!");
    }

    private static void testInitialBoard() {
        System.out.print("1. Estado inicial do tabuleiro: ");
        ChessMatch match = new ChessMatch();
        if (match.getTurn() != 1 || match.getCurrentPlayer() != Color.WHITE || match.isCheck() || match.isCheckMate()) {
            throw new AssertionError("Falha no estado inicial.");
        }
        System.out.println("OK");
    }

    private static void testTurnAlternationAndWrongPiece() {
        System.out.print("2. Turnos e bloqueio de peca inimiga: ");
        ChessMatch match = new ChessMatch();

        boolean blocked = false;
        try {
            // Tentar mover preta no turno 1
            match.performChessMove(new Position(1, 4), new Position(3, 4));
        } catch (RuntimeException e) {
            blocked = true;
        }
        if (!blocked) {
            throw new AssertionError("Deveria bloquear peca preta no turno 1.");
        }

        // Mover peao branco e2 -> e4
        match.performChessMove(new Position(6, 4), new Position(4, 4));
        if (match.getTurn() != 2 || match.getCurrentPlayer() != Color.BLACK) {
            throw new AssertionError("Falha na alternancia para o jogador PRETO.");
        }
        System.out.println("OK");
    }

    private static void testSelfCheckPrevention() {
        System.out.print("3. Impedimento de Auto-Xeque (Rollback): ");
        ChessMatch match = new ChessMatch();

        // 1. e4 / e5
        match.performChessMove(new Position(6, 4), new Position(4, 4));
        match.performChessMove(new Position(1, 4), new Position(3, 4));

        // 2. Qh5 / d6
        match.performChessMove(new Position(7, 3), new Position(3, 7));
        match.performChessMove(new Position(1, 3), new Position(2, 3));

        // 3. Qxf7+ (Xeque)
        match.performChessMove(new Position(3, 7), new Position(1, 5));
        if (!match.isCheck()) {
            throw new AssertionError("Deveria acusar xeque.");
        }

        boolean prevented = false;
        try {
            // Lance invalido que nao tira do xeque
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
        System.out.print("4. Deteccao de Xeque-Mate (Fool's Mate): ");
        ChessMatch match = new ChessMatch();

        // 1. f3 / e5
        match.performChessMove(new Position(6, 5), new Position(5, 5));
        match.performChessMove(new Position(1, 4), new Position(3, 4));

        // 2. g4 / Qh4#
        match.performChessMove(new Position(6, 6), new Position(4, 6));
        match.performChessMove(new Position(0, 3), new Position(4, 7));

        if (!match.isCheck() || !match.isCheckMate()) {
            throw new AssertionError("Deveria acusar xeque e xeque-mate.");
        }
        System.out.println("OK");
    }
}