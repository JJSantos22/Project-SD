package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.service.AdminNamingServerService;

public class AdminClientMain {
    private final static String LOCALHOST = "localhost";
    private final static int PORT = 5001;
    public static void main(String[] args) {

        System.out.println(AdminClientMain.class.getSimpleName());

        final String address = LOCALHOST + ":" + PORT;

        CommandParser parser = new CommandParser(new AdminNamingServerService(address));

		/** Input handling and service's methods calls*/
        parser.parseInput();
    }
}
