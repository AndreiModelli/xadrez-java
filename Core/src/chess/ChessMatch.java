package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        this.board = new Board();
        this.turn = 1;
        this.currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckMate() {
        return checkMate;
    }

    public Board getBoard() {
        return board;
    }

    // -------------------------------------------------------------------------
    // Execução da Jogada
    // -------------------------------------------------------------------------
    public Piece performChessMove(Position source, Position target) {
        validateSourcePosition(source);
        validateTargetPosition(source, target);

        Piece capturedPiece = makeMove(source, target);

        // Se o jogador se colocou em xeque, desfaz a jogada
        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new RuntimeException("Voce nao pode se colocar em xeque.");
        }

        // Atualiza status de xeque para o oponente
        check = testCheck(opponent(currentPlayer));

        // Verifica se o movimento resultou em xeque-mate
        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        return capturedPiece;
    }

    // -------------------------------------------------------------------------
    // Validações
    // -------------------------------------------------------------------------
    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new RuntimeException("Nao existe peca na posicao de origem.");
        }
        if (currentPlayer != board.piece(position).getColor()) {
            throw new RuntimeException("A peca escolhida nao e sua.");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new RuntimeException("Nao ha movimentos possiveis para a peca escolhida.");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new RuntimeException("A peca escolhida nao pode se mover para a posicao de destino.");
        }
    }

    // -------------------------------------------------------------------------
    // Simulação (Make / Undo)
    // -------------------------------------------------------------------------
    private Piece makeMove(Position source, Position target) {
        Piece p = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        Piece p = board.removePiece(target);
        board.placePiece(p, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Position findPiecePosition(Piece piece) {
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                if (board.piece(i, j) == piece) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Xeque e Xeque-Mate
    // -------------------------------------------------------------------------
    private Piece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream()
                .filter(x -> x.getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list) {
            if (p instanceof King) {
                return p;
            }
        }
        throw new IllegalStateException("Nao existe o rei da cor " + color + " no tabuleiro.");
    }

    public boolean testCheck(Color color) {
        Position kingPosition = findPiecePosition(king(color));
        if (kingPosition == null)
            return false;

        List<Piece> opponentPieces = piecesOnTheBoard.stream()
                .filter(x -> x.getColor() == opponent(color))
                .collect(Collectors.toList());

        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    public boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }

        List<Piece> list = piecesOnTheBoard.stream()
                .filter(x -> x.getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            Position source = findPiecePosition(p);

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position target = new Position(i, j);

                        Piece capturedPiece = makeMove(source, target);
                        boolean stillInCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);

                        if (!stillInCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Setup Inicial
    // -------------------------------------------------------------------------
    private void placeNewPiece(int row, int column, Piece piece) {
        board.placePiece(piece, new Position(row, column));
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        // Peças Pretas
        placeNewPiece(0, 0, new Rook(board, Color.BLACK));
        placeNewPiece(0, 1, new Knight(board, Color.BLACK));
        placeNewPiece(0, 2, new Bishop(board, Color.BLACK));
        placeNewPiece(0, 3, new Queen(board, Color.BLACK));
        placeNewPiece(0, 4, new King(board, Color.BLACK));
        placeNewPiece(0, 5, new Bishop(board, Color.BLACK));
        placeNewPiece(0, 6, new Knight(board, Color.BLACK));
        placeNewPiece(0, 7, new Rook(board, Color.BLACK));
        for (int j = 0; j < 8; j++) {
            placeNewPiece(1, j, new Pawn(board, Color.BLACK));
        }

        // Peças Brancas
        placeNewPiece(7, 0, new Rook(board, Color.WHITE));
        placeNewPiece(7, 1, new Knight(board, Color.WHITE));
        placeNewPiece(7, 2, new Bishop(board, Color.WHITE));
        placeNewPiece(7, 3, new Queen(board, Color.WHITE));
        placeNewPiece(7, 4, new King(board, Color.WHITE));
        placeNewPiece(7, 5, new Bishop(board, Color.WHITE));
        placeNewPiece(7, 6, new Knight(board, Color.WHITE));
        placeNewPiece(7, 7, new Rook(board, Color.WHITE));
        for (int j = 0; j < 8; j++) {
            placeNewPiece(6, j, new Pawn(board, Color.WHITE));
        }
    }
}