package top.yuany3721.BulletBotServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.yuany3721.BulletBotServer.util.BulletBuffer;

import java.util.Random;

/**
 * for test only
 */
@Controller
@RequestMapping("/")
@SuppressWarnings("unused")
public class BulletScreenController {

    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String get(){
        return "hello";
    }

    @ResponseBody
    @RequestMapping(value = "/newBullet", method = RequestMethod.GET)
    public void newBullet(@RequestParam("message") String message, @RequestParam("qq") String qq){
        BulletBuffer.getInstance().newBullet(message, Long.parseLong(qq));
    }

    @ResponseBody
    @RequestMapping(value = "/testBullet", method = RequestMethod.GET)
    public void testBullet(@RequestParam("message") String message, @RequestParam("count") String count, @RequestParam("qq") String qq){
        for (int i = 0; i < Integer.parseInt(count); i++){
            BulletBuffer.getInstance().newBullet(new Random().nextInt(10) < 5 ? message.substring(new Random().nextInt(message.length())) : message.substring(0, new Random().nextInt(message.length())), Long.parseLong(qq) + i);
        }
    }

}