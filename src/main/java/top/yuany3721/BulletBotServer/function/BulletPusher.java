package top.yuany3721.BulletBotServer.function;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import top.yuany3721.BulletBotServer.annotation.Function;
import top.yuany3721.BulletBotServer.function.basic.FunctionInterface;
import top.yuany3721.BulletBotServer.function.basic.MessageFunctionValidator;
import top.yuany3721.BulletBotServer.util.BulletBuffer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 弹幕推送
 */
@Function(name = "弹幕推送", usage = "开启功能/关闭功能 弹幕推送", needAdmin = true, close = true)
public class BulletPusher extends MessageFunctionValidator implements FunctionInterface {
    @Override
    protected void operate(Event event, Object message) {
        BulletBuffer.getInstance().newBullet(((PlainText)message).contentToString(), ((MessageEvent) event).getSender().getId());
    }

    @Deprecated
    private void sendBullet(Bot bot, String message, Long qq) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try{
            client = HttpClients.createDefault();
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8087/newBullet?" +
                    "message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) + "&" +
                    "qq=" + URLEncoder.encode(String.valueOf(qq), StandardCharsets.UTF_8));
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
                bot.getLogger().error("弹幕发送失败: " + message);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (response != null)
                response.close();
            if (client != null)
                client.close();
        }
    }
}
