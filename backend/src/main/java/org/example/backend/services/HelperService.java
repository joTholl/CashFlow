package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HelperService {

    private final Clock clock;

    public String getRandomId() {
        return UUID.randomUUID().toString();
    }

    public LocalDate getLocalDateNow() {
        return LocalDate.now(clock);
    }

}
