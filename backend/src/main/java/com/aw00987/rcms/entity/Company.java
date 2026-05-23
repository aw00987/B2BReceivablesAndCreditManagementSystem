package com.aw00987.rcms.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Company {

    private Long id;

    private String companyCode;

    private String companyName;

    private String email;

    private String phoneNum;

    private String faxNum;

    private String address;

    /**
     * 信用格付け
     * 在日本，B2B 交易高度依赖第三方信用调查机构。需求文档中提到的 帝国 DataBank (TDB) 和 东京商工 Research (TSR) 是日本最权威的两家机构。
     * 具体含义： 它是对一家企业“破产风险”的量化评估。
     * 日本背景下的表现形式：
     * 评分制（Scoring）： TDB 和 TSR 通常会给出一个 0 到 100 的分数。
     * 50分以上： 被视为信用良好（通常是 A 或 B 级）。
     * 40-49分： 需警惕，属于“中等”或“观察”对象。
     * 40分以下： 高风险客户，通常会被要求预付货款或提供担保。
     * 等级制： 系统中定义的 length = 10 的 String 字段，通常存储的是类似 "A", "B1", "C2" 或者直接存储 TDB 的分数。
     * 业务作用：
     * 决定利率： 需求文档提到“契约利率模式 (高风险客户)”。系统会根据 creditRating 自动判断：如果评级低于某个阈值，逾期利息将从法定利率（3%）跳升至惩罚性的契约利率（14.6%）。
     * 审批流触发： 评级较低的客户在录入账单时，可能需要触发“稟議（Ringi）”审批流程。
     */
    private String creditRating;

    /**
     * 与信限度額
     * 这是日本“挂账销售（掛売 - Kakeuri）”文化中的核心红线。
     * 具体含义： 卖方允许买方持有的最大未付账款总额。
     * 日本背景下的业务逻辑：
     * 信用额度控制： 日本商社在与分销商交易前，会根据其 creditRating 设定一个额度。例如，某分销商的额度是 1000 万日元。
     * 动态占用： 只要该客户“所有未结清账单（売掛金残高）”的总和超过 1000 万，系统就应该阻止新的发货或录入新账单。
     * 精度要求： 代码中使用了 precision = 18, scale = 0 的 BigDecimal。这是因为日元（JPY）没有辅币单位（没有“分”的概念），所以 scale 为 0 是符合日本财务习惯的。
     * 业务作用：
     * 风险隔离： 防止单一客户因经营不善突然倒闭（连锁倒产）导致商社产生巨额坏账。
     * 预警机制： 当 creditLimit 接近饱和时，营业员（PIC）需要催促客户回款，以腾出额度进行下一笔交易。
     */
    private BigDecimal creditLimit;

    private String picUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
