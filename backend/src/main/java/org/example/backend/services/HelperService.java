package org.example.backend.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HelperService {

    public String getRandomId() {
        return UUID.randomUUID().toString();
    }

}
