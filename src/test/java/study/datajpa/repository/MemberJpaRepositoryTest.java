package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.domain.Member;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    EntityManager em;

    @Test
    public void testMember(){
        //given
        Member member = new Member("memberA");
        memberJpaRepository.save(member);
//        em.flush();
//        em.clear();

        //when
        Member findMember = memberJpaRepository.findById(member.getId());

        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
    }
}