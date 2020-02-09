import React, {useEffect, useState} from 'react';
import './App.css';
import {webSocket} from "rxjs/webSocket";
import Button from '@material-ui/core/Button';

// Set up for testing whether the basic dataflow is working correctly
// Integration tests follow after the MVP is done (in the Backend application)
function App() {
  const [wss] = useState(webSocket({url: 'ws://localhost:8080/sentiment', deserializer: msg => msg.data}))

  useEffect(() => {
      wss.subscribe(message => console.log(message))
  }, [wss])

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
            wss.next(hashtags.split(',').join(''))
          }) 
        }
      }>
        Test
      </Button>
    </div>
  );
}

export default App;
