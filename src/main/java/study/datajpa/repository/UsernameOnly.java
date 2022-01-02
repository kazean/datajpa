package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
    /**
     * 단! 이렇게 SpEL문법을 사용하면, DB에서 엔티티 필드를 다 조회해온 다음에 계산한다! 따라서 JPQL
     * SELECT 절 최적화가 안된다
     * @Value("#{target.username+ ' ' + target.age + ' ' + target.team.name}")
     */
    String getUsername();
}
