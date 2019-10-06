package com.jvaluate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EvaluateExpression {
    String queryDateFormat;
    boolean checkTypes;
    String inputExpression;
    List<ExpressionToken> tokens;
    List<String> dateColumns = Arrays.asList("date","created_at","updated_at");

    public EvaluateExpression(String expression) throws JValuateException {
        this.queryDateFormat = "yyyy-MM-dd";
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


    public String ToSQLQuery() throws JValuateException {

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

//    private  boolean isSimpleDate(String d) {
//        if (d != null) {
//            for (String parse : formats) {
//                SimpleDateFormat sdf = new SimpleDateFormat(parse);
//                try {
//                    sdf.parse(d);
//                    System.out.println("Printing the value of " + parse);
//                } catch (ParseException e) {
//
//                }
//            }
//        }
//    }

    private String findNextSQLString(TokenStream stream, ExpressionOutputStream transactions) throws JValuateException {

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
                String value = (String) token.value;
                if(dateColumns.contains(value)){
                    ret = "DATE(`"+value+"`)";
                }else{
                    ret = "["+value+"]";
                }
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
                throw new JValuateException("Unrecognized query token " + token.value + " of kind " + token.kind, "JValuateException");
        }

        return ret;

    }
}
