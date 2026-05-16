package pixelbattle.game.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class NetworkManager {
    public enum NetMode { OFFLINE, HOST, CLIENT }
    public enum NetState { DISCONNECTED, WAITING, CONNECTED }

    private NetMode mode = NetMode.OFFLINE;
    private NetState state = NetState.DISCONNECTED;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private int port = 25565;
    private String hostAddress;

    private final BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> recvQueue = new LinkedBlockingQueue<>();

    private Thread acceptThread;
    private Thread sendThread;
    private Thread recvThread;

    private String disconnectReason;

    public NetworkManager() {}

    public boolean startHost() {
        try {
            mode = NetMode.HOST;
            state = NetState.WAITING;
            serverSocket = new ServerSocket(port);
            hostAddress = getLocalIP();

            acceptThread = new Thread(() -> {
                try {
                    Socket client = serverSocket.accept();
                    socket = client;
                    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    state = NetState.CONNECTED;
                    startIOThreads();
                } catch (IOException e) {
                    if (state == NetState.WAITING) {
                        disconnectReason = "等待连接失败";
                        state = NetState.DISCONNECTED;
                    }
                }
            }, "LAN-Accept");
            acceptThread.setDaemon(true);
            acceptThread.start();
            return true;
        } catch (IOException e) {
            disconnectReason = "无法启动服务器: " + e.getMessage();
            state = NetState.DISCONNECTED;
            return false;
        }
    }

    public boolean connect(String ip) {
        try {
            mode = NetMode.CLIENT;
            state = NetState.WAITING;
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            state = NetState.CONNECTED;
            startIOThreads();
            return true;
        } catch (IOException e) {
            disconnectReason = "连接失败: " + e.getMessage();
            state = NetState.DISCONNECTED;
            return false;
        }
    }

    private void startIOThreads() {
        sendThread = new Thread(() -> {
            try {
                while (state == NetState.CONNECTED) {
                    byte[] data = sendQueue.poll(50, TimeUnit.MILLISECONDS);
                    if (data != null) {
                        synchronized (out) {
                            out.writeInt(data.length);
                            out.write(data);
                            out.flush();
                        }
                    }
                }
            } catch (Exception e) {
                if (state == NetState.CONNECTED) {
                    disconnectReason = "发送错误";
                    state = NetState.DISCONNECTED;
                }
            }
        }, "LAN-Send");
        sendThread.setDaemon(true);
        sendThread.start();

        recvThread = new Thread(() -> {
            try {
                while (state == NetState.CONNECTED) {
                    int len = in.readInt();
                    if (len <= 0 || len > 65536) {
                        disconnectReason = "数据包异常";
                        state = NetState.DISCONNECTED;
                        break;
                    }
                    byte[] data = new byte[len];
                    in.readFully(data);
                    recvQueue.offer(data);
                }
            } catch (Exception e) {
                if (state == NetState.CONNECTED) {
                    disconnectReason = "连接断开";
                    state = NetState.DISCONNECTED;
                }
            }
        }, "LAN-Recv");
        recvThread.setDaemon(true);
        recvThread.start();
    }

    public void send(byte[] data) {
        if (state == NetState.CONNECTED) {
            sendQueue.offer(data);
        }
    }

    public byte[] recv() {
        return recvQueue.poll();
    }

    public void stop() {
        state = NetState.DISCONNECTED;
        mode = NetMode.OFFLINE;
        try { if (socket != null) socket.close(); } catch (Exception e) {}
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception e) {}
        socket = null;
        serverSocket = null;
        in = null;
        out = null;
        sendQueue.clear();
        recvQueue.clear();
        disconnectReason = null;
    }

    public NetMode getMode() { return mode; }
    public NetState getState() { return state; }
    public String getHostAddress() { return hostAddress; }
    public int getPort() { return port; }
    public String getDisconnectReason() { return disconnectReason; }
    public boolean isConnected() { return state == NetState.CONNECTED; }

    private String getLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {}
        return "127.0.0.1";
    }
}
