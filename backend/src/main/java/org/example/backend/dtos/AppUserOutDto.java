package org.example.backend.dtos;

import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;

import java.util.List;

public record AppUserOutDto(String username, List<Asset> assets) {

    public AppUserOutDto(AppUser appUser){
        this(appUser.username(), appUser.assets());
    }
}
