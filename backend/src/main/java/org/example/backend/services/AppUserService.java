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
        List<Asset> assets = appUser.assets();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            if (asset.ticker().equals(transaction.ticker())) {
                Asset updatedAsset = asset
                        .withCost(asset.cost().add(costAndFee))
                        .withShares(asset.shares().add(transaction.shares()));
                assets.set(i, updatedAsset);
                appUserRepository.save(appUser);
                return;
            }
        }
        assets.add(new Asset(transaction.ticker(), transaction.shares(), transaction.assetname(), costAndFee));
        appUserRepository.save(appUser);
    }

    public void subtractTransaction(Transaction transaction, String userId) {
        AppUser appUser = appUserRepository.findById(userId).orElseThrow(() -> new UserNotLoggedInException("User not found!"));
        BigDecimal costAndFee = transaction.cost().add(transaction.fee());
        List<Asset> assets = appUser.assets();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            if (asset.ticker().equals(transaction.ticker())) {
                BigDecimal newShares = asset.shares().subtract(transaction.shares());
                if (newShares.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Not enough shares to subtract");
                } else if (newShares.compareTo(BigDecimal.ZERO) == 0) {
                    assets.remove(i);
                } else {
                    Asset updatedAsset = asset
                            .withCost(asset.cost().subtract(costAndFee))
                            .withShares(newShares);
                    assets.set(i, updatedAsset);
                }
                appUserRepository.save(appUser);
                return;
            }
        }
        throw new IllegalArgumentException("Asset not found for ticker: " + transaction.ticker());
    }

}
