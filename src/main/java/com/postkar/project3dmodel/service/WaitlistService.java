package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.WaitlistRequestDto;
import com.postkar.project3dmodel.dto.WaitlistResponseDto;
import com.postkar.project3dmodel.entity.Waitlist;
import com.postkar.project3dmodel.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    public WaitlistResponseDto addToWaitlist(WaitlistRequestDto requestDto) {
        log.info("Adding user to waitlist: {}", requestDto.getEmail());

        // Check if email already exists
        if (waitlistRepository.existsByEmail(requestDto.getEmail())) {
            log.warn("Email already exists in waitlist: {}", requestDto.getEmail());
            throw new IllegalArgumentException("Email already registered in waitlist");
        }

        // Check if phone already exists
        if (waitlistRepository.existsByPhone(requestDto.getPhone())) {
            log.warn("Phone number already exists in waitlist: {}", requestDto.getPhone());
            throw new IllegalArgumentException("Phone number already registered in waitlist");
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setName(requestDto.getName());
        waitlist.setEmail(requestDto.getEmail());
        waitlist.setPhone(requestDto.getPhone());
        waitlist.setCreatedAt(LocalDateTime.now());
        waitlist.setUpdatedAt(LocalDateTime.now());

        Waitlist savedWaitlist = waitlistRepository.save(waitlist);
        log.info("User successfully added to waitlist with ID: {}", savedWaitlist.getId());

        return mapToResponseDto(savedWaitlist);
    }

    public List<WaitlistResponseDto> getAllWaitlistEntries() {
        log.info("Fetching all waitlist entries");
        return waitlistRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public long getWaitlistCount() {
        return waitlistRepository.count();
    }

    private WaitlistResponseDto mapToResponseDto(Waitlist waitlist) {
        WaitlistResponseDto responseDto = new WaitlistResponseDto();
        responseDto.setId(waitlist.getId().toHexString());
        responseDto.setName(waitlist.getName());
        responseDto.setEmail(waitlist.getEmail());
        responseDto.setPhone(waitlist.getPhone());
        responseDto.setCreatedAt(waitlist.getCreatedAt());
        return responseDto;
    }
}
