package com.mcqqbridge;

import com.mcqqbridge.command.BridgeCommand;
import com.mcqqbridge.listener.PlayerEventListener;
import com.mcqqbridge.network.TcpClient;
import com.mcqqbridge.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MC-QQ-Bridge 主插件类
 * 实现Minecraft服务器与QQ机器人的消息互通
 */
public class McQqBridge extends JavaPlugin {

    private static McQqBridge instance;
    private TcpClient tcpClient;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 加载配置
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // 初始化TCP客户端
        tcpClient = new TcpClient(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        
        // 注册命令
        BridgeCommand bridgeCommand = new BridgeCommand(this);
        getCommand("qqbridge").setExecutor(bridgeCommand);
        getCommand("qqbridge").setTabCompleter(bridgeCommand);
        
        // 启动TCP连接
        tcpClient.connect();
        
        // 服务器启动完成后发送通知
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (configManager.isSendServerStart() && tcpClient.isConnected()) {
                tcpClient.sendServerStart();
            }
        }, 20L); // 延迟1秒确保连接建立
        
        getLogger().info("MC-QQ-Bridge 插件已启动!");
        getLogger().info("正在连接到 " + configManager.getTcpHost() + ":" + configManager.getTcpPort());
    }

    @Override
    public void onDisable() {
        // 关闭TCP连接
        if (tcpClient != null) {
            tcpClient.disconnect();
        }
        
        getLogger().info("MC-QQ-Bridge 插件已关闭!");
    }

    /**
     * 获取插件实例
     */
    public static McQqBridge getInstance() {
        return instance;
    }

    /**
     * 获取TCP客户端
     */
    public TcpClient getTcpClient() {
        return tcpClient;
    }

    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 重载插件配置
     */
    public void reload() {
        reloadConfig();
        configManager.reload();
        getLogger().info("配置已重载!");
    }
}
