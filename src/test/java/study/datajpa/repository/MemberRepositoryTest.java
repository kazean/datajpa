package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.domain.Member;
import study.datajpa.domain.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

    @Test
    public void testMember(){
        //given
        Member member1 = new Member("member1");
        memberRepository.save(member1);
        //when
        Member findMember = memberRepository.findById(member1.getId()).get();



        //then
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void basicCRUD(){
//        given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

//        when then
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2L);
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0L);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan(){
//        given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

//        when
        List<Member> findMember = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

//        then
        assertThat(findMember.get(0).getUsername()).isEqualTo(m2.getUsername());
        assertThat(findMember.get(0).getAge()).isEqualTo(m2.getAge());
        assertThat(findMember.size()).isEqualTo(1);
    }

    @Test
    public void paging(){
//        given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        int age = 10;
        int offsetPage = 0;
        int limit = 3;
//        when
        PageRequest pageRequest = PageRequest.of(offsetPage, limit, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

//        then
        List<Member> content = page.getContent();
        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5L);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdate(){
//        given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",20));
        memberRepository.save(new Member("member3",30));
        memberRepository.save(new Member("member4",40));
        memberRepository.save(new Member("member5",50));

//        when
        int updateCount = memberRepository.bulkAgePlus(20);

//        then
        assertThat(updateCount).isEqualTo(4);
    }

    @Test
    public void findMemberLazy(){
//        given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1",10,teamA));
        memberRepository.save(new Member("member2",20,teamB));

//        when
        em.flush();
        em.clear();
        List<Member> members = memberRepository.findAll();

//        then
        members.stream()
                .forEach(m -> System.out.println("m.getTeam().getName() = " + m.getTeam().getName()));
    }

    @Test
    public void queryHint(){
//        given
        memberRepository.save(new Member("member1",10));
        em.flush();
        em.clear();

//        when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");

//        then
        em.flush();
    }
    
    @Test
    public void userCustomRepository(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",20));
        memberRepository.save(new Member("member3",30));
        memberRepository.save(new Member("member4",40));
        memberRepository.save(new Member("member5",50));

        List<Member> members = memberRepository.findMemberCustom();
        members.stream()
                .forEach(m -> System.out.println("m.getUsername() = " + m.getUsername()));
    }


    /**
     * #순수 JPA
     * @PrePersist @PreUpdate
     * @PostPersist @PostUpdate
     *
     * #Spring data
     * 스프링부트 @EnableJpaAuditing
     *
     * 엔티티 단위 설정 @EntityListener(AuditingEntityListener.class)
     *  > META-INF/orm.xml 전역설정법
     *  persistence-unit-metadata
     *      persistence-unit-default
     *          entity-listener class="org.springframework.data.jpa.domain.support.AuditingEntityListener”

     * @CreatedDate @LastModifiedDate
     * @CreatedBy @LastModifiedBy
     */
    @Test
    public void jpaEventBaseEntity() throws InterruptedException {
//        given
        Member member = new Member("member1");
        memberRepository.save(member);

        Thread.sleep(100);
        member.setUsername("member2");

        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(member.getId()).get();

//        then
        System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
        System.out.println("findMember.getUpdatedDate() = " + findMember.getUpdatedDate());
        System.out.println("findMember.getCreateBy() = " + findMember.getCreateBy());
        System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
    }

    @Test
    public void specBasic(){
//        given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1",0,teamA);
        Member m2 = new Member("m2",0,teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

        //when
        Specification<Member> spec =
                MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        //then
        assertThat(result.size()).isEqualTo(1);
    }


    @Test
    public void projections(){
//        given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1",0,teamA);
        Member m2 = new Member("m2",0,teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

//        when
//        List<UsernameOnly> usernames = memberRepository.findProjectionsByUsername("m1");
        List<UsernameOnlyClass> usernames = memberRepository.findProjectionsByUsername("m1", UsernameOnlyClass.class);

//        then
        assertThat(usernames.size()).isEqualTo(1);
    }

    @Test
    public void nestedProjection(){
//        given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1",0,teamA);
        Member m2 = new Member("m2",0,teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

//        when
        List<NestedCloseProjection> names = memberRepository.findProjectionsByUsername("m1", NestedCloseProjection.class);
        
//        then
        names.stream().forEach(n-> {
            System.out.println("n.getUsername() = " + n.getUsername());
            System.out.println("n.getTeam().getName() = " + n.getTeam().getName());
        });
    }

    @Test
    public void nativeQueryAndProjections(){
//        given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1",0,teamA);
        Member m2 = new Member("m2",0,teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

//        when
        Page<MemberProjection> result = memberRepository.findByNativeQuery(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username")));

//        then
        result.getContent().stream()
                .forEach(r->{
                    System.out.println("r.getId() = " + r.getId());
                    System.out.println("r.getUsername() = " + r.getUsername());
                    System.out.println("r.getTeamname() = " + r.getTeamname());
                });
    }
}