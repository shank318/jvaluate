Jvaluate
====

Convert Arbitrary expression into relational query.

How to use it?
--

```java
	EvaluateExpression evaluateExpression = new EvaluateExpression("(date == '2019-03-04') && ((height > 100) || (height < 200))");
	System.out.println(evaluateExpression.ToSQLQuery());
	// ( [date] = '2019-03-04T00:00:00+0530' ) AND ( ( [height] > 100 ) OR ( [height] < 200 ) ).
```