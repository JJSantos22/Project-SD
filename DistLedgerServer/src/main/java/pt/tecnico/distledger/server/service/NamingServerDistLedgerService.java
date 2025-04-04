package pt.tecnico.distledger.server.service;

import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.exceptions.NamingServerException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistledgerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserverservice.NamingServerDistLedger.*;

public class NamingServerDistLedgerService {

    private static final String NAMING_SERVER_SERVICE_STARTED = "NamingServerDistledgerServices created";

    private static final String REGISTER_REQUEST = "Register request sent: \n";
    private static final String REGISTER_RESPONSE = "Register response received";
    private static final String REGISTER_FAILED = "Register failed: ";

    private static final String DELETE_REQUEST = "Delete request sent: \n";
    private static final String DELETE_RESPONSE = "Delete response received";
    private static final String DELETE_FAILED = "Delete failed: ";

    private static final String LOOKUP_REQUEST = "Lookup request sent: \n";
    private static final String LOOKUP_RESPONSE = "Lookup response received";
    private static final String LOOKUP_FAILED = "Lookup failed: ";

    private String address;
    private ManagedChannel channel;
    private NamingServerDistledgerServiceGrpc.NamingServerDistledgerServiceBlockingStub stub;

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

    public NamingServerDistLedgerService(String address) {
        this.address = address;
        // Create a channel to the provided address
        this.channel = ManagedChannelBuilder.forTarget(this.address).usePlaintext().build();
        // Create a stub to send requests to the provided address
        this.stub = NamingServerDistledgerServiceGrpc.newBlockingStub(this.channel);
        debug(NAMING_SERVER_SERVICE_STARTED);
    }

    /**
     * Request the registration of a server into the naming server
     * 
     * @param serviceName the name of the service to register.
     * @param qualifier the qualifier for the service.
     * @param address the address of the service.
     * @throws NamingServerException if the registration fails.
     */
    public void register(String serviceName, String qualifier, String address) throws NamingServerException {
        RegisterRequest request = RegisterRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).setAddress(address).build();
        debug(REGISTER_REQUEST + request);
        try {
            stub.register(request);
        } catch (StatusRuntimeException exception) {
            throw new NamingServerException(REGISTER_FAILED + exception.getStatus().getDescription());
        }
        debug(REGISTER_RESPONSE);
    }

     /**
     * Request the deletion of a server from the naming server
     * 
     * @param serviceName the name of the service to delete.
     * @param address the address of the service to delete
     * @throws NamingServerException if the deletion fails.
     */
    public void delete(String serviceName, String address) throws NamingServerException {
        DeleteRequest request = DeleteRequest.newBuilder().setServiceName(serviceName).setAddress(address).build();
        debug(DELETE_REQUEST + request);
        try {
            stub.delete(request);
        } catch (StatusRuntimeException exception) {
            throw new NamingServerException(DELETE_FAILED + exception.getStatus().getDescription());
        }
        debug(DELETE_RESPONSE);
    }

  /**
     * Request the lookup of a server from the naming server
     * 
     * @param serviceName the name of the service to lookup
     * @param qualifier the qualifier of the service to lookup
     * @return list of strings of the address
     */
    public List<Server> lookup(String serviceName, String qualifier) throws NamingServerException {
        LookupRequest request = LookupRequest.newBuilder().setServiceName(serviceName).setQualifier(qualifier).build();
        debug(LOOKUP_REQUEST + request);
        try {
            LookupResponse response = stub.lookup(request);
            debug(LOOKUP_RESPONSE);
            return response.getServerList();
        } catch (StatusRuntimeException exception) {
            throw new NamingServerException(LOOKUP_FAILED + exception.getStatus().getDescription());
        }
    }
    /**
     * Request the lookup of a server from the naming server
     * 
     * @param serviceName the name of the service to lookup
     * @param qualifier the qualifier of the service to lookup
     * @return list of strings of the address
     */
    public List<Server> lookup(String serviceName) throws NamingServerException {
        return lookup(serviceName, "");
    }
}
