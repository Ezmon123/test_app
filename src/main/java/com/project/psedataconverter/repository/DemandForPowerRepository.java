package com.project.psedataconverter.repository;

import com.project.psedataconverter.model.DemandForPower;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandForPowerRepository extends CrudRepository<DemandForPower, Long> {
    List<DemandForPower> findByActualPowerDemandIsNullOrderByIdAsc();

    DemandForPower findTopByOrderByIdDesc();
}
