package com.meetflow.backend.repository;

import com.meetflow.backend.model.JoinRequest;
import com.meetflow.backend.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByStatus(RequestStatus status);
}
