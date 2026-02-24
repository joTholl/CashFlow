package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.exceptions.UserNotLoggedInException;
import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.AppUserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserOutDto getAppUser(String id) {
        return new AppUserOutDto(appUserRepository.findById(id).orElseThrow(() -> new UserNotLoggedInException("User not logged in!")));
    }

    public void addTransaction(Transaction transaction, String userId) {
        AppUser appUser = appUserRepository.findById(userId).orElseThrow(() -> new UserNotLoggedInException("User not found!"));

        BigDecimal costAndFee = transaction.cost().add(transaction.fee());
        List<Asset> assets = new ArrayList<>();
        boolean updated = false;
        for (Asset asset : appUser.assets()) {
            if (asset.ticker().equals(transaction.ticker())) {
                Asset updatedAsset = asset
                        .withCost(asset.cost().add(costAndFee))
                        .withShares(asset.shares().add(transaction.shares()));
                assets.add(updatedAsset);
                updated = true;
            } else {
                assets.add(asset);
            }
        }
        if (!updated) {
            assets.add(new Asset(transaction.ticker(), transaction.shares(), transaction.assetName(), costAndFee, transaction.assetType()));
        }
        appUserRepository.save(appUser.withAssets(assets));
    }

    public void subtractTransaction(Transaction transaction, String userId) {
        AppUser appUser = appUserRepository.findById(userId).orElseThrow(() -> new UserNotLoggedInException("User not found!"));
        BigDecimal costAndFee = transaction.cost().add(transaction.fee());
        List<Asset> assets = new ArrayList<>();
        boolean updated = false;
        for (Asset asset : appUser.assets()) {
            if (asset.ticker().equals(transaction.ticker())) {
                BigDecimal newShares = asset.shares().subtract(transaction.shares());
                if (newShares.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Not enough shares to subtract!");
                } else if (newShares.compareTo(BigDecimal.ZERO) == 0) {
                    updated = true;
                } else {
                    Asset updatedAsset = asset
                            .withCost(asset.cost().subtract(costAndFee))
                            .withShares(newShares);
                    assets.add(updatedAsset);
                    updated = true;
                }
            } else {
                assets.add(asset);
            }
        }
        if(!updated){
            throw new IllegalArgumentException("Asset not found!");
        }
        appUserRepository.save(appUser.withAssets(assets));

    }

}
