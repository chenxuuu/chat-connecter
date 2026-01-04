package com.mcqqbridge.config;

import com.mcqqbridge.McQqBridge;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 配置管理器
 * 负责读取和管理插件配置
 */
public class ConfigManager {

    private final McQqBridge plugin;
    
    // TCP配置
    private String tcpHost;
    private int tcpPort;
    private int reconnectInterval;
    private boolean autoReconnect;
    
    // 消息格式配置
    private String qqToMcFormat;
    private String mcToQqFormat;
    private String playerJoinFormat;
    private String playerQuitFormat;
    
    // 功能开关
    private boolean forwardChat;
    private boolean forwardJoinQuit;
    private boolean forwardQqToMc;
    private boolean allowRemoteCommand;
    private boolean sendServerStart;
    
    // 日志配置
    private boolean verbose;

    public ConfigManager(McQqBridge plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * 重载配置
     */
    public void reload() {
        FileConfiguration config = plugin.getConfig();
        
        // TCP配置
        tcpHost = config.getString("tcp.host", "127.0.0.1");
        tcpPort = config.getInt("tcp.port", 23333);
        reconnectInterval = config.getInt("tcp.reconnect-interval", 5);
        autoReconnect = config.getBoolean("tcp.auto-reconnect", true);
        
        // 消息格式配置
        qqToMcFormat = config.getString("message.qq-to-mc-format", "§b[QQ] §f{sender}§7: §r{message}");
        mcToQqFormat = config.getString("message.mc-to-qq-format", "{player}: {message}");
        playerJoinFormat = config.getString("message.player-join-format", "{player} 加入了服务器");
        playerQuitFormat = config.getString("message.player-quit-format", "{player} 离开了服务器");
        
        // 功能开关
        forwardChat = config.getBoolean("features.forward-chat", true);
        forwardJoinQuit = config.getBoolean("features.forward-join-quit", true);
        forwardQqToMc = config.getBoolean("features.forward-qq-to-mc", true);
        allowRemoteCommand = config.getBoolean("features.allow-remote-command", true);
        sendServerStart = config.getBoolean("features.send-server-start", true);
        
        // 日志配置
        verbose = config.getBoolean("logging.verbose", false);
    }

    // Getters

    public String getTcpHost() {
        return tcpHost;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public String getQqToMcFormat() {
        return qqToMcFormat;
    }

    public String getMcToQqFormat() {
        return mcToQqFormat;
    }

    public String getPlayerJoinFormat() {
        return playerJoinFormat;
    }

    public String getPlayerQuitFormat() {
        return playerQuitFormat;
    }

    public boolean isForwardChat() {
        return forwardChat;
    }

    public boolean isForwardJoinQuit() {
        return forwardJoinQuit;
    }

    public boolean isForwardQqToMc() {
        return forwardQqToMc;
    }

    public boolean isAllowRemoteCommand() {
        return allowRemoteCommand;
    }

    public boolean isSendServerStart() {
        return sendServerStart;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
