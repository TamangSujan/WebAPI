package com.ayata.urldatabase.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CDisease{
    private String _visit_patient_detail_ward_ward_id;
    private String chronic_disease_id;
    private ArrayList<String> diseaseName;
    private String patient_id;
    private String visit_id;
}