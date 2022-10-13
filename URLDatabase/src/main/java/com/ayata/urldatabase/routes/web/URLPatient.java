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
import com.ayata.urldatabase.static_methods.Library;
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
        return ResponseEntity.status(HttpStatus.OK).body(new Message("Patient List not found in database."));
    }

    @GetMapping("/Patient/getPatientDetails/{id}")
    public ResponseEntity<?> getPatientList(@PathVariable String id){
        Patients patient = patientRepository.getPatientById(id);
        if(patient != null){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("200", "success", patient));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Message("Patient List not found in database."));
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
            int chronicCount = visitListsRepository.getCounts("CHRONIC_DISEASE");
            int safeCount = visitListsRepository.getCounts("SAFE_MOTHERHOOD");
            int total = patientRepository.getTotalPatient();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDetails(200, "Success", "", new PatientCountResponse(chronicCount, safeCount, total)));
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
                                pastVisitDates.setHealth_facility_name(modelVisitList.getModelVisitChronic().getCHealthFacility().health_facility_name);
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
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData("200", "Success", patientDetails));
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("400", "Failure", "No patient found!"));
        }
    }

    //TODO: not checked due to pupupdate/id
    @PutMapping("/Patient/update/{id}")
    public ResponseEntity<?> updatePatientDetails(@PathVariable(value = "id")String id, @RequestBody UpdatePatientModel patientModel){
        String userId = Library.splitAndGetFirst(id, "_");
        Patients patient = patientRepository.getPatientById(id);
        if(patient!=null){
            patientRepository.save(patientModel.getChangedPatient(patient));
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("200", "Success", "Patient updated successfully!"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("400", "Failure", "Patient not found!"));
    }

    @DeleteMapping("/Patient/delete/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable(value = "id")String id){
        Patients patient = patientRepository.getPatientById(id);
        if(patient!=null){
            patientRepository.delete(patient);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("200", "Success", "Patient deleted successfully!"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("400", "Failure", "Patient not found!"));
    }

    @GetMapping("/patient/pregnantwoman")
    public ResponseEntity<?> getPregnantWomen(){
        List<VisitLists> list = visitListsRepository.getPregnantList();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(list!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(list);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("List not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/pregnantwoman/{id}")
    public ResponseEntity<?> getPregnantWomen(@PathVariable(value = "id")String id){
        List<VisitLists> list = visitListsRepository.getPregnantListByChwId(id);
        FinalResponse response = new FinalResponse("400", "Failure");
        if(list!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(list);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("List not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/trimesterphase/{id}")
    public ResponseEntity<?> getTrimesterPhaseByChw(@PathVariable(value = "id")String id){
        Object object = visitService.getTrimesterCount(id);
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("Trimester not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/trimesterphase")
    public ResponseEntity<?> getPatientTrimester(){
        Object object = visitService.getPatientTrimester();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("Trimester not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/newpregnancy")
    public ResponseEntity<?> getNewPregnancyList(){
        Object object = visitService.getNewChronicOrPregnancy(Category.SAFE_MOTHERHOOD);
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("New Pregnancy not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/newpregnancy/{id}")
    public ResponseEntity<?> getNewPregnancyListById(@PathVariable(value = "id")String id){
        Object object = visitService.getNewChronicOrPregnancyById(Category.SAFE_MOTHERHOOD, id);
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("New Pregnancy List not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/newncd")
    public ResponseEntity<?> getNewChronicList(){
        Object object = visitService.getNewChronicOrPregnancy(Category.CHRONIC_DISEASE);
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("New Chronic Patients not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/newinfant")
    public ResponseEntity<?> getNewInfant(){
        Object object = infantVisitListsRepository.getRiskInfants();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("New Risk Infants not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/newdelivery")
    public ResponseEntity<?> getNewDelivery(){
        Object object = visitService.getNewDelivery();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("New Delivery Patients not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/riskpregnancy")
    public ResponseEntity<?> getRiskPregnancy(){
        Object object = visitService.getRiskPregnancy();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("Risk Pregnancy not found!");
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping("/patient/complicationdelivery")
    public ResponseEntity<?> getComplicationDelivery(){
        Object object = visitService.getComplicationDelivery();
        FinalResponse response = new FinalResponse("400", "Failure");
        if(object!=null){
            response.setStatusCode("200", "Success");
            response.setDetails(object);
            return ResponseEntity.status(200).body(response);
        }
        response.setMessage("Complication delivery not found!");
        return ResponseEntity.status(400).body(response);
    }
}
