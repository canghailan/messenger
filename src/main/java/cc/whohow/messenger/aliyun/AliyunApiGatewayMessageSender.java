package cc.whohow.messenger.aliyun;

import cc.whohow.messenger.Message;

import java.util.function.Consumer;

public class AliyunApiGatewayMessageSender implements Consumer<Message> {
    private final String deviceId;

    public AliyunApiGatewayMessageSender(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void accept(Message message) {

    }
}
