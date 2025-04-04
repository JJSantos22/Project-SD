package pt.tecnico.distledger.server.domain.operation;

import java.util.Map;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class TransferOp extends Operation {

    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount, Map<String, Integer> prevTS, Map<String, Integer> TS) {
        super(fromAccount, prevTS, TS);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DistLedgerCommonDefinitions.Operation proto() {
        return DistLedgerCommonDefinitions.Operation.newBuilder()
            .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
            .setUserId(getAccount())
            .setAmount(amount)
            .setDestUserId(destAccount)
            .putAllPrevTS(this.getPrevTS())
            .putAllTS(this.getTS())
            .build();
    }
    
    @Override
    public TransferOp clone() {
        return new TransferOp(getAccount(), destAccount, amount, this.getPrevTS(), this.getTS());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransferOp)) 
            return false;
            TransferOp newOp = (TransferOp) o;

        if (newOp.getAccount().equals(this.getAccount())
            && newOp.getDestAccount().equals(destAccount)
            && newOp.getAmount() == amount)
            return true;
        return false;
    }
}