import React, {useEffect, useState} from 'react';
import './App.css';
import {webSocket} from "rxjs/webSocket";
import Button from '@material-ui/core/Button';

function App() {
  const [wss] = useState(webSocket('ws://localhost:8080/chat'))

  useEffect(() => {
      wss.subscribe(message => console.log(message))
  }, [wss])

  return (
    <div className="App">
      <Button variant="contained" color="primary" onClick={() => {
        fetch('http://localhost:8080/chat?hashtags=#...,#...,#...')
          .then((response) => console.log(response)) 
        wss.next("...")
        }
      }>
        Let's hope this goes they way I'd like it to
      </Button>
    </div>
  );
}

export default App;
