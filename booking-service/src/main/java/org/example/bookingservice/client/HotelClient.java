package org.example.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @PostMapping("/api/rooms/{id}/confirm-availability")
    Boolean confirmAvailability(@PathVariable Long id,
                                @RequestParam String start,
                                @RequestParam String end,
                                @RequestParam String requestId);

    @PostMapping("/api/rooms/{id}/release")
    void release(@PathVariable Long id, @RequestParam String requestId);
}