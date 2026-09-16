package com.meetflow.backend.repository;

import com.meetflow.backend.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    Optional<MeetingRoom> findByCode(String code);
    List<MeetingRoom> findByIsLiveTrue();
}
