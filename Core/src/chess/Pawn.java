package chess;

import boardgame.*;

public class Pawn extends Piece {

    public Pawn(Board board, Color color) {
        super(board, color);
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

        // Avançar 1 casa
        p.setValues(position.getRow() + direction, position.getColumn());
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;

            // Avançar 2 casas no primeiro movimento
            Position p2 = new Position(position.getRow() + (2 * direction), position.getColumn());
            if (position.getRow() == initialRow && !getBoard().thereIsAPiece(p2)) {
                mat[p2.getRow()][p2.getColumn()] = true;
            }
        }

        // Captura diagonal esquerda
        p.setValues(position.getRow() + direction, position.getColumn() - 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;
        }

        // Captura diagonal direita
        p.setValues(position.getRow() + direction, position.getColumn() + 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            mat[p.getRow()][p.getColumn()] = true;
        }

        return mat;
    }

    private boolean isThereOpponentPiece(Position position) {
        Piece p = getBoard().piece(position);
        return p != null && p.getColor() != getColor();
    }
}