package chess;

import boardgame.*;

/**
 * Peão - avança 1 casa (ou 2 no primeiro movimento), captura na diagonal.
 * Movimento especial: En Passant.
 *   - Quando um peão adversário avança 2 casas e fica ao lado deste peão,
 *     este peão pode capturá-lo na diagonal como se tivesse avançado apenas 1 casa.
 *   - Só pode ser feito imediatamente após o avanço duplo do adversário.
 */
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

        // En Passant
        int enPassantRow = (getColor() == Color.WHITE) ? 3 : 4;
        if (position.getRow() == enPassantRow) {
            // En passant à esquerda
            Position left = new Position(position.getRow(), position.getColumn() - 1);
            if (getBoard().positionExists(left) && isThereOpponentPiece(left)
                    && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
                mat[position.getRow() + direction][position.getColumn() - 1] = true;
            }

            // En passant à direita
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
