package com.hotel.controller;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDto>> getRooms(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
        Page<Room> rooms = roomService.getRooms(PageRequest.of(page, size, Sort.by(sortBy).ascending()));
        return ResponseEntity.ok(rooms.getContent().stream().map(roomService::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDto> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.toDto(roomService.getRoomDetails(id)));
    }
}
