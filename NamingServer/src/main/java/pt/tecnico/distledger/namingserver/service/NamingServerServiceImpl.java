package pt.tecnico.distledger.namingserver.service;

import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;
import pt.tecnico.distledger.namingserver.exceptions.NamingServerException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistledgerServiceGrpc.NamingServerDistledgerServiceImplBase;

public class NamingServerServiceImpl extends NamingServerDistledgerServiceImplBase {

	private static final String NAMING_SERVER_SERVICE_STARTED = "Naming server service created";

	private static final String REGISTER_REQUEST = "Register request received: \n";
	private static final String REGISTER_RESPONSE = "Register response sent";

	private static final String DELETE_REQUEST = "Delete request received: \n";
	private static final String DELETE_RESPONSE = "Delete response sent";

	private static final String LOOKUP_REQUEST = "Lookup request received: \n";
	private static final String LOOKUP_RESPONSE = "Lookup response sent";

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

	/** Naming Server */
	private NamingServerState namingserver;

	public NamingServerServiceImpl(NamingServerState namingserver) {
		this.namingserver = namingserver;
		debug(NAMING_SERVER_SERVICE_STARTED);
	}

	/**
	 * Receives the request to register a new server
	 * 
	 * @param request the register request with the data to register in the naming server a new server
	 * @param responseObserver the response observer for sending the register response
	 */
	@Override
	public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
		try {
			debug(REGISTER_REQUEST + request);
			namingserver.register(request.getServiceName(), request.getQualifier(), request.getAddress());
			RegisterResponse response = RegisterResponse.getDefaultInstance();
			debug(REGISTER_RESPONSE);
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (NamingServerException exception) {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription(exception.getErrorMessage()).asRuntimeException());
			debug(exception.getErrorMessage());
		}
	}

	/**
	 * Receives the request to lookup a server
	 * 
	 * @param request the lookup request with the data to lookup a server
	 * @param responseObserver the response observer for sending the lookup response
	 */
	@Override
	public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
		debug(LOOKUP_REQUEST + request);
		List<Server> servers = namingserver.lookup(request.getServiceName(), request.getQualifier());
		LookupResponse response = LookupResponse.newBuilder().addAllServer(servers).build();
		debug(LOOKUP_RESPONSE);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	/**
	 * Receives the request to delete a server
	 * 
	 * @param request the delete request with the data to identify the server that will be deleted
	 * @param responseObserver the response observer for sending the delete response
	 */
	@Override
	public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
		try {
			debug(DELETE_REQUEST + request);
			namingserver.delete(request.getServiceName(), request.getAddress());
			DeleteResponse response = DeleteResponse.getDefaultInstance();
			debug(DELETE_RESPONSE);
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (NamingServerException exception) {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription(exception.getErrorMessage()).asRuntimeException());
			debug(exception.getErrorMessage());
		}
	}
}
