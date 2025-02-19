package cn.hippo4j.config.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * Config info base.
 *
 * @author chen.ma
 * @date 2021/6/20 14:05
 */
@Data
public class ConfigInfoBase implements Serializable {

    private static final long serialVersionUID = -1892597426099265730L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * tenantId
     */
    private String tenantId;

    /**
     * TpId
     */
    private String tpId;

    /**
     * ItemId
     */
    private String itemId;

    /**
     * coreSize
     */
    private Integer coreSize;

    /**
     * maxSize
     */
    private Integer maxSize;

    /**
     * queueType
     */
    private Integer queueType;

    /**
     * capacity
     */
    private Integer capacity;

    /**
     * keepAliveTime
     */
    private Integer keepAliveTime;

    /**
     * rejectedType
     */
    private Integer rejectedType;

    /**
     * isAlarm
     */
    private Integer isAlarm;

    /**
     * capacityAlarm
     */
    private Integer capacityAlarm;

    /**
     * livenessAlarm
     */
    private Integer livenessAlarm;

    /**
     * MD5
     */
    @JSONField(serialize = false)
    private String md5;

    /**
     * content
     */
    @JSONField(serialize = false)
    private String content;

}
