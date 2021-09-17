import java.util.ArrayList;
import java.util.List;

// Грамматика:
// expression : plusMinus* EOF
// plusMinus : mul_div ( ( '+' | '-' ) mul_div ) *;
// multDiv : factor ( ( '*' | '/' ) factor ) *;
// factor : number | '(' expr ')';

public class MathExpressionParser {
    public static void main(String[] args) {
        MathExpressionParser mathExpression = new MathExpressionParser();

        System.out.println(mathExpression.expression(args[0]));
    }

    public int expression(String expression) {
        List<Lexeme> lexemes = lexAnalyze(expression);

        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
        return expr(lexemeBuffer);
    }


    private List<Lexeme> lexAnalyze(String expr) {
        List<Lexeme> lexemes = new ArrayList<>();
        int pos = 0;

        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            switch (c) {
                case '*':
                    lexemes.add(new Lexeme(LexemeType.OP_MUL, c));
                    pos++;
                    continue;
                case '/':
                    lexemes.add(new Lexeme(LexemeType.OP_DIV, c));
                    pos++;
                    continue;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, c));
                    pos++;
                    continue;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, c));
                    pos++;
                    continue;
                default:
                    if (c >= '0' && c <= '9') {
                        StringBuilder sb = new StringBuilder();
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= expr.length()) {
                                break;
                            }
                            c = expr.charAt(pos);
                        } while (c >= '0' && c <= '9');
                        lexemes.add(new Lexeme(LexemeType.NUMBER, sb.toString()));

                    } else {
                        if (c != ' ') {
                            throw new RuntimeException("Unexpected character: " + c + " at position: " + pos);
                        }
                        pos++;
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }

    private static int expr(LexemeBuffer lexemeBuffer) {
        Lexeme lexeme = lexemeBuffer.next();
        if (lexeme.type == LexemeType.EOF) {
            return 0;
        } else {
            lexemeBuffer.back();
            return plusMinus(lexemeBuffer);
        }
    }

    private static int plusMinus(LexemeBuffer lexemeBuffer) {
        int value = multDiv(lexemeBuffer);
        while (true) {
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.type) {
                case OP_PLUS:
                    value += multDiv(lexemeBuffer);
                    continue;
                case OP_MINUS:
                    value -= multDiv(lexemeBuffer);
                    continue;
                case EOF:
                    lexemeBuffer.back();
                    return value;
                default:
                    throw new RuntimeException("Unexpected token: " + lexeme.value
                            + " at position: " + lexemeBuffer.getPos());
            }
        }
    }

    private static int multDiv(LexemeBuffer lexemeBuffer) {
        int value = factor(lexemeBuffer);
        while (true) {
            Lexeme lexeme = lexemeBuffer.next();
            switch (lexeme.type) {
                case OP_MUL:
                    value *= factor(lexemeBuffer);
                    continue;
                case OP_DIV:
                    value /= factor(lexemeBuffer);
                    continue;
                case EOF, OP_PLUS, OP_MINUS:
                    lexemeBuffer.back();
                    return value;
                default:
                    throw new RuntimeException("Unexpected lexeme: " + lexeme.value
                            + " at position: " + lexemeBuffer.getPos());
            }
        }
    }

    private static int factor(LexemeBuffer lexemeBuffer) {
        Lexeme lexeme = lexemeBuffer.next();
        switch (lexeme.type) {
            case OP_MINUS:
                return -multDiv(lexemeBuffer);
            case NUMBER:
                return Integer.parseInt(lexeme.value);
            default:
                throw new RuntimeException("Unexpected lexeme: " + lexeme.value
                        + " at position: " + lexemeBuffer.getPos());
        }
    }

//    Функции и классы необходимые для вычисления выражения

    private enum LexemeType {
        OP_PLUS, OP_MINUS, OP_MUL, OP_DIV,
        NUMBER,
        EOF,
    }

    private class Lexeme {
        private LexemeType type;
        private String value;

        public Lexeme(LexemeType type, String value) {
            this.type = type;
            this.value = value;
        }

        public Lexeme(LexemeType type, Character value) {
            this.type = type;
            this.value = value.toString();
        }
    }

    private static class LexemeBuffer {
        private int pos;

        private List<Lexeme> lexemes;

        public LexemeBuffer(List<Lexeme> lexemes) {
            this.lexemes = lexemes;
        }

        public Lexeme next() {
            return lexemes.get(pos++);
        }

        public void back() {
            pos--;
        }

        public int getPos() {
            return pos;
        }
    }
}
