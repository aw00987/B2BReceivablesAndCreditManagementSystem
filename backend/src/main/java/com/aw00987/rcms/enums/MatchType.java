package com.aw00987.rcms.enums;

/**
 * マッチングタイプ
 */
public enum MatchType {
    /**
     * 完全一致
     */
    EXACT,
    /**
     * 名義曖昧一致
     */
    NAME_FUZZY,
    /**
     * 金額差異一致
     */
    AMOUNT_VARIANCE,
    /**
     * 複合一致
     */
    COMBINED,
    /**
     * 手動消込
     */
    MANUAL
}
