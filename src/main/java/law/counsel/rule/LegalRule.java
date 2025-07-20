package law.counsel.rule;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class LegalRule {
    private final String keyword;
    private final String lawName;
    private final String article;

    public LegalRule(String keyword, String lawName, String article) {
        this.keyword = keyword;
        this.lawName = lawName;
        this.article = article;
    }

    public static final List<LegalRule> RULES = Arrays.asList(
            new LegalRule("손해배상", "민법", "제390조"),
            new LegalRule("비밀유지", "부정경쟁방지 및 영업비밀보호에 관한 법률", "제2조"),
            new LegalRule("계약의 해제", "민법", "제543조"),
            new LegalRule("불가항력", "민법", "제390조"),
            new LegalRule("양도", "민법", "제449조"),
            new LegalRule("관할", "민사소송법", "제2조")
    );

    public static List<String> findLegalReferences(String text) {
        return RULES.stream()
                .filter(rule -> text.contains(rule.getKeyword()))
                .map(rule -> String.format("%s %s", rule.getLawName(), rule.getArticle()))
                .collect(Collectors.toList());
    }
}