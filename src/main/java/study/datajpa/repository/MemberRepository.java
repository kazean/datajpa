package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.domain.Member;
import study.datajpa.dto.MemberDto;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * @Query 생략가능, 메서드이름만으로 @Named쿼리 호출가능
     * Spring data JPA 도메인 . 메서드이름으로 먼저 찾고 없으면
     * 메서드 이름 생성 전략 사용
     * List<Member> findByUsername(@Param("username") String username);
     */
//

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

//    단순히 값 하나 조회(값 타입 조회)
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id,m.username,t.name) from Member m join Team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username = :username")
    Member findMembers(@Param("username") String username);

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    /**
     * Spring data jpa 유연한 반환타입 지원
     * return collection > empty()
     * return Object > 단건, null, NonUniqueResultException
     */
    List<Member> findByUsername(String username);
//    Member findByUsername(String username);
//    Optional<Member> findByUsername(String username);

    /**
     * paging
     * org.springframework.data.domain Pageable Interface
     * org.springframework.data.domain.PageRequest 구현체 (int page, int size, Sort sort)
     * page = 0 시작
     *
     * cf,
     * List<Member> findTop3By();
     * Slice는 Limit+1을 조회 무한스크롤링 페이지 구현시 다음페이지 있는지 확인여부
     *
     *
     */
    Page<Member> findByAge(int age, Pageable pageable);
//    Page<Member> findByUsername(String name, Pageable pageable); //count query o
//    Slice<Member> findByUsernmae(String name, Pageable pageable); // x
//    List<Member> findByUsername(String name, Pageable pageable); // x
//    List<Member> findByUsername(String name, Sort sort);

    /**
     * countQuery 분리가능, 최적화 안될시에 최적화
     */
    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);


    /**
     * bulk update
     * org.springframework.data.jpa.repsitory
     * @Modifying 어노테이션 사용
     * 벌크성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화: @Modifying(clearAutomatically = true)
     * (이 옵션의 기본값은 false)
     * > 이 옵션 없이 회원을 findById 로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수
     * 있다. 만약 다시 조회해야 하면 꼭 영속성 컨텍스트를 초기화 하자.
     *
     * 참고: 벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와
     * DB에 엔티티 상태가 달라질 수 있다.
     * > 권장하는 방안
     * > 1. 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.
     * > 2. 부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화 한다.
     */
    @Modifying
    @Query("update Member m set m.age = m.age +1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    /**
     * @EntityGraph(attributePaths)
     * 사실상 패치조인
     * Left Outer Join 사용
     */
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    /*
    @EntityGraph(attributePaths = {"team"})
    List<Member> findByUsername(String name);
    */

    /**
     * JPA Hint & Lock
     * @QueryHints(value = @QueryHint(name="org.hibernate.readOnly" value="true))
     *
     * paging 추가예제
     * @QueryHints(value = {@QueryHint(name="org.hibernate.readOnly" value="true)}, forCounting = true)
     * Page<Member> findByUsername(String name, Pagable pageable);
     * org.springframework.data.jpa.repository.QueryHints 어노테이션을 사용
     * forCounting : 반환 타입으로 Page 인터페이스를 적용하면 추가로 호출하는 페이징을 위한 count
     * 쿼리도 쿼리 힌트 적용(기본값 true )
     *
     * Lock
     * @Lock(LockModeType.PESSIMISTIC_WRITE)
     * select for update?
     */
    @QueryHints(value = @QueryHint(name="org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    /*
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findByUsername(String username);
    */

    <T> List<T> findProjectionsByUsername(String username, Class<T> type);

    /**
     * JPQL은 위치 기반 파리미터를 1부터 시작하지만 네이티브 SQL은 0부터 시작
     * 네이티브 SQL을 엔티티가 아닌 DTO로 변환은 하려면
     *
     * DTO 대신 JPA TUPLE 조회
     * DTO 대신 MAP 조회
     * @SqlResultSetMapping 복잡
     * Hibernate ResultTransformer를 사용해야함 복잡
     * https://vladmihalcea.com/the-best-way-to-map-a-projection-query-to-a-dto-with-jpaand-
     * hibernate/
     * 네이티브 SQL을 DTO로 조회할 때는 JdbcTemplate or myBatis 권장
     */
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    @Query(value = "select m.member_id as id, m.username, t.name as teamName from member m" +
            " left join team t", nativeQuery = true, countQuery = "select count(*) from member")
    Page<MemberProjection> findByNativeQuery(Pageable pageable);

}
