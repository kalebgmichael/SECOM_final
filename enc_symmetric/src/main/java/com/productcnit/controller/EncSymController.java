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

    @GetMapping("/getsecret")
    public String getdata()
    {
        encSymService.initFromStrings("CHuO1Fjd8YgJqTyapibFBQ==", "e3IYYJC2hxe24/EO");

        return encSymService.encrypt("kalebCyber");
    }

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
                              @RequestParam("sendid") String sendid, @RequestParam("peerid") String peerid, Authentication authentication) throws ExecutionException, InterruptedException, ParseException {

//        try
//        {
            // Initialize a new CompletableFuture for each request
            symKeyResponseCompletableFuture = new CompletableFuture<>();
            WebClient webClient1 = webClientBuilder.build();
           Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            String message1 = URLDecoder.decode(message, StandardCharsets.UTF_8);
            String sendid1 = URLDecoder.decode(sendid, StandardCharsets.UTF_8);
            String peerid1 = URLDecoder.decode(peerid, StandardCharsets.UTF_8);
            System.out.println("message1 in encryptsym is " + message1);
            System.out.println("message in encryptsym is " + message);

            EncKeyResponse[] sharedkeys = encKeyRepository.findByOwnerAndPairId(sendid, peerid).toArray(new EncKeyResponse[0]);
            String enc_sharedkey;
            if(sharedkeys.length!=0)
            {
                enc_sharedkey= sharedkeys[0].getEnc_Key();
                String Createdat= sharedkeys[0].getCreatedat();
                String Currenttime= new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());
                // Assuming the date format is "HH:mm dd-MM-yyyy"
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yyyy");

                // Parse the created time and current time into Date objects
                Date createdTimeDate = formatter.parse(Createdat);
                Date currentTimeDate = formatter.parse(Currenttime);
                // Get the difference in milliseconds
                long timeDifferenceInMillis = Math.abs(currentTimeDate.getTime() - createdTimeDate.getTime());

                if(enc_sharedkey != null && timeDifferenceInMillis <= 60000)
                {
                    System.out.println("sym key is not null and created time at"+sharedkeys[0].getCreatedat());
                }
                else
                {
                    Sender_Reciever senderReciever = new Sender_Reciever(sendid,peerid);
                    kafkaSenderReciever.send("send_pub_own_ca-topic", "Gen-key-pair", senderReciever);
                    // Log before waiting for the Kafka response
                    System.out.println("Encrypt: Waiting for Symmetric Key from Kafka...");

                    // Wait for the Kafka response (blocking call)
                    long startTime = System.currentTimeMillis();  // Start timer to measure waiting time
                    SymKeyResponse symKeyResponse = symKeyResponseCompletableFuture.get();  // Blocking until Kafka response
                    long endTime = System.currentTimeMillis();  // End timer

                    // Log after receiving the response
                    System.out.println("Encrypt: Received Symmetric Key after waiting for " + (endTime - startTime) + " ms.");
                    Boolean symKey = symKeyResponse.getSymKey();
                    System.out.println("Symmetric Key: " + symKey);

                    EncKeyResponse[] sharedkeys1 = encKeyRepository.findByOwnerAndPairId(sendid, peerid).toArray(new EncKeyResponse[0]);
                    enc_sharedkey= sharedkeys1[0].getEnc_Key();
                }

                // logic to check key refreshment
                System.out.println("key array is not null and created time at"+sharedkeys[0].getCreatedat());

            }
            else
            {
                enc_sharedkey = "";
                Sender_Reciever senderReciever = new Sender_Reciever(sendid,peerid);
                kafkaSenderReciever.send("send_pub_own_ca-topic", "Gen-key-pair", senderReciever);
                // Log before waiting for the Kafka response
                System.out.println("Encrypt: Waiting for Symmetric Key from Kafka...");

                // Wait for the Kafka response (blocking call)
                long startTime = System.currentTimeMillis();  // Start timer to measure waiting time
                SymKeyResponse symKeyResponse = symKeyResponseCompletableFuture.get();  // Blocking until Kafka response
                long endTime = System.currentTimeMillis();  // End timer

                // Log after receiving the response
                System.out.println("Encrypt: Received Symmetric Key after waiting for " + (endTime - startTime) + " ms.");
                Boolean symKey = symKeyResponse.getSymKey();
                System.out.println("Symmetric Key: " + symKey);

                EncKeyResponse[] sharedkeys1 = encKeyRepository.findByOwnerAndPairId(sendid, peerid).toArray(new EncKeyResponse[0]);
                enc_sharedkey= sharedkeys1[0].getEnc_Key();
            }
        String finalEnc_sharedkey = enc_sharedkey;
        String response = webClient1.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("DECKEY-SERVICE")
                            .path("/api/deckey/get_enc_sig_verif")
                            .queryParam("encryptedmessage", URLEncoder.encode(finalEnc_sharedkey, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            System.out.println("decrypted sharedkey is"+response);
            encSymService.initFromStrings(response, "e3IYYJC2hxe24/EO");
            String encryptedmessage= encSymService.encrypt(message1);
            System.out.println("encryptedmessage in encryptsym is " + encryptedmessage);
            EncMessage encMessage = new EncMessage();
            encMessage.setMessage(encryptedmessage);
            encMessage.setSenderId(sendid);
            encMessage.setRecId(peerid);
            return encMessage;
//        }
//        catch (Exception ignored){
//            System.out.println("error in Encrypt method");
//            return null;
//        }


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
