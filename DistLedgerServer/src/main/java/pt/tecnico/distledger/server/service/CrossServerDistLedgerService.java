package pt.tecnico.distledger.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.grpc.Status.UNAVAILABLE;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.exceptions.CrossServerException;
import static pt.tecnico.distledger.server.exceptions.CrossServerException.ErrorMessages.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;

public class CrossServerDistLedgerService extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private static final String CROSS_SERVER_SERVICE_STARTED = "CrossServerDistLedgerService started";

    private static final String STATE_REQUEST_SENT = "Propagate State request sent";
    private static final String STATE_RESPONSE_RECEIVED = "Propagate State response received";

    private static final String STATE_REQUEST_RECEIVED = "Propagate State request received: \n";
    private static final String STATE_RESPONSE_SENT = "Propagate State response sent";

    private String address;
    private String qualifier;
    private ManagedChannel channel;
    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub;
    private ServerState serverState = null;

    /**
     * Set flag to true to print debug messages.
     * The flag can be set using the -Ddebug command line option.
     */
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    public CrossServerDistLedgerService(ServerState serverState) {
		this.serverState = serverState;
	}

    // Constructor for sending requests
    public CrossServerDistLedgerService(String qualifier, String address, ServerState serverState) {
        this.serverState = serverState;
        this.address = address;
        this.qualifier = qualifier;
        this.channel = ManagedChannelBuilder.forTarget(this.address).usePlaintext().build();
        this.stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(this.channel);
        debug(CROSS_SERVER_SERVICE_STARTED);
    }

    /**
     *  Propagate the server state to the server's replica
     * 
     * @param listOperations a list of operations to be included in the propagated state
     * @throws CrossServerException if there is an error propagating the state to other servers
     */
    public void propagateState(List<Operation> listOperations, Map<String, Integer> timeStamps) throws CrossServerException {
        // This creates ledgerState from CrossServer_DistLedger.proto and the request
        PropagateStateRequest request = PropagateStateRequest
            .newBuilder()
            .setState(LedgerState
                .newBuilder()
                .addAllLedger(listOperations
                    .stream()
                    .map(operation -> operation.proto())
                    .collect(Collectors.toList())))
            .putAllReplicaTS(timeStamps)
            .build();
        debug(STATE_REQUEST_SENT);

        try {
            stub.propagateState(request);
            debug(STATE_RESPONSE_RECEIVED);

        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(UNAVAILABLE.getCode())) {
                throw new CrossServerException(qualifier, FAIL_PROPAGATE_STATE);
            }
            throw new CrossServerException(qualifier, exception.getStatus().getDescription());
        }
    }

    /** 
     * Process the request to propagate the state to the other servers
     * 
     * @param request the request to propagate the state
     */
    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        debug(STATE_REQUEST_RECEIVED + request);
        serverState.receiveGossip(unProto(request.getState().getLedgerList()), request.getReplicaTSMap());
        responseObserver.onNext(PropagateStateResponse.getDefaultInstance());
        responseObserver.onCompleted();
        debug(STATE_RESPONSE_SENT);	
    }

    /**
     * Helper method to convert operation type from proto to domain 
     * 
     * @param ledger the ledger to be converted
     * @return the converted ledger
     */
    private List<Operation> unProto(List<DistLedgerCommonDefinitions.Operation> ledger) {
        List<Operation> toReturn = new ArrayList<>();
        for(DistLedgerCommonDefinitions.Operation operation : ledger) {
            switch (operation.getType()) {
                case OP_CREATE_ACCOUNT:
                    toReturn.add(new CreateOp(operation.getUserId(), operation.getPrevTSMap(), operation.getTSMap()));
                    break;
                case OP_TRANSFER_TO:
                    toReturn.add(new TransferOp(operation.getUserId(), operation.getDestUserId(), operation.getAmount(), operation.getPrevTSMap(), operation.getTSMap()));
                    break;
                default:
                    break;
            }
        }
        return toReturn;
    }

    public ServerState getServerState() {
        return this.serverState;
    }
}
