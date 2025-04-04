package pt.tecnico.distledger.namingserver.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServiceEntry {
    private String name;

    /** Map to associate the qualifier and the corresponding server entry */
    private Map<String, ServerEntry> servers = new HashMap<>();

    public ServiceEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addServer(ServerEntry serverEntry) {
        servers.put(serverEntry.getQualifier(), serverEntry);
    }

    public ServerEntry getServerByQualifier(String qualifier) {
        return servers.get(qualifier);
    }

    public void deleteServer(String qualifier) {
        servers.remove(qualifier);
    }

    public Collection<ServerEntry> getServers() {
        return servers.values();
    }
}