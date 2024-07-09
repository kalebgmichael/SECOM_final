package com.productcnit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("KeyPair")
public class KeyPair implements Serializable {

    @Id
    private String recId;
    private String publicKey;
    private String privateKey;
}
