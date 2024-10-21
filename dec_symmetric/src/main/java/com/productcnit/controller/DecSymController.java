package com.productcnit.controller;

import com.productcnit.dto.DecMessage;
import com.productcnit.dto.EncKeyResponse;
import com.productcnit.dto.EncMessage;
import com.productcnit.repository.EncKeyRepository;
import com.productcnit.service.DecSymService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/decsym")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DecSymController {

    private final DecSymService decSymService;

    WebClient webClient2= WebClient.create();
    @Autowired
    private WebClient.Builder webClientBuilder;

    private final EncKeyRepository encKeyRepository;

    @GetMapping("getmsg")
    public String getdata()
    {
        return  decSymService.getData();
    }

    @GetMapping("getDecrypt")
    public String getDecrypt()
    {
        return  decSymService.getData();
    }

    @GetMapping("/getDecryptmessage")
    public DecMessage getDecrypt(@RequestParam("encryptedMessage") String encryptedMessage,@RequestParam("senderid") String senderid,
                                 @RequestParam("secretkey") String secretkey, @RequestParam("peerid") String peerid)

    {
        System.out.println("this is secretkey"+ secretkey );
//        String SecretKey = secretkey.replaceAll("^\"|\"$", ""); // Remove leading and trailing quotation marks
        String SecretKey = secretkey;
        System.out.println("this is secretkey"+ SecretKey );
        return  decSymService.getDecrypt(encryptedMessage,senderid,peerid,secretkey);
    }

    @GetMapping("/Decrypt")
    public DecMessage Decrypt(@RequestParam("encryptedMessage") String encryptedMessage,@RequestParam("senderid") String senderid,
                              @RequestParam("peerid") String peerid,@RequestParam("secrectkey") String secrectkey)
    {
        decSymService.initFromStrings("CHuO1Fjd8YgJqTyapibFBQ==", "e3IYYJC2hxe24/EO");

        String decryptedmessage= decSymService.decrypt(encryptedMessage);
        DecMessage decMessage= new DecMessage();
        decMessage.setMessage(decryptedmessage);
        decMessage.setSenderId(senderid);
        decMessage.setRecId(peerid);
        return decMessage;
    }
    @GetMapping("/GetDecrypt")
    public DecMessage getDecrypt(@RequestParam("message") String message, @RequestParam("sendid") String sendid,
            @RequestParam("peerid") String peerid, Authentication authentication) {
        // Decode input parameters
        String decodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);
        String decodedSendid = URLDecoder.decode(sendid, StandardCharsets.UTF_8);
        String decodedPeerid = URLDecoder.decode(peerid, StandardCharsets.UTF_8);

        logDecodedInputs(decodedMessage, decodedSendid, decodedPeerid);

        // Fetch shared encryption key
        String encSharedKey = fetchSharedEncryptionKey(decodedSendid, decodedPeerid);
        if (encSharedKey == null) {
            System.out.println("The Symmetric Encryption key is null");
            return null;
        }
        // Retrieve and decrypt the shared key from external service
        String decryptedSharedKey = fetchDecryptedSharedKey(encSharedKey, getJwtToken(authentication));
        if (decryptedSharedKey == null) {
            return null; // Handle error in case the key retrieval failed
        }
        // Decrypt the message using the decrypted shared key
        String decryptedMessage = decryptMessage(decryptedSharedKey, decodedMessage);
        if (decryptedMessage == null) {
            return null; // Handle decryption error
        }
        // Prepare response
        return createDecryptedMessageResponse(decryptedMessage, decodedSendid, decodedPeerid);
    }

    // Helper method to log decoded inputs
    private void logDecodedInputs(String message, String sendid, String peerid) {
        System.out.println("Decoded message: " + message);
        System.out.println("Decoded sendid: " + sendid);
        System.out.println("Decoded peerid: " + peerid);
    }

    // Helper method to fetch the shared encryption key
    private String fetchSharedEncryptionKey(String sendid, String peerid) {
        EncKeyResponse[] sharedKeys = encKeyRepository.findByOwnerAndPairId(sendid, peerid).toArray(new EncKeyResponse[0]);
        if (sharedKeys.length > 0) {
            System.out.println("Key is valid, created at: " + sharedKeys[0].getCreatedat());
            return sharedKeys[0].getEnc_Key();
        } else {
            return null;
        }
    }

    // Helper method to fetch the decrypted shared key
    private String fetchDecryptedSharedKey(String encSharedKey, String jwtToken) {
        WebClient webClient = webClientBuilder.build();
        try {
            return webClient.get()
                    .uri(builder -> UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("DECKEY-SERVICE")
                            .path("/api/deckey/get_enc_sig_verif")
                            .queryParam("encryptedmessage", URLEncoder.encode(encSharedKey, StandardCharsets.UTF_8))
                            .build().toUri())
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(jwtToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.out.println("Error fetching decrypted shared key: " + e.getMessage());
            return null;
        }
    }

    // Refactored method to handle message decryption
    private String decryptMessage(String decryptedSharedKey, String message) {
        try {
            System.out.println("Initializing decryption service...");
            decSymService.initFromStrings(decryptedSharedKey, "e3IYYJC2hxe24/EO");
            System.out.println("Decrypting message: " + message);
            return decSymService.decrypt(message);
        } catch (Exception e) {
            System.out.println("Error during message decryption: " + e.getMessage());
            return null;
        }
    }

    // Helper method to create the DecMessage response
    private DecMessage createDecryptedMessageResponse(String decryptedMessage, String sendid, String peerid) {
        DecMessage decMessage = new DecMessage();
        decMessage.setMessage(decryptedMessage);
        decMessage.setSenderId(sendid);
        decMessage.setRecId(peerid);
        System.out.println("Message Decrypted"+decMessage);
        return decMessage;
    }

    // Helper method to extract JWT token from authentication
    private String getJwtToken(Authentication authentication) {
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        return jwt.getTokenValue();
    }

    //Enckey Repository info
    @PostMapping("/EncKeySave")
    public EncKeyResponse SaveGen(@RequestBody EncKeyResponse encKeyResponse) {
        try {
            return encKeyRepository.save(encKeyResponse);
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/GetEncKey/{Id}")
    public EncKeyResponse FindEncKey(@PathVariable String Id) {
        try {
            EncKeyResponse keys = encKeyRepository.findKeypairbyId(Id);
            System.out.println("this is ownerid " + keys.getOwner_Id() + "this is pair " + keys.getPair_Id());
            return keys;
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/DelEncKey/{Id}")
    public String DeleteEncKey(@PathVariable String Id) {
        try {
            String keys = encKeyRepository.deletekeypair(Id);
            return "keys deleted successfully";
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/EncKeyfindall")
    public List<Object> EncKeyfindall() {
        try {
            return encKeyRepository.findall();
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/keys")
    public List<EncKeyResponse> findByOwnerAndPairId(
            @RequestParam String Owner_Id,
            @RequestParam String Pair_Id) {
        return encKeyRepository.findByOwnerAndPairId(Owner_Id, Pair_Id);
    }

    @GetMapping("/sharedkey_pair")
    public String findByOwnerAndPairId_sharedkey(
            @RequestParam String Owner_Id,
            @RequestParam String Pair_Id) {
        EncKeyResponse[] sharedkeys = encKeyRepository.findByOwnerAndPairId(Owner_Id, Pair_Id).toArray(new EncKeyResponse[0]);
        String sharedkey= sharedkeys[0].getEnc_Key();
        return sharedkey;
    }
}
