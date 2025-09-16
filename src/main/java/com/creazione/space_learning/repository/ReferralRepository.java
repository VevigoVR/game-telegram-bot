package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.ReferralP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralRepository extends JpaRepository<ReferralP, Long> {
}
