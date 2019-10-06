package src;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EvaluateExpression {
    String queryDateFormat;
    boolean checkTypes;
    String inputExpression;
    List<ExpressionToken> tokens;

    public EvaluateExpression(String expression) throws MyException {
        this.queryDateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
        this.inputExpression = expression;

        final List<ExpressionToken> expressionTokens = Parsing.parseTokens(expression);

        Parsing.checkBalance(expressionTokens);
        LexerState.checkExpressionSyntax(expressionTokens);
        this.checkTypes = true;
        this.tokens = expressionTokens;
    }

    public List<ExpressionToken> getTokens(){
        return tokens;
    }


    public String ToSQLQuery() throws MyException {

        TokenStream tokenStream = new TokenStream(this.tokens);
        ExpressionOutputStream transactions = new ExpressionOutputStream();
        String transaction;

        for (; tokenStream.hasNext(); ) {

            transaction = this.findNextSQLString(tokenStream, transactions);
            transactions.add(transaction);
        }

        return transactions.createString(" ");
    }

    private  String trimTrailingZeros(String number) {
        if(!number.contains(".")) {
            return number;
        }

        return number.replaceAll("\\.?0*$", "");
    }

    private String findNextSQLString(TokenStream stream, ExpressionOutputStream transactions) throws MyException {

        ExpressionToken token;
        String ret = "";

        token = stream.next();

        switch (token.kind) {

            case STRING:
                ret = "'"+(String) token.value+"'";
                break;
            case TIME:
                DateFormat dateFormat = new SimpleDateFormat(this.queryDateFormat);
                ret = "'"+dateFormat.format((Date) token.value)+"'";
                break;
            case LOGICALOP:
                switch (OperatorSymbol.logicalSymbols().get((String) token.value)) {

                    case AND:
                        ret = "AND";
                        break;
                    case OR:
                        ret = "OR";
                        break;
                }
                break;

            case VARIABLE:
                ret = "["+(String) token.value+"]";
                break;

            case NUMERIC:
                ret = trimTrailingZeros(Float.toString((Float)token.value));
                break;

            case COMPARATOR:
                switch (OperatorSymbol.comparatorSymbols().get((String) token.value)) {

                    case EQ:
                        ret = "=";
                        break;
                    case NEQ:
                        ret = "<>";
                        break;
                    default:
                        ret = (String) token.value;
                }
                break;
            case CLAUSE:
                ret = "(";
                break;
            case CLAUSE_CLOSE:
                ret = ")";
                break;
            case SEPARATOR:
                ret = ",";
                break;

            default:
                throw new MyException("Unrecognized query token " + token.value + " of kind " + token.kind, "JValuateException");
        }

        return ret;

    }
}
