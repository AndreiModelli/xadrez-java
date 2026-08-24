package application;

import boardgame.Position;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Classe responsável por ler e interpretar a notação de movimentos do jogador.
 * Aceita formatos:
 *   - Separado: "e2" (origem) e depois "e4" (destino)
 *   - Concatenado: "e2e4" (origem e destino juntos)
 *   - Comandos especiais: "desistir", "quit", "sair"
 */
public class InputReader {

    /**
     * Resultado da leitura de um movimento. Pode conter um movimento (origem/destino)
     * ou um comando especial (desistência).
     */
    public static class MoveInput {
        private Position source;
        private Position target;
        private boolean resignation;

        private MoveInput(Position source, Position target) {
            this.source = source;
            this.target = target;
            this.resignation = false;
        }

        private MoveInput() {
            this.resignation = true;
        }

        public Position getSource() {
            return source;
        }

        public Position getTarget() {
            return target;
        }

        public boolean isResignation() {
            return resignation;
        }

        public static MoveInput resign() {
            return new MoveInput();
        }

        public static MoveInput move(Position source, Position target) {
            return new MoveInput(source, target);
        }
    }

    /**
     * Lê um movimento completo do jogador (origem e destino).
     * Suporta entrada no formato "e2e4" (concatenado) ou separada.
     * Também detecta comandos de desistência.
     *
     * @param sc Scanner para leitura do console
     * @return MoveInput com as posições de origem/destino ou indicação de desistência
     */
    public static MoveInput readMove(Scanner sc) {
        System.out.print("\nOrigem (ex: e2) ou 'desistir': ");
        String input = sc.nextLine().trim().toLowerCase();

        // Verifica comandos de desistência
        if (isResignCommand(input)) {
            return MoveInput.resign();
        }

        // Verifica formato concatenado (ex: "e2e4")
        if (input.length() == 4) {
            String sourcePart = input.substring(0, 2);
            String targetPart = input.substring(2, 4);
            Position source = parsePosition(sourcePart);
            Position target = parsePosition(targetPart);
            return MoveInput.move(source, target);
        }

        // Formato separado: primeira entrada é a origem
        Position source = parsePosition(input);

        System.out.print("Destino (ex: e4): ");
        String targetInput = sc.nextLine().trim().toLowerCase();

        if (isResignCommand(targetInput)) {
            return MoveInput.resign();
        }

        Position target = parsePosition(targetInput);
        return MoveInput.move(source, target);
    }

    /**
     * Converte uma string de notação algébrica (ex: "e2") para uma Position interna.
     * A coluna 'a'-'h' mapeia para 0-7.
     * A linha '1'-'8' mapeia para row = 8 - número (padrão de tabuleiro invertido).
     *
     * @param s String de 2 caracteres no formato coluna+linha (ex: "e2")
     * @return Position correspondente na matriz interna
     * @throws InputMismatchException se o formato for inválido
     */
    public static Position parsePosition(String s) {
        if (s == null || s.length() != 2) {
            throw new InputMismatchException(
                    "Formato invalido: '" + s + "'. Use notacao algebrica (ex: e2, a7).");
        }

        char columnChar = s.charAt(0);
        char rowChar = s.charAt(1);

        if (columnChar < 'a' || columnChar > 'h') {
            throw new InputMismatchException(
                    "Coluna invalida: '" + columnChar + "'. Use letras de 'a' ate 'h'.");
        }

        if (rowChar < '1' || rowChar > '8') {
            throw new InputMismatchException(
                    "Linha invalida: '" + rowChar + "'. Use numeros de 1 ate 8.");
        }

        int column = columnChar - 'a';
        int row = 8 - Character.getNumericValue(rowChar);

        return new Position(row, column);
    }

    /**
     * Verifica se o texto digitado é um comando de desistência.
     */
    private static boolean isResignCommand(String input) {
        return input.equals("desistir") || input.equals("quit") || input.equals("sair");
    }
}
