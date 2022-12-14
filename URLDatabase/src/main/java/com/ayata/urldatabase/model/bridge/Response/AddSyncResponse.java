package com.ayata.urldatabase.model.bridge.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSyncResponse {
    private String app_user_id;
    private String date;
    private String time;
    private int id;
}
