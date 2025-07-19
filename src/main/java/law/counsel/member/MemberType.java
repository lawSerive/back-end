package law.counsel.member;

import lombok.Getter;

@Getter
public enum MemberType {
    USER("ROLE_USER",       "일반 사용자"),
    LAWYER("ROLE_LAWYER",   "법률 전문가"),
    COMPANY("ROLE_COMPANY", "기업 사용자"),
    ADMIN("ROLE_ADMIN",     "관리자");

    private final String key;
    private final String title;

    MemberType(String key, String title) {
        this.key   = key;
        this.title = title;
    }

    @Override
    public String toString() {
        return key;
    }
}