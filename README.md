# MC-QQ-Bridge

一个用于 Minecraft 服务器与 QQ 机器人消息互通的 PaperSpigot 插件。

## 功能特性

- 🔗 **TCP 长连接通信**：与 QQ 机器人建立稳定的 TCP 连接
- 💬 **消息互通**：
  - MC 游戏内聊天消息转发到 QQ 群
  - QQ 群消息转发到 MC 游戏内
- 👤 **玩家状态通知**：
  - 玩家上线/下线通知发送到 QQ 群
- 🎮 **远程命令执行**：通过 QQ 群执行服务器命令
- 🔄 **自动重连**：断线后自动重新连接
- ⚙️ **高度可配置**：支持自定义消息格式和功能开关

## 安装

1. 下载插件 JAR 文件
2. 将 JAR 文件放入服务器的 `plugins` 目录
3. 启动服务器，插件会自动生成配置文件
4. 编辑 `plugins/MC-QQ-Bridge/config.yml` 配置 TCP 服务器地址
5. 使用 `/qqbridge reload` 重载配置或重启服务器

## 编译

```bash
cd mc-qq-bridge
mvn clean package
```

编译后的 JAR 文件位于 `target/mc-qq-bridge-1.0.0.jar`

## 配置说明

```yaml
# TCP 连接配置
tcp:
  host: "127.0.0.1"        # QQ机器人TCP服务器地址
  port: 25580              # QQ机器人TCP服务器端口
  reconnect-interval: 5    # 重连间隔（秒）
  auto-reconnect: true     # 是否自动重连

# 消息格式配置
message:
  qq-to-mc-format: "§b[QQ] §f{sender}§7: §r{message}"
  mc-to-qq-format: "{player}: {message}"
  player-join-format: "{player} 加入了服务器"
  player-quit-format: "{player} 离开了服务器"

# 功能开关
features:
  forward-chat: true          # 转发MC聊天到QQ
  forward-join-quit: true     # 转发玩家上下线
  forward-qq-to-mc: true      # 转发QQ消息到MC
  allow-remote-command: true  # 允许远程命令
  send-server-start: true     # 发送服务器启动通知
```

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/qqbridge reload` | `mcqqbridge.admin` | 重载配置文件 |
| `/qqbridge status` | `mcqqbridge.command` | 查看连接状态 |
| `/qqbridge reconnect` | `mcqqbridge.admin` | 重新连接服务器 |
| `/qqbridge help` | `mcqqbridge.command` | 显示帮助信息 |

命令别名：`/mqb`、`/qq`

## 权限

| 权限节点 | 默认 | 说明 |
|----------|------|------|
| `mcqqbridge.admin` | OP | 管理插件（重载、重连） |
| `mcqqbridge.command` | OP | 使用基础命令 |

## 通信协议

详见 [PROTOCOL.md](PROTOCOL.md)

### MC → QQ 消息格式

| 类型 | 格式 | 说明 |
|------|------|------|
| 玩家上线 | `l<玩家名>\|` | 发送玩家登录通知 |
| 玩家下线 | `d<玩家名>\|` | 发送玩家退出通知 |
| 聊天消息 | `m<消息内容>\|` | 转发游戏内聊天 |
| 服务器启动 | `c\|` | 服务器启动完成通知 |

### QQ → MC 消息格式

| 格式 | 说明 |
|------|------|
| `cmd<命令>` | 执行服务器命令 |
| `名字:消息` | 转发 QQ 群消息 |

## 系统要求

- Minecraft 服务器：PaperSpigot 1.21.1+
- Java 版本：Java 21+

## 许可证

MIT License
