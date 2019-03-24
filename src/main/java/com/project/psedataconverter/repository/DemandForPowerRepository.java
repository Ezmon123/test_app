package com.project.psedataconverter.repository;

import com.project.psedataconverter.model.DemandForPower;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandForPowerRepository extends CrudRepository<DemandForPower, Long> {
//    Iterable<DemandForPower> findByActualPowerDemandIsNullAndOrderByDateOfMeasurement();
}
