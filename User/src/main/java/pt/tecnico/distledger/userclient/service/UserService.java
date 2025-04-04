package pt.tecnico.distledger.userclient.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.exceptions.UserException;
import static pt.tecnico.distledger.userclient.exceptions.UserException.ErrorMessages.*;

import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

public class UserService {

    private static final String USER_SERVICE_STARTED = "UserService created.";
    private static final String USER_SERVICE_SHUT_DOWN = "UserService shut down";

    private static final String CREATE_ACCOUNT_REQUEST = "CreateAccount request sent: \n";
    private static final String CREATE_ACCOUNT_RESPONSE = "CreateAccount response received: \n";

    private static final String TRANSFER_TO_REQUEST = "TransferTo request sent: \n";
    private static final String TRANSFER_TO_RESPONSE = "TransferTo response received: \n";

    private static final String BALANCE_REQUEST = "Balance request sent: \n";
    private static final String BALANCE_RESPONSE = "Balance response received";

    private String address;
    private String qualifier;
    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;

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

    public UserService(String qualifier, String address) {
        this.qualifier = qualifier;
        this.address = address;
        /** Creating the channel to the target server */
        this.channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        /** Creating a blocking stub for the channel */
        this.stub = UserServiceGrpc.newBlockingStub(this.channel);
        debug(USER_SERVICE_STARTED);
    }

    /**
     * Request from the user to create a new account in a server
     * 
     * @param accountID the ID of the account that will be created
     * @throws UserException if the account already exists or the connection with the server is not possible
     */
    public Map<String, Integer> createAccount(String accountID, Map<String, Integer> timeStamps) throws UserException {
        CreateAccountRequest request = CreateAccountRequest.newBuilder().setUserId(accountID)
        .putAllPrevTS(timeStamps).build();
        debug(CREATE_ACCOUNT_REQUEST + request);
        CreateAccountResponse response;
        try {
            response = stub.createAccount(request);
            debug(CREATE_ACCOUNT_RESPONSE + response);
            return new HashMap<String, Integer>(response.getTSMap());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
                throw new UserException(IO_ERROR);
            }
            throw new UserException(exception.getStatus().getDescription());
        }
    }

    /**
     * Request from the user to transfer an amount from one account to another
     * 
     * @param fromAccountID the ID of the account to transfer from
     * @param toAccountID the ID of the account to transfer to
     * @param amount the amount to transfer
     * @throws UserExceptionif an error occurs during the process
     */
    public Map<String, Integer> transferTo(String fromAccountID, String toAccountID, int amount) throws UserException {
        TransferToRequest request = TransferToRequest.newBuilder().setAccountFrom(fromAccountID).setAccountTo(toAccountID).setAmount(amount).build();
        debug(TRANSFER_TO_REQUEST + request);
        try {
            TransferToResponse response = stub.transferTo(request);
            debug(TRANSFER_TO_RESPONSE + response);
            return new HashMap<String, Integer>(response.getTSMap());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
                throw new UserException(IO_ERROR);
            }
            throw new UserException(exception.getStatus().getDescription());
        }
    }

    /**
     * Request from the user to get the balance of an account
     * 
     * @param accountID the ID of the account
     * @return the balance of the account identified
     * @throws UserException if an error occurs during the process
     */
    public int balance(String accountID, Map<String, Integer> prevTS) throws UserException {
        BalanceRequest request = BalanceRequest.newBuilder().setUserId(accountID).putAllPrevTS(prevTS).build();
        debug(BALANCE_REQUEST + request);
        BalanceResponse response;
        try {
            response = stub.balance(request);
            debug(BALANCE_RESPONSE);
            int value = response.getValue();
            return value;

        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
                throw new UserException(IO_ERROR);
            }
            throw new UserException(exception.getStatus().getDescription());
        }
    }

    /** Shut down the channel and the service */
    public void shutdown() {
        channel.shutdownNow();
        debug(USER_SERVICE_SHUT_DOWN);
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getAddress() {
        return address;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
