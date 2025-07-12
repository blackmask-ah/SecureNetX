# 🔐 SecureNetX Firewall Manager

**SecureNetX** is a modern, JavaFX-based Linux firewall manager that provides a user-friendly GUI to control `iptables` rules, block/unblock IPs, and monitor real-time network services.  
Ideal for cybersecurity learners, system administrators, or anyone who wants a graphical way to control Linux firewalls.

<p align="center">
  <img width="721" height="461" alt="dash" src="https://github.com/user-attachments/assets/d1b881f6-a41c-4e41-94da-83f253dd2550" />
  <br/>
  <img width="720" height="980" alt="view01" src="https://github.com/user-attachments/assets/8af4c808-4230-4e8e-a0c5-28dbf6df2cb8" />
  <br/>
  <img width="720" height=980" alt="view02" src="https://github.com/user-attachments/assets/14e9fa4f-95e5-44ba-8f50-05ef020bda9d" />
</p>

---

## ✨ Features

- 📡 **Service Scanner**  
  Lists all currently active TCP/UDP services with IP, port, and process name using `ss -tulnp`.

- 🔥 **Firewall Rule Management**  
  - Add or remove rules using protocol, IP address, port, and action (`ACCEPT` / `DROP`).
  - Logs feedback and errors in real time.
  - Extensible to support INPUT / OUTPUT direction (currently INPUT by default).

- 🚫 **Block/Unblock IPs**  
  Block or unblock any external IP using `iptables -A OUTPUT -d IP -j DROP` (and delete to unblock).

- 📋 **Live Logging**  
  Real-time visual log inside the application, showing rule actions, scanned services, IP blocks, and more.

- ❌ **Exit Button**  
  Gracefully close the application with one click.

---

## 🧪 Testing Example: Block Facebook

A great way to test the app is by blocking a known public IP like Facebook.

### 🔎 Steps:
1. Use `nslookup` or `ping` to find Facebook’s public IP:
   
   ```bash
   
   nslookup facebook.com

Example IP: 157.240.1.35

    Enter 157.240.1.35 into the “Block IP” field in the app and click Block IP.

    Test the block:

    ping 157.240.1.35         # Should fail
    curl https://facebook.com # May time out

    Click Unblock IP to reverse it.

    ⚠️ Note: Facebook uses many dynamic IPs and CDNs. Blocking one IP may not fully block the website.

🧰 Requirements

    Java 17+

    JavaFX SDK (e.g., javafx-sdk-21)

    Linux OS with:

        iptables

        ss command

    Run with sudo/root to allow iptables modifications

🚀 Getting Started
1. Clone the Repository

git clone https://github.com/your-username/SecureNetX-Firewall.git
cd SecureNetX-Firewall

2. Compile & Run (Manual)

javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml application/*.java
sudo java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml application.Main

    🛑 Important: Always run with sudo so the application can apply firewall rules.

3. Open in IDE

    Open the project in Eclipse / IntelliJ.

    Set VM Options to include JavaFX:

    --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml

📁 Project Structure

<img width="320" height="419" alt="files" src="https://github.com/user-attachments/assets/8023725e-9120-413d-aa73-8f510eda1382" />

⚠️ Disclaimer

    This tool uses and modifies iptables firewall rules. Misuse may result in loss of network access. Use responsibly and preferably in a controlled/test environment before applying to production systems.

📄 License

MIT License © 2025 [Your Name]
Feel free to modify, contribute, or share under the terms of the license.


Let me know if you'd like:
- `.jar` build instructions  
- `.deb` package guide for Debian-based systems  
- or README in another language (e.g., Urdu, Spanish)
