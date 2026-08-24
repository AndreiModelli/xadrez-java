package chess;

import boardgame.*;

/**
 * Rei - move uma casa em qualquer direção.
 * Movimento especial: Roque (castling) - O-O (curto) e O-O-O (longo).
 * Condições para roque:
 *   - Rei nunca moveu (moveCount == 0)
 *   - Torre correspondente nunca moveu
 *   - Não há peças entre rei e torre
 *   - Rei não está em xeque
 *   - Rei não passa por casa atacada
 */
public class King extends Piece {

    private ChessMatch chessMatch;

    public King(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    @Override
    public String toString() {
        return "K";
    }

    private boolean canMove(Position position) {
        Piece p = getBoard().piece(position);
        return p == null || p.getColor() != getColor();
    }

    /**
     * Verifica se uma torre está apta para roque (existe, nunca moveu).
     */
    private boolean testRookCastling(Position position) {
        Piece p = getBoard().piece(position);
        return p != null && p instanceof Rook && p.getColor() == getColor() && p.getMoveCount() == 0;
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        Position p = new Position(0, 0);

        // Movimentos normais do rei (8 direções, 1 casa)
        int[][] moves = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] move : moves) {
            p.setValues(position.getRow() + move[0], position.getColumn() + move[1]);
            if (getBoard().positionExists(p) && canMove(p)) {
                mat[p.getRow()][p.getColumn()] = true;
            }
        }

        // Roque (só se o rei nunca moveu e não está em xeque)
        if (getMoveCount() == 0 && !chessMatch.isCheck()) {
            // Roque do lado do rei (curto) - torre na coluna 7
            Position rookPos = new Position(position.getRow(), position.getColumn() + 3);
            if (testRookCastling(rookPos)) {
                Position p1 = new Position(position.getRow(), position.getColumn() + 1);
                Position p2 = new Position(position.getRow(), position.getColumn() + 2);
                // Verifica se não há peças entre rei e torre
                if (!getBoard().thereIsAPiece(p1) && !getBoard().thereIsAPiece(p2)) {
                    mat[position.getRow()][position.getColumn() + 2] = true;
                }
            }

            // Roque do lado da dama (longo) - torre na coluna 0
            Position rookPos2 = new Position(position.getRow(), position.getColumn() - 4);
            if (testRookCastling(rookPos2)) {
                Position p1 = new Position(position.getRow(), position.getColumn() - 1);
                Position p2 = new Position(position.getRow(), position.getColumn() - 2);
                Position p3 = new Position(position.getRow(), position.getColumn() - 3);
                // Verifica se não há peças entre rei e torre
                if (!getBoard().thereIsAPiece(p1) && !getBoard().thereIsAPiece(p2) && !getBoard().thereIsAPiece(p3)) {
                    mat[position.getRow()][position.getColumn() - 2] = true;
                }
            }
        }

        return mat;
    }
}
