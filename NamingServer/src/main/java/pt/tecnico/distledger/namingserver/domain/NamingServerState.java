package pt.tecnico.distledger.namingserver.domain;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import pt.tecnico.distledger.namingserver.exceptions.NamingServerException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;

import static pt.tecnico.distledger.namingserver.exceptions.NamingServerException.ErrorMessages.*;

import java.util.HashMap;

public class NamingServerState {

    /** Map to save the Service name with the ServiceEntry */
    private Map<String, ServiceEntry> serviceEntries = new HashMap<String, ServiceEntry>();

    /**
    * Register the new server in the Naming Server
    *
    * @param serviceName the service name for the service that will be registered
    * @param qualifier the qualifier for the service to identify the server entry
    * @param address the address of the new server entry
    * @throws NamingServerException if the register process fails
    */
    public synchronized void register(String serviceName, String qualifier, String address) throws NamingServerException{
        if(!serviceEntries.containsKey(serviceName)) {
            ServerEntry serverEntry = new ServerEntry(qualifier, address);
            ServiceEntry serviceEntry = new ServiceEntry(serviceName);
            serviceEntry.addServer(serverEntry);
            serviceEntries.put(serviceName, serviceEntry);
        } else
            serviceEntries.get(serviceName).addServer(new ServerEntry(qualifier, address));
    }

    /**
     * Lookup the server in the Naming Server
     * 
     * @param serviceName the service name for the service that will be lookup
     * @param qualifier the qualifier for the service to identify the server entry that we are looking for
     * @return list of address of that belong to that service and with the given qualifier
     */
    public synchronized List<Server> lookup(String serviceName, String qualifier){
        List<Server> addresses = new ArrayList<Server>();
        if (qualifier.equals("") && serviceEntries.get(serviceName) != null) {
            for (ServerEntry server : serviceEntries.get(serviceName).getServers())
                addresses.add(server.proto());
        }
            
        else if (serviceEntries.containsKey(serviceName) && serviceEntries.get(serviceName).getServerByQualifier(qualifier) != null) 
            addresses.add(serviceEntries.get(serviceName).getServerByQualifier(qualifier).proto());
        return addresses;
    }

    /**
     * Delete the server in the Naming Server
     * 
     * @param serviceName the service name for the service that will be deleted
     * @param qualifier the qualifier for the service to identify the server entry that will be deleted
     * @throws NamingServerException if the delete process fails
    */
    public synchronized void delete(String serviceName, String qualifier) throws NamingServerException{
        if(!serviceEntries.containsKey(serviceName))
            throw new NamingServerException(FAIL_DELETE + String.format(NO_SERVICE, serviceName));
        
        serviceEntries.get(serviceName).deleteServer(qualifier);  
    }

}
