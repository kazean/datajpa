package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.domain.Member;
import study.datajpa.domain.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    TeamJpaRepository teamJpaRepository;

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
        Member findMember = memberJpaRepository.findById(member.getId()).get();

        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
    }

    @Test
    public void basicCRUD(){
//        given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

//        when then
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2L);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deleteCount = memberJpaRepository.count();
        assertThat(deleteCount).isEqualTo(0L);
    }

    @Test
    public void paging(){
//        given
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",10));
        memberJpaRepository.save(new Member("member3",10));
        memberJpaRepository.save(new Member("member4",10));
        memberJpaRepository.save(new Member("member5",10));

        int age = 10;
        int offset = 0;
        int limit = 3;

//        when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

//        then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5L);
    }

    @Test
    public void bulkUpdate(){
//        given
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",20));
        memberJpaRepository.save(new Member("member3",30));
        memberJpaRepository.save(new Member("member4",40));
        memberJpaRepository.save(new Member("member5",50));

//        when
        int updateCount = memberJpaRepository.bulkAgePlus(20);

//        then
        assertThat(updateCount).isEqualTo(4);
    }

    @Test
    public void findMemberLazy(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);
        memberJpaRepository.save(new Member("member1",10,teamA));
        memberJpaRepository.save(new Member("member2",20,teamB));

        em.flush();
        em.clear();
        List<Member> members = memberJpaRepository.findAll();
        members.stream()
                .forEach(m -> System.out.println("m.getTeam().getName() = " + m.getTeam().getName()));
    }
}