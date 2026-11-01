package org.example.bookingservice.controller;

import org.example.bookingservice.entity.Booking;
import org.example.bookingservice.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingsController {

    private final BookingRepository bookingRepository;

    public BookingsController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<Booking> myBookings(@RequestParam Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}