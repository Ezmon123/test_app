package com.project.psedataconverter.service;


import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.repository.DemandForPowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemandForPowerService {
    private DemandForPowerRepository demandForPowerRepository;

    @Autowired
    public DemandForPowerService(DemandForPowerRepository demandForPowerRepository) {
        this.demandForPowerRepository = demandForPowerRepository;
    }


    public DemandForPower saveDemandForPowerInDb(DemandForPower demandForPower) {
        return demandForPowerRepository.save(demandForPower);
    }

//    public Iterable<DemandForPower> findAllWhereActualPowerIsNull(){
//        return demandForPowerRepository.findByActualPowerDemandIsNullAndOrderByDateOfMeasurement();
//    }
}
