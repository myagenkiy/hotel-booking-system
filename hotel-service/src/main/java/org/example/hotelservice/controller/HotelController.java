package org.example.hotelservice.controller;

import org.example.hotelservice.entity.Hotel;
import org.example.hotelservice.entity.Room;
import org.example.hotelservice.entity.RoomLock;
import org.example.hotelservice.repository.HotelRepository;
import org.example.hotelservice.repository.RoomLockRepository;
import org.example.hotelservice.repository.RoomRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HotelController {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;

    public HotelController(HotelRepository hotelRepository,
                           RoomRepository roomRepository,
                           RoomLockRepository roomLockRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomLockRepository = roomLockRepository;
    }

    @GetMapping("/hotels/ping")
    public String ping() { return "hotel-service is alive"; }

    @PostMapping("/hotels")
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @PostMapping("/rooms")
    public Room createRoom(@RequestBody Room room) {
        if (room.getAvailable() == null) room.setAvailable(true);
        if (room.getTimesBooked() == null) room.setTimesBooked(0);
        return roomRepository.save(room);
    }

    @GetMapping("/hotels")
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @GetMapping("/rooms")
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    @GetMapping("/rooms/recommend")
    public List<Room> recommendRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc();
    }

    @PostMapping("/rooms/{id}/confirm-availability")
    public Boolean confirmAvailability(@PathVariable Long id,
                                       @RequestParam String start,
                                       @RequestParam String end,
                                       @RequestParam String requestId) {
        List<RoomLock> existing = roomLockRepository.findByRoomIdAndRequestId(id, requestId);
        if (!existing.isEmpty()) {
            return true;
        }
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null || !Boolean.TRUE.equals(room.getAvailable())) {
            return false;
        }
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        List<RoomLock> locks = roomLockRepository.findAll();
        for (RoomLock lock : locks) {
            if (lock.getRoomId().equals(id)
                    && !lock.getEndDate().isBefore(s)
                    && !lock.getStartDate().isAfter(e)) {
                return false;
            }
        }
        RoomLock lock = new RoomLock();
        lock.setRoomId(id);
        lock.setStartDate(s);
        lock.setEndDate(e);
        lock.setRequestId(requestId);
        roomLockRepository.save(lock);

        // счётчик бронирований
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        return true;
    }

    @PostMapping("/rooms/{id}/release")
    public void release(@PathVariable Long id, @RequestParam String requestId) {
        List<RoomLock> locks = roomLockRepository.findByRoomIdAndRequestId(id, requestId);
        roomLockRepository.deleteAll(locks);
    }
}