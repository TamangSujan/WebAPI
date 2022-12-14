package com.ayata.urldatabase.model.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CHabit{
    private int _visit_patient_detail_ward_ward_id;
    private int chronic_habit_id;
    private String drink;
    private String drink_amount;
    private String exercise;
    private String patient_id;
    private String smoke;
    private String smoke_amount;
    private String visit_id;
}
