package com.mcqqbridge.network;

import com.mcqqbridge.McQqBridge;
import com.mcqqbridge.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP 客户端
 * 负责与QQ机器人的TCP服务器建立连接和通信
 */
public class TcpClient {

    private final McQqBridge plugin;
    private final ConfigManager config;
    
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    
    private Thread receiveThread;
    private BukkitTask reconnectTask;
    
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    public TcpClient(McQqBridge plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    /**
     * 连接到TCP服务器
     */
    public void connect() {
        if (connected.get() || connecting.get()) {
            return;
        }
        
        connecting.set(true);
        shouldReconnect.set(true);
        
        // 在异步线程中建立连接
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String host = config.getTcpHost();
                int port = config.getTcpPort();
                
                if (config.isVerbose()) {
                    plugin.getLogger().info("正在连接到 " + host + ":" + port + "...");
                }
                
                socket = new Socket(host, port);
                socket.setKeepAlive(true);
                socket.setSoTimeout(0); // 无超时
                
                writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), 
                        true
                );
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                
                connected.set(true);
                connecting.set(false);
                
                plugin.getLogger().info("已成功连接到 QQ 机器人服务器!");
                
                // 启动接收线程
                startReceiveThread();
                
            } catch (IOException e) {
                connecting.set(false);
                plugin.getLogger().warning("连接失败: " + e.getMessage());
                scheduleReconnect();
            }
        });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        shouldReconnect.set(false);
        connected.set(false);
        
        // 取消重连任务
        if (reconnectTask != null) {
            reconnectTask.cancel();
            reconnectTask = null;
        }
        
        // 关闭接收线程
        if (receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;
        }
        
        // 关闭连接
        closeConnection();
    }

    /**
     * 重新连接
     */
    public void reconnect() {
        disconnect();
        shouldReconnect.set(true);
        connect();
    }

    /**
     * 关闭连接资源
     */
    private void closeConnection() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            if (config.isVerbose()) {
                plugin.getLogger().warning("关闭连接时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 发送消息
     */
    public synchronized void send(String message) {
        if (!connected.get() || writer == null) {
            if (config.isVerbose()) {
                plugin.getLogger().warning("无法发送消息，连接未建立");
            }
            return;
        }
        
        try {
            writer.print(message);
            writer.flush();
            
            if (config.isVerbose()) {
                plugin.getLogger().info("[发送] " + message.replace("\n", "\\n"));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("发送消息失败: " + e.getMessage());
            handleDisconnect();
        }
    }

    /**
     * 发送玩家登录消息
     */
    public void sendPlayerLogin(String playerName) {
        send(MessageProtocol.buildLoginMessage(playerName));
    }

    /**
     * 发送玩家下线消息
     */
    public void sendPlayerDisconnect(String playerName) {
        send(MessageProtocol.buildDisconnectMessage(playerName));
    }

    /**
     * 发送聊天消息
     */
    public void sendChatMessage(String playerName, String message) {
        String formattedMessage = config.getMcToQqFormat()
                .replace("{player}", playerName)
                .replace("{message}", message);
        send(MessageProtocol.buildChatMessage(formattedMessage));
    }

    /**
     * 发送服务器启动消息
     */
    public void sendServerStart() {
        send(MessageProtocol.buildServerStartMessage());
    }

    /**
     * 启动接收线程
     */
    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            StringBuilder buffer = new StringBuilder();
            char[] charBuffer = new char[1024];
            
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    int charsRead = reader.read(charBuffer);
                    if (charsRead == -1) {
                        // 连接关闭
                        handleDisconnect();
                        break;
                    }
                    
                    buffer.append(charBuffer, 0, charsRead);
                    
                    // 处理收到的数据，按 | 分隔
                    String data = buffer.toString();
                    int lastSeparator = data.lastIndexOf(MessageProtocol.SEPARATOR);
                    
                    if (lastSeparator >= 0) {
                        String completeData = data.substring(0, lastSeparator);
                        buffer = new StringBuilder(data.substring(lastSeparator + 1));
                        
                        // 分割并处理每条消息
                        String[] messages = completeData.split("\\|");
                        for (String msg : messages) {
                            if (!msg.isEmpty()) {
                                processReceivedMessage(msg);
                            }
                        }
                    }
                    
                } catch (SocketException e) {
                    if (connected.get()) {
                        plugin.getLogger().warning("连接异常: " + e.getMessage());
                        handleDisconnect();
                    }
                    break;
                } catch (IOException e) {
                    if (connected.get()) {
                        plugin.getLogger().warning("读取消息失败: " + e.getMessage());
                        handleDisconnect();
                    }
                    break;
                }
            }
        }, "McQqBridge-Receiver");
        
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    /**
     * 处理接收到的消息
     */
    private void processReceivedMessage(String rawMessage) {
        if (config.isVerbose()) {
            plugin.getLogger().info("[接收] " + rawMessage);
        }
        
        MessageProtocol.ParsedMessage message = MessageProtocol.parseMessage(rawMessage);
        if (message == null) {
            return;
        }
        
        // 在主线程中处理消息
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (message.isCommand()) {
                handleCommand(message.getContent());
            } else if (message.isQqMessage()) {
                handleQqMessage(message.getSender(), message.getContent());
            }
        });
    }

    /**
     * 处理远程命令
     */
    private void handleCommand(String command) {
        if (!config.isAllowRemoteCommand()) {
            if (config.isVerbose()) {
                plugin.getLogger().info("远程命令已禁用，忽略命令: " + command);
            }
            return;
        }
        
        plugin.getLogger().info("[QQ远程命令] " + command);
        
        try {
            // 使用控制台执行命令
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getLogger().warning("执行命令失败: " + e.getMessage());
        }
    }

    /**
     * 处理QQ消息
     */
    private void handleQqMessage(String sender, String content) {
        if (!config.isForwardQqToMc()) {
            return;
        }
        
        // 格式化消息
        String formattedMessage = config.getQqToMcFormat()
                .replace("{sender}", sender)
                .replace("{message}", content);
        
        // 广播到所有在线玩家
        Bukkit.broadcastMessage(formattedMessage);
    }

    /**
     * 处理断开连接
     */
    private void handleDisconnect() {
        if (!connected.getAndSet(false)) {
            return;
        }
        
        plugin.getLogger().warning("与 QQ 机器人服务器的连接已断开");
        closeConnection();
        scheduleReconnect();
    }

    /**
     * 安排重连任务
     */
    private void scheduleReconnect() {
        if (!shouldReconnect.get() || !config.isAutoReconnect()) {
            return;
        }
        
        int interval = config.getReconnectInterval();
        plugin.getLogger().info("将在 " + interval + " 秒后尝试重新连接...");
        
        reconnectTask = Bukkit.getScheduler().runTaskLater(plugin, this::connect, interval * 20L);
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * 获取连接状态描述
     */
    public String getStatusDescription() {
        if (connected.get()) {
            return "§a已连接";
        } else if (connecting.get()) {
            return "§e连接中...";
        } else {
            return "§c未连接";
        }
    }
}
