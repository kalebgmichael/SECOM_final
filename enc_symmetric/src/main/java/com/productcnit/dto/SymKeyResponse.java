package com.productcnit.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SymKeyResponse {
    private String Gen_Owner_Id;
    private String Gen_User_Id;
    private Boolean SymKey;
}
