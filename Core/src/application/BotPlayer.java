package application;

import boardgame.Board;
import boardgame.Color;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotPlayer {

    private final Color color;
    private final Random random;

    public BotPlayer(Color color) {
        this.color = color;
        this.random = new Random();
    }

    public Color getColor() {
        return color;
    }

    public Position[] chooseMove(ChessMatch match) {
        List<Position[]> allValidMoves = getAllValidMoves(match);

        if (allValidMoves.isEmpty()) {
            return null;
        }

        int index = random.nextInt(allValidMoves.size());
        return allValidMoves.get(index);
    }

    private List<Position[]> getAllValidMoves(ChessMatch match) {
        List<Position[]> moves = new ArrayList<>();
        Board board = match.getBoard();

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                Piece piece = board.piece(i, j);

                if (piece != null && piece.getColor() == color) {
                    boolean[][] possibleMoves = piece.possibleMoves();
                    Position source = new Position(i, j);

                    for (int row = 0; row < 8; row++) {
                        for (int col = 0; col < 8; col++) {
                            if (possibleMoves[row][col]) {
                                Position target = new Position(row, col);

                                if (isMoveSafe(match, source, target)) {
                                    moves.add(new Position[] {
                                            new Position(i, j),
                                            new Position(row, col)
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }

    private boolean isMoveSafe(ChessMatch match, Position source, Position target) {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String positionToAlgebraic(Position pos) {
        char column = (char) ('a' + pos.getColumn());
        int row = 8 - pos.getRow();
        return "" + column + row;
    }
}
