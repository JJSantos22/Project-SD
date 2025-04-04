package pt.tecnico.distledger.server.domain.operation;

import java.util.Map;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class CreateOp extends Operation {

    public CreateOp(String account, Map<String, Integer> prevTS, Map<String, Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public DistLedgerCommonDefinitions.Operation proto() {
        return DistLedgerCommonDefinitions.Operation
						.newBuilder()
						.setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
						.setUserId(this.getAccount())
                        .putAllPrevTS(this.getPrevTS())
                        .putAllTS(this.getTS())
						.build();
    }

    @Override
    public CreateOp clone() {
        return new CreateOp(getAccount(), this.getPrevTS(), this.getTS());
    }
}
