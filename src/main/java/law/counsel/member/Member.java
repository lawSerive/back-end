package law.counsel.member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Column(name = "member_type")
    @Enumerated(EnumType.STRING)
    private MemberType memberType;


    @Builder
    public Member(Long memberId, String email, String password, String name, MemberType memberType) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.memberType = memberType;
    }
}