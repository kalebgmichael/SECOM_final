package com.productcnit.Controller;


import com.productcnit.Service.KeyManager;
import com.productcnit.dto.KeyPairResponse;
import com.productcnit.dto.PublicKeyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.*;

@RestController
@RequestMapping("/key")
public class KeyexchangeController {

    private String privateKey;
    private String publicKey;
    KeyPairResponse keyPairResponse;

    @Autowired
    private final KeyManager keyManager;

    private final KafkaTemplate<String, PublicKeyMessage> kafkaTemplate;

    public KeyexchangeController(KafkaTemplate<String, PublicKeyMessage> kafkaTemplate, KeyManager keyManager) {
        this.kafkaTemplate = kafkaTemplate;
        this.keyManager = keyManager;
    }

    @GetMapping("/getkeypair")
    public KeyPairResponse getpairkey() {
        KeyManager.generateKeyPair();
        privateKey = keyManager.generatePrviatekey();
        publicKey = keyManager.generatePublicKey();
        keyPairResponse= new KeyPairResponse(publicKey,privateKey);

        return keyPairResponse;
    }

    @GetMapping("/getpair")
    public KeyPairResponse getKeyStrPrv() {
        return new KeyPairResponse(keyPairResponse.getPublickey(), keyPairResponse.getPrivatekey());
    }

    @GetMapping("/getpub")
    public ResponseEntity<String> getPublicKey() {
        if (publicKey != null) {
            PublicKeyMessage publicKeyMessage = new PublicKeyMessage("client-idx", publicKey);
            kafkaTemplate.send("public-key-topic", "client-idx", publicKeyMessage);
            return ResponseEntity.ok("Public key published successfully");
        } else {
            return ResponseEntity.badRequest().body("Public key not found");
        }
    }

    @KafkaListener(topics = "public-key-topic", groupId = "group-id")
    public String getsecsharedkey(PublicKeyMessage publicKeyMessage) {
        System.out.println("private"+privateKey);
        System.out.println("public"+publicKey);
        String peerPublicKey = publicKeyMessage.getPublicKey();
        keyManager.initFromStringsPublickey(peerPublicKey);
        keyManager.initFromStringsPrvkey(privateKey);
        String sharedKey = keyManager.generateSharedSecret();
        System.out.println("the shared key is "+sharedKey);
        return sharedKey;
    }
}

//
//
//import com.productcnit.Service.KeyManager;
//import com.productcnit.dto.KeyPairResponse;
//import com.productcnit.dto.PublicKeyMessage;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/key")
//public class KeyexchangeController {
//
//
//    private String privateKey2;
//    private String publicKey2;
//
//
//    private final HttpSession session;
//    private final KafkaTemplate<String, PublicKeyMessage> kafkaTemplate;
//
//
//    @Autowired
//    private final KeyManager keyManager3;
//
//    public KeyexchangeController(HttpSession session, KafkaTemplate<String, PublicKeyMessage> kafkaTemplate, KeyManager keyManager) {
//        this.session = session;
//        this.kafkaTemplate = kafkaTemplate;
//        this.keyManager3 = keyManager;
//    }
//
//    @GetMapping("/getkeypair")
//    public KeyPairResponse getpairkey() {
//        KeyManager.generateKeyPair();
//        String prvkey = keyManager3.generatePrviatekey();
//        String pubkey = keyManager3.generatePublicKey();
//        return new KeyPairResponse(prvkey, pubkey);
//    }
//
//    @GetMapping("/getpair")
//    public KeyPairResponse getKeyStrPrv()
//    {
//        try {
//            WebClient webClient = WebClient.create("http://localhost:8088/key");
//
//            // Build the request
//            WebClient.RequestHeadersSpec<?> requestSpec = webClient
//                    .method(HttpMethod.GET)
//                    .uri("/getkeypair");
//
//            // Execute the request
//            Mono<KeyPairResponse> responseMono = requestSpec.retrieve().bodyToMono(KeyPairResponse.class);
//
//            // Block and get the response
//            KeyPairResponse response = responseMono.block();
//            if (response != null) {
//                // Access the private key and public key
//                privateKey2 = response.getPrivatekey();
//                String publicKey1 = response.getPublickey();
//                session.setAttribute("privateKeyx", privateKey2);
//                session.setAttribute("publicKeyx", publicKey1);
//                System.out.println("Private Key: " + privateKey2);
//                System.out.println("Public Key: " + publicKey1);
//                return new KeyPairResponse(privateKey2,publicKey1);
//            } else {
//                return null;
//            }
//        } catch (Exception ignored) {
//            System.out.println("Error: " + ignored.getMessage());
//            return null;
//        }
//    }
//
//    @GetMapping("/getpub")
//    public ResponseEntity<String> getPublicKey() {
//        String publicKey = (String) session.getAttribute("publicKeyx");
//        if (publicKey != null) {
//            PublicKeyMessage publicKeyMessage = new PublicKeyMessage("client-idx", publicKey);
//            kafkaTemplate.send("public-key-topic", "client-idx", publicKeyMessage);
//            return ResponseEntity.ok("Public key published successfully");
//        } else {
//            return ResponseEntity.badRequest().body("Public key not found in session");
//        }
//    }
//
//    @GetMapping("/testsession")
//    public String testSession() {
//        session.setAttribute("testAttribute", "testValue");
//        String testValue = (String) session.getAttribute("testAttribute");
//        return "Test value from session: " + testValue;
//    }
//
//    @KafkaListener(topics = "public-key-topic", groupId = "group-id")
//    public String getsecsharedkey(PublicKeyMessage publicKeyMessage)
//   {
////       String privateKey = (String) session.getAttribute("privateKeyx");
//       System.out.println(privateKey2);
//       if (privateKey2 == null) {
//           return "Private key not found in session";
//       }
//       String PeerPubKey = publicKeyMessage.getPublicKey();
//
////       try
////       {
////           WebClient webClient = WebClient.create("http://localhost:8088/key");
////
////           // Build the request
////           WebClient.RequestHeadersSpec<?> requestSpec = webClient
////                   .method(HttpMethod.GET)
////                   .uri("/getpub");
////
////           // Execute the request
////           Mono<String> responseMono = requestSpec.retrieve().bodyToMono(String.class);
////
////           // Block and get the response
////           String response = responseMono.block();
////           PeerPubKey= response.toString();
////           System.out.println("this is the peerpublickey" + PeerPubKey);
////           System.out.println("this is the privatekey" + privateKey);
////       }
////       catch (Exception ignored)
////       {
////           System.out.println("error");
////       }
//
//       keyManager3.initFromStringsPublickey(PeerPubKey);
//       keyManager3.initFromStringsPrvkey(privateKey2);
//       String sharedKey = keyManager3.generateSharedSecret();
//       System.out.println("Shared key: " + sharedKey);
//       return sharedKey;
//   }
//}
