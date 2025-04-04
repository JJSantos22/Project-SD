package pt.tecnico.distledger.userclient.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.exceptions.UserException;
import static pt.tecnico.distledger.userclient.exceptions.UserException.ErrorMessages.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistledgerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.*;

import java.util.List;

public class UserNamingServerService {

    private static final String USER_NAMING_SERVER_SERVICE_STARTED = "UserNamingServerService created";
    private static final String USER_NAMING_SERVER_SERVICE_SHUT_DOWN = "UserNamingServerService shut down";

    private static final String LOOKUP_REQUEST = "Lookup request sent";
    private static final String LOOKUP_RESPONSE = "Lookup response received";

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

    final ManagedChannel channel;
    NamingServerDistledgerServiceGrpc.NamingServerDistledgerServiceBlockingStub stub;

    public UserNamingServerService(String address) {
        /** Creating the channel to the target server */
        this.channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        /** Creating a blocking stub for the channel */
        stub = NamingServerDistledgerServiceGrpc.newBlockingStub(channel);
        debug(USER_NAMING_SERVER_SERVICE_STARTED);
    }

    /** Shut down the channel and the service */
    public void shutdown() {
        channel.shutdownNow();
        debug(USER_NAMING_SERVER_SERVICE_SHUT_DOWN);
    }

    /**
     * Request to lookup a server in the naming server
     * 
     * @param serviceName the name of the service to lookup
     * @param qualifier the qualifier of the service to lookup
     * @return a list with all the addresses of a server in this service and the given qualifier
     */
    public List<Server> lookup(String serviceName, String qualifier) throws UserException {
        debug(LOOKUP_REQUEST);
        try {
            LookupResponse response = stub.lookup(LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build());
            debug(LOOKUP_RESPONSE);
            return response.getServerList();
        } catch(StatusRuntimeException exception) {
            throw new UserException(IO_ERROR);
        }
    }
}
