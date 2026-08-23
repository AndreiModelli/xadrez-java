package application;

import boardgame.Board;
import boardgame.Piece;
import chess.ChessMatch;
import chess.Color;

import java.util.List;

/**
 * Classe responsável pela renderização da interface em modo texto (CLI).
 * Exibe o tabuleiro 8x8 em formato ASCII com coordenadas algébricas.
 */
public class UI {

    // Códigos ANSI para cores no terminal
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_BG_GRAY = "\u001B[47m";
    private static final String ANSI_BLACK_TEXT = "\u001B[30m";

    /**
     * Limpa a tela do terminal (funciona na maioria dos terminais).
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Imprime o estado completo da partida: tabuleiro, peças capturadas, turno e status.
     */
    public static void printMatch(ChessMatch match) {
        printBoard(match.getBoard());
        System.out.println();
        printCapturedPieces(match);
        System.out.println();
        System.out.println("Turno: " + match.getTurn());

        if (!match.isCheckMate()) {
            System.out.println("Jogador atual: " + formatPlayer(match.getCurrentPlayer()));
            if (match.isCheck()) {
                System.out.println(ANSI_RED + ">>> XEQUE! <<<" + ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_GREEN + ANSI_BOLD + "XEQUE-MATE!" + ANSI_RESET);
            System.out.println("Vencedor: " + formatPlayer(match.getCurrentPlayer()));
        }
    }

    /**
     * Imprime o tabuleiro 8x8 com coordenadas (1-8 nas linhas, a-h nas colunas).
     * Peças brancas em MAIÚSCULO, peças pretas em minúsculo.
     */
    public static void printBoard(Board board) {
        System.out.println();
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int i = 0; i < board.getRows(); i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < board.getColumns(); j++) {
                System.out.print("| ");
                printPiece(board.piece(i, j));
                System.out.print(" ");
            }
            System.out.println("| " + (8 - i));
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }

        System.out.println("    a   b   c   d   e   f   g   h");
    }

    /**
     * Imprime o tabuleiro destacando os movimentos possíveis de uma peça.
     */
    public static void printBoard(Board board, boolean[][] possibleMoves) {
        System.out.println();
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int i = 0; i < board.getRows(); i++) {
            System.out.print((8 - i) + " ");
            for (int j = 0; j < board.getColumns(); j++) {
                if (possibleMoves[i][j]) {
                    System.out.print("|" + ANSI_BG_GRAY + ANSI_BLACK_TEXT + " ");
                    printPieceHighlighted(board.piece(i, j));
                    System.out.print(" " + ANSI_RESET);
                } else {
                    System.out.print("| ");
                    printPiece(board.piece(i, j));
                    System.out.print(" ");
                }
            }
            System.out.println("| " + (8 - i));
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }

        System.out.println("    a   b   c   d   e   f   g   h");
    }

    /**
     * Imprime uma peça individual com cor apropriada.
     */
    private static void printPiece(Piece piece) {
        if (piece == null) {
            System.out.print(".");
        } else if (piece.getColor() == Color.WHITE) {
            System.out.print(ANSI_CYAN + piece.toString().toUpperCase() + ANSI_RESET);
        } else {
            System.out.print(ANSI_YELLOW + piece.toString().toLowerCase() + ANSI_RESET);
        }
    }

    /**
     * Imprime peça dentro de destaque (fundo cinza para movimentos possíveis).
     */
    private static void printPieceHighlighted(Piece piece) {
        if (piece == null) {
            System.out.print("*");
        } else if (piece.getColor() == Color.WHITE) {
            System.out.print(piece.toString().toUpperCase());
        } else {
            System.out.print(piece.toString().toLowerCase());
        }
    }

    /**
     * Imprime as peças capturadas separadas por cor.
     */
    public static void printCapturedPieces(ChessMatch match) {
        List<Piece> captured = match.getCapturedPieces();
        if (captured == null || captured.isEmpty()) {
            return;
        }

        StringBuilder white = new StringBuilder();
        StringBuilder black = new StringBuilder();

        for (Piece p : captured) {
            if (p.getColor() == Color.WHITE) {
                white.append(p.toString()).append(" ");
            } else {
                black.append(p.toString().toLowerCase()).append(" ");
            }
        }

        if (white.length() > 0) {
            System.out.println("Capturadas (brancas): " + ANSI_CYAN + white.toString().trim() + ANSI_RESET);
        }
        if (black.length() > 0) {
            System.out.println("Capturadas (pretas):  " + ANSI_YELLOW + black.toString().trim() + ANSI_RESET);
        }
    }

    /**
     * Formata o nome do jogador com cor.
     */
    private static String formatPlayer(Color color) {
        if (color == Color.WHITE) {
            return ANSI_CYAN + "BRANCAS" + ANSI_RESET;
        } else {
            return ANSI_YELLOW + "PRETAS" + ANSI_RESET;
        }
    }

    /**
     * Exibe o menu principal do jogo.
     */
    public static void printMenu() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════╗");
        System.out.println("║         XADREZ EM JAVA           ║");
        System.out.println("╠═══════════════════════════════════╣");
        System.out.println("║  1 - Jogador vs Jogador (PvP)    ║");
        System.out.println("║  2 - Jogador vs Computador (Bot) ║");
        System.out.println("║  3 - Executar Testes             ║");
        System.out.println("║  0 - Sair                        ║");
        System.out.println("╚═══════════════════════════════════╝");
        System.out.print("Escolha: ");
    }

    /**
     * Exibe mensagem de boas-vindas com instruções.
     */
    public static void printInstructions() {
        System.out.println();
        System.out.println("=== INSTRUCOES ===");
        System.out.println("- Para mover: digite origem e destino (ex: e2 e4)");
        System.out.println("- Para desistir: digite 'desistir' ou 'quit'");
        System.out.println("- Notacao: coluna (a-h) + linha (1-8)");
        System.out.println("- Pecas Brancas: MAIUSCULAS (K=Rei, Q=Rainha, R=Torre, B=Bispo, N=Cavalo, P=Peao)");
        System.out.println("- Pecas Pretas:  minusculas (k=rei, q=rainha, r=torre, b=bispo, n=cavalo, p=peao)");
        System.out.println("==================");
    }
}
