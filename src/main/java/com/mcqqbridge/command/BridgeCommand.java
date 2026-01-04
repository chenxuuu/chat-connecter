package com.mcqqbridge.command;

import com.mcqqbridge.McQqBridge;
import com.mcqqbridge.config.ConfigManager;
import com.mcqqbridge.network.TcpClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件主命令处理器
 */
public class BridgeCommand implements CommandExecutor, TabCompleter {

    private final McQqBridge plugin;
    private final List<String> subCommands = Arrays.asList("reload", "status", "reconnect", "help");

    public BridgeCommand(McQqBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("mcqqbridge.command")) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "reconnect":
                handleReconnect(sender);
                break;
            case "help":
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * 重载配置
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mcqqbridge.admin")) {
            sender.sendMessage("§c你没有权限执行此操作!");
            return;
        }

        plugin.reload();
        sender.sendMessage("§a[MC-QQ-Bridge] 配置已重载!");
    }

    /**
     * 显示状态
     */
    private void handleStatus(CommandSender sender) {
        TcpClient tcpClient = plugin.getTcpClient();
        ConfigManager config = plugin.getConfigManager();

        sender.sendMessage("§6========== MC-QQ-Bridge 状态 ==========");
        sender.sendMessage("§7连接状态: " + tcpClient.getStatusDescription());
        sender.sendMessage("§7服务器地址: §f" + config.getTcpHost() + ":" + config.getTcpPort());
        sender.sendMessage("§7自动重连: " + (config.isAutoReconnect() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§7转发聊天: " + (config.isForwardChat() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§7转发上下线: " + (config.isForwardJoinQuit() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§7转发QQ消息: " + (config.isForwardQqToMc() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§7远程命令: " + (config.isAllowRemoteCommand() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§6=========================================");
    }

    /**
     * 重新连接
     */
    private void handleReconnect(CommandSender sender) {
        if (!sender.hasPermission("mcqqbridge.admin")) {
            sender.sendMessage("§c你没有权限执行此操作!");
            return;
        }

        sender.sendMessage("§e[MC-QQ-Bridge] 正在重新连接...");
        plugin.getTcpClient().reconnect();
        sender.sendMessage("§a[MC-QQ-Bridge] 重连请求已发送!");
    }

    /**
     * 显示帮助
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6========== MC-QQ-Bridge 帮助 ==========");
        sender.sendMessage("§e/qqbridge reload §7- 重载配置文件");
        sender.sendMessage("§e/qqbridge status §7- 查看连接状态");
        sender.sendMessage("§e/qqbridge reconnect §7- 重新连接服务器");
        sender.sendMessage("§e/qqbridge help §7- 显示帮助信息");
        sender.sendMessage("§7别名: /mqb, /qq");
        sender.sendMessage("§6=========================================");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
