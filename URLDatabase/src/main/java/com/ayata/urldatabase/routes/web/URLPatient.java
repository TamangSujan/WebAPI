package com.ayata.urldatabase.routes.web;

import com.ayata.urldatabase.model.bridge.*;
import com.ayata.urldatabase.model.bridge.Response.*;
import com.ayata.urldatabase.model.database.*;
import com.ayata.urldatabase.model.token.Message;
import com.ayata.urldatabase.repository.InfantVisitListsRepository;
import com.ayata.urldatabase.repository.PatientRepository;
import com.ayata.urldatabase.repository.UserRepository;
import com.ayata.urldatabase.repository.VisitListsRepository;
import com.ayata.urldatabase.routes.web.misc.Category;
import com.ayata.urldatabase.services.PatientService;
import com.ayata.urldatabase.services.VisitService;
import com.ayata.urldatabase.static_files.Library;
import com.ayata.urldatabase.static_files.StatusCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/web")
public class URLPatient {
    private PatientRepository patientRepository;
    private PatientService patientService;
    private VisitListsRepository visitListsRepository;
    private VisitService visitService;
    private UserRepository userRepository;
    private InfantVisitListsRepository infantVisitListsRepository;
    @GetMapping("/Patient/getPatientList")
    public ResponseEntity<?> getPatientList(@RequestParam int perPage, @RequestParam int currentPage){
        if(currentPage<=0){
            currentPage = 1;
        }
        List<Patients> list = patientRepository.getLimitPatient(perPage, (currentPage - 1) * perPage);
        if( list.size()>0) {
            return ResponseEntity.status(HttpStatus.OK).body(new PatientListResponse(perPage, currentPage, list.size(), list));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "Patient List not found in database."));
    }

    @GetMapping("/Patient/getPatientDetails/{id}")
    public ResponseEntity<?> getPatientList(@PathVariable String id){
        Patients patient = patientRepository.getPatientById(id);
        if(patient != null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, patient, null));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "Patient List not found in database."));
        }
    }

    @GetMapping("/Patient/get")
    public ResponseEntity<?> findPatient(@RequestParam int perPage, @RequestParam int currentPage) throws Exception {
        PatientListResponseV2 response = patientService.getPatientShortDetails(perPage, (currentPage-1)*perPage);
        if(response!=null){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        throw new IllegalStateException("No patients found!");
    }

    @GetMapping("/Patient/chart")
    public ResponseEntity<?> getPatientChart() throws Exception {
        try {
            List<PatientChartList> list = patientRepository.getAllChart();
            List<String> x = new ArrayList<>();
            List<Integer> y = new ArrayList<>();
            for (PatientChartList patientChart : list) {
                x.add(patientChart.get_id());
                y.add(patientChart.getCount());
            }
            PatientChart chart = new PatientChart(list.size(), list, x, y);
            return ResponseEntity.status(HttpStatus.OK).body(chart);
        }catch (Exception e){
            throw new Exception(e.getCause());
        }
    }

    @GetMapping("/Patient/location")
    public ResponseEntity<?> getPatientLocation() throws Exception {
        try {
            List<LocationPatient> locationPatients = patientRepository.patientLocationDetails();
            LocationCensusResponse censusResponse = new LocationCensusResponse(patientRepository.getTotalPatient(), locationPatients);
            return ResponseEntity.status(HttpStatus.OK).body(censusResponse);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/Patient/total")
    public ResponseEntity<?> getTotal() throws Exception {
        try {
            Integer chronicCount = visitListsRepository.getCounts("CHRONIC_DISEASE");
            if(chronicCount==null){
                chronicCount = 0;
            }
            Integer safeCount = visitListsRepository.getCounts("SAFE_MOTHERHOOD");
            if(safeCount==null){
                safeCount = 0;
            }
            Integer total = patientRepository.getTotalPatient();
            if(total==null){
                total = 0;
            }
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, new PatientCountResponse(chronicCount, safeCount, total)));
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/Patient/details/{id}")
    public ResponseEntity<?> getPatientDetails(@PathVariable(value = "id")String id){
        String chwId = Library.splitAndGetFirst(id, "_");
        Patients patient =  patientRepository.getPatientById(id);
        if(patient!=null){
            List<VisitLists> visitLists = visitListsRepository.getVisitsList(chwId, id);
            PatientDetails patientDetails = new PatientDetails(patient);

            //For Null Safety
            patientDetails.setLatitude(0.00);
            patientDetails.setLongitude(0.00);
            List<PastVisitDates> pastVisitDatesList = new ArrayList<>();
            List<NextVisitDates> nextVisitDatesList = new ArrayList<>();
            patientDetails.setPastVisitDates(pastVisitDatesList);
            patientDetails.setNextVisitDates(nextVisitDatesList);
            patientDetails.setVisitLists(new ArrayList<>());

            Users user = userRepository.findByChwId(Integer.parseInt(chwId));
            if(user!=null) {
                UsersShortDetail usersShortDetail = new UsersShortDetail();
                usersShortDetail.set_id(user.get_id());
                usersShortDetail.setChw_name(user.getChw_name());
                patientDetails.setUser(usersShortDetail);
            }
            if(visitLists!=null && visitLists.size()>0){
                patientDetails.setLatitude(visitLists.get(0).getVisit().get(0).getVisit_latitude());
                patientDetails.setLongitude(visitLists.get(0).getVisit().get(0).getVisit_longitude());
                for(VisitLists visitList: visitLists){
                    for(ModelVisitList modelVisitList: visitList.getVisit()){
                        PastVisitDates pastVisitDates = new PastVisitDates();
                        NextVisitDates nextVisitDates = new NextVisitDates();
                        if(modelVisitList!=null){
                            if(modelVisitList.getVisit_category().equals("CHRONIC_DISEASE")){
                                pastVisitDates.setVisit_category("Chronic Disease");
                                pastVisitDates.setHealth_facility_name(modelVisitList.getModelVisitChronic().getCHealthFacility().getHealth_facility_name());
                            }else if(modelVisitList.getVisit_category().equals("SAFE_MOTHERHOOD")){
                                pastVisitDates.setVisit_category("Safe Motherhood");
                                pastVisitDates.setHealth_facility_name(modelVisitList.getModelVisitSafe().getHealthDetail()._health_detail_visit_location);
                            }
                            pastVisitDates.setVisit_lastdate_english(modelVisitList.getVisit_lastdate_english());
                            pastVisitDates.setVisit_lastdate_nepali(modelVisitList.getVisit_lastdate_nepali());
                            nextVisitDates.setVisit_followupdate_english(modelVisitList.getVisit_followupdate_english());
                            nextVisitDates.setVisit_followupdate_nepali(modelVisitList.getVisit_followupdate_nepali());
                            pastVisitDatesList.add(pastVisitDates);
                            nextVisitDatesList.add(nextVisitDates);
                        }
                    }
                }
                patientDetails.setPastVisitDates(pastVisitDatesList);
                patientDetails.setNextVisitDates(nextVisitDatesList);
                patientDetails.setVisitLists(visitLists);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, patientDetails, null));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "No patient found!"));
        }
    }

    //TODO: not checked due to pupupdate/id
    @PutMapping("/Patient/update/{id}")
    public ResponseEntity<?> updatePatientDetails(@PathVariable(value = "id")String id, @RequestBody UpdatePatientModel patientModel){
        String userId = Library.splitAndGetFirst(id, "_");
        Patients patient = patientRepository.getPatientById(id);
        if(patient!=null){
            patientRepository.save(patientModel.getChangedPatient(patient));
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, "Patient updated successfully!"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FinalResponse(StatusCode.BAD_REQUEST, "Patient not found!"));
    }

    @DeleteMapping("/Patient/delete/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable(value = "id")String id){
        Patients patient = patientRepository.getPatientById(id);
        if(patient!=null){
            patientRepository.delete(patient);
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, "Patient deleted successfully!"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FinalResponse(StatusCode.BAD_REQUEST, "Patient not found!"));
    }

    @GetMapping("/patient/pregnantwoman")
    public ResponseEntity<?> getPregnantWomen(){
        List<VisitLists> list = visitListsRepository.getPregnantList();
        if(list!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, list));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "List not found in database!"));
    }

    @GetMapping("/patient/pregnantwoman/{id}")
    public ResponseEntity<?> getPregnantWomen(@PathVariable(value = "id")String id){
        List<VisitLists> list = visitListsRepository.getPregnantListByChwId(id);
        if(list!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, list));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "List not found in database!"));
    }

    @GetMapping("/patient/trimesterphase/{id}")
    public ResponseEntity<?> getTrimesterPhaseByChw(@PathVariable(value = "id")String id){
        Object object = visitService.getTrimesterCount(id);
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "Trimester not found!"));
    }

    @GetMapping("/patient/trimesterphase")
    public ResponseEntity<?> getPatientTrimester(){
        Object object = visitService.getPatientTrimester();
        if(object!=null){
            return ResponseEntity.status(200).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(400).body(new FinalResponse(StatusCode.NO_CONTENT, "Trimester not found!"));
    }

    @GetMapping("/patient/newpregnancy")
    public ResponseEntity<?> getNewPregnancyList(){
        Object object = visitService.getNewChronicOrPregnancy(Category.SAFE_MOTHERHOOD);
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "New Pregnancy not found!"));
    }

    @GetMapping("/patient/newpregnancy/{id}")
    public ResponseEntity<?> getNewPregnancyListById(@PathVariable(value = "id")String id){
        Object object = visitService.getNewChronicOrPregnancyById(Category.SAFE_MOTHERHOOD, id);
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "New Pregnancy List not found!"));
    }

    @GetMapping("/patient/newncd")
    public ResponseEntity<?> getNewChronicList(){
        Object object = visitService.getNewChronicOrPregnancy(Category.CHRONIC_DISEASE);
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "New Chronic Patients not found!"));
    }

    @GetMapping("/patient/newinfant")
    public ResponseEntity<?> getNewInfant(){
        Object object = infantVisitListsRepository.getRiskInfants();
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "New Risk Infants not found!"));
    }

    @GetMapping("/patient/newdelivery")
    public ResponseEntity<?> getNewDelivery(){
        Object object = visitService.getNewDelivery();
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "New Delivery Patients not found!"));
    }

    @GetMapping("/patient/riskpregnancy")
    public ResponseEntity<?> getRiskPregnancy(){
        Object object = visitService.getRiskPregnancy();
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "Risk pregnancy not found!"));
    }

    @GetMapping("/patient/complicationdelivery")
    public ResponseEntity<?> getComplicationDelivery(){
        Object object = visitService.getComplicationDelivery();
        if(object!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new FinalResponse(StatusCode.OK, null, null, object));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FinalResponse(StatusCode.NO_CONTENT, "Complication delivery not found!"));
    }
}
