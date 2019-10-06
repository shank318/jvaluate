import com.jvaluate.EvaluateExpression;
import com.jvaluate.ExpressionToken;
import com.jvaluate.JValuateException;
import com.jvaluate.TokenKind;

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
        }catch (JValuateException e){
            e.printStackTrace();
        }
    }

}
