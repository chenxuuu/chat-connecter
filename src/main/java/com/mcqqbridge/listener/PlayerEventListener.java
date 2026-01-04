package com.mcqqbridge.listener;

import com.mcqqbridge.McQqBridge;
import com.mcqqbridge.config.ConfigManager;
import com.mcqqbridge.network.TcpClient;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 监听玩家的登录、退出和聊天事件
 */
public class PlayerEventListener implements Listener {

    private final McQqBridge plugin;
    private final TcpClient tcpClient;
    private final ConfigManager config;

    public PlayerEventListener(McQqBridge plugin) {
        this.plugin = plugin;
        this.tcpClient = plugin.getTcpClient();
        this.config = plugin.getConfigManager();
    }

    /**
     * 玩家加入服务器事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isForwardJoinQuit()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        // 发送玩家上线消息
        tcpClient.sendPlayerLogin(playerName);

        if (config.isVerbose()) {
            plugin.getLogger().info("已发送玩家上线消息: " + playerName);
        }
    }

    /**
     * 玩家退出服务器事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!config.isForwardJoinQuit()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        // 发送玩家下线消息
        tcpClient.sendPlayerDisconnect(playerName);

        if (config.isVerbose()) {
            plugin.getLogger().info("已发送玩家下线消息: " + playerName);
        }
    }

    /**
     * 玩家聊天事件 (Paper API)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!config.isForwardChat()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // 使用 PlainTextComponentSerializer 将 Adventure Component 转换为纯文本
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 发送聊天消息
        tcpClient.sendChatMessage(playerName, message);

        if (config.isVerbose()) {
            plugin.getLogger().info("已发送聊天消息: " + playerName + ": " + message);
        }
    }
}
