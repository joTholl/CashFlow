package org.example.backend.repositories;

import org.example.backend.models.HistoricalEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalRepository extends MongoRepository<HistoricalEntry, String> {

}
