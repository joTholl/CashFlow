package org.example.backend.models;

import org.springframework.data.annotation.Id;

import java.util.List;

public record AppUser(@Id String id, String username, List<Asset> assets) {
}
