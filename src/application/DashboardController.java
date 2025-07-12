package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardController {

    @FXML private TableView<ServiceInfo> table;
    @FXML private TableColumn<ServiceInfo, String> protocolCol;
    @FXML private TableColumn<ServiceInfo, String> ipAddressCol;
    @FXML private TableColumn<ServiceInfo, String> portCol;
    @FXML private TableColumn<ServiceInfo, String> processCol;

    @FXML private TextField ruleProtocolField;
    @FXML private TextField ruleIpField;
    @FXML private TextField rulePortField;
    @FXML private ComboBox<String> ruleActionBox;

    @FXML private TextField blockIpField;
    @FXML private TextArea logArea;

    private final Set<String> blockedIps = new HashSet<>();

    @FXML
    public void initialize() {
        protocolCol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        ipAddressCol.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        portCol.setCellValueFactory(new PropertyValueFactory<>("port"));
        processCol.setCellValueFactory(new PropertyValueFactory<>("process"));

        ruleActionBox.getItems().addAll("ACCEPT", "DROP");
        ruleActionBox.getSelectionModel().selectFirst();

        scanServices();
    }

    @FXML
    public void scanServices() {
        ObservableList<ServiceInfo> services = FXCollections.observableArrayList();
        try {
            Process dropProc = new ProcessBuilder("bash", "-c", "iptables -S | grep '\\-p .* --dport'").start();
            BufferedReader dropReader = new BufferedReader(new InputStreamReader(dropProc.getInputStream()));
            List<String> drops = dropReader.lines().toList();

            Process cmd = new ProcessBuilder("bash", "-c", "ss -tulnp").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("tcp") || line.startsWith("udp")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 7) {
                        String proto = parts[0];
                        String addrPort = parts[4];
                        String ip = addrPort.substring(0, addrPort.lastIndexOf(':'));
                        String port = addrPort.substring(addrPort.lastIndexOf(':') + 1);
                        String proc = parts[6];
                        boolean dropped = drops.stream().anyMatch(r -> r.contains("-p " + proto) && r.contains("--dport " + port));
                        if (!dropped) {
                            services.add(new ServiceInfo(proto, ip, port, proc));
                        }
                    }
                }
            }

            table.setItems(services);
            logArea.appendText("🔍 [INFO] Scanned and updated service list.\n");

        } catch (Exception e) {
            logArea.appendText("❌ [ERROR] Scan Error: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void addFirewallRule() {
        String rule = formatRule();
        if (!rule.isEmpty()) {
            String cmd = "iptables -A INPUT " + rule;
            runIptables(cmd, "✅ [ADDED] Rule applied: " + cmd);
        }
    }

    @FXML
    private void deleteFirewallRule() {
        String rule = formatRule();
        if (!rule.isEmpty()) {
            String cmd = "iptables -D INPUT " + rule;
            runIptables(cmd, "🗑️ [REMOVED] Rule removed: " + cmd);
        }
    }

    private String formatRule() {
        String proto = ruleProtocolField.getText().trim();
        String ip = ruleIpField.getText().trim();
        String port = rulePortField.getText().trim();
        String action = ruleActionBox.getValue();

        if (proto.isEmpty() || port.isEmpty()) {
            logArea.appendText("⚠️ [WARNING] Protocol and Port are required.\n");
            return "";
        }

        return "-p " + proto + (ip.isEmpty() ? "" : " -s " + ip) + " --dport " + port + " -j " + action;
    }

    @FXML
    private void blockIp() {
        String input = blockIpField.getText().trim();
        if (input.isEmpty()) {
            logArea.appendText("⚠️ [WARNING] Enter IP or domain to block.\n");
            return;
        }

        Set<String> ips = resolveToIps(input);
        if (ips.isEmpty()) {
            logArea.appendText("❌ [ERROR] Could not resolve: " + input + "\n");
            return;
        }

        for (String ip : ips) {
            if (!blockedIps.contains(ip)) {
                String cmd = "iptables -A OUTPUT -d " + ip + " -j DROP";
                runIptables(cmd, "🚫 [BLOCKED] " + input + " => " + ip);
                blockedIps.add(ip);
            } else {
                logArea.appendText("⚠️ [INFO] Already blocked: " + ip + "\n");
            }
        }
    }

    @FXML
    private void unblockIp() {
        String input = blockIpField.getText().trim();
        if (input.isEmpty()) {
            logArea.appendText("⚠️ [WARNING] Enter IP or domain to unblock.\n");
            return;
        }

        Set<String> ips = resolveToIps(input);
        if (ips.isEmpty()) {
            logArea.appendText("❌ [ERROR] Could not resolve: " + input + "\n");
            return;
        }

        for (String ip : ips) {
            if (blockedIps.contains(ip)) {
                String cmd = "iptables -D OUTPUT -d " + ip + " -j DROP";
                runIptables(cmd, "✅ [UNBLOCKED] " + input + " => " + ip);
                blockedIps.remove(ip);
            } else {
                logArea.appendText("⚠️ [INFO] Not blocked previously: " + ip + "\n");
            }
        }
    }

    private Set<String> resolveToIps(String domainOrIp) {
        Set<String> result = new HashSet<>();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domainOrIp);
            for (InetAddress addr : addresses) {
                String ip = addr.getHostAddress();
                if (!ip.contains(":")) { // IPv4 only
                    result.add(ip);
                }
            }
            logArea.appendText("🌐 [DNS] " + domainOrIp + " resolved to: " + result + "\n");
        } catch (Exception e) {
            logArea.appendText("❌ [DNS ERROR] " + domainOrIp + " - " + e.getMessage() + "\n");
        }
        return result;
    }

    public void exitProgram() {
        Platform.exit();
    }

    private void runIptables(String command, String successMsg) {
        try {
            Process p = new ProcessBuilder("bash", "-c", command).start();
            int exitCode = p.waitFor();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

            if (exitCode == 0) {
                logArea.appendText(successMsg + "\n");
            } else {
                logArea.appendText("❌ [FAILED] Command: " + command + "\n");
                logArea.appendText("🧨 [ERROR] " + errorMsg + "\n");
            }

            scanServices();

        } catch (Exception e) {
            logArea.appendText("❌ [EXCEPTION] While executing: " + command + "\n" + e.getMessage() + "\n");
        }
    }
}
