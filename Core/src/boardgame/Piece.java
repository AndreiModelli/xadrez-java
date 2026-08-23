package boardgame;

public abstract class Piece {
    protected Position position;
    private Color color;
    private Board board;

    public Piece(Board board, Color color) {
        this.board = board;
        this.color = color;
        this.position = null;
    }

    public Color getColor() { return color; }
    protected Board getBoard() { return board; }

    // Retorna uma matriz de booleans com as posições válidas para a peça
    public abstract boolean[][] possibleMoves();

    public boolean possibleMove(Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];
    }

    public boolean isThereAnyPossibleMove() {
        boolean[][] mat = possibleMoves();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                if (mat[i][j]) return true;
            }
        }
        return false;
    }
}