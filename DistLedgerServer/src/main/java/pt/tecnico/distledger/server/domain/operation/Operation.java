package pt.tecnico.distledger.server.domain.operation;

import java.util.Map;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class Operation {

    private String account;
    private boolean stable = false;
    private Map<String, Integer> TS;
    private Map<String, Integer> prevTS;

    public Operation(String fromAccount, Map<String, Integer> prevTS, Map<String, Integer> TS) {
        this.account = fromAccount;
        this.TS = TS;
        this.prevTS = prevTS;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public DistLedgerCommonDefinitions.Operation proto() {
        return DistLedgerCommonDefinitions.Operation
                        .newBuilder()
                        .setType(DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED)
                        .setUserId(account)
                        .putAllPrevTS(prevTS)
                        .putAllTS(TS)
                        .build();
    }

    public Operation clone() {
        return new Operation(account, prevTS, TS);
    }

    public void setStable(boolean b) {
        this.stable = b;
    }

    public boolean getStable() {
        return stable;
    }

    public void setTS(Map<String, Integer> TS) {
        this.TS = TS;
    }

    public void setPrevTS(Map<String, Integer> prevTS) {
        this.prevTS = prevTS;
    }

    public Map<String, Integer> getTS() {
        return TS;
    }

    public Map<String, Integer> getPrevTS() {
        return prevTS;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Operation)) 
            return false;
        Operation newOp = (Operation) o;

        if (newOp.getAccount().equals(account))
            return true;
        return false;
    }
}