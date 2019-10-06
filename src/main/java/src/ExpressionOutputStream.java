package src;

import java.util.ArrayList;
import java.util.List;

public class ExpressionOutputStream {
    List<String> transactions;

    public ExpressionOutputStream() {
        this.transactions = new ArrayList<>();
    }


    public void add(String transaction) {
        this.transactions.add(transaction);
    }

    public String rollback() {

        int index = this.transactions.size() - 1;
        String ret = this.transactions.get(index);

        this.transactions.remove(index);
        return ret;
    }

    public String createString(String delimiter) {

        StringBuffer retBuffer = new StringBuffer();
        String transaction;

        int penultimate = this.transactions.size() - 1;

        for (int i = 0; i < penultimate; i++) {

            transaction = this.transactions.get(i);
            retBuffer.append(transaction);
            retBuffer.append(delimiter);
        }
        retBuffer.append(this.transactions.get(penultimate));

        return retBuffer.toString();
    }
}
