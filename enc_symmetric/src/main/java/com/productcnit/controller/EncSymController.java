package com.productcnit.controller;


import com.productcnit.dto.EncKeyResponse;
import com.productcnit.dto.EncMessage;
import com.productcnit.dto.Sender_Reciever;
import com.productcnit.dto.SymKeyResponse;
import com.productcnit.repository.EncKeyRepository;
import com.productcnit.service.EncSymService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/encsym")
@CrossOrigin("*")
@RequiredArgsConstructor
public class EncSymController {

    private final EncSymService encSymService;
    @Autowired
    private EncKeyRepository encKeyRepository;
    private final KafkaTemplate<String, Sender_Reciever> kafkaSenderReciever;
    private CompletableFuture<SymKeyResponse> symKeyResponseCompletableFuture;

    private final WebClient webClient = WebClient.builder().build();
    @Autowired
    private WebClient.Builder webClientBuilder;
    // Kafka listener to process the received message
    @KafkaListener(topics = "SymKeyGenerated-topic-test", groupId = "group-id2")
    public void listenForSymKeyResponse(SymKeyResponse symKeyResponse) {
        // Log when the Kafka message is received
        System.out.println("listenForSymKeyResponse: Received message from Kafka: " + symKeyResponse);

        // Extract fields
        Boolean symKey = symKeyResponse.getSymKey();
        String ownerId = symKeyResponse.getGen_Owner_Id();
        String recId = symKeyResponse.getGen_User_Id();

        // Log the extracted values
        System.out.println("listenForSymKeyResponse: Symmetric Key = " + symKey + ", Owner ID = " + ownerId + ", Recipient ID = " + recId);

        // Complete the CompletableFuture, allowing GetEncrypt to continue
        symKeyResponseCompletableFuture.complete(symKeyResponse);

        // Log that the CompletableFuture is completed
        System.out.println("listenForSymKeyResponse: CompletableFuture completed.");
    }

    @GetMapping("/Encrypt")
    public EncMessage Encrypt(@RequestParam("message") String message,
                              @RequestParam("sendid") String sendid,
                              @RequestParam("peerid") String peerid,
                              Authentication authentication) throws ExecutionException, InterruptedException, ParseException {

        // Initialize a new CompletableFuture for each request
        symKeyResponseCompletableFuture = new CompletableFuture<>();

        // Decode incoming request parameters
        String decodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);
        String decodedSendId = URLDecoder.decode(sendid, StandardCharsets.UTF_8);
        String decodedPeerId = URLDecoder.decode(peerid, StandardCharsets.UTF_8);

        // Extract JWT token
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();

        // Retrieve shared encryption keys
        EncKeyResponse[] sharedKeys = encKeyRepository.findByOwnerAndPairId(decodedSendId, decodedPeerId).toArray(new EncKeyResponse[0]);
        String encryptedSharedKey = getSharedKey(sharedKeys, decodedSendId, decodedPeerId);

        // Fetch the encrypted symmetric key
        String decryptedSharedKey = fetchDecryptedSharedKey(encryptedSharedKey, authentication);
        System.out.println("This is the decrypted shared key"+decryptedSharedKey);

        // Initialize encryption service
        encSymService.initFromStrings(decryptedSharedKey, "e3IYYJC2hxe24/EO");

        // Encrypt the message
        String encryptedMessage = encSymService.encrypt(decodedMessage);

        // Prepare and return encrypted message response
        EncMessage encMessage = new EncMessage();
        encMessage.setMessage(encryptedMessage);
        encMessage.setSenderId(decodedSendId);
        encMessage.setRecId(decodedPeerId);

        return encMessage;
    }

    /**
     * Helper method to fetch the shared encryption key, generating a new one if necessary.
     */
    private String getSharedKey(EncKeyResponse[] sharedKeys, String sendid, String peerid) throws ExecutionException, InterruptedException, ParseException {
        String encSharedKey;

        if (sharedKeys.length > 0 && isKeyValid(sharedKeys[0])) {
            encSharedKey = sharedKeys[0].getEnc_Key();
            System.out.println("Key is valid, created at: " + sharedKeys[0].getCreatedat());
        } else {
            // Generate a new key pair using Kafka

            Sender_Reciever senderReceiver = new Sender_Reciever(sendid, peerid);
            kafkaSenderReciever.send("send_pub_own_ca-topic", "Gen-key-pair", senderReceiver);

            System.out.println("Waiting for new Symmetric Key from Kafka...");

            // Wait for the Kafka listener to complete the future with the symmetric key
            SymKeyResponse symKeyResponse = symKeyResponseCompletableFuture.get();
            System.out.println("Received Symmetric Key: " + symKeyResponse.getSymKey());
            //Create delay to retrieve the new key instead of the old key
            // Introduce delays
            System.out.println("Delay one");
            Thread.sleep(100); // 50 milliseconds
            // Fetch the newly generated key
            EncKeyResponse[] newSharedKeys = encKeyRepository.findByOwnerAndPairId(sendid, peerid).toArray(new EncKeyResponse[0]);
            encSharedKey = newSharedKeys[0].getEnc_Key();
        }

        return encSharedKey;
    }

    /**
     * Helper method to check if the encryption key is still valid.
     */
    private boolean isKeyValid(EncKeyResponse keyResponse) throws ParseException {
        String createdAt = keyResponse.getCreatedat();
        String currentTime = new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        Date createdTimeDate = formatter.parse(createdAt);
        Date currentTimeDate = formatter.parse(currentTime);

        // Return true if the time difference is less than or equal to 60,000 ms (1 minute)
        return Math.abs(currentTimeDate.getTime() - createdTimeDate.getTime()) <= 60000;
    }

    /**
     * Helper method to fetch the decrypted shared key from the DECKEY service.
     */
    private String fetchDecryptedSharedKey(String encryptedSharedKey, Authentication authentication) {
        WebClient webClient = webClientBuilder.build();
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String response = webClient.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("DECKEY-SERVICE")
                            .path("/api/deckey/get_enc_sig_verif")
                            .queryParam("encryptedmessage", URLEncoder.encode(encryptedSharedKey, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return  response;
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
