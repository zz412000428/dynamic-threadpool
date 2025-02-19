package cn.hippo4j.starter.common;

import cn.hippo4j.starter.core.DynamicThreadPoolExecutor;
import cn.hippo4j.starter.toolkit.thread.QueueTypeEnum;
import cn.hippo4j.starter.toolkit.thread.RejectedPolicies;
import cn.hippo4j.starter.toolkit.thread.ThreadPoolBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Common dynamic threadPool.
 *
 * @author chen.ma
 * @date 2021/6/16 22:35
 */
public class CommonDynamicThreadPool {

    public static DynamicThreadPoolExecutor getInstance(String threadPoolId) {
        DynamicThreadPoolExecutor poolExecutor = (DynamicThreadPoolExecutor) ThreadPoolBuilder.builder()
                .dynamicPool()
                .threadFactory(threadPoolId)
                .poolThreadSize(3, 5)
                .keepAliveTime(1000L, TimeUnit.SECONDS)
                .rejected(RejectedPolicies.runsOldestTaskPolicy())
                .alarmConfig(1, 80, 80)
                .workQueue(QueueTypeEnum.RESIZABLE_LINKED_BLOCKING_QUEUE, 512)
                .build();
        return poolExecutor;
    }

}
