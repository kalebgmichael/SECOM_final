package com.productcnit.Controller;


import com.productcnit.Service.KeyManager;
import com.productcnit.dto.*;
import com.productcnit.model.KeyPair;
import com.productcnit.repository.EncKeyRepository;
import com.productcnit.repository.GeneralKeyPairRepository;
import com.productcnit.repository.KeyPairRespository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/key")
@CrossOrigin("*")
@RequiredArgsConstructor
public class KeyexchangeController {


    String sendid= "ship0";
    String recid="shore0";
    private  String PUBLIC_KEY_STRINGS="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAQLQjHVXBTFYFfzlIKvuBCX6mZmAQfvGpAVSGhnXZ9g3Tha4FKsi9BTUlz2zwPtkzINLfUYIRPf71Q5hCk4Y7QAcJH3AviTnCasAwG7KBDzGYFM/ka52kiol/0vMVSle1o9d9ZTzF+9pJ+GkoF5ykFF62y7mrx9yopFSucezaCQIDAQAB";
    private  String PRIVATE_KEY_STRINGS="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIBAtCMdVcFMVgV/OUgq+4EJfqZmYBB+8akBVIaGddn2DdOFrgUqyL0FNSXPbPA+2TMg0t9RghE9/vVDmEKThjtABwkfcC+JOcJqwDAbsoEPMZgUz+RrnaSKiX/S8xVKV7Wj131lPMX72kn4aSgXnKQUXrbLuavH3KikVK5x7NoJAgMBAAECgYA2uWUjxpSc0jGyTsLmZFDEkoSUBALhhwkekA69CAqpYjAsHVJPqh3Vaa9v3r4hFPAgvNS9rU3OhaGQjbMeVUxkx9GVt2LUnGMX/M5yFh7OZl7cMtebhoeEt0z5SXI3NULiEKKjHgXwTc4DaA+lcp/gxSNDQNlSFJWhfvTUQDqosQJBAJ8nPN0eOesTaCCzATO3FpkcuhQ9XLhAQTvbzYb67gtK+4Vh8pBAEFLEuKZAmBidub63Lv50cLnm/1L7+4Y6+H8CQQDOS871vu+n914il1GBZ81IqbwKki3IWx/d6iXz1UlXyOEMgniNpQYKW8EWzvrT/lXDlTe3VKgpBfllpgIBySl3AkBzmfmglxr0wDTrQ3qFCOEWOAKFPwkBIFMB2qdP+yY696z4dmvNEWuJ4zBIOjT/9Fj9yWsOEp/quHoO2c8Z8e2bAkB05i5bwRuq8ZjNPzP3gWupXk1pLBZ3b3OqW7Gv70/FR9aHMTPBCB9ZJU9Qbm9iS8AruVW+NGGqBXGisSR4AJbXAkAjPxbNZsSfeJZY/AkU2SNbIvHnLACQvRwJwibvzAkDfJ4t1gVohi+enBO91slbMT+tQGD9qnLxDJpso4B3rmSG";



    private RestTemplate restTemplate;



    private Authentication authentication;


    private final WebClient webClient = WebClient.builder().build();
    private String privateKey;
    private String publicKey;
    KeyPairResponse keyPairResponse;

    @Autowired
    private final KeyManager keyManager;

    @Autowired
    private KeyPairRespository keyPairRespository;

    @Autowired
    private GeneralKeyPairRepository generalKeyPairRepository;

    @Autowired
    private EncKeyRepository encKeyRepository;
    @Autowired
    private final HttpSession session;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String, PublicKeyMessage> kafkaTemplate;
    private final KafkaTemplate<String, GenKeyPairResponse> kafkaTemplate1;
    private final KafkaTemplate<String, SymKeyResponse> kafkasymKeyGenerated;
    private final KafkaTemplate<String, ReGenKeyPairResponse> reGenKeyPairResponseKafkaTemplate;
    private final KafkaTemplate<String, SenRecResponse> kafkaloginTemplate;
    private CompletableFuture<ReGenKeyPairResponse> reGenKeyPairResponseCompletableFuture;


    @GetMapping("/getpair")
    public KeyPairResponse getKeyStrPrv() {
        return new KeyPairResponse(keyPairResponse.getPublickey(), keyPairResponse.getPrivatekey());
    }
    @GetMapping("/getkeypair1")
    public KeyPairResponse getpairkey1() {
        KeyManager.generateKeyPair();
        privateKey = keyManager.generatePrviatekey();
        publicKey = keyManager.generatePublicKey();
        keyPairResponse= new KeyPairResponse(publicKey,privateKey);

        return keyPairResponse;
    }

    @GetMapping("/sendpub")
    public ResponseEntity<String> getPublicKey() {
        String PUBLIC_KEY_STRINGS1="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAQLQjHVXBTFYFfzlIKvuBCX6mZmAQfvGpAVSGhnXZ9g3Tha4FKsi9BTUlz2zwPtkzINLfUYIRPf71Q5hCk4Y7QAcJH3AviTnCasAwG7KBDzGYFM/ka52kiol/0vMVSle1o9d9ZTzF+9pJ+GkoF5ykFF62y7mrx9yopFSucezaCQIDAQAB";

        PublicKeyMessage publicKeyMessage = new PublicKeyMessage("ship0", PUBLIC_KEY_STRINGS1,"shore0");
            kafkaTemplate.send("key-pair-topic", "client-idx", publicKeyMessage);
            return ResponseEntity.ok("Public key published successfully");

    }

    @KafkaListener(topics = "key-pair-topic", groupId = "group-id1")
    public KeyPairResponse getpairkey(PublicKeyMessage publicKeyMessage) {
        System.out.println(publicKeyMessage.getRecId());

        String clientidx= publicKeyMessage.getRecId();
        if(clientidx.equals(recid))
        {
            KeyManager.generateKeyPair();
            privateKey = keyManager.generatePrviatekey();
            publicKey = keyManager.generatePublicKey();
//            session.setAttribute("privateKeyx", privateKey);
//            session.setAttribute("publicKeyx", publicKey);
            System.out.println("Private Key: " + privateKey);
            System.out.println("Public Key: " + publicKey);
            keyPairResponse= new KeyPairResponse(publicKey,privateKey);


            return keyPairResponse;
        }
        else
        {
            System.out.println("no clientid");
            return null;
        }

    }

    @KafkaListener(topics = "Save-Gen-keypair-topic", groupId = "group-id3")
    public KeyPairResponse SaveGenKeyPair(SenRecResponse senRecResponse) throws ParseException {
        String Owner_ID = senRecResponse.getGen_Owner_Id();
        String userId = senRecResponse.getGen_User_Id();
        System.out.println("this userid "+userId);

        GenKeyPairResponse keys= generalKeyPairRepository.findKeypairbyId(Owner_ID);
        if(keys != null && isKeyValid(keys.getCreatedat()))
        {
          System.out.println("Data already exists in cache");
          return null;
        }
        else
        {
            KeyManager.generateKeyPair();
            String privateKey = keyManager.generatePrviatekey();
            String publicKey = keyManager.generatePublicKey();
            System.out.println("Private Key: " + privateKey);
            System.out.println("Public Key: " + publicKey);
            keyPairResponse= new KeyPairResponse(publicKey,privateKey);
            keyManager.initFromStringsPublickey(publicKey);
            keyManager.initFromStringsPrvkey(privateKey);
            String sharedKey = keyManager.generateSharedSecret();
            String Createdat= new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());
//            System.out.println("the shared key is "+sharedKey);
            // Save the generated key pair to the repository
            GenKeyPairResponse genKeyPairResponse= new GenKeyPairResponse(Owner_ID,userId,privateKey,publicKey,Createdat);
            generalKeyPairRepository.save(genKeyPairResponse);
            System.out.println("Saved data to cache by "+ userId);

            return keyPairResponse;
        }

    }
    // Method to regenerate the DH keypair
    @KafkaListener(topics = "Save-reGenerate-keypair-topic", groupId = "group-id3")
    public KeyPairResponse SaveReGenerateKeyPair_new(SenRecResponse senRecResponse) throws ParseException {
        String Owner_ID = senRecResponse.getGen_Owner_Id();
        String userId = senRecResponse.getGen_User_Id();
        System.out.println("this userid "+userId);
            KeyManager.generateKeyPair();
            String privateKey = keyManager.generatePrviatekey();
            String publicKey = keyManager.generatePublicKey();
            System.out.println("Private Key: " + privateKey);
            System.out.println("Public Key: " + publicKey);
            keyPairResponse= new KeyPairResponse(publicKey,privateKey);
            keyManager.initFromStringsPublickey(publicKey);
            keyManager.initFromStringsPrvkey(privateKey);
            String sharedKey = keyManager.generateSharedSecret();
            String Createdat= new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());
//            System.out.println("the shared key is "+sharedKey);
            // Save the generated key pair to the repository
            GenKeyPairResponse genKeyPairResponse= new GenKeyPairResponse(Owner_ID,userId,privateKey,publicKey,Createdat);
            generalKeyPairRepository.save(genKeyPairResponse);
            ReGenKeyPairResponse reGenKeyPairResponse= new ReGenKeyPairResponse(Owner_ID,userId,true,Createdat);
            reGenKeyPairResponseKafkaTemplate.send("DHkeypairRegenerated-topic", "Gen-key-pair", reGenKeyPairResponse);
            System.out.println("Saved data to cache by "+ userId);
            return keyPairResponse;
    }

    // Kafka listener to process the received message
    @KafkaListener(topics = "DHkeypairRegenerated-topic", groupId = "group-id2")
    public void listenForGenKeyPairResponse(ReGenKeyPairResponse reGenKeyPairResponse) {
        // Log when the Kafka message is received
        System.out.println("listenForGenKeyPairResponse: Received message from Kafka: " + reGenKeyPairResponse);
        // Extract fields
        Boolean dhkeypair = reGenKeyPairResponse.getGenerated();
        String ownerId = reGenKeyPairResponse.getGen_Owner_Id();
        String userId = reGenKeyPairResponse.getGen_User_Id();
        // Log the extracted values
        System.out.println("listenForGenKeyPairResponse: Symmetric Key = " + dhkeypair + ", Owner ID = " + ownerId + ", Recipient ID = " + userId);
        // Complete the CompletableFuture, allowing GetEncrypt to continue
        reGenKeyPairResponseCompletableFuture.complete(reGenKeyPairResponse);
        // Log that the CompletableFuture is completed
        System.out.println("listenForGenKeyPairResponse: CompletableFuture completed.");
    }


    @GetMapping("/userId2")
    private String  getUserId2(Authentication authentication) {
        try {
            if (!(authentication instanceof JwtAuthenticationToken)) {
                throw new SecurityException("Invalid authentication type");
            }

            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            Map<String, Object> claims = jwt.getClaims();

            System.out.println("claims"+claims);

            // Extract profile and email claims
            String email = (String) claims.get("email"); // Adjust claim key if needed
            String firstName = (String) claims.get("firstName"); // Adjust claim key if needed
            String lastName = (String) claims.get("lastName"); // Adjust claim key if needed

            // Combine for a username-like identifier (optional)
            String username = String.format("%s %s", firstName, lastName);
            String ownerid = (String) claims.get("Owner_ID"); // Adjust claim key if needed
            System.out.println("ownerid"+ ownerid);
            System.out.println("email"+ email);
            return ownerid;
        } catch (Exception e) {
            //log.error("Error retrieving user information from JWT:", e);
//            response.put("error", "Failed to retrieve user information");
            return "error faild to retrive";
        }
    }

@GetMapping("/sharedkey")
@KafkaListener(topics = "key-pair-topic", groupId = "group-id2")
public String getsecsharedkey(@RequestParam("SenderId") String SenderId,@RequestParam("Recid") String Recid, @RequestParam("publicKey") String publicKey,Authentication authentication)
{
        WebClient webClient1 = webClientBuilder.build();
        // Decode the public key
        String decodedPublicKey = URLDecoder.decode(publicKey, StandardCharsets.UTF_8);
        // Print the decoded public key
        System.out.println("Decoded public key: " + decodedPublicKey);
        GenKeyPairResponse keys = generalKeyPairRepository.findKeypairbyId(Recid);
        System.out.println("privatekey1"+keys.getGen_private_Key().toString());
        keyManager.initFromStringsPublickey(decodedPublicKey);
        keyManager.initFromStringsPrvkey(keys.getGen_private_Key());
        String sharedKey = keyManager.generateSharedSecret();
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String response = webClient1.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("ENCKEY-SERVICE")
                            .path("/api/enckey/getenc_sig")
                            .queryParam("message", URLEncoder.encode(sharedKey, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Map<String, Object> claims = jwt.getClaims();
        String  userId =authentication.getName();
        String Createdat= new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());
        EncKeyResponse encKeyresponse = new EncKeyResponse(SenderId,Recid,response,Createdat);
        encKeyRepository.save(encKeyresponse);
//        SymKeyResponse symKeyResponse = new SymKeyResponse(SenderId,Recid,true);
//        kafkasymKeyGenerated.send("SymKeyGenerated-topic", "Gen-key-pair", symKeyResponse);
        System.out.println("Saved data to cache by "+ userId);
        System.out.println("the shared key is "+sharedKey);
        return sharedKey;
//    }
//    catch (Exception ignored)
//    {
//        System.out.println("error");
//        return null;
//    }

}

    private boolean isKeyValid(String Createdat) throws ParseException {
        String createdAt = Createdat;
        String currentTime = new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yyyy");
        Date createdTimeDate = formatter.parse(createdAt);
        Date currentTimeDate = formatter.parse(currentTime);

        // Return true if the time difference is less than or equal to 60,000 ms (1 minute)
        return Math.abs(currentTimeDate.getTime() - createdTimeDate.getTime()) <= 60000;
    }
    // key-pair CRUD handling

    @PostMapping("/savekeypair")
    public KeyPair save(@RequestBody KeyPair keyPair) {
        try {
            return keyPairRespository.save(keyPair);
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/getkeypair/{Id}")
    public KeyPair findkeypair(@PathVariable String Id) {
        try {
            KeyPair keys = keyPairRespository.findKeypairbyId(Id);
            System.out.println("this is private " + keys.getPrivateKey() + "this is public " + keys.getPublicKey());
            return keys;
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/findall")
    public List<Object> findall() {
        try {
            return keyPairRespository.findall();
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

// Gen key-pair CRUD handling

    @PostMapping("/GenSave_keypair")
    public GenKeyPairResponse SaveGen(@RequestBody GenKeyPairResponse genKeyPairResponse) {
        try {
            return generalKeyPairRepository.save(genKeyPairResponse);
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/Gengetkeypair/{Id}")
    public GenKeyPairResponse findGenkeypair(@PathVariable String Id,Authentication authentication) throws ParseException, ExecutionException, InterruptedException {
        System.out.print("findGenkeypair:This is message to retrive the regenerated DH keypair based on ownerid of the one who has recieved public key of the peer");
        reGenKeyPairResponseCompletableFuture = new CompletableFuture<>();
//        try {
            GenKeyPairResponse keys_old = generalKeyPairRepository.findKeypairbyId(Id);
            // check the created at and generate a new key
            if(isKeyValid(keys_old.getCreatedat()))
            {
                System.out.println("this is private " + keys_old.getGen_private_Key() + "this is public " + keys_old.getGen_public_Key());
                return keys_old;
            }
            else
            {
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                Map<String, Object> claims = jwt.getClaims();
                String userId =  authentication.getName();
                String ownerid = (String) claims.get("Owner_ID"); // Adjust claim key if needed
                SenRecResponse senRecResponse= new SenRecResponse(ownerid,userId);
                kafkaloginTemplate.send("Save-reGenerate-keypair-topic", "Gen-key-pair", senRecResponse);
                System.out.println("Waiting for new DH Key pair regeneration message from Kafka...");
                // Wait for the Kafka listener to complete the future with the symmetric key
                ReGenKeyPairResponse reGenKeyPairResponse = reGenKeyPairResponseCompletableFuture.get();
                System.out.println("Generated DH Key pair: " + reGenKeyPairResponse.getGenerated());
                GenKeyPairResponse keys_new = generalKeyPairRepository.findKeypairbyId(Id);
                System.out.println("this is DH private_new " + keys_new.getGen_private_Key() + "this is DH public new " + keys_new.getGen_public_Key());
                return keys_new;

            }

//        } catch (Exception e) {
//            // Handle exception (e.g., log it)
//            e.printStackTrace();
//            return null;
//        }
    }

    @GetMapping("/GenDelkeypair/{Id}")
    public String DeleteGenkeypair(@PathVariable String Id) {
        try {
            String keys = generalKeyPairRepository.deletekeypair(Id);
            return "keys deleted successfully";
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/Genfindall")
    public List<Object> findGenall() {
        try {
            return generalKeyPairRepository.findall();
        } catch (Exception e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
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



    // User Info

    @GetMapping("/userId")
    public String userName(Authentication authentication) {

        try {
            if (!(authentication instanceof JwtAuthenticationToken)) {
                throw new SecurityException("Invalid authentication type");
            }

            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            Map<String, Object> claims = jwt.getClaims();

            System.out.println("claims"+claims);

            // Extract profile and email claims
            String email = (String) claims.get("email");
            String  userId =authentication.getName();
//            System.out.println("email"+ email);
            System.out.println("userId"+ userId);
            return userId;
        } catch (Exception e) {
            //log.error("Error retrieving user information from JWT:", e);
//            response.put("error", "Failed to retrieve user information");
            return "error faild to retrive userId";
        }
    }
    @GetMapping("/userName")
    public String getuserId( Authentication authentication) {

        try {
            if (!(authentication instanceof JwtAuthenticationToken)) {
                throw new SecurityException("Invalid authentication type");
            }
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            Map<String, Object> claims = jwt.getClaims();
//            System.out.println("claims"+claims);
            String username = (String) claims.get("name"); // Adjust claim key if needed
            System.out.println("username"+ username);
            return username;
        } catch (Exception e) {
            //log.error("Error retrieving user information from JWT:", e);
//            response.put("error", "Failed to retrieve user information");
            return "error faild to retrive User Name";
        }
    }

    @GetMapping("/OwnerID")
    public String Owner_ID(Authentication authentication) {
        try {
            if (!(authentication instanceof JwtAuthenticationToken)) {
                throw new SecurityException("Invalid authentication type");
            }

            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            Map<String, Object> claims = jwt.getClaims();
            String ownerid = (String) claims.get("Owner_ID"); // Adjust claim key if needed
            System.out.println("ownerid"+ ownerid);
            return ownerid;
        } catch (Exception e) {
            return "error faild to retrive ownerId";
        }
    }

    @GetMapping("/getstring")
    public String getstring()
    {
        return "this is example";
    }


}
