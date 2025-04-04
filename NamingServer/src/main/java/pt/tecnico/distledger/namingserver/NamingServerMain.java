package pt.tecnico.distledger.namingserver;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;
import pt.tecnico.distledger.namingserver.service.NamingServerServiceImpl;

public class NamingServerMain {
    
    public static final int port = 5001;
	public static final String NAMING_SERVER_STARTED = "Naming server started";
	public static final String NAMING_SERVER_TERMINATED = "Naming server terminated";
	private static final String SHUTDOWN_SERVER = "Press enter to shutdown";

	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println(NamingServerMain.class.getSimpleName());

		
		NamingServerState namingserver = new NamingServerState();
		final BindableService namingServerService = new NamingServerServiceImpl(namingserver);
        
		/** Create a new server with multiple services listening to port */
		Server server = ServerBuilder
			.forPort(port)
			.addService(namingServerService)
			.build();

		/** Init the naming server */
		server.start();
		debug(NAMING_SERVER_STARTED);

		/* Awaits for shutdown */
		System.out.println(SHUTDOWN_SERVER);
		System.in.read();

		/** Naming server waiting to be terminated */
		server.shutdown();
		debug(NAMING_SERVER_TERMINATED);
    }
}

