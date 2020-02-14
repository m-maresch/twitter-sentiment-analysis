import React, { useState, useEffect } from 'react';
import './App.css';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import { styles } from "./Props";
import { withStyles } from '@material-ui/core/styles';
import { Client } from '@stomp/stompjs';

// Set up for testing whether the basic dataflow is working correctly
// Integration tests follow after the MVP is done
const App = (props) => {
  const [hashtags, setHashtags] = useState('');

  const [results, setResults] = useState([]);

  const { classes } = props;

  const client = new Client({
    brokerURL: "ws://localhost:8080/socket",
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
  });

  const [ws] = useState(client)

  useEffect(() => {
    ws.onConnect = (frame) => ws.subscribe("/analysed-sentiment", (message) => setResults(res  => [...res, message.body]))
    
    ws.activate();
  }, [ws]);   

  const sendHashtags = (event) => {
    fetch('http://localhost:8080/api/sentiment?hashtags=' + hashtags, 
    { 
      method: 'POST', 
      headers: {
        'Content-Type': 'application/json'
      }
    })
    .then((response) => {
      ws.publish({destination: '/api/send/hashtags', body: hashtags.split(',').join('')});
    })

    event.preventDefault();
  }

  return (
    <div className="App">
      <form className={classes.container} noValidate onSubmit={sendHashtags}>
          <TextField
              id="hashtags"
              className={classes.textField}
              label="Enter Twitter #'s"
              value={hashtags}
              onChange={(e) => setHashtags(e.target.value)}
              margin="normal"
          />
          <Button className={classes.button} variant="contained" color="primary" type="submit">
              Analyse
          </Button>
      </form>      
      <List component="nav" aria-label="main mailbox folders">      
      {        
        results.map((res, i) => {
          return (
            <ListItem button key={i}>
              <ListItemText primary={res} />
            </ListItem>
          )
        })
      }
      </List>
    </div>
  );
}

export default withStyles(styles)(App);
