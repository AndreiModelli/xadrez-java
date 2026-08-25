package application;

import boardgame.Position;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputReader {

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

    public static MoveInput readMove(Scanner sc) {
        System.out.print("\nOrigem (ex: e2) ou 'desistir': ");
        String input = sc.nextLine().trim().toLowerCase();

        if (isResignCommand(input)) {
            return MoveInput.resign();
        }

        if (input.length() == 4) {
            String sourcePart = input.substring(0, 2);
            String targetPart = input.substring(2, 4);
            Position source = parsePosition(sourcePart);
            Position target = parsePosition(targetPart);
            return MoveInput.move(source, target);
        }

        Position source = parsePosition(input);

        System.out.print("Destino (ex: e4): ");
        String targetInput = sc.nextLine().trim().toLowerCase();

        if (isResignCommand(targetInput)) {
            return MoveInput.resign();
        }

        Position target = parsePosition(targetInput);
        return MoveInput.move(source, target);
    }

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

    private static boolean isResignCommand(String input) {
        return input.equals("desistir") || input.equals("quit") || input.equals("sair");
    }
}
