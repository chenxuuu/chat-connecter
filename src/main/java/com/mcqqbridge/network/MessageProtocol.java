package com.mcqqbridge.network;

/**
 * 消息协议工具类
 * 负责消息的转义和解析
 */
public class MessageProtocol {

    // 消息类型标识符
    public static final char TYPE_LOGIN = 'l';      // 玩家上线
    public static final char TYPE_DISCONNECT = 'd'; // 玩家下线
    public static final char TYPE_MESSAGE = 'm';    // 聊天消息
    public static final char TYPE_SERVER_START = 'c'; // 服务器启动

    // 命令前缀
    public static final String CMD_PREFIX = "cmd";

    // 消息分隔符
    public static final String SEPARATOR = "|";

    /**
     * 转义消息内容
     * 将 \ 转义为 \\，将 | 转义为 \s
     */
    public static String escape(String content) {
        if (content == null) {
            return "";
        }
        return content
                .replace("\\", "\\\\")
                .replace("|", "\\s");
    }

    /**
     * 解码转义的消息内容
     * 先将 \\ 还原为 \，再将 \s 还原为 |
     */
    public static String unescape(String content) {
        if (content == null) {
            return "";
        }
        return content
                .replace("\\\\", "\u0000") // 临时替换，避免干扰
                .replace("\\s", "|")
                .replace("\u0000", "\\");
    }

    /**
     * 构建玩家登录消息
     */
    public static String buildLoginMessage(String playerName) {
        return TYPE_LOGIN + escape(playerName) + SEPARATOR;
    }

    /**
     * 构建玩家下线消息
     */
    public static String buildDisconnectMessage(String playerName) {
        return TYPE_DISCONNECT + escape(playerName) + SEPARATOR;
    }

    /**
     * 构建聊天消息
     */
    public static String buildChatMessage(String content) {
        return TYPE_MESSAGE + escape(content) + SEPARATOR;
    }

    /**
     * 构建服务器启动消息
     */
    public static String buildServerStartMessage() {
        return TYPE_SERVER_START + SEPARATOR;
    }

    /**
     * 解析接收到的消息
     * 返回解析后的消息对象
     */
    public static ParsedMessage parseMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return null;
        }

        // 移除可能的换行符
        rawMessage = rawMessage.replace("\r", "").replace("\n", "");

        // 检查是否是命令
        if (rawMessage.startsWith(CMD_PREFIX)) {
            String command = rawMessage.substring(CMD_PREFIX.length());
            return new ParsedMessage(MessageType.COMMAND, unescape(command));
        }

        // 否则当作QQ消息处理（格式：名字:消息）
        int colonIndex = rawMessage.indexOf(":");
        if (colonIndex > 0) {
            String sender = rawMessage.substring(0, colonIndex);
            String content = rawMessage.substring(colonIndex + 1);
            return new ParsedMessage(MessageType.QQ_MESSAGE, unescape(sender), unescape(content));
        }

        // 无法解析的消息，当作普通消息
        return new ParsedMessage(MessageType.QQ_MESSAGE, "QQ", unescape(rawMessage));
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        COMMAND,    // 服务器命令
        QQ_MESSAGE  // QQ消息
    }

    /**
     * 解析后的消息对象
     */
    public static class ParsedMessage {
        private final MessageType type;
        private final String sender;
        private final String content;

        public ParsedMessage(MessageType type, String content) {
            this(type, null, content);
        }

        public ParsedMessage(MessageType type, String sender, String content) {
            this.type = type;
            this.sender = sender;
            this.content = content;
        }

        public MessageType getType() {
            return type;
        }

        public String getSender() {
            return sender;
        }

        public String getContent() {
            return content;
        }

        public boolean isCommand() {
            return type == MessageType.COMMAND;
        }

        public boolean isQqMessage() {
            return type == MessageType.QQ_MESSAGE;
        }
    }
}
