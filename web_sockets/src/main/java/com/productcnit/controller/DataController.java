package com.productcnit.controller;

import com.productcnit.dto.*;
import com.productcnit.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/socket")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DataController {

    @Autowired
    private final WebSocketService webSocketService;
    private final KafkaTemplate<String, String> kafkasharedkeyTemplate;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/data")
    public ResponseEntity<String> getData(@RequestParam("privatekey") String privateKey) {
        String secretMessage = webSocketService.getData(privateKey);
        if (secretMessage != null) {
            return ResponseEntity.ok(secretMessage);
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch data");
        }
    }

    @GetMapping("/getkeypair")
    public ResponseEntity<GenKeyPairResponse> getkeypair(String Recid, Authentication authentication) {
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        GenKeyPairResponse secretMessage = webSocketService.getkeypair(authentication,Ownerid);
        PublicKeyMessageSend ResMessage= new PublicKeyMessageSend();
        ResMessage.setPublicKey(secretMessage.getGen_public_Key());
        ResMessage.setSenderId(secretMessage.getGen_Owner_Id());
        ResMessage.setRecId(Recid);
        webSocketService.SendPublicKey(ResMessage);
        if (secretMessage != null) {
            return ResponseEntity.ok(secretMessage);
        } else {
            System.out.println("error in getkeypair");
            return null;
        }
    }

    @GetMapping("/getkey_own_dh")
    public ResponseEntity<GenKeyPairResponse> getkey_own_dh(String Recid, String publickey, Authentication authentication) {
        System.out.println("getkey_own_dh:This is the fifth message to retrive the DH keypairs");
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        WebClient webClient2 = webClientBuilder.build();
        WebClient webClient1= WebClient.create();
        GenKeyPairResponse secretMessage = webSocketService.getkeypair(authentication,Ownerid);
        String Ownpubkey_ca= webSocketService.getUser_ca();
        System.out.println("getkey_own_dh:This is the 8th message to retrive the public key after retriving the DH keypair and preparing to send back to user who sent the publickey");
        String publickey_peer= publickey;
        String response = webClient2.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("ENCKEY-SERVICE")
                            .path("/api/enckey/getenc_sig_peer")
                            .queryParam("message", URLEncoder.encode(secretMessage.getGen_public_Key(), StandardCharsets.UTF_8))
                            .queryParam("publickey_peer", URLEncoder.encode(publickey_peer, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("getkey_own_dh: this is the 10th message form getenc_sig_peer concatenation of symmetric key signed " +
                "by private key of sender and encrypted by public key of peer togehter with the DH publci key encrypted with symmetric key"+response);

        Peer_PublicKeyMessageSend ResMessage= new Peer_PublicKeyMessageSend();
        ResMessage.setSenderId(secretMessage.getGen_Owner_Id());
        ResMessage.setDh_Pubkey(response);
        ResMessage.setCa_Pubkey(Ownpubkey_ca);
        ResMessage.setRecId(Recid);
        webSocketService.SendPublicKey_ca_peer(ResMessage);
        if (secretMessage != null) {
            return ResponseEntity.ok(secretMessage);
        } else {
            System.out.println("error in getkeypair");
            return null;
        }
    }
    @GetMapping("/rec_dh_pub_key")
    public PublicKeyMessage rec_dh_pub_key(@RequestParam("encryptedmessage") String encryptedmessage,@RequestParam("publickey") String publickey,
                                 @RequestParam("sendid") String sendid,@RequestParam("peerid") String peerid,Authentication authentication)
    {
         System.out.println("rec_dh_pub_key:This is the 12th message we have public key of the " +
                 "message receiver of the initial message  and signature of the symmetric key used to ecnrypt the dh of the initial message receiver ");
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        WebClient webClient2 = webClientBuilder.build();
        WebClient webClient1= WebClient.create();
        String response = webClient2.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("DECKEY-SERVICE")
                            .path("/api/deckey/get_enc_sig_verif_pubkey")
                            .queryParam("encryptedmessage", URLEncoder.encode(encryptedmessage, StandardCharsets.UTF_8))
                            .queryParam("publickey", URLEncoder.encode(publickey, StandardCharsets.UTF_8))
                            .queryParam("sendid", URLEncoder.encode(sendid, StandardCharsets.UTF_8))
                            .queryParam("peerid", URLEncoder.encode(peerid, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        String[] data= response.split("_.._");
        String dh_pub_key=data[0];
        String pub_key_ca=data[1];
        System.out.println("14th message:rec_dh_pub_key:dh_pub_key of the peer message reciever"+dh_pub_key);
        System.out.println("rec_dh_pub_key:pub_key_ca of the peer "+pub_key_ca);
        System.out.println("rec_dh_pub_key:decrypted key is"+response);

        GenKeyPairResponse secretMessage = webSocketService.getkeypair(authentication,Ownerid);
        System.out.println("rec_dh_pub_key:This is the 14th message retrival of the DH key public key of the message owner to send back to peer to be signed by privatekey: " +
                "encrypted by symmetric key: and again the symmetric key encrypted by public key of the peer which is pub_key_ca ");
        String publickey_peer= pub_key_ca;
        String response1 = webClient2.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("ENCKEY-SERVICE")
                            .path("/api/enckey/getenc_sig_peer")
                            .queryParam("message", URLEncoder.encode(secretMessage.getGen_public_Key(), StandardCharsets.UTF_8))
                            .queryParam("publickey_peer", URLEncoder.encode(publickey_peer, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println("rec_dh_pub_key: this is 15th message: symkey  encrypted with public key of the peer and signed by private key of the owener: concatenated with dh publc key of owner  encrypted by symm key  getenc_sig_peer"+response1);

        Peer_PublicKeyMessageSend ResMessage= new Peer_PublicKeyMessageSend();
        ResMessage.setSenderId(secretMessage.getGen_Owner_Id());
        ResMessage.setDh_Pubkey(response1);
        ResMessage.setCa_Pubkey(pub_key_ca);
        ResMessage.setRecId(sendid);
        webSocketService.SendPublicKey_ca_peer_rec(ResMessage);
        System.out.println("this is the 16 th message concatendated message with dh to be decrypted sent to peer to topic public_key_ca_rec");

        PublicKeyMessage outMessage= new PublicKeyMessage();
        outMessage.setPublicKey(dh_pub_key);
        outMessage.setSenderId(sendid);
        outMessage.setRecId(peerid);
        outMessage.setTime(new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date()));
        return outMessage;
    }
    @GetMapping("/rec_dh_pub_key_rec")
    public PublicKeyMessage rec_dh_pub_key_rec(@RequestParam("encryptedmessage") String encryptedmessage,
                                           @RequestParam("sendid") String sendid,@RequestParam("peerid") String peerid,Authentication authentication)
    {
        System.out.println("This is the 21th message cocnatendated dh of owner to be sent for decryption by verifying signature");
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        WebClient webClient2 = webClientBuilder.build();
        WebClient webClient1= WebClient.create();
        String response = webClient2.get()
                .uri(builder -> {
                    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                            .scheme("http")
                            .host("DECKEY-SERVICE")
                            .path("/api/deckey/get_enc_sig_verif_pubkey_rec")
                            .queryParam("encryptedmessage", URLEncoder.encode(encryptedmessage, StandardCharsets.UTF_8))
                            .queryParam("sendid", URLEncoder.encode(sendid, StandardCharsets.UTF_8))
                            .queryParam("peerid", URLEncoder.encode(peerid, StandardCharsets.UTF_8));
                    return uriBuilder.build().toUri();
                })
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println("decrypted dh key is"+response);
        String kafkasendid= sendid;
        kafkasharedkeyTemplate.send("key-generated",kafkasendid);
        PublicKeyMessage outMessage= new PublicKeyMessage();
        outMessage.setPublicKey(response);
        outMessage.setSenderId(sendid);
        outMessage.setRecId(peerid);
        outMessage.setTime(new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date()));
        return outMessage;
    }



    @GetMapping("/send_pub_own_ca")
    public String get_pub_own_ca(String Recid, Authentication authentication) {
        System.out.println("get_pub_own_ca:This is Second message from get_pub_own_ca method to retrive initial sender public key");
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        Own_PublicKeyMessageSend ResMessage= new Own_PublicKeyMessageSend();
        String publickey= webSocketService.getUser_ca();
        ResMessage.setEnc_Sig_Own_Pubkey(publickey);
        ResMessage.setSenderId(Ownerid);
        ResMessage.setRecId(Recid);
        webSocketService.SendPublicKey_ca(ResMessage);
        return "Successfully sent public key";
    }

    @GetMapping("/getsharedkey")
    public ResponseEntity<String> Sharedkey(@RequestParam("Recid") String Recid,@RequestParam("SenderId") String SenderId, @RequestParam("publicKey") String publicKey, Authentication authentication)
     {
         String secretMessage = webSocketService.Sharedkey(Recid,SenderId,publicKey,authentication);
         if (secretMessage != null) {
             return ResponseEntity.ok(secretMessage);
         } else {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch data");
         }

     }
    @PostMapping("/SendPubKey")
    public void SendPublicKey(@RequestBody final PublicKeyMessageSend publicKeyMessage)
    {
        webSocketService.SendPublicKey(publicKeyMessage);
    }

    @PostMapping("/SendPubKey_ca")
    public void SendPublicKey_ca(@RequestBody final Own_PublicKeyMessageSend publicKeyMessage)
    {
        webSocketService.SendPublicKey_ca(publicKeyMessage);
    }

    @PostMapping("/SendEncMessage")
    public void SendEncMessage(@RequestBody final EncMessageResponse encMessageResponse)
    {
        webSocketService.SendEncMessage(encMessageResponse);
    }

    @GetMapping("/getDecryptmessage")
    public DecMessage getDecrypt(@RequestParam("encryptedMessage") String encryptedMessage,@RequestParam("senderid") String senderid,
                                 @RequestParam("secretkey") String secretkey, @RequestParam("peerid") String peerid,Authentication authentication)

    {
        String SecretKey = secretkey;
        return webSocketService.getDecrypt(encryptedMessage,senderid,peerid,secretkey,authentication);
    }

    @GetMapping("/getMessageDecrypted")
    public DecMessage getMessageDecrypted(@RequestParam("encryptedMessage") String encryptedMessage,@RequestParam("senderid") String senderid,
                                 @RequestParam("secretkey") String secretkey, @RequestParam("peerid") String peerid)

    {
        System.out.println("encryptedMessage to be decrypted in websocket"+encryptedMessage);
        String SecretKey = secretkey;
//        String encryptedMessage1 = URLDecoder.decode(encryptedMessage, StandardCharsets.UTF_8);
        System.out.println("encryptedMessage1 to be decrypted in websocket"+encryptedMessage);
        return webSocketService.getMessageDecrypted(encryptedMessage,senderid,peerid,secretkey);
    }

    @GetMapping("/MessageDecrypted")
    public DecMessage MessageDecrypted(@RequestParam("encryptedMessage") String encryptedMessage,@RequestParam("senderid") String senderid,
                                          @RequestParam("secretkey") String secretkey, @RequestParam("peerid") String peerid,Authentication authentication)
    {
        System.out.println("encryptedMessage to be decrypted in websocket"+encryptedMessage);
        String SecretKey = secretkey;
//        String encryptedMessage1 = URLDecoder.decode(encryptedMessage, StandardCharsets.UTF_8);
        System.out.println("encryptedMessage1 to be decrypted in websocket"+encryptedMessage);
        return webSocketService.MessageDecrypted(encryptedMessage,senderid,peerid,secretkey,authentication);
    }
    @GetMapping("/kafkalistener")
    @KafkaListener(topics = "key-generated", groupId = "group-id4")
    public String kafkamessaefromsenderid(String sendid)
    {
        System.out.println("this is from kafka"+sendid);
        return "this is kafka listner";
    }

    @GetMapping("/Encrypt")
    @KafkaListener(topics = "key-generated", groupId = "group-id4")
    public EncMessageResponse GetEncrypt(Authentication authentication, @RequestParam(value= "encryptedMessage",required = false) String encryptedMessage,
                                         @RequestParam(value = "peerid",required = false) String peerid,
                                         @RequestParam(value = "kafkasendid",required = false) String kafkasendid)
    {
        System.out.println("GetEncrypt:This is First message");
        get_pub_own_ca(peerid,authentication);
        System.out.println("message to be encrypted"+encryptedMessage);
//        String encryptedMessage1 = URLDecoder.decode(encryptedMessage, StandardCharsets.UTF_8);
        System.out.println("message to be encrypted1"+encryptedMessage);
        System.out.println("this is kafkasendid"+kafkasendid);
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        Map<String, Object> claims = jwt.getClaims();
        String Ownerid = (String) claims.get("Owner_ID");
        System.out.println("OwnerId"+Ownerid);
        return webSocketService.getEncrypt(encryptedMessage,Ownerid,peerid,authentication);

    }





}
