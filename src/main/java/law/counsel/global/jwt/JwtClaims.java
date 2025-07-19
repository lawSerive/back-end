package law.counsel.global.jwt;

import law.counsel.member.Member;
import law.counsel.member.MemberType;


public record JwtClaims(
        Long memberId,
        MemberType memberType
) {
    public static JwtClaims create(Member member) {
        return new JwtClaims(member.getMemberId(), member.getMemberType());
    }
}