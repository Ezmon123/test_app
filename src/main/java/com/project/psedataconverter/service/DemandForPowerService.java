package com.project.psedataconverter.service;


import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.repository.DemandForPowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<DemandForPower> saveAllDemandForPowerInDb(List<DemandForPower> demandForPowerAll) {
        return (List<DemandForPower>) demandForPowerRepository.saveAll(demandForPowerAll);
    }
    public List<DemandForPower> findAllWhereActualPowerIsNull(){
        return demandForPowerRepository.findByActualPowerDemandIsNullOrderByIdAsc();
    }

    public DemandForPower getLastRow(){
        return demandForPowerRepository.findTopByOrderByIdDesc();
    }
}
