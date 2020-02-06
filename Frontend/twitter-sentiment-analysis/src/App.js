import React, {useEffect, useState} from 'react';
import './App.css';
import {webSocket} from "rxjs/webSocket";
import Button from '@material-ui/core/Button';

// Set up for testing whether the basic dataflow is working correctly
// Integration tests follow after the MVP is done
function App() {
  const [wss] = useState(webSocket('ws://localhost:8080/sentiment'))

  useEffect(() => {
      wss.subscribe(message => console.log(message))
  }, [wss])

  return (
    <div className="App">
      <Button variant="contained" color="primary" onClick={() => {
        fetch('http://localhost:8080/api/sentiment?hashtags=#...,#...,#...', 
          { 
            method: 'POST', 
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then((response) => console.log(response)) 
        wss.next("...")
        }
      }>
        Test
      </Button>
    </div>
  );
}

export default App;
