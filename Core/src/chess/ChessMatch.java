package chess;

import boardgame.Board;
import boardgame.Color;
import boardgame.Piece;
import boardgame.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Motor principal do jogo de xadrez.
 * Gerencia turnos, validações, xeque/mate, e movimentos especiais:
 * - Roque (curto e longo)
 * - En Passant
 * - Promoção de peão
 * - Empate por insuficiência material
 */
public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private boolean draw;

    private Piece enPassantVulnerable;
    private Piece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        this.board = new Board();
        this.turn = 1;
        this.currentPlayer = Color.WHITE;
        initialSetup();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
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

    public boolean isDraw() {
        return draw;
    }

    public Board getBoard() {
        return board;
    }

    public Piece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public Piece getPromoted() {
        return promoted;
    }

    public List<Piece> getCapturedPieces() {
        return capturedPieces;
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

        Piece movedPiece = board.piece(target);

        // --- Promoção de Peão ---
        promoted = null;
        if (movedPiece instanceof Pawn) {
            int promotionRow = (movedPiece.getColor() == Color.WHITE) ? 0 : 7;
            if (target.getRow() == promotionRow) {
                promoted = movedPiece;
            }
        }

        // Atualiza status de xeque para o oponente
        check = testCheck(opponent(currentPlayer));

        // Verifica se o movimento resultou em xeque-mate
        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        // --- En Passant: marca peão vulnerável ---
        if (movedPiece instanceof Pawn
                && Math.abs(target.getRow() - source.getRow()) == 2) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }

        // --- Verifica empate por insuficiência material ---
        if (testInsufficientMaterial()) {
            draw = true;
        }

        return capturedPiece;
    }

    /**
     * Realiza a promoção do peão para a peça escolhida.
     * Deve ser chamado após performChessMove quando getPromoted() != null.
     *
     * @param type Tipo da peça: "Q" (Rainha), "R" (Torre), "B" (Bispo), "N" (Cavalo)
     */
    public void replacePromotedPiece(String type) {
        if (promoted == null) {
            throw new IllegalStateException("Nao ha peca para promover.");
        }

        if (!type.equals("Q") && !type.equals("R") && !type.equals("B") && !type.equals("N")) {
            throw new IllegalStateException("Tipo invalido para promocao. Use Q, R, B ou N.");
        }

        Position pos = findPiecePosition(promoted);
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        Piece newPiece = createPromotedPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        promoted = null;

        // Recalcula xeque após promoção
        check = testCheck(opponent(currentPlayer));
        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
    }

    private Piece createPromotedPiece(String type, Color color) {
        switch (type) {
            case "Q": return new Queen(board, color);
            case "R": return new Rook(board, color);
            case "B": return new Bishop(board, color);
            case "N": return new Knight(board, color);
            default: return new Queen(board, color);
        }
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
    // Simulação (Make / Undo) com movimentos especiais
    // -------------------------------------------------------------------------
    private Piece makeMove(Position source, Position target) {
        Piece p = board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // --- Roque curto (rei move 2 casas para direita) ---
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position rookSource = new Position(source.getRow(), source.getColumn() + 3);
            Position rookTarget = new Position(source.getRow(), source.getColumn() + 1);
            Piece rook = board.removePiece(rookSource);
            rook.increaseMoveCount();
            board.placePiece(rook, rookTarget);
        }

        // --- Roque longo (rei move 2 casas para esquerda) ---
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position rookSource = new Position(source.getRow(), source.getColumn() - 4);
            Position rookTarget = new Position(source.getRow(), source.getColumn() - 1);
            Piece rook = board.removePiece(rookSource);
            rook.increaseMoveCount();
            board.placePiece(rook, rookTarget);
        }

        // --- En Passant (peão captura na diagonal sem peça no destino) ---
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                // Captura en passant: a peça capturada está ao lado, não no destino
                Position pawnPos;
                if (p.getColor() == Color.WHITE) {
                    pawnPos = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPos = new Position(target.getRow() - 1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPos);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        Piece p = board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // --- Desfaz Roque curto ---
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position rookSource = new Position(source.getRow(), source.getColumn() + 3);
            Position rookTarget = new Position(source.getRow(), source.getColumn() + 1);
            Piece rook = board.removePiece(rookTarget);
            rook.decreaseMoveCount();
            board.placePiece(rook, rookSource);
        }

        // --- Desfaz Roque longo ---
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position rookSource = new Position(source.getRow(), source.getColumn() - 4);
            Position rookTarget = new Position(source.getRow(), source.getColumn() - 1);
            Piece rook = board.removePiece(rookTarget);
            rook.decreaseMoveCount();
            board.placePiece(rook, rookSource);
        }

        // --- Desfaz En Passant ---
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                Piece pawn = board.removePiece(target);
                Position pawnPos;
                if (p.getColor() == Color.WHITE) {
                    pawnPos = new Position(3, target.getColumn());
                } else {
                    pawnPos = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPos);
            }
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
    // Empate por Insuficiência Material
    // -------------------------------------------------------------------------
    /**
     * Verifica se as peças restantes no tabuleiro são insuficientes para dar mate.
     * Casos de empate:
     *   - Rei vs Rei
     *   - Rei + Bispo vs Rei
     *   - Rei + Cavalo vs Rei
     *   - Rei + Bispo vs Rei + Bispo (bispos na mesma cor de casa)
     */
    private boolean testInsufficientMaterial() {
        List<Piece> whitePieces = piecesOnTheBoard.stream()
                .filter(p -> p.getColor() == Color.WHITE)
                .collect(Collectors.toList());

        List<Piece> blackPieces = piecesOnTheBoard.stream()
                .filter(p -> p.getColor() == Color.BLACK)
                .collect(Collectors.toList());

        // Rei vs Rei
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }

        // Rei + peça menor vs Rei
        if (whitePieces.size() == 1 && blackPieces.size() == 2) {
            for (Piece p : blackPieces) {
                if (p instanceof Bishop || p instanceof Knight) {
                    return true;
                }
            }
        }
        if (blackPieces.size() == 1 && whitePieces.size() == 2) {
            for (Piece p : whitePieces) {
                if (p instanceof Bishop || p instanceof Knight) {
                    return true;
                }
            }
        }

        // Rei + Bispo vs Rei + Bispo (mesma cor de casa)
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            Piece whiteBishop = null;
            Piece blackBishop = null;

            for (Piece p : whitePieces) {
                if (p instanceof Bishop) whiteBishop = p;
            }
            for (Piece p : blackPieces) {
                if (p instanceof Bishop) blackBishop = p;
            }

            if (whiteBishop != null && blackBishop != null) {
                Position wPos = findPiecePosition(whiteBishop);
                Position bPos = findPiecePosition(blackBishop);
                if (wPos != null && bPos != null) {
                    // Mesma cor de casa = soma de row+col com mesma paridade
                    boolean whiteOnLight = (wPos.getRow() + wPos.getColumn()) % 2 == 0;
                    boolean blackOnLight = (bPos.getRow() + bPos.getColumn()) % 2 == 0;
                    if (whiteOnLight == blackOnLight) {
                        return true;
                    }
                }
            }
        }

        return false;
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
        placeNewPiece(0, 4, new King(board, Color.BLACK, this));
        placeNewPiece(0, 5, new Bishop(board, Color.BLACK));
        placeNewPiece(0, 6, new Knight(board, Color.BLACK));
        placeNewPiece(0, 7, new Rook(board, Color.BLACK));
        for (int j = 0; j < 8; j++) {
            placeNewPiece(1, j, new Pawn(board, Color.BLACK, this));
        }

        // Peças Brancas
        placeNewPiece(7, 0, new Rook(board, Color.WHITE));
        placeNewPiece(7, 1, new Knight(board, Color.WHITE));
        placeNewPiece(7, 2, new Bishop(board, Color.WHITE));
        placeNewPiece(7, 3, new Queen(board, Color.WHITE));
        placeNewPiece(7, 4, new King(board, Color.WHITE, this));
        placeNewPiece(7, 5, new Bishop(board, Color.WHITE));
        placeNewPiece(7, 6, new Knight(board, Color.WHITE));
        placeNewPiece(7, 7, new Rook(board, Color.WHITE));
        for (int j = 0; j < 8; j++) {
            placeNewPiece(6, j, new Pawn(board, Color.WHITE, this));
        }
    }
}
