package cn.hippo4j.starter.alarm;

import cn.hippo4j.common.model.InstanceInfo;
import cn.hippo4j.common.model.PoolParameterInfo;
import cn.hippo4j.starter.core.DynamicThreadPoolExecutor;
import cn.hippo4j.starter.core.GlobalThreadPoolManage;
import cn.hippo4j.starter.toolkit.thread.QueueTypeEnum;
import cn.hippo4j.starter.toolkit.thread.RejectedTypeEnum;
import cn.hippo4j.starter.wrapper.DynamicThreadPoolWrapper;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.google.common.base.Joiner;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Send ding notification message.
 *
 * @author chen.ma
 * @date 2021/8/15 15:49
 */
@Slf4j
@AllArgsConstructor
public class DingSendMessageHandler implements SendMessageHandler {

    private final String active;

    private final InstanceInfo instanceInfo;

    @Override
    public String getType() {
        return NotifyPlatformEnum.DING.name();
    }

    @Override
    public void sendAlarmMessage(NotifyDTO notifyConfig, DynamicThreadPoolExecutor pool) {
        dingAlarmSendMessage(notifyConfig, pool);
    }

    @Override
    public void sendChangeMessage(NotifyDTO notifyConfig, PoolParameterInfo parameter) {
        dingChangeSendMessage(notifyConfig, parameter);
    }

    private void dingAlarmSendMessage(NotifyDTO notifyConfig, DynamicThreadPoolExecutor pool) {
        List<String> receives = StrUtil.split(notifyConfig.getReceives(), ',');
        String afterReceives = Joiner.on(", @").join(receives);

        BlockingQueue<Runnable> queue = pool.getQueue();
        String text = String.format(
                "<font color='#FF0000'>[警报] </font>%s - 动态线程池运行告警 \n\n" +
                        " --- \n\n " +
                        "<font color='#708090' size=2>线程池ID：%s</font> \n\n " +
                        "<font color='#708090' size=2>应用名称：%s</font> \n\n " +
                        "<font color='#778899' size=2>应用实例：%s</font> \n\n " +
                        "<font color='#778899' size=2>报警类型：%s</font> \n\n " +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>核心线程数：%d</font> \n\n " +
                        "<font color='#708090' size=2>最大线程数：%d</font> \n\n " +
                        "<font color='#708090' size=2>当前线程数：%d</font> \n\n " +
                        "<font color='#708090' size=2>活跃线程数：%d</font> \n\n " +
                        "<font color='#708090' size=2>最大任务数：%d</font> \n\n " +
                        "<font color='#708090' size=2>线程池任务总量：%d</font> \n\n " +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>队列类型：%s</font> \n\n " +
                        "<font color='#708090' size=2>队列容量：%d</font> \n\n " +
                        "<font color='#708090' size=2>队列元素个数：%d</font> \n\n " +
                        "<font color='#708090' size=2>队列剩余个数：%d</font> \n\n " +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>拒绝策略：%s</font> \n\n" +
                        "<font color='#708090' size=2>拒绝策略执行次数：</font><font color='#FF0000' size=2>%d</font> \n\n " +
                        "<font color='#708090' size=2>OWNER：@%s</font> \n\n" +
                        "<font color='#708090' size=2>提示：%d 分钟内此线程池不会重复告警（可配置）</font> \n\n" +
                        " --- \n\n  " +
                        "**播报时间：%s**",

                // 环境
                active.toUpperCase(),
                // 线程池ID
                pool.getThreadPoolId(),
                // 应用名称
                instanceInfo.getAppName(),
                // 实例信息
                instanceInfo.getIdentify(),
                // 报警类型
                notifyConfig.getTypeEnum(),
                // 核心线程数
                pool.getCorePoolSize(),
                // 最大线程数
                pool.getMaximumPoolSize(),
                // 当前线程数
                pool.getPoolSize(),
                // 活跃线程数
                pool.getActiveCount(),
                // 最大任务数
                pool.getLargestPoolSize(),
                // 线程池任务总量
                pool.getCompletedTaskCount(),
                // 队列类型名称
                queue.getClass().getSimpleName(),
                // 队列容量
                queue.size() + queue.remainingCapacity(),
                // 队列元素个数
                queue.size(),
                // 队列剩余个数
                queue.remainingCapacity(),
                // 拒绝策略名称
                pool.getRejectedExecutionHandler().getClass().getSimpleName(),
                // 拒绝策略次数
                pool.getRejectCount(),
                // 告警手机号
                afterReceives,
                // 报警频率
                notifyConfig.getInterval(),
                // 当前时间
                DateUtil.now()
        );

        execute(notifyConfig, "动态线程池告警", text, receives);
    }

    private void dingChangeSendMessage(NotifyDTO notifyConfig, PoolParameterInfo parameter) {
        String threadPoolId = parameter.getTpId();
        DynamicThreadPoolWrapper poolWrap = GlobalThreadPoolManage.getExecutorService(threadPoolId);
        if (poolWrap == null) {
            log.warn("Thread pool is empty when sending change notification, threadPoolId :: {}", threadPoolId);
            return;
        }

        List<String> receives = StrUtil.split(notifyConfig.getReceives(), ',');
        String afterReceives = Joiner.on(", @").join(receives);

        ThreadPoolExecutor customPool = poolWrap.getExecutor();
        /**
         * hesitant e.g. ➲  ➜  ⇨  ➪
         */
        String text = String.format(
                "<font color='#2a9d8f'>[通知] </font>%s - 动态线程池参数变更 \n\n" +
                        " --- \n\n " +
                        "<font color='#708090' size=2>线程池ID：%s</font> \n\n " +
                        "<font color='#708090' size=2>应用名称：%s</font> \n\n " +
                        "<font color='#778899' size=2>应用实例：%s</font> \n\n " +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>核心线程数：%s</font> \n\n " +
                        "<font color='#708090' size=2>最大线程数：%s</font> \n\n " +
                        "<font color='#708090' size=2>线程存活时间：%s / SECONDS</font> \n\n" +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>队列类型：%s</font> \n\n " +
                        "<font color='#708090' size=2>队列容量：%s</font> \n\n " +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>AGO 拒绝策略：%s</font> \n\n" +
                        "<font color='#708090' size=2>NOW 拒绝策略：%s</font> \n\n" +
                        " --- \n\n  " +
                        "<font color='#708090' size=2>提示：动态线程池配置变更实时通知（无限制）</font> \n\n" +
                        "<font color='#708090' size=2>OWNER：@%s</font> \n\n" +
                        " --- \n\n  " +
                        "**播报时间：%s**",
                // 环境
                active.toUpperCase(),
                // 线程池名称
                threadPoolId,
                // 应用名称
                instanceInfo.getAppName(),
                // 实例信息
                instanceInfo.getIdentify(),
                // 核心线程数
                customPool.getCorePoolSize() + "  ➲  " + parameter.getCoreSize(),
                // 最大线程数
                customPool.getMaximumPoolSize() + "  ➲  " + parameter.getMaxSize(),
                // 线程存活时间
                customPool.getKeepAliveTime(TimeUnit.SECONDS) + "  ➲  " + parameter.getKeepAliveTime(),
                // 阻塞队列
                QueueTypeEnum.getBlockingQueueNameByType(parameter.getQueueType()),
                // 阻塞队列容量
                (customPool.getQueue().size() + customPool.getQueue().remainingCapacity()) + "  ➲  " + parameter.getCapacity(),
                // 拒绝策略
                customPool.getRejectedExecutionHandler().getClass().getSimpleName(),
                RejectedTypeEnum.getRejectedNameByType(parameter.getRejectedType()),
                // 告警手机号
                afterReceives,
                // 当前时间
                DateUtil.now()
        );

        execute(notifyConfig, "动态线程池通知", text, receives);
    }

    private void execute(NotifyDTO notifyConfig, String title, String text, List<String> mobiles) {
        String url = "https://oapi.dingtalk.com/robot/send?access_token=";
        String serverUrl = url + notifyConfig.getSecretKey();
        DingTalkClient dingTalkClient = new DefaultDingTalkClient(serverUrl);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");

        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtMobiles(mobiles);

        request.setAt(at);
        request.setMarkdown(markdown);

        try {
            dingTalkClient.execute(request);
        } catch (ApiException ex) {
            log.error("Ding failed to send message", ex);
        }
    }

}
