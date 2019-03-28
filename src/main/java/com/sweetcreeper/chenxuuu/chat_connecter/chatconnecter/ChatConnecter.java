package com.sweetcreeper.chenxuuu.chat_connecter.chatconnecter;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(
        id = "chatconnecter",
        name = "ChatConnecter",
        version = "1.0.0",
        description = "connect with others"
)
public class ChatConnecter {

    @Inject
    private Logger logger;
    private boolean tabInit = false;

    @Listener//服务器启动
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("chat connector plugin start!");
    }

    @Listener//玩家登陆
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        if(!tabInit)
        {
            TabList tablist = player.getTabList();
            tablist.setHeader(Text.of(TextColors.DARK_GREEN, "糖拌苦力怕"));
            tablist.setFooter(Text.of(TextColors.LIGHT_PURPLE, "sweetcreeper.com"));
            tabInit = true;
        }
    }
    @Listener//玩家掉线
    public void onClientDisconnect(ClientConnectionEvent.Disconnect event)
    {
        Player player = event.getTargetEntity();
    }

    @Listener//玩家发消息
    public void onMessage(MessageEvent event)
    {
        String message = event.getMessage().toPlain();
    }
}
