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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

@Plugin(
        id = "chatconnecter",
        name = "ChatConnecter",
        version = "1.0.0",
        description = "connect with others"
)
public class ChatConnecter {

    @Inject
    private Logger logger;

    @Listener//服务器启动
    public void onServerStart(GameStartedServerEvent event) {
        StartServer();
        logger.info("chat connector plugin start!");
    }

    @Listener//玩家登陆
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        SendTcp("l"+player.getName());
        TabList tablist = player.getTabList();
        tablist.setHeader(Text.of(TextColors.DARK_GREEN, "糖拌苦力怕"));
        tablist.setFooter(Text.of(TextColors.LIGHT_PURPLE, "sweetcreeper.com"));
    }
    @Listener//玩家掉线
    public void onClientDisconnect(ClientConnectionEvent.Disconnect event)
    {
        Player player = event.getTargetEntity();
        SendTcp("d"+player.getName());
    }

    @Listener//玩家发消息
    public void onMessage(MessageEvent event)
    {
        String message = event.getMessage().toPlain();
        if(message.indexOf("<") == 0 || message.indexOf("[") == 0)
            SendTcp("m"+message);
    }



    private Client tcpClient = null;
    public void StartServer()
    {
        logger.info("starting tcp client");
        tcpClient = new Client();
        tcpClient.new SocketThread().start();
        logger.info("tcp client started!");
    }
    public void SendTcp(String msg)
    {
        try{
            tcpClient.os.write(msg.getBytes("utf8"));
            tcpClient.os.flush();
        }
        catch (Exception e)
        {
            logger.info("tcp send failed!"+msg);
            //e.printStackTrace();
            System.out.println("heartbeat send error");
            try {
                tcpClient.socket.close();
                tcpClient.is.close();
                tcpClient.os.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

class Client {
    public Socket socket = null;
    public OutputStream os = null;
    public InputStream is = null;
    private boolean clear = false;

    /**
     * 发送心跳包
     */
    public void sendHeartbeat() {
        try {
            String heartbeat = "h";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(120 * 1000);// 120s发送一次心跳
                            os.write(heartbeat.getBytes());
                            os.flush();
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.out.println("heartbeat send error");
                            try {
                                socket.close();
                                is.close();
                                os.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SocketThread extends Thread {
        @Override
        public void run() {
            sendHeartbeat();
            while (true) {
                try {
                    if (socket == null || socket.isClosed()) {
                        socket = new Socket("localhost", 23333); // 连接socket
                        os = socket.getOutputStream();
                        System.out.println("socket connected");
                    }
                    Thread.sleep(100);
                    if(!clear)
                    {
                        os.write(("c").getBytes());
                        os.flush();
                        clear = true;
                        System.out.println("socket server start clear all");
                    }
                    is = socket.getInputStream();
                    int size = is.available();
                    if(size <= 0)
                        continue;
                    byte[] resp = new byte[size];
                    is.read(resp);
                    String response = new String(resp,"utf8");
                    try{
                        MessageChannel.TO_ALL.send(Text.of(response));
                    }
                    catch (Exception e){

                    }
                    //Logger.info("receive data:" + response);
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("socket error");
                    try {
                        socket.close();
                        is.close();
                        os.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
