package cn.hippo4j.starter.config;

import cn.hippo4j.common.model.InstanceInfo;
import cn.hippo4j.starter.alarm.*;
import cn.hippo4j.starter.alarm.lark.LarkSendMessageHandler;
import cn.hippo4j.starter.remote.HttpAgent;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Message alarm config.
 *
 * @author chen.ma
 * @date 2021/8/15 15:39
 */
@AllArgsConstructor
public class MessageAlarmConfig {

    private final BootstrapProperties properties;

    private final InstanceInfo instanceInfo;

    private ConfigurableEnvironment environment;

    public static final String SEND_MESSAGE_BEAN_NAME = "hippo4JSendMessageService";

    @DependsOn("hippo4JApplicationContextHolder")
    @Bean(MessageAlarmConfig.SEND_MESSAGE_BEAN_NAME)
    public SendMessageService hippo4JSendMessageService(HttpAgent httpAgent, AlarmControlHandler alarmControlHandler) {
        return new BaseSendMessageService(httpAgent, properties, alarmControlHandler);
    }

    @Bean
    public SendMessageHandler dingSendMessageHandler() {
        String active = environment.getProperty("spring.profiles.active", Strings.EMPTY);
        return new DingSendMessageHandler(active, instanceInfo);
    }

    @Bean
    public SendMessageHandler larkSendMessageHandler() {
        String active = environment.getProperty("spring.profiles.active", Strings.EMPTY);
        return new LarkSendMessageHandler(active, instanceInfo);
    }

    @Bean
    public AlarmControlHandler alarmControlHandler() {
        return new AlarmControlHandler();
    }

}
