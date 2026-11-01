package org.example.bookingservice.controller;

import org.example.bookingservice.client.HotelClient;
import org.example.bookingservice.entity.Booking;
import org.example.bookingservice.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    public BookingController(BookingRepository bookingRepository, HotelClient hotelClient) {
        this.bookingRepository = bookingRepository;
        this.hotelClient = hotelClient;
    }

    @PostMapping
    public Booking create(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long roomId = Long.valueOf(body.get("roomId").toString());
        LocalDate start = LocalDate.parse(body.get("startDate").toString());
        LocalDate end = LocalDate.parse(body.get("endDate").toString());
        String requestId = body.containsKey("requestId")
                ? body.get("requestId").toString()
                : UUID.randomUUID().toString();

        List<Booking> existing = bookingRepository.findByRequestId(requestId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(roomId);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setRequestId(requestId);
        booking = bookingRepository.save(booking);

        try {
            Boolean ok = hotelClient.confirmAvailability(roomId, start.toString(), end.toString(), requestId);
            if (Boolean.TRUE.equals(ok)) {
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
            } else {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
                try { hotelClient.release(roomId, requestId); } catch (Exception ignored) {}
                throw new ResponseStatusException(HttpStatus.CONFLICT, "room not available");
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            try { hotelClient.release(roomId, requestId); } catch (Exception ignored) {}
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "hotel-service unavailable");
        }

        return booking;
    }

    @GetMapping
    public List<Booking> myBookings(@RequestParam Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public Booking cancel(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        try { hotelClient.release(booking.getRoomId(), booking.getRequestId()); } catch (Exception ignored) {}
        return booking;
    }
}