package application;

public class ServiceInfo {
    private String protocol;
    private String ipAddress;
    private String port;
    private String process;

    public ServiceInfo(String protocol, String ipAddress, String port, String process) {
        this.protocol = protocol;
        this.ipAddress = ipAddress;
        this.port = port;
        this.process = process;
    }

    public String getProtocol() { return protocol; }
    public String getIpAddress() { return ipAddress; }
    public String getPort() { return port; }
    public String getProcess() { return process; }
}
