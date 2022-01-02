package study.datajpa.repository;

public interface NestedCloseProjection {
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
