package top.yuany3721.BulletBotServer.util;

import top.yuany3721.BulletBotServer.socket.WebSocket;

import javax.websocket.Session;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BulletBuffer {
    private static final BulletBuffer instance = new BulletBuffer();
    private static final Deque<Pair<Long, String>> buffer = new ArrayDeque<>();
    private static final AtomicInteger totalBullets = new AtomicInteger(0);
    // 最大弹幕数量
    private static final int MAX_BULLET_BUFFER_NUM = 150;
    // 弹幕最小推送间隔（ms）
    private static final int PUSH_SCHEDULE = 200;

    public static BulletBuffer getInstance() {
        return instance;
    }

    private BulletBuffer(){
        pushBullet();
    }

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS  ");

    /**
     * 新建弹幕
     * @param message 弹幕内容
     */
    public void newBullet(String message, Long qq){
        if (totalBullets.get() >= MAX_BULLET_BUFFER_NUM){
            // 缓存超限则将最早进入队列的弹幕丢弃
            buffer.removeLast();
            totalBullets.decrementAndGet();
        }
        // 新弹幕入队
        buffer.addFirst(new Pair<>(qq, message));
        totalBullets.incrementAndGet();
    }

    /**
     * 获取要推送的弹幕
     * @return bullet String
     */
    public Pair<Long, String> getBullet(){
        if (totalBullets.get() > 0){
            totalBullets.decrementAndGet();
            return buffer.removeFirst();
        }
        return new Pair<>(0L, "");
    }

    /**
     * 推送弹幕
     */
    private void pushBullet(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Pair<Long, String> bullet = getBullet();
                if (bullet.getKey().equals(0L))
                    return;
                if (WebSocket.onlineCount.get() == 0){
                    newBullet(bullet.getValue(), bullet.getKey());
                    return;
                }
                try {
                    System.out.println(format.format(new Date()) + "broadcast " + bullet);
                    broadcastBullet(bullet.getKey() + "+-+-+" + bullet.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, PUSH_SCHEDULE);
    }

    private void broadcastBullet(String bullet) throws IOException {
        // 广播消息
        for (Map.Entry<String, Session> sessionEntry : WebSocket.clients.entrySet()) {
            Session toSession = sessionEntry.getValue();
            toSession.getBasicRemote().sendText(bullet);
        }
    }
}
