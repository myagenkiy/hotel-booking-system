package org.example.hotelservice.repository;

import org.example.hotelservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAvailableTrueOrderByTimesBookedAscIdAsc();
    List<Room> findByAvailableTrue();
}