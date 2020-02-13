import React, {useState} from 'react';
import './App.css';
import Button from '@material-ui/core/Button';
import {Client} from '@stomp/stompjs';

// Set up for testing whether the basic dataflow is working correctly
// Integration tests follow after the MVP is done
function App() {
  const client = new Client({
    brokerURL: "ws://localhost:8080/socket",
    debug: function (str) {
      console.log(str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
  });
  
  client.onConnect = function(frame) {
    client.subscribe("/analysed-sentiment", (message) => console.log(message.body))
  };
  
  client.onStompError = function (frame) {
    console.log('Broker reported error: ' + frame.headers['message']);
    console.log('Additional details: ' + frame.body);
  };
  
  client.activate();
  
  const [ws] = useState(client)

  const hashtags = "test,test1,test2"

  return (
    <div className="App">
      <Button variant="contained" color="primary" onClick={() => {
        fetch('http://localhost:8080/api/sentiment?hashtags=' + hashtags, 
          { 
            method: 'POST', 
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then((response) => {
            console.log(response)
            ws.publish({destination: '/api/send/hashtags', body: hashtags.split(',').join('')});
          }) 
        }
      }>
        Test
      </Button>
    </div>
  );
}

export default App;
