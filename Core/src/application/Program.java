package application;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.*;

public class Program {
    public static void main(String[] args) {

        // 1. Teste do Rei (King)
        Board b1 = new Board();
        King king = new King(b1, Color.WHITE);
        b1.placePiece(king, new Position(3, 3));
        printMoves("Rei (King) na posição (3,3)", king.possibleMoves());

        // 2. Teste da Dama (Queen)
        Board b2 = new Board();
        Queen queen = new Queen(b2, Color.WHITE);
        b2.placePiece(queen, new Position(3, 3));
        printMoves("Dama (Queen) na posição (3,3)", queen.possibleMoves());

        // 3. Teste do Bispo (Bishop)
        Board b3 = new Board();
        Bishop bishop = new Bishop(b3, Color.WHITE);
        b3.placePiece(bishop, new Position(3, 3));
        printMoves("Bispo (Bishop) na posição (3,3)", bishop.possibleMoves());

        // 4. Teste do Cavalo (Knight)
        Board b4 = new Board();
        Knight knight = new Knight(b4, Color.WHITE);
        b4.placePiece(knight, new Position(3, 3));
        printMoves("Cavalo (Knight) na posição (3,3)", knight.possibleMoves());

        // 5. Teste do Peão Branco (Pawn) na posição inicial
        Board b5 = new Board();
        Pawn pawn = new Pawn(b5, Color.WHITE);
        b5.placePiece(pawn, new Position(6, 3));
        printMoves("Peão BR (Pawn) na posição inicial (6,3)", pawn.possibleMoves());
    }

    private static void printMoves(String title, boolean[][] moves) {
        System.out.println("\n=== " + title + " ===");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(moves[i][j] ? " X " : " . ");
            }
            System.out.println();
        }
    }
}