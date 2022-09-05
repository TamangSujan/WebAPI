package com.ayata.urldatabase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PregnancyHistory{
    private String _patient_detail_patient_detail_id;
    private String _patient_detail_ward_ward_id;
    private String _pregnancy_history_id;
    private String _pregnancy_history_type;
}