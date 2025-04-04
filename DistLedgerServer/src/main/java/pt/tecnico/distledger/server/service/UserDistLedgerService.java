package pt.tecnico.distledger.server.service;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.*;
import pt.tecnico.distledger.server.exceptions.UserException;
import pt.ulisboa.tecnico.distledger.contract.user.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import static pt.tecnico.distledger.server.exceptions.UserException.ErrorMessages.*;

import java.util.Map;

public class UserDistLedgerService extends UserServiceGrpc.UserServiceImplBase {

	private static final String USER_DISTLEDGER_SERVICE_STARTED = "UserService created";

	private static final String CREATE_ACCOUNT_REQUEST = "CreateAccount request received: \n";
	private static final String CREATE_ACCOUNT_RESPONSE = "CreateAccount response sent: \n";

	private static final String TRANSFER_TO_REQUEST = "TransferTo request received: \n";
	private static final String TRANSFER_TO_RESPONSE = "TransferTo response sent: \n";

	private static final String BALANCE_REQUEST = "Balance request received: \n";
	private static final String BALANCE_RESPONSE = "Balance response sent";

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

	private ServerState serverState;

	public UserDistLedgerService(ServerState serverState) {
		this.serverState = serverState;
		debug(USER_DISTLEDGER_SERVICE_STARTED);
	}

	/**
	 * Request to create a new account in the server
	 * 
	 * @param request the create account request with the new account data
	 * @param responseObserver the response observer for sending the activation response
	 */
	@Override
	public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
		try {
			debug(CREATE_ACCOUNT_REQUEST + request);
			Map<String, Integer> timeStamps = serverState.createAccount(request.getUserId(), request.getPrevTSMap());
			CreateAccountResponse response = CreateAccountResponse.newBuilder().putAllTS(timeStamps).build();
			debug(CREATE_ACCOUNT_RESPONSE + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (UserException exception) {
			if (exception.getErrorMessage().equals(SERVER_UNAVAILABLE)) 
				responseObserver.onError(UNAVAILABLE.withDescription(exception.getErrorMessage()).asRuntimeException());
			else 
				responseObserver.onError(INVALID_ARGUMENT.withDescription(exception.getErrorMessage()).asRuntimeException());
			debug(exception.getErrorMessage());

		} 
	}

	/**
	 * Request to transfer an amount from one account to another
	 * 
	 * @param request the transfer request with the transfer data (from, to, amount)
	 * @param responseObserver the response observer for sending the transfer response
	 */
	@Override
	public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
		try {
			debug(TRANSFER_TO_REQUEST + request);
			Map<String, Integer> timeStamps = serverState.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), request.getPrevTSMap());
			TransferToResponse response = TransferToResponse.newBuilder().putAllTS(timeStamps).build();
			debug(TRANSFER_TO_RESPONSE + response);
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (UserException exception) {
			if (exception.getErrorMessage().equals(SERVER_UNAVAILABLE)) {
				responseObserver.onError(UNAVAILABLE.withDescription(exception.getErrorMessage()).asRuntimeException());
				debug(exception.getErrorMessage());
			} else {
				responseObserver
						.onError(INVALID_ARGUMENT.withDescription(exception.getErrorMessage()).asRuntimeException());
				debug(exception.getErrorMessage());
			}
		}
	}
	
	/**
	 * Request the balance of an account
	 * 
	 * @param request the balance request with the account identifier
	 * @param responseObserver the response observer for sending the balance response
	 */
	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		try {
			debug(BALANCE_REQUEST + request);
			int balance = serverState.balance(request.getUserId(), request.getPrevTSMap());
			BalanceResponse response = BalanceResponse.newBuilder().setValue(balance).build();
			debug(BALANCE_RESPONSE);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (UserException exception) {
			if (exception.getErrorMessage().equals(SERVER_UNAVAILABLE)) {
				responseObserver.onError(UNAVAILABLE.withDescription(exception.getErrorMessage()).asRuntimeException());
				debug(exception.getErrorMessage());
			} else {
				responseObserver
						.onError(INVALID_ARGUMENT.withDescription(exception.getErrorMessage()).asRuntimeException());
				debug(exception.getErrorMessage());
			}
		}
	}
}
