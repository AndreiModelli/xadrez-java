package application;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.ChessMatch;
import chess.King;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ChessGUI extends JFrame {

    private static final int SQUARE_SIZE = 75;
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color HIGHLIGHT_COLOR = new Color(106, 168, 79, 180);
    private static final Color SELECTED_COLOR = new Color(246, 246, 105, 200);
    private static final Color CHECK_COLOR = new Color(235, 64, 52, 180);

    private static final String[][] PIECE_SYMBOLS = {
            // Brancas: K, Q, R, B, N, P
            { "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659" },
            // Pretas: K, Q, R, B, N, P
            { "\u265A", "\u265B", "\u265C", "\u265D", "\u265E", "\u265F" }
    };

    private ChessMatch match;
    private BotPlayer bot;
    private boardgame.Color playerColor;
    private boolean isBotMode;

    private Position selectedPosition;
    private boolean[][] possibleMoves;

    private JPanel boardPanel;
    private JPanel[][] squares;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JLabel checkLabel;
    private JLabel capturedWhiteLabel;
    private JLabel capturedBlackLabel;
    private JButton resignButton;
    private JButton newGameButton;

    public ChessGUI(boolean botMode, boardgame.Color chosenColor) {
        this.isBotMode = botMode;
        this.match = new ChessMatch();
        this.selectedPosition = null;
        this.possibleMoves = null;

        if (botMode) {
            this.playerColor = chosenColor;
            boardgame.Color botColor = (chosenColor == boardgame.Color.WHITE) ? boardgame.Color.BLACK
                    : boardgame.Color.WHITE;
            this.bot = new BotPlayer(botColor);
        }

        initializeGUI();
        updateBoard();

        if (botMode && chosenColor == boardgame.Color.BLACK) {
            SwingUtilities.invokeLater(this::botPlay);
        }
    }

    private void initializeGUI() {
        setTitle("Xadrez em Java - Modo Grafico");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        boardPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 40, 20), 3));

        squares = new JPanel[8][8];
        initializeBoard();

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createColumnLabels(), BorderLayout.NORTH);
        centerPanel.add(createRowLabels(), BorderLayout.WEST);
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(createColumnLabels(), BorderLayout.SOUTH);
        centerPanel.add(createRowLabelsRight(), BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(false);
    }

    private void initializeBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));

                if ((i + j) % 2 == 0) {
                    square.setBackground(LIGHT_SQUARE);
                } else {
                    square.setBackground(DARK_SQUARE);
                }

                final int row = i;
                final int col = j;
                square.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(row, col);
                    }
                });

                square.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                squares[i][j] = square;
                boardPanel.add(square);
            }
        }
    }

    private JPanel createColumnLabels() {
        JPanel panel = new JPanel(new GridLayout(1, 8));
        panel.setPreferredSize(new Dimension(SQUARE_SIZE * 8, 20));
        String[] cols = { "a", "b", "c", "d", "e", "f", "g", "h" };
        for (String col : cols) {
            JLabel label = new JLabel(col, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            panel.add(label);
        }
        return panel;
    }

    private JPanel createRowLabels() {
        JPanel panel = new JPanel(new GridLayout(8, 1));
        panel.setPreferredSize(new Dimension(20, SQUARE_SIZE * 8));
        for (int i = 8; i >= 1; i--) {
            JLabel label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            panel.add(label);
        }
        return panel;
    }

    private JPanel createRowLabelsRight() {
        JPanel panel = new JPanel(new GridLayout(8, 1));
        panel.setPreferredSize(new Dimension(20, SQUARE_SIZE * 8));
        for (int i = 8; i >= 1; i--) {
            JLabel label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            panel.add(label);
        }
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(220, SQUARE_SIZE * 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 240));

        JLabel title = new JLabel("XADREZ EM JAVA");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        turnLabel = new JLabel("Turno: 1");
        turnLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(turnLabel);
        panel.add(Box.createVerticalStrut(5));

        statusLabel = new JLabel("Vez: BRANCAS");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(5));

        checkLabel = new JLabel(" ");
        checkLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        checkLabel.setForeground(java.awt.Color.RED);
        checkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(checkLabel);
        panel.add(Box.createVerticalStrut(20));

        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));

        JLabel capturedTitle = new JLabel("Pecas Capturadas:");
        capturedTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        capturedTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(capturedTitle);
        panel.add(Box.createVerticalStrut(5));

        capturedWhiteLabel = new JLabel("Brancas: ");
        capturedWhiteLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        capturedWhiteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(capturedWhiteLabel);

        capturedBlackLabel = new JLabel("Pretas: ");
        capturedBlackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        capturedBlackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(capturedBlackLabel);
        panel.add(Box.createVerticalStrut(20));

        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));

        String modeText = isBotMode ? "Modo: vs Computador" : "Modo: PvP";
        JLabel modeLabel = new JLabel(modeText);
        modeLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(modeLabel);
        panel.add(Box.createVerticalStrut(20));

        resignButton = new JButton("Desistir");
        resignButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resignButton.setMaximumSize(new Dimension(150, 35));
        resignButton.addActionListener(e -> handleResignation());
        panel.add(resignButton);
        panel.add(Box.createVerticalStrut(10));

        newGameButton = new JButton("Novo Jogo");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.setMaximumSize(new Dimension(150, 35));
        newGameButton.addActionListener(e -> handleNewGame());
        panel.add(newGameButton);

        panel.add(Box.createVerticalGlue());

        JLabel helpLabel = new JLabel(
                "<html><center><small>Clique para selecionar<br>Clique para mover</small></center></html>");
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(helpLabel);

        return panel;
    }

    private void handleSquareClick(int row, int col) {
        if (match.isCheckMate() || match.isDraw()) {
            return;
        }

        if (isBotMode && match.getCurrentPlayer() != playerColor) {
            return;
        }

        Position clickedPos = new Position(row, col);

        if (selectedPosition == null) {
            selectPiece(clickedPos);
        } else {
            movePiece(clickedPos);
        }
    }

    private void selectPiece(Position pos) {
        Piece piece = match.getBoard().piece(pos);

        if (piece == null || piece.getColor() != match.getCurrentPlayer()) {
            return;
        }

        boolean[][] moves = piece.possibleMoves();
        boolean hasMove = false;
        for (int i = 0; i < 8 && !hasMove; i++) {
            for (int j = 0; j < 8 && !hasMove; j++) {
                if (moves[i][j])
                    hasMove = true;
            }
        }

        if (!hasMove) {
            return;
        }

        selectedPosition = pos;
        possibleMoves = moves;
        updateBoard();
    }

    private void movePiece(Position target) {
        if (target.getRow() == selectedPosition.getRow()
                && target.getColumn() == selectedPosition.getColumn()) {
            clearSelection();
            updateBoard();
            return;
        }

        Piece targetPiece = match.getBoard().piece(target);
        if (targetPiece != null && targetPiece.getColor() == match.getCurrentPlayer()) {
            selectPiece(target);
            return;
        }

        if (possibleMoves != null && possibleMoves[target.getRow()][target.getColumn()]) {
            try {
                match.performChessMove(selectedPosition, target);
                clearSelection();

                if (match.getPromoted() != null) {
                    handlePromotion();
                }

                updateBoard();
                updateInfo();

                if (match.isCheckMate() || match.isDraw()) {
                    handleGameOver();
                    return;
                }

                if (isBotMode && match.getCurrentPlayer() != playerColor) {
                    Timer timer = new Timer(600, e -> {
                        botPlay();
                        ((Timer) e.getSource()).stop();
                    });
                    timer.setRepeats(false);
                    timer.start();
                }

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Movimento Invalido", JOptionPane.WARNING_MESSAGE);
                clearSelection();
                updateBoard();
            }
        } else {
            clearSelection();
            updateBoard();
        }
    }

    private void clearSelection() {
        selectedPosition = null;
        possibleMoves = null;
    }

    private void botPlay() {
        if (match.isCheckMate() || match.isDraw())
            return;

        Position[] botMove = bot.chooseMove(match);
        if (botMove == null)
            return;

        boolean moveDone = false;
        int attempts = 0;

        while (!moveDone && attempts < 100) {
            try {
                match.performChessMove(botMove[0], botMove[1]);
                moveDone = true;

                if (match.getPromoted() != null) {
                    match.replacePromotedPiece("Q");
                }

            } catch (RuntimeException e) {
                botMove = bot.chooseMove(match);
                if (botMove == null)
                    break;
                attempts++;
            }
        }

        updateBoard();
        updateInfo();

        if (match.isCheckMate() || match.isDraw()) {
            handleGameOver();
        }
    }

    private void handlePromotion() {
        String[] options = { "Rainha (Q)", "Torre (R)", "Bispo (B)", "Cavalo (N)" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Escolha a peca para promocao:",
                "Promocao de Peao",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        String[] types = { "Q", "R", "B", "N" };
        String type = (choice >= 0 && choice < 4) ? types[choice] : "Q";
        match.replacePromotedPiece(type);
    }

    private void handleGameOver() {
        updateBoard();
        updateInfo();
        resignButton.setEnabled(false);

        String message;
        if (match.isCheckMate()) {
            message = "XEQUE-MATE!\nVencedor: " + formatColor(match.getCurrentPlayer());
        } else {
            message = "EMPATE!\nInsuficiencia material.";
        }

        JOptionPane.showMessageDialog(this, message, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleResignation() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja desistir?",
                "Desistencia",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boardgame.Color winner = (match.getCurrentPlayer() == boardgame.Color.WHITE)
                    ? boardgame.Color.BLACK
                    : boardgame.Color.WHITE;
            resignButton.setEnabled(false);
            statusLabel.setText("Desistencia!");
            checkLabel.setText("Vencedor: " + formatColor(winner));
            JOptionPane.showMessageDialog(this,
                    formatColor(match.getCurrentPlayer()) + " desistiu.\nVencedor: " + formatColor(winner),
                    "Desistencia", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleNewGame() {
        dispose();
        showModeDialog();
    }

    private void updateBoard() {
        Board board = match.getBoard();

        Position kingInCheck = null;
        if (match.isCheck()) {
            kingInCheck = findKingPosition(match.getCurrentPlayer());
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JPanel square = squares[i][j];
                square.removeAll();

                java.awt.Color baseColor = ((i + j) % 2 == 0) ? LIGHT_SQUARE : DARK_SQUARE;

                if (selectedPosition != null
                        && i == selectedPosition.getRow()
                        && j == selectedPosition.getColumn()) {
                    square.setBackground(SELECTED_COLOR);
                } else if (possibleMoves != null && possibleMoves[i][j]) {
                    square.setBackground(HIGHLIGHT_COLOR);
                } else if (kingInCheck != null
                        && i == kingInCheck.getRow()
                        && j == kingInCheck.getColumn()) {
                    square.setBackground(CHECK_COLOR);
                } else {
                    square.setBackground(baseColor);
                }

                Piece piece = board.piece(i, j);
                if (piece != null) {
                    JLabel pieceLabel = new JLabel(getPieceSymbol(piece), SwingConstants.CENTER);
                    pieceLabel.setFont(new Font("Serif", Font.PLAIN, 48));
                    square.add(pieceLabel, BorderLayout.CENTER);
                } else if (possibleMoves != null && possibleMoves[i][j]) {
                    JLabel dot = new JLabel("\u2022", SwingConstants.CENTER);
                    dot.setFont(new Font("SansSerif", Font.BOLD, 28));
                    dot.setForeground(new java.awt.Color(0, 0, 0, 100));
                    square.add(dot, BorderLayout.CENTER);
                }

                square.revalidate();
                square.repaint();
            }
        }

        updateInfo();
    }

    private void updateInfo() {
        turnLabel.setText("Turno: " + match.getTurn());

        if (match.isCheckMate()) {
            statusLabel.setText("XEQUE-MATE!");
            checkLabel.setText("Vencedor: " + formatColor(match.getCurrentPlayer()));
        } else if (match.isDraw()) {
            statusLabel.setText("EMPATE!");
            checkLabel.setText("Insuficiencia material");
        } else {
            statusLabel.setText("Vez: " + formatColor(match.getCurrentPlayer()));
            checkLabel.setText(match.isCheck() ? "XEQUE!" : " ");
        }

        List<Piece> captured = match.getCapturedPieces();
        StringBuilder white = new StringBuilder();
        StringBuilder black = new StringBuilder();

        for (Piece p : captured) {
            if (p.getColor() == boardgame.Color.WHITE) {
                white.append(getPieceSymbol(p));
            } else {
                black.append(getPieceSymbol(p));
            }
        }

        capturedWhiteLabel.setText("Brancas: " + white.toString());
        capturedBlackLabel.setText("Pretas: " + black.toString());
    }

    private String getPieceSymbol(Piece piece) {
        int colorIndex = (piece.getColor() == boardgame.Color.WHITE) ? 0 : 1;
        String pieceLetter = piece.toString();

        switch (pieceLetter) {
            case "K":
                return PIECE_SYMBOLS[colorIndex][0];
            case "Q":
                return PIECE_SYMBOLS[colorIndex][1];
            case "R":
                return PIECE_SYMBOLS[colorIndex][2];
            case "B":
                return PIECE_SYMBOLS[colorIndex][3];
            case "N":
                return PIECE_SYMBOLS[colorIndex][4];
            case "P":
                return PIECE_SYMBOLS[colorIndex][5];
            default:
                return "?";
        }
    }

    private Position findKingPosition(boardgame.Color color) {
        Board board = match.getBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board.piece(i, j);
                if (p != null && p instanceof King && p.getColor() == color) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    private String formatColor(boardgame.Color color) {
        return (color == boardgame.Color.WHITE) ? "BRANCAS" : "PRETAS";
    }

    public static void showModeDialog() {
        JFrame launcher = new JFrame("Xadrez em Java - Escolha o Modo");
        launcher.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("XADREZ EM JAVA");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));

        JLabel subtitle = new JLabel("Escolha o modo de jogo:");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(15));

        JButton pvpButton = new JButton("Jogador vs Jogador");
        pvpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pvpButton.setMaximumSize(new Dimension(260, 40));
        pvpButton.addActionListener(e -> {
            launcher.dispose();
            new ChessGUI(false, boardgame.Color.WHITE);
        });
        content.add(pvpButton);
        content.add(Box.createVerticalStrut(10));

        JButton whiteButton = new JButton("vs Computador (jogar de Brancas)");
        whiteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        whiteButton.setMaximumSize(new Dimension(260, 40));
        whiteButton.addActionListener(e -> {
            launcher.dispose();
            new ChessGUI(true, boardgame.Color.WHITE);
        });
        content.add(whiteButton);
        content.add(Box.createVerticalStrut(10));

        JButton blackButton = new JButton("vs Computador (jogar de Pretas)");
        blackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        blackButton.setMaximumSize(new Dimension(260, 40));
        blackButton.addActionListener(e -> {
            launcher.dispose();
            new ChessGUI(true, boardgame.Color.BLACK);
        });
        content.add(blackButton);

        launcher.setContentPane(content);
        launcher.pack();
        launcher.setLocationRelativeTo(null);
        launcher.setAlwaysOnTop(true);
        launcher.setVisible(true);
        launcher.toFront();
        launcher.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(ChessGUI::showModeDialog);
    }
}
