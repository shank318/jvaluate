package com.jvaluate;

import org.junit.Assert;
import org.junit.Test;


public class EvaluateExpressionTest {

    @Test
    public void evaluateExpressionTest() throws JValuateException {
        EvaluateExpression evaluateExpression = new EvaluateExpression("(date == '2016-05-01') && ((distance > 20) || (distance < 10))");
        Assert.assertEquals("( DATE(`date`) = '2016-05-01' ) AND ( ( [distance] > 20 ) OR ( [distance] < 10 ) )", evaluateExpression.ToSQLQuery());
    }

    @Test(expected = JValuateException.class)
    public void evaluateExpressionTestFail() throws JValuateException {
        EvaluateExpression evaluateExpression = new EvaluateExpression("(date == '2016-05-01') && (distance > 20) || (distance < 10))");
        Assert.assertEquals("( DATE(`date`) = '2016-05-01' ) AND ( ( [distance] > 20 ) OR ( [distance] < 10 ) )", evaluateExpression.ToSQLQuery());
    }
}
