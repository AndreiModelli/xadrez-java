package boardgame;

public abstract class Piece {
    protected Position position;
    private Color color;
    private Board board;
    private int moveCount;

    public Piece(Board board, Color color) {
        this.board = board;
        this.color = color;
        this.position = null;
        this.moveCount = 0;
    }

    public Color getColor() {
        return color;
    }

    protected Board getBoard() {
        return board;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void increaseMoveCount() {
        moveCount++;
    }

    public void decreaseMoveCount() {
        moveCount--;
    }

    public abstract boolean[][] possibleMoves();

    public boolean possibleMove(Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];
    }

    public boolean isThereAnyPossibleMove() {
        boolean[][] mat = possibleMoves();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                if (mat[i][j])
                    return true;
            }
        }
        return false;
    }
}
