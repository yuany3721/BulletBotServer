package top.yuany3721.BulletBotServer.socket;

import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;
import org.springframework.stereotype.Component;
import top.yuany3721.BulletBotServer.Application;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@ServerEndpoint(value = "/connect")
@Component
public class WebSocket {

    // 当前在线的客户端
    public static final Map<String, Session> clients = new ConcurrentHashMap<>();
    // 当前在线连接数
    public static final AtomicInteger onlineCount = new AtomicInteger(0);
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS  ");

    @OnOpen
    public void onOpen(Session session) {
        onlineCount.incrementAndGet();
        clients.put(session.getId(), session);
        System.out.println(format.format(new Date()) + "new connect established：" + session + "\n当前连接数：" + onlineCount.get());
        try {
            session.getBasicRemote().sendText(session.toString().split("@")[1] + "+-+-+成功连接弹幕服务器，这是一条测试弹幕");
        } catch (Exception e) {
            System.err.println("连接确认消息发送失败");
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        onlineCount.decrementAndGet(); // 在线数减1
        clients.remove(session.getId());
        System.out.println(format.format(new Date()) + session + " closed\n剩余连接数：" + onlineCount.get());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message.contains("lottery:")) {
            long qq = Long.parseLong(message.split("lottery:")[1]);
            boolean flag = false;
            for (NormalMember member : Objects.requireNonNull(Application.bot.getGroup(1058176965)).getMembers()){
                if (member.getId() == qq) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Objects.requireNonNull(Application.bot.getGroup(1058176965)).sendMessage(qq + "好像不在群里，这次抽奖无效哦~");
                sendMessage("lottery:invalid#" + qq);
                return;
            }
            try {
                Objects.requireNonNull(Application.bot.getGroup(1058176965)).sendMessage(new At(qq).plus(new PlainText("恭喜中奖！")));
                sendMessage("lottery:valid#" + qq);
            } catch (Exception e) {
                Objects.requireNonNull(Application.bot.getGroup(1058176965)).sendMessage("蠢蠢的Bot好像出了点问题，这次抽奖以人工记录为准好了~");
            }
        }
        else
            System.out.println(format.format(new Date()) + "received from(" + session + "):" + message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println(format.format(new Date()) + "WebSocket Error");
        error.printStackTrace();
    }

    /**
     * 广播消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
            Session toSession = sessionEntry.getValue();
            toSession.getAsyncRemote().sendText(message);
        }
    }

}