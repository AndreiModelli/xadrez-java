package chess;

import boardgame.*;

public class Pawn extends Piece {

    private ChessMatch chessMatch;

    public Pawn(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    @Override
    public String toString() {
        return "P";
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        Position p = new Position(0, 0);

        int direction = (getColor() == Color.WHITE) ? -1 : 1;
        int initialRow = (getColor() == Color.WHITE) ? 6 : 1;

        p.setValues(position.getRow() + direction, position.getColumn());
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;

            Position p2 = new Position(position.getRow() + (2 * direction), position.getColumn());
            if (position.getRow() == initialRow && !getBoard().thereIsAPiece(p2)) {
                mat[p2.getRow()][p2.getColumn()] = true;
            }
        }

        p.setValues(position.getRow() + direction, position.getColumn() - 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;
        }

        p.setValues(position.getRow() + direction, position.getColumn() + 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;
        }

        int enPassantRow = (getColor() == Color.WHITE) ? 3 : 4;
        if (position.getRow() == enPassantRow) {
            Position left = new Position(position.getRow(), position.getColumn() - 1);
            if (getBoard().positionExists(left) && isThereOpponentPiece(left)
                    && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
                mat[position.getRow() + direction][position.getColumn() - 1] = true;
            }

            Position right = new Position(position.getRow(), position.getColumn() + 1);
            if (getBoard().positionExists(right) && isThereOpponentPiece(right)
                    && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
                mat[position.getRow() + direction][position.getColumn() + 1] = true;
            }
        }

        return mat;
    }

    private boolean isThereOpponentPiece(Position position) {
        Piece p = getBoard().piece(position);
        return p != null && p.getColor() != getColor();
    }
}
