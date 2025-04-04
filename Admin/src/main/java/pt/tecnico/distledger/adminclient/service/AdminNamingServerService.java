package pt.tecnico.distledger.adminclient.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistledgerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.*;

import java.util.List;


public class AdminNamingServerService {

    private static final String ADMIN_NAMING_SERVER_SERVICE_STARTED = "AdminNamingServerService created";
    private static final String ADMIN_NAMING_SERVER_SERVICE_SHUT_DOWN = "AdminNamingServerService shut down";

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

    public AdminNamingServerService(String target){
        /** Creating the channel to the target server */
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        /** Creating a blocking stub for the channel */
        stub = NamingServerDistledgerServiceGrpc.newBlockingStub(channel);
		debug(ADMIN_NAMING_SERVER_SERVICE_STARTED);
    }

    /** Shut down the channel and the service */
    public void shutdown() {
        channel.shutdownNow();
		debug(ADMIN_NAMING_SERVER_SERVICE_SHUT_DOWN);
    }

    /**
     * Sends a lookup request to the server for a specific service name and qualifier.
     *
     * @param serviceName the name of the service to look up.
     * @param qualifier  the qualifier of the service to look up.
     * @return a list of addresses for the service, as strings.
     */
    public List<Server> lookup(String serviceName, String qualifier) {
        debug(LOOKUP_REQUEST);
        LookupResponse response = stub.lookup(LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build());
        debug(LOOKUP_RESPONSE);
        return response.getServerList();
    }
}
