package org.example.backend.models;

import lombok.With;
import org.springframework.data.annotation.Id;

import java.util.List;

@With
public record AppUser(@Id String id, String username, List<Asset> assets) {
}
