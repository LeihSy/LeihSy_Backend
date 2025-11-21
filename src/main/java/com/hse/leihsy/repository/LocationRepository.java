package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l WHERE l.deletedAt IS NULL")
    List<Location> findAllActive();

    Optional<Location> findByRoomNr(String roomNr);
}