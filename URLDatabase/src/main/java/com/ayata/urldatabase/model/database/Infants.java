package com.ayata.urldatabase.model.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "infants")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Infants {
    @Id
    private String _id;
    private String user;
    private String infantAddedDate;
    private String infantAge;
    private String infantDobEnglish;
    private String infantFirstName;
    private String infantFullName;
    private String infantGender;
    private String infantHouseno;
    private String infantId;
    private String infantLastName;
    private String infantPhone;
    private String infantVillagename;
    private String infantmotherfirstname;
    private String infantmotherlastname;
    private int infantwardno;
    private String __v;
    private boolean deleted;
    private int infantAgeInDays;
    private int infantAgeInMonth;
    private int infantAgeInYear;
    private String infantDobNepali;
    private String infantModifyDate;
}
