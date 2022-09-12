package com.ayata.urldatabase.routes.mobile;

import com.ayata.urldatabase.model.bridge.CensusRoot;
import com.ayata.urldatabase.model.bridge.ResponseMessage;
import com.ayata.urldatabase.model.database.Residents;
import com.ayata.urldatabase.model.bridge.CheckCensusResponse;
import com.ayata.urldatabase.repository.ResidentsRepository;
import com.ayata.urldatabase.static_methods.Library;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/mobile")
public class MobileURLCensus {
    private ResidentsRepository residentsRepository;
    @PostMapping("/checkCensus")
    public ResponseEntity<?> checkMobileCensus(@RequestBody List<String> residents){
        String appUserId = Library.splitAndGetFirst(residents.get(0), "_");
        List<Residents> checkResident = residentsRepository.findAllByUserIdExceptGivenList(appUserId, residents);
        CheckCensusResponse response = new CheckCensusResponse(appUserId, new ArrayList<>());
        for(Residents resident: checkResident){
            response.getCensusList().add(resident.getResidentOnly());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/addCensus")
    public ResponseEntity<?> addCensus(@RequestBody CensusRoot censusRoot){
        Residents checkResident = residentsRepository.findByResidentId(censusRoot.censusList.get(0).getResident_id());
        if(checkResident!=null){
            return new ResponseEntity(new ResponseMessage("403", "Failure", "Resident Exists"), HttpStatus.OK);
        }else{
            residentsRepository.save(censusRoot.censusList.get(0).getNewResident());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("200", "Success", "Resident Added"));
        }
    }
}
