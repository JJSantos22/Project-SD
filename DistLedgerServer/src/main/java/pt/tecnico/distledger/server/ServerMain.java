package pt.tecnico.distledger.server;

import java.io.IOException;
import sun.misc.Signal;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.exceptions.NamingServerException;
import pt.tecnico.distledger.server.service.*;

public class ServerMain {

	
	
	private static final String SERVER_STARTED = "Server started";
	private static final String SERVER_TERMINATED = "Server terminated";
	private static final String SERVER_REGISTERED = "Server registered in naming server";
	private static final String SERVER_DELETED = "Server deleted from naming server";

	private static final String SERVICE = "DistLedger";
	private static final String SHUTDOWN_SERVER = "Press enter to shutdown";
	private static final String LOCALHOST = "localhost";
	
	private static final int EXIT_CODE_SUCESS = 0;

	/**
	 * Set flag to true to print debug messages.
	 * The flag can be set using the -Ddebug command line option.
	 */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/* Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static void main(String[] args) throws IOException, InterruptedException, NamingServerException {

		System.out.println(ServerMain.class.getSimpleName());

		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port qualifier%n", ServerMain.class.getName());
			return;
		}

		/** Example: args="2001 A" */
		final String host = LOCALHOST;
		final int port = Integer.parseInt(args[0]);
		final String qualifier = args[1];

		/** Naming server configurations */
		final int namingServerPort = 5001;
		final String namingServerHost = LOCALHOST;

		/** Address as host:port */
		final String address = host + ":" + port;
		final String namingServerAddress = namingServerHost + ":" + namingServerPort;
		

		/* Creating Naming Server Service */
		final NamingServerDistLedgerService namingservice = new NamingServerDistLedgerService(namingServerAddress);
		ServerState serverState = new ServerState(qualifier, namingservice);

		final BindableService userService = new UserDistLedgerService(serverState);
		final BindableService adminService = new AdminDistLedgerService(serverState);
		final CrossServerDistLedgerService crossService = new CrossServerDistLedgerService(serverState);

		/* Create a new server with the given port */
		Server server = ServerBuilder
				.forPort(port)
				.addService(userService)
				.addService(adminService)
				.addService(crossService)
				.build();

		
		/* Start the server */
		server.start();
		debug(SERVER_STARTED);

		/* Register the server in the Naming Server */
		try {
			namingservice.register(SERVICE, qualifier, address);
		} catch (NamingServerException exception) {
			debug(exception.getErrorMessage());
		}
		debug(SERVER_REGISTERED);

		Signal.handle(new Signal("INT"),  // SIGINT
		signal -> {
			try {
				namingservice.delete(SERVICE, address);
				debug(SERVER_DELETED);
			} catch (NamingServerException e) {
				debug(e.getErrorMessage());
			}
			server.shutdown();
			debug(SERVER_TERMINATED);
			System.exit(EXIT_CODE_SUCESS);
		});

		/* Awaits for shutdown */
		System.out.println(SHUTDOWN_SERVER);
		System.in.read();

		try {
			namingservice.delete(SERVICE, address);
			debug(SERVER_DELETED);
		} catch(NamingServerException exception) {
			debug(exception.getErrorMessage());
		}

		server.shutdown();
		debug(SERVER_TERMINATED);
	}
	
}
