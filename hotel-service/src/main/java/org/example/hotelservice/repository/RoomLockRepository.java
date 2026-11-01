package org.example.hotelservice.repository;

import org.example.hotelservice.entity.RoomLock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {
    List<RoomLock> findByRoomIdAndRequestId(Long roomId, String requestId);
}