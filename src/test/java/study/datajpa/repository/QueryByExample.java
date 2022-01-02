package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.domain.Member;
import study.datajpa.domain.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QueryByExample {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    /**
     * Probe: 필드에 데이터가 있는 실제 도메인 객체
     * ExampleMatcher: 특정 필드를 일치시키는 상세한 정보 제공, 재사용 가능
     * Example: Probe와 ExampleMatcher로 구성, 쿼리를 생성하는데 사용
     *
     * 장점
     * 동적 쿼리를 편리하게 처리
     * 도메인 객체를 그대로 사용
     * 데이터 저장소를 RDB에서 NOSQL로 변경해도 코드 변경이 없게 추상화 되어 있음
     * 스프링 데이터 JPA JpaRepository 인터페이스에 이미 포함
     *
     * 단점
     * 조인은 가능하지만 내부 조인(INNER JOIN)만 가능함 외부 조인(LEFT JOIN) 안됨
     * 다음과 같은 중첩 제약조건 안됨
     * firstname = ?0 or (firstname = ?1 and lastname = ?2)
     * 매칭 조건이 매우 단순함
     * 문자는 starts/contains/ends/regex
     * 다른 속성은 정확한 매칭( = )만 지원
     *
     * 정리
     * 실무에서 사용하기에는 매칭 조건이 너무 단순하고, LEFT 조인이 안됨
     * 실무에서는 QueryDSL을 사용하자
     *
     */
    @Test
    public void basic() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();

        //when
        //Probe 생성
        Member member = new Member("m1");
        Team team = new Team("teamA"); //내부조인으로 teamA 가능
        member.setTeam(team);

        //ExampleMatcher 생성, age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);

        //then
        assertThat(result.size()).isEqualTo(1);
    }
}
