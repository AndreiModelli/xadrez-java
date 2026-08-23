package application;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;
import chess.Color;

public class Program {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" INICIANDO BATERIA DE TESTES DO MOTOR (PESSOA 2) ");
        System.out.println("==================================================");

        testInitialBoard();
        testTurnAlternationAndWrongPiece();
        testSelfCheckPrevention();
        testScholarCheckmate();

        System.out.println("\n[SUCESSO] Todos os testes do motor passaram!");
    }

    // Teste 1: Tabuleiro Inicial
    private static void testInitialBoard() {
        System.out.print("\n[TESTE 1] Estado inicial do tabuleiro: ");
        ChessMatch match = new ChessMatch();
        assert match.getTurn() == 1 : "Turno inicial deveria ser 1";
        assert match.getCurrentPlayer() == Color.WHITE : "Primeiro jogador deveria ser BRANCO";
        assert !match.isCheck() : "Jogo não deveria iniciar em xeque";
        assert !match.isCheckMate() : "Jogo não deveria iniciar em xeque-mate";
        System.out.println("PASSED ✓");
    }

    // Teste 2: Alternância de Turnos e Validação de Peça Oposta
    private static void testTurnAlternationAndWrongPiece() {
        System.out.print("[TESTE 2] Alternância de turnos e bloqueio de peça inimiga: ");
        ChessMatch match = new ChessMatch();

        // Tentar mover peça preta na vez das brancas (deve falhar)
        boolean errorThrown = false;
        try {
            match.performChessMove(new Position(1, 4), new Position(3, 4)); // e7 -> e5
        } catch (RuntimeException e) {
            errorThrown = true;
        }
        assert errorThrown : "Deveria lançar erro ao tentar mover peça preta no turno 1";

        // Mover peão branco e2 -> e4 (6,4 -> 4,4)
        match.performChessMove(new Position(6, 4), new Position(4, 4));
        assert match.getTurn() == 2 : "Turno deveria avançar para 2";
        assert match.getCurrentPlayer() == Color.BLACK : "Jogador atual deveria ser PRETO";
        System.out.println("PASSED ✓");
    }

    // Teste 3: Impedir Jogada que Deixa o Rei em Xeque (Auto-Xeque)
    private static void testSelfCheckPrevention() {
        System.out.print("[TESTE 3] Impedimento de Auto-Xeque (Rollback): ");
        ChessMatch match = new ChessMatch();

        // 1. e4 (6,4 -> 4,4) / e5 (1,4 -> 3,4)
        match.performChessMove(new Position(6, 4), new Position(4, 4));
        match.performChessMove(new Position(1, 4), new Position(3, 4));

        // 2. Qh5 (7,3 -> 3,7) / d6 (1,3 -> 2,3)
        match.performChessMove(new Position(7, 3), new Position(3, 7));
        match.performChessMove(new Position(1, 3), new Position(2, 3));

        // 3. Qxf7+ (3,7 -> 1,5) - Dama branca coloca o Rei preto em Xeque
        match.performChessMove(new Position(3, 7), new Position(1, 5));
        assert match.isCheck() : "Rei preto deveria estar em xeque";

        // Tentar mover peão a7 -> a6 enquanto está em xeque (deve falhar e desmanchar a
        // jogada)
        boolean blocked = false;
        try {
            match.performChessMove(new Position(1, 0), new Position(2, 0));
        } catch (RuntimeException e) {
            blocked = true;
        }
        assert blocked : "Movimento irrelevante durante xeque deveria ser bloqueado";
        System.out.println("PASSED ✓");
    }

    // Teste 4: Xeque-Mate do Louco (Fool's Mate em 2 lances)
    private static void testScholarCheckmate() {
        System.out.print("[TESTE 4] Detecção de Xeque-Mate (Fool's Mate): ");
        ChessMatch match = new ChessMatch();

        // 1. f3 (6,5 -> 5,5) / e5 (1,4 -> 3,4)
        match.performChessMove(new Position(6, 5), new Position(5, 5));
        match.performChessMove(new Position(1, 4), new Position(3, 4));

        // 2. g4 (6,6 -> 4,6) / Qh4# (0,3 -> 4,7)
        match.performChessMove(new Position(6, 6), new Position(4, 6));
        match.performChessMove(new Position(0, 3), new Position(4, 7));

        assert match.isCheck() : "Deveria acusar xeque";
        assert match.isCheckMate() : "Deveria acusar xeque-mate no lance Qh4#";
        System.out.println("PASSED ✓");
    }
}