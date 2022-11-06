package com.taka7646.rundeck.plugins.chatwork;

import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.Password;
import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Plugin(service="Notification",name="rundeck-chatwork-plugin")
@PluginDescription(title="rundeck-chatwork-plugin", description="My plugin description")
public class ChatworkNotificationPlugin implements NotificationPlugin{

    @PluginProperty(name = "baseUrl",title = "chatwork base url",description = "chatwork base url", defaultValue = "https://api.chatwork.com", scope=PropertyScope.Instance)
    private String baseUrl;
    @PluginProperty(name = "room", title = "ルームID", description = "チャットワーク ルームID", defaultValue = "", scope=PropertyScope.Instance)
    private String room;
    @PluginProperty(name = "apiToken", title = "APIトークン", description = "APIトークン", defaultValue = "", scope=PropertyScope.Instance)
    @Password
    private String apiToken;
    @PluginProperty(name = "message", title = "送信メッセージ", description = "送信メッセージ", defaultValue = "", scope=PropertyScope.Instance)
    private String message;

    public boolean postNotification(String trigger, Map executionData, Map config) {
        if ("".equals(this.message) || this.message == null) {
            return true;
        }
        this.send(this.createMessage(executionData, this.message));
        return true;
    }

    public String createMessage(Map executionData, String message)
    {
        message = message.replace("\\n", "\n");
        message = message.replace("<br>", "\n");
        message = message.replace("{STATUS}", (String)executionData.get("status"));
        message = message.replace("{JOB}", (String)executionData.get("project"));
        message = message.replace("{URL}", (String)executionData.get("href"));
        message = message.replace("{ID}", executionData.get("id").toString());
        message = message.replace("{USER}", (String)executionData.get("user"));

        return message;
    }

    public String send(String message)
    {
        try {
            URL url = new URL(this.baseUrl + "/v2/rooms/" + this.room + "/messages");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("X-ChatWorkToken", this.apiToken);
            con.setDoInput(true);
            con.setDoOutput(true);
            String payload = "body=";
            payload += URLEncoder.encode(message, "UTF-8");
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(payload);
            wr.flush();
            wr.close();

            InputStream input = con.getInputStream();
            String res = new Scanner(input,"UTF-8").useDelimiter("\\A").next();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
