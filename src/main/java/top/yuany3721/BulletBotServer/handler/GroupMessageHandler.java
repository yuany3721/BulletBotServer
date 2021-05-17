package top.yuany3721.BulletBotServer.handler;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import top.yuany3721.BulletBotServer.buffer.MessageBuffer;
import top.yuany3721.BulletBotServer.function.*;
import top.yuany3721.BulletBotServer.util.BaseProperties;

/**
 * Group Message Handler
 */
public class GroupMessageHandler extends SimpleListenerHost {

    @EventHandler
    public void onMessage(@NotNull MessageEvent event) {
        // 疯狂的复读机
        new CrazyRepeater().execute(event, event.getMessage());
        // Mirai MessageChain
        MessageChain messageChain = event.getMessage();
        MessageBuffer.getInstance().newMessageBuffer(messageChain, event.getSubject().getId());
        // 复读机
        if (MessageBuffer.getInstance().getRepeat(event.getSubject().getId()))
            new Repeater().execute(event, MessageBuffer.getInstance().getRepeatMessageChain(event.getSubject().getId()));
        // 纯文本
        PlainText plainText = (PlainText) messageChain.stream().filter(PlainText.class::isInstance).findFirst().orElse(null);
        if (plainText != null) {
            String frontMessage = plainText.contentToString().split("[ +]")[0];
            if (frontMessage.contains("功能") && frontMessage.length() < 5)
                new FunctionSwitch().execute(event, plainText); // 功能管理
            if (frontMessage.contains("禁言") && frontMessage.length() < 5)
                new Mute().execute(event, messageChain); // 禁言管理
            else if (plainText.contentToString().contains(BaseProperties.botAlias) && plainText.contentToString().contains("在"))
                new Hello().execute(event, plainText); // Hello
            else
                new BulletPusher().execute(event, plainText); // 弹幕推送
        }
    }
}
