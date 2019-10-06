import src.EvaluateExpression;
import src.ExpressionToken;
import src.MyException;
import src.TokenKind;

public class Main {



    public static void main(String[] args){

        try {
            EvaluateExpression evaluateExpression = new EvaluateExpression("(date == '2016-05-01') && ((distance > 20) || (distance < 10))");

            for(ExpressionToken token: evaluateExpression.getTokens()){
                if (token.kind.name() == TokenKind.VARIABLE.name()){
                    System.out.println((String) token.value);
                }
            }

            System.out.println(evaluateExpression.ToSQLQuery());
        }catch (MyException e){
            e.printStackTrace();
        }
    }

}
