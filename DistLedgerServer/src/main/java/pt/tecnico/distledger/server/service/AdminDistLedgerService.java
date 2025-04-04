package pt.tecnico.distledger.server.service;

import java.util.ArrayList;
import java.util.List;
import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.*;
import pt.tecnico.distledger.server.exceptions.AdminException;
import pt.tecnico.distledger.server.exceptions.CrossServerException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminDistLedgerService extends AdminServiceGrpc.AdminServiceImplBase {

	private static final String ACTIVATE_REQUEST = "Activate Server Request received";
	private static final String ACTIVATE_RESPONSE = "Activate Server Response Sent";

	private static final String DEACTIVATE_REQUEST = "Deactivate Server Request received";
	private static final String DEACTIVATE_RESPONSE = "Deactivate Server Response Sent";

	private static final String GET_LEDGER_STATE_REQUEST = "Get Ledger State Request received";
	private static final String GET_LEDGER_STATE_RESPONSE = "Get Ledger State Response Sent: \n";

	private static final String GOSSIP_REQUEST = "Gossip Response Sent: \n";
	private static final String GOSSIP_RESPONSE = "Gossip Response received";


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

	/** Server state */
	private ServerState serverState;

	public AdminDistLedgerService(ServerState serverState) {
		this.serverState = serverState;
	}

	/**
	 * Process the request to activate the server
	 * 
	 * @param request the activation request
	 * @param responseObserver the response observer for sending the activation response
	 * @throws RuntimeException if an error occurs during the activation process
	 */
	@Override
	public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
		try {
			debug(ACTIVATE_REQUEST);
			serverState.activate();

			ActivateResponse response = ActivateResponse.getDefaultInstance();
			debug(ACTIVATE_RESPONSE);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (AdminException exception) {
			responseObserver
					.onError(FAILED_PRECONDITION.withDescription(exception.getErrorMessage()).asRuntimeException());
			debug(exception.getErrorMessage());
		}
	}

	/**
	 * Process the request to deactivate the server
	 *
	 * @param request the deactivation request
	 * @param responseObserver the response observer for sending the deactivation response
	 * @throws RuntimeException if an error occurs during the deactivation process
	 */
	@Override
	public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
		try {
			debug(DEACTIVATE_REQUEST);
			serverState.deactivate();

			DeactivateResponse response = DeactivateResponse.getDefaultInstance();
			debug(DEACTIVATE_RESPONSE);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (AdminException exception) {
			responseObserver
					.onError(FAILED_PRECONDITION.withDescription(exception.getErrorMessage()).asRuntimeException());
			debug(exception.getErrorMessage());
		}
	}

	/**
	 * Retrieves the current ledger state of the server and sends it as a response to the client.
	 * 
	 * @param request the request message of the current state of the Ledger
	 * @param responseObserver the response observer that will receive the response message
	 */
	@Override
	public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
		debug(GET_LEDGER_STATE_REQUEST);
		List<DistLedgerCommonDefinitions.Operation> ledger = new ArrayList<>();

		serverState.getLedgerState().stream().forEach(op -> ledger.add(op.proto()));
		getLedgerStateResponse response = getLedgerStateResponse
			.newBuilder()
			.setLedgerState(LedgerState
				.newBuilder()
				.addAllLedger(ledger)
				.build())
			.build();
		debug(GET_LEDGER_STATE_RESPONSE + response);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	/**
	 * Retrieves the current ledger state of the server and sends it as a response to the client.
	 * 
	 * @param request the request message of the current state of the Ledger
	 * @param responseObserver the response observer that will receive the response message
	 */
	@Override
	public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
		try {
			debug(GOSSIP_REQUEST);
			serverState.sendGossip();
			debug(GOSSIP_RESPONSE);
			responseObserver.onNext(GossipResponse.getDefaultInstance());
			responseObserver.onCompleted();
		} catch (CrossServerException exception) {
			debug("Error Gossiping");
		}		
	}
}
