    // Function to display received messages
    function showMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = message;
        document.getElementById('messages').appendChild(messageElement);
    }

    function showMessage_private(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = message;
        document.getElementById('private-messages').appendChild(messageElement);
    }

    function showMessage_Encrypted_messages(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = message;
        document.getElementById('Encrypted-messages').appendChild(messageElement);
    }

    function handleSecretMessage(secretMessage) {
        secretmessage_new = secretMessage;
        console.log("secretmessage_new", secretmessage_new);
        // Here you can do whatever you want with the secret message


            const url = `/api/socket/getsharedkey?Recid=${encodeURIComponent(recId)}&SenderId=${encodeURIComponent(senderId)}&publicKey=${encodeURIComponent(publicKey)}`;
            const ownerId = document.getElementById('ownerId').innerText;

                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    showMessage('Secret message from getsharedkey: ' + secretMessage);
                     console.log("secretmessage_new from getsharedkey", secretmessage_new);
                })
                .catch(error => {
                    console.error('Error:', error);
                });



    }

        function handleSharedkey(secretMessage) {
        secretmessage_new = secretMessage;
        console.log("secretmessage_new in handlesharedkey", secretmessage_new);
        // Here you can do whatever you want with the secret message
            const publicKey = secretmessage_new.publicKey;
            console.log("publickey"+publicKey);

            const senderId = secretmessage_new.senderId;
            console.log("senderId"+senderId);
            const recId = secretmessage_new.recId;
            console.log("recId"+recId);
            const time = secretmessage_new.time;

            const url = `/api/socket/getsharedkey?Recid=${encodeURIComponent(recId)}&SenderId=${encodeURIComponent(senderId)}&publicKey=${encodeURIComponent(publicKey)}`;
            const ownerId = document.getElementById('ownerId').innerText;

                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    showMessage('Secret message from getsharedkey: ' + secretMessage);
                     console.log("secretmessage_new from getsharedkey", secretmessage_new);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
                }

          function  handlePublicKey(secretMessage)
          {
          const ownerId1=secretMessage.recId
          secretmessage_new = secretMessage;
           console.log("secretmessage_new in hadnlepublickey"+ownerId1+"is"+secretmessage_new);
           // Extract values from the message body
            const dh_publicKey = secretMessage.dh_Pubkey;
            console.log("dh_publicKey"+secretmessage_new.dh_Pubkey);
            const ca_Pubkey = secretMessage.ca_Pubkey;
            console.log("ca_Pubkey"+ca_Pubkey);
            const senderId = secretMessage.senderId;
             console.log("senderId"+senderId);
            const recId = secretMessage.recId;
             console.log("recId"+recId);
            const time = secretMessage.time;
             console.log("time"+time);

        // Here you can do whatever you want with the secret message

            const url = `/api/socket/rec_dh_pub_key?encryptedmessage=${encodeURIComponent(dh_publicKey)}&publickey=${encodeURIComponent(ca_Pubkey)}&sendid=${encodeURIComponent(senderId)}&peerid=${encodeURIComponent(recId)}`;
            const ownerId = document.getElementById('ownerId').innerText;

                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    showMessage('Secret message from handlepublic key: ' + secretMessage);
                     console.log("secretmessage_new from rec_dh_pub_key"+ secretMessage);
                     // Parse the JSON string into a JavaScript object
                const secretMessageObject = JSON.parse(secretmessage_new);

                // Access the publicKey property
                const publicKey = secretMessageObject.publicKey;
                console.log("publicKey"+publicKey);
                     handleSharedkey(secretMessageObject);

                })
                .catch(error => {
                    console.error('Error:', error);
                });

          }
          function  handlePublicKey_rec(secretMessage)
          {
          const ownerId1=secretMessage.recId
          secretmessage_new = secretMessage;
           console.log("secretmessage_new in hadnlepublickey"+ownerId1+"is"+secretmessage_new);
           // Extract values from the message body
            const dh_publicKey = secretMessage.dh_Pubkey;
            console.log("dh_publicKey"+secretmessage_new.dh_Pubkey);
            const ca_Pubkey = secretMessage.ca_Pubkey;
            console.log("ca_Pubkey"+ca_Pubkey);
            const senderId = secretMessage.senderId;
             console.log("senderId"+senderId);
            const recId = secretMessage.recId;
             console.log("recId"+recId);
            const time = secretMessage.time;
             console.log("time"+time);

        // Here you can do whatever you want with the secret message

            const url = `/api/socket/rec_dh_pub_key_rec?encryptedmessage=${encodeURIComponent(dh_publicKey)}&sendid=${encodeURIComponent(senderId)}&peerid=${encodeURIComponent(recId)}`;
            const ownerId = document.getElementById('ownerId').innerText;

                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    showMessage('Secret message from handlepublic key: ' + secretMessage);
                     console.log("secretmessage_new from rec_dh_pub_key"+ secretMessage);
                     // Parse the JSON string into a JavaScript object
                const secretMessageObject = JSON.parse(secretmessage_new);

                // Access the publicKey property
                const publicKey = secretMessageObject.publicKey;
                console.log("publicKey"+publicKey);
                     handleSharedkey(secretMessageObject);

                })
                .catch(error => {
                    console.error('Error:', error);
                });

          }



    // Connect to WebSocket server
    var secretmessage_new = "";
    const socket = new SockJS('/chat-websocket');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        // Check if headers are available
    if (frame && frame.headers) {
        const headers = frame.headers;
        console.log(headers);
        // Access individual headers
        console.log('User name:', headers['user-name']);
        console.log('Other header:', headers['ownerId']);
        // You can access other headers similarly
    }

        // Subscribe to the topic to receive messages
        stompClient.subscribe('/topic/chat', function (message) {

            console.log("message from topic "+message)
<!--            showMessage('Message received: ' + message.body);-->
            // Extract private key from the received message
            const privateKey = JSON.parse(message.body).text;



<!--            // Make AJAX request to server endpoint with private key-->
<!--            fetch('/api/socket/data?privatekey=' + encodeURIComponent(privateKey))-->

<!--                .then(response => {-->
<!--                    if (!response.ok) {-->
<!--                        throw new Error('Failed to fetch data');-->
<!--                    }-->
<!--                    return response.text();-->
<!--                })-->
<!--                .then(secretMessage => {-->
<!--                    showMessage('Secret message: ' + secretMessage);-->
<!--                })-->
<!--                .catch(error => {-->
<!--                    console.error('Error:', error);-->
<!--                });-->
        });

          // Subscribe to the topic to receive messages
        stompClient.subscribe('/topic/public-key', function (message) {

            console.log("message from topic "+message)
<!--            showMessage('Message received: ' + message.body);-->
            // Extract private key from the received message

             // Parse the JSON message body
             const messageBody = JSON.parse(message.body);


         // Extract values from the message body
            const publicKey = messageBody.publicKey;
            console.log("publickey"+publicKey);
            const senderId = messageBody.senderId;
            const recId = messageBody.recId;
            const time = messageBody.time;

            // Print the extracted values
<!--            console.log("Public Key1: " + encodeURIComponent(publicKey));-->
              console.log("senderId"+senderId);
              console.log("recId"+recId);

            // Make AJAX request to server endpoint with private key
            // Construct the URL with query parameters using backticks (`) and ${} syntax for interpolation

            const url = `/api/socket/getsharedkey?Recid=${encodeURIComponent(recId)}&SenderId=${encodeURIComponent(senderId)}&publicKey=${encodeURIComponent(publicKey)}`;
            const ownerId = document.getElementById('ownerId').innerText;
            if(ownerId===recId)
            {
                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    showMessage('Secret message: ' + secretMessage);
                    console.log("secretmessage_new"+secretmessage_new);
                      handleSecretMessage(secretMessage);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
            }





        });

        stompClient.subscribe('/topic/public_key_ca', function (message) {
            console.log("message from topic public_key_ca "+message)
            // Extract private key from the received message

            // Parse the JSON message body
             const messageBody = JSON.parse(message.body);

         // Extract values from the message body
            const dh_publicKey = messageBody.dh_Pubkey;
            console.log("dh_publicKey"+dh_publicKey);
            const ca_Pubkey = messageBody.ca_Pubkey;
            console.log("ca_Pubkey"+ca_Pubkey);
            const senderId = messageBody.senderId;
            const recId = messageBody.recId;
            const time = messageBody.time;

            // Print the extracted values
              console.log("senderId"+senderId);
              console.log("recId"+recId);
              console.log("this is public_key_ca_topic");
              if(ownerId===recId)
                {

                 handlePublicKey(messageBody);
                }

        });

             stompClient.subscribe('/topic/public_key_ca_rec', function (message) {
            console.log("message from topic public_key_ca "+message)
            // Extract private key from the received message

            // Parse the JSON message body
             const messageBody = JSON.parse(message.body);

         // Extract values from the message body
            const dh_publicKey = messageBody.dh_Pubkey;
            console.log("dh_publicKey"+dh_publicKey);
            const ca_Pubkey = messageBody.ca_Pubkey;
            console.log("ca_Pubkey"+ca_Pubkey);
            const senderId = messageBody.senderId;
            const recId = messageBody.recId;
            const time = messageBody.time;

            // Print the extracted values
              console.log("senderId"+senderId);
              console.log("recId"+recId);
              if(ownerId===recId)
                {
                    console.log("this is public_key_ca_rec");
                 handlePublicKey_rec(messageBody);
                }

        });


                  // Subscribe to the topic to receive messages
        stompClient.subscribe('/topic/peer-public-key', function (message) {
                    console.log("message from topic peer-publice "+message)
                    // Extract private key from the received message
                    const messageBody = JSON.parse(message.body);
                    // Extract values from the message body
                    const publicKey = messageBody.publicKey;
                    console.log("publickey"+publicKey);
                    const senderId = messageBody.senderId;
                    const recId = messageBody.recId;
                    const time = messageBody.time;

                    // Print the extracted values
                    console.log("senderId"+senderId);
                    console.log("recId"+recId);

                    // Make AJAX request to server endpoint with private key
                    // Construct the URL with query parameters using backticks (`) and ${} syntax for interpolation
            const url = `/api/socket/getkey_own_dh?Recid=${encodeURIComponent(senderId)}&publickey=${encodeURIComponent(publicKey)}`;
            const ownerId = document.getElementById('ownerId').innerText;
            if(ownerId===recId)
            {
                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
                    console.log("secretmessage_new in "+ownerId+"is"+secretmessage_new);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
            }

                    });

        console.log("secretmessage_new"+secretmessage_new);

        // subscribe to Receive Encrypted message
         // Subscribe to the topic to receive messages
        stompClient.subscribe('/topic/EncryptedMessage', function (message_enc) {

            console.log("message from topic "+message_enc)

            // Extract private key from the received message

             // Parse the JSON message body
             const messageBody_enc1 = JSON.parse(message_enc.body);


         // Extract values from the message body
             const SenderId_enc = messageBody_enc1.senderId;
             const RecId_enc = messageBody_enc1.recId;
            const message_enc1 = messageBody_enc1.message;
             console.log("Encrypted message"+message_enc1);
            const time_enc = messageBody_enc1.time;

            // Print the extracted values
              console.log("SenderId"+ SenderId_enc);
              console.log("RecId"+ RecId_enc);
               console.log("RecId"+ time_enc);
               var secretkeypass="Di3jyJYxTquIuVMvRrn6Ng==";
               if(ownerId===RecId_enc)
               {
                   showMessage_Encrypted_messages('Message received: ' + message_enc.body);
                   // Make AJAX request to server endpoint with private key
            // Construct the URL with query parameters using backticks (`) and ${} syntax for interpolation

            const url1 = `/api/socket/getMessageDecrypted?encryptedMessage=${encodeURIComponent(message_enc1)}&senderid=${encodeURIComponent(SenderId_enc)}
            &secretkey=${encodeURIComponent(secretkeypass)}&peerid=${encodeURIComponent(RecId_enc)}`;

             const url = `/api/socket/MessageDecrypted?encryptedMessage=${encodeURIComponent(message_enc1)}&senderid=${encodeURIComponent(SenderId_enc)}
            &secretkey=${encodeURIComponent(secretkeypass)}&peerid=${encodeURIComponent(RecId_enc)}`;
            const ownerId = document.getElementById('ownerId').innerText;

                 // Make AJAX request to server endpoint with private key
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch data');
                    }
                    return response.text();
                })
                .then(secretMessage => {
                secretmessage_new = secretMessage;
<!--                    showMessage('Secret message: ' + secretMessage);-->
                    showMessage_Encrypted_messages('This is the secret message'+secretMessage);
                    console.log("secretmessage_new"+secretmessage_new);
                })
                .catch(error => {
                    console.error('Error:', error);
                });

               }









        });


         var user1= "00002";
         var user2= "0001";
   stompClient.subscribe('/user1/specific/private-chat', function (message) {
            console.log("Subscribed to: " + message.headers.destination);
            console.log("User: " + message.headers.user); // Assuming 'user' is a header sent by the server
            console.log("Message from topic: " + message);
            showMessage_private('Message received: ' + message.body);
            // Extract private key from the received message
            const privateKey = JSON.parse(message.body).text;
            });

   stompClient.subscribe('/user2/specific/private-chat', function (message) {
            console.log("Subscribed to: " + message.headers.destination);
            console.log("User: " + message.headers.user); // Assuming 'user' is a header sent by the server
            console.log("Message from topic: " + message);
            showMessage_private('Message received: ' + message.body);
            // Extract private key from the received message
            const privateKey = JSON.parse(message.body).text;
            });

<!--     stompClient.subscribe('/topic/public-key', function (message) {-->
<!--            console.log("Subscribed to: " + message.headers.destination);-->
<!--            console.log("User: " + message.headers.user); // Assuming 'user' is a header sent by the server-->
<!--            console.log("Message from topic: " + message);-->
<!--            showMessage_private('Message received: ' + message.body);-->
<!--            // Extract private key from the received message-->
<!--            const privateKey = JSON.parse(message.body).text;-->
<!--            });-->




        // Send a message
    var message = {
        sender: 'kaleb',
        rec: '00002',
        text: 'MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIBAtCMdVcFMVgV/OUgq+4EJfqZmYBB+8akBVIaGddn2DdOFrgUqyL0FNSXPbPA+2TMg0t9RghE9/vVDmEKThjtABwkfcC+JOcJqwDAbsoEPMZgUz+RrnaSKiX/S8xVKV7Wj131lPMX72kn4aSgXnKQUXrbLuavH3KikVK5x7NoJAgMBAAECgYA2uWUjxpSc0jGyTsLmZFDEkoSUBALhhwkekA69CAqpYjAsHVJPqh3Vaa9v3r4hFPAgvNS9rU3OhaGQjbMeVUxkx9GVt2LUnGMX/M5yFh7OZl7cMtebhoeEt0z5SXI3NULiEKKjHgXwTc4DaA+lcp/gxSNDQNlSFJWhfvTUQDqosQJBAJ8nPN0eOesTaCCzATO3FpkcuhQ9XLhAQTvbzYb67gtK+4Vh8pBAEFLEuKZAmBidub63Lv50cLnm/1L7+4Y6+H8CQQDOS871vu+n914il1GBZ81IqbwKki3IWx/d6iXz1UlXyOEMgniNpQYKW8EWzvrT/lXDlTe3VKgpBfllpgIBySl3AkBzmfmglxr0wDTrQ3qFCOEWOAKFPwkBIFMB2qdP+yY696z4dmvNEWuJ4zBIOjT/9Fj9yWsOEp/quHoO2c8Z8e2bAkB05i5bwRuq8ZjNPzP3gWupXk1pLBZ3b3OqW7Gv70/FR9aHMTPBCB9ZJU9Qbm9iS8AruVW+NGGqBXGisSR4AJbXAkAjPxbNZsSfeJZY/AkU2SNbIvHnLACQvRwJwibvzAkDfJ4t1gVohi+enBO91slbMT+tQGD9qnLxDJpso4B3rmSG'
         };
         console.log("this is message"+message);
         console.log("this is message"+message);

                 // Send a message
    var message1 = {
        sender: 'kaleb',
        rec: '00001',
        text: 'MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIBAtCMdVcFMVgV/OUgq+4EJfqZmYBB+8akBVIaGddn2DdOFrgUqyL0FNSXPbPA+2TMg0t9RghE9/vVDmEKThjtABwkfcC+JOcJqwDAbsoEPMZgUz+RrnaSKiX/S8xVKV7Wj131lPMX72kn4aSgXnKQUXrbLuavH3KikVK5x7NoJAgMBAAECgYA2uWUjxpSc0jGyTsLmZFDEkoSUBALhhwkekA69CAqpYjAsHVJPqh3Vaa9v3r4hFPAgvNS9rU3OhaGQjbMeVUxkx9GVt2LUnGMX/M5yFh7OZl7cMtebhoeEt0z5SXI3NULiEKKjHgXwTc4DaA+lcp/gxSNDQNlSFJWhfvTUQDqosQJBAJ8nPN0eOesTaCCzATO3FpkcuhQ9XLhAQTvbzYb67gtK+4Vh8pBAEFLEuKZAmBidub63Lv50cLnm/1L7+4Y6+H8CQQDOS871vu+n914il1GBZ81IqbwKki3IWx/d6iXz1UlXyOEMgniNpQYKW8EWzvrT/lXDlTe3VKgpBfllpgIBySl3AkBzmfmglxr0wDTrQ3qFCOEWOAKFPwkBIFMB2qdP+yY696z4dmvNEWuJ4zBIOjT/9Fj9yWsOEp/quHoO2c8Z8e2bAkB05i5bwRuq8ZjNPzP3gWupXk1pLBZ3b3OqW7Gv70/FR9aHMTPBCB9ZJU9Qbm9iS8AruVW+NGGqBXGisSR4AJbXAkAjPxbNZsSfeJZY/AkU2SNbIvHnLACQvRwJwibvzAkDfJ4t1gVohi+enBO91slbMT+tQGD9qnLxDJpso4B3rmSG'
         };
         console.log("this is message"+message1);
         console.log("this is message"+message1);

    var message3 = {
        senderId: 'kaleb',
        publicKey: 'MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIBAtCMdVcFMVgV/OUgq+4EJfqZmYBB+8akBVIaGddn2DdOFrgUqyL0FNSXPbPA+2TMg0t9RghE9/vVDmEKThjtABwkfcC+JOcJqwDAbsoEPMZgUz+RrnaSKiX/S8xVKV7Wj131lPMX72kn4aSgXnKQUXrbLuavH3KikVK5x7NoJAgMBAAECgYA2uWUjxpSc0jGyTsLmZFDEkoSUBALhhwkekA69CAqpYjAsHVJPqh3Vaa9v3r4hFPAgvNS9rU3OhaGQjbMeVUxkx9GVt2LUnGMX/M5yFh7OZl7cMtebhoeEt0z5SXI3NULiEKKjHgXwTc4DaA+lcp/gxSNDQNlSFJWhfvTUQDqosQJBAJ8nPN0eOesTaCCzATO3FpkcuhQ9XLhAQTvbzYb67gtK+4Vh8pBAEFLEuKZAmBidub63Lv50cLnm/1L7+4Y6+H8CQQDOS871vu+n914il1GBZ81IqbwKki3IWx/d6iXz1UlXyOEMgniNpQYKW8EWzvrT/lXDlTe3VKgpBfllpgIBySl3AkBzmfmglxr0wDTrQ3qFCOEWOAKFPwkBIFMB2qdP+yY696z4dmvNEWuJ4zBIOjT/9Fj9yWsOEp/quHoO2c8Z8e2bAkB05i5bwRuq8ZjNPzP3gWupXk1pLBZ3b3OqW7Gv70/FR9aHMTPBCB9ZJU9Qbm9iS8AruVW+NGGqBXGisSR4AJbXAkAjPxbNZsSfeJZY/AkU2SNbIvHnLACQvRwJwibvzAkDfJ4t1gVohi+enBO91slbMT+tQGD9qnLxDJpso4B3rmSG',
        recId: '00001'
         };

    stompClient.send('/chat-app/api/socket/chat', {}, JSON.stringify(message));
        // Get the username from a hidden input field injected by the server-side code
        const ownerId = document.getElementById('ownerId').innerText;
        // Now you can use the username in your JavaScript code
        console.log("Logged in ownerId:", ownerId);

<!--    stompClient.send('/chat-app/api/socket/private-chat', {}, JSON.stringify(message1));-->
<!--        // Get the username from a hidden input field injected by the server-side code-->
<!--        const ownerId1 = document.getElementById('ownerId').innerText;-->
<!--        // Now you can use the username in your JavaScript code-->
<!--        console.log("Logged in ownerId_private:", ownerId1);-->

<!--    stompClient.send('/chat-app/api/socket/public-key', {}, JSON.stringify(message3));-->
<!--        // Get the username from a hidden input field injected by the server-side code-->
<!--        const ownerId2 = document.getElementById('ownerId').innerText;-->
<!--        // Now you can use the username in your JavaScript code-->
<!--        console.log("Logged in ownerId:", ownerId1);-->
    });


   document.getElementById('encryptionForm').addEventListener('submit', function(event) {
        event.preventDefault();
        const encryptedMessage = document.getElementById('encryptedMessage').value;
        const senderid = document.getElementById('senderid').value;
        const secretkey = document.getElementById('secretkey').value;
        const peerid = document.getElementById('peerid').value;

        // Construct the URL with parameters included in the query string
        const url = `http://localhost:8989/api/socket/Encrypt?encryptedMessage=${encodeURIComponent(encryptedMessage)}&senderid=${encodeURIComponent(senderid)}&secretkey=${encodeURIComponent(secretkey)}&peerid=${encodeURIComponent(peerid)}`;

        // Make a GET request to the server
        fetch(url)
          .then(response => response.json()) // Parsing the JSON response body
          .then(data => {
              console.log('Success:', data);
              alert('Encryption Successful!'); // Alerting the user upon successful operation
          })
          .catch((error) => {
              console.error('Error:', error); // Handling errors in the request
          });
    });