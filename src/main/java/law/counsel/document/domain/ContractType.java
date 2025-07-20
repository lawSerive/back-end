package law.counsel.document.domain;


import lombok.Getter;

@Getter
public enum ContractType {

    NDA     ("NDA"    , "비밀유지계약"),
    EMPLOY  ("EMPLOY" , "근로계약"),
    SUB     ("SUB"    , "하도급계약");

    private final String code;
    private final String koName;

    ContractType(String code, String koName) {
        this.code   = code;
        this.koName = koName;
    }

}
